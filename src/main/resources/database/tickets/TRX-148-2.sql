ALTER TABLE milkrun_vehicle ADD COLUMN driver_id bigint;
ALTER TABLE milkrun_trip ADD COLUMN TPT_request_activity varchar(128);

ALTER TABLE milkrun_vehicle DROP COLUMN day_of_week;
ALTER TABLE milkrun_trip ADD COLUMN day_of_week varchar(128);


