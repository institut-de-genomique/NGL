package workflows.readset;

import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.Valuation;
import models.laboratory.project.instance.Project;
import models.laboratory.run.instance.File;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.SampleOnContainer;
import models.utils.InstanceConstants;
import models.utils.InstanceHelpers;
import validation.ContextValidation;

@Singleton
public class ReadSetWorkflowsHelper {

	private static final play.Logger.ALogger logger = play.Logger.of(ReadSetWorkflowsHelper.class);
	
	public void updateContainer(ReadSet readSet) {
		//insert sample container properties at the end of the ngsrg
		SampleOnContainer sampleOnContainer = InstanceHelpers.getSampleOnContainer(readSet);
		if (sampleOnContainer != null) {
			MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME,  ReadSet.class, 
					DBQuery.is("code", readSet.code), DBUpdate.set("sampleOnContainer", sampleOnContainer));
		} else {
			logger.error("sampleOnContainer null for {}", readSet.code);
		}			
	}
	
	public void updateDispatch(ReadSet readSet)	{
		//update dispatch
		MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME,  ReadSet.class, 
				DBQuery.is("code", readSet.code), DBUpdate.set("dispatch", Boolean.TRUE));	
	}
	
	public void updateBioinformaticValuation(ReadSet readSet) {
		Valuation newBioinformaticValuation = readSet.bioinformaticValuation.withValuesFrom(readSet.productionValuation);
		readSet.bioinformaticValuation = newBioinformaticValuation;

		MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME,  ReadSet.class, 
				DBQuery.is("code", readSet.code), DBUpdate.set("bioinformaticValuation", readSet.bioinformaticValuation));
	}
	
	public void updateFiles(ReadSet readSet, ContextValidation contextValidation) {
		//met les fichiers dipo ou non d
		State state = State.cloneState(readSet.state, contextValidation.getUser());
		if (readSet.files != null) {
			for (File f : readSet.files) {
//				WriteResult<ReadSet, String> r = 
						MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, 
								          ReadSet.class, 
						                  DBQuery.and(DBQuery.is("code", readSet.code), DBQuery.is("files.fullname", f.fullname)),
						                  DBUpdate.set("files.$.state", state));					
			}
		} else {
			logger.error("No files for {}", readSet.code);
		}
	}
	
	public boolean isHasBA(ReadSet readSet) {
		Project p = MongoDBDAO.findByCode(InstanceConstants.PROJECT_COLL_NAME, Project.class, readSet.projectCode);
		if (p.bioinformaticParameters.biologicalAnalysis) {  //"^.+_.+F_.+_.+$" pour BFY
			return StringUtils.isNotBlank(p.bioinformaticParameters.regexBiologicalAnalysis)
					 ? readSet.code.matches(p.bioinformaticParameters.regexBiologicalAnalysis)
					 : p.bioinformaticParameters.biologicalAnalysis; // GA: matche PE of type F
		}
		return false;
	}
	
	
	
}
