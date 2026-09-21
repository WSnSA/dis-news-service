package mn.usug.dis_news_service.Controller;

import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.Service.Report.RepairDocxWriter;
import mn.usug.dis_news_service.Service.Report.RepairReportService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Засварын мэдээ татах — Word (.docx).
 *
 * Өдөр тутам / 7 хоног / хагас жил, жилийн нэгтгэл гэсэн гурван давтамж.
 * Тайлангийн өгөгдөл засварын бүртгэлээс шууд бүрдэнэ.
 */
@RestController
@RequestMapping("/repair/report")
@RequiredArgsConstructor
public class RepairReportController {

    private static final String DOCX =
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

    private final RepairDocxWriter writer;
    private final RepairReportService data;

    /** Дэлгэц дээр урьдчилан харах — татахаасаа өмнө тоогоо шалгах */
    @GetMapping("/preview")
    public Map<String, Object> preview(
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to")   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        List<RepairReportService.Row> rows = data.rows(from, to);
        RepairReportService.Attendance att = data.attendance(from, to);
        // Тухайн хугацаанд бичигдсэн акт / шаардах — тайланд хамт харагдана
        List<mn.usug.dis_news_service.Entity.RepairDocument> docs = data.documents(from, to);
        Map<String, Object> out = new java.util.LinkedHashMap<>(Map.of(
                "fleetTotal",  data.fleetTotal(),
                "total",       rows.size(),
                "ready",       rows.stream().filter(RepairReportService.Row::done).count(),
                "waitingParts", rows.stream().filter(RepairReportService.Row::waitingParts).count(),
                "bySection",   data.countBySection(rows),
                "attendance",  Map.of(
                        "worked", att.worked(), "sick", att.sick(),
                        "leave", att.leave(), "bySpecialty", att.bySpecialty()),
                "rows",        rows
        ));
        out.put("documents", docs);
        out.put("actCount", docs.stream().filter(d -> "ACT".equals(d.getDocType())).count());
        out.put("requestCount", docs.stream().filter(d -> "REQUEST".equals(d.getDocType())).count());
        return out;
    }

    /** Өдөр тутмын мэдээ */
    @GetMapping("/daily")
    public ResponseEntity<ByteArrayResource> daily(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) throws IOException {
        return file(writer.daily(date), "zasvariin-medee_%s.docx".formatted(date));
    }

    /** 7 хоногийн тайлан */
    @GetMapping("/weekly")
    public ResponseEntity<ByteArrayResource> weekly(
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to")   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) throws IOException {
        return file(writer.weekly(from, to), "zasvar-7-honog_%s_%s.docx".formatted(from, to));
    }

    /** Хагас жил / жилийн нэгтгэл */
    @GetMapping("/period")
    public ResponseEntity<ByteArrayResource> period(
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to")   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(value = "title", required = false) String title
    ) throws IOException {
        String heading = (title == null || title.isBlank())
                ? "АВТО БААЗЫН ЗАСВАРЫН ХЭСГИЙН ТАЙЛАН" : title.trim();
        return file(writer.period(from, to, heading), "zasvar-tailan_%s_%s.docx".formatted(from, to));
    }

    private ResponseEntity<ByteArrayResource> file(byte[] bytes, String name) {
        // Кирилл нэр зөв татагдахын тулд filename* (RFC 5987)
        String encoded = java.net.URLEncoder.encode(name, StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"report.docx\"; filename*=UTF-8''" + encoded)
                .contentType(MediaType.parseMediaType(DOCX))
                .contentLength(bytes.length)
                .body(new ByteArrayResource(bytes));
    }
}
