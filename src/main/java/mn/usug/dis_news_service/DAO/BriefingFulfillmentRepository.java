package mn.usug.dis_news_service.DAO;

import mn.usug.dis_news_service.Entity.BriefingFulfillment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BriefingFulfillmentRepository extends JpaRepository<BriefingFulfillment, Integer> {

    List<BriefingFulfillment> findByCycleId(Integer cycleId);

    List<BriefingFulfillment> findByCycleIdIn(List<Integer> cycleIds);

    Optional<BriefingFulfillment> findByCycleIdAndDepartmentId(Integer cycleId, Integer departmentId);

    Optional<BriefingFulfillment> findByFolderId(String folderId);
}
