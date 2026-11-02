package mn.usug.dis_news_service.Entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Давтамжит захиалгын загварын тээврийн төрөл/тоо (vehicle_order_item-ийн загвар хувилбар).
 */
@Entity
@Table(name = "vehicle_recurring_order_item")
@Data
public class VehicleRecurringOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recurring_id")
    private Long recurringId;

    @Column(name = "vehicle_type_id")
    private Integer vehicleTypeId;

    @Column(name = "qty")
    private Integer qty;

    @Column(name = "other_text", length = 500)
    private String otherText;
}
