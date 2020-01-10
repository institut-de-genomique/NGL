-- MySQL Workbench Synchronization
-- Generated: 2018-05-16 10:44
-- Model: New Model
-- Version: 1.0
-- Project: Name of the project
-- Author: galbini

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

ALTER TABLE `NGL`.`process_experiment_type` 
CHANGE COLUMN `position_in_process` `position_in_process` INT(2) NOT NULL ,
DROP PRIMARY KEY,
ADD PRIMARY KEY (`fk_process_type`, `fk_experiment_type`, `position_in_process`);

ALTER TABLE `NGL`.`user` 
DROP COLUMN `confirmpassword`,
ADD COLUMN `active` TINYINT(1) NULL DEFAULT '1' AFTER `password`;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
