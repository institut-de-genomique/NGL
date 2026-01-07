package scripts.from.object;

import java.util.Date;
import java.util.List;

import controllers.admin.supports.api.NGLObject;
import controllers.admin.supports.api.objects.ExperimentUpdate;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.experiment.instance.Experiment;
import play.Logger;
import validation.ContextValidation;

public class ExperimentUpdateFromProperties extends ExperimentUpdate {

	@Override
	public void update(NGLObject input, ContextValidation cv) {
		Experiment exp = getObject(input.code);
		String newValue     = input.newValue;

		if(NGLObject.Action.add.equals(NGLObject.Action.valueOf(input.action))){
			addPropertyInputContainers(exp, input, newValue, cv);
			addPropertyOutputContainers(exp, input, newValue, cv);
		} else {
			throw new RuntimeException(input.action + " not implemented");
		}

		//On garde la validation car ne vérifie pas la description des propriétés content
		exp.validate(cv, null);
		if (!cv.hasErrors()) {
			updateObject(exp);
		}else{
			Logger.debug("ERROR EXP "+exp.code+" = "+cv.getErrors());
		}
	}


	private void addPropertyInputContainers(Experiment exp, NGLObject input, Object newValue,  ContextValidation cv) {
		exp.atomicTransfertMethods
		.stream()
		.map(atm -> atm.inputContainerUseds)
		.flatMap(List::stream)
		.map(icu -> icu.contents)
		.flatMap(List::stream)
		.forEach(content -> {
			if (input.projectCode.equals(content.projectCode) 
					&& input.sampleCode.equals(content.sampleCode) 
					&& !content.properties.containsKey(input.contentPropertyNameUpdated)){
				//Add traceInfoModify
				exp.traceInformation.modifyDate=new Date();
				exp.traceInformation.modifyUser=cv.getUser();
				//Create new PropertyValue
				PropertySingleValue pv = new PropertySingleValue(newValue);
				content.properties.put(input.contentPropertyNameUpdated, pv);
				Logger.debug("Add "+input.contentPropertyNameUpdated+" with value "+pv.getValue()+" for "+content.sampleCode);
			}
		});
	}

	private void addPropertyOutputContainers(Experiment exp, NGLObject input, Object newValue,  ContextValidation cv) {
		exp.atomicTransfertMethods
		.stream()
		.filter(atm -> atm.outputContainerUseds != null)
		.map(atm -> atm.outputContainerUseds)
		.flatMap(List::stream)
		.map(ocu -> ocu.contents)
		.flatMap(List::stream)
		.forEach (content -> {
			if (input.projectCode.equals(content.projectCode) 
					&& input.sampleCode.equals(content.sampleCode)
					&& !content.properties.containsKey(input.contentPropertyNameUpdated) ){
				//Create new PropertyValue
				PropertySingleValue pv = new PropertySingleValue(newValue);
				content.properties.put(input.contentPropertyNameUpdated, pv);
				Logger.debug("Add "+input.contentPropertyNameUpdated+" with value "+pv.getValue()+" for "+content.sampleCode);
			}
		});
	}

	
}
