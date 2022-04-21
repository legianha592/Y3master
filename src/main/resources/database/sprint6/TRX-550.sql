
-- ----------------------------
-- Table structure for sequence api config
-- ----------------------------
CREATE TABLE `seq_api_config` (
    `next_val` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for api config
-- ----------------------------
DROP TABLE IF EXISTS `api_config`;
CREATE TABLE `api_config` (
                              `id` bigint NOT NULL,
                              `hashcode` int(11) DEFAULT NULL,
                              `active_ind` bit(1) DEFAULT NULL,
                              `created_by` varchar(255) DEFAULT NULL,
                              `created_date` datetime(6) DEFAULT NULL,
                              `updated_by` varchar(255) DEFAULT NULL,
                              `updated_date` datetime(6) DEFAULT NULL,
                              `version` bigint(20) DEFAULT NULL,
                              `lookup_id` bigint NOT NULL,
                              `customer_id` bigint NOT NULL,
                              `type` varchar(50) NOT NULL,
                              `url` varchar(300) NOT NULL,
                              `urn` varchar(128) DEFAULT NULL,
                              `description` varchar(300) DEFAULT NULL,
                              `is_active` int(1) DEFAULT 0,
                              `tenant_id` bigint(20) NOT NULL DEFAULT 0,
                              `authen_type` varchar(50) DEFAULT NULL,
                              `api_key` varchar(128) DEFAULT NULL,
                              `username` varchar(50) DEFAULT NULL,
                              `password` varchar(500) DEFAULT NULL,
                              PRIMARY KEY (`id`),
                              CONSTRAINT `api_config_ibfk_1` UNIQUE(`lookup_id`,`customer_id`),
                              CONSTRAINT `api_config_ibfk_2` FOREIGN KEY (`lookup_id`) REFERENCES `lookup` (`ID`),
                              CONSTRAINT `api_config_ibfk_3` FOREIGN KEY (`customer_id`) REFERENCES `partners` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for sequence api config
-- ----------------------------
CREATE TABLE `seq_api_audit_log` (
    `next_val` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for api config
-- ----------------------------
DROP TABLE IF EXISTS `api_audit_log`;
CREATE TABLE `api_audit_log` (
                              `id` bigint NOT NULL,
                              `hashcode` int(11) DEFAULT NULL,
                              `active_ind` bit(1) DEFAULT NULL,
                              `created_by` varchar(255) DEFAULT NULL,
                              `created_date` datetime(6) DEFAULT NULL,
                              `updated_by` varchar(255) DEFAULT NULL,
                              `updated_date` datetime(6) DEFAULT NULL,
                              `version` bigint(20) DEFAULT NULL,
                              `api_id` bigint NOT NULL,
                              `tenant_id` bigint NOT NULL,
                              `audit_user` bigint NOT NULL,
                              `audit_request_dttm` datetime(6) NOT NULL,
                              `audit_response_dttm` datetime(6) NOT NULL,
                              `audit_action` varchar(255) NOT NULL,
                              `api_url` varchar(300) NOT NULL,
                              `api_urn` varchar(128) NOT NULL,
                              `api_name` varchar(255) NOT NULL,
                              `api_type` varchar(10) NOT NULL,
                              `customer_name` varchar(255) NOT NULL,
                              `request_params` text NOT NULL,
                              `request_headers` text NOT NULL,
                              `response_status` varchar(10) NOT NULL,
                              `request_payload` text NOT NULL,
                              `response_payload` text NOT NULL,
                              `is_active` int(1) DEFAULT 0,
                              PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `lookup` (`active_ind`, `created_by`, `created_date`, `lookup_code`, `lookup_description`, `lookup_type`, `tenant_id`) VALUES (1, 'system', now(), 'Transport Request (Incoming)', 'INCOMING', 'API_CONFIG', 0);
INSERT INTO `lookup` (`active_ind`, `created_by`, `created_date`, `lookup_code`, `lookup_description`, `lookup_type`, `tenant_id`) VALUES (1, 'system', now(), 'Plan & Dispatch (Incoming)', 'INCOMING', 'API_CONFIG', 0);
INSERT INTO `lookup` (`active_ind`, `created_by`, `created_date`, `lookup_code`, `lookup_description`, `lookup_type`, `tenant_id`) VALUES (1, 'system', now(), 'Plan & Dispatch (Smart Log - Outgoing)', 'OUT_GOING', 'API_CONFIG', 0);
INSERT INTO `lookup` (`active_ind`, `created_by`, `created_date`, `lookup_code`, `lookup_description`, `lookup_type`, `tenant_id`) VALUES (1, 'system', now(), 'Status Update (Incoming)', 'INCOMING', 'API_CONFIG', 0);
INSERT INTO `lookup` (`active_ind`, `created_by`, `created_date`, `lookup_code`, `lookup_description`, `lookup_type`, `tenant_id`) VALUES (1, 'system', now(), 'Status Update (Outgoing)', 'OUT_GOING', 'API_CONFIG', 0);
