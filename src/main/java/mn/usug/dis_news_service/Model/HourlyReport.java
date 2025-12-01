package mn.usug.dis_news_service.Model;

import lombok.Data;
import mn.usug.dis_news_service.Entity.HourlyWsStation;

import java.util.Date;
import java.util.List;

@Data
public class HourlyReport {
    private Date date;
    private Integer hour;
    private Integer menuId;

    // SUM fields from station
    private Integer firstWorkingCount;
    private Integer firstPendingCount;
    private Integer firstRepairingCount;
    private Integer firstPool;
    private Integer secondPool;
    private Integer thirdPool;
    private Integer fourthPool;
    private Integer pipeFm1;
    private Integer pipeFm7;
    private Integer pipeFm8;

    // List (aggregated) fields for Daily/Monthly
    private List<Integer> firstWorkingCountList;
    private List<Integer> firstPendingCountList;
    private List<Integer> firstRepairingCountList;

    // 🟢 NEW → stationList from POST JSON
    private List<HourlyWsStation> stationList;

    // 🟢 NEW → secondList from POST JSON
    private List<HourlySecondReport> secondList;

    // 🟢 Output aggregated second report
    private List<HourlySecondReport> hourlyWsSecondList;
}
