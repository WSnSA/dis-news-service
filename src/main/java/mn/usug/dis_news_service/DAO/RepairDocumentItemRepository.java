package mn.usug.dis_news_service.DAO;

import mn.usug.dis_news_service.Entity.RepairDocumentItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface RepairDocumentItemRepository extends JpaRepository<RepairDocumentItem, Long> {

    List<RepairDocumentItem> findByRepairDocumentIdAndActiveFlagOrderBySortOrderAscIdAsc(
            Long repairDocumentId, Integer activeFlag);

    /** Олон баримтын мөрийг нэг дуудлагаар — жагсаалт, тайланд */
    List<RepairDocumentItem> findByRepairDocumentIdInAndActiveFlag(
            Collection<Long> repairDocumentIds, Integer activeFlag);

    void deleteByRepairDocumentId(Long repairDocumentId);
}
