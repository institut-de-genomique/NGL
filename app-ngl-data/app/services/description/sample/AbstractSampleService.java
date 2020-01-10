package services.description.sample;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import models.laboratory.common.description.Institute;
import models.laboratory.sample.description.SampleCategory;
import models.laboratory.sample.description.SampleType;
import models.utils.dao.DAOException;
import models.utils.dao.DAOHelpers;
import play.data.validation.ValidationError;
import services.description.Constants;
import services.description.DescriptionFactory;

public abstract class AbstractSampleService {
	
	public void main(Map<String, List<ValidationError>> errors)  throws DAOException{
		DAOHelpers.removeAll(SampleType.class, SampleType.find);
		DAOHelpers.removeAll(SampleCategory.class, SampleCategory.find);
		
		saveSampleCategories(errors);
		saveSampleTypes(errors);
	}

	public abstract void saveSampleTypes(Map<String, List<ValidationError>> errors) throws DAOException;

	public abstract void saveSampleCategories(Map<String, List<ValidationError>> errors) throws DAOException;
	
	public static SampleCategory newSampleCategory(String name, String code) {
		SampleCategory sc = DescriptionFactory.newSimpleCategory(SampleCategory.class,name, code);
		return sc;
	}
	
	public static List<Institute> getInstitutes(Constants.CODE... codes) throws DAOException {
		List<Institute> institutes = new ArrayList<>();
		for(Constants.CODE code : codes){
			institutes.add(Institute.find.findByCode(code.name()));
		}
		return institutes;
	}
}
