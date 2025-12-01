package mn.usug.dis_news_service.DAO;


import mn.usug.dis_news_service.Entity.HourlyWsStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface HourlyWsStationDAO extends JpaRepository<HourlyWsStation,Integer> {
    @Query("select a from HourlyWsStation a where a.menuId = ?1 and DATE(a.date) = ?2 and a.hour = ?3")
    HourlyWsStation findByMenuIdAndDateAndHour(Integer menuId, Date date, Integer hour);

    @Query("select a from HourlyWsStation a where a.menuId = ?1 and DATE(a.date) = ?2")
    List<HourlyWsStation> findByMenuIdAndDate(Integer menuId, Date date);

    @Query("select a from HourlyWsStation a where a.menuId = ?1 and DATE(a.date) between ?2 and ?3")
    List<HourlyWsStation> findByMenuIdAndDateBetween(Integer menuId, Date start, Date end);
}
