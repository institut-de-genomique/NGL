package sra.scripts;

import java.io.File;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.chunked.ScriptWithArgs;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.sra.instance.RawData;
import models.sra.submit.sra.instance.Submission;
import models.utils.InstanceConstants;
import play.Logger;
import validation.ContextValidation;

/**
 * Script permettant de changer la localisation des rawData en location=CNS
 * Mise à jour du path des rawdata avec localisation CNS
 * Script à utiler lorsque TGCC n'est pas disponible et que les données ont été récupérées (SUPSQ-4111)
 * 
 * @author ejacoby
 *
 */
public class ChangeRawLocationCNS extends ScriptWithArgs<ChangeRawLocationCNS.Args>{

	public static class Args {
		public String submissionCode;
	}


	@Override
	public void execute(Args args) throws Exception {
		//Récupère code soumission
		String submissionCode = args.submissionCode;
		
		Submission submission = MongoDBDAO.findByCode(InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class, submissionCode);
		if(submission!=null) {
			Logger.debug("Start update for submission "+submission.code);
			//Get rawData
			for(String expCode : submission.experimentCodes) {
				Experiment experiment = MongoDBDAO.findByCode(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, Experiment.class, expCode);
				for(RawData rawData : experiment.run.listRawData) {
					rawData.location="CNS";
					String cns_directory = rawData.directory;
					if (rawData.directory.startsWith("/ccc/")) {
						int index = rawData.directory.indexOf("/rawdata/");
						String lotseq_dir = rawData.directory.substring(index + 9);
						cns_directory="/env/cns/proj/" + lotseq_dir;
					}
					rawData.directory=cns_directory;
					Logger.debug("ln -s "+cns_directory + File.separator + rawData.relatifName+" "+submission.submissionDirectory + File.separator + rawData.relatifName);
				}
				
				//Update experiment
				ContextValidation ctx = ContextValidation.createUpdateContext("ngl-support");
				//experiment.validate(ctx);
				if(ctx.hasErrors()) {
					Logger.debug("Experiment cannot be update "+experiment.code);
				}else {
					//Logger.debug("Update experiment "+experiment.code);
					MongoDBDAO.update(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, experiment);
				}
			}
		}
		Logger.debug("End update Submission");
		
	}
}
