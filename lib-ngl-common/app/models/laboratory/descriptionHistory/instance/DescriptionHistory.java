package models.laboratory.descriptionHistory.instance;

import java.util.Date;

import fr.cea.ig.DBObject;
import play.libs.Json;
import validation.ContextValidation;
import validation.IValidation;
import validation.common.instance.CommonValidationHelper;

public class DescriptionHistory extends DBObject implements IValidation {

    public String comment;
    public Date date;
    public String user;
    public String objMAjJson;
    public String type;

    public DescriptionHistory() {		
		date = new Date();
		
	}
	
	public DescriptionHistory(String code,String comment, Date date, String user, String objMAjJson,String type) {
		this.code                = code.toLowerCase().replace("\\s+", "-");
		this.comment             = comment;
        this.date                = date;
        this.user                = user;
        this.objMAjJson          = objMAjJson;
        this.type                = type;		
	}

    @Override
    public void validate(ContextValidation contextValidation) {
        CommonValidationHelper  .validateIdPrimary(contextValidation, this);
        CommonValidationHelper . validateComment(contextValidation,comment);
        CommonValidationHelper . validateUser(contextValidation,user);    
        CommonValidationHelper . validateObjMaj(contextValidation, objMAjJson);        
    }

    public static <T extends DBObject> String asObjMAjJson(T object){
		return Json.stringify(Json.toJson(object));
    }
}

