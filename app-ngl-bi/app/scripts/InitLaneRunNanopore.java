package scripts;

import java.util.ArrayList;
import java.util.List;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import com.mongodb.BasicDBObject;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.MongoDBResult;
import fr.cea.ig.MongoDBResult.Sort;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import models.laboratory.run.instance.Lane;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import models.utils.InstanceConstants;
import play.Logger;

public class InitLaneRunNanopore extends ScriptNoArgs{

	@Override
	public void execute() throws Exception {
		//Get liste Run Nanopore without lanes
		BasicDBObject keys = new BasicDBObject();
		keys.put("treatments", 0);
		Integer skip = 0;
		
		//On ne peut pas faire un skip sur cette requête car résultats differents à chaque skip
		//MongoDBResult<Run> result = MongoDBDAO.find(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, DBQuery.is("categoryCode", "nanopore").notExists("lanes"),keys);	
		MongoDBResult<Run> result = MongoDBDAO.find(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, DBQuery.is("categoryCode", "nanopore"),keys);	
		Integer nbResult = result.count(); 
		Logger.debug("Nb result "+nbResult);
		while (skip < nbResult) {
			List<Run> cursor = MongoDBDAO.find(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, DBQuery.is("categoryCode", "nanopore"),keys)
					.sort("code", Sort.DESC).skip(skip).limit(1000)
					.toList();
			cursor.forEach(run -> {
				Logger.debug("Run code "+run.code);
				//Get readset Nanopore
				List<ReadSet> readSets = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("runCode", run.code),keys).toList();
				List<String> readSetCodes = new ArrayList<String>();
				for(ReadSet rs : readSets) {
					readSetCodes.add(rs.code);
				}
				run.lanes = new ArrayList<Lane>();
				Lane lane = new Lane();
				lane.number = 1;
				lane.readSetCodes = readSetCodes;
				run.lanes.add(lane);
				MongoDBDAO.update(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class
						,DBQuery.is("code",run.code)
						,DBUpdate.set("lanes",run.lanes));
			});
			skip = skip + 1000;
			Logger.debug("Nb run update "+skip+"/"+nbResult);
		}
	}

}
