package sra.scripts;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;

import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import fr.cea.ig.ngl.dao.api.sra.AbstractSampleAPI;
import fr.cea.ig.ngl.dao.api.sra.AbstractStudyAPI;
import fr.cea.ig.ngl.dao.api.sra.ExperimentAPI;
import fr.cea.ig.ngl.dao.api.sra.SampleAPI;
import fr.cea.ig.ngl.dao.api.sra.StudyAPI;
import fr.cea.ig.ngl.dao.api.sra.SubmissionAPI;
import fr.cea.ig.ngl.dao.readsets.ReadSetsDAO;
import models.sra.submit.sra.instance.AbstractSample;
import models.sra.submit.sra.instance.AbstractStudy;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.sra.instance.ExternalSample;
import models.sra.submit.sra.instance.ExternalStudy;
import models.sra.submit.sra.instance.Sample;
import models.sra.submit.sra.instance.Study;
import models.sra.submit.sra.instance.Submission;
import services.SraEbiAPI;


/*
 * Bilan des collections study, sample, experiment non associé à une soumission
 * Exemple de lancement :
 * http://localhost:9000/sra/scripts/run/sra.scripts.BilanAllSraCollectionsNoSubmission
 * @author sgas
 *
 */
public class BilanAllSraCollectionsNoSubmission extends ScriptNoArgs {
	
	private final SubmissionAPI  		submissionAPI;
	private final StudyAPI       		studyAPI;
	private final SampleAPI       		sampleAPI;

	private final AbstractStudyAPI      abstractStudyAPI;
	private final AbstractSampleAPI     abstractSampleAPI;
	private final ExperimentAPI  		experimentAPI;
	


	
	

	@Inject
	public BilanAllSraCollectionsNoSubmission (SubmissionAPI submissionAPI,
					StudyAPI              studyAPI,
					SampleAPI             sampleAPI,
					AbstractStudyAPI      abstractStudyAPI,
				    AbstractSampleAPI     abstractSampleAPI,
				    ExperimentAPI         experimentAPI,
				    ReadSetsDAO           laboReadSetDAO,
				    SraEbiAPI                ebiAPI) {
		this.submissionAPI       = submissionAPI;
		this.studyAPI            = studyAPI;
		this.sampleAPI           = sampleAPI;
		this.abstractStudyAPI    = abstractStudyAPI;
		this.abstractSampleAPI   = abstractSampleAPI;
		this.experimentAPI       = experimentAPI;

	}
	


	public void bilanForExperiments () {
		int cpExp = 0;
		int cpExpNoSub = 0;
		int cpExpNoCreateUser = 0;
		int cpExpNoSubmissionDate = 0;
		
		Map<String, String> map_user =  new HashMap<>();

		
		Map<Date, String> map_date =  new HashMap<>();
		Iterable<Experiment> experiments = experimentAPI.dao_all();
		for (Experiment experiment : experiments) {	
			cpExp++;
			List <Submission> submissionList = submissionAPI.dao_find(DBQuery.in("experimentCodes", experiment.code).is("type", Submission.Type.CREATION)).toList();
			Submission submission = null;
			if (submissionList.size() == 0) {
				println(experiment.code + " associe a aucune submission");
				cpExpNoSub++;
			}  else {
				submission = submissionList.get(0);
			}
			
			if (experiment.firstSubmissionDate == null && experiment.state.code.equals("SUB-F")) {
				println(experiment.code + " avec state " + experiment.state.code + " et sans firstSubmissionDate");
				if (submission != null && submission.state.code.equals("SUB-F")) {
					experiment.firstSubmissionDate = submission.submissionDate;
				}
				cpExpNoSubmissionDate++;
			} else {
				if (!map_date.containsKey(experiment.firstSubmissionDate)) {
					map_date.put(experiment.firstSubmissionDate, null);
				}
			}
			if (StringUtils.isBlank(experiment.traceInformation.createUser)) {
				println(experiment.code + " avec state " + experiment.state.code + " et sans firstSubmissionDate");
				if (submission != null && submission.state.code.equals("SUB-F")) {
					experiment.traceInformation.createUser = submission.traceInformation.createUser;
				}
				cpExpNoCreateUser++;
			} else {
				if (!map_user.containsKey(experiment.traceInformation.createUser)) {
					map_user.put(experiment.traceInformation.createUser, null);
				}
			}
			experimentAPI.dao_saveObject(experiment);
		}
		println("Nombre d'experiment dans base = " + cpExp);
		println("Nombre d'experiment dans base associe a aucune soumission = " + cpExpNoSub);
		println("Nombre d'experiment dans base sans submissionDate = " + cpExpNoSubmissionDate);
		println("Nombre d'experiment dans base sans submissionUser = " + cpExpNoCreateUser);
		println("Liste des createUser dans les experiments de la base : ");
		for (Iterator<Entry<String, String>> iterator = map_user.entrySet().iterator(); iterator.hasNext();) {
			Entry<String, String> entry = iterator.next();
			println(entry.getKey());
		}
		println("Liste des dates de soumission dans les experiments de la base : ");
		for (Iterator<Entry<Date, String>> iterator = map_date.entrySet().iterator(); iterator.hasNext();) {
			Entry<Date, String> entry = iterator.next();
			println(entry.getKey().toString());
		}
	}


	public void bilanForSamples () {
		int cp = 0;
		int cpNoSub = 0;
		for (AbstractSample abstractSample : abstractSampleAPI.dao_all()) {	
			if(abstractSample instanceof ExternalSample) {
				continue;
			}
			Sample sample = sampleAPI.get(abstractSample.code);
			cp++;
			List <Submission> submissionList = submissionAPI.dao_find(DBQuery.in("sampleCodes", sample.code).is("type", Submission.Type.CREATION)).toList();
			if (submissionList.size() == 0) {
				println(sample.accession + " associe a aucune submission (" + sample.code + ")");
				cpNoSub++;
			}
			if (sample.firstSubmissionDate == null && sample.state.code.equals("SUB-F")) {
				println(sample.code + " avec state" + sample.state.code + " et sans firstSubmissionDate");
			}
		}
		println("Nombre de Sample dans base = " + cp);
		println("Nombre de Sample dans base associe a aucune soumission = " + cpNoSub);
	}
	

	public void bilanForStudies () {
		int cp = 0;
		int cpNoSub = 0;
		
		Calendar calendar = Calendar.getInstance();
		java.util.Date dateJour = calendar.getTime();
		
		for (AbstractStudy abstractStudy : abstractStudyAPI.dao_all()) {	
			if(abstractStudy instanceof ExternalStudy) {
				continue;
			}
			Study study = studyAPI.get(abstractStudy.code);
			cp++;
			List <Submission> submissionList = submissionAPI.dao_find(DBQuery.in("studyCode", study.code).is("type", Submission.Type.CREATION)).toList();
			if (submissionList.size() == 0) {
				String publicData = "false";
				if(study.releaseDate == null) {
					publicData = "undef";
				} else {
					if(study.releaseDate.before(dateJour)) {
						publicData = "true";
					}
				}
				println(study.accession + " associe a aucune submission (" + study.code + ")" + " et publicData = " + publicData);
				if (study.firstSubmissionDate == null && study.state.code.equals("SUB-F")) {
					println(study.code + " avec state" + study.state.code + " et sans firstSubmissionDate");
				}
				cpNoSub++;
			}
		}
		println("Nombre de Study dans base = " + cp);
		println("Nombre de Study dans base associe a aucune soumission = " + cpNoSub);
	}


	@Override
	public void execute() throws Exception {
		bilanForExperiments();
		println("fin bilan experiment");
		//bilanForSamples();
		//println("fin bilan samples");
		//bilanForStudies();
		//println("fin bilan study");
	}

}
