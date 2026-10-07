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
            no++;
            // Үүрэг тус бүрийн түвшний утгууд — JRXML дээр group-ийн эхний мөрөнд л хэвлэгдэнэ
            String groupKey = String.valueOf(c.getId());
            String noStr = String.valueOf(no);
            String taskText = nz(t.getDescription());
            String deadline = c.getSubmitDeadline() != null ? c.getSubmitDeadline().toLocalDate().format(D) : "";
            String assigner = nz(t.getAssignerName());
            String score = c.getScore() != null ? c.getScore() + "%" : "-";

            // ЗБН (алба) бүрийг тусдаа мөр болгоно (өмнөх HTML-ийн адил)
            for (String[] dr : deptFulfillmentRows(t, c)) {
                Map<String, Object> r = new HashMap<>();
                r.put("groupKey", groupKey);
                r.put("no", noStr);
                r.put("taskText", taskText);
                r.put("dept", dr[0]);
                r.put("deadline", deadline);
                r.put("fulfillment", dr[1]);
                r.put("assigner", assigner);
                r.put("score", score);
                rows.add(r);
            }
        }
        return rows;
    }

    /**
     * Үүрэг-cycle-ийг ЗБН (алба) бүрээр мөр болгон задлана — [дэд нэр, биелэлтийн текст].
     * Биелэлттэй бол алба тус бүрийн мөр; эс бол хариуцагч алба бүрээр "Оруулаагүй".
     */
    private List<String[]> deptFulfillmentRows(BriefingDto t, BriefingDto.Cycle c) {
        List<String[]> out = new ArrayList<>();
        if (c.getFulfillments() != null && !c.getFulfillments().isEmpty()) {
            for (BriefingDto.Fulfillment f : c.getFulfillments()) {
                String text = f.getWorkText() != null ? f.getWorkText().trim() : "";
                boolean submitted = f.getSubmittedAt() != null && !text.isEmpty();
                out.add(new String[]{ nz(f.getDepName()), submitted ? text : "Оруулаагүй" });
            }
        } else if (t.getDepartments() != null && !t.getDepartments().isEmpty()) {
            for (BriefingDto.DepRef d : t.getDepartments()) {
                out.add(new String[]{ nz(d.getDepName()), "Оруулаагүй" });
            }
        } else {
            out.add(new String[]{ "", "Оруулаагүй" });
        }
        return out;
    }
}
