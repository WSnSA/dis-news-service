package mn.usug.dis_news_service.DAO;

import mn.usug.dis_news_service.Entity.BriefingTaskDelegate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface BriefingTaskDelegateRepository extends JpaRepository<BriefingTaskDelegate, Integer> {

    List<BriefingTaskDelegate> findByTaskId(Integer taskId);

    List<BriefingTaskDelegate> findByTaskIdIn(List<Integer> taskIds);

    List<BriefingTaskDelegate> findByUserId(Integer userId);

    boolean existsByTaskIdAndUserId(Integer taskId, Integer userId);

    @Modifying
    @Transactional
    void deleteByTaskId(Integer taskId);
}
