package mn.usug.dis_news_service.Entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "vehicle_insurance")
@EntityListeners(AuditingEntityListener.class)
public class VehicleInsurance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String plateNumber;

    @Column(length = 50)
    private String policyNumber;

    @Column(length = 100)
    private String insuranceCompany;

    private LocalDateTime expireDate;
    private Boolean isActive;
    private LocalDateTime checkedAt;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
