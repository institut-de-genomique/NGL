package scripts;

import java.util.List;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import com.mongodb.BasicDBObject;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import models.utils.InstanceConstants;
import play.Logger;

public class UpdateFractionRunReadSet extends ScriptNoArgs{

	@Override
	public void execute() throws Exception {
		//Get liste Run Nanopore without lanes
		BasicDBObject keys = new BasicDBObject();
		keys.put("treatments", 0);

		//On ne peut pas faire un skip sur cette requête car résultats differents à chaque skip
		//A utiliser lorsque la premiere reprise histo faite
		List<ReadSet> result = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, 
				DBQuery.in("typeCode", "rsillumina","rsbionano","rsnanopore")
				.notExists("sampleOnContainer.properties.runFractionPercentage").exists("sampleOnContainer.percentage"),keys).limit(10000).toList();	
		Logger.debug("Nb result "+result.size());
		for(ReadSet readset : result) {
			Run run = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, readset.runCode);
			//Calculate runFractionPercentage
			if(readset.sampleOnContainer!=null && readset.sampleOnContainer.percentage!=null && run.lanes!=null) {
				PropertySingleValue runFractionPercentage = new PropertySingleValue(readset.sampleOnContainer.percentage/run.lanes.size());
				MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, 
						DBQuery.is("code", readset.code),
						DBUpdate.set("sampleOnContainer.properties.runFractionPercentage", runFractionPercentage));
			}else {
				Logger.error("ReadSet without data "+readset.code);
			}
		}
		Logger.debug("End update");

	}
}
