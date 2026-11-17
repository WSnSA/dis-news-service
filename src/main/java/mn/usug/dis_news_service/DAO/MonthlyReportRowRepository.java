package mn.usug.dis_news_service.DAO;

import mn.usug.dis_news_service.Entity.MonthlyReportRow;
import mn.usug.dis_news_service.Entity.StFacilityDaily;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

/**
 * "Сарын мэдээ"-ний мөрүүд + тайланд хэрэгтэй түүхий өгөгдлийн query-үүд.
 * (Бусад DAO-г өөрчлөхгүйн тулд энд төвлөрүүлэв.)
 */
public interface MonthlyReportRowRepository extends JpaRepository<MonthlyReportRow, Integer> {

    List<MonthlyReportRow> findByActiveFlagOrderBySortOrderAscIdAsc(Integer activeFlag);

    /**
     * FM тоолуурын заалтууд (хуанлийн огноо + цаг), станц → цаг хугацааны дарааллаар.
     * [0]=menu_id, [1]=yyyy-MM-dd, [2]=hour, [3]=pipe_fm_1, [4]=pipe_fm_7, [5]=pipe_fm_8
     */
    @Query(value = """
        SELECT menu_id, DATE_FORMAT(date, '%Y-%m-%d') AS d, hour, pipe_fm_1, pipe_fm_7, pipe_fm_8
        FROM hourly_ws_station
        WHERE menu_id IN (:menuIds)
          AND DATE(date) BETWEEN :fromStr AND :toStr
          AND (pipe_fm_1 IS NOT NULL OR pipe_fm_7 IS NOT NULL OR pipe_fm_8 IS NOT NULL)
        ORDER BY menu_id, DATE(date), hour, id
        """, nativeQuery = true)
    List<Object[]> findFmReadings(@Param("menuIds") Collection<Integer> menuIds,
                                  @Param("fromStr") String fromStr,
                                  @Param("toStr") String toStr);

    /**
     * sewage_treatment-ийн ээлжийн сар бүрийн нийлбэр (07:00 ээлж → created_date - 7 цаг).
     * [0]=station_id, [1]=сар (1..12), [2]=нийлбэр
     * fromTs / toTs: 'yyyy-MM-dd 07:00:00' (toTs орохгүй)
     */
    @Query(value = """
        SELECT station_id,
               MONTH(DATE_SUB(created_date, INTERVAL 7 HOUR)) AS mon,
               SUM(CASE :metric
                     WHEN 'ST_RECEIVED_WATER' THEN COALESCE(received_water, 0)
                     WHEN 'ST_TREATED_WATER'  THEN COALESCE(treated_water, 0)
                     ELSE COALESCE(received_waste, 0)
                   END) AS total
        FROM sewage_treatment
        WHERE active_flag = 1
          AND station_id IN (:stationIds)
          AND created_date >= :fromTs
          AND created_date <  :toTs
        GROUP BY station_id, mon
        """, nativeQuery = true)
    List<Object[]> sumSewageByMonth(@Param("stationIds") Collection<Integer> stationIds,
                                    @Param("metric") String metric,
                                    @Param("fromTs") String fromTs,
                                    @Param("toTs") String toTs);

    /** Шинэ (олон хэсэгтэй) цэвэрлэх байгууламжийн бүртгэлүүд — огнооны хүрээнд */
    @Query("SELECT s FROM StFacilityDaily s WHERE s.activeFlag = 1 AND s.stationId IN :stationIds "
         + "AND s.recordDate BETWEEN :from AND :to")
    List<StFacilityDaily> findFacilityRange(@Param("stationIds") Collection<Integer> stationIds,
                                            @Param("from") LocalDate from,
                                            @Param("to") LocalDate to);
}
