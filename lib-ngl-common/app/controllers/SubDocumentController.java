package controllers;

// import static play.data.Form.form;
//import static fr.cea.ig.play.IGGlobals.form;

import org.mongojack.DBQuery.Query;

import fr.cea.ig.DBObject;
import fr.cea.ig.ngl.NGLApplication;
import models.laboratory.common.instance.TraceInformation;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;

public abstract class SubDocumentController<T extends DBObject, V> extends MongoCommonController<T> {

	protected Class<V>      subType;
	protected final Form<V> subForm;
	
//	public SubDocumentController(NGLContext ctx, String collectionName,	Class<T> type, Class<V> subType) {
//		super(ctx,collectionName, type);
//		this.subType = subType;
//		this.subForm = ctx.form(subType);
//	}
	
	public SubDocumentController(NGLApplication app, String collectionName,	Class<T> type, Class<V> subType) {
		super(app,collectionName, type);
		this.subType = subType;
		this.subForm = app.form(subType);
	}

	protected TraceInformation getUpdateTraceInformation(TraceInformation ti) {
		ti.setTraceInformation(getCurrentUser());
		return ti;
	}
	
	protected Form<V> getSubFilledForm(){
		return getFilledForm(subForm, subType); 
	} 
	
	//@Permission(value={"reading"})
	public Result list(String parentCode){
		T objectInDB = getObject(parentCode);
		if (objectInDB != null) {
			Object subObjectInDB = getSubObjects(objectInDB);
			if(null != subObjectInDB)return ok(Json.toJson(subObjectInDB));
			else return ok();
		} else{
			return notFound();
		}		
	}
	
	//@Permission(value={"reading"})
	public Result get(String parentCode, String code){
		T objectInDB = getObject(getSubObjectQuery(parentCode, code));
		if (objectInDB == null) {
			return notFound();			
		}
		return ok(Json.toJson(getSubObject(objectInDB, code)));		
	}
	
	//@Permission(value={"reading"})
	public Result head(String parentCode, String code){
		if (!isObjectExist(getSubObjectQuery(parentCode, code))) {
			return notFound();
		}
		return ok();
	}
	
	protected abstract Object getSubObject(T objectInDB, String code) ;

	protected abstract Query getSubObjectQuery(String parentCode, String code);

	protected abstract Object getSubObjects(T objectInDB);

			
}
