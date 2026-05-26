-- ============================================================================
--  Шуурхай хурлын үүрэг даалгаврын модуль (BRIEFING)
--  Энэ нь хуучин tasks / task_departments-тэй ОГТ хольдоггүй, бүрэн шинэ бүтэц.
--
--  Урсгал:
--    7 хоногийн 2 дахь өдөр (Мягмар) — шуурхай хурлаар үүрэг даалгавар үүснэ.
--    Баасан 16:00 хүртэл — алба бүр биелэлт + нотлох баримт оруулна.
--    Дараа 7 хоногийн Даваа 14:00 хүртэл — үүрэг өгсөн албан тушаалтан 0-100 дүгнэнэ.
--    Дараа Мягмар — биелээгүй бол сунгана (шинэ cycle), түүх хадгалагдана.
-- ============================================================================

-- 1) Үүрэг даалгавар өгөх / дүгнэх эрхтэй албан тушаалтан эсэх
ALTER TABLE users ADD COLUMN can_assign_task TINYINT(1) NOT NULL DEFAULT 0;

-- 2) Үүрэг даалгавар (cycle-ууд дамжин нэг мөрөнд хадгалагдана)
CREATE TABLE briefing_task (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    assigner_id  INT NOT NULL,                  -- үүрэг өгсөн ба дүгнэх албан тушаалтан (users.id, can_assign_task=1)
    description  VARCHAR(2000) NOT NULL,        -- үүрэг даалгаврын тайлбар
    status       TINYINT NOT NULL DEFAULT 0,    -- 0=идэвхтэй, 1=бүрэн биелсэн (дүн=100)
    created_by   INT,
    created_date DATETIME,
    updated_by   INT,
    updated_date DATETIME,
    active_flag  INT NOT NULL DEFAULT 1,        -- soft delete
    KEY idx_bt_assigner (assigner_id),
    KEY idx_bt_active (active_flag)
);

-- 3) Холбогдох албад (1-с доошгүй, тухайн үүрэгт тогтмол)
CREATE TABLE briefing_task_department (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    task_id       INT NOT NULL,
    department_id INT NOT NULL,
    KEY idx_btd_task (task_id),
    KEY idx_btd_dep (department_id)
);

-- 4) 7 хоног тутмын мөчлөг (cycle). Сунгах бүрд шинэ мөр нэмэгдэнэ.
CREATE TABLE briefing_cycle (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    task_id         INT NOT NULL,
    cycle_no        INT NOT NULL DEFAULT 1,     -- 1, 2, 3 ... сунгалтын дугаар
    meeting_date    DATE NOT NULL,              -- тухайн 7 хоногийн 2 дахь өдөр (Мягмар)
    submit_deadline DATETIME NOT NULL,          -- биелэлт оруулах эцсийн хугацаа (Баасан 16:00)
    score_deadline  DATETIME NOT NULL,          -- дүгнэх эцсийн хугацаа (дараа Даваа 14:00)
    score           INT NULL,                   -- ерөнхий дүн 0-100 (NULL=дүгнээгүй)
    scored_by       INT NULL,
    scored_at       DATETIME NULL,
    status          TINYINT NOT NULL DEFAULT 0, -- 0=нээлттэй, 1=дүгнэгдсэн
    created_date    DATETIME,
    KEY idx_bc_task (task_id)
);

-- 5) Биелэлт — cycle × алба бүрд нэг мөр (хоосон ч эхэндээ үүснэ)
CREATE TABLE briefing_fulfillment (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    cycle_id      INT NOT NULL,
    department_id INT NOT NULL,
    work_text     VARCHAR(2000),               -- хийгдсэн ажлын тайлбар
    folder_id     VARCHAR(64) NOT NULL,        -- нотлох баримтын folder (generated UUID)
    submitted_by  INT,
    submitted_at  DATETIME,
    updated_at    DATETIME,
    UNIQUE KEY uq_bf_cycle_dep (cycle_id, department_id),
    KEY idx_bf_folder (folder_id)
);

-- 6) Нотлох баримт — folder доторх файлууд (file service-н objectName-р хадгална)
CREATE TABLE briefing_evidence (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    folder_id    VARCHAR(64) NOT NULL,         -- briefing_fulfillment.folder_id
    object_name  VARCHAR(255) NOT NULL,        -- file service upload-аас буцсан objectName / UUID
    file_name    VARCHAR(255),                 -- эх файлын нэр
    content_type VARCHAR(120),
    file_size    BIGINT,
    uploaded_by  INT,
    uploaded_at  DATETIME,
    KEY idx_be_folder (folder_id)
);
