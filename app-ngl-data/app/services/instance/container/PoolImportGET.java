package services.instance.container;

import java.sql.SQLException;

import models.utils.dao.DAOException;
//import rules.services.RulesException;
import scala.concurrent.duration.FiniteDuration;
import play.Logger;
public class PoolImportGET extends ContainerImportGET {

	public PoolImportGET(FiniteDuration durationFromStart,FiniteDuration durationFromNextIteration) {
		super("Container Pool GET", durationFromStart, durationFromNextIteration);
	}

	@Override
	public void runImport() throws SQLException, DAOException{
		
		//sélectionner les containers sortants (OUT) de l'exp. "Pooling" de Barcode
		String SQLContainer="SELECT  DISTINCT "
			+"tob.object_barcode as code, "
			+"tob.object_id as barcodeid, "
			+"tob.object_barcode as name, "
			+"tot.type_object_name as typeCode,"
			+"tot.type_object_name as categoryCode,"
			+"tob.object_barcode as sampleCode,"
			+"tob.description as comment,"
			+"tob.localization_barcode  as codeSupport,"
//			+"tot.max_position as nbContainer,"
			+"totParent.max_position as nbContainer, "
			+"tob.creation_date as receptionDate,"
			+"tob.localization_barcode as localisation,"
			+"tob.position_on_real_localization as position"
			+", pep.ident as createUser "
			+"FROM trace_object tob "
			+"INNER JOIN trace_object_type tot on tot.type_object_id = tob.type_object_id "
			+ "INNER JOIN trace_object tobParent on tobParent.object_barcode = tob.localization_barcode "
			+ "INNER JOIN trace_object_type totParent on totParent.type_object_id = tobParent.type_object_id "
			+"INNER JOIN people pep on tob.userid = pep.userid "
			+"WHERE tob.localization_barcode != 'POUBELLE' "
			//hasn't a link with "DateImportNgl" or its value is "Indefini"
			+ "AND (tob.object_id NOT IN (SELECT object_id FROM trace_caracteristique_link_object tclo "
			+ 									"INNER JOIN trace_caracteristique tc on tclo.caracteristique_id=tc.caracteristique_id "
			+ 									"WHERE tc.caracteristique_type_id = "+play.Play.application().configuration().getString("caracteristicstypeEsitoul.DateImportNgl")+""
			+ 									"AND tclo.caracteristique_id != "+play.Play.application().configuration().getString("caracteristiqueEsitoulDateImportNglIndefini")+"))"
			//from pooling process
			+"AND tob.object_id IN (SELECT object_id FROM trace_operation_link_object tolo "
                                              +"INNER join trace_operation top on top.operation_id = tolo.operation_id "
                                              +"INNER join trace_operation_type topt on topt.operation_type_id = top.operation_type_id "
                                              +"WHERE topt.nom_operation='Pooling' "
                                              +"AND tolo.in_out ='OUT') "
                                              //ORDER BY code ASC ";//LIMIT 1";
            //for test - limit by 1 container 
//            +"AND tob.object_barcode = 'GENO41754' "
            
            //Import_dans_NGL is TRUE
            +"AND tob.object_id IN (SELECT object_id FROM trace_caracteristique_link_object "
			+ 									"WHERE caracteristique_id ="+ play.Play.application().configuration().getString("caracteristiqueEsitoulImportDansNglVrai") + ") "; //vrai 

		//sélectionner les containers en entrée (IN) dans l'exp. "Pooling" précisé par le container en sortie (OUT) donné
		String sqlContent="SELECT  DISTINCT "
			+"tob.object_barcode as code, "
			+"tob.object_id as barcodeid, "
			+"tob.object_barcode as name, "
			+"tot.type_object_name as typeCode, "
			+"tot.type_object_name as categoryCode, "
			+"tob.object_barcode as sampleCode, "
			+"tob.description as comment, "
			+"tob.localization_barcode  as codeSupport, "
			+"tot.max_position as nbContainer, "
			+"tob.creation_date as receptionDate, "
			+"tob.localization_barcode as localisation, "
			+"tob.position_on_real_localization as position "
			+", pep.ident as createUser "
			+"FROM trace_object tob "
			+"INNER JOIN trace_object_type tot on tot.type_object_id = tob.type_object_id "
			+"INNER JOIN people pep on tob.userid = pep.userid "
			+"WHERE tob.object_id IN (SELECT tolo.object_id FROM trace_operation_link_object tolo "
                                              +"INNER join trace_operation top on top.operation_id = tolo.operation_id "
                                              +"INNER join trace_operation_type topt on topt.operation_type_id = top.operation_type_id "
                                              +"WHERE topt.nom_operation='Pooling' "
                                              +"AND tolo.in_out ='IN' "
                                              +"AND tolo.operation_id || tolo.rank::text = (SELECT operation_id||rank::text FROM trace_operation_link_object tolo "
                                                                        +"INNER JOIN trace_object tob on tolo.object_id = tob.object_id "
                                                                        +"WHERE object_barcode = ? ORDER BY operation_id DESC LIMIT 1)) "; //ex. GENO34763
		
		Logger.debug("PoolImportGET : " + SQLContainer);	
		createContainers(contextError,SQLContainer,null,"IW-P",null,sqlContent);
		
			
	}

}
