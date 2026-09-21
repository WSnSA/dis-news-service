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
 * Засварын ангилал — Их засвар, Урсгал засвар, Техникийн үйлчилгээ, Сервис үйлчилгээ.
 * Засварын бүртгэлийн "Ангилал" таб энэ хүснэгтийг удирдана.
 */
@Entity
@Table(name = "repair_category")
@Data
@EntityListeners(AuditingEntityListener.class)
public class RepairCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    /**
     * Системийн түлхүүр (MAJOR / CURRENT / TECH / SERVICE). Зөвхөн migration-аар суудаг
     * үндсэн 4 ангилалд утгатай — UI-аас нэмсэн ангилалд NULL үлдэнэ.
     * Код нь хожим "машин засвартай эсэх" логикт ангиллыг нэрээр биш кодоор таних боломж өгнө.
     */
    @Column(name = "code", length = 30)
    private String code;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "sort_order")
    private Integer sortOrder;

    /**
     * Энэ ангилалд засварт байгаа машиныг захиалгад сонгуулахгүй эсэх.
     * 1 = сонгогдохгүй (их засвар гэх мэт), 0 = сонгогдож болно (тос тосолгоо).
     */
    @Column(name = "blocks_dispatch", nullable = false)
    private Integer blocksDispatch = 1;

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
