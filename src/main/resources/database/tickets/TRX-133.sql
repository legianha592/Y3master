
CREATE TABLE `driver` (
	`id` BIGINT(11) NOT NULL AUTO_INCREMENT,
	`created_by` VARCHAR(255) NULL DEFAULT NULL COLLATE 'utf8_bin',
	`updated_by` VARCHAR(255) NULL DEFAULT NULL COLLATE 'utf8_bin',
	`created_date` TIMESTAMP NULL DEFAULT NULL,
	`updated_date` TIMESTAMP NULL DEFAULT NULL ON UPDATE current_timestamp(),
	`active_ind` TINYINT(1) NULL DEFAULT 1,
	`name` VARCHAR(255) NULL DEFAULT NULL COLLATE 'utf8_bin',
	`mobile_number` VARCHAR(255) NULL DEFAULT NULL COLLATE 'utf8_bin',
	`email` VARCHAR(255) NULL DEFAULT NULL COLLATE 'utf8_bin',
	`available` TINYINT(1) NULL DEFAULT NULL,
	`licence_type` VARCHAR(255) NULL DEFAULT NULL COLLATE 'utf8_bin',
	`licence_number` VARCHAR(255) NULL DEFAULT NULL COLLATE 'utf8_bin',
	`tenant_id` BIGINT(20) NULL DEFAULT NULL,
	PRIMARY KEY (`id`)
)
COLLATE='utf8_bin'
ENGINE=InnoDB
AUTO_INCREMENT=1
;