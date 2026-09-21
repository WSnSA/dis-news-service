package mn.usug.dis_news_service.Entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Засварын хэсгийн өдөр тутмын ирц.
 *
 * Өдөр тутмын мэдээний эхний хоёр мөр үүнээс бүрдэнэ:
 *   "АВТО БААЗ ДЭЭР-:7 … нар ажиллаж байна"
 *   "Өвчтэй-1: … Нөхөн амралт-2: …"
 */
@Entity
@Table(name = "repair_attendance")
@Data
@EntityListeners(AuditingEntityListener.class)
public class RepairAttendance {

    public static final int WORKED = 1;
    public static final int SICK   = 2;
    public static final int LEAVE  = 3;
    public static final int OFF    = 4;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(name = "repair_worker_id", nullable = false)
    private Long repairWorkerId;

    /** 1=ажилласан, 2=өвчтэй, 3=нөхөн амралт, 4=чөлөөтэй */
    @Column(name = "status", nullable = false)
    private Integer status = WORKED;

    @Column(name = "note", length = 300)
    private String note;

    @Column(name = "active_flag", nullable = false)
    private Integer activeFlag = 1;

    @CreatedDate @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;
    @CreatedBy   @Column(name = "created_by", updatable = false) private Integer createdBy;
    @LastModifiedDate @Column(name = "updated_at") private LocalDateTime updatedAt;
    @LastModifiedBy   @Column(name = "updated_by") private Integer updatedBy;

    /* Тайланд харуулах нэр — join-оор нөхнө */
    @Transient private String workerName;
    @Transient private String specialtyName;
}
