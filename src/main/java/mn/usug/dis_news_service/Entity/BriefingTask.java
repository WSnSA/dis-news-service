package mn.usug.dis_news_service.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Шуурхай хурлын үүрэг даалгавар.
 * Хуучин {@link Task}-тай огт хамаагүй, бүрэн шинэ бүтэц.
 * Нэг үүрэг олон 7-хоногийн cycle (сунгалт) дамжин нэг мөрөнд хадгалагдана.
 */
@Entity
@Table(name = "briefing_task")
@Data
public class BriefingTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** Үүрэг өгсөн ба биелэлтийг дүгнэх албан тушаалтан (users.id, can_assign_task=1) */
    @Column(name = "assigner_id")
    private Integer assignerId;

    @Column(name = "description", length = 2000)
    private String description;

    /** 0=идэвхтэй, 1=бүрэн биелсэн (дүн=100) */
    @Column(name = "status")
    private Integer status;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @Column(name = "active_flag")
    private Integer activeFlag;
}
