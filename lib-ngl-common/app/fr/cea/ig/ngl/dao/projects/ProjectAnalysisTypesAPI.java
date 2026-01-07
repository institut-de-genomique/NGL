package fr.cea.ig.ngl.dao.projects;

import java.util.Collections;
import java.util.Set;

import javax.inject.Inject;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import fr.cea.ig.ngl.dao.api.InnerDocumentGenericAPI;
import models.laboratory.common.description.Level;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.project.instance.Project;
import validation.ContextValidation;

public class ProjectAnalysisTypesAPI extends InnerDocumentGenericAPI<PropertyValue, ProjectsDAO, Project> {

	@Inject
	public ProjectAnalysisTypesAPI(ProjectsDAO dao) {
		super(dao);
	}
	
	@Override
	protected Set<String> authorizedUpdateFields() {
		return Collections.emptySet();
	}

	@Override
	protected void setContextValidationLevel(ContextValidation ctxVal, Project objectInDB) {
		ctxVal.putObject("level", Level.CODE.Project);
	    ctxVal.putObject("project", objectInDB);
	}

	@Override
	protected void saveInnerResource(ContextValidation ctxVal, Project objectInDB, PropertyValue input,
			String currentUser) {
		TraceInformation ti = objectInDB.traceInformation;
        ti.modificationStamp(ctxVal, currentUser);
        dao.update(DBQuery.is("code", objectInDB.code),
                   DBUpdate.set("properties.analysisTypes", input)
                           .set("traceInformation",       ti));		
	}

	@Override
	protected void deleteInnerResource(Project objectInDB, String currentUser) {
		TraceInformation ti = objectInDB.traceInformation;
        ti.setTraceInformation(currentUser);
        dao.update(DBQuery.is("code", objectInDB.code), 
                   DBUpdate.unset("properties.analysisTypes")
                           .set("traceInformation", ti));
	}

	@Override
	public PropertyValue getInnerObject(Project objectInDB) {
		return objectInDB.properties.get("analysisTypes");
	}

	@Override
	public void setInnerObject(Project objectInDB, PropertyValue input) {
		objectInDB.properties.put("analysisTypes", input);
	}

	@Override
	public String innerObjectName() {
		return "analysisTypes";
	}

}
