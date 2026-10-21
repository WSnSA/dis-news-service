-- ============================================================================
--  Шуурхай хурал — Нэгж (ЗБН) тус бүрийн гүйцэтгэлийн үнэлгээ
--  db_migration_briefing_v2.sql-ийн ДАРАА ажиллуулна.
--
--  Шаардлага: Үүрэг өгсөн удирдлага зохион байгуулалтын нэгж тус бүрийн хавсралтыг
--  нягтлаад гүйцэтгэлийн хувь (0-100) + дүгнэлтийг ЗААВАЛ бичиж өгнө.
--  Cycle-ийн ерөнхий дүн нь илгээгдсэн нэгжүүдийн оноонуудын дундаж болж тооцоологдоно.
--
--  Зөвхөн шинэ багана нэмнэ — одоо байгаа өгөгдлийг эвдэхгүй (бүгд NULL default).
-- ============================================================================

ALTER TABLE briefing_fulfillment ADD COLUMN score         INT NULL           AFTER returned_by;
ALTER TABLE briefing_fulfillment ADD COLUMN score_comment VARCHAR(2000) NULL AFTER score;
ALTER TABLE briefing_fulfillment ADD COLUMN scored_by     INT NULL           AFTER score_comment;
ALTER TABLE briefing_fulfillment ADD COLUMN scored_at     DATETIME NULL      AFTER scored_by;
