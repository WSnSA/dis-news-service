package mn.usug.dis_news_service.DAO;

import mn.usug.dis_news_service.Entity.VehicleRepair;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VehicleRepairRepository extends JpaRepository<VehicleRepair, Long> {

    List<VehicleRepair> findByActiveFlagOrderByStartDateDescIdDesc(Integer activeFlag);

    List<VehicleRepair> findByActiveFlagAndStatusOrderByStartDateDescIdDesc(Integer activeFlag, Integer status);

    /** Нэг машины засварын түүх — улсын дугаарыг зай/том жижиг үсэг үл харгалзан харьцуулна */
    @Query("""
        SELECT r FROM VehicleRepair r
        WHERE r.activeFlag = 1
          AND UPPER(REPLACE(r.plateNumber, ' ', '')) = UPPER(REPLACE(:plate, ' ', ''))
        ORDER BY r.startDate DESC, r.id DESC
    """)
    List<VehicleRepair> findByPlate(@Param("plate") String plate);

    /**
     * Өгөгдсөн хугацаанд засвартай байх машинууд.
     * Давхцал: repair.start <= :to AND (repair.end IS NULL OR repair.end >= :from)
     */
    @Query("""
        SELECT r FROM VehicleRepair r
        WHERE r.activeFlag = 1
          AND r.status = 0
          AND r.startDate <= :to
          AND (r.endDate IS NULL OR r.endDate >= :from)
    """)
    List<VehicleRepair> findOverlapping(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
