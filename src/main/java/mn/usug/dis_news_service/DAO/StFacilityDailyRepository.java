package mn.usug.dis_news_service.DAO;

import mn.usug.dis_news_service.Entity.StFacilityDaily;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StFacilityDailyRepository extends JpaRepository<StFacilityDaily, Integer> {

    Optional<StFacilityDaily> findFirstByStationIdAndRecordDateAndActiveFlag(
            Integer stationId, LocalDate recordDate, Integer activeFlag);

    List<StFacilityDaily> findByStationIdAndRecordDateBetweenAndActiveFlagOrderByRecordDate(
            Integer stationId, LocalDate from, LocalDate to, Integer activeFlag);
}
