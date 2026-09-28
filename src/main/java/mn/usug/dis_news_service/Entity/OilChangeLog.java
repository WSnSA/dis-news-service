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
 * Тос тосолгооны бүртгэл — машины засвараас (vehicle_repair) тусдаа, зөвхөн
 * тээврийн хэрэгсэл, жолооч, огноо, гүйлтийг хөтлөх хөнгөн лог.
 */
@Entity
@Table(name = "oil_change_log")
@Data
@EntityListeners(AuditingEntityListener.class)
public class OilChangeLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    /** Улсын дугаарыг давхар хадгална — машин устсан ч лог дугаараараа уншигдана */
    @Column(name = "plate_number", nullable = false, length = 20)
    private String plateNumber;

    /** driver.id — сонголтоор */
    @Column(name = "driver_id")
    private Long driverId;

    /** Бүртгэсэн үеийн жолоочийн нэр — лавлахаас устсан ч, эсвэл гараар бичсэн ч түүх хэвээр */
    @Column(name = "driver_name", length = 150)
    private String driverName;

    @Column(name = "change_date", nullable = false)
    private LocalDate changeDate;

    /** Тос солих үеийн одометрийн заалт (км) */
    @Column(name = "odometer_km", nullable = false)
    private Integer odometerKm;

    @Column(name = "note", length = 300)
    private String note;

    /** 1=идэвхтэй, 0=устгасан (soft delete) */
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

    // ── Хариуд дүүргэнэ (DB-д хадгалагдахгүй) ────────────────
    @Transient private String brand;
    @Transient private String model;
}
