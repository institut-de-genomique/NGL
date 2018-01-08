package services.instance.container;

import java.sql.SQLException;
import java.util.Calendar;

import models.utils.dao.DAOException;
import scala.concurrent.duration.FiniteDuration;
import play.Logger;
public class TubeImportGET extends ContainerImportGET {

	public TubeImportGET(FiniteDuration durationFromStart,FiniteDuration durationFromNextIteration) {
		super("Container Tube GET", durationFromStart, durationFromNextIteration);
	}

	@Override 
	public void runImport() throws SQLException, DAOException {
		
		
			
		Calendar today = Calendar.getInstance();
		
		String SQLContainer="SELECT  DISTINCT "
				+ "tob.object_barcode as code,"
				+ "tob.object_id as barcodeid,"
				+ "tob.object_barcode as name, "
				+ "tot.type_object_name as typeCode, "
				+ "tot.type_object_name as categoryCode,"
				+ "tob.object_barcode as sampleCode, "
				+ "tob.description as comment,"
				+ "tob.localization_barcode  as codeSupport,"
				+ "tot.max_position as nbContainer, "
				+ "tob.creation_date as receptionDate,"
				+ "tob.localization_barcode as localisation, "
				+ "tob.position_on_real_localization as position "
				+ ", pep.ident as createUser "
				+ "FROM trace_object tob "
				+ "INNER JOIN people pep on tob.userid = pep.userid "
				+ "INNER JOIN trace_object_type tot on tot.type_object_id = tob.type_object_id  "
				+ "INNER JOIN trace_caracteristique_link_object tclo on tob.object_id=tclo.object_id  "
				+ "WHERE (tot.type_object_name = 'TUBE' OR tot.type_object_name = 'TUBERTL') "
				+ "AND tob.localization_barcode != 'POUBELLE' "
				//don't out from operation 'Pooling'
				+ "AND tob.object_id NOT IN (SELECT object_id FROM trace_operation_link_object tolo "
                +								"INNER join trace_operation top on top.operation_id = tolo.operation_id "
                +								"INNER join trace_operation_type topt on topt.operation_type_id = top.operation_type_id "
                +								"WHERE topt.nom_operation='Pooling' )"
				+ "AND (tob.object_id NOT IN (SELECT object_id FROM trace_caracteristique_link_object tclo "
				+ 									"INNER JOIN trace_caracteristique tc on tclo.caracteristique_id=tc.caracteristique_id "
				+ 										"WHERE tc.caracteristique_type_id = "+play.Play.application().configuration().getString("caracteristicstypeEsitoul.DateImportNgl")+" "
				+ 										"AND tclo.caracteristique_id != "+play.Play.application().configuration().getString("caracteristiqueEsitoulDateImportNglIndefini")+")) "
				//Import_dans_NGL is TRUE
				+ "AND tclo.caracteristique_id ="+ play.Play.application().configuration().getString("caracteristiqueEsitoulImportDansNglVrai"); //vrai 
			Logger.debug("TubeImportGET : " + SQLContainer);
			createContainers(contextError,SQLContainer,"tube","IW-P",null,null);
			
			//contextError.setUpdateMode();
	}
}
//creation_date 2016-12-23 15:20:29.91+01SELECT  d WHERE tob.creation_date BETWEEN '2016-10-01' AND '2016-12-20' AND tob.localization_barcode != 'POUBELLE' AND tot.type_object_name = 'TUBE' AND tclo.caracteristique_id =300386 

