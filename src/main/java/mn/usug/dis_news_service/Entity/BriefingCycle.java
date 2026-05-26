package mn.usug.dis_news_service.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Үүрэг даалгаврын 7 хоног тутмын мөчлөг.
 * Сунгах (биелээгүй) бүрд шинэ cycle нэмэгдэж, өмнөх cycle-ийн биелэлт/дүн түүх болж хадгалагдана.
 */
@Entity
@Table(name = "briefing_cycle")
@Data
public class BriefingCycle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "task_id")
    private Integer taskId;

    /** 1, 2, 3 ... сунгалтын дугаар */
    @Column(name = "cycle_no")
    private Integer cycleNo;

    /** Тухайн 7 хоногийн 2 дахь өдөр (Мягмар) — хурал болсон огноо */
    @Column(name = "meeting_date")
    private LocalDate meetingDate;

    /** Биелэлт оруулах эцсийн хугацаа — Баасан 16:00 */
    @Column(name = "submit_deadline")
    private LocalDateTime submitDeadline;

    /** Дүгнэх эцсийн хугацаа — дараа 7 хоногийн Даваа 14:00 */
    @Column(name = "score_deadline")
    private LocalDateTime scoreDeadline;

    /** Ерөнхий дүн 0-100 (NULL=дүгнээгүй) */
    @Column(name = "score")
    private Integer score;

    @Column(name = "scored_by")
    private Integer scoredBy;

    @Column(name = "scored_at")
    private LocalDateTime scoredAt;

    /** 0=нээлттэй, 1=дүгнэгдсэн */
    @Column(name = "status")
    private Integer status;

    @Column(name = "created_date")
    private LocalDateTime createdDate;
}
