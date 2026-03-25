package mn.usug.dis_news_service.Entity;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import lombok.Data;

@Data
@Entity
@Table(name = "water_supply")
@EntityListeners(AuditingEntityListener.class)
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

    @CreatedDate
    @Column(name = "created_date", updatable = false)
    private Instant createdDate;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private Integer createdBy;

    @LastModifiedDate
    @Column(name = "updated_date")
    private Instant updatedDate;

    @LastModifiedBy
    @Column(name = "updated_by")
    private Integer updatedBy;

}