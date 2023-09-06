package sra.scripts.generic;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.Script;
import fr.cea.ig.ngl.dao.api.sra.AbstractSampleAPI;
import fr.cea.ig.ngl.dao.api.sra.AbstractStudyAPI;
import fr.cea.ig.ngl.dao.api.sra.ExperimentAPI;
import fr.cea.ig.ngl.dao.api.sra.SubmissionAPI;
import models.laboratory.common.instance.State;
import models.sra.submit.sra.instance.AbstractSample;
import models.sra.submit.sra.instance.AbstractStudy;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.sra.instance.Submission;
import models.utils.InstanceConstants;
import sra.api.submission.SubmissionNewAPI;
import sra.api.submission.SubmissionNewAPITools;

//http://localhost:9000/sra/scripts/run/sra.scripts.generic.ForceDeletionSubmission?codes=code_soumission_1

public class ForceDeletionSubmission extends Script<ForceDeletionSubmission.MyParam>{
	

	private final SubmissionAPI         submissionAPI;
	private final SubmissionNewAPITools submissionNewAPITools;
	private final AbstractSampleAPI     abstractSampleAPI;
	private final AbstractStudyAPI      abstractStudyAPI;
	private final ExperimentAPI         experimentAPI;

	//private static final play.Logger.ALogger logger = play.Logger.of(ForceDeletionSubmission.class);

	
	@Inject
	public ForceDeletionSubmission (SubmissionAPI       submissionAPI, 
											 SubmissionNewAPI      submissionNewAPI,
											 AbstractSampleAPI     abstractSampleAPI,
											 AbstractStudyAPI      abstractStudyAPI,
											 ExperimentAPI         experimentAPI,
											 SubmissionNewAPITools submissionNewAPITools) {
		this.submissionAPI         = submissionAPI;
		this.submissionNewAPITools = submissionNewAPITools;
		this.abstractSampleAPI     = abstractSampleAPI;
		this.abstractStudyAPI      = abstractStudyAPI;
		this.experimentAPI         = experimentAPI;



	}
		
	// ma structure de controle et stockage des arguments de l'url
	public static class MyParam {
		//public String state;
		//public int code;
		public String[] codes;
	}
	

	@Override
	public void execute(MyParam args) throws Exception {
	
		for (String submissionCode : args.codes) {
			printfln(" submissionCode : %s", submissionCode);
	
			Submission submission = MongoDBDAO
					.findOne(InstanceConstants.SRA_SUBMISSION_COLL_NAME, 
							Submission.class, DBQuery.is("code", submissionCode));
			if (submission == null) {
				printfln("'%s' n'existe pas dans la base", submissionCode);
				return;
			}
			if(!submission.type.equals(Submission.Type.CREATION)) {
				printfln("'%s' est une soumission de type '%s' pour laquelle le script ne s'applique pas", submissionCode, submission.type);
				return;
			}				
			// installation du state avec etat N sans passer par le workflow car transition 
			// FE-SUB vers N non autorisee :
			String user = submission.state.user;
			
			// supprimer tous les AC de la soumission et des ses enfants et sauver en base :
			submission.accession = null;
			submissionAPI.dao_saveObject(submission);

			for (String sampleCode : submission.sampleCodes) {
				AbstractSample abstSample = abstractSampleAPI.get(sampleCode);
				if(abstSample == null) {
					continue;
				}
				abstSample.accession = null;
				abstSample.externalId = null;
				abstractSampleAPI.dao_saveObject(abstSample);
			}
			if(StringUtils.isNotBlank(submission.studyCode)) {
				AbstractStudy abstStudy = abstractStudyAPI.get(submission.studyCode);
				if(abstStudy == null) {
					continue;
				}
				abstStudy.accession = null;
				abstStudy.externalId = null;
				abstractStudyAPI.dao_saveObject(abstStudy);
			}
			for (String expCode : submission.experimentCodes) {
				Experiment exp = experimentAPI.get(expCode);
				if(exp== null) {
					continue;
				}
				exp.accession = null;
				experimentAPI.dao_saveObject(exp);
				
			}	
			
			// mettre la soumission et ses enfants dans l'etat SUB-N sans passer par le workflow et sauver dans base:
			String stateCode = "SUB-N";
			State nextState = new State(stateCode, user);
			submission.state.code = nextState.code;
			submissionAPI.dao_saveObject(submission);
			submissionNewAPITools.updateSubmissionChildObject(submission);	
			printfln("ZZZZZZZZZZ      Suppression des AC dans la Soumission '%s' et soumission mise dans l'etat '%s' ", submission.code, stateCode);
			
			submissionNewAPITools.rollbackSubmission(submission, user);
			println("Supression de la base de la soumission "+ submission.code);
		}

	}


}
