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

    // Ээлжийн өдөр: D өдрийн 7:00 – D+1 өдрийн 6:00 (String param-аар timezone алдаа гарахгүй)
    /** Станцын хамгийн сүүлийн бүртгэл — өдрөөс үл хамааран */
    @Query(value = "SELECT * FROM hourly_ws_station WHERE menu_id = :menuId ORDER BY date DESC, id DESC LIMIT 1",
           nativeQuery = true)
    java.util.Optional<HourlyWsStation> findLatestByMenuId(@Param("menuId") Integer menuId);

    @Query(value = """
    SELECT *
    FROM hourly_ws_station
    WHERE menu_id = :menuId
      AND (
            (DATE(date) = :dateStr AND hour >= 7)
         OR (DATE(date) = :nextDateStr AND hour <= 6)
      )
    ORDER BY date ASC, hour ASC
    """, nativeQuery = true)
    List<HourlyWsStation> findByMenuIdAndShiftDay(
            @Param("menuId") Integer menuId,
            @Param("dateStr") String dateStr,
            @Param("nextDateStr") String nextDateStr);

    @Query("select a from HourlyWsStation a where a.menuId = ?1 and DATE(a.date) between ?2 and ?3")
    List<HourlyWsStation> findByMenuIdAndDateBetween(Integer menuId, Date start, Date end);

    // ── Batch query: бүх станцыг нэг дор татна (getDailySummary N+1 → 4 query болгох) ──

    @Query(value = """
    SELECT *
    FROM hourly_ws_station
    WHERE menu_id IN :menuIds
      AND (
            (DATE(date) = :dateStr     AND hour >= 7)
         OR (DATE(date) = :nextDateStr AND hour <= 6)
      )
    ORDER BY date ASC, hour ASC
    """, nativeQuery = true)
    List<HourlyWsStation> findAllByMenuIdsAndShiftDay(
            @Param("menuIds") List<Integer> menuIds,
            @Param("dateStr") String dateStr,
            @Param("nextDateStr") String nextDateStr);

    /** Станц тус бүрийн хамгийн сүүлийн бүртгэл (MAX(id) per menu_id) */
    @Query(value = """
    SELECT h.*
    FROM hourly_ws_station h
    INNER JOIN (
        SELECT menu_id, MAX(id) AS max_id
        FROM hourly_ws_station
        WHERE menu_id IN :menuIds
        GROUP BY menu_id
    ) latest ON h.id = latest.max_id
    """, nativeQuery = true)
    List<HourlyWsStation> findLatestByMenuIds(@Param("menuIds") List<Integer> menuIds);

    /** Өмнөх ээлжийн (D-1 өдрийн 07:00 – D өдрийн 06:00) сүүлийн бүртгэл станц бүрт — FM baseline */
    @Query(value = """
    SELECT h.*
    FROM hourly_ws_station h
    INNER JOIN (
        SELECT menu_id, MAX(id) AS max_id
        FROM hourly_ws_station
        WHERE menu_id IN :menuIds
          AND (
                (DATE(date) = :prevDateStr AND hour >= 7)
             OR (DATE(date) = :dateStr     AND hour <= 6)
          )
        GROUP BY menu_id
    ) latest ON h.id = latest.max_id
    """, nativeQuery = true)
    List<HourlyWsStation> findLastByMenuIdsAndPrevShift(
            @Param("menuIds") List<Integer> menuIds,
            @Param("prevDateStr") String prevDateStr,
            @Param("dateStr") String dateStr);

    /** Тухайн станцын өмнөх ээлжийн (D-1 өдрийн 07:00 – D өдрийн 06:00) хамгийн сүүлийн бүртгэл */
    @Query(value = """
    SELECT * FROM hourly_ws_station
    WHERE menu_id = :menuId
      AND (
            (DATE(date) = :prevDateStr AND hour >= 7)
         OR (DATE(date) = :dateStr     AND hour <= 6)
      )
    ORDER BY date DESC, id DESC LIMIT 1
    """, nativeQuery = true)
    java.util.Optional<HourlyWsStation> findLastByMenuIdAndPrevShift(
            @Param("menuId") Integer menuId,
            @Param("prevDateStr") String prevDateStr,
            @Param("dateStr") String dateStr);

    @Query(value = "SELECT hour, " +
        "SUM(COALESCE(pipe_fm_1,0)) as fm1, " +
        "SUM(COALESCE(pipe_fm_7,0)) as fm7, " +
        "SUM(COALESCE(pipe_fm_8,0)) as fm8, " +
        "SUM(COALESCE(pipe_fm_1,0)) + SUM(COALESCE(pipe_fm_7,0)) + SUM(COALESCE(pipe_fm_8,0)) as total " +
        "FROM hourly_ws_station WHERE DATE(date) = :date GROUP BY hour ORDER BY hour", nativeQuery = true)
    List<Object[]> getDailyFmByHour(@Param("date") String date);

    // Ээлжийн өдрийн FM: D өдрийн 7:00 – D+1 өдрийн 6:00
    @Query(value = "SELECT hour, " +
        "SUM(COALESCE(pipe_fm_1,0)) as fm1, " +
        "SUM(COALESCE(pipe_fm_7,0)) as fm7, " +
        "SUM(COALESCE(pipe_fm_8,0)) as fm8, " +
        "SUM(COALESCE(pipe_fm_1,0)) + SUM(COALESCE(pipe_fm_7,0)) + SUM(COALESCE(pipe_fm_8,0)) as total " +
        "FROM hourly_ws_station " +
        "WHERE (DATE(date) = :date AND hour >= 7) OR (DATE(date) = :nextDate AND hour <= 6) " +
        "GROUP BY hour ORDER BY hour", nativeQuery = true)
    List<Object[]> getDailyFmByHourShift(@Param("date") String date, @Param("nextDate") String nextDate);
}
