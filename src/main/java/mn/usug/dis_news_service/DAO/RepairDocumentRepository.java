package mn.usug.dis_news_service.DAO;

import mn.usug.dis_news_service.Entity.RepairDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepairDocumentRepository extends JpaRepository<RepairDocument, Long> {

    List<RepairDocument> findByActiveFlagOrderByDocDateDescIdDesc(Integer activeFlag);
}
