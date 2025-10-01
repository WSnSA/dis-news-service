package mn.usug.dis_news_service.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "work_order")
public class WorkOrder {
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "work_description")
    private Integer workDescription;

    @Column(name = "work_location")
    private Integer workLocation;

    @Column(name = "assigned_department_id")
    private Integer assignedDepartmentId;

    @Column(name = "assigned_employee_id")
    private Integer assignedEmployeeId;

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

    @Column(name = "updated_by")
    private Integer updatedBy;

    @Column(name = "updated_date")
    private Integer updatedDate;

}