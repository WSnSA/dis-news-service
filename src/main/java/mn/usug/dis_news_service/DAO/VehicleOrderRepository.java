package mn.usug.dis_news_service.DAO;

import mn.usug.dis_news_service.Entity.VehicleOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VehicleOrderRepository extends JpaRepository<VehicleOrder, Long> {

    @Query("""
        select v
        from VehicleOrder v
        where v.activeFlag = 1
          and v.orderDate = :date
        order by v.createdDate
    """)
    List<VehicleOrder> findByDate(@Param("date") LocalDate date);
}

