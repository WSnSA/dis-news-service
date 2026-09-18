-- Засварын ангилал — захиалгад саад болох эсэх + Тос тосолгоо ангилал
-- 2026-09-18
--
-- Одоогоор засварт байгаа БҮХ машин захиалгад сонгогдох боломжгүй байсан.
-- Гэвч тос тосолгоо шиг богино үйлчилгээний үед машин ажилд гарч чадна.
-- blocks_dispatch = 0 бол тухайн ангилалд байгаа машин захиалгад хэвээр гарна.

ALTER TABLE repair_category
    ADD COLUMN blocks_dispatch TINYINT NOT NULL DEFAULT 1
        COMMENT '1=засварт байхад захиалгад сонгогдохгүй, 0=сонгогдож болно';

-- Тос тосолгоо — машиныг захиалгаас хасахгүй
INSERT INTO repair_category (name, code, description, sort_order, blocks_dispatch, active_flag, created_at)
SELECT 'Тос тосолгоо', 'LUBRICATION',
       'Тос, шингэн солих үйлчилгээ. Машин захиалгад хэвээр гарна.',
       COALESCE((SELECT MAX(sort_order) FROM (SELECT sort_order FROM repair_category) t), 0) + 1,
       0, 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM repair_category WHERE code = 'LUBRICATION');
