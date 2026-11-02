package mn.usug.dis_news_service.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Давтамжит (долоо хоног тутмын) машин/механизм захиалгын ЗАГВАР.
 * Долоо хоног бүрийн Баасан гарагт scheduler ажиллаж, идэвхтэй загвар бүрээс
 * ирэх 7 хоногийн (Даваа–Баасан) сонгосон гаригуудад vehicle_order мөр үүсгэнэ.
 */
@Entity
@Table(name = "vehicle_recurring_order")
@Data
public class VehicleRecurringOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String workDescription;

    @Column(name = "assigned_department_id")
    private Integer assignedDepartmentId;

    @Column(name = "assigned_employee_id")
    private Integer assignedEmployeeId;

    /** 0 = Механизм захиалга, 1 = Суудлын машин захиалга */
    @Column(name = "order_type")
    private Integer orderType;

    /** Суудлын машин: эхлэх цэг */
    @Column(name = "pickup_location", length = 200)
    private String pickupLocation;

    /** Суудлын машин: очих цэг */
    @Column(name = "dropoff_location", length = 200)
    private String dropoffLocation;

    /** Суудлын машин: зорчигчийн тоо */
    @Column(name = "passenger_count")
    private Integer passengerCount;

    /** Суудлын машин: хэрэгтэй цаг (жш: "09:30") */
    @Column(name = "requested_time", length = 20)
    private String requestedTime;

    /**
     * Давтагдах гаригууд — ISO өдрийн дугаарыг таслалаар (Даваа=1 ... Баасан=5).
     * Жишээ: "1,2,3,4,5" = Даваа–Баасан; "1,3,5" = Даваа/Лхагва/Баасан.
     */
    @Column(name = "weekdays", length = 20)
    private String weekdays;

    /** 1=идэвхтэй, 0=идэвхгүй (устгасан — soft delete) */
    @Column(name = "active_flag")
    private Integer activeFlag;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
}
