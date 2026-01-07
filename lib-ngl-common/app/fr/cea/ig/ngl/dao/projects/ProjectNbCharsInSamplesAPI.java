package fr.cea.ig.ngl.dao.projects;

import java.util.Collections;
import java.util.Set;

import javax.inject.Inject;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import fr.cea.ig.ngl.dao.api.InnerDocumentGenericAPI;
import models.laboratory.common.description.Level;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.project.instance.Project;
import validation.ContextValidation;

public class ProjectNbCharsInSamplesAPI extends InnerDocumentGenericAPI<Integer, ProjectsDAO, Project> {

	@Inject
	public ProjectNbCharsInSamplesAPI(ProjectsDAO dao) {
		super(dao);
	}
	
	@Override
	protected Set<String> authorizedUpdateFields() {
		return Collections.emptySet();
	}

	@Override
	protected void setContextValidationLevel(ContextValidation ctxVal, Project project) {
		ctxVal.putObject("level", Level.CODE.Project);
	    ctxVal.putObject("project", project);
	}

	@Override
	protected void saveInnerResource(ContextValidation ctxVal, Project project, Integer nbCharactersInSampleCode, String currentUser) {
		TraceInformation ti = project.traceInformation;
        ti.modificationStamp(ctxVal, currentUser);
        dao.update(DBQuery.is("code", project.code),
                   DBUpdate.set("nbCharactersInSampleCode", nbCharactersInSampleCode)
                           .set("traceInformation",       ti));
	}

	@Override
	protected void deleteInnerResource(Project project, String currentUser) {
		TraceInformation ti = project.traceInformation;
        ti.setTraceInformation(currentUser);
        dao.update(DBQuery.is("code", project.code), 
                   DBUpdate.unset("nbCharactersInSampleCode")
                           .set("traceInformation", ti));
	}

	@Override
	public Integer getInnerObject(Project project) {
		return project.nbCharactersInSampleCode;
	}

	@Override
	public void setInnerObject(Project project, Integer nbCharactersInSampleCode) {
		project.nbCharactersInSampleCode = nbCharactersInSampleCode;
	}

	@Override
	public String innerObjectName() {
		return "nbCharactersInSampleCode";
	}

}
