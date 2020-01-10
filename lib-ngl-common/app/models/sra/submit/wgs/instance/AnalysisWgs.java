package models.sra.submit.wgs.instance;

import java.util.ArrayList;
import java.util.List;

import fr.cea.ig.DBObject;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import validation.ContextValidation;
import validation.IValidation;


public class AnalysisWgs extends DBObject implements IValidation {

	//public String alias;             // required mais remplacé par code herité de DBObject, et valeur = study_projectCode_num
	public String projectCode;         // required pour nos stats  
	public String studyCode;
	public String sampleCode;
	public String title = "";	       // required next soon      
	public String description = "";
	public String submitter_id;
	public String sequenceAssemblyName;
	public Boolean completAssembly = false;
	public int coverage;
	public String programUsedForAssembly;
	public String platformUsedForSequencing;
	public List<DataWgs> runCodes = new ArrayList<>();

	public State state = new State("New", null); // Reference sur "models.laboratory.common.instance.state"
	// pour gerer les differents etats de l'objet.

	public TraceInformation traceInformation = new TraceInformation();// .Reference sur "models.laboratory.common.instance.TraceInformation" 
	// pour loguer les dernieres modifications utilisateurs
	
	@Override
	public void validate(ContextValidation contextValidation) {
	}

}
