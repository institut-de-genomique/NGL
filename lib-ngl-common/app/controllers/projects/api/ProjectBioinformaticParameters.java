package controllers.projects.api;


import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;
import org.mongojack.DBUpdate;

import controllers.SubDocumentController;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.MongoDBResult;
import fr.cea.ig.ngl.NGLApplication;
import models.laboratory.project.instance.BioinformaticParameters;
import models.laboratory.project.instance.Project;
import models.utils.InstanceConstants;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;

public class ProjectBioinformaticParameters extends SubDocumentController<Project, BioinformaticParameters> {

//	@Inject
//	public ProjectBioinformaticParameters(NGLContext ctx) {
//		super(ctx,InstanceConstants.PROJECT_COLL_NAME, Project.class, BioinformaticParameters.class);
//	}

	@Inject
	public ProjectBioinformaticParameters(NGLApplication app) {
		super(app,InstanceConstants.PROJECT_COLL_NAME, Project.class, BioinformaticParameters.class);
	}

	@Override
	protected Object getSubObject(Project objectInDB, String code) {
		return getSubObject(objectInDB);
	}

	@Override
	protected Query getSubObjectQuery(String parentCode, String code) {
		return getSubObjectQuery(parentCode);
	}

	@Override
	protected BioinformaticParameters getSubObjects(Project objectInDB) {
		return objectInDB.bioinformaticParameters;
	}

	protected Query getSubObjectQuery(String parentCode){
		return DBQuery.and(DBQuery.is("code", parentCode), DBQuery.exists("bioinformaticParameters"));
	}


	protected BioinformaticParameters getSubObject(Project object){
		return object.bioinformaticParameters;
	}


	//@Permission(value={"reading"})
	@Override
	public Result get(String parentCode){
		Project objectInDB = getObject(getSubObjectQuery(parentCode));
		if (objectInDB == null) {
			return notFound();			
		}
		return ok(Json.toJson(getSubObject(objectInDB)));		
	}

	//@Permission(value={"reading"})
	@Override
	public Result head(String parentCode){
		if (!isObjectExist(getSubObjectQuery(parentCode))) {
			return notFound();
		}
		return ok();
	}


	//@Permission(value={"bioinformaticParameters"})
	public Result put(String parentCode){
		Project objectInDB = getObject(parentCode);
		if (objectInDB == null) {
			return notFound();
		}
		Form<BioinformaticParameters> filledForm = getSubFilledForm();
		BioinformaticParameters inputBioinfParams = filledForm.get();

		updateObject(DBQuery.is("code", parentCode), 
				DBUpdate.set("bioinformaticParameters", inputBioinfParams)
				.set("traceInformation", getUpdateTraceInformation(objectInDB.traceInformation)));

		return get(parentCode);
	}

	public Result list() {
		MongoDBResult<Project> results = MongoDBDAO.find(InstanceConstants.PROJECT_COLL_NAME, Project.class, DBQuery.exists("bioinformaticParameters.fgGroup"));			
		List<BioinformaticParameters> bioinformaticParameters = results.toList().stream().map(p->p.bioinformaticParameters).collect(Collectors.toList());			
		return ok(Json.toJson(bioinformaticParameters));
	}




}
