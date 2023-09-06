/* FDS 19/06/2015
  ATTENTION 
  cette requete rameme tous les ancetres du type demande, il peut y en avoir plusieurs
  soit par ce qu'il y a du pooling dans l'historique, soit parceque plusieurs tubes de 
  meme type se suivent ( cas frequent dilutions:  libXnM --> libXnM )
*/

CREATE OR REPLACE FUNCTION fn_get_type_ancestors(
    IN tube_id oid,
    IN type int,
    OUT ancestor_id oid,
    OUT ancestor_barcode text)
  RETURNS SETOF record AS
'
--08/06/2015
--parametres d''entres  1=id du tube, 2=type d''ancetre a trouver
--parametres de sortie  3=id de l''ancetre, 4=barcode du tube ancetre ( N rows en sortie )
SELECT distinct p.id, p.barcode
FROM fn_hierarchy_asc( $1 ) f
JOIN t_tube p on p.id=parent_id and p.type=$2
JOIN t_tube t on t.id=tube_id
'
LANGUAGE 'sql'
