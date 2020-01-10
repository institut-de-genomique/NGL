DELETE FROM `CNS_NGL`.`role_permission`;
DELETE FROM `CNS_NGL`.`permission`;

DELETE FROM `CNS_NGL`.`role` WHERE `label` NOT IN ("reader");

INSERT INTO `CNS_NGL`.`role` (`id`, `label`) VALUES ('2', 'writer');
INSERT INTO `CNS_NGL`.`role` (`id`, `label`) VALUES ('3', 'admin');

INSERT INTO `CNS_NGL`.`permission` (`id`, `label`, `code`) VALUES ('1', 'reading', 'reading');
INSERT INTO `CNS_NGL`.`permission` (`id`, `label`, `code`) VALUES ('2', 'writing', 'writing');
INSERT INTO `CNS_NGL`.`permission` (`id`, `label`, `code`) VALUES ('3', 'admin', 'admin');

INSERT INTO `CNS_NGL`.`role_permission` (`role_id`, `permission_id`) VALUES ('1', '1');
INSERT INTO `CNS_NGL`.`role_permission` (`role_id`, `permission_id`) VALUES ('2', '1');
INSERT INTO `CNS_NGL`.`role_permission` (`role_id`, `permission_id`) VALUES ('2', '2');
INSERT INTO `CNS_NGL`.`role_permission` (`role_id`, `permission_id`) VALUES ('3', '1');
INSERT INTO `CNS_NGL`.`role_permission` (`role_id`, `permission_id`) VALUES ('3', '2');
INSERT INTO `CNS_NGL`.`role_permission` (`role_id`, `permission_id`) VALUES ('3', '3');

UPDATE `CNS_NGL`.`user_role`
SET `role_id`= '2';

