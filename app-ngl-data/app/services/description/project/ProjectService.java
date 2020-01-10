package services.description.project;

import static fr.cea.ig.play.IGGlobals.configuration;

import java.util.List;
import java.util.Map;

import models.utils.dao.DAOException;
// import play.Logger;
import play.data.validation.ValidationError;

public abstract class ProjectService {
	
	private static final play.Logger.ALogger logger = play.Logger.of(ProjectService.class);
	
//	public static void main(Map<String,List<ValidationError>> errors) throws DAOException{
//		String institute=play.Play.application().configuration().getString("institute");
//		if (institute.equals("CNS")){
//			(new ProjectServiceCNS()).main(errors);
//		}else if(institute.equals("CNG")){
//			(new ProjectServiceCNG()).main(errors);
//		}else if(institute.equals("TEST")){
//			(new ProjectServiceTEST()).main(errors);
//		}else{
//			Logger.error("You need to specify only one institute ! Now, it's "+ play.Play.application().configuration().getString("institute"));
//		}
//
//		}

	public static void main(Map<String,List<ValidationError>> errors) throws DAOException{
		String institute = configuration().getString("institute");
		switch (institute) {
		case "CNS"  : new ProjectServiceCNS().main(errors);  break;
		case "CNG"  : new ProjectServiceCNG().main(errors);  break;
		case "TEST" : new ProjectServiceTEST().main(errors); break;
		default     : logger.error("You need to specify only one institute ! Now, it's {}", institute);
		}
	}
	
}
