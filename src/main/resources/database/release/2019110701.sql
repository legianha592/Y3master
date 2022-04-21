CREATE TABLE `config_code` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT,
  `code` varchar(255) DEFAULT '',
  `description` varchar(255) DEFAULT '',
  `active_ind` tinyint(1) DEFAULT 0,
  `created_by` varchar(255) DEFAULT '',
  `created_date` timestamp NULL DEFAULT NULL ON UPDATE current_timestamp(),
  `updated_by` varchar(255) DEFAULT '',
  `updated_date` timestamp NULL DEFAULT NULL ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `CODE` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;


CREATE TABLE `partner_types` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(255) DEFAULT '',
  `description` varchar(255) DEFAULT '',
  `active_ind` tinyint(1) DEFAULT 0,
  `created_by` varchar(255) DEFAULT '',
  `created_date` timestamp NULL DEFAULT NULL ON UPDATE current_timestamp(),
  `updated_by` varchar(255) DEFAULT '',
  `updated_date` timestamp NULL DEFAULT NULL ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `CODE` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;


CREATE TABLE `partners` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL,
  `partner_code` varchar(255) NOT NULL DEFAULT '',
  `partner_name` varchar(255) NOT NULL,
  `partner_types_id` bigint(20) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `active_ind` tinyint(1) DEFAULT 0,
  `created_by` varchar(255) DEFAULT NULL,
  `created_date` timestamp NULL DEFAULT NULL ON UPDATE current_timestamp(),
  `updated_by` varchar(255) DEFAULT NULL,
  `updated_date` timestamp NULL DEFAULT NULL ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `TENANT_ID` (`tenant_id`,`partner_code`),
  KEY `PARTNER_TYPES_ID` (`partner_types_id`),
  CONSTRAINT `partners_ibfk_1` FOREIGN KEY (`partner_types_id`) REFERENCES `partner_types` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=35 DEFAULT CHARSET=utf8;


CREATE TABLE `partner_config` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT,
  `value` varchar(255) DEFAULT '',
  `description` varchar(255) DEFAULT '',
  `config_code_id` bigint(20) DEFAULT NULL,
  `partner_id` bigint(20) DEFAULT NULL,
  `active_ind` tinyint(1) DEFAULT 0,
  `created_by` varchar(255) DEFAULT '',
  `created_date` timestamp NULL DEFAULT NULL ON UPDATE current_timestamp(),
  `updated_by` varchar(255) DEFAULT '',
  `updated_date` timestamp NULL DEFAULT NULL ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `CONFIG_CODE_ID` (`config_code_id`),
  KEY `PARTNER_ID` (`partner_id`),
  CONSTRAINT `partner_config_ibfk_1` FOREIGN KEY (`config_code_id`) REFERENCES `config_code` (`id`),
  CONSTRAINT `partner_config_ibfk_2` FOREIGN KEY (`partner_id`) REFERENCES `partners` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8;


CREATE TABLE `location` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`created_by` VARCHAR(255) NULL DEFAULT NULL COLLATE 'utf8_bin',
	`updated_by` VARCHAR(255) NULL DEFAULT NULL COLLATE 'utf8_bin',
	`created_date` TIMESTAMP NULL DEFAULT NULL,
	`updated_date` TIMESTAMP NULL DEFAULT NULL ON UPDATE current_timestamp(),
	`active_ind` TINYINT(1) NULL DEFAULT 1,
	`loc_code` VARCHAR(255) NULL DEFAULT NULL COLLATE 'utf8_bin',
	`loc_desc` VARCHAR(255) NULL DEFAULT NULL COLLATE 'utf8_bin',
	`address_id` BIGINT NULL,
	`address_contact_id` BIGINT NULL,
	`tenant_id` BIGINT(20) NULL DEFAULT NULL,
	PRIMARY KEY (`id`)
)
COLLATE='utf8_bin'
ENGINE=InnoDB
AUTO_INCREMENT=1;


CREATE TABLE `partner_location` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT,
  `location_id` bigint(20) DEFAULT NULL,
  `partner_id` bigint(20) DEFAULT NULL,
  `active_ind` tinyint(1) DEFAULT 0,
  `created_by` varchar(255) DEFAULT '',
  `created_date` timestamp NULL DEFAULT NULL ON UPDATE current_timestamp(),
  `updated_by` varchar(255) DEFAULT '',
  `updated_date` timestamp NULL DEFAULT NULL ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `PARTNER_ID` (`partner_id`),
  KEY `LOCATION_ID` (`location_id`),
  CONSTRAINT `partner_location_ibfk_1` FOREIGN KEY (`partner_id`) REFERENCES `partners` (`id`),
  CONSTRAINT `partner_location_ibfk_2` FOREIGN KEY (`location_id`) REFERENCES `location` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


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