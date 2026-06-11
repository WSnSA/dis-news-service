package mn.usug.dis_news_service.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Шуурхайн мэдээ (§3.4) — хурлаас өмнө нэгж бүр 7 хоногийн онцлох ажлаа оруулна.
 * Нэг хурал × нэг нэгж = нэг мөр (unique).
 */
@Entity
@Table(name = "briefing_news")
@Data
public class BriefingNews {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** briefing_meeting.id */
    @Column(name = "meeting_id")
    private Integer meetingId;

    /** briefing_unit.id */
    @Column(name = "unit_id")
    private Integer unitId;

    /** 7 хоногт хийсэн ажлын товч тайлбар */
    @Column(name = "summary_text", length = 4000)
    private String summaryText;

    /** Үр дүн */
    @Column(name = "result", length = 4000)
    private String result;

    /** Нэмэлт санал */
    @Column(name = "extra_proposal", length = 4000)
    private String extraProposal;

    /** Фото/файлын folder (generated UUID) */
    @Column(name = "folder_id", length = 64)
    private String folderId;

    /** 0=draft, 1=submitted */
    @Column(name = "status")
    private Integer status;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
