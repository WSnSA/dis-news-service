package mn.usug.dis_news_service.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "vehicle_driver")
@Data
public class VehicleDriver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    @Column(name = "driver_id", nullable = false)
    private Long driverId;

    @Column(name = "note", length = 100)
    private String note;

    /* Хариуд дүүргэхэд ашиглана */
    @Transient
    private String driverName;

    @Transient
    private String driverPhone;
}
