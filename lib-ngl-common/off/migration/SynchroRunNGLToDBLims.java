package controllers.migration;		

import java.util.List;

import org.mongojack.DBQuery;

import lims.services.ILimsRunServices;
import models.laboratory.common.instance.TBoolean;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import models.utils.InstanceConstants;
import play.Logger;
import play.api.modules.spring.Spring;
import play.libs.Akka;
import play.mvc.Result;
import rules.services.RulesActor6;
import akka.actor.ActorRef;
import akka.actor.Props;
import controllers.CommonController;
import fr.cea.ig.MongoDBDAO;

/**
 * Update SampleOnContainer on ReadSet
 * @author galbini
 *
 */
public class SynchroRunNGLToDBLims extends CommonController {
	
	public static Result synchro(String runCode){
		
		Logger.info("Start SynchroRunNGLToDBLims for run : "+runCode);
		
		Run run = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, DBQuery.is("code", runCode));		
		List<ReadSet> readSets = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("runCode", run.code)).toList();
		Logger.info("insert run : "+runCode);
		Spring.getBeanOfType(ILimsRunServices.class).insertRun(run, readSets, true);
		
		if(!TBoolean.UNSET.equals(run.valuation.valid)){
			Logger.info("valuation run : "+runCode);
			Spring.getBeanOfType(ILimsRunServices.class).valuationRun(run);
		}
		
		for(ReadSet readSet:readSets){
			if("IW-VQC".equals(readSet.state.code)){
				Logger.info("updateReadSetAfterQC : "+readSet.code);
				Spring.getBeanOfType(ILimsRunServices.class).updateReadSetAfterQC(readSet);
				
			} else if("A".equals(readSet.state.code) || "UA".equals(readSet.state.code)){
				Logger.info("updateReadSetAfterQC : "+readSet.code);
				Spring.getBeanOfType(ILimsRunServices.class).updateReadSetAfterQC(readSet);
				Logger.info("valuationReadSet : "+readSet.code);
				Spring.getBeanOfType(ILimsRunServices.class).valuationReadSet(readSet, true);	
			}
					
		}				
		return ok("SynchroRunNGLToDBLims Finish");

	}

	

	
	
	

}
