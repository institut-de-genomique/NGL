/* 10/09/2015  SUPSQCNG-303 ajout du type H4 (HiSeq4000)
   A l'occasion de l'arrivée du HiSeq4000
   remplacer le SQL en dur dans du java 
   public List<LimsExperiment> getExperiments(Experiment experiment)
   par une vue...
*/

DROP VIEW IF EXISTS v_get_experiments;

CREATE VIEW v_get_experiments

AS

/* 
  11/09/2015 problem avec les run rapides sur H2...
  => repasser au sql qui etait en production dans le java..
  mais ajouter H4 a la liste des mt.type

  SELECT f.barcode, m.pc_name as code, w.start_date as date, mt.name as categoryCode, f.nb_cycles
  FROM t_flowcell f
  JOIN t_workflow w on w.flowcell_id=f.id
  left outer JOIN t_stage s on w.stage_id=s.id and (s.workflow='SEQ' and s.name='Read1')
  JOIN t_machine m on w.machine_id=m.id
  JOIN t_machine_type mt on ( m.type_id=mt.id AND mt.type IN ('GA','HS','H2','H4','MS','NS'))
*/

  SELECT f.barcode, m.pc_name as code, min(w.start_date) as date, mt.name as categoryCode, f.nb_cycles
  FROM t_flowcell f
  JOIN t_workflow w on w.flowcell_id=f.id
  JOIN t_machine m on w.machine_id=m.id
  JOIN t_machine_type mt on ( m.type_id=mt.id AND mt.type IN ('GA','HS','H2','H4','MS','NS'))
  GROUP BY  f.barcode, m.pc_name, mt.name,  f.nb_cycles
;

COMMENT ON VIEW v_get_experiments IS '<date>: vue pour recuperer la liste des runs d une flowcell';

GRANT SELECT ON TABLE v_get_experiments TO solexaread;
GRANT SELECT ON TABLE v_get_experiments TO solexa;
GRANT SELECT ON TABLE v_get_experiments TO ngl_bi;
GRANT SELECT ON TABLE v_get_experiments TO ngl_bi_dev;
GRANT SELECT ON TABLE v_get_experiments TO ngl_bi_test;
