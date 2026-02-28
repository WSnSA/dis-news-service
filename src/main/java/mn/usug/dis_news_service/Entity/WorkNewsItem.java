package mn.usug.dis_news_service.Entity;
// WorkNewsItem.java
import jakarta.persistence.*;
import java.time.*;
import lombok.*;

@Entity
@Table(name = "work_news_item",
        indexes = {
                @Index(name="idx_work_news_item_day_sort", columnList="day_id,sort_order"),
                @Index(name="idx_work_news_item_legacy", columnList="legacy_id")
        })
@Getter @Setter @NoArgsConstructor
public class WorkNewsItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "day_id", nullable = false,
            foreignKey = @ForeignKey(name="fk_work_news_item_day"))
    private WorkNewsDay day;

    @Column(name = "legacy_id")
    private Long legacyId;

    @Column(name = "item_type", nullable = false, length = 40)
    private String itemType = "OTHER";

    @Column(name = "title")
    private String title;

    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    // JSON type ашиглахгүйгээр эхлээд String болгож болно
    @Lob
    @Column(name = "meta_json")
    private String metaJson;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;
}