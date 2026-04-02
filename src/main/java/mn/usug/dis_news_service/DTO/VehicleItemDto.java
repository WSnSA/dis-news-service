package mn.usug.dis_news_service.DTO;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VehicleItemDto {
    private Integer vehicleTypeId;
    private String vehicleTypeName;
    private Integer qty;
    private String otherText;
}
