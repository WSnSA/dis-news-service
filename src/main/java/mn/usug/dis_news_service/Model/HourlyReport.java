package mn.usug.dis_news_service.Model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import mn.usug.dis_news_service.Entity.HourlyWsStation;

import java.util.Date;
import java.util.List;

@Data
public class HourlyReport {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-M-d")
    private Date date;
    private Integer hour;
    private Integer menuId;

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

    private List<HourlyWsStation> stationList;

    private List<HourlySecondReport> secondList;

    private Integer secondWorkingCount;
    private Integer secondPendingCount;
    private Integer secondRepairingCount;

    private List<HourlySecondReport> hourlyWsSecondList;

    private String stationName;
}
