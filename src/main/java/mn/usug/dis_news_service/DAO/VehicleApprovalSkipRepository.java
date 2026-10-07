package mn.usug.dis_news_service.DAO;

import mn.usug.dis_news_service.Entity.VehicleApprovalSkip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface VehicleApprovalSkipRepository extends JpaRepository<VehicleApprovalSkip, Integer> {

    boolean existsByUserId(Integer userId);

    @Modifying
    @Transactional
    void deleteByUserId(Integer userId);

    @Query("select s.userId from VehicleApprovalSkip s")
    List<Integer> findAllUserIds();
}
