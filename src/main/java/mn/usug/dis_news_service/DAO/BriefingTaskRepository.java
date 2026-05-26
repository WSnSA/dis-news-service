package mn.usug.dis_news_service.DAO;

import mn.usug.dis_news_service.Entity.BriefingTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BriefingTaskRepository extends JpaRepository<BriefingTask, Integer> {

    @Query("select t from BriefingTask t where t.activeFlag = 1 order by t.id desc")
    List<BriefingTask> findActive();

    @Query("select t from BriefingTask t where t.activeFlag = 1 and t.assignerId = :assignerId order by t.id desc")
    List<BriefingTask> findActiveByAssigner(Integer assignerId);
}
