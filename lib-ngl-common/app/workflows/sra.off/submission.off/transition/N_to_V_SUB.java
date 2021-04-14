


//package workflows.sra.submission.transition;
//
//import javax.inject.Inject;
//
//import org.apache.commons.lang3.StringUtils;
//import org.mongojack.DBQuery;
//import org.mongojack.DBUpdate;
//
//import fr.cea.ig.MongoDBDAO;
//import fr.cea.ig.ngl.dao.api.sra.ExperimentAPI;
//import fr.cea.ig.ngl.dao.api.sra.SampleAPI;
//import fr.cea.ig.ngl.dao.api.sra.StudyAPI;
//import fr.cea.ig.ngl.dao.readsets.ReadSetsAPI;
//import models.laboratory.common.instance.State;
//import models.laboratory.run.instance.ReadSet;
//import models.sra.submit.common.instance.Sample;
//import models.sra.submit.common.instance.Study;
//import models.sra.submit.common.instance.Submission;
//import models.sra.submit.sra.instance.Experiment;
//import models.utils.InstanceConstants;
//import validation.ContextValidation;
//import workflows.sra.submission.Transition;
//
///**
// * Cascade le traceInformation et le state de la soumission à ses sous-objets. 
// * 
// * @author sgas
// *
// */
//public class N_to_V_SUB implements Transition<Submission> {
//	
//	private static final play.Logger.ALogger logger = play.Logger.of(N_to_V_SUB.class);
//	
//	private final StudyAPI      studyAPI;
//	private final SampleAPI     sampleAPI;
//	private final ExperimentAPI experimentAPI;
//	private final ReadSetsAPI   readsetAPI;
//	
//	@Inject
//	public N_to_V_SUB(StudyAPI studyAPI, SampleAPI     sampleAPI, ExperimentAPI experimentAPI, ReadSetsAPI   readsetAPI) {
//		this.studyAPI      = studyAPI;
//		this.sampleAPI     = sampleAPI;
//		this.experimentAPI = experimentAPI;
//		this.readsetAPI    = readsetAPI;
//    }
//
//	/**
//	 * Always successful.
//	 */
//	@Override
//	public void execute(ContextValidation contextValidation, Submission object, State nextState) {
//		// nothing to do, always successful
//	}
//	
//	/**
//	 * Cascade le traceInformation et le state de la soumission à ses sous-objets 
//	 */
//	@Override 
//	public void success(ContextValidation contextValidation, Submission submission, State nextState) {
//		logger.debug("dans applySuccessPostStateRules submission={} avec state.code='{}'", submission.code, submission.state.code);
//		if (StringUtils.isNotBlank(submission.studyCode)) {
//			Study study = studyAPI.dao_getObject(submission.studyCode);
//			if (study == null) {
//				logger.error("study {} absent de la base de données", submission.studyCode);
//			} else {
//				// Recuperer object study pour mettre historique des state traceInformation à jour:
//				studyAPI.dao_update(DBQuery.is("code", submission.studyCode), 
//									DBUpdate.set("state", submission.state)
//											.set("traceInformation", submission.traceInformation));
//				logger.debug("mise à jour du study avec state.code=" + submission.state.code);
//			}
//		}
//		// etat à propager dans samples à soumettre 
//		// donc dans collection sampleCodes qui ne contient que des samples et jamais d'ExternalSample
//		if (submission.sampleCodes != null) {
//			for (int i = 0; i < submission.sampleCodes.size() ; i++) {
//				Sample sample = MongoDBDAO.findByCode(InstanceConstants.SRA_SAMPLE_COLL_NAME, Sample.class, submission.sampleCodes.get(i));
//				sampleAPI.dao_update(DBQuery.is("code", sample.code),					
//									 DBUpdate.set("state", submission.state)
//									 		 .set("traceInformation", submission.traceInformation));
//			}
//		}
//		// etat à propager dans experiments à soumettre et readset: 
//		if (submission.experimentCodes != null) {
//			for (int i = 0; i < submission.experimentCodes.size() ; i++) {
//				Experiment experiment = MongoDBDAO.findByCode(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, Experiment.class, submission.experimentCodes.get(i));
//				if (experiment == null) {
//					logger.error("experiment {} absent de la base de données", submission.experimentCodes.get(i));
//					continue;
//				} else {
//					experimentAPI.dao_update(DBQuery.is("code", experiment.code),					
//							 DBUpdate.set("state",            submission.state)
//							 		 .set("traceInformation", submission.traceInformation));
//				}
//				// Update objet readSet :
//				ReadSet readset = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, experiment.readSetCode);
//				if (readset == null) {
//					logger.error("readset " + experiment.readSetCode + " absent de la base de données" );
//				} else {
//					readsetAPI.dao_update(DBQuery.is("code", experiment.readSetCode),					
//							              DBUpdate.set("submissionState",  submission.state)
//							                      .set("traceInformation", submission.traceInformation));
//				}
//			}
//		}
//	}
//
//	@Override
//	public void error(ContextValidation contextValidation, Submission object, State nextState) {
//		// no error as the execute is always successful
//	}
//
//}

