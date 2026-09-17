-- ============================================================================
--  Засварын бүртгэл — машины засварын тэмдэглэл (vehicle_repair)
--  2026-09-17
--
--  db_migration_repair_category.sql-ийг ЭХЛЭЭД ажиллуулсан байх ёстой
--  (repair_category_id нь тэр хүснэгтийн мөрийг заана).
--
--  Нээлттэй бичлэг (status=0) байх хугацаанд тухайн машиныг "Машин хуваарилалт"
--  дэлгэцийн сонголтоос хасна.
-- ============================================================================

CREATE TABLE IF NOT EXISTS vehicle_repair (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    vehicle_id         BIGINT      NOT NULL,
    plate_number       VARCHAR(20) NOT NULL COMMENT 'Давхар хадгална — машин устсан ч түүх уншигдана',
    repair_category_id BIGINT      NOT NULL,
    start_date         DATE        NOT NULL,
    end_date           DATE        NULL COMMENT 'Төлөвлөсөн/бодит дуусах огноо. NULL = тодорхойгүй',
    status             INT         NOT NULL DEFAULT 0 COMMENT '0=засварт байна, 1=дууссан',
    note               TEXT,
    active_flag        INT         NOT NULL DEFAULT 1 COMMENT '1=идэвхтэй, 0=устгасан (soft delete)',
    created_at         DATETIME,
    created_by         INT,
    updated_at         DATETIME,
    updated_by         INT,
    INDEX idx_vr_vehicle (vehicle_id),
    INDEX idx_vr_plate   (plate_number),
    INDEX idx_vr_open    (active_flag, status),
    INDEX idx_vr_dates   (start_date, end_date)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
