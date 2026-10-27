package mn.usug.dis_news_service.DAO;

import mn.usug.dis_news_service.Entity.VehicleRecurringOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VehicleRecurringOrderItemRepository extends JpaRepository<VehicleRecurringOrderItem, Long> {

    List<VehicleRecurringOrderItem> findByRecurringId(Long recurringId);

    void deleteByRecurringId(Long recurringId);
}
