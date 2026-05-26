-- Цэсний харагдах дарааллыг удирдах sort_order багана нэмэх
-- Эхний утгыг id-ээр дүүргэнэ (одоогийн дараалал хадгалагдана)

ALTER TABLE menu ADD COLUMN sort_order INT NULL AFTER active_flag;

UPDATE menu SET sort_order = id WHERE sort_order IS NULL;
