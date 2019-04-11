package services.instance.project;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;
import org.mongojack.DBUpdate.Builder;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.projects.ProjectsAPI;
import models.Constants;
import models.laboratory.project.instance.Project;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import models.utils.InstanceHelpers;
import models.utils.dao.DAOException;
import play.Logger;
import services.instance.AbstractImportDataCNS;
import validation.ContextValidation;
import workflows.project.ProjectWorkflows;

public class ProjectImportCNS extends AbstractImportDataCNS {

//	@Inject
//	public ProjectImportCNS(FiniteDuration durationFromStart,
//			                FiniteDuration durationFromNextIteration, 
//			                NGLContext ctx) {
//		super("Project CNS",durationFromStart, durationFromNextIteration, ctx);
//	}

//	@Inject
//	public ProjectImportCNS(NGLContext ctx) {
//		super("Project CNS", ctx);
//	}

	ProjectsAPI projectsAPI;
	ProjectWorkflows projectsWorkflows;
	
	@Inject
	public ProjectImportCNS(NGLApplication app) {
		super("Project CNS", app);
		this.projectsAPI=app.apis().project();
		this.projectsWorkflows=app.apis().projectWorkflow();
	}

	@Override
	public void runImport(ContextValidation contextError) throws SQLException, DAOException, APIValidationException, APIException {
		
		createProject(contextError);
		deleteProject(contextError);
		
		//lastNGLSampleCode(contextError);
	}

	private void deleteProject(ContextValidation contextValidation) throws SQLException, DAOException{
		List<String> availableProjectCodes = limsServices.findProjectToCreate(contextValidation)
				.stream()
				.map(p -> p.code)
				.collect(Collectors.toList());
		// MongoDBDAO.find(InstanceConstants.PROJECT_COLL_NAME, Project.class).getCursor().forEach(p -> {
		MongoDBDAO.find(InstanceConstants.PROJECT_COLL_NAME, Project.class).cursor.forEach(p -> {
			if(!availableProjectCodes.contains(p.code)){
				logger.info("delete project : "+p.code);
				int nbSample = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.in("projectCodes", p.code)).count();
				if (nbSample == 0) {
					MongoDBDAO.deleteByCode(InstanceConstants.PROJECT_COLL_NAME, Project.class, p.code);
				} else {
					logger.error("Try to delete project : "+p.code+" but sample exists :"+nbSample);
				}
			}
		});
	}

	public /*static*/ void createProject(ContextValidation contextValidation) throws SQLException, DAOException, APIValidationException, APIException{

		List<Project> projects = limsServices.findProjectToCreate(contextValidation) ;

		for (Project limsProject : projects) {

			Project nglProject = MongoDBDAO.findByCode(InstanceConstants.PROJECT_COLL_NAME, Project.class, limsProject.code);
			if (nglProject != null) {
				Builder update =  DBUpdate.set("name",limsProject.name).set("archive", limsProject.archive);

				if((nglProject.lastSampleCode == null &&  limsProject.lastSampleCode != null)
						|| (nglProject.lastSampleCode != null &&  limsProject.lastSampleCode != null
						&& limsProject.nbCharactersInSampleCode > nglProject.nbCharactersInSampleCode)
						|| (nglProject.lastSampleCode != null &&  limsProject.lastSampleCode != null
						&& limsProject.nbCharactersInSampleCode == nglProject.nbCharactersInSampleCode
						&& limsProject.lastSampleCode.compareTo(nglProject.lastSampleCode) > 0)){
					update.set("lastSampleCode",limsProject.lastSampleCode);
					update.set("nbCharactersInSampleCode",limsProject.nbCharactersInSampleCode);
					Logger.error(nglProject.code+": "+limsProject.lastSampleCode +" != "+ nglProject.lastSampleCode);
				}

				nglProject.traceInformation.setTraceInformation(Constants.NGL_DATA_USER);
				MongoDBDAO.update(InstanceConstants.PROJECT_COLL_NAME, Project.class, 
						DBQuery.is("code", limsProject.code),update);

			} else {
				//TODO EJACOBY AD Call API Project save
				/*Project project = projectsAPI.create(limsProject, "ngl-data");
				State state = new State(); 
				state.code="IP";
				state.user = "ngl-data";
				projectsWorkflows.setState(contextValidation, project, state);*/
				InstanceHelpers.save(InstanceConstants.PROJECT_COLL_NAME,limsProject,contextValidation);
			}
		}

		//InstanceHelpers.save(InstanceConstants.PROJECT_COLL_NAME,projects,contextValidation);

	}

//	private void lastNGLSampleCode(ContextValidation contextError) {
//		// MongoDBDAO.find(InstanceConstants.PROJECT_COLL_NAME, Project.class).getCursor().forEach(p -> {
//		MongoDBDAO.find(InstanceConstants.PROJECT_COLL_NAME, Project.class).cursor.forEach(p -> {
//				String lastNGLSampleCode = getLastSample(p.code);
//				if(null != lastNGLSampleCode){
//					p.lastSampleCode = lastNGLSampleCode;
//					p.nbCharactersInSampleCode = lastNGLSampleCode.replace(p.code+"_","").length();
//					MongoDBDAO.update(InstanceConstants.PROJECT_COLL_NAME, p);
//				}else{
//					p.lastSampleCode = null;
//					p.nbCharactersInSampleCode = null;
//					MongoDBDAO.update(InstanceConstants.PROJECT_COLL_NAME, p);
//				}
//			
//		});;
//		
//	}
	
//	private static String getLastSample(String code) {
//		String[] last = new String[]{null};
//		// MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.in("projectCodes", code)).sort("code").getCursor().forEach(s -> {
//		MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.in("projectCodes", code)).sort("code").cursor.forEach(s -> {	
//			if(last[0] == null || s.code.length() > last[0].length()){
//					last[0] = s.code;
//				}else if(s.code.length() == last[0].length() && s.code.compareTo(last[0]) > 0){
//					last[0] = s.code;
//				}
//				
//			});
//		return last[0];
//	}
	
}
