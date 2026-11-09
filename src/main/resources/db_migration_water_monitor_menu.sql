-- ============================================================================
--  "Ус хангамжийн хяналт" — өдрийн / цагийн цэсийг НЭГ цэс болгох (СОНГОЛТТОЙ)
--
--  Frontend-д хоёр цэс хоёулаа нэгдсэн хуудсыг (toggle-той) нээдэг тул энэ
--  скриптийг ажиллуулахгүй байсан ч ажиллана. Энэ нь зөвхөн sidebar-аас
--  давхардсан нэг цэсийг нууна.
--
--  1) Цагийн цэсийн эрхтэй боловч өдрийн цэсийн эрхгүй хэрэглэгчдэд эрхийг хуулна
--     (цагийн цэсийг нуухад эрх алдагдахгүйн тулд).
--  2) Өдрийн цэсийн нэрийг "Ус хангамжийн хяналт" болгоно.
--  3) Цагийн цэсийг идэвхгүй болгоно (active_flag = 0, устгахгүй — буцаахад амар).
--
--  ЭХЛЭЭД локал DB (127.0.0.1:3307) дээр туршина уу.
-- ============================================================================

SET @daily_id  := (SELECT id FROM menu WHERE component = 'pages/water-supply/water-supply.component' LIMIT 1);
SET @hourly_id := (SELECT id FROM menu WHERE component = 'pages/water-hourly/water-hourly.component' LIMIT 1);

SELECT @daily_id AS daily_menu_id, @hourly_id AS hourly_menu_id;   -- хоёулаа NULL биш эсэхийг шалгана

-- 1) Эрх хуулах
INSERT INTO permission (user_id, menu_id, can_view, can_edit)
SELECT p.user_id, @daily_id, p.can_view, p.can_edit
FROM permission p
WHERE p.menu_id = @hourly_id
  AND @daily_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM permission q WHERE q.user_id = p.user_id AND q.menu_id = @daily_id);

-- Аль хэдийн өдрийн эрхтэй хэрэглэгчийн can_view/can_edit-ийг ихийг нь авна
UPDATE permission d
JOIN permission h ON h.user_id = d.user_id AND h.menu_id = @hourly_id
SET d.can_view = GREATEST(COALESCE(d.can_view, 0), COALESCE(h.can_view, 0)),
    d.can_edit = GREATEST(COALESCE(d.can_edit, 0), COALESCE(h.can_edit, 0))
WHERE d.menu_id = @daily_id;

-- 2) Нэр солих
UPDATE menu SET name = 'Ус хангамжийн хяналт' WHERE id = @daily_id;

-- 3) Цагийн цэсийг нуух
UPDATE menu SET active_flag = 0 WHERE id = @hourly_id;

-- ── Буцаах (rollback) ──
-- UPDATE menu SET active_flag = 1 WHERE id = @hourly_id;
-- UPDATE menu SET name = 'Ус хангамж /өдрийн/' WHERE id = @daily_id;   -- хуучин нэрээ шалгаад тавина
