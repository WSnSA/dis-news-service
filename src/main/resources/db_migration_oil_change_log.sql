-- ============================================================================
--  Тос тосолгооны бүртгэл (oil_change_log)
--  2026-09-28
--
--  Машины засварын бүртгэлээс (vehicle_repair) тусдаа, хөнгөн лог — зөвхөн
--  тээврийн хэрэгсэл, жолооч, огноо, гүйлтийг хурдан бичихэд зориулав.
-- ============================================================================

CREATE TABLE IF NOT EXISTS oil_change_log (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    vehicle_id    BIGINT      NOT NULL,
    plate_number  VARCHAR(20) NOT NULL COMMENT 'Давхар хадгална — машин устсан ч лог уншигдана',
    driver_id     BIGINT      NULL,
    driver_name   VARCHAR(150) NULL COMMENT 'Бүртгэсэн үеийн нэр — лавлахаас устсан ч түүх хэвээр',
    change_date   DATE        NOT NULL,
    odometer_km   INT         NOT NULL,
    note          VARCHAR(300) NULL,
    active_flag   INT         NOT NULL DEFAULT 1 COMMENT '1=идэвхтэй, 0=устгасан (soft delete)',
    created_at    DATETIME,
    created_by    INT,
    updated_at    DATETIME,
    updated_by    INT,
    INDEX idx_ocl_vehicle (vehicle_id),
    INDEX idx_ocl_plate   (plate_number),
    INDEX idx_ocl_date    (change_date)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
