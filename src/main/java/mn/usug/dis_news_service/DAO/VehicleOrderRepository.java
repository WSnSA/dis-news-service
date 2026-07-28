package mn.usug.dis_news_service.DAO;

import mn.usug.dis_news_service.Entity.VehicleOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VehicleOrderRepository extends JpaRepository<VehicleOrder, Long> {

    @Query("""
        select v
        from VehicleOrder v
        where v.activeFlag = 1
          and (
            (v.startDate is null and v.orderDate = :date)
            or (v.startDate is not null and :date between v.startDate and v.endDate)
          )
        order by v.createdDate
    """)
    List<VehicleOrder> findByDate(@Param("date") LocalDate date);

    @Query("""
        select v
        from VehicleOrder v
        where v.activeFlag = 1
          and v.status = 0
          and (v.orderType is null or v.orderType <> 1 or v.deptApproved = true)
          and (v.startDate is null and v.orderDate = :date or v.startDate = :date)
        order by v.assignedDepartmentId, v.createdDate
    """)
    List<VehicleOrder> findPendingByDate(@Param("date") LocalDate date);

    /** Суудлын машин — албаны баталгаажуулалт хүлээж буй */
    @Query("""
        select v
        from VehicleOrder v
        where v.activeFlag = 1
          and v.status = 0
          and v.orderType = 1
          and (v.deptApproved is null or v.deptApproved = false)
          and (v.startDate is null and v.orderDate = :date or v.startDate = :date)
        order by v.assignedDepartmentId, v.createdDate
    """)
    List<VehicleOrder> findDeptPendingByDate(@Param("date") LocalDate date);

    /** Суудлын машин — албаны баталгаажуулалт хүлээж буй БҮХ хүсэлт (огноогоор шүүхгүй, өдрөөр эрэмбэлсэн) */
    @Query("""
        select v
        from VehicleOrder v
        where v.activeFlag = 1
          and v.status = 0
          and v.orderType = 1
          and (v.deptApproved is null or v.deptApproved = false)
        order by coalesce(v.startDate, v.orderDate), v.assignedDepartmentId, v.createdDate
    """)
    List<VehicleOrder> findAllDeptPending();

    /**
     * Баталгаажсан (status=1) БОЛОН боломжгүй болгосон (status=3) захиалгыг захиалсан өдрөөр нь буцаана.
     * Боломжгүй болгосон захиалга захиалсан хугацаандаа хэвээр харагдана — машин сул гарвал
     * тухайн өдөр нь дахин хуваарилах боломжтой.
     * Эрэмбэ: эхэлж баталгаажсан (1), дараа нь боломжгүй (3).
     */
    @Query("""
        select v
        from VehicleOrder v
        where v.activeFlag = 1
          and v.status in (1, 3)
          and (v.startDate is null and v.orderDate = :date or v.startDate = :date)
        order by v.status, v.assignedDepartmentId, v.createdDate
    """)
    List<VehicleOrder> findConfirmedByDate(@Param("date") LocalDate date);

    /**
     * Суудлын машины (order_type=1) захиалгыг өдрөөр нь бүлэглэн статус тус бүрийн тоог буцаана.
     * Өдрийг start_date (байхгүй бол order_date)-аар авна.
     * → [date, total, dispatched(2), pending(0), confirmed(1), declined(3)]
     */
    @Query(value = """
        SELECT DATE(COALESCE(v.start_date, v.order_date)) AS d,
               COUNT(*) AS total,
               SUM(v.status = 2) AS dispatched,
               SUM(v.status = 0) AS pending,
               SUM(v.status = 1) AS confirmed,
               SUM(v.status = 3) AS declined
        FROM vehicle_order v
        WHERE v.active_flag = 1 AND v.order_type = 1
          AND DATE(COALESCE(v.start_date, v.order_date)) BETWEEN :from AND :to
        GROUP BY DATE(COALESCE(v.start_date, v.order_date))
        ORDER BY d
    """, nativeQuery = true)
    List<Object[]> carDispatchStats(@Param("from") LocalDate from, @Param("to") LocalDate to);
}

