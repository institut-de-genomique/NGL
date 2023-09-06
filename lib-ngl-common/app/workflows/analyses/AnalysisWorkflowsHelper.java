package workflows.analyses;

import static models.laboratory.common.instance.TBoolean.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;
import org.mongojack.DBUpdate;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;

import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TBoolean;
import models.laboratory.common.instance.property.PropertyListValue;
import models.laboratory.project.instance.Project;
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

	public void updateStateMasterReadSetCodes(Analysis analysis, ContextValidation validation, String nextStepCode, boolean stateInHistory)	{
		//Get listAnalysisType for each project
		Map<String, List<String>> mapAnalysisType = new HashMap<String, List<String>>();
		for(String codeProjet : analysis.projectCodes) {
			Project project = MongoDBDAO.findByCode(InstanceConstants.PROJECT_COLL_NAME, Project.class, codeProjet);
			if(project.properties.containsKey("analysisTypes") && ((PropertyListValue)project.properties.get("analysisTypes")).listValue().size()>0) {
				List<String> listTypes = ((PropertyListValue)project.properties.get("analysisTypes")).listValue().stream()
						.map(o->Objects.toString(o,null))
						.collect(Collectors.toList());
				mapAnalysisType.put(codeProjet, listTypes);
			}
		}
		String stepAnalysis=nextStepCode;
		if(nextStepCode.equals("IW-VBA"))
			stepAnalysis="IW-V";
		if(nextStepCode.equals("F-VBA"))
			stepAnalysis="F-V";
		
		for (String rsCode : analysis.masterReadSetCodes) {
			if(checkMultiAnalysesUpdateStateMasterReadSetCodes(rsCode, mapAnalysisType, stepAnalysis, stateInHistory)) {
				ReadSet readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, rsCode, getReadSetKeys());
				State nextStep = State.cloneState(readSet.state, validation.getUser());
				nextStep.code = nextStepCode;
				if(stepAnalysis.equals("F-BA") || stepAnalysis.equals("IW-V") || (stepAnalysis.equals("F-V") && readSet.bioinformaticValuation.is(TRUE)))
					readSetWorkflows.setState(validation, readSet, nextStep);
			}
		}
	}
	public void updateStateMasterReadSetCodes(Analysis analysis, ContextValidation validation, String nextStepCode)	{

		for (String rsCode : analysis.masterReadSetCodes) {
			ReadSet readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, rsCode, getReadSetKeys());
			State nextStep = State.cloneState(readSet.state, validation.getUser());
			nextStep.code = nextStepCode;
			readSetWorkflows.setState(validation, readSet, nextStep);
		}
	}


	/**
	 * Readset with multi analyses
	 * new rule : check if all analyses set to state 
	 * @param codeReadtSet
	 * @param mapAnalysisType : list of analysis type by project
	 * @param state : state to set
	 * @param stateInHistory : true if get analysis state in state historical 
	 * @return
	 */
	private boolean checkMultiAnalysesUpdateStateMasterReadSetCodes(String codeReadtSet , Map<String, List<String>> mapAnalysisType, String stateCode, boolean stateInHistory)
	{
		//Get ReadSet
		ReadSet readset = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, codeReadtSet, getReadSetKeys());
		if(mapAnalysisType.containsKey(readset.projectCode)) {
			int nbAnalysis = 0;
			for(String type : mapAnalysisType.get(readset.projectCode)) {
				//Find analysis filter by type, readsetcode, state
				Query queryDB = DBQuery.is("typeCode", type).in("masterReadSetCodes", codeReadtSet);
				if(stateInHistory) {
					queryDB.is("state.historical.code", stateCode);
				}else {
					queryDB.is("state.code", stateCode);
				}
				if(MongoDBDAO.find(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, queryDB, getAnalysisKeys()).size()>=1)
					nbAnalysis++;
			}
			if(nbAnalysis==mapAnalysisType.get(readset.projectCode).size())
				return true;
			else
				return false;
		}else
			return true;

	}

	public void updateBioinformaticValuationMasterReadSetCodes(Analysis analysis, ContextValidation validation, TBoolean valid, String user, Date date)	{
		for(String rsCode : analysis.masterReadSetCodes){
			ReadSet readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, rsCode, getReadSetKeys());
			//if different of state IW-VBA
			//if(valid.equals(TBoolean.UNSET) && !"IW-VBA".equals(readSet.state.code)){
			if((analysis.state.code.equals("IW-V") && !"IW-VBA".equals(readSet.state.code)) ||
					(analysis.state.code.equals("F-V") && analysis.valuation.is(TRUE))){
				readSet.bioinformaticValuation = readSet.bioinformaticValuation.withValues(user, valid, date);

				readSet.traceInformation.modificationStamp(validation.getUser());

				MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME,  ReadSet.class, 
						DBQuery.is("code", rsCode), DBUpdate.set("bioinformaticValuation", readSet.bioinformaticValuation).set("traceInformation", readSet.traceInformation));

			}
		}
	}
	
	public void updateBioinformaticValuationMasterReadSetCodes(Analysis analysis, ContextValidation validation, TBoolean valid, String user, Date date, boolean checkMultiAnalysis) {
		Map<String, List<String>> mapAnalysisType = new HashMap<String, List<String>>();
		for(String codeProjet : analysis.projectCodes) {
			Project project = MongoDBDAO.findByCode(InstanceConstants.PROJECT_COLL_NAME, Project.class, codeProjet);
			if(project.properties.containsKey("analysisTypes") && ((PropertyListValue)project.properties.get("analysisTypes")).listValue().size()>0) {
				List<String> listTypes = ((PropertyListValue)project.properties.get("analysisTypes")).listValue().stream()
						.map(o->Objects.toString(o,null))
						.collect(Collectors.toList());
				mapAnalysisType.put(codeProjet, listTypes);
			}
		}
		for(String rsCode : analysis.masterReadSetCodes){
			ReadSet readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, rsCode, getReadSetKeys());
			if(checkMultiAnalysesValuationMasterReadSetCodes(readSet,mapAnalysisType, valid)) {
				if(analysis.state.code.equals("F-V") && analysis.valuation.is(TRUE)){
					readSet.bioinformaticValuation = readSet.bioinformaticValuation.withValues(user, valid, date);

					readSet.traceInformation.modificationStamp(validation.getUser());


					MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME,  ReadSet.class, 
							DBQuery.is("code", rsCode), DBUpdate.set("bioinformaticValuation", readSet.bioinformaticValuation).set("traceInformation", readSet.traceInformation));

				}
			}
		}
	}
	
	private boolean checkMultiAnalysesValuationMasterReadSetCodes(ReadSet readSet, Map<String, List<String>> mapAnalysisType,  TBoolean valid)
	{
		if(mapAnalysisType.containsKey(readSet.projectCode)) {
			int nbAnalysis = 0;
			for(String type : mapAnalysisType.get(readSet.projectCode)) {
				//Find analysis filter by type, readsetcode, state
				Query queryDB = DBQuery.is("typeCode", type).in("masterReadSetCodes", readSet.code).is("valuation.valid", valid);
				if(MongoDBDAO.find(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, queryDB, getAnalysisKeys()).size()>=1)
					nbAnalysis++;
			}
			if(nbAnalysis==mapAnalysisType.get(readSet.projectCode).size())
				return true;
			else
				return false;
		}else
			return true;

	}

	public BasicDBObject getReadSetKeys() {
		BasicDBObject keys = new BasicDBObject();
		keys.put("treatments", 0);
		return keys;
	}
	
	public BasicDBObject getAnalysisKeys() {
		BasicDBObject keys = new BasicDBObject();
		keys.put("treatments", 0);
		return keys;
	}

}
