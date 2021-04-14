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
	}

	private void deleteProject(ContextValidation contextValidation) throws SQLException, DAOException{
		//// pl_ProjetToNGL=> Liste des ss-projets de sequencage pas termines et dont la date reelle
		//de fin n'est pas anterieure a getdate() 
		List<String> availableProjectCodes = limsServices.findProjectToCreate(contextValidation)
				.stream()
				.map(p -> p.code)
				.collect(Collectors.toList());
		
		MongoDBDAO.find(InstanceConstants.PROJECT_COLL_NAME, Project.class).cursor.forEach(p -> {
			if(!availableProjectCodes.contains(p.code)){
				int nbSample = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.in("projectCodes", p.code)).count();
				if (nbSample == 0) {
					logger.info("delete project : "+p.code);
					MongoDBDAO.deleteByCode(InstanceConstants.PROJECT_COLL_NAME, Project.class, p.code);
				} else {
					logger.error("Try to delete project : "+p.code+" but sample exists :"+nbSample);
				}
			}
		});
	}

	public void createProject(ContextValidation contextValidation) throws SQLException, DAOException, APIValidationException, APIException{
		// pl_ProjetToNGL=> Liste des ss-projets de sequencage pas termines et dont la date reelle
		//de fin n'est pas anterieure a getdate() 
	List<Project> projects = limsServices.findProjectToCreate(contextValidation) ;
		
	for (Project limsProject : projects) {
		Project nglProject = MongoDBDAO.findByCode(InstanceConstants.PROJECT_COLL_NAME, Project.class, limsProject.code);
		if (nglProject != null) {
			if (nglProject.name != limsProject.name)
				Logger.info("Project renamed in NGL: \""+nglProject.name+"\" (name in NGL) => \""+limsProject.name+"\" (name in Lims)");
			Builder update =  DBUpdate.set("name",limsProject.name);

			//1 si lastSampleCode est renseigné uniquement dans le Lims
			//2 nbCharactersInSampleCode dans le Lims > NGL
			//3 nbCharactersInSampleCode Lims identique NGL mais lastSampleCode différent
			// => mise a jour NGL avec infos du Lims !!!!!!! + log erreur
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
			// EJACOBY AD Call API Project save
			/*Project project = projectsAPI.create(limsProject, "ngl-data");
				State state = new State(); 
				state.code="IP";
				state.user = "ngl-data";
				projectsWorkflows.setState(contextValidation, project, state);*/

			InstanceHelpers.save(InstanceConstants.PROJECT_COLL_NAME,limsProject,contextValidation);
		}
	}

		// InstanceHelpers.save(InstanceConstants.PROJECT_COLL_NAME,projects,contextValidation);
	}	
}
