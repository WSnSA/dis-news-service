package mn.usug.dis_news_service.Controller;

import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.DTO.MonthlyReportDto;
import mn.usug.dis_news_service.Service.MonthlyReportService;
import mn.usug.dis_news_service.Service.Report.MonthlyReportXlsxWriter;
import mn.usug.dis_news_service.Service.UserContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * "Сарын мэдээ" — олборлосон болон татан зайлуулж цэвэрлэсэн усны сарын мэдээ.
 *
 * GET  /report/monthly?year=&month=        — дэлгэцийн preview (JSON)
 * GET  /report/monthly/xlsx?year=&month=   — Excel татах
 * GET  /report/monthly/plan?year=          — оны төлөвлөгөө (мөр × 12 сар)
 * PUT  /report/monthly/plan                — { year, items: [{ rowId, month, value }] }
 * GET  /report/monthly/signers             — гарын үсгийн мөрүүд
 * PUT  /report/monthly/signers             — [{ title, name }]
 */
@RestController
@RequestMapping("/report/monthly")
@RequiredArgsConstructor
public class MonthlyReportController {

    private static final String XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final MonthlyReportService service;
    private final MonthlyReportXlsxWriter writer;

    @GetMapping
    public MonthlyReportDto get(@RequestParam("year") int year, @RequestParam("month") int month) {
        return service.build(year, month);
    }

    @GetMapping("/xlsx")
    public ResponseEntity<ByteArrayResource> xlsx(@RequestParam("year") int year, @RequestParam("month") int month)
            throws IOException {
        byte[] bytes = writer.write(service.build(year, month));
        String name = String.format("Сарын_мэдээ_%d_%02d.xlsx", year, month);
        // Кирилл нэр зөв татагдахын тулд filename* (RFC 5987)
        String encoded = URLEncoder.encode(name, StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"monthly_report.xlsx\"; filename*=UTF-8''" + encoded)
                .contentType(MediaType.parseMediaType(XLSX))
                .contentLength(bytes.length)
                .body(new ByteArrayResource(bytes));
    }

    @GetMapping("/plan")
    public List<MonthlyReportService.PlanRow> plans(@RequestParam("year") int year) {
        return service.getPlans(year);
    }

    public record PlanSaveReq(Integer year, List<MonthlyReportService.PlanItem> items) {}

    @PutMapping("/plan")
    public Map<String, Object> savePlans(@RequestBody PlanSaveReq req) {
        int n = service.savePlans(req.year() == null ? 0 : req.year(), req.items(), UserContext.getUserId());
        return Map.of("saved", n);
    }

    @GetMapping("/signers")
    public List<MonthlyReportDto.Signer> signers() {
        return service.getSigners();
    }

    @PutMapping("/signers")
    public List<MonthlyReportDto.Signer> saveSigners(@RequestBody List<MonthlyReportDto.Signer> signers) {
        return service.saveSigners(signers, UserContext.getUserId());
    }
}
