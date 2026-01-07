package sra.api;

import static ngl.refactoring.state.SRASubmissionStateNames.SUBR_SMD_IP;
import static ngl.refactoring.state.SRASubmissionStateNames.SUBR_SMD_F;
import static ngl.refactoring.state.SRASubmissionStateNames.SUBR_SMD_FE;
import static ngl.refactoring.state.SRASubmissionStateNames.SUBR_FE;

import static ngl.refactoring.state.SRASubmissionStateNames.SUBU_SMD_IP;
import static ngl.refactoring.state.SRASubmissionStateNames.SUBU_SMD_F;
import static ngl.refactoring.state.SRASubmissionStateNames.SUBU_SMD_FE;
import static ngl.refactoring.state.SRASubmissionStateNames.SUBU_FE;
import static ngl.refactoring.state.SRASubmissionStateNames.SUB_F;
import static ngl.refactoring.state.SRASubmissionStateNames.SUB_V;
import static ngl.refactoring.state.SRASubmissionStateNames.SUB_SRD_IW;


import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import fr.cea.ig.authentication.Authentication;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.sra.SubmissionDAO;
import models.laboratory.common.instance.State;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.sra.instance.Project;
import models.sra.submit.sra.instance.Sample;
import models.sra.submit.sra.instance.Study;
import models.sra.submit.sra.instance.Submission;
import services.sra.SraException;
import services.sra.SraValidationException;
import sra.workflows.SubmissionWorkflows;
import validation.ContextValidation;

public class SubmissionWorkflowsAPI {
	private static final play.Logger.ALogger logger = play.Logger.of(SubmissionWorkflowsAPI.class);

    private final SubmissionNewAPI          submissionNewAPI;
    private final SubmissionNewAPITools     submissionNewAPITools;
    private final SubmissionWorkflows       submissionWorkflows;
    private final SubmissionDAO             submissionDAO;
    
    @Inject
    SubmissionWorkflowsAPI(SubmissionNewAPI           submissionNewAPI,
    		              SubmissionWorkflows         submissionWorkflows,
    		              SubmissionDAO               submissionDAO,
        				  SubmissionNewAPITools       submissionNewAPITools) {
    	this.submissionNewAPI      = submissionNewAPI;
    	this.submissionNewAPITools = submissionNewAPITools;
    	this.submissionWorkflows   = submissionWorkflows;
    	this.submissionDAO         = submissionDAO;
    }

	
	/**
	 * Construit la soumission correspondant à la release du study, ecrit les metadonnées sur le disque,
	 * envoie les metadonnées à l'EBI, charge le rapport de soumission de l'EBI et met la soumission dans l'etat
	 * SUB-F si tout c'est bien passé et SUBR-FE sinon.
	 * @param  ctxVal             contexte de validation
	 * @param  studyCode          code du study
	 * @return submission     	  Soumission après chargement du rapport de soumission.
	 * @throws SraException       error
	 */
	public Submission processSubmissionForStudyRelease(ContextValidation ctxVal, String studyCode) throws SraException {
		// Creer la soumission et sauver en base avec etat SUBR_N :
		Submission submission = submissionNewAPI.createSubmissionFromStudyRelease(ctxVal, studyCode);
		SraException.assertNoError(ctxVal);
		logger.debug("Soumission pour release du study " + studyCode + " bien cree avec code=" + submission.code);
		String user = Authentication.getUser();
		return processSubmissionForRelease(ctxVal, submission);
	}
	
	
	/**
	 * Construit la soumission correspondant à la release du sample, ecrit les metadonnées sur le disque,
	 * envoie les metadonnées à l'EBI, charge le rapport de soumission de l'EBI et met la soumission dans l'etat
	 * SUB-F si tout c'est bien passé et SUBR-FE sinon.
	 * @param  ctxVal             contexte de validation
	 * @param  sampleCode         code du sample
	 * @return submission     	  Soumission après chargement du rapport de soumission.
	 * @throws SraException       error 
	 */
	public Submission processSubmissionForSampleRelease(ContextValidation ctxVal, String sampleCode) throws SraException {
		Submission submission = null;
		submission = submissionNewAPI.createSubmissionFromSampleRelease(ctxVal, sampleCode);
		// Creer la soumission et sauver en base avec etat SUBR_N :
		SraException.assertNoError(ctxVal);
		logger.debug("Soumission pour release du sample " + sampleCode + " bien cree avec code=" + submission.code);
		return processSubmissionForRelease(ctxVal, submission);
	}
		
	// Realise les differentes etapes pour soumettre la soumission de type release
	public Submission processSubmissionForRelease(ContextValidation ctxVal, Submission submission) throws SraException {	
		String user = Authentication.getUser();
		// Creer repertoire de soumission et ecrire metadonnées pour soumission et sauver en base avec etat SUBR-SMD-IP :
		////submissionNewAPI.writeAndActivateSubmissionRelease(contextValidation, submission);
		submissionWorkflows.setState(ctxVal, submission, new State(SUBR_SMD_IP, user));
		SraException.assertNoError(ctxVal);
		logger.debug("!!!! processSubmissionForRelease :: Ecriture des fichiers ok pour la submission " + submission.code + " avec state="  + submission.state.code);
	
		// Envoyer Metadonnées à l'EBI et mettre en base soumission avec etat SUBR-SMD-F ou SUR-SMF-FE:
		////submissionNewAPI.ebiCurl(submission, contextValidation, user);
		submissionWorkflows.setState(ctxVal, submission, new State(SUBR_SMD_F, user));
		if(ctxVal.hasErrors()) {
			ContextValidation ctxVal_B = ContextValidation.createUndefinedContext(ctxVal.getUser());
			submissionWorkflows.setState(ctxVal_B, submission, new State(SUBR_SMD_FE, user));
			SraException.assertNoError(ctxVal_B);
		}
		SraException.assertNoError(ctxVal);
		logger.debug("!!!! processSubmissionForRelease :: Envoie des metadonnees a l'EBI ok pour la submission " + submission.code + " avec state="  + submission.state.code);

		// Charger Rapport de soumission de l'EBI et mettre en base soumission avec etat SUB-F ou SUBR-FE :
		////submissionNewAPI.loadRespEbiForRelease(contextValidation, submission);
		submissionWorkflows.setState(ctxVal, submission, new State(SUB_F, user));
		
//		if(ctxVal.hasErrors()) {
//			logger.debug("Affichage de displayError :");
//			ctxVal.displayErrors(logger, "debug");
//			logger.debug("End Affichage de displayError :");
//			// soit on controle le status d'erreur ici mais ne pas oublier d'utiliser un nouveau context de validation sans erreur, (ok marche)
//			// soit on controle le status d'erreur dans methode error de la transition 
//			// soit c'est deja gere par loadRespEbiForRelease() appelé dans execute=> c'est le cas ici
//			ContextValidation ctxValB = ContextValidation.createUndefinedContext(user);
//			logger.debug("OOOOOOOOOOOOOOO      avant appel de setState(SUBR_FE), submission.state.code=" + submission.state.code);
//			submissionWorkflows.setState(ctxValB, submission, new State(SUBR_FE, user));
//			logger.debug("OOOOOOOOOOOOOOO      apres appel de setState(SUBR_FE), submission.state.code=" + submission.state.code);
//		}
		SraException.assertNoError(ctxVal);
		logger.debug("!!!! processSubmissionForRelease :: Chargement du rapport de l'EBI ok pour la submission " + submission.code + " avec state="  + submission.state.code);
		return submissionDAO.getObject(submission.code);		
	}




	public Submission processSubmissionForUmbrella(ContextValidation ctxVal, 
												   String            title, 
												   String            description, 
												   List<String>      parentProjectAccessions, 
												   List<String>      childrenProjectAccessions, 
												   List <String>     idsPubmed, 
												   String            taxonId, 
												   String            attributes, 
												   String            dropBox) throws SraException, SraValidationException, APIValidationException, APIException {
		return(submissionNewAPI.initPrimarySubmissionForUmbrella(ctxVal, title, description, parentProjectAccessions, childrenProjectAccessions, idsPubmed, taxonId, attributes, dropBox));
	}
		
	
	/**
	 * Construit la soumission correspondant à l'update de données (umbrella, study, samples ou experiments), 
	 * ecrit les metadonnées sur le disque, envoie les metadonnées à l'EBI, charge le rapport de soumission de l'EBI 
	 * et met la soumission dans l'etat SUB-F si tout c'est bien passé et SUBR-FE sinon.
	 * @param  ctxVal             contexte de validation
	 * @param  umbrella           umbrella
	 * @param  study              study
	 * @param  samples            liste de samples
	 * @param  experiments        liste d'experiments
	 * @return submission     	  Soumission après chargement du rapport de soumission.
	 * @throws SraException       error
	 */
	public Submission processSubmissionForUpdate(ContextValidation ctxVal, 
			            						 Project umbrella, 
			                                     Study study, 
			                                     List<Sample> samples, 
			                                     List<Experiment> experiments,
			                                     boolean noCheckTaxonSampleCollab) throws SraException {
		String user = ctxVal.getUser();
		// Creer la soumission et sauver en base avec etat SUBU_N :
		Submission submission;
		try {
			submission = submissionNewAPI.createSubmissionForUpdate(ctxVal, umbrella, study, samples, experiments, noCheckTaxonSampleCollab);
		} catch (APIException e) {
			if (e.getMessage() != null) {
				ctxVal.addError("Error", e.getMessage()); 
			} else {
				ctxVal.addError("Error", "Erreur non identifie dans processSubmissionForUpdate"); 
			} 
			throw new SraValidationException(ctxVal);
		}
		
		SraException.assertNoError(ctxVal);
		//logger.debug("dans processSubmissionForUpdate, creation de la soumission " +  submission.code + " avec state=" + submission.state.code);
		// Creer repertoire de soumission et ecrire metadonnées pour soumission et sauver en base avec etat SUBR-SMD-IP :
		////submissionNewAPI.writeAndActivateSubmissionRelease(contextValidation, submission);
		submissionWorkflows.setState(ctxVal, submission, new State(SUBU_SMD_IP, user));
		SraException.assertNoError(ctxVal);
		//logger.debug("Ecriture des fichiers ok pour la submission " + submission.code);
		
		// Envoyer Metadonnées à l'EBI et mettre en base soumission avec etat SUBR-SMD-F :
		////submissionNewAPI.ebiCurlForRelease(submission, contextValidation, user);
		submissionWorkflows.setState(ctxVal, submission, new State(SUBU_SMD_F, user));
		if(ctxVal.hasErrors()) {
			//Si on utilise cette solution, penser à utiliser un nouveau ctxVal sans erreur (marche bien)
			ContextValidation ctxVal_B = ContextValidation.createUndefinedContext(user);
			submissionWorkflows.setState(ctxVal_B, submission, new State(SUBU_SMD_FE, user));
			SraException.assertNoError(ctxVal_B);
		}
		SraException.assertNoError(ctxVal);
		logger.debug("Envoie des metadonnees a l'EBI ok pour la submission " + submission.code);
		
		// Charger Rapport de soumission de l'EBI et mettre en base soumission avec etat SUB-F ou SUBR-FE :
		////submissionNewAPI.loadRespEbiForRelease(contextValidation, submission);
		submissionWorkflows.setState(ctxVal, submission, new State(SUB_F, user));
		// gestion du status d'erreur fait par la transition, par l'appel de loadRespEbiForUpdate dans le execute.
		SraException.assertNoError(ctxVal);
		logger.debug("Chargement du rapport de l'EBI ok pour la submission " + submission.code);
		return submissionDAO.getObject(submission.code);		
	}
	
	
	
	/**
	 * Construit les soumissions correspondant à la création des soumissions pour un tolid Atlasea
	 * valide la soumission et la démarre. Met la soumission dans l'état SUB-SRD-IW
	 * @param  ctxVal             contexte de validation
	 * @param  tolid              tolid
	 * @throws SraException       error
	 */
	public void processSubmissionsForAtlaseaRawData(String tolid, ContextValidation ctxVal, ContextValidation ctxSuccess) throws SraException {
		String user = ctxVal.getUser();
		ArrayList<Submission> submissions = submissionNewAPI.initPrimarySubmissionsAtlaseaForOneTolid(tolid, ctxVal, ctxSuccess);
		if(ctxVal.hasErrors()) {
			return;
		} else 

		if ( submissions.size() == 0 ) {
			// normalement message deja present dans succes mais au cas ou :
			if( ! ctxSuccess.hasErrors()) {
				ctxSuccess.addError("tolid." + tolid,  "Aucun readsets à soumettre");
			}
			return;
		}
		for (Submission submission : submissions) {
			submissionWorkflows.setState(ctxVal, submission, new State(SUB_V, user));
			SraException.assertNoError(ctxVal);
			submissionWorkflows.setState(ctxVal, submission, new State(SUB_SRD_IW, user));
			SraException.assertNoError(ctxVal);
			ctxSuccess.addError("submission." + submission.code, " validée et démarrée");
		}
		
	}
}
