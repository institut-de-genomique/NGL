package controllers.admin.supports.api.objects;

import static validation.utils.ValidationHelper.dynamicCast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;

import controllers.admin.supports.api.NGLObject;
import controllers.admin.supports.api.NGLObjectsSearchForm;
import models.laboratory.common.description.Level;
import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.instance.Container;
import models.utils.InstanceConstants;
import play.Logger;
import validation.ContextValidation;

public class ContainerUpdate extends AbstractUpdate<Container> {

	public ContainerUpdate() {
		super(InstanceConstants.CONTAINER_COLL_NAME, Container.class);		
	}

	@Override
	public Query getQuery(NGLObjectsSearchForm form) {
		Query query = null;

		List<DBQuery.Query> queryElts = new ArrayList<>();
		queryElts.add(getProjectCodeQuery(form, ""));
		queryElts.add(getSampleCodeQuery(form, ""));
		queryElts.addAll(getContentPropertiesQuery(form, ""));
		query = DBQuery.and(queryElts.toArray(new DBQuery.Query[queryElts.size()]));
		query = DBQuery.elemMatch("contents", query);

		if (CollectionUtils.isNotEmpty(form.codes)) {
			query.and(DBQuery.in("code", form.codes));
		} else if(StringUtils.isNotBlank(form.codeRegex)) {
			query.and(DBQuery.regex("code", Pattern.compile(form.codeRegex)));
		}
		return query;
	}

	@Override
	public void update(NGLObject input, ContextValidation cv) {
		Container container = getObject(input.code);
		//Add traceInfo if not
		container.traceInformation.modifyDate=new Date();
		container.traceInformation.modifyUser=cv.getUser();
		
		if (NGLObject.Action.replace.equals(NGLObject.Action.valueOf(input.action))) {
			updateContent(container, input);
		} else if(NGLObject.Action.add.equals(NGLObject.Action.valueOf(input.action))){
			addPropertyContent(container,input);
		}else{
			throw new RuntimeException(input.action + " not implemented");
		}
		//		container.validate(cv);
		container.validate(cv, null, null);
		if (!cv.hasErrors()) {
			updateObject(container);
		}
	}

	private void updateContent(Container container, NGLObject input) {		
		PropertyDefinition pd = PropertyDefinition.find.get().findUnique(input.contentPropertyNameUpdated, Level.CODE.Content);
		//		Object currentValue   = ValidationHelper.convertStringToType(pd.valueType, input.currentValue);
		//		Object newValue       = ValidationHelper.convertStringToType(pd.valueType, input.newValue);
		Object currentValue   = dynamicCast(pd.valueType, input.currentValue);
		Object newValue       = dynamicCast(pd.valueType, input.newValue);
		container.contents.stream()
		.forEach(c -> { 
			if (input.projectCode.equals(c.projectCode) 
					&& input.sampleCode.equals(c.sampleCode) 
					&& c.properties.containsKey(input.contentPropertyNameUpdated) 
					&& currentValue.equals(dynamicCast(pd.valueType, c.properties.get(input.contentPropertyNameUpdated).value))){
				if(StringUtils.isBlank(input.newValue)){
					c.properties.remove(input.contentPropertyNameUpdated);
				}else{
					c.properties.get(input.contentPropertyNameUpdated).assignValue(newValue);
					Logger.debug("Update "+input.contentPropertyNameUpdated+" with "+c.properties.get(input.contentPropertyNameUpdated).getValue()+" for "+c.sampleCode);
				}
			}
		});

	}

	/**
	 * Ajout d'une nouvelle propriete non definie de type single 
	 * @param container
	 * @param input
	 */
	private void addPropertyContent(Container container, NGLObject input) {		
		PropertyDefinition pd = PropertyDefinition.find.get().findUnique(input.contentPropertyNameUpdated, Level.CODE.Content);
		if(pd.propertyValueType.equals(PropertySingleValue.singleType)){
			//Object currentValue   = dynamicCast(pd.valueType, input.currentValue);
			Object newValue       = dynamicCast(pd.valueType, input.newValue);
			container.contents.stream()
			.forEach(c -> { 
				if (input.projectCode.equals(c.projectCode) 
						&& input.sampleCode.equals(c.sampleCode) 
						&& !c.properties.containsKey(input.contentPropertyNameUpdated)){
					//Create new PropertyValue
					PropertySingleValue pv = new PropertySingleValue(newValue);
					c.properties.put(input.contentPropertyNameUpdated, pv);
					Logger.debug("Add "+input.contentPropertyNameUpdated+" with value "+pv.getValue()+" for "+c.sampleCode);
				}
			});
		}else{
			throw new RuntimeException("add property type "+pd.propertyValueType + " not implemented");
		}

	}

	@Override
	public Long getNbOccurrence(NGLObject input) {
		Container container = getObject(input.code);

		PropertyDefinition pd = PropertyDefinition.find.get().findUnique(input.contentPropertyNameUpdated, Level.CODE.Content);
		//		Object value = ValidationHelper.convertStringToType(pd.valueType, input.currentValue);
		Object value = dynamicCast(pd.valueType, input.currentValue);
		return container.contents.stream()
				//		.filter(c -> {
				//			if (input.projectCode.equals(c.projectCode) &&
				//				input.sampleCode.equals(c.sampleCode) &&
				//				value.equals(ValidationHelper.convertStringToType(pd.valueType, c.properties.get(input.contentPropertyNameUpdated).value.toString()))){
				//					return true;
				//			} else {
				//				return false;
				//			}
				//		})
				.filter(c -> input.projectCode.equals(c.projectCode) 
						&& input.sampleCode.equals(c.sampleCode) 
						&& value.equals(dynamicCast(pd.valueType, c.properties.get(input.contentPropertyNameUpdated).value)))
				.count();	
	}

}
