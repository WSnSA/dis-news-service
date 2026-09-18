package mn.usug.dis_news_service.Entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Засварын ажилтны мэргэжил — Автын засварчин, Цахилгаанчин, Моторчин, Гагнуурчин.
 * "Ажилтан" табын шүүлтүүр.
 */
@Entity
@Table(name = "repair_specialty")
@Data
@EntityListeners(AuditingEntityListener.class)
public class RepairSpecialty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    /** MECHANIC / ELECTRICIAN / MOTOR / WELDER */
    @Column(name = "code", length = 30)
    private String code;

    @Column(name = "sort_order")
    private Integer sortOrder;

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
