package mn.usug.dis_news_service.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Шуурхайн мэдээний нотлох баримт (фото/файл эсвэл линк).
 * evidenceType: FILE (file service objectName) / LINK (видео/cloud холбоос).
 */
@Entity
@Table(name = "briefing_news_evidence")
@Data
public class BriefingNewsEvidence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "folder_id", length = 64)
    private String folderId;

    @Column(name = "object_name", length = 255)
    private String objectName;

    @Column(name = "link_url", length = 1000)
    private String linkUrl;

    @Column(name = "evidence_type", length = 20)
    private String evidenceType;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "content_type", length = 120)
    private String contentType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "uploaded_by")
    private Integer uploadedBy;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;
}
