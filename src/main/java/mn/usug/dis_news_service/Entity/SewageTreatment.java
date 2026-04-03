package mn.usug.dis_news_service.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import lombok.Data;

@Data
@Entity
@Table(name = "sewage_treatment")
public class SewageTreatment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "station_id")
    private Integer stationId;

    @Column(name = "working_count", length = 100)
    private String workingCount;

    @Column(name = "pending_count", length = 100)
    private String pendingCount;

    @Column(name = "repairing_count", length = 100)
    private String repairingCount;

    @Column(name = "received_waste")
    private Double receivedWaste;

    @Column(name = "received_wool")
    private Double receivedWool;

    @Column(name = "received_water")
    private Double receivedWater;

    @Column(name = "substance_spent")
    private Double substanceSpent;

    @Column(name = "treated_water")
    private Double treatedWater;

    @Column(name = "solid_waste")
    private Double solidWaste;

    @Column(name = "active_flag", nullable = false)
    private Integer activeFlag;

    @Column(name = "status")
    private Integer status;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

}