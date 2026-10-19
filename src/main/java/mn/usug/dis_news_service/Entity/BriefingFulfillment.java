package mn.usug.dis_news_service.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Биелэлт — cycle × холбогдох алба бүрд нэг мөр.
 * folder_id нь нотлох баримтын folder (generated UUID); файлууд {@link BriefingEvidence}-д хадгалагдана.
 */
@Entity
@Table(name = "briefing_fulfillment")
@Data
public class BriefingFulfillment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "cycle_id")
    private Integer cycleId;

    @Column(name = "department_id")
    private Integer departmentId;

    /** Хийгдсэн ажлын тайлбар */
    @Column(name = "work_text", length = 2000)
    private String workText;

    /**
     * 0=ороогүй(draft), 1=илгээгдсэн(шалгаж байгаа), 2=буцаагдсан (§3.3).
     * DB-д NOT NULL багана тул анхдагч утгыг энд өгнө — шинэ мөр үүсгэхэд
     * санамсаргүй null үлдэж INSERT унахаас сэргийлнэ.
     */
    @Column(name = "status", nullable = false)
    private Integer status = 0;

    /**
     * Тухайн нэгжийн гүйцэтгэлийн үнэлгээ 0-100 (NULL=дүгнээгүй).
     * Үүрэг өгсөн удирдлага нэгж (ЗБН) тус бүрийн хавсралтыг нягтлаад тус тусад нь өгнө.
     * Cycle-ийн ерөнхий дүн нь илгээгдсэн нэгжүүдийн энэ оноонуудын дундаж.
     */
    @Column(name = "score")
    private Integer score;

    /** Нэгжийн дүгнэлт/тайлбар — хувьтай хамт ЗААВАЛ бичигдэнэ */
    @Column(name = "score_comment", length = 2000)
    private String scoreComment;

    @Column(name = "scored_by")
    private Integer scoredBy;

    @Column(name = "scored_at")
    private LocalDateTime scoredAt;

    /** Буцаах тайлбар (удирдлага) */
    @Column(name = "return_comment", length = 2000)
    private String returnComment;

    @Column(name = "returned_at")
    private LocalDateTime returnedAt;

    @Column(name = "returned_by")
    private Integer returnedBy;

    /** Нотлох баримтын folder (generated UUID) — fulfillment үүсэхэд автоматаар онооно */
    @Column(name = "folder_id", length = 64)
    private String folderId;

    @Column(name = "submitted_by")
    private Integer submittedBy;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
