-- ================================================================
-- Машин хуваарилалт: Боломжгүй + Машины чиглэл
-- Run once on dis_news database
-- ================================================================

-- 1. vehicle_order: decline_reason багана нэмэх
ALTER TABLE vehicle_order
    ADD COLUMN decline_reason TEXT NULL
    COMMENT 'status=3 (Боломжгүй) үед автобаазаас ирсэн шалтгаан'
    AFTER status;

-- 2. vehicle: service_type багана нэмэх
--    1=Үйлчилгээ  2=Цэвэр ус  3=Бохир ус
ALTER TABLE vehicle
    ADD COLUMN service_type TINYINT NULL
    COMMENT '1=Үйлчилгээ, 2=Цэвэр ус, 3=Бохир ус'
    AFTER note;
