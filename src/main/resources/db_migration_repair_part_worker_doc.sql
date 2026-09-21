-- Засварын бүртгэл — Сэлбэг, Ажилтан, Баримт бөглөх
-- 2026-09-18
--
-- 1-р шат: лавлах жагсаалтууд (сэлбэгийн нэр төрөл, засварчдын бүртгэл) ба
-- баримтын бүртгэл. Тухайн засварт ямар сэлбэг зарцуулсан, хэн ажилласныг
-- холбох хэсэг 2-р шатанд нэмэгдэнэ.
--
-- db_migration_repair_category.sql-ийн ДАРАА ажиллуулна.

/* ─────────── Сэлбэгийн төрөл (Сэлбэг табын шүүлтүүр) ─────────── */
CREATE TABLE IF NOT EXISTS repair_part_type (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(150) NOT NULL,
    code        VARCHAR(30)           COMMENT 'Системийн түлхүүр — үндсэн 4 төрөлд л утгатай',
    sort_order  INT,
    active_flag TINYINT NOT NULL DEFAULT 1,
    created_at  DATETIME,
    created_by  INT,
    updated_at  DATETIME,
    updated_by  INT,
    UNIQUE KEY uq_repair_part_type_code (code)
) COMMENT 'Сэлбэгийн төрөл';

INSERT INTO repair_part_type (name, code, sort_order, active_flag, created_at)
SELECT * FROM (
    SELECT 'Сэлбэг'        AS name, 'PART'      AS code, 1 AS sort_order, 1 AS active_flag, NOW() AS created_at UNION ALL
    SELECT 'Тос тосолгоо',        'LUBRICANT',        2, 1, NOW() UNION ALL
    SELECT 'Дугуй',               'TYRE',             3, 1, NOW() UNION ALL
    SELECT 'Аккумулятор',         'BATTERY',          4, 1, NOW()
) seed
WHERE NOT EXISTS (SELECT 1 FROM repair_part_type WHERE code = seed.code);

/* ─────────── Сэлбэгийн лавлах ─────────── */
CREATE TABLE IF NOT EXISTS repair_part (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    part_type_id  BIGINT NOT NULL      COMMENT 'repair_part_type.id',
    name          VARCHAR(200) NOT NULL,
    code          VARCHAR(60)          COMMENT 'Каталогийн код / артикул',
    unit          VARCHAR(30)          COMMENT 'ш, л, кг гэх мэт',
    unit_price    DECIMAL(15,2)        COMMENT 'Нэгж үнэ (₮)',
    stock_qty     DECIMAL(15,2)        COMMENT 'Үлдэгдэл',
    note          TEXT,
    active_flag   TINYINT NOT NULL DEFAULT 1,
    created_at    DATETIME,
    created_by    INT,
    updated_at    DATETIME,
    updated_by    INT,
    KEY idx_repair_part_type (part_type_id, active_flag)
) COMMENT 'Сэлбэгийн лавлах жагсаалт';

/* ─────────── Ажилтны мэргэжил (Ажилтан табын шүүлтүүр) ─────────── */
CREATE TABLE IF NOT EXISTS repair_specialty (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(150) NOT NULL,
    code        VARCHAR(30),
    sort_order  INT,
    active_flag TINYINT NOT NULL DEFAULT 1,
    created_at  DATETIME,
    created_by  INT,
    updated_at  DATETIME,
    updated_by  INT,
    UNIQUE KEY uq_repair_specialty_code (code)
) COMMENT 'Засварын ажилтны мэргэжил';

INSERT INTO repair_specialty (name, code, sort_order, active_flag, created_at)
SELECT * FROM (
    SELECT 'Автын засварчин' AS name, 'MECHANIC'    AS code, 1 AS sort_order, 1 AS active_flag, NOW() AS created_at UNION ALL
    SELECT 'Цахилгаанчин',           'ELECTRICIAN',        2, 1, NOW() UNION ALL
    SELECT 'Моторчин',               'MOTOR',              3, 1, NOW() UNION ALL
    SELECT 'Гагнуурчин',             'WELDER',             4, 1, NOW()
) seed
WHERE NOT EXISTS (SELECT 1 FROM repair_specialty WHERE code = seed.code);

/* ─────────── Ажилтны бүртгэл ─────────── */
CREATE TABLE IF NOT EXISTS repair_worker (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    specialty_id BIGINT NOT NULL       COMMENT 'repair_specialty.id',
    name         VARCHAR(150) NOT NULL,
    phone        VARCHAR(30),
    grade        VARCHAR(60)           COMMENT 'Зэрэг / мэргэшлийн зэрэглэл',
    note         TEXT,
    active_flag  TINYINT NOT NULL DEFAULT 1,
    created_at   DATETIME,
    created_by   INT,
    updated_at   DATETIME,
    updated_by   INT,
    KEY idx_repair_worker_specialty (specialty_id, active_flag)
) COMMENT 'Засварын ажилтан';

/* ─────────── Баримт (Акт / Шаардах) ─────────── */
CREATE TABLE IF NOT EXISTS repair_document (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    doc_type          VARCHAR(20) NOT NULL COMMENT 'ACT=Акт, REQUEST=Шаардах',
    doc_no            VARCHAR(60)          COMMENT 'Баримтын дугаар',
    doc_date          DATE                 COMMENT 'Баримтын огноо',
    vehicle_repair_id BIGINT               COMMENT 'vehicle_repair.id — холбоотой засвар',
    plate_number      VARCHAR(50)          COMMENT 'Улсын дугаар (түүхэнд үлдээх)',
    amount            DECIMAL(15,2)        COMMENT 'Нийт дүн (₮)',
    content           TEXT                 COMMENT 'Баримтын агуулга / тайлбар',
    active_flag       TINYINT NOT NULL DEFAULT 1,
    created_at        DATETIME,
    created_by        INT,
    updated_at        DATETIME,
    updated_by        INT,
    KEY idx_repair_document_type (doc_type, active_flag),
    KEY idx_repair_document_repair (vehicle_repair_id)
) COMMENT 'Засварын баримт — акт, шаардах';
