package mn.usug.dis_news_service.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Шуурхай зөвлөгөөн (§3.1).
 * Нэг 7 хоногт нэг хурал (Мягмар). Хуралдааны дугаар + ерөнхий мэдээлэлтэй.
 * Үүрэг (briefing_cycle) болон шуурхайн мэдээ (briefing_news) тус хуралд холбогдоно.
 */
@Entity
@Table(name = "briefing_meeting")
@Data
public class BriefingMeeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** 7 хоногийн Мягмар */
    @Column(name = "meeting_date")
    private LocalDate meetingDate;

    /** Хуралдааны дугаар (заавал) */
    @Column(name = "meeting_no", length = 32)
    private String meetingNo;

    /** Шуурхайн ерөнхий мэдээлэл */
    @Column(name = "summary", length = 4000)
    private String summary;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "active_flag")
    private Integer activeFlag;
}
