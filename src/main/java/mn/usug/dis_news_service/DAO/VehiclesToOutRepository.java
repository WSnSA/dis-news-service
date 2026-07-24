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

    /* ==================== СТАТИСТИК ==================== */

    /** Тухайн жилд сар бүрээр хэдэн машин хуваарилсан → [month, count] */
    @Query(value = """
        SELECT MONTH(created_date) AS m, COUNT(*) AS c
        FROM vehicles_to_out
        WHERE active_flag = 1 AND YEAR(created_date) = :year
        GROUP BY MONTH(created_date)
    """, nativeQuery = true)
    List<Object[]> countByMonth(@Param("year") int year);

    /** Алба бүрт ямар төрлийн машин хэдэн ширхэг → [department, typeName, count] */
    @Query(value = """
        SELECT COALESCE(NULLIF(TRIM(v.department), ''), 'Тодорхойгүй') AS dep,
               COALESCE(t.name,
                        CASE v.order_type WHEN 1 THEN 'Суудлын' WHEN 0 THEN 'Механизм' ELSE 'Тодорхойгүй' END
               ) AS type_name,
               COUNT(*) AS c
        FROM vehicles_to_out v
        LEFT JOIN vehicle_type t ON v.vehicle_type_id = t.id
        WHERE v.active_flag = 1 AND YEAR(v.created_date) = :year
        GROUP BY dep, type_name
        ORDER BY dep, c DESC
    """, nativeQuery = true)
    List<Object[]> countByDeptAndType(@Param("year") int year);

    /** Нэг машин (улсын дугаараар) захиалгаар ажилд гарсан түүх. Хоосон зайг үл тоомсорлон харьцуулна. */
    @Query(value = """
        SELECT v.* FROM vehicles_to_out v
        WHERE v.active_flag = 1 AND (
            REPLACE(UPPER(COALESCE(v.vehicle_registration_number, '')), ' ', '') = REPLACE(UPPER(:plate), ' ', '')
            OR REPLACE(UPPER(COALESCE(JSON_UNQUOTE(JSON_EXTRACT(v.legacy_data, '$.vehicle_reg_raw')), '')), ' ', '')
               = REPLACE(UPPER(:plate), ' ', '')
        )
        ORDER BY v.created_date DESC
    """, nativeQuery = true)
    List<VehiclesToOut> findByPlate(@Param("plate") String plate);
}
