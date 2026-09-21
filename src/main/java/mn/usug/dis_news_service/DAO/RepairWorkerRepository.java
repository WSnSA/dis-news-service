package mn.usug.dis_news_service.DAO;

import mn.usug.dis_news_service.Entity.RepairWorker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepairWorkerRepository extends JpaRepository<RepairWorker, Long> {

    List<RepairWorker> findByActiveFlagOrderByNameAsc(Integer activeFlag);
}
