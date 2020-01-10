package controllers.migration;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;

import models.laboratory.run.instance.Analysis;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import models.laboratory.valuation.instance.ValuationCriteria;
import models.utils.InstanceConstants;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import com.mongodb.BasicDBObject;

import play.Logger;
import controllers.CommonController;
import fr.cea.ig.MongoDBDAO;
import play.mvc.Result;

/**
 * Replace old codes of ValuationCriteria (NGL-277)
 * @author dnoisett
 * 05-11-2014
 */


public class ValuationCriteriaCodes  extends CommonController {

	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmm");

	//main
	public static Result migration() {
		
		HashMap<String, String> mappingVCCodes = defineMapping();
		
		//verify criteriaCodes
		BasicDBObject keys = new BasicDBObject();
		keys.put("treatments", 0);
		if(verifyIntegrity(mappingVCCodes, keys)){
			//Only if OK...
			Logger.info("1/4 Migration ValuationCriteria starts");
	
			backupVCCollection();
			migrateVCCodes(mappingVCCodes);
			
			Logger.info("1/4 Migration ValuationCriteria end");
			
			//Migrate Runs
			Logger.info("2/4 Migration Run starts");
	
			backupRunCollection();
			migrateRunVCCodes(mappingVCCodes);
			
			Logger.info("2/4 Migration Run end");
			
			//Migrate Analysis...
			Logger.info("3/4 Migration Analysis starts");
	
			backupAnalysisCollection();
			migrateAnalysisVCCodes(mappingVCCodes);
			
			Logger.info("3/4 Migration Analysis end");
			
			//Migrate ReadSets...		
			Logger.info("4/4 Migration ReadSet starts");
	
			backupReadSetCollection(keys);
			migrateReadSetVCCodes(mappingVCCodes, keys);
			
			Logger.info("4/4 Migration ReadSet end");
	 		
		}
		return ok("Migrations finish");
	}


	


	


	
	
	
	private static HashMap<String, String> defineMapping() {	
		HashMap<String, String> mappingVCCodes = new HashMap<String, String>();
		//old code to new code
		mappingVCCodes.put("VC-20140604170100", "VC-RMISEQ-PE-101-v1");
		mappingVCCodes.put("VC-20140604170101","VC-RMISEQ-PE-151-v1");
		mappingVCCodes.put("VC-20140604170200","VC-RMISEQ-PE-251-v1");
		mappingVCCodes.put("VC-20140604170201","VC-RMISEQ-PE-301-v1");
		mappingVCCodes.put("VC-20140604165527","VC-RHS2500R-PE-101-v1");
		mappingVCCodes.put("VC-20140521183026","VC-RHS2500R-PE-151-v1");
		mappingVCCodes.put("VC-20140604164026","VC-RHS2500-PE-101-v1");
		mappingVCCodes.put("VC-20140604170000","VC-RHS2000-PE-101-v1");
		mappingVCCodes.put("VC-20140428152037","VC-ReadsetBlePE-v1");
		mappingVCCodes.put("VC-20140428152226","VC-ReadsetBleMP-v1");
		mappingVCCodes.put("VC-20140709175326","VC-AnalysisBPA-v1");
		mappingVCCodes.put("VC-20140709172226","VC-RHS2000-PE-101-v1");		
		//new code to... new code (for not lose them after a second migration)
		mappingVCCodes.put("VC-RMISEQ-PE-101-v1", "VC-RMISEQ-PE-101-v1");
		mappingVCCodes.put("VC-RMISEQ-PE-151-v1","VC-RMISEQ-PE-151-v1");
		mappingVCCodes.put("VC-RMISEQ-PE-251-v1","VC-RMISEQ-PE-251-v1");
		mappingVCCodes.put("VC-RMISEQ-PE-301-v1","VC-RMISEQ-PE-301-v1");
		mappingVCCodes.put("VC-RHS2500R-PE-101-v1","VC-RHS2500R-PE-101-v1");
		mappingVCCodes.put("VC-RHS2500R-PE-151-v1","VC-RHS2500R-PE-151-v1");
		mappingVCCodes.put("VC-RHS2500-PE-101-v1","VC-RHS2500-PE-101-v1");
		mappingVCCodes.put("VC-RHS2000-PE-101-v1","VC-RHS2000-PE-101-v1");
		mappingVCCodes.put("VC-ReadsetBlePE-v1","VC-ReadsetBlePE-v1");
		mappingVCCodes.put("VC-ReadsetBleMP-v1","VC-ReadsetBleMP-v1");
		mappingVCCodes.put("VC-AnalysisBPA-v1","VC-AnalysisBPA-v1");
		mappingVCCodes.put("VC-RHS2000-PE-101-v1","VC-RHS2000-PE-101-v1");
		return mappingVCCodes;
	}
	
	
	private static boolean verifyIntegrity(HashMap<String, String> mappingVCCodes, BasicDBObject keys) {	
		HashMap<String, Boolean> hCriteriaCode = new HashMap<String, Boolean>(); 	
		//valuation criteria control
		List<ValuationCriteria> valuationCriterias = MongoDBDAO.find(InstanceConstants.VALUATION_CRITERIA_COLL_NAME, ValuationCriteria.class, DBQuery.exists("code")).toList();
		for (ValuationCriteria vc : valuationCriterias) {
			if ((vc.code != null) && !hCriteriaCode.containsKey(vc.code)) {
				hCriteriaCode.put(vc.code, Boolean.FALSE);
			}
		}
		//run control
		List<Run> runs = MongoDBDAO.find(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, DBQuery.exists("valuation.criteriaCode")).toList();
		for (Run run : runs) {
			if ((run.valuation.criteriaCode != null) && !hCriteriaCode.containsKey(run.valuation.criteriaCode)) {
				hCriteriaCode.put(run.valuation.criteriaCode, Boolean.FALSE);
			}
		}	
		//analysis control
		List<Analysis> analysis = MongoDBDAO.find(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, DBQuery.exists("valuation.criteriaCode")).toList();
		for (Analysis analyse : analysis) {
			if ((analyse.valuation.criteriaCode != null) && !hCriteriaCode.containsKey(analyse.valuation.criteriaCode)) {
				hCriteriaCode.put(analyse.valuation.criteriaCode, Boolean.FALSE);
			}
		}
		//readset control
		List<ReadSet> readSets = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, 
				DBQuery.or(DBQuery.exists("bioinformaticValuation.criteriaCode"), DBQuery.exists("productionValuation.criteriaCode")), keys).toList();
		for(ReadSet readSet : readSets){
			if ((readSet.bioinformaticValuation.criteriaCode != null) && !hCriteriaCode.containsKey(readSet.bioinformaticValuation.criteriaCode)) {
				hCriteriaCode.put(readSet.bioinformaticValuation.criteriaCode, Boolean.FALSE);
			}
			if ((readSet.productionValuation.criteriaCode != null) && !hCriteriaCode.containsKey(readSet.productionValuation.criteriaCode)) {
				hCriteriaCode.put(readSet.productionValuation.criteriaCode, Boolean.FALSE);
			}
		}	
		//checkup
		boolean find = false;
		for (String realKey : hCriteriaCode.keySet()) {
			find = false;
			for (String refKey : mappingVCCodes.keySet()) {	
				if (realKey.equals(refKey)) {
					find = true;
					break;
				}
			}
			if (!find) {
				hCriteriaCode.put(realKey, Boolean.TRUE);				
				//Logger.error("ERROR : real key : " + realKey + "not found in Reference List keys (mappingVCCodes)");
			}
		}
		
		boolean error = false;
		for (String key : hCriteriaCode.keySet()) {
			if (hCriteriaCode.get(key)) {
				Logger.error("ERROR : real key : " + key + "not found in Reference List keys (mappingVCCodes)");
				error = true;
			}
		}
		
		if (error){
			Logger.error("ERROR : Database has orphelin(s) criteriaCode(s). Bad integrity.");
		}
		return !error;
	}
	
	
	
	
	private static void backupVCCollection() {
		String backupName = InstanceConstants.VALUATION_CRITERIA_COLL_NAME+"_BCK_VCC_"+sdf.format(new java.util.Date());
		Logger.info("\tCopie "+InstanceConstants.VALUATION_CRITERIA_COLL_NAME+" to "+backupName+" start");		
		MongoDBDAO.save(backupName, MongoDBDAO.find(InstanceConstants.VALUATION_CRITERIA_COLL_NAME, ValuationCriteria.class, DBQuery.exists("code")).toList());
		Logger.info("\tCopie "+InstanceConstants.VALUATION_CRITERIA_COLL_NAME+" to "+backupName+" end");
	}
	
	
	private static void backupRunCollection() {
		String backupName = InstanceConstants.RUN_ILLUMINA_COLL_NAME+"_BCK_VCC_"+sdf.format(new java.util.Date());
		Logger.info("\tCopie "+InstanceConstants.RUN_ILLUMINA_COLL_NAME+" to "+backupName+" start");		
		MongoDBDAO.save(backupName, MongoDBDAO.find(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, DBQuery.exists("valuation.criteriaCode")).toList());
		Logger.info("\tCopie "+InstanceConstants.RUN_ILLUMINA_COLL_NAME+" to "+backupName+" end");
	}
	
	
	private static void backupReadSetCollection(BasicDBObject keys) {
		String backupName = InstanceConstants.READSET_ILLUMINA_COLL_NAME+"_BCK_VCC_"+sdf.format(new java.util.Date());
		Logger.info("\tCopie "+InstanceConstants.READSET_ILLUMINA_COLL_NAME+" to "+backupName+" start");		
		MongoDBDAO.save(backupName, MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, 
				DBQuery.or(DBQuery.exists("bioinformaticValuation.criteriaCode"), DBQuery.exists("productionValuation.criteriaCode")), keys).toList());
		Logger.info("\tCopie "+InstanceConstants.READSET_ILLUMINA_COLL_NAME+" to "+backupName+" end");		
	}
	
	
	private static void backupAnalysisCollection() {
		String backupName = InstanceConstants.ANALYSIS_COLL_NAME+"_BCK_VCC_"+sdf.format(new java.util.Date());
		Logger.info("\tCopie "+InstanceConstants.ANALYSIS_COLL_NAME+" to "+backupName+" start");		
		MongoDBDAO.save(backupName, MongoDBDAO.find(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, DBQuery.exists("valuation.criteriaCode")).toList());
		Logger.info("\tCopie "+InstanceConstants.ANALYSIS_COLL_NAME+" to "+backupName+" end");
	}
	
	
	
	
	
	private static void migrateVCCodes(HashMap<String, String> mappingVCCodes) {
		List<ValuationCriteria> vcs = MongoDBDAO.find(InstanceConstants.VALUATION_CRITERIA_COLL_NAME, ValuationCriteria.class).toList();	
		Logger.info("Expected to migrate "+vcs.size()+" VALUATION CRITERIA");				
		int nb = 0;
		for (ValuationCriteria vc : vcs) {
			MongoDBDAO.update(InstanceConstants.VALUATION_CRITERIA_COLL_NAME, ValuationCriteria.class, DBQuery.is("code", vc.code),   
					DBUpdate.set("code", mappingVCCodes.get(vc.code)));
			nb++;
		}		
		Logger.info("Migrate "+nb+" VALUATION CRITERIA");
	}
	
	
	private static void migrateRunVCCodes(HashMap<String, String> mappingVCCodes) {
		List<Run> runs = MongoDBDAO.find(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, DBQuery.exists("valuation.criteriaCode")).toList();
		Logger.info("Expected to migrate "+runs.size()+" RUNS");		
		int nb = 0;
		for (Run run : runs) {
			if (run.valuation.criteriaCode != null) {
				MongoDBDAO.update(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, DBQuery.is("code", run.code),   
						DBUpdate.set("valuation.criteriaCode", mappingVCCodes.get(run.valuation.criteriaCode)));
				nb++;
			}
		}			
		Logger.info("Migrate "+nb+" RUNS");
	}
	
	private static void migrateAnalysisVCCodes(HashMap<String, String> mappingVCCodes) {
		List<Analysis> analysis = MongoDBDAO.find(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, DBQuery.exists("valuation.criteriaCode")).toList();		
		Logger.info("Expected to migrate "+analysis.size()+" ANALYSIS");		
		int nb = 0; 
		for (Analysis analyse : analysis) {
			if (analyse.valuation != null && analyse.valuation.criteriaCode != null) {
				MongoDBDAO.update(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, DBQuery.is("code", analyse.code),   
						DBUpdate.set("valuation.criteriaCode", mappingVCCodes.get(analyse.valuation.criteriaCode)));
				nb++;
			}
		}
		Logger.info("Migrate "+nb+" ANALYSIS");
	}

	
	private static void migrateReadSetVCCodes(HashMap<String, String> mappingVCCodes, BasicDBObject keys) { 
		List<ReadSet> readSets = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, 
				DBQuery.or(DBQuery.exists("bioinformaticValuation.criteriaCode"), DBQuery.exists("productionValuation.criteriaCode")), keys).toList();
		Logger.info("Expected to migrate "+readSets.size()+" READSETS");
		int nb = 0;
		for(ReadSet readSet : readSets){
			if (readSet.bioinformaticValuation.criteriaCode != null) {
				MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("code", readSet.code),   
						DBUpdate.set("bioinformaticValuation.criteriaCode", mappingVCCodes.get(readSet.bioinformaticValuation.criteriaCode)));
				nb++;
			}
			if (readSet.productionValuation.criteriaCode != null) {
				MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("code", readSet.code),   
						DBUpdate.set("productionValuation.criteriaCode", mappingVCCodes.get(readSet.productionValuation.criteriaCode)));
				nb++;
			}
		}
		Logger.info("Migrate "+nb+" READSETS");	
	}
	
}