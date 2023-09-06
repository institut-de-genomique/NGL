package models.sra.submit.sra.instance;

import models.sra.submit.util.VariableSRA;
import validation.ContextValidation;
import validation.IValidation;
import validation.common.instance.CommonValidationHelper;
import validation.sra.SraValidationHelper;
import fr.cea.ig.DBObject;

public class Readset extends DBObject implements IValidation {
	
	private static final play.Logger.ALogger logger = play.Logger.of(Readset.class);
	
	// public String code = null; champs declaré dans DBObject qui ne doit pas etre surchargé ici sinon pas mis dans base
	// au moment du save.
	public String runCode        = null;
	public String experimentCode = null;
	public String type           = null; // ILLUMINA ou LS454 ou nanopore ou encore bionano
	public String analysisCode   = null;

	@Override
	public void validate(ContextValidation contextValidation) {
		logger.debug("Validate ngl-sub::Readset");
		//Logger.debug("ok dans Sample.validate\n");
		contextValidation = contextValidation.appendPath("sraReadset");
		CommonValidationHelper.validateIdPrimary    (contextValidation, this);
		SraValidationHelper   .requiredAndConstraint(contextValidation, this.type, VariableSRA.mapTypeReadset(), "typeReadset");
		//logger.debug("sortie de sample.validate pour " + this.code);
	}

}
