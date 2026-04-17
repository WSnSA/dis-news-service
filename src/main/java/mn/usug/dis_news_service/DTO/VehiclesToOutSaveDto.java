package mn.usug.dis_news_service.DTO;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class VehiclesToOutSaveDto {

    private Integer vehicleOrderId;

    @JsonAlias({"assignedDepartmentName"})
    private String department;

    private String workDescription;

    @JsonAlias({"vehicleTypeName"})
    private String vehicleMechanism;

    @JsonAlias({"vehicleRegistrationNumber"})
    private String vehicleRegistration;

    @JsonAlias({"driverPhoneNumber"})
    private String phone;

    private String driverName;
}
