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
 * Засварын ажилтан. Тухайн засварт хэн ажилласныг бүртгэх хэсэг 2-р шатанд
 * энэ жагсаалтаас сонгогдоно.
 */
@Entity
@Table(name = "repair_worker")
@Data
@EntityListeners(AuditingEntityListener.class)
public class RepairWorker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** repair_specialty.id */
    @Column(name = "specialty_id", nullable = false)
    private Long specialtyId;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "phone", length = 30)
    private String phone;

    /** Зэрэг / мэргэшлийн зэрэглэл */
    @Column(name = "grade", length = 60)
    private String grade;

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
