package mn.usug.dis_news_service.Entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Шаардах хуудас / актын нэг мөр — БМ-6 маягтын хүснэгтийн мөр.
 *
 * Нэр, кодыг мөрөнд хуулж авдаг: сэлбэгийн лавлах хожим өөрчлөгдсөн ч
 * бичигдсэн баримт хэвээр үлдэнэ (гарын үсэг зурсан цаас).
 */
@Entity
@Table(name = "repair_document_item")
@Data
@EntityListeners(AuditingEntityListener.class)
public class RepairDocumentItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "repair_document_id", nullable = false)
    private Long repairDocumentId;

    /** repair_part.id — гараар бичсэн мөрөнд NULL */
    @Column(name = "repair_part_id")
    private Long repairPartId;

    @Column(name = "item_name", nullable = false, length = 300)
    private String itemName;

    @Column(name = "item_code", length = 60)
    private String itemCode;

    @Column(name = "unit", length = 30)
    private String unit;

    /** Хүссэн тоо хэмжээ */
    @Column(name = "qty_requested", nullable = false, precision = 15, scale = 2)
    private BigDecimal qtyRequested = BigDecimal.ONE;

    /** Зөвшөөрсөн — хоосон бол хүссэнтэй тэнцүү гэж үзнэ */
    @Column(name = "qty_approved", precision = 15, scale = 2)
    private BigDecimal qtyApproved;

    /** Олгосон */
    @Column(name = "qty_issued", precision = 15, scale = 2)
    private BigDecimal qtyIssued;

    @Column(name = "unit_price", precision = 15, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(name = "active_flag", nullable = false)
    private Integer activeFlag = 1;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private Integer createdBy;
}
