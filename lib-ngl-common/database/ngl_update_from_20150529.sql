-- MySQL Workbench Synchronization
-- Generated: 2016-05-19 17:15
-- Model: New Model
-- Version: 1.0
-- Project: Name of the project
-- Author: galbini

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

ALTER TABLE `NGL`.`instrument` 
DROP FOREIGN KEY `instrument_ibfk_1`;

ALTER TABLE `NGL`.`experiment_type` 
ADD COLUMN `short_code` VARCHAR(10) NULL DEFAULT NULL AFTER `atomic_transfert_method`;

ALTER TABLE `NGL`.`experiment_type_node` 
ADD COLUMN `doTransfert` TINYINT(1) NOT NULL DEFAULT '0' AFTER `mandatoryQualityControl`,
ADD COLUMN `mandatoryTransfert` TINYINT(1) NOT NULL DEFAULT '0' AFTER `doTransfert`;

ALTER TABLE `NGL`.`instrument` 
CHANGE COLUMN `fk_instrument_used_type` `fk_instrument_used_type` BIGINT(20) NULL DEFAULT NULL ;

ALTER TABLE `NGL`.`permission` 
CHANGE COLUMN `label` `label` VARCHAR(255) NOT NULL ,
CHANGE COLUMN `code` `code` VARCHAR(255) NOT NULL ,
ADD UNIQUE INDEX `code_UNIQUE` (`code` ASC);

ALTER TABLE `NGL`.`process_experiment_type` 
ADD COLUMN `position_in_process` INT(2) NULL DEFAULT NULL AFTER `fk_experiment_type`;

ALTER TABLE `NGL`.`property_definition` 
ADD COLUMN `required_state` VARCHAR(10) NULL DEFAULT NULL AFTER `required`;

ALTER TABLE `NGL`.`role` 
CHANGE COLUMN `label` `label` VARCHAR(255) NOT NULL ,
ADD UNIQUE INDEX `label_UNIQUE` (`label` ASC);

DROP TABLE IF EXISTS `NGL`.`resolution_institute` ;

DROP TABLE IF EXISTS `NGL`.`resolution_object_type` ;

DROP TABLE IF EXISTS `NGL`.`valuation_criteria_institute` ;

DROP TABLE IF EXISTS `NGL`.`valuation_criteria` ;

DROP TABLE IF EXISTS `NGL`.`valuation_criteria_common_info_type` ;

DROP TABLE IF EXISTS `NGL`.`resolution_category` ;

DROP TABLE IF EXISTS `NGL`.`protocol_reagent_type` ;

DROP TABLE IF EXISTS `NGL`.`resolution` ;

DROP TABLE IF EXISTS `NGL`.`reagent_type` ;

DROP TABLE IF EXISTS `NGL`.`protocol` ;

DROP TABLE IF EXISTS `NGL`.`common_info_type_resolution` ;

DROP TABLE IF EXISTS `NGL`.`experiment_type_protocol` ;

ALTER TABLE `NGL`.`instrument` 
ADD CONSTRAINT `instrument_ibfk_1`
  FOREIGN KEY (`fk_instrument_used_type`)
  REFERENCES `NGL`.`instrument_used_type` (`id`);


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
