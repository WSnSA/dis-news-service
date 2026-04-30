package mn.usug.dis_news_service.DAO;

import mn.usug.dis_news_service.Entity.VehicleInsurance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VehicleInsuranceRepository extends JpaRepository<VehicleInsurance, Long> {
    Optional<VehicleInsurance> findByPlateNumber(String plateNumber);
}
