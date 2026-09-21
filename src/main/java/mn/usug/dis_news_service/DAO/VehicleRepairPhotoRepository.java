package mn.usug.dis_news_service.DAO;

import mn.usug.dis_news_service.Entity.VehicleRepairPhoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface VehicleRepairPhotoRepository extends JpaRepository<VehicleRepairPhoto, Long> {

    List<VehicleRepairPhoto> findByVehicleRepairIdAndActiveFlagOrderByIdAsc(Long vehicleRepairId, Integer activeFlag);
}
