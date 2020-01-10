package controllers.migration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import controllers.CommonController;
import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Treatment;
import models.utils.InstanceConstants;
import play.Logger;
import play.mvc.Result;

public class MigrationUpdateReadSetQ30QualMean extends CommonController{

	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmm");
	
	public static Result migration()
	{
		
		backUpReadSet();
		
		//Read input
		String[] inputData = getDataToUpdate();
		Logger.debug("Nb inputData "+inputData.length);
		//For each readset 
		int nbNotFound=0;
		for(int i=0; i<inputData.length; i++){
			//code get readset
			Logger.debug("Line "+inputData[i]);
			String[] readSetData = inputData[i].split(" ");
			String codeReadSet = readSetData[0];
			Double q30 = Double.parseDouble(readSetData[1]);
			Double qualScore = Double.parseDouble(readSetData[2]);
			Logger.debug("Q30 "+q30+" qualScore "+qualScore);
			ReadSet readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, codeReadSet);
			Logger.debug("ReadSet "+readSet);
			if(readSet==null){
				nbNotFound++;
			}else{
				//get treatment ngsrg
				Treatment treatmentNGSRG = readSet.treatments.get("ngsrg");
				//Modify value Q30 and QualMean
				treatmentNGSRG.results.get("default").put("Q30", new PropertySingleValue(q30));
				treatmentNGSRG.results.get("default").put("qualityScore", new PropertySingleValue(qualScore));
				//Update treatment.ngsrg for readset code typeCode=ngsrg-illumina
				MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("code", codeReadSet),  DBUpdate.set("treatments.ngsrg", treatmentNGSRG));
			}
		}
		if(nbNotFound>0)
			return badRequest(nbNotFound+" readsets not found");
		else
			return ok("Migration finish");
	}

	private static void backUpReadSet()
	{
		String backupName = InstanceConstants.READSET_ILLUMINA_COLL_NAME+"_BCK_SUBSQ1740_"+sdf.format(new java.util.Date());
		Logger.info("\tCopie "+InstanceConstants.READSET_ILLUMINA_COLL_NAME+" to "+backupName+" start");
		
		List<ReadSet> readSets = new ArrayList<ReadSet>();
		String[] inputData = getDataToUpdate();
		for(int i=0; i<inputData.length; i++){
			//code get readset
			Logger.debug("Line "+inputData[i]);
			String[] readSetData = inputData[i].split(" ");
			String codeReadSet = readSetData[0];
			ReadSet readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, codeReadSet);
			readSets.add(readSet);
		}
		MongoDBDAO.save(backupName, readSets);
		Logger.info("\tCopie "+InstanceConstants.READSET_ILLUMINA_COLL_NAME+" to "+backupName+" end");
		
	}
	
	private static String[] getDataToUpdate()
	{
		String data = "BPN_BOSF_1_H3TGMBBXX.IND10 87.34 37.01#"+
				"BPN_COSF_1_H3TGMBBXX.IND19 85.64 36.55#"+
				"BPN_DOSF_1_H3TGMBBXX.IND21 80.83 35.22#"+
				"BPN_EOSF_1_H3TGMBBXX.IND29 87.90 37.20#"+
				"BPN_FOSF_1_H3TGMBBXX.IND40 87.67 37.10#"+
				"BPN_GOSF_1_H3TGMBBXX.IND48 84.96 36.37#"+
				"BPN_AOSF_1_H3TGMBBXX.IND5 87.52 37.05#"+
				"BPN_BOSF_2_H3TGMBBXX.IND10 85.34 36.46#"+
				"BPN_COSF_2_H3TGMBBXX.IND19 83.53 35.97#"+
				"BPN_DOSF_2_H3TGMBBXX.IND21 79.20 34.78#"+
				"BPN_EOSF_2_H3TGMBBXX.IND29 85.35 36.49#"+
				"BPN_FOSF_2_H3TGMBBXX.IND40 85.53 36.51#"+
				"BPN_GOSF_2_H3TGMBBXX.IND48 82.79 35.78#"+
				"BPN_AOSF_2_H3TGMBBXX.IND5 85.40 36.48#"+
				"BPN_BOSF_3_H3TGMBBXX.IND10 85.24 36.43#"+
				"BPN_COSF_3_H3TGMBBXX.IND19 83.33 35.92#"+
				"BPN_DOSF_3_H3TGMBBXX.IND21 79.43 34.84#"+
				"BPN_EOSF_3_H3TGMBBXX.IND29 85.56 36.54#"+
				"BPN_FOSF_3_H3TGMBBXX.IND40 85.30 36.45#"+
				"BPN_GOSF_3_H3TGMBBXX.IND48 82.50 35.71#"+
				"BPN_AOSF_3_H3TGMBBXX.IND5 85.21 36.43#"+
				"BPN_BOSF_4_H3TGMBBXX.IND10 78.56 34.66#"+
				"BPN_COSF_4_H3TGMBBXX.IND19 77.73 34.44#"+
				"BPN_DOSF_4_H3TGMBBXX.IND21 73.59 33.32#"+
				"BPN_EOSF_4_H3TGMBBXX.IND29 75.56 33.79#"+
				"BPN_FOSF_4_H3TGMBBXX.IND40 80.11 35.08#"+
				"BPN_GOSF_4_H3TGMBBXX.IND48 75.19 33.78#"+
				"BPN_AOSF_4_H3TGMBBXX.IND5 79.57 34.94#"+
				"BPN_BOSF_5_H3TGMBBXX.IND10 77.30 34.30#"+
				"BPN_COSF_5_H3TGMBBXX.IND19 76.43 34.08#"+
				"BPN_DOSF_5_H3TGMBBXX.IND21 72.03 32.90#"+
				"BPN_EOSF_5_H3TGMBBXX.IND29 75.26 33.69#"+
				"BPN_FOSF_5_H3TGMBBXX.IND40 78.59 34.67#"+
				"BPN_GOSF_5_H3TGMBBXX.IND48 73.86 33.41#"+
				"BPN_AOSF_5_H3TGMBBXX.IND5 78.15 34.55#"+
				"BPN_BOSW_6_H3TGMBBXX.IND10 75.75 33.91#"+
				"BPN_COSW_6_H3TGMBBXX.IND19 78.16 34.55#"+
				"BPN_DOSW_6_H3TGMBBXX.IND21 68.99 32.11#"+
				"BPN_EOSW_6_H3TGMBBXX.IND29 75.23 33.67#"+
				"BPN_FOSW_6_H3TGMBBXX.IND40 78.56 34.68#"+
				"BPN_GOSW_6_H3TGMBBXX.IND48 68.65 32.05#"+
				"BPN_AOSW_6_H3TGMBBXX.IND5 78.03 34.53#"+
				"BPN_BOSW_7_H3TGMBBXX.IND10 73.99 33.42#"+
				"BPN_COSW_7_H3TGMBBXX.IND19 76.59 34.12#"+
				"BPN_DOSW_7_H3TGMBBXX.IND21 67.99 31.83#"+
				"BPN_EOSW_7_H3TGMBBXX.IND29 74.19 33.37#"+
				"BPN_FOSW_7_H3TGMBBXX.IND40 77.10 34.27#"+
				"BPN_GOSW_7_H3TGMBBXX.IND48 67.01 31.60#"+
				"BPN_AOSW_7_H3TGMBBXX.IND5 76.13 34.01#"+
				"BPN_BOSW_8_H3TGMBBXX.IND10 73.80 33.36#"+
				"BPN_COSW_8_H3TGMBBXX.IND19 76.39 34.05#"+
				"BPN_DOSW_8_H3TGMBBXX.IND21 68.61 31.98#"+
				"BPN_EOSW_8_H3TGMBBXX.IND29 74.77 33.53#"+
				"BPN_FOSW_8_H3TGMBBXX.IND40 77.02 34.23#"+
				"BPN_GOSW_8_H3TGMBBXX.IND48 66.90 31.56#"+
				"BPN_AOSW_8_H3TGMBBXX.IND5 75.85 34.92";
		return data.split("#");
	}

}
