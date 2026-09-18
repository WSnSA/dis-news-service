package mn.usug.dis_news_service.DAO;

import mn.usug.dis_news_service.Entity.RepairSpecialty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepairSpecialtyRepository extends JpaRepository<RepairSpecialty, Long> {

    List<RepairSpecialty> findByActiveFlagOrderBySortOrderAscIdAsc(Integer activeFlag);
}
