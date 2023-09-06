package controllers.instruments.io.utils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.laboratory.common.instance.property.PropertyFileValue;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.experiment.instance.InputContainerUsed;
import validation.ContextValidation;

public abstract class AbstractMultiInput extends AbstractInputHandler {
	
	protected final play.Logger.ALogger logger;
	
	public AbstractMultiInput() {
		logger = play.Logger.of(getClass());
	}
	
	/**
	 * Get unique keys to register global files.<br>
	 * <br>
	 * <b>Override this method if you want to use global files.</b>
	 * 
	 * @return keys
	 */
	public Set<String> getGlobalFileKeys() {
		return Collections.emptySet();
	}
	
	/**
	 * Create a map from file list (pfvs):<br>
	 * Key: Global file key, compatible with {@link controllers.instruments.io.utils.AbstractMultiInput#getGlobalFileKeys}<br>
	 * Value: PropertyFileValue<br>
	 * <br>
	 * <b>Override this method if you want to use global files.</b>
	 * 
	 * @param experiment the Experiment
	 * @param pfvs the PropertyFileValues
	 * @param contextValidation the ContextValidation
	 * @return globalFilesMap the Global Files Map
	 */
	public Map<String, PropertyFileValue> getGlobalFilesMap(Experiment experiment, List<PropertyFileValue> pfvs, ContextValidation contextValidation) {
		return Collections.emptyMap();
	}
	
	/**
	 * Process a global file. Called for each global file before 
	 * {@link controllers.instruments.io.utils.AbstractMultiInput#importPartialFile}.<br>
	 * <br>
	 * <b>Override this method if you want to use global files.</b>
	 * 
	 * @param experiment the Experiment
	 * @param pfv the PropertyFileValue
	 * @param globalKey the Key
	 * @param contextValidation the ContextValidation
	 * @return experiment
	 * @throws Exception exception
	 */
	public Experiment importGlobalFile(Experiment experiment, PropertyFileValue pfv, String globalKey, ContextValidation contextValidation) throws Exception {
		return experiment;
	}
	
	/**
	 * Called on missing Global File: Override to set custom error message
	 * 
	 * @param globalKey the Key
	 * @param contextValidation the ContextValidation
	 */
	public void missingGlobalFile(String globalKey, ContextValidation contextValidation) {
		contextValidation.addError("Erreurs fichier", "experiments.msg.import.multi.missing.file", String.valueOf(globalKey));
	}
	
	/**
	 * Create a map from file list (pfvs):<br>
	 * Key: Position (String)<br>
	 * Value: PropertyFileValue
	 * 
	 * @param experiment the Experiment
	 * @param pfvs the PropertyFileValues
	 * @param contextValidation the ContextValidation
	 * @return positionsMap
	 */
	public abstract Map<String, PropertyFileValue> getPositionsMap(Experiment experiment, List<PropertyFileValue> pfvs, ContextValidation contextValidation);
	
	/**
	 * Process a position file. Called for each remaining file.
	 * 
	 * @param experiment the Experiment
	 * @param pfv the Position File
	 * @param icu Experiment's IncputContainerUsed
	 * @param contextValidation the ContextValidation
	 * @return experiment
	 * @throws Exception exception
	 */
	public abstract Experiment importPartialFile(Experiment experiment, PropertyFileValue pfv, InputContainerUsed icu, ContextValidation contextValidation) throws Exception;
	
	/**
	 * Called on missing Position(s) File(s): Override to set custom error message
	 * 
	 * @param missingPositions the Missing Positions List
	 * @param contextValidation the ContextValidation
	 */
	public void missingPositionFile(List<String> missingPositions, ContextValidation contextValidation) {
		contextValidation.addError("Erreurs fichier", "experiments.msg.import.multi.missing.position", String.join(", ", missingPositions));
	}
	
	/**
	 * Override this method to apply some prost processing after positions files import.
	 * 
	 * @param experiment the Experiment
	 * @param contextValidation the ContextValidation
	 * @return experiment
	 * @throws Exception exception
	 */
	public Experiment postProcessing(Experiment experiment, ContextValidation contextValidation) throws Exception {
		return experiment;
	}
		

}
