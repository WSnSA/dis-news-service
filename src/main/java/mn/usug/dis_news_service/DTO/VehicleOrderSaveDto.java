package mn.usug.dis_news_service.DTO;


import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class VehicleOrderSaveDto {

    private String workDescription;
    private LocalDate orderDate;

    private LocalDate startDate;
    private LocalDate endDate;
    private String taskDuration;

    private Integer assignedDepartmentId;
    private Integer assignedEmployeeId;

    /** 0=механизм  1=суудлын машин */
    private Integer orderType;

    private String  pickupLocation;
    private String  dropoffLocation;
    private Integer passengerCount;
    private String  requestedTime;

    /** Ээлж: 1=Өглөө, 2=Өдөр, 3=Бүтэн өдөр. Хоосон бол бүтэн өдөр гэж үзнэ. */
    private Integer timeSlot;

    private List<VehicleOrderItemSaveDto> vehicles;
}

