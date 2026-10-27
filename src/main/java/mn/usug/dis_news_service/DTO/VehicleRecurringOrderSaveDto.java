package mn.usug.dis_news_service.DTO;

import lombok.Data;

import java.util.List;

/** Давтамжит захиалгын загвар хадгалах/засах хүсэлт. */
@Data
public class VehicleRecurringOrderSaveDto {

    private Long id;                       // null = шинэ

    private String workDescription;
    private Integer assignedDepartmentId;
    private Integer assignedEmployeeId;

    /** 0 = Механизм, 1 = Суудлын машин */
    private Integer orderType;

    // Суудлын машин талбарууд
    private String pickupLocation;
    private String dropoffLocation;
    private Integer passengerCount;
    private String requestedTime;

    /** Давтагдах гаригууд ISO дугаараар (Даваа=1 ... Баасан=5) */
    private List<Integer> weekdays;

    /** Идэвхтэй эсэх (null бол 1) */
    private Integer activeFlag;

    private List<VehicleOrderItemSaveDto> vehicles;
}
