package mn.usug.dis_news_service.DAO;

import mn.usug.dis_news_service.Entity.VehiclesToOutCancel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface VehiclesToOutCancelRepository extends JpaRepository<VehiclesToOutCancel, Integer> {

    /** Тухайн өдөр цуцлагдсан бүх хуваарилалт — жагсаалт буцаахад */
    List<VehiclesToOutCancel> findByCancelDate(LocalDate cancelDate);

    Optional<VehiclesToOutCancel> findByVehiclesToOutIdAndCancelDate(Integer vehiclesToOutId, LocalDate cancelDate);

    /** Хуваарийн жагсаалтад цуцлагдсан өдрүүдийг нэг дуудлагаар нөхөхөд */
    List<VehiclesToOutCancel> findByVehiclesToOutIdIn(Collection<Integer> vehiclesToOutIds);

    /** Хуваарилалт устгагдахад түүний бүх цуцлалтыг цэвэрлэнэ */
    void deleteByVehiclesToOutId(Integer vehiclesToOutId);

    void deleteByVehiclesToOutIdIn(Collection<Integer> vehiclesToOutIds);
}
