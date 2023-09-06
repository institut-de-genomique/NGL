-- MySQL Workbench Synchronization
-- Generated: 2015-05-29 16:05
-- Model: New Model
-- Version: 1.0
-- Project: Name of the project
-- Author: galbini

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

CREATE SCHEMA IF NOT EXISTS `NGL` DEFAULT CHARACTER SET latin1 COLLATE latin1_swedish_ci ;

CREATE TABLE IF NOT EXISTS `NGL`.`common_info_type` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `code` VARCHAR(255) NOT NULL,
  `display_order` INT(11) NULL DEFAULT NULL,
  `fk_object_type` BIGINT(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `code` (`code` ASC),
  INDEX `fk_object_type` (`fk_object_type` ASC),
  INDEX `idx_common_code` (`code` ASC),
  CONSTRAINT `common_info_type_ibfk_1`
    FOREIGN KEY (`fk_object_type`)
    REFERENCES `NGL`.`object_type` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
AUTO_INCREMENT = 10
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`experiment_type_instrument_type` (
  `fk_experiment_type` BIGINT(20) NOT NULL,
  `fk_instrument_used_type` BIGINT(20) NOT NULL,
  PRIMARY KEY (`fk_experiment_type`, `fk_instrument_used_type`),
  INDEX `fk_instrument_type` (`fk_instrument_used_type` ASC),
  INDEX `common_info_type_instrument_type_ibfk_1_idx` (`fk_experiment_type` ASC),
  CONSTRAINT `common_info_type_instrument_type_ibfk_1`
    FOREIGN KEY (`fk_experiment_type`)
    REFERENCES `NGL`.`experiment_type` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `common_info_type_instrument_type_ibfk_2`
    FOREIGN KEY (`fk_instrument_used_type`)
    REFERENCES `NGL`.`instrument_used_type` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`experiment_type_protocol` (
  `fk_experiment_type` BIGINT(20) NOT NULL,
  `fk_protocol` BIGINT(20) NOT NULL,
  PRIMARY KEY (`fk_experiment_type`, `fk_protocol`),
  INDEX `fk_protocol` (`fk_protocol` ASC),
  INDEX `common_info_type_protocol_ibfk_1_idx` (`fk_experiment_type` ASC),
  CONSTRAINT `common_info_type_protocol_ibfk_1`
    FOREIGN KEY (`fk_experiment_type`)
    REFERENCES `NGL`.`experiment_type` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `common_info_type_protocol_ibfk_2`
    FOREIGN KEY (`fk_protocol`)
    REFERENCES `NGL`.`protocol` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`common_info_type_resolution` (
  `fk_common_info_type` BIGINT(20) NOT NULL,
  `fk_resolution` BIGINT(20) NOT NULL,
  PRIMARY KEY (`fk_common_info_type`, `fk_resolution`),
  INDEX `fk_resolution` (`fk_resolution` ASC),
  INDEX `fk_common_info_type` (`fk_common_info_type` ASC),
  CONSTRAINT `common_info_type_resolution_ibfk_1`
    FOREIGN KEY (`fk_common_info_type`)
    REFERENCES `NGL`.`common_info_type` (`id`),
  CONSTRAINT `common_info_type_resolution_ibfk_2`
    FOREIGN KEY (`fk_resolution`)
    REFERENCES `NGL`.`resolution` (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`common_info_type_state` (
  `fk_common_info_type` BIGINT(20) NOT NULL,
  `fk_state` BIGINT(20) NOT NULL,
  PRIMARY KEY (`fk_common_info_type`, `fk_state`),
  INDEX `fk_state` (`fk_state` ASC),
  INDEX `fk_common_info_type` (`fk_common_info_type` ASC),
  CONSTRAINT `common_info_type_state_ibfk_1`
    FOREIGN KEY (`fk_common_info_type`)
    REFERENCES `NGL`.`common_info_type` (`id`),
  CONSTRAINT `common_info_type_state_ibfk_2`
    FOREIGN KEY (`fk_state`)
    REFERENCES `NGL`.`state` (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`container_category` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `code` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `code` (`code` ASC),
  INDEX `idx_container_category_code` (`code` ASC))
ENGINE = InnoDB
AUTO_INCREMENT = 2
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`container_support_category` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `code` VARCHAR(255) NOT NULL,
  `nbUsableContainer` INT(11) NOT NULL,
  `nbLine` INT(11) NULL DEFAULT NULL,
  `nbColumn` INT(11) NULL DEFAULT NULL,
  `fk_container_category` BIGINT(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `code` (`code` ASC),
  INDEX `idx_container_support_category_code` (`code` ASC),
  INDEX `fk_container_category_idx` (`fk_container_category` ASC),
  CONSTRAINT `fk_container_category`
    FOREIGN KEY (`fk_container_category`)
    REFERENCES `NGL`.`container_category` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
AUTO_INCREMENT = 5
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`experiment_category` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `code` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `code` (`code` ASC),
  INDEX `idx_exp_category_code` (`code` ASC))
ENGINE = InnoDB
AUTO_INCREMENT = 2
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`experiment_type` (
  `id` BIGINT(20) NOT NULL,
  `fk_common_info_type` BIGINT(20) NOT NULL,
  `fk_experiment_category` BIGINT(20) NOT NULL,
  `atomic_transfert_method` VARCHAR(255) NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_experiment_category` (`fk_experiment_category` ASC),
  INDEX `fk_common_info_type` (`fk_common_info_type` ASC),
  CONSTRAINT `experiment_type_ibfk_1`
    FOREIGN KEY (`fk_experiment_category`)
    REFERENCES `NGL`.`experiment_category` (`id`),
  CONSTRAINT `experiment_type_ibfk_2`
    FOREIGN KEY (`fk_common_info_type`)
    REFERENCES `NGL`.`common_info_type` (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`experiment_type_node` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `code` VARCHAR(255) NOT NULL,
  `doPurification` TINYINT(1) NOT NULL DEFAULT '0',
  `mandatoryPurification` TINYINT(1) NOT NULL DEFAULT '0',
  `doQualityControl` TINYINT(1) NOT NULL DEFAULT '0',
  `mandatoryQualityControl` TINYINT(1) NOT NULL DEFAULT '0',
  `fk_experiment_type` BIGINT(20) NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_experiment_type` (`fk_experiment_type` ASC),
  UNIQUE INDEX `code_UNIQUE` (`code` ASC),
  CONSTRAINT `experiment_type_node_ibfk_1`
    FOREIGN KEY (`fk_experiment_type`)
    REFERENCES `NGL`.`experiment_type` (`id`)
    ON DELETE RESTRICT
    ON UPDATE RESTRICT)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`import_category` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `code` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `code` (`code` ASC),
  INDEX `idx_import_category_code` (`code` ASC))
ENGINE = InnoDB
AUTO_INCREMENT = 2
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`import_type` (
  `id` BIGINT(20) NOT NULL,
  `fk_common_info_type` BIGINT(20) NOT NULL,
  `fk_import_category` BIGINT(20) NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_common_info_type` (`fk_common_info_type` ASC),
  INDEX `fk_import_category` (`fk_import_category` ASC),
  CONSTRAINT `import_type_ibfk_1`
    FOREIGN KEY (`fk_common_info_type`)
    REFERENCES `NGL`.`common_info_type` (`id`),
  CONSTRAINT `import_type_ibfk_2`
    FOREIGN KEY (`fk_import_category`)
    REFERENCES `NGL`.`import_category` (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`instrument` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `short_name` VARCHAR(30) NULL DEFAULT NULL,
  `name` VARCHAR(255) NULL DEFAULT NULL,
  `code` VARCHAR(255) NOT NULL,
  `fk_instrument_used_type` BIGINT(20) NOT NULL,
  `active` TINYINT(1) NOT NULL,
  `path` VARCHAR(255) NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  INDEX `instrument_used_type_id` (`fk_instrument_used_type` ASC),
  INDEX `idx_instrument_code` (`code` ASC),
  CONSTRAINT `instrument_ibfk_1`
    FOREIGN KEY (`fk_instrument_used_type`)
    REFERENCES `NGL`.`instrument_used_type` (`id`))
ENGINE = InnoDB
AUTO_INCREMENT = 6500
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`instrument_ut_in_container_support_cat` (
  `fk_instrument_used_type` BIGINT(20) NOT NULL,
  `fk_container_support_category` BIGINT(20) NOT NULL,
  PRIMARY KEY (`fk_instrument_used_type`, `fk_container_support_category`),
  INDEX `fk_container_support_category` (`fk_container_support_category` ASC),
  CONSTRAINT `instrumentCategory_inContainerSupportCategory_ibfk_1`
    FOREIGN KEY (`fk_instrument_used_type`)
    REFERENCES `NGL`.`instrument_used_type` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `instrumentCategory_inContainerSupportCategory_ibfk_2`
    FOREIGN KEY (`fk_container_support_category`)
    REFERENCES `NGL`.`container_support_category` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`instrument_ut_out_container_support_cat` (
  `fk_instrument_used_type` BIGINT(20) NOT NULL,
  `fk_container_support_category` BIGINT(20) NOT NULL,
  PRIMARY KEY (`fk_instrument_used_type`, `fk_container_support_category`),
  INDEX `fk_container_support_category` (`fk_container_support_category` ASC),
  CONSTRAINT `instrumentCategory_outContainerSupportCategory_ibfk_1`
    FOREIGN KEY (`fk_instrument_used_type`)
    REFERENCES `NGL`.`instrument_used_type` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `instrumentCategory_outContainerSupportCategory_ibfk_2`
    FOREIGN KEY (`fk_container_support_category`)
    REFERENCES `NGL`.`container_support_category` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`instrument_category` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `code` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `code` (`code` ASC),
  INDEX `idx_inst_category_code` (`code` ASC))
ENGINE = InnoDB
AUTO_INCREMENT = 3
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`instrument_used_type` (
  `id` BIGINT(20) NOT NULL,
  `fk_common_info_type` BIGINT(20) NOT NULL,
  `fk_instrument_category` BIGINT(20) NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_common_info_type` (`fk_common_info_type` ASC),
  INDEX `fk_instrument_category` (`fk_instrument_category` ASC),
  CONSTRAINT `instrument_used_type_ibfk_1`
    FOREIGN KEY (`fk_common_info_type`)
    REFERENCES `NGL`.`common_info_type` (`id`),
  CONSTRAINT `instrument_used_type_ibfk_2`
    FOREIGN KEY (`fk_instrument_category`)
    REFERENCES `NGL`.`instrument_category` (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`measure_category` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `code` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `code` (`code` ASC),
  INDEX `idx_measure_category_code` (`code` ASC))
ENGINE = InnoDB
AUTO_INCREMENT = 3
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`measure_unit` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `code` VARCHAR(255) NOT NULL,
  `value` VARCHAR(255) NOT NULL,
  `default_unit` TINYINT(1) NOT NULL DEFAULT '0',
  `fk_measure_category` BIGINT(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `code` (`code` ASC),
  INDEX `measure_category_id` (`fk_measure_category` ASC),
  INDEX `idx_measure_value_code` (`code` ASC),
  CONSTRAINT `measure_value_ibfk_1`
    FOREIGN KEY (`fk_measure_category`)
    REFERENCES `NGL`.`measure_category` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
AUTO_INCREMENT = 3
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`object_type` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `code` VARCHAR(255) NOT NULL,
  `generic` TINYINT(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `code` (`code` ASC),
  INDEX `idx_object_type_code` (`code` ASC))
ENGINE = InnoDB
AUTO_INCREMENT = 9
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`permission` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `label` VARCHAR(255) NULL DEFAULT NULL,
  `code` VARCHAR(255) NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_permission_code` (`code` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`previous_nodes` (
  `fk_previous_node` BIGINT(20) NOT NULL,
  `fk_node` BIGINT(20) NOT NULL,
  PRIMARY KEY (`fk_previous_node`, `fk_node`),
  INDEX `fk_node` (`fk_node` ASC),
  CONSTRAINT `previous_nodes_ibfk_1`
    FOREIGN KEY (`fk_previous_node`)
    REFERENCES `NGL`.`experiment_type_node` (`id`)
    ON DELETE RESTRICT
    ON UPDATE RESTRICT,
  CONSTRAINT `previous_nodes_ibfk_2`
    FOREIGN KEY (`fk_node`)
    REFERENCES `NGL`.`experiment_type_node` (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`process_category` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `code` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `code` (`code` ASC),
  INDEX `idx_process_category_code` (`code` ASC))
ENGINE = InnoDB
AUTO_INCREMENT = 2
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`process_experiment_type` (
  `fk_process_type` BIGINT(20) NOT NULL,
  `fk_experiment_type` BIGINT(20) NOT NULL,
  INDEX `fk_experiment_type` (`fk_experiment_type` ASC),
  PRIMARY KEY (`fk_process_type`, `fk_experiment_type`),
  CONSTRAINT `process_experiment_type_ibfk_1`
    FOREIGN KEY (`fk_process_type`)
    REFERENCES `NGL`.`process_type` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `process_experiment_type_ibfk_2`
    FOREIGN KEY (`fk_experiment_type`)
    REFERENCES `NGL`.`experiment_type` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`process_type` (
  `id` BIGINT(20) NOT NULL,
  `fk_common_info_type` BIGINT(20) NOT NULL,
  `fk_process_category` BIGINT(20) NOT NULL,
  `fk_void_experiment_type` BIGINT(20) NOT NULL,
  `fk_first_experiment_type` BIGINT(20) NOT NULL,
  `fk_last_experiment_type` BIGINT(20) NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_common_info_type` (`fk_common_info_type` ASC),
  INDEX `fk_process_category` (`fk_process_category` ASC),
  INDEX `fk_void_experiment_type` (`fk_void_experiment_type` ASC),
  INDEX `fk_first_experiment_type` (`fk_first_experiment_type` ASC),
  INDEX `fk_last_experiment_type` (`fk_last_experiment_type` ASC),
  CONSTRAINT `process_type_ibfk_1`
    FOREIGN KEY (`fk_common_info_type`)
    REFERENCES `NGL`.`common_info_type` (`id`),
  CONSTRAINT `process_type_ibfk_2`
    FOREIGN KEY (`fk_process_category`)
    REFERENCES `NGL`.`process_category` (`id`),
  CONSTRAINT `process_type_ibfk_3`
    FOREIGN KEY (`fk_void_experiment_type`)
    REFERENCES `NGL`.`experiment_type` (`id`),
  CONSTRAINT `process_type_ibfk_4`
    FOREIGN KEY (`fk_first_experiment_type`)
    REFERENCES `NGL`.`experiment_type` (`id`),
  CONSTRAINT `process_type_ibfk_5`
    FOREIGN KEY (`fk_last_experiment_type`)
    REFERENCES `NGL`.`experiment_type` (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`project_category` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `code` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `code` (`code` ASC),
  INDEX `idx_project_category_code` (`code` ASC))
ENGINE = InnoDB
AUTO_INCREMENT = 2
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`project_type` (
  `id` BIGINT(20) NOT NULL,
  `fk_common_info_type` BIGINT(20) NOT NULL,
  `fk_project_category` BIGINT(20) NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_common_info_type` (`fk_common_info_type` ASC),
  INDEX `fk_project_category` (`fk_project_category` ASC),
  CONSTRAINT `project_type_ibfk_1`
    FOREIGN KEY (`fk_common_info_type`)
    REFERENCES `NGL`.`common_info_type` (`id`),
  CONSTRAINT `project_type_ibfk_2`
    FOREIGN KEY (`fk_project_category`)
    REFERENCES `NGL`.`project_category` (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`property_definition` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `code` VARCHAR(255) NOT NULL,
  `name` VARCHAR(255) NOT NULL,
  `required` TINYINT(1) NOT NULL DEFAULT '0',
  `editable` TINYINT(1) NOT NULL DEFAULT '0',
  `active` TINYINT(1) NOT NULL DEFAULT '0',
  `type` VARCHAR(255) NOT NULL,
  `display_format` VARCHAR(255) NULL DEFAULT NULL,
  `display_order` INT(2) NULL DEFAULT NULL,
  `default_value` VARCHAR(255) NULL DEFAULT NULL,
  `description` TEXT NULL DEFAULT NULL,
  `choice_in_list` TINYINT(1) NOT NULL DEFAULT '0',
  `property_value_type` VARCHAR(15) NOT NULL DEFAULT 'single',
  `fk_measure_category` BIGINT(20) NULL DEFAULT NULL,
  `fk_save_measure_unit` BIGINT(20) NULL DEFAULT NULL,
  `fk_display_measure_unit` BIGINT(20) NULL DEFAULT NULL,
  `fk_common_info_type` BIGINT(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `code_type` (`code` ASC, `fk_common_info_type` ASC),
  INDEX `measure_category_id` (`fk_measure_category` ASC),
  INDEX `measure_value_id` (`fk_save_measure_unit` ASC),
  INDEX `display_measure_value_id` (`fk_display_measure_unit` ASC),
  INDEX `common_info_type_id` (`fk_common_info_type` ASC),
  INDEX `idx_property_def_code` (`code` ASC),
  CONSTRAINT `property_definition_ibfk_1`
    FOREIGN KEY (`fk_measure_category`)
    REFERENCES `NGL`.`measure_category` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `property_definition_ibfk_2`
    FOREIGN KEY (`fk_save_measure_unit`)
    REFERENCES `NGL`.`measure_unit` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `property_definition_ibfk_3`
    FOREIGN KEY (`fk_display_measure_unit`)
    REFERENCES `NGL`.`measure_unit` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `property_definition_ibfk_4`
    FOREIGN KEY (`fk_common_info_type`)
    REFERENCES `NGL`.`common_info_type` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
AUTO_INCREMENT = 94816
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`protocol` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `code` VARCHAR(255) NOT NULL,
  `name` VARCHAR(255) NULL DEFAULT NULL,
  `file_path` VARCHAR(255) NULL DEFAULT NULL,
  `version` VARCHAR(255) NOT NULL,
  `fk_protocol_category` BIGINT(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `code` (`code` ASC),
  INDEX `fk_protocol_category` (`fk_protocol_category` ASC),
  INDEX `idx_protocol_code` (`code` ASC),
  CONSTRAINT `protocol_ibfk_1`
    FOREIGN KEY (`fk_protocol_category`)
    REFERENCES `NGL`.`protocol_category` (`id`))
ENGINE = InnoDB
AUTO_INCREMENT = 3
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`protocol_category` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `code` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `code` (`code` ASC),
  INDEX `idx_protocol_category_code` (`code` ASC))
ENGINE = InnoDB
AUTO_INCREMENT = 4
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`reagent_type` (
  `id` BIGINT(20) NOT NULL,
  `fk_common_info_type` BIGINT(20) NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_common_info_type` (`fk_common_info_type` ASC),
  CONSTRAINT `reagent_type_ibfk_1`
    FOREIGN KEY (`fk_common_info_type`)
    REFERENCES `NGL`.`common_info_type` (`id`)
    ON DELETE RESTRICT
    ON UPDATE RESTRICT)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`resolution` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `code` VARCHAR(255) NOT NULL,
  `fk_resolution_category` BIGINT(20) NOT NULL,
  `display_order` INT(5) NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `code_UNIQUE` (`code` ASC),
  INDEX `idx_resolution_code` (`code` ASC),
  INDEX `fk_resolution_category_idx` (`fk_resolution_category` ASC),
  CONSTRAINT `fk_resolution_category`
    FOREIGN KEY (`fk_resolution_category`)
    REFERENCES `NGL`.`resolution_category` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
AUTO_INCREMENT = 9524
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`role` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `label` VARCHAR(255) NULL DEFAULT NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`role_permission` (
  `role_id` INT(11) NOT NULL,
  `permission_id` INT(11) NOT NULL,
  PRIMARY KEY (`role_id`, `permission_id`),
  INDEX `permission_id` (`permission_id` ASC),
  CONSTRAINT `role_permission_ibfk_1`
    FOREIGN KEY (`role_id`)
    REFERENCES `NGL`.`role` (`id`),
  CONSTRAINT `role_permission_ibfk_2`
    FOREIGN KEY (`permission_id`)
    REFERENCES `NGL`.`permission` (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`sample_category` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `code` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `code` (`code` ASC),
  INDEX `idx_sample_category_code` (`code` ASC))
ENGINE = InnoDB
AUTO_INCREMENT = 2
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`sample_type` (
  `id` BIGINT(20) NOT NULL,
  `fk_common_info_type` BIGINT(20) NOT NULL,
  `fk_sample_category` BIGINT(20) NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_common_info_type` (`fk_common_info_type` ASC),
  INDEX `fk_sample_category` (`fk_sample_category` ASC),
  CONSTRAINT `sample_type_ibfk_1`
    FOREIGN KEY (`fk_common_info_type`)
    REFERENCES `NGL`.`common_info_type` (`id`),
  CONSTRAINT `sample_type_ibfk_2`
    FOREIGN KEY (`fk_sample_category`)
    REFERENCES `NGL`.`sample_category` (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`state` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `code` VARCHAR(255) NOT NULL,
  `active` TINYINT(1) NOT NULL DEFAULT '0',
  `position` INT(11) NOT NULL,
  `fk_state_category` BIGINT(20) NOT NULL,
  `display` TINYINT(1) NULL DEFAULT NULL,
  `functionnal_group` VARCHAR(50) NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `code_UNIQUE` (`code` ASC),
  INDEX `idx_state_code` (`code` ASC),
  INDEX `state_ibfk_1_idx` (`fk_state_category` ASC),
  CONSTRAINT `state_ibfk_1`
    FOREIGN KEY (`fk_state_category`)
    REFERENCES `NGL`.`state_category` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
AUTO_INCREMENT = 9103
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`state_category` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `code` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `code` (`code` ASC),
  INDEX `idx_state_category_code` (`code` ASC))
ENGINE = InnoDB
AUTO_INCREMENT = 3
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`team` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `nom` VARCHAR(255) NULL DEFAULT NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`user` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `login` VARCHAR(255) NULL DEFAULT NULL,
  `firstname` VARCHAR(255) NULL DEFAULT NULL,
  `lastname` VARCHAR(255) NULL DEFAULT NULL,
  `email` VARCHAR(255) NULL DEFAULT NULL,
  `technicaluser` SMALLINT(6) NULL DEFAULT 0,
  `password` VARCHAR(255) NULL DEFAULT NULL,
  `confirmpassword` VARCHAR(255) NULL DEFAULT NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`user_role` (
  `user_id` INT(11) NOT NULL,
  `role_id` INT(11) NOT NULL,
  PRIMARY KEY (`user_id`, `role_id`),
  INDEX `role_id` (`role_id` ASC),
  CONSTRAINT `user_role_ibfk_1`
    FOREIGN KEY (`user_id`)
    REFERENCES `NGL`.`user` (`id`),
  CONSTRAINT `user_role_ibfk_2`
    FOREIGN KEY (`role_id`)
    REFERENCES `NGL`.`role` (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`user_team` (
  `user_id` INT(11) NOT NULL,
  `team_id` INT(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`user_id`, `team_id`),
  INDEX `team_id` (`team_id` ASC),
  CONSTRAINT `user_team_ibfk_1`
    FOREIGN KEY (`user_id`)
    REFERENCES `NGL`.`user` (`id`),
  CONSTRAINT `user_team_ibfk_2`
    FOREIGN KEY (`team_id`)
    REFERENCES `NGL`.`team` (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`value` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `code` VARCHAR(255) NOT NULL,
  `value` VARCHAR(255) NOT NULL,
  `name` VARCHAR(255) NOT NULL,
  `default_value` TINYINT(1) NOT NULL DEFAULT '0',
  `fk_property_definition` BIGINT(20) NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `property_definition_id` (`fk_property_definition` ASC),
  CONSTRAINT `value_ibfk_1`
    FOREIGN KEY (`fk_property_definition`)
    REFERENCES `NGL`.`property_definition` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
AUTO_INCREMENT = 13
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`satellite_experiment_type` (
  `fk_experiment_type` BIGINT(20) NOT NULL,
  `fk_experiment_type_node` BIGINT(20) NOT NULL,
  PRIMARY KEY (`fk_experiment_type`, `fk_experiment_type_node`),
  INDEX `fk_node_idx` (`fk_experiment_type_node` ASC),
  INDEX `fk_experiment_type_idx` (`fk_experiment_type` ASC),
  CONSTRAINT `fk_experiment_type`
    FOREIGN KEY (`fk_experiment_type`)
    REFERENCES `NGL`.`experiment_type` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_node`
    FOREIGN KEY (`fk_experiment_type_node`)
    REFERENCES `NGL`.`experiment_type_node` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`protocol_reagent_type` (
  `fk_protocol` BIGINT(20) NOT NULL,
  `fk_reagent_type` BIGINT(20) NOT NULL,
  PRIMARY KEY (`fk_protocol`, `fk_reagent_type`),
  INDEX `fk_reagent_type` (`fk_reagent_type` ASC),
  CONSTRAINT `protocol_reagent_type_ibfk_100`
    FOREIGN KEY (`fk_protocol`)
    REFERENCES `NGL`.`protocol` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `protocol_reagent_type_ibfk_200`
    FOREIGN KEY (`fk_reagent_type`)
    REFERENCES `NGL`.`reagent_type` (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`resolution_category` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `code` VARCHAR(255) NOT NULL,
  `display_order` INT(5) NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `code` (`code` ASC),
  INDEX `idx_state_category_code` (`code` ASC))
ENGINE = InnoDB
AUTO_INCREMENT = 1395
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`level` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `code` VARCHAR(45) NULL DEFAULT NULL,
  `name` VARCHAR(45) NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `code_UNIQUE` (`code` ASC),
  UNIQUE INDEX `name_UNIQUE` (`name` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`property_definition_level` (
  `fk_property_definition` BIGINT(20) NOT NULL,
  `fk_level` BIGINT(20) NOT NULL,
  PRIMARY KEY (`fk_property_definition`, `fk_level`),
  INDEX `fk_level` (`fk_level` ASC),
  CONSTRAINT `property_definition_level_ibfk_1`
    FOREIGN KEY (`fk_property_definition`)
    REFERENCES `NGL`.`property_definition` (`id`),
  CONSTRAINT `property_definition_level_ibfk_2`
    FOREIGN KEY (`fk_level`)
    REFERENCES `NGL`.`level` (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`treatment_type` (
  `id` BIGINT(20) NOT NULL,
  `names` VARCHAR(100) NULL DEFAULT NULL,
  `fk_common_info_type` BIGINT(20) NOT NULL,
  `fk_treatment_category` BIGINT(20) NOT NULL,
  `display_orders` VARCHAR(45) NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_common_info_type` (`fk_common_info_type` ASC),
  INDEX `fk_treatment_category` (`fk_treatment_category` ASC),
  CONSTRAINT `treatment_type_ibfk_1`
    FOREIGN KEY (`fk_common_info_type`)
    REFERENCES `NGL`.`common_info_type` (`id`),
  CONSTRAINT `treatment_type_ibfk_2`
    FOREIGN KEY (`fk_treatment_category`)
    REFERENCES `NGL`.`treatment_category` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`treatment_category` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `code` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `code` (`code` ASC),
  INDEX `idx_project_category_code` (`code` ASC))
ENGINE = InnoDB
AUTO_INCREMENT = 2
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`treatment_context` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `code` VARCHAR(45) NOT NULL,
  `name` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `code_UNIQUE` (`code` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`treatment_type_context` (
  `fk_treatment_type` BIGINT(20) NOT NULL,
  `fk_treatment_context` INT(11) NOT NULL,
  `required` TINYINT(1) NOT NULL,
  PRIMARY KEY (`fk_treatment_type`, `fk_treatment_context`),
  INDEX `fk_treatment_type_context_fk1_idx` (`fk_treatment_type` ASC),
  INDEX `fk_treatment_type_context_fk2_idx` (`fk_treatment_context` ASC),
  CONSTRAINT `fk_treatment_type_context_fk1`
    FOREIGN KEY (`fk_treatment_type`)
    REFERENCES `NGL`.`treatment_type` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_treatment_type_context_fk2`
    FOREIGN KEY (`fk_treatment_context`)
    REFERENCES `NGL`.`treatment_context` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`run_type` (
  `id` BIGINT(20) NOT NULL,
  `nb_lanes` INT(11) NOT NULL,
  `fk_common_info_type` BIGINT(20) NOT NULL,
  `fk_run_category` INT(11) NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `run_type_ibfk_1_idx` (`fk_common_info_type` ASC),
  INDEX `run_type_ibfk_2_idx` (`fk_run_category` ASC),
  CONSTRAINT `run_type_ibfk_1`
    FOREIGN KEY (`fk_common_info_type`)
    REFERENCES `NGL`.`common_info_type` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `run_type_ibfk_2`
    FOREIGN KEY (`fk_run_category`)
    REFERENCES `NGL`.`run_category` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`run_category` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(45) NOT NULL,
  `code` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `code_UNIQUE` (`code` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`readset_type` (
  `id` BIGINT(20) NOT NULL,
  `fk_common_info_type` BIGINT(20) NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `readset_type_ibfk_1` (`fk_common_info_type` ASC),
  CONSTRAINT `readset_type_ibfk_10`
    FOREIGN KEY (`fk_common_info_type`)
    REFERENCES `NGL`.`common_info_type` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`state_object_type` (
  `fk_state` BIGINT(20) NOT NULL,
  `fk_object_type` BIGINT(20) NOT NULL,
  PRIMARY KEY (`fk_state`, `fk_object_type`),
  INDEX `fk_state_object_type_fk1_idx` (`fk_state` ASC),
  INDEX `fk_state_object_type_fk2_idx` (`fk_object_type` ASC),
  CONSTRAINT `fk_state_object_type_fk1`
    FOREIGN KEY (`fk_state`)
    REFERENCES `NGL`.`state` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_state_object_type_fk2`
    FOREIGN KEY (`fk_object_type`)
    REFERENCES `NGL`.`object_type` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`institute` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `code` VARCHAR(10) NOT NULL,
  `name` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `code_UNIQUE` (`code` ASC),
  UNIQUE INDEX `name_UNIQUE` (`name` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`common_info_type_institute` (
  `fk_institute` INT(11) NOT NULL,
  `fk_common_info_type` BIGINT(20) NOT NULL,
  PRIMARY KEY (`fk_institute`, `fk_common_info_type`),
  INDEX `fk_institute_common_info_type_fk1_idx` (`fk_institute` ASC),
  INDEX `fk_institute_common_info_type_fk2_idx` (`fk_common_info_type` ASC),
  CONSTRAINT `fk_institute_common_info_type_fk1`
    FOREIGN KEY (`fk_institute`)
    REFERENCES `NGL`.`institute` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_institute_common_info_type_fk2`
    FOREIGN KEY (`fk_common_info_type`)
    REFERENCES `NGL`.`common_info_type` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`user_application` (
  `user_id` INT(11) NOT NULL,
  `application_id` INT(11) NOT NULL,
  PRIMARY KEY (`user_id`, `application_id`),
  INDEX `fk_user_application_application_02_idx` (`application_id` ASC),
  CONSTRAINT `fk_user_application_application_02`
    FOREIGN KEY (`application_id`)
    REFERENCES `NGL`.`application` (`id`),
  CONSTRAINT `fk_user_application_user_01`
    FOREIGN KEY (`user_id`)
    REFERENCES `NGL`.`user` (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`application` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `label` VARCHAR(255) NULL DEFAULT NULL,
  `code` VARCHAR(255) NULL DEFAULT NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB
AUTO_INCREMENT = 3
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`instrument_institute` (
  `fk_instrument` BIGINT(20) NOT NULL,
  `fk_institute` INT(11) NOT NULL,
  PRIMARY KEY (`fk_instrument`, `fk_institute`),
  INDEX `fk_instrument_fk1` (`fk_instrument` ASC),
  INDEX `fk_institute_fk2` (`fk_institute` ASC),
  CONSTRAINT `instrument_institute_ibfk_1`
    FOREIGN KEY (`fk_instrument`)
    REFERENCES `NGL`.`instrument` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `instrument_institute_ibfk_2`
    FOREIGN KEY (`fk_institute`)
    REFERENCES `NGL`.`institute` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`valuation_criteria_common_info_type` (
  `fk_common_info_type` BIGINT(20) NOT NULL,
  `fk_valuation_criteria` BIGINT(20) NOT NULL,
  PRIMARY KEY (`fk_common_info_type`, `fk_valuation_criteria`),
  UNIQUE INDEX `code` (`fk_common_info_type` ASC, `fk_valuation_criteria` ASC),
  INDEX `c_val_criteria_fk_idx` (`fk_valuation_criteria` ASC),
  CONSTRAINT `c_common_info_type_fk`
    FOREIGN KEY (`fk_common_info_type`)
    REFERENCES `NGL`.`common_info_type` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `c_val_criteria_fk`
    FOREIGN KEY (`fk_valuation_criteria`)
    REFERENCES `NGL`.`valuation_criteria` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`valuation_criteria` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `code` VARCHAR(255) NOT NULL,
  `name` VARCHAR(255) NOT NULL,
  `path` VARCHAR(255) NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `code` (`code` ASC))
ENGINE = InnoDB
AUTO_INCREMENT = 1519
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`valuation_criteria_institute` (
  `fk_valuation_criteria` BIGINT(20) NOT NULL,
  `fk_institute` INT(11) NOT NULL,
  PRIMARY KEY (`fk_valuation_criteria`, `fk_institute`),
  INDEX `fk_valuation_criteria_fk1` (`fk_valuation_criteria` ASC),
  INDEX `fk_institute_fk2` (`fk_institute` ASC),
  CONSTRAINT `valuation_criteria_institute_ibfk_1`
    FOREIGN KEY (`fk_valuation_criteria`)
    REFERENCES `NGL`.`valuation_criteria` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `valuation_criteria_institute_ibfk_2`
    FOREIGN KEY (`fk_institute`)
    REFERENCES `NGL`.`institute` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`resolution_object_type` (
  `fk_resolution` BIGINT(20) NOT NULL,
  `fk_object_type` BIGINT(20) NOT NULL,
  PRIMARY KEY (`fk_resolution`, `fk_object_type`),
  INDEX `fk_resolution_object_type_fk1_idx` (`fk_resolution` ASC),
  INDEX `fk_resolution_object_type_fk2_idx` (`fk_object_type` ASC),
  CONSTRAINT `fk_resolution_object_type_fk1`
    FOREIGN KEY (`fk_resolution`)
    REFERENCES `NGL`.`resolution` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_resolution_object_type_fk2`
    FOREIGN KEY (`fk_object_type`)
    REFERENCES `NGL`.`object_type` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`resolution_institute` (
  `fk_resolution` BIGINT(20) NOT NULL,
  `fk_institute` INT(11) NOT NULL,
  PRIMARY KEY (`fk_resolution`, `fk_institute`),
  INDEX `fk_resolution_fk1` (`fk_resolution` ASC),
  INDEX `fk_institute_fk2` (`fk_institute` ASC),
  CONSTRAINT `resolution_institute_ibfk_1`
    FOREIGN KEY (`fk_resolution`)
    REFERENCES `NGL`.`resolution` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `resolution_institute_ibfk_2`
    FOREIGN KEY (`fk_institute`)
    REFERENCES `NGL`.`institute` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`analysis_type` (
  `id` BIGINT(20) NOT NULL,
  `fk_common_info_type` BIGINT(20) NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `readset_type_ibfk_1` (`fk_common_info_type` ASC),
  CONSTRAINT `readset_type_ibfk_100`
    FOREIGN KEY (`fk_common_info_type`)
    REFERENCES `NGL`.`common_info_type` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `NGL`.`state_object_type_hierarchy` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `code` VARCHAR(255) NOT NULL,
  `fk_child_state` BIGINT(20) NOT NULL,
  `fk_object_type` BIGINT(20) NOT NULL,
  `fk_parent_state` BIGINT(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `code_UNIQUE` (`code` ASC),
  INDEX `code_compose` (`fk_child_state` ASC, `fk_object_type` ASC),
  INDEX `state2_idx` (`fk_parent_state` ASC),
  INDEX `object_type_idx` (`fk_object_type` ASC),
  CONSTRAINT `object_type`
    FOREIGN KEY (`fk_object_type`)
    REFERENCES `NGL`.`object_type` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `state1`
    FOREIGN KEY (`fk_child_state`)
    REFERENCES `NGL`.`state` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `state2`
    FOREIGN KEY (`fk_parent_state`)
    REFERENCES `NGL`.`state` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
AUTO_INCREMENT = 245
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
