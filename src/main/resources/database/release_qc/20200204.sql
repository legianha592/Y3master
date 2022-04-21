/*
Navicat MariaDB Data Transfer

Source Server         : 10.210.3.80_trx4
Source Server Version : 100410
Source Host           : 10.210.3.80:3306
Source Database       : trxmaster

Target Server Type    : MariaDB
Target Server Version : 100410
File Encoding         : 65001

Date: 2020-02-04 17:09:04
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for common_tag
-- ----------------------------
DROP TABLE IF EXISTS `common_tag`;
CREATE TABLE `common_tag` (
  `id` bigint(20) NOT NULL,
  `active_ind` bit(1) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `reference_function` varchar(255) DEFAULT NULL,
  `reference_id` bigint(20) DEFAULT NULL,
  `tag` varchar(255) DEFAULT NULL,
  `tag_type` varchar(255) DEFAULT NULL,
  `tenant_id` bigint(20) DEFAULT NULL,
  `hashcode` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for config_code
-- ----------------------------
DROP TABLE IF EXISTS `config_code`;
CREATE TABLE `config_code` (
  `id` bigint(20) NOT NULL,
  `active_ind` bit(1) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `code` varchar(255) NOT NULL DEFAULT '',
  `description` varchar(255) DEFAULT NULL,
  `hashcode` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UNQ_CONFIG_CODE_1` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for driver
-- ----------------------------
DROP TABLE IF EXISTS `driver`;
CREATE TABLE `driver` (
  `id` bigint(20) NOT NULL,
  `active_ind` bit(1) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `available` bit(1) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `licence_number` varchar(255) DEFAULT NULL,
  `licence_type` varchar(255) DEFAULT NULL,
  `mobile_number` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `tenant_id` bigint(20) DEFAULT NULL,
  `hashcode` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UNQ_DRIVER_NAME_TENANT_ID` (`name`,`tenant_id`)
  UNIQUE KEY `UNQ_DRIVER_EMAIL_TENANT_ID` (`email`,`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for equipment
-- ----------------------------
DROP TABLE IF EXISTS `equipment`;
CREATE TABLE `equipment` (
  `id` bigint(20) NOT NULL,
  `active_ind` bit(1) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `equipment_type` varchar(255) DEFAULT '' COMMENT 'lookup value',
  `unit_aidc1` varchar(255) DEFAULT '',
  `unit_aidc2` varchar(255) DEFAULT '',
  `unit_type` varchar(255) DEFAULT '' COMMENT 'lookup value',
  `tare_weight` decimal(10,6) DEFAULT NULL,
  `max_weight` decimal(10,6) DEFAULT NULL,
  `volumn` decimal(10,6) DEFAULT NULL,
  `tenant_id` bigint(20) DEFAULT NULL,
  `unit_owner` varchar(255) DEFAULT NULL,
  `hashcode` int(11) DEFAULT NULL,
  `remark` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UNQ_Milestone_1` (`tenant_id`,`unit_aidc1`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for location
-- ----------------------------
DROP TABLE IF EXISTS `location`;
CREATE TABLE `location` (
  `id` bigint(20) NOT NULL,
  `active_ind` bit(1) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `loc_contact_email` varchar(255) DEFAULT NULL,
  `loc_contact_name` varchar(255) DEFAULT NULL,
  `loc_contact_phone` varchar(255) DEFAULT NULL,
  `loc_code` varchar(255) NOT NULL,
  `loc_desc` varchar(255) DEFAULT NULL,
  `multi_partner_addresses` varchar(255) DEFAULT NULL,
  `tenant_id` bigint(20) DEFAULT NULL,
  `hashcode` int(11) DEFAULT NULL,
  `address_id` bigint(20) DEFAULT NULL,
  `address_contact_id` bigint(20) DEFAULT NULL,
  `loc_name` varchar(255) DEFAULT NULL,
  `loc_contact_office_number` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UNQ_LOCATION_LOC_NAME_TENANT_ID` (`loc_name`,`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for lookup
-- ----------------------------
DROP TABLE IF EXISTS `lookup`;
CREATE TABLE `lookup` (
  `id` bigint(20) NOT NULL,
  `active_ind` bit(1) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `customer_id` bigint(20) DEFAULT NULL,
  `language_label_code` varchar(255) DEFAULT NULL,
  `lookup_code` varchar(255) DEFAULT NULL,
  `lookup_description` varchar(255) DEFAULT NULL,
  `lookup_type` varchar(255) DEFAULT NULL,
  `seq` int(11) DEFAULT NULL,
  `service_bean` varchar(255) DEFAULT NULL,
  `tenant_id` bigint(20) DEFAULT NULL,
  `hashcode` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UNQ_LOOKUP_1` (`tenant_id`,`lookup_code`,`lookup_type`)

) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for milestone
-- ----------------------------
DROP TABLE IF EXISTS `milestone`;
CREATE TABLE `milestone` (
  `id` bigint(20) NOT NULL,
  `active_ind` bit(1) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `milestone_category` varchar(255) DEFAULT NULL,
  `milestone_code` varchar(255) DEFAULT NULL,
  `milestone_description` varchar(255) DEFAULT NULL,
  `milestone_group` varchar(255) DEFAULT NULL,
  `tenant_id` bigint(20) DEFAULT NULL,
  `customer_description` varchar(255) DEFAULT NULL,
  `is_internal` bit(1) DEFAULT NULL,
  `hashcode` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UNQ_Milestone_1` (`tenant_id`,`milestone_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for milkrun_trip
-- ----------------------------
DROP TABLE IF EXISTS `milkrun_trip`;
CREATE TABLE `milkrun_trip` (
  `id` bigint(20) NOT NULL,
  `active_ind` bit(1) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `end_time` datetime(6) DEFAULT NULL,
  `location_id` bigint(20) DEFAULT NULL,
  `milkrun_vehicle_id` bigint(20) DEFAULT NULL,
  `start_time` datetime(6) DEFAULT NULL,
  `trip_sequence` int(11) DEFAULT NULL,
  `vist_sequence` int(11) DEFAULT NULL,
  `TPT_request_activity` varchar(128) DEFAULT NULL,
  `day_of_week` varchar(128) DEFAULT NULL,
  `hashcode` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for milkrun_vehicle
-- ----------------------------
DROP TABLE IF EXISTS `milkrun_vehicle`;
CREATE TABLE `milkrun_vehicle` (
  `id` bigint(20) NOT NULL,
  `active_ind` bit(1) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `customer_id` bigint(20) DEFAULT NULL,
  `tenant_id` bigint(20) DEFAULT NULL,
  `vehicle_id` bigint(20) DEFAULT NULL,
  `driver_id` bigint(20) DEFAULT NULL,
  `hashcode` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for operation_log
-- ----------------------------
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log` (
  `id` bigint(20) NOT NULL,
  `active_ind` bit(1) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `model` varchar(255) DEFAULT NULL,
  `record` varchar(4000) DEFAULT '',
  `hashcode` int(11) DEFAULT NULL,
  `operation` varchar(20) DEFAULT NULL,
  `search_key` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for partner_types
-- ----------------------------
DROP TABLE IF EXISTS `partner_types`;
CREATE TABLE `partner_types` (
  `id` bigint(20) NOT NULL,
  `active_ind` bit(1) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `code` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `hashcode` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UNQ_PARTNER_TYPES_1` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for partners
-- ----------------------------
DROP TABLE IF EXISTS `partners`;
CREATE TABLE `partners` (
  `id` bigint(20) NOT NULL,
  `active_ind` bit(1) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `partner_code` varchar(255) DEFAULT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `tenant_id` bigint(20) DEFAULT 0,
  `partner_name` varchar(255) DEFAULT NULL,
  `hashcode` int(11) DEFAULT NULL,
  `address_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UNQ_PARTNERS_1` (`tenant_id`,`partner_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for partners_partner_types
-- ----------------------------
DROP TABLE IF EXISTS `partners_partner_types`;
CREATE TABLE `partners_partner_types` (
  `partners_id` bigint(20) NOT NULL,
  `partner_types_id` bigint(20) NOT NULL,
  KEY `FKnif75y1ns290dqohj45udjmt1` (`partner_types_id`),
  KEY `FKelt2sn5xr0ehk7y51298eu358` (`partners_id`),
  CONSTRAINT `FKelt2sn5xr0ehk7y51298eu358` FOREIGN KEY (`partners_id`) REFERENCES `partners` (`id`),
  CONSTRAINT `FKnif75y1ns290dqohj45udjmt1` FOREIGN KEY (`partner_types_id`) REFERENCES `partner_types` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for partner_config
-- ----------------------------
DROP TABLE IF EXISTS `partner_config`;
CREATE TABLE `partner_config` (
  `id` bigint(20) NOT NULL,
  `active_ind` bit(1) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `value` varchar(255) DEFAULT NULL,
  `config_code_id` bigint(20) DEFAULT NULL,
  `partner_id` bigint(20) DEFAULT NULL,
  `hashcode` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK4lslrtqbbs955rk5juhqx1d7o` (`config_code_id`),
  KEY `FKb6awh1y1ydrg3wvd8ffgc95tx` (`partner_id`),
  CONSTRAINT `FK4lslrtqbbs955rk5juhqx1d7o` FOREIGN KEY (`config_code_id`) REFERENCES `config_code` (`id`),
  CONSTRAINT `FKb6awh1y1ydrg3wvd8ffgc95tx` FOREIGN KEY (`partner_id`) REFERENCES `partners` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for partner_location
-- ----------------------------
DROP TABLE IF EXISTS `partner_location`;
CREATE TABLE `partner_location` (
  `id` bigint(20) NOT NULL,
  `active_ind` bit(1) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `location_id` bigint(20) DEFAULT NULL,
  `partner_id` bigint(20) DEFAULT NULL,
  `hashcode` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKshlvpg77bi86a8u40dg7pax3v` (`partner_id`),
  CONSTRAINT `FKshlvpg77bi86a8u40dg7pax3v` FOREIGN KEY (`partner_id`) REFERENCES `partners` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for reason_code
-- ----------------------------
DROP TABLE IF EXISTS `reason_code`;
CREATE TABLE `reason_code` (
  `id` bigint(20) NOT NULL,
  `active_ind` bit(1) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `category` varchar(255) DEFAULT NULL,
  `induced_by` varchar(255) DEFAULT NULL,
  `reason_code` varchar(255) DEFAULT NULL,
  `reason_description` varchar(255) DEFAULT NULL,
  `tenant_id` bigint(20) DEFAULT NULL,
  `reason_usage` varchar(255) DEFAULT NULL,
  `hashcode` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UNQ_REASON_CODE_1` (`tenant_id`,`reason_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for seq_common_tag
-- ----------------------------
DROP TABLE IF EXISTS `seq_common_tag`;
CREATE TABLE `seq_common_tag` (
  `next_not_cached_value` bigint(21) NOT NULL,
  `minimum_value` bigint(21) NOT NULL,
  `maximum_value` bigint(21) NOT NULL,
  `start_value` bigint(21) NOT NULL COMMENT 'start value when sequences is created or value if RESTART is used',
  `increment` bigint(21) NOT NULL COMMENT 'increment value',
  `cache_size` bigint(21) unsigned NOT NULL,
  `cycle_option` tinyint(1) unsigned NOT NULL COMMENT '0 if no cycles are allowed, 1 if the sequence should begin a new cycle when maximum_value is passed',
  `cycle_count` bigint(21) NOT NULL COMMENT 'How many cycles have been done'
) ENGINE=InnoDB SEQUENCE=1;

-- ----------------------------
-- Table structure for seq_config_code
-- ----------------------------
DROP TABLE IF EXISTS `seq_config_code`;
CREATE TABLE `seq_config_code` (
  `next_not_cached_value` bigint(21) NOT NULL,
  `minimum_value` bigint(21) NOT NULL,
  `maximum_value` bigint(21) NOT NULL,
  `start_value` bigint(21) NOT NULL COMMENT 'start value when sequences is created or value if RESTART is used',
  `increment` bigint(21) NOT NULL COMMENT 'increment value',
  `cache_size` bigint(21) unsigned NOT NULL,
  `cycle_option` tinyint(1) unsigned NOT NULL COMMENT '0 if no cycles are allowed, 1 if the sequence should begin a new cycle when maximum_value is passed',
  `cycle_count` bigint(21) NOT NULL COMMENT 'How many cycles have been done'
) ENGINE=InnoDB SEQUENCE=1;

-- ----------------------------
-- Table structure for seq_driver
-- ----------------------------
DROP TABLE IF EXISTS `seq_driver`;
CREATE TABLE `seq_driver` (
  `next_not_cached_value` bigint(21) NOT NULL,
  `minimum_value` bigint(21) NOT NULL,
  `maximum_value` bigint(21) NOT NULL,
  `start_value` bigint(21) NOT NULL COMMENT 'start value when sequences is created or value if RESTART is used',
  `increment` bigint(21) NOT NULL COMMENT 'increment value',
  `cache_size` bigint(21) unsigned NOT NULL,
  `cycle_option` tinyint(1) unsigned NOT NULL COMMENT '0 if no cycles are allowed, 1 if the sequence should begin a new cycle when maximum_value is passed',
  `cycle_count` bigint(21) NOT NULL COMMENT 'How many cycles have been done'
) ENGINE=InnoDB SEQUENCE=1;

-- ----------------------------
-- Table structure for seq_equipment
-- ----------------------------
DROP TABLE IF EXISTS `seq_equipment`;
CREATE TABLE `seq_equipment` (
  `next_not_cached_value` bigint(21) NOT NULL,
  `minimum_value` bigint(21) NOT NULL,
  `maximum_value` bigint(21) NOT NULL,
  `start_value` bigint(21) NOT NULL COMMENT 'start value when sequences is created or value if RESTART is used',
  `increment` bigint(21) NOT NULL COMMENT 'increment value',
  `cache_size` bigint(21) unsigned NOT NULL,
  `cycle_option` tinyint(1) unsigned NOT NULL COMMENT '0 if no cycles are allowed, 1 if the sequence should begin a new cycle when maximum_value is passed',
  `cycle_count` bigint(21) NOT NULL COMMENT 'How many cycles have been done'
) ENGINE=InnoDB SEQUENCE=1;

-- ----------------------------
-- Table structure for seq_location
-- ----------------------------
DROP TABLE IF EXISTS `seq_location`;
CREATE TABLE `seq_location` (
  `next_not_cached_value` bigint(21) NOT NULL,
  `minimum_value` bigint(21) NOT NULL,
  `maximum_value` bigint(21) NOT NULL,
  `start_value` bigint(21) NOT NULL COMMENT 'start value when sequences is created or value if RESTART is used',
  `increment` bigint(21) NOT NULL COMMENT 'increment value',
  `cache_size` bigint(21) unsigned NOT NULL,
  `cycle_option` tinyint(1) unsigned NOT NULL COMMENT '0 if no cycles are allowed, 1 if the sequence should begin a new cycle when maximum_value is passed',
  `cycle_count` bigint(21) NOT NULL COMMENT 'How many cycles have been done'
) ENGINE=InnoDB SEQUENCE=1;

-- ----------------------------
-- Table structure for seq_lookup
-- ----------------------------
DROP TABLE IF EXISTS `seq_lookup`;
CREATE TABLE `seq_lookup` (
  `next_not_cached_value` bigint(21) NOT NULL,
  `minimum_value` bigint(21) NOT NULL,
  `maximum_value` bigint(21) NOT NULL,
  `start_value` bigint(21) NOT NULL COMMENT 'start value when sequences is created or value if RESTART is used',
  `increment` bigint(21) NOT NULL COMMENT 'increment value',
  `cache_size` bigint(21) unsigned NOT NULL,
  `cycle_option` tinyint(1) unsigned NOT NULL COMMENT '0 if no cycles are allowed, 1 if the sequence should begin a new cycle when maximum_value is passed',
  `cycle_count` bigint(21) NOT NULL COMMENT 'How many cycles have been done'
) ENGINE=InnoDB SEQUENCE=1;

-- ----------------------------
-- Table structure for seq_milestone
-- ----------------------------
DROP TABLE IF EXISTS `seq_milestone`;
CREATE TABLE `seq_milestone` (
  `next_not_cached_value` bigint(21) NOT NULL,
  `minimum_value` bigint(21) NOT NULL,
  `maximum_value` bigint(21) NOT NULL,
  `start_value` bigint(21) NOT NULL COMMENT 'start value when sequences is created or value if RESTART is used',
  `increment` bigint(21) NOT NULL COMMENT 'increment value',
  `cache_size` bigint(21) unsigned NOT NULL,
  `cycle_option` tinyint(1) unsigned NOT NULL COMMENT '0 if no cycles are allowed, 1 if the sequence should begin a new cycle when maximum_value is passed',
  `cycle_count` bigint(21) NOT NULL COMMENT 'How many cycles have been done'
) ENGINE=InnoDB SEQUENCE=1;

-- ----------------------------
-- Table structure for seq_milkrun_trip
-- ----------------------------
DROP TABLE IF EXISTS `seq_milkrun_trip`;
CREATE TABLE `seq_milkrun_trip` (
  `next_not_cached_value` bigint(21) NOT NULL,
  `minimum_value` bigint(21) NOT NULL,
  `maximum_value` bigint(21) NOT NULL,
  `start_value` bigint(21) NOT NULL COMMENT 'start value when sequences is created or value if RESTART is used',
  `increment` bigint(21) NOT NULL COMMENT 'increment value',
  `cache_size` bigint(21) unsigned NOT NULL,
  `cycle_option` tinyint(1) unsigned NOT NULL COMMENT '0 if no cycles are allowed, 1 if the sequence should begin a new cycle when maximum_value is passed',
  `cycle_count` bigint(21) NOT NULL COMMENT 'How many cycles have been done'
) ENGINE=InnoDB SEQUENCE=1;

-- ----------------------------
-- Table structure for seq_milkrun_vehicle
-- ----------------------------
DROP TABLE IF EXISTS `seq_milkrun_vehicle`;
CREATE TABLE `seq_milkrun_vehicle` (
  `next_not_cached_value` bigint(21) NOT NULL,
  `minimum_value` bigint(21) NOT NULL,
  `maximum_value` bigint(21) NOT NULL,
  `start_value` bigint(21) NOT NULL COMMENT 'start value when sequences is created or value if RESTART is used',
  `increment` bigint(21) NOT NULL COMMENT 'increment value',
  `cache_size` bigint(21) unsigned NOT NULL,
  `cycle_option` tinyint(1) unsigned NOT NULL COMMENT '0 if no cycles are allowed, 1 if the sequence should begin a new cycle when maximum_value is passed',
  `cycle_count` bigint(21) NOT NULL COMMENT 'How many cycles have been done'
) ENGINE=InnoDB SEQUENCE=1;

-- ----------------------------
-- Table structure for seq_operation_log
-- ----------------------------
DROP TABLE IF EXISTS `seq_operation_log`;
CREATE TABLE `seq_operation_log` (
  `next_not_cached_value` bigint(21) NOT NULL,
  `minimum_value` bigint(21) NOT NULL,
  `maximum_value` bigint(21) NOT NULL,
  `start_value` bigint(21) NOT NULL COMMENT 'start value when sequences is created or value if RESTART is used',
  `increment` bigint(21) NOT NULL COMMENT 'increment value',
  `cache_size` bigint(21) unsigned NOT NULL,
  `cycle_option` tinyint(1) unsigned NOT NULL COMMENT '0 if no cycles are allowed, 1 if the sequence should begin a new cycle when maximum_value is passed',
  `cycle_count` bigint(21) NOT NULL COMMENT 'How many cycles have been done'
) ENGINE=InnoDB SEQUENCE=1;

-- ----------------------------
-- Table structure for seq_partner_config
-- ----------------------------
DROP TABLE IF EXISTS `seq_partner_config`;
CREATE TABLE `seq_partner_config` (
  `next_not_cached_value` bigint(21) NOT NULL,
  `minimum_value` bigint(21) NOT NULL,
  `maximum_value` bigint(21) NOT NULL,
  `start_value` bigint(21) NOT NULL COMMENT 'start value when sequences is created or value if RESTART is used',
  `increment` bigint(21) NOT NULL COMMENT 'increment value',
  `cache_size` bigint(21) unsigned NOT NULL,
  `cycle_option` tinyint(1) unsigned NOT NULL COMMENT '0 if no cycles are allowed, 1 if the sequence should begin a new cycle when maximum_value is passed',
  `cycle_count` bigint(21) NOT NULL COMMENT 'How many cycles have been done'
) ENGINE=InnoDB SEQUENCE=1;

-- ----------------------------
-- Table structure for seq_partner_location
-- ----------------------------
DROP TABLE IF EXISTS `seq_partner_location`;
CREATE TABLE `seq_partner_location` (
  `next_not_cached_value` bigint(21) NOT NULL,
  `minimum_value` bigint(21) NOT NULL,
  `maximum_value` bigint(21) NOT NULL,
  `start_value` bigint(21) NOT NULL COMMENT 'start value when sequences is created or value if RESTART is used',
  `increment` bigint(21) NOT NULL COMMENT 'increment value',
  `cache_size` bigint(21) unsigned NOT NULL,
  `cycle_option` tinyint(1) unsigned NOT NULL COMMENT '0 if no cycles are allowed, 1 if the sequence should begin a new cycle when maximum_value is passed',
  `cycle_count` bigint(21) NOT NULL COMMENT 'How many cycles have been done'
) ENGINE=InnoDB SEQUENCE=1;

-- ----------------------------
-- Table structure for seq_partner_types
-- ----------------------------
DROP TABLE IF EXISTS `seq_partner_types`;
CREATE TABLE `seq_partner_types` (
  `next_not_cached_value` bigint(21) NOT NULL,
  `minimum_value` bigint(21) NOT NULL,
  `maximum_value` bigint(21) NOT NULL,
  `start_value` bigint(21) NOT NULL COMMENT 'start value when sequences is created or value if RESTART is used',
  `increment` bigint(21) NOT NULL COMMENT 'increment value',
  `cache_size` bigint(21) unsigned NOT NULL,
  `cycle_option` tinyint(1) unsigned NOT NULL COMMENT '0 if no cycles are allowed, 1 if the sequence should begin a new cycle when maximum_value is passed',
  `cycle_count` bigint(21) NOT NULL COMMENT 'How many cycles have been done'
) ENGINE=InnoDB SEQUENCE=1;

-- ----------------------------
-- Table structure for seq_partners
-- ----------------------------
DROP TABLE IF EXISTS `seq_partners`;
CREATE TABLE `seq_partners` (
  `next_not_cached_value` bigint(21) NOT NULL,
  `minimum_value` bigint(21) NOT NULL,
  `maximum_value` bigint(21) NOT NULL,
  `start_value` bigint(21) NOT NULL COMMENT 'start value when sequences is created or value if RESTART is used',
  `increment` bigint(21) NOT NULL COMMENT 'increment value',
  `cache_size` bigint(21) unsigned NOT NULL,
  `cycle_option` tinyint(1) unsigned NOT NULL COMMENT '0 if no cycles are allowed, 1 if the sequence should begin a new cycle when maximum_value is passed',
  `cycle_count` bigint(21) NOT NULL COMMENT 'How many cycles have been done'
) ENGINE=InnoDB SEQUENCE=1;

-- ----------------------------
-- Table structure for seq_reason_code
-- ----------------------------
DROP TABLE IF EXISTS `seq_reason_code`;
CREATE TABLE `seq_reason_code` (
  `next_not_cached_value` bigint(21) NOT NULL,
  `minimum_value` bigint(21) NOT NULL,
  `maximum_value` bigint(21) NOT NULL,
  `start_value` bigint(21) NOT NULL COMMENT 'start value when sequences is created or value if RESTART is used',
  `increment` bigint(21) NOT NULL COMMENT 'increment value',
  `cache_size` bigint(21) unsigned NOT NULL,
  `cycle_option` tinyint(1) unsigned NOT NULL COMMENT '0 if no cycles are allowed, 1 if the sequence should begin a new cycle when maximum_value is passed',
  `cycle_count` bigint(21) NOT NULL COMMENT 'How many cycles have been done'
) ENGINE=InnoDB SEQUENCE=1;

-- ----------------------------
-- Table structure for seq_uom_setting
-- ----------------------------
DROP TABLE IF EXISTS `seq_uom_setting`;
CREATE TABLE `seq_uom_setting` (
  `next_not_cached_value` bigint(21) NOT NULL,
  `minimum_value` bigint(21) NOT NULL,
  `maximum_value` bigint(21) NOT NULL,
  `start_value` bigint(21) NOT NULL COMMENT 'start value when sequences is created or value if RESTART is used',
  `increment` bigint(21) NOT NULL COMMENT 'increment value',
  `cache_size` bigint(21) unsigned NOT NULL,
  `cycle_option` tinyint(1) unsigned NOT NULL COMMENT '0 if no cycles are allowed, 1 if the sequence should begin a new cycle when maximum_value is passed',
  `cycle_count` bigint(21) NOT NULL COMMENT 'How many cycles have been done'
) ENGINE=InnoDB SEQUENCE=1;

-- ----------------------------
-- Table structure for seq_vehicle
-- ----------------------------
DROP TABLE IF EXISTS `seq_vehicle`;
CREATE TABLE `seq_vehicle` (
  `next_not_cached_value` bigint(21) NOT NULL,
  `minimum_value` bigint(21) NOT NULL,
  `maximum_value` bigint(21) NOT NULL,
  `start_value` bigint(21) NOT NULL COMMENT 'start value when sequences is created or value if RESTART is used',
  `increment` bigint(21) NOT NULL COMMENT 'increment value',
  `cache_size` bigint(21) unsigned NOT NULL,
  `cycle_option` tinyint(1) unsigned NOT NULL COMMENT '0 if no cycles are allowed, 1 if the sequence should begin a new cycle when maximum_value is passed',
  `cycle_count` bigint(21) NOT NULL COMMENT 'How many cycles have been done'
) ENGINE=InnoDB SEQUENCE=1;

-- ----------------------------
-- Table structure for uom_setting
-- ----------------------------
DROP TABLE IF EXISTS `uom_setting`;
CREATE TABLE `uom_setting` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `updated_by` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `created_date` timestamp NULL DEFAULT NULL,
  `updated_date` timestamp NULL DEFAULT NULL ON UPDATE current_timestamp(),
  `active_ind` tinyint(1) DEFAULT 1,
  `uom` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `description` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `uom_group` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `remark` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `hashcode` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UNQ_UOM_SETTING_1` (`uom`,`uom_group`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for vehicle
-- ----------------------------
DROP TABLE IF EXISTS `vehicle`;
CREATE TABLE `vehicle` (
  `id` bigint(20) NOT NULL,
  `active_ind` bit(1) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `available` bit(1) DEFAULT NULL,
  `licence_type_required` varchar(255) DEFAULT NULL,
  `tenant_id` bigint(20) DEFAULT NULL,
  `vehicle_reg_number` varchar(255) DEFAULT NULL,
  `vehicle_type` varchar(255) DEFAULT NULL,
  `asset_default_loc_id` bigint(20) DEFAULT NULL,
  `pkgs` int(11) DEFAULT NULL,
  `vol` decimal(19,2) DEFAULT NULL,
  `wt` decimal(19,2) DEFAULT NULL,
  `default_driver_id` bigint(20) DEFAULT NULL,
  `hashcode` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UNQ_VEHICLE_1` (`vehicle_reg_number`,`tenant_id`)
  KEY `FKgle7f84dc8akdch5wp5rr6qqd` (`asset_default_loc_id`),
  KEY `FKbci91mphoi2nkg6lnf2sj1bl4` (`default_driver_id`),
  CONSTRAINT `FKbci91mphoi2nkg6lnf2sj1bl4` FOREIGN KEY (`default_driver_id`) REFERENCES `driver` (`id`),
  CONSTRAINT `FKgle7f84dc8akdch5wp5rr6qqd` FOREIGN KEY (`asset_default_loc_id`) REFERENCES `location` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- ----------------------------
-- Records of common_tag
-- ----------------------------
INSERT INTO `common_tag` VALUES ('1', '', null, null, null, null, null, null, 'category1', 'ReasonCodeCategory', '0', null);
INSERT INTO `common_tag` VALUES ('2', '', null, null, null, null, null, null, 'category2', 'ReasonCodeCategory', '0', null);
INSERT INTO `common_tag` VALUES ('3', '', null, null, null, null, null, null, 'usage1', 'ReasonCodeUsage', '0', null);
INSERT INTO `common_tag` VALUES ('4', '', null, null, null, null, null, null, 'usage2', 'ReasonCodeUsage', '0', null);
INSERT INTO `common_tag` VALUES ('5', '', null, null, null, null, null, null, 'category1', 'MilestoneCategory', '0', null);
INSERT INTO `common_tag` VALUES ('6', '', null, null, null, null, null, null, 'category2', 'MilestoneCategory', '0', null);
INSERT INTO `common_tag` VALUES ('7', '', null, null, null, null, null, null, 'LicenseType1', 'LicenseType', '0', null);
INSERT INTO `common_tag` VALUES ('8', '', null, null, null, null, null, null, 'LicenseType2', 'LicenseType', '0', null);
INSERT INTO `common_tag` VALUES ('9', '', null, null, null, null, null, null, 'LicenseType3', 'LicenseType', '0', null);



-- ----------------------------
-- Records of lookup
-- ----------------------------
INSERT INTO `lookup` VALUES ('3', '', null, null, null, null, null, null, '1', 'equipmentType1', 'EquipmentType', '1', null, null, null);
INSERT INTO `lookup` VALUES ('4', '', null, null, null, null, null, null, '2', 'equipmentType2', 'EquipmentType', '2', null, null, null);
INSERT INTO `lookup` VALUES ('5', '', null, null, null, null, null, null, '3', 'equipmentType3', 'EquipmentType', '3', null, null, null);
INSERT INTO `lookup` VALUES ('6', '', null, null, null, null, null, null, '1', 'unitType1', 'EquipmentUnitType', '1', null, null, null);
INSERT INTO `lookup` VALUES ('7', '', null, null, null, null, null, null, '2', 'unitType2', 'EquipmentUnitType', '2', null, null, null);
INSERT INTO `lookup` VALUES ('8', '', null, null, null, null, null, null, '3', 'unitType3', 'EquipmentUnitType', '3', null, null, null);
INSERT INTO `lookup` VALUES ('9', '', null, null, null, null, null, null, '1', 'licenseType1', 'DriverLicenseType', '1', null, null, null);
INSERT INTO `lookup` VALUES ('10', '', null, null, null, null, null, null, '2', 'licenseType2', 'DriverLicenseType', '2', null, null, null);
INSERT INTO `lookup` VALUES ('11', '', null, null, null, '2020-01-07 10:55:00.000000', null, null, '3', 'licenseType3', 'DriverLicenseType', '3', null, null, null);
INSERT INTO `lookup` VALUES ('12', '', null, null, null, null, null, null, '4', 'licenseType4', 'DriverLicenseType', '4', null, null, null);
INSERT INTO `lookup` VALUES ('13', '', null, null, null, null, null, null, 'Master', 'Master', 'ServiceType', '1', null, null, null);
INSERT INTO `lookup` VALUES ('14', '', null, null, null, null, null, null, 'Schedule', 'Schedule', 'ServiceType', '2', null, null, null);
INSERT INTO `lookup` VALUES ('15', '', null, null, null, null, null, null, 'masterTestBean1', 'MasterTestBean1', 'Master_BeanType', '1', '', null, null);
INSERT INTO `lookup` VALUES ('16', '', null, null, null, null, null, null, 'masterTestBean2', 'MasterTestBean2', 'Master_BeanType', '2', '', null, null);
INSERT INTO `lookup` VALUES ('17', '', null, null, null, null, null, null, 'scheduleTestBean1', 'ScheduleTestBean1', 'Schedule_BeanType', '1', '', null, null);
INSERT INTO `lookup` VALUES ('18', '', null, null, null, null, null, null, 'scheduleTestBean2', 'ScheduleTestBean2', 'Schedule_BeanType', '2', '', null, null);
INSERT INTO `lookup` VALUES ('19', '', null, null, null, null, null, null, '/v1/masters/lookup/listByParam', 'FunctionUrl1', 'masterTestBean1_FunctionType', '1', '', null, null);
INSERT INTO `lookup` VALUES ('20', '', null, null, null, null, null, null, '/v1/masters/lookup/listByParam', 'FunctionUrl1', 'scheduleTestBean1_FunctionType', '1', '', null, null);
INSERT INTO `lookup` VALUES ('21', '', null, null, null, null, null, null, '/v1/masters/lookup/listByParam', 'FunctionUrl1', 'scheduleTestBean2_FunctionType', null, null, null, null);
INSERT INTO `lookup` VALUES ('22', '', null, null, null, null, null, null, '/v1/masters/lookup/listByParam', 'FunctionUrl1', 'masterTestBean2_FunctionType', null, null, null, null);
INSERT INTO `lookup` VALUES ('23', '', null, null, null, null, null, null, 'Licence Class 1', 'Licence Class 1', 'VehicleLicenceClass', '1', null, null, null);
INSERT INTO `lookup` VALUES ('24', '', null, null, null, null, null, null, 'Licence Class 2', 'Licence Class 2', 'VehicleLicenceClass', '2', null, null, null);
INSERT INTO `lookup` VALUES ('25', '', null, null, null, null, null, null, 'Licence Class 3', 'Licence Class 3', 'VehicleLicenceClass', '3', null, null, null);
INSERT INTO `lookup` VALUES ('26', '', null, null, null, null, null, null, 'Licence Class 4', 'Licence Class 4', 'VehicleLicenceClass', '4', null, null, null);
INSERT INTO `lookup` VALUES ('27', '', null, null, null, null, null, null, 'VehicleType1', 'VehicleType1', 'VehicleType', '1', null, null, null);
INSERT INTO `lookup` VALUES ('28', '', null, null, null, null, null, null, 'VehicleType2', 'VehicleType2', 'VehicleType', '2', null, null, null);
INSERT INTO `lookup` VALUES ('29', '', null, null, null, null, null, null, 'VehicleType3', 'VehicleType3', 'VehicleType', '3', null, null, null);
INSERT INTO `lookup` VALUES ('30', '', null, null, null, null, null, null, 'MilestoneGroup1', 'MilestoneGroup1', 'MilestoneGroup', '1', null, null, null);
INSERT INTO `lookup` VALUES ('31', '', null, '2020-01-07 11:28:10.000000', null, null, null, null, 'MilestoneGroup2', 'MilestoneGroup2', 'MilestoneGroup', '2', null, null, null);
INSERT INTO `lookup` VALUES ('32', '', null, null, null, null, null, null, 'MilestoneGroup3', 'MilestoneGroup3', 'MilestoneGroup', '3', null, null, null);
INSERT INTO `lookup` VALUES ('33', '', null, null, null, null, null, null, 'MilestoneGroup4', 'MilestoneGroup4', 'MilestoneGroup', '4', null, null, null);
INSERT INTO `lookup` VALUES ('34', '', null, null, null, null, null, null, 'PickUp', 'Pick Up', 'MilkrunServiceType', '1', null, null, null);
INSERT INTO `lookup` VALUES ('35', '', null, null, null, null, null, null, 'DripOff', 'Drip Off', 'MilkrunServiceType', '2', null, null, null);
INSERT INTO `lookup` VALUES ('36', '', null, null, null, null, null, null, 'TakeOver', 'Take Over', 'MilkrunServiceType', '3', null, null, null);
INSERT INTO `LOOKUP` VALUES (NEXTVAL(SEQ_LOOKUP), 1, 'SYSADMIN', CURRENT_TIMESTAMP, null, null, null, null, 'CUSTOMER', 'CUSTOMER', 'ReasonCodeInducedBy', '1', null, 0, null, 0);
INSERT INTO `LOOKUP` VALUES (NEXTVAL(SEQ_LOOKUP), 1, 'SYSADMIN', CURRENT_TIMESTAMP, null, null, null, null, 'TRANSPORTER', 'TRANSPORTER', 'ReasonCodeInducedBy', '2', 0, null, null, 0);
INSERT INTO `LOOKUP` VALUES (NEXTVAL(SEQ_LOOKUP), 1, 'SYSADMIN', CURRENT_TIMESTAMP, null, null, null, null, 'CONSIGNEE', 'CONSIGNEE', 'ReasonCodeInducedBy', '3', null, 0, null, 0);

-- ----------------------------
-- Records of partner_types
-- ----------------------------
INSERT INTO `partner_types` VALUES ('1', '', null, null, null, null, 'CUSTOMER', 'customer', null);
INSERT INTO `partner_types` VALUES ('2', '', null, null, null, null, 'TRANSPORTER', 'transporter', null);
INSERT INTO `partner_types` VALUES ('3', '', null, null, null, null, 'CONSIGNEE', 'consignee', null);

-- ALTER
alter table vehicle add column `cost_perkm` decimal(19,2) DEFAULT NULL;

-- ADD VERSION FOR ALL TABLES EXCEPT PARTNERS_PARTNER_TYPES
alter table vehicle add column version bigint(20) DEFAULT NULL;
alter table lookup add column version bigint(20) DEFAULT NULL;
alter table location add column version bigint(20) DEFAULT NULL;
alter table reason_code add column version bigint(20) DEFAULT NULL;
alter table partners add column version bigint(20) DEFAULT NULL;
alter table config_code add column version bigint(20) DEFAULT NULL;
alter table partner_location add column version bigint(20) DEFAULT NULL;
alter table uom_setting add column version bigint(20) DEFAULT NULL;
alter table milkrun_trip add column version bigint(20) DEFAULT NULL;
alter table driver add column version bigint(20) DEFAULT NULL;
alter table partner_types add column version bigint(20) DEFAULT NULL;
alter table common_tag add column version bigint(20) DEFAULT NULL;
alter table partner_config add column version bigint(20) DEFAULT NULL;
alter table milkrun_vehicle add column version bigint(20) DEFAULT NULL;
alter table equipment add column version bigint(20) DEFAULT NULL;
alter table operation_log add column version bigint(20) DEFAULT NULL;
alter table milestone add column version bigint(20) DEFAULT NULL;

-- increase record size
alter table operation_log change column record record varchar(4000) DEFAULT NULL;

-- alter add address contact id field
alter table driver add column address_contact_id bigint(20) DEFAULT NULL;
alter table partners add column address_contact_id bigint(20) DEFAULT NULL;

-- -----------------------------------------------------
-- ALTER TABLE NAMING TO UPPERCASE
-- -----------------------------------------------------
ALTER TABLE  common_tag RENAME TO COMMON_TAG;
ALTER TABLE  config_code RENAME TO CONFIG_CODE;
ALTER TABLE  driver RENAME TO DRIVER;
ALTER TABLE  equipment RENAME TO EQUIPMENT;
ALTER TABLE  location RENAME TO LOCATION;
ALTER TABLE  lookup RENAME TO LOOKUP;
ALTER TABLE  milestone RENAME TO MILESTONE;
ALTER TABLE  milkrun_trip RENAME TO MILKRUN_TRIP;
ALTER TABLE  milkrun_vehicle RENAME TO MILKRUN_VEHICLE;
ALTER TABLE  operation_log RENAME TO OPERATION_LOG;
ALTER TABLE  partner_config RENAME TO PARTNER_CONFIG;
ALTER TABLE  partner_location RENAME TO PARTNER_LOCATION;
ALTER TABLE  partner_types RENAME TO PARTNER_TYPES;
ALTER TABLE  partners RENAME TO PARTNERS;
ALTER TABLE  partners_partner_types RENAME TO PARTNERS_PARTNER_TYPES;
ALTER TABLE  reason_code RENAME TO REASON_CODE;
ALTER TABLE  uom_setting RENAME TO UOM_SETTING;
ALTER TABLE  vehicle RENAME TO VEHICLE;

ALTER TABLE  seq_common_tag RENAME TO SEQ_COMMON_TAG;
ALTER TABLE  seq_config_code RENAME TO SEQ_CONFIG_CODE;
ALTER TABLE  seq_driver RENAME TO SEQ_DRIVER;
ALTER TABLE  seq_equipment RENAME TO SEQ_EQUIPMENT;
ALTER TABLE  seq_location RENAME TO SEQ_LOCATION;
ALTER TABLE  seq_lookup RENAME TO SEQ_LOOKUP;
ALTER TABLE  seq_milestone RENAME TO SEQ_MILESTONE;
ALTER TABLE  seq_milkrun_trip RENAME TO SEQ_MILKRUN_TRIP;
ALTER TABLE  seq_milkrun_vehicle RENAME TO SEQ_MILKRUN_VEHICLE;
ALTER TABLE  seq_operation_log RENAME TO SEQ_OPERATION_LOG;
ALTER TABLE  seq_partner_config RENAME TO SEQ_PARTNER_CONFIG;
ALTER TABLE  seq_partner_location RENAME TO SEQ_PARTNER_LOCATION;
ALTER TABLE  seq_partner_types RENAME TO SEQ_PARTNER_TYPES;
ALTER TABLE  seq_partners RENAME TO SEQ_PARTNERS;
ALTER TABLE  seq_reason_code RENAME TO SEQ_REASON_CODE;
ALTER TABLE  seq_uom_setting RENAME TO SEQ_UOM_SETTING;
ALTER TABLE  seq_vehicle RENAME TO SEQ_VEHICLE;

-- -----------------------------------------------------
-- CHANGE ON DATA_TYPE
-- -----------------------------------------------------
ALTER TABLE MILKRUN_TRIP CHANGE COLUMN end_time end_time TIME NULL DEFAULT NULL;
ALTER TABLE MILKRUN_TRIP CHANGE COLUMN start_time start_time TIME NULL DEFAULT NULL;

-- -----------------------------------------------------
-- MODIFY CONFIG_CODE TABLE
-- -----------------------------------------------------
alter table CONFIG_CODE add column usage_level varchar(255) DEFAULT NOT NULL;
alter table CONFIG_CODE add column config_value varchar(255) DEFAULT NULL;

-- -----------------------------------------------------
-- FOR MOBILE
-- -----------------------------------------------------
INSERT INTO `COMMON_TAG` (`id`, `active_ind`, `tag`, `tag_type`, `tenant_id`, `version`) VALUES ('16', 1, 'PARTIAL', 'ReasonCodeUsage', '0', '0');
INSERT INTO `COMMON_TAG` (`id`, `active_ind`, `tag`, `tag_type`, `tenant_id`, `version`) VALUES ('17', 1, 'FAILED', 'ReasonCodeUsage', '0', '0');
INSERT INTO `COMMON_TAG` (`id`, `active_ind`, `tag`, `tag_type`, `tenant_id`, `version`) VALUES ('18', 1, 'DELIVERY', 'ReasonCodeCategory', '0', '0');
INSERT INTO `COMMON_TAG` (`id`, `active_ind`, `tag`, `tag_type`, `tenant_id`, `version`) VALUES ('19', 1, 'COLLECTION', 'ReasonCodeCategory', '0', '0');
