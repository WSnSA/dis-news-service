-- Засварын мэдээ (өдөр тутам / 7 хоног / хагас жил) гаргахад шаардагдах өгөгдөл
-- 2026-09-21
--
-- Тайлангийн загварт байгаа боловч системд байхгүй байсан талбарууд:
--   - засварт орсон ЦАГ (09:00) — одоогоор зөвхөн огноо хадгалагддаг байв
--   - бэлэн болох хугацаа
--   - сэлбэггүй зогсож байгаа эсэх ("Сэлбэггүй зогсож байгаа – 2")
--   - тухайн өдөр хэн ажилласан, хэн өвчтэй, хэн нөхөн амралттай байсан
--
-- ДАХИН АЖИЛЛУУЛАХАД АЮУЛГҮЙ.

/* ── vehicle_repair дээрх нэмэлт талбарууд ── */
SET @c := (SELECT COUNT(*) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'vehicle_repair'
             AND COLUMN_NAME = 'start_time');
SET @ddl := IF(@c = 0,
    'ALTER TABLE vehicle_repair ADD COLUMN start_time TIME COMMENT ''Засварт орсон цаг (09:00)''',
    'DO 0');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @c := (SELECT COUNT(*) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'vehicle_repair'
             AND COLUMN_NAME = 'expected_ready');
SET @ddl := IF(@c = 0,
    'ALTER TABLE vehicle_repair ADD COLUMN expected_ready DATE COMMENT ''Бэлэн болох хугацаа''',
    'DO 0');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @c := (SELECT COUNT(*) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'vehicle_repair'
             AND COLUMN_NAME = 'waiting_parts');
SET @ddl := IF(@c = 0,
    'ALTER TABLE vehicle_repair ADD COLUMN waiting_parts TINYINT NOT NULL DEFAULT 0 COMMENT ''1=сэлбэггүй зогсож байгаа''',
    'DO 0');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

/* ── Өдөр тутмын ирц: хэн ажилласан, өвчтэй, нөхөн амралттай ── */
CREATE TABLE IF NOT EXISTS repair_attendance (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    work_date        DATE   NOT NULL COMMENT 'Аль өдрийн ирц',
    repair_worker_id BIGINT NOT NULL COMMENT 'repair_worker.id',
    status           TINYINT NOT NULL DEFAULT 1
                     COMMENT '1=ажилласан, 2=өвчтэй, 3=нөхөн амралт, 4=чөлөөтэй',
    note             VARCHAR(300),
    active_flag      TINYINT NOT NULL DEFAULT 1,
    created_at       DATETIME,
    created_by       INT,
    updated_at       DATETIME,
    updated_by       INT,
    UNIQUE KEY uq_attendance (work_date, repair_worker_id),
    KEY idx_attendance_date (work_date, active_flag)
) COMMENT 'Засварын хэсгийн өдөр тутмын ирц';

/* ── Засварын зураг ── */
CREATE TABLE IF NOT EXISTS vehicle_repair_photo (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    vehicle_repair_id BIGINT NOT NULL COMMENT 'vehicle_repair.id',
    object_name       VARCHAR(300) NOT NULL COMMENT 'File service дээрх нэр (uuid)',
    file_name         VARCHAR(300) COMMENT 'Анхны файлын нэр',
    content_type      VARCHAR(100),
    file_size         BIGINT,
    note              VARCHAR(300),
    active_flag       TINYINT NOT NULL DEFAULT 1,
    created_at        DATETIME,
    created_by        INT,
    KEY idx_repair_photo (vehicle_repair_id, active_flag)
) COMMENT 'Засварын зураг — нотлох баримт';
