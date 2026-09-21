package mn.usug.dis_news_service.DAO;

import mn.usug.dis_news_service.Entity.RepairCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepairCategoryRepository extends JpaRepository<RepairCategory, Long> {

    /** Зөвхөн идэвхтэй ангилал — UI жагсаалт / dropdown-д */
    List<RepairCategory> findByActiveFlagOrderBySortOrderAscIdAsc(Integer activeFlag);
}
