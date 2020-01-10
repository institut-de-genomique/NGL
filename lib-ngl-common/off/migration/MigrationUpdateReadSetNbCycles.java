package controllers.migration;		

import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.inject.Inject;

import org.mongojack.DBCursor;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

<<<<<<< HEAD:lib-ngl-common/off/migration/MigrationUpdateReadSetNbCycles.java
import play.Logger;
import play.Play;
import play.libs.Akka;
import play.mvc.Result;
import rules.services.LazyRules6Actor;
import rules.services.RulesActor6;
import rules.services.RulesMessage;
import akka.actor.ActorRef;
import akka.actor.Props;

=======
>>>>>>> master-isoprod-bi-2.1.X:lib-ngl-common/app/controllers/migration/MigrationUpdateReadSetNbCycles.java
import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;

import akka.actor.ActorRef;
import akka.actor.Props;
import controllers.DocumentController;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.MongoDBResult;
import fr.cea.ig.play.migration.NGLContext;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import models.laboratory.run.instance.Treatment;
import models.utils.InstanceConstants;
import play.Logger;
import play.Play;
import play.mvc.Result;
import rules.services.RulesActor6;
import rules.services.RulesMessage;

/**
 * Update SampleOnContainer on ReadSet
 * @author galbini
 *
 */
public class MigrationUpdateReadSetNbCycles extends DocumentController<ReadSet> { //CommonController {
	
	//private static ActorRef rulesActor = Akka.system().actorOf(Props.create(RulesActor6.class));
	// private /*static*/ ActorRef rulesActor;// = akkaSystem().actorOf(Props.create(RulesActor6.class));
	private final LazyRules6Actor rulesActor;
	
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
	
	@Inject
	public MigrationUpdateReadSetNbCycles(NGLContext ctx) {
		super(ctx, InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class);
		// rulesActor = ctx.akkaSystem().actorOf(Props.create(RulesActor6.class));
		rulesActor = ctx.rules6Actor();
	}
	
	public /*static*/ Result migration() throws MongoException, ParseException{
		
		Logger.info("Start MigrationUpdateReadSetNbCycles");
		
		BasicDBObject keys = new BasicDBObject();
		keys.put("code", 1);
		keys.put("treatments.ngsrg", 1);
		keys.put("runSequencingStartDate", 1);
		keys.put("laneNumber", 1);
		keys.put("runCode", 1);
		
		MongoDBResult<ReadSet> results = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, 
				DBQuery.greaterThan("runSequencingStartDate", sdf.parse("2017/12/01")).notExists("treatments.ngsrg.default.nbUsefulCycleRead1").exists("treatments.ngsrg"),keys);
		
		Logger.info("Update nb readsets = "+results.count());
		
		DBCursor<ReadSet> cursor = results.cursor;
		
		while(cursor.hasNext()){
			ReadSet rs = cursor.next();

			Logger.debug("rs "+rs.code+" "+rs.runSequencingStartDate+" "+rs.laneNumber+" "+rs.runCode);
			Logger.debug("rs trt "+rs.treatments.get("ngsrg"));
			//rulesActor.tell(new RulesMessage(Play.application().configuration().getString("rules.key"),"F_RG_1", rs),null);
			keys = new BasicDBObject();
			keys.put("code", 1);
			keys.put("lanes", 1);
			Run run = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, DBQuery.is("code", rs.runCode), keys);
			if(null != run){
				Logger.debug("Load RUN "+run.code);
				Treatment laneNGSRG = run.getLane(rs.laneNumber).treatments.get("ngsrg");
				Treatment readSetNGSRG = rs.treatments.get("ngsrg");
				Logger.debug("laneNGSRG "+laneNGSRG+" "+"readSetNGSRG "+readSetNGSRG);
				if(laneNGSRG!=null && readSetNGSRG!=null){
					boolean isUpdate = false;
					Logger.debug("start copy");
					if(null != laneNGSRG.results.get("default").get("nbUsefulCycleReadIndex2")){
						readSetNGSRG.results.get("default").put("nbUsefulCycleReadIndex2", laneNGSRG.results.get("default").get("nbUsefulCycleReadIndex2"));
						isUpdate = true;
					}
					if(null != laneNGSRG.results.get("default").get("nbUsefulCycleRead2")){
						readSetNGSRG.results.get("default").put("nbUsefulCycleRead2", laneNGSRG.results.get("default").get("nbUsefulCycleRead2"));
						isUpdate = true;
					}
					if(null != laneNGSRG.results.get("default").get("nbUsefulCycleRead1")){
						readSetNGSRG.results.get("default").put("nbUsefulCycleRead1", laneNGSRG.results.get("default").get("nbUsefulCycleRead1"));
						isUpdate = true;
					}
					if(null != laneNGSRG.results.get("default").get("nbUsefulCycleReadIndex1")){
						readSetNGSRG.results.get("default").put("nbUsefulCycleReadIndex1", laneNGSRG.results.get("default").get("nbUsefulCycleReadIndex1"));
					isUpdate = true;
					}
					Logger.debug("isUpdate "+isUpdate);
					if(isUpdate){
						MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("code", rs.code),  DBUpdate.set("treatments.ngsrg", readSetNGSRG));
					}
				}
			}else{
				Logger.error("ReadSet Does not exist: "+rs.runCode);
			}

		}
		
		return ok("Migration Finish");

	}

	

	
	
	

}
