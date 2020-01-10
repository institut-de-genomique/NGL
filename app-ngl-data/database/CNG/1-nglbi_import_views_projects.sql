/* 14/10/2013 */

--pas de vue v_project_tongl_reprise !!!!

DROP VIEW  IF EXISTS v_project_tongl;

CREATE VIEW v_project_tongl 
AS
 SELECT t_project.name AS code,
        t_project.name AS name,
        t_project.comments,
        t_project.creation_date
 FROM t_project

 WHERE t_project.nglimport_date IS NULL; --uniqt data pas deja importees

COMMENT ON VIEW v_project_tongl IS '<date>: vue pour la synchronisation dans NGL';

GRANT SELECT ON TABLE v_project_tongl TO solexaread;
-- utile ?
GRANT SELECT ON TABLE v_project_tongl TO ngl_bi;
GRANT SELECT ON TABLE v_project_tongl TO ngl_bi_dev;
GRANT SELECT ON TABLE v_project_tongl TO ngl_bi_test;


/*13/06/2014 */

DROP VIEW IF EXISTS v_project_updated_tongl;

CREATE VIEW v_project_updated_tongl
AS
  SELECT t_project.name AS code, 
         t_project.name AS name, 
         t_project.comments, 
         t_project.creation_date
  FROM t_project

  WHERE t_project.nglimport_date IS NOT NULL AND
        t_project.ngl_needupdate_date IS NOT NULL AND
        t_project.ngl_update_date IS NULL;

COMMENT ON VIEW v_project_updated_tongl IS '<date>: vue pour la RE-synchronisation';

GRANT SELECT ON TABLE v_project_updated_tongl TO solexaread;
-- utile ?
GRANT SELECT ON TABLE v_project_updated_tongl TO ngl_bi;
GRANT SELECT ON TABLE v_project_updated_tongl TO ngl_bi_dev;
GRANT SELECT ON TABLE v_project_updated_tongl TO ngl_bi_test;
