package mn.usug.dis_news_service.DAO;

import mn.usug.dis_news_service.Entity.BriefingAuditLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BriefingAuditLogRepository extends JpaRepository<BriefingAuditLog, Long> {

    List<BriefingAuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<BriefingAuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, Integer entityId);
}
