package sra.api.submission;

import static ngl.refactoring.state.SRASubmissionStateNames.NONE;
import static ngl.refactoring.state.SRASubmissionStateNames.SUBR_N;
import static ngl.refactoring.state.SRASubmissionStateNames.SUBR_SMD_IW;
import static ngl.refactoring.state.SRASubmissionStateNames.SUB_F;
import static ngl.refactoring.state.SRASubmissionStateNames.SUB_N;
import static ngl.refactoring.state.SRASubmissionStateNames.SUB_V;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import fr.cea.ig.ngl.dao.readsets.ReadSetsDAO;
import fr.cea.ig.ngl.dao.sra.AbstractSampleDAO;
import fr.cea.ig.ngl.dao.sra.AbstractStudyDAO;
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
import models.sra.submit.common.instance.AbstractSample;
import models.sra.submit.common.instance.AbstractStudy;
import models.sra.submit.common.instance.ExternalSample;
import models.sra.submit.common.instance.ExternalStudy;
import models.sra.submit.common.instance.IStateReference;
import models.sra.submit.common.instance.Project;
import models.sra.submit.common.instance.Sample;
import models.sra.submit.common.instance.Study;
import models.sra.submit.common.instance.Submission;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.util.SraException;
import models.utils.dao.DAOException;

public class SubmissionNewAPITools {
	private static final play.Logger.ALogger logger = play.Logger.of(SubmissionNewAPITools.class);
	private final SubmissionDAO         submissionDAO;
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
	
	@Inject
	SubmissionNewAPITools(SubmissionDAO       submissionDAO,
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
						  ReadSetsDAO         laboReadSetDAO) {
		this.submissionDAO     = submissionDAO;
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

	/**
	 * Cascade le traceInformation et le state de la soumission à ses sous-objets et aux readsets 
	 * dans database. Declenche une erreur si la cascade n'a pas pu etre réalisée.
	 * @param  submission   submission
	 * @throws SraException error
	 */
	public void updateSubmissionChildObject(Submission    submission) throws SraException {
		//      Constructeur par injection donc  utiliser injection.
		//		StudyDAO studyDAO = new StudyDAO();
		//		SampleDAO sampleDAO = new SampleDAO();
		//		ExperimentDAO experimentDAO = new ExperimentDAO();
		
		if (StringUtils.isNotBlank(submission.ebiProjectCode)) {
			Project project = projectDAO.getObject(submission.ebiProjectCode);
			if (project == null) {
				String message = "Le project avec le code " + submission.ebiProjectCode 
						+ " indiquee dans la submission " + submission.code 
						+ " n'existe pas dans la base";
				logger.debug(message);
				throw new SraException("updateSubmissionChildObject", message);
			} 
			// Recuperer object project pour mettre historique des state et traceInformation à jour:
			project.updateStateAndTrace(submission.state);
			projectDAO.update(DBQuery.is("code", project.code), 
					DBUpdate.set("state", project.state)
					.set("traceInformation", project.traceInformation));
			//logger.debug("Mise à jour du project avec state.code=" + project.state.code);
		}
		
		if (StringUtils.isNotBlank(submission.studyCode)) {
			Study study = studyDAO.getObject(submission.studyCode);
			if (study == null) {
				String message = "Le study avec le code " + submission.studyCode 
						+ " indiquee dans la submission " + submission.code 
						+ " n'existe pas dans la base";
				logger.debug(message);
				throw new SraException("updateSubmissionChildObject", message);
			} 
			// Recuperer object study pour mettre historique des state et traceInformation à jour:
			study.updateStateAndTrace(submission.state);
			studyDAO.update(DBQuery.is("code", study.code), 
					DBUpdate.set("state", study.state)
					.set("traceInformation", study.traceInformation));
			//logger.debug("Mise à jour du study avec state.code=" + study.state.code);
		}
		// etat à propager dans samples à soumettre 
		// donc dans collection sampleCodes qui ne contient que des samples et jamais d'ExternalSample
		if (submission.sampleCodes != null) {
			for (int i = 0; i < submission.sampleCodes.size() ; i++) {
				Sample sample = sampleDAO.getObject(submission.sampleCodes.get(i));
				if (sample == null) {
					String message = "Le sample avec le code " + submission.sampleCodes.get(i) 
					+ " indiquee dans la submission " + submission.code 
					+ " n'existe pas dans la base";
					logger.debug(message);
					throw new SraException("updateSubmissionChildObject", message);
				}
				//sample.setState(submission.state);
				sample.updateStateAndTrace(submission.state);
				sampleDAO.update(DBQuery.is("code", sample.code),					
						DBUpdate.set("state", sample.state)
						.set("traceInformation", sample.traceInformation));
			}
		}
		// etat à propager dans experiments à soumettre et readset: 
		if (submission.experimentCodes != null) {
			for (int i = 0; i < submission.experimentCodes.size() ; i++) {
				Experiment experiment = experimentDAO.getObject(submission.experimentCodes.get(i));
				if (experiment == null) {
					String message = "L'experiment avec le code " 
							+ submission.experimentCodes.get(i)  
							+ " indiquee dans la submission " + submission.code 
							+ " n'existe pas dans la base";
					logger.debug(message);
					throw new SraException(message);
				} 
				experiment.updateStateAndTrace(submission.state);
				experimentDAO.update(DBQuery.is("code", experiment.code),					
						DBUpdate.set("state", experiment.state)
						.set("traceInformation", experiment.traceInformation));
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
					updateLaboReadsetForSubmissionStateAndTrace(laboreadset, submission.state.code, submission.state.user);
					laboReadSetDAO.update(DBQuery.is("code", laboreadset.code),					
							DBUpdate.set("submissionState", laboreadset.submissionState)
							.set("traceInformation.modifyDate", laboreadset.traceInformation.modifyDate)
							.set("traceInformation.modifyUser", laboreadset.traceInformation.modifyUser));					
					// pas de sauvegarde des etats dans la collection sra.readSet car pas de state
				}
			}
		}
	}

	

	public void rollbackSubmission(Submission submission, String user) throws SraException{
		switch(submission.type) {
		case  UPDATE :
			throw new SraException("Deletion impossible pour la soumission " + submission.code + " de type CREATION");
		case RELEASE :
			 if (submission.state.code.equalsIgnoreCase(SUBR_N) || (submission.state.code.equalsIgnoreCase(SUBR_SMD_IW))) {
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
			logger.debug("demande deletion d'une soumission de type creation");
			rallbackCreationSubmission(submission, user);
			break;
		default:
			//throw new SubmissionTypeException(submission);
			throw new SraException("Type de soumission non gerée :" + submission.type 
			+ " pour la soumission " + submission.code);
		}
	}
		
	
	public void rallbackCreationSubmission(Submission submission, String user) throws SraException{
		// Si la soumission est connu de l'EBI on ne pourra pas l'enlever de la base :
		if (StringUtils.isNotBlank(submission.accession)){
			logger.debug("objet submission avec AC : submissionCode = "+ submission.code + " et submissionAC = "+ submission.accession);
			throw new SraException("Deletion impossible pour la soumission " + submission.code + " avec un AC :" + submission.accession);
		} 
		if ( submission.type != Submission.Type.CREATION) {
			throw new SraException("Deletion impossible pour la soumission " + submission.code + " avec type '" + submission.type + "'");
		}
		// Si la soumission concerne une soumission primaire de données avec status != N :
		if ( !submission.state.code.equalsIgnoreCase(SUB_N) && !submission.state.code.equalsIgnoreCase(SUB_V)) {
			throw new SraException("Deletion impossible pour la soumission " + submission.code + " avec etat " + submission.state.code + " != SUB-N ou SUB-V");
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

					//						List <Submission> submissionList = MongoDBDAO.find(InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class, DBQuery.in("experimentCodes", experimentCode)).toList();
					List <Submission> submissionList = submissionDAO.dao_find(DBQuery.in("experimentCodes", experimentCode)).toList();
					if (submissionList.size() > 1) {
						for (Submission sub: submissionList) {
							//								System.out.println(experimentCode + " utilise par objet Submission " + sub.code);
							logger.debug(experimentCode + " utilise par objet Submission " + sub.code);
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
							logger.debug(experimentCode + " non delété dans base car status = " + experiment.state.code);
						}
					}
				}
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
						logger.debug(sampleCode + " utilise par objet Submission " + sub.code);
					}
				} else {
					//						abstractSampleAPI.delete(sampleCode);

					// verifier qu'on ne detruit que des samples avec un status N ou bien des samples externes:
					if (abstSample instanceof ExternalSample) {
						logger.debug("C'est bien un externalSample pour " + sampleCode);
						try {
							externalSampleDAO.deleteByCode(sampleCode);
						} catch (DAOException e) {
							logger.debug(e.getMessage(), e);
							throw new SraException(e.getMessage(), e);
						}
					} else {
						Sample sample = (Sample) abstSample; 
						if ( SUB_N.equals(sample.state.code) || SUB_V.equals(sample.state.code) ) {
							try {
								sampleDAO.deleteByCode(sampleCode);
							} catch (DAOException e) {
								logger.debug(e.getMessage(), e);
								throw new SraException(e.getMessage(), e);
							}
						} else {
							logger.debug("Pas de deletion dans base du sample interne " + sampleCode + "avec statut= "+sample.state.code);
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
			logger.debug("state.code remis à '" + NONE + "' pour configuration " + submission.configCode);
		}

		// verifier que le study à l'etat N n'est pas utilisé par une autre soumission avant de remettre son etat à "NONE"
		if (StringUtils.isNotBlank(submission.studyCode)){
			List <Submission> submissionList2 = submissionDAO.dao_find(DBQuery.in("studyCode", submission.studyCode)).toList();
			if (submissionList2.size() == 1) {
				studyDAO.update(DBQuery.is("code", submission.studyCode).in("state.code", SUB_N, SUB_V),
						DBUpdate.set("state.code", NONE)
						.set("traceInformation.modifyUser", user)
						.set("traceInformation.modifyDate", new Date()));	
				logger.debug("state.code remis à '\"+NONE+\"' pour study "+submission.studyCode);
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
						logger.debug(studyCode + " utilise par objet Submission " + sub.code);
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
									logger.debug("deletion dans base pour study "+studyCode);
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
		logger.debug("deletion dans base pour submission "+submission.code);
		submissionDAO.deleteByCode(submission.code);
	}
	
	
	//SGAS 
	// Pas testee
	// Permet d'inactiver une soumission ne contenant aucun sample ou study, pour laquelle
	// les numeros d'accession obtenus par l'EBI ont ete supprimés après coup par l'EBI (à notre demande).
	// les AC de la soumission, des runs, et des experiments sont remplaces par 
	// la valeur suppressed_AC, tandis que les readsets sont rendus disponibles pour une nouvelle
	// soumission.
	public void inactiveCreationSubmissionWithoutSampleNorStudy(Submission submission, String user) throws SraException{
		// Si la soumission est inconnu de l'EBI, sucun sens de l'inactiver
		if (StringUtils.isBlank(submission.accession)){
			logger.debug("objet submission avec AC : submissionCode = "+ submission.code + " et submissionAC = "+ submission.accession);
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
							logger.debug(experimentCode + " utilise par objet Submission " + sub.code);
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
								logger.debug(e.getMessage(), e);
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
			logger.debug("state.code remis à '"+NONE+"' pour configuration "+submission.configCode);
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


		logger.debug("deletion dans base pour submission "+submission.code);
		submissionDAO.deleteByCode(submission.code);
	}
	
	/**
	 * Met la soumission et tous ses objets enfants avec le createUser indiqué sans 
	 * modifier le traceInformation.modifyDate.
	 * @param  submissionCode   submissionCode
	 * @param  user             user
	 * @throws SraException error
	 */
	public void updateSubmissionChildObjectForUser(String submissionCode, String user) throws SraException {
		Submission submission = submissionDAO.getObject(submissionCode);
		if (submission == null) {
			throw new SraException("updateSubmissionChildObjectForUser", "La soumission " + submissionCode + " n'existe pas dans la base");
		}
		
		if (StringUtils.isNotBlank(submission.ebiProjectCode)) {
			Project project = projectDAO.getObject(submission.ebiProjectCode);
			if (project == null) {
				String message = "Le project avec le code " + submission.ebiProjectCode 
						+ " indiquee dans la submission " + submission.code 
						+ " n'existe pas dans la base";
				logger.debug(message);
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
				logger.debug(message);
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
					logger.debug(message);
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
					logger.debug(message);
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
	
}
