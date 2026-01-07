package sra.services;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import com.gargoylesoftware.htmlunit.javascript.host.Console;

import controllers.sra.api.AbstractSampleAPI;
import controllers.sra.api.AbstractStudyAPI;
import controllers.sra.api.ExperimentAPI;
import controllers.sra.api.ReadsetAPI;
import controllers.sra.api.SubmissionAPI;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import models.sra.submit.sra.instance.AbstractSample;
import models.sra.submit.sra.instance.AbstractStudy;
import models.sra.submit.sra.instance.Sample;
import models.sra.submit.sra.instance.Study;
import models.sra.submit.sra.instance.Submission;
import services.sra.SraException;
import services.sra.SraValidationException;
import sra.services.SraEbiAPI;
import validation.ContextValidation;



public class DatabaseConsistencyTools {

	private final SubmissionAPI       submissionAPI;
	private final AbstractStudyAPI    abstractStudyAPI;
	private final AbstractSampleAPI   abstractSampleAPI;
	private final SraEbiAPI           sraEbiAPI;
	private final ReadsetAPI          readsetAPI;
	private static final play.Logger.ALogger logger = play.Logger.of(DatabaseConsistencyTools.class);

	@Inject
	public DatabaseConsistencyTools(
			SubmissionAPI       submissionAPI,
			AbstractStudyAPI    abstractStudyAPI,
			AbstractSampleAPI   abstractSampleAPI,
			ExperimentAPI       experimentAPI,
			SraEbiAPI           sraEbiAPI,
			ReadsetAPI          readsetAPI) {
		this.submissionAPI       = submissionAPI;
		this.abstractStudyAPI    = abstractStudyAPI;
		this.abstractSampleAPI   = abstractSampleAPI;
		this.sraEbiAPI           = sraEbiAPI;
		this.readsetAPI          = readsetAPI;
	}


	private void _completeDatabaseForExternalSample(Submission submission, HashMap<String, AbstractSample> corrected_samples, ContextValidation ctxVal) throws Exception {
		//logger.debug("Entree dans _completeDatabaseForExternalSample");
		Date courantDate = new Date();
		String user = "ngsrg";
		for (String sampleCode : submission.refSampleCodes) {
			//logger.debug("XXXX  sampleCode='{}'",sampleCode);
			AbstractSample dbSample = abstractSampleAPI.get(sampleCode);
			if( ! abstractSampleAPI.isObjectExist(sampleCode)) {
				ctxVal.addError("sample." + sampleCode, "n'existe pas dans la base de données");
				continue;
			}
			if(dbSample instanceof Sample) {
				continue;
			}
			// on ne travaille que sur les externalSample pour qui il manque un identifiant :
			
			if (StringUtils.isNotBlank(dbSample.accession) && StringUtils.isNotBlank(dbSample.externalId) ) {
				// inutile d'interroger l'EBI, on a les 2 identifiants
				//logger.debug("sample avec accession et externalId");
				continue;
			}				
			if (StringUtils.isBlank(dbSample.accession) && StringUtils.isBlank(dbSample.externalId) ) {
				//logger.debug("sample sans accession ni externalId");
				throw new SraException("Le sample de la base " + dbSample.code + " ne contient ni AC ni externalId ???? ");
			}
			// recuperer sample a l'EBI :
			if(sraEbiAPI.completeSampleIdentifier(dbSample)) {
				// ajouter le sample complété pour les identifiants à la liste des study corrigés à sauvegarder
				dbSample.traceInformation.modifyDate = courantDate;
				dbSample.traceInformation.modifyUser = user;
				if( ! corrected_samples.containsKey(dbSample.code)) {
					corrected_samples.put(dbSample.code, dbSample);
				}
			} else {
				// signaler probleme
				ctxVal.addError("sample." + dbSample.code, "recuperation des identifiants a l'EBI en echec");
			}
		}
	}


	private void _completeDatabaseForExternalStudy(Submission submission, HashMap<String, AbstractStudy> corrected_studies, ContextValidation ctxVal) throws Exception  {
		Date courantDate = new Date();
		String user = "ngsrg";
		//logger.debug("Entree dans _completeDatabaseForExternalStudy");

		for(String studyCode : submission.refStudyCodes) {
			//logger.debug("studyCode={}", studyCode);
			if( ! abstractStudyAPI.isObjectExist(studyCode)) {
				ctxVal.addError("study." + studyCode, "n'existe pas dans la base de données");
				continue;
			}
			AbstractStudy dbStudy = abstractStudyAPI.get(studyCode);
			//logger.debug("dbStudy.code={}", dbStudy.code);
			if(dbStudy instanceof Study) {
				continue;
			}
			// on ne travaille que sur les externalStudy pour qui il manque un identifiant :

			// si les 2 identifiants du study sont presents, inutile de traiter le study deja complet
			if (StringUtils.isNotBlank(dbStudy.accession) && StringUtils.isNotBlank(dbStudy.externalId) ) {
				// inutile d'interroger l'EBI, on a les 2 identifiants
				continue;
			}
			
			// si aucun identifiant du study n'est present, problème
			if (StringUtils.isBlank(dbStudy.accession) && StringUtils.isBlank(dbStudy.externalId) ) {
				throw new SraException("Le study de la base " + dbStudy.code + " ne contient ni AC ni externalId");
			}
			// s'il manque un seul identifiant au study alors le traiter :
			// Recherche du studyAccession ou studyExternalId dans un experiment de notre dropBox referencant
			// ce study
			if(sraEbiAPI.completeStudyIdentifier(dbStudy)) {
				// ajouter le study complété pour les identifiants à la liste des study corrigés à sauvegarder
				dbStudy.traceInformation.modifyDate = courantDate;
				dbStudy.traceInformation.modifyUser = user;
				if( ! corrected_studies.containsKey(dbStudy.code)) {
					corrected_studies.put(dbStudy.code, dbStudy);
				}
				//logger.debug("correction ok pour le study {}", dbStudy.code);
			} else {
				// signaler probleme
				//logger.debug("studyCode avec pb");				
				ctxVal.addError("study." + dbStudy.code, "recuperation des identifiants sur notre drop-box Webin-9 en echec");
			}
		}	
	}



	/**
	 * Met à jour la base de données pour les ExternalSamples et ExternalStudy referencés dans la soumission.
	 * On laisse les experiments avec leur identifiant de study et de sample qu'ils soient de type AC ou externalId. 
	 * Ajoute une erreur dans le contexte de validation si une erreur survient.
	 * @param  submissionCode     code de la soumission pour laquelle on met à jour les ExternalStudy et ExternalSample
	 * @throws SraException       error
	 * @throws APIException       error
	 * @throws APIValidationException error
	 */
	public void completeDatabaseForExternalSampleAndStudy(String submissionCode) throws SraException, APIValidationException, APIException {
		//logger.debug("Entree dans completeBaseForExternalSampleAndStudy");
		Submission submission = submissionAPI.get(submissionCode);
		if(submission == null) {
			throw new SraException("La soumission indiquée " +  submissionCode + " n'existe pas dans la base");
		}
		String user = "ngsrg";
		ContextValidation contextValidation = ContextValidation.createUpdateContext(user);

		HashMap<String, AbstractSample> samples_to_save = new HashMap<String, AbstractSample>();
		HashMap<String, AbstractStudy> studies_to_save = new HashMap<String, AbstractStudy>();

		String errorMessage = "";
		String headErrorMessage = "completeDatabaseForExternalSampleAndStudy : Detection d'erreurs pour la soumission " + submissionCode + "\n";
		try {
			_completeDatabaseForExternalSample(submission, samples_to_save, contextValidation);
			_completeDatabaseForExternalStudy(submission, studies_to_save, contextValidation);

		} catch (Exception e) {
			throw new SraException("completeDatabaseForExternalSampleAndStudy:: " + headErrorMessage);
		}
		
		// validation des objets à sauvegarder :
		for (Iterator<Entry<String, AbstractSample>> iterator = samples_to_save.entrySet().iterator(); iterator.hasNext();) {
			Entry<String, AbstractSample> entry = iterator.next();
			entry.getValue().validate(contextValidation);
			// verifier qu'il n'existe pas dans la base un autre sample avec meme accession
			if(StringUtils.isNotBlank(entry.getValue().accession)) {			
				List <AbstractSample> absSampleList = abstractSampleAPI.dao_find(DBQuery.in("accession", entry.getValue().accession)).toList();
				//logger.debug(absSampleList.size() + " Sample avec accession = " + entry.getValue().accession);
				if (absSampleList.size() > 1) {
					errorMessage += "- Existence de plusieurs samples pour le sampleAccession=" + entry.getValue().accession + "\n";
					for (AbstractSample absSample: absSampleList) {
						errorMessage += "       -  dans sample avec code " + absSample.code + "\n";
					}
				} 
			}
			// verifier qu'il n'existe pas dans la base un autre sample avec meme externalId
			if(StringUtils.isNotBlank(entry.getValue().externalId)) {			
				List <AbstractSample> absSampleList = abstractSampleAPI.dao_find(DBQuery.in("externalId", entry.getValue().externalId)).toList();
				//logger.debug(absSampleList.size() + " Sample avec externalId = " + entry.getValue().externalId);
				if (absSampleList.size() > 1) {
					errorMessage += "- Existence de plusieurs samples pour le sampleExternalId=" + entry.getValue().externalId + "\n";
					for (AbstractSample absSample: absSampleList) {
						errorMessage += "       - voir sample avec code " + absSample.code + "\n";
					}
				} 
			}

		}
		for (Iterator<Entry<String, AbstractStudy>> iterator = studies_to_save.entrySet().iterator(); iterator.hasNext();) {
			Entry<String, AbstractStudy> entry = iterator.next();
			entry.getValue().validate(contextValidation);
			// verifier qu'il n'existe pas dans la base un autre study avec meme accession
			if(StringUtils.isNotBlank(entry.getValue().accession)) {			
				List <AbstractStudy> absStudyList = abstractStudyAPI.dao_find(DBQuery.in("accession", entry.getValue().accession)).toList();
				//logger.debug(absStudyList.size() + " Study avec accession = " + entry.getValue().accession);
				if (absStudyList.size() > 1) {
					errorMessage += "- Existence de plusieurs studies pour le studyAccession=" + entry.getValue().accession + "\n";
					for (AbstractStudy absStudy: absStudyList) {
						errorMessage += "       -  voir study avec code " + absStudy.code + "\n";
					}
				} 
			}
			// verifier qu'il n'existe pas dans la base un autre study avec meme externalId
			if(StringUtils.isNotBlank(entry.getValue().externalId)) {			
				List <AbstractStudy> absStudyList = abstractStudyAPI.dao_find(DBQuery.in("externalId", entry.getValue().externalId)).toList();
				//logger.debug(absStudyList.size() + " Study avec externalId = " + entry.getValue().externalId);
				if (absStudyList.size() > 1) {
					errorMessage += "- Existence de plusieurs studies pour le studyExternalId=" + entry.getValue().externalId + "\n";
					for (AbstractStudy absStudy: absStudyList) {
						errorMessage += "       - voir study avec code " + absStudy.code + "\n";
					}
				} 
			}
		}					

		if (contextValidation.hasErrors()) {
			throw new SraValidationException(contextValidation);
		} 
		// Envoyer message d'erreur si probleme de doublons d'ExternalSample ou d'ExternalStudy :
		if(StringUtils.isNotBlank(errorMessage)) {
			throw new SraException("completeDatabaseForExternalSampleAndStudy:: " + headErrorMessage + errorMessage);	
		}
		// sauvegarde dans base des samples, study corrigés :

		Date courantDate = new Date();
		for (Iterator<Entry<String, AbstractSample>> iterator = samples_to_save.entrySet().iterator(); iterator.hasNext();) {
			Entry<String, AbstractSample> entry = iterator.next();
			if ( StringUtils.isNotBlank(entry.getValue().accession) && StringUtils.isNotBlank(entry.getValue().externalId) ) {
				entry.getValue().traceInformation.modifyUser=user;
				entry.getValue().traceInformation.modifyDate=courantDate;
				abstractSampleAPI.dao_update(DBQuery.is("code", entry.getValue().code),
					DBUpdate.set("accession", entry.getValue().accession)
							.set("externalId", entry.getValue().externalId)
							.set("traceInformation.modifyUser", user)
							.set("traceInformation.modifyDate", courantDate)); 
				//logger.debug("sauvegarde dans la base du sample " + entry.getValue().code);
			// Repercuter recuperation d'un identifier de AbstractSample sur la collection readset de ngl-sub
			readsetAPI.dao_update(DBQuery.is("sampleCode", entry.getValue().code),
					 DBUpdate.set("sampleAccession", entry.getValue().accession)
					 		 .set("sampleExternalId", entry.getValue().externalId));
			}
		}
		for (Iterator<Entry<String, AbstractStudy>> iterator = studies_to_save.entrySet().iterator(); iterator.hasNext();) {
			Entry<String, AbstractStudy> entry = iterator.next();
			if ( StringUtils.isNotBlank(entry.getValue().accession) && StringUtils.isNotBlank(entry.getValue().externalId) ) {
				abstractStudyAPI.dao_update(DBQuery.is("code", entry.getValue().code),
						DBUpdate.set("accession", entry.getValue().accession)
						.set("externalId", entry.getValue().externalId)
						.set("traceInformation.modifyUser", user)
						.set("traceInformation.modifyDate", courantDate)); 
				//logger.debug("sauvegarde dans la base du study " + entry.getValue().code);
				// Repercuter recuperation d'un identifier de AbstractStudy sur la collection readset de ngl-sub
				readsetAPI.dao_update(DBQuery.is("studyCode", entry.getValue().code),
						 DBUpdate.set("studyAccession", entry.getValue().accession)
						 		 .set("studyExternalId", entry.getValue().externalId));
			}
		}					
		logger.debug("Fin du traitement completeDatabaseForExternalSampleAndStudy");
	}


}
