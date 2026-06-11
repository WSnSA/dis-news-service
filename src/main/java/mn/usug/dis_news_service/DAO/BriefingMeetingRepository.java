package mn.usug.dis_news_service.DAO;

import mn.usug.dis_news_service.Entity.BriefingMeeting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BriefingMeetingRepository extends JpaRepository<BriefingMeeting, Integer> {

    Optional<BriefingMeeting> findByMeetingDate(LocalDate meetingDate);

    List<BriefingMeeting> findByActiveFlagOrderByMeetingDateDesc(Integer activeFlag);

    BriefingMeeting findTopByOrderByMeetingDateDesc();
}
