package mn.usug.dis_news_service.Entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Машины засварын бүртгэл — нэг машин тодорхой ангиллаар засварт орсон тэмдэглэл.
 *
 * Нээлттэй бичлэг (status=0) байх хугацаанд тухайн машиныг захиалгад хуваарилах
 * боломжгүй болно — машин хуваарилалтын assign-form үүнийг шүүнэ.
 */
@Entity
@Table(name = "vehicle_repair")
@Data
@EntityListeners(AuditingEntityListener.class)
public class VehicleRepair {

    public static final int STATUS_IN_REPAIR = 0;
    public static final int STATUS_DONE      = 1;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    /**
     * Улсын дугаарыг давхар хадгална. `ref/vehicle/delete` нь hard delete хийдэг тул
     * машин устсан ч засварын түүх дугаараараа уншигдана (vehicles_to_out-той ижил зарчим).
     */
    @Column(name = "plate_number", nullable = false, length = 20)
    private String plateNumber;

    @Column(name = "repair_category_id", nullable = false)
    private Long repairCategoryId;

    /** Засварт орсон огноо */
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /** Төлөвлөсөн (эсвэл бодит) дуусах огноо. NULL = хугацаа тодорхойгүй */
    @Column(name = "end_date")
    private LocalDate endDate;

    /** 0=засварт байна, 1=дууссан */
    @Column(name = "status", nullable = false)
    private Integer status = STATUS_IN_REPAIR;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    /** 1=идэвхтэй, 0=устгасан (soft delete) */
    @Column(name = "active_flag", nullable = false)
    private Integer activeFlag = 1;

    // ── Audit ────────────────────────────────────────────────
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

    // ── Хариуд дүүргэнэ (DB-д хадгалагдахгүй) ────────────────
    @Transient private String brand;
    @Transient private String model;
    @Transient private String categoryName;
    @Transient private String categoryCode;
}
