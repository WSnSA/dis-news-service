package mn.usug.dis_news_service.DTO;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class VehicleOrderDto {

    private Long id;
    private LocalDate orderDate;

    private LocalDate startDate;
    private LocalDate endDate;
    private String taskDuration;

    /** 0=хүлээгдэж байна  1=баталгаажсан  2=хуваарилагдсан */
    private Integer status;

    private String workDescription;
    private Integer assignedDepartmentId;
    private String assignedDepartmentName;

    private List<VehicleItemDto> vehicles;
}

