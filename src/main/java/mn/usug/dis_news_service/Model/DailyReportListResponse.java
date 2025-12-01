package mn.usug.dis_news_service.Model;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class DailyReportListResponse {
    private Integer menuId;
    private Date date;
    private List<HourReport> hours;
}
