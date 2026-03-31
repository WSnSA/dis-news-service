package mn.usug.dis_news_service.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "vehicles_to_out")
public class VehiclesToOut {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "vehicle_order_id")
    private Integer vehicleOrderId;

    @Column(name = "department")
    private String department;

    @Column(name = "work_description", columnDefinition = "TEXT")
    private String workDescription;

    @Column(name = "vehicle_mechanism", length = 500)
    private String vehicleMechanism;

    @Column(name = "order_code")
    private Integer orderCode;

    @Column(name = "driver_name")
    private String driverName;

    @Column(name = "driver_phone_number", length = 30)
    private String driverPhoneNumber;

    @Column(name = "vehicle_registration_number", length = 50)
    private String vehicleRegistrationNumber;

    @Column(name = "vehicle_type_id")
    private Integer vehicleTypeId;

    @Column(name = "qty")
    private Integer count;

    @Column(name = "active_flag")
    private Integer activeFlag;

    @Column(name = "status")
    private Integer status;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @Column(name = "legacy_data", columnDefinition = "json")
    private String legacyData;
}
