package mn.usug.dis_news_service.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import lombok.Data;

@Data
@Entity
@Table(name = "water_supply")
public class WaterSupply {
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "station_id", nullable = false)
    private Integer stationId;

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

    @Column(name = "second_working_count", length = 100)
    private String secondWorkingCount;

    @Column(name = "second_pending_count", length = 100)
    private String secondPendingCount;

    @Column(name = "second_repairing_count", length = 100)
    private String secondRepairingCount;

    @Column(name = "out_pipe_pressure", length = 100)
    private String outPipePressure;

    @Column(name = "out_pipe_stream")
    private Integer outPipeStream;

    @Column(name = "amount_of_chlorine")
    private Double amountOfChlorine;

    @Column(name = "pumped_water")
    private Double pumpedWater;

    @ColumnDefault("1")
    @Column(name = "active_flag", nullable = false)
    private Integer activeFlag;

    @ColumnDefault("1")
    @Column(name = "status", nullable = false)
    private Integer status;

    @Column(name = "created_date")
    private Instant createdDate;

    @Column(name = "created_by", nullable = false)
    private Integer createdBy;

    @Column(name = "updated_date")
    private Instant updatedDate;

    @Column(name = "updated_by", nullable = false)
    private Integer updatedBy;

}