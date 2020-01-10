-- MySQL dump 10.13  Distrib 5.1.45, for redhat-linux-gnu (i386)
--
-- Host: mysql    Database: NGL
-- ------------------------------------------------------
-- Server version	5.0.51a-community-log

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
-- Not dumping tablespaces as no INFORMATION_SCHEMA.FILES table on this server
--

--
-- Current Database: `NGL`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `NGL` /*!40100 DEFAULT CHARACTER SET latin1 */;

USE `NGL`;

--
-- Table structure for table `common_info_type`
--

DROP TABLE IF EXISTS `common_info_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `common_info_type` (
  `id` bigint(20) NOT NULL auto_increment,
  `name` varchar(255) NOT NULL,
  `code` varchar(255) NOT NULL,
  `collection_name` varchar(255) NOT NULL,
  `fk_object_type` bigint(20) NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `uq_common_info_type_type_name` (`name`),
  UNIQUE KEY `uq_common_info_type_type_code` (`code`),
  UNIQUE KEY `uq_common_info_type_name` (`name`),
  UNIQUE KEY `uq_common_info_type_code` (`code`),
  KEY `ix_common_info_type_objectType_1` (`fk_object_type`)
) ENGINE=MyISAM AUTO_INCREMENT=47 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `common_info_type_instrument_type`
--

DROP TABLE IF EXISTS `common_info_type_instrument_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `common_info_type_instrument_type` (
  `fk_common_info_type` bigint(20) NOT NULL,
  `fk_instrument_type` bigint(20) NOT NULL,
  PRIMARY KEY  (`fk_common_info_type`,`fk_instrument_type`),
  KEY `fk_instrument_type` (`fk_instrument_type`),
  KEY `fk_common_info_type` (`fk_common_info_type`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `common_info_type_protocol`
--

DROP TABLE IF EXISTS `common_info_type_protocol`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `common_info_type_protocol` (
  `fk_common_info_type` bigint(20) NOT NULL,
  `fk_protocol` bigint(20) NOT NULL,
  PRIMARY KEY  (`fk_common_info_type`,`fk_protocol`),
  KEY `fk_protocol` (`fk_protocol`),
  KEY `fk_common_info_type` (`fk_common_info_type`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `common_info_type_resolution`
--

DROP TABLE IF EXISTS `common_info_type_resolution`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `common_info_type_resolution` (
  `fk_common_info_type` bigint(20) NOT NULL,
  `fk_resolution` bigint(20) NOT NULL,
  PRIMARY KEY  (`fk_common_info_type`,`fk_resolution`),
  KEY `fk_common_info_type_resolution__02` (`fk_resolution`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
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
  PRIMARY KEY  (`fk_common_info_type`,`fk_state`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `container_category`
--

DROP TABLE IF EXISTS `container_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `container_category` (
  `id` bigint(20) NOT NULL auto_increment,
  `name` varchar(255) NOT NULL,
  `code` varchar(255) NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `uq_container_category_code` (`code`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `container_support_category`
--

DROP TABLE IF EXISTS `container_support_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `container_support_category` (
  `id` bigint(20) NOT NULL auto_increment,
  `name` varchar(255) NOT NULL,
  `code` varchar(255) NOT NULL,
  `nbUsableContainer` int(11) NOT NULL,
  `nbLine` int(11) default NULL,
  `nbColumn` int(11) default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `uq_container_support_category_co` (`code`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `equipe`
--

DROP TABLE IF EXISTS `equipe`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `equipe` (
  `id` int(11) NOT NULL auto_increment,
  `nom` varchar(255) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `experiment_category`
--

DROP TABLE IF EXISTS `experiment_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `experiment_category` (
  `id` bigint(20) NOT NULL auto_increment,
  `name` varchar(255) NOT NULL,
  `code` varchar(255) NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `uq_experiment_category_code` (`code`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `experiment_purification_method`
--

DROP TABLE IF EXISTS `experiment_purification_method`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `experiment_purification_method` (
  `fk_purification_method_type` bigint(20) NOT NULL,
  `fk_experiment_type` bigint(20) NOT NULL,
  PRIMARY KEY  (`fk_purification_method_type`,`fk_experiment_type`),
  KEY `fk_purification_method_type` (`fk_purification_method_type`),
  KEY `fk_experiment_type` (`fk_experiment_type`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `experiment_quality_control`
--

DROP TABLE IF EXISTS `experiment_quality_control`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `experiment_quality_control` (
  `fk_quality_control_type` bigint(20) NOT NULL,
  `fk_experiment_type` bigint(20) NOT NULL,
  PRIMARY KEY  (`fk_quality_control_type`,`fk_experiment_type`),
  KEY `fk_quality_control_type` (`fk_quality_control_type`),
  KEY `fk_experiment_type` (`fk_experiment_type`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `experiment_type`
--

DROP TABLE IF EXISTS `experiment_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `experiment_type` (
  `id` bigint(20) NOT NULL,
  `doPurification` tinyint(1) NOT NULL default '0',
  `mandatoryPurification` tinyint(1) NOT NULL default '0',
  `doQualityControl` tinyint(1) NOT NULL default '0',
  `mandatoryQualityControl` tinyint(1) NOT NULL default '0',
  `fk_experiment_category` bigint(20) NOT NULL,
  `fk_common_info_type` bigint(20) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `fk_experiment_type_category` (`fk_experiment_category`),
  KEY `fk_experiment_cit` (`fk_common_info_type`)
) ENGINE=MyISAM AUTO_INCREMENT=12 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `instrument`
--

DROP TABLE IF EXISTS `instrument`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `instrument` (
  `id` bigint(20) NOT NULL auto_increment,
  `name` varchar(255) default NULL,
  `code` varchar(255) NOT NULL,
  `instrument_used_type_id` bigint(20) default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `uq_instrument_code` (`code`),
  KEY `ix_instrument_instrumentUsedTy_3` (`instrument_used_type_id`)
) ENGINE=MyISAM AUTO_INCREMENT=6 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `instrumentCategory_inContainerSupportCategory`
--

DROP TABLE IF EXISTS `instrumentCategory_inContainerSupportCategory`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `instrumentCategory_inContainerSupportCategory` (
  `fk_instrument_category` bigint(20) NOT NULL,
  `fk_container_support_category` bigint(20) NOT NULL,
  PRIMARY KEY  (`fk_instrument_category`,`fk_container_support_category`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `instrumentCategory_outContainerSupportCategory`
--

DROP TABLE IF EXISTS `instrumentCategory_outContainerSupportCategory`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `instrumentCategory_outContainerSupportCategory` (
  `fk_instrument_category` bigint(20) NOT NULL,
  `fk_container_support_category` bigint(20) NOT NULL,
  PRIMARY KEY  (`fk_instrument_category`,`fk_container_support_category`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `instrument_category`
--

DROP TABLE IF EXISTS `instrument_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `instrument_category` (
  `id` bigint(20) NOT NULL auto_increment,
  `name` varchar(255) NOT NULL,
  `code` varchar(255) NOT NULL,
  `nbInContainerSupportCategories` int(11) default NULL,
  `nbOutContainerSupportCategories` int(11) default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `uq_instrument_category_code` (`code`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;
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
  PRIMARY KEY  (`id`),
  KEY `ix_instrument_used_type_common_4` (`fk_common_info_type`),
  KEY `ix_instrument_used_type_instrumentCategory` (`fk_instrument_category`)
) ENGINE=MyISAM AUTO_INCREMENT=7 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `measure_category`
--

DROP TABLE IF EXISTS `measure_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `measure_category` (
  `id` bigint(20) NOT NULL auto_increment,
  `name` varchar(255) NOT NULL,
  `code` varchar(255) NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `uq_measure_category_code` (`code`)
) ENGINE=MyISAM AUTO_INCREMENT=11 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `measure_value`
--

DROP TABLE IF EXISTS `measure_value`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `measure_value` (
  `id` bigint(20) NOT NULL auto_increment,
  `code` varchar(255) NOT NULL,
  `value` varchar(255) NOT NULL,
  `default_value` tinyint(1) NOT NULL default '0',
  `measure_category_id` bigint(20) default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `uq_measure_value_code` (`code`)
) ENGINE=MyISAM AUTO_INCREMENT=11 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `next_experiment_types`
--

DROP TABLE IF EXISTS `next_experiment_types`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `next_experiment_types` (
  `fk_experiment_type` bigint(20) NOT NULL,
  `fk_next_experiment_type` bigint(20) NOT NULL,
  PRIMARY KEY  (`fk_experiment_type`,`fk_next_experiment_type`),
  KEY `fk_next_experiment_types_expe_02` (`fk_next_experiment_type`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `object_type`
--

DROP TABLE IF EXISTS `object_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `object_type` (
  `id` bigint(20) NOT NULL auto_increment,
  `code` varchar(255) NOT NULL,
  `generic` tinyint(1) NOT NULL default '0',
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=12 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `permission`
--

DROP TABLE IF EXISTS `permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `permission` (
  `id` int(11) NOT NULL auto_increment,
  `label` varchar(255) default NULL,
  `code` varchar(255) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=25 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `play_evolutions`
--

DROP TABLE IF EXISTS `play_evolutions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `play_evolutions` (
  `id` int(11) NOT NULL,
  `hash` varchar(255) NOT NULL,
  `applied_at` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `apply_script` text,
  `revert_script` text,
  `state` varchar(255) default NULL,
  `last_problem` text,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `process_category`
--

DROP TABLE IF EXISTS `process_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `process_category` (
  `id` bigint(20) NOT NULL auto_increment,
  `name` varchar(255) NOT NULL,
  `code` varchar(255) NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `uq_process_category_code` (`code`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;
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
  PRIMARY KEY  (`fk_process_type`,`fk_experiment_type`),
  KEY `fk_process_type` (`fk_process_type`),
  KEY `fk_experiment_type` (`fk_experiment_type`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
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
  PRIMARY KEY  (`id`),
  KEY `ix_process_type_commonInfoType` (`fk_common_info_type`),
  KEY `ix_process_type_processCategory` (`fk_process_category`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `project_category`
--

DROP TABLE IF EXISTS `project_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `project_category` (
  `id` bigint(20) NOT NULL auto_increment,
  `name` varchar(255) NOT NULL,
  `code` varchar(255) NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `uq_project_category_code` (`code`)
) ENGINE=MyISAM AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;
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
  PRIMARY KEY  (`id`),
  KEY `ix_project_type_commonInfoType` (`fk_common_info_type`),
  KEY `ix_project_type_projectCategory` (`fk_project_category`)
) ENGINE=MyISAM AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `property_definition`
--

DROP TABLE IF EXISTS `property_definition`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `property_definition` (
  `id` bigint(20) NOT NULL auto_increment,
  `code` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `required` tinyint(1) NOT NULL default '0',
  `active` tinyint(1) NOT NULL default '0',
  `type` varchar(255) NOT NULL,
  `display_format` varchar(255) default NULL,
  `display_order` int(11) default NULL,
  `default_value` varchar(255) default NULL,
  `measure_category_id` bigint(20) default NULL,
  `measure_value_id` bigint(20) default NULL,
  `common_info_type_id` bigint(20) default NULL,
  `description` text,
  `level` enum('current','content','containing') NOT NULL,
  `in_out` enum('in','out') default NULL,
  `propagation` tinyint(1) default NULL,
  `choice_in_list` tinyint(1) NOT NULL default '0',
  PRIMARY KEY  (`id`),
  KEY `ix_property_definition_measure_7` (`measure_category_id`),
  KEY `ix_property_definition_measure_8` (`measure_value_id`),
  KEY `ix_property_definition_commonI_9` (`common_info_type_id`),
  UNIQUE KEY `uq_property_code` (`code`)
) ENGINE=MyISAM AUTO_INCREMENT=2088 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `protocol`
--

DROP TABLE IF EXISTS `protocol`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `protocol` (
  `id` bigint(20) NOT NULL auto_increment,
  `code` varchar(255) NOT NULL,
  `name` varchar(255) default NULL,
  `file_path` varchar(255) default NULL,
  `fk_protocol_category` bigint(20) NOT NULL,
  `version` varchar(255) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `fk_protocol_protocolCategory` (`fk_protocol_category`),
  UNIQUE KEY `uq_protocol_code` (`code`)
) ENGINE=MyISAM AUTO_INCREMENT=7 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `protocol_category`
--

DROP TABLE IF EXISTS `protocol_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `protocol_category` (
  `id` bigint(20) NOT NULL auto_increment,
  `name` varchar(255) NOT NULL,
  `code` varchar(255) NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `uq_protocol_category_code` (`code`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `protocol_reagent_type`
--

DROP TABLE IF EXISTS `protocol_reagent_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `protocol_reagent_type` (
  `fk_protocol` bigint(20) NOT NULL,
  `fk_reagent_type` bigint(20) NOT NULL,
  PRIMARY KEY  (`fk_protocol`,`fk_reagent_type`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `purification_method_type`
--

DROP TABLE IF EXISTS `purification_method_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `purification_method_type` (
  `id` bigint(20) NOT NULL,
  `fk_common_info_type` bigint(20) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `fk_purification_cit` (`fk_common_info_type`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `quality_control_type`
--

DROP TABLE IF EXISTS `quality_control_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `quality_control_type` (
  `id` bigint(20) NOT NULL,
  `fk_common_info_type` bigint(20) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `fk_qc_cit` (`fk_common_info_type`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `reagent_type`
--

DROP TABLE IF EXISTS `reagent_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `reagent_type` (
  `id` bigint(20) NOT NULL,
  `fk_common_info_type` bigint(20) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `ix_reagent_type_commonInfoTyp_13` (`fk_common_info_type`)
) ENGINE=MyISAM AUTO_INCREMENT=5 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `resolution`
--

DROP TABLE IF EXISTS `resolution`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `resolution` (
  `id` bigint(20) NOT NULL auto_increment,
  `name` varchar(255) NOT NULL,
  `code` varchar(255) NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `uq_resolution_code` (`code`)
) ENGINE=MyISAM AUTO_INCREMENT=21 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `role`
--

DROP TABLE IF EXISTS `role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `role` (
  `id` int(11) NOT NULL auto_increment,
  `label` varchar(255) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=20 DEFAULT CHARSET=latin1;
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
  PRIMARY KEY  (`role_id`,`permission_id`),
  KEY `fk_role_permission_permission_02` (`permission_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sample_category`
--

DROP TABLE IF EXISTS `sample_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sample_category` (
  `id` bigint(20) NOT NULL auto_increment,
  `name` varchar(255) NOT NULL,
  `code` varchar(255) NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `uq_sample_category_code` (`code`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

DROP TABLE IF EXISTS `import_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `import_category` (
  `id` bigint(20) NOT NULL auto_increment,
  `name` varchar(255) NOT NULL,
  `code` varchar(255) NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `uq_import_category_code` (`code`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;
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
  PRIMARY KEY  (`id`),
  KEY `ix_sample_type_commonInfoType_15` (`fk_common_info_type`),
  KEY `ix_sample_type_sampleCategory` (`fk_sample_category`)
) ENGINE=MyISAM AUTO_INCREMENT=5 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

DROP TABLE IF EXISTS `import_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `import_type` (
  `id` bigint(20) NOT NULL,
  `fk_common_info_type` bigint(20) NOT NULL,
  `fk_import_category` bigint(20) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `ix_import_type_commonInfoType_15` (`fk_common_info_type`),
  KEY `ix_import_type_importCategory` (`fk_import_category`)
) ENGINE=MyISAM AUTO_INCREMENT=5 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `state`
--

DROP TABLE IF EXISTS `state`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `state` (
  `id` bigint(20) NOT NULL auto_increment,
  `name` varchar(255) NOT NULL,
  `code` varchar(255) NOT NULL,
  `active` tinyint(1) NOT NULL default '0',
  `priority` int(11) default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `uq_state_code` (`code`)
) ENGINE=MyISAM AUTO_INCREMENT=21 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `team`
--

DROP TABLE IF EXISTS `team`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `team` (
  `id` int(11) NOT NULL auto_increment,
  `nom` varchar(255) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=30 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `transfer_method_type`
--

DROP TABLE IF EXISTS `transfer_method_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `transfer_method_type` (
  `id` bigint(20) NOT NULL,
  `fk_common_info_type` bigint(20) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `ix_transfer_method_type_commo_16` (`fk_common_info_type`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user` (
  `id` int(11) NOT NULL auto_increment,
  `login` varchar(255) default NULL,
  `firstname` varchar(255) default NULL,
  `lastname` varchar(255) default NULL,
  `email` varchar(255) default NULL,
  `technicaluser` int(11) default NULL,
  `password` varchar(255) default NULL,
  `confirmpassword` varchar(255) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=76 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_equipe`
--

DROP TABLE IF EXISTS `user_equipe`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_equipe` (
  `user_id` int(11) NOT NULL,
  `equipe_id` int(11) NOT NULL,
  PRIMARY KEY  (`user_id`,`equipe_id`),
  KEY `fk_user_equipe_equipe_02` (`equipe_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
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
  PRIMARY KEY  (`user_id`,`role_id`),
  KEY `fk_user_role_role_02` (`role_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_team`
--

DROP TABLE IF EXISTS `user_team`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_team` (
  `user_id` int(11) NOT NULL,
  `team_id` int(11) NOT NULL default '0',
  PRIMARY KEY  (`user_id`,`team_id`),
  KEY `fk_user_equipe_equipe_02` (`team_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `value`
--

DROP TABLE IF EXISTS `value`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `value` (
  `id` bigint(20) NOT NULL auto_increment,
  `code` varchar(255) NOT NULL,
  `value` varchar(255) NOT NULL,
  `default_value` tinyint(1) NOT NULL default '0',
  `property_definition_id` bigint(20) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=17 DEFAULT CHARSET=latin1;


INSERT INTO `object_type`(`code`,`generic`) VALUES('Project','0');
INSERT INTO `object_type`(`code`,`generic`) VALUES('Process','0');
INSERT INTO `object_type`(`code`,`generic`) VALUES('Sample','0');
INSERT INTO `object_type`(`code`,`generic`) VALUES('Instrument','0');
INSERT INTO `object_type`(`code`,`generic`) VALUES('Reagent','1');
INSERT INTO `object_type`(`code`,`generic`) VALUES('Experiment','0');
INSERT INTO `object_type`(`code`,`generic`) VALUES('Purification','0');
INSERT INTO `object_type`(`code`,`generic`) VALUES('ControlQuality','0');
INSERT INTO `object_type`(`code`,`generic`) VALUES('Transfer','0');
INSERT INTO `object_type`(`code`,`generic`) VALUES('Import','0');
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-01-30 17:01:11
