package mn.usug.dis_news_service.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/** "Сарын мэдээ"-ний мөр × он × сарын төлөвлөгөө (м3) */
@Data
@Entity
@Table(name = "monthly_report_plan")
public class MonthlyReportPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "row_id", nullable = false)
    private Integer rowId;

    @Column(name = "plan_year", nullable = false)
    private Integer planYear;

    @Column(name = "plan_month", nullable = false)
    private Integer planMonth;

    /** NULL = оруулаагүй */
    @Column(name = "plan_value")
    private Long planValue;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
}
