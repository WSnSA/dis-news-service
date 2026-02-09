package mn.usug.dis_news_service.DAO;


import mn.usug.dis_news_service.Entity.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VehicleTypeRepository
        extends JpaRepository<VehicleType, Integer> {
}
