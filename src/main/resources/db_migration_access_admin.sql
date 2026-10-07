-- ============================================================================
--  "Эрхийн удирдлага" цэс — тусгай хэрэглэгчийн эрхийн нэгдсэн удирдлага
--  2026-10-07
--
--  ⚠  Backend deploy хийхийн ӨМНӨ ажиллуулна (ddl-auto=none).
--     vehicle_approval_skip хүснэгт байхгүй бол машин захиалгын баталгаажуулалт
--     алгасах логик хоосон ажиллана (код defensive — захиалга эвдрэхгүй), гэхдээ
--     одоогийн зан төлөв (user 258 алгасдаг) хадгалагдахын тулд энэ migration-ийг
--     ажиллуулах шаардлагатай.
--
--  ДАХИН АЖИЛЛУУЛАХАД АЮУЛГҮЙ (IF NOT EXISTS / ON DUPLICATE KEY / NOT EXISTS).
-- ============================================================================

-- Машин захиалгын албаны баталгаажуулалт алгасах хэрэглэгчид
-- (өмнө нь application.properties: vehicle-order.dept-approval-skip.user-ids=258)
CREATE TABLE IF NOT EXISTS vehicle_approval_skip (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    user_id      INT NOT NULL,
    created_by   INT,
    created_date DATETIME,
    UNIQUE KEY ux_vehicle_approval_skip_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Одоогийн зан төлвийг хадгалах seed (config дахь 258)
INSERT INTO vehicle_approval_skip (user_id, created_date)
VALUES (258, NOW())
ON DUPLICATE KEY UPDATE user_id = user_id;

-- "Эрхийн удирдлага" цэс (байхгүй бол)
INSERT INTO menu (parent_id, name, icon, path, component, active_flag, sort_order, created_date)
SELECT NULL, 'Эрхийн удирдлага', 'pi pi-shield', 'access-admin',
       'pages/access-admin/access-admin.component', 1,
       (SELECT COALESCE(MAX(m.sort_order), 0) + 1 FROM (SELECT * FROM menu) m), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM (SELECT * FROM menu) mm
    WHERE mm.component = 'pages/access-admin/access-admin.component'
);

-- Цэс үүссэний дараа "Эрх" (permission) UI-аас админд canView/canEdit өгнө үү.
