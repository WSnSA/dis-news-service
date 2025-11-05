package mn.usug.dis_news_service.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.util.Date;

@Data
@Entity
@Table(name = "hourly_ws_second")
public class HourlyWsSecond {
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "station_code")
    private Integer stationCode;

    @Column(name = "date")
    private Date date;

    @Column(name = "hour")
    private Integer hour;

    @Column(name = "generator_no")
    private Integer generatorNo;

    @Column(name = "status")
    private Integer status;

    @Column(name = "pressure")
    private Integer pressure;

    @Column(name = "frequency")
    private Integer frequency;

    @Column(name = "e_current")
    private Integer eCurrent;

    @Column(name = "creation")
    private Integer creation;

    @Column(name = "pumped_water")
    private Integer pumpedWater;

    @Column(name = "percent")
    private Integer percent;
}
