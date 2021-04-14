package sra.scripts;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import fr.cea.ig.lfw.utils.Iterables;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.sra.AbstractStudyAPI;
import fr.cea.ig.ngl.dao.api.sra.ProjectAPI;
import fr.cea.ig.ngl.dao.api.sra.StudyAPI;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.sra.submit.common.instance.AbstractStudy;
import models.sra.submit.common.instance.ExternalStudy;
import models.sra.submit.common.instance.Project;
import models.sra.submit.common.instance.Study;
import play.libs.Json;
import services.EbiAPI;
import services.XmlToSra;
import validation.ContextValidation;

import static ngl.refactoring.state.SRASubmissionStateNames.SUB_F;

/*
* Bilan des collections study, sample, experiment non associé à une soumission
* Exemple de lancement :
* http://localhost:9000/sra/scripts/run/sra.scripts.CreateAllProject
* @author sgas
*/
public class CreateAllProject  extends ScriptNoArgs {
	private final AbstractStudyAPI       		abstStudyAPI;
	private final StudyAPI               		studyAPI;
	private final ProjectAPI                    projectAPI;
	private final EbiAPI                        ebiAPI;
	private final NGLApplication                app;

	private static final play.Logger.ALogger logger = play.Logger.of(ImportAcEbi.class);


	@Inject
	public CreateAllProject (
			AbstractStudyAPI              abstStudyAPI,
			StudyAPI                      studyAPI,
			ProjectAPI                    projectAPI,
			EbiAPI                        ebiAPI,
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
		
		Iterator<AbstractStudy> iterator = abstStudyAPI.dao_all().iterator();
		List <Project> all_projects = new ArrayList<Project>();
		while(iterator.hasNext()) {
			AbstractStudy abstStudy = iterator.next();
			if(abstStudy instanceof ExternalStudy) {
				continue;
			}
			Study study = studyAPI.get(abstStudy.code);
			if (StringUtils.isBlank(study.externalId)) {
				continue;
			}
			if("PRJEB39225".equals(study.externalId)) {
				continue;
			}
			if("PRJEB4241".equals(study.externalId)) {
				continue;
			}
			String xmlProjects = ebiAPI.ebiProjectXml(study.externalId);
//			println(xmlProjects);
			XmlToSra repriseHistorique = new XmlToSra();
			
			Project project = Iterables.first(repriseHistorique.forProjects(xmlProjects, study.firstSubmissionDate)).orElse(null);
			project.traceInformation = new TraceInformation();
			project.traceInformation.createUser = study.traceInformation.createUser;
			project.traceInformation.creationDate = study.firstSubmissionDate;
			project.state = new State(SUB_F, study.traceInformation.createUser);
			project.externalId = study.accession;
			for(String projectCode : study.projectCodes) {
				if (! project.projectCodes.contains(projectCode)) {
					project.projectCodes.add(projectCode);
				}
			}
			if (! all_projects.contains(project)) {
				all_projects.add(project);
			}
		}
		boolean noError = true;
		for (Project project : all_projects) {
			ContextValidation contextValidation = ContextValidation.createCreationContext(project.traceInformation.createUser);
			project.validate(contextValidation);
			if(contextValidation.hasErrors()) {
				noError = false;
				contextValidation.displayErrors(logger, "debug");
				println(Json.prettyPrint(app.errorsAsJson(contextValidation.getErrors())));
			}
		}
		if (noError) {
			for (Project project : all_projects) {
				projectAPI.dao_saveObject(project);
			}
			println("sauvegarde dans la base de " + all_projects.size() + " project" );
		} else {
			println("Aucune sauvegarde de project dans la base");
		}
		println("Fin de traitement");
		
	} 

}
