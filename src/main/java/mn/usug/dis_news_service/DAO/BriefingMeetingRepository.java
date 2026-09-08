package mn.usug.dis_news_service.DAO;

import mn.usug.dis_news_service.Entity.BriefingMeeting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BriefingMeetingRepository extends JpaRepository<BriefingMeeting, Integer> {

    Optional<BriefingMeeting> findByMeetingDate(LocalDate meetingDate);

    /**
     * Нэг өдөрт санамсаргүй 2 мөр үүссэн тохиолдолд findByMeetingDate нь
     * IncorrectResultSizeDataAccessException (500) шидэж, UI-д "тодорхойгүй алдаа"
     * харагддаг байсан. Жагсаалтаар авч хамгийн эртнийг нь сонгож найдвартай болгов.
     */
    List<BriefingMeeting> findAllByMeetingDateOrderByIdAsc(LocalDate meetingDate);

    List<BriefingMeeting> findByActiveFlagOrderByMeetingDateDesc(Integer activeFlag);

    BriefingMeeting findTopByOrderByMeetingDateDesc();
}
