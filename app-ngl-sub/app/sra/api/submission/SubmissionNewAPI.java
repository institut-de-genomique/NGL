package sra.api.submission;

import java.io.File;
import java.io.IOException;
//import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import fr.cea.ig.authentication.Authentication;
import fr.cea.ig.ngl.dao.sra.StudyDAO;
import fr.cea.ig.ngl.dao.sra.SubmissionDAO;
import mail.MailServiceException;
import models.laboratory.common.instance.State;
import models.sra.submit.common.instance.Project;
import models.sra.submit.common.instance.Sample;
import models.sra.submit.common.instance.Study;
import models.sra.submit.common.instance.Submission;
import models.sra.submit.common.instance.UserRefCollabType;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.util.SraException;
import models.sra.submit.util.SraValidationException;
import ngl.refactoring.state.SRASubmissionStateNames;
import services.ActivateServices;
import services.CreateServices;
import services.EbiAPI;
import services.EbiFileResponseServices;
import services.ReleaseServices;
import services.UpdateServices;
import services.XmlServices;
import validation.ContextValidation;

/**
 * Point d'entrée des differentes actions dans ngl-sub. 
 * Les controlleurs et le workflow ne devraient passer que par cette API pour declencher des actions dans ngl-sub.
 * @author sgas
 *
 */
// Attention les methodes qui modifient le state de l'objet submission devraient prendre l'objet submission 
// en argument (et non la string submissionCode) pour eviter d'avoir a gerer des rechargement de la soumission
// depuis la base dans le workflow.
public class SubmissionNewAPI {

	private final CreateServices            createServices;
	private final XmlServices               xmlServices;
	private final ActivateServices          activateServices;
	private final ReleaseServices           releaseServices;
	private final UpdateServices            updateServices;
	private final EbiAPI                    ebiAPI;
    private final EbiFileResponseServices   ebiFileResponseServices;
	private final SubmissionDAO             submissionDAO;
    private final SubmissionNewAPITools     submissionNewAPITools;
	
	private static final play.Logger.ALogger logger = play.Logger.of(SubmissionNewAPI.class);

	@Inject
	SubmissionNewAPI(CreateServices            createServices,
					 XmlServices               xmlServices,
//					 FileAcServices            fileAcServices,
					 ActivateServices          activateServices,
					 ReleaseServices           releaseServices,
					 UpdateServices            updateServices,
					 EbiAPI                    ebiAPI,
					 EbiFileResponseServices   ebiFileResponseServices, 
					 SubmissionDAO             submissionDAO,
					 SubmissionNewAPITools     submissionNewAPITools
					 ) {
		this.createServices          = createServices;
		this.xmlServices             = xmlServices;
		this.activateServices        = activateServices;
		this.releaseServices         = releaseServices;
		this.updateServices          = updateServices;
		this.ebiAPI                  = ebiAPI;
		this.ebiFileResponseServices = ebiFileResponseServices;
		this.submissionDAO           = submissionDAO;
	    this.submissionNewAPITools   = submissionNewAPITools;
	}
	
	/**
	 * Construit la soumission correspondant à des nouvelles données à soumettre à l'EBI.
	 * Met la soumission dans l'etat {@link ngl.refactoring.state.States#SUB_N}, valide 
	 * et sauve la soumission dans la base et cascade l'etat à tous les objets de la soumission.
	 * Declenche une  {@link SraException} en cas de probleme
	 * @param readSetCodes 		 liste des readsetCodes
	 * @param studyCode   	     Code du study 
	 * @param configCode         Code de la configuration de soumission
	 * @param acStudy            AC du study si soumission avec un seul study fournit par collaborateur
	 * @param acSample           AC du sample si soumission avec un seul sample fournit par collaborateur
	 * @param mapUserRefCollab   Correspondance entre refCollaborateur
	 * 							 et ac du study et/ou du sample à utiliser pour la soumission
	 * @param contextValidation  Context de validation qui contient erreurs si problemes
	 * @return codeSubmission    Code de la soumission crée
	 * @throws SraException      error
	 */
	public String initPrimarySubmission(List<String> readSetCodes, 
										String studyCode, 
										String configCode, 
										String acStudy, 
										String acSample, 
										Map<String, UserRefCollabType>mapUserRefCollab,
										ContextValidation contextValidation) throws SraException, SraValidationException {
		
		// La soumission n'existe pas à ce stade donc pas de validation de state à faire
		Submission submission;
		try {
		submission = createServices.initPrimarySubmission(readSetCodes, studyCode, 
				                                          configCode  , acStudy, 
												          acSample    , mapUserRefCollab, 
								       			          contextValidation);
		} catch (IOException e) {
			logger.debug("initPrimarySubmission " + e.getMessage());
			throw new SraException("initPrimarySubmission ", e.getMessage());
		} 
		// laisser filer les SraException, SraValidationException qui seront gerees dans le controlleur

		// Dans le cas d'un create, ne pas utiliser updateSubmissionState ni updateSubmissionChilObject
		// (les objets sont crées dans le bon etat)  car sinon duplication du state au niveau de l'historique.
		submission.setTraceUpdateStamp(contextValidation.getUser());
		contextValidation.setUpdateMode();
		submission.validate(contextValidation);
		if (contextValidation.hasErrors()) {
			throw new SraValidationException(contextValidation);
		}
		SraException.assertNoError(contextValidation);
		return submission.code;
	}	
	
		
	
	
	/**
	 * Verifie que la soumission est dans le bon etat pour etre demarré. Crée le repertoire
	 * de soumission, fait les liens sur les données brutes si données locales et met la 
	 * soumission dans l'état permettant d'etre prise en charge par Birds ({@link ngl.refactoring.state.States#SUB_SRD_IW}), 
	 * validation et sauvegarde dans base avec cascade de l'etats à tous les objets de la soumission.
	 * @param  contextValidation         Context de validation
	 * @param  submission                Soumission à demarrer (dans etat SUB_V)
	 * @throws SraValidationException    error
	 * @throws SraException              error
	 */
	public void startPrimarySubmission(ContextValidation contextValidation, Submission submission) throws SraValidationException, SraException {
		
		SraException.assertSubmissionStateCode(submission, SRASubmissionStateNames.SUB_V);	
		String user = Authentication.getUser();
		activateServices.activationPrimarySubmission(contextValidation, submission);
		submission.updateStateAndTrace(new State(SRASubmissionStateNames.SUB_SRD_IW, user));
		submission.validate(contextValidation);
		SraException.assertNoError(contextValidation);
//		if (contextValidation.hasErrors()) {
//			logger.debug("Dans startPrimarySubmission, detection d'erreur :");
//			contextValidation.displayErrors(logger, "debug");
//			return;
//		}
		submissionDAO.saveStateAndTrace(submission);
		// executionContext.exec(submissionDAO.update(submission));
		// cascade du state et traceInformation tous les objets de la soumission dans la base de données:
		submissionNewAPITools.updateSubmissionChildObject(submission);
	}
	

	
//	public void activationPrimarySubmission(ContextValidation contextValidation, Submission submission) throws SraException {
		
//		Ec ec = new Ec();
//		try {
//			assertSubmissionStateCode(submission, SRASubmissionStateNames.SUB_V);	
//		
//		String user = VariableSRA.admin;
//		submissionWorkflowsHelper.activationPrimarySubmission(contextValidation, submission, ec);
//		if (contextValidation.hasErrors()) {
//			throw new SraException("Soumission invalide :" + contextValidation.getErrors());
//		}
//		setSubmissionState(submission, new State(SRASubmissionStateNames.SUB_SRD_IW, user));
//		ec.add(submissionDAO.update(submission));
//		//executionContext.exec(submissionDAO.update(submission));
//		// cascade du state et traceInformation tous les objets de la soumission dans la base de données:
//		updateSubmissionChildObject(submission, contextValidation);
//		ec.commit;
//		} catch(Exception e){
//			ec.rollBack();
//		}
//	}
//		
//	public void m(Runnable leGrosAppel) {
//		Ec ec = new Ec();
//		try {
//			leGrosAppel.run();
//			ec.commit;
//		} catch(Exception e){
//			ec.rollBack();
//		}
//	}
//	
		
	
	/**
	 * Lors d'une soumission primaire de données, ecrit les fichiers de metadonnées dans le repertoire 
	 * de soumission et met la soumission dans l'etat {@link ngl.refactoring.state.States#SUB_SMD_IW}.
	 * Sauvegarde la soumission dans la base et cascade etat dans tous les objets de la soumission.
	 * @param submission          objet submission
	 * @param contextValidation   context de validation
	 * @throws SraException       error
	 */
	public void writeMetaData(Submission submission, ContextValidation contextValidation) throws SraException {
		//Submission submission = submissionDAO.getSubmission(submissionCode);
//		String user = VariableSRA.admin;
		String user = Authentication.getUser();
		SraException.assertSubmissionStateCode(submission, SRASubmissionStateNames.SUB_SRD_F);
		// La soumission est mise a jour dans xmlServices pour les champs xml.	
		xmlServices.writeAllXml(submission);
		// installation etat après action:
//		updateSubmissionStateAndTrace(submission, new State(SRASubmissionStateNames.SUB_SMD_IW, user));
		submission.updateStateAndTrace(new State(SRASubmissionStateNames.SUB_SMD_IW, user));
		// validation + cascade du state et traceInformation à tous les objets de la soumission :
		contextValidation.setUpdateMode();
		submission.validate(contextValidation);
		SraException.assertNoError(contextValidation);
		//submissionDAO.update(submission);	
		submissionDAO.saveStateAndTrace(submission);
		// sauver les champs xmlstudys...... mis à jour dans l'objet
		submissionDAO.save(submission);  
		submissionNewAPITools.updateSubmissionChildObject(submission);
	}
	

	/**
	 * Parse le fichier des AC de l'EBI recu en retour de l'envoie 
	 * des metadonnées pour une soumission primaire de données. Met à jour les differents objets de la 
	 * soumission pour les AC, les dates (creation, releaseDate), le status et TraceInformation
	 * Si le fichier des AC contient un studyAc, alors va chercher le project correspondant à l'EBI
	 * et le met dans la collection ngl-sub.project.
	 * @param  submission         objet soumission
	 * @param  contextValidation  error
	 * @throws SraException       error
	 */
	public void loadEbiResponseAC(ContextValidation contextValidation, Submission submission) throws SraException {
		//Submission submission = submissionDAO.getSubmission(submissionCode);
		SraException.assertSubmissionStateCode(submission, SRASubmissionStateNames.SUB_SMD_F);	
		String user;
		contextValidation.setUpdateMode();
		if (StringUtils.isNotBlank(submission.traceInformation.createUser)) {
			user = submission.traceInformation.createUser;
		} else {
			user = contextValidation.getUser();
		}
		
		try {
//			fileAcServices.traitementFileAC(contextValidation, 
//											submission.code, 
//											new File(submission.submissionDirectory + File.separator + submission.ebiResult));
			if(StringUtils.isBlank(submission.ebiResult)) {
				logger.debug("Dans SubmissionNewAPI.loadEbiResponseAC : soumission passee en parametre sans champs ebiResult");
				throw new SraException("Dans SubmissionNewAPI.loadEbiResponseAC : soumission passee en parametre sans champs ebiResult");

			}
			//logger.debug("Dans SubmissionNewAPI.loadEbiResponseAC, avant appel de ebiFileResponseServices.loadEbiResp");
			ebiFileResponseServices.loadEbiResp(contextValidation, 
												submission, 
												new File(submission.submissionDirectory + File.separator + submission.ebiResult));
			//logger.debug("Dans SubmissionNewAPI.loadEbiResponseAC, apres appel de ebiFileResponseServices.loadEbiResp");

		} catch (SraException e) {
			submission.updateStateAndTrace(new State(SRASubmissionStateNames.SUB_FE, user));
			submissionDAO.saveStateAndTrace(submission);
			submissionNewAPITools.updateSubmissionChildObject(submission);
			logger.debug("Dans SubmissionNewAPI.loadEbiResponseAC, Soumission invalide :" + contextValidation.getErrors());
			throw new SraException("loadEbiResponseAC",e);
		}
		// validation :
		submission.validate(contextValidation);	
		if (contextValidation.hasErrors()) {
			submission.updateStateAndTrace(new State(SRASubmissionStateNames.SUB_FE, user));
			submissionDAO.saveStateAndTrace(submission);
			submissionNewAPITools.updateSubmissionChildObject(submission);
			logger.debug("Soumission invalide :" + contextValidation.getErrors());
			throw new SraValidationException(contextValidation);
		} 
		submission.updateStateAndTrace(new State(SRASubmissionStateNames.SUB_F, user));
		submissionDAO.saveStateAndTrace(submission);
		// cascade du state et traceInformation tous les objets de la soumission dans la base de données:
		submissionNewAPITools.updateSubmissionChildObject(submission);
		if(StringUtils.isNotBlank(submission.studyCode)) {
			ebiAPI.loadProjectFromEbi(submission.studyCode);
		}
	}
	
	
	/**
	 * Construit la soumission correspondant à la release du study dont le code est passé
	 * en parametre et sauve la soumission dans la database avec l'etat
	 * {@link ngl.refactoring.state.States#SUBR_N}.
	 * Ajoute une erreur dans le contexte de validation si une erreur survient.
	 * @param  contextValidation  contexte de validation
	 * @param  studyCode          code du study
	 * @param  user               nom de l'utilisateur
	 * @return submission     	  Soumission nouvellement cree pour la release
	 * @throws SraException       error
	 */
	public Submission createSubmissionFromStudy(ContextValidation contextValidation, 
												String studyCode, 
												String user) throws SraException {		
		// La soumission n'existe pas à ce stade donc pas de validation de state à faire
		//String user = Authentication.getUser();	
		String submissionCode = releaseServices.createSubmissionForRelease(contextValidation, studyCode, user);
		Submission submission = submissionDAO.getObject(submissionCode);
		submission.setTraceUpdateStamp(contextValidation.getUser());
		contextValidation.setUpdateMode();
		submission.validate(contextValidation);
//		if (contextValidation.hasErrors()) {
//			throw new SraValidationException(contextValidation);
//		}
		SraException.assertNoError(contextValidation);
		return submissionDAO.getObject(submissionCode);		
	}

	/**
	 * Cree le repertoire de soumission pour la release du study. Ecrit les metadonnées
	 * et sauve dans la base la soumission et le study dans l'etat {@link ngl.refactoring.state.States#SUBR_SMD_IW}.
	 * Met a jour le context de validation si problemes ou declenche une {@link SraException}
	 * @param contextValidation   contexte de validation
	 * @param submission          soumission
	 * @throws SraException       error
	 */
	public void writeAndActivateSubmissionRelease(ContextValidation contextValidation, 
												  Submission submission) throws SraException {
		SraException.assertSubmissionStateCode(submission, SRASubmissionStateNames.SUBR_N);
		String user = Authentication.getUser();
		submission.createDirSubmission();
		xmlServices.writeAllXml(submission);

		// installation etat après action:
		submission.updateStateAndTrace(new State(SRASubmissionStateNames.SUBR_SMD_IW, user));
		contextValidation.setUpdateMode();
		// validation + cascade du state et traceInformation à tous les objets de la soumission :
		submission.validate(contextValidation);
		SraException.assertNoError(contextValidation);
		submissionDAO.saveStateAndTrace(submission);
		// cascade du state et traceInformation tous les objets de la soumission dans la base de données:
		submissionNewAPITools.updateSubmissionChildObject(submission);
	}
	

	/**
	 * Parse le fichier retour de l'EBI dans le cadre d'une release de study. Sauve la date de release 
	 * du study et met la soumission et le study dans l'etat {@link ngl.refactoring.state.States#SUB_F},
	 * si aucun probleme. En cas de probleme, met la soumission et le study dans l'etat
	 * {@link ngl.refactoring.state.States#SUB_FE}
	 * @param contextValidation   context de validation
	 * @param submission          soumission
	 * @throws SraException       error
	 */
	public void loadRespEbiForRelease(ContextValidation contextValidation, 
								      Submission submission) throws SraException {
		SraException.assertSubmissionStateCode(submission, SRASubmissionStateNames.SUBR_SMD_F);
		String user = Authentication.getUser();	
		logger.debug("loadRespEbiForRelease");
		try {
			releaseServices.traitementRetourRelease(contextValidation, 
													submission, 
													new File(submission.submissionDirectory + File.separator + submission.ebiResult));
		} catch(IOException| SraException | MailServiceException e) {
			submission.updateStateAndTrace(new State(SRASubmissionStateNames.SUBR_FE, user));
			submissionDAO.saveStateAndTrace(submission);
			submissionNewAPITools.updateSubmissionChildObject(submission);
			throw new SraException("loadRespEbiForRelease", e.getMessage());
		} 
		// validation :
		contextValidation.setUpdateMode();
		submission.validate(contextValidation);	
		if (contextValidation.hasErrors()) {
			submission.updateStateAndTrace(new State(SRASubmissionStateNames.SUBR_FE, user));
			submissionDAO.saveStateAndTrace(submission);
			submissionNewAPITools.updateSubmissionChildObject(submission);
			throw new SraValidationException(contextValidation);
		} 
//		updateSubmissionStateAndTrace(submission, new State(SRASubmissionStateNames.SUB_F, user));
		submission.updateStateAndTrace(new State(SRASubmissionStateNames.SUB_F, user));
		submissionDAO.saveStateAndTrace(submission);
		// cascade du state et traceInformation tous les objets de la soumission dans la base de données:
		submissionNewAPITools.updateSubmissionChildObject(submission);
	}
	
	
	/**
	 * Construit la soumission correspondant au study, samples ou experiments presents dans la base
	 * et qui doivent etre soumis à l'EBI pour une mise à jour. (les objets modifiés sont dans la base mais
	 * il faut les soumettre à l'EBI pour que les modifications soient prises en compte.).
	 * Renvoie la soumission avec ses sous-objets dans l'etat {@link ngl.refactoring.state.States#SUBU_N}.
	 * Ajoute une erreur dans le contexte de validation si une erreur survient.
	 * @param  contextValidation  contexte de validation
	 * @param  project            objet project à mettre à jour à l'EBI
	 * @param  study              objet study à mettre à jour à l'EBI
	 * @param  samples            liste des samples à mettre à jour à l'EBI
	 * @param  experiments        liste des experiments à mettre à jour à l'EBI
	 * @return                    soumission nouvellement cree, et sauvée dans base.
	 * @throws SraException       error
	 */
	public Submission createSubmissionForUpdate(ContextValidation contextValidation, 
												Project project,
												Study study, 
												List<Sample> samples,
												List<Experiment> experiments) throws SraException {		
		// La soumission n'existe pas à ce stade donc pas de validation de state à faire
		//String user = Authentication.getUser();	
		logger.debug("SubmissionNewAPI::createSubmissionForUpdate");

		if (project != null && StringUtils.isNotBlank(project.code)) {
			logger.debug("projectCode = " + project.code); 
		}
		if (study != null && StringUtils.isNotBlank(study.code)) {
			logger.debug("studyCode = " + study.code); 
		}
		for (Sample sample : samples) {
			logger.debug("sampleCode = " + sample.code);
		}
		for (Experiment experiment : experiments) {
			logger.debug("experimentCodes = " + experiment.code);
		}

		Submission submission = updateServices.createSubmissionForUpdate(contextValidation, project, study, samples, experiments);
		// Dans le cas d'un update, ne pas utiliser updateSubmissionState car la soumission est cree dans
		// le bon etat , par contre il faut utiliser updateSubmissionChildObject car les objets de la soumission
		// ne viennent pas d'etre cree et leur etat doit etre synchronisé.
		logger.debug(" HHHHHHHHHH  SubmissionNewAPI::createSubmissionForUpdate: avant setTraceUpdateStamp");
		submission.setTraceUpdateStamp(contextValidation.getUser());
		
		contextValidation.setUpdateMode();
		submission.validate(contextValidation);		
		if (contextValidation.hasErrors()) {
			logger.debug("HHHHHHHHHHH  submissionNewAPI::createSubmissionForUpdate: error dans hasError:");
			contextValidation.displayErrors(logger, "debug");
		} 
		SraException.assertNoError(contextValidation);
		logger.debug(" HHHHHHHHHH   SubmissionNewAPI::createSubmissionForUpdate: avant retour");
		Submission submissionFinal = submissionDAO.getObject(submission.code);
		//return submissionDAO.getObject(submission.code);
		logger.debug(" HHHHHHHHHH   SubmissionNewAPI::createSubmissionForUpdate: avant retour, submissionFinal.code = " + submissionFinal.code);
		return submissionFinal;
	}
	
	/**
	 * Cree le repertoire de soumission pour une mise à jour des données. Ecrit les metadonnées
	 * et sauve dans la base la soumission et le study dans l'etat {@link ngl.refactoring.state.States#SUBU_SMD_IW}.
	 * @param contextValidation   contexte de validation
	 * @param submission          soumission
	 * @throws SraException       error
	 */
	public void writeAndActivateSubmissionUpdate(ContextValidation contextValidation, 
												 Submission submission) throws SraException {
		SraException.assertSubmissionStateCode(submission, SRASubmissionStateNames.SUBU_N);
		String user = Authentication.getUser();
		submission.createDirSubmission();
		xmlServices.writeAllXml(submission);
		// sauver la soumission avec les informations de fichier xml :
		submissionDAO.save(submission);
		// installation etat après action:
		submission.updateStateAndTrace(new State(SRASubmissionStateNames.SUBU_SMD_IW, user));
		contextValidation.setUpdateMode();
		// validation + cascade du state et traceInformation à tous les objets de la soumission :
		submission.validate(contextValidation);
		SraException.assertNoError(contextValidation);
		submissionDAO.saveStateAndTrace(submission);
		// cascade du state et traceInformation tous les objets de la soumission dans la base de données:
		submissionNewAPITools.updateSubmissionChildObject(submission);
	}
	
	/**
	 * Parse le fichier retour de l'EBI dans le cadre d'une mise à jour de données (study, samples ou experiments)
	 * Met la soumission et ses objets dans l'etat {@link ngl.refactoring.state.States#SUB_F},
	 * si aucun probleme. En cas de probleme, met la soumission et ses objets dans l'etat
	 * {@link ngl.refactoring.state.States#SUB_FE}
	 * @param contextValidation   context de validation
	 * @param submission          soumission
	 * @throws SraException       error
	 */	
	public void loadRespEbiForUpdate(ContextValidation contextValidation, 
			Submission submission) throws SraException {
		SraException.assertSubmissionStateCode(submission, SRASubmissionStateNames.SUBU_SMD_F);
		String user = Authentication.getUser();	
		//logger.debug("loadRespEbiForUpdate");
		try {
			ebiFileResponseServices.loadEbiResp(contextValidation, 
												submission, 
												new File(submission.submissionDirectory + 
														 File.separator + 
														 submission.ebiResult));
		} catch(SraException e) {
			//logger.debug("je catch bien SraException");
			submission.updateStateAndTrace(new State(SRASubmissionStateNames.SUBU_FE, user));
			//logger.debug("j'ai mis le state a jour ");
			submissionDAO.saveStateAndTrace(submission);
			//logger.debug("j'ai sauvé le state");

			submissionNewAPITools.updateSubmissionChildObject(submission);
			//logger.debug("j'ai propagé le state et je relance la SraException");
			throw new SraException("loadRespEbiForUpdate", e.getMessage());
		} 
		// validation :
		contextValidation.setUpdateMode();
		submission.validate(contextValidation);	
		if (contextValidation.hasErrors()) {
			submission.updateStateAndTrace(new State(SRASubmissionStateNames.SUBU_FE, user));
			submissionDAO.saveStateAndTrace(submission);
			submissionNewAPITools.updateSubmissionChildObject(submission);
			throw new SraValidationException(contextValidation);
		} 
		submission.updateStateAndTrace(new State(SRASubmissionStateNames.SUB_F, user));
		submissionDAO.saveStateAndTrace(submission);
		// cascade du state et traceInformation tous les objets de la soumission dans la base de données:
		submissionNewAPITools.updateSubmissionChildObject(submission);
	}
			
}