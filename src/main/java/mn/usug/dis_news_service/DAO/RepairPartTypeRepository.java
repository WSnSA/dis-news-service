package mn.usug.dis_news_service.DAO;

import mn.usug.dis_news_service.Entity.RepairPartType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepairPartTypeRepository extends JpaRepository<RepairPartType, Long> {

    List<RepairPartType> findByActiveFlagOrderBySortOrderAscIdAsc(Integer activeFlag);
}
