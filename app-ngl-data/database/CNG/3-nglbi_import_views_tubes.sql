/* 
 15/04/2015 JIRA NGL-623: pour NGLBI ajouter le champ i.nglb_code remplace i.short_name
 04/05/2015 les filtres sur type/concentration sont supprimes de la vue et transferre dans des vues specialisees
            => evite de devoir modifier la vue principale !!!!!!
*/

-- ajoutees le 04/05/2015: vues specialisees avec filtres sur conc et volume
DROP VIEW IF EXISTS v_libnorm_tongl;         -- utilise v_libnorm_tongl_reprise;
DROP VIEW IF EXISTS v_libnorm_updated_tongl; -- utilise v_libnorm_tongl_reprise;
DROP VIEW IF EXISTS v_libnorm_tongl_reprise; -- utilise v_tube_tongl_reprise

-- ajoutees le 16/06/2015: vues specialisees avec filtres sur conc et volume
DROP VIEW IF EXISTS v_libdenatdil_tongl;         -- utilise v_libdenatdil_tongl_reprise
DROP VIEW IF EXISTS v_libdenatdil_updated_tongl; -- utilise v_libdenatdil_tongl_reprise
DROP VIEW IF EXISTS v_libdenatdil_tongl_reprise; -- utilise v_tube_tongl_reprise


DROP VIEW IF EXISTS v_tube_tongl;         -- utilise v_tube_tongl_reprise
DROP VIEW IF EXISTS v_tube_updated_tongl; -- utilise v_tube_tongl_reprise
DROP VIEW IF EXISTS v_tube_tongl_reprise; -- utilise v_sample_project

CREATE VIEW v_tube_tongl_reprise
AS
  SELECT DISTINCT
         temp.*,
         vsp.p_name AS project,
         vsp.i_name AS ref_collab,
         s.stock_barcode AS sample_code,  -- 19/06/2015 renomme sample_code ( dans NGL le sample correspond au stock barcode)
         -- 16/06/2015 ajout sample_type pour JIRA 672
         CASE WHEN st.name='DNA' THEN 'gDNA'
              WHEN st.name='IP'  THEN 'IP-sample'
              WHEN st.name='MBD' THEN 'methylated-base-DNA'
              WHEN st.name='UNK' THEN 'default-sample-cng'
              ELSE st.name
         END AS sample_type,
         CASE
             WHEN i.type = 1 THEN 'SINGLE-INDEX'
             WHEN i.type = 2 THEN 'DUAL-INDEX'
             WHEN i.type = 3 THEN 'MID'
             ELSE NULL
         END AS tagcategory,
         i.nglbi_code AS tag,            -- 16/04/2015 nglbi_code remplace short_name
         e.short_name AS exp_short_name
         --null as seq_program_type      --19/06/2015 supprimé car inutile pour tubes.

  FROM
   (SELECT
           t.nglimport_date, t.ngl_needupdate_date, t.ngl_update_date,   --  09/12/2014 ajoutés pour simplifier écriture des 2 autres
           t.id AS lims_code,
           t.barcode AS container_code,   -- 19/06/2015 renommé  code => container_code
           t.barcode AS support_code,     -- 19/06/2015 renommé  code_support => support_code
           1 AS  "nb_usable_container" ,  -- fixed; 19/06/2015 renommé  nb_container => nb_usable_container 
           1 AS "row",                    -- 19/06/2015 ajouté, fixed
           1 AS "column",                 -- fixed;
           split_part(regexp_split_to_table(t.size2,'#'),':',1) AS aliquote_code,   -- 06/07/2015 JIRA 673 renommé solexa_sample => aliquote_code
           CASE  WHEN t.index IS NOT NULL THEN split_part(regexp_split_to_table(t.index,'#'),':',2) 
                 ELSE NULL
           END AS index,                  -- nécessaire pour jointure avec t_index 
           CASE  WHEN t.exp_type IS NOT NULL THEN split_part(regexp_split_to_table(t.exp_type,'#'),':',2) 
                 ELSE NULL                                                                                
           END AS exp_type,               -- nécessaire pour jointure avec t_exp_type
           t.comments AS comment,
           t.concentration,
           t.type as lib_type             -- 17/04/2015 ajoute pour pourvoir etre filtre
           FROM t_tube  t
 
           WHERE creation_date > '01/01/2015' -- on ne reprend pas tout l'historique...
                 AND t.is_usable=true         -- pour ne pas a voir a filter dans le code java!! 
    ) temp
  JOIN v_sample_project vsp ON vsp.s_barcode= temp.aliquote_code         -- 06/07/2015 JIRA 673 renommé temp.solexa_sample => aliquote_code
  LEFT JOIN t_index i ON ( i.cng_name = temp.index OR i.short_name = temp.index )/* le nom d'index dans les tubes est le cng_name
                                                                                    pour les lib indexees au CNG et le short_name
                                                                                    pour celles indexes a l'exterieur 
                                                                                  */                   
  LEFT JOIN t_exp_type e ON (e.name = temp.exp_type OR ( e.name ~ ('^'||temp.exp_type||'\_' )))     
                                                                                 /* le nom de DefCap est parfois note DefCapXX ou parfois 
                                                                                    complet DefCapXX_AB 
                                                                                    13/03/2015 bug !! il y a des cas a problemes: ssRNASeq et RNASeq
                                                                                    => ajouter '^' pour eviter l'over match
                                                                                    17/06/2015 pas suffisant: 
                                                                                       autre cas d'over match: MedipSeq   Medipseq/Depl
                                                                                    27/07/2015 bug  
                                                                                       => correction:   \_  sans le %
  --ajoutés pour JIRA 672                                                         */
  LEFT OUTER JOIN t_sample s ON vsp.s_id=s.id 
  LEFT OUTER JOIN t_sample_type st ON s.type_id=st.id
;


COMMENT ON VIEW v_tube_tongl_reprise IS '<date>: vue pour l import massif dans NGL';

GRANT SELECT ON TABLE v_tube_tongl_reprise TO solexaread;
GRANT SELECT ON TABLE v_tube_tongl_reprise TO solexa;
GRANT SELECT ON TABLE v_tube_tongl_reprise TO ngl_bi;
GRANT SELECT ON TABLE v_tube_tongl_reprise TO ngl_bi_dev;
GRANT SELECT ON TABLE v_tube_tongl_reprise TO ngl_bi_test;

--------------------------------------------------

CREATE VIEW v_tube_tongl
AS
  SELECT * FROM v_tube_tongl_reprise
  WHERE nglimport_date IS NULL
;

COMMENT ON VIEW v_tube_tongl IS '<date>: vue pour la synchronisation dans NGL';

GRANT SELECT ON TABLE v_tube_tongl TO solexaread;
GRANT SELECT ON TABLE v_tube_tongl TO solexa;
GRANT SELECT ON TABLE v_tube_tongl TO ngl_bi;
GRANT SELECT ON TABLE v_tube_tongl TO ngl_bi_dev;
GRANT SELECT ON TABLE v_tube_tongl TO ngl_bi_test;

--------------------------------------------------

CREATE VIEW v_tube_updated_tongl
AS
  SELECT * FROM v_tube_tongl_reprise
  WHERE  (nglimport_date IS NOT NULL
          AND  ngl_needupdate_date IS NOT NULL
          AND  ngl_update_date IS NULL )
;

COMMENT ON VIEW v_tube_updated_tongl IS '<date>: vue pour la RE-synchronisation dans NGL';

GRANT SELECT ON TABLE v_tube_updated_tongl TO solexaread;
GRANT SELECT ON TABLE v_tube_updated_tongl TO solexa;
GRANT SELECT ON TABLE v_tube_updated_tongl TO ngl_bi;
GRANT SELECT ON TABLE v_tube_updated_tongl TO ngl_bi_dev;
GRANT SELECT ON TABLE v_tube_updated_tongl TO ngl_bi_test;
                                                              
----------------------Vues Ajoutees le 04/05/2015-------------------------------
/*  types de la base Solexa:
  lib_type=3 ---> lib10nM
  lib_type=4 ---> lib-B
  lib_type=5 ---> libXnM
*/
--1--
CREATE VIEW v_libnorm_tongl_reprise
AS
  SELECT * FROM v_tube_tongl_reprise
  -- on exclue les lib-B et filtre les xnM   >= 1 nM ( pour from "lib-normalization" dans NGL )
  --                     les lib-B seront importées dans NGL from "lib-b" ??
  --                     les libxnM < 1 nM (0.02,...) seront importees dans NGL from "denat-dil-lib" 
  WHERE (lib_type=3 or  ( lib_type=5 and concentration >= 1))
;

COMMENT ON VIEW v_libnorm_tongl_reprise IS '<date>: vue pour la synchronisation dans NGL';

GRANT SELECT ON TABLE v_libnorm_tongl_reprise TO solexaread;
GRANT SELECT ON TABLE v_libnorm_tongl_reprise TO solexa;
GRANT SELECT ON TABLE v_libnorm_tongl_reprise TO ngl_bi;
GRANT SELECT ON TABLE v_libnorm_tongl_reprise TO ngl_bi_dev;
GRANT SELECT ON TABLE v_libnorm_tongl_reprise TO ngl_bi_test;

--2--
/* modif le 16/06/2015... meme arborescence...*/
CREATE VIEW v_libnorm_tongl
AS
  SELECT * FROM v_libnorm_tongl_reprise
  WHERE nglimport_date IS NULL
;

COMMENT ON VIEW v_libnorm_tongl IS '<date>: vue pour la synchronisation dans NGL';

GRANT SELECT ON TABLE v_libnorm_tongl TO solexaread;
GRANT SELECT ON TABLE v_libnorm_tongl TO solexa;
GRANT SELECT ON TABLE v_libnorm_tongl TO ngl_bi;
GRANT SELECT ON TABLE v_libnorm_tongl TO ngl_bi_dev;
GRANT SELECT ON TABLE v_libnorm_tongl TO ngl_bi_test;

--3--
CREATE VIEW v_libnorm_updated_tongl
AS
  SELECT * FROM v_libnorm_tongl_reprise
  WHERE  (nglimport_date IS NOT NULL
          AND  ngl_needupdate_date IS NOT NULL
          AND  ngl_update_date IS NULL )
;

COMMENT ON VIEW v_libnorm_updated_tongl IS '<date>: vue pour la RE-synchronisation dans NGL';

GRANT SELECT ON TABLE v_libnorm_updated_tongl TO solexaread;
GRANT SELECT ON TABLE v_libnorm_updated_tongl TO solexa;
GRANT SELECT ON TABLE v_libnorm_updated_tongl TO ngl_bi;
GRANT SELECT ON TABLE v_libnorm_updated_tongl TO ngl_bi_dev;
GRANT SELECT ON TABLE v_libnorm_updated_tongl TO ngl_bi_test;

----------------------Vues Ajoutées le 16/06/2015-------------------------------

--1--
CREATE VIEW v_libdenatdil_tongl_reprise
AS
  SELECT * FROM v_tube_tongl_reprise
  WHERE ( lib_type=5 and concentration < 1)
;

COMMENT ON VIEW v_libdenatdil_tongl_reprise IS '<date>: vue pour la synchronisation dans NGL';

GRANT SELECT ON TABLE v_libdenatdil_tongl_reprise TO solexaread;
GRANT SELECT ON TABLE v_libdenatdil_tongl_reprise TO solexa;
GRANT SELECT ON TABLE v_libdenatdil_tongl_reprise TO ngl_bi;
GRANT SELECT ON TABLE v_libdenatdil_tongl_reprise TO ngl_bi_dev;
GRANT SELECT ON TABLE v_libdenatdil_tongl_reprise TO ngl_bi_test;

--2--
CREATE VIEW v_libdenatdil_tongl
AS
  SELECT * FROM v_libdenatdil_tongl_reprise
  WHERE nglimport_date IS NULL
;

COMMENT ON VIEW v_libnorm_tongl IS '<date>: vue pour la synchronisation dans NGL';

GRANT SELECT ON TABLE v_libdenatdil_tongl TO solexaread;
GRANT SELECT ON TABLE v_libdenatdil_tongl TO solexa;
GRANT SELECT ON TABLE v_libdenatdil_tongl TO ngl_bi;
GRANT SELECT ON TABLE v_libdenatdil_tongl TO ngl_bi_dev;
GRANT SELECT ON TABLE v_libdenatdil_tongl TO ngl_bi_test;

--3--
CREATE VIEW v_libdenatdil_updated_tongl
AS
  SELECT * FROM v_libdenatdil_tongl_reprise
  WHERE  (nglimport_date IS NOT NULL
          AND  ngl_needupdate_date IS NOT NULL
          AND  ngl_update_date IS NULL )
;

COMMENT ON VIEW v_libdenatdil_updated_tongl IS '<date>: vue pour la RE-synchronisation dans NGL';

GRANT SELECT ON TABLE v_libdenatdil_updated_tongl TO solexaread;
GRANT SELECT ON TABLE v_libdenatdil_updated_tongl TO solexa;
GRANT SELECT ON TABLE v_libdenatdil_updated_tongl TO ngl_bi;
GRANT SELECT ON TABLE v_libdenatdil_updated_tongl TO ngl_bi_dev;
GRANT SELECT ON TABLE v_libdenatdil_updated_tongl TO ngl_bi_test;

