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

    /** 0=механизм  1=суудлын машин */
    private Integer orderType;

    private String workDescription;
    private Integer assignedDepartmentId;
    private String assignedDepartmentName;

    /* Суудлын машин захиалгын нэмэлт талбарууд */
    private String  pickupLocation;
    private String  dropoffLocation;
    private Integer passengerCount;
    private String  requestedTime;

    private List<VehicleItemDto> vehicles;
}

