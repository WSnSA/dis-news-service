package mn.usug.dis_news_service.Model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class VehiclesToOutRowDto {
    private Integer id;

    // Grid баганууд
    private String department;            // zahialga_ogson_heltes
    private String workDescription;       // hiigdeh_ajil
    private String vehicleMechanism;      // mashin_mehanizm
    private String vehicleRegistration;   // vehicleRegistrationNumber эсвэл vehicle_reg_raw
    private String phone;                 // driverPhoneNumber эсвэл phone_raw
    private String driverName;            // driverName
    private LocalDateTime createdDate;
    private String orderCreatedByName;    // vehicle_order.created_by → users.first_name
}
