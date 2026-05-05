-- EртHub Vehicle Compliance tables
-- Run once on dis_news database

CREATE TABLE IF NOT EXISTS vehicle_inspection (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    plate_number VARCHAR(20) NOT NULL,
    inspected_date DATE,
    expire_date    DATE,
    is_passed      TINYINT(1) DEFAULT 0,
    checked_at     DATETIME,
    created_at     DATETIME,
    updated_at     DATETIME,
    UNIQUE KEY uq_vi_plate (plate_number)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS vehicle_insurance (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    plate_number     VARCHAR(20) NOT NULL,
    policy_number    VARCHAR(50),
    insurance_company VARCHAR(100),
    expire_date      DATETIME,
    is_active        TINYINT(1) DEFAULT 0,
    checked_at       DATETIME,
    created_at       DATETIME,
    updated_at       DATETIME,
    UNIQUE KEY uq_vins_plate (plate_number)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS vehicle_penalty (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    plate_number     VARCHAR(20) NOT NULL,
    bar_code         VARCHAR(60) NOT NULL,
    amount           INT DEFAULT 0,
    local_name       VARCHAR(300),
    is_paid          TINYINT(1) DEFAULT 0,
    pass_date        DATETIME,
    reason_type      VARCHAR(200),
    reason_type_code INT,
    checked_at       DATETIME,
    created_at       DATETIME,
    UNIQUE KEY uq_vp_barcode (bar_code),
    INDEX idx_vp_plate (plate_number),
    INDEX idx_vp_paid  (is_paid)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
