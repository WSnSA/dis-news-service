package mn.usug.dis_news_service.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Машин хуваарилалтын НЭГ ӨДРИЙН цуцлалт.
 *
 * Олон өдрийн захиалгад оногдсон машиныг бүх хугацаагаар нь устгалгүйгээр
 * зөвхөн нэг өдөр чөлөөлөхөд ашиглана. Тухайн өдөр машин захиалгын
 * жагсаалтад дахин сул болж харагдана.
 */
@Entity
@Table(name = "vehicles_to_out_cancel")
@Data
public class VehiclesToOutCancel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "vehicles_to_out_id", nullable = false)
    private Integer vehiclesToOutId;

    @Column(name = "cancel_date", nullable = false)
    private LocalDate cancelDate;

    @Column(name = "reason", length = 500, nullable = false)
    private String reason;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "created_date")
    private LocalDateTime createdDate;
}
