package controllers.instruments.io.utils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import models.laboratory.common.instance.property.PropertyFileValue;
import models.laboratory.experiment.instance.AtomicTransfertMethod;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.experiment.instance.InputContainerUsed;
import validation.ContextValidation;

public final class MultiInputHelper {
	
	private MultiInputHelper() {}
	
	public static final play.Logger.ALogger logger = play.Logger.of(MultiInputHelper.class);
	
	/**
	 * Get InputContainers positions Map:<br>
	 * Key: Position (String) (see {@link controllers.instruments.io.utils.InputHelper#getIcuPosition})<br>
	 * Value: InputContainerUsed
	 * 
	 * @param experiment the Experiment
	 * @return icuPositionsMap the InputContainerUseds Positions Map
	 */
	public static final Map<String, InputContainerUsed> getIcuPositionsMap(Experiment experiment) {
		return experiment.atomicTransfertMethods.stream()
				.flatMap((AtomicTransfertMethod atm) -> atm.inputContainerUseds.stream())
				.collect(Collectors.toMap(InputHelper::getIcuPosition, Function.identity()));
	}
	
	/**
	 * Search for icuPositions missing in filePositions. If one or more is missing, add an error.
	 * 
	 * @param icuPositions the InputContainerUseds Positions Set
	 * @param filePositions the Files Positions Set
	 * @param inputHandler the MultiInputHandler
	 * @param contextValidation the ContextValidation
	 */
	public static final void handleMissingPositions(Set<String> icuPositions, Set<String> filePositions, AbstractMultiInput inputHandler, 
			ContextValidation contextValidation) {
		List<String> missingPositions = icuPositions.stream()
				.filter((String position) -> !filePositions.contains(position))
				.collect(Collectors.toList());
		if(!missingPositions.isEmpty()) {
			inputHandler.missingPositionFile(missingPositions, contextValidation);
		}
	}
	
	/**
	 * Get global-files keys from InputHandler, if key is not present in globalFilesMap then add an error, 
	 * else call {@link controllers.instruments.io.utils.AbstractMultiInput#importGlobalFile}.
	 * 
	 * @param experiment the Experiment
	 * @param inputHandler the MultiInputHandler
	 * @param globalFilesMap the Global Files Map
	 * @param contextValidation the ContextValidation
	 * @throws Exception exception
	 */
	public static final void handleGlobalFiles(Experiment experiment, AbstractMultiInput inputHandler, 
			Map<String, PropertyFileValue> globalFilesMap, ContextValidation contextValidation) throws Exception {
		Set<String> globalFileKeys = inputHandler.getGlobalFileKeys();
		if(!globalFileKeys.isEmpty()) {
			for(String key : globalFileKeys) {
				if(globalFilesMap.containsKey(key)) {
					inputHandler.importGlobalFile(experiment, globalFilesMap.get(key), key, contextValidation);
				} else {
					inputHandler.missingGlobalFile(key, contextValidation);
				}
			}
		}
	}

}
