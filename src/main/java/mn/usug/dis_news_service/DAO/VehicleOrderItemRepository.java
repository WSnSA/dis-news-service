package mn.usug.dis_news_service.DAO;

import mn.usug.dis_news_service.Entity.VehicleOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleOrderItemRepository
        extends JpaRepository<VehicleOrderItem, Long> {

    List<VehicleOrderItem> findByVehicleOrderId(Long vehicleOrderId);
}

