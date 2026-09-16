-- ============================================================================
--  Цэвэрлэх байгууламж (шинэ, WWTP) — цаг тус бүрийн бүртгэл идэвхжүүлэх
--  st_facility_daily хүснэгтэд record_hour багана нэмнэ.
--
--  ⚠️  ЭНЭ SQL-ийг backend deploy хийхээс ӨМНӨ (эсвэл зэрэг) ажиллуулна.
--      ddl-auto=none тул багана байхгүй бол st-facility get/save болон
--      sewage-treatment/summary нэгтгэл алдаа өгнө.
--
--  Багана NULL зөвшөөрнө — хуучин (өдрөөр) бүртгэлүүд NULL хэвээр үлдэж,
--  цаг тус бүрийн шинэ бүртгэлүүд 0–23 утгатай болно. Тэдгээр давхцахгүй.
-- ============================================================================

ALTER TABLE st_facility_daily
    ADD COLUMN record_hour INT NULL COMMENT 'Бүртгэлийн цаг (0-23); хуучин өдрийн бүртгэл NULL' AFTER record_date;

-- Нэг станц + өдөр + цаг дээр давхардал үүсэхээс сэргийлэх (сонголт).
-- record_hour NULL-ууд MySQL-д давхцсан гэж тооцогдохгүй тул хуучин мөрүүдэд нөлөөлөхгүй.
-- CREATE UNIQUE INDEX ux_st_facility_station_date_hour
--     ON st_facility_daily (station_id, record_date, record_hour, active_flag);
