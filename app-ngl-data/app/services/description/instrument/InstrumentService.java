package services.description.instrument;

import static fr.cea.ig.play.IGGlobals.configuration;

import java.util.List;
import java.util.Map;

import models.utils.dao.DAOException;
//import play.Logger;
import play.data.validation.ValidationError;


public class InstrumentService {

	private static final play.Logger.ALogger logger = play.Logger.of(InstrumentService.class);
	
	public static void main(Map<String,List<ValidationError>> errors) throws DAOException{
//		String institute=play.Play.application().configuration().getString("institute");
		String institute = configuration().getString("institute");
//		if (institute.equals("CNS")) {
//			(new InstrumentServiceCNS()).main(errors);
//		} else if(institute.equals("CNG")) {
//			(new InstrumentServiceCNG()).main(errors);
//		} else if(institute.equals("TEST")) {
//			(new InstrumentServiceTEST()).main(errors);
//		} else {
//			logger.error("You need to specify only one institute ! Now, it's "+ play.Play.application().configuration().getString("institute"));
//		}
		switch (institute) {
		case "CNS"  : new InstrumentServiceCNS().main(errors);  break;
		case "CNG"  : new InstrumentServiceCNG().main(errors);  break;
		case "TEST" : new InstrumentServiceTEST().main(errors); break;
		default     : logger.error("You need to specify only one institute ! Now, it's " + institute);
		}
	}
	
}
