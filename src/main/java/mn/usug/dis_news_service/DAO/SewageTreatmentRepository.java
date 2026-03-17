package mn.usug.dis_news_service.DAO;

import mn.usug.dis_news_service.Entity.SewageTreatment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface SewageTreatmentRepository extends JpaRepository<SewageTreatment, Integer> {

    @Query(value = """
        SELECT
          m.id   AS stationId,
          p.name AS groupName,
          m.name AS stationName,

          COALESCE(st.working_count, '0')   AS workingCount,
          COALESCE(st.pending_count, '0')   AS pendingCount,
          COALESCE(st.repairing_count, '0') AS repairingCount,

          COALESCE(st.received_waste, 0)    AS receivedWaste,
          COALESCE(st.received_wool, 0)     AS receivedWool,
          COALESCE(st.received_water, 0)    AS receivedWater,
          COALESCE(st.substance_spent, 0)   AS substanceSpent,
          COALESCE(st.treated_water, 0)     AS treatedWater
        FROM dis_news.sewage_treatment st
        JOIN dis_news.menu m ON m.id = st.station_id
        LEFT JOIN dis_news.menu p ON p.id = m.parent_id
        WHERE DATE(st.created_date) = :d
          AND HOUR(st.created_date) = :h
          AND st.active_flag = 1
        ORDER BY p.id, m.id
        """, nativeQuery = true)
    List<Map<String, Object>> findSummaryRaw(@Param("d") LocalDate date, @Param("h") int hour);

    /** Тухайн станц, огноо, цагт бүртгэгдсэн хамгийн сүүлийн бичлэгийг буцаана */
    @Query(value = """
        SELECT * FROM sewage_treatment
        WHERE station_id = :stationId
          AND DATE(created_date) = :date
          AND HOUR(created_date) = :hour
          AND active_flag = 1
        ORDER BY id DESC LIMIT 1
        """, nativeQuery = true)
    Optional<SewageTreatment> findLatestByStationAndHour(
            @Param("stationId") int stationId,
            @Param("date") java.time.LocalDate date,
            @Param("hour") int hour);

    /** Тухайн станцын өдрийн бүх цагийн бүртгэл (хяналтын grid-д) */
    @Query(value = """
        SELECT
          HOUR(created_date)  AS hour,
          working_count       AS workingCount,
          pending_count       AS pendingCount,
          repairing_count     AS repairingCount,
          received_waste      AS receivedWaste,
          received_wool       AS receivedWool,
          received_water      AS receivedWater,
          substance_spent     AS substanceSpent,
          treated_water       AS treatedWater
        FROM sewage_treatment
        WHERE station_id = :stationId
          AND DATE(created_date) = :date
          AND active_flag = 1
        ORDER BY HOUR(created_date)
        """, nativeQuery = true)
    List<Map<String, Object>> findDailyHistory(
            @Param("stationId") int stationId,
            @Param("date") java.time.LocalDate date);
}
