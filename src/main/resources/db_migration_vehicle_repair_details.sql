-- Засварын бүртгэл — дэлгэрэнгүй талбарууд
-- 2026-09-18
--
-- Их засвар / урсгал засвар / тос тосолгоо бүрт хэрэгтэй мэдээллийг нэмнэ:
-- гүйлтийн заалт, хариуцсан засварчин, байршил, гэмтлийн шалтгаан.
-- Бүгд NULL зөвшөөрнө — хуучин бичлэгүүд хэвээр ажиллана.

ALTER TABLE vehicle_repair
    ADD COLUMN odometer_km          INT           COMMENT 'Засварт орох үеийн гүйлт (км)',
    ADD COLUMN next_service_km      INT           COMMENT 'Дараагийн ТҮ хийх гүйлт (км) — тос тосолгоонд чухал',
    ADD COLUMN responsible_worker_id BIGINT       COMMENT 'repair_worker.id — засварыг хариуцсан хүн',
    ADD COLUMN location             VARCHAR(200)  COMMENT 'Байршил — гараж, талбай, гадны сервис',
    ADD COLUMN external_org         VARCHAR(200)  COMMENT 'Гадны байгууллагаар хийлгэсэн бол нэр',
    ADD COLUMN fault_description    TEXT          COMMENT 'Гэмтлийн шалтгаан / хийгдэх ажлын тайлбар',
    ADD COLUMN driver_name          VARCHAR(150)  COMMENT 'Хүлээлгэн өгсөн жолооч';

CREATE INDEX idx_vehicle_repair_worker ON vehicle_repair (responsible_worker_id);
