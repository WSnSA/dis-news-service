package mn.usug.dis_news_service.DTO;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class VehicleOrderDto {

    private Long id;
    private LocalDate orderDate;

    private String workDescription;
    private String assignedDepartmentName;

    private List<VehicleItemDto> vehicles;
}

