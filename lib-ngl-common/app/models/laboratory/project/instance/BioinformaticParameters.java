package models.laboratory.project.instance;

import validation.ContextValidation;
import validation.IValidation;

/**
 * Subset of project with bio-informatic data.
 * 
 * @author dnoisett
 * 
 */

public class BioinformaticParameters implements IValidation {
	
	public Boolean biologicalAnalysis      = Boolean.FALSE;	
	public String  regexBiologicalAnalysis;
	public String  mappingReference;
	public String  fgGroup;
	public Integer fgPriority;
	public Boolean localDataDelete         = Boolean.FALSE;
	public Boolean ccrtAutomaticTransfer   = Boolean.FALSE;
	
	@Override
	public String toString() {
		return "BioinformaticParameters [biologicalAnalysis=" + biologicalAnalysis
				+ ", regexBiologicalAnalysis="                + regexBiologicalAnalysis 
				+ ", mappingReference="                       + mappingReference 
				+ ", fgGroup="                                + fgGroup
				+ ", fgPriority="                             + fgPriority
				+ ", localDataDelete="                        + localDataDelete
				+ "]";
	}

	@Override
	public void validate(ContextValidation contextValidation) {		
	}

}
