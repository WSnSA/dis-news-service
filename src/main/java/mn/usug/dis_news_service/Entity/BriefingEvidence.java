package mn.usug.dis_news_service.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Нотлох баримт — folder доторх нэг файл.
 * Бодит файл нь file service-д (bucket=disnews) хадгалагдана; энд зөвхөн objectName + meta.
 * parent = folder_id ({@link BriefingFulfillment#getFolderId()}).
 */
@Entity
@Table(name = "briefing_evidence")
@Data
public class BriefingEvidence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "folder_id", length = 64)
    private String folderId;

    /** file service upload-аас буцсан objectName / UUID */
    @Column(name = "object_name", length = 255)
    private String objectName;

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
