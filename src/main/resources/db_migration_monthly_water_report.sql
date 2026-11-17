-- ============================================================================
--  "Сарын мэдээ" — олборлосон болон татан зайлуулж цэвэрлэсэн усны сарын мэдээ
--  2026-11-09
--
--  ⚠  Backend deploy хийхийн ӨМНӨ ажиллуулна (ddl-auto=none).
--  ДАХИН АЖИЛЛУУЛАХАД АЮУЛГҮЙ (IF NOT EXISTS / NOT EXISTS / ON DUPLICATE KEY).
--  ЭХЛЭЭД локал DB (127.0.0.1:3307) дээр туршина уу.
--
--  1) monthly_report_row  — тайлангийн мөр бүр (эх үүсвэр / цэвэрлэх байгууламж)
--       ба тухайн мөрийн тоог аль цэс(станц)-аас авахыг тодорхойлно.
--       Мөр нэмэх, станц солих, дараалал өөрчлөхөд код өөрчлөхгүй — зөвхөн энэ хүснэгт.
--  2) monthly_report_plan — мөр × он × сарын төлөвлөгөө (м3). UI-аас оруулна.
--  3) dis_setting         — доод талын гарын үсгийн мөрүүд (monthly_report_sign_1..6)
--  4) menu                — "Сарын мэдээ" цэс
-- ============================================================================

CREATE TABLE IF NOT EXISTS monthly_report_row (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    section          VARCHAR(20)  NOT NULL COMMENT 'WATER = олборлосон ус, SEWAGE = татан зайлуулсан ус',
    group_no         INT          NOT NULL DEFAULT 1 COMMENT 'Бүлэг бүрийн дараа ДҮН мөр гарна',
    name             VARCHAR(150) NOT NULL,
    unit             VARCHAR(20)  NULL,
    -- WS_FM               : hourly_ws_station FM-1 + FM-7 + FM-8 тоолуурын зөрүү (ээлжийн 07:00 цонх)
    -- ST_RECEIVED_WASTE   : sewage_treatment.received_waste + шинэ байгууламжийн (st_facility_daily) receivedWaste
    -- ST_RECEIVED_WATER   : sewage_treatment.received_water
    -- ST_TREATED_WATER    : sewage_treatment.treated_water
    metric           VARCHAR(30)  NOT NULL,
    menu_ids         VARCHAR(255) NULL COMMENT 'Тоог нэгтгэх menu.id-ууд, таслалаар. Хоосон бол 0',
    include_in_total TINYINT      NOT NULL DEFAULT 1 COMMENT '0 бол НИЙТ мөрөнд орохгүй (ДҮН-д орно)',
    sort_order       INT          NOT NULL DEFAULT 0,
    active_flag      TINYINT      NOT NULL DEFAULT 1,
    KEY ix_mrr_section (section, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS monthly_report_plan (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    row_id       INT    NOT NULL,
    plan_year    INT    NOT NULL,
    plan_month   INT    NOT NULL,
    plan_value   BIGINT NULL,
    updated_by   INT    NULL,
    updated_date DATETIME NULL,
    UNIQUE KEY ux_mrp (row_id, plan_year, plan_month)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── Мөрүүд (хүснэгт хоосон үед л оруулна) ──────────────────────────────────
-- menu_ids-ийг component эсвэл path-аар нь хайж олно. Ажиллуулсны дараа доорх
-- "ШАЛГАХ" query-гээр menu_ids NULL болсон мөр байгаа эсэхийг заавал хараарай.
SET @seed := (SELECT COUNT(*) = 0 FROM monthly_report_row);

INSERT INTO monthly_report_row (section, group_no, name, unit, metric, menu_ids, include_in_total, sort_order)
SELECT * FROM (
    SELECT 'WATER' s, 1 g, 'Төвийн НС' n, 'м3' u, 'WS_FM' m,
           (SELECT GROUP_CONCAT(id) FROM menu WHERE (component IN ('ws/source/central') OR path IN ('ws/source/central', '/ws/source/central'))) ids, 1 t, 10 o
    UNION ALL SELECT 'WATER', 1, 'ДЭҮ-н НС', 'м3', 'WS_FM',
           (SELECT GROUP_CONCAT(id) FROM menu WHERE (component IN ('ws/source/deu') OR path IN ('ws/source/deu', '/ws/source/deu'))), 1, 20
    UNION ALL SELECT 'WATER', 1, 'Үйлдвэрийн НС', 'м3', 'WS_FM',
           (SELECT GROUP_CONCAT(id) FROM menu WHERE (component IN ('ws/source/factory') OR path IN ('ws/source/factory', '/ws/source/factory'))), 1, 30
    UNION ALL SELECT 'WATER', 1, 'Махын НС', 'м3', 'WS_FM',
           (SELECT GROUP_CONCAT(id) FROM menu WHERE (component IN ('ws/source/mah') OR path IN ('ws/source/mah', '/ws/source/mah'))), 1, 40
    UNION ALL SELECT 'WATER', 1, 'Яармаг НС', 'м3', 'WS_FM',
           (SELECT GROUP_CONCAT(id) FROM menu WHERE (component IN ('ws/source/yarmag') OR path IN ('ws/source/yarmag', '/ws/source/yarmag'))), 1, 50
    UNION ALL SELECT 'WATER', 1, 'Гачуурт', 'м3', 'WS_FM',
           (SELECT GROUP_CONCAT(id) FROM menu WHERE (component IN ('ws/source/gachuurt') OR path IN ('ws/source/gachuurt', '/ws/source/gachuurt'))), 1, 60
    UNION ALL SELECT 'WATER', 1, 'Буянт-Ухаа', 'м3', 'WS_FM',
           (SELECT GROUP_CONCAT(id) FROM menu WHERE (component IN ('ws/source/buyantukhaa') OR path IN ('ws/source/buyantukhaa', '/ws/source/buyantukhaa'))), 1, 70
    UNION ALL SELECT 'WATER', 1, 'Нисэхийн эх үүсвэр', 'м3', 'WS_FM',
           (SELECT GROUP_CONCAT(id) FROM menu WHERE (component IN ('ws/source/nisekh') OR path IN ('ws/source/nisekh', '/ws/source/nisekh'))), 1, 80
    UNION ALL SELECT 'WATER', 1, 'Баруун эх үүсвэр', 'м3', 'WS_FM',
           (SELECT GROUP_CONCAT(id) FROM menu WHERE (component IN ('ws/source/baruun') OR path IN ('ws/source/baruun', '/ws/source/baruun'))), 1, 90
    UNION ALL SELECT 'WATER', 2, 'Баянгол УСАТ', 'м3', 'WS_FM',
           (SELECT GROUP_CONCAT(id) FROM menu WHERE (component IN ('ws/js/bayangol') OR path IN ('ws/js/bayangol', '/ws/js/bayangol'))), 1, 110
    UNION ALL SELECT 'WATER', 2, 'Багахангай УСАТ', 'м3', 'WS_FM',
           (SELECT GROUP_CONCAT(id) FROM menu WHERE (component IN ('ws/js/bagahangai') OR path IN ('ws/js/bagahangai', '/ws/js/bagahangai'))), 1, 120

    UNION ALL SELECT 'SEWAGE', 1, 'Төв цэвэрлэх', NULL, 'ST_RECEIVED_WASTE',
           (SELECT GROUP_CONCAT(id) FROM menu WHERE (component IN ('st/central/shzns') OR path IN ('st/central/shzns', '/st/central/shzns'))), 1, 210
    UNION ALL SELECT 'SEWAGE', 1, 'Шинэ цэвэрлэх', NULL, 'ST_RECEIVED_WASTE',
           (SELECT GROUP_CONCAT(id) FROM menu WHERE (component IN ('st/wwtp/pump') OR path IN ('st/wwtp/pump', '/st/wwtp/pump'))), 1, 220
    UNION ALL SELECT 'SEWAGE', 1, 'Био', NULL, 'ST_RECEIVED_WASTE',
           (SELECT GROUP_CONCAT(id) FROM menu WHERE (component IN ('st/bio/sewage') OR path IN ('st/bio/sewage', '/st/bio/sewage'))), 1, 230
    UNION ALL SELECT 'SEWAGE', 1, 'Дамба', NULL, 'ST_RECEIVED_WASTE',
           (SELECT GROUP_CONCAT(id) FROM menu WHERE (component IN ('st/damba/sewage', 'st/damba/new') OR path IN ('st/damba/sewage', '/st/damba/sewage', 'st/damba/new', '/st/damba/new'))), 1, 240
    UNION ALL SELECT 'SEWAGE', 1, 'Баянгол', NULL, 'ST_RECEIVED_WASTE',
           (SELECT GROUP_CONCAT(id) FROM menu WHERE (component IN ('st/js/bayangol') OR path IN ('st/js/bayangol', '/st/js/bayangol'))), 1, 250
    UNION ALL SELECT 'SEWAGE', 1, 'Багахангай', NULL, 'ST_RECEIVED_WASTE',
           (SELECT GROUP_CONCAT(id) FROM menu WHERE (component IN ('st/js/bagahangai') OR path IN ('st/js/bagahangai', '/st/js/bagahangai'))), 1, 260
    -- Урьдчилан цэвэрлэсэн ус Төв цэвэрлэх рүү ордог тул НИЙТ-д давхар тооцохгүй (жишээ файлтай ижил)
    UNION ALL SELECT 'SEWAGE', 1, 'Урьдчилан цэвэрлэх', NULL, 'ST_RECEIVED_WASTE',
           (SELECT GROUP_CONCAT(id) FROM menu WHERE (component IN ('st/pre/chrome', 'st/pre/sulphite') OR path IN ('st/pre/chrome', '/st/pre/chrome', 'st/pre/sulphite', '/st/pre/sulphite'))), 0, 270
    UNION ALL SELECT 'SEWAGE', 1, 'Морингийн даваан цэвэрлэх', NULL, 'ST_RECEIVED_WASTE',
           (SELECT GROUP_CONCAT(id) FROM menu WHERE (component IN ('st/morin/sewage') OR path IN ('st/morin/sewage', '/st/morin/sewage'))), 1, 280
    UNION ALL SELECT 'SEWAGE', 1, 'Чингэлтэйн цэвэрлэх', NULL, 'ST_RECEIVED_WASTE',
           (SELECT GROUP_CONCAT(id) FROM menu WHERE (component IN ('st/chingeltei/sewage') OR path IN ('st/chingeltei/sewage', '/st/chingeltei/sewage'))), 1, 290
) seed
WHERE @seed = 1;

-- ШАЛГАХ: menu_ids хоосон мөр байвал тухайн станцын menu.id-г гараар тавина.
-- SELECT id, section, name, metric, menu_ids FROM monthly_report_row ORDER BY section, sort_order;
-- UPDATE monthly_report_row SET menu_ids = '4' WHERE name = 'Төвийн НС';

-- ── Гарын үсэг ("Албан тушаал|Нэр") ────────────────────────────────────────
INSERT INTO dis_setting (setting_key, setting_value, label, group_name, sort_order) VALUES
    ('monthly_report_sign_1', 'ХУТ-ийн Диспетчер инженер|',     'Сарын мэдээ — гарын үсэг 1', 'Сарын мэдээ', 101),
    ('monthly_report_sign_2', 'ХУТ-ийн дарга|Н.Батсайхан',      'Сарын мэдээ — гарын үсэг 2', 'Сарын мэдээ', 102),
    ('monthly_report_sign_3', 'УХА-ны дарга|П.Сүхбаатар',       'Сарын мэдээ — гарын үсэг 3', 'Сарын мэдээ', 103),
    ('monthly_report_sign_4', 'ЦБА-ны дарга|О.Оюунбаяр',        'Сарын мэдээ — гарын үсэг 4', 'Сарын мэдээ', 104),
    ('monthly_report_sign_5', 'ИБХ-ийн инженер|Р.Атаржаргал',   'Сарын мэдээ — гарын үсэг 5', 'Сарын мэдээ', 105),
    ('monthly_report_sign_6', 'ИБХ-ийн инженер|Д.Жавхлан',      'Сарын мэдээ — гарын үсэг 6', 'Сарын мэдээ', 106)
ON DUPLICATE KEY UPDATE setting_key = setting_key;

-- ── "Сарын мэдээ" цэс (Өдрийн тайлантай нэг эцэг цэсэнд) ───────────────────
INSERT INTO menu (parent_id, name, icon, path, component, active_flag, sort_order, created_date)
SELECT (SELECT r.parent_id FROM (SELECT * FROM menu) r WHERE r.component = 'pages/report/report.component' LIMIT 1),
       'Сарын мэдээ', 'pi pi-file-excel', 'monthly-water-report',
       'pages/monthly-water-report/monthly-water-report.component', 1,
       (SELECT COALESCE(MAX(m.sort_order), 0) + 1 FROM (SELECT * FROM menu) m), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM (SELECT * FROM menu) mm
    WHERE mm.component = 'pages/monthly-water-report/monthly-water-report.component'
);

-- Цэс үүссэний дараа "Эрх" (permission) UI-аас диспетчер, админд canView өгнө үү.
