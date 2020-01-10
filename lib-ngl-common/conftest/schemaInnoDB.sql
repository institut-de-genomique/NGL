DROP TABLE IF EXISTS experiment_type_node_experiment_type;
DROP TABLE IF EXISTS previous_nodes;
DROP TABLE IF EXISTS experiment_type_node;
DROP TABLE IF EXISTS common_info_type_instrument_type;
DROP TABLE IF EXISTS process_experiment_type;
DROP TABLE IF EXISTS protocol_reagent_type;
DROP TABLE IF EXISTS instrument;
DROP TABLE IF EXISTS instrument_used_type;
DROP TABLE IF EXISTS process_type;
DROP TABLE IF EXISTS experiment_type;
DROP TABLE IF EXISTS reagent_type;
DROP TABLE IF EXISTS sample_type;
DROP TABLE IF EXISTS import_type;
DROP TABLE IF EXISTS project_type;
DROP TABLE IF EXISTS common_info_type_protocol;
DROP TABLE IF EXISTS common_info_type_resolution;
DROP TABLE IF EXISTS common_info_type_state;
DROP TABLE IF EXISTS value;
DROP TABLE IF EXISTS property_definition;
DROP TABLE IF EXISTS common_info_type;
DROP TABLE IF EXISTS object_type;
DROP TABLE IF EXISTS resolution;
DROP TABLE IF EXISTS state;
DROP TABLE IF EXISTS protocol;
DROP TABLE IF EXISTS measure_value;
DROP TABLE IF EXISTS instrumentCategory_inContainerSupportCategory;
DROP TABLE IF EXISTS instrumentCategory_outContainerSupportCategory;
DROP TABLE IF EXISTS measure_category;
DROP TABLE IF EXISTS protocol_category;
DROP TABLE IF EXISTS container_category;
DROP TABLE IF EXISTS container_support_category;
DROP TABLE IF EXISTS experiment_category;
DROP TABLE IF EXISTS instrument_category;
DROP TABLE IF EXISTS sample_category;
DROP TABLE IF EXISTS import_category;
DROP TABLE IF EXISTS process_category;
DROP TABLE IF EXISTS project_category;
DROP TABLE IF EXISTS state_category;
DROP TABLE IF EXISTS role_permission;
DROP TABLE IF EXISTS user_equipe;
DROP TABLE IF EXISTS user_role;
DROP TABLE IF EXISTS user_team;
DROP TABLE IF EXISTS equipe;
DROP TABLE IF EXISTS permission;
DROP TABLE IF EXISTS role;
DROP TABLE IF EXISTS team;
DROP TABLE IF EXISTS user;
DROP TABLE IF EXISTS play_evolutions;
--
-- Table structure for table `object_type`
--

CREATE TABLE object_type (
  id bigint(20) NOT NULL auto_increment,
  code varchar(255) NOT NULL unique,
  generic tinyint(1) NOT NULL default '0',
  PRIMARY KEY  (id),
  INDEX idx_object_type_code (code)
) ENGINE=InnoDB;

--
-- Table structure for table `common_info_type`
--

CREATE TABLE common_info_type (
  id bigint(20) NOT NULL auto_increment,
  name varchar(255) NOT NULL,
  code varchar(255) NOT NULL unique,
  collection_name varchar(255) NOT NULL,
  fk_object_type bigint(20) NOT NULL,
  PRIMARY KEY  (id),
  FOREIGN KEY (fk_object_type) REFERENCES object_type(id),
  INDEX idx_common_code (code)
) ENGINE=InnoDB;

--
-- Table structure for table `resolution`
--

CREATE TABLE resolution (
  id bigint(20) NOT NULL auto_increment,
  name varchar(255) NOT NULL,
  code varchar(255) NOT NULL unique,
  PRIMARY KEY  (id),
  INDEX idx_resolution_code (code)
) ENGINE=InnoDB;

--
-- Table structure for table `common_info_type_resolution`
--

CREATE TABLE common_info_type_resolution (
  fk_common_info_type bigint(20) NOT NULL,
  fk_resolution bigint(20) NOT NULL,
  PRIMARY KEY  (fk_common_info_type,fk_resolution),
  FOREIGN KEY (fk_common_info_type) REFERENCES common_info_type(id),
  FOREIGN KEY (fk_resolution) REFERENCES resolution(id)
) ENGINE=InnoDB;

--
-- Table structure for table `state_category`
--

CREATE TABLE state_category(
	id bigint(20) NOT NULL auto_increment,
	name varchar(255) NOT NULL,
	code varchar(255) NOT NULL unique,
	PRIMARY KEY(id),
	INDEX idx_state_category_code (code)
)ENGINE=InnoDB;

--
-- Table structure for table `state`
--

CREATE TABLE state (
  id bigint(20) NOT NULL auto_increment,
  name varchar(255) NOT NULL,
  code varchar(255) NOT NULL unique,
  active tinyint(1) NOT NULL default '0',
  priority int(11) default NULL,
  fk_state_category bigint(20) NOT NULL,
  PRIMARY KEY  (id),
  FOREIGN KEY(fk_state_category) REFERENCES state_category(id),
  INDEX idx_state_code (code)
) ENGINE=InnoDB;

--
-- Table structure for table `common_info_type_state`
--

CREATE TABLE common_info_type_state (
  fk_common_info_type bigint(20) NOT NULL,
  fk_state bigint(20) NOT NULL,
  PRIMARY KEY  (fk_common_info_type,fk_state),
  FOREIGN KEY (fk_common_info_type) REFERENCES common_info_type(id),
  FOREIGN KEY (fk_state) REFERENCES state(id)
) ENGINE=InnoDB;

--
-- Table structure for table `measure_category`
--

CREATE TABLE measure_category (
  id bigint(20) NOT NULL auto_increment,
  name varchar(255) NOT NULL,
  code varchar(255) NOT NULL unique,
  PRIMARY KEY  (id),
  INDEX idx_measure_category_code (code)
) ENGINE=InnoDB;

--
-- Table structure for table `measure_value`
--

CREATE TABLE measure_value (
  id bigint(20) NOT NULL auto_increment,
  code varchar(255) NOT NULL unique,
  value varchar(255) NOT NULL,
  default_value tinyint(1) NOT NULL default '0',
  measure_category_id bigint(20) default NULL,
  PRIMARY KEY  (id),
 FOREIGN KEY (measure_category_id) REFERENCES measure_category(id),
 INDEX idx_measure_value_code (code)
) ENGINE=InnoDB;


--
-- Table structure for table `property_definition`
--

CREATE TABLE property_definition (
  id bigint(20) NOT NULL auto_increment,
  code varchar(255) NOT NULL,
  name varchar(255) NOT NULL,
  required tinyint(1) NOT NULL default '0',
  active tinyint(1) NOT NULL default '0',
  type varchar(255) NOT NULL,
  display_format varchar(255) default NULL,
  display_order int(11) default NULL,
  default_value varchar(255) default NULL,
  description text,
  level enum('current','content','container') NOT NULL,
  in_out enum('in','out') default NULL,
  propagation tinyint(1) default NULL,
  choice_in_list tinyint(1) NOT NULL default '0',
  measure_category_id bigint(20) default NULL,
  measure_value_id bigint(20) default NULL,
  display_measure_value_id bigint(20) default NULL,
  common_info_type_id bigint(20) default NULL,
  PRIMARY KEY  (id),
  FOREIGN KEY (measure_category_id) REFERENCES measure_category(id),
  FOREIGN KEY (measure_value_id) REFERENCES measure_value(id),
  FOREIGN KEY (display_measure_value_id) REFERENCES measure_value(id),
  FOREIGN KEY (common_info_type_id) REFERENCES common_info_type(id),
  INDEX idx_property_def_code (code),
  CONSTRAINT UNIQUE (code,common_info_type_id)
) ENGINE=InnoDB;

--
-- Table structure for table `value`
--

CREATE TABLE value (
  id bigint(20) NOT NULL auto_increment,
  code varchar(255) NOT NULL unique,
  value varchar(255) NOT NULL,
  default_value tinyint(1) NOT NULL default '0',
  property_definition_id bigint(20) default NULL,
  PRIMARY KEY  (id),
  FOREIGN KEY (property_definition_id) REFERENCES property_definition(id),
  INDEX idx_value_code (code)
) ENGINE=InnoDB;

--
-- Table structure for table `experiment_category`
--

CREATE TABLE experiment_category (
  id bigint(20) NOT NULL auto_increment,
  name varchar(255) NOT NULL,
  code varchar(255) NOT NULL unique,
  PRIMARY KEY  (id),
  INDEX idx_exp_category_code (code)
) ENGINE=InnoDB;

--
-- Table structure for table `experiment_type`
--

CREATE TABLE experiment_type (
  id bigint(20) NOT NULL,
  fk_experiment_category bigint(20) NOT NULL,
  fk_common_info_type bigint(20) NOT NULL,
  atomic_transfert_method varchar(255) NOT NULL,
  PRIMARY KEY  (id),
  FOREIGN KEY (fk_experiment_category) REFERENCES experiment_category(id),
  FOREIGN KEY (fk_common_info_type) REFERENCES common_info_type(id)
) ENGINE=InnoDB;


CREATE TABLE experiment_type_node (
  id bigint(20) NOT NULL,
  fk_experiment_type bigint(20) NOT NULL,
  doPurification tinyint(1) NOT NULL default '0',
  mandatoryPurification tinyint(1) NOT NULL default '0',
  doQualityControl tinyint(1) NOT NULL default '0',
  mandatoryQualityControl tinyint(1) NOT NULL default '0',
  PRIMARY KEY  (id),
  FOREIGN KEY (fk_experiment_type) REFERENCES experiment_type(id) 
) ENGINE=InnoDB;


--
-- Table structure for table `previous_experiment_types`
--

CREATE TABLE previous_nodes (
  fk_previous_node bigint(20) NOT NULL,
  fk_node bigint(20) NOT NULL,  
  PRIMARY KEY  (fk_previous_node,fk_node),
  FOREIGN KEY (fk_previous_node) REFERENCES experiment_type_node(id),
  FOREIGN KEY (fk_node) REFERENCES experiment_type_node(id)
) ENGINE=InnoDB;

--
-- Table structure for table `experiment_purification_method`
--

CREATE TABLE experiment_type_node_experiment_type (
  fk_experiment_type bigint(20) NOT NULL,
  fk_experiment_type_node bigint(20) NOT NULL,
  PRIMARY KEY  (fk_experiment_type,fk_experiment_type_node),
  FOREIGN KEY (fk_experiment_type) REFERENCES experiment_type(id),
  FOREIGN KEY (fk_experiment_type_node) REFERENCES experiment_type_node(id)
) ENGINE=InnoDB;

--
-- Table structure for table `instrument_category`
--

CREATE TABLE instrument_category (
  id bigint(20) NOT NULL auto_increment,
  name varchar(255) NOT NULL,
  code varchar(255) NOT NULL unique,
  nbInContainerSupportCategories int(11) default NULL,
  nbOutContainerSupportCategories int(11) default NULL,
  PRIMARY KEY  (id),
  INDEX idx_inst_category_code (code)
) ENGINE=InnoDB;

--
-- Table structure for table `instrument_used_type`
--

CREATE TABLE instrument_used_type (
  id bigint(20) NOT NULL,
  fk_common_info_type bigint(20) NOT NULL,
  fk_instrument_category bigint(20) NOT NULL,
  PRIMARY KEY  (id),
  FOREIGN KEY (fk_common_info_type) REFERENCES common_info_type(id),
  FOREIGN KEY (fk_instrument_category) REFERENCES instrument_category(id)
) ENGINE=InnoDB;

--
-- Table structure for table `instrument`
--

CREATE TABLE instrument (
  id bigint(20) NOT NULL auto_increment,
  name varchar(255) default NULL,
  code varchar(255) NOT NULL unique,
  instrument_used_type_id bigint(20) default NULL,
  PRIMARY KEY  (id),
   FOREIGN KEY (instrument_used_type_id) REFERENCES instrument_used_type(id),
   INDEX idx_instrument_code (code)
) ENGINE=InnoDB;

--
-- Table structure for table `common_info_type_instrument_type`
--

CREATE TABLE common_info_type_instrument_type (
  fk_common_info_type bigint(20) NOT NULL,
  fk_instrument_type bigint(20) NOT NULL,
  PRIMARY KEY  (fk_common_info_type,fk_instrument_type),
   FOREIGN KEY (fk_common_info_type) REFERENCES common_info_type(id),
    FOREIGN KEY (fk_instrument_type) REFERENCES instrument_used_type(id)
) ENGINE=InnoDB;

--
-- Table structure for table `container_category`
--

CREATE TABLE container_category (
  id bigint(20) NOT NULL auto_increment,
  name varchar(255) NOT NULL,
  code varchar(255) NOT NULL unique,
  PRIMARY KEY  (id),
  INDEX idx_container_category_code (code)
) ENGINE=InnoDB;

--
-- Table structure for table `container_support_category`
--

CREATE TABLE container_support_category(
  id bigint(20) NOT NULL auto_increment,
  name varchar(255) NOT NULL,
  code varchar(255) NOT NULL unique,
  nbUsableContainer int(11) NOT NULL,
  nbLine int(11) default NULL,
  nbColumn int(11) default NULL,
  PRIMARY KEY  (`id`),
  INDEX idx_container_support_category_code (code)
) ENGINE=InnoDB;

--
-- Table structure for table `instrumentCategory_inContainerSupportCategory`
--

CREATE TABLE instrumentCategory_inContainerSupportCategory (
  fk_instrument_category bigint(20) NOT NULL,
  fk_container_support_category bigint(20) NOT NULL,
  PRIMARY KEY  (fk_instrument_category,fk_container_support_category),
  FOREIGN KEY (fk_instrument_category) REFERENCES instrument_category(id),
  FOREIGN KEY (fk_container_support_category) REFERENCES container_support_category(id)
) ENGINE=InnoDB;


--
-- Table structure for table `instrumentCategory_outContainerSupportCategory`
--

CREATE TABLE instrumentCategory_outContainerSupportCategory (
  fk_instrument_category bigint(20) NOT NULL,
  fk_container_support_category bigint(20) NOT NULL,
  PRIMARY KEY  (fk_instrument_category,fk_container_support_category),
  FOREIGN KEY (fk_instrument_category) REFERENCES instrument_category(id),
   FOREIGN KEY (fk_container_support_category) REFERENCES container_support_category(id)
) ENGINE=InnoDB;




--
-- Table structure for table `reagent_type`
--

CREATE TABLE reagent_type (
  id bigint(20) NOT NULL,
  fk_common_info_type bigint(20) NOT NULL,
  PRIMARY KEY  (id),
  FOREIGN KEY (fk_common_info_type) REFERENCES common_info_type(id)
) ENGINE=InnoDB;

--
-- Table structure for table `protocol_category`
--

CREATE TABLE protocol_category (
  id bigint(20) NOT NULL auto_increment,
  name varchar(255) NOT NULL,
  code varchar(255) NOT NULL unique,
  PRIMARY KEY  (id),
  INDEX idx_protocol_category_code (code)
) ENGINE=InnoDB;

--
-- Table structure for table `protocol`
--

CREATE TABLE protocol (
  id bigint(20) NOT NULL auto_increment,
  code varchar(255) NOT NULL unique,
  name varchar(255) default NULL,
  file_path varchar(255) default NULL,
  fk_protocol_category bigint(20) NOT NULL,
  version varchar(255) NOT NULL,
  PRIMARY KEY  (id),
   FOREIGN KEY (fk_protocol_category) REFERENCES protocol_category(id),
   INDEX idx_protocol_code (code)
) ENGINE=InnoDB;

--
-- Table structure for table `protocol_reagent_type`
--

CREATE TABLE protocol_reagent_type (
  fk_protocol bigint(20) NOT NULL,
  fk_reagent_type bigint(20) NOT NULL,
  PRIMARY KEY  (fk_protocol,fk_reagent_type),
  FOREIGN KEY (fk_protocol) REFERENCES protocol(id),
  FOREIGN KEY (fk_reagent_type) REFERENCES reagent_type(id)
) ENGINE=InnoDB;

--
-- Table structure for table `common_info_type_protocol`
--

CREATE TABLE common_info_type_protocol (
  fk_common_info_type bigint(20) NOT NULL,
  fk_protocol bigint(20) NOT NULL,
  PRIMARY KEY  (fk_common_info_type,fk_protocol),
  FOREIGN KEY (fk_common_info_type) REFERENCES common_info_type(id),
  FOREIGN KEY (fk_protocol) REFERENCES protocol(id)
) ENGINE=InnoDB;

--
-- Table structure for table `sample_category`
--

CREATE TABLE sample_category (
  id bigint(20) NOT NULL auto_increment,
  name varchar(255) NOT NULL,
  code varchar(255) NOT NULL unique,
  PRIMARY KEY  (id),
  INDEX idx_sample_category_code (code)
) ENGINE=InnoDB;

--
-- Table structure for table `sample_type`
--

CREATE TABLE sample_type (
  id bigint(20) NOT NULL,
  fk_common_info_type bigint(20) NOT NULL,
  fk_sample_category bigint(20) NOT NULL,
  PRIMARY KEY  (id),
   FOREIGN KEY (fk_common_info_type) REFERENCES common_info_type(id),
    FOREIGN KEY (fk_sample_category) REFERENCES sample_category(id)
) ENGINE=InnoDB;


--
-- Table structure for table `import_category`
--

CREATE TABLE import_category (
  id bigint(20) NOT NULL auto_increment,
  name varchar(255) NOT NULL,
  code varchar(255) NOT NULL unique,
  PRIMARY KEY  (id),
  INDEX idx_import_category_code (code)
) ENGINE=InnoDB;

--
-- Table structure for table `import_type`
--

CREATE TABLE import_type (
  id bigint(20) NOT NULL,
  fk_common_info_type bigint(20) NOT NULL,
  fk_import_category bigint(20) NOT NULL,
  PRIMARY KEY  (id),
 FOREIGN KEY (fk_common_info_type) REFERENCES common_info_type(id),
 FOREIGN KEY (fk_import_category) REFERENCES import_category(id)
) ENGINE=InnoDB;

--
-- Table structure for table `process_category`
--

CREATE TABLE process_category (
  id bigint(20) NOT NULL auto_increment,
  name varchar(255) NOT NULL,
  code varchar(255) NOT NULL unique,
  PRIMARY KEY  (id),
  INDEX idx_process_category_code (code)
) ENGINE=InnoDB;

--
-- Table structure for table `process_type`
--

CREATE TABLE process_type (
  id bigint(20) NOT NULL,
  fk_common_info_type bigint(20) NOT NULL,
  fk_process_category bigint(20) NOT NULL,
  fk_void_experiment_type bigint(20) NOT NULL,
  fk_first_experiment_type bigint(20) NOT NULL,
  fk_last_experiment_type bigint(20) NOT NULL,
  PRIMARY KEY  (id),
 FOREIGN KEY (fk_common_info_type) REFERENCES common_info_type(id),
 FOREIGN KEY (fk_process_category) REFERENCES process_category(id),
 FOREIGN KEY (fk_void_experiment_type) REFERENCES experiment_type(id),
 FOREIGN KEY (fk_first_experiment_type) REFERENCES experiment_type(id),
 FOREIGN KEY (fk_last_experiment_type) REFERENCES experiment_type(id)
) ENGINE=InnoDB;


--
-- Table structure for table `process_experiment_type`
--

CREATE TABLE process_experiment_type (
  fk_process_type bigint(20) NOT NULL,
  fk_experiment_type bigint(20) NOT NULL,
  PRIMARY KEY  (fk_process_type,fk_experiment_type),
  FOREIGN KEY (fk_process_type) REFERENCES process_type(id),
  FOREIGN KEY (fk_experiment_type) REFERENCES experiment_type(id)
) ENGINE=InnoDB;

--
-- Table structure for table `project_category`
--

CREATE TABLE project_category (
  id bigint(20) NOT NULL auto_increment,
  name varchar(255) NOT NULL,
  code varchar(255) NOT NULL unique,
  PRIMARY KEY  (id),
  INDEX idx_project_category_code (code)
) ENGINE=InnoDB;

--
-- Table structure for table `project_type`
--

CREATE TABLE project_type (
  id bigint(20) NOT NULL,
  fk_common_info_type bigint(20) NOT NULL,
  fk_project_category bigint(20) NOT NULL,
  PRIMARY KEY  (id),
 FOREIGN KEY (fk_common_info_type) REFERENCES common_info_type(id),
 FOREIGN KEY (fk_project_category) REFERENCES project_category(id)
) ENGINE=InnoDB;

--
-- Table structure for table `equipe`
--

CREATE TABLE equipe (
  id int(11) NOT NULL auto_increment,
  nom varchar(255) default NULL,
  PRIMARY KEY  (id)
) ENGINE=InnoDB;

--
-- Table structure for table `permission`
--

CREATE TABLE permission (
  id int(11) NOT NULL auto_increment,
  label varchar(255) default NULL,
  code varchar(255) default NULL,
  PRIMARY KEY  (id),
  INDEX idx_permission_code (code)
) ENGINE=InnoDB;

--
-- Table structure for table `role`
--

CREATE TABLE role (
  id int(11) NOT NULL auto_increment,
  label varchar(255) default NULL,
  PRIMARY KEY  (id)
) ENGINE=InnoDB;


--
-- Table structure for table `role_permission`
--

CREATE TABLE role_permission (
  role_id int(11) NOT NULL,
  permission_id int(11) NOT NULL,
  PRIMARY KEY  (role_id,permission_id),
  FOREIGN KEY (role_id) REFERENCES role(id),
  FOREIGN KEY (permission_id) REFERENCES permission(id)
) ENGINE=InnoDB;

--
-- Table structure for table `team`
--

CREATE TABLE team (
  id int(11) NOT NULL auto_increment,
  nom varchar(255) default NULL,
  PRIMARY KEY  (id)
) ENGINE=InnoDB;

--
-- Table structure for table `user`
--

CREATE TABLE user (
  id int(11) NOT NULL auto_increment,
  login varchar(255) default NULL,
  firstname varchar(255) default NULL,
  lastname varchar(255) default NULL,
  email varchar(255) default NULL,
  technicaluser int(11) default NULL,
  password varchar(255) default NULL,
  confirmpassword varchar(255) default NULL,
  PRIMARY KEY  (id)
) ENGINE=InnoDB;


--
-- Table structure for table `user_equipe`
--

CREATE TABLE user_equipe (
  user_id int(11) NOT NULL,
  equipe_id int(11) NOT NULL,
  PRIMARY KEY  (user_id,equipe_id),
  FOREIGN KEY (user_id) REFERENCES user(id),
   FOREIGN KEY (equipe_id) REFERENCES equipe(id)
) ENGINE=InnoDB;


--
-- Table structure for table `user_role`
--

CREATE TABLE user_role (
  user_id int(11) NOT NULL,
  role_id int(11) NOT NULL,
  PRIMARY KEY  (user_id,role_id),
  FOREIGN KEY (user_id) REFERENCES user(id),
  FOREIGN KEY (role_id) REFERENCES role(id)
) ENGINE=InnoDB;

--
-- Table structure for table `user_team`
--

CREATE TABLE user_team (
  user_id int(11) NOT NULL,
  team_id int(11) NOT NULL default '0',
  PRIMARY KEY  (user_id,team_id),
  FOREIGN KEY (user_id) REFERENCES user(id),
  FOREIGN KEY (team_id) REFERENCES team(id)
) ENGINE=InnoDB;


INSERT INTO `object_type`(`code`,`generic`) VALUES('Project','0');
INSERT INTO `object_type`(`code`,`generic`) VALUES('Process','0');
INSERT INTO `object_type`(`code`,`generic`) VALUES('Sample','0');
INSERT INTO `object_type`(`code`,`generic`) VALUES('Instrument','0');
INSERT INTO `object_type`(`code`,`generic`) VALUES('Reagent','1');
INSERT INTO `object_type`(`code`,`generic`) VALUES('Experiment','0');
INSERT INTO `object_type`(`code`,`generic`) VALUES('Import','0');