package mn.usug.dis_news_service.Entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "vehicle_penalty")
@EntityListeners(AuditingEntityListener.class)
public class VehiclePenalty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String plateNumber;

    @Column(nullable = false, unique = true, length = 60)
    private String barCode;

    private Integer amount;

    @Column(length = 300)
    private String localName;

    private Boolean isPaid;
    private LocalDateTime passDate;

    @Column(length = 200)
    private String reasonType;

    private Integer reasonTypeCode;
    private LocalDateTime checkedAt;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Transient
    private String vehicleName;
}
