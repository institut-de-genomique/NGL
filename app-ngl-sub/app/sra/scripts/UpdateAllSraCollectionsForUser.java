package sra.scripts;


import javax.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import fr.cea.ig.ngl.dao.api.sra.AbstractSampleAPI;
import fr.cea.ig.ngl.dao.api.sra.AbstractStudyAPI;
import fr.cea.ig.ngl.dao.api.sra.ExperimentAPI;
import fr.cea.ig.ngl.dao.api.sra.ReadsetAPI;
import fr.cea.ig.ngl.dao.api.sra.SubmissionAPI;
import fr.cea.ig.ngl.dao.readsets.ReadSetsDAO;
import models.sra.submit.sra.instance.AbstractSample;
import models.sra.submit.sra.instance.AbstractStudy;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.sra.instance.Readset;
import models.sra.submit.sra.instance.Submission;
import services.SraEbiAPI;


/*
 * Met a jour les collections submission, study, sample, experiment pour remplacer le user william par user ngsrg
 * Met egalement à jour le studyCode et studySample dans experiment soumis si besoin
 * Exemple de lancement :
 * http://localhost:9000/sra/scripts/run/sra.scripts.UpdateAllSraCollectionsForUser

 * @author sgas
 *
 */
public class UpdateAllSraCollectionsForUser extends ScriptNoArgs {
	private final SubmissionAPI      submissionAPI;
	private final AbstractStudyAPI   abstractStudyAPI;
	private final AbstractSampleAPI  abstractSampleAPI;
	private final ExperimentAPI      experimentAPI;
	private final ReadSetsDAO        laboReadSetDAO;
	private final ReadsetAPI         readsetAPI;

	private final String ngsrgUser   = "ngsrg";    

	
	

	@Inject
	public UpdateAllSraCollectionsForUser (
					SubmissionAPI         submissionAPI,
					AbstractStudyAPI      abstractStudyAPI,
				    AbstractSampleAPI     abstractSampleAPI,
				    ExperimentAPI         experimentAPI,
				    ReadSetsDAO           laboReadSetDAO,
				    ReadsetAPI            readsetAPI,
				    SraEbiAPI                ebiAPI) {
		this.submissionAPI     = submissionAPI;
		this.abstractStudyAPI  = abstractStudyAPI;
		this.abstractSampleAPI = abstractSampleAPI;
		this.experimentAPI     = experimentAPI;
		this.laboReadSetDAO    = laboReadSetDAO;
	    this.readsetAPI        = readsetAPI;
	}
	


	public void updateSubmissionForUser() {
		for (Submission submission : submissionAPI.dao_all()) {
			if(StringUtils.isNotBlank(submission.adminComment)) {
				submission.traceInformation.createUser = ngsrgUser;
				submission.traceInformation.modifyDate = null;
				submission.traceInformation.modifyDate = null;
				submission.state.historical = null;
			} else {
				if(StringUtils.isNotBlank(submission.traceInformation.createUser) 
						&& submission.traceInformation.createUser.equals("william")) {
					submission.traceInformation.createUser= ngsrgUser;
				}
				if(StringUtils.isNotBlank(submission.traceInformation.modifyUser) 
						&& submission.traceInformation.modifyUser.equals("william")) {
					submission.traceInformation.modifyUser= ngsrgUser;
				}
			}
			submissionAPI.dao_saveObject(submission);
		}
	}	
	
	

	public void updateStudyForUser() {
		for (AbstractStudy study : abstractStudyAPI.dao_all()) {
			if(StringUtils.isNotBlank(study.adminComment)) {
				study.traceInformation.createUser = ngsrgUser;
				study.traceInformation.modifyDate = null;
				study.traceInformation.modifyDate = null;
				study.state.historical = null;
			} else {
				if(StringUtils.isNotBlank(study.traceInformation.createUser) 
						&& study.traceInformation.createUser.equals("william")) {
					study.traceInformation.createUser= ngsrgUser;
				}
				if(StringUtils.isNotBlank(study.traceInformation.modifyUser) 
						&& study.traceInformation.modifyUser.equals("william")) {
					study.traceInformation.modifyUser= ngsrgUser;
				}
			}
			abstractStudyAPI.dao_saveObject(study);
		}
	}	
	
	public void updateSampleForUser() {
		for (AbstractSample sample : abstractSampleAPI.dao_all()) {
			if(StringUtils.isNotBlank(sample.adminComment)) {
				sample.traceInformation.createUser = ngsrgUser;
				sample.traceInformation.modifyDate = null;
				sample.traceInformation.modifyDate = null;
				sample.state.historical = null;
			} else {
				if(StringUtils.isNotBlank(sample.traceInformation.createUser) 
						&& sample.traceInformation.createUser.equals("william")) {
					sample.traceInformation.createUser= ngsrgUser;
				}
				if(StringUtils.isNotBlank(sample.traceInformation.modifyUser) 
						&& sample.traceInformation.modifyUser.equals("william")) {
					sample.traceInformation.modifyUser= ngsrgUser;
				}
			}
			abstractSampleAPI.dao_saveObject(sample);
		}
	}			
	
	public void updateExperimentAndReadsetForUser() {
		for (Experiment experiment : experimentAPI.dao_all()) {	
			if (StringUtils.isNotBlank(experiment.adminComment)) {
				experiment.traceInformation.createUser = ngsrgUser;
				experiment.traceInformation.modifyDate = null;
				experiment.traceInformation.modifyDate = null;
				experiment.state.historical = null;
			} else {
				if(StringUtils.isNotBlank(experiment.traceInformation.createUser) 
						&& experiment.traceInformation.createUser.equals("william")) {
					experiment.traceInformation.createUser= ngsrgUser;
				}
				if(StringUtils.isNotBlank(experiment.traceInformation.modifyUser) 
						&& experiment.traceInformation.modifyUser.equals("william")) {
					experiment.traceInformation.modifyUser= ngsrgUser;
				}
			}
			// si experiment soumis à l'EBI completer studyCode et sampleCode si besoin
			if (StringUtils.isNotBlank(experiment.accession) && 
			(StringUtils.isBlank(experiment.studyCode) || StringUtils.isBlank(experiment.sampleCode) )) {
				AbstractStudy absStudy  = abstractStudyAPI.dao_findOne(DBQuery.is("accession", experiment.studyAccession));
				experiment.studyCode = absStudy.code;
				AbstractSample absSample = abstractSampleAPI.dao_findOne(DBQuery.is("accession", experiment.sampleAccession));
				experiment.sampleCode = absSample.code;
			}
			experimentAPI.dao_saveObject(experiment);
			
			// mettre à jour si besoin la collection sra.readset :
			if( ! readsetAPI.isObjectExist(experiment.readSetCode)) {
				// creation du readset (collection readset de ngl-sub)
				Readset readset = new Readset();
				readset.code = experiment.readSetCode;
				readset.experimentCode = experiment.code;
				readset.type = experiment.typePlatform.toLowerCase();
				readset.runCode = experiment.run.code;
				readsetAPI.dao_saveObject(readset);
			}
			// mettre a jour la collection ngl-bi.ReadSetIllumina pour submissionUser si besoin
			// Pour les 454, le readsetCode n'existe pas forcement dans la collection, ne pas declencher erreur
			if (laboReadSetDAO.isCodeExist(experiment.readSetCode)) {
				laboReadSetDAO.update(DBQuery.is("code", experiment.readSetCode),					
						DBUpdate.set("submissionUser", experiment.traceInformation.createUser));
			}
		}
	}


	@Override
	public void execute() throws Exception {
		//bilanForExperiments();
		updateStudyForUser();
		println("ok pour studies");
		updateSampleForUser();
		println("ok pour samples");
		updateExperimentAndReadsetForUser();
		println("ok pour experiment et readset");
		println("fin du traitement");
	}

}
