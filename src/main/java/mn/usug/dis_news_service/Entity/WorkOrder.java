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

@Data
@Entity
@Table(name = "work_order")
@EntityListeners(AuditingEntityListener.class)
public class WorkOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /* ===== ШИНЭ СИСТЕМ ===== */

    @Column(name = "work_description", columnDefinition = "TEXT")
    private String workDescription;

    @Column(name = "work_location", columnDefinition = "TEXT")
    private String workLocation;

    @Column(name = "assigned_department_id")
    private Integer assignedDepartmentId;

    @Column(name = "assigned_employee_id")
    private Integer assignedEmployeeId;

    @Column(name = "department_id")
    private Integer departmentId;

    @Column(name = "deadline_date")
    private LocalDate deadlineDate;

    @Column(name = "fulfillment", columnDefinition = "TEXT")
    private String fulfillment;

    @Column(name = "lat")
    private Double lat;

    @Column(name = "lng")
    private Double lng;

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

    /* ===== ХУУЧИН СИСТЕМ ===== */

    @Column(name = "old_mur_id")
    private Long oldMurId;

    @Column(name = "old_assigned_department", columnDefinition = "TEXT")
    private String oldAssignedDepartment;

    @Column(name = "old_assigned_employee", columnDefinition = "TEXT")
    private String oldAssignedEmployee;

    @Column(name = "old_work_description", columnDefinition = "TEXT")
    private String oldWorkDescription;

    @Column(name = "old_work_location", columnDefinition = "TEXT")
    private String oldWorkLocation;

    @Column(name = "old_deadline_date", columnDefinition = "TEXT")
    private String oldDeadlineDate;

    @Column(name = "old_fulfillment", columnDefinition = "TEXT")
    private String oldFulfillment;

    @Column(name = "old_created_date", columnDefinition = "TEXT")
    private String oldCreatedDate;
}
