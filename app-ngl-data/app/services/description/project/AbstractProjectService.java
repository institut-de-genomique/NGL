package services.description.project;

import java.util.List;
import java.util.Map;

import models.laboratory.project.description.ProjectCategory;
import models.laboratory.project.description.ProjectType;
import models.utils.dao.DAOException;
import models.utils.dao.DAOHelpers;
import play.data.validation.ValidationError;

public abstract class AbstractProjectService {
	
	
	public void main(Map<String, List<ValidationError>> errors)  throws DAOException{
		DAOHelpers.removeAll(ProjectType.class, ProjectType.find);
		DAOHelpers.removeAll(ProjectCategory.class, ProjectCategory.find);
		
		saveProjectCategories(errors);
		saveProjectTypes(errors);
	}

	public abstract void saveProjectTypes(Map<String, List<ValidationError>> errors) throws DAOException;

	public abstract void saveProjectCategories(Map<String, List<ValidationError>> errors) throws DAOException;

}
