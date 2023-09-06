package validation.parameter.instance;



import fr.cea.ig.MongoDBDAO;
import models.laboratory.project.instance.Project;
import models.utils.InstanceConstants;
import validation.ContextValidation;

public final class MapParameterValidationHelper {
	private MapParameterValidationHelper() {}
	public static void validateParentExist(ContextValidation contextValidation, String code) {
		if( !MongoDBDAO.checkObjectExistByCode(InstanceConstants.PROJECT_COLL_NAME, Project.class, code)) {
			contextValidation.addError("parent",  "Le projet Parent '"+ code +"' n'existe pas");
		} 
	}
	public static void validateChildExist(ContextValidation contextValidation, String code) {
		if( !MongoDBDAO.checkObjectExistByCode(InstanceConstants.PROJECT_COLL_NAME, Project.class, code)) {
			contextValidation.addError("child",  "Le projet Enfant '"+ code +"' n'existe pas");
		} 
	}
}