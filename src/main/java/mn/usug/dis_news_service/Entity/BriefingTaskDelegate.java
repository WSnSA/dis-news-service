package mn.usug.dis_news_service.Entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Газрын даргын өмнөөс тухайн үүргийг дүгнэх эрх олгогдсон хэрэглэгч
 * (хэлтсийн дарга нар / Ерөнхий инженер). Үүрэг тус бүрд тохируулна.
 */
@Entity
@Table(name = "briefing_task_delegate")
@Data
public class BriefingTaskDelegate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "task_id")
    private Integer taskId;

    @Column(name = "user_id")
    private Integer userId;
}
