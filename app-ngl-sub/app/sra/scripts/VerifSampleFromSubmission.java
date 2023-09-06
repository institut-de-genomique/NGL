package sra.scripts;



import javax.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import fr.cea.ig.ngl.dao.api.sra.SampleAPI;
import fr.cea.ig.ngl.dao.api.sra.SubmissionAPI;
import models.sra.submit.sra.instance.Sample;
import models.sra.submit.sra.instance.Submission;


/*
 * Script a lancer pour verifier sample.firstSubmissionDate, sample.state.code à partir des soumissions.
 * Exemple de lancement :
 * http://localhost:9000/sra/scripts/run/sra.scripts.VerifSampleFromSubmission

 * @author sgas
 *
 */
// ok aucune erreur en PROD
public class VerifSampleFromSubmission extends ScriptNoArgs {

	private final SubmissionAPI submissionAPI;
	private final SampleAPI sampleAPI;



	@Inject
	public VerifSampleFromSubmission (SubmissionAPI submissionAPI,
			SampleAPI sampleAPI) {
		this.submissionAPI = submissionAPI;
		this.sampleAPI = sampleAPI;

	}




	@Override
	public void execute() throws Exception {
		int cp = 0;
		Iterable<Submission> iterable = submissionAPI.dao_all();
		for (Submission submission: iterable) {
			if (StringUtils.isNotBlank(submission.accession) && submission.type.equals(Submission.Type.CREATION)) {
				//println("submission.accession=" + submission.accession + "submission.date");
				
				if(! submission.state.code.equals("SUB-F")) {
					println("Probleme de state pour la soumission" + submission.code + " => state = " + submission.state.code);	
				}
				for (String sampleCode : submission.sampleCodes) {
					Sample sample = sampleAPI.get(sampleCode);
					if (sample == null) {
						println("sample " + sampleCode + " de la soumission " + submission.code + " n'existe pas dans la base");
					}
					if(! submission.submissionDate.toString().equals(sample.firstSubmissionDate.toString())) {
						println("Probleme de date pour le sample" + sample.code + " => date = " + sample.firstSubmissionDate + " et submission date = "+ submission.submissionDate);
					} else {
						println("ok pour date pour le sample" + sample.code);
					}
					if(!sample.state.code.equals("SUB-F")) {
						println("Sample " + sample.code +" avec state.code " + sample.state.code);
					} else {
						println("ok pour stateCode pour le sample" + sample.code);
					}
				}
			}	
		}
		println("Nombre de soumission traitées = " + cp);
	}

}





