package mn.usug.dis_news_service.DAO;


import mn.usug.dis_news_service.Entity.HourlyWsStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface HourlyWsStationDAO extends JpaRepository<HourlyWsStation,Integer> {
    @Query("select a from HourlyWsStation a where a.menuId = ?1 and DATE(a.date) = ?2 and a.hour = ?3")
    HourlyWsStation findByMenuIdAndDateAndHour(Integer menuId, Date date, Integer hour);

    @Query("select a from HourlyWsStation a where a.menuId = ?1 and DATE(a.date) = ?2")
    List<HourlyWsStation> findByMenuIdAndDate(Integer menuId, Date date);

    // Ээлжийн өдөр: D өдрийн 8:00 – D+1 өдрийн 7:00 (String param-аар timezone алдаа гарахгүй)
    @Query(value = """
    SELECT *
    FROM hourly_ws_station
    WHERE menu_id = :menuId
      AND (
            (DATE(date) = :dateStr AND hour >= 8)
         OR (DATE(date) = :nextDateStr AND hour <= 7)
      )
    ORDER BY date ASC, hour ASC
    """, nativeQuery = true)
    List<HourlyWsStation> findByMenuIdAndShiftDay(
            @Param("menuId") Integer menuId,
            @Param("dateStr") String dateStr,
            @Param("nextDateStr") String nextDateStr);

    @Query("select a from HourlyWsStation a where a.menuId = ?1 and DATE(a.date) between ?2 and ?3")
    List<HourlyWsStation> findByMenuIdAndDateBetween(Integer menuId, Date start, Date end);

    @Query(value = "SELECT hour, " +
        "SUM(COALESCE(pipe_fm_1,0)) as fm1, " +
        "SUM(COALESCE(pipe_fm_7,0)) as fm7, " +
        "SUM(COALESCE(pipe_fm_8,0)) as fm8, " +
        "SUM(COALESCE(pipe_fm_1,0)) + SUM(COALESCE(pipe_fm_7,0)) + SUM(COALESCE(pipe_fm_8,0)) as total " +
        "FROM hourly_ws_station WHERE DATE(date) = :date GROUP BY hour ORDER BY hour", nativeQuery = true)
    List<Object[]> getDailyFmByHour(@Param("date") String date);

    // Ээлжийн өдрийн FM: D өдрийн 8:00 – D+1 өдрийн 7:00
    @Query(value = "SELECT hour, " +
        "SUM(COALESCE(pipe_fm_1,0)) as fm1, " +
        "SUM(COALESCE(pipe_fm_7,0)) as fm7, " +
        "SUM(COALESCE(pipe_fm_8,0)) as fm8, " +
        "SUM(COALESCE(pipe_fm_1,0)) + SUM(COALESCE(pipe_fm_7,0)) + SUM(COALESCE(pipe_fm_8,0)) as total " +
        "FROM hourly_ws_station " +
        "WHERE (DATE(date) = :date AND hour >= 8) OR (DATE(date) = :nextDate AND hour <= 7) " +
        "GROUP BY hour ORDER BY hour", nativeQuery = true)
    List<Object[]> getDailyFmByHourShift(@Param("date") String date, @Param("nextDate") String nextDate);
}
