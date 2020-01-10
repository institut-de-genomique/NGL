/*
   01/04/2014
   vue pour reprise ( import massif de l'existant )

   attention aux MIDs => ne sont pas des vrai index !!
   compter le nombre de % de sample par lane
   !!WARNING la vue plante avec division by 0 si on ajoute une clause  "WHERE percent_per_lane=...."

   04/05/2015 - JIRA NGL-623:pour NGLBI le TAG a recuperer est maintenant le champ 'nglbi_code'
              - le filtre isavaible est mis dans le where pour eviter de le mettre dans le java...
   
    16/06/2015  JIRA NGL-672 et NGL-673  => ajouts et renommages            
*/

---!!! attention a l'ordre des drop
DROP VIEW IF EXISTS v_flowcell_tongl;         -- utilise v_flowcell_tongl_reprise
DROP VIEW IF EXISTS v_flowcell_updated_tongl; -- utilise v_flowcell_tongl_reprise
DROP VIEW IF EXISTS v_flowcell_tongl_reprise; -- utilise v_sample_project


CREATE VIEW v_flowcell_tongl_reprise
AS
  SELECT DISTINCT
       l.id AS lims_code,
       vsp.p_name AS project,
       s.stock_barcode AS sample_code,    -- 22/06/2015 renommé code_sample => sample_code
       f.barcode || '_' || l.number AS container_code,  -- l.number est un INT pourtant marche sans CAST; 22/06/2015 renommé  code => container_code
       f.barcode AS support_code,         -- 22/06/2015 renommé code_support => support_code
       ft.nb_lanes AS nb_usable_container, --22/06/2015 renommé nb_container => nb_usable_container
       l.number AS "column",  
       1 AS "row",                         --22/06/2015 ajouté pour etre homogène avec l'import des tubes, fixed pour lane
       i.nglbi_code AS tag, 
       aliquot.barcode as aliquote_code,   -- 22/06/2015 ajouté pour JIRA NGL-673
       /*ajout  sample_type 22/06/2015 pour JIRA NGL-672 */
       CASE WHEN st.name='DNA' THEN 'gDNA'
            WHEN st.name='IP'  THEN 'IP-sample'
            WHEN st.name='MBD' THEN 'methylated-base-DNA'
            WHEN st.name='UNK' THEN 'default-sample-cng'
            ELSE st.name
       END AS sample_type,
       CASE
          WHEN i.type= 1 THEN 'SINGLE-INDEX'
          WHEN i.type= 2 THEN 'DUAL-INDEX'
          WHEN i.type= 3 THEN 'MID'
       END AS tagCategory,
       l.comments AS comment,
       d.name AS ref_collab,
       f.runtype as seq_program_type,  -- 13/06/2014 ajouté 
       e.short_name as exp_short_name,  -- 16/10/2014 ajouté

       -- 23/01/2015 ajoutés pour simplifier l'écriture de v_flowcell_tongl
       l.nglimport_date      as l_nglimport_date,
       l.ngl_needupdate_date as l_ngl_needupdate_date,
       l.ngl_update_date     as l_ngl_update_date,

       sl.nglimport_date      as sl_nglimport_date,
       sl.ngl_needupdate_date as sl_ngl_needupdate_date,
       sl.ngl_update_date     as sl_ngl_update_date,

       s.nglimport_date      as s_nglimport_date,
       s.ngl_needupdate_date as s_ngl_needupdate_date,
       s.ngl_update_date     as s_ngl_update_date,

       d.nglimport_date      as d_nglimport_date,
       d.ngl_needupdate_date as d_ngl_needupdate_date,
       d.ngl_update_date     as d_ngl_update_date

  FROM t_flowcell f
  JOIN t_workflow w ON ( w.flowcell_id = f.id AND  w.is_last = true)
  JOIN t_lane l ON f.id = l.flowcell_id
  JOIN t_sample_lane sl ON l.id = sl.lane_id
  JOIN t_sample s ON s.id = sl.sample_id
  JOIN t_flowcell_type ft ON f.type_id = ft.id
  JOIN v_sample_project vsp ON vsp.s_id = s.id
  JOIN t_individual d ON d.id = s.individual_id
  LEFT JOIN t_index i ON ( sl.index = i.cng_name OR sl.index = i.short_name)
  LEFT JOIN t_exp_type e ON sl.exp_type_id=e.id  -- ajoute le 16/10/2014

  -- 22/06/2015 ajoutés pour JIRA NGL-674
  LEFT OUTER JOIN t_tube_sample ts ON s.id=ts.sample_id
  LEFT OUTER JOIN t_tube AS aliquot ON ts.tube_id=aliquot.id

 --22/06/2015 ajoutés  pour JIRA NGL-672                                                         */
  LEFT OUTER JOIN t_sample_type st ON s.type_id=st.id

  WHERE w.status != -1                    -- where ajouté le 04/05/2015; ne pas prendre en compte les flowcell en échec
        AND f.creation_date >'01/01/2015' -- 22/06/2015 on ne reprend pas tout l'historique !!
;

COMMENT ON VIEW v_flowcell_tongl_reprise IS '<date>: vue pour l import massif dans NGL';

GRANT SELECT ON TABLE v_flowcell_tongl_reprise TO solexaread;
GRANT SELECT ON TABLE v_flowcell_tongl_reprise TO solexa;
GRANT SELECT ON TABLE v_flowcell_tongl_reprise TO ngl_bi;
GRANT SELECT ON TABLE v_flowcell_tongl_reprise TO ngl_bi_dev;
GRANT SELECT ON TABLE v_flowcell_tongl_reprise TO ngl_bi_test;


/* 23/01/2015 récrite en version simplifiée a partir de v_flowcell_tongl_reprise */

CREATE OR REPLACE VIEW v_flowcell_tongl
AS
  SELECT * from v_flowcell_tongl_reprise
  WHERE l_nglimport_date IS NULL
;

COMMENT ON VIEW v_flowcell_tongl IS '<date>: Vue pour la synchronisation dans NGL';

GRANT SELECT ON TABLE v_flowcell_tongl TO solexaread;
GRANT SELECT ON TABLE v_flowcell_tongl TO solexa;
GRANT SELECT ON TABLE v_flowcell_tongl TO ngl_bi;
GRANT SELECT ON TABLE v_flowcell_tongl TO ngl_bi_dev;
GRANT SELECT ON TABLE v_flowcell_tongl TO ngl_bi_test;


/* 23/01/2015 reecrite en version simplifiée a partir de v_flowcell_tongl_reprise */

CREATE VIEW v_flowcell_updated_tongl
AS
  SELECT * from v_flowcell_tongl_reprise
  WHERE
     -- lane a changé 
     ( l_ngl_needupdate_date IS NOT NULL AND
       l_ngl_update_date IS NULL AND
       l_nglimport_date IS NOT NULL )
  OR
     -- sample_lane a changé 
     ( sl_ngl_needupdate_date IS NOT NULL AND
       sl_ngl_update_date IS NULL AND
       sl_nglimport_date IS NOT NULL )
  OR
     -- sample a changé
     ( s_ngl_needupdate_date IS NOT NULL AND
       s_ngl_update_date IS NULL AND
       s_nglimport_date IS NOT NULL )
  OR
     -- individual a changé
     ( d_ngl_needupdate_date IS NOT NULL AND
       d_ngl_update_date IS NULL AND
       d_nglimport_date IS NOT NULL )
;

COMMENT ON VIEW v_flowcell_updated_tongl IS '<date>: vue pour la RE-synchronisation';

GRANT SELECT ON TABLE v_flowcell_updated_tongl TO solexaread;
GRANT SELECT ON TABLE v_flowcell_updated_tongl TO solexa;
GRANT SELECT ON TABLE v_flowcell_updated_tongl TO ngl_bi;
GRANT SELECT ON TABLE v_flowcell_updated_tongl TO ngl_bi_dev;
GRANT SELECT ON TABLE v_flowcell_updated_tongl TO ngl_bi_test;
