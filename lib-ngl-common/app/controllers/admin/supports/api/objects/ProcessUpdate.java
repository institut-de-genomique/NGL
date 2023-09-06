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
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.processes.instance.Process;
import models.utils.InstanceConstants;
import play.Logger;
import validation.ContextValidation;

public class ProcessUpdate extends AbstractUpdate<Process> {

	public ProcessUpdate() {
		super(InstanceConstants.PROCESS_COLL_NAME, Process.class);		
	}
	
	@Override
	public Query getQuery(NGLObjectsSearchForm form) {
		Query query = null;
		
		List<DBQuery.Query> queryElts = new ArrayList<>();
		queryElts.add   (getProjectCodeQuery      (form, "sampleOnInputContainer."));
		queryElts.add   (getSampleCodeQuery       (form, "sampleOnInputContainer."));
		queryElts.addAll(getContentPropertiesQuery(form, "sampleOnInputContainer."));
		query = DBQuery.and(queryElts.toArray(new DBQuery.Query[queryElts.size()]));
		if (CollectionUtils.isNotEmpty(form.codes)) {
			query.and(DBQuery.in("code", form.codes));
		} else if (StringUtils.isNotBlank(form.codeRegex)) {
			query.and(DBQuery.regex("code", Pattern.compile(form.codeRegex)));
		}
		return query;
	}
	
	// Called from a fresh validation update context, there are no hidden 
	// parameter.
	@Override
	public void update(NGLObject input, ContextValidation cv) {
		Process process = getObject(input.code);
		process.traceInformation.modifyDate=new Date();
		process.traceInformation.modifyUser=cv.getUser();
		
		PropertyDefinition pd = PropertyDefinition.find.get().findUnique(input.contentPropertyNameUpdated, Level.CODE.Content);
//		Object newValue = ValidationHelper.convertStringToType(pd.valueType, input.newValue);
		Object newValue = dynamicCast(pd.valueType, input.newValue);
		
		if (NGLObject.Action.replace.equals(NGLObject.Action.valueOf(input.action))) {
			if(StringUtils.isBlank(newValue.toString()))
				process.sampleOnInputContainer.properties.remove(input.contentPropertyNameUpdated);
			else
				process.sampleOnInputContainer.properties.get(input.contentPropertyNameUpdated).assignValue(newValue);	
		} else if(NGLObject.Action.add.equals(NGLObject.Action.valueOf(input.action))){
			if(pd.propertyValueType.equals(PropertyValue.singleType)){
				if(!process.sampleOnInputContainer.properties.containsKey(input.contentPropertyNameUpdated)){
					//Create new PropertyValue
					PropertySingleValue pv = new PropertySingleValue(newValue);
					process.sampleOnInputContainer.properties.put(input.contentPropertyNameUpdated, pv);
					Logger.debug("Add "+input.contentPropertyNameUpdated+" with value "+pv.getValue()+" for "+input.sampleCode);
				}
			}else{
				throw new RuntimeException("add property type "+pd.propertyValueType + " not implemented");
			}
		}else {
			throw new RuntimeException(input.action + " not implemented");
		}
//		process.validate(cv);
		process.validateUpdate(cv);
		if (!cv.hasErrors()) {
			updateObject(process);
		}else{
			Logger.debug("ERROR PROCESS "+process.code+" = "+cv.getErrors());
		}
	}

	@Override
	public Long getNbOccurrence(NGLObject input) {
		return 1L;
	}

}
