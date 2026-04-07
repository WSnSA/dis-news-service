package mn.usug.dis_news_service.Service;

import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.Model.WaterHourlyRowDto;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WaterHourlyService {

    private final NamedParameterJdbcTemplate jdbc;

    private static final String SQL = """
        WITH leaf_menu AS (
          SELECT
            m.id,
            m.parent_id,
            m.name,
            CASE
              WHEN m.parent_id = 3  THEN 'Эх үүсвэр'
              WHEN m.parent_id = 12 THEN 'Дамжуулах'
              WHEN m.parent_id = 24 THEN 'ЖС'
              WHEN m.parent_id = 27 THEN 'Усан сан'
              ELSE 'Бусад'
            END AS group_name,
            CASE
              WHEN m.parent_id = 3  THEN 1
              WHEN m.parent_id = 12 THEN 2
              WHEN m.parent_id = 24 THEN 3
              WHEN m.parent_id = 27 THEN 4
              ELSE 9
            END AS group_ord
          FROM dis_news.menu m
          WHERE m.parent_id IN (3,12,24,27)
            AND (m.active_flag IS NULL OR m.active_flag = 1)
        ),
        sec_pumps AS (
          SELECT
            menu_id,
            status,
            COUNT(*) AS cnt,
            GROUP_CONCAT(generator_no ORDER BY generator_no SEPARATOR ',') AS gen_list
          FROM dis_news.hourly_ws_second
          WHERE `date` >= :from AND `date` < :to
            AND `hour` = :h
            AND status IN (1,2,3)
          GROUP BY menu_id, status
        ),
        sec_meta AS (
          SELECT
            menu_id,
            MAX(pressure)     AS pressure,
            MAX(pressure_2)   AS pressure_2,
            MAX(pressure_3)   AS pressure_3,
            MAX(pressure_4)   AS pressure_4,
            MAX(chlorine)     AS chlorine,
            MAX(pumped_water) AS pumped_water
          FROM dis_news.hourly_ws_second
          WHERE `date` >= :from AND `date` < :to
            AND `hour` = :h
            AND status = 0 AND generator_no = 0
          GROUP BY menu_id
        ),
        -- Тухайн цагаас өмнөх хамгийн сүүлд бичсэн FM заалт (station-д тулгуурлан)
        prev_fm_ranked AS (
          SELECT
            menu_id,
            pipe_fm_1,
            pipe_fm_7,
            pipe_fm_8,
            ROW_NUMBER() OVER (
              PARTITION BY menu_id
              ORDER BY `date` DESC, `hour` DESC
            ) AS rn
          FROM dis_news.hourly_ws_station
          WHERE (
            (`date` >= :from AND `date` < :to AND `hour` < :h)
            OR `date` < :from
          )
        ),
        prev_fm AS (
          SELECT menu_id, pipe_fm_1 AS prev_fm1, pipe_fm_7 AS prev_fm7, pipe_fm_8 AS prev_fm8
          FROM prev_fm_ranked
          WHERE rn = 1
        )
        SELECT
          lm.group_name,
          lm.group_ord,
          lm.id   AS menu_id,
          lm.name AS station_name,

          ws.first_working_count   AS well_working,
          ws.first_pending_count   AS well_pending,
          ws.first_repairing_count AS well_repairing,

          ws.first_pool  AS pool_1,
          ws.second_pool AS pool_2,
          ws.third_pool  AS pool_3,
          ws.fourth_pool AS pool_4,

          ws.pipe_fm_1 AS pipe_fm_1,
          ws.pipe_fm_7 AS pipe_fm_7,
          ws.pipe_fm_8 AS pipe_fm_8,

          COALESCE(pw.gen_list, '0') AS pump_working,
          COALESCE(pp.gen_list, '0') AS pump_pending,
          COALESCE(pr.gen_list, '0') AS pump_repairing,

          CASE
            WHEN sm.pressure IS NULL AND sm.pressure_2 IS NULL THEN NULL
            WHEN sm.pressure_2 IS NULL THEN CAST(sm.pressure AS CHAR)
            ELSE CONCAT(GREATEST(sm.pressure, sm.pressure_2), '-', LEAST(sm.pressure, sm.pressure_2))
          END AS pressure_bar,

          sm.chlorine AS chlorine_mgL,

          -- Шахсан ус = FM заалтын зөрүү (өмнөх цагтай харьцуулсан)
          CASE
            WHEN ws.pipe_fm_1 IS NULL AND ws.pipe_fm_7 IS NULL AND ws.pipe_fm_8 IS NULL THEN NULL
            ELSE (
              CASE WHEN ws.pipe_fm_1 IS NOT NULL AND pf.prev_fm1 IS NOT NULL AND ws.pipe_fm_1 > pf.prev_fm1
                   THEN ws.pipe_fm_1 - pf.prev_fm1 ELSE 0 END +
              CASE WHEN ws.pipe_fm_7 IS NOT NULL AND pf.prev_fm7 IS NOT NULL AND ws.pipe_fm_7 > pf.prev_fm7
                   THEN ws.pipe_fm_7 - pf.prev_fm7 ELSE 0 END +
              CASE WHEN ws.pipe_fm_8 IS NOT NULL AND pf.prev_fm8 IS NOT NULL AND ws.pipe_fm_8 > pf.prev_fm8
                   THEN ws.pipe_fm_8 - pf.prev_fm8 ELSE 0 END
            )
          END AS pumped_m3h,

          TRIM(CONCAT(COALESCE(cu.last_name,''), ' ', COALESCE(cu.first_name,''))) AS created_by_name,
          DATE_FORMAT(ws.created_date, '%H:%i')                                    AS created_time,
          TRIM(CONCAT(COALESCE(uu.last_name,''), ' ', COALESCE(uu.first_name,''))) AS updated_by_name,
          DATE_FORMAT(ws.updated_date, '%H:%i')                                    AS updated_time

        FROM leaf_menu lm

        LEFT JOIN dis_news.hourly_ws_station ws
          ON ws.`date` >= :from AND ws.`date` < :to
         AND ws.`hour` = :h
         AND ws.menu_id = lm.id

        LEFT JOIN dis_news.users cu ON cu.id = ws.created_by
        LEFT JOIN dis_news.users uu ON uu.id = ws.updated_by

        LEFT JOIN prev_fm pf ON pf.menu_id = lm.id

        LEFT JOIN sec_meta sm ON sm.menu_id = lm.id

        LEFT JOIN sec_pumps pw ON pw.menu_id = lm.id AND pw.status = 1
        LEFT JOIN sec_pumps pp ON pp.menu_id = lm.id AND pp.status = 2
        LEFT JOIN sec_pumps pr ON pr.menu_id = lm.id AND pr.status = 3

        ORDER BY lm.group_ord, lm.id
        """;

    public List<WaterHourlyRowDto> getWaterHourly(LocalDate date, int hour) {
        if (hour < 0 || hour > 23) throw new IllegalArgumentException("hour must be 0..23");

        LocalDateTime from = date.atStartOfDay();
        LocalDateTime to = date.plusDays(1).atStartOfDay();

        var params = new MapSqlParameterSource()
                .addValue("from", Timestamp.valueOf(from))
                .addValue("to", Timestamp.valueOf(to))
                .addValue("h", hour);

        return jdbc.query(SQL, params, (rs, rowNum) -> WaterHourlyRowDto.builder()
                .groupName(rs.getString("group_name"))
                .groupOrd(rs.getInt("group_ord"))
                .menuId(rs.getInt("menu_id"))
                .stationName(rs.getString("station_name"))

                .wellWorking((Integer) rs.getObject("well_working"))
                .wellPending((Integer) rs.getObject("well_pending"))
                .wellRepairing((Integer) rs.getObject("well_repairing"))

                .pool1((Integer) rs.getObject("pool_1"))
                .pool2((Integer) rs.getObject("pool_2"))
                .pool3((Integer) rs.getObject("pool_3"))
                .pool4((Integer) rs.getObject("pool_4"))

                .pipeFm1((Integer) rs.getObject("pipe_fm_1"))
                .pipeFm7((Integer) rs.getObject("pipe_fm_7"))
                .pipeFm8((Integer) rs.getObject("pipe_fm_8"))

                .pumpWorking(rs.getString("pump_working"))
                .pumpPending(rs.getString("pump_pending"))
                .pumpRepairing(rs.getString("pump_repairing"))

                .pressureBar(rs.getString("pressure_bar"))
                .chlorineMgL(toDouble(rs.getObject("chlorine_mgL")))
                .pumpedM3h(toDouble(rs.getObject("pumped_m3h")))
                .createdByName(rs.getString("created_by_name"))
                .createdTime(rs.getString("created_time"))
                .updatedByName(rs.getString("updated_by_name"))
                .updatedTime(rs.getString("updated_time"))
                .build()
        );
    }

    private static Double toDouble(Object v) {
        if (v == null) return null;
        if (v instanceof Number n) return n.doubleValue(); // BigDecimal ч энд орно
        return Double.valueOf(v.toString());
    }
}
