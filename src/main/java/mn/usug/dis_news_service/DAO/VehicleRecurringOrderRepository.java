package mn.usug.dis_news_service.DAO;

import mn.usug.dis_news_service.Entity.VehicleRecurringOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VehicleRecurringOrderRepository extends JpaRepository<VehicleRecurringOrder, Long> {

    List<VehicleRecurringOrder> findByActiveFlagOrderByIdDesc(Integer activeFlag);
}
