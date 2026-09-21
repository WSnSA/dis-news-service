package mn.usug.dis_news_service.DAO;

import mn.usug.dis_news_service.Entity.RepairAttendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface RepairAttendanceRepository extends JpaRepository<RepairAttendance, Long> {

    List<RepairAttendance> findByWorkDateAndActiveFlagOrderByIdAsc(LocalDate workDate, Integer activeFlag);

    List<RepairAttendance> findByWorkDateBetweenAndActiveFlag(LocalDate from, LocalDate to, Integer activeFlag);

    java.util.Optional<RepairAttendance> findByWorkDateAndRepairWorkerId(LocalDate workDate, Long repairWorkerId);
}
