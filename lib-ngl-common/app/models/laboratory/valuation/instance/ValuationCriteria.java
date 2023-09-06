package models.laboratory.valuation.instance;

import java.util.List;

import org.mongojack.DBQuery;

import fr.cea.ig.DBObject;
import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.instance.TraceInformation;
import models.utils.InstanceConstants;
import validation.ContextValidation;
import validation.ContextValidation.Mode;
import validation.IValidation;
import validation.utils.ValidationHelper;

public class ValuationCriteria extends DBObject implements IValidation {

	public String name;
	
	public String objectTypeCode;
	
	public List<String> typeCodes;
	
	public List<Property> properties;
	public Boolean active = Boolean.TRUE;
	
	public TraceInformation traceInformation;
	
	@Override
	public void validate(ContextValidation contextValidation) {
		ValidationHelper.validateNotEmpty(contextValidation, name, "name");		
		ValidationHelper.validateNotEmpty(contextValidation, code, "code");	
		
		traceInformation.validate(contextValidation); 
		if(contextValidation.getMode().equals(Mode.CREATION)) {
			if (MongoDBDAO.checkObjectExist(InstanceConstants.VALUATION_CRITERIA_COLL_NAME, ValuationCriteria.class, DBQuery.and(DBQuery.is("name", name)))) {
				contextValidation.addError(name, " deja present dans base ", this);
			}
			if (MongoDBDAO.checkObjectExist(InstanceConstants.VALUATION_CRITERIA_COLL_NAME, ValuationCriteria.class, DBQuery.and(DBQuery.is("code", code)))) {
				contextValidation.addError(code, " deja present dans base ", this);
			}
		}
	}

}
