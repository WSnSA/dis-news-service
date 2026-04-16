package mn.usug.dis_news_service.DAO;

import mn.usug.dis_news_service.Entity.VehicleDriver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleDriverRepository extends JpaRepository<VehicleDriver, Long> {

    List<VehicleDriver> findByVehicleId(Long vehicleId);

    void deleteByVehicleId(Long vehicleId);
}
