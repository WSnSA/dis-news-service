-- Засварын бүртгэл — 2-р шат: тухайн засварт зарцуулсан сэлбэг, ажилласан хүн
-- 2026-09-18
--
-- 1-р шатны лавлах жагсаалтууд (repair_part, repair_worker) дээр тулгуурлана.
-- db_migration_repair_part_worker_doc.sql-ийн ДАРАА ажиллуулна.

/* ─────────── Засварт зарцуулсан сэлбэг ─────────── */
CREATE TABLE IF NOT EXISTS vehicle_repair_part (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    vehicle_repair_id BIGINT NOT NULL      COMMENT 'vehicle_repair.id',
    repair_part_id    BIGINT NOT NULL      COMMENT 'repair_part.id',
    qty               DECIMAL(15,2) NOT NULL DEFAULT 1,
    -- Үнийг мөрөнд хуулж авна: лавлахын үнэ хожим өөрчлөгдсөн ч
    -- бүртгэсэн үеийн дүн хэвээр үлдэнэ.
    unit_price        DECIMAL(15,2)        COMMENT 'Бүртгэсэн үеийн нэгж үнэ',
    note              VARCHAR(300),
    active_flag       TINYINT NOT NULL DEFAULT 1,
    created_at        DATETIME,
    created_by        INT,
    updated_at        DATETIME,
    updated_by        INT,
    KEY idx_vrp_repair (vehicle_repair_id, active_flag),
    KEY idx_vrp_part (repair_part_id)
) COMMENT 'Засварт зарцуулсан сэлбэг';

/* ─────────── Засварт ажилласан хүн ─────────── */
CREATE TABLE IF NOT EXISTS vehicle_repair_worker (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    vehicle_repair_id BIGINT NOT NULL      COMMENT 'vehicle_repair.id',
    repair_worker_id  BIGINT NOT NULL      COMMENT 'repair_worker.id',
    hours             DECIMAL(10,2)        COMMENT 'Ажилласан цаг',
    note              VARCHAR(300),
    active_flag       TINYINT NOT NULL DEFAULT 1,
    created_at        DATETIME,
    created_by        INT,
    updated_at        DATETIME,
    updated_by        INT,
    KEY idx_vrw_repair (vehicle_repair_id, active_flag),
    KEY idx_vrw_worker (repair_worker_id)
) COMMENT 'Засварт ажилласан хүн';
