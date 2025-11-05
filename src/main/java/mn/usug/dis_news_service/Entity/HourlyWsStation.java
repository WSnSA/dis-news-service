package mn.usug.dis_news_service.Entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;

import java.util.Date;

@Data
@Entity
@Table(name = "hourly_ws_station")
public class HourlyWsStation {
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "station_code")
    private Integer stationCode;

    @Column(name = "date")
    private Date date;

    @Column(name = "hour")
    private Integer hour;

    @Column(name = "first_working_count")
    private Integer firstWorkingCount;

    @Column(name = "first_pending_count")
    private Integer firstPendingCount;

    @Column(name = "first_repairing_count")
    private Integer firstRepairingCount;

    @Column(name = "first_pool")
    private Integer firstPool;

    @Column(name = "second_pool")
    private Integer secondPool;

    @Column(name = "third_pool")
    private Integer thirdPool;

    @Column(name = "fourth_pool")
    private Integer fourthPool;

    @Column(name = "chlorine")
    private Integer chlorine;
}
