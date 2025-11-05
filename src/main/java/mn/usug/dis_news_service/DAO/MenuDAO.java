package mn.usug.dis_news_service.DAO;

import mn.usug.dis_news_service.Entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MenuDAO extends JpaRepository<Menu,Integer> {

    @Query("select a from Menu a where a.activeFlag = 1")
    List<Menu> findAll();

}
