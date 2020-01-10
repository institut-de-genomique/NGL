-- MySQL Workbench Synchronization
-- Generated: 2016-09-13 11:06
-- Model: New Model
-- Version: 1.0
-- Project: Name of the project
-- Author: galbini

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

ALTER TABLE `NGL`.`common_info_type` 
ADD COLUMN `active` TINYINT(1) NOT NULL DEFAULT 1 AFTER `display_order`;

ALTER TABLE `NGL`.`experiment_type` 
ADD COLUMN `new_sample` TINYINT(1) NOT NULL DEFAULT '0' AFTER `short_code`;

CREATE TABLE IF NOT EXISTS `NGL`.`experiment_type_sample_type` (
  `fk_experiment_type` BIGINT(20) NOT NULL,
  `fk_sample_type` BIGINT(20) NOT NULL,
  PRIMARY KEY (`fk_experiment_type`, `fk_sample_type`),
  INDEX `sample_type_fk2_idx` (`fk_sample_type` ASC),
  CONSTRAINT `experiment_type_fk1`
    FOREIGN KEY (`fk_experiment_type`)
    REFERENCES `NGL`.`experiment_type` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `sample_type_fk2`
    FOREIGN KEY (`fk_sample_type`)
    REFERENCES `NGL`.`sample_type` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
