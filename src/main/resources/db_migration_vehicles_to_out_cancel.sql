-- Машин хуваарилалтыг НЭГ ӨДРӨӨР цуцлах боломж
-- 2026-09-17
--
-- vehicles_to_out нь (захиалга × машин) тутамд НЭГ мөртэй бөгөөд хугацааг нь
-- vehicle_order.start_date/end_date-аас авдаг. Тиймээс "09-02-ыг цуцлах" гэдгийг
-- хадгалах газар байхгүй байсан — уг хүснэгт тэр үл хамаарах өдрүүдийг бүртгэнэ.
--
-- Устгах  = бүх хугацааны хуваарилалт устна (vehicles_to_out мөр өөрөө устна)
-- Цуцлах  = зөвхөн тухайн нэг өдөр чөлөөлөгдөнө, бусад өдөр хэвээр

CREATE TABLE IF NOT EXISTS vehicles_to_out_cancel (
    id                 INT AUTO_INCREMENT PRIMARY KEY,
    vehicles_to_out_id INT          NOT NULL COMMENT 'vehicles_to_out.id',
    cancel_date        DATE         NOT NULL COMMENT 'Цуцлагдсан өдөр',
    reason             VARCHAR(500) NOT NULL COMMENT 'Цуцлах шалтгаан (заавал)',
    created_by         INT                   COMMENT 'Цуцалсан хэрэглэгч',
    created_date       DATETIME              COMMENT 'Цуцалсан огноо, цаг',
    UNIQUE KEY uq_vto_cancel (vehicles_to_out_id, cancel_date),
    KEY idx_vto_cancel_date (cancel_date)
) COMMENT 'Машин хуваарилалтын өдрийн цуцлалт';
