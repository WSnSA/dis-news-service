package mn.usug.dis_news_service.DAO;

import mn.usug.dis_news_service.Entity.WorkOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface WorkOrderRepository extends JpaRepository<WorkOrder, Integer> {

    @Query("select w from WorkOrder w where w.activeFlag = 1 order by w.createdDate desc")
    List<WorkOrder> findActive();

    /* 🔹 Огноогоор шүүх */
    @Query("""
        select w from WorkOrder w
        where w.activeFlag = 1
          and date(w.createdDate) = :date
        order by w.createdDate desc
    """)
    List<WorkOrder> findByDate(LocalDate date);

    /* 🔹 Захиалга үүсгэсэн алба (assignedDepartmentId = миний алба → Үүсгэсэн tab) */
    @Query("select w from WorkOrder w where w.activeFlag = 1 and w.assignedDepartmentId = :deptId order by w.createdDate desc")
    List<WorkOrder> findByAssignedDept(@Param("deptId") Integer deptId);

    /* 🔹 Хийлгэх алба (departmentId = миний алба → Хүлээн авсан tab) */
    @Query("select w from WorkOrder w where w.activeFlag = 1 and w.departmentId = :deptId order by w.createdDate desc")
    List<WorkOrder> findByExecutorDept(@Param("deptId") Integer deptId);

    /* 🔹 Тухайн албаны дуусаагүй захиалгын тоо (хоёр tab хамт) */
    @Query("select count(w) from WorkOrder w where w.activeFlag = 1 and w.status < 2 and (w.assignedDepartmentId = :deptId or w.departmentId = :deptId)")
    long countPendingByDept(@Param("deptId") Integer deptId);

    /* 🔹 Нийт бүх дуусаагүй ажлын захиалга */
    @Query("select count(w) from WorkOrder w where w.activeFlag = 1 and w.status < 2")
    long countPendingAll();
}
