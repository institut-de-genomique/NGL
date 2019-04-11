-- MySQL dump 10.13  Distrib 5.6.13, for linux-glibc2.5 (x86_64)
--
-- Host: mysqldev.genoscope.cns.fr    Database: NGL_6
-- ------------------------------------------------------
-- Server version	5.6.13-enterprise-commercial-advanced-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `analysis_type`
--

DROP TABLE IF EXISTS `analysis_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `analysis_type` (
  `id` bigint(20) NOT NULL,
  `fk_common_info_type` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `readset_type_ibfk_1` (`fk_common_info_type`),
  CONSTRAINT `readset_type_ibfk_100` FOREIGN KEY (`fk_common_info_type`) REFERENCES `common_info_type` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `application`
--

DROP TABLE IF EXISTS `application`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `application` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `label` varchar(255) DEFAULT NULL,
  `code` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `common_info_type`
--

DROP TABLE IF EXISTS `common_info_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `common_info_type` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `code` varchar(255) NOT NULL,
  `display_order` int(11) DEFAULT NULL,
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `fk_object_type` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`),
  KEY `fk_object_type` (`fk_object_type`),
  KEY `idx_common_code` (`code`),
  CONSTRAINT `common_info_type_ibfk_1` FOREIGN KEY (`fk_object_type`) REFERENCES `object_type` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=34123 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `common_info_type_institute`
--

DROP TABLE IF EXISTS `common_info_type_institute`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `common_info_type_institute` (
  `fk_institute` int(11) NOT NULL,
  `fk_common_info_type` bigint(20) NOT NULL,
  PRIMARY KEY (`fk_institute`,`fk_common_info_type`),
  KEY `fk_institute_common_info_type_fk1_idx` (`fk_institute`),
  KEY `fk_institute_common_info_type_fk2_idx` (`fk_common_info_type`),
  CONSTRAINT `fk_institute_common_info_type_fk1` FOREIGN KEY (`fk_institute`) REFERENCES `institute` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_institute_common_info_type_fk2` FOREIGN KEY (`fk_common_info_type`) REFERENCES `common_info_type` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `common_info_type_state`
--

DROP TABLE IF EXISTS `common_info_type_state`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `common_info_type_state` (
  `fk_common_info_type` bigint(20) NOT NULL,
  `fk_state` bigint(20) NOT NULL,
  PRIMARY KEY (`fk_common_info_type`,`fk_state`),
  KEY `fk_state` (`fk_state`),
  KEY `fk_common_info_type` (`fk_common_info_type`),
  CONSTRAINT `common_info_type_state_ibfk_1` FOREIGN KEY (`fk_common_info_type`) REFERENCES `common_info_type` (`id`),
  CONSTRAINT `common_info_type_state_ibfk_2` FOREIGN KEY (`fk_state`) REFERENCES `state` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `container_category`
--

DROP TABLE IF EXISTS `container_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `container_category` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `code` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`),
  KEY `idx_container_category_code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=1909 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `container_support_category`
--

DROP TABLE IF EXISTS `container_support_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `container_support_category` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `code` varchar(255) NOT NULL,
  `nbUsableContainer` int(11) NOT NULL,
  `nbLine` int(11) DEFAULT NULL,
  `nbColumn` int(11) DEFAULT NULL,
  `fk_container_category` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`),
  KEY `idx_container_support_category_code` (`code`),
  KEY `fk_container_category_idx` (`fk_container_category`),
  CONSTRAINT `fk_container_category` FOREIGN KEY (`fk_container_category`) REFERENCES `container_category` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=2929 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `experiment_category`
--

DROP TABLE IF EXISTS `experiment_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `experiment_category` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `code` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`),
  KEY `idx_exp_category_code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=1172 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `experiment_type`
--

DROP TABLE IF EXISTS `experiment_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `experiment_type` (
  `id` bigint(20) NOT NULL,
  `fk_common_info_type` bigint(20) NOT NULL,
  `fk_experiment_category` bigint(20) NOT NULL,
  `atomic_transfert_method` varchar(255) DEFAULT NULL,
  `short_code` varchar(10) DEFAULT NULL,
  `new_sample` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `fk_experiment_category` (`fk_experiment_category`),
  KEY `fk_common_info_type` (`fk_common_info_type`),
  CONSTRAINT `experiment_type_ibfk_1` FOREIGN KEY (`fk_experiment_category`) REFERENCES `experiment_category` (`id`),
  CONSTRAINT `experiment_type_ibfk_2` FOREIGN KEY (`fk_common_info_type`) REFERENCES `common_info_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `experiment_type_instrument_type`
--

DROP TABLE IF EXISTS `experiment_type_instrument_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `experiment_type_instrument_type` (
  `fk_experiment_type` bigint(20) NOT NULL,
  `fk_instrument_used_type` bigint(20) NOT NULL,
  PRIMARY KEY (`fk_experiment_type`,`fk_instrument_used_type`),
  KEY `fk_instrument_type` (`fk_instrument_used_type`),
  KEY `common_info_type_instrument_type_ibfk_1_idx` (`fk_experiment_type`),
  CONSTRAINT `common_info_type_instrument_type_ibfk_1` FOREIGN KEY (`fk_experiment_type`) REFERENCES `experiment_type` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `common_info_type_instrument_type_ibfk_2` FOREIGN KEY (`fk_instrument_used_type`) REFERENCES `instrument_used_type` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `experiment_type_node`
--

DROP TABLE IF EXISTS `experiment_type_node`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `experiment_type_node` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(255) NOT NULL,
  `doPurification` tinyint(1) NOT NULL DEFAULT '0',
  `mandatoryPurification` tinyint(1) NOT NULL DEFAULT '0',
  `doQualityControl` tinyint(1) NOT NULL DEFAULT '0',
  `mandatoryQualityControl` tinyint(1) NOT NULL DEFAULT '0',
  `doTransfert` tinyint(1) NOT NULL DEFAULT '0',
  `mandatoryTransfert` tinyint(1) NOT NULL DEFAULT '0',
  `fk_experiment_type` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code_UNIQUE` (`code`),
  KEY `fk_experiment_type` (`fk_experiment_type`),
  CONSTRAINT `experiment_type_node_ibfk_1` FOREIGN KEY (`fk_experiment_type`) REFERENCES `experiment_type` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8486 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `experiment_type_sample_type`
--

DROP TABLE IF EXISTS `experiment_type_sample_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `experiment_type_sample_type` (
  `fk_experiment_type` bigint(20) NOT NULL,
  `fk_sample_type` bigint(20) NOT NULL,
  PRIMARY KEY (`fk_experiment_type`,`fk_sample_type`),
  KEY `sample_type_fk2_idx` (`fk_sample_type`),
  CONSTRAINT `experiment_type_fk1` FOREIGN KEY (`fk_experiment_type`) REFERENCES `experiment_type` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `sample_type_fk2` FOREIGN KEY (`fk_sample_type`) REFERENCES `sample_type` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `import_category`
--

DROP TABLE IF EXISTS `import_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `import_category` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `code` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`),
  KEY `idx_import_category_code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=235 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `import_type`
--

DROP TABLE IF EXISTS `import_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `import_type` (
  `id` bigint(20) NOT NULL,
  `fk_common_info_type` bigint(20) NOT NULL,
  `fk_import_category` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_common_info_type` (`fk_common_info_type`),
  KEY `fk_import_category` (`fk_import_category`),
  CONSTRAINT `import_type_ibfk_1` FOREIGN KEY (`fk_common_info_type`) REFERENCES `common_info_type` (`id`),
  CONSTRAINT `import_type_ibfk_2` FOREIGN KEY (`fk_import_category`) REFERENCES `import_category` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `institute`
--

DROP TABLE IF EXISTS `institute`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `institute` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `code` varchar(10) NOT NULL,
  `name` varchar(45) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code_UNIQUE` (`code`),
  UNIQUE KEY `name_UNIQUE` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `instrument`
--

DROP TABLE IF EXISTS `instrument`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `instrument` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `short_name` varchar(30) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `code` varchar(255) NOT NULL,
  `fk_instrument_used_type` bigint(20) DEFAULT NULL,
  `active` tinyint(1) NOT NULL,
  `path` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `instrument_used_type_id` (`fk_instrument_used_type`),
  KEY `idx_instrument_code` (`code`),
  CONSTRAINT `instrument_ibfk_1` FOREIGN KEY (`fk_instrument_used_type`) REFERENCES `instrument_used_type` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3962 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `instrument_category`
--

DROP TABLE IF EXISTS `instrument_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `instrument_category` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `code` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`),
  KEY `idx_inst_category_code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=4028 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `instrument_institute`
--

DROP TABLE IF EXISTS `instrument_institute`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `instrument_institute` (
  `fk_instrument` bigint(20) NOT NULL,
  `fk_institute` int(11) NOT NULL,
  PRIMARY KEY (`fk_instrument`,`fk_institute`),
  KEY `fk_instrument_fk1` (`fk_instrument`),
  KEY `fk_institute_fk2` (`fk_institute`),
  CONSTRAINT `instrument_institute_ibfk_1` FOREIGN KEY (`fk_instrument`) REFERENCES `instrument` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `instrument_institute_ibfk_2` FOREIGN KEY (`fk_institute`) REFERENCES `institute` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `instrument_used_type`
--

DROP TABLE IF EXISTS `instrument_used_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `instrument_used_type` (
  `id` bigint(20) NOT NULL,
  `fk_common_info_type` bigint(20) NOT NULL,
  `fk_instrument_category` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_common_info_type` (`fk_common_info_type`),
  KEY `fk_instrument_category` (`fk_instrument_category`),
  CONSTRAINT `instrument_used_type_ibfk_1` FOREIGN KEY (`fk_common_info_type`) REFERENCES `common_info_type` (`id`),
  CONSTRAINT `instrument_used_type_ibfk_2` FOREIGN KEY (`fk_instrument_category`) REFERENCES `instrument_category` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `instrument_ut_in_container_support_cat`
--

DROP TABLE IF EXISTS `instrument_ut_in_container_support_cat`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `instrument_ut_in_container_support_cat` (
  `fk_instrument_used_type` bigint(20) NOT NULL,
  `fk_container_support_category` bigint(20) NOT NULL,
  PRIMARY KEY (`fk_instrument_used_type`,`fk_container_support_category`),
  KEY `fk_container_support_category` (`fk_container_support_category`),
  CONSTRAINT `instrumentCategory_inContainerSupportCategory_ibfk_1` FOREIGN KEY (`fk_instrument_used_type`) REFERENCES `instrument_used_type` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `instrumentCategory_inContainerSupportCategory_ibfk_2` FOREIGN KEY (`fk_container_support_category`) REFERENCES `container_support_category` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `instrument_ut_out_container_support_cat`
--

DROP TABLE IF EXISTS `instrument_ut_out_container_support_cat`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `instrument_ut_out_container_support_cat` (
  `fk_instrument_used_type` bigint(20) NOT NULL,
  `fk_container_support_category` bigint(20) NOT NULL,
  PRIMARY KEY (`fk_instrument_used_type`,`fk_container_support_category`),
  KEY `fk_container_support_category` (`fk_container_support_category`),
  CONSTRAINT `instrumentCategory_outContainerSupportCategory_ibfk_1` FOREIGN KEY (`fk_instrument_used_type`) REFERENCES `instrument_used_type` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `instrumentCategory_outContainerSupportCategory_ibfk_2` FOREIGN KEY (`fk_container_support_category`) REFERENCES `container_support_category` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `level`
--

DROP TABLE IF EXISTS `level`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `level` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(45) DEFAULT NULL,
  `name` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code_UNIQUE` (`code`),
  UNIQUE KEY `name_UNIQUE` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=80 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `measure_category`
--

DROP TABLE IF EXISTS `measure_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `measure_category` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `code` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`),
  KEY `idx_measure_category_code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `measure_unit`
--

DROP TABLE IF EXISTS `measure_unit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `measure_unit` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(255) NOT NULL,
  `value` varchar(255) NOT NULL,
  `default_unit` tinyint(1) NOT NULL DEFAULT '0',
  `fk_measure_category` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`),
  KEY `measure_category_id` (`fk_measure_category`),
  KEY `idx_measure_value_code` (`code`),
  CONSTRAINT `measure_value_ibfk_1` FOREIGN KEY (`fk_measure_category`) REFERENCES `measure_category` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=55 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `object_type`
--

DROP TABLE IF EXISTS `object_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `object_type` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(255) NOT NULL,
  `generic` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`),
  KEY `idx_object_type_code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=45 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `permission`
--

DROP TABLE IF EXISTS `permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `permission` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `label` varchar(255) NOT NULL,
  `code` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code_UNIQUE` (`code`),
  KEY `idx_permission_code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `previous_nodes`
--

DROP TABLE IF EXISTS `previous_nodes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `previous_nodes` (
  `fk_previous_node` bigint(20) NOT NULL,
  `fk_node` bigint(20) NOT NULL,
  PRIMARY KEY (`fk_previous_node`,`fk_node`),
  KEY `fk_node` (`fk_node`),
  CONSTRAINT `previous_nodes_ibfk_1` FOREIGN KEY (`fk_previous_node`) REFERENCES `experiment_type_node` (`id`),
  CONSTRAINT `previous_nodes_ibfk_2` FOREIGN KEY (`fk_node`) REFERENCES `experiment_type_node` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `process_category`
--

DROP TABLE IF EXISTS `process_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `process_category` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `code` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`),
  KEY `idx_process_category_code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=1070 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `process_experiment_type`
--

DROP TABLE IF EXISTS `process_experiment_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `process_experiment_type` (
  `fk_process_type` bigint(20) NOT NULL,
  `fk_experiment_type` bigint(20) NOT NULL,
  `position_in_process` int(2) NOT NULL,
  PRIMARY KEY (`fk_process_type`,`fk_experiment_type`,`position_in_process`),
  KEY `fk_experiment_type` (`fk_experiment_type`),
  CONSTRAINT `process_experiment_type_ibfk_1` FOREIGN KEY (`fk_process_type`) REFERENCES `process_type` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `process_experiment_type_ibfk_2` FOREIGN KEY (`fk_experiment_type`) REFERENCES `experiment_type` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `process_type`
--

DROP TABLE IF EXISTS `process_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `process_type` (
  `id` bigint(20) NOT NULL,
  `fk_common_info_type` bigint(20) NOT NULL,
  `fk_process_category` bigint(20) NOT NULL,
  `fk_void_experiment_type` bigint(20) NOT NULL,
  `fk_first_experiment_type` bigint(20) NOT NULL,
  `fk_last_experiment_type` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_common_info_type` (`fk_common_info_type`),
  KEY `fk_process_category` (`fk_process_category`),
  KEY `fk_void_experiment_type` (`fk_void_experiment_type`),
  KEY `fk_first_experiment_type` (`fk_first_experiment_type`),
  KEY `fk_last_experiment_type` (`fk_last_experiment_type`),
  CONSTRAINT `process_type_ibfk_1` FOREIGN KEY (`fk_common_info_type`) REFERENCES `common_info_type` (`id`),
  CONSTRAINT `process_type_ibfk_2` FOREIGN KEY (`fk_process_category`) REFERENCES `process_category` (`id`),
  CONSTRAINT `process_type_ibfk_3` FOREIGN KEY (`fk_void_experiment_type`) REFERENCES `experiment_type` (`id`),
  CONSTRAINT `process_type_ibfk_4` FOREIGN KEY (`fk_first_experiment_type`) REFERENCES `experiment_type` (`id`),
  CONSTRAINT `process_type_ibfk_5` FOREIGN KEY (`fk_last_experiment_type`) REFERENCES `experiment_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `project_category`
--

DROP TABLE IF EXISTS `project_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `project_category` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `code` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`),
  KEY `idx_project_category_code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=106 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `project_type`
--

DROP TABLE IF EXISTS `project_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `project_type` (
  `id` bigint(20) NOT NULL,
  `fk_common_info_type` bigint(20) NOT NULL,
  `fk_project_category` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_common_info_type` (`fk_common_info_type`),
  KEY `fk_project_category` (`fk_project_category`),
  CONSTRAINT `project_type_ibfk_1` FOREIGN KEY (`fk_common_info_type`) REFERENCES `common_info_type` (`id`),
  CONSTRAINT `project_type_ibfk_2` FOREIGN KEY (`fk_project_category`) REFERENCES `project_category` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `property_definition`
--

DROP TABLE IF EXISTS `property_definition`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `property_definition` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `required` tinyint(1) NOT NULL DEFAULT '0',
  `required_state` varchar(10) DEFAULT NULL,
  `editable` tinyint(1) NOT NULL DEFAULT '0',
  `active` tinyint(1) NOT NULL DEFAULT '0',
  `type` varchar(255) NOT NULL,
  `display_format` varchar(255) DEFAULT NULL,
  `display_order` int(2) DEFAULT NULL,
  `default_value` varchar(255) DEFAULT NULL,
  `description` text,
  `choice_in_list` tinyint(1) NOT NULL DEFAULT '0',
  `property_value_type` varchar(15) NOT NULL DEFAULT 'single',
  `fk_measure_category` bigint(20) DEFAULT NULL,
  `fk_save_measure_unit` bigint(20) DEFAULT NULL,
  `fk_display_measure_unit` bigint(20) DEFAULT NULL,
  `fk_common_info_type` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code_type` (`code`,`fk_common_info_type`),
  KEY `measure_category_id` (`fk_measure_category`),
  KEY `measure_value_id` (`fk_save_measure_unit`),
  KEY `display_measure_value_id` (`fk_display_measure_unit`),
  KEY `common_info_type_id` (`fk_common_info_type`),
  KEY `idx_property_def_code` (`code`),
  CONSTRAINT `property_definition_ibfk_1` FOREIGN KEY (`fk_measure_category`) REFERENCES `measure_category` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `property_definition_ibfk_2` FOREIGN KEY (`fk_save_measure_unit`) REFERENCES `measure_unit` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `property_definition_ibfk_3` FOREIGN KEY (`fk_display_measure_unit`) REFERENCES `measure_unit` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `property_definition_ibfk_4` FOREIGN KEY (`fk_common_info_type`) REFERENCES `common_info_type` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=138294 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `property_definition_level`
--

DROP TABLE IF EXISTS `property_definition_level`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `property_definition_level` (
  `fk_property_definition` bigint(20) NOT NULL,
  `fk_level` bigint(20) NOT NULL,
  PRIMARY KEY (`fk_property_definition`,`fk_level`),
  KEY `fk_level` (`fk_level`),
  CONSTRAINT `property_definition_level_ibfk_1` FOREIGN KEY (`fk_property_definition`) REFERENCES `property_definition` (`id`),
  CONSTRAINT `property_definition_level_ibfk_2` FOREIGN KEY (`fk_level`) REFERENCES `level` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `protocol_category`
--

DROP TABLE IF EXISTS `protocol_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `protocol_category` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `code` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`),
  KEY `idx_protocol_category_code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=126 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `readset_type`
--

DROP TABLE IF EXISTS `readset_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `readset_type` (
  `id` bigint(20) NOT NULL,
  `fk_common_info_type` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `readset_type_ibfk_1` (`fk_common_info_type`),
  CONSTRAINT `readset_type_ibfk_10` FOREIGN KEY (`fk_common_info_type`) REFERENCES `common_info_type` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `role`
--

DROP TABLE IF EXISTS `role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `role` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `label` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `label_UNIQUE` (`label`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `role_permission`
--

DROP TABLE IF EXISTS `role_permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `role_permission` (
  `role_id` int(11) NOT NULL,
  `permission_id` int(11) NOT NULL,
  PRIMARY KEY (`role_id`,`permission_id`),
  KEY `permission_id` (`permission_id`),
  CONSTRAINT `role_permission_ibfk_1` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`),
  CONSTRAINT `role_permission_ibfk_2` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `run_category`
--

DROP TABLE IF EXISTS `run_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `run_category` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `code` varchar(45) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code_UNIQUE` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=324 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `run_type`
--

DROP TABLE IF EXISTS `run_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `run_type` (
  `id` bigint(20) NOT NULL,
  `nb_lanes` int(11) NOT NULL,
  `fk_common_info_type` bigint(20) NOT NULL,
  `fk_run_category` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `run_type_ibfk_1_idx` (`fk_common_info_type`),
  KEY `run_type_ibfk_2_idx` (`fk_run_category`),
  CONSTRAINT `run_type_ibfk_1` FOREIGN KEY (`fk_common_info_type`) REFERENCES `common_info_type` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `run_type_ibfk_2` FOREIGN KEY (`fk_run_category`) REFERENCES `run_category` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sample_category`
--

DROP TABLE IF EXISTS `sample_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sample_category` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `code` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`),
  KEY `idx_sample_category_code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=2850 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sample_type`
--

DROP TABLE IF EXISTS `sample_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sample_type` (
  `id` bigint(20) NOT NULL,
  `fk_common_info_type` bigint(20) NOT NULL,
  `fk_sample_category` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_common_info_type` (`fk_common_info_type`),
  KEY `fk_sample_category` (`fk_sample_category`),
  CONSTRAINT `sample_type_ibfk_1` FOREIGN KEY (`fk_common_info_type`) REFERENCES `common_info_type` (`id`),
  CONSTRAINT `sample_type_ibfk_2` FOREIGN KEY (`fk_sample_category`) REFERENCES `sample_category` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `satellite_experiment_type`
--

DROP TABLE IF EXISTS `satellite_experiment_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `satellite_experiment_type` (
  `fk_experiment_type` bigint(20) NOT NULL,
  `fk_experiment_type_node` bigint(20) NOT NULL,
  PRIMARY KEY (`fk_experiment_type`,`fk_experiment_type_node`),
  KEY `fk_node_idx` (`fk_experiment_type_node`),
  KEY `fk_experiment_type_idx` (`fk_experiment_type`),
  CONSTRAINT `fk_experiment_type` FOREIGN KEY (`fk_experiment_type`) REFERENCES `experiment_type` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_node` FOREIGN KEY (`fk_experiment_type_node`) REFERENCES `experiment_type_node` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `state`
--

DROP TABLE IF EXISTS `state`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `state` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `code` varchar(255) NOT NULL,
  `active` tinyint(1) NOT NULL DEFAULT '0',
  `position` int(11) NOT NULL,
  `fk_state_category` bigint(20) NOT NULL,
  `display` tinyint(1) DEFAULT NULL,
  `functionnal_group` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code_UNIQUE` (`code`),
  KEY `idx_state_code` (`code`),
  KEY `state_ibfk_1_idx` (`fk_state_category`),
  CONSTRAINT `state_ibfk_1` FOREIGN KEY (`fk_state_category`) REFERENCES `state_category` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=4843 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `state_category`
--

DROP TABLE IF EXISTS `state_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `state_category` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `code` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`),
  KEY `idx_state_category_code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=597 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `state_object_type`
--

DROP TABLE IF EXISTS `state_object_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `state_object_type` (
  `fk_state` bigint(20) NOT NULL,
  `fk_object_type` bigint(20) NOT NULL,
  PRIMARY KEY (`fk_state`,`fk_object_type`),
  KEY `fk_state_object_type_fk1_idx` (`fk_state`),
  KEY `fk_state_object_type_fk2_idx` (`fk_object_type`),
  CONSTRAINT `fk_state_object_type_fk1` FOREIGN KEY (`fk_state`) REFERENCES `state` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_state_object_type_fk2` FOREIGN KEY (`fk_object_type`) REFERENCES `object_type` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `state_object_type_hierarchy`
--

DROP TABLE IF EXISTS `state_object_type_hierarchy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `state_object_type_hierarchy` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(255) NOT NULL,
  `fk_child_state` bigint(20) NOT NULL,
  `fk_object_type` bigint(20) NOT NULL,
  `fk_parent_state` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code_UNIQUE` (`code`),
  KEY `code_compose` (`fk_child_state`,`fk_object_type`),
  KEY `state2_idx` (`fk_parent_state`),
  KEY `object_type_idx` (`fk_object_type`),
  CONSTRAINT `object_type` FOREIGN KEY (`fk_object_type`) REFERENCES `object_type` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `state1` FOREIGN KEY (`fk_child_state`) REFERENCES `state` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `state2` FOREIGN KEY (`fk_parent_state`) REFERENCES `state` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=2430 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `team`
--

DROP TABLE IF EXISTS `team`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `team` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `nom` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `treatment_category`
--

DROP TABLE IF EXISTS `treatment_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `treatment_category` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `code` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`),
  KEY `idx_project_category_code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=703 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `treatment_context`
--

DROP TABLE IF EXISTS `treatment_context`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `treatment_context` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `code` varchar(45) NOT NULL,
  `name` varchar(45) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code_UNIQUE` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=756 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `treatment_type`
--

DROP TABLE IF EXISTS `treatment_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `treatment_type` (
  `id` bigint(20) NOT NULL,
  `names` varchar(100) DEFAULT NULL,
  `fk_common_info_type` bigint(20) NOT NULL,
  `fk_treatment_category` bigint(20) NOT NULL,
  `display_orders` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_common_info_type` (`fk_common_info_type`),
  KEY `fk_treatment_category` (`fk_treatment_category`),
  CONSTRAINT `treatment_type_ibfk_1` FOREIGN KEY (`fk_common_info_type`) REFERENCES `common_info_type` (`id`),
  CONSTRAINT `treatment_type_ibfk_2` FOREIGN KEY (`fk_treatment_category`) REFERENCES `treatment_category` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `treatment_type_context`
--

DROP TABLE IF EXISTS `treatment_type_context`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `treatment_type_context` (
  `fk_treatment_type` bigint(20) NOT NULL,
  `fk_treatment_context` int(11) NOT NULL,
  `required` tinyint(1) NOT NULL,
  PRIMARY KEY (`fk_treatment_type`,`fk_treatment_context`),
  KEY `fk_treatment_type_context_fk1_idx` (`fk_treatment_type`),
  KEY `fk_treatment_type_context_fk2_idx` (`fk_treatment_context`),
  CONSTRAINT `fk_treatment_type_context_fk1` FOREIGN KEY (`fk_treatment_type`) REFERENCES `treatment_type` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_treatment_type_context_fk2` FOREIGN KEY (`fk_treatment_context`) REFERENCES `treatment_context` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `login` varchar(255) DEFAULT NULL,
  `firstname` varchar(255) DEFAULT NULL,
  `lastname` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `technicaluser` smallint(6) DEFAULT '0',
  `password` varchar(255) DEFAULT NULL,
  `active` tinyint(1) DEFAULT '1',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=197 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_application`
--

DROP TABLE IF EXISTS `user_application`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_application` (
  `user_id` int(11) NOT NULL,
  `application_id` int(11) NOT NULL,
  PRIMARY KEY (`user_id`,`application_id`),
  KEY `fk_user_application_application_02_idx` (`application_id`),
  CONSTRAINT `fk_user_application_application_02` FOREIGN KEY (`application_id`) REFERENCES `application` (`id`),
  CONSTRAINT `fk_user_application_user_01` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_role`
--

DROP TABLE IF EXISTS `user_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_role` (
  `user_id` int(11) NOT NULL,
  `role_id` int(11) NOT NULL,
  PRIMARY KEY (`user_id`,`role_id`),
  KEY `role_id` (`role_id`),
  CONSTRAINT `user_role_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `user_role_ibfk_2` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_team`
--

DROP TABLE IF EXISTS `user_team`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_team` (
  `user_id` int(11) NOT NULL,
  `team_id` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`user_id`,`team_id`),
  KEY `team_id` (`team_id`),
  CONSTRAINT `user_team_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `user_team_ibfk_2` FOREIGN KEY (`team_id`) REFERENCES `team` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `value`
--

DROP TABLE IF EXISTS `value`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `value` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(255) NOT NULL,
  `value` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `default_value` tinyint(1) NOT NULL DEFAULT '0',
  `fk_property_definition` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `property_definition_id` (`fk_property_definition`),
  CONSTRAINT `value_ibfk_1` FOREIGN KEY (`fk_property_definition`) REFERENCES `property_definition` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=1456290 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2018-06-01 11:08:19
