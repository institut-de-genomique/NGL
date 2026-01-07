package fr.cea.ig.ngl.dao.projects;

import java.util.Collections;
import java.util.Set;

import javax.inject.Inject;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import fr.cea.ig.ngl.dao.api.InnerDocumentGenericAPI;
import models.laboratory.common.description.Level;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.project.instance.BioinformaticParameters;
import models.laboratory.project.instance.Project;
import validation.ContextValidation;

public class ProjectBioinformaticParametersAPI extends InnerDocumentGenericAPI<BioinformaticParameters, ProjectsDAO, Project> {

	@Inject
	public ProjectBioinformaticParametersAPI(ProjectsDAO dao) {
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
	protected void saveInnerResource(ContextValidation ctxVal, Project project, BioinformaticParameters bioInfoParams, String currentUser) {
		TraceInformation ti = project.traceInformation;
        ti.modificationStamp(ctxVal, currentUser);
        dao.update(DBQuery.is("code", project.code),
                   DBUpdate.set("bioinformaticParameters", bioInfoParams)
                           .set("traceInformation",       ti));
	}

	@Override
	protected void deleteInnerResource(Project objectInDB, String currentUser) {
		TraceInformation ti = objectInDB.traceInformation;
        ti.setTraceInformation(currentUser);
        dao.update(DBQuery.is("code", objectInDB.code), 
                   DBUpdate.unset("bioinformaticParameters")
                           .set("traceInformation", ti));
	}
	
	@Override
	public BioinformaticParameters getInnerObject(Project project) {
		return project.bioinformaticParameters;
	}

	@Override
	public void setInnerObject(Project project, BioinformaticParameters bioinformaticParameters) {
		project.bioinformaticParameters = bioinformaticParameters;
	}

	@Override
	public String innerObjectName() {
		return "BioinformaticParameters";
	}

}
