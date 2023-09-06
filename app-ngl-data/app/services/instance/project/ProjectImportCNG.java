package services.instance.project;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
//import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import com.typesafe.config.ConfigFactory;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.projects.ProjectsAPI;
import models.laboratory.common.instance.State;
import mail.MailServiceException;
import mail.MailServices;
import models.laboratory.project.instance.Project;
import models.utils.InstanceConstants;
import models.utils.InstanceHelpers;
import models.utils.dao.DAOException;
import services.instance.AbstractImportDataCNG;
import validation.ContextValidation;
import workflows.project.ProjectWorkflows;

/**
 * Import Projects from CNG's LIMS to NGL 
 * FDS remplacement de l'appel a Logger par logger
 * 
 * @author dnoisett
 */

public class ProjectImportCNG extends AbstractImportDataCNG {

	ProjectsAPI projectsAPI;
	ProjectWorkflows projectsWorkflows;

	@Inject
	public ProjectImportCNG(NGLApplication app) {
		super("ProjectImportCNG", app);
		this.projectsAPI=app.apis().project();
		this.projectsWorkflows=app.apis().projectWorkflow();
	}

	@Override
	public void runImport(ContextValidation contextError) throws SQLException, DAOException, APIValidationException, APIException {
		// FDS 17/07/2015 création de de 2 méthodes bien séparées
		loadProjects(contextError);	 	
		updateProjects(contextError);
	}
	
	public void loadProjects(ContextValidation contextError) throws SQLException, DAOException, APIValidationException, APIException {
		logger.info("start loading projects");
		
		//-1- chargement depuis la base source Postgresql
		List<Project> projects = limsServices.findProjectToCreate(contextError, this.app.nglConfig().getString("ad.default.group.access"));
		
		logger.info("found "+projects.size() + " items");

		//-2- sauvegarde dans la base cible MongoDb
		//AD Call API Project
		for(Project project : projects){
			project = projectsAPI.create(project, "ngl-data");
			
			State state = new State(); 
			state.code="IP";
			state.user = "ngl-data";
			projectsWorkflows.setState(contextError, project, state); 
		}

		// NGL-3019 - Alerter par mail création nouveau projet.
		if (!projects.isEmpty()) {
			try {
				buildAndSendMail(projects);
			} catch (MailServiceException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}

		//-3- timestamp-er dans la base source Postgresql ce qui a été traité
		limsServices.updateLimsProjects(projects, contextError, "creation");
		
		logger.info("end loading projects");
	}

	private void buildAndSendMail(List<Project> projects) throws MailServiceException, UnsupportedEncodingException {
		MailServices mailService = new MailServices();
		
		String expediteur = ConfigFactory.load().getString("project.email.from");
		String destinataire = ConfigFactory.load().getString("project.email.to");

		String subjectSimple = "Nouveau projet créé dans NGL";
		String subjectMultiple = "Nouveaux projets créés dans NGL";
		String appName = "[NGL-PROJECTS] ";

		String subject = (projects.size() > 1 ? subjectMultiple : subjectSimple);

		String message = "Bonjour,<br /><br />" + subject + " avec le code :<br />";
		
		for (int i = 0; i < projects.size(); i++) {
			message += projects.get(i).code + "<br />";
		}
		
		message += "<br /><br />";
		message += "Merci et à bientôt sur NGL-PROJECTS !";
		
		Set<String> destinataires = new HashSet<>();
		destinataires.addAll(Arrays.asList(destinataire.split(",")));

		mailService.sendMail(expediteur, destinataires, appName + subject, new String(message.getBytes(), "iso-8859-1"));
		logger.info("mail sent for new projects");
	}
	
	public void updateProjects(ContextValidation contextError) throws SQLException, DAOException {
		logger.debug("start update projects");	

		// -1- chargement depuis la base source Postgresql
		List<Project> projects = limsServices.findProjectToModify(contextError);
		logger.info("found "+ projects.size() + " items");
		
		// -2a- delete old projects
		for (Project project : projects) {
			if (MongoDBDAO.checkObjectExistByCode(InstanceConstants.PROJECT_COLL_NAME,Project.class, project.code)) {
				MongoDBDAO.deleteByCode(InstanceConstants.PROJECT_COLL_NAME, Project.class, project.code);
			}
		}
		
		// -2b- sauvegarde dans la base cible MongoDb
		List<Project> projs = InstanceHelpers.save(InstanceConstants.PROJECT_COLL_NAME,projects,contextError, true);
		
		// -3- timestamp-er dans la base source Postgresql ce qui a été traité
		limsServices.updateLimsProjects(projs, contextError, "update");
		
		logger.debug("end update projects");	
	}
	
}
