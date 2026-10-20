-- Засварын зарцуулалт — лавлахад байхгүй сэлбэгийг гараар бичиж бүртгэх
-- 2026-10-20
--
-- Гараар бичсэн мөрөнд repair_part_id NULL, нэр нь part_name-д хадгалагдана.
-- Үлдэгдэл хөтлөхгүй.
--
-- ДАХИН АЖИЛЛУУЛАХАД АЮУЛГҮЙ.

ALTER TABLE vehicle_repair_part
    MODIFY COLUMN repair_part_id BIGINT NULL COMMENT 'repair_part.id — гараар бичсэн мөрөнд NULL';
