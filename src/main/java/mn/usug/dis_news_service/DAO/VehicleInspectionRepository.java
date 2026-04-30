package mn.usug.dis_news_service.DAO;

import mn.usug.dis_news_service.Entity.VehicleInspection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VehicleInspectionRepository extends JpaRepository<VehicleInspection, Long> {
    Optional<VehicleInspection> findByPlateNumber(String plateNumber);
}
