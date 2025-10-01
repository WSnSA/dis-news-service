package mn.usug.dis_news_service.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data

@Entity
@Table(name = "vehicles_to_out")
public class VehiclesToOut {
    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "vehicle_order_id")
    private Integer vehicleOrderId;

    @Column(name = "order_code")
    private Integer orderCode;

    @Column(name = "driver_name")
    private Integer driverName;

    @Column(name = "driver_phone_number")
    private Integer driverPhoneNumber;

    @Column(name = "vehicle_registration_number")
    private Integer vehicleRegistrationNumber;

    @Column(name = "vehicle_type_id")
    private Integer vehicleTypeId;

    @Column(name = "count")
    private Integer count;

    @Column(name = "active_flag")
    private Integer activeFlag;

    @Column(name = "status")
    private Integer status;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "created_date")
    private Integer createdDate;

    @Column(name = "updated_date")
    private Integer updatedDate;

    @Column(name = "updated_by")
    private Integer updatedBy;

}