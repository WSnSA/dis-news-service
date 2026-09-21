package mn.usug.dis_news_service.Entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Засварын зураг. Файл нь гадаад file service дээр хадгалагдана —
 * энд зөвхөн лавлах мэдээллийг (object_name) хадгална.
 */
@Entity
@Table(name = "vehicle_repair_photo")
@Data
@EntityListeners(AuditingEntityListener.class)
public class VehicleRepairPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_repair_id", nullable = false)
    private Long vehicleRepairId;

    /** File service дээрх нэр (uuid) */
    @Column(name = "object_name", nullable = false, length = 300)
    private String objectName;

    @Column(name = "file_name", length = 300)
    private String fileName;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "note", length = 300)
    private String note;

    @Column(name = "active_flag", nullable = false)
    private Integer activeFlag = 1;

    @CreatedDate @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;
    @CreatedBy   @Column(name = "created_by", updatable = false) private Integer createdBy;
}
