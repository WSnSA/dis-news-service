package mn.usug.dis_news_service.DAO;


import mn.usug.dis_news_service.Entity.HourlyWsSecond;
import mn.usug.dis_news_service.Entity.HourlyWsStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface HourlyWsSecondDAO extends JpaRepository<HourlyWsSecond,Integer> {
    @Query("select a from HourlyWsSecond a where a.menuId = ?1 and DATE(a.date) = ?2 and a.hour = ?3")
    List<HourlyWsSecond> findAllByMenuIdAndDateAndHour(Integer menuId, Date date, Integer hour);

    @Query("select a from HourlyWsSecond a where a.menuId = ?1 and DATE(a.date) = ?2")
    List<HourlyWsSecond> findAllByMenuIdAndDate(Integer menuId, Date date);

    // Ээлжийн өдөр: D өдрийн 8:00 – D+1 өдрийн 7:00 (String param-аар timezone алдаа гарахгүй)
    @Query(value = """
    SELECT *
    FROM hourly_ws_second
    WHERE menu_id = :menuId
      AND (
            (DATE(date) = :dateStr AND hour >= 8)
         OR (DATE(date) = :nextDateStr AND hour <= 7)
      )
    ORDER BY date ASC, hour ASC, id ASC
    """, nativeQuery = true)
    List<HourlyWsSecond> findAllByMenuIdAndShiftDay(
            @Param("menuId") Integer menuId,
            @Param("dateStr") String dateStr,
            @Param("nextDateStr") String nextDateStr);

    @Query("select a from HourlyWsSecond a where a.menuId = ?1 and DATE(a.date) between ?2 and ?3")
    List<HourlyWsSecond> findByMenuIdAndDateBetween(Integer menuId, Date start, Date end);

    // ── Batch query ──
    @Query(value = """
    SELECT *
    FROM hourly_ws_second
    WHERE menu_id IN :menuIds
      AND (
            (DATE(date) = :dateStr     AND hour >= 8)
         OR (DATE(date) = :nextDateStr AND hour <= 7)
      )
    ORDER BY date ASC, hour ASC, id ASC
    """, nativeQuery = true)
    List<HourlyWsSecond> findAllByMenuIdsAndShiftDay(
            @Param("menuIds") List<Integer> menuIds,
            @Param("dateStr") String dateStr,
            @Param("nextDateStr") String nextDateStr);

    @Modifying
    @Transactional
    @Query("delete from HourlyWsSecond a where a.menuId = ?1 and DATE(a.date) = ?2 and a.hour = ?3")
    void deleteByMenuIdAndDateAndHour(Integer menuId, Date date, Integer hour);

}
