package mn.usug.dis_news_service.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "tasks")
public class Task {
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "work_description")
    private Integer workDescription;

    @Column(name = "assigned_employee")
    private Integer assignedEmployee;

    @Column(name = "deadline_date")
    private Integer deadlineDate;

    @Column(name = "department_id")
    private Integer departmentId;

    @Column(name = "fulfillment")
    private Integer fulfillment;

    @Column(name = "active_flag")
    private Integer activeFlag;

    @Column(name = "status")
    private Integer status;

    @Column(name = "created_date")
    private Integer createdDate;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_date")
    private Integer updatedDate;

    @Column(name = "updated_by")
    private Integer updatedBy;

}