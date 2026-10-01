package mn.usug.dis_news_service.Model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class VehiclesToOutRowDto {
    private Integer id;

    // Grid баганууд
    private String department;            // zahialga_ogson_heltes
    private String workDescription;       // hiigdeh_ajil
    private String vehicleMechanism;      // mashin_mehanizm
    private String vehicleRegistration;   // vehicleRegistrationNumber эсвэл vehicle_reg_raw
    private String phone;                 // driverPhoneNumber эсвэл phone_raw
    private String driverName;            // driverName
    private LocalDateTime createdDate;
    private String orderCreatedByName;    // vehicle_order.created_by → users.first_name

    /** vehicle_order.id — frontend дээр group хийхэд хэрэгтэй */
    private Integer vehicleOrderId;
    /** 0=механизм, 1=суудлын — frontend дээр төрлөөр шүүхэд хэрэгтэй */
    private Integer orderType;

    /* ── Захиалгын хамрах хугацаа (устгах диалогт "хэзээнээс хэзээ" харуулна) ── */
    private java.time.LocalDate startDate;
    private java.time.LocalDate endDate;

    /* ── Хүссэн өдрийн цуцлалт (нэг өдрөөр чөлөөлсөн эсэх) ── */
    /** Тухайн өдөр цуцлагдсан эсэх — true бол машин тэр өдөр сул гэж тооцогдоно */
    private boolean cancelled;
    private String cancelReason;
    private String cancelledByName;
    private LocalDateTime cancelledAt;

    /** Энэ хуваарилалтын бүх цуцлагдсан өдрүүд (by-order дээр нөхөгдөнө) — олон өдрийн захиалгад аль өдөр цуцлагдсаныг харуулна */
    private List<CancelDay> cancellations;

    @Data
    @Builder
    public static class CancelDay {
        private LocalDate date;
        private String reason;
        private String cancelledByName;
    }
}
