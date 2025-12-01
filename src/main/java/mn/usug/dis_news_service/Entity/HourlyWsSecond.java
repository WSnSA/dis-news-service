package mn.usug.dis_news_service.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Data
@Entity
@Table(name = "hourly_ws_second")
public class HourlyWsSecond {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "date")
    private Date date;

    @Column(name = "hour")
    private Integer hour;

    @Column(name = "menu_id")
    private Integer menuId;

    @Column(name = "status")
    private Integer status;

    @Column(name = "generator_no")
    private Integer generatorNo;

    @Column(name = "frequency")
    private Double frequency;

    @Column(name = "e_current")
    private Double eCurrent;

    @Column(name = "pressure")
    private Double pressure;

    @Column(name = "pressure_2")
    private Double pressure2;

    @Column(name = "pressure_3")
    private Double pressure3;

    @Column(name = "pressure_4")
    private Double pressure4;

    @Column(name = "temperature")
    private Double temperature;

    @Column(name = "temperature_2")
    private Double temperature2;

    @Column(name = "temperature_3")
    private Double temperature3;

    @Column(name = "temperature_4")
    private Double temperature4;

    @Column(name = "gauge")
    private Double gauge;

    @Column(name = "creation")
    private Double creation;

    @Column(name = "pumped_water")
    private Double pumpedWater;

    @Column(name = "pool")
    private Double pool;

    @Column(name = "chlorine")
    private Double chlorine;
}
