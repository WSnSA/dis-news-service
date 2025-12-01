package mn.usug.dis_news_service.Model;

import lombok.Data;
import mn.usug.dis_news_service.Entity.HourlyWsSecond;
import mn.usug.dis_news_service.Entity.HourlyWsStation;

import java.util.List;

@Data
public class ReportModelDaily {
    HourlyWsStation station;
    List<HourlyWsSecond> secondList;
}
