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

    /* ==================== МАШИНЫ ХУВААРЬ ==================== */

    /**
     * Өгөгдсөн өдрөөс хойш үргэлжлэх бүх хуваарилалт — машинаар нь эрэмбэлсэн.
     * "Ажилд гарах" таб нэг өдрөөр шүүдэг тул 15-19-нд захиалсан машиныг олоход
     * өдөр бүрийг гүйлгэх шаардлагатай байсныг орлоно.
     *
     * → [id, plate, mechanism, driver, phone, orderId, dep, work, startDate, endDate, orderType]
     */
    @Query(value = """
        SELECT v.id                                                            AS id,
               v.vehicle_registration_number                                   AS plate,
               v.vehicle_mechanism                                             AS mechanism,
               v.driver_name                                                   AS driver,
               v.driver_phone_number                                           AS phone,
               o.id                                                            AS order_id,
               COALESCE(NULLIF(TRIM(v.department), ''), '')                    AS dep,
               COALESCE(NULLIF(TRIM(v.work_description), ''), o.work_description) AS work,
               COALESCE(o.start_date, o.order_date)                            AS s_date,
               COALESCE(o.end_date, o.start_date, o.order_date)                AS e_date,
               COALESCE(o.order_type, 0)                                       AS o_type
        FROM vehicles_to_out v
        JOIN vehicle_order o ON v.vehicle_order_id = o.id
        WHERE v.active_flag = 1
          AND o.active_flag = 1
          AND v.vehicle_registration_number IS NOT NULL
          AND TRIM(v.vehicle_registration_number) <> ''
          AND COALESCE(o.end_date, o.start_date, o.order_date) >= :from
        ORDER BY plate, s_date
    """, nativeQuery = true)
    List<Object[]> findUpcoming(@Param("from") LocalDate from);

    /**
     * Жолоочийн ажлын түүх — огнооны мужид давхцаж буй бүх хуваарилалт.
     *
     * Огноо нь vehicle_order дээр байдаг тул JOIN хийнэ. Захиалгагүй
     * (vehicle_order_id IS NULL) хуучин мөрүүд энд ОРОХГҮЙ — тэдэнд огноо
     * байхгүй тул "хэзээ ажилласан" гэдгийг хэлэх аргагүй.
     *
     * → [vtoId, driverId, driverName, phone, plate, mechanism, dep, work, startDate, endDate]
     */
    @Query(value = """
        SELECT v.id                                                            AS vto_id,
               v.driver_id                                                     AS driver_id,
               v.driver_name                                                   AS driver_name,
               v.driver_phone_number                                           AS phone,
               v.vehicle_registration_number                                   AS plate,
               v.vehicle_mechanism                                             AS mechanism,
               COALESCE(NULLIF(TRIM(v.department), ''), '')                    AS dep,
               COALESCE(NULLIF(TRIM(v.work_description), ''), o.work_description) AS work,
               COALESCE(o.start_date, o.order_date)                            AS s_date,
               COALESCE(o.end_date, o.start_date, o.order_date)                AS e_date
        FROM vehicles_to_out v
        JOIN vehicle_order o ON v.vehicle_order_id = o.id
        WHERE v.active_flag = 1
          AND o.active_flag = 1
          AND v.driver_id IS NOT NULL
          AND COALESCE(o.end_date, o.start_date, o.order_date) >= :from
          AND COALESCE(o.start_date, o.order_date)             <= :to
        ORDER BY s_date DESC, v.id DESC
    """, nativeQuery = true)
    List<Object[]> findDriverHistory(@Param("from") LocalDate from, @Param("to") LocalDate to);

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

    /**
     * Тайлангийн тоо бүрийн ард байгаа дэлгэрэнгүй мөрүүд (нэг жилээр).
     * dep / type_name / month нь countByMonth / countByDeptAndType-тэй ижил дүрмээр тооцогдоно
     * тул тоонууд яг тааран экспортлогдоно.
     */
    @Query(value = """
        SELECT DATE_FORMAT(v.created_date, '%Y-%m-%d %H:%i') AS created_str,
               MONTH(v.created_date) AS m,
               COALESCE(NULLIF(TRIM(v.department), ''), 'Тодорхойгүй') AS dep,
               COALESCE(t.name,
                        CASE v.order_type WHEN 1 THEN 'Суудлын' WHEN 0 THEN 'Механизм' ELSE 'Тодорхойгүй' END
               ) AS type_name,
               COALESCE(NULLIF(TRIM(v.work_description), ''), JSON_UNQUOTE(JSON_EXTRACT(v.legacy_data, '$.hiigdeh_ajil'))) AS work,
               COALESCE(NULLIF(TRIM(v.vehicle_mechanism), ''), JSON_UNQUOTE(JSON_EXTRACT(v.legacy_data, '$.mashin_mehanizm'))) AS mech,
               COALESCE(NULLIF(TRIM(v.vehicle_registration_number), ''), JSON_UNQUOTE(JSON_EXTRACT(v.legacy_data, '$.vehicle_reg_raw'))) AS reg,
               v.driver_name AS driver,
               COALESCE(NULLIF(TRIM(v.driver_phone_number), ''), JSON_UNQUOTE(JSON_EXTRACT(v.legacy_data, '$.phone_raw'))) AS phone
        FROM vehicles_to_out v
        LEFT JOIN vehicle_type t ON v.vehicle_type_id = t.id
        WHERE v.active_flag = 1 AND YEAR(v.created_date) = :year
        ORDER BY v.created_date
    """, nativeQuery = true)
    List<Object[]> statsDetailByYear(@Param("year") int year);

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
