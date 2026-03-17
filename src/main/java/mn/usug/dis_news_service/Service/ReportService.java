package mn.usug.dis_news_service.Service;

import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.DAO.MenuDAO;
import mn.usug.dis_news_service.DTO.DailyReportRes;
import mn.usug.dis_news_service.DTO.SewageTreatmentSummaryDto;
import mn.usug.dis_news_service.DTO.WorkNewsDayRes;
import mn.usug.dis_news_service.Entity.Menu;
import mn.usug.dis_news_service.Model.HourlyReport;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final MenuDAO menuDAO;
    private final MainService mainService;
    private final WorkNewsService workNewsService;
    private final SewageTreatmentService sewageTreatmentService;

    public DailyReportRes getDaily(LocalDate date) {

        Date javaDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());

        // ── Ус хангамж ──────────────────────────────────────────
        List<HourlyReport> source       = buildSection("source",       javaDate);
        List<HourlyReport> transmission = buildSection("transmission", javaDate);
        List<HourlyReport> pool         = buildSection("pool",         javaDate);
        List<HourlyReport> js           = buildSection("js",           javaDate);

        // ── Цэвэрлэх байгууламж (7:00 цагийн тайлан) ────────────
        List<SewageTreatmentSummaryDto> sewage = sewageTreatmentService.getSummary(date, 7);

        // ── Ажлын мэдээ ──────────────────────────────────────────
        WorkNewsDayRes workNews = null;
        try {
            workNews = workNewsService.getByDay(date);
        } catch (Exception ignored) {
            // тухайн өдрийн мэдээ байхгүй тохиолдолд null буцаана
        }

        return new DailyReportRes(
                date.toString(),
                source,
                transmission,
                pool,
                js,
                sewage,
                workNews
        );
    }

    // ── Тухайн төрлийн станцуудын өдрийн тайлан ────────────────
    private List<HourlyReport> buildSection(String type, Date date) {
        return menuDAO.findByType(type).stream()
                .map(menu -> {
                    HourlyReport r = mainService.getDailyReport(menu.getId(), date);
                    r.setStationName(menu.getName());
                    return r;
                })
                .toList();
    }
}
