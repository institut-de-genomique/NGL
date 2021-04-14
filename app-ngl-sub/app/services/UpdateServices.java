package services;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;

import fr.cea.ig.lfw.utils.Iterables;
//import fr.cea.ig.ngl.NGLConfig;
import fr.cea.ig.ngl.dao.sra.ExperimentDAO;
import fr.cea.ig.ngl.dao.sra.ProjectDAO;
import fr.cea.ig.ngl.dao.sra.SampleDAO;
import fr.cea.ig.ngl.dao.sra.StudyDAO;
import fr.cea.ig.ngl.dao.sra.SubmissionDAO;
import models.laboratory.common.instance.State;
import models.sra.submit.common.instance.Project;
import models.sra.submit.common.instance.Sample;
import models.sra.submit.common.instance.Study;
import models.sra.submit.common.instance.Submission;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.util.SraCodeHelper;
import models.sra.submit.util.SraException;
import models.sra.submit.util.VariableSRA;
import ngl.refactoring.state.SRASubmissionStateNames;
import validation.ContextValidation;

public class UpdateServices  {

	private static final play.Logger.ALogger logger = play.Logger.of(UpdateServices.class);
	private final ProjectDAO      	  projectDAO;
	private final StudyDAO      	  studyDAO;
	private final SampleDAO           sampleDAO;
	private final ExperimentDAO       experimentDAO;
	private final SubmissionDAO       submissionDAO;
	private final SraCodeHelper       sraCodeHelper;
	
	@Inject
	public UpdateServices(SubmissionDAO       submissionDAO,
						  ProjectDAO          projectDAO,
						  StudyDAO            studyDAO,
						  SampleDAO           sampleDAO,
						  ExperimentDAO       experimentDAO,
						  SraCodeHelper       sraCodeHelper) {
		this.submissionDAO       = submissionDAO;
		this.projectDAO          = projectDAO;
		this.studyDAO            = studyDAO;
		this.sampleDAO           = sampleDAO;
		this.experimentDAO       = experimentDAO;
		this.sraCodeHelper       = sraCodeHelper;
	}
	
	

	
	
	private  List <String> valideArgsCreateSubmissionForUpdate(ContextValidation ctxVal,
															   Project project,
															   Study study, 
															   List<Sample> samples,
															   List<Experiment> experiments) throws SraException {
		
		List <String> projectCodes = new ArrayList<>();
		boolean dataForEbi = false;
		ctxVal.setUpdateMode();
		String user = ctxVal.getUser();
		// sgas user
		//user = "william";
		if (project != null) {
			dataForEbi = true;	
			SraException.assertObjectStateCode(project, SRASubmissionStateNames.SUB_F);
			// Mettre dans l'etat qui sera sauvé si tout va bien:
			project.updateStateAndTrace(new State(SRASubmissionStateNames.SUBU_N, user));
			project.validate(ctxVal); 
			SraException.assertNoError(ctxVal);
			
			if (project.traceInformation.createUser == null) {
				String message = "Le project " + project.code
						+ " n'est pas renseigné dans la base pour le champs createUser";	
				throw new SraException("createSubmissionForUpdate", message);
			}
			if (user == null) {
				String message = "l'utilisateur n'est pas authentifié";
				throw new SraException("createSubmissionForUpdate", message);
			}
			// sgas user
//			if (! project.traceInformation.createUser.equals(user)) {		
//				String message = "l'utilisateur " + user + 
//						" n'est pas autorisé à mettre à jour le project " + project.code 
//						+ " crée par " + project.traceInformation.createUser;
//				throw new SraException("createSubmissionForUpdate", message);
//			}

			if (submissionDAO.checkObjectExist(DBQuery.and(DBQuery.is("projectCode", project.code),
					DBQuery.notIn("state.code", SRASubmissionStateNames.SUB_F)))) {
	
				Iterable<Submission> it = submissionDAO.find(DBQuery.and(DBQuery.is("projectCode", project.code),
						DBQuery.notIn("state.code", SRASubmissionStateNames.SUB_F)));
				String mess = Iterables.map(it, s->s.code)
						.surround("(", ", ", ")")
						.asString();
				String message = "Il existe deja des soumissions en cours " + mess 
						+ " qui impliquent le project " + project.code;
				logger.debug("dans OOOOOOOOOOOOOO, message = " + message);
				throw new SraException("createSubmissionForUpdate", message);
			}
			
			for (String projCode : project.projectCodes) {
				if(!projectCodes.contains(projCode)) {
					projectCodes.add(projCode);
				}
			}
		} 
		
		if (study != null) {
			dataForEbi = true;	
			SraException.assertObjectStateCode(study, SRASubmissionStateNames.SUB_F);
			//study.setTraceUpdateStamp(user);
			// Mettre dans l'etat qui sera sauvé si tout va bien:
			study.updateStateAndTrace(new State(SRASubmissionStateNames.SUBU_N, user));
			study.validate(ctxVal); 
			SraException.assertNoError(ctxVal);
			
			if (study.traceInformation.createUser == null) {
				String message = "Le study " + study.code
						+ " n'est pas renseigné dans la base pour le champs createUser";	
				throw new SraException("createSubmissionForUpdate", message);
			}
			if (user == null) {
				String message = "l'utilisateur n'est pas authentifié";
				throw new SraException("createSubmissionForUpdate", message);
			}
			// sgas user
//			if (! study.traceInformation.createUser.equals(user)) {		
//				String message = "l'utilisateur " + user + 
//						" n'est pas autorisé à mettre à jour le study " + study.code 
//						+ " crée par " + study.traceInformation.createUser;
//				throw new SraException("createSubmissionForUpdate", message);
//			}

			// Modification pour ticket SUPSQ-4248 : on ne veut pas seulement qu'il n'y ait aucun studyCode dans 
			// une soumission de type release mais on veut qu'il n'y ait aucun studyCode 
			// engagé dans une soumission avec un status autre que SUB-F :
//			if (submissionDAO.checkObjectExist(DBQuery.and(DBQuery.is("studyCode", study.code),
//					DBQuery.is("type", Submission.Type.UPDATE)))) {
//		
//				Iterable<Submission> it = submissionDAO.find(DBQuery.and(DBQuery.is("studyCode", study.code),
//						DBQuery.is("type", Submission.Type.UPDATE)));
//				String mess = Iterables.map(it, s->s.code)
//						.surround("(", ", ", ")")
//						.asString();
//				String message = "Il existe deja des soumissions " + mess 
//						+ " pour la mise à jour du study " + study.code;
//				logger.debug("dans OOOOOOOOOOOOOO, message = " + message);
//				throw new SraException("createSubmissionForUpdate", message);
//			}
			if (submissionDAO.checkObjectExist(DBQuery.and(DBQuery.is("studyCode", study.code),
			DBQuery.notIn("state.code", SRASubmissionStateNames.SUB_F)))) {

		Iterable<Submission> it = submissionDAO.find(DBQuery.and(DBQuery.is("studyCode", study.code),
				DBQuery.notIn("state.code", SRASubmissionStateNames.SUB_F)));
		String mess = Iterables.map(it, s->s.code)
				.surround("(", ", ", ")")
				.asString();
		String message = "Il existe deja des soumissions en cours " + mess 
				+ " impliquant le study " + study.code;
		logger.debug("dans OOOOOOOOOOOOOO, message = " + message);
		throw new SraException("createSubmissionForUpdate", message);
	}	
			for (String projCode :((Study) study).projectCodes) {
				if(!projectCodes.contains(projCode)) {
					projectCodes.add(projCode);
				}
			}
		} 
		
		for (Sample sample : samples) {
			dataForEbi = true;

			SraException.assertObjectStateCode(sample, SRASubmissionStateNames.SUB_F);
			// Mettre dans l'etat qui sera sauvé si tout va bien:
			sample.updateStateAndTrace(new State(SRASubmissionStateNames.SUBU_N, user));
			sample.validate(ctxVal);
			SraException.assertNoError(ctxVal);// ok et bien recuperé 
			// sgas user
//			if (! sample.traceInformation.createUser.equals(user)) {		
//				String message = "l'utilisateur " + user + 
//						" n'est pas autorisé à mettre à jour le sample " + sample.code 
//						+ " crée par " + sample.traceInformation.createUser;
//				throw new SraException("createSubmissionForUpdate", message);
//			}
			if (submissionDAO.checkObjectExist(DBQuery.and(DBQuery.in("sampleCodes", sample.code),
											   DBQuery.notIn("state.code", SRASubmissionStateNames.SUB_F)))) {
				
				Iterable<Submission> it = submissionDAO.find(DBQuery.and(DBQuery.in("sampleCodes", sample.code),
						DBQuery.notIn("state.code", SRASubmissionStateNames.SUB_F)));
				String mess = Iterables.map(it, s->s.code)
									   .surround("(", ", ", ")")
									   .asString();
				String message = "Il existe deja des soumissions en cours " + mess 
						+ " impliquant le sample " + sample.code;
				throw new SraException("createSubmissionForUpdate", message);
			}
			if(!projectCodes.contains(((Sample)sample).projectCode)) {
				projectCodes.add(((Sample)sample).projectCode);
			}
		}

		for (Experiment experiment :experiments) {
			dataForEbi = true;
			SraException.assertObjectStateCode(experiment, SRASubmissionStateNames.SUB_F);
			// Mettre dans l'etat qui sera sauvé si tout va bien:
			experiment.updateStateAndTrace(new State(SRASubmissionStateNames.SUBU_N, user));
			experiment.validate(ctxVal);
			SraException.assertNoError(ctxVal);// ok et bien recuperé 
			// sgas user
//			if (! experiment.traceInformation.createUser.equals(user)) {		
//				String message = "l'utilisateur " + user + 
//						" n'est pas autorisé à mettre à jour l'experiment " + experiment.code 
//						+ " crée par " + experiment.traceInformation.createUser;
//				throw new SraException("createSubmissionForUpdate", message);
//			}
			if (submissionDAO.checkObjectExist(DBQuery.and(DBQuery.in("experimentCodes", experiment.code),
					DBQuery.notIn("state.code", SRASubmissionStateNames.SUB_F)))) {
				Iterable<Submission> it = submissionDAO.find(DBQuery.and(DBQuery.in("experimentCodes", experiment.code),
						DBQuery.notIn("state.code", SRASubmissionStateNames.SUB_F)));
				String mess = Iterables.map(it, s->s.code)
									   .surround("(", ", ", ")")
									   .asString();
				String message = "Il existe deja des soumissions en cours " + mess 
						+ " impliquant l'experiment " + experiment.code;
				throw new SraException("createSubmissionForUpdate", message);
			}
			if(!projectCodes.contains(experiment.projectCode)) {
				projectCodes.add(experiment.projectCode);
			}			
		}
		if(!dataForEbi) {
			throw new SraException("createSubmissionForUpdate", "Aucun study, sample ou experiment à mettre à jour??");	
		}
		return projectCodes;
	}
	


/**
 * Construit la soumission correspondant au study, samples ou experiments presents dans la base
 * et qui doivent etre soumis à l'EBI pour une mise à jour. 
 * Renvoie la soumission dans l'etat {@link ngl.refactoring.state.States#SUBU_N}.
 * Ajoute une erreur dans le contexte de validation si une erreur survient ou declenche une erreur
 * @param  contextValidation  contexte de validation
 * @param project             objet project à mettre à jour à l'EBI
 * @param study               objet study à mettre à jour à l'EBI
 * @param samples             liste des samples à mettre à jour à l'EBI
 * @param experiments         liste des experiments à mettre à jour à l'EBI
 * @return                    soumission nouvellement cree, et sauvée dans base.
 * @throws SraException       error
 */
public Submission createSubmissionForUpdate(ContextValidation contextValidation, 
										Project project,
										Study study,
										List<Sample> samples,
										List<Experiment> experiments) throws SraException {
	logger.debug("UpdateServices.createSubmissionForUpdate");
	// La soumission n'existe pas à ce stade donc pas de validation de state à faire
	Submission submission = null;
	Date courantDate = new java.util.Date();
	List <String> projectCodes = valideArgsCreateSubmissionForUpdate(contextValidation, project, study, samples, experiments);
	SraException.assertNoError(contextValidation);
	contextValidation.setCreationMode();

	submission = new Submission(contextValidation.getUser(), projectCodes);
	submission.code = sraCodeHelper.generateSubmissionCode(projectCodes);
	submission.traceInformation.creationDate = courantDate;
	submission.type = Submission.Type.UPDATE;
	submission.state = new State(SRASubmissionStateNames.SUBU_N, contextValidation.getUser());
	submission.submissionDirectory = VariableSRA.submissionRootDirectory + File.separator + submission.code + "_update"; 	
	submission.ebiResult = "ebi_update_" + submission.code + ".txt";

	//important pour la suite : ??????
	submission.setTraceUpdateStamp(contextValidation.getUser());

	// On verifie que l'objet qu'on vient de creer est bien ok :
	submission.validate(contextValidation);
	SraException.assertNoError(contextValidation);// ok et bien recuperé 

	// mettre à jour l'objet submission pour le cas d'une mise à jour du project
	if (project != null && StringUtils.isNotBlank(project.code)) {		
		submission.ebiProjectCode = project.code;	// si update d'un project (PRJEB)
	}
	// mettre à jour l'objet submission pour le cas d'une mise à jour de study 
	if (study != null && StringUtils.isNotBlank(study.code)) {		
		submission.studyCode = study.code;	
		submission.refStudyCodes.add(study.code);
	}
	// mettre à jour l'objet submission pour le cas d'une mise à jour de samples :
	for (Sample sample :samples) {
		submission.sampleCodes.add(sample.code);
		submission.refSampleCodes.add(sample.code);
	}		 
	// mettre à jour l'objet submission pour le cas d'une mise à jour d'experiments :
	for (Experiment experiment : experiments) {
		submission.experimentCodes.add(experiment.code);
	}
	
	// inutile, sinon begaiement sur historique
	//submission.updateStateAndTrace(new State(SRASubmissionStateNames.SUBU_N, contextValidation.getUser()));
	
	contextValidation.setCreationMode();// On crée une soumission pour update
	logger.debug("UpdateServices.createSubmissionForUpdate:: submissionCode = " + submission.code);
	submission.validate(contextValidation);
	logger.debug("UpdateServices.createSubmissionForUpdate:: apres validate de la soumission = ");
	if(contextValidation.hasErrors()) {
		logger.debug("UpdateServices.createSubmissionForUpdate:: validation soumission avec erreur");
	}
	
	SraException.assertNoError(contextValidation);
	logger.debug("UpdateServices.createSubmissionForUpdate:: apres assertNoError WWWWWWWWWWWW");

	Submission dbSubmission = submissionDAO.save(submission);
	logger.debug("UpdateServices.createSubmissionForUpdate:: dbSubmissionCode   wwwwwwwwww=" + dbSubmission.code);

	
	// updater dans la base project, study, samples et experiments 
	//qui sont deja validés et avec bon etat  et bon state après valideArgsCreateSubmissionForUpdate:
	
	contextValidation.setUpdateMode();	
	if (project != null) {
		projectDAO.save(project);
	}
	if (study != null) {
		studyDAO.save(study);
	} 
	for (Sample sample : samples) {
		sampleDAO.save(sample);
	}			
	for (Experiment experiment : experiments) {
		experimentDAO.save(experiment);
	}		
	return dbSubmission;
	}
	
}