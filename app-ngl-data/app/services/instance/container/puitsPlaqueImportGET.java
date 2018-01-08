package services.instance.container;

import java.sql.SQLException;


import models.utils.dao.DAOException;
import scala.concurrent.duration.FiniteDuration;
import play.Logger;
public class puitsPlaqueImportGET extends ContainerImportGET {

	public puitsPlaqueImportGET(FiniteDuration durationFromStart,FiniteDuration durationFromNextIteration) {
		super("Container well GET", durationFromStart, durationFromNextIteration);
	}

	@Override 
	public void runImport() throws SQLException, DAOException {
		
		
		String SQLContainer="SELECT  DISTINCT "
				+ "tob.object_barcode as code,"
				+ "tob.object_id as barcodeid,"
				+ "tob.object_barcode as name, "
				+ "tot.type_object_name as typeCode, "
				+ "tot.type_object_name as categoryCode,"
				+ "tob.object_barcode as sampleCode, "
				+ "tob.description as comment,"
				+ "tob.localization_barcode  as codeSupport,"
				+ "totParent.max_position as nbContainer, "
				+ "tob.creation_date as receptionDate,"
				+ "tob.localization_barcode as localisation, "
				+ "tob.position_on_real_localization as position "
				+ ", pep.ident as createUser "
				+ "FROM trace_object tob "
				+ "INNER JOIN people pep on tob.userid = pep.userid "
				+ "INNER JOIN trace_object_type tot on tot.type_object_id = tob.type_object_id  "
				+ "INNER JOIN trace_object tobParent on tobParent.object_barcode = tob.localization_barcode "
				+ "INNER JOIN trace_object_type totParent on totParent.type_object_id = tobParent.type_object_id "
				+ "INNER JOIN trace_caracteristique_link_object tclo on tob.object_id=tclo.object_id  "
				+ "WHERE tob.localization_barcode != 'POUBELLE' "
				+ "AND tot.type_object_name = 'POSITION' "
				+ "AND tob.object_id NOT IN (SELECT object_id FROM trace_operation_link_object tolo "
                +								"INNER join trace_operation top on top.operation_id = tolo.operation_id "
                +								"INNER join trace_operation_type topt on topt.operation_type_id = top.operation_type_id "
                +								"WHERE topt.nom_operation='Pooling' ) "
				+ "AND (tob.object_id NOT IN (SELECT object_id FROM trace_caracteristique_link_object tclo "
				+ 									"INNER JOIN trace_caracteristique tc on tclo.caracteristique_id=tc.caracteristique_id "
				+ 										"WHERE tc.caracteristique_type_id = "+play.Play.application().configuration().getString("caracteristicstypeEsitoul.DateImportNgl")+" "
				+ 										"AND tclo.caracteristique_id != "+play.Play.application().configuration().getString("caracteristiqueEsitoulDateImportNglIndefini")+")) "

                //for test - limit by 1 container 
//				+ "AND tob.object_barcode = 'GENO43136:B4' "
				//Import_dans_NGL is TRUE
				+ "AND tclo.caracteristique_id ="+ play.Play.application().configuration().getString("caracteristiqueEsitoulImportDansNglVrai"); //vrai 
			Logger.debug("puitsPlaqueImportGET : " + SQLContainer);
			createContainers(contextError,SQLContainer,"well","IW-P",null,null);
			//contextError.setUpdateMode();
	}
}
