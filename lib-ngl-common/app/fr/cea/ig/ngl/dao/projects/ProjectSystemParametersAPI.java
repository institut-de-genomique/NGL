package fr.cea.ig.ngl.dao.projects;

import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.api.GenericAPI;
import fr.cea.ig.ngl.dao.api.InnerDocumentGenericAPI;
import models.laboratory.common.description.Level;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.project.instance.Project;
import models.laboratory.project.instance.SystemParameters;
import models.laboratory.project.instance.SystemParameters.ArchiveEntry;
import validation.ContextValidation;

public class ProjectSystemParametersAPI extends InnerDocumentGenericAPI<SystemParameters, ProjectsDAO, Project> {

	@Inject
	public ProjectSystemParametersAPI(ProjectsDAO dao) {
		super(dao);
	}
	
	@Override
	protected Set<String> authorizedUpdateFields() {
		return Stream.of("synchroProj", "qtreeQuota.proj", "qtreeQuota.rawdata", "qtreeQuota.scratch")
				.collect(Collectors.toSet());
	}

	@Override
	protected void setContextValidationLevel(ContextValidation ctxVal, Project project) {
		 ctxVal.putObject("level", Level.CODE.Project);
	     ctxVal.putObject("project", project);
	}

	@Override
	protected void saveInnerResource(ContextValidation ctxVal, Project project, SystemParameters sysParams, String currentUser) {
		TraceInformation ti = project.traceInformation;
        ti.modificationStamp(ctxVal, currentUser);
        dao.update(DBQuery.is("code", project.code),
                   DBUpdate.set("systemParameters", sysParams)
                           .set("traceInformation",       ti));
	}

	@Override
	protected void deleteInnerResource(Project objectInDB, String currentUser) {
		TraceInformation ti = objectInDB.traceInformation;
        ti.setTraceInformation(currentUser);
        dao.update(DBQuery.is("code", objectInDB.code), 
                   DBUpdate.unset("systemParameters")
                           .set("traceInformation", ti));
	}
	
	@Override
	public SystemParameters getInnerObject(Project project) {
		return project.systemParameters;
	}

	@Override
	public void setInnerObject(Project project, SystemParameters systemParameters) {
		project.systemParameters = systemParameters;
	}

	@Override
	public String innerObjectName() {
		return "SystemParameters";
	}
	
	public SystemParameters appendToArchiveHistory(Project project, ArchiveEntry entry, String currentUser) throws APIException {
		if(!isInnerObject(project)) throw new APIException("No " + innerObjectName() + " to update!");
		ContextValidation ctxVal = createUpdateContext(currentUser, project);
		SystemParameters systemParameters = getInnerObject(project);
		if(systemParameters.archiveHistory == null) systemParameters.archiveHistory = new ArrayList<>();
		systemParameters.archiveHistory.add(entry);
		setInnerObject(project, systemParameters);
		project.validate(ctxVal);
		if (ctxVal.hasErrors())  throw new APIValidationException(GenericAPI.INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
		saveInnerResource(ctxVal, project, systemParameters, currentUser);
		Project savedProject = dao.getObject(project.code);
		return getInnerObject(savedProject);
	}

}
