package services.description.project;

import services.description.DescriptionFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import models.laboratory.common.description.Institute;
import models.laboratory.common.description.Level;
import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.project.description.ProjectCategory;
import models.laboratory.project.description.ProjectType;
import models.utils.dao.DAOException;
import models.utils.dao.DAOHelpers;
import play.data.validation.ValidationError;
import services.description.common.LevelService;
import services.description.Constants;

public class ProjectServiceGET extends AbstractProjectService{

	
	public void saveProjectCategories(Map<String, List<ValidationError>> errors) throws DAOException {
		List<ProjectCategory> l = new ArrayList<ProjectCategory>();
		l.add(DescriptionFactory.newSimpleCategory(ProjectCategory.class,"defaut", "default"));
		DAOHelpers.saveModels(ProjectCategory.class, l, errors);
	}

	public void saveProjectTypes(Map<String, List<ValidationError>> errors) throws DAOException{
		List<ProjectType> l = new ArrayList<ProjectType>();
		
		l.add(DescriptionFactory.newProjectType("Defaut", "default-project", ProjectCategory.find.findByCode("default"), null, DescriptionFactory.getInstitutes(Constants.CODE.GET)));
		l.add(DescriptionFactory.newProjectType("France Génomique", "france-genomique", ProjectCategory.find.findByCode("default"), getFGPropertyDefinitions(), DescriptionFactory.getInstitutes(Constants.CODE.GET)));
		
		DAOHelpers.saveModels(ProjectType.class, l, errors);
		
	}

	private static List<PropertyDefinition> getFGPropertyDefinitions() throws DAOException {
		List<PropertyDefinition> pds = new ArrayList<PropertyDefinition>();
		pds.add(DescriptionFactory.newPropertiesDefinition("Groupe", "fgGroup", LevelService.getLevels(Level.CODE.Project), String.class, true, "single"));
		return pds;
	}
	
	
	
}
