package services.description.process;

import static fr.cea.ig.play.IGGlobals.configuration;

import java.util.List;
import java.util.Map;

import models.utils.dao.DAOException;
//import play.Logger;
import play.data.validation.ValidationError;

public class ProcessService {
	
	private static final play.Logger.ALogger logger = play.Logger.of(ProcessService.class);
	
	public static void main(Map<String,List<ValidationError>> errors) throws DAOException {
//		String institute=play.Play.application().configuration().getString("institute");
//		if (institute.equals("CNS")){
//			(new ProcessServiceCNS()).main(errors);
//		}else if(institute.equals("CNG")){
//			(new ProcessServiceCNG()).main(errors);
//		}else if(institute.equals("TEST")){
//			(new ProcessServiceTEST()).main(errors);
//		}else{
//			Logger.error("You need to specify only one institute ! Now, it's "+ play.Play.application().configuration().getString("institute"));
//		}
		String institute = configuration().getString("institute");
		switch (institute) {
		case "CNS"  : new ProcessServiceCNS().main(errors);  break;
		case "CNG"  : new ProcessServiceCNG().main(errors);  break;
		case "TEST" : new ProcessServiceTEST().main(errors); break;
		default     : logger.error("You need to specify only one institute ! Now, it's " + institute);
		}
	}
	
}
