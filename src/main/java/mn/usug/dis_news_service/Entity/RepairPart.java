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

/**
 * Сэлбэгийн лавлах жагсаалт. Тухайн засварт зарцуулсан сэлбэгийг бүртгэх хэсэг
 * 2-р шатанд энэ жагсаалтаас сонгож холбогдоно.
 */
@Entity
@Table(name = "repair_part")
@Data
@EntityListeners(AuditingEntityListener.class)
public class RepairPart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** repair_part_type.id */
    @Column(name = "part_type_id", nullable = false)
    private Long partTypeId;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    /** Каталогийн код / артикул */
    @Column(name = "code", length = 60)
    private String code;

    /** Хэмжих нэгж — ш, л, кг гэх мэт */
    @Column(name = "unit", length = 30)
    private String unit;

    @Column(name = "unit_price", precision = 15, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "stock_qty", precision = 15, scale = 2)
    private BigDecimal stockQty;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

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
