package mn.usug.dis_news_service.DAO;


import mn.usug.dis_news_service.Entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StationDAO extends JpaRepository<Station,Integer> {
    List<Station> findAll();

    @Query("select a from Station a where a.departmentId = ?1")
    List<Station> findByDepId();
}
