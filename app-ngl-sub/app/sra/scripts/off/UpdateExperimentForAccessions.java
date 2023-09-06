package sra.scripts.off;


import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;


import fr.cea.ig.lfw.controllers.scripts.buffered.Script;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.sra.AbstractSampleAPI;
import fr.cea.ig.ngl.dao.api.sra.AbstractStudyAPI;
import fr.cea.ig.ngl.dao.api.sra.ConfigurationAPI;
import fr.cea.ig.ngl.dao.api.sra.ExperimentAPI;
import fr.cea.ig.ngl.dao.api.sra.SampleAPI;
import fr.cea.ig.ngl.dao.api.sra.SubmissionAPI;
import fr.cea.ig.ngl.dao.readsets.ReadSetsAPI;
import models.sra.submit.sra.instance.AbstractSample;
import models.sra.submit.sra.instance.AbstractStudy;
import models.sra.submit.sra.instance.Experiment;


/*
 * Met a jour les experiment pour studyAccession et sampleAccession
 * Exemple de lancement :
 * http://localhost:9000/sra/scripts/run/sra.scripts.off.UpdateExperimentForAccessions
 * @author sgas
 *
 */
public class UpdateExperimentForAccessions extends Script<UpdateExperimentForAccessions.MyParam>{
	private static final play.Logger.ALogger logger = play.Logger.of(UpdateExperimentForAccessions.class);

	private final SubmissionAPI     submissionAPI;
//	private final ConfigurationAPI  configurationAPI;
	private final AbstractStudyAPI  abstractStudyAPI;
	private final AbstractSampleAPI abstractSampleAPI;
//	private final SampleAPI         sampleAPI;
	private final ExperimentAPI     experimentAPI;
	private final NGLApplication    app;

	@Inject
	public UpdateExperimentForAccessions(SubmissionAPI     submissionAPI,
					 ConfigurationAPI  configurationAPI,
					 AbstractStudyAPI  abstractStudyAPI,
					 AbstractSampleAPI abstractSampleAPI,
					 SampleAPI         sampleAPI,
					 ExperimentAPI     experimentAPI,
					 ReadSetsAPI       readsetAPI,
					 NGLApplication    app) {

		this.submissionAPI     = submissionAPI;
//		this.configurationAPI  = configurationAPI;
		this.abstractStudyAPI  = abstractStudyAPI;
		this.abstractSampleAPI = abstractSampleAPI;
//		this.sampleAPI         = sampleAPI;
		this.experimentAPI     = experimentAPI;
		this.app               = app;
	}


	// ma structure de controle et stockage des arguments de l'url
	public static class MyParam {
	}


	@Override
	public void execute(MyParam args) throws Exception {
		printfln("debut traitement des experiments");
		boolean update = false;
		for (Experiment experiment : experimentAPI.dao_all()) {
			String user = "ngsrg";
			
			if( experiment.studyAccession == null && StringUtils.isNotBlank(experiment.studyCode) ) {
				if (! abstractStudyAPI.dao_checkObjectExist("code", experiment.studyCode)) {
					printfln("pour l' experiment %s, le studyCode %s n'existe pas dans base", experiment.code, experiment.studyCode);				
				} else {
					AbstractStudy abstractStudy = abstractStudyAPI.dao_findOne(DBQuery.and(DBQuery.in("code", experiment.studyCode)));
					if( abstractStudy != null && StringUtils.isNotBlank(abstractStudy.accession)) {
						printfln("Pour l'experiment %s, mise à jour du studyAccession %s", experiment.code, abstractStudy.accession);				
						experiment.studyAccession = abstractStudy.accession;
						update = true;
					}
				}
			}
			if( experiment.sampleAccession == null && StringUtils.isNotBlank(experiment.sampleCode)) {
				if (! abstractSampleAPI.dao_checkObjectExist("code", experiment.sampleCode)) {
					printfln("pour l' experiment %s, le sampleCode %s n'existe pas dans base", experiment.code, experiment.sampleCode);				
				} else {
					AbstractSample abstractSample = abstractSampleAPI.dao_findOne(DBQuery.and(DBQuery.in("code", experiment.sampleCode)));
					if( abstractSample != null && StringUtils.isNotBlank(abstractSample.accession)) {
						printfln("Pour l'experiment %s, mise à jour du sampleAccession %s", experiment.code, abstractSample.accession);				
						experiment.sampleAccession = abstractSample.accession;
						update = true;
					}
				}
			}	
			if (! update) {
				update = false;
				continue;
			}
			
			//printfln("experimentCode :%s", experiment.code);
			
			//if ((experiment.traceInformation.creationDate == null) || (experiment.traceInformation.createUser == null)) {
			//printfln("ok pour check avec experimentCode :%s", experiment.code);
//			List <Submission> submissionList = submissionAPI.dao_find(DBQuery.in("experimentCodes", experiment.code).is("type", Submission.Type.CREATION)).toList();
//			if (submissionList != null) {
//				if (submissionList.size() > 0) {
//					experiment.traceInformation.creationDate = submissionList.get(0).traceInformation.creationDate;
//					experiment.traceInformation.createUser = submissionList.get(0).traceInformation.createUser;
//				}
//			}
			experimentAPI.dao_saveObject(experiment);
			
//			ContextValidation contextValidation = ContextValidation.createUpdateContext(user); // on veut updater l'experiment sans run pour son r
//			boolean update = true;
//			if (experiment.traceInformation.modifyUser== null || experiment.traceInformation.modifyDate== null) {
//				experiment.traceInformation.modifyUser = "william";
//				experiment.traceInformation.modifyDate = new Date();
//				update = false;
//			}
//			experiment.validate(contextValidation);
//			if(contextValidation.hasErrors()) {
//				contextValidation.displayErrors(logger);
//				println(Json.prettyPrint(app.errorsAsJson(contextValidation.getErrors())));				
//			} else {
//				if(!update) { // on ne veut pas changer dans  base, les valeurs ont ete ajoutees uniquement pour la validation
//					experiment.traceInformation.modifyUser = null;
//					experiment.traceInformation.modifyDate = null;
//				}
//				experimentAPI.dao_saveObject(experiment);
//				printfln("ok pour experiment.code=%s", experiment.code);				
//			}
		}
		printfln("fin traitement des experiments");
	}

}


