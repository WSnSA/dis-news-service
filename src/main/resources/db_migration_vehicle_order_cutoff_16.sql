-- Run ONCE against the shared dis_news database before deploying both backends.
-- If dis_setting does not exist, run db_migration_dis_setting.sql first.
-- This intentionally resets the shared car/mechanism cutoff to 16:00.
-- Later changes through DIS "Цагийн тохиргоо" apply to both websites.
INSERT INTO dis_setting (setting_key, setting_value, label, group_name, sort_order, updated_date)
VALUES ('order_cutoff_hour', '16',
        'Машин захиалгын эцсийн цаг — ERP болон DIS', 'Машин захиалга', 1, NOW())
ON DUPLICATE KEY UPDATE setting_value = '16', updated_date = NOW();
