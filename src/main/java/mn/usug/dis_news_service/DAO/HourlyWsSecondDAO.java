package mn.usug.dis_news_service.DAO;


import mn.usug.dis_news_service.Entity.HourlyWsSecond;
import mn.usug.dis_news_service.Entity.HourlyWsStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface HourlyWsSecondDAO extends JpaRepository<HourlyWsSecond,Integer> {
    @Query("select a from HourlyWsSecond a where a.menuId = ?1 and DATE(a.date) = ?2 and a.hour = ?3")
    List<HourlyWsSecond> findAllByMenuIdAndDateAndHour(Integer menuId, Date date, Integer hour);

    @Query("select a from HourlyWsSecond a where a.menuId = ?1 and DATE(a.date) = ?2")
    List<HourlyWsSecond> findAllByMenuIdAndDate(Integer menuId, Date date);

    @Query("select a from HourlyWsSecond a where a.menuId = ?1 and DATE(a.date) between ?2 and ?3")
    List<HourlyWsSecond> findByMenuIdAndDateBetween(Integer menuId, Date start, Date end);

}
