package services.instance.container;

import java.sql.SQLException;


import models.utils.dao.DAOException;
import scala.concurrent.duration.FiniteDuration;

public class SampleImportGET extends ContainerImportGET {

	public SampleImportGET(FiniteDuration durationFromStart,FiniteDuration durationFromNextIteration) {
		super("Samples Tube GET", durationFromStart, durationFromNextIteration);
	}

	@Override 
	public void runImport() throws SQLException, DAOException {
		
		
//		String SQLContainer="SELECT  DISTINCT "
//				+ "tob.object_barcode as code,"
//				+ "tob.object_id as barcodeid,"
//				+ "tob.object_barcode as name, "
//				+ "tot.type_object_name as typeCode, "
//				+ "tot.type_object_name as categoryCode,"
//				+ "tob.object_barcode as sampleCode, "
//				+ "tob.description as comment,"
//				+ "tob.localization_barcode  as codeSupport,"
//				+ "1 as limscode,"
//				+ " '' as experimentTypeCode,"
//				+ "4 as etatLims, "
//				+ "tot.max_position as nbContainer, "
//				+ "1 as column, "
//				+ "1 as line,"
//				+ "'' as sequencingProgramType ,"
//				+ "tob.creation_date as receptionDate,"
//				+ " 'ml' as measuredConcentrationUnit,"
//				+ "10.0 as measuredConcentration,"
//				+ "10.0 as measuredVolume, "
//				+ "10 measuredQuantity, "
//				+ "0 as controlLane, "
//				+ "'12' as taxonSize, "
//				+ " '0' as isFragmented, "
//				+ "'0' as isAdapters, "
//				+ "'mRNA' as sampleTypeCode,"
//				+ "'0' as isSeveralTargets, "
//				+ "'reftoto' as referenceCollab, "
//				+ "'taxocon' as taxonCode,  "
//				+ "'4' as LIMS_CODE, "
//				+ "object_barcode as code, "
//				+ "'1' as tadco, "
//				+ "'11' as tprco, "
//				+ "'' as tag, "
//				+ "'' as tagCategory, "
//				+ "'' as libProcessTypeCode,"
//				+ "1 as libLayoutNominalLength, "
//				+ "tob.localization_barcode as localisation, "
//				+ "tob.position_on_real_localization as position "
//				+ "FROM trace_object tob "
//				+ "INNER JOIN trace_object_type tot on tot.type_object_id = tob.type_object_id  "
//				+ "INNER JOIN trace_caracteristique_link_object tclo on tob.object_id=tclo.object_id  "
//				+ "WHERE tob.userid IN (SELECT userid FROM people INNER join unit on unit.unitid=people.unitid WHERE unit.ident='GENO') "
//				+ "AND tot.type_object_name = 'TUBE' "
//				+"AND tclo.caracteristique_id =316746";
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
				+ "FROM trace_object tob "
				+ "INNER JOIN trace_object_type tot on tot.type_object_id = tob.type_object_id  "
				+ "INNER JOIN trace_caracteristique_link_object tclo on tob.object_id=tclo.object_id  "
				+ "WHERE tob.userid IN (SELECT userid FROM people INNER join unit on unit.unitid=people.unitid WHERE unit.ident='GENO') "
				+ "AND tot.type_object_name = 'TUBE' "
				+"AND tclo.caracteristique_id =316746";
			createContainers(contextError,SQLContainer,"tube","A-TM",null,null);
			//contextError.setUpdateMode();
	//		updateSampleFromTara();
	}
}
