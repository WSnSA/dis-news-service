package mn.usug.dis_news_service.DAO;

import mn.usug.dis_news_service.Entity.RepairDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface RepairDocumentRepository extends JpaRepository<RepairDocument, Long> {

    List<RepairDocument> findByActiveFlagOrderByDocDateDescIdDesc(Integer activeFlag);

    List<RepairDocument> findByVehicleRepairIdAndActiveFlagOrderByDocDateDescIdDesc(
            Long vehicleRepairId, Integer activeFlag);

    /** Тайлан, машины түүхэд — олон засварын баримтыг нэг дуудлагаар */
    List<RepairDocument> findByVehicleRepairIdInAndActiveFlag(
            Collection<Long> vehicleRepairIds, Integer activeFlag);

    /** Нэг машины бүх баримт — зай, том жижиг үсэг үл харгалзан */
    @Query("""
        SELECT d FROM RepairDocument d
        WHERE d.activeFlag = 1
          AND UPPER(REPLACE(d.plateNumber, ' ', '')) = UPPER(REPLACE(:plate, ' ', ''))
        ORDER BY d.docDate DESC, d.id DESC
    """)
    List<RepairDocument> findByPlate(@Param("plate") String plate);
}
