package sra.scripts;


import java.util.Date;

import fr.cea.ig.lfw.controllers.scripts.chunked.ScriptWithArgs;
import fr.cea.ig.ngl.dao.api.sra.ExperimentAPI;
import fr.cea.ig.ngl.dao.api.sra.SubmissionAPI;
import fr.cea.ig.ngl.dao.readsets.ReadSetsDAO;
import fr.cea.ig.ngl.dao.sra.AbstractSampleDAO;
import fr.cea.ig.ngl.dao.sra.AbstractStudyDAO;
import fr.cea.ig.ngl.dao.sra.ConfigurationDAO;
import fr.cea.ig.ngl.dao.sra.ExperimentDAO;
import fr.cea.ig.ngl.dao.sra.ExternalSampleDAO;
import fr.cea.ig.ngl.dao.sra.ExternalStudyDAO;
import fr.cea.ig.ngl.dao.sra.ReadsetDAO;
import fr.cea.ig.ngl.dao.sra.SampleDAO;
import fr.cea.ig.ngl.dao.sra.StudyDAO;
import fr.cea.ig.ngl.dao.sra.SubmissionDAO;
import models.sra.submit.common.instance.AbstractSample;
import models.sra.submit.common.instance.AbstractStudy;
import models.sra.submit.common.instance.ExternalSample;
import models.sra.submit.common.instance.ExternalStudy;
import models.sra.submit.common.instance.Sample;

import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import fr.cea.ig.ngl.NGLApplication;
import models.sra.submit.common.instance.Submission;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.util.SraException;
import models.utils.dao.DAOException;
import validation.ContextValidation;

/*
* Script pour deleter une soumission dans l'etat nouveau dans la base enlevant les samples et experiments si non utilises par ailleurs et 
* en remettant les readset au status NONE
* Exemple de lancement :
* http://localhost:9000/sra/scripts/run/sra.scripts.DeleteSubmissionAndCleanDatabase?submissionCode=code_soumission_1
* @author sgas
*
*/
//SGAS
//utiliser directement la methode SubmissionNewAPITools.rollbackSubmission recopiée ici pour pouvoir etre utilise sur prod pas encore dans nouveau workflow.
public class DeleteSubmissionAndCleanDatabase extends ScriptWithArgs<DeleteSubmissionAndCleanDatabase.MyParam> {
//	private static final play.Logger.ALogger logger = play.Logger.of(Debug_BDA.class);

	private final SubmissionAPI     submissionAPI;
//	private final ExperimentAPI     experimentAPI;
//	private final NGLApplication    app;

	private final SubmissionDAO         submissionDAO;
	private final StudyDAO              studyDAO;
	private final AbstractStudyDAO      abstractStudyDAO;
	private final ExternalStudyDAO      externalStudyDAO;
	private final SampleDAO             sampleDAO;
	private final AbstractSampleDAO     abstractSampleDAO;
	private final ExternalSampleDAO     externalSampleDAO;
	private final ExperimentDAO         experimentDAO;
	private final ReadsetDAO            readSetDAO;
	private final ConfigurationDAO      configurationDAO;
	private final ReadSetsDAO           laboReadSetDAO;

	
	private final String initialState = "N";
	private final String noneState = "NONE";
	private final String finalState = "F-SUB";

	
	@Inject
	public DeleteSubmissionAndCleanDatabase(SubmissionAPI     submissionAPI,
					 ExperimentAPI     experimentAPI,
					 SubmissionDAO       submissionDAO,
					  StudyDAO            studyDAO, 
		              AbstractStudyDAO    abstractStudyDAO,
		              ExternalStudyDAO    externalStudyDAO,
					  SampleDAO           sampleDAO,
					  AbstractSampleDAO   abstractSampleDAO,
		              ExternalSampleDAO   externalSampleDAO,
					  ExperimentDAO       experimentDAO,
					  ReadsetDAO          readSetDAO,
					  ConfigurationDAO    configurationDAO,
					  ReadSetsDAO         laboReadSetDAO,
					 
					 
					 
					 NGLApplication    app) {

		this.submissionAPI     = submissionAPI;
//		this.experimentAPI     = experimentAPI;
//		this.app               = app;
		
		this.submissionDAO     = submissionDAO;
		this.studyDAO          = studyDAO;
		this.abstractStudyDAO  = abstractStudyDAO;
		this.externalStudyDAO  = externalStudyDAO;
		this.sampleDAO         = sampleDAO;
		this.abstractSampleDAO = abstractSampleDAO;
		this.externalSampleDAO = externalSampleDAO;
		this.experimentDAO     = experimentDAO;
		this.readSetDAO        = readSetDAO;
		this.configurationDAO  = configurationDAO;
		this.laboReadSetDAO    = laboReadSetDAO;
		
	}


	// ma structure de controle et stockage des arguments de l'url
	public static class MyParam {
		public String submissionCode;
	}


	@Override
	public void execute(MyParam args) throws Exception {
		println("Demande de deletion de la soumission %s" , args.submissionCode);
		
		Submission submission = submissionDAO.getObject(args.submissionCode);
		if (submission != null) {
			println("Demande de deletion de la soumission %s" , submission.code);
		}
		if(!submission.state.code.equals(initialState)) {
			throw new RuntimeException("Seule une soumission à l'etat N peut etre deletée et ici la soumission est dans l'etat "+ submission.state.code);
		}
		
		ContextValidation validation = ContextValidation.createCreationContext("william");
		rollbackSubmission(submission, validation);
	}
	
	
	// methode SubmissionNewAPITools.rollbackSubmission recopiée ici pour pouvoir etre utilise sur prod pas encore dans nouveau workflow.
	public void rollbackSubmission(Submission submission, ContextValidation validation) throws SraException{
		// Si la soumission est connu de l'EBI on ne pourra pas l'enlever de la base :
		if (StringUtils.isNotBlank(submission.accession)){
			logger.debug("objet submission avec AC : submissionCode = "+ submission.code + " et submissionAC = "+ submission.accession);
			return;
		} 

		// Si la soumission concerne une release avec status "N-R" ou IW-SUB-R:
//		if (submission.release && (submission.state.code.equalsIgnoreCase(SUBR_N)||(submission.state.code.equalsIgnoreCase(SUBR_SMD_IW)))) {
//		if ((submission.type == Submission.Type.RELEASE) && 
//		    (submission.state.code.equalsIgnoreCase(SUBR_N) || (submission.state.code.equalsIgnoreCase(SUBR_SMD_IW)))) {
//			// detruire la soumission :
//			submissionDAO.deleteByCode(submission.code);
//			// remettre le status du study avec un status F-SUB
//			// La date de release du study est modifié seulement si retour positif de l'EBI pour release donc si status F-SUB
//			// study qui ne peut etre external si release.
//			studyDAO.update(DBQuery.is("code", submission.studyCode),
//					DBUpdate.set("state.code", SUB_F)
//					.set("traceInformation.modifyUser", validation.getUser())
//					.set("traceInformation.modifyDate", new Date()));
//			return;
//		} 

		// Si la soumission concerne une soumission primaire de données avec status != N :
//		if ( !submission.release && !submission.state.code.equalsIgnoreCase(SUB_N)) {
//		if ( submission.type == Submission.Type.CREATION && !submission.state.code.equalsIgnoreCase(SUB_N)) {
		if(!submission.state.code.equalsIgnoreCase(initialState)) {
			return;
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
//							DBUpdate.set("submissionState.code", NONE)
							DBUpdate.set("submissionState.code", "NONE")

							.set("traceInformation.modifyUser", validation.getUser())
							.set("traceInformation.modifyDate", new Date()));

					// Enlever les readsets de la collection sra :
					readSetDAO.deleteByCode(experiment.readSetCode);

					//						List <Submission> submissionList = MongoDBDAO.find(InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class, DBQuery.in("experimentCodes", experimentCode)).toList();
					List <Submission> submissionList = submissionDAO.dao_find(DBQuery.in("experimentCodes", experimentCode)).toList();
					if (submissionList.size() > 1) {
						for (Submission sub: submissionList) {
							//								System.out.println(experimentCode + " utilise par objet Submission " + sub.code);
							logger.debug(experimentCode + " utilise par objet Submission " + sub.code);
						}
					} else {
						// verifier qu'on ne detruit que des experiments avec un status N ou V_SUB
//						if (SUB_N.equals(experiment.state.code) || SUB_V.equals(experiment.state.code)){
							//								MongoDBDAO.deleteByCode(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, models.sra.submit.sra.instance.Experiment.class, experimentCode);
						if (initialState.equals(experiment.state.code)){
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
						try {
							externalSampleDAO.deleteByCode(sampleCode);
						} catch (DAOException e) {
							logger.debug(e.getMessage(), e);
							throw new SraException(e.getMessage(), e);
						}
					} else {
						Sample sample = (Sample) abstSample; 
					//	if ( SUB_N.equals(sample.state.code) ) {
						if ( initialState.equals(sample.state.code) ) {
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
					DBUpdate.set("state.code", noneState)
					.set("traceInformation.modifyUser", validation.getUser())
					.set("traceInformation.modifyDate", new Date()));
			logger.debug("state.code remis à '"+noneState+"' pour configuration "+submission.configCode);
		}

		// verifier que le study à l'etat N n'est pas utilisé par une autre soumission avant de remettre son etat à "NONE"
		if (StringUtils.isNotBlank(submission.studyCode)){
			List <Submission> submissionList2 = submissionDAO.dao_find(DBQuery.in("studyCode", submission.studyCode)).toList();
			if (submissionList2.size() == 1) {
				studyDAO.update(DBQuery.is("code", submission.studyCode).is("state.code", initialState),
						DBUpdate.set("state.code", noneState)
						.set("traceInformation.modifyUser", validation.getUser())
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
							if (finalState.equalsIgnoreCase(absStudy.state.code) ) {
								try {
									externalStudyDAO.deleteByCode(studyCode);
									logger.debug("deletion dans base pour study "+studyCode);
								} catch (DAOException e) {
									logger.debug(e.getMessage(), e);
									throw new SraException(e.getMessage(), e);
								}
							}
						} else { // instanceof Study
							if (initialState.equalsIgnoreCase(absStudy.state.code) ) {
								studyDAO.update(DBQuery.is("code", studyCode),
										DBUpdate.set("state.code", noneState)
										.set("traceInformation.modifyUser", validation.getUser())
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
	

}
