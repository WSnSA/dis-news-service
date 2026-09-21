package mn.usug.dis_news_service.Model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * Нэг жолоочийн ажлын түүх — хэзээ, ямар машинаар, аль албанд гарсан.
 *
 * Огноо нь `vehicle_order`-оос ирдэг тул захиалгагүй хуучин хуваарилалт
 * (vehicle_order_id NULL) энд ОРОХГҮЙ. Мөн нэрээр нь ганцаархан таарахгүй
 * байсан хуучин мөрүүд driver_id-гүй тул мөн орохгүй.
 */
@Data
@Builder
public class DriverHistoryDto {

    private Long driverId;
    private String driverName;
    private String phone;
    private Integer activeFlag;

    /** Хуваарилалтын тоо (нэг захиалга = нэг мөр) */
    private int tripCount;

    /** Ажилласан өдрийн тоо — цуцалсан өдрийг хассан, давхардалгүй */
    private int workedDays;

    /** Хэдэн өөр машин барьсан */
    private int vehicleCount;

    private List<Trip> trips;

    @Data
    @Builder
    public static class Trip {
        private Integer id;
        private String plateNumber;
        private String vehicleText;
        private String department;
        private String workDescription;
        private LocalDate startDate;
        private LocalDate endDate;
        /** Энэ хуваарилалтын цуцлагдсан өдрүүд */
        private List<LocalDate> cancelledDates;
        /** Цуцлалт хассан ажилласан өдрийн тоо */
        private int days;
    }
}
