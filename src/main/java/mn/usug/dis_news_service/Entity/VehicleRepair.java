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

    /** Засварт орох үеийн гүйлт (км) */
    @Column(name = "odometer_km")
    private Integer odometerKm;

    /**
     * Дараагийн үйлчилгээ хийх одометрийн заалт.
     * Жишээ: 125000 км-д тос сольсон, 10000 км тутам солих бол энд 135000.
     */
    @Column(name = "next_service_km")
    private Integer nextServiceKm;

    /** repair_worker.id — засварыг хариуцаж буй хүн */
    @Column(name = "responsible_worker_id")
    private Long responsibleWorkerId;

    /** Байршил — гараж, талбай, гадны сервис */
    @Column(name = "location", length = 200)
    private String location;

    /** Гадны байгууллагаар хийлгэсэн бол нэр */
    @Column(name = "external_org", length = 200)
    private String externalOrg;

    /** Гэмтлийн шалтгаан / хийгдэх ажлын тайлбар */
    @Column(name = "fault_description", columnDefinition = "TEXT")
    private String faultDescription;

    /** Хүлээлгэн өгсөн жолооч */
    @Column(name = "driver_name", length = 150)
    private String driverName;

    /** Засварт орсон цаг — өдөр тутмын мэдээнд "09:00" гэж ордог */
    @Column(name = "start_time")
    private java.time.LocalTime startTime;

    /** Бэлэн болох хугацаа */
    @Column(name = "expected_ready")
    private LocalDate expectedReady;

    /** 1 = сэлбэггүй зогсож байна (7 хоногийн мэдээнд тусад нь гардаг) */
    @Column(name = "waiting_parts", nullable = false)
    private Integer waitingParts = 0;

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
    @Transient private String responsibleWorkerName;
    /** Ангилал нь машиныг захиалгаас хасах эсэх — frontend шүүхэд */
    @Transient private Integer blocksDispatch;
    /** 1=Үйлчилгээ 2=Цэвэр ус 3=Бохир ус — тайланд хэсэг тус бүрээр бүлэглэнэ */
    @Transient private Integer serviceType;
}
