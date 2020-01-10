# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table application (
  id                        integer auto_increment not null,
  label                     varchar(255),
  code                      varchar(255),
  constraint pk_application primary key (id))
;

create table permission (
  id                        integer auto_increment not null,
  label                     varchar(255),
  code                      varchar(255),
  constraint pk_permission primary key (id))
;

create table role (
  id                        integer auto_increment not null,
  label                     varchar(255),
  constraint pk_role primary key (id))
;

create table team (
  id                        integer auto_increment not null,
  nom                       varchar(255),
  constraint pk_team primary key (id))
;

create table user (
  id                        integer auto_increment not null,
  login                     varchar(255),
  firstname                 varchar(255),
  lastname                  varchar(255),
  email                     varchar(255),
  technicaluser             integer,
  password                  varchar(255),
  confirmpassword           varchar(255),
  constraint pk_user primary key (id))
;


create table role_permission (
  role_id                        integer not null,
  permission_id                  integer not null,
  constraint pk_role_permission primary key (role_id, permission_id))
;

create table user_role (
  user_id                        integer not null,
  role_id                        integer not null,
  constraint pk_user_role primary key (user_id, role_id))
;

create table user_team (
  user_id                        integer not null,
  team_id                        integer not null,
  constraint pk_user_team primary key (user_id, team_id))
;

create table user_application (
  user_id                        integer not null,
  application_id                 integer not null,
  constraint pk_user_application primary key (user_id, application_id))
;



alter table role_permission add constraint fk_role_permission_role_01 foreign key (role_id) references role (id) on delete restrict on update restrict;

alter table role_permission add constraint fk_role_permission_permission_02 foreign key (permission_id) references permission (id) on delete restrict on update restrict;

alter table user_role add constraint fk_user_role_user_01 foreign key (user_id) references user (id) on delete restrict on update restrict;

alter table user_role add constraint fk_user_role_role_02 foreign key (role_id) references role (id) on delete restrict on update restrict;

alter table user_team add constraint fk_user_team_user_01 foreign key (user_id) references user (id) on delete restrict on update restrict;

alter table user_team add constraint fk_user_team_team_02 foreign key (team_id) references team (id) on delete restrict on update restrict;

alter table user_application add constraint fk_user_application_user_01 foreign key (user_id) references user (id) on delete restrict on update restrict;

alter table user_application add constraint fk_user_application_application_02 foreign key (application_id) references application (id) on delete restrict on update restrict;

# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table application;

drop table permission;

drop table role;

drop table role_permission;

drop table team;

drop table user;

drop table user_role;

drop table user_team;

drop table user_application;

SET FOREIGN_KEY_CHECKS=1;

