-- Машин захиалгын ээлж (Өглөө / Өдөр) — нэг машин давхар хуваарилагдахаас сэргийлнэ
-- 2026-09-16
--
-- time_slot нь bitmask:
--     1 = Өглөө
--     2 = Өдөр (үдээс хойш)
--     3 = Бүтэн өдөр (1|2)
-- Хоёр захиалга мөргөлдөж байгаа эсэхийг (a.time_slot & b.time_slot) <> 0 гэж шалгана.

ALTER TABLE vehicle_order
    ADD COLUMN time_slot TINYINT NOT NULL DEFAULT 3
        COMMENT 'Ээлж bitmask: 1=Өглөө, 2=Өдөр, 3=Бүтэн өдөр';

-- Хуучин суудлын захиалгын requested_time текстээс ээлжийг тааруулна.
-- "08:00" → Өглөө, "14:30" → Өдөр. Таних боломжгүй бол бүтэн өдөр (3) хэвээр үлдэнэ.
UPDATE vehicle_order
SET time_slot = 1
WHERE order_type = 1
  AND start_date = end_date
  AND requested_time REGEXP '^[0-9]{1,2}:'
  AND CAST(SUBSTRING_INDEX(requested_time, ':', 1) AS UNSIGNED) < 12;

UPDATE vehicle_order
SET time_slot = 2
WHERE order_type = 1
  AND start_date = end_date
  AND requested_time REGEXP '^[0-9]{1,2}:'
  AND CAST(SUBSTRING_INDEX(requested_time, ':', 1) AS UNSIGNED) >= 12;

-- Олон өдрийн захиалга бүтэн өдрөөр тооцогдоно
UPDATE vehicle_order
SET time_slot = 3
WHERE start_date IS NOT NULL AND end_date IS NOT NULL AND end_date > start_date;

-- Мөргөлдөөн шалгах query (start_date/end_date хамрах муж + status) хурдан ажиллахад
CREATE INDEX idx_vehicle_order_range ON vehicle_order (start_date, end_date, status, active_flag);

-- Хуваарилалтаас улсын дугаараар хайхад
CREATE INDEX idx_vehicles_to_out_order ON vehicles_to_out (vehicle_order_id, active_flag);
