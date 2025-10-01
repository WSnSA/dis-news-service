package mn.usug.dis_news_service.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data

@Entity
@Table(name = "vehicle_order")
public class VehicleOrder {
    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "work_description")
    private Integer workDescription;

    @Column(name = "assigned_department_id")
    private Integer assignedDepartmentId;

    @Column(name = "assigned_employee_id")
    private Integer assignedEmployeeId;

    @Column(name = "order_code")
    private Integer orderCode;

    @Column(name = "vehicle_type_id")
    private Integer vehicleTypeId;

    @Column(name = "count")
    private Integer count;

    @Column(name = "active_flag")
    private Integer activeFlag;

    @Column(name = "status")
    private Integer status;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "created_date")
    private Integer createdDate;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @Column(name = "updated_date")
    private Integer updatedDate;

}