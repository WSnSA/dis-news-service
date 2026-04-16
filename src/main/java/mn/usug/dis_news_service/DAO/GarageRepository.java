package mn.usug.dis_news_service.DAO;

import mn.usug.dis_news_service.Entity.Garage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GarageRepository extends JpaRepository<Garage, Long> {
}
