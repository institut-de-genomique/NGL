package services.description.run;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import models.laboratory.common.description.Level;
import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.run.description.AnalysisType;
import models.laboratory.run.description.ReadSetType;
import models.laboratory.run.description.RunCategory;
import models.laboratory.run.description.RunType;
import models.utils.dao.DAOException;
import models.utils.dao.DAOHelpers;
import play.data.validation.ValidationError;
import services.description.DescriptionFactory;
import services.description.common.LevelService;

public abstract class AbstractRunService {

	public void main(Map<String, List<ValidationError>> errors)  throws DAOException {
		DAOHelpers.removeAll(ReadSetType.class, ReadSetType.find);
		DAOHelpers.removeAll(AnalysisType.class, AnalysisType.find);

		DAOHelpers.removeAll(RunType.class, RunType.find);
		DAOHelpers.removeAll(RunCategory.class, RunCategory.find);

		saveReadSetType(errors);
		saveAnalysisType(errors);
		saveRunCategories(errors);
		saveRunType(errors);
	}

	public abstract void saveRunType(Map<String, List<ValidationError>> errors)throws DAOException;

	public abstract void saveRunCategories(Map<String, List<ValidationError>> errors)throws DAOException;

	public abstract void saveAnalysisType(Map<String, List<ValidationError>> errors)throws DAOException;

	public abstract void saveReadSetType(Map<String, List<ValidationError>> errors)throws DAOException;

	public static List<PropertyDefinition> getReadSetPropertyDefinitionsNanopore() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("asciiEncoding","asciiEncoding",LevelService.getLevels(Level.CODE.File), String.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("label","label",LevelService.getLevels(Level.CODE.File), String.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("md5","md5",LevelService.getLevels(Level.CODE.File), String.class, false, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Protocole banque","libraryProtocol",LevelService.getLevels(Level.CODE.Content), String.class, false, "single"));

		return propertyDefinitions;
	}

	public static List<PropertyDefinition> getRunNanoporePropertyDefinitions() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Version Flowcell", "flowcellChemistry", LevelService.getLevels(Level.CODE.Run), String.class, false, null, null, 
				"single", null, false, null, null));

		return propertyDefinitions;
	}
	
}
