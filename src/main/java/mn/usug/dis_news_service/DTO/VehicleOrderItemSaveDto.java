package mn.usug.dis_news_service.DTO;

import lombok.Data;

@Data
public class VehicleOrderItemSaveDto {

    private Integer vehicleTypeId;

    // Ердийн машинд
    private Integer qty;

    // "Бусад"-д
    private String otherText;
}
