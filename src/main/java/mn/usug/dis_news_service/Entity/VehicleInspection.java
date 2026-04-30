package mn.usug.dis_news_service.Entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "vehicle_inspection")
@EntityListeners(AuditingEntityListener.class)
public class VehicleInspection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String plateNumber;

    private LocalDate inspectedDate;
    private LocalDate expireDate;
    private Boolean isPassed;
    private LocalDateTime checkedAt;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
