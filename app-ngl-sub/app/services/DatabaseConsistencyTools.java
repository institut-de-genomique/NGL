package services;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import fr.cea.ig.ngl.dao.api.sra.AbstractSampleAPI;
import fr.cea.ig.ngl.dao.api.sra.AbstractStudyAPI;
import fr.cea.ig.ngl.dao.api.sra.ExperimentAPI;
import fr.cea.ig.ngl.dao.api.sra.SubmissionAPI;
import models.sra.submit.sra.instance.AbstractSample;
import models.sra.submit.sra.instance.AbstractStudy;
import models.sra.submit.sra.instance.EbiIdentifiers;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.sra.instance.Sample;
import models.sra.submit.sra.instance.Study;
import models.sra.submit.sra.instance.Submission;
import models.sra.submit.util.SraException;
import models.sra.submit.util.SraValidationException;
import validation.ContextValidation;



public class DatabaseConsistencyTools {

	private final SubmissionAPI       submissionAPI;
	private final AbstractStudyAPI    abstractStudyAPI;
	private final AbstractSampleAPI   abstractSampleAPI;
	private final ExperimentAPI       experimentAPI;
	private final SraEbiAPI              ebiAPI;
	private static final play.Logger.ALogger logger = play.Logger.of(DatabaseConsistencyTools.class);

	@Inject
	public DatabaseConsistencyTools(
			SubmissionAPI       submissionAPI,
			AbstractStudyAPI    abstractStudyAPI,
			AbstractSampleAPI   abstractSampleAPI,
			ExperimentAPI       experimentAPI,
			SraEbiAPI              ebiAPI) {
		this.submissionAPI       = submissionAPI;
		this.abstractStudyAPI    = abstractStudyAPI;
		this.abstractSampleAPI   = abstractSampleAPI;
		this.experimentAPI       = experimentAPI;
		this.ebiAPI              = ebiAPI;
	}
	
	 
		private String  _completeDatabaseForExternalSample(Submission submission, HashMap<String, AbstractSample> corrected_samples, HashMap<String, Experiment> corrected_experiments) throws Exception {
			//logger.debug("Entree dans _completeDatabaseForExternalSample");
			Date courantDate = new Date();
			String user = "ngsrg";
			String errorMessage = "";
			for (String sampleCode : submission.refSampleCodes) {
				//logger.debug("XXXX  sampleCode="+sampleCode);
				AbstractSample dbSample = abstractSampleAPI.get(sampleCode);
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
					throw new SraException("Le sample de la base " + dbSample.code + " ne contient ni AC ni externalId ???? ");
				}
				// recuperer sample a l'EBI :
				
				// le sample de la base a un ERS, il faut recuperer le sample à l'EBI et son SAM
				if (StringUtils.isNotBlank(dbSample.accession)) {
					String head = "- Pour le sample de la base avec accession=" + dbSample.accession + ", ";

					if ( ! dbSample.accession.startsWith("ERS") ) {
						throw new SraException(head + "l'AC ne commence pas par ERS");	
					}
					//logger.debug("Recherche du sample à l'EBI via AC  " + dbSample.accession);

					EbiIdentifiers ebiIdentifiers = ebiAPI.EbiBrowserFetchEbiIdentifiers(dbSample.accession);
					// corriger dbSample et stocker pour validation et sauvegarde en base:
					if (ebiIdentifiers == null ) {
						//logger.debug(head + "aucun sample recupere a l'EBI");
						errorMessage += head + "aucun sample recupere a l'EBI\n";
						continue;
					}
					if (StringUtils.isBlank(ebiIdentifiers.getAccession()) || StringUtils.isBlank(ebiIdentifiers.getExternalId()) ) {
						errorMessage += head + "le numeros d'accession ou l'externalId n'ont pas ete recuperes a l'EBI\n";
						continue;					
					}
					if ( ! ebiIdentifiers.getAccession().startsWith("ERS")) {
						throw new SraException(head + "le sample recupere de l'EBI a un numeros d'accession qui ne commence pas par ERS" + ebiIdentifiers.getAccession());
					}
					if ( ! ebiIdentifiers.getExternalId().startsWith("SAM")) {
						throw new SraException(head + "le sample recupere de l'EBI a un externalId qui ne commence pas par SAM" + ebiIdentifiers.getExternalId());
					}	
					dbSample.externalId = ebiIdentifiers.getExternalId();
					dbSample.traceInformation.modifyUser = user;
					dbSample.traceInformation.modifyDate = courantDate;
					if( ! corrected_samples.containsKey(dbSample.code)) {
						corrected_samples.put(dbSample.code, dbSample);
					}
					
				
				}
				// le sample de la base a un SAM, il faut recuperer le sample a l'EBI et son ERS
				if (StringUtils.isNotBlank(dbSample.externalId)) {
					String head = "- Pour le sample de la base avec externalId=" + dbSample.externalId + ", ";
					
					if ( ! dbSample.externalId.startsWith("SAM") ) {
						throw new SraException(head + "l'externalId ne commence pas par SAM ");	
					}
					EbiIdentifiers ebiIdentifiers = ebiAPI.EbiBrowserFetchEbiIdentifiers(dbSample.externalId);
					// corriger dbSample et stocker pour validation et sauvegarde en base:
					if (ebiIdentifiers == null ) {
						//logger.debug(head + "aucun sample recupere a l'EBI");
						errorMessage += head + "aucun sample recupere a l'EBI\n";
						continue;
					}
					if (StringUtils.isBlank(ebiIdentifiers.getAccession()) || StringUtils.isBlank(ebiIdentifiers.getExternalId()) ) {
						errorMessage += head + "le numeros d'accession ou l'externalId n'ont pas ete recuperes a l'EBI\n";
						continue;					
					}
					if ( ! ebiIdentifiers.getAccession().startsWith("ERS")) {
						throw new SraException(head + "le sample recupere de l'EBI a un numeros d'accession qui ne commence pas par ERS" + ebiIdentifiers.getAccession());
					}
					if ( ! ebiIdentifiers.getExternalId().startsWith("SAM")) {
						throw new SraException(head + "le sample recupere de l'EBI a un externalId qui ne commence pas par SAM" + ebiIdentifiers.getExternalId());
					}	
					dbSample.accession = ebiIdentifiers.getAccession();
					dbSample.traceInformation.modifyUser = user;
					dbSample.traceInformation.modifyDate = courantDate;
					if( ! corrected_samples.containsKey(dbSample.code)) {
						corrected_samples.put(dbSample.code, dbSample);
					}
					// corriger les experiments qui referencent dbSample pour le numeros d'ac du sample 
					List<Experiment> dbExperiments = experimentAPI.dao_findAsList(DBQuery.in("sampleCode", dbSample.code));
					for (Experiment dbExperiment : dbExperiments) {
						if( ! corrected_experiments.containsKey(dbExperiment.code)) {
							corrected_experiments.put(dbExperiment.code, dbExperiment);
						}
						// correction de l'experiment directement dans le hash pour cumuler correction sampleAc et studyAc
						corrected_experiments.get(dbExperiment.code).traceInformation.modifyUser = user;
						corrected_experiments.get(dbExperiment.code).traceInformation.modifyDate = courantDate;
						corrected_experiments.get(dbExperiment.code).sampleAccession = dbSample.accession;	
						//logger.debug("pour "+ dbExperiment.code + " - nouveau numeros d'accession du sample = " + dbExperiment.sampleAccession);
					}
				}	
			}
			return errorMessage;
		}
		
		private String _completeDatabaseForExternalStudy(Submission submission, HashMap<String, AbstractStudy> corrected_studies, HashMap<String, Experiment> corrected_experiments) throws Exception  {
			Date courantDate = new Date();
			String user = "ngsrg";
			String errorMessage = "";
			//logger.debug("Entree dans _completeDatabaseForExternalStudy");

			for(String studyCode : submission.refStudyCodes) {
				AbstractStudy dbStudy = abstractStudyAPI.get(studyCode);
				if(dbStudy instanceof Study) {
					continue;
				}
				// on ne travaille que sur les externalStudy pour qui il manque un identifiant :
				if (StringUtils.isNotBlank(dbStudy.accession) && StringUtils.isNotBlank(dbStudy.externalId) ) {
					// inutile d'interroger l'EBI, on a les 2 identifiants
					continue;
				}				
				if (StringUtils.isBlank(dbStudy.accession) && StringUtils.isBlank(dbStudy.externalId) ) {
					throw new SraException("Le study de la base " + dbStudy.code + " ne contient ni AC ni externalId");
				}
				// le study de la base a un ERP, il faut recuperer le study à l'EBI et son PRJ
				if (StringUtils.isNotBlank(dbStudy.accession)) {	
					String head = "- Pour le study de la base avec accession=" + dbStudy.accession + ", ";
					if ( ! dbStudy.accession.startsWith("ERP") ) {
						throw new SraException(head + "le numeros d'accession ne commence pas par ERP");	
					}
					// on recupere à l'EBI le xml d'un study et les identifiers associés:
					EbiIdentifiers ebiIdentifiers = ebiAPI.EbiBrowserFetchEbiIdentifiers(dbStudy.accession);
					// corriger dbStudy et stocker pour validation et sauvegarde en base:
					if (ebiIdentifiers == null ) {
						//logger.debug(head + "aucun study recupere a l'EBI");
						errorMessage += head + "aucun study recupere a l'EBI\n";
						continue;
					}
					if (StringUtils.isBlank(ebiIdentifiers.getAccession()) || StringUtils.isBlank(ebiIdentifiers.getExternalId()) ) {
						errorMessage += head + "le numeros d'accession ou l'externalId n'ont pas ete recuperes a l'EBI\n";
						continue;					
					}
					// dans le study.xml, l'accession doit etre de type ERP et l'externalId de type PRJ
					if ( ! ebiIdentifiers.getAccession().startsWith("ERP")) {
						throw new SraException(head + "le study recupere a l'EBI a un numeros d'accession qui ne commence pas par ERP");
					}
					if ( ! ebiIdentifiers.getExternalId().startsWith("PRJ")) {
						throw new SraException(head + "le study recupere a l'EBI a un externalId qui ne commence pas par PRJ");
					}	
					dbStudy.externalId = ebiIdentifiers.getExternalId();
					dbStudy.traceInformation.modifyUser = user;
					dbStudy.traceInformation.modifyDate = courantDate;
				
					if( ! corrected_studies.containsKey(dbStudy.code)) {
						corrected_studies.put(dbStudy.code, dbStudy);
					}
				}
				// le study de la base a un PRJEB, il faut recuperer le project à l'EBI et son ERP
				if (StringUtils.isNotBlank(dbStudy.externalId)) {
					String head = "- Pour le study de la base avec externalId=" + dbStudy.externalId + ", ";

					if ( ! dbStudy.externalId.startsWith("PRJ") ) {
						throw new SraException(head + "l'externalId ne commence pas par PRJ");	
					}
					// on recupere à l'EBI le xml d'un project et les identifiers associés:
					EbiIdentifiers ebiIdentifiers = ebiAPI.EbiBrowserFetchEbiIdentifiers(dbStudy.externalId);
					// corriger dbStudy et stocker pour validation et sauvegarde en base:
					if (ebiIdentifiers == null ) {
						//logger.debug(head + "aucun study recupere a l'EBI");
						errorMessage += head + "aucun study recupere a l'EBI\n";
						continue;
					}					
					if (StringUtils.isBlank(ebiIdentifiers.getAccession()) || StringUtils.isBlank(ebiIdentifiers.getExternalId()) ) {
						errorMessage += head + "le numeros d'accession ou l'externalId n'ont pas ete recuperes a l'EBI\n";
						continue;					
					}
					// attention ici, dans le project.xml, l'accession doit etre de type PRJ et l'externalId de type ERP
					if ( ! ebiIdentifiers.getAccession().startsWith("PRJ")) {
						throw new SraException(head + "le project recupere a l'EBI a un numeros d'accession qui ne commence pas par PRJ");
					}
					if ( ! ebiIdentifiers.getExternalId().startsWith("ERP")) {
						throw new SraException(head + "le project recupere a l'EBI a un externalId qui ne commence pas par ERP");
					}	
					dbStudy.accession = ebiIdentifiers.getExternalId(); 
					dbStudy.traceInformation.modifyUser = user;
					dbStudy.traceInformation.modifyDate = courantDate;
					if( ! corrected_studies.containsKey(dbStudy.code)) {
						corrected_studies.put(dbStudy.code, dbStudy);
					}
					// corriger les experiments qui referencent dbStudy pour le numeros d'ac du study 
					List<Experiment> dbExperiments = experimentAPI.dao_findAsList(DBQuery.in("studyCode", dbStudy.code));
					for (Experiment dbExperiment : dbExperiments) {
						//logger.debug("pour "+ dbExperiment.code + " - ancien numeros d'accession du study = " + dbExperiment.studyAccession);
						if( ! corrected_experiments.containsKey(dbExperiment.code)) {
							corrected_experiments.put(dbExperiment.code, dbExperiment);
						}
						corrected_experiments.get(dbExperiment.code).traceInformation.modifyUser = user;
						corrected_experiments.get(dbExperiment.code).traceInformation.modifyDate = courantDate;
						corrected_experiments.get(dbExperiment.code).studyAccession = dbStudy.accession;				
						//logger.debug("pour "+ dbExperiment.code + " - nouveau numeros d'accession du study = " + corrected_experiments.get(dbExperiment.code).studyAccession);	
					}	
				}			
			}	
			return errorMessage;
		}
		
		/**
		 * Met à jour la base de données pour les ExternalSamples et ExternalStudy referencés dans la soumission 
		 * et met à jour tous les experiment de la base (et pas seulement de la soumission) qui referencent ces ExternalSamples et ExternalStudy.
		 * Ajoute une erreur dans le contexte de validation si une erreur survient.
		 * @param  submissionCode     code de la soumission pour laquelle on 
		 * @throws SraException       error
		 */
	public void completeDatabaseForExternalSampleAndStudy(String submissionCode) throws SraException {
		//logger.debug("Entree dans completeBaseForExternalSampleAndStudy");
		Submission submission = submissionAPI.get(submissionCode);
		if(submission == null) {
			throw new SraException("La soumission indiquée " +  submissionCode + " n'existe pas dans la base");
		}
		String user = "ngsrg";
		ContextValidation contextValidation = ContextValidation.createUpdateContext(user);
		
		HashMap<String, AbstractSample> samples_to_save = new HashMap<String, AbstractSample>();
		HashMap<String, AbstractStudy> studies_to_save = new HashMap<String, AbstractStudy>();
		HashMap<String, Experiment> experiments_to_save = new HashMap<String, Experiment>();
		
		String errorMessage = "";
		String headErrorMessage = "completeDatabaseForExternalSampleAndStudy : Detection d'erreurs pour la soumission " + submissionCode + "\n";
		try {
			errorMessage = _completeDatabaseForExternalSample(submission, samples_to_save, experiments_to_save);
			errorMessage += _completeDatabaseForExternalStudy(submission, studies_to_save, experiments_to_save);
		} catch (Exception e) {
			throw new SraException("completeDatabaseForExternalSampleAndStudy:: " + headErrorMessage + e.getMessage());
		}
		
		// Envoyer message d'erreur si probleme lors de la recuperation des identifiants à l'EBI :
		if(StringUtils.isNotBlank(errorMessage)) {
			throw new SraException("completeDatabaseForExternalSampleAndStudy:: " + headErrorMessage + errorMessage);	
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
		for (Iterator<Entry<String, Experiment>> iterator = experiments_to_save.entrySet().iterator(); iterator.hasNext();) {
		  Entry<String, Experiment> entry = iterator.next();
		  entry.getValue().validate(contextValidation);
		}
		
		if (contextValidation.hasErrors()) {
			throw new SraValidationException(contextValidation);
		} 
		// Envoyer message d'erreur si probleme de doublons d'ExternalSample ou d'ExternalStudy :
		if(StringUtils.isNotBlank(errorMessage)) {
			throw new SraException("completeDatabaseForExternalSampleAndStudy:: " + headErrorMessage + errorMessage);	
		}
		// sauvegarde dans base des samples, study et experiments corrigés :
		Date courantDate = new Date();
		for (Iterator<Entry<String, AbstractSample>> iterator = samples_to_save.entrySet().iterator(); iterator.hasNext();) {
			Entry<String, AbstractSample> entry = iterator.next();
			if ( StringUtils.isNotBlank(entry.getValue().accession) && StringUtils.isNotBlank(entry.getValue().externalId) ) {
				abstractSampleAPI.dao_update(DBQuery.is("code", entry.getValue().code),
					DBUpdate.set("accession", entry.getValue().accession)
							.set("externalId", entry.getValue().externalId)
							.set("traceInformation.modifyUser", user)
							.set("traceInformation.modifyDate", courantDate)); 
					//logger.debug("sauvegarde dans la base du sample " + entry.getValue().code);
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
			}
		}					
		for (Iterator<Entry<String, Experiment>> iterator = experiments_to_save.entrySet().iterator(); iterator.hasNext();) {
			Entry<String, Experiment> entry = iterator.next();
			if ( StringUtils.isNotBlank(entry.getValue().studyAccession) && StringUtils.isNotBlank(entry.getValue().sampleAccession) ) {
				experimentAPI.dao_update(DBQuery.is("code", entry.getValue().code),
					DBUpdate.set("studyAccession", entry.getValue().studyAccession)
							.set("sampleAccession", entry.getValue().sampleAccession)
							.set("traceInformation.modifyUser", user)
							.set("traceInformation.modifyDate", courantDate)); 
					//logger.debug("sauvegarde dans la base de l'experiment " + entry.getValue().code);
			}
		}
		
		//logger.debug("Fin du traitement");
	}
		
		
	
}
