package services.description.common;

import static fr.cea.ig.play.IGGlobals.configuration;
import static services.description.DescriptionFactory.newInstitute;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import models.laboratory.common.description.Institute;
import models.utils.dao.DAOException;
import models.utils.dao.DAOHelpers;
import play.data.validation.ValidationError;

public class InstituteService {
	
	public static void main(Map<String, List<ValidationError>> errors) throws DAOException{				
		//DAOHelpers.removeAll(Institute.class, Institute.find);
		saveInstitutes(errors);	
	}

	public static void saveInstitutes(Map<String,List<ValidationError>> errors) throws DAOException{
		List<Institute> l = new ArrayList<>();
//		String institute=play.Play.application().configuration().getString("institute");
		String institute = configuration().getString("institute");
		if(institute.equals("CNG")){
			l.add(newInstitute("Centre National de Génomique","CNG"));
		} else if(institute.equals("CNS")) {
			l.add(newInstitute("Centre National de Séquençage","CNS"));
		} else if(institute.equals("TEST")) {
			l.add(newInstitute("Test","TEST"));
		}
		DAOHelpers.saveModels(Institute.class, l, errors);
	}

}
