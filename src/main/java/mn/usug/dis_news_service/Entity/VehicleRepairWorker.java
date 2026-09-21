package mn.usug.dis_news_service.Entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Тухайн засварт ажилласан хүний мөр */
@Entity
@Table(name = "vehicle_repair_worker")
@Data
@EntityListeners(AuditingEntityListener.class)
public class VehicleRepairWorker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_repair_id", nullable = false)
    private Long vehicleRepairId;

    @Column(name = "repair_worker_id", nullable = false)
    private Long repairWorkerId;

    /**
     * Бүртгэсэн үеийн нэр. Лавлах дээрх ажилтныг сэргээгээд нэрийг нь
     * өөрчилсөн ч энд бичигдсэн нэр хэвээр үлдэнэ — нэгж үнэтэй ижил зарчим.
     */
    @Column(name = "worker_name", length = 200)
    private String workerName;

    /** Бүртгэсэн үеийн мэргэжил */
    @Column(name = "specialty_name", length = 200)
    private String specialtyName;

    /** Ажилласан цаг */
    @Column(name = "hours", precision = 10, scale = 2)
    private BigDecimal hours;

    @Column(name = "note", length = 300)
    private String note;

    @Column(name = "active_flag", nullable = false)
    private Integer activeFlag = 1;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private Integer createdBy;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @LastModifiedBy
    @Column(name = "updated_by")
    private Integer updatedBy;
}
