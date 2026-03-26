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


@Entity
@Table(name = "tasks")
@Data
@EntityListeners(AuditingEntityListener.class)
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /* ===== ШИНЭ СИСТЕМ ===== */

    @Column(name = "assigned_position_name")
    private String assignedPositionName;

    @Column(name = "assigned_by_user_id")
    private Integer assignedByUserId;

    @Column(name = "work_description", length = 2000)
    private String workDescription;

    @Column(name = "department_id")
    private Integer departmentId;

    @Column(name = "position_id")
    private Integer positionId;

    @Column(name = "deadline_date")
    private LocalDate deadlineDate;

    @Column(name = "fulfillment", length = 1000)
    private String fulfillment;

    @Column(name = "return_note", columnDefinition = "text")
    private String returnNote;

    @Column(name = "status")
    private Integer status;

    @Column(name = "active_flag")
    private Integer activeFlag;

    @CreatedDate
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private Integer createdBy;

    @LastModifiedDate
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @LastModifiedBy
    @Column(name = "updated_by")
    private Integer updatedBy;

    /* ===== ХУУЧИН СИСТЕМ (READ ONLY) ===== */

    @Column(name = "old_mur_id")
    private Long oldMurId;

    @Column(name = "old_assigned_date")
    private String oldAssignedDate;

    @Column(name = "old_assigned_position_name")
    private String oldAssignedPositionName;

    @Column(name = "old_work_description", columnDefinition = "text")
    private String oldWorkDescription;

    @Column(name = "old_deadline_date")
    private String oldDeadlineDate;

    @Column(name = "old_respondent")
    private String oldRespondent;

    @Column(name = "old_fulfillment", columnDefinition = "text")
    private String oldFulfillment;
}
