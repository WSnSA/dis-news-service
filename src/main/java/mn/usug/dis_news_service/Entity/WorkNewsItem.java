package mn.usug.dis_news_service.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "work_news_item",
        indexes = {
                @Index(name = "idx_work_news_item_day_sort", columnList = "day_id,sort_order"),
                @Index(name = "idx_work_news_item_legacy", columnList = "legacy_id"),
                @Index(name = "idx_work_news_item_department", columnList = "department_id"),
                @Index(name = "idx_work_news_item_day_department", columnList = "day_id,department_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class WorkNewsItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "day_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_work_news_item_day")
    )
    private WorkNewsDay day;

    @Column(name = "legacy_id")
    private Long legacyId;

    @Column(name = "item_type", nullable = false, length = 40)
    private String itemType = "TASK";

    @Column(name = "title")
    private String title;

    @Lob
    @Column(name = "content", nullable = false)
    private String content; // workDescription

    @Lob
    @Column(name = "meta_json")
    private String metaJson;

    @Column(name = "department_id", nullable = false)
    private Long departmentId;

    @Column(name = "position_id")
    private Long positionId;

    @Column(name = "assigned_position_name", length = 255)
    private String assignedPositionName;

    @Column(name = "deadline_date")
    private LocalDate deadlineDate;

    @Lob
    @Column(name = "fulfillment")
    private String fulfillment;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(name = "status", nullable = false)
    private Integer status = 1;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Long updatedBy;
}