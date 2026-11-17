package mn.usug.dis_news_service.DAO;

import mn.usug.dis_news_service.Entity.MonthlyReportPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MonthlyReportPlanRepository extends JpaRepository<MonthlyReportPlan, Integer> {
    List<MonthlyReportPlan> findByPlanYear(Integer planYear);

    Optional<MonthlyReportPlan> findByRowIdAndPlanYearAndPlanMonth(Integer rowId, Integer planYear, Integer planMonth);
}
