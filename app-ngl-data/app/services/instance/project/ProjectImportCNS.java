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
import models.laboratory.project.instance.UmbrellaProject;
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
		createUmbrellaProject(contextError);
		createProject(contextError);
		deleteProject(contextError);
	}

	private void deleteProject(ContextValidation contextValidation) throws SQLException, DAOException{
		//// pl_ProjetToNGL=> Liste l'ensemble des ss-projets de sequencage
		// Les projets Lims qui ne sont pas associés à des Samples ne sont pas conservés dans NGL
		List<String> availableProjectCodes = limsServices.findProjectToCreate(contextValidation)
				.stream()
				.map(p -> p.code)
				.collect(Collectors.toList());

		MongoDBDAO.find(InstanceConstants.PROJECT_COLL_NAME, Project.class).cursor.forEach(p -> {
			if(!availableProjectCodes.contains(p.code)){
				int nbSample = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.in("projectCodes", p.code)).count();
				if (nbSample == 0) {
					MongoDBDAO.deleteByCode(InstanceConstants.PROJECT_COLL_NAME, Project.class, p.code);
				} 
			}
		});
	}

	public void createUmbrellaProject(ContextValidation contextValidation) {
		List<UmbrellaProject> umbrellaProjects = limsServices.findUmbrellaProjectToCreate(contextValidation) ;

		for (UmbrellaProject limsUmbrellaProject : umbrellaProjects) {
			UmbrellaProject nglUmbrellaProject = null;
			List<UmbrellaProject> upList = MongoDBDAO.find(InstanceConstants.UMBRELLA_PROJECT_COLL_NAME, UmbrellaProject.class, DBQuery.is("properties.limsCode.value", limsUmbrellaProject.properties.get("limsCode").value)).toList();
			
			if (!upList.isEmpty()) {
				nglUmbrellaProject = upList.get(0);
			}

			if (nglUmbrellaProject != null) {
				contextValidation.setUpdateMode();

				boolean isUpdated = false;

				Builder update =  DBUpdate.set("name",limsUmbrellaProject.name);

				if (nglUmbrellaProject.name != limsUmbrellaProject.name) {
					update.set("name", limsUmbrellaProject.name);
					isUpdated = true;
				}

				if (nglUmbrellaProject.description != limsUmbrellaProject.description) {
					update.set("description", limsUmbrellaProject.description);
					isUpdated = true;
				}

				if (nglUmbrellaProject.properties.get("applicant").getValue() != limsUmbrellaProject.properties.get("applicant").getValue()) {
					update.set("properties.applicant.value", limsUmbrellaProject.properties.get("applicant").getValue());
					isUpdated = true;
				}

				if (isUpdated) {
					Logger.info("createUmbrellaProject:: Mise a jour du umbrella projet dans NGL: "+limsUmbrellaProject.name+"::"+limsUmbrellaProject.code);			
				}

				MongoDBDAO.update(InstanceConstants.UMBRELLA_PROJECT_COLL_NAME, UmbrellaProject.class, 
						DBQuery.is("code", limsUmbrellaProject.code), update);
			} else {
				contextValidation.setCreationMode();

				Logger.info("createUmbrellaProject:: Creation nouveau umbrella projet dans NGL: "+limsUmbrellaProject.name+"::"+limsUmbrellaProject.code);			
				InstanceHelpers.save(InstanceConstants.UMBRELLA_PROJECT_COLL_NAME,limsUmbrellaProject,contextValidation);
			}
		}
	}	

	public void createProject(ContextValidation contextValidation) throws SQLException, DAOException, APIValidationException, APIException{
		// pl_ProjetToNGL=> Liste des ss-projets de sequencage 
		List<Project> projects = limsServices.findProjectToCreate(contextValidation) ;

		for (Project limsProject : projects) {
			Project nglProject = MongoDBDAO.findByCode(InstanceConstants.PROJECT_COLL_NAME, Project.class, limsProject.code);

			if (nglProject != null) {
				contextValidation.setUpdateMode();

				if (! nglProject.name.equals( limsProject.name)) {
					Logger.info("Project renamed in NGL: \""+nglProject.code+"::"+nglProject.name+"\" (name in NGL) => \""+limsProject.name+"\" (name in Lims)");
					nglProject.traceInformation.modificationStamp(Constants.NGL_DATA_USER);
				}

				Builder update =  DBUpdate.set("name",limsProject.name);
				boolean isUpdated = false;
				boolean withoutDate = false; // Permet de savoir si on met la modifyDate à maintenant ou si on garde celle de l'ancien LIMS.

				//1 si lastSampleCode est renseigné uniquement dans le Lims
				//2 nbCharactersInSampleCode dans le Lims > NGL
				//3 nbCharactersInSampleCode Lims identique NGL mais lastSampleCode différent
				// => mise a jour NGL avec infos du Lims !!!!!!! + log erreur
				if ((nglProject.lastSampleCode == null &&  limsProject.lastSampleCode != null)
						|| (nglProject.lastSampleCode != null &&  limsProject.lastSampleCode != null
						&& limsProject.nbCharactersInSampleCode > nglProject.nbCharactersInSampleCode)
						|| (nglProject.lastSampleCode != null &&  limsProject.lastSampleCode != null
						&& limsProject.nbCharactersInSampleCode == nglProject.nbCharactersInSampleCode
						&& limsProject.lastSampleCode.compareTo(nglProject.lastSampleCode) > 0)){
					update.set("lastSampleCode",limsProject.lastSampleCode);
					update.set("nbCharactersInSampleCode",limsProject.nbCharactersInSampleCode);

					nglProject.traceInformation.modificationStamp(Constants.NGL_DATA_USER);
				}

				//si le projet est à l'état terminé dans le Lims alors on met à jour l'état
				if (limsProject.state.code.contentEquals("F")) {
					update.set("state",limsProject.state);
					isUpdated = true;
				}	

				if ((	nglProject.properties == null || nglProject.properties.get("infoProjectManager") == null) || 
						nglProject.properties.get("infoProjectManager").value != limsProject.properties.get("infoProjectManager").value) {
					update.set("properties.infoProjectManager", limsProject.properties.get("infoProjectManager"));
					isUpdated = true;
				}

				if ((	nglProject.properties == null || nglProject.properties.get("bioProjectManager") == null) || 
						nglProject.properties.get("bioProjectManager").value != limsProject.properties.get("bioProjectManager").value) {
					update.set("properties.bioProjectManager", limsProject.properties.get("bioProjectManager"));
					isUpdated = true;
				}

				if (limsProject.traceInformation.modifyDate != null && nglProject.traceInformation.modifyDate != null) {
					if (limsProject.traceInformation.modifyDate.after(nglProject.traceInformation.modifyDate)) {
						update.set("traceInformation.modifyDate", limsProject.traceInformation.modifyDate);
						withoutDate = true;
					}
				}

				if (isUpdated) {
					Logger.info("Mise a jour du projet :: " + nglProject.code);

					if (!withoutDate) {
						nglProject.traceInformation.modificationStamp(Constants.NGL_DATA_USER);
					}

					MongoDBDAO.update(InstanceConstants.PROJECT_COLL_NAME, Project.class,  DBQuery.is("code", limsProject.code),update);
				}
			} else {
				//Projet absent de NGL

				contextValidation.setCreationMode();

				//Si l'état du projet Lims est a finish et que le projet n'existe pas dans NGL alors on ne le créé pas!!!			
				if (! limsProject.state.code.contentEquals("F")) {
					Logger.info("createProject:: Creation nouveau projet dans NGL: "+limsProject.name+"::"+limsProject.code);			
					InstanceHelpers.save(InstanceConstants.PROJECT_COLL_NAME,limsProject,contextValidation);
				}else {
					Logger.info("createProject:: Projet absent dans NGL et a l'etat F dans Lims::"+limsProject.name+"::"+limsProject.code+" => projet non importe !");						
				}
			}
		}
	}	
}
