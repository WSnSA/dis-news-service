package mn.usug.dis_news_service.DAO;

import mn.usug.dis_news_service.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserDAO extends JpaRepository<User,Integer> {

    @Query("select a from User a")
    List<User> findAll();

    @Query("select a from User a where a.activeFlag = true and a.username = ?1")
    User findUserByUsername(String username);

    @Query("select a from User a where a.activeFlag = true and a.departmentId = ?1 and a.positionId = ?2")
    List<User> findUsersByFilter(Integer depId, Integer positionId);
}
