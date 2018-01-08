package services.description.instrument;

import java.util.List;
import java.util.Map;

import models.utils.dao.DAOException;
import play.Logger;
import play.data.validation.ValidationError;


public class InstrumentService {
	
	public static void main(Map<String,List<ValidationError>> errors) throws DAOException{
		(new InstrumentServiceGET()).main(errors);
		}
	

}
