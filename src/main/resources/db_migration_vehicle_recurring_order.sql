-- ============================================================================
--  Давтамжит (долоо хоног тутмын) машин/механизм захиалга
--
--  Загвар (vehicle_recurring_order) + түүний тээврийн төрлүүд
--  (vehicle_recurring_order_item). Долоо хоног бүрийн Баасан 15:00-д scheduler
--  ажиллаж, идэвхтэй загвар бүрээс ирэх 7 хоногийн (Даваа–Баасан) сонгосон
--  гаригуудад vehicle_order мөр (status=0) үүсгэнэ.
--
--  Эрхийг "Давтамжит захиалга" цэсний permission (menuId × canView/canEdit)-ээр олгоно.
-- ============================================================================

CREATE TABLE vehicle_recurring_order (
    id                     BIGINT AUTO_INCREMENT PRIMARY KEY,
    work_description       VARCHAR(1000),
    assigned_department_id INT,
    assigned_employee_id   INT,
    order_type             INT NOT NULL DEFAULT 0,      -- 0=Механизм, 1=Суудлын машин
    pickup_location        VARCHAR(200),
    dropoff_location       VARCHAR(200),
    passenger_count        INT,
    requested_time         VARCHAR(20),
    weekdays               VARCHAR(20) NOT NULL,        -- давтагдах гаригууд ISO дугаараар "1,2,3,4,5"
    active_flag            INT NOT NULL DEFAULT 1,
    created_by             INT,
    created_date           DATETIME,
    updated_by             INT,
    updated_date           DATETIME,
    KEY idx_vro_active (active_flag)
);

CREATE TABLE vehicle_recurring_order_item (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    recurring_id    BIGINT NOT NULL,
    vehicle_type_id INT,
    qty             INT,
    other_text      VARCHAR(500),
    KEY idx_vroi_recurring (recurring_id)
);

-- Автоматаар үүссэн захиалгыг загвартай нь холбож, давхардлаас сэргийлнэ
ALTER TABLE vehicle_order ADD COLUMN recurring_id BIGINT NULL AFTER active_flag;
ALTER TABLE vehicle_order ADD KEY idx_vo_recurring (recurring_id, order_date);

-- Цэс (component key нь frontend app.component-map.ts-тэй таарна).
-- parent_id-г цэсний бүтцэд тааруулж засаж болно (NULL = дээд түвшний цэс).
INSERT INTO menu (parent_id, name, icon, path, component, active_flag, sort_order, created_date)
VALUES (NULL, 'Давтамжит захиалга', 'pi pi-replay', 'vehicle-recurring',
        'pages/vehicle-recurring/vehicle-recurring.component', 1,
        (SELECT COALESCE(MAX(m.sort_order), 0) + 1 FROM (SELECT * FROM menu) m), NOW());

-- Цэс үүссэний дараа "Эрхийн тохиргоо" (permission) UI-аас зөвхөн тодорхой
-- хэрэглэгчдэд canView / canEdit эрхийг гараар олгоно.
