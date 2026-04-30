package mn.usug.dis_news_service.DAO;

import mn.usug.dis_news_service.Entity.VehiclePenalty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VehiclePenaltyRepository extends JpaRepository<VehiclePenalty, Long> {
    Optional<VehiclePenalty> findByBarCode(String barCode);
    List<VehiclePenalty> findByIsPaidFalse();
    List<VehiclePenalty> findByPlateNumber(String plateNumber);
}
