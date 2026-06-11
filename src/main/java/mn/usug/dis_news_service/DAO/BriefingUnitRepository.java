package mn.usug.dis_news_service.DAO;

import mn.usug.dis_news_service.Entity.BriefingUnit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BriefingUnitRepository extends JpaRepository<BriefingUnit, Integer> {

    List<BriefingUnit> findByActiveFlagOrderBySortOrderAsc(Integer activeFlag);
}
