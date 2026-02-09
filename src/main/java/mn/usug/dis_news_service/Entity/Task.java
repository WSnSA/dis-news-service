package mn.usug.dis_news_service.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Entity
@Table(name = "tasks")
@Data
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

    @Column(name = "status")
    private Integer status;

    @Column(name = "active_flag")
    private Integer activeFlag;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "created_by")
    private Integer createdBy;

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
