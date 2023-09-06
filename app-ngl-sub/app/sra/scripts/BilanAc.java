package sra.scripts;

import javax.inject.Inject;
import org.apache.commons.lang3.StringUtils;

import fr.cea.ig.lfw.controllers.scripts.chunked.ScriptWithArgs;
import fr.cea.ig.ngl.dao.api.sra.ExperimentAPI;
import fr.cea.ig.ngl.dao.api.sra.SampleAPI;
import fr.cea.ig.ngl.dao.api.sra.StudyAPI;
import fr.cea.ig.ngl.dao.api.sra.SubmissionAPI;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.sra.instance.Sample;
import models.sra.submit.sra.instance.Study;
import models.sra.submit.sra.instance.Submission;


/*
 * Script a lancer pour avoir les numeros d'accession associés à une soumission ou plusieurs soumissions.
 * Exemple de lancement :
 * http://localhost:9000/sra/scripts/run/sra.scripts.BilanAc?codes=code_soumission_1&codes=code_soumission_2
 * @author sgas
 *
 */
public class BilanAc extends ScriptWithArgs<BilanAc.MyParam>{
	
	private final SubmissionAPI submissionAPI;
	private final StudyAPI studyAPI;
	private final SampleAPI sampleAPI;
	private final ExperimentAPI experimentAPI;
	
	

	@Inject
	public BilanAc (SubmissionAPI submissionAPI,
					StudyAPI      studyAPI,
				    SampleAPI     sampleAPI,
				    ExperimentAPI experimentAPI) {
		this.submissionAPI = submissionAPI;
		this.studyAPI      = studyAPI;
		this.sampleAPI     = sampleAPI;
		this.experimentAPI = experimentAPI;
	}

	
	// ma structure de controle et stockage des arguments de l'url
	public static class MyParam {
		public String[] codes;
	}
	

	@Override
	public void execute(MyParam args) throws Exception {	
		for (String submissionCode : args.codes) {
			Submission submission = submissionAPI.dao_getObject(submissionCode);			
			if (submission == null) {
				println(" %s n'existe pas dans la base", submissionCode);
				return;
			}
			if (submission.type == Submission.Type.RELEASE) {
				println(" %s est une soumission pour release du study %s", submissionCode, submission.studyCode);
			} else {
				println("Liste des AC attribues pour la soumission %s", submissionCode);
				if (submission.studyCode != null && StringUtils.isNotBlank(submission.studyCode)) {
					Study study = studyAPI.dao_getObject(submission.studyCode);
					if (StringUtils.isNotBlank(study.accession)) {
						println("   - studyCode = %s, AC = %s,  bioproject= %s", study.code, study.accession, study.externalId);
					} else {
						println("   - studyCode = %s", study.code);
					}
				}
				if (submission.sampleCodes != null) {
					for (int i = 0; i < submission.sampleCodes.size() ; i++) {
						Sample sample = sampleAPI.dao_getObject(submission.sampleCodes.get(i));
						if (StringUtils.isNotBlank(sample.accession)) {
							println("   - sampleCode = %s, AC = %s,  externalId= %s", sample.code, sample.accession, sample.externalId);
						} else {
							println("   - sampleCode = %s", sample.code);
						}
					}					
				}
				if (submission.experimentCodes != null) {
					for (int i = 0; i < submission.experimentCodes.size() ; i++) {
						Experiment experiment = experimentAPI.get(submission.experimentCodes.get(i));
						if (StringUtils.isNotBlank(experiment.accession)) {
							println("   - experimentCode = %s, AC = %s", experiment.code, experiment.accession);
						} else {
							println("   - experimentCode = %s", experiment.code);
						}

					}
					for (int i = 0; i < submission.experimentCodes.size() ; i++) {
						Experiment experiment = experimentAPI.get(submission.experimentCodes.get(i));
						if (StringUtils.isNotBlank(experiment.run.accession)) {
							println("   - runCode = %s, AC = %s", experiment.run.code, experiment.run.accession);
						} else {
							println("   - runCode = %s", experiment.run.code);
						}
					}					
				}
				println();
			}	
		}
	}

}
