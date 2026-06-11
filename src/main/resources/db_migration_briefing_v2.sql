-- ============================================================================
--  Шуурхай хурлын модуль — V2 өргөтгөл (Ажлын даалгаврын бүрэн шаардлага)
--  db_migration_briefing.sql-г АЖИЛЛУУЛСНЫ ДАРАА энийг ажиллуулна.
--
--  Энэ migration нь ОДОО БАЙГАА briefing_* хүснэгтүүдийг ЭВДЭХГҮЙ — зөвхөн
--  шинэ хүснэгт болон шинэ багана нэмнэ. Бүх ALTER нь default утгатай тул
--  одоо байгаа өгөгдөл хэвээр үлдэнэ.
--
--  Шинэ боломжууд:
--    §2  RBAC          → briefing_user_role (5 дүр), briefing_task_delegate
--    §3.1 Хурал        → briefing_meeting (хуралдааны дугаар + ерөнхий мэдээлэл)
--    §3.4 Шуурхайн мэдээ→ briefing_news + briefing_news_evidence, briefing_unit (seed)
--    §3.3 Дүгнэх       → briefing_cycle.score_comment, briefing_fulfillment.status/return_*
--    §3.2 Validation   → briefing_evidence.link_url / evidence_type (видео линк, cloud)
--    §7  Audit log     → briefing_audit_log (JSON)
-- ============================================================================

-- ────────────────────────────────────────────────────────────────────────────
--  §2 — RBAC: briefing_user_role (RECORDER_IDS хатуу кодыг орлоно)
--  5 дүр: BRIEFING_ADMIN / BRIEFING_SECRETARY / BRIEFING_UNIT /
--         BRIEFING_MANAGER / BRIEFING_VIEWER
-- ────────────────────────────────────────────────────────────────────────────
CREATE TABLE briefing_user_role (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    user_id      INT NOT NULL,
    role_key     VARCHAR(32) NOT NULL,        -- BRIEFING_ADMIN/SECRETARY/UNIT/MANAGER/VIEWER
    created_by   INT,
    created_date DATETIME,
    UNIQUE KEY uq_bur_user_role (user_id, role_key),
    KEY idx_bur_user (user_id),
    KEY idx_bur_role (role_key)
);

-- Газрын даргын өмнөөс дүгнэх эрх (хэлтсийн дарга / Ерөнхий инженер) — үүрэг тус бүрд
CREATE TABLE briefing_task_delegate (
    id      INT AUTO_INCREMENT PRIMARY KEY,
    task_id INT NOT NULL,                     -- briefing_task.id
    user_id INT NOT NULL,                     -- төлөөлөн дүгнэх хэрэглэгч (users.id)
    UNIQUE KEY uq_btdel (task_id, user_id),
    KEY idx_btdel_task (task_id)
);

-- ────────────────────────────────────────────────────────────────────────────
--  §3.1 — Шуурхай зөвлөгөөн (хуралдааны дугаар + ерөнхий мэдээлэл)
--  Нэг 7 хоногт нэг хурал (Мягмар). Үүрэг (briefing_cycle) тухайн хуралд холбогдоно.
-- ────────────────────────────────────────────────────────────────────────────
CREATE TABLE briefing_meeting (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    meeting_date DATE NOT NULL,               -- 7 хоногийн Мягмар
    meeting_no   VARCHAR(32) NOT NULL,        -- хуралдааны дугаар (ж: 2026-15)
    summary      VARCHAR(4000),               -- шуурхайн ерөнхий мэдээлэл
    created_by   INT,
    created_date DATETIME,
    active_flag  INT NOT NULL DEFAULT 1,
    UNIQUE KEY uq_bm_date (meeting_date),
    KEY idx_bm_no (meeting_no)
);

-- briefing_cycle-ийг хуралд холбоно + дүгнэх тайлбар нэмнэ
ALTER TABLE briefing_cycle ADD COLUMN meeting_id    INT NULL          AFTER task_id;
ALTER TABLE briefing_cycle ADD COLUMN score_comment VARCHAR(2000) NULL AFTER score;
ALTER TABLE briefing_cycle ADD KEY idx_bc_meeting (meeting_id);

-- ────────────────────────────────────────────────────────────────────────────
--  §3.4 — Зохион байгуулалтын нэгж/хэлтэс/удирдлага (танилцуулгын дараалал)
--  unit_type: UNIT (ЗБН) / DEPARTMENT (хэлтэс) / MANAGEMENT (удирдлага)
-- ────────────────────────────────────────────────────────────────────────────
CREATE TABLE briefing_unit (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    code          VARCHAR(32) NOT NULL,       -- ХУТ, УХА, ХХ ...
    name          VARCHAR(255),               -- бүтэн нэр (хожим бөглөж болно)
    unit_type     VARCHAR(20) NOT NULL,       -- UNIT / DEPARTMENT / MANAGEMENT
    department_id INT NULL,                   -- ref/department-тэй холбоос (сонголтоор)
    sort_order    INT NOT NULL DEFAULT 0,     -- танилцуулгын дараалал
    active_flag   INT NOT NULL DEFAULT 1,
    UNIQUE KEY uq_bunit_code (code),
    KEY idx_bunit_sort (sort_order)
);

-- Танилцуулгын дараалал (§3.4) — sort_order өсөхөөр жагсана
-- Зохион байгуулалтын нэгжүүд (UNIT) 1..13
INSERT INTO briefing_unit (code, unit_type, sort_order) VALUES
    ('ХУТ',    'UNIT', 1),
    ('УХА',    'UNIT', 2),
    ('ЦБА',    'UNIT', 3),
    ('ХУА',    'UNIT', 4),
    ('ЗУХА',   'UNIT', 5),
    ('ШУХА',   'UNIT', 6),
    ('ШАУЗА',  'UNIT', 7),
    ('УТЛ',    'UNIT', 8),
    ('АБ',     'UNIT', 9),
    ('УЦБ',    'UNIT', 10),
    ('НУА',    'UNIT', 11),
    ('БХУСАТ', 'UNIT', 12),
    ('УДБУА',  'UNIT', 13);
-- Хэлтсүүд (DEPARTMENT) 14..19
INSERT INTO briefing_unit (code, unit_type, sort_order) VALUES
    ('ХХ',       'DEPARTMENT', 14),
    ('ХЭЗХАБХ',  'DEPARTMENT', 15),
    ('ҮАТАХ',    'DEPARTMENT', 16),
    ('СБХ',      'DEPARTMENT', 17),
    ('ЗУХ',      'DEPARTMENT', 18),
    ('ИБТХ',     'DEPARTMENT', 19);
-- Удирдлага (MANAGEMENT) 20..21
INSERT INTO briefing_unit (code, name, unit_type, sort_order) VALUES
    ('ЕРИНЖ',   'Ерөнхий инженер', 'MANAGEMENT', 20),
    ('ГАЗДАР',  'Газрын дарга',    'MANAGEMENT', 21);

-- ────────────────────────────────────────────────────────────────────────────
--  §3.4 — Шуурхайн мэдээ (хурлаас өмнө нэгж бүр 7 хоногийн онцлох ажлаа оруулна)
--  Хатуу хугацаа: хурлаас өмнөх өдөр (Даваа) 16:00 (backend дээр шалгана)
-- ────────────────────────────────────────────────────────────────────────────
CREATE TABLE briefing_news (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    meeting_id     INT NOT NULL,              -- briefing_meeting.id
    unit_id        INT NOT NULL,              -- briefing_unit.id
    summary_text   VARCHAR(4000),             -- 7 хоногт хийсэн ажлын товч тайлбар
    result         VARCHAR(4000),             -- үр дүн
    extra_proposal VARCHAR(4000),             -- нэмэлт санал
    folder_id      VARCHAR(64) NOT NULL,      -- фото/файлын folder (generated UUID)
    status         TINYINT NOT NULL DEFAULT 0,-- 0=draft, 1=submitted
    submitted_at   DATETIME,
    created_by     INT,
    created_date   DATETIME,
    updated_at     DATETIME,
    UNIQUE KEY uq_bn_meeting_unit (meeting_id, unit_id),
    KEY idx_bn_meeting (meeting_id),
    KEY idx_bn_unit (unit_id),
    KEY idx_bn_folder (folder_id)
);

CREATE TABLE briefing_news_evidence (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    folder_id     VARCHAR(64) NOT NULL,       -- briefing_news.folder_id
    object_name   VARCHAR(255) NULL,          -- file service objectName (FILE төрөлд)
    link_url      VARCHAR(1000) NULL,         -- видео/cloud холбоос (LINK төрөлд)
    evidence_type VARCHAR(20) NOT NULL DEFAULT 'FILE',  -- FILE / LINK
    file_name     VARCHAR(255),
    content_type  VARCHAR(120),
    file_size     BIGINT,
    uploaded_by   INT,
    uploaded_at   DATETIME,
    KEY idx_bne_folder (folder_id)
);

-- ────────────────────────────────────────────────────────────────────────────
--  §3.2 — Биелэлтийн нотлох баримтад видео линк / Google Drive / Cloud холбоос
--  object_name-ийг nullable болгож, линк төрлийг нэмнэ.
-- ────────────────────────────────────────────────────────────────────────────
ALTER TABLE briefing_evidence MODIFY object_name VARCHAR(255) NULL;
ALTER TABLE briefing_evidence ADD COLUMN link_url      VARCHAR(1000) NULL                 AFTER object_name;
ALTER TABLE briefing_evidence ADD COLUMN evidence_type VARCHAR(20) NOT NULL DEFAULT 'FILE' AFTER link_url;

-- ────────────────────────────────────────────────────────────────────────────
--  §3.3 — Биелэлтийн төлөв + буцаах (return)
--  status: 0=ороогүй(draft), 1=илгээгдсэн(шалгаж байгаа), 2=буцаагдсан
-- ────────────────────────────────────────────────────────────────────────────
ALTER TABLE briefing_fulfillment ADD COLUMN status         TINYINT NOT NULL DEFAULT 0 AFTER work_text;
ALTER TABLE briefing_fulfillment ADD COLUMN return_comment VARCHAR(2000) NULL;
ALTER TABLE briefing_fulfillment ADD COLUMN returned_at    DATETIME NULL;
ALTER TABLE briefing_fulfillment ADD COLUMN returned_by    INT NULL;
-- Одоо байгаа илгээгдсэн биелэлтүүдийг "илгээгдсэн" төлөвт оруулна
UPDATE briefing_fulfillment SET status = 1 WHERE submitted_at IS NOT NULL;

-- ────────────────────────────────────────────────────────────────────────────
--  §7 — Audit log (хэн, хэзээ, ямар үйлдэл хийснийг бүртгэнэ)
--  old_value / new_value нь MySQL JSON багана.
-- ────────────────────────────────────────────────────────────────────────────
CREATE TABLE briefing_audit_log (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     INT,
    user_name   VARCHAR(255),
    action      VARCHAR(40) NOT NULL,         -- LOGIN/CREATE_TASK/UPDATE_TASK/DELETE_TASK/
                                              -- SUBMIT_FULFILLMENT/ADD_EVIDENCE/SCORE/RETURN/EXTEND/SUBMIT_NEWS ...
    entity_type VARCHAR(40),                  -- TASK/CYCLE/FULFILLMENT/EVIDENCE/NEWS/MEETING/ROLE
    entity_id   INT,
    old_value   JSON NULL,
    new_value   JSON NULL,
    ip          VARCHAR(64),
    created_at  DATETIME NOT NULL,
    KEY idx_bal_user (user_id),
    KEY idx_bal_entity (entity_type, entity_id),
    KEY idx_bal_created (created_at)
);

-- ────────────────────────────────────────────────────────────────────────────
--  Дүр оноох жишээ (системийн админ гар аргаар / админ UI-аар хийнэ):
--    INSERT INTO briefing_user_role (user_id, role_key, created_date)
--    VALUES (3, 'BRIEFING_SECRETARY', NOW()), (260, 'BRIEFING_SECRETARY', NOW());
--  Хуучин can_assign_task=1 хэрэглэгчдийг MANAGER дүрд шилжүүлэх (нэг удаа):
--    INSERT INTO briefing_user_role (user_id, role_key, created_date)
--    SELECT id, 'BRIEFING_MANAGER', NOW() FROM users WHERE can_assign_task = 1;
-- ────────────────────────────────────────────────────────────────────────────
