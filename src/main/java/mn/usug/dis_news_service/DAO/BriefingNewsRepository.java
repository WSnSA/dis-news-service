package mn.usug.dis_news_service.DAO;

import mn.usug.dis_news_service.Entity.BriefingNews;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BriefingNewsRepository extends JpaRepository<BriefingNews, Integer> {

    List<BriefingNews> findByMeetingId(Integer meetingId);

    Optional<BriefingNews> findByMeetingIdAndUnitId(Integer meetingId, Integer unitId);

    Optional<BriefingNews> findByFolderId(String folderId);
}
