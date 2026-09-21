-- Хуваарилалтад жолоочийн ID холбоно
-- 2026-09-21
--
-- vehicles_to_out нь жолоочийг зөвхөн НЭРЭЭР нь хадгалдаг байв. Тиймээс
-- "энэ жолооч хэзээ ажилласан бэ" гэдгийг нэрийн тэнцүүгээр л хайх
-- боломжтой — нэр давхцвал холилдоно, засвал холбоо тасарна.
--
-- Нэрийг хэвээр үлдээнэ (түүхийн бичлэг, ХУР-аас орсон хуучин мөрүүд),
-- ID нь холбоос болж нэмэгдэнэ.
--
-- ДАХИН АЖИЛЛУУЛАХАД АЮУЛГҮЙ.

SET @c := (SELECT COUNT(*) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'vehicles_to_out'
             AND COLUMN_NAME = 'driver_id');
SET @ddl := IF(@c = 0,
    'ALTER TABLE vehicles_to_out ADD COLUMN driver_id BIGINT NULL COMMENT ''driver.id — нэрээс гадна холбоос''',
    'DO 0');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @c := (SELECT COUNT(*) FROM information_schema.STATISTICS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'vehicles_to_out'
             AND INDEX_NAME = 'idx_vto_driver');
SET @ddl := IF(@c = 0,
    'CREATE INDEX idx_vto_driver ON vehicles_to_out (driver_id)',
    'DO 0');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

/* ── Хуучин мөрүүдийг нэрээр нь нөхнө ──
   ЗӨВХӨН нэр нь ганц жолоочтой яг таарсан тохиолдолд. Хоёр жолооч ижил
   нэртэй бол аль нь болохыг мэдэх аргагүй тул хөндөхгүй — тэр мөрүүд
   driver_id NULL хэвээр үлдэж, түүхэнд орохгүй. */
UPDATE vehicles_to_out v
   SET v.driver_id = (
        SELECT d.id FROM driver d
         WHERE TRIM(LOWER(d.full_name)) = TRIM(LOWER(v.driver_name))
         LIMIT 1
   )
 WHERE v.driver_id IS NULL
   AND v.driver_name IS NOT NULL
   AND TRIM(v.driver_name) <> ''
   AND (SELECT COUNT(*) FROM driver d
         WHERE TRIM(LOWER(d.full_name)) = TRIM(LOWER(v.driver_name))) = 1;

-- Хэдэн мөр нөхөгдөөгүйг харах:
--   SELECT driver_name, COUNT(*) FROM vehicles_to_out
--    WHERE driver_id IS NULL AND TRIM(COALESCE(driver_name,'')) <> ''
--    GROUP BY driver_name ORDER BY 2 DESC;
