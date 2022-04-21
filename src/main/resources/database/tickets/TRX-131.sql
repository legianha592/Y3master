CREATE TABLE `vehicle` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`created_by` VARCHAR(255) NULL DEFAULT NULL COLLATE 'utf8_bin',
	`updated_by` VARCHAR(255) NULL DEFAULT NULL COLLATE 'utf8_bin',
	`created_date` TIMESTAMP NULL DEFAULT NULL,
	`updated_date` TIMESTAMP NULL DEFAULT NULL ON UPDATE current_timestamp(),
	`active_ind` TINYINT(1) NULL DEFAULT 1,
	`vehicle_reg_number` VARCHAR(255) NOT NULL COLLATE 'utf8_bin',
	`vehicle_type` VARCHAR(255) NULL DEFAULT NULL COLLATE 'utf8_bin',
	`asset_default_loc_id` BIGINT(20) NULL DEFAULT NULL,
	`available` TINYINT(1) NULL DEFAULT NULL,
	`licence_type_required` VARCHAR(255) NULL DEFAULT NULL COLLATE 'utf8_bin',
	`tenant_id` BIGINT(20) NULL DEFAULT NULL,
	PRIMARY KEY (`id`),
	INDEX `FK_vehicle_location` (`asset_default_loc_id`),
	CONSTRAINT `FK_vehicle_location` FOREIGN KEY (`asset_default_loc_id`) REFERENCES `location` (`id`)
)
COLLATE='utf8_bin'
ENGINE=InnoDB
AUTO_INCREMENT=3
;