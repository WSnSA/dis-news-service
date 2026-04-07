package mn.usug.dis_news_service.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "task_departments")
@Data
public class TaskDepartment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "task_id", nullable = false)
    private Integer taskId;

    @Column(name = "department_id", nullable = false)
    private Integer departmentId;

    @Column(name = "fulfillment", columnDefinition = "TEXT")
    private String fulfillment;

    @Column(name = "status", nullable = false)
    private Integer status = 0;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
}
