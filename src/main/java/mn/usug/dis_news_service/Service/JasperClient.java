package mn.usug.dis_news_service.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * jasper-service (JasperReports REST)-тэй ярих цорын ганц газар.
 *
 * Урсгал:
 *   1. POST {base}/api/v1/auth/token {clientKey, clientSecret} → accessToken (кэшлэнэ)
 *   2. GET  {base}/api/v1/templates → [{id, code, status, updatedAt}] (code→id кэш)
 *   3. POST {base}/api/v1/render/final/sync {templateId, outputFormat:PDF, data} → PDF байт
 *
 * ЧУХАЛ: Загварыг ҮРГЭЛЖ КОД-оор дууд (id нь дахин import бүрт өөрчлөгдөнө).
 */
@Component
@Slf4j
public class JasperClient {

    @Value("${jasper.base-url:http://172.16.0.101:8082}")
    private String baseUrl;

    @Value("${jasper.client-key:dis-news-service}")
    private String clientKey;

    @Value("${jasper.client-secret:}")
    private String clientSecret;

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final ObjectMapper om = new ObjectMapper();

    private volatile String token;
    private volatile long tokenExpiresAt = 0L;              // epoch millis
    private final Map<String, Integer> codeIdCache = new ConcurrentHashMap<>();

    // ── Нийтийн API ───────────────────────────────────────────────────────────────

    /** Загварыг КОД-оор рендерлэж PDF байт буцаана. Код олдохгүй бол кэш цэвэрлээд 1 удаа дахин оролдоно. */
    public byte[] renderPdfByCode(String code, Map<String, Object> data) {
        try {
            return render(resolveId(code, false), data);
        } catch (ResponseStatusException e) {
            // Загвар устсан/дахин import хийгдсэн байж магадгүй — кэш цэвэрлээд дахин
            if (e.getStatusCode().value() == 404 || String.valueOf(e.getReason()).contains("TEMPLATE_NOT_FOUND")) {
                codeIdCache.remove(code);
                return render(resolveId(code, true), data);
            }
            throw e;
        }
    }

    // ── Токен ─────────────────────────────────────────────────────────────────────

    private synchronized String token() {
        if (token != null && System.currentTimeMillis() < tokenExpiresAt - 10_000) return token;
        try {
            String body = om.writeValueAsString(Map.of("clientKey", clientKey, "clientSecret", clientSecret));
            HttpResponse<String> res = http.send(
                    HttpRequest.newBuilder(URI.create(baseUrl + "/api/v1/auth/token"))
                            .timeout(Duration.ofSeconds(15))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                            .build(),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (res.statusCode() / 100 != 2)
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "Jasper токен авахад алдаа гарлаа (" + res.statusCode() + "): " + res.body());
            JsonNode n = om.readTree(res.body());
            String t = n.hasNonNull("accessToken") ? n.get("accessToken").asText()
                     : n.hasNonNull("token") ? n.get("token").asText() : null;
            if (t == null || t.isBlank())
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Jasper токен хоосон ирлээ");
            long ttlSec = n.hasNonNull("expiresIn") ? n.get("expiresIn").asLong() : 1800L;
            token = t;
            tokenExpiresAt = System.currentTimeMillis() + ttlSec * 1000L;
            return token;
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Jasper сервертэй холбогдож чадсангүй: " + e.getMessage());
        }
    }

    // ── Код → id ───────────────────────────────────────────────────────────────────

    private Integer resolveId(String code, boolean forceReload) {
        if (!forceReload) {
            Integer cached = codeIdCache.get(code);
            if (cached != null) return cached;
        }
        Integer id = fetchIdByCode(code);
        if (id == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Тайлангийн загвар олдсонгүй (код: " + code + "). Jasper-т уг кодоор import хийнэ үү.");
        codeIdCache.put(code, id);
        return id;
    }

    /** Идэвхгүйг алгасаж, ижил кодтойгоос хамгийн сүүлийн updatedAt-тайг сонгоно. */
    private Integer fetchIdByCode(String code) {
        try {
            HttpResponse<String> res = http.send(
                    HttpRequest.newBuilder(URI.create(baseUrl + "/api/v1/templates"))
                            .timeout(Duration.ofSeconds(15))
                            .header("Authorization", "Bearer " + token())
                            .GET().build(),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (res.statusCode() / 100 != 2)
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "Jasper загварын жагсаалт авахад алдаа (" + res.statusCode() + "): " + res.body());
            JsonNode arr = om.readTree(res.body());
            if (arr.isObject() && arr.has("data")) arr = arr.get("data");   // боож өгдөг хувилбар
            Integer bestId = null; String bestUpdated = "";
            for (JsonNode t : arr) {
                if (!code.equals(t.path("code").asText(null))) continue;
                String status = t.path("status").asText("");
                if (!status.isBlank() && status.equalsIgnoreCase("INACTIVE")) continue;
                String updated = t.path("updatedAt").asText("");
                if (bestId == null || updated.compareTo(bestUpdated) >= 0) {
                    bestId = t.path("id").asInt();
                    bestUpdated = updated;
                }
            }
            return bestId;
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Jasper загвар хайхад алдаа: " + e.getMessage());
        }
    }

    // ── Рендер ──────────────────────────────────────────────────────────────────────

    private byte[] render(Integer templateId, Map<String, Object> data) {
        try {
            String body = om.writeValueAsString(Map.of(
                    "templateId", templateId,
                    "outputFormat", "PDF",
                    "data", data));
            HttpResponse<byte[]> res = http.send(
                    HttpRequest.newBuilder(URI.create(baseUrl + "/api/v1/render/final/sync"))
                            .timeout(Duration.ofSeconds(45))
                            .header("Authorization", "Bearer " + token())
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                            .build(),
                    HttpResponse.BodyHandlers.ofByteArray());
            if (res.statusCode() == 404)
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "TEMPLATE_NOT_FOUND");
            if (res.statusCode() / 100 != 2) {
                String msg = new String(res.body(), StandardCharsets.UTF_8);
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "Тайлан үүсгэхэд алдаа гарлаа (" + res.statusCode() + "): " + msg);
            }
            return res.body();
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Тайлан рендерлэхэд алдаа: " + e.getMessage());
        }
    }

    /** Null-ийг "" болгож буцаана (data map-д null бүү дамжуул). */
    public static String nz(String s) { return s == null ? "" : s; }

    @SuppressWarnings("unused")
    private static List<?> asList(Object o) { return (o instanceof List<?> l) ? l : List.of(); }
}
