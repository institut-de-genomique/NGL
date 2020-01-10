package services.description.process;

import java.util.List;
import java.util.Map;

import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.processes.description.ProcessCategory;
import models.laboratory.processes.description.ProcessExperimentType;
import models.laboratory.processes.description.ProcessType;
import models.utils.dao.DAOException;
import models.utils.dao.DAOHelpers;
import play.data.validation.ValidationError;

public abstract class AbstractProcessService {

	public void main(Map<String, List<ValidationError>> errors)  throws DAOException{
		DAOHelpers.removeAll(ProcessType.class, ProcessType.find);
		DAOHelpers.removeAll(ProcessCategory.class, ProcessCategory.find);

		saveProcessCategories(errors);
		saveProcessTypes(errors);
	}

	public abstract void saveProcessTypes(Map<String, List<ValidationError>> errors)  throws DAOException;

	public abstract void saveProcessCategories(Map<String, List<ValidationError>> errors) throws DAOException;
	
	
	protected ProcessExperimentType getPET(String expCode, Integer index) {
		return new ProcessExperimentType(getExperimentType(expCode), index);
	}
	
	protected static List<ExperimentType> getExperimentTypes(String...codes) throws DAOException {
		return DAOHelpers.getModelByCodes(ExperimentType.class,ExperimentType.find, codes);
	}
	
	protected static ExperimentType getExperimentType(String code) throws DAOException {
		return DAOHelpers.getModelByCode(ExperimentType.class,ExperimentType.find, code);
	}

}