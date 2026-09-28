-- ============================================================================
--  DIS цагийн хязгааруудын тохиргоо (dis_setting) + "Цагийн тохиргоо" цэс
--  2026-09-28
--
--  ⚠  Backend deploy хийхийн ӨМНӨ ажиллуулна (ddl-auto=none).
--  ДАХИН АЖИЛЛУУЛАХАД АЮУЛГҮЙ (IF NOT EXISTS / ON DUPLICATE KEY / NOT EXISTS).
--
--  Seed утгууд одоогийн код доторх анхдагчтай ижил тул зан төлөв өөрчлөгдөхгүй.
-- ============================================================================

CREATE TABLE IF NOT EXISTS dis_setting (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    setting_key   VARCHAR(80)  NOT NULL,
    setting_value VARCHAR(255),
    label         VARCHAR(255),
    group_name    VARCHAR(120),
    sort_order    INT DEFAULT 0,
    updated_by    INT,
    updated_date  DATETIME,
    UNIQUE KEY ux_dis_setting_key (setting_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO dis_setting (setting_key, setting_value, label, group_name, sort_order) VALUES
    ('order_cutoff_hour',             '17', 'Машин захиалгын эцсийн цаг — энэ цагаас хойш захиалга өгөх/засах/устгах боломжгүй', 'Машин захиалга', 1),
    ('briefing_news_deadline_hour',   '17', 'Шуурхайн мэдээ оруулах эцсийн цаг (хурлын өмнөх өдөр — Даваа)',                    'Шуурхай хурал',   2),
    ('briefing_submit_deadline_hour', '16', 'Биелэлт оруулах анхдагч эцсийн цаг (Баасан гараг)',                                'Шуурхай хурал',   3),
    ('briefing_score_deadline_hour',  '14', 'Хянан дүгнэх эцсийн цаг (дараа долоо хоногийн Даваа)',                             'Шуурхай хурал',   4)
ON DUPLICATE KEY UPDATE setting_key = setting_key;

-- "Цагийн тохиргоо" цэс (байхгүй бол)
INSERT INTO menu (parent_id, name, icon, path, component, active_flag, sort_order, created_date)
SELECT NULL, 'Цагийн тохиргоо', 'pi pi-clock', 'settings-time',
       'pages/settings-time/settings-time.component', 1,
       (SELECT COALESCE(MAX(m.sort_order), 0) + 1 FROM (SELECT * FROM menu) m), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM (SELECT * FROM menu) mm
    WHERE mm.component = 'pages/settings-time/settings-time.component'
);

-- Цэс үүссэний дараа "Эрх" (permission) UI-аас админд canView/canEdit өгнө үү.
