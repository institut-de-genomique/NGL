package controllers.admin.supports.api.objects;

import static validation.utils.ValidationHelper.dynamicCast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;

import controllers.admin.supports.api.NGLObject;
import controllers.admin.supports.api.NGLObjectsSearchForm;
import models.laboratory.common.description.Level;
import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.experiment.instance.OutputContainerUsed;
import models.utils.InstanceConstants;
import play.Logger;
import validation.ContextValidation;

public class ExperimentUpdate extends AbstractUpdate<Experiment> {

	public ExperimentUpdate() {
		super(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class);		
	}

	@Override
	public Query getQuery(NGLObjectsSearchForm form) {
		Query query = null;

		List<DBQuery.Query> queryElts = new ArrayList<>();
		queryElts.add(getProjectCodeQuery(form, ""));
		queryElts.add(getSampleCodeQuery(form, ""));
		queryElts.addAll(getContentPropertiesQuery(form, ""));
		query = DBQuery.and(queryElts.toArray(new DBQuery.Query[queryElts.size()]));
		query = DBQuery.or(DBQuery.elemMatch("atomicTransfertMethods.outputContainerUseds.contents", query),
				DBQuery.elemMatch("atomicTransfertMethods.inputContainerUseds.contents", query));	

		if (CollectionUtils.isNotEmpty(form.codes)) {
			query.and(DBQuery.in("code", form.codes));
		} else if(StringUtils.isNotBlank(form.codeRegex)) {
			query.and(DBQuery.regex("code", Pattern.compile(form.codeRegex)));
		}
		return query;
	}

	@Override
	public void update(NGLObject input, ContextValidation cv) {
		Experiment exp = getObject(input.code);
		//Add traceInfoModify
		exp.traceInformation.modifyDate=new Date();
		exp.traceInformation.modifyUser=cv.getUser();
		
		PropertyDefinition pd = PropertyDefinition.find.get().findUnique(input.contentPropertyNameUpdated, Level.CODE.Content);
		//		Object currentValue = ValidationHelper.convertStringToType(pd.valueType, input.currentValue);
		//		Object newValue = ValidationHelper.convertStringToType(pd.valueType, input.newValue);
		Object currentValue = dynamicCast(pd.valueType, input.currentValue);
		Object newValue     = dynamicCast(pd.valueType, input.newValue);

		// 1 update input containers
		if (NGLObject.Action.replace.equals(NGLObject.Action.valueOf(input.action))) {
			updateInputContainers           (exp, input, pd, currentValue, newValue);
			updateOutputContainers          (exp, input, pd, currentValue, newValue);
			updateOutputExperimentProperties(exp, input, pd, currentValue, newValue);			
		} else if(NGLObject.Action.add.equals(NGLObject.Action.valueOf(input.action))){
			if(pd.propertyValueType.equals(PropertyValue.singleType)){
				addPropertyInputContainers           (exp, input, pd, newValue);
				addPropertyOutputContainers          (exp, input, pd, newValue);
				Logger.debug("pd levels "+pd.levels);
				if(null!=pd.levels && pd.levels.contains(Level.CODE.Experiment))
					addPropertyOutputExperimentProperties(exp, input, pd, newValue);
			}else{
				throw new RuntimeException("add property type "+pd.propertyValueType + " not implemented");
			}
		} else {
			throw new RuntimeException(input.action + " not implemented");
		}

		//		exp.validate(cv);
		exp.validate(cv, null);
		if (!cv.hasErrors()) {
			updateObject(exp);
		}else{
			Logger.debug("ERROR EXP "+exp.code+" = "+cv.getErrors());
		}
	}

	private void updateInputContainers(Experiment exp, NGLObject input, PropertyDefinition pd, Object currentValue, Object newValue) {
		exp.atomicTransfertMethods
		.stream()
		.map(atm -> atm.inputContainerUseds)
		.flatMap(List::stream)
		.map(icu -> icu.contents)
		.flatMap(List::stream)
		.forEach(content -> {
			if (input.projectCode.equals(content.projectCode) 
					&& input.sampleCode.equals(content.sampleCode) 
					&& content.properties.containsKey(input.contentPropertyNameUpdated) 
					&& currentValue.equals(dynamicCast(pd.valueType, content.properties.get(input.contentPropertyNameUpdated).value))){
				if(StringUtils.isBlank(newValue.toString()))
					content.properties.remove(input.contentPropertyNameUpdated);
				else
					content.properties.get(input.contentPropertyNameUpdated).assignValue(newValue);
			}
		});
	}

	private void addPropertyInputContainers(Experiment exp, NGLObject input, PropertyDefinition pd, Object newValue) {
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
				//Create new PropertyValue
				PropertySingleValue pv = new PropertySingleValue(newValue);
				content.properties.put(input.contentPropertyNameUpdated, pv);
				Logger.debug("Add "+input.contentPropertyNameUpdated+" with value "+pv.getValue()+" for "+content.sampleCode);
			}
		});
	}

	private void addPropertyOutputContainers(Experiment exp,	NGLObject input, PropertyDefinition pd, Object newValue) {
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

	private void updateOutputContainers(Experiment exp,	NGLObject input, PropertyDefinition pd, Object currentValue, Object newValue) {
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
					&& content.properties.containsKey(input.contentPropertyNameUpdated) 
					&& currentValue.equals(dynamicCast(pd.valueType, content.properties.get(input.contentPropertyNameUpdated).value))){
				if(StringUtils.isBlank(newValue.toString()))
					content.properties.remove(input.contentPropertyNameUpdated);
				else
					content.properties.get(input.contentPropertyNameUpdated).assignValue(newValue);
			}
		});
	}

	private void addPropertyOutputExperimentProperties(Experiment exp, NGLObject input, PropertyDefinition pd, Object newValue) {
		exp.atomicTransfertMethods
		.stream()
		.filter(atm -> atm.outputContainerUseds != null)			
		.flatMap(atm -> atm.outputContainerUseds.stream())
		.filter(ocu -> ocu.experimentProperties != null && ifSameProjectSample(input, ocu))
		.forEach(ocu -> {
			if(!ocu.experimentProperties.containsKey(input.contentPropertyNameUpdated)){
				//Create new PropertyValue
				PropertySingleValue pv = new PropertySingleValue(newValue);
				ocu.experimentProperties.put(input.contentPropertyNameUpdated, pv);
				Logger.debug("Add experiment properties "+input.contentPropertyNameUpdated+" with value "+pv.getValue()+" for "+input.sampleCode);
			}
		});					
	}

	private void updateOutputExperimentProperties(Experiment exp, NGLObject input, PropertyDefinition pd, Object currentValue, Object newValue) {
		exp.atomicTransfertMethods
		.stream()
		.filter(atm -> atm.outputContainerUseds != null)			
		.flatMap(atm -> atm.outputContainerUseds.stream())
		.filter(ocu -> ocu.experimentProperties != null && ifSameProjectSample(input, ocu))
		.flatMap(ocu -> ocu.experimentProperties.entrySet().stream())
		.forEach(entry -> {
			if (entry.getKey().equals(input.contentPropertyNameUpdated) 
					&& currentValue.equals(dynamicCast(pd.valueType, entry.getValue().value)))
				entry.getValue().assignValue(newValue);
		});					
	}

	private boolean ifSameProjectSample(NGLObject input, OutputContainerUsed ocu) {
		return ocu.contents
				.stream()
				.anyMatch(content -> input.projectCode.equals(content.projectCode) 
						&& input.sampleCode.equals(content.sampleCode));		
	}

	@Override
	public Long getNbOccurrence(NGLObject input) {
		Experiment exp = getObject(input.code);
		PropertyDefinition pd = PropertyDefinition.find.get().findUnique(input.contentPropertyNameUpdated, Level.CODE.Content);
		//		Object value = ValidationHelper.convertStringToType(pd.valueType, input.currentValue);
		Object value = dynamicCast(pd.valueType, input.currentValue);

		//		Long count = exp.atomicTransfertMethods
		long count = exp.atomicTransfertMethods
				.stream()
				.map(atm -> atm.inputContainerUseds)
				.flatMap(List::stream)
				.map(icu -> icu.contents)
				.flatMap(List::stream)
				//			.filter(content -> {
				//				if(input.projectCode.equals(content.projectCode) &&
				//						input.sampleCode.equals(content.sampleCode) &&
				//						content.properties.containsKey(input.contentPropertyNameUpdated) && 
				//						value.equals(ValidationHelper.convertStringToType(pd.valueType, content.properties.get(input.contentPropertyNameUpdated).value.toString()))){
				//							return true;
				//					}else{
				//						return false;
				//					}
				//			})
				//			.count();
				.filter(content -> input.projectCode.equals(content.projectCode) 
						&& input.sampleCode.equals(content.sampleCode) 
						&& content.properties.containsKey(input.contentPropertyNameUpdated) 
						&& value.equals(dynamicCast(pd.valueType, content.properties.get(input.contentPropertyNameUpdated).value)))
				.count();

		count = count + exp.atomicTransfertMethods
				.stream()
		.filter(atm -> atm.outputContainerUseds != null)
		.map(atm -> atm.outputContainerUseds)
		.flatMap(List::stream)
		.map(ocu -> ocu.contents)
		.flatMap(List::stream)
		//				.filter(content -> {
		//					if(input.projectCode.equals(content.projectCode) &&
		//							input.sampleCode.equals(content.sampleCode) &&
		//							content.properties.containsKey(input.contentPropertyNameUpdated) && 
		//							value.equals(ValidationHelper.convertStringToType(pd.valueType, content.properties.get(input.contentPropertyNameUpdated).value.toString()))){
		//								return true;
		//						}else{
		//							return false;
		//						}
		//				})
		//				.count();
		.filter(content -> input.projectCode.equals(content.projectCode) 
				&& input.sampleCode.equals(content.sampleCode) 
				&& content.properties.containsKey(input.contentPropertyNameUpdated) 
				&& value.equals(dynamicCast(pd.valueType, content.properties.get(input.contentPropertyNameUpdated).value)))
		.count();

		count = count + exp.atomicTransfertMethods
				.stream()
		.filter(atm -> atm.outputContainerUseds != null)			
		.map(atm -> atm.outputContainerUseds)
		.flatMap(List::stream)
		.filter(ocu -> ocu.experimentProperties != null)
		.map(ocu -> ocu.experimentProperties.entrySet())
		.flatMap(Set::stream)
		//				.filter(entry -> (entry.getKey().equals(input.contentPropertyNameUpdated) && ValidationHelper.convertStringToType(pd.valueType, entry.getValue().value.toString()).equals(value)))
		//				.count();	
		.filter(entry -> entry.getKey().equals(input.contentPropertyNameUpdated) 
				&& value.equals(dynamicCast(pd.valueType, entry.getValue().value)))
		.count();	

		return count;
	}

}
