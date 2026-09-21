-- Баримтын мөрүүд (БМ-6 Шаардах хуудас) + засварын хариуцсан жолооч
-- 2026-09-21
--
-- Шаардах хуудас хоёр хэлбэртэй:
--   TEXT  — зориулалтаа бичгээр тайлбарлана (мөр бөглөхгүй)
--   ITEMS — сэлбэгийн лавлахаас сонгосон мөрүүдтэй (БМ-6 хүснэгт)
--
-- ДАХИН АЖИЛЛУУЛАХАД АЮУЛГҮЙ.

/* ── repair_document дээрх нэмэлт талбарууд ── */
SET @c := (SELECT COUNT(*) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'repair_document'
             AND COLUMN_NAME = 'doc_mode');
SET @ddl := IF(@c = 0,
    'ALTER TABLE repair_document ADD COLUMN doc_mode VARCHAR(20) NOT NULL DEFAULT ''TEXT'' COMMENT ''TEXT=тайлбартай, ITEMS=сэлбэгийн мөртэй''',
    'DO 0');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @c := (SELECT COUNT(*) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'repair_document'
             AND COLUMN_NAME = 'vehicle_id');
SET @ddl := IF(@c = 0,
    'ALTER TABLE repair_document ADD COLUMN vehicle_id BIGINT NULL COMMENT ''vehicle.id — дугаараар хайж сонгосон машин (заавал биш)''',
    'DO 0');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

/* БМ-6 маягтын толгойн талбарууд */
SET @c := (SELECT COUNT(*) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'repair_document'
             AND COLUMN_NAME = 'from_person');
SET @ddl := IF(@c = 0,
    'ALTER TABLE repair_document ADD COLUMN from_person VARCHAR(200) COMMENT ''Хэнээс — овог нэр, албан тушаал''',
    'DO 0');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @c := (SELECT COUNT(*) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'repair_document'
             AND COLUMN_NAME = 'to_place');
SET @ddl := IF(@c = 0,
    'ALTER TABLE repair_document ADD COLUMN to_place VARCHAR(200) COMMENT ''Хаана — цех, тасаг, алба''',
    'DO 0');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @c := (SELECT COUNT(*) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'repair_document'
             AND COLUMN_NAME = 'purpose');
SET @ddl := IF(@c = 0,
    'ALTER TABLE repair_document ADD COLUMN purpose VARCHAR(300) COMMENT ''Зориулалт''',
    'DO 0');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

/* Хүлээн авсан / олгосон / зөвшөөрсөн хүмүүс */
SET @c := (SELECT COUNT(*) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'repair_document'
             AND COLUMN_NAME = 'receiver_name');
SET @ddl := IF(@c = 0,
    'ALTER TABLE repair_document ADD COLUMN receiver_name VARCHAR(200) COMMENT ''Хүлээн авсан''',
    'DO 0');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @c := (SELECT COUNT(*) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'repair_document'
             AND COLUMN_NAME = 'issuer_name');
SET @ddl := IF(@c = 0,
    'ALTER TABLE repair_document ADD COLUMN issuer_name VARCHAR(200) COMMENT ''Олгосон''',
    'DO 0');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

/* ── Баримтын мөрүүд (БМ-6 хүснэгт) ── */
CREATE TABLE IF NOT EXISTS repair_document_item (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    repair_document_id BIGINT NOT NULL      COMMENT 'repair_document.id',
    repair_part_id     BIGINT               COMMENT 'repair_part.id — гараар бичсэн бол NULL',
    -- Нэр, кодыг мөрөнд хуулж авна: лавлах хожим өөрчлөгдсөн ч баримт хэвээр
    item_name          VARCHAR(300) NOT NULL COMMENT 'Материалын үнэт зүйлийн нэр',
    item_code          VARCHAR(60)           COMMENT 'Код / артикул',
    unit               VARCHAR(30)           COMMENT 'Хэмжих нэгж',
    qty_requested      DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT 'Хүссэн',
    qty_approved       DECIMAL(15,2)         COMMENT 'Зөвшөөрсөн',
    qty_issued         DECIMAL(15,2)         COMMENT 'Олгосон',
    unit_price         DECIMAL(15,2)         COMMENT 'Бүртгэсэн үеийн нэгж үнэ',
    sort_order         INT NOT NULL DEFAULT 0,
    active_flag        TINYINT NOT NULL DEFAULT 1,
    created_at         DATETIME,
    created_by         INT,
    KEY idx_rdi_doc (repair_document_id, active_flag)
) COMMENT 'Шаардах хуудас / актын мөрүүд';

/* ── Засварыг хариуцсан жолооч ──
   Машиныг хүлээлгэж өгсөн жолоочийг НЭРЭЭР нь бичдэг байв. Аль жолоочийн
   машин хэдийд засварт байсныг харахын тулд ID хэрэгтэй. */
SET @c := (SELECT COUNT(*) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'vehicle_repair'
             AND COLUMN_NAME = 'responsible_driver_id');
SET @ddl := IF(@c = 0,
    'ALTER TABLE vehicle_repair ADD COLUMN responsible_driver_id BIGINT NULL COMMENT ''driver.id — машиныг хариуцсан жолооч''',
    'DO 0');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @c := (SELECT COUNT(*) FROM information_schema.STATISTICS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'vehicle_repair'
             AND INDEX_NAME = 'idx_vr_driver');
SET @ddl := IF(@c = 0,
    'CREATE INDEX idx_vr_driver ON vehicle_repair (responsible_driver_id)',
    'DO 0');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

/* Хуучин мөрүүдийг driver_name-ээр нь нөхнө — ганц таарсан тохиолдолд */
UPDATE vehicle_repair r
   SET r.responsible_driver_id = (
        SELECT d.id FROM driver d
         WHERE TRIM(LOWER(d.full_name)) = TRIM(LOWER(r.driver_name))
         LIMIT 1
   )
 WHERE r.responsible_driver_id IS NULL
   AND r.driver_name IS NOT NULL
   AND TRIM(r.driver_name) <> ''
   AND (SELECT COUNT(*) FROM driver d
         WHERE TRIM(LOWER(d.full_name)) = TRIM(LOWER(r.driver_name))) = 1;
