-- Суудлын машин захиалга — албаны баталгаажуулалт нэмэх
-- 2026-05-20

ALTER TABLE vehicle_order
    ADD COLUMN dept_approved TINYINT(1) DEFAULT NULL COMMENT 'Албаны баталгаажуулалт (1=баталгаажсан, 0=хүлээгдэж буй)',
    ADD COLUMN dept_approved_by INT DEFAULT NULL COMMENT 'Албаны баталгаажуулсан хэрэглэгчийн id';

-- Шинэ цонхны менюг автомат нэмэх (хэрэв байхгүй бол)
-- Албаны баталгаажуулагч эрхтэй хэрэглэгчдэд permission өгнө үү
INSERT INTO menu (parent_id, name, path, icon, component, active_flag, sort_order)
SELECT
    (SELECT id FROM (SELECT id FROM menu WHERE name = 'Машин захиалга' LIMIT 1) m),
    'Суудлын машин баталгаажуулалт',
    'car-order-confirm',
    'pi pi-verified',
    'car-order-confirm',
    1,
    0
WHERE NOT EXISTS (SELECT 1 FROM menu WHERE component = 'car-order-confirm');

-- Хуучин захиалгуудыг баталгаажуулсан гэж тэмдэглэе (миграцын момент)
UPDATE vehicle_order
SET dept_approved = 1
WHERE dept_approved IS NULL;
