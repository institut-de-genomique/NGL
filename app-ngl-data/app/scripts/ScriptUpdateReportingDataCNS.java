package scripts;

import java.util.Date;

import javax.inject.Inject;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.Script;
import fr.cea.ig.ngl.NGLApplication;
import models.Constants;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import play.Logger;
import services.instance.sample.UpdateReportingData;
import validation.ContextValidation;

/**
 * Call reportingData for one sample
 * ReportingData compute statistics on sample 
 * @author ejacoby
 *
 */
public class ScriptUpdateReportingDataCNS extends Script<ScriptUpdateReportingDataCNS.Args>{

	public static class Args {
		//Sample code
		public String code;
	}

	private final NGLApplication app;
	
	@Inject
	public ScriptUpdateReportingDataCNS(NGLApplication app) {
		this.app = app;
	}

	@Override
	public void execute(Args args) throws Exception {
		ContextValidation contextError = ContextValidation.createUndefinedContext(Constants.NGL_DATA_USER);
		contextError.addKeyToRootKeyName("import");
		Date date = new Date();
		Sample sampleToUpdate = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, args.code);
		
		UpdateReportingData update = new UpdateReportingData(app);
		update.updateProcesses(sampleToUpdate);						
		Logger.debug("update sample "+sampleToUpdate.code);
		if (sampleToUpdate.processes != null && sampleToUpdate.processes.size() > 0) {
			MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.is("code", sampleToUpdate.code), 
					DBUpdate.set("processes", sampleToUpdate.processes).set("processesStatistics", sampleToUpdate.processesStatistics).set("processesUpdatedDate", date));
		} else {
			MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.is("code", sampleToUpdate.code), 
					DBUpdate.unset("processes").unset("processesStatistics").set("processesUpdatedDate", date));
		}
	}

	
}
