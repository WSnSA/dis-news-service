package mn.usug.dis_news_service.DAO;

import mn.usug.dis_news_service.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserDAO extends JpaRepository<User,Integer> {

    @Query("select a from User a")
    List<User> findAll();

    @Query("select a from User a where a.activeFlag = true and a.username = ?1")
    User findUserByUsername(String username);

    @Query("""
  select u from User u
  where (:depId = 0 or u.departmentId = :depId)
    and (:positionId = 0 or u.positionId = :positionId)
""")
    List<User> findUsersByFilter(@Param("depId") Integer depId,
                                 @Param("positionId") Integer positionId);


    User findUserByMailAddress(String mailAddress);
}
