-- work_order дээр газрын зургийн координат талбар нэмэх
ALTER TABLE work_order
    ADD COLUMN lat DOUBLE NULL AFTER fulfillment,
    ADD COLUMN lng DOUBLE NULL AFTER lat;
