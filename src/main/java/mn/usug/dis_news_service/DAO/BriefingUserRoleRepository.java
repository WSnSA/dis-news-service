package mn.usug.dis_news_service.DAO;

import mn.usug.dis_news_service.Entity.BriefingUserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface BriefingUserRoleRepository extends JpaRepository<BriefingUserRole, Integer> {

    List<BriefingUserRole> findByUserId(Integer userId);

    List<BriefingUserRole> findByRoleKey(String roleKey);

    boolean existsByUserIdAndRoleKey(Integer userId, String roleKey);

    @Modifying
    @Transactional
    void deleteByUserIdAndRoleKey(Integer userId, String roleKey);

    /** MANAGER эсвэл ADMIN дүртэй хэрэглэгчдийн id (assigner сонгох жагсаалтад) */
    @Query("select distinct r.userId from BriefingUserRole r where r.roleKey in :roleKeys")
    List<Integer> findUserIdsByRoleKeys(List<String> roleKeys);
}
