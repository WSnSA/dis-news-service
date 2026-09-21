-- Техникийн комиссын акт (ДМYА маягт) — актын жинхэнэ загварын талбарууд
-- 2026-09-21
--
-- Акт нь зүгээр нэг "дүн бүхий баримт" биш: элэгдсэн эд ангийг комисс
-- шалгаж, гүйлтийн норм хэдэн км байснаас хэд явсныг тэмдэглэж, дутууг
-- хэн хариуцахыг шийддэг тогтоол. Тиймээс тусдаа талбарууд хэрэгтэй.
--
-- ДАХИН АЖИЛЛУУЛАХАД АЮУЛГҮЙ.

SET @t := 'repair_document';

/* form_no — "УСУГ ДМYА № 12" гэсэн маягтын дугаар */
SET @c := (SELECT COUNT(*) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = @t AND COLUMN_NAME = 'form_no');
SET @ddl := IF(@c = 0, 'ALTER TABLE repair_document ADD COLUMN form_no VARCHAR(60) COMMENT ''ДМYА № — маягтын дугаар''', 'DO 0');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

/* БАТЛАВ — баталсан хүн */
SET @c := (SELECT COUNT(*) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = @t AND COLUMN_NAME = 'approver_title');
SET @ddl := IF(@c = 0, 'ALTER TABLE repair_document ADD COLUMN approver_title VARCHAR(200) COMMENT ''Баталсан хүний албан тушаал''', 'DO 0');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @c := (SELECT COUNT(*) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = @t AND COLUMN_NAME = 'approver_name');
SET @ddl := IF(@c = 0, 'ALTER TABLE repair_document ADD COLUMN approver_name VARCHAR(200) COMMENT ''Баталсан хүний нэр''', 'DO 0');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @c := (SELECT COUNT(*) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = @t AND COLUMN_NAME = 'city');
SET @ddl := IF(@c = 0, 'ALTER TABLE repair_document ADD COLUMN city VARCHAR(100) COMMENT ''Улаанбаатар хот''', 'DO 0');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

/* Комиссын гишүүд — [{title,name}] JSON. Бүрэлдэхүүн нь актаас актад
   өөрчлөгдөж болох тул тусдаа хүснэгт болгосонгүй. */
SET @c := (SELECT COUNT(*) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = @t AND COLUMN_NAME = 'commission');
SET @ddl := IF(@c = 0, 'ALTER TABLE repair_document ADD COLUMN commission TEXT COMMENT ''Комиссын гишүүд — JSON [{title,name}]''', 'DO 0');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

/* Зөвшөөрсөн жолооч — актад оролцсон */
SET @c := (SELECT COUNT(*) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = @t AND COLUMN_NAME = 'commission_driver');
SET @ddl := IF(@c = 0, 'ALTER TABLE repair_document ADD COLUMN commission_driver VARCHAR(200) COMMENT ''Зөвшөөрсөн жолооч''', 'DO 0');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

/* Гүйлтийн норм ба бодит гүйлт — дутууг эндээс тооцно */
SET @c := (SELECT COUNT(*) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = @t AND COLUMN_NAME = 'norm_km');
SET @ddl := IF(@c = 0, 'ALTER TABLE repair_document ADD COLUMN norm_km INT COMMENT ''Гүйлтийн норм (км)''', 'DO 0');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @c := (SELECT COUNT(*) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = @t AND COLUMN_NAME = 'actual_km');
SET @ddl := IF(@c = 0, 'ALTER TABLE repair_document ADD COLUMN actual_km INT COMMENT ''Ашиглалтад орсноос хойш явсан (км)''', 'DO 0');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

/* Комиссоос тогтоосон нь — 1, 2 дугаар зүйл */
SET @c := (SELECT COUNT(*) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = @t AND COLUMN_NAME = 'finding');
SET @ddl := IF(@c = 0, 'ALTER TABLE repair_document ADD COLUMN finding TEXT COMMENT ''Комиссоос тогтоосон нь — 1''', 'DO 0');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @c := (SELECT COUNT(*) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = @t AND COLUMN_NAME = 'liability');
SET @ddl := IF(@c = 0, 'ALTER TABLE repair_document ADD COLUMN liability TEXT COMMENT ''Эвдрэлийн шалтгаан, хариуцах эзэн, төлбөр — 2''', 'DO 0');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

/* Машины марк — актын толгой мөрөнд ордог */
SET @c := (SELECT COUNT(*) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = @t AND COLUMN_NAME = 'vehicle_brand');
SET @ddl := IF(@c = 0, 'ALTER TABLE repair_document ADD COLUMN vehicle_brand VARCHAR(150) COMMENT ''Маркийн нэр''', 'DO 0');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
