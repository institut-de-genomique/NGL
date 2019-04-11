package models.laboratory.run.instance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.laboratory.common.description.Level;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.Valuation;
import validation.ContextValidation;
import validation.run.instance.LaneValidationHelper;
import validation.run.instance.TreatmentValidationHelper;

//public class Lane implements IValidation {
public class Lane {

	public Integer                    number;
	public Valuation                  valuation = new Valuation();
	//public List<ReadSet> readsets;
	// dnoisett, the lane doesn't contain the entire readset anymore, just a code to refer it;
	public List<String>               readSetCodes;
	public Map<String, PropertyValue> properties = new HashMap<>(); // <String, PropertyValue>();
	public Map<String,Treatment>      treatments = new HashMap<>();
	
	/*
	nbCycleRead1
	nbCycleReadIndex1
	nbCycleRead2
	nbCycleReadIndex2
	nbCluster
	nbClusterInternalFilter 		nombre de clusters passant les filtres
	percentClusterInternalFilter 	pourcentage de clusters passant les filtres
	nbClusterIlluminaFilter 		nombre de clusters passant le filtre illumina
	percentClusterIlluminaFilter 	pourcentage de clusters passant le filtre illumina
	nbClusterTotal 					nombre de clusters
	nbBaseInternalFilter			nombre de bases total des sequences passant les filtres
	nbTiles 						nombre de tiles
	phasing
	prephasing
	 */

//	@Deprecated
//	@Override
//	public void validate(ContextValidation contextValidation) {
//		LaneValidationHelper.validationLaneNumber      (number, contextValidation);
//		LaneValidationHelper.validateLaneReadSetCodes(contextValidation, number, readSetCodes);
//		LaneValidationHelper.validateLaneValuation     (valuation, contextValidation);
//		contextValidation.putObject("lane", this);
//		contextValidation.putObject("level", Level.CODE.Lane);
//		TreatmentValidationHelper.validationTreatments (contextValidation, treatments);
//		LaneValidationHelper.validationLaneProperties  (this.properties, contextValidation);		
//	}
	
	public void validate(ContextValidation contextValidation, Run run) {
		LaneValidationHelper.validateLaneNumber      (contextValidation, run, number);
		LaneValidationHelper.validateLaneReadSetCodes(contextValidation, number, readSetCodes);
		LaneValidationHelper.validateLaneValuation   (contextValidation, run, valuation);
		contextValidation.putObject("lane", this);             // CTX: to remove
		contextValidation.putObject("level", Level.CODE.Lane); // CTX: to remove
		TreatmentValidationHelper.validateTreatments (contextValidation, treatments, run, this);
		LaneValidationHelper.validateLaneProperties  (contextValidation, run, properties);		
	}

}
