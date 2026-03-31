package mn.usug.dis_news_service.DTO;

import lombok.Data;

@Data
public class VehiclesToOutSaveDto {

    private Integer vehicleOrderId;

    /** Захиалга дээрх алба (frontend-с хуулж ирнэ) */
    private String department;

    /** Хаана, ямар ажил */
    private String workDescription;

    /** Машин/механизмын нэр (форматлагдсан) */
    private String vehicleMechanism;

    /** Улсын дугаар */
    private String vehicleRegistration;

    /** Жолоочийн утас */
    private String phone;

    /** Ажилд гарч буй ажилтны нэр */
    private String driverName;
}
