package mn.usug.dis_news_service.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Суудлын машины захиалгад албаны дотоод баталгаажуулалтыг алгасах эрхтэй хэрэглэгч.
 * Өмнө нь application.properties (vehicle-order.dept-approval-skip.user-ids)-д хатуу
 * бичигддэг байсныг "Эрхийн удирдлага" цэснээс удирддаг болгов.
 */
@Entity
@Table(name = "vehicle_approval_skip")
@Data
public class VehicleApprovalSkip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", unique = true)
    private Integer userId;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "created_date")
    private LocalDateTime createdDate;
}
