package sra.scripts.off;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import fr.cea.ig.lfw.utils.Iterables;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.sra.AbstractStudyAPI;
import fr.cea.ig.ngl.dao.api.sra.ProjectAPI;
import fr.cea.ig.ngl.dao.api.sra.StudyAPI;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.sra.submit.sra.instance.AbstractStudy;
import models.sra.submit.sra.instance.ExternalStudy;
import models.sra.submit.sra.instance.Project;
import models.sra.submit.sra.instance.Study;
import play.libs.Json;
import services.SraEbiAPI;
import services.XmlToSra;
import validation.ContextValidation;

import static ngl.refactoring.state.SRASubmissionStateNames.SUB_F;

/*
* Exemple de lancement :
* http://localhost:9000/sra/scripts/run/sra.scripts.CreateAllProject
* @author sgas
*/
// fonctionne avec model de Project pour SEQUENCING et UMBRELLA => plus d'actualité
@Deprecated 
public class CreateAllProject  extends ScriptNoArgs {
	private final AbstractStudyAPI       		abstStudyAPI;
	private final StudyAPI               		studyAPI;
	private final ProjectAPI                    projectAPI;
	private final SraEbiAPI                        ebiAPI;
	private final NGLApplication                app;

	private static final play.Logger.ALogger logger = play.Logger.of(CreateAllProject.class);


	@Inject
	public CreateAllProject (
			AbstractStudyAPI              abstStudyAPI,
			StudyAPI                      studyAPI,
			ProjectAPI                    projectAPI,
			SraEbiAPI                        ebiAPI,
			NGLApplication                app) {
		this.abstStudyAPI = abstStudyAPI;
		this.studyAPI     = studyAPI;
		this.projectAPI   = projectAPI;
		this.ebiAPI       = ebiAPI;
		this.app          = app;
	}
	
	
	public Project EbiFetchProject(String accession, Date submissionDate) {		
		try {
			String xmlProjects = ebiAPI.ebiXml(accession, "projects");
			XmlToSra repriseHistorique = new XmlToSra();
			return Iterables.first(repriseHistorique.forProjects(xmlProjects, submissionDate)).orElse(null);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void execute() throws Exception {
		List <Project> all_projects = new ArrayList<Project>();
		
//		List<AbstractStudy>list_abstStudy = new ArrayList<AbstractStudy>();
//		list_abstStudy.add(abstStudyAPI.dao_getObject("STUDY_CCU_573H3TTCA"));
//		list_abstStudy.add(abstStudyAPI.dao_getObject("study_AQN_1"));
//		for(AbstractStudy abstStudy: list_abstStudy) {
		
		Iterator<AbstractStudy> iterator = abstStudyAPI.dao_all().iterator();
		while(iterator.hasNext()) {

			AbstractStudy abstStudy = iterator.next();
			if(abstStudy instanceof ExternalStudy) {
				continue;
			}
			Study study = studyAPI.get(abstStudy.code);
			//println("study.code=" + study.code);
			if (StringUtils.isBlank(study.externalId)) {
				continue;
			}
//			if("PRJEB39225".equals(study.externalId)) {
//				continue;
//			}
//			if("PRJEB4241".equals(study.externalId)) {
//				continue;
//			}
			String xmlProjects = ebiAPI.ebiProjectXml(study.externalId);
//			println(xmlProjects);
			XmlToSra repriseHistorique = new XmlToSra();
			
			Project project = Iterables.first(repriseHistorique.forProjects(xmlProjects, study.firstSubmissionDate)).orElse(null);
			project.traceInformation = new TraceInformation();
			project.traceInformation.createUser = study.traceInformation.createUser;
			project.traceInformation.creationDate = study.firstSubmissionDate;
			project.state = new State(SUB_F, study.traceInformation.createUser);
			// ne marche plus dans model actualisé :
			//project.externalId = study.accession;
//			for(String projectCode : study.projectCodes) {
//				if(project.projectCodes == null) {
//					project.projectCodes = new ArrayList<String>();
//				}
//				if (! project.projectCodes.contains(projectCode)) {
//					project.projectCodes.add(projectCode);
//				}
//			}
			if (! all_projects.contains(project)) {
				all_projects.add(project);
			}
			//break; // Pour tester sur une seule entree
		}
		boolean noError = true;
		ContextValidation contextValidation = null;
		for (Project project : all_projects) {
			Project projectDb = projectAPI.get(project.code);
			
			if(projectDb != null) {
				project._id = projectDb._id;
				contextValidation = ContextValidation.createUpdateContext(project.traceInformation.createUser);
			} else {
				contextValidation = ContextValidation.createCreationContext(project.traceInformation.createUser);
			}
			logger.debug("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX Appel de validate depuis CreateAllProject\n");
			project.validate(contextValidation);
			if(contextValidation.hasErrors()) {
				noError = false;
				contextValidation.displayErrors(logger, "debug");
				println(Json.prettyPrint(app.errorsAsJson(contextValidation.getErrors())));
			}
		}
		if (noError) {
			Calendar calendar = Calendar.getInstance();
			Date date  = calendar.getTime();
			String user = "ngsrg";
			for (Project project : all_projects) {
				Project projectDb = projectAPI.get(project.code);
				if (projectDb != null) {
					projectAPI.dao_update(DBQuery.is("code", project.code),
							DBUpdate.set("traceInformation.modifyUser", user)
									.set("traceInformation.modifyDate", date)
									.set("description", project.description)
									.set("title", project.title));
				} else {
					projectAPI.dao_saveObject(project);
				}
			}
			println("sauvegarde virtuelle dans la base de " + all_projects.size() + " project" );
		} else {
			println("Aucune sauvegarde de project dans la base");
		}
		println("Fin de traitement");
		
	} 

}
