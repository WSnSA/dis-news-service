package mn.usug.dis_news_service.DAO;

// WorkNewsItemRepository.java
import mn.usug.dis_news_service.Entity.WorkNewsItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.*;
import java.util.*;

public interface WorkNewsItemRepository extends JpaRepository<WorkNewsItem, Long> {

    List<WorkNewsItem> findByDayIdOrderBySortOrderAsc(Long dayId);

    @Query("select i from WorkNewsItem i where i.createdAt >= :from and i.createdAt < :to order by i.sortOrder asc, i.id asc")
    List<WorkNewsItem> findByCreatedAtRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("""
    select i from WorkNewsItem i
    join i.day d
    where d.newsDate = :newsDate
    order by i.sortOrder asc
  """)
    List<WorkNewsItem> findByNewsDate(@Param("newsDate") LocalDate newsDate);

    @Query("""
    select i from WorkNewsItem i
    join i.day d
    where d.monthKey = :monthKey
    order by d.newsDate asc, i.sortOrder asc
  """)
    List<WorkNewsItem> findByMonthKey(@Param("monthKey") String monthKey);
}