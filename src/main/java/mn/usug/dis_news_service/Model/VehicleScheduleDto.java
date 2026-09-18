package mn.usug.dis_news_service.Model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * Нэг машины ИРЭЭДҮЙН нэг захиалга (хуваарийн мөр).
 *
 * "Ажилд гарах" таб нь нэг өдрийг харуулдаг тул 15-19-нд захиалсан машиныг
 * олохын тулд өдөр бүрийг гүйлгэх шаардлагатай байсан. Энэ DTO нь машин
 * тус бүрийн урагшаа хамаарах бүх захиалгыг нэг дор буцаана.
 */
@Data
@Builder
public class VehicleScheduleDto {

    private Integer id;                 // vehicles_to_out.id
    private String plate;               // улсын дугаар — бүлэглэх түлхүүр
    private String mechanism;
    private String driverName;
    private String phone;

    private Integer vehicleOrderId;
    private String department;
    private String workDescription;
    private Integer orderType;          // 0=механизм, 1=суудлын

    private LocalDate startDate;
    private LocalDate endDate;

    /** Тухайн захиалгын доторх цуцлагдсан өдрүүд — тэр өдрүүдэд машин сул */
    private List<LocalDate> cancelledDates;
}
