package mn.usug.dis_news_service.DAO;

import mn.usug.dis_news_service.Entity.StFacilityDaily;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StFacilityDailyRepository extends JpaRepository<StFacilityDaily, Integer> {

    Optional<StFacilityDaily> findFirstByStationIdAndRecordDateAndActiveFlag(
            Integer stationId, LocalDate recordDate, Integer activeFlag);

    /** Цаг тус бүрийн бүртгэл (station + өдөр + цаг) */
    Optional<StFacilityDaily> findFirstByStationIdAndRecordDateAndRecordHourAndActiveFlag(
            Integer stationId, LocalDate recordDate, Integer recordHour, Integer activeFlag);

    List<StFacilityDaily> findByStationIdAndRecordDateBetweenAndActiveFlagOrderByRecordDate(
            Integer stationId, LocalDate from, LocalDate to, Integer activeFlag);

    /** Тухайн өдрийн бүх байгууламжийн бүртгэл — нэгтгэлийн дэлгэцүүдэд оруулахад */
    List<StFacilityDaily> findByRecordDateAndActiveFlag(LocalDate recordDate, Integer activeFlag);

    /**
     * 07:00 ээлжийн цонх (бусад ST станцтай ижил): тухайн өдрийн 07:00 → маргаашийн 06:59.
     * Хуучин цаггүй (record_hour IS NULL) бүртгэл тухайн өдрийнхөө ээлжид үлдэнэ.
     */
    @Query("SELECT s FROM StFacilityDaily s WHERE s.activeFlag = 1 AND ("
         + "(s.recordDate = :d AND (s.recordHour IS NULL OR s.recordHour >= 7)) "
         + "OR (s.recordDate = :nextD AND s.recordHour <= 6))")
    List<StFacilityDaily> findFacilityShiftWindow(@Param("d") LocalDate d, @Param("nextD") LocalDate nextD);
}
