-- vehicles_to_out — order_type багана нэмэх (Суудлын / Механизм ялгах)
-- 2026-05-20

ALTER TABLE vehicles_to_out
    ADD COLUMN order_type INT NULL COMMENT '0=механизм, 1=суудлын — vehicle_order.order_type-аас';

-- Хуучин мөрнүүдийг vehicle_order-аас backfill хийе
UPDATE vehicles_to_out v
JOIN vehicle_order o ON o.id = v.vehicle_order_id
SET v.order_type = o.order_type
WHERE v.order_type IS NULL AND o.order_type IS NOT NULL;
