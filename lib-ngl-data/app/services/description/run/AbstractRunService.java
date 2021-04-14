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
import play.data.validation.ValidationError;
import services.description.DescriptionFactory;
import services.description.common.LevelService;

public abstract class AbstractRunService {

	public void main(Map<String, List<ValidationError>> errors)  throws DAOException {
//		DAOHelpers.removeAll(ReadSetType .class, ReadSetType .find.get());
//		DAOHelpers.removeAll(AnalysisType.class, AnalysisType.find.get());
//		DAOHelpers.removeAll(RunType     .class, RunType     .find.get());
//		DAOHelpers.removeAll(RunCategory .class, RunCategory .find.get());
		ReadSetType .find.get().removeAll();
		AnalysisType.find.get().removeAll();
		RunType     .find.get().removeAll();
		RunCategory .find.get().removeAll();

		saveReadSetType(errors);
		saveAnalysisType(errors);
		saveRunCategories(errors);
		saveRunType(errors); 
	}

	public abstract void saveRunType(Map<String, List<ValidationError>> errors)throws DAOException;

	public abstract void saveRunCategories(Map<String, List<ValidationError>> errors)throws DAOException;

	public abstract void saveAnalysisType(Map<String, List<ValidationError>> errors)throws DAOException;

	public abstract void saveReadSetType(Map<String, List<ValidationError>> errors)throws DAOException;

	//NGL-2995 Deplace dans le class specifique CNS et CNG a cause de l'ajout de la prop de fichier de readset CollabFileName
	/*public static List<PropertyDefinition> getReadSetPropertyDefinitionsNanopore() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("asciiEncoding",    "asciiEncoding",   LevelService.getLevels(Level.CODE.File),    String.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("label",            "label",           LevelService.getLevels(Level.CODE.File),    String.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("md5",              "md5",             LevelService.getLevels(Level.CODE.File),    String.class, false, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Protocole banque", "libraryProtocol", LevelService.getLevels(Level.CODE.Content), String.class, false, "single"));

		return propertyDefinitions;
	}*/

	public static List<PropertyDefinition> getRunNanoporePropertyDefinitions() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Version Flowcell",
                                                                           "flowcellChemistry",
                                                                           LevelService.getLevels(Level.CODE.Run),
                                                                           String.class,
                                                                           false,
                                                                           null,
                                                                           null,
                                                                           "single",
                                                                           null,
                                                                           false,
                                                                           null,
                                                                           null));

		// NGL-2175: Add missing property
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Types processus banque", 
		                                                                   "libProcessTypeCodes", 
		                                                                   LevelService.getLevels(Level.CODE.Run), 
		                                                                   String.class, 
		                                                                   false, 
		                                                                   null, 
		                                                                   null, 
		                                                                   "list", 
		                                                                   null, 
		                                                                   false, 
		                                                                   null, 
		                                                                   null));
		return propertyDefinitions;
	}
	
}
