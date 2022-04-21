-- MySQL dump 10.13  Distrib 8.0.19, for Win64 (x86_64)
--
-- Host: 10.202.87.58    Database: trx_aas_demo
-- ------------------------------------------------------
-- Server version	8.0.15-6

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Dumping data for table `LOOKUP`
--

LOCK TABLES `LOOKUP` WRITE;
/*!40000 ALTER TABLE `LOOKUP` DISABLE KEYS */;
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (3,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'TROLLY','TROLLY','EquipmentType',1,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (4,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'BASKET','BASKET','EquipmentType',2,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (5,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'BOX','BOX','EquipmentType',3,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (6,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'METAL','METAL','EquipmentUnitType',1,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (7,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'PLASTIC','PLASTIC','EquipmentUnitType',2,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (8,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'WODDEN','WODDEN','EquipmentUnitType',3,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (9,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'Class 3','Class 3','DriverLicenseType',1,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (10,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'Class 3A','Class 3A','DriverLicenseType',2,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (11,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'Class 4','Class 4','DriverLicenseType',3,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (12,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'Class 4A','Class 4A','DriverLicenseType',4,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (13,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'Master','Master','ServiceType',1,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (14,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'Schedule','Schedule','ServiceType',2,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (15,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'masterTestBean1','MasterTestBean1','Master_BeanType',1,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (16,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'masterTestBean2','MasterTestBean2','Master_BeanType',2,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (17,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'scheduleTestBean1','ScheduleTestBean1','Schedule_BeanType',1,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (18,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'scheduleTestBean2','ScheduleTestBean2','Schedule_BeanType',2,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (19,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'/v1/masters/lookup/listByParam','FunctionUrl1','masterTestBean1_FunctionType',1,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (20,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'/v1/masters/lookup/listByParam','FunctionUrl1','scheduleTestBean1_FunctionType',1,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (21,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'/v1/masters/lookup/listByParam','FunctionUrl1','scheduleTestBean2_FunctionType',0,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (22,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'/v1/masters/lookup/listByParam','FunctionUrl1','masterTestBean2_FunctionType',0,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (23,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'Class 3','Class 3','VehicleLicenceClass',1,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (24,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'Class 3A','Class 3A','VehicleLicenceClass',2,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (25,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'Class 4','Class 4','VehicleLicenceClass',3,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (26,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'Class 4A','Class 4A','VehicleLicenceClass',4,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (27,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'Motor Bike','Motor Bike','VehicleType',1,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (28,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'Car','Car','VehicleType',2,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (29,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'Truck','Truck','VehicleType',3,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (30,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'Van','Van','VehicleType',3,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (31,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'Lorry','Lorry','VehicleType',3,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (32,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'CREATION','CREATION','MilestoneGroup',1,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (33,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'PLANNING','PLANNING','MilestoneGroup',2,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (34,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'TRANSIT','TRANSIT','MilestoneGroup',3,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (35,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'DELIVERY','DELIVERY','MilestoneGroup',4,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (36,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'PickUp','Pick Up','MilkrunServiceType',1,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (37,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'DripOff','Drip Off','MilkrunServiceType',2,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (38,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'TakeOver','Take Over','MilkrunServiceType',3,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (39,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'Weight','Weight','UOMGroup',1,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (40,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'Volume','Volume','UOMGroup',2,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (41,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'Packages','Packages','UOMGroup',3,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (42,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'Dimensions','Dimensions','UOMGroup',4,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (44,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'New','New','TransportRequestStatus',1,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (45,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'Ready','Ready','TransportRequestStatus',1,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (46,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'Pending','Pending','TransportRequestStatus',2,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (47,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'Partial','Partial','TransportRequestStatus',3,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (48,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'Fail','Fail','TransportRequestStatus',3,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (49,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'Boxes','Boxes','PackageTypes',2,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (50,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'Packets','Packets','PackageTypes',3,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (51,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'Carton','Carton','PackageTypes',2,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (52,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'Pallet','Pallet','PackageTypes',3,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (53,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'TRANSPORTER','TRANSPORTER','ReasonCodeInducedBy',2,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (54,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'CONSIGNEE','CONSIGNEE','ReasonCodeInducedBy',3,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (55,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'CUSTOMER','CUSTOMER','ReasonCodeInducedBy',1,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (56,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'NEW','NEW','TptRequestStatusType',1,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (57,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'PLAN','PLAN','TptRequestStatusType',2,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (58,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'CANCEL','CANCEL','TptRequestStatusType',3,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (59,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'PICKUP','PICKUP','TptRequestStatusType',4,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (60,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'TRANSIT','TRANSIT','TptRequestStatusType',5,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (61,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'COMPLETE','COMPLETE','TptRequestStatusType',6,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (62,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'PARTIAL','PARTIAL','TptRequestStatusType',7,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (63,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'FAILED','FAILED','TptRequestStatusType',8,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (64,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'DISPATCH','DISPATCH','TptRequestStatusType',9,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (65,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'COMPLETE','COMPLETE','MilestoneUpdateStatusType',1,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (66,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'PARTIAL','PARTIAL','MilestoneUpdateStatusType',2,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (67,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'FAILED','FAILED','MilestoneUpdateStatusType',3,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (68,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'CUSTOMER','customer','PartnerTypes',1,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (69,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'TRANSPORTER','transporter','PartnerTypes',2,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (70,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'CONSIGNEE','consignee','PartnerTypes',3,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (71,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'REGISTRATION','Registration','templateCategory',1,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (72,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'VERIFICATION','Verification','templateCategory',2,null,0,0,0);
INSERT INTO LOOKUP (id,active_ind,created_by,created_date,updated_by,updated_date,customer_id,language_label_code,lookup_code,lookup_description,lookup_type,seq,service_bean,tenant_id,hashcode,version) VALUES (73,1,'DBSCRIPT',CURRENT_TIMESTAMP,null,null,null,null,'FORGOT PASSWORD','Forgot Password','templateCategory',3,null,0,0,0);
INSERT INTO lookup (`id`,`active_ind`,`created_by`,`created_date`,`updated_by`,`updated_date`,`customer_id`,`language_label_code`,`lookup_code`,`lookup_description`,`lookup_type`,`seq`,`service_bean`,`tenant_id`,`hashcode`,`version`) VALUES (74,1,NULL,NULL,NULL,NULL,NULL,NULL,'Delivery','Delivery','TransportRequestServiceType',1,NULL,0,NULL,0);
INSERT INTO lookup (`id`,`active_ind`,`created_by`,`created_date`,`updated_by`,`updated_date`,`customer_id`,`language_label_code`,`lookup_code`,`lookup_description`,`lookup_type`,`seq`,`service_bean`,`tenant_id`,`hashcode`,`version`) VALUES (75,1,NULL,NULL,NULL,NULL,NULL,NULL,'Collection','Collection','TransportRequestServiceType',0,NULL,0,NULL,0);

/*!40000 ALTER TABLE `LOOKUP` ENABLE KEYS */;
UNLOCK TABLES;
--
-- Dumping data for table `UOM_SETTING`
--
LOCK TABLES `UOM_SETTING` WRITE;
/*!40000 ALTER TABLE `UOM_SETTING` DISABLE KEYS */;
INSERT INTO UOM_SETTING (ID,CREATED_BY,CREATED_DATE,ACTIVE_IND,UOM,DESCRIPTION,UOM_GROUP,REMARK,VERSION) VALUES(1,'DBSCRIPT',CURRENT_TIMESTAMP,1,'KG','kilogram','Weight','KGM',1);
INSERT INTO UOM_SETTING (ID,CREATED_BY,CREATED_DATE,ACTIVE_IND,UOM,DESCRIPTION,UOM_GROUP,REMARK,VERSION) VALUES(2,'DBSCRIPT',CURRENT_TIMESTAMP,1,'DG','decigram','Weight','DG',1);
INSERT INTO UOM_SETTING (ID,CREATED_BY,CREATED_DATE,ACTIVE_IND,UOM,DESCRIPTION,UOM_GROUP,REMARK,VERSION) VALUES(3,'DBSCRIPT',CURRENT_TIMESTAMP,1,'G','gram','Weight','GRM',1);
INSERT INTO UOM_SETTING (ID,CREATED_BY,CREATED_DATE,ACTIVE_IND,UOM,DESCRIPTION,UOM_GROUP,REMARK,VERSION) VALUES(4,'DBSCRIPT',CURRENT_TIMESTAMP,1,'CG','centigram','Weight','CGM',1);
INSERT INTO UOM_SETTING (ID,CREATED_BY,CREATED_DATE,ACTIVE_IND,UOM,DESCRIPTION,UOM_GROUP,REMARK,VERSION) VALUES(5,'DBSCRIPT',CURRENT_TIMESTAMP,1,'MG','milligram','Weight','MGM',1);
INSERT INTO UOM_SETTING (ID,CREATED_BY,CREATED_DATE,ACTIVE_IND,UOM,DESCRIPTION,UOM_GROUP,REMARK,VERSION) VALUES(6,'DBSCRIPT',CURRENT_TIMESTAMP,1,'KT','kilotonne','Weight','KTN',1);
INSERT INTO UOM_SETTING (ID,CREATED_BY,CREATED_DATE,ACTIVE_IND,UOM,DESCRIPTION,UOM_GROUP,REMARK,VERSION) VALUES(7,'DBSCRIPT',CURRENT_TIMESTAMP,1,'LB','pound','Weight','LBR',1);
INSERT INTO UOM_SETTING (ID,CREATED_BY,CREATED_DATE,ACTIVE_IND,UOM,DESCRIPTION,UOM_GROUP,REMARK,VERSION) VALUES(8,'DBSCRIPT',CURRENT_TIMESTAMP,1,'OZ','ounce (avoirdupois)','Weight','ONZ',1);
INSERT INTO UOM_SETTING (ID,CREATED_BY,CREATED_DATE,ACTIVE_IND,UOM,DESCRIPTION,UOM_GROUP,REMARK,VERSION) VALUES(9,'DBSCRIPT',CURRENT_TIMESTAMP,1,'TON','ton','Weight','LTN',1);
INSERT INTO UOM_SETTING (ID,CREATED_BY,CREATED_DATE,ACTIVE_IND,UOM,DESCRIPTION,UOM_GROUP,REMARK,VERSION) VALUES(10,'DBSCRIPT',CURRENT_TIMESTAMP,1,'M3','cubic metre','Volume','MTQ',1);
INSERT INTO UOM_SETTING (ID,CREATED_BY,CREATED_DATE,ACTIVE_IND,UOM,DESCRIPTION,UOM_GROUP,REMARK,VERSION) VALUES(11,'DBSCRIPT',CURRENT_TIMESTAMP,1,'MAL','megalitre','Volume','MAL',1);
INSERT INTO UOM_SETTING (ID,CREATED_BY,CREATED_DATE,ACTIVE_IND,UOM,DESCRIPTION,UOM_GROUP,REMARK,VERSION) VALUES(12,'DBSCRIPT',CURRENT_TIMESTAMP,1,'L','litre','Volume','LTR',1);
INSERT INTO UOM_SETTING (ID,CREATED_BY,CREATED_DATE,ACTIVE_IND,UOM,DESCRIPTION,UOM_GROUP,REMARK,VERSION) VALUES(13,'DBSCRIPT',CURRENT_TIMESTAMP,1,'MM3','cubic millimetre','Volume','MMQ',1);
INSERT INTO UOM_SETTING (ID,CREATED_BY,CREATED_DATE,ACTIVE_IND,UOM,DESCRIPTION,UOM_GROUP,REMARK,VERSION) VALUES(14,'DBSCRIPT',CURRENT_TIMESTAMP,1,'CM3','cubic centimetre','Volume','CMQ',1);
INSERT INTO UOM_SETTING (ID,CREATED_BY,CREATED_DATE,ACTIVE_IND,UOM,DESCRIPTION,UOM_GROUP,REMARK,VERSION) VALUES(15,'DBSCRIPT',CURRENT_TIMESTAMP,1,'DM3','cubic decimetre','Volume','DMQ',1);
INSERT INTO UOM_SETTING (ID,CREATED_BY,CREATED_DATE,ACTIVE_IND,UOM,DESCRIPTION,UOM_GROUP,REMARK,VERSION) VALUES(16,'DBSCRIPT',CURRENT_TIMESTAMP,1,'MLT','millilitre','Volume','MLT',1);
INSERT INTO UOM_SETTING (ID,CREATED_BY,CREATED_DATE,ACTIVE_IND,UOM,DESCRIPTION,UOM_GROUP,REMARK,VERSION) VALUES(17,'DBSCRIPT',CURRENT_TIMESTAMP,1,'IN3','cubic inch','Volume','INQ',1);
INSERT INTO UOM_SETTING (ID,CREATED_BY,CREATED_DATE,ACTIVE_IND,UOM,DESCRIPTION,UOM_GROUP,REMARK,VERSION) VALUES(18,'DBSCRIPT',CURRENT_TIMESTAMP,1,'FT3','cubic foot','Volume','FTQ',1);
INSERT INTO UOM_SETTING (ID,CREATED_BY,CREATED_DATE,ACTIVE_IND,UOM,DESCRIPTION,UOM_GROUP,REMARK,VERSION) VALUES(19,'DBSCRIPT',CURRENT_TIMESTAMP,1,'YD3','cubic yard','Volume','YDQ',1);
INSERT INTO UOM_SETTING (ID,CREATED_BY,CREATED_DATE,ACTIVE_IND,UOM,DESCRIPTION,UOM_GROUP,REMARK,VERSION) VALUES(20,'DBSCRIPT',CURRENT_TIMESTAMP,1,'GAL','gallon (US)','Volume','GLL',1);
INSERT INTO UOM_SETTING (ID,CREATED_BY,CREATED_DATE,ACTIVE_IND,UOM,DESCRIPTION,UOM_GROUP,REMARK,VERSION) VALUES(21,'DBSCRIPT',CURRENT_TIMESTAMP,1,'BARREL','barrel (US)','Volume','BLL',1);
INSERT INTO UOM_SETTING (ID,CREATED_BY,CREATED_DATE,ACTIVE_IND,UOM,DESCRIPTION,UOM_GROUP,REMARK,VERSION) VALUES(22,'DBSCRIPT',CURRENT_TIMESTAMP,1,'RT','Revenue Ton','Volume','M70',1);
INSERT INTO UOM_SETTING (ID,CREATED_BY,CREATED_DATE,ACTIVE_IND,UOM,DESCRIPTION,UOM_GROUP,REMARK,VERSION) VALUES(23,'DBSCRIPT',CURRENT_TIMESTAMP,1,'BAG','Bag','Packages','BAG',1);
INSERT INTO UOM_SETTING (ID,CREATED_BY,CREATED_DATE,ACTIVE_IND,UOM,DESCRIPTION,UOM_GROUP,REMARK,VERSION) VALUES(24,'DBSCRIPT',CURRENT_TIMESTAMP,1,'BKT','Bucket','Packages','BKT',1);
INSERT INTO UOM_SETTING (ID,CREATED_BY,CREATED_DATE,ACTIVE_IND,UOM,DESCRIPTION,UOM_GROUP,REMARK,VERSION) VALUES(25,'DBSCRIPT',CURRENT_TIMESTAMP,1,'BND','Bundle','Packages','BND',1);
INSERT INTO UOM_SETTING (ID,CREATED_BY,CREATED_DATE,ACTIVE_IND,UOM,DESCRIPTION,UOM_GROUP,REMARK,VERSION) VALUES(26,'DBSCRIPT',CURRENT_TIMESTAMP,1,'BOWL','Bowl','Packages','BOWL',1);
INSERT INTO UOM_SETTING (ID,CREATED_BY,CREATED_DATE,ACTIVE_IND,UOM,DESCRIPTION,UOM_GROUP,REMARK,VERSION) VALUES(27,'DBSCRIPT',CURRENT_TIMESTAMP,1,'CS','Case','Packages','CS',1);
INSERT INTO UOM_SETTING (ID,CREATED_BY,CREATED_DATE,ACTIVE_IND,UOM,DESCRIPTION,UOM_GROUP,REMARK,VERSION) VALUES(28,'DBSCRIPT',CURRENT_TIMESTAMP,1,'CTN','Carton','Packages','CTN',1);
INSERT INTO UOM_SETTING (ID,CREATED_BY,CREATED_DATE,ACTIVE_IND,UOM,DESCRIPTION,UOM_GROUP,REMARK,VERSION) VALUES(29,'DBSCRIPT',CURRENT_TIMESTAMP,1,'DZ','Dozen','Packages','DZ',1);
INSERT INTO UOM_SETTING (ID,CREATED_BY,CREATED_DATE,ACTIVE_IND,UOM,DESCRIPTION,UOM_GROUP,REMARK,VERSION) VALUES(30,'DBSCRIPT',CURRENT_TIMESTAMP,1,'KIT','Kit','Packages','KIT',1);
INSERT INTO UOM_SETTING (ID,CREATED_BY,CREATED_DATE,ACTIVE_IND,UOM,DESCRIPTION,UOM_GROUP,REMARK,VERSION) VALUES(31,'DBSCRIPT',CURRENT_TIMESTAMP,1,'LOT','Lot','Packages','LOT',1);
INSERT INTO UOM_SETTING (ID,CREATED_BY,CREATED_DATE,ACTIVE_IND,UOM,DESCRIPTION,UOM_GROUP,REMARK,VERSION) VALUES(32,'DBSCRIPT',CURRENT_TIMESTAMP,1,'PK','Pack','Packages','PK',1);
INSERT INTO UOM_SETTING (ID,CREATED_BY,CREATED_DATE,ACTIVE_IND,UOM,DESCRIPTION,UOM_GROUP,REMARK,VERSION) VALUES(33,'DBSCRIPT',CURRENT_TIMESTAMP,1,'RL','Roll','Packages','RL',1);
INSERT INTO UOM_SETTING (ID,CREATED_BY,CREATED_DATE,ACTIVE_IND,UOM,DESCRIPTION,UOM_GROUP,REMARK,VERSION) VALUES(34,'DBSCRIPT',CURRENT_TIMESTAMP,1,'BBG','Bulk Bag','Packages','BBG',1);
INSERT INTO UOM_SETTING (ID,CREATED_BY,CREATED_DATE,ACTIVE_IND,UOM,DESCRIPTION,UOM_GROUP,REMARK,VERSION) VALUES(35,'DBSCRIPT',CURRENT_TIMESTAMP,1,'BOT','BOTTLE','Packages','BOT',1);
INSERT INTO UOM_SETTING (ID,CREATED_BY,CREATED_DATE,ACTIVE_IND,UOM,DESCRIPTION,UOM_GROUP,REMARK,VERSION) VALUES(36,'DBSCRIPT',CURRENT_TIMESTAMP,1,'BOX','BOX','Packages','BOX',1);
INSERT INTO UOM_SETTING (ID,CREATED_BY,CREATED_DATE,ACTIVE_IND,UOM,DESCRIPTION,UOM_GROUP,REMARK,VERSION) VALUES(37,'DBSCRIPT',CURRENT_TIMESTAMP,1,'CAN','CAN','Packages','CAN',1);
INSERT INTO UOM_SETTING (ID,CREATED_BY,CREATED_DATE,ACTIVE_IND,UOM,DESCRIPTION,UOM_GROUP,REMARK,VERSION) VALUES(38,'DBSCRIPT',CURRENT_TIMESTAMP,1,'CANISTER','CANISTER','Packages','CANISTER',1);
INSERT INTO UOM_SETTING (ID,CREATED_BY,CREATED_DATE,ACTIVE_IND,UOM,DESCRIPTION,UOM_GROUP,REMARK,VERSION) VALUES(39,'DBSCRIPT',CURRENT_TIMESTAMP,1,'CASE','Case','Packages','CASE',1);
INSERT INTO UOM_SETTING (ID,CREATED_BY,CREATED_DATE,ACTIVE_IND,UOM,DESCRIPTION,UOM_GROUP,REMARK,VERSION) VALUES(41,'DBSCRIPT',CURRENT_TIMESTAMP,1,'DRUM','DRUM','Packages','DRUM',1);
INSERT INTO UOM_SETTING (ID,CREATED_BY,CREATED_DATE,ACTIVE_IND,UOM,DESCRIPTION,UOM_GROUP,REMARK,VERSION) VALUES(42,'DBSCRIPT',CURRENT_TIMESTAMP,1,'EA','EA','Packages','EA',1);
INSERT INTO UOM_SETTING (ID,CREATED_BY,CREATED_DATE,ACTIVE_IND,UOM,DESCRIPTION,UOM_GROUP,REMARK,VERSION) VALUES(43,'DBSCRIPT',CURRENT_TIMESTAMP,1,'MPB','Multi-Ply Bag','Packages','MPB',1);
INSERT INTO UOM_SETTING (ID,CREATED_BY,CREATED_DATE,ACTIVE_IND,UOM,DESCRIPTION,UOM_GROUP,REMARK,VERSION) VALUES(44,'DBSCRIPT',CURRENT_TIMESTAMP,1,'PKG','PACKAGE','Packages','PKG',1);
INSERT INTO UOM_SETTING (ID,CREATED_BY,CREATED_DATE,ACTIVE_IND,UOM,DESCRIPTION,UOM_GROUP,REMARK,VERSION) VALUES(45,'DBSCRIPT',CURRENT_TIMESTAMP,1,'PCS','PIECE','Packages','PCS',1);
INSERT INTO UOM_SETTING (ID,CREATED_BY,CREATED_DATE,ACTIVE_IND,UOM,DESCRIPTION,UOM_GROUP,REMARK,VERSION) VALUES(46,'DBSCRIPT',CURRENT_TIMESTAMP,1,'PKT','Packet','Packages','PKT',1);
INSERT INTO UOM_SETTING (ID,CREATED_BY,CREATED_DATE,ACTIVE_IND,UOM,DESCRIPTION,UOM_GROUP,REMARK,VERSION) VALUES(47,'DBSCRIPT',CURRENT_TIMESTAMP,1,'PLT','PALLET','Packages','PLT',1);
INSERT INTO UOM_SETTING (ID,CREATED_BY,CREATED_DATE,ACTIVE_IND,UOM,DESCRIPTION,UOM_GROUP,REMARK,VERSION) VALUES(48,'DBSCRIPT',CURRENT_TIMESTAMP,1,'M','metre','Dimensions','MTR',1);
INSERT INTO UOM_SETTING (ID,CREATED_BY,CREATED_DATE,ACTIVE_IND,UOM,DESCRIPTION,UOM_GROUP,REMARK,VERSION) VALUES(49,'DBSCRIPT',CURRENT_TIMESTAMP,1,'DM','decimetre','Dimensions','DMT',1);
INSERT INTO UOM_SETTING (ID,CREATED_BY,CREATED_DATE,ACTIVE_IND,UOM,DESCRIPTION,UOM_GROUP,REMARK,VERSION) VALUES(50,'DBSCRIPT',CURRENT_TIMESTAMP,1,'CM','centimetre','Dimensions','CMT',1);
INSERT INTO UOM_SETTING (ID,CREATED_BY,CREATED_DATE,ACTIVE_IND,UOM,DESCRIPTION,UOM_GROUP,REMARK,VERSION) VALUES(51,'DBSCRIPT',CURRENT_TIMESTAMP,1,'MM','millimetre','Dimensions','MMT',1);
INSERT INTO UOM_SETTING (ID,CREATED_BY,CREATED_DATE,ACTIVE_IND,UOM,DESCRIPTION,UOM_GROUP,REMARK,VERSION) VALUES(52,'DBSCRIPT',CURRENT_TIMESTAMP,1,'KM','kilometre','Dimensions','KMT',1);
INSERT INTO UOM_SETTING (ID,CREATED_BY,CREATED_DATE,ACTIVE_IND,UOM,DESCRIPTION,UOM_GROUP,REMARK,VERSION) VALUES(53,'DBSCRIPT',CURRENT_TIMESTAMP,1,'IN','inch','Dimensions','INH',1);
INSERT INTO UOM_SETTING (ID,CREATED_BY,CREATED_DATE,ACTIVE_IND,UOM,DESCRIPTION,UOM_GROUP,REMARK,VERSION) VALUES(54,'DBSCRIPT',CURRENT_TIMESTAMP,1,'FT','foot','Dimensions','FOT',1);
INSERT INTO UOM_SETTING (ID,CREATED_BY,CREATED_DATE,ACTIVE_IND,UOM,DESCRIPTION,UOM_GROUP,REMARK,VERSION) VALUES(55,'DBSCRIPT',CURRENT_TIMESTAMP,1,'YD','yard','Dimensions','YRD',1);
/*!40000 ALTER TABLE `UOM_SETTING` ENABLE KEYS */;
UNLOCK TABLES;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2020-05-14 12:23:27

INSERT INTO `CONFIG_CODE` VALUES (1, 1, 'SYSADMIN', CURRENT_TIMESTAMP, null, null, 'TPT_REQUEST_SEQ_GEN', 'TPT_REQUEST_SEQ_GEN', 0, 0, 'TENANT', 1);

LOCK TABLES `COMMON_TAG` WRITE;
/*!40000 ALTER TABLE `common_tag` DISABLE KEYS */;
INSERT INTO `COMMON_TAG` VALUES (1,1,'SYSADMIN',NULL,NULL,NULL,NULL,NULL,'SOURCE','ReasonCodeCategory',0,NULL,0);
INSERT INTO `COMMON_TAG` VALUES (2,1,'SYSADMIN',NULL,NULL,NULL,NULL,NULL,'TRANSIT','ReasonCodeCategory',0,NULL,0);
INSERT INTO `COMMON_TAG` VALUES (3,1,'SYSADMIN',NULL,NULL,NULL,NULL,NULL,'DESTINATION','ReasonCodeCategory',0,NULL,0);
INSERT INTO `COMMON_TAG` VALUES (4,1,'SYSADMIN',NULL,NULL,NULL,NULL,NULL,'OTHERS','ReasonCodeCategory',0,NULL,0);
INSERT INTO `COMMON_TAG` VALUES (5,1,'SYSADMIN',NULL,NULL,NULL,NULL,NULL,'SOURCE','MilestoneCategory',0,NULL,0);
INSERT INTO `COMMON_TAG` VALUES (6,1,'SYSADMIN',NULL,NULL,NULL,NULL,NULL,'TRANSIT','MilestoneCategory',0,NULL,0);
INSERT INTO `COMMON_TAG` VALUES (7,1,'SYSADMIN',NULL,NULL,NULL,NULL,NULL,'DESTINATION','MilestoneCategory',0,NULL,0);
INSERT INTO `COMMON_TAG` VALUES (8,1,'SYSADMIN',NULL,NULL,NULL,NULL,NULL,'OTHERS','MilestoneCategory',0,NULL,0);
INSERT INTO `COMMON_TAG` VALUES (9,1,'SYSADMIN',NULL,NULL,NULL,NULL,NULL,'PARTIAL','ReasonCodeUsage',0,NULL,0);
INSERT INTO `COMMON_TAG` VALUES (10,1,'SYSADMIN',NULL,NULL,NULL,NULL,NULL,'FAILED','ReasonCodeUsage',0,NULL,0);
INSERT INTO `COMMON_TAG` VALUES (11,1,'SYSADMIN',NULL,NULL,NULL,NULL,NULL,'DELIVERY','ReasonCodeCategory',0,NULL,0);
INSERT INTO `COMMON_TAG` VALUES (12,1,'SYSADMIN',NULL,NULL,NULL,NULL,NULL,'COLLECTION','ReasonCodeCategory',0,NULL,0);
INSERT INTO `COMMON_TAG` VALUES (13,1,'SYSADMIN',NULL,NULL,NULL,NULL,NULL,'COMPLETE','ReasonCodeUsage',0,NULL,0);
/*!40000 ALTER TABLE `COMMON_TAG` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

ALTER TABLE DRIVER ADD transporter_id bigint(20) NULL;
ALTER TABLE DRIVER ADD INDEX `FK_DRIVER_PARTNERS_idx` (`transporter_id` ASC) ;
ALTER TABLE DRIVER ADD CONSTRAINT `FK_DRIVER_PARTNERS` FOREIGN KEY (`transporter_id`) REFERENCES PARTNERS (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

ALTER TABLE VEHICLE ADD transporter_id bigint(20) NULL;
ALTER TABLE VEHICLE ADD INDEX `FK_VEHICLE_PARTNERS_idx` (`transporter_id` ASC) ;
ALTER TABLE VEHICLE ADD CONSTRAINT `FK_VEHICLE_PARTNERS` FOREIGN KEY (`transporter_id`) REFERENCES PARTNERS (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION;

insert into seq_common_tag values (14);
insert into seq_config_code values (2);
insert into seq_driver values (1);
insert into seq_equipment values (1);
insert into seq_location values (1);
insert into seq_lookup values (76);
insert into seq_milestone values (1);
insert into seq_milkrun_trip values (1);
insert into seq_milkrun_vehicle values (1);
insert into seq_operation_log values (1);
insert into seq_partner_config values (1);
insert into seq_partner_location values (1);
insert into seq_partner_types values (1);
insert into seq_partners values (1);
insert into seq_reason_code values (1);
insert into seq_uom_setting values (56);
insert into seq_vehicle values (56);
