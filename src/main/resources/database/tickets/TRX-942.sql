ALTER TABLE reason_code
   DROP INDEX `UNQ_REASON_CODE_1`,
   ADD UNIQUE KEY `UNQ_REASON_CODE_1` (`tenant_id`,`reason_code`, `reason_description`);