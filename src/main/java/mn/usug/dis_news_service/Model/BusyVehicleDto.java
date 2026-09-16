package mn.usug.dis_news_service.Model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

/**
 * Тухайн захиалгын хугацаанд аль хэдийн өөр захиалгад оногдсон машин.
 * Хуваарилах формд уг машиныг жагсаалтаас хасахад ашиглана.
 */
@Data
@AllArgsConstructor
public class BusyVehicleDto {

    /** Улсын дугаар — жагсаалтаас хасах түлхүүр */
    private String plate;

    /** Аль захиалга эзэлж байгаа */
    private Integer vehicleOrderId;
    private String department;
    private String workDescription;

    private LocalDate startDate;
    private LocalDate endDate;

    /** 1=Өглөө, 2=Өдөр, 3=Бүтэн өдөр */
    private Integer timeSlot;
    private String timeSlotLabel;
}
