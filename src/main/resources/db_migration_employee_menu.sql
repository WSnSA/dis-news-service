-- "Ажилтан" нэгдсэн цэс — засварчин, жолооч, ажилтны түүх нэг хуудсанд
-- 2026-09-21
--
-- Зорилго: хүний нөөцийн хүнд ганц эрх өгөхөд бүх ажилтны жагсаалт харагдана.
-- Өмнө нь засварчин нь "Засварын бүртгэл" дотор таб байсан тул тэр хуудсыг
-- харах эрхгүй хүн ажилтны жагсаалтад хүрэх боломжгүй байв.
--
-- ДАХИН АЖИЛЛУУЛАХАД АЮУЛГҮЙ.

/* ── 1. driver хүснэгтэд soft delete ──
   Жолоочийг устгахад мөр бүрмөсөн устдаг байв. Хуваарилалтын түүхэд нэр нь
   үлдэх ёстой тул бусад лавлахтай ижил active_flag-тай болгоно. */
SET @c := (SELECT COUNT(*) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'driver'
             AND COLUMN_NAME = 'active_flag');
SET @ddl := IF(@c = 0,
    'ALTER TABLE driver ADD COLUMN active_flag TINYINT NOT NULL DEFAULT 1 COMMENT ''1=идэвхтэй, 0=устгасан''',
    'DO 0');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

/* ── 2. Цэсний мөр ──
   Автобааз бүлгийг "Машин бүртгэл" (autopark) мөрөөс нь тодорхойлно —
   бүлгийн id орчин бүрт өөр байж болно. */
SET @autopark_parent = (SELECT parent_id  FROM (SELECT parent_id  FROM menu WHERE component = 'pages/autopark/autopark.component' LIMIT 1) t);
SET @autopark_sort   = (SELECT sort_order FROM (SELECT sort_order FROM menu WHERE component = 'pages/autopark/autopark.component' LIMIT 1) t);
SET @already         = (SELECT COUNT(*)   FROM (SELECT id FROM menu WHERE component = 'pages/employee/employee.component') t);

-- ⚠ autopark цэс энэ орчинд байхгүй бол @autopark_parent NULL үлдэж, шинэ цэс
--   дээд түвшинд орно. Тэр тохиолдолд parent_id-г гараар засна:
--   UPDATE menu SET parent_id = <бүлгийн id>
--    WHERE component = 'pages/employee/employee.component';

UPDATE menu
   SET sort_order = sort_order + 1
 WHERE @already = 0
   AND parent_id <=> @autopark_parent
   AND sort_order > @autopark_sort;

INSERT INTO menu (parent_id, name, icon, path, component, active_flag, sort_order, created_date)
SELECT @autopark_parent, 'Ажилтан', 'pi-id-card', 'pages/employee',
       'pages/employee/employee.component', 1,
       COALESCE(@autopark_sort, 0) + 1, NOW()
  FROM DUAL
 WHERE @already = 0;

-- Цэс үүссэний дараа "Эрхийн тохиргоо"-оос ХН-ийн хүнд canView / canEdit эрхийг
-- гараар олгоно. Эрхгүй бол цэс сайдбарт харагдахгүй.

/* ── 3. Хуучин "Жолооч" цэсийг хаана ──
   Жолооч одоо "Ажилтан" хуудсанд байна. Хуучин хуудасны код frontend-ээс
   устсан тул энэ цэс үлдвэл дарахад хоосон хуудас гарна. */
UPDATE menu
   SET active_flag = 0
 WHERE component = 'pages/driver/driver.component';
