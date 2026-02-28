package mn.usug.dis_news_service.DAO;
// WorkNewsDayRepository.java
import mn.usug.dis_news_service.Entity.WorkNewsDay;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.*;
import java.util.*;

public interface WorkNewsDayRepository extends JpaRepository<WorkNewsDay, Long> {
    Optional<WorkNewsDay> findByNewsDate(LocalDate newsDate);
    List<WorkNewsDay> findByMonthKeyOrderByNewsDateAsc(String monthKey);
    List<WorkNewsDay> findByMonthKeyOrderByNewsDateDesc(String monthKey);
}