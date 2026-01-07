package scripts;

import org.mongojack.DBQuery;

import java.util.*;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;

import play.Logger;

public class ScriptReprise4940 extends ScriptNoArgs {

	@Override
	public void execute() throws Exception {
		Logger.error("Start ScriptReprise4940");

		List<Sample> sampList = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, 
			 DBQuery.exists("properties.sampleOrigin.value"))
		.toList();

		sampList.forEach(samp -> {
			if (samp.properties != null && samp.properties.get("sampleOrigin") != null && ((String) samp.properties.get("sampleOrigin").value).contains("Community_Phase2") && ((String) samp.properties.get("sampleOrigin").value).contains("\n")) {
				Logger.error(samp.code);
				// MongoDBDAO.save(InstanceConstants.SAMPLE_COLL_NAME, samp);
			} 
		});

		Logger.error("End ScriptReprise4940");
	}
}