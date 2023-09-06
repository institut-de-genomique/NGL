package services;

import java.io.File;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import fr.cea.ig.ngl.dao.api.sra.AnalysisAPI;
import fr.cea.ig.ngl.dao.api.sra.ExperimentAPI;
import fr.cea.ig.ngl.dao.api.sra.SubmissionAPI;
import fr.cea.ig.ngl.dao.projects.ProjectsAPI;
import fr.cea.ig.ngl.dao.readsets.ReadSetsAPI;
import models.laboratory.project.instance.Project;
import models.laboratory.run.instance.ReadSet;
import models.sra.submit.sra.instance.Analysis;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.sra.instance.RawData;
import models.sra.submit.sra.instance.Submission;
import models.sra.submit.util.SraException;
import models.sra.submit.util.SraValidationException;
import validation.ContextValidation;

public class ActivateServices {
	
	private static final play.Logger.ALogger logger = play.Logger.of(ActivateServices.class);
	private final ExperimentAPI experimentAPI;
	private final SubmissionAPI submissionAPI;
	private final ReadSetsAPI   readsetsAPI;	
	private final ProjectsAPI   projectAPI;
	private final AnalysisAPI   analysisAPI;

	@Inject
	public ActivateServices(ExperimentAPI     experimentAPI,
							SubmissionAPI     submissionAPI,
							ReadSetsAPI       readsetsAPI,
							ProjectsAPI       projectAPI,
							AnalysisAPI       analysisAPI) {
		
		this.experimentAPI     = experimentAPI;
		this.submissionAPI     = submissionAPI;
		this.readsetsAPI       = readsetsAPI;   
		this.projectAPI        = projectAPI;
		this.analysisAPI       = analysisAPI;
    }
	
	

	/**
	 * Active la soumission primaire de données si possible et met à jour la soumission 
	 * dans la base pour directorySoumission (pas de changement d'etat)
	 * @param contextValidation : Ne doit pas contenir d'erreur si activation reussie
	 * @param submission submission
	 * @throws SraValidationException SRA exception
	 */
	public void activationPrimarySubmission(ContextValidation contextValidation, Submission submission) throws SraValidationException {
		
		activatePrimarySubmission(contextValidation, submission);
		if (contextValidation.hasErrors()) {
			//logger.debug("Dans activationPrimarySubmission, contextValidation avec erreur");
			throw new SraValidationException((contextValidation));
		}
	}

	/**
	 * Active une première soumission de données : 
	 * cree le repertoire de soumission et fait les liens sur les données brutes si données zippées 
	 * et sauve la soumission dans base pour champs directorySubmission
	 * @param contextValidation validation context
	 * @param submission        submission
	 */
	public void activatePrimarySubmission(ContextValidation contextValidation, Submission submission) {
		//logger.debug("Dans activatePrimarySubmission");
		// met les rawData à jour par rapport au fileSystem:		
		checkForActivatePrimarySubmission(contextValidation, submission);
		//logger.debug("Dans activatePrimarySubmission, en sortie de checkForActivate");

		if (contextValidation.hasErrors()) {
			throw new SraValidationException((contextValidation));
		}
		// creer repertoire de soumission sur disque et faire liens sur données brutes
		try {
			// creation repertoire de soumission :
			//logger.debug("appel a createDirSubmission");
			submission.createDirSubmission();
			// creation liens donnees brutes vers repertoire de soumission :
			for (String experimentCode: submission.experimentCodes) {
				Experiment expElt =  experimentAPI.get(experimentCode);
				ReadSet readSet = readsetsAPI.get(expElt.readSetCode);
				System.out.println("exp = "+ expElt.code);
				//logger.debug("exp = "+ expElt.code);

				for (RawData rawData :expElt.run.listRawData) {
					// On ne cree les liens dans repertoire de soumission vers rep des projets que si la 
					// donnée est au CNS et si elle n'est pas à zipper
					if ("CNS".equalsIgnoreCase(rawData.location) && ! rawData.gzipForSubmission) {
						File fileCible = new File(rawData.directory + File.separator + rawData.relatifName);
						//logger.debug("run = "+ expElt.run.code);
						File fileLien = new File(submission.submissionDirectory + File.separator + rawData.relatifName);
						if(fileLien.exists()){
							fileLien.delete();
						}
						//logger.debug("fileCible = " + fileCible);
						//logger.debug("fileLien = " + fileLien);
						Path lien = Paths.get(fileLien.getPath());
						Path cible = Paths.get(fileCible.getPath());
						Files.createSymbolicLink(lien, cible);
						//logger.debug("Lien symbolique avec :  lien= " + lien + " et  cible=" + cible);
					} else {
						//logger.debug("Donnée "+ rawData.relatifName + " localisée au " + rawData.location);
					}
				}
			}
			if (StringUtils.isNotBlank(submission.analysisCode)) {
				if( ! analysisAPI.dao_checkObjectExist("code", submission.analysisCode)) {
					throw new SraException("activatePrimarySubmission", "l'analyse avec le code "+ submission.analysisCode + " n'existe pas dans la base");
				}
				Analysis analysis = analysisAPI.dao_getObject(submission.analysisCode);
				for (RawData rawData : analysis.listRawData) {
					// On ne cree les liens dans repertoire de soumission vers rep des projets que si la 
					// donnée est au CNS et si elle n'est pas à zipper
					if ("CNS".equalsIgnoreCase(rawData.location) && ! rawData.gzipForSubmission) {
						File fileCible = new File(rawData.directory + File.separator + rawData.relatifName);
						File fileLien = new File(submission.submissionDirectory + File.separator + rawData.relatifName);
						if(fileLien.exists()){
							fileLien.delete();
						}
						//logger.debug("fileCible = " + fileCible);
						//logger.debug("fileLien = " + fileLien);
						Path lien = Paths.get(fileLien.getPath());
						Path cible = Paths.get(fileCible.getPath());
						Files.createSymbolicLink(lien, cible);
						//logger.debug("Lien symbolique avec :  lien= " + lien + " et  cible=" + cible);
					} else {
						//logger.debug("Donnée "+ rawData.relatifName + " localisée au " + rawData.location);
					}
				}
			}
		} catch (SraException e) {
			contextValidation.addError("submission", e.getMessage());
		} catch (SecurityException e) {
			contextValidation.addError("Dans activatePrimarySubmission, pb SecurityException: ", e.getMessage());
		} catch (UnsupportedOperationException e) {
			contextValidation.addError(" Dans activatePrimarySubmission, pb UnsupportedOperationException: ", e.getMessage());
		} catch (FileAlreadyExistsException e) {
			contextValidation.addError(" Dans activatePrimarySubmission, pb FileAlreadyExistsException: ", e.getMessage());
		} catch (IOException e) {
			contextValidation.addError(" Dans activatePrimarySubmission, pb IOException: ", e.getMessage());		
		}

		if (! contextValidation.hasErrors()) {
			// updater la soumission dans la base pour le repertoire de soumission (la date de soumission sera mise à la reception des AC)
			submissionAPI.dao_update(DBQuery.is("code", submission.code),
									 DBUpdate.set("submissionDirectory", submission.submissionDirectory));
		} 
		//logger.debug("sauvegarde dans la base de " + submission.code +" avec state = " + submission.state.code);
	}

	/**
	 * Verifie que la soumission primaire de données peut etre activée, 
	 * corrige les chemins des rawData si elles existent au CNS, meme si elles sont notifiés dans ngl-bi
	 * comme presentes au CCRT. 
	 * @param contextValidation validation context
	 * @param submission        submission
	 */
	public void checkForActivatePrimarySubmission(ContextValidation contextValidation, Submission submission) {
		// verifications liens donnees brutes vers repertoire de soumission
		//logger.debug("Dans checkForActivatePrimarySubmission !!!!!!!!!!!");
		for (String experimentCode: submission.experimentCodes) {
			Experiment expElt =  experimentAPI.get(experimentCode);
			ReadSet readSet = readsetsAPI.get(expElt.readSetCode);
			Project p = projectAPI.get(readSet.projectCode);
			//logger.debug("exp = "+ expElt.code);
			contextValidation.addKeyToRootKeyName("experiment");
			contextValidation.addKeyToRootKeyName("run");
			contextValidation.addKeyToRootKeyName("rawData");

			for (RawData rawData :expElt.run.listRawData) {
				// Partie de code deportée dans activate : on met la variable location à CNS
				// si les données sont physiquement au CNS meme si elles sont aussi au CCRT
				// et on change le chemin pour remplacer /ccc/.../rawdata par /env/cns/proj/ 
				
				// cas ou le md5 devrait etre present mais n'a pas ete recupere dans ngl-bi (readSetIllumina) :
				// le logger.error permet de recevoir un mail et avec message recu faire un ticket pour les Joe
				// mais pas d'exception car on ne veut pas bloquer la soumission, on met md5sumForSubmission à true
				// Attention à conserver intitulé NGL-SUB_TICKET_JOE A CREER car il existe un filtre des mails
				if(StringUtils.isBlank(rawData.md5)) {
					rawData.md5 = "";
					rawData.md5sumForSubmission = true;
				}
				
				String cns_directory = rawData.directory;
				//logger.debug("XXXXX   1    cns_directory = " + cns_directory);
				if (rawData.directory.startsWith("/ccc/")) {
					int index = rawData.directory.indexOf("/rawdata/");
					String lotseq_dir = rawData.directory.substring(index + 9);
					cns_directory = "/env/cns/proj/" + lotseq_dir;
					//logger.debug("XXXXX    2   cns_directory = " + cns_directory);

				}
				String relatifName      = rawData.relatifName;
				String relatifNameUnzip = rawData.relatifName.replace(".gz", "");
				String relatifNameZip   = rawData.relatifName.concat(".gz");
				File fileCible      = new File(cns_directory + File.separator + relatifName);
				File fileCibleUnzip = new File(cns_directory + File.separator + relatifNameUnzip);
				File fileCibleZip   = new File(cns_directory + File.separator + relatifNameZip);

				if (fileCible.exists()) {
					//logger.debug("xxxx      le fichier fileCible " + fileCible + "existe bien");
					rawData.location = "CNS";
					rawData.directory = cns_directory;
					// donnees extention et md5 correctes dans NGL
					// informer birds des actions à faire :
					if (! rawData.relatifName.endsWith(".gz") && rawData.extention.contains("fastq")) {
						rawData.gzipForSubmission   = true;
						rawData.md5sumForSubmission = true;
						rawData.md5 = "";
					}
				} else if (fileCibleUnzip.exists() && rawData.extention.contains("fastq")) {
					//logger.debug("xxxx      le fichier fileCibleUnzip " + fileCibleUnzip + "existe bien");

					// correction relatifName, extention et md5
					rawData.location = "CNS";
					rawData.directory = cns_directory;
					rawData.relatifName = relatifNameUnzip;
					rawData.extention = "fastq";
					// informer birds des actions à faire :
					rawData.gzipForSubmission   = true;
					rawData.md5sumForSubmission = true;
					rawData.md5 = "";
				} else if (fileCibleZip.exists() && rawData.extention.contains("fastq")) {
					//logger.debug("xxxx      le fichier fileCibleZip " + fileCibleZip + "existe bien");

					rawData.location = "CNS";
					rawData.directory = cns_directory;
					// correction relatifName, extention et md5
					rawData.relatifName = relatifNameZip;
					rawData.extention = "fastq.gz";
					// informer birds des actions à faire :
					rawData.gzipForSubmission   = false;
					rawData.md5sumForSubmission = true;
					rawData.md5 = "";
				} else {
					//logger.debug("xxxxx      le fichier "+ fileCible + " n'existe pas au CNS");
					if (readSet.location!= null && "CNS".equalsIgnoreCase(readSet.location)) {
						if (!(boolean) p.properties.get("synchroProj").getValue()) {
						//if (p.archive){
							//logger.debug("error.rawData.activate.CNS.archive");
							contextValidation.addError("rawData", "error.rawData.activate.CNS.archive", rawData.directory + File.separator + rawData.relatifName, p.code, readSet.code);
						} else {
							//logger.debug("error.rawData.activate.CNS.notArchive");
							contextValidation.addError("rawData", "error.rawData.activate.CNS.notArchive", rawData.directory + File.separator + rawData.relatifName, p.code, readSet.code);
						}
					} else if (readSet.location!= null && "CCRT".equalsIgnoreCase(readSet.location)) {
						rawData.location = readSet.location;
						// informer birds pour données CCRT :
						if (! rawData.relatifName.endsWith(".gz") && rawData.extention.contains("fastq")) {
							rawData.gzipForSubmission   = true;
							rawData.md5sumForSubmission = true;
							rawData.md5 = "";
						}	
					} else {
						contextValidation.addError("rawData", "error.rawData.activate.location", readSet.location, rawData.directory + File.separator + rawData.relatifName);
					}
				}
				
				contextValidation.removeKeyFromRootKeyName("rawData");
				contextValidation.removeKeyFromRootKeyName("run");
				contextValidation.removeKeyFromRootKeyName("experiment");
			}
			// sauver dans base les rawData corrigés en accord avec fileSystem
			experimentAPI.dao_update(DBQuery.is("code", experimentCode),
						 DBUpdate.set("run.listRawData", expElt.run.listRawData));
			
		}		
	}

}
