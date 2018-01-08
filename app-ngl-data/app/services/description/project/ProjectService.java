package services.description.project;

import java.util.List;
import java.util.Map;

import models.utils.dao.DAOException;
import play.Logger;
import play.data.validation.ValidationError;

public abstract class ProjectService {
	
	public static void main(Map<String,List<ValidationError>> errors) throws DAOException{
		(new ProjectServiceGET()).main(errors);
		}
	
	
}
