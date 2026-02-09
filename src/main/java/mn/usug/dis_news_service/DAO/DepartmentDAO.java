package mn.usug.dis_news_service.DAO;

import mn.usug.dis_news_service.Entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DepartmentDAO extends JpaRepository<Department,Integer> {

    @Query("select a from Department a")
    List<Department> findAll();

    Department findByDepId(Integer id);

    List<Department> findDepartmentsByDepNameContaining(String name);


//    Integer findMaxId();
}
