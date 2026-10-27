package mn.usug.dis_news_service.DTO;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/** Давтамжит захиалгын загвар (frontend-д бүрэн харуулах). */
@Data
public class VehicleRecurringOrderDto {

    private Long id;
    private String workDescription;

    private Integer assignedDepartmentId;
    private String  assignedDepartmentName;
    private Integer assignedEmployeeId;
    private String  assignedEmployeeName;

    private Integer orderType;              // 0=Механизм, 1=Суудлын машин
    private String  pickupLocation;
    private String  dropoffLocation;
    private Integer passengerCount;
    private String  requestedTime;

    private List<Integer> weekdays;         // [1,2,3,4,5]
    private Integer activeFlag;

    private Integer createdBy;
    private String  createdByName;
    private LocalDateTime createdDate;

    private List<Item> items;

    @Data
    public static class Item {
        private Integer vehicleTypeId;
        private String  vehicleTypeName;
        private Integer qty;
        private String  otherText;
    }
}
