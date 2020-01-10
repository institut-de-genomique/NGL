package services.description.sample;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import models.laboratory.common.description.Institute;
import models.laboratory.sample.description.ImportCategory;
import models.laboratory.sample.description.ImportType;
import models.utils.dao.DAOException;
import models.utils.dao.DAOHelpers;
import play.data.validation.ValidationError;
import services.description.Constants;
import services.description.DescriptionFactory;

public abstract class AbstractImportService {
	
	public void main(Map<String, List<ValidationError>> errors)  throws DAOException{
		DAOHelpers.removeAll(ImportType.class, ImportType.find);
		DAOHelpers.removeAll(ImportCategory.class, ImportCategory.find);
		
		saveImportCategories(errors);
		saveImportTypes(errors);
	}

	public abstract void saveImportTypes(Map<String, List<ValidationError>> errors) throws DAOException;

	public abstract void saveImportCategories(Map<String, List<ValidationError>> errors) throws DAOException;

	protected static ImportCategory saveImportCategory(String name, String code) {
		ImportCategory ic = DescriptionFactory.newSimpleCategory(ImportCategory.class,name, code);
		return ic;
	}
	
	public static List<Institute> getInstitutes(Constants.CODE...codes) throws DAOException {
		List<Institute> institutes = new ArrayList<>();
		for(Constants.CODE code : codes){
			institutes.add(Institute.find.findByCode(code.name()));
		}
		return institutes;
	}
	
}
