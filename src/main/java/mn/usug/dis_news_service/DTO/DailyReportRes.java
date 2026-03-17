package mn.usug.dis_news_service.DTO;

import mn.usug.dis_news_service.Model.HourlyReport;
import java.util.List;

public record DailyReportRes(
        String reportDate,

        // Ус хангамж
        List<HourlyReport> sourceStations,
        List<HourlyReport> transmissionStations,
        List<HourlyReport> poolStations,
        List<HourlyReport> jsStations,

        // Цэвэрлэх байгууламж
        List<SewageTreatmentSummaryDto> sewage,

        // Ажлын мэдээ
        WorkNewsDayRes workNews
) {}
