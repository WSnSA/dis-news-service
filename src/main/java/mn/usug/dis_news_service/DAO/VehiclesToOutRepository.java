package mn.usug.dis_news_service.DAO;

import mn.usug.dis_news_service.Entity.VehiclesToOut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VehiclesToOutRepository extends JpaRepository<VehiclesToOut, Integer> {
    List<VehiclesToOut> findByCreatedDate(LocalDate createdDate);
}
