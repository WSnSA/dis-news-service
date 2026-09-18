-- Засварын бүртгэл — дэлгэрэнгүй талбарууд
-- 2026-09-18
--
-- Их засвар / урсгал засвар / тос тосолгоо бүрт хэрэгтэй мэдээллийг нэмнэ:
-- гүйлтийн заалт, хариуцсан засварчин, байршил, гэмтлийн шалтгаан.
-- Бүгд NULL зөвшөөрнө — хуучин бичлэгүүд хэвээр ажиллана.
--
-- ДАХИН АЖИЛЛУУЛАХАД АЮУЛГҮЙ. MySQL-д "ADD COLUMN IF NOT EXISTS" байхгүй тул
-- багана бүрийг information_schema-аас шалгаад байхгүй үед л нэмнэ.
-- DELIMITER / PROCEDURE ашиглаагүй — бүх SQL client дээр ажиллана.

/* ── odometer_km ── */
SET @c := (SELECT COUNT(*) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE()
             AND TABLE_NAME   = 'vehicle_repair'
             AND COLUMN_NAME  = 'odometer_km');
SET @ddl := IF(@c = 0,
    'ALTER TABLE vehicle_repair ADD COLUMN odometer_km INT COMMENT ''Засварт орох үеийн гүйлт (км)''',
    'DO 0');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

/* ── next_service_km ── */
SET @c := (SELECT COUNT(*) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE()
             AND TABLE_NAME   = 'vehicle_repair'
             AND COLUMN_NAME  = 'next_service_km');
SET @ddl := IF(@c = 0,
    'ALTER TABLE vehicle_repair ADD COLUMN next_service_km INT COMMENT ''Дараагийн үйлчилгээ хийх одометрийн заалт (км)''',
    'DO 0');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

/* ── responsible_worker_id ── */
SET @c := (SELECT COUNT(*) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE()
             AND TABLE_NAME   = 'vehicle_repair'
             AND COLUMN_NAME  = 'responsible_worker_id');
SET @ddl := IF(@c = 0,
    'ALTER TABLE vehicle_repair ADD COLUMN responsible_worker_id BIGINT COMMENT ''repair_worker.id — засварыг хариуцсан хүн''',
    'DO 0');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

/* ── location ── */
SET @c := (SELECT COUNT(*) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE()
             AND TABLE_NAME   = 'vehicle_repair'
             AND COLUMN_NAME  = 'location');
SET @ddl := IF(@c = 0,
    'ALTER TABLE vehicle_repair ADD COLUMN location VARCHAR(200) COMMENT ''Байршил — гараж, талбай, гадны сервис''',
    'DO 0');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

/* ── external_org ── */
SET @c := (SELECT COUNT(*) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE()
             AND TABLE_NAME   = 'vehicle_repair'
             AND COLUMN_NAME  = 'external_org');
SET @ddl := IF(@c = 0,
    'ALTER TABLE vehicle_repair ADD COLUMN external_org VARCHAR(200) COMMENT ''Гадны байгууллагаар хийлгэсэн бол нэр''',
    'DO 0');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

/* ── fault_description ── */
SET @c := (SELECT COUNT(*) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE()
             AND TABLE_NAME   = 'vehicle_repair'
             AND COLUMN_NAME  = 'fault_description');
SET @ddl := IF(@c = 0,
    'ALTER TABLE vehicle_repair ADD COLUMN fault_description TEXT COMMENT ''Гэмтлийн шалтгаан / хийгдэх ажлын тайлбар''',
    'DO 0');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

/* ── driver_name ── */
SET @c := (SELECT COUNT(*) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE()
             AND TABLE_NAME   = 'vehicle_repair'
             AND COLUMN_NAME  = 'driver_name');
SET @ddl := IF(@c = 0,
    'ALTER TABLE vehicle_repair ADD COLUMN driver_name VARCHAR(150) COMMENT ''Хүлээлгэн өгсөн жолооч''',
    'DO 0');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

/* ── Индекс ── */
SET @c := (SELECT COUNT(*) FROM information_schema.STATISTICS
           WHERE TABLE_SCHEMA = DATABASE()
             AND TABLE_NAME   = 'vehicle_repair'
             AND INDEX_NAME   = 'idx_vehicle_repair_worker');
SET @ddl := IF(@c = 0,
    'CREATE INDEX idx_vehicle_repair_worker ON vehicle_repair (responsible_worker_id)',
    'DO 0');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
