package mn.usug.dis_news_service.DAO;

import mn.usug.dis_news_service.Entity.RepairPart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepairPartRepository extends JpaRepository<RepairPart, Long> {

    List<RepairPart> findByActiveFlagOrderByNameAsc(Integer activeFlag);
}
