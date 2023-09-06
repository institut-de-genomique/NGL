/* 14/10/2013 

  taxonsize, isfragmented, isadapters  n'existent pas dans LIMS SOLEXA
     => supprimes 16/06/2015 grace a modif dans le code import java
  !! stock_barcode n'EST PAS UNIQUE plusieurs samples peuvent etre produits a partir du meme
  => sous requete pour recuperer les infos pour l'id min

24/11/2014 ajout v_sample_tongl_reprise basee sur  v_sample_tongl sans la contrainte...
23/01/2015 ajout s.nglimport_date pour pouvoir simplifier l'ecriture de v_sample_tongl..
05/06/2015 JIRA NGL-672: ajout sample_type
    il faut mapper certains codes
     DNA=>gDNA
     IP => IP-sample           (61 dans la base)
     MBD=>methylated-base-DNA (1 dans la base..)
     UNK=>default-sample-cng
     pour RNA pas de mapping necessaire pour l'instant...
*/

DROP VIEW IF EXISTS v_sample_tongl;         -- utilise v_sample_tongl_reprise
DROP VIEW IF EXISTS v_sample_updated_tongl; -- utilise v_sample_tongl_reprise 
DROP VIEW IF EXISTS v_sample_tongl_reprise; -- utilise v_sample_project

CREATE VIEW v_sample_tongl_reprise 
AS
 SELECT s.id            AS lims_code,
        s.stock_barcode AS code,
        s.stock_barcode AS name,
        CASE WHEN st.name='DNA' THEN 'gDNA'
             WHEN st.name='IP'  THEN 'IP-sample'
             WHEN st.name='MBD' THEN 'methylated-base-DNA'
             WHEN st.name='UNK' THEN 'default-sample-cng'
             ELSE st.name
        END AS sample_type,
        vsp.p_name      AS project,
        o.ncbi_taxon_id AS taxon_code,
        s.comments,
        i.name AS ref_collab,
        -- 16/06/2015 taxonsize, isfragmented,isadapters,  supprimes car inutiles
        -- 23/01/2015 champs suivants ajoutes  pour simplifier l'ecriture des vues suivantes
        s.nglimport_date       AS s_nglimport_date,
        s.ngl_needupdate_date  AS s_ngl_needupdate_date,
        s.ngl_update_date      AS s_ngl_update_date,

        i.nglimport_date       AS i_nglimport_date, 
        i.ngl_needupdate_date  AS i_ngl_needupdate_date,
        i.ngl_update_date      AS i_ngl_update_date

 FROM t_sample s
 JOIN v_sample_project vsp ON vsp.s_id=s.id
 JOIN t_individual i       ON vsp.i_id = i.id
 JOIN t_org o              ON i.org_id = o.id
 JOIN t_sample_type st     ON s.type_id = st.id

  -- 23/01/2015 il faut cette sous-requete meme pour la reprise car sinon on insere des doublons!!!
  WHERE
   ( s.id IN ( SELECT min(t_sample.id) AS min
     FROM t_sample
     GROUP BY t_sample.stock_barcode)
   )

 ORDER BY s.stock_barcode    --ralenti la requete MAIS necessaire pour l'algo d'import en Java...
;

COMMENT ON VIEW v_sample_tongl_reprise IS '<date>: vue pour l import massif dans NGL';

GRANT SELECT ON TABLE v_sample_tongl_reprise TO solexaread;
GRANT SELECT ON TABLE v_sample_tongl_reprise TO solexa;
GRANT SELECT ON TABLE v_sample_tongl_reprise TO ngl_bi;
GRANT SELECT ON TABLE v_sample_tongl_reprise TO ngl_bi_dev;
GRANT SELECT ON TABLE v_sample_tongl_reprise TO ngl_bi_test;


/* 23/01/2015 recrite en version simplifiee */

CREATE VIEW v_sample_tongl 
AS
 SELECT * from v_sample_tongl_reprise
 WHERE s_nglimport_date IS NULL --uniqt data pas deja importees
 --NON ORDER BY stock_barcode    --ralenti la requete MAIS necessaire pour l'algo d'import en Java...
 ORDER BY code    --ralenti la requete MAIS necessaire pour l'algo d'import en Java...
;

COMMENT ON VIEW v_sample_tongl IS '<date>: vue pour la synchronisation dans NGL';

GRANT SELECT ON TABLE v_sample_tongl TO solexaread;
GRANT SELECT ON TABLE v_sample_tongl TO solexa;
GRANT SELECT ON TABLE v_sample_tongl TO ngl_bi;
GRANT SELECT ON TABLE v_sample_tongl TO ngl_bi_dev;
GRANT SELECT ON TABLE v_sample_tongl TO ngl_bi_test;

/* 23/01/2015 reecrite en version simplifiee */

CREATE VIEW v_sample_updated_tongl
AS
 SELECT * from v_sample_tongl_reprise
 WHERE
       (
         ( s_ngl_needupdate_date IS NOT NULL AND
           s_ngl_update_date IS NULL AND
           s_nglimport_date IS NOT NULL  )
      OR
         ( i_ngl_needupdate_date IS NOT NULL AND
           i_ngl_update_date IS NULL AND
           i_nglimport_date IS NOT NULL  )
       )
 --NON ORDER BY stock_barcode
 ORDER BY code
;

COMMENT ON VIEW v_sample_updated_tongl IS '<date>: vue pour la RE-synchronisation';

GRANT SELECT ON TABLE v_sample_updated_tongl TO solexaread;
GRANT SELECT ON TABLE v_sample_tongl TO solexa;
GRANT SELECT ON TABLE v_sample_tongl TO ngl_bi;
GRANT SELECT ON TABLE v_sample_tongl TO ngl_bi_dev;
GRANT SELECT ON TABLE v_sample_tongl TO ngl_bi_test;
