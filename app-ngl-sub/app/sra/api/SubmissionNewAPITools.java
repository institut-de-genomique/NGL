package sra.api;

import static ngl.refactoring.state.SRASubmissionStateNames.NONE;
import static ngl.refactoring.state.SRASubmissionStateNames.SUBR_N;
import static ngl.refactoring.state.SRASubmissionStateNames.SUBR_SMD_IP;
import static ngl.refactoring.state.SRASubmissionStateNames.SUB_F;
import static ngl.refactoring.state.SRASubmissionStateNames.SUB_N;
import static ngl.refactoring.state.SRASubmissionStateNames.SUB_V;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import controllers.sra.api.AbstractSampleAPI;
import controllers.sra.api.AnalysisAPI;
import controllers.sra.api.ExperimentAPI;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.ngl.dao.readsets.ReadSetsAPI;
import fr.cea.ig.ngl.dao.readsets.ReadSetsDAO;
import fr.cea.ig.ngl.dao.sra.AbstractSampleDAO;
import fr.cea.ig.ngl.dao.sra.AbstractStudyDAO;
import fr.cea.ig.ngl.dao.sra.AnalysisDAO;
import fr.cea.ig.ngl.dao.sra.ConfigurationDAO;
import fr.cea.ig.ngl.dao.sra.ExperimentDAO;
import fr.cea.ig.ngl.dao.sra.ExternalSampleDAO;
import fr.cea.ig.ngl.dao.sra.ExternalStudyDAO;
import fr.cea.ig.ngl.dao.sra.ProjectDAO;
import fr.cea.ig.ngl.dao.sra.ReadsetDAO;
import fr.cea.ig.ngl.dao.sra.SampleDAO;
import fr.cea.ig.ngl.dao.sra.StudyDAO;
import fr.cea.ig.ngl.dao.sra.SubmissionDAO;
import models.laboratory.common.instance.State;
import models.laboratory.run.instance.ReadSet;
import models.sra.submit.sra.instance.AbstractSample;
import models.sra.submit.sra.instance.AbstractStudy;
import models.sra.submit.sra.instance.Analysis;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.sra.instance.ExternalSample;
import models.sra.submit.sra.instance.ExternalStudy;
import models.sra.submit.sra.instance.IStateReference;
import models.sra.submit.sra.instance.Project;
import models.sra.submit.sra.instance.Sample;
import models.sra.submit.sra.instance.Study;
import models.sra.submit.sra.instance.Submission;
import models.sra.submit.sra.instance.RawData;
import models.sra.submit.sra.instance.Readset;
import models.utils.InstanceConstants;
import models.utils.dao.DAOException;
import services.sra.SraException;
import services.taxonomy.TaxonomyServices;
import sra.services.SraEbiAPI;
import validation.ContextValidation;

public class SubmissionNewAPITools {
	private static final play.Logger.ALogger logger = play.Logger.of(SubmissionNewAPITools.class);

	private final SubmissionDAO         submissionDAO;
	private final AnalysisDAO           analysisDAO;
	private final ProjectDAO            projectDAO;
	private final StudyDAO              studyDAO;
	private final AbstractStudyDAO      abstractStudyDAO;
	private final ExternalStudyDAO      externalStudyDAO;
	private final SampleDAO             sampleDAO;
	private final AbstractSampleDAO     abstractSampleDAO;
	private final ExternalSampleDAO     externalSampleDAO;
	private final ExperimentDAO         experimentDAO;
	private final ReadsetDAO            readsetDAO;
	private final ConfigurationDAO      configurationDAO;
	private final ReadSetsDAO           laboReadSetDAO;
	private final ReadSetsAPI           laboReadSetAPI;
	private final AbstractSampleAPI     abstractSampleAPI;
	private final ExperimentAPI         experimentAPI;
	private final AnalysisAPI           analysisAPI;
	private final TaxonomyServices      taxonomyServices;
	private final SraEbiAPI             ebiAPI;


	@Inject
	SubmissionNewAPITools(SubmissionDAO       submissionDAO,
			SraEbiAPI           ebiAPI,
			AnalysisDAO         analysisDAO,
			ProjectDAO          projectDAO, 
			StudyDAO            studyDAO, 
			AbstractStudyDAO    abstractStudyDAO,
			ExternalStudyDAO    externalStudyDAO,
			SampleDAO           sampleDAO,
			AbstractSampleDAO   abstractSampleDAO,
			ExternalSampleDAO   externalSampleDAO,
			ExperimentDAO       experimentDAO,
			ReadsetDAO          readsetDAO,
			ConfigurationDAO    configurationDAO,
			ReadSetsDAO         laboReadSetDAO,
			ReadSetsAPI         laboReadSetAPI,
			ExperimentAPI       experimentAPI,
			AnalysisAPI         analysisAPI,
			AbstractSampleAPI   abstractSampleAPI,
			TaxonomyServices    taxonomyServices) {
		this.ebiAPI            = ebiAPI;
		this.submissionDAO     = submissionDAO;
		this.analysisDAO       = analysisDAO;
		this.projectDAO        = projectDAO;
		this.studyDAO          = studyDAO;
		this.abstractStudyDAO  = abstractStudyDAO;
		this.externalStudyDAO  = externalStudyDAO;
		this.sampleDAO         = sampleDAO;
		this.abstractSampleDAO = abstractSampleDAO;
		this.externalSampleDAO = externalSampleDAO;
		this.experimentDAO     = experimentDAO;
		this.readsetDAO        = readsetDAO;
		this.configurationDAO  = configurationDAO;
		this.laboReadSetDAO    = laboReadSetDAO;
		this.laboReadSetAPI    = laboReadSetAPI;
		this.experimentAPI     = experimentAPI;
		this.analysisAPI       = analysisAPI;
		this.abstractSampleAPI = abstractSampleAPI;
		this.taxonomyServices  = taxonomyServices;
	}
	
	
	
	// NGL-4952
	// On peut verifier ici pour chaque experiment construit et eventuellement avant sauvegarde de tout objet en base,
	// que chaque experiment pointe bien vers un sample ayant meme taxonId que le readset :
	// Il faut que les experiments de la liste soient renseignés pour le sample, cad appel de la cette methode
	// après execution de traitementSubmissionForReadset si Creation, et si Update des experiments, alors experiments
	// forcement renseignés pour sampleIdentifier et donc vérification automatique à l'EBI
	public void checkCoherenceTaxonInSampleAndReadset(List<Experiment> listExperiments, ContextValidation ctx){
		Map<String, Sample> map = new HashMap<String, Sample>();

		for (Experiment expElt : listExperiments) {
			String sampleCode       = expElt.sampleCode;
			//logger.debug("XXXXXXXXXXXXXX      exp='{}', avec sampleCode =", expElt.code , expElt.sampleCode);
			String sampleIdentifier = expElt.sampleIdentifier;
			// ignorer cas ou sample construit pas CNS et pas encore soumis, donc pas encore de sampleIdentifier sur l'exp
			if(StringUtils.isBlank(sampleIdentifier)) {
				continue;
			}
			String rsCode = expElt.readSetCode;
			AbstractSample absSample = null;
			if(StringUtils.isBlank(sampleCode)) {
				ctx.addError(expElt.code, "experiment sans sampleCode ");
				continue;
			}
			// code ci dessous à supprimer car cas update d'un experiment avec reference sur un sample du CNS,
			// il faut alors controler que le readset a meme taxonId que le sample
//			if(abstractSampleAPI.dao_checkObjectExist("code", sampleCode)) {
//				//logger.debug("KKKKKKKKKKKKKKK    le sampleCode {} existe bien", sampleCode);
//				absSample = abstractSampleAPI.get(sampleCode);
//				if( absSample._type.equals("Sample")) {
//					// faire verif ? normalement inutile car construction du sample en fonction des infos de taxonomy
//					// et on ne modifie pas le taxonId du sample à l'EBI.
//					continue;
//				}
//			}
			Sample sample = null;
			// si sample construit pas CNS et soumis, mais qui peuvent etre privés 
			// donc pas visibles à l'EBI mais bien visibles sur notre dropBox, alors prendre sample chez nous.
			if (sampleDAO.checkObjectExist(DBQuery.and(DBQuery.is("_type", "Sample"),DBQuery.or(DBQuery.is("accession", sampleIdentifier), DBQuery.is("externalId", sampleIdentifier))))) {
				if(sampleIdentifier.startsWith("ERS") || sampleIdentifier.startsWith("SRS")) {
					sample = sampleDAO.findOne(DBQuery.is("accession", sampleIdentifier)); 
				} else if(sampleIdentifier.startsWith("SAM")) {
					sample = sampleDAO.findOne(DBQuery.is("externalId", sampleIdentifier));
				}
				if(sample==null) {
					ctx.addError(rsCode, "Pas de recuperation dans NGL-SUB du sample " + sampleIdentifier);
					continue;
				}
				if( ! map.containsKey(sampleIdentifier) ) {
					map.put(sampleIdentifier, (Sample) sample);
				}
			}
			if(map.containsKey(sampleIdentifier)) {
				sample = map.get(sampleIdentifier);
			} else {
				try {
					//logger.debug("dans try     rs.='{}'", rs.code);
					sample = ebiAPI.ebiBrowserFetchSample(sampleIdentifier);
					//logger.debug("fin try     rs.='{}'", rs.code);
				} catch (SraException | IOException | ParserConfigurationException | ParseException  e) {
					//logger.debug("dans catch AAAA     rs.{}", rs.code);
					ctx.addError(rsCode, "Pas de recuperation a l'EBI du sample " + sampleIdentifier);
					continue;
				} catch (Exception e) {
					//logger.debug("dans catch BBBBB      rs.{} et displayError", rs.code);
					ctx.addError(rsCode, "Pas de recuperation a l'EBI du sample " + sampleIdentifier);
					continue;
				}
				//logger.debug("apres try catch, pour le rs "+ rs.code + ", sample recupere à l'EBI :" + sample.code);
				map.put(sampleIdentifier, (Sample) sample);
			}
			if( sample == null ) {
				// on ne devrait jamais avoir un retour de l'EBI null mais une exception si n'existe pas à l'EBI
				logger.debug("Impossible de verifier le sample '{}' pour le readset '{}'", sampleCode, expElt.readSetCode);
				ctx.addError(rsCode, "impossible de verifier le sample " + sampleCode +"(sampleIdentifier : "+ sampleIdentifier +")");
				continue;
			} 
			int    sampleTaxonId    = ((Sample) sample).taxonId;
			String sampleTaxonCode  = Integer.toString(sampleTaxonId);

			if( ! ebiAPI.submittable(sampleTaxonId)) {
				ctx.addError(rsCode, "associe au sampleCode " + sample.code + " avec taxon=" + sample.scientificName + " et taxonId=" + sampleTaxonCode + " non submittable à l'EBI");
			} 
			ReadSet readset = laboReadSetAPI.get(rsCode);
			if( ! readset.sampleOnContainer.taxonCode.equals(sampleTaxonCode) ) {
				ctx.addError(readset.code, "avec informations de taxon='" + readset.sampleOnContainer.ncbiScientificName + "' et taxonId=" + readset.sampleOnContainer.taxonCode 
						+ " differentes des information du sample " + sampleIdentifier + " avec sampleTaxon='" + sample.scientificName + "' et sampleTaxonId=" + sampleTaxonCode);
			}
			//logger.debug("le sample '{}' (taxonId='{}') et le readset '{}'(taxonId='{}' ont meme taxonCode", sampleCode, sampleTaxonCode, readset.code, readset.sampleOnContainer.taxonCode );
		}
	}
	
	// NGL-5313
	private void checkVisibiliteSample(Experiment experiment, Map<String, Sample> map, ContextValidation ctx) {
		String sampleCode       = experiment.sampleCode;
		//logger.debug("XXXXXXXXXXXXXX      exp='{}', avec sampleCode =", expElt.code , expElt.sampleCode);
		String sampleIdentifier = experiment.sampleIdentifier;
		String rsCode = experiment.readSetCode;
		
		// ignorer cas ou sample construit pas CNS et pas encore soumis, donc pas encore de sampleIdentifier sur l'exp
		if(StringUtils.isBlank(sampleIdentifier) ) {
			return;
		}
		// ignorer cas ou sample construit pas CNS et soumis, mais qui peuvent etre privés 
		// donc pas visibles à l'EBI mais bien visibles sur notre dropBox
		if (sampleDAO.checkObjectExist(DBQuery.and(DBQuery.is("_type", "Sample"),DBQuery.or(DBQuery.is("accession", sampleIdentifier), DBQuery.is("externalId", sampleIdentifier))))) {
			return;
		}
		if(StringUtils.isBlank(sampleCode) ) {
			ctx.addError(experiment.code, "experiment sans sampleCode");
			return;
		}
		Sample sample = null;
		if(map.containsKey(sampleIdentifier)) {
			sample = map.get(sampleIdentifier);
		} else {
			try {
				//logger.debug("dans try     rs.='{}'", rs.code);
				sample = ebiAPI.ebiBrowserFetchSample(sampleIdentifier);
				//logger.debug("fin try     rs.='{}'", rs.code);
			} catch (SraException | IOException | ParserConfigurationException | ParseException  e) {
				//logger.debug("dans catch AAAA     rs.{}", rs.code);
				ctx.addError(rsCode, "Pas de recuperation a l'EBI du sample " + sampleIdentifier);
				return;
			} catch (Exception e) {
				//logger.debug("dans catch BBBBB      rs.{} et displayError", rs.code);
				ctx.addError(rsCode, "Pas de recuperation a l'EBI du sample " + sampleIdentifier);
				return;
			}
			//logger.debug("apres try catch, pour le rs "+ rs.code + ", sample recupere à l'EBI :" + sample.code);
			map.put(sampleIdentifier, (Sample) sample);
		}
		if( sample == null ) {
			// on ne devrait jamais avoir un retour de l'EBI null mais une exception si n'existe pas à l'EBI
			logger.debug("Impossible de verifier le sample '{}' pour le readset '{}'", sampleCode, rsCode);
			ctx.addError(rsCode, "impossible de verifier le sample " + sampleCode +"(sampleIdentifier : "+ sampleIdentifier +")");
			return;
		} 
	}
	
	// NGL-5313
	private void checkVisibiliteStudy(Experiment experiment, Map<String, Study> map, ContextValidation ctx) {
		String studyCode       = experiment.studyCode;
		//logger.debug("XXXXXXXXXXXXXX      exp='{}', avec studyCode =", expElt.code , expElt.studyCode);
		String studyIdentifier = experiment.studyIdentifier;
		String rsCode = experiment.readSetCode;
		
		// ignorer cas ou study construit pas CNS et pas encore soumis, donc pas encore de studyIdentifier sur l'exp
		if(StringUtils.isBlank(studyIdentifier) ) {
			return;
		}
		// ignorer cas ou study construit pas CNS et soumis, mais qui peuvent etre privés 
		// donc pas visibles à l'EBI mais bien visibles sur notre dropBox
		if (studyDAO.checkObjectExist(DBQuery.and(DBQuery.is("_type", "Study"),DBQuery.or(DBQuery.is("accession", studyIdentifier), DBQuery.is("externalId", studyIdentifier))))) {
			return;
		}
		if(StringUtils.isBlank(studyCode) ) {
			ctx.addError(experiment.code, "experiment sans studyCode");
			return;
		}
		Study study = null;
		if(map.containsKey(studyIdentifier)) {
			study = map.get(studyIdentifier);
		} else {
			try {
				//logger.debug("dans try     rs.='{}'", rs.code);
				study = ebiAPI.ebiBrowserFetchStudy(studyIdentifier);
				//logger.debug("fin try     rs.='{}'", rs.code);
			} catch (SraException | IOException | ParserConfigurationException | ParseException  e) {
				//logger.debug("dans catch AAAA     rs.{}", rs.code);
				ctx.addError(rsCode, "Pas de recuperation a l'EBI du study " + studyIdentifier);
				return;
			} catch (Exception e) {
				//logger.debug("dans catch BBBBB      rs.{} et displayError", rs.code);
				ctx.addError(rsCode, "Pas de recuperation a l'EBI du study " + studyIdentifier);
				return;
			}
			//logger.debug("apres try catch, pour le rs "+ rs.code + ", study recupere à l'EBI :" + study.code);
			map.put(studyIdentifier, (Study) study);
		}
		if( study == null ) {
			// on ne devrait jamais avoir un retour de l'EBI null mais une exception si n'existe pas à l'EBI
			logger.debug("Impossible de verifier le study '{}' pour le readset '{}'", studyCode, rsCode);
			ctx.addError(rsCode, "impossible de verifier le study " + studyCode +"(sampleIdentifier : "+ studyIdentifier +")");
			return;
		} 
	}
		
	// NGL-5313
	public void checkVisibiliteSamplesAndStudy(List<Experiment> listExperiments, ContextValidation ctx){
		
		Map<String, Sample> mapSamples = new HashMap<String, Sample>();
		Map<String, Study> mapStudies = new HashMap<String, Study>();

		for (Experiment expElt : listExperiments) {
			checkVisibiliteSample(expElt, mapSamples, ctx);
			checkVisibiliteStudy(expElt, mapStudies, ctx);
		}
	}
	
	
	
	
	
	/**
	 * Met à jour le readset de type {@link ReadSet} pour la trace information, 
	 * le submissionState et son historique.
	 * @param readset               objet readset à mettre à jour  pour les champs Sumissionstate et traceInformation
	 * @param nextStateCode         objet {@link State} à installer dans le champs submissionState
	 * @param user                  user
	 */
	public static void updateLaboReadsetForSubmissionStateAndTrace(ReadSet readset, String nextStateCode, String user) {
		//		submission.getTraceInformation().forceModificationStamp(nextState.user, nextState.date);
		//		submission.setState(nextState.createHistory(submission.getState()));
		readset.traceInformation.modificationStamp(user);
		State newState = IStateReference.createStateHistorique(readset.submissionState, nextStateCode, user);
		readset.submissionState = newState;
	}

	private void updateUmbrella(Submission submission) {
		if (StringUtils.isBlank(submission.umbrellaCode)) {
			return;
		}
		Project project = projectDAO.getObject(submission.umbrellaCode);
		if (project == null) {
			String message = "Le project avec le code " + submission.umbrellaCode 
					+ " indiquee dans la submission " + submission.code 
					+ " n'existe pas dans la base";
			//logger.debug(message);
			throw new SraException("updateSubmissionChildObject", message);
		} 
		// Recuperer object project pour mettre historique des state et traceInformation à jour:
		project.updateStateAndTrace(submission.state);
		projectDAO.update(DBQuery.is("code", project.code), 
				DBUpdate.set("state", project.state)
				.set("traceInformation", project.traceInformation));
		//logger.debug("Mise à jour du project avec state.code=" + project.state.code);
	}

	private void updateStudy(Submission submission) {
		if (StringUtils.isBlank(submission.studyCode)) {
			return;	
		}
		Study study = studyDAO.getObject(submission.studyCode);
		if (study == null) {
			String message = "Le study avec le code " + submission.studyCode 
					+ " indiquee dans la submission " + submission.code 
					+ " n'existe pas dans la base";
			//logger.debug(message);
			throw new SraException("updateSubmissionChildObject", message);
		} 
		// Recuperer object study pour mettre historique des state et traceInformation à jour:
		study.updateStateAndTrace(submission.state);
		studyDAO.update(DBQuery.is("code", study.code), 
				DBUpdate.set("state", study.state)
				.set("traceInformation", study.traceInformation));
		//logger.debug("Mise à jour du study avec state.code=" + study.state.code);

	}

	private void updateSamples(Submission submission) {
		if(submission.sampleCodes==null || submission.sampleCodes.size()==0) {
			return;
		}
		// etat à propager dans samples à soumettre 
		// donc dans collection sampleCodes qui ne contient que des samples et jamais d'ExternalSample
		for (int i = 0; i < submission.sampleCodes.size() ; i++) {
			Sample sample = sampleDAO.getObject(submission.sampleCodes.get(i));
			if (sample == null) {
				String message = "Le sample avec le code " + submission.sampleCodes.get(i) 
				+ " indiquee dans la submission " + submission.code 
				+ " n'existe pas dans la base";
				//logger.debug(message);
				throw new SraException("updateSubmissionChildObject", message);
			}
			//sample.setState(submission.state);
			sample.updateStateAndTrace(submission.state);
			sampleDAO.update(DBQuery.is("code", sample.code),					
					DBUpdate.set("state", sample.state)
					.set("traceInformation", sample.traceInformation));
		}
	}

	private void updateExperimentsAndReadsets(Submission submission) {
		if(submission.experimentCodes==null || submission.experimentCodes.size()==0) {
			return;
		}
		// etat à propager dans experiments à soumettre et readset ssi soumission de type CREATION: 
		for (int i = 0; i < submission.experimentCodes.size() ; i++) {
			Experiment experiment = experimentDAO.getObject(submission.experimentCodes.get(i));
			if (experiment == null) {
				continue;
			} 
			experiment.updateStateAndTrace(submission.state);
			experimentDAO.update(DBQuery.is("code", experiment.code),					
					DBUpdate.set("state", experiment.state)
					.set("traceInformation", experiment.traceInformation));

			// mettre à jour la collection readset de ngl-sub :
			MongoDBDAO.update(InstanceConstants.SRA_READSET_COLL_NAME, Readset.class, 
					DBQuery.is("experimentCode", experiment.code),
					DBUpdate
					.set("state", experiment.state)
					.set("traceInformation", experiment.traceInformation));
			// mettre à jour la collection ReadSet de ngl-bi :
			ReadSet laboreadset = laboReadSetDAO.getObject(experiment.readSetCode);
			if (laboreadset == null)  {
				if (experiment.typePlatform.equalsIgnoreCase("ls454")) {
					// Pas de sauvegarde des etats dans la collection labo.readSet 
					// car le readset 454 n'existe pas dans cette collection.
					// Pas de sauvegarde des etats dans la collection sra.readSet car pas de state	
				} else if (experiment.typePlatform.equalsIgnoreCase("oxford_nanopore")) {
					String message = "Le readset de type oxford nanopore " + experiment.readSetCode
							+ " n'existe pas dans la base";
					throw new SraException(message);
				} else if (experiment.typePlatform.equalsIgnoreCase("illumina")) {
					String message = "Le readset de type oxford nanopore " + experiment.readSetCode
							+ " n'existe pas dans la base";
					throw new SraException(message);
				} else if (experiment.typePlatform.equalsIgnoreCase("dnbseq")) {
					String message = "Le readset de type mgi " + experiment.readSetCode
							+ " n'existe pas dans la base";
					throw new SraException(message);
				} else {
					String message = "readset de type " + experiment.typePlatform
							+ " non prevu par le code";
					throw new SraException(message);
				}
			} else {					
				updateLaboReadsetForSubmissionStateAndTrace(laboreadset, submission.state.code, submission.state.user);
				laboReadSetDAO.update(DBQuery.is("code", laboreadset.code),					
						DBUpdate.set("submissionState", laboreadset.submissionState)
						.set("traceInformation.modifyDate", laboreadset.traceInformation.modifyDate)
						.set("traceInformation.modifyUser", laboreadset.traceInformation.modifyUser));					
				// pas de sauvegarde des etats dans la collection sra.readSet car pas de state
			}
		}
	}

	
	private void updateExperimentsAlone(Submission submission) {
		if(submission.experimentCodes==null || submission.experimentCodes.size()==0) {
			return;
		}
		// etat à propager dans experiments sans propager aux readset si soumission de type UPDATE 	
		for (int i = 0; i < submission.experimentCodes.size() ; i++) {
			Experiment experiment = experimentDAO.getObject(submission.experimentCodes.get(i));
			if (experiment == null) {
				continue;
			} 
			experiment.updateStateAndTrace(submission.state);
			experimentDAO.update(DBQuery.is("code", experiment.code),					
					DBUpdate.set("state", experiment.state)
					.set("traceInformation", experiment.traceInformation));
			// mettre à jour la collection readset de ngl-sub :
			MongoDBDAO.update(InstanceConstants.SRA_READSET_COLL_NAME, Readset.class, 
					DBQuery.is("experimentCode", experiment.code),
					DBUpdate
					.set("state", experiment.state)
					.set("traceInformation", experiment.traceInformation));
		}
	}


	private void updateExperiments(Submission submission) {
		switch(submission.type) {
			case CREATION : {
				updateExperimentsAndReadsets(submission);
				break;
			}
			case UPDATE : {
				updateExperimentsAlone(submission);
				break;
			} 
			case RELEASE : {
				if(submission.experimentCodes != null && submission.experimentCodes.size() > 0) {	
					throw new SraException("updateSubmissionChildObject", "soumission de type RELEASE avec experimentCodes renseignés pour la soumission " + submission.code);
				}
				break;
			}
		}
	}

	private void updateAnalysisAndRawData(Submission submission) {
		if (StringUtils.isBlank(submission.analysisCode)) {
			//throw new SraException("appel de updateAnalysisAndRawData avec submission.analysisCode null pour la soumission " + submission.code);	
			return;
		}
		switch(submission.type) {
			case CREATION : {
					Analysis analysis = analysisDAO.getObject(submission.analysisCode);
					if (analysis == null) {
						String message = "L'analysis avec le code " + submission.analysisCode 
							+ " indiquee dans la submission " + submission.code 
							+ " n'existe pas dans la base";
						//logger.debug(message);
						throw new SraException("updateSubmissionChildObject", message);
					} 
					// Recuperer object analysis pour mettre historique des state et traceInformation à jour:
					analysis.updateStateAndTrace(submission.state);
					analysisDAO.update(DBQuery.is("code", analysis.code), 
						DBUpdate.set("state", analysis.state)
						.set("traceInformation", analysis.traceInformation));
					//logger.debug("Mise à jour du analysis avec state.code=" + analysis.state.code);

					// etat à propager dans rawData de analysis à soumettre et readset: 
					for (RawData rawData: analysis.listRawData) {
						if(StringUtils.isNotBlank(rawData.readsetCode)) {
							ReadSet laboreadset = laboReadSetDAO.findByCode(rawData.readsetCode);
							if (laboreadset == null)  {
								String message = "Le readset de type bionano " + rawData.readsetCode
									+ " n'existe pas dans la base";
								throw new SraException(message);
							} 					
							updateLaboReadsetForSubmissionStateAndTrace(laboreadset, submission.state.code, submission.state.user);
							laboReadSetDAO.update(DBQuery.is("code", laboreadset.code),					
								DBUpdate.set("submissionState", laboreadset.submissionState)
								.set("traceInformation.modifyDate", laboreadset.traceInformation.modifyDate)
								.set("traceInformation.modifyUser", laboreadset.traceInformation.modifyUser));					
							// pas de sauvegarde des etats dans la collection sra.readSet car pas de state
						}
					}
					// mettre à jour la collection readset de ngl-sub :
					MongoDBDAO.update(InstanceConstants.SRA_READSET_COLL_NAME, Readset.class, 
							DBQuery.is("analysisCode", analysis.code),
							DBUpdate
							.set("state", analysis.state)
							.set("traceInformation", analysis.traceInformation));
				
				break;
			}
			case UPDATE : case RELEASE : {
				throw new SraException("updateAnalysisAndRawData", "cas non implemente : soumission de type UPDATE ou RELEASE avec analysis renseigne pour la soumission " + submission.code);
			} 
		}
	}
	
	
	/**
	 * Cascade le traceInformation et le state de la soumission à ses sous-objets et aux readsets 
	 * dans database. Declenche une erreur si la cascade n'a pas pu etre réalisée.
	 * @param  submission   submission
	 * @throws SraException error
	 */
	public void updateSubmissionChildObject(Submission submission) throws SraException {
		updateUmbrella(submission);
		updateStudy(submission);
		updateSamples(submission);
		updateExperiments(submission);
		updateAnalysisAndRawData(submission);
	}



	public void rollbackSubmission(Submission submission, String user) throws SraException{
		//logger.debug("Entree dans rallback");
		switch(submission.type) {
		case  UPDATE :
			throw new SraException("Deletion impossible pour la soumission " + submission.code + " de type CREATION");
		case RELEASE :
			if (submission.state.code.equalsIgnoreCase(SUBR_N) || (submission.state.code.equalsIgnoreCase(SUBR_SMD_IP))) {
				// detruire la soumission :
				submissionDAO.deleteByCode(submission.code);
				// remettre le status du study avec un status F-SUB
				// La date de release du study est modifié seulement si retour positif de l'EBI pour release donc si status F-SUB
				// study qui ne peut etre external si release.
				studyDAO.update(DBQuery.is("code", submission.studyCode),
						DBUpdate.set("state.code", SUB_F)
						.set("traceInformation.modifyUser", user)
						.set("traceInformation.modifyDate", new Date()));
			}
			break;
		case CREATION:
			//logger.debug("demande rallbackCreationSubmission");
			rallbackCreationSubmission(submission, user);
			break;
		default:
			//throw new SubmissionTypeException(submission);
			throw new SraException("Type de soumission non gerée :" + submission.type 
					+ " pour la soumission " + submission.code);
		}
	}

	/**
	 * Ne pas enlever des soumissions connus de l'EBI car perturbe releaseDate des objets.
	 * @param submission     soumission
	 * @param user           utilisateur
	 * @throws SraException  erreur
	 */
	public void rallbackCreationSubmission(Submission submission, String user) throws SraException{
		//logger.debug("Entrée dans rallbackCreationSubmission");
		// Si la soumission est connu de l'EBI on ne pourra pas l'enlever de la base :
		if (StringUtils.isNotBlank(submission.accession)){
			//logger.debug("objet submission avec AC : submissionCode = "+ submission.code + " et submissionAC = "+ submission.accession);
			throw new SraException("Deletion impossible pour la soumission " + submission.code + " avec un AC :" + submission.accession);
		} 
		if ( submission.type != Submission.Type.CREATION) {
			throw new SraException("Deletion impossible pour la soumission " + submission.code + " avec type '" + submission.type + "'");
		}
		// Si la soumission concerne une soumission primaire de données avec status != N ou != V:
		if ( !submission.state.code.equalsIgnoreCase(SUB_N) && !submission.state.code.equalsIgnoreCase(SUB_V)) {
			throw new SraException("Deletion impossible pour la soumission " + submission.code + " avec etat " + submission.state.code + " != SUB-N ou SUB-V");
		}
		
		if (! submission.experimentCodes.isEmpty()) {
			for (String experimentCode : submission.experimentCodes) {
				Experiment experiment = experimentDAO.findByCode(experimentCode);
				if (! SUB_N.equals(experiment.state.code) && ! SUB_V.equals(experiment.state.code)) {
					throw new SraException("Deletion impossible pour la soumission " + submission.code + " avec experiment " + experiment.code + " dans etat != SUB-N ou SUB-V");
				}
			}
		}
		// Si la soumission concerne une soumission primaire de données avec status = N :
		if (! submission.experimentCodes.isEmpty()) {
			for (String experimentCode : submission.experimentCodes) {
				// verifier que l'experiment n'est pas utilisé par autre objet submission avant destruction
				Experiment experiment = experimentDAO.findByCode(experimentCode);
				// mettre le status pour la soumission des readSet à NONE si possible: 
				if (experiment != null){
					// remettre les readSet dans la base avec submissionState à "NONE":
					//						//System.out.println("submissionState.code remis à 'N' pour le readSet "+experiment.readSetCode);

					laboReadSetDAO.update(DBQuery.is("code", experiment.readSetCode),
							DBUpdate.set("submissionState.code", NONE)
							.set("submissionUser", null)
							.set("submissionDate", null)
							.set("traceInformation.modifyUser", user)
							.set("traceInformation.modifyDate", new Date()));

					// Enlever les readsets de la collection sra :
					readsetDAO.deleteByCode(experiment.readSetCode);

					List <Submission> submissionList = submissionDAO.dao_find(DBQuery.in("experimentCodes", experimentCode)).toList();
					if (submissionList.size() > 1) {
						for (Submission sub: submissionList) {
							//								System.out.println(experimentCode + " utilise par objet Submission " + sub.code);
							//logger.debug(experimentCode + " utilise par objet Submission " + sub.code);
						}
					} else {
						// verifier qu'on ne detruit que des experiments avec un status N ou V_SUB
						if (SUB_N.equals(experiment.state.code) || SUB_V.equals(experiment.state.code)){
							//								MongoDBDAO.deleteByCode(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, models.sra.submit.sra.instance.Experiment.class, experimentCode);
							try {
								experimentDAO.deleteByCode(experimentCode);
							} catch (DAOException e) {
								logger.debug(e.getMessage(), e);
								throw new SraException(e.getMessage(), e);
							}
						} else {
							//logger.debug(experimentCode + " non delété dans base car status = " + experiment.state.code);
						}
					}
				}
			}
		}
		if (StringUtils.isNotBlank(submission.analysisCode)) {
			//logger.debug("dans rallbackCreationSubmission : analysisCode ok");
			// Recuperer list rawData du study et remettre statut des readset ) NONE :
			Analysis analysis = analysisDAO.findByCode(submission.analysisCode);
			// mettre le status pour la soumission des readSet à NONE si possible: 
			if (analysis != null){
				//logger.debug("yyyyyyyyyyyyyyyy    dans rallbackCreationSubmission : analysis  ok");
				//logger.debug("dans rallbackCreationSubmission , listRawData.size() = " + analysis.listRawData.size());
				for (RawData rawData : analysis.listRawData) {

					//logger.debug("rawData.collabFileName="+ rawData.collabFileName );
					if (StringUtils.isBlank(rawData.readsetCode)) {
						continue;
					}

					// remettre les readSet dans la base avec submissionState à "NONE":
					//logger.debug("demande de mise a jour dans collection illumina du state de "+ rawData.readsetCode);
					laboReadSetDAO.update(DBQuery.is("code", rawData.readsetCode),
							DBUpdate.set("submissionState.code", NONE)
							.set("submissionUser", null)
							.set("submissionDate", null)
							.set("traceInformation.modifyUser", user)
							.set("traceInformation.modifyDate", new Date()));

					// Enlever les readsets de la collection sra :
					//logger.debug("demande de deletion dans ngl-sub.readset de "+ rawData.readsetCode);

					readsetDAO.deleteByCode(rawData.readsetCode);
				}	


				//logger.debug("yyyyyyyyyyyyyyyyy   update de analyse dans rallbackCreationSubmission");

				//				analysisDAO.update(DBQuery.is("code", submission.analysisCode),
				//					DBUpdate.set("state.code", NONE)
				//					.set("listRawData", null)
				//					.set("traceInformation.modifyUser", user)
				//					.set("traceInformation.modifyDate", new Date()));
				//				


				analysis.listRawData = null;
				analysis.traceInformation.modifyUser = user;
				analysis.traceInformation.modifyDate = new Date();
				analysis.state.code = NONE;

				analysisDAO.update(analysis);

				//logger.debug("yyyyyyyyyyyyyyyyyyyyyyyyyyy 2222    state.code remis à '\"+NONE+\"' pour analysis "+ submission.analysisCode);	

			}	
		}
		if (! submission.refSampleCodes.isEmpty()) {	
			for (String sampleCode : submission.refSampleCodes){
				// verifier que sample n'est pas utilisé par autre objet submission avant destruction
				// normalement sample crees dans init de type external avec state=F-SUB ou sample avec state='N'
				AbstractSample abstSample = abstractSampleDAO.getObject(sampleCode);

				//					List <Submission> submissionList = MongoDBDAO.find(InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class, DBQuery.in("refSampleCodes", sampleCode)).toList();
				List <Submission> submissionList = submissionDAO.dao_find(DBQuery.in("refSampleCodes", sampleCode)).toList();
				if (submissionList.size() > 1) {
					for (Submission sub: submissionList) {
						//logger.debug(sampleCode + " utilise par objet Submission " + sub.code);
					}
				} else {
					//						abstractSampleAPI.delete(sampleCode);

					// verifier qu'on ne detruit que des samples avec un status N ou bien des samples externes:
					if (abstSample instanceof ExternalSample) {
						//logger.debug("C'est bien un externalSample pour " + sampleCode);
						try {
							externalSampleDAO.deleteByCode(sampleCode);
						} catch (DAOException e) {
							logger.debug(e.getMessage(), e);
							throw new SraException(e.getMessage(), e);
						}
					} else {
						Sample sample = (Sample) abstSample; 
						if (sample == null) {
							continue;
						}
						if (SUB_N.equals(sample.state.code) || SUB_V.equals(sample.state.code) ) {
							try {
								sampleDAO.deleteByCode(sampleCode);
							} catch (DAOException e) {
								logger.debug(e.getMessage(), e);
								throw new SraException(e.getMessage(), e);
							}
						} else {
							//logger.debug("Pas de deletion dans base du sample interne " + sampleCode + "avec statut= "+sample.state.code);
						}
					}
				}
			}
		}

		// verifier que la config à l'etat used n'est pas utilisé par une autre soumission avant de remettre son etat à "NONE"
		// update la configuration pour le statut en remettant le statut new si pas utilisé par ailleurs :
		List <Submission> submissionList = submissionDAO.dao_find(DBQuery.in("configCode", submission.configCode)).toList();
		if (submissionList.size() <= 1) {
			configurationDAO.update(DBQuery.is("code", submission.configCode), 
					DBUpdate.set("state.code", NONE)
					.set("traceInformation.modifyUser", user)
					.set("traceInformation.modifyDate", new Date()));
			//logger.debug("state.code remis à '" + NONE + "' pour configuration " + submission.configCode);
		}

		// verifier que le study à l'etat N n'est pas utilisé par une autre soumission avant de remettre son etat à "NONE"
		if (StringUtils.isNotBlank(submission.studyCode)){
			List <Submission> submissionList2 = submissionDAO.dao_find(DBQuery.in("studyCode", submission.studyCode)).toList();
			if (! (submissionList2.size() > 1)) {
				studyDAO.update(DBQuery.is("code", submission.studyCode).in("state.code", SUB_N, SUB_V),
						DBUpdate.set("state.code", NONE)
						.set("traceInformation.modifyUser", user)
						.set("traceInformation.modifyDate", new Date()));	
				//logger.debug("state.code remis à '\"+NONE+\"' pour study "+submission.studyCode);
			}	
		}

		// verifier que le project umbrella à l'etat N ou V n'est pas utilisé par une autre soumission avant de le deleter
		if (StringUtils.isNotBlank(submission.umbrellaCode)){
			//logger.debug("demande deletion pour project umbrella " + submission.umbrellaCode);
			List <Submission> submissionList2 = submissionDAO.dao_find(DBQuery.in("umbrellaCode", submission.umbrellaCode)).toList();
			if (! (submissionList2.size()  > 1)) {
				Project project = projectDAO.findByCode(submission.umbrellaCode);
				//logger.debug("project umbrella bien recupere dans base");
				if ("UMBRELLA_PROJECT".equalsIgnoreCase(project.submissionProjectType)) {
					//logger.debug("project umbrella de type umbrella");
					// on autorise suppression y compris si umbrella à etat NONE car creation possible uniquement via interface create de project qui 
					// On n'autorise pas l'utilisateur à creer des project umbrella sans les soumettre aussitot (pas de project umbrella à NONE ou SUB-N ou SUB-V si pas de 
					// soumission lancée
					if (NONE.equalsIgnoreCase(project.state.code) || SUB_N.equalsIgnoreCase(project.state.code) || SUB_V.equalsIgnoreCase(project.state.code) ) {
						//logger.debug("project umbrella dans etat autorisant suppression");
						try {
							projectDAO.deleteByCode(submission.umbrellaCode);
							//logger.debug("deletion dans base pour umbrellaProject " + submission.umbrellaCode);
						} catch (DAOException e) {
							logger.debug(e.getMessage(), e);
							throw new SraException(e.getMessage(), e);
						}
					} else {
						//logger.debug("Pas de deletion dans base pour umbrellaProject " + submission.umbrellaCode + " qui n'est pas dans etat SUB-N ou SUB-V");
					}
				} else {
					//logger.debug("Pas de deletion dans base pour umbrellaProject " + submission.umbrellaCode + " qui n'est pas de type umbrella dans base");
				}
			} else {
				//logger.debug("Pas de deletion dans base pour umbrellaProject " + submission.umbrellaCode + " utilisé dans plusieurs soumissions");
			}
		}

		if (! submission.refStudyCodes.isEmpty()) {	
			// On ne peut detruire que des ExternalStudy crées et utilisés seulement par la soumission courante.
			for (String studyCode : submission.refStudyCodes){
				// verifier que study n'est pas utilisé par autre objet submission avant destruction
				// normalement study crees dans init de type external avec state=F-SUB ou study avec state='N'
				List <Submission> submissionList2 = submissionDAO.dao_find(DBQuery.in("refStudyCodes", studyCode)).toList();
				if (submissionList2.size() > 1) {
					for (Submission sub: submissionList2) {
						//logger.debug(studyCode + " utilise par objet Submission " + sub.code);
					}
				} else {
					// on ne veut enlever que les external_study cree par cette soumission, si internalStudy cree, on veut juste le remettre avec bon state.

					AbstractStudy absStudy = abstractStudyDAO.findByCode(studyCode);
					if (absStudy != null) {
						if ( absStudy instanceof ExternalStudy ) {
							//System.out.println("Recuperation dans base du study avec Ac = " + studyAc +" qui est du type externalStudy ");
							if (SUB_F.equalsIgnoreCase(absStudy.state.code) ) {
								try {
									externalStudyDAO.deleteByCode(studyCode);
									//logger.debug("deletion dans base pour study "+studyCode);
								} catch (DAOException e) {
									logger.debug(e.getMessage(), e);
									throw new SraException(e.getMessage(), e);
								}
							}
						} else { // instanceof Study
							if (SUB_N.equalsIgnoreCase(absStudy.state.code) || SUB_V.equalsIgnoreCase(absStudy.state.code) ) {
								studyDAO.update(DBQuery.is("code", studyCode),
										DBUpdate.set("state.code", NONE)
										.set("traceInformation.modifyUser", user)
										.set("traceInformation.modifyDate", new Date()));
							}
						}
					}
				}
			}
		}
		//logger.debug("deletion dans base pour submission "+submission.code);
		submissionDAO.deleteByCode(submission.code);
	}


	//SGAS 
	// Pas testee
	// Permet d'inactiver une soumission ne contenant aucun sample ou study, pour laquelle
	// les numeros d'accession obtenus par l'EBI ont ete supprimés après coup par l'EBI (à notre demande).
	// les AC de la soumission, des runs, et des experiments sont remplaces par 
	// la valeur suppressed_AC, tandis que les readsets sont rendus disponibles pour une nouvelle
	// soumission.
	@Deprecated
	public void inactiveCreationSubmissionWithoutSampleNorStudy(Submission submission, String user) throws SraException{
		// Si la soumission est inconnu de l'EBI, sucun sens de l'inactiver
		if (StringUtils.isBlank(submission.accession)){
			//logger.debug("objet submission avec AC : submissionCode = "+ submission.code + " et submissionAC = "+ submission.accession);
			throw new SraException("Inactivation impossible pour la soumission " + submission.code + " car aucun AC");
		} 
		if ( submission.type == Submission.Type.CREATION && !submission.state.code.equalsIgnoreCase(SUB_F)) {
			throw new SraException("Inactivation impossible pour la soumission " + submission.code + " avec etat " + submission.state.code + " != SUB-F");
		}
		if (StringUtils.isNotBlank(submission.studyCode)){
			throw new SraException("Inactivation impossible pour la soumission " + submission.code + " qui contient le studyCode " + submission.studyCode);
		}
		if (! submission.sampleCodes.isEmpty()) {
			throw new SraException("Inactivation impossible pour la soumission " + submission.code + " avec sampleCodes qui contient des valeurs" );
		}
		List<String> experimentCodes = new ArrayList<>();
		List<String> runCodes = new ArrayList<>();
		// Si la soumission concerne une soumission primaire de données avec status = N :
		for (String experimentCode : submission.experimentCodes) {
			// verifier que l'experiment n'est pas utilisé par autre objet submission avant destruction
			Experiment experiment = experimentDAO.findByCode(experimentCode);
			// mettre le status pour la soumission des readSet à NONE si possible: 
			if (experiment != null){
				// remettre les readSet dans la base avec submissionState à "NONE":
				//						//System.out.println("submissionState.code remis à 'N' pour le readSet "+experiment.readSetCode);

				laboReadSetDAO.update(DBQuery.is("code", experiment.readSetCode),
						DBUpdate.set("submissionState.code", NONE)
						.set("traceInformation.modifyUser", user)
						.set("traceInformation.modifyDate", new Date()));

				// Enlever les readsets de la collection sra :
				readsetDAO.deleteByCode(experiment.readSetCode);

				List <Submission> submissionList = submissionDAO.dao_find(DBQuery.in("experimentCodes", experimentCode)).toList();
				if (submissionList.size() > 1) {
					for (Submission sub: submissionList) {
						//logger.debug(experimentCode + " utilise par objet Submission " + sub.code);
					}
				} else {
					String runCode = experiment.run.code;
					String runAccession = experiment.run.accession;
					experimentCodes.add("suppressed_" + experimentCode);
					runCodes.add("suppressed_" + runCode);

					try {
						experimentDAO.update(DBQuery.is("code", experimentCode),
								DBUpdate.set("accession", "suppressed_" + experiment.accession)
								.set("run.code", "suppressed_" + runCode)
								.set("code", "suppressed_" + experimentCode)
								.set("run.accession", "suppressed_" + runAccession)
								.set("run.expCode", "suppressed_" + experimentCode)
								.set("run.expAccession", "suppressed_" + runAccession)
								.set("traceInformation.modifyUser", user)
								.set("traceInformation.modifyDate", new Date()));
					} catch (DAOException e) {
						//logger.debug(e.getMessage(), e);
						throw new SraException(e.getMessage(), e);
					}

				}
			}

		}


		// verifier que la config à l'etat used n'est pas utilisé par une autre soumission avant de remettre son etat à "NONE"
		// update la configuration pour le statut en remettant le statut new si pas utilisé par ailleurs :
		List <Submission> submissionList = submissionDAO.dao_find(DBQuery.in("configCode", submission.configCode)).toList();
		if (submissionList.size() <= 1) {
			configurationDAO.update(DBQuery.is("code", submission.configCode), 
					DBUpdate.set("state.code", NONE)
					.set("traceInformation.modifyUser", user)
					.set("traceInformation.modifyDate", new Date()));
			//logger.debug("state.code remis à '"+NONE+"' pour configuration "+submission.configCode);
		}

		// mettre la soumission à jour pour le code et le numeros d'accession



		String submissionCode = submission.code;
		String submissionAccession = submission.accession;
		submissionDAO.update(DBQuery.is("code", submissionCode),
				DBUpdate.set("accession", "suppressed_" + submissionAccession)
				.set("code", "suppressed_" + submissionCode)
				.set("experimentCodes", experimentCodes)
				.set("runCodes", runCodes)
				.set("traceInformation.modifyUser", user)
				.set("traceInformation.modifyDate", new Date()));


		//logger.debug("deletion dans base pour submission "+submission.code);
		submissionDAO.deleteByCode(submission.code);
	}

	/**
	 * Met la soumission et tous ses objets enfants avec le createUser indiqué sans 
	 * modifier le traceInformation.modifyDate.
	 * @param  submissionCode   submissionCode
	 * @param  user             user
	 * @throws SraException error
	 */
	@Deprecated
	public void updateSubmissionChildObjectForUser(String submissionCode, String user) throws SraException {
		Submission submission = submissionDAO.getObject(submissionCode);
		if (submission == null) {
			throw new SraException("updateSubmissionChildObjectForUser", "La soumission " + submissionCode + " n'existe pas dans la base");
		}

		if (StringUtils.isNotBlank(submission.umbrellaCode)) {
			Project project = projectDAO.getObject(submission.umbrellaCode);
			if (project == null) {
				String message = "Le project avec le code " + submission.umbrellaCode 
						+ " indiquee dans la submission " + submission.code 
						+ " n'existe pas dans la base";
				//logger.debug(message);
				throw new SraException("updateSubmissionChildObjectForUser", message);
			} 
			// Recuperer object project pour mettre bon user :
			projectDAO.update(DBQuery.is("code", project.code), 
					DBUpdate.set("traceInformation.createUser", user));
		}


		if (StringUtils.isNotBlank(submission.studyCode)) {
			Study study = studyDAO.getObject(submission.studyCode);
			if (study == null) {
				String message = "Le study avec le code " + submission.studyCode 
						+ " indiquee dans la submission " + submission.code 
						+ " n'existe pas dans la base";
				//logger.debug(message);
				throw new SraException("updateSubmissionChildObjectForUser", message);
			} 
			// Recuperer object study pour mettre bon user :
			studyDAO.update(DBQuery.is("code", study.code), 
					DBUpdate.set("traceInformation.createUser", user));
		}
		// user à propager dans samples de la la soumission
		// donc dans collection sampleCodes qui ne contient que des samples et jamais d'ExternalSample
		if (submission.sampleCodes != null) {
			for (int i = 0; i < submission.sampleCodes.size() ; i++) {
				Sample sample = sampleDAO.getObject(submission.sampleCodes.get(i));
				if (sample == null) {
					String message = "Le sample avec le code " + submission.sampleCodes.get(i) 
					+ " indiquee dans la submission " + submission.code 
					+ " n'existe pas dans la base";
					//logger.debug(message);
					throw new SraException("updateSubmissionChildObjectForUser", message);
				}
				sampleDAO.update(DBQuery.is("code", sample.code),					
						DBUpdate.set("traceInformation.createUser", user));
			}
		}
		// user à propager dans experiments à soumettre et readset: 
		if (submission.experimentCodes != null) {
			for (int i = 0; i < submission.experimentCodes.size() ; i++) {
				Experiment experiment = experimentDAO.getObject(submission.experimentCodes.get(i));
				if (experiment == null) {
					String message = "L'experiment avec le code " 
							+ submission.experimentCodes.get(i)  
							+ " indiquee dans la submission " + submission.code 
							+ " n'existe pas dans la base";
					//logger.debug(message);
					throw new SraException(message);
				} 
				experimentDAO.update(DBQuery.is("code", experiment.code),					
						DBUpdate.set("traceInformation.createUser", user));
				ReadSet laboreadset = laboReadSetDAO.findByCode(experiment.readSetCode);
				if (laboreadset == null)  {
					if (experiment.typePlatform.equalsIgnoreCase("ls454")) {
						// Pas de sauvegarde des etats dans la collection labo.readSet 
						// car le readset 454 n'existe pas dans cette collection.
						// Pas de sauvegarde des etats dans la collection sra.readSet car pas de state	
					} else if (experiment.typePlatform.equalsIgnoreCase("oxford_nanopore")) {
						String message = "Le readset de type oxford nanopore " + experiment.readSetCode
								+ " n'existe pas dans la base";
						throw new SraException(message);
					} else if (experiment.typePlatform.equalsIgnoreCase("illumina")) {
						String message = "Le readset de type oxford nanopore " + experiment.readSetCode
								+ " n'existe pas dans la base";
						throw new SraException(message);
					} else {
						String message = "readset de type " + experiment.typePlatform
								+ " non prevu par le code";
						throw new SraException(message);
					}
				} else {					
					laboReadSetDAO.update(DBQuery.is("code", laboreadset.code),					
							DBUpdate.set("submissionState.user", user));					
					// pas de sauvegarde des etats dans la collection sra.readSet car pas de state
				}
			}
		}
	}
	
	
	//---------------------------------------------------------------------------------------------------------------------------------------------
	// methodes pour les problemes propagation de date de release
	//---------------------------------------------------------------------------------------------------------------------------------------------

	public AbstractSample getAbstractSample(String sampleCode, String sampleAccession) {
		AbstractSample absSampleDB = null;
		if( abstractSampleAPI.dao_checkObjectExist("code", sampleCode)) {
			absSampleDB = abstractSampleAPI.dao_findOne(DBQuery.is("code", sampleCode));
		} else if ( abstractSampleAPI.dao_checkObjectExist("accession", sampleAccession)) {
			absSampleDB = abstractSampleAPI.dao_findOne(DBQuery.is("code", sampleAccession));
		} else {
			// rien
		}
		return absSampleDB;
	}

	
	public boolean isPublicAbstractStudy(AbstractStudy absStudy) {
		if(absStudy == null) {
			throw new SraException("isPublicAbstractStudy :: absStudy null");
		}
		//logger.debug("Dans isPublic avec absStudy='{}'", absStudy.code);

		if(absStudy instanceof ExternalStudy || absStudy.consolidatedPublicStatus) {
			//logger.debug("Dans isPublic avec absStudy='{}', avec consolidatedPublicStatus = true", absStudy.code);
			return true;
		}
		if (((Study) absStudy).releaseDate != null && ((Study) absStudy).releaseDate.before(new Date())) {
			//logger.debug("Dans isPublic avec absStudy='{}', avec releaseDate '{}' publique", absStudy.code, ((Study) absStudy).releaseDate);
			return true;
		} else {
			//logger.debug("Dans isPublic avec absStudy='{}', avec releaseDate '{}' prive", absStudy.code, ((Study) absStudy).releaseDate);
		}
		return false;
	}
	
	public boolean isPublicExperiment(Experiment experiment) {
		if(experiment == null) {
			throw new SraException("isPublicExperiment :: experiment null");
		}	
		//logger.debug("Dans isPublic avec experiment='{}'", experiment.code);
		if (experiment.releaseDate != null && experiment.releaseDate.before(new Date())) {
			//logger.debug("Dans isPublic avec experiment='{}', avec releaseDate '{}' publique", experiment.code, experiment.releaseDate);
			return true;
		} else {
			//logger.debug("Dans isPublic avec experiment='{}', avec releaseDate '{}' prive", experiment.code, experiment.releaseDate);
		}
		if (experiment.consolidatedPublicStatus) {
			//logger.debug("Dans isPublic avec experiment='{}', avec consolidatedPublicStatus = true", experiment.code);
			return true;
		}		
		return false;
	}	
	
	
	public boolean isPublicAnalysis(Analysis analysis) {
		if(analysis == null) {
			throw new SraException("isPublicAnalysis :: analysis null");
		}		
		if (analysis.releaseDate != null && analysis.releaseDate.before(new Date())) {
			return true;
		}
		if (analysis.consolidatedPublicStatus) {
			return true;
		}		
		return false;
	}	
	
	public boolean isPublicAbstractSample(AbstractSample absSample) {
		if(absSample == null) {
			throw new SraException("isPublicAbstractSample :: absSample null");
		}
		if(absSample instanceof ExternalSample || absSample.consolidatedPublicStatus) {
			return true;
		}
		if (((Sample) absSample).releaseDate != null && ((Sample) absSample).releaseDate.before(new Date())) {
			return true;
		}
		return false;
	}	
	
	
	private void buildAllListSample(Experiment                                  experiment, 
									HashMap<String, HashMap<String,Experiment>> hSampleReferenceByExpCodeExp, 
									HashMap<String, HashMap<String,Experiment>> hSampleDereferenceByExpCodeExp,
									HashMap<String, Sample>                     hSampleToUpdateForReleaseDate,
									boolean                                     possibleDereferenceInExpOrAnalyses) {
		
		if(experiment.sampleCode.startsWith("externalSample_")) {
			// rien l'experiment reference un externalSample qui est forcement public
		} else {
			if(abstractSampleAPI.dao_checkObjectExist("code", experiment.sampleCode)) {
				AbstractSample abstSample = abstractSampleAPI.get(experiment.sampleCode);
				//logger.debug("dans buildAllListSample sample="+abstSample.code);
				if (abstSample instanceof Sample && ! isPublicAbstractSample(abstSample) && StringUtils.isNotBlank(abstSample.accession)) {
					//logger.debug("dans buildAllListSample sample="+abstSample.code);
					if(! hSampleReferenceByExpCodeExp.containsKey(experiment.sampleCode)) {
						//logger.debug("dans buildAllListSample sample="+abstSample.code+ "reference par experiment "+ experiment.code);
						hSampleReferenceByExpCodeExp.put(experiment.sampleCode, new HashMap<String,Experiment>());	
					}
					if( ! hSampleReferenceByExpCodeExp.get(experiment.sampleCode).containsKey(experiment.code)) {
						//logger.debug("dans buildAllListSample sample="+abstSample.code+ "ajout ref par experiment "+ experiment.code);
						hSampleReferenceByExpCodeExp.get(experiment.sampleCode).put(experiment.code, experiment);
					}
					if( ! hSampleToUpdateForReleaseDate.containsKey(abstSample.code)) {
						hSampleToUpdateForReleaseDate.put(abstSample.code, (Sample) abstSample);
					}
				}
			}
		}
		if (possibleDereferenceInExpOrAnalyses) {
			// Repertorier les oldSample qui ne sont plus referencés par les experiments modifiés pour leur releaseDate:
			if(experimentAPI.dao_checkObjectExist("code", experiment.code)) {
				Experiment oldExperiment = experimentAPI.get(experiment.code);
				if(oldExperiment.sampleCode.startsWith("externalSample_")) {
					// rien l'oldExperiment reference un externalSample qui reste forcement public
				} else {
					// determiner si changement de ref du sampleCode dans exp
					if(! experiment.sampleCode.equals(oldExperiment.sampleCode)) {
						if(abstractSampleAPI.dao_checkObjectExist("code", oldExperiment.sampleCode)) {
							AbstractSample abstSample = abstractSampleAPI.get(oldExperiment.sampleCode);
							if (abstSample instanceof Sample && ! isPublicAbstractSample(abstSample) && StringUtils.isNotBlank(abstSample.accession)) {
								if(! hSampleDereferenceByExpCodeExp.containsKey(oldExperiment.sampleCode)) {
									hSampleDereferenceByExpCodeExp.put(oldExperiment.sampleCode,  new HashMap<String,Experiment>());	
								}
								if( ! hSampleDereferenceByExpCodeExp.get(oldExperiment.sampleCode).containsKey(oldExperiment.code)) {
									hSampleDereferenceByExpCodeExp.get(oldExperiment.sampleCode).put(oldExperiment.code, oldExperiment);
								}
								if( ! hSampleToUpdateForReleaseDate.containsKey(abstSample.code)) {
									hSampleToUpdateForReleaseDate.put(abstSample.code, (Sample) abstSample);
								}
							}
						}	
					}	
				}
			}
		}
	}
	
	
	private void buildAllListSample(Analysis                                  analysis, 
									HashMap<String, HashMap<String,Analysis>> hSampleReferenceByAnalysisCodeAnalysis, 
									HashMap<String, HashMap<String,Analysis>> hSampleDereferenceByAnalysisCodeAnalysis, 
									HashMap<String, Sample>                   hSampleToUpdateForReleaseDate,
									boolean                                   possibleDereferenceInExpOrAnalyses) {
		
		// Repertorier les userSample avec numeros d'accession referencés par les analyses
		if(analysis.sampleCode.startsWith("externalSample_")) {
			// rien l'experiment reference un externalSample qui est forcement public
		} else {
			if(abstractSampleAPI.dao_checkObjectExist("code", analysis.sampleCode)) {
				AbstractSample abstSample = abstractSampleAPI.dao_findOne(DBQuery.is("code", analysis.sampleCode));
				if (abstSample instanceof Sample && ! isPublicAbstractSample(abstSample) && StringUtils.isNotBlank(abstSample.accession)) {
					if(! hSampleReferenceByAnalysisCodeAnalysis.containsKey(analysis.sampleCode)) {
						hSampleReferenceByAnalysisCodeAnalysis.put(analysis.sampleCode, new HashMap<String,Analysis>());	
					}
					if( ! hSampleReferenceByAnalysisCodeAnalysis.get(analysis.sampleCode).containsKey(analysis.code)) {
						hSampleReferenceByAnalysisCodeAnalysis.get(analysis.sampleCode).put(analysis.code, analysis);
					}
					if(! hSampleToUpdateForReleaseDate.containsKey(abstSample.code)) {
						hSampleToUpdateForReleaseDate.put(abstSample.code, (Sample) abstSample);
					}
				}
			}
		}
		
		// Repertorier les oldSample qui ne sont plus referencés par les analyses
		// Normalement aucune, on n'autorise pas en janvier 2024 l'update des analyses, donc l'analyse ne peut pas changer de sample
		// Repertorier les oldSample qui ne sont plus referencés par les experiments :
		if(possibleDereferenceInExpOrAnalyses) {
			if(analysisAPI.dao_checkObjectExist("code", analysis.code)) {
				Analysis oldAnalysis = analysisAPI.get(analysis.code);
				if(oldAnalysis.sampleCode.startsWith("externalSample_")) {
					// rien l'oldExperiment reference un externalSample qui reste forcement public
				} else {
					// determiner si changement de ref du sampleCode dans exp
					if(! analysis.sampleCode.equals(oldAnalysis.sampleCode)) {
						if(abstractSampleAPI.dao_checkObjectExist("code", oldAnalysis.sampleCode)) {
							AbstractSample abstSample = abstractSampleAPI.get(oldAnalysis.sampleCode);
							if (abstSample instanceof Sample && ! isPublicAbstractSample(abstSample) && StringUtils.isNotBlank(abstSample.accession)) {
								if(! hSampleDereferenceByAnalysisCodeAnalysis.containsKey(oldAnalysis.sampleCode)) {
									hSampleDereferenceByAnalysisCodeAnalysis.put(oldAnalysis.sampleCode,  new HashMap<String,Analysis>());	
								}
								if( ! hSampleDereferenceByAnalysisCodeAnalysis.get(oldAnalysis.sampleCode).containsKey(oldAnalysis.code)) {
									hSampleDereferenceByAnalysisCodeAnalysis.get(oldAnalysis.sampleCode).put(oldAnalysis.code, oldAnalysis);
								}
								if( ! hSampleToUpdateForReleaseDate.containsKey(abstSample.code)) {
									hSampleToUpdateForReleaseDate.put(abstSample.code, (Sample) abstSample);
								}
							}
						}
					}
				}	
			}	
		}
	}
	
	
	public HashMap<String, Sample> updateSampleForReleaseDate(HashMap<String, Experiment> experiments, 
															  HashMap<String, Analysis>   analyzes, 
															  ContextValidation           contextValidation,
															  boolean                     possibleDereferenceInExpOrAnalyses) {
		
		HashMap<String, Sample> hSampleToUpdateForReleaseDate = new HashMap<String,Sample>();

		if(experiments == null) {
			contextValidation.addError("updateSampleForReleaseDate", " hash experiments en argument null");
			return hSampleToUpdateForReleaseDate;
		}
		if(analyzes == null) {
			contextValidation.addError("updateSampleForReleaseDate", " hash experiments en argument null");
			return hSampleToUpdateForReleaseDate;
		}	
		
		// construction d'un hash hash{sampleCode => {exp1.code => exp1, expN.code => expN}}
		HashMap<String, HashMap<String,Experiment>> hSampleReferenceByExpCodeExp             = new HashMap<String,HashMap<String,Experiment>>();
		HashMap<String, HashMap<String,Experiment>> hSampleDereferenceByExpCodeExp           = new HashMap<String,HashMap<String,Experiment>>();
		HashMap<String, HashMap<String,Analysis>>   hSampleReferenceByAnalysisCodeAnalysis   = new HashMap<String,HashMap<String,Analysis>>();
		HashMap<String, HashMap<String,Analysis>>   hSampleDereferenceByAnalysisCodeAnalysis = new HashMap<String,HashMap<String,Analysis>>();
		// construire listes
		//logger.debug("KKKKKKKKKKKK dans updateSampleForReleaseDate :  avant construction des hSampleReferenceByExpCodeExp  "+ new Date());

		for (Entry<String, Experiment> setExperiment : experiments.entrySet()) {
			//Experiment experiment = setExperiment.getValue();
			// voir si cas France avec soumission SAME plutot que accession, exp.sampleCode est renseigné
			// Repertorier les userSample referencés par les experiment modifiés pour la releaseDate :
			buildAllListSample(setExperiment.getValue(), hSampleReferenceByExpCodeExp, hSampleDereferenceByExpCodeExp, hSampleToUpdateForReleaseDate, possibleDereferenceInExpOrAnalyses);
		} // end for setExperiment
		
		//logger.debug("KKKKKKKKKKKK dans updateSampleForReleaseDate :  apres construction des hSampleReferenceByExpCodeExp  "+ new Date());
		//logger.debug("KKKKKKKKKKKK dans updateSampleForReleaseDate :  avant construction des hSampleReferenceByAnalysisCodeAnalysis  "+ new Date());

		for (Entry<String, Analysis> setAnalysis : analyzes.entrySet()) {
			//Analysis analysis = setAnalysis.getValue();
			// voir si cas France avec soumission SAME plutot que accession, exp.sampleCode est renseigné
			buildAllListSample(setAnalysis.getValue(), hSampleReferenceByAnalysisCodeAnalysis, hSampleDereferenceByAnalysisCodeAnalysis, hSampleToUpdateForReleaseDate, possibleDereferenceInExpOrAnalyses);
		}// end for setAnalysis
		//logger.debug("KKKKKKKKKKKK dans updateSampleForReleaseDate :  apres construction des hSampleReferenceByAnalysisCodeAnalysis  "+ new Date());

		
		// Calcul des releaseDate des samples referencés et deréférencés par les experiments et les analyses:
		//logger.debug("KKKKKKKKKKK   avant calculateSampleReleaseDate : taille de samples a mettre a jour pour releaseDate = " + hSampleToUpdateForReleaseDate.entrySet().size() + " "+ new Date());

		for (Entry<String, Sample> setSample : hSampleToUpdateForReleaseDate.entrySet()) {
			//Sample sample = setSample.getValue();
			 calculateSampleReleaseDate (setSample.getValue(), 
					 					 hSampleReferenceByExpCodeExp, 
					 					 hSampleDereferenceByExpCodeExp,
					 					 hSampleReferenceByAnalysisCodeAnalysis,
					 					 hSampleDereferenceByAnalysisCodeAnalysis,
					 					 contextValidation);
//				if(setSample.getValue().releaseDate!= null) {
//					logger.debug("dans updateSampleForReleaseDate apres calculate IIIIIIIIIIIIII         pour le sample " + setSample.getValue().code + ", releaseDate = " + setSample.getValue().releaseDate);
//				} else {
//					logger.debug("dans updateSampleForReleaseDate  apres calculate IIIIIIIIIIIIII         pour le sample " + setSample.getValue().code + ", releaseDate = null");
//				}

		}	
		//logger.debug("KKKKKKKKKKK    apres calculateSampleReleaseDate " + new Date());

		//logger.debug("Dans updateSampleForReleaseDate : taille de samples a mettre a jour pour releaseDate = " + hSampleToUpdateForReleaseDate.entrySet().size() + " "+ new Date());
		return hSampleToUpdateForReleaseDate;
	} // end updateOldSampleExpForReleaseDateBis

	
	// calcule de facon dynamique la releaseDate du sample si le sample n'a pas dejà un status  public
	// on est obligé de tenir compte du statu prive ou public du sample, car il peut avoir ete mis public par un experiment et 
	// cet experiment ne plus pointer sur ce sample, auquel cas, le sample doit rester public.
	public void calculateSampleReleaseDate (Sample sample, 
			HashMap<String, HashMap<String,Experiment>> hSampleReferenceByExpCodeExp, 
			HashMap<String, HashMap<String,Experiment>> hSampleDereferenceByExpCodeExp,
			HashMap<String, HashMap<String,Analysis>>   hSampleReferenceByAnalysisCodeAnalysis,
			HashMap<String, HashMap<String,Analysis>>   hSampleDereferenceByAnalysisCodeAnalysis,
			ContextValidation                           contextValidation) {
		// le sample n'a été modifié pour aucun champs. sample identique à celui dans base si updateExp ou releaseStudy
		if(isPublicAbstractSample(sample) || StringUtils.isBlank(sample.accession)) {
			// si le sample est deja public, ne pas modifier sa date de release meme s'il n'y a plus que des exp privés qui pointent dessus.
			// si sample sans numeros d'accession, pas de releaseDate à calculer.
			return;
		}
		//logger.debug("dans calculate, sample prive " + sample.code);
		// recuperer dans base tous les exp et analyses liés au sample et prendre la date de release la plus proche 
		List <Analysis> analysisList = new ArrayList<Analysis>();
		List <Experiment> expList = new ArrayList<Experiment>();
		String sampleAccession = "NONE";
		String sampleCode = "NONE";
		if(StringUtils.isNotBlank(sample.accession)) {
			sampleAccession = sample.accession;
		}
		if (StringUtils.isNotBlank(sample.code)) {
			sampleCode = sample.code;
		}

		analysisList = MongoDBDAO.find( InstanceConstants.SRA_ANALYSIS_COLL_NAME, Analysis.class, 
				DBQuery.or
				(DBQuery.in("sampleAccession", sampleAccession), 
						DBQuery.in("sampleCode", sampleCode))).toList();

		expList = MongoDBDAO.find( InstanceConstants.SRA_EXPERIMENT_COLL_NAME, Experiment.class, 
				DBQuery.or
				(DBQuery.in("sampleAccession", sampleAccession), 
						DBQuery.in("sampleCode", sampleCode))).toList();

		Date minReleaseDate = null;
		for (Analysis analysis : analysisList) {
			if(hSampleReferenceByAnalysisCodeAnalysis.containsKey(analysis.sampleCode) && hSampleReferenceByAnalysisCodeAnalysis.get(analysis.sampleCode).containsKey(analysis.code)) {
				analysis = hSampleReferenceByAnalysisCodeAnalysis.get(analysis.sampleCode).get(analysis.code);
			}
			if(hSampleDereferenceByAnalysisCodeAnalysis.containsKey(analysis.sampleCode) && hSampleDereferenceByAnalysisCodeAnalysis.get(analysis.sampleCode).containsKey(analysis.code)) {
				continue;
			}
			if( minReleaseDate == null && analysis.releaseDate != null) {
				minReleaseDate = analysis.releaseDate;
				continue;
			}
			if( minReleaseDate != null && analysis.releaseDate != null) {
				if (analysis.releaseDate.before(minReleaseDate)) {
					minReleaseDate = analysis.releaseDate;
				}
			}
		}
		// Attention, il peut y avoir des analyses qui referencent ce sample et cette reference n'existe pas en base :
		if(hSampleReferenceByAnalysisCodeAnalysis.containsKey(sample.code)) {
			for (Entry<String, Analysis> setAnalysis : hSampleReferenceByAnalysisCodeAnalysis.get(sample.code).entrySet()) {
				if( minReleaseDate == null && setAnalysis.getValue().releaseDate != null) {
					minReleaseDate = setAnalysis.getValue().releaseDate;
					continue;
				}
				if( minReleaseDate != null && setAnalysis.getValue().releaseDate != null) {
					if (setAnalysis.getValue().releaseDate.before(minReleaseDate)) {
						minReleaseDate = setAnalysis.getValue().releaseDate;
					}
				}		
			}
		}

		for (Experiment experiment : expList) {
			//logger.debug("dans calculate, experiment="+ experiment.code);
			// si exp existe dans exp passé en arg avec releaseDate potentiellement != de la valeur en base, prendre exp passe en arg plutot que exp dans base
			// ssi exp a bien meme ref de sample que l'exp de la base.
			if(hSampleReferenceByExpCodeExp.containsKey(experiment.sampleCode) && hSampleReferenceByExpCodeExp.get(experiment.sampleCode).containsKey(experiment.code)) {
				//logger.debug("dans calculate, ref vers exp " + experiment.code + " avec releaseDate="+ experiment.releaseDate);
				experiment = hSampleReferenceByExpCodeExp.get(experiment.sampleCode).get(experiment.code);
			}
			if(hSampleDereferenceByExpCodeExp.containsKey(experiment.sampleCode) && hSampleDereferenceByExpCodeExp.get(experiment.sampleCode).containsKey(experiment.code)) {
				//logger.debug("dans calculate, dereference de exp " + experiment.code + " avec releaseDate="+ experiment.releaseDate);
				continue;
			}
			if( minReleaseDate == null && experiment.releaseDate != null) {
				minReleaseDate = experiment.releaseDate;
				continue;
			}
			if( minReleaseDate != null && experiment.releaseDate != null) {
				if (experiment.releaseDate.before(minReleaseDate)) {
					minReleaseDate = experiment.releaseDate;
				}
			}	
		}
		// Attention, il peut y avoir des exp qui referencent ce sample  et cette reference n'existe pas en base :
		if(hSampleReferenceByExpCodeExp.containsKey(sample.code)) {
			for (Entry<String, Experiment> setExperiment : hSampleReferenceByExpCodeExp.get(sample.code).entrySet()) {
				//logger.debug("OOOOOOOOOOOOOO    dans calculate, sample reference par experiment " + setExperiment.getValue().code + " avec releasedate=" +setExperiment.getValue().releaseDate);
				if( minReleaseDate == null && setExperiment.getValue().releaseDate != null) {
					minReleaseDate = setExperiment.getValue().releaseDate;
					continue;
				}
				if( minReleaseDate != null && setExperiment.getValue().releaseDate != null) {
					if (setExperiment.getValue().releaseDate.before(minReleaseDate)) {
						minReleaseDate = setExperiment.getValue().releaseDate;
					}
				}		
			}
		}

		sample.releaseDate = minReleaseDate;
		
		if(isPublicAbstractSample(sample)) {
			sample.consolidatedPublicStatus = true;
		} else {
			sample.consolidatedPublicStatus = false;
		}
		
//		if(sample.releaseDate!= null) {
//			logger.debug("dans calculate IIIIIIIIIIIIII         pour le sample " + sample.code + ", releaseDate = " + sample.releaseDate);
//		} else {
//			logger.debug("dans calculate IIIIIIIIIIIIII         pour le sample " + sample.code + ", releaseDate = null");
//		}
		//logger.debug("sortie de calculate");
	}// end calculate
	
	
	// on passe le studyOfExp en parametre car il n'est pas forcement en base si appel depuis caseExperiment avec userExp qui pointe vers externalStudy pas encore en base
	public void updateAnalysisForReleaseDate(AbstractStudy abstStudyOfUserExp, Analysis userAnalysis, HashMap<String, Analysis> hAnalysisToUpdateForReleaseDate, ContextValidation contextValidation) {
		
		if(abstStudyOfUserExp == null) {
			contextValidation.addError("updateAnalysisForReleaseDate", "argument abstStudyOfExp null");
			return;
		}
		// une analyse sans numeros d'accession n'a pas à avoir de release Date ou celle-ci n'a pas de sens.
		if(StringUtils.isBlank(userAnalysis.accession)) {
			return;
		}
		
		// calcul de la releaseDate de l'analyse :
		if( isPublicAnalysis(userAnalysis)) { 
			if(! isPublicAbstractStudy(abstStudyOfUserExp)) {
				contextValidation.addError("updateAnalysisForReleaseDate ", " tentative de mettre l'analyse "+ userAnalysis.code + " public sous la reference du study privé "+ abstStudyOfUserExp.code);
			} else {
				// si analyse deja public, ne pas modifier sa date de release. 
				userAnalysis.consolidatedPublicStatus = true;
			}
		} else {
			// si analyse privée, et study public alors analyse devient public avec date de release = date de cette soumission et non date de release du study
			if(isPublicAbstractStudy(abstStudyOfUserExp)) {
				userAnalysis.consolidatedPublicStatus = true;
				userAnalysis.releaseDate = new Date();
			} else {
				// si experiment privé et study privé alors experiment reste privé avec date de release = date de release du study
				userAnalysis.consolidatedPublicStatus = false;
				if(abstStudyOfUserExp instanceof Study && ((Study) abstStudyOfUserExp).releaseDate != null) {
					userAnalysis.releaseDate = ((Study) abstStudyOfUserExp).releaseDate;
				} 
			}
		}
		
		// Identifier les analyses avec changement de ref au sample et les analyses avec changement de date de release qui impacteront les samples à modifier pour la releaseDate :

		// pb si analysis avec num d'accession sans releaseDate ici
		if(userAnalysis.releaseDate == null ) {
			contextValidation.addError("updateAnalysisForReleaseDate ", " analyse "+ userAnalysis.code + " avec numeros d'accession, sans releaseDate ?");	
		} 
		
		if(! analysisAPI.dao_checkObjectExist("code", userAnalysis.code)) {
			// l'analyse n'a jamais ete enregistré dans base (cas d'un import de données produites hors NGLS-SUB) => On considere qu'on modifie sa date de release.
			if(!hAnalysisToUpdateForReleaseDate.containsKey(userAnalysis.code)) {
				hAnalysisToUpdateForReleaseDate.put(userAnalysis.code, userAnalysis);
			}
		} else {
			Analysis oldAnalysis = analysisAPI.get(userAnalysis.code);
			if(isPublicAnalysis(oldAnalysis) && ! isPublicAnalysis(userAnalysis)) {
				contextValidation.addError("updateAnalysisForReleaseDate ", " tentative de mettre l'analyse "+ userAnalysis.code + " privee alors qu'elle est public en base");					
			}
			if (oldAnalysis.releaseDate == null) {
				// cas du userAnalysis.releaseDate à null déja mis en erreur.
				if(!hAnalysisToUpdateForReleaseDate.containsKey(userAnalysis.code)) {
					hAnalysisToUpdateForReleaseDate.put(userAnalysis.code, userAnalysis);	
				}
			} else {
				if (! oldAnalysis.releaseDate.equals(userAnalysis.releaseDate)) {
					if(! hAnalysisToUpdateForReleaseDate.containsKey(userAnalysis.code)) {
						hAnalysisToUpdateForReleaseDate.put(userAnalysis.code, userAnalysis);
					}
				}
			}
			if ( ! oldAnalysis.sampleCode.equals(userAnalysis.sampleCode) ) {
				if(!hAnalysisToUpdateForReleaseDate.containsKey(userAnalysis.code)) {
					hAnalysisToUpdateForReleaseDate.put(userAnalysis.code, userAnalysis);
				}
			}
		} 
		
	} // end updateAnalysisForReleaseDate	
	
	
	// on passe le studyOfExp en parametre car il n'est pas forcement en base si appel depuis caseExperiment avec userExp qui pointe vers externalStudy pas encore en base
	public void updateExperimentForReleaseDate(AbstractStudy abstStudyOfUserExp, Experiment userExp, HashMap<String, Experiment> hExperimentToUpdateForReleaseDate, ContextValidation contextValidation) {
		
		if(abstStudyOfUserExp == null) {
			contextValidation.addError("updateExperimentForReleaseDate", "argument abstStudyOfExp null");
			return;
		}
		// un experiment sans numeros d'accession n'a pas à avoir de release Date ou celle-ci n'a pas de sens.
		if(StringUtils.isBlank(userExp.accession)) {
			return;
		}
		
		// calcul de la releaseDate de l'exp :
		if( isPublicExperiment(userExp)) { 
			//logger.debug("updateExperimentForReleaseDate : exp '{}' public ", userExp.code);
			if(! isPublicAbstractStudy(abstStudyOfUserExp)) {
				//logger.debug("updateExperimentForReleaseDate : abstStudyOfUserExp='{}' private ", abstStudyOfUserExp.code);
				contextValidation.addError("updateExperimentForReleaseDate ", " tentative de mettre l'experiment "+ userExp.code + " public sous la reference du study privé "+ abstStudyOfUserExp.code);
			} else {
				// si experiment deja public, ne pas modifier sa date de release. 
				userExp.consolidatedPublicStatus = true;
			}
		} else {
			// si experiment privé, et study public alors experiment devient public avec date de release = date de cette soumission et non date de release du study
			if(isPublicAbstractStudy(abstStudyOfUserExp)) {
				userExp.consolidatedPublicStatus = true;
				userExp.releaseDate = new Date();
			} else {
				// si experiment privé et study privé alors experiment reste privé avec date de release = date de release du study
				userExp.consolidatedPublicStatus = false;
				if(abstStudyOfUserExp instanceof Study && ((Study) abstStudyOfUserExp).releaseDate != null) {
					userExp.releaseDate = ((Study) abstStudyOfUserExp).releaseDate;
				} 
			}
		}
		
		// Identifier les experiments avec changement de ref au sample et les exp avec changement de date de release qui impacteront les samples à modifier pour la releaseDate :

		// pb si userExp avec num d'accession sans releaseDate ici
		if(userExp.releaseDate == null ) {
			contextValidation.addError("updateExperimentForReleaseDate ", " experiment "+ userExp.code + " avec numeros d'accession, sans releaseDate ?");	
		}
		
		if(! experimentAPI.dao_checkObjectExist("code", userExp.code)) {
			// l'experiment n'a jamais ete enregistré dans base (cas d'un import de données produites hors NGLS-SUB) => On considere qu'on modifie sa date de release.
			if(!hExperimentToUpdateForReleaseDate.containsKey(userExp.code)) {
				hExperimentToUpdateForReleaseDate.put(userExp.code, userExp);
			}
		} else {
			Experiment oldExperiment = experimentAPI.get(userExp.code);
			if(isPublicExperiment(oldExperiment) && ! isPublicExperiment(userExp)) {
				contextValidation.addError("updateExperimentForReleaseDate ", " tentative de mettre l'experiment "+ userExp.code + " prive alors qu'il est public en base");					
			}
			if (oldExperiment.releaseDate == null) {
				// cas du userExp.releaseDate à null déja mis en erreur.
				if(!hExperimentToUpdateForReleaseDate.containsKey(userExp.code)) {
					hExperimentToUpdateForReleaseDate.put(userExp.code, userExp);	
				}
			} else {
				if (! oldExperiment.releaseDate.equals(userExp.releaseDate)) {
					if(!hExperimentToUpdateForReleaseDate.containsKey(userExp.code)) {
						hExperimentToUpdateForReleaseDate.put(userExp.code, userExp);
					}
				}
			}
			if ( ! oldExperiment.sampleCode.equals(userExp.sampleCode) ) {
				if(!hExperimentToUpdateForReleaseDate.containsKey(userExp.code)) {
					hExperimentToUpdateForReleaseDate.put(userExp.code, userExp);
				}
			}
		} 
		
	} // end updateExperimentForReleaseDate

	// synchronise les valeurs de certains champs du study
	public void synchronyseValuesInStudy(Study study, ContextValidation contextValidation) {
		//NGL-4803 : synchronisation de scientificName avec taxonId
		// si on supprime taxonId existant alors, supprimer scientificName
		// si on modifie taxonId existant alors recuperer scientificName
		//logger.debug("Je passe bien par synchronyseValuesInStudy");
		if(study.taxonId == null || StringUtils.isBlank(study.taxonId)) {
			//logger.debug("Je passe bien par synchronyseValuesInStudy, avec taxonId null");
			study.scientificName = null;
		} else {
			study.scientificName = taxonomyServices.getNcbiEbiScientificName(study.taxonId, contextValidation);
		}
		// synchroniser description et abstract :
		study.studyAbstract = study.description;
	}
	
	// synchronise les valeurs de certains champs de l'umbrella
	public void synchronyseValuesInUmbrella(Project umbrella, ContextValidation contextValidation) {
		//NGL-4803 : synchronisation de scientificName avec taxonId
		// si on supprime taxonId existant alors, supprimer scientificName
		// si on modifie taxonId existant alors recuperer scientificName

		if(StringUtils.isBlank(umbrella.taxonId)) {
			//logger.debug("RRRRRRRRRRRRRRRRRRRRRRRRRRR                        " + umbrella.taxonId);
			umbrella.scientificName = null;
		} else {
			umbrella.scientificName = taxonomyServices.getNcbiEbiScientificName(umbrella.taxonId, contextValidation);
		}

	}
	
}
