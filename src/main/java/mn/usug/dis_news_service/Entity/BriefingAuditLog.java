package mn.usug.dis_news_service.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Шуурхай хурлын аудит бүртгэл (§7) — хэн, хэзээ, ямар үйлдэл хийснийг хадгална.
 * old_value / new_value нь MySQL JSON багана.
 */
@Entity
@Table(name = "briefing_audit_log")
@Data
public class BriefingAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "user_name", length = 255)
    private String userName;

    /** CREATE_TASK / UPDATE_TASK / DELETE_TASK / SUBMIT_FULFILLMENT / SCORE / RETURN / EXTEND / SUBMIT_NEWS ... */
    @Column(name = "action", length = 40)
    private String action;

    /** TASK / CYCLE / FULFILLMENT / EVIDENCE / NEWS / MEETING / ROLE */
    @Column(name = "entity_type", length = 40)
    private String entityType;

    @Column(name = "entity_id")
    private Integer entityId;

    @Column(name = "old_value", columnDefinition = "json")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "json")
    private String newValue;

    @Column(name = "ip", length = 64)
    private String ip;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
