package services.description.experiment;

import static fr.cea.ig.play.IGGlobals.configuration;

import java.util.List;
import java.util.Map;
import models.utils.dao.DAOException;
// import play.Logger;
import play.data.validation.ValidationError;

public class ExperimentService {
	
	private static final play.Logger.ALogger logger = play.Logger.of(ExperimentService.class);

	public static void main(Map<String,List<ValidationError>> errors) throws DAOException{
//		String institute = play.Play.application().configuration().getString("institute");
		String institute = configuration().getString("institute");
		if (institute.equals("CNS")) {
			new ExperimentServiceCNS().main(errors);
		} else if(institute.equals("CNG")) {
			new ExperimentServiceCNG().main(errors);
		} else if(institute.equals("TEST")) {
			new ExperimentServiceTEST().main(errors);
		} else {
			logger.error("You need to specify only one institute ! Now, it's {}", institute);
		}
	}
	
}
