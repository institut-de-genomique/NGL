package scripts.from.object;

import java.util.Date;

import controllers.admin.supports.api.NGLObject;
import controllers.admin.supports.api.objects.ProcessUpdate;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.processes.instance.Process;
import play.Logger;
import validation.ContextValidation;

public class ProcessUpdateFromProperties extends ProcessUpdate {

	
	@Override
	public void update(NGLObject input, ContextValidation cv) {
		Process process = getObject(input.code);
		

		String newValue = input.newValue;

		if(NGLObject.Action.add.equals(NGLObject.Action.valueOf(input.action))){
			if(!process.sampleOnInputContainer.properties.containsKey(input.contentPropertyNameUpdated)){
				process.traceInformation.modifyDate=new Date();
				process.traceInformation.modifyUser=cv.getUser();
				//Create new PropertyValue
				PropertySingleValue pv = new PropertySingleValue(newValue);
				process.sampleOnInputContainer.properties.put(input.contentPropertyNameUpdated, pv);
				Logger.debug("Add "+input.contentPropertyNameUpdated+" with value "+pv.getValue()+" for "+input.sampleCode);
			}

		}else {
			throw new RuntimeException(input.action + " not implemented");
		}
		//On garde la validation car ne vérifie pas la description des propriétés content
		process.validateUpdate(cv);
		if (!cv.hasErrors()) {
			updateObject(process);
		}else{
			Logger.debug("ERROR PROCESS "+process.code+" = "+cv.getErrors());
		}
	}

	

}
