package scripts.from.object;

import java.util.Date;

import controllers.admin.supports.api.NGLObject;
import controllers.admin.supports.api.objects.ContainerUpdate;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.instance.Container;
import play.Logger;
import validation.ContextValidation;

public class ContainerUpdateFromProperties extends ContainerUpdate {


	@Override
	public void update(NGLObject input, ContextValidation cv) {
		Container container = getObject(input.code);
	

		if(NGLObject.Action.add.equals(NGLObject.Action.valueOf(input.action))){
			addSinglePropertyContent(container,input, cv);
		}else{
			throw new RuntimeException(input.action + " not implemented");
		}
		
		container.validate(cv, null, null);
		if (!cv.hasErrors()) {
			updateObject(container);
		}
		
		//Pas de validation pour les from car absent de la description
		//updateObject(container);
	}

	/**
	 * Ajout d'une nouvelle propriete non definie de type single 
	 * @param container
	 * @param input
	 */
	private void addSinglePropertyContent(Container container, NGLObject input, ContextValidation cv) {		
		String newValue       = input.newValue;
		container.contents.stream()
		.forEach(c -> { 
			if (input.projectCode.equals(c.projectCode) 
					&& input.sampleCode.equals(c.sampleCode) 
					&& !c.properties.containsKey(input.contentPropertyNameUpdated)){
				//Add traceInfo if not
				container.traceInformation.modifyDate=new Date();
				container.traceInformation.modifyUser=cv.getUser();
				//Create new PropertyValue
				PropertySingleValue pv = new PropertySingleValue(newValue);
				c.properties.put(input.contentPropertyNameUpdated, pv);
				Logger.debug("Add "+input.contentPropertyNameUpdated+" with value "+pv.getValue()+" for "+c.sampleCode);
			}
		});

	}

}
