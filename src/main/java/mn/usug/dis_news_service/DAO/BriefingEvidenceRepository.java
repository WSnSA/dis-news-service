package mn.usug.dis_news_service.DAO;

import mn.usug.dis_news_service.Entity.BriefingEvidence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BriefingEvidenceRepository extends JpaRepository<BriefingEvidence, Integer> {

    List<BriefingEvidence> findByFolderId(String folderId);

    List<BriefingEvidence> findByFolderIdIn(List<String> folderIds);
}
