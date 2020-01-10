package controllers.migration.cns;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import akka.actor.ActorRef;
import akka.actor.Props;
import controllers.CommonController;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.play.migration.NGLContext;
import models.laboratory.experiment.instance.AtomicTransfertMethod;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import models.laboratory.run.instance.Treatment;
import models.utils.InstanceConstants;
import play.Logger;
import play.Play;
// import play.libs.Akka;
import play.mvc.Result;
import rules.services.RulesActor6;
import rules.services.RulesMessage;
import validation.ContextValidation;

//import static fr.cea.ig.play.IGGlobals.akkaSystem;

public class MigrationReadSetNanopore extends CommonController{

	/*private static ActorRef rulesActor = // Akka.system().actorOf(Props.create(RulesActor6.class));
			akkaSystem().actorOf(Props.create(RulesActor6.class));*/

	private static ActorRef rulesActor;
	
	@Inject
	public MigrationReadSetNanopore(NGLContext ctx) {
		rulesActor = ctx.akkaSystem().actorOf(Props.create(RulesActor6.class));
	}
	
	public /*static*/ Result migration(String collectionName){

		//Get RunNanopore  from backup for project and sampleCodes
		List<Run> runs = MongoDBDAO.find(collectionName, Run.class, DBQuery.in("sampleCodes", "BWW_V","BWW_AT").is("categoryCode","nanopore")).toList();
		Logger.debug("Size runs "+runs.size());

		//Run run = MongoDBDAO.findOne(collectionName, Run.class, DBQuery.is("code", "170626_MN19358_FAH04417_A"));
		for(Run run : runs){
				//Get experiment
				Experiment experiment = (Experiment) MongoDBDAO.findOne(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.is("typeCode", "nanopore-depot").is("instrumentProperties.containerSupportCode.value", run.containerSupportCode));
				if(experiment!=null){
					Logger.debug("Code experiment for run "+run.code+" "+experiment.code);
					//Call rule
					ContextValidation contextVal = new ContextValidation("ejacoby");
					ArrayList<Object> facts = new ArrayList<Object>();
					facts.add(experiment);
					facts.add(contextVal);
					for(int i=0;i<experiment.atomicTransfertMethods.size();i++){
						AtomicTransfertMethod atomic = experiment.atomicTransfertMethods.get(i);
						if(atomic.viewIndex == null)atomic.viewIndex = i+1; //used to have the position in the list
						facts.add(atomic);
					}


					rulesActor.tell(new RulesMessage(Play.application().configuration().getString("rules.key"),"workflow", facts),null);
				}else{
					Logger.error("No experiment found for run "+run.code);
				}
		}
		return ok("MigrationReadSetNanopore finished");
	}

	public /*static*/ Result updateMinknowBasecalling(String collectionNameRun, String collectionNameReadSet)
	{
		//get all runs to copy treatment
		List<Run> backupRuns = MongoDBDAO.find(collectionNameRun, Run.class).toList();
		for(Run bckRun : backupRuns){
			Logger.debug("Run "+bckRun.code);
			Treatment treatmentMinknowBasecalling = bckRun.treatments.get("minknowBasecalling");
			//get run
			Run run = (Run) MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, DBQuery.is("code", bckRun.code));
			MongoDBDAO.update(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, 
					DBQuery.is("code", run.code),  
					DBUpdate.set("treatments.minknowBasecalling", treatmentMinknowBasecalling));
		}

		//get all readSets to copy treatment
		List<ReadSet> backupReadSets = MongoDBDAO.find(collectionNameReadSet, ReadSet.class).toList();
		for(ReadSet bckReadSet : backupReadSets){
			Logger.debug("ReadSet "+bckReadSet.code);
			Treatment treatmentMinknowBasecalling = bckReadSet.treatments.get("minknowBasecalling");
			//get ReadSet
			ReadSet readSet = MongoDBDAO.findOne(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("code", bckReadSet.code));
			MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, 
					DBQuery.is("code", readSet.code),  
					DBUpdate.set("treatments.minknowBasecalling", treatmentMinknowBasecalling));
		}

		return ok("Update Treatment MinknowBasecalling ok");
	}


}
