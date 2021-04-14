package controllers.admin.supports.api.objects;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;
import org.mongojack.DBUpdate.Builder;

import controllers.NGLControllerHelper;
import controllers.admin.supports.api.NGLObject;
import controllers.admin.supports.api.NGLObjectsSearchForm;
import fr.cea.ig.DBObject;
import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.description.Level;
import validation.ContextValidation;

public abstract class AbstractUpdate<T extends DBObject> {
	
	protected String   collectionName;
	protected Class<T> type;
	
	protected AbstractUpdate(String collectionName, Class<T> type) {
		this.collectionName = collectionName;
		this.type           = type;
	}

	protected T getObject(String code) {
    	return MongoDBDAO.findByCode(collectionName, type, code);
    }
	
	protected void updateObject(T o) {
		MongoDBDAO.update(collectionName, o);
	}
	
	protected void updateObject(Query query, Builder builder) {
		MongoDBDAO.update(collectionName, type, query, builder);
	}
	
	public abstract Query getQuery(NGLObjectsSearchForm form);

	protected List<Query> getContentPropertiesQuery(NGLObjectsSearchForm form, String prefix) {
		return NGLControllerHelper.generateQueriesForProperties(form.contentProperties, Level.CODE.Content, prefix+"properties");
	}
		
	protected Query getSampleCodeQuery(NGLObjectsSearchForm form, String prefix) {
		if (StringUtils.isNotBlank(form.sampleCode)) 
			return DBQuery.in(prefix+"sampleCode", form.sampleCode);
		return DBQuery.empty();
	}

	protected Query getProjectCodeQuery(NGLObjectsSearchForm form, String prefix) {		
		if (StringUtils.isNotBlank(form.projectCode))
			return DBQuery.in(prefix+"projectCode", form.projectCode);
		return DBQuery.empty();
	}

	public abstract void update(NGLObject input, ContextValidation cv) ;

	public abstract Long getNbOccurrence(NGLObject o);

}
