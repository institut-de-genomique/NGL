package models.sra.submit.sra.instance;

import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import services.sra.SraValidationHelper;
import services.sra.VariableSRA;
import validation.ContextValidation;
import validation.IValidation;
import validation.common.instance.CommonValidationHelper;

import java.util.Date;

import fr.cea.ig.DBObject;

public class Readset extends DBObject implements IValidation, IStateReference {
	
	private static final play.Logger.ALogger logger = play.Logger.of(Readset.class);
	
	// public String code = null; champs declaré dans DBObject qui ne doit pas etre surchargé ici sinon pas mis dans base
	// au moment du save.
	public String experimentCode      = null;
	public String experimentAccession = null;
	public String runCode             = null;
	public String runAccession        = null;
	public String type                = null; // ILLUMINA ou LS454 ou nanopore ou encore bionano
	public String analysisCode        = null;
	public String analysisAccession   = null;
	public String studyCode           = null;
	public String studyAccession      = null;
	public String studyExternalId     = null;
	public String sampleCode          = null;
	public String sampleAccession     = null;
	public String sampleExternalId    = null;
	public State state                = new State(); // Reference sur "models.laboratory.common.instance.state" 
	public TraceInformation traceInformation = new TraceInformation();
	public Date firstSubmissionDate   = null;
	public Date releaseDate           = null;
	public boolean consolidatedPublicStatus = false;
    public String dropBox;
	
	
	@Override
	public void validate(ContextValidation contextValidation) {
		logger.debug("Validate ngl-sub::Readset");
		//Logger.debug("ok dans Sample.validate\n");
		contextValidation = contextValidation.appendPath("sraReadset");
		CommonValidationHelper.validateIdPrimary    (contextValidation, this);
		SraValidationHelper   .requiredAndConstraint(contextValidation, this.type, VariableSRA.mapTypeReadset(), "typeReadset");
		//logger.debug("sortie de sample.validate pour " + this.code);
	}


	@Override
	public State getState() {
		return state;
	}

	@Override
	public void setState(State state) {
		this.state = state;
	}

	
	@Override
	public TraceInformation getTraceInformation() {
		return traceInformation;
	}
	
}
