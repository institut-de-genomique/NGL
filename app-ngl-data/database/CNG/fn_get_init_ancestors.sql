/* FDS 30/06/2015
  retrouver les tubes ancetres initiaux ( ceux directement lies aux samples)
*/

CREATE OR REPLACE FUNCTION fn_get_init_ancestors(
    IN tube_id oid,
    OUT ancestor_id oid,
    OUT ancestor_barcode text)
  RETURNS SETOF record AS
'
--parametres d''entres  1=id du tube
--parametres de sortie  2=id de l''ancetre, 2=barcode du tube ancetre ( N rows en sortie )
SELECT distinct p.id, p.barcode
FROM fn_hierarchy_asc( $1 ) f
JOIN t_tube p on p.id=parent_id
JOIN t_tube t on t.id=tube_id
JOIN t_tube_sample ts on p.id=ts.tube_id
'
LANGUAGE 'sql'
