package sra.scripts;


import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import fr.cea.ig.lfw.controllers.scripts.buffered.Script;
import fr.cea.ig.ngl.dao.api.sra.AbstractSampleAPI;
import fr.cea.ig.ngl.dao.api.sra.AbstractStudyAPI;
import fr.cea.ig.ngl.dao.api.sra.ExperimentAPI;
import fr.cea.ig.ngl.dao.api.sra.ReadsetAPI;
import fr.cea.ig.ngl.dao.api.sra.SubmissionAPI;
import fr.cea.ig.ngl.dao.readsets.ReadSetsDAO;
import models.sra.submit.sra.instance.AbstractSample;
import models.sra.submit.sra.instance.AbstractStudy;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.sra.instance.Submission;
import models.sra.submit.util.SraException;
import services.SraEbiAPI;


/*
 * Change le user de la soumission et de tous ses objets enfants.
 * Exemple de lancement :
 * http://localhost:9000/sra/scripts/run/sra.scripts.changeUserForSubmissionAndChildren?submissionCode=code_1&user=sgas

 * @author sgas
 *
 */
public class changeUserForSubmissionAndChildren extends Script<changeUserForSubmissionAndChildren.MyParam>{
	private final SubmissionAPI      submissionAPI;
	private final AbstractStudyAPI   abstractStudyAPI;
	private final AbstractSampleAPI  abstractSampleAPI;
	private final ExperimentAPI      experimentAPI;
	private final ReadSetsDAO        laboReadSetDAO;

	
	

	@Inject
	public changeUserForSubmissionAndChildren (
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
	}
	


	public static class MyParam {
		public String user;
		public String submissionCode;
	}	

	
	public void updateSubmissionForUser(Submission submission, String user) {
		if (submission == null) {
			throw new SraException("Aucun objet Submission à mettre à jour ?");
		}
		submission.traceInformation.createUser = user;
		submissionAPI.dao_saveObject(submission);
	}
	
	public void updateStudyForUser(List<String> studyCodes, String user) {
		for (String studyCode : studyCodes) {
			AbstractStudy study = abstractStudyAPI.dao_getObject(studyCode);
			study.traceInformation.createUser = user;
			abstractStudyAPI.dao_saveObject(study);
		}
	}	
	
	public void updateSampleForUser(List<String> sampleCodes, String user) {
		for (String  sampleCode : sampleCodes) {
			AbstractSample sample = abstractSampleAPI.dao_getObject(sampleCode);
			sample.traceInformation.createUser = user;
			abstractSampleAPI.dao_saveObject(sample);
		}
	}			
	
	public void updateExperimentAndReadsetForUser(List<String> experimentCodes, String user) {
		for (String experimentCode : experimentCodes) {	
			Experiment experiment = experimentAPI.get(experimentCode);
			experiment.traceInformation.createUser = user;
			experimentAPI.dao_saveObject(experiment);
			
			// mettre a jour la collection ngl-bi.ReadSetIllumina pour submissionUser si besoin
			// Pour les 454, le readsetCode n'existe pas forcement dans la collection, ne pas declencher erreur
			if (laboReadSetDAO.isCodeExist(experiment.readSetCode)) {
				laboReadSetDAO.update(DBQuery.is("code", experiment.readSetCode),					
						DBUpdate.set("submissionUser", experiment.traceInformation.createUser));
			}
		}
	}


	


	@Override
	public void execute(MyParam args) throws Exception {
		String submissionCode = args.submissionCode;
		String user = args.user;
		Submission submission = submissionAPI.dao_getObject(submissionCode);
		if (submission == null) {
			println("Aucune soumission dans la base pour le code " + submissionCode);
			return;
		}
		updateSubmissionForUser(submission, user);
		if (StringUtils.isNotBlank(submission.studyCode)) {
			List<String> studyCodes = new ArrayList<String>();
			studyCodes.add(submission.studyCode);
			updateStudyForUser(studyCodes, user);
		}
		updateSampleForUser(submission.sampleCodes, user);
		updateExperimentAndReadsetForUser(submission.experimentCodes, user);
		println("submission et sous-objets de " + submissionCode + " mis au nom de l'utilisateur " + user);
	}

}
