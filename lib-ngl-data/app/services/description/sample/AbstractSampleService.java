package services.description.sample;

import java.util.List;
import java.util.Map;

import models.laboratory.sample.description.SampleCategory;
import models.laboratory.sample.description.SampleType;
import models.utils.dao.DAOException;
import play.data.validation.ValidationError;

public abstract class AbstractSampleService {
	
	public void main(Map<String, List<ValidationError>> errors)  throws DAOException {
//		DAOHelpers.removeAll(SampleType    .class, SampleType    .find.get());
//		DAOHelpers.removeAll(SampleCategory.class, SampleCategory.find.get());
		SampleType    .find.get().removeAll();
		SampleCategory.find.get().removeAll();
		
		saveSampleCategories(errors);
		saveSampleTypes(errors);
	}

	public abstract void saveSampleTypes(Map<String, List<ValidationError>> errors) throws DAOException;

	public abstract void saveSampleCategories(Map<String, List<ValidationError>> errors) throws DAOException;
	
//	public static SampleCategory newSampleCategory(String name, String code) {
//		SampleCategory sc = DescriptionFactory.newSimpleCategory(SampleCategory.class,name, code);
//		return sc;
//	}
	
//	public static List<Institute> getInstitutes(Constants.CODE... codes) throws DAOException {
//		List<Institute> institutes = new ArrayList<>();
//		for(Constants.CODE code : codes){
//			institutes.add(Institute.find.get().findByCode(code.name()));
//		}
//		return institutes;
//	}
}
