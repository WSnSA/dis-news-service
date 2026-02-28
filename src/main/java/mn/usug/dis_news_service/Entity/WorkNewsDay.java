package mn.usug.dis_news_service.Entity;
// WorkNewsDay.java
import jakarta.persistence.*;
import java.time.*;
import lombok.*;

@Entity
@Table(name = "work_news_day",
        uniqueConstraints = @UniqueConstraint(name="uq_work_news_day_date", columnNames = "news_date"),
        indexes = { @Index(name="idx_work_news_day_month_date", columnList="month_key,news_date") })
@Getter @Setter @NoArgsConstructor
public class WorkNewsDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "news_date", nullable = false)
    private LocalDate newsDate;

    @Column(name = "month_key", nullable = false, length = 7)
    private String monthKey; // YYYY/MM

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