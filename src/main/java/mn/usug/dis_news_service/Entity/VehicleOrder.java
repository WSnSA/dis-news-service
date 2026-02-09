package mn.usug.dis_news_service.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "vehicle_order")
@Data
public class VehicleOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String workDescription;

    private Integer assignedDepartmentId;
    private Integer assignedEmployeeId;

    private LocalDate orderDate;

    private Integer status;
    private Integer activeFlag;

    private LocalDateTime createdDate;

    // хуучин систем
    private Long oldMurId;
    private String oldAssignedDepartment;
}
