package mn.usug.dis_news_service.DAO;

import mn.usug.dis_news_service.Entity.VehicleRepairPart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VehicleRepairPartRepository extends JpaRepository<VehicleRepairPart, Long> {

    List<VehicleRepairPart> findByVehicleRepairIdAndActiveFlagOrderByIdAsc(Long vehicleRepairId, Integer activeFlag);
}
