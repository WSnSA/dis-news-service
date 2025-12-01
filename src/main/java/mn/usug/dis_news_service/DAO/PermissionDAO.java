package mn.usug.dis_news_service.DAO;

import mn.usug.dis_news_service.Entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PermissionDAO extends JpaRepository<Permission,Integer> {

    @Query("select a from Permission a")
    List<Permission> findAll();

    List<Permission> findByUserId(Integer id);

    Permission findByUserIdAndMenuId(Integer userId, Integer menuId);
}
