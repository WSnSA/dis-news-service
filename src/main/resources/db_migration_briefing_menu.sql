-- ============================================================================
--  Шуурхай хурлын модулийн цэс (menu) бүртгэл.
--  db_migration_briefing.sql-г АЖИЛЛУУЛСНЫ ДАРАА энийг ажиллуулна.
--
--  parent_id-г өөрийн цэсний бүтцэд тааруулж засна уу (NULL = дээд түвшний цэс).
--  Component key нь frontend app.component-map.ts-тэй яг таарна.
-- ============================================================================

INSERT INTO menu (parent_id, name, icon, path, component, active_flag, sort_order, created_date)
VALUES (NULL, 'Шуурхай хурал', 'pi pi-megaphone', 'briefing',
        'pages/briefing/briefing.component', 1,
        (SELECT COALESCE(MAX(m.sort_order), 0) + 1 FROM (SELECT * FROM menu) m), NOW());

-- Цэс үүссэний дараа эрхийг "Эрхийн тохиргоо" (permission) UI-аас холбогдох
-- хэрэглэгчдэд canView / canEdit өгнө. (Биелэлт оруулах албадад canEdit шаардлагатай.)
--
-- Үүрэг даалгавар өгөх / дүгнэх эрхтэй албан тушаалтнуудыг тэмдэглэх жишээ:
--   UPDATE users SET can_assign_task = 1 WHERE id IN ( /* дарга нар, ер.инженер, газрын дарга */ );
