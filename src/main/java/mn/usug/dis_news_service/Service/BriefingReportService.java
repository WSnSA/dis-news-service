package mn.usug.dis_news_service.Service;

import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.DTO.BriefingDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static mn.usug.dis_news_service.Service.JasperClient.nz;

/**
 * Шуурхай хурлын үүрэг даалгаврын биелэлтийн тайлан (PDF, jasper-service).
 * Хугацааны мужийн (from–to) бүх cycle-ийг нэг хүснэгтэд гаргана.
 *
 * Тайлангийн код (jasper-т import хийх КОД): BRIEFING_FULFILLMENT
 */
@Service
@RequiredArgsConstructor
public class BriefingReportService {

    private final BriefingService briefingService;
    private final JasperClient jasperClient;

    @Value("${jasper.briefing-fulfillment-template-code:BRIEFING_FULFILLMENT}")
    private String templateCode;

    private static final DateTimeFormatter D = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    /** Гарын үсэг (тогтмол — хожим тохиргоонд гаргаж болно) */
    private static final String REVIEWER_TITLE = "ЗАХИРГАА УДИРДЛАГЫН ХЭЛТСИЙН ДАРГА";
    private static final String REVIEWER_NAME  = "Ж.НАСАНТОГТОХ";
    private static final String COMPILER_TITLE = "ЗАХИРГАА УДИРДЛАГЫН ХЭЛТСИЙН МЭРГЭЖИЛТЭН";
    private static final String COMPILER_NAME  = "Б.ОЧИРБАТ";

    @Transactional(readOnly = true)
    public byte[] fulfillmentReport(LocalDate from, LocalDate to) {
        if (from == null || to == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Хугацааны муж (from, to) заавал");
        if (to.isBefore(from))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Төгсгөлийн огноо эхлэлээс өмнө байна");

        List<Map<String, Object>> rows = buildRows(from, to);

        Map<String, Object> data = new HashMap<>();
        data.put("reportTitle", "Газрын удирдлагын шуурхай зөвлөгөөнөөр өгөгдсөн үүрэг даалгаврын биелэлт");
        data.put("periodLabel", from.format(D) + " – " + to.format(D));
        data.put("reviewerTitle", REVIEWER_TITLE);
        data.put("reviewerName", REVIEWER_NAME);
        data.put("compilerTitle", COMPILER_TITLE);
        data.put("compilerName", COMPILER_NAME);
        data.put("rows", rows);

        return jasperClient.renderPdfByCode(templateCode, data);
    }

    /** Хугацааны мужид багтах cycle бүрийг нэг мөр болгон угсарна. */
    private List<Map<String, Object>> buildRows(LocalDate from, LocalDate to) {
        List<BriefingDto> tasks = briefingService.getAll();
        List<Map<String, Object>> rows = new ArrayList<>();
        int no = 0;

        // Огноо → мөч дарааллаар эрэмбэлэхийн тулд эхлээд цуглуулна
        List<Object[]> flat = new ArrayList<>();   // [meetingDate, BriefingDto, Cycle]
        for (BriefingDto t : tasks) {
            if (t.getCycles() == null) continue;
            for (BriefingDto.Cycle c : t.getCycles()) {
                LocalDate md = c.getMeetingDate();
                if (md == null || md.isBefore(from) || md.isAfter(to)) continue;
                flat.add(new Object[]{md, t, c});
            }
        }
        flat.sort(Comparator
                .comparing((Object[] a) -> (LocalDate) a[0])
                .thenComparing(a -> ((BriefingDto) a[1]).getId()));

        for (Object[] a : flat) {
            BriefingDto t = (BriefingDto) a[1];
            BriefingDto.Cycle c = (BriefingDto.Cycle) a[2];
            Map<String, Object> r = new HashMap<>();
            r.put("no", String.valueOf(++no));
            r.put("taskText", nz(t.getDescription()));
            r.put("dept", deptList(t));
            r.put("deadline", c.getSubmitDeadline() != null ? c.getSubmitDeadline().toLocalDate().format(D) : "");
            r.put("fulfillment", fulfillmentText(c));
            r.put("assigner", nz(t.getAssignerName()));
            r.put("score", c.getScore() != null ? c.getScore() + "%" : "-");
            rows.add(r);
        }
        return rows;
    }

    private String deptList(BriefingDto t) {
        if (t.getDepartments() == null) return "";
        return t.getDepartments().stream()
                .map(BriefingDto.DepRef::getDepName)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(", "));
    }

    /** Алба тус бүрийн биелэлтийн текст — "Алба: текст" мөр мөрөөр. Хоосон бол "Оруулаагүй". */
    private String fulfillmentText(BriefingDto.Cycle c) {
        if (c.getFulfillments() == null || c.getFulfillments().isEmpty()) return "Оруулаагүй";
        List<String> lines = new ArrayList<>();
        for (BriefingDto.Fulfillment f : c.getFulfillments()) {
            String text = f.getWorkText() != null ? f.getWorkText().trim() : "";
            boolean submitted = f.getSubmittedAt() != null && !text.isEmpty();
            String dep = f.getDepName() != null ? f.getDepName() : "";
            lines.add((dep.isBlank() ? "" : dep + ": ") + (submitted ? text : "Оруулаагүй"));
        }
        return String.join("\n", lines);
    }
}
