package mn.usug.dis_news_service.Config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Бүх REST хариултын алдааг нэг стандарт бүтэцтэй болгоно.
 *
 * Шалтгаан: Spring Boot-ийн анхдагч тохиргоо (server.error.include-message=never) нь
 * ResponseStatusException-ий `reason` текстийг хариултаас хасдаг тул frontend талд
 * зөвхөн "Алдаа гарлаа" гэсэн тодорхойгүй мэдэгдэл харагддаг байсан.
 *
 * Хариултын бүтэц:
 * <pre>
 * { "timestamp": "...", "status": 403, "error": "Forbidden",
 *   "message": "Шуурхайн нарийн бичгийн эрх шаардлагатай", "path": "/api/ref/briefing/save" }
 * </pre>
 *
 * Урьдчилан бодож бичсэн (санаатай) алдааны текстийг хэрэглэгчид шууд харуулна.
 * Гэнэтийн (500) алдааны дотоод мэдээллийг задлахгүй — оронд нь log-той тааруулах
 * богино `traceId` буцаана.
 */
@RestControllerAdvice
@Slf4j
public class ApiExceptionHandler {

    /** Санаатай шидсэн статустай алдаа — reason текстийг хэвээр нь дамжуулна */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException ex,
                                                                   HttpServletRequest req) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = (ex.getReason() != null && !ex.getReason().isBlank())
                ? ex.getReason() : defaultMessage(status);
        if (status.is5xxServerError()) {
            log.error("ResponseStatusException {} {}: {}", status.value(), req.getRequestURI(), message, ex);
        }
        return build(status, message, req, null);
    }

    /** Бизнес дүрмийн шалгалт (ж: "Зөвхөн хүлээгдэж буй захиалгыг засах боломжтой") */
    @ExceptionHandler({ IllegalStateException.class, IllegalArgumentException.class })
    public ResponseEntity<Map<String, Object>> handleIllegal(RuntimeException ex, HttpServletRequest req) {
        String message = (ex.getMessage() != null && !ex.getMessage().isBlank())
                ? ex.getMessage() : "Илгээсэн өгөгдөл буруу байна";
        return build(HttpStatus.BAD_REQUEST, message, req, null);
    }

    /** Spring Security-ийн хандах эрхийн алдаа */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex,
                                                                 HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, "Танд энэ үйлдлийг хийх эрх алга", req, null);
    }

    /** Буруу/дутуу параметр, задлах боломжгүй JSON */
    @ExceptionHandler({ MethodArgumentTypeMismatchException.class,
                        MissingServletRequestParameterException.class,
                        HttpMessageNotReadableException.class,
                        MethodArgumentNotValidException.class })
    public ResponseEntity<Map<String, Object>> handleBadRequest(Exception ex, HttpServletRequest req) {
        log.warn("Bad request {}: {}", req.getRequestURI(), ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "Илгээсэн өгөгдөл дутуу эсвэл буруу байна", req, null);
    }

    /** Давхардсан утга, гадаад түлхүүр зөрчил, NOT NULL зөрчил */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleIntegrity(DataIntegrityViolationException ex,
                                                               HttpServletRequest req) {
        String traceId = newTraceId();
        log.error("Data integrity violation [{}] {}", traceId, req.getRequestURI(), ex);
        // Зөрчлийн шалтгааныг (багана/түлхүүрийн нэр) хэлж өгснөөр админ шууд засах боломжтой
        return build(HttpStatus.CONFLICT, integrityMessage(ex), req, traceId);
    }

    /** SQL-ийн зөрчлийн мессежийг хүнд ойлгомжтой болгоно (багана/түлхүүрийн нэрийг хадгална) */
    private String integrityMessage(DataIntegrityViolationException ex) {
        Throwable cause = ex.getMostSpecificCause();
        String raw = cause != null && cause.getMessage() != null ? cause.getMessage().trim() : "";
        if (raw.isEmpty())
            return "Өгөгдлийн бүрэн бүтэн байдал зөрчигдлөө — давхардсан эсвэл дутуу утга байна";

        java.util.regex.Matcher m;
        if ((m = java.util.regex.Pattern
                .compile("Duplicate entry '(.*?)' for key '(.*?)'").matcher(raw)).find())
            return "Давхардсан утга: '" + m.group(1) + "' (түлхүүр: " + m.group(2) + ")";

        if ((m = java.util.regex.Pattern
                .compile("Field '(.*?)' doesn't have a default value").matcher(raw)).find())
            return "'" + m.group(1) + "' талбар дутуу байна (өгөгдлийн сангийн багана заавал утгатай)";

        if ((m = java.util.regex.Pattern
                .compile("Column '(.*?)' cannot be null").matcher(raw)).find())
            return "'" + m.group(1) + "' талбар хоосон байж болохгүй";

        if ((m = java.util.regex.Pattern
                .compile("Data too long for column '(.*?)'").matcher(raw)).find())
            return "'" + m.group(1) + "' талбарын утга хэт урт байна";

        if (raw.contains("foreign key constraint fails"))
            return "Холбоотой бичлэг олдсонгүй (foreign key зөрчил)";

        return "Өгөгдлийн зөрчил: " + (raw.length() > 300 ? raw.substring(0, 300) + "…" : raw);
    }

    /** Гэнэтийн алдаа — дотоод мэдээллийг задлахгүй, log-той холбох код буцаана */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpected(Exception ex, HttpServletRequest req) {
        String traceId = newTraceId();
        log.error("Unhandled exception [{}] {} {}", traceId, req.getMethod(), req.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                "Системд алдаа гарлаа. Мэдээллийн технологийн албанд дараах кодыг мэдэгдэнэ үү",
                req, traceId);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message,
                                                      HttpServletRequest req, String traceId) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("path", req != null ? req.getRequestURI() : null);
        if (traceId != null) body.put("traceId", traceId);
        return ResponseEntity.status(status).body(body);
    }

    private String newTraceId() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String defaultMessage(HttpStatus status) {
        return switch (status) {
            case UNAUTHORIZED -> "Нэвтрэх хугацаа дууссан байна. Дахин нэвтэрнэ үү";
            case FORBIDDEN    -> "Танд энэ үйлдлийг хийх эрх алга";
            case NOT_FOUND    -> "Хүсэлт хийсэн мэдээлэл олдсонгүй";
            case BAD_REQUEST  -> "Илгээсэн өгөгдөл буруу байна";
            case CONFLICT     -> "Өгөгдөл зөрчилдөж байна";
            default           -> "Системд алдаа гарлаа";
        };
    }
}
