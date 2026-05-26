package mn.usug.dis_news_service.DAO;

import mn.usug.dis_news_service.Entity.BriefingTaskDepartment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface BriefingTaskDepartmentRepository extends JpaRepository<BriefingTaskDepartment, Integer> {

    List<BriefingTaskDepartment> findByTaskId(Integer taskId);

    List<BriefingTaskDepartment> findByTaskIdIn(List<Integer> taskIds);

    @Query("SELECT DISTINCT d.taskId FROM BriefingTaskDepartment d WHERE d.departmentId = :deptId")
    List<Integer> findTaskIdsByDept(@Param("deptId") Integer deptId);

    @Modifying
    @Transactional
    void deleteByTaskId(Integer taskId);
}
