package mn.usug.dis_news_service.DAO;

import mn.usug.dis_news_service.Entity.OilChangeLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OilChangeLogRepository extends JpaRepository<OilChangeLog, Long> {

    List<OilChangeLog> findByActiveFlagOrderByChangeDateDescIdDesc(Integer activeFlag);
}
