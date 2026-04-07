package mn.usug.dis_news_service.DAO;

import mn.usug.dis_news_service.Entity.TaskDepartment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface TaskDepartmentRepository extends JpaRepository<TaskDepartment, Integer> {

    List<TaskDepartment> findByTaskId(Integer taskId);

    List<TaskDepartment> findByTaskIdIn(List<Integer> taskIds);

    Optional<TaskDepartment> findByTaskIdAndDepartmentId(Integer taskId, Integer departmentId);

    @Query("SELECT DISTINCT td.taskId FROM TaskDepartment td WHERE td.departmentId = :deptId")
    List<Integer> findTaskIdsByDept(@Param("deptId") Integer deptId);

    @Modifying
    @Transactional
    void deleteByTaskId(Integer taskId);
}
