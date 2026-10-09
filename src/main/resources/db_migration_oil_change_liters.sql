-- Тос тосолгооны бүртгэл — зарцуулсан тосны хэмжээ (литр)
-- 2026-10-09
--
-- Хуучин бичлэгт литр бүртгэгдээгүй тул NULL байна. Шинэ бүртгэлд UI болон
-- /repair/oil-change/save заавал шаардана.
--
-- ДАХИН АЖИЛЛУУЛАХАД АЮУЛГҮЙ.

SET @c := (SELECT COUNT(*) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE()
             AND TABLE_NAME   = 'oil_change_log'
             AND COLUMN_NAME  = 'oil_liters');
SET @ddl := IF(@c = 0,
    'ALTER TABLE oil_change_log ADD COLUMN oil_liters DECIMAL(8,2) NULL COMMENT ''Зарцуулсан тос (литр)'' AFTER odometer_km',
    'DO 0');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
