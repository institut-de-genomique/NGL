package scripts;

import java.util.List;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.SampleOnContainer;
import models.utils.InstanceConstants;
import models.utils.InstanceHelpers;
import play.Logger;

public class AddSampleOnContainerReadSet extends ScriptNoArgs{

	@Override
	public void execute() throws Exception {
		//Get liste ReadSet with no sampleOnContainer
		List<ReadSet> readSets = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.notEquals("state.code", "N").notExists("sampleOnContainer")).toList();
		for(ReadSet readSet : readSets){
			Logger.debug("ReadSet "+readSet.code);
			SampleOnContainer soc = InstanceHelpers.getSampleOnContainer(readSet);
			readSet.sampleOnContainer=soc;
			MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, 
					DBQuery.is("code", readSet.code),
					DBUpdate.set("sampleOnContainer", soc));
		}
	}


}
