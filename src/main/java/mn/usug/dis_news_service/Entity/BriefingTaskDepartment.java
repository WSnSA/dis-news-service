package mn.usug.dis_news_service.Entity;

import jakarta.persistence.*;
import lombok.Data;

/** Үүрэг даалгаварт холбогдох алба (1-с доошгүй, тогтмол). */
@Entity
@Table(name = "briefing_task_department")
@Data
public class BriefingTaskDepartment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "task_id")
    private Integer taskId;

    @Column(name = "department_id")
    private Integer departmentId;
}
