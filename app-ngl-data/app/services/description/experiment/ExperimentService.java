package services.description.experiment;

import java.util.List;
import java.util.Map;

import models.utils.dao.DAOException;
import play.Logger;
import play.data.validation.ValidationError;


public class ExperimentService {
	
	public static void main(Map<String,List<ValidationError>> errors) throws DAOException{
		(new ExperimentServiceGET()).main(errors);
	}
	

}
