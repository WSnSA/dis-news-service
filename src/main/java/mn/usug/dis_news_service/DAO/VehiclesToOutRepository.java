package mn.usug.dis_news_service.DAO;

import mn.usug.dis_news_service.Entity.VehiclesToOut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface VehiclesToOutRepository extends JpaRepository<VehiclesToOut, Integer> {
    List<VehiclesToOut> findAllByCreatedDateBetweenOrderByIdAsc(LocalDateTime from, LocalDateTime to);

    /**
     * Тухайн өдрийн ажилд гарах машинуудыг буцаана:
     * - vehicle_order-тай бол order.start_date <= date <= order.end_date
     * - vehicle_order-гүй (legacy) бол created_date-аар шүүнэ
     */
    @Query(value = """
        SELECT v.* FROM vehicles_to_out v
        LEFT JOIN vehicle_order o ON v.vehicle_order_id = o.id
        WHERE (
            (v.vehicle_order_id IS NULL AND DATE(v.created_date) = :date)
            OR (o.start_date <= :date AND o.end_date >= :date)
        )
        ORDER BY v.id ASC
    """, nativeQuery = true)
    List<VehiclesToOut> findByDate(@Param("date") LocalDate date);

    java.util.Optional<VehiclesToOut> findFirstByVehicleOrderIdOrderByCreatedDateDesc(Integer vehicleOrderId);

    List<VehiclesToOut> findAllByVehicleOrderIdOrderByIdAsc(Integer vehicleOrderId);
}
