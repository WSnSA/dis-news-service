package mn.usug.dis_news_service.DAO;

import mn.usug.dis_news_service.Entity.BriefingNewsEvidence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BriefingNewsEvidenceRepository extends JpaRepository<BriefingNewsEvidence, Integer> {

    List<BriefingNewsEvidence> findByFolderId(String folderId);

    List<BriefingNewsEvidence> findByFolderIdIn(List<String> folderIds);
}
