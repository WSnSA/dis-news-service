package mn.usug.dis_news_service.DAO;

import mn.usug.dis_news_service.Entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Integer> {

    // Алба + албан тушаалаар
    List<Task> findByDepartmentIdAndPositionId(Integer departmentId, Integer positionId);

    @Query("select t from Task t where t.activeFlag = 1 and t.departmentId = :depId and t.positionId = :posId")
    List<Task> findActiveByDeptAndPosition(@Param("depId") Integer depId, @Param("posId") Integer posId);

    @Query("select t from Task t where t.activeFlag = 1 and t.departmentId = :depId")
    List<Task> findActiveByDept(@Param("depId") Integer depId);

    // Тухайн өдрийн хурлын үүргүүд
    @Query("""
    select t from Task t
    where t.activeFlag = 1
      and DATE(t.createdDate) = :date
""")
    List<Task> findByCreatedDate(@Param("date") LocalDate date);


    // Албанд ирсэн идэвхтэй үүрэг
    List<Task> findByDepartmentIdAndActiveFlag(Integer departmentId, Integer activeFlag);

    // Статусаар
    List<Task> findByStatus(Integer status);

    @Query("select a from Task a where a.activeFlag = 1")
    List<Task> findActive();

    /** Нийт биелэгдээгүй үүрэг: task_departments бүхий шинэ + хуучин (departmentId-тай) */
    @Query(value = """
        SELECT COUNT(*) FROM (
            SELECT DISTINCT td.task_id AS id FROM task_departments td
            JOIN tasks t ON t.id = td.task_id WHERE t.active_flag = 1 AND td.status < 2
            UNION
            SELECT t2.id FROM tasks t2 WHERE t2.active_flag = 1 AND t2.status < 2
            AND NOT EXISTS (SELECT 1 FROM task_departments td2 WHERE td2.task_id = t2.id)
        ) combined
    """, nativeQuery = true)
    long countPending();

    @Query("select t from Task t where t.id in :ids and t.activeFlag = 1")
    List<Task> findByIds(@Param("ids") List<Integer> ids);
}
