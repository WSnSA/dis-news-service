package mn.usug.dis_news_service.DAO;

import mn.usug.dis_news_service.Entity.VehicleRepairWorker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VehicleRepairWorkerRepository extends JpaRepository<VehicleRepairWorker, Long> {

    List<VehicleRepairWorker> findByVehicleRepairIdAndActiveFlagOrderByIdAsc(Long vehicleRepairId, Integer activeFlag);
}
