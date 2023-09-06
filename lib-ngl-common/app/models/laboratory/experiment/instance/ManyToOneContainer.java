package models.laboratory.experiment.instance;

import models.laboratory.container.description.ContainerSupportCategory;
import models.laboratory.instrument.instance.InstrumentUsed;
import validation.ContextValidation;
import validation.experiment.instance.AtomicTransfertMethodValidationHelper;

public class ManyToOneContainer extends AtomicTransfertMethod {

//	public ManyToOneContainer(){
//		super();
//	}
	
	//
	//  if (ocu.code == null || !ocu.code.equals(ocu.locationOnContainerSupport.code) {
	// 		ocu.code = ocu.locationOnContainerSupport.code;
	// <=> if (x == null || x != y)
	//         x = y
	// <=> x = y
	//
	// GA_ 22/06/2016 gestion des cas ou le locationOnContainerSupport.code n'est pas null
	@Override
	public void updateOutputCodeIfNeeded(ContainerSupportCategory outputCsc, String supportCode) {
		// case tube : one support for each atm
		if (outputCsc.nbLine.compareTo(Integer.valueOf(1)) == 0 && outputCsc.nbColumn.compareTo(Integer.valueOf(1)) == 0) {
			outputContainerUseds.forEach((OutputContainerUsed ocu) -> {
				if (ocu.locationOnContainerSupport.code == null && supportCode != null) {
					ocu.locationOnContainerSupport.code = supportCode;
					ocu.code                            = supportCode;
				} else if (ocu.locationOnContainerSupport.code != null 
						&& (ocu.code == null || !ocu.code.equals(ocu.locationOnContainerSupport.code))) {
					ocu.code = ocu.locationOnContainerSupport.code;
				}
			});
		} else if (outputCsc.nbLine.compareTo(Integer.valueOf(1)) > 0 && outputCsc.nbColumn.compareTo(Integer.valueOf(1)) == 0) {
			outputContainerUseds.forEach((OutputContainerUsed ocu) -> {
				if (ocu.locationOnContainerSupport.code == null && supportCode != null) {
					ocu.locationOnContainerSupport.code = supportCode;
					ocu.code = supportCode + "_" + ocu.locationOnContainerSupport.line;
				} else if (ocu.locationOnContainerSupport.code != null && ocu.locationOnContainerSupport.line != null
						&& (ocu.code == null || !ocu.code.equals(ocu.locationOnContainerSupport.code+"_"+ocu.locationOnContainerSupport.line))){
					ocu.code = ocu.locationOnContainerSupport.code + "_" + ocu.locationOnContainerSupport.line;
				}
			});
		} else if (outputCsc.nbLine.compareTo(Integer.valueOf(1)) == 0 && outputCsc.nbColumn.compareTo(Integer.valueOf(1)) > 0) {
			// case strip-8
			outputContainerUseds.forEach((OutputContainerUsed ocu) -> {
				if (ocu.locationOnContainerSupport.code == null && supportCode != null) {
					ocu.locationOnContainerSupport.code = supportCode;
					ocu.code = supportCode + "_" + ocu.locationOnContainerSupport.column;
				} else if (ocu.locationOnContainerSupport.code != null && ocu.locationOnContainerSupport.column != null
						&& (ocu.code == null || !ocu.code.equals(ocu.locationOnContainerSupport.code + "_" + ocu.locationOnContainerSupport.column))){
					ocu.code = ocu.locationOnContainerSupport.code + "_" + ocu.locationOnContainerSupport.column;
				}
			});
		} else if (outputCsc.nbLine.compareTo(Integer.valueOf(1)) > 0 && outputCsc.nbColumn.compareTo(Integer.valueOf(1)) > 0) {
			outputContainerUseds.forEach((OutputContainerUsed ocu) -> {
				if (ocu.locationOnContainerSupport.code == null && supportCode != null) {
					ocu.locationOnContainerSupport.code = supportCode;
					ocu.code = supportCode+"_"+ocu.locationOnContainerSupport.line+ocu.locationOnContainerSupport.column;
				} else if (ocu.locationOnContainerSupport.code != null 
						&& ocu.locationOnContainerSupport.line != null 
						&& ocu.locationOnContainerSupport.column != null
						&& (ocu.code == null || !ocu.code.equals(ocu.locationOnContainerSupport.code+"_"+ocu.locationOnContainerSupport.line+ocu.locationOnContainerSupport.column))){
					ocu.code = ocu.locationOnContainerSupport.code+"_"+ocu.locationOnContainerSupport.line+ocu.locationOnContainerSupport.column;
				}
			});
		}
	}
	

	// stripped down version, supposedly equivalent to the original
	public void updateOutputCodeIfNeeded_(ContainerSupportCategory outputCsc, String supportCode) {
		int nbLine   = outputCsc.nbLine;
		int nbColumn = outputCsc.nbColumn;
		// case tube : one support for each atm
		if (nbLine == 1 && nbColumn == 1) {
			outputContainerUseds.forEach((OutputContainerUsed ocu) -> {
				if (ocu.locationOnContainerSupport.code == null) {
					if (supportCode != null) {
						ocu.locationOnContainerSupport.code = supportCode;
						ocu.code                            = supportCode;
					}
				} else {
					ocu.code = ocu.locationOnContainerSupport.code;
				}
			});
		} else if (nbLine > 1 && nbColumn == 1) {
			outputContainerUseds.forEach((OutputContainerUsed ocu) -> {
				if (ocu.locationOnContainerSupport.code == null) {
					if (supportCode != null) {
						ocu.locationOnContainerSupport.code = supportCode;
						ocu.code                            = supportCode + "_" + ocu.locationOnContainerSupport.line;
					}
				} else { 
					if (ocu.locationOnContainerSupport.line != null)
						ocu.code = ocu.locationOnContainerSupport.code + "_" + ocu.locationOnContainerSupport.line;
				}
			});
		} else if (nbLine == 1 && nbColumn > 1) {
			// case strip-8
			outputContainerUseds.forEach((OutputContainerUsed ocu) -> {
				if (ocu.locationOnContainerSupport.code == null) {
					if (supportCode != null) {
						ocu.locationOnContainerSupport.code = supportCode;
						ocu.code                            = supportCode + "_" + ocu.locationOnContainerSupport.column;
					}
				} else {
					if (ocu.locationOnContainerSupport.column != null)
						ocu.code = ocu.locationOnContainerSupport.code + "_" + ocu.locationOnContainerSupport.column;
				}
			});
		} else if (nbLine > 1 && nbColumn > 1) {
			outputContainerUseds.forEach((OutputContainerUsed ocu) -> {
				if (ocu.locationOnContainerSupport.code == null) {
					if (supportCode != null) {
						ocu.locationOnContainerSupport.code = supportCode;
						ocu.code                            = supportCode + "_" + ocu.locationOnContainerSupport.line + ocu.locationOnContainerSupport.column;
					}
				} else {
					if (ocu.locationOnContainerSupport.line != null && ocu.locationOnContainerSupport.column != null)
						ocu.code = ocu.locationOnContainerSupport.code + "_" + ocu.locationOnContainerSupport.line + ocu.locationOnContainerSupport.column;
				}
			});
		} else {
			// line <= 0 || column <= 0
		}
	}

	@Override
	public void removeOutputContainerCode() {
		outputContainerUseds.forEach(ocu -> ocu.code = null);
	}

	@Deprecated
	@Override
	public void validate(ContextValidation contextValidation) {
		super.validate(contextValidation);
		AtomicTransfertMethodValidationHelper.validateOutputContainerAtMostOne(outputContainerUseds, contextValidation);
		AtomicTransfertMethodValidationHelper.validateOutputContainers(contextValidation, outputContainerUseds);
	}

	@Override
	public void validate(ContextValidation contextValidation, String experimentTypeCode, String stateCode, InstrumentUsed instrumentUsed, String importTypeCode) {
		super.validate(contextValidation, experimentTypeCode, stateCode, instrumentUsed, importTypeCode);
		AtomicTransfertMethodValidationHelper.validateOutputContainerAtMostOne(outputContainerUseds, contextValidation);
		AtomicTransfertMethodValidationHelper.validateOutputContainers(contextValidation, outputContainerUseds, stateCode, instrumentUsed, importTypeCode, experimentTypeCode);
	}
	
}
