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

/**
 * Тухайн засварт зарцуулсан сэлбэгийн мөр.
 *
 * Нэгж үнийг лавлахаас хуулж мөрөнд хадгална — каталогийн үнэ хожим
 * өөрчлөгдсөн ч бүртгэсэн үеийн дүн алдагдахгүй.
 */
@Entity
@Table(name = "vehicle_repair_part")
@Data
@EntityListeners(AuditingEntityListener.class)
public class VehicleRepairPart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_repair_id", nullable = false)
    private Long vehicleRepairId;

    @Column(name = "repair_part_id", nullable = false)
    private Long repairPartId;

    @Column(name = "qty", nullable = false, precision = 15, scale = 2)
    private BigDecimal qty = BigDecimal.ONE;

    /** Бүртгэсэн үеийн нэгж үнэ */
    @Column(name = "unit_price", precision = 15, scale = 2)
    private BigDecimal unitPrice;

    /** Бүртгэсэн үеийн нэр — лавлахаас устсан үеийн нөөц */
    @Column(name = "part_name", length = 200)
    private String partName;

    /** Бүртгэсэн үеийн төрөл */
    @Column(name = "part_type_name", length = 200)
    private String partTypeName;

    /** Бүртгэсэн үеийн хэмжих нэгж */
    @Column(name = "part_unit", length = 30)
    private String partUnit;

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
