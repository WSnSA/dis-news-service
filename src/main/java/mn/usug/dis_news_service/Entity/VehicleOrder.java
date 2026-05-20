package mn.usug.dis_news_service.Entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "vehicle_order")
@Data
@EntityListeners(AuditingEntityListener.class)
public class VehicleOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String workDescription;

    private Integer assignedDepartmentId;
    private Integer assignedEmployeeId;

    private LocalDate orderDate;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "task_duration", length = 100)
    private String taskDuration;

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

    /** 0=хүлээгдэж байна  1=баталгаажсан  2=хуваарилагдсан  3=боломжгүй */
    private Integer status;

    /** Суудлын машин (orderType=1) — албаны дотоод баталгаажуулалт. true болсны дараа автобаазын pending-д орно */
    @Column(name = "dept_approved")
    private Boolean deptApproved;

    /** Албаны баталгаажуулсан хэрэглэгчийн id */
    @Column(name = "dept_approved_by")
    private Integer deptApprovedBy;

    /** status=3 үед автобаазаас ирсэн шалтгаан */
    @Column(name = "decline_reason", columnDefinition = "TEXT")
    private String declineReason;

    private Integer activeFlag;

    @CreatedDate
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private Integer createdBy;

    @LastModifiedDate
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @LastModifiedBy
    @Column(name = "updated_by")
    private Integer updatedBy;

    // хуучин систем
    private Long oldMurId;
    private String oldAssignedDepartment;
}
