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

    private List<VehicleOrderItemSaveDto> vehicles;
}

