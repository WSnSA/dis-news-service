-- Зарцуулалтын мөрөнд НЭРийг хуулж авна (snapshot)
-- 2026-09-21
--
-- Асуудал: ажилтан / сэлбэгийн нэр нь лавлахаас амьдаар уншигддаг байв.
-- Тиймээс устгасан ажилтныг сэргээгээд нэрийг нь өөрчлөхөд түүний ӨМНӨХ
-- бүх засварын бичлэг дээрх нэр хамт өөрчлөгддөг — түүх гэж хэлэх аргагүй.
--
-- Нэгж үнийг аль хэдийн мөрөнд хуулж авдаг (unit_price). Нэрийг ч мөн адил
-- болгоно: бүртгэсэн агшны нэр мөрөндөө үлдэж, лавлах дараа нь хэрхэн
-- өөрчлөгдсөнөөс үл хамаарна.
--
-- ДАХИН АЖИЛЛУУЛАХАД АЮУЛГҮЙ.

/* ── vehicle_repair_worker: ажилтан, мэргэжлийн нэр ── */
SET @c := (SELECT COUNT(*) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'vehicle_repair_worker'
             AND COLUMN_NAME = 'worker_name');
SET @ddl := IF(@c = 0,
    'ALTER TABLE vehicle_repair_worker ADD COLUMN worker_name VARCHAR(200) COMMENT ''Бүртгэсэн үеийн ажилтны нэр''',
    'DO 0');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @c := (SELECT COUNT(*) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'vehicle_repair_worker'
             AND COLUMN_NAME = 'specialty_name');
SET @ddl := IF(@c = 0,
    'ALTER TABLE vehicle_repair_worker ADD COLUMN specialty_name VARCHAR(200) COMMENT ''Бүртгэсэн үеийн мэргэжил''',
    'DO 0');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

/* ── vehicle_repair_part: сэлбэгийн нэр, нэгж ── */
SET @c := (SELECT COUNT(*) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'vehicle_repair_part'
             AND COLUMN_NAME = 'part_name');
SET @ddl := IF(@c = 0,
    'ALTER TABLE vehicle_repair_part ADD COLUMN part_name VARCHAR(200) COMMENT ''Бүртгэсэн үеийн сэлбэгийн нэр''',
    'DO 0');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @c := (SELECT COUNT(*) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'vehicle_repair_part'
             AND COLUMN_NAME = 'part_type_name');
SET @ddl := IF(@c = 0,
    'ALTER TABLE vehicle_repair_part ADD COLUMN part_type_name VARCHAR(200) COMMENT ''Бүртгэсэн үеийн төрөл''',
    'DO 0');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @c := (SELECT COUNT(*) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'vehicle_repair_part'
             AND COLUMN_NAME = 'part_unit');
SET @ddl := IF(@c = 0,
    'ALTER TABLE vehicle_repair_part ADD COLUMN part_unit VARCHAR(30) COMMENT ''Бүртгэсэн үеийн хэмжих нэгж''',
    'DO 0');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

/* ── Хуучин мөрүүдийг одоогийн лавлахаас нөхнө ──
   Одоогийн нэр л байгаа мэдээлэл. Үүнээс хойш нэр мөрөндөө царцана. */
UPDATE vehicle_repair_worker l
    JOIN repair_worker w ON w.id = l.repair_worker_id
    LEFT JOIN repair_specialty s ON s.id = w.specialty_id
SET l.worker_name    = COALESCE(l.worker_name, w.name),
    l.specialty_name = COALESCE(l.specialty_name, s.name)
WHERE l.worker_name IS NULL OR l.specialty_name IS NULL;

UPDATE vehicle_repair_part l
    JOIN repair_part p ON p.id = l.repair_part_id
    LEFT JOIN repair_part_type t ON t.id = p.part_type_id
SET l.part_name      = COALESCE(l.part_name, p.name),
    l.part_type_name = COALESCE(l.part_type_name, t.name),
    l.part_unit      = COALESCE(l.part_unit, p.unit)
WHERE l.part_name IS NULL OR l.part_type_name IS NULL OR l.part_unit IS NULL;
