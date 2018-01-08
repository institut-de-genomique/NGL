package services.description.run;

import java.util.List;
import java.util.Map;

import models.utils.dao.DAOException;
import play.Logger;
import play.data.validation.ValidationError;

public class TreatmentService {
	
	public static void main(Map<String,List<ValidationError>> errors) throws DAOException{
		String institute=play.Play.application().configuration().getString("institute");
		if (institute.equals("GET")){
			(new TreatmentServiceGET()).main(errors);
		}else if(institute.equals("CNS")){
			(new TreatmentServiceCNG()).main(errors);
		}else if(institute.equals("CNG")){
			(new TreatmentServiceCNG()).main(errors);
		}else{
			Logger.error("You need to specify only one institute ! Now, it's "+ play.Play.application().configuration().getString("institute"));
		}

	}
}

