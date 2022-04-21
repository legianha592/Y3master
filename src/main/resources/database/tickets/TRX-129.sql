
CREATE TABLE `location` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`created_by` VARCHAR(255) NULL DEFAULT NULL COLLATE 'utf8_bin',
	`updated_by` VARCHAR(255) NULL DEFAULT NULL COLLATE 'utf8_bin',
	`created_date` TIMESTAMP NULL DEFAULT NULL,
	`updated_date` TIMESTAMP NULL DEFAULT NULL ON UPDATE current_timestamp(),
	`active_ind` TINYINT(1) NULL DEFAULT 1,
	`loc_code` VARCHAR(255) NULL DEFAULT NULL COLLATE 'utf8_bin',
	`loc_desc` VARCHAR(255) NULL DEFAULT NULL COLLATE 'utf8_bin',
	`loc_contact_name` VARCHAR(255) NULL DEFAULT NULL COLLATE 'utf8_bin',
	`loc_contact_phone` VARCHAR(255) NULL DEFAULT NULL COLLATE 'utf8_bin',
	`loc_contact_email` VARCHAR(255) NULL DEFAULT NULL COLLATE 'utf8_bin',
	`multi_partner_addresses` TEXT NULL DEFAULT NULL COLLATE 'utf8_bin',
	`tenant_id` BIGINT(20) NULL DEFAULT NULL,
	PRIMARY KEY (`id`)
)
COLLATE='utf8_bin'
ENGINE=InnoDB
AUTO_INCREMENT=1;