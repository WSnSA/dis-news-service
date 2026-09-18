package mn.usug.dis_news_service.Entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Засварын баримт — Акт (ACT) ба Шаардах (REQUEST).
 * Сэлбэг/ажилтны мөрүүдээс автоматаар бүрдэх хэсэг 2-р шатанд нэмэгдэнэ.
 */
@Entity
@Table(name = "repair_document")
@Data
@EntityListeners(AuditingEntityListener.class)
public class RepairDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ACT = Акт, REQUEST = Шаардах */
    @Column(name = "doc_type", nullable = false, length = 20)
    private String docType;

    @Column(name = "doc_no", length = 60)
    private String docNo;

    @Column(name = "doc_date")
    private LocalDate docDate;

    /** vehicle_repair.id — холбоотой засвар */
    @Column(name = "vehicle_repair_id")
    private Long vehicleRepairId;

    /** Улсын дугаарыг давхар хадгална — засвар устсан ч түүх үлдэнэ */
    @Column(name = "plate_number", length = 50)
    private String plateNumber;

    @Column(name = "amount", precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    /** 1=идэвхтэй, 0=идэвхгүй (soft delete) */
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
}
