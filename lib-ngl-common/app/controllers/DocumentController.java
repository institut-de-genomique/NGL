package controllers;

import java.util.List;

import fr.cea.ig.DBObject;
import fr.cea.ig.ngl.NGLApplication;
import models.laboratory.common.instance.TraceInformation;


/**
 * Root class for DBObject subclasses api controllers. The name does not
 * match any NGL concept at the moment.  
 *
 * @param <T> DBObject subclass to provide controller implementation for
 */
public abstract class DocumentController<T extends DBObject> extends MongoCommonController<T> {

	protected DocumentController(NGLApplication app, String collectionName, Class<T> type) {
		super(app,collectionName, type);
	}
		
	protected DocumentController(NGLApplication app, String collectionName, Class<T> type, List<String> defaultKeys) {
		super(app,collectionName, type, defaultKeys);
		
	}
	
	// remove as this is a duplicate of the TraceInformation method and
	// does not forces the update of the update fields as setTraceInformation
	// has some bogus behavior. 
	protected TraceInformation getUpdateTraceInformation(TraceInformation ti) {
		ti.setTraceInformation(getCurrentUser());
		return ti;
	}
	
	protected TraceInformation getUpdateTraceInformation_(TraceInformation ti) {
		ti.forceModificationStamp(getCurrentUser());
		return ti;
	}

}
