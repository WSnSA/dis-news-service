package mn.usug.dis_news_service.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Шуурхай хурлын модулийн хэрэглэгчийн дүр (RBAC).
 * Нэг хэрэглэгч олон дүртэй байж болно.
 * role_key: BRIEFING_ADMIN / BRIEFING_SECRETARY / BRIEFING_UNIT / BRIEFING_MANAGER / BRIEFING_VIEWER
 */
@Entity
@Table(name = "briefing_user_role")
@Data
public class BriefingUserRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "role_key", length = 32)
    private String roleKey;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "created_date")
    private LocalDateTime createdDate;
}
