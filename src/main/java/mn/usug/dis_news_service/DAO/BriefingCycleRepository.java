package mn.usug.dis_news_service.DAO;

import mn.usug.dis_news_service.Entity.BriefingCycle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BriefingCycleRepository extends JpaRepository<BriefingCycle, Integer> {

    List<BriefingCycle> findByTaskIdOrderByCycleNoAsc(Integer taskId);

    List<BriefingCycle> findByTaskIdInOrderByCycleNoAsc(List<Integer> taskIds);

    /** Хамгийн сүүлийн (идэвхтэй) cycle */
    BriefingCycle findTopByTaskIdOrderByCycleNoDesc(Integer taskId);

    /** Дүгнэх хугацаа дууссан хэрнээ дүгнээгүй (score=null) cycle-ууд — auto-0 болгох нэр дэвшигч */
    List<BriefingCycle> findByScoreIsNullAndScoreDeadlineBefore(LocalDateTime deadline);
}
