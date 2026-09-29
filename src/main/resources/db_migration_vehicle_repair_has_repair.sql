-- Засварын бүртгэл — "Засвартай/Засваргүй бүртгэгдсэн" тэмдэглэл (Цэвэр ус жолоочийн лог)
-- 2026-09-29
--
-- Цэвэр усны машин бүртгэх үед бодит засвар болоогүй ч жолоочийн ажилласан
-- хоногийг лог хийхийн тулд бичлэг үүсгэдэг тохиолдол гарна. Энэ баганаар
-- тухайн бичлэг бодит засвартай эсэхийг ялгана. 1=засвартай, 0=засваргүй.
-- Хуучин бичлэгүүд бүгд бодит засвар байсан тул анхны утга нь 1.
--
-- ДАХИН АЖИЛЛУУЛАХАД АЮУЛГҮЙ.

SET @c := (SELECT COUNT(*) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE()
             AND TABLE_NAME   = 'vehicle_repair'
             AND COLUMN_NAME  = 'has_repair');
SET @ddl := IF(@c = 0,
    'ALTER TABLE vehicle_repair ADD COLUMN has_repair TINYINT NOT NULL DEFAULT 1 COMMENT ''1=засвартай бүртгэгдсэн, 0=засваргүй бүртгэгдсэн''',
    'DO 0');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
