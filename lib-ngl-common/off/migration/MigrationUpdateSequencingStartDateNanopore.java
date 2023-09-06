package controllers.migration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import controllers.CommonController;
import fr.cea.ig.MongoDBDAO;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import models.utils.InstanceConstants;
import play.Logger;
import play.mvc.Result;

public class MigrationUpdateSequencingStartDateNanopore extends CommonController{

	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmm");
	
	private static SimpleDateFormat sdfCodeRun = new SimpleDateFormat("yyMMdd");

	public static Result migration()
	{
		//Get all experiment typeCode=nanopore-depot
		List<Experiment> experiments = MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class,DBQuery.is("typeCode", "nanopore-depot").and(DBQuery.is("state.code", "F"))).toList();
		//Backup Run and ReadSet for experiments nanopore-depot
		backUp(experiments);
		
		for(Experiment experiment : experiments){
			Logger.info("experiment code "+experiment.code);
			String containerSupportCode = (String) experiment.instrumentProperties.get("containerSupportCode").value;
			Logger.info("containerSupportCode "+containerSupportCode);
			Date runStartDate = (Date) experiment.experimentProperties.get("runStartDate").value;
			Logger.info("runStartDate "+runStartDate);
			
			Run run = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, DBQuery.is("containerSupportCode", containerSupportCode));
			Logger.info("Update sequencingStartDate for run "+run.code+" = "+runStartDate);
			MongoDBDAO.update(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, DBQuery.is("code", run.code),  DBUpdate.set("sequencingStartDate", runStartDate));
			String newCodeRun = sdfCodeRun.format(runStartDate)+run.code.substring(run.code.indexOf("_"));
			Logger.info("New code run "+newCodeRun);
			MongoDBDAO.update(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, DBQuery.is("code", run.code),  DBUpdate.set("code", newCodeRun));
			
			ReadSet readSet = MongoDBDAO.findOne(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("runCode", run.code));
			Logger.info("Update readSet code "+readSet.code+" = "+runStartDate);
			Logger.info("Update new runCode "+newCodeRun);
			MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("code", readSet.code),  DBUpdate.set("runSequencingStartDate", runStartDate));
			MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("code", readSet.code),  DBUpdate.set("runCode", newCodeRun));
		}
		
		return ok("Migration finish");
	}

	private static void backUp(List<Experiment> experiments)
	{
		String backupNameRun = InstanceConstants.RUN_ILLUMINA_COLL_NAME+"_BCK_SUPSQ1803_"+sdf.format(new Date());
		Logger.info("\tCopie "+InstanceConstants.READSET_ILLUMINA_COLL_NAME+" to "+backupNameRun+" start");
		String backupNameReadSet = InstanceConstants.READSET_ILLUMINA_COLL_NAME+"_BCK_SUPSQ1803_"+sdf.format(new java.util.Date());
		Logger.info("\tCopie "+InstanceConstants.READSET_ILLUMINA_COLL_NAME+" to "+backupNameReadSet+" start");

		List<Run> runs = new ArrayList<Run>();
		List<ReadSet> readSets = new ArrayList<ReadSet>();
		
		for(Experiment experiment : experiments){
			Logger.info("Experiment code "+experiment.code);
			String containerSupportCode = (String) experiment.instrumentProperties.get("containerSupportCode").value;
			Logger.info("Container support Code "+containerSupportCode);
			Date runStartDate = (Date) experiment.experimentProperties.get("runStartDate").value;
			Logger.info("runStartDate "+runStartDate);
			Run run = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, DBQuery.is("containerSupportCode", containerSupportCode));
			runs.add(run);
			Logger.info("Find ReadSet by runCode "+run.code);
			ReadSet readSet = MongoDBDAO.findOne(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("runCode", run.code));
			readSets.add(readSet);
		}
		Logger.info("Size experiment "+experiments.size());
		Logger.info("Size runs "+runs.size());
		Logger.info("Size readSets "+readSets.size());
		
		MongoDBDAO.save(backupNameRun, runs);
		Logger.info("\tCopie "+InstanceConstants.RUN_ILLUMINA_COLL_NAME+" to "+backupNameRun+" end");
		MongoDBDAO.save(backupNameReadSet, readSets);
		Logger.info("\tCopie "+InstanceConstants.READSET_ILLUMINA_COLL_NAME+" to "+backupNameReadSet+" end");

	}

	
}
