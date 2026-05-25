-- vehicle_type — active_flag багана нэмэх (soft delete)
-- 2026-05-25
-- Устгах оронд идэвхгүй болгоно. Мөрийг үлдээснээр машин/захиалгын хуучин
-- бичлэгүүд vehicle_type_id-аар нэрээ хадгална, foreign key зөрчигдөхгүй.

-- Багана аль хэдийн байвал алдаа гаргахгүй (idempotent).
ALTER TABLE vehicle_type
    ADD COLUMN IF NOT EXISTS active_flag INT NOT NULL DEFAULT 1 COMMENT '1=идэвхтэй, 0=идэвхгүй (soft delete)';

-- Хэрэв багана урьд нь default-гүй нэмэгдсэн бол NULL мөрүүдийг идэвхтэй болгоно.
UPDATE vehicle_type SET active_flag = 1 WHERE active_flag IS NULL;
