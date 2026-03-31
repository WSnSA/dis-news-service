package mn.usug.dis_news_service.DAO;

import mn.usug.dis_news_service.Entity.VehiclesToOut;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface VehiclesToOutRepository extends JpaRepository<VehiclesToOut, Integer> {
    List<VehiclesToOut> findAllByCreatedDateBetweenOrderByIdAsc(LocalDateTime from, LocalDateTime to);

    java.util.Optional<VehiclesToOut> findFirstByVehicleOrderIdOrderByCreatedDateDesc(Integer vehicleOrderId);

    List<VehiclesToOut> findAllByVehicleOrderIdOrderByIdAsc(Integer vehicleOrderId);
}
