package workflows.analyses;

import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;

import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TBoolean;
import models.laboratory.run.instance.Analysis;
import models.laboratory.run.instance.ReadSet;
import models.utils.InstanceConstants;
import validation.ContextValidation;
import workflows.readset.ReadSetWorkflows;

// @Service
@Singleton
public class AnalysisWorkflowsHelper {

	//@Autowired
	// ReadSetWorkflows readSetWorflows;
	
	private final ReadSetWorkflows readSetWorkflows;
	
	@Inject
	public AnalysisWorkflowsHelper(ReadSetWorkflows readSetWorkflows) {
		this.readSetWorkflows = readSetWorkflows;
	}
	
	/*private final WorkflowsCatalog wc;
	
	// Not an injection constructor on purpose
	public AnalysisWorkflowsHelper(WorkflowsCatalog wc) {
		this.wc = wc;
	}*/
	
	public void updateStateMasterReadSetCodes(Analysis analysis, ContextValidation validation, String nextStepCode)	{
		for (String rsCode : analysis.masterReadSetCodes) {
			ReadSet readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, rsCode, getReadSetKeys());
			State nextStep = State.cloneState(readSet.state, validation.getUser());
			nextStep.code = nextStepCode;
			readSetWorkflows.setState(validation, readSet, nextStep);
		}
	}
	
	public void updateBioinformaticValuationMasterReadSetCodes(Analysis analysis, ContextValidation validation, TBoolean valid, String user, Date date)	{
		for(String rsCode : analysis.masterReadSetCodes){
			ReadSet readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, rsCode, getReadSetKeys());
			//if different of state IW-VBA
			//if(valid.equals(TBoolean.UNSET) && !"IW-VBA".equals(readSet.state.code)){
			if((analysis.state.code.equals("IW-V") && !"IW-VBA".equals(readSet.state.code)) ||
					(analysis.state.code.equals("F-V") && TBoolean.TRUE.equals(analysis.valuation.valid))){
					readSet.bioinformaticValuation.valid = valid;
					readSet.bioinformaticValuation.date = date;
					readSet.bioinformaticValuation.user = user;
	
					readSet.traceInformation.modifyDate = new Date();
					readSet.traceInformation.modifyUser = validation.getUser();
	
	
					MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME,  ReadSet.class, 
							DBQuery.is("code", rsCode), DBUpdate.set("bioinformaticValuation", readSet.bioinformaticValuation).set("traceInformation", readSet.traceInformation));
	
			}
		}
	}
	
	public BasicDBObject getReadSetKeys() {
		BasicDBObject keys = new BasicDBObject();
		keys.put("treatments", 0);
		return keys;
	}
	
}
