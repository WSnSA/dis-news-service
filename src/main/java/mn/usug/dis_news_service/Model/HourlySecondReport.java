package mn.usug.dis_news_service.Model;

import lombok.Data;

import java.util.List;

@Data
public class HourlySecondReport {
    private Integer generatorNo;
    private Integer status;
    private Double frequency;
    private Double eCurrent;
    private Double pressure;
    private Double pressure2;
    private Double pressure3;
    private Double pressure4;
    private Double temperature;
    private Double temperature2;
    private Double temperature3;
    private Double temperature4;
    private Double gauge;
    private Double creation;
    private Double pumpedWater;
    private Double pool;
    private Double chlorine;

    private List<Integer> statusList;
}
