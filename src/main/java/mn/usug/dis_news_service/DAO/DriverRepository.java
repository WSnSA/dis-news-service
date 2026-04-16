package mn.usug.dis_news_service.DAO;

import mn.usug.dis_news_service.Entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {

    List<Driver> findByNameContainingIgnoreCase(String name);
}
