-- ============================================================================
--  Засварын бүртгэл — "Ангилал" таб (repair_category)
--  2026-09-17
--
--  Автобааз цэсэнд "Машин бүртгэл"-ийн доор "Засварын бүртгэл" хуудас нэмнэ.
--  Component key нь frontend app.component-map.ts-тэй яг таарна.
--  Скрипт idempotent — дахин ажиллуулахад мөр давхардахгүй.
-- ============================================================================

CREATE TABLE IF NOT EXISTS repair_category (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(150) NOT NULL,
    code        VARCHAR(30)  NULL COMMENT 'Системийн түлхүүр. Үндсэн 4 ангилалд л утгатай',
    description TEXT,
    sort_order  INT,
    active_flag INT NOT NULL DEFAULT 1 COMMENT '1=идэвхтэй, 0=идэвхгүй (soft delete)',
    created_at  DATETIME,
    created_by  INT,
    updated_at  DATETIME,
    updated_by  INT,
    UNIQUE KEY uq_repair_category_code (code),
    INDEX idx_repair_category_active (active_flag)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- ── Үндсэн 4 ангилал ────────────────────────────────────────────────────────
-- code нь UNIQUE тул INSERT IGNORE дахин ажиллуулахад алгасна.
-- UI-аас нэмсэн ангилал код авахгүй (NULL) — MySQL-д NULL нь UNIQUE-д давхцахгүй.
INSERT IGNORE INTO repair_category (name, code, description, sort_order, active_flag, created_at) VALUES
    ('Их засвар',            'MAJOR',   'Эд ангийн иж бүрэн задаргаа шаардсан урт хугацааны засвар', 1, 1, NOW()),
    ('Урсгал засвар',        'CURRENT', 'Богино хугацааны тогтмол засвар',                            2, 1, NOW()),
    ('Техникийн үйлчилгээ',  'TECH',    'Төлөвлөгөөт техникийн үзлэг, тохиргоо',                      3, 1, NOW()),
    ('Сервис үйлчилгээ',     'SERVICE', 'Тосны солилт зэрэг ээлжит сервис',                           4, 1, NOW());

-- ── Цэсний мөр ──────────────────────────────────────────────────────────────
-- path нь sidebar-ийн routerLink болно (app.menu.ts normalizePath → '/' + path),
-- бусад хуудастай ижилхэн 'pages/...' хэлбэртэй. icon нь 'pi pi-fw' дээр нэмэгддэг
-- тул зөвхөн icon-ы нэрийг хадгална.
-- Автобааз бүлгийг "Машин бүртгэл" (autopark) мөрөөс нь тодорхойлно —
-- ингэснээр бүлгийн нэр/id орчин бүрт өөр байсан ч зөв байрандаа орно.
SET @autopark_parent = (SELECT parent_id  FROM (SELECT parent_id  FROM menu WHERE component = 'pages/autopark/autopark.component' LIMIT 1) t);
SET @autopark_sort   = (SELECT sort_order FROM (SELECT sort_order FROM menu WHERE component = 'pages/autopark/autopark.component' LIMIT 1) t);
SET @already         = (SELECT COUNT(*)   FROM (SELECT id FROM menu WHERE component = 'pages/repair-registration/repair-registration.component') t);

-- ⚠ Хэрэв autopark цэс энэ орчинд байхгүй бол @autopark_parent NULL үлдэж, шинэ цэс
--   дээд түвшинд орно. Тэр тохиолдолд INSERT-ийн дараа parent_id-г гараар засна:
--   UPDATE menu SET parent_id = <Автобааз бүлгийн id>
--    WHERE component = 'pages/repair-registration/repair-registration.component';

-- "Машин бүртгэл"-ийн доор зай гаргахын тулд араас нь орох мөрүүдийг нэгээр шахна.
UPDATE menu
   SET sort_order = sort_order + 1
 WHERE @already = 0
   AND parent_id <=> @autopark_parent
   AND sort_order > @autopark_sort;

INSERT INTO menu (parent_id, name, icon, path, component, active_flag, sort_order, created_date)
SELECT @autopark_parent, 'Засварын бүртгэл', 'pi-wrench', 'pages/repair-registration',
       'pages/repair-registration/repair-registration.component', 1,
       COALESCE(@autopark_sort, 0) + 1, NOW()
  FROM DUAL
 WHERE @already = 0;

-- Цэс үүссэний дараа "Эрхийн тохиргоо" (permission) UI-аас холбогдох хэрэглэгчдэд
-- canView / canEdit эрхийг гараар олгоно. Эрхгүй бол цэс сайдбарт харагдахгүй.
