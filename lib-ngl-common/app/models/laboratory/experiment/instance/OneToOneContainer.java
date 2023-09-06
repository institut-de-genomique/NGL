package models.laboratory.experiment.instance;

import models.laboratory.container.description.ContainerSupportCategory;
import models.laboratory.instrument.instance.InstrumentUsed;
import validation.ContextValidation;
import validation.experiment.instance.AtomicTransfertMethodValidationHelper;

/**
 * One input container to (at most) one output container mapping.
 * The 'at most one' looks like this overlaps with the OneToVoid mapping but
 * is in fact used in the N state (N state is OK with no output container).
 */
public class OneToOneContainer extends AtomicTransfertMethod {

//	@Override
//	public void updateOutputCodeIfNeeded(ContainerSupportCategory outputCsc, String supportCode) {
//		// case tube : one support for each atm
//		if (outputCsc.nbLine.compareTo(Integer.valueOf(1)) == 0 && outputCsc.nbColumn.compareTo(Integer.valueOf(1)) == 0) {
//			outputContainerUseds.forEach((OutputContainerUsed ocu) -> {
//					if(null == ocu.locationOnContainerSupport.code && null != supportCode){
//						ocu.locationOnContainerSupport.code = supportCode;
//						ocu.code = supportCode;
//					}else if(null != ocu.locationOnContainerSupport.code 
//							&& (null == ocu.code || !ocu.code.equals(ocu.locationOnContainerSupport.code))){
//						ocu.code = ocu.locationOnContainerSupport.code;
//					}
//			});
//		}else if(outputCsc.nbLine.compareTo(Integer.valueOf(1)) > 0 && outputCsc.nbColumn.compareTo(Integer.valueOf(1)) == 0){
//			outputContainerUseds.forEach((OutputContainerUsed ocu) -> {
//				if (null == ocu.locationOnContainerSupport.code && null != supportCode) {
//					ocu.locationOnContainerSupport.code = supportCode;
//					ocu.code = supportCode+"_"+ocu.locationOnContainerSupport.line;
//				}else if(null != ocu.locationOnContainerSupport.code && null != ocu.locationOnContainerSupport.line
//						&& (null == ocu.code || !ocu.code.equals(ocu.locationOnContainerSupport.code+"_"+ocu.locationOnContainerSupport.line))){
//					ocu.code = ocu.locationOnContainerSupport.code+"_"+ocu.locationOnContainerSupport.line;
//				}
//			});
//		}else if(outputCsc.nbLine.compareTo(Integer.valueOf(1)) == 0 && outputCsc.nbColumn.compareTo(Integer.valueOf(1)) > 0){
//			// case strip-8
//			outputContainerUseds.forEach((OutputContainerUsed ocu) -> {
//				if (null == ocu.locationOnContainerSupport.code && null != supportCode) {
//					ocu.locationOnContainerSupport.code = supportCode;
//					ocu.code = supportCode+"_"+ocu.locationOnContainerSupport.column;
//				}else if(null != ocu.locationOnContainerSupport.code && null != ocu.locationOnContainerSupport.column
//						&& (null == ocu.code || !ocu.code.equals(ocu.locationOnContainerSupport.code+"_"+ocu.locationOnContainerSupport.column))){
//					ocu.code = ocu.locationOnContainerSupport.code+"_"+ocu.locationOnContainerSupport.column;
//				}
//			});
//		}else if(outputCsc.nbLine.compareTo(Integer.valueOf(1)) > 0 && outputCsc.nbColumn.compareTo(Integer.valueOf(1)) > 0){
//			outputContainerUseds.forEach((OutputContainerUsed ocu) -> {
//				if(null == ocu.locationOnContainerSupport.code && null != supportCode){
//					ocu.locationOnContainerSupport.code = supportCode;
//					ocu.code = supportCode+"_"+ocu.locationOnContainerSupport.line+ocu.locationOnContainerSupport.column;
//				}else if(null != ocu.locationOnContainerSupport.code && null != ocu.locationOnContainerSupport.line && null != ocu.locationOnContainerSupport.column
//						&& (null == ocu.code || !ocu.code.equals(ocu.locationOnContainerSupport.code+"_"+ocu.locationOnContainerSupport.line+ocu.locationOnContainerSupport.column))){
//					ocu.code = ocu.locationOnContainerSupport.code+"_"+ocu.locationOnContainerSupport.line+ocu.locationOnContainerSupport.column;
//				}
//			});
//		}
//	}
	
	@Override
	public void updateOutputCodeIfNeeded(ContainerSupportCategory outputCsc, String supportCode) {
		int lineCount   = outputCsc.nbLine;
		int columnCount = outputCsc.nbColumn; 
		// case tube : one support for each atm
		if (lineCount == 1 && columnCount == 1) {
			outputContainerUseds.forEach(ocu -> {
				if (ocu.locationOnContainerSupport.code == null && supportCode != null) {
					ocu.locationOnContainerSupport.code = supportCode;
					ocu.code                            = supportCode;
				} else if (ocu.locationOnContainerSupport.code != null) {
//							&& (ocu.code == null || !ocu.code.equals(ocu.locationOnContainerSupport.code))){
					ocu.code = ocu.locationOnContainerSupport.code;
				}
			});
		} else if (lineCount > 1 && columnCount == 1) {
			outputContainerUseds.forEach(ocu -> {
				if (ocu.locationOnContainerSupport.code == null && supportCode != null) {
					ocu.locationOnContainerSupport.code = supportCode;
					ocu.code = supportCode + "_" + ocu.locationOnContainerSupport.line;
				} else if (  ocu.locationOnContainerSupport.code != null 
						  && ocu.locationOnContainerSupport.line != null) {
//						&& (ocu.code == null || !ocu.code.equals(ocu.locationOnContainerSupport.code + "_" + ocu.locationOnContainerSupport.line))) {
					ocu.code = ocu.locationOnContainerSupport.code + "_" + ocu.locationOnContainerSupport.line;
				}
			});
		} else if (lineCount == 1 && columnCount > 1) {
			// case strip-8
			outputContainerUseds.forEach(ocu -> {
				if (ocu.locationOnContainerSupport.code == null && supportCode != null) {
					ocu.locationOnContainerSupport.code = supportCode;
					ocu.code = supportCode + "_" + ocu.locationOnContainerSupport.column;
				} else if (  ocu.locationOnContainerSupport.code   != null 
						  && ocu.locationOnContainerSupport.column != null) {
//						  && (null == ocu.code || !ocu.code.equals(ocu.locationOnContainerSupport.code+"_"+ocu.locationOnContainerSupport.column))) {
					ocu.code = ocu.locationOnContainerSupport.code + "_" + ocu.locationOnContainerSupport.column;
				}
			});
		} else if (lineCount > 1 && columnCount > 1){
			outputContainerUseds.forEach(ocu -> {
				if (ocu.locationOnContainerSupport.code == null && supportCode != null) {
					ocu.locationOnContainerSupport.code = supportCode;
					ocu.code = supportCode+"_"+ocu.locationOnContainerSupport.line+ocu.locationOnContainerSupport.column;
				} else if (  ocu.locationOnContainerSupport.code   != null 
						  && ocu.locationOnContainerSupport.line   != null 
						  && ocu.locationOnContainerSupport.column != null) {
//						  && (ocu.code != null || !ocu.code.equals(ocu.locationOnContainerSupport.code+"_"+ocu.locationOnContainerSupport.line+ocu.locationOnContainerSupport.column))){
					ocu.code = ocu.locationOnContainerSupport.code + "_" + ocu.locationOnContainerSupport.line+ocu.locationOnContainerSupport.column;
				}
			});
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
		AtomicTransfertMethodValidationHelper.validateInputContainerOne       (contextValidation, inputContainerUseds);
		AtomicTransfertMethodValidationHelper.validateOutputContainerAtMostOne(outputContainerUseds, contextValidation);
		AtomicTransfertMethodValidationHelper.validateOutputContainers        (contextValidation, outputContainerUseds);
	}
	
	@Override
	public void validate(ContextValidation contextValidation, String experimentTypeCode, String stateCode, InstrumentUsed instrumentUsed, String importTypeCode) {
		super.validate(contextValidation, experimentTypeCode, stateCode, instrumentUsed, importTypeCode);
		AtomicTransfertMethodValidationHelper.validateInputContainerOne       (contextValidation, inputContainerUseds);
		AtomicTransfertMethodValidationHelper.validateOutputContainerAtMostOne(outputContainerUseds, contextValidation);
		AtomicTransfertMethodValidationHelper.validateOutputContainers        (contextValidation, outputContainerUseds, stateCode, instrumentUsed, importTypeCode, experimentTypeCode);
	}

}
