package mn.usug.dis_news_service.DAO;

import mn.usug.dis_news_service.Entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MenuDAO extends JpaRepository<Menu,Integer> {

    @Query("select a from Menu a where a.activeFlag = 1")
    List<Menu> findAll();

    @Query("select a from Menu a where a.activeFlag = 1 and a.path is not null and a.component is not null and a.path like '%ws%'")
    List<Integer> findByWS();

    @Query("select a from Menu a where a.activeFlag = 1 and a.latitude is not null and a.longitude is not null")
    List<Menu> getMarkers();

    @Query("""
    select a from Menu a
    where a.activeFlag = 1
      and a.path is not null
      and a.component is not null
      and a.path like concat('%ws/', :type, '%')
""")
    List<Menu> findByType(@Param("type") String type);

}
