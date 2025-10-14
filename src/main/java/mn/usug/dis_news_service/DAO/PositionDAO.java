package mn.usug.dis_news_service.DAO;

import mn.usug.dis_news_service.Entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PositionDAO extends JpaRepository<Position,Integer> {

    @Query("select a from Position a")
    List<Position> findAll();

    @Query("select a from Position a where a.depId = ?1")
    List<Position> findByDepId(Integer id);

    @Query("select a from Position a where a.depId = ?1 and lower(a.name) like lower(concat('%', ?2, '%'))")
    List<Position> findByNameContaining(Integer depId, String name);

}
