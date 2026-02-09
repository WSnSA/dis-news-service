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
}
