package mn.usug.dis_news_service.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "vehicle_order_item")
@Data
public class VehicleOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_order_id", nullable = false)
    private VehicleOrder vehicleOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_type_id")
    private VehicleType vehicleType;

    @Column(name = "qty")
    private Integer qty;   // ← nullable

    @Column(name = "other_text", length = 500)
    private String otherText;

    @Column(name = "created_date")
    private LocalDateTime createdDate;
}
