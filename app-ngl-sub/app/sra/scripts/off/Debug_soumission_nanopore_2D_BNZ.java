package sra.scripts.off;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import mail.MailServiceException;
import models.laboratory.run.instance.ReadSet;
import models.sra.submit.sra.instance.RawData;
import models.sra.submit.sra.instance.Run;
import models.sra.submit.sra.instance.Submission;
import models.sra.submit.util.SraException;
import models.sra.submit.util.VariableSRA;
import models.utils.InstanceConstants;
import sra.api.submission.SubmissionNewAPI;
import validation.ContextValidation;

public class Debug_soumission_nanopore_2D_BNZ extends ScriptNoArgs {

//	private final FileAcServices     fileAcServices;
//	private final SubmissionServices submissionServices;
	private final SubmissionNewAPI   submissionNewAPI;
	@Inject
	public Debug_soumission_nanopore_2D_BNZ(SubmissionNewAPI submissionNewAPI) {
//		super();
//		this.fileAcServices     = fileAcServices;
//		this.submissionServices = submissionServices;
		this.submissionNewAPI   = submissionNewAPI;	
	}
	
	@Override
	public void execute() throws IOException, SraException, MailServiceException {
		//reloadAC();
	}
	
	public void reloadAC() throws IOException, SraException, MailServiceException {
//		List<String> submissionCodes = new ArrayList<String>();
//		submissionCodes.add("CNS_BNZ_2CMG1195V");
//		submissionCodes.add("CNS_BNZ_2CLH1PNRK");
//		submissionCodes.add("CNS_BNZ_2CLH1D04I");
		List<String> submissionCodes = 
				Arrays.asList("CNS_BNZ_2CMG1195V", 
						      "CNS_BNZ_2CLH1PNRK",
						      "CNS_BNZ_2CLH1D04I" );
		for (String submissionCode: submissionCodes) {
			Submission submission = MongoDBDAO
					.findByCode(InstanceConstants.SRA_SUBMISSION_COLL_NAME, 
							    models.sra.submit.sra.instance.Submission.class, 
							    submissionCode);
//			File fileEbi = new File("/env/cns/home/sgas/debug_soumission_nanopore",  "listAC_" + submission.code + ".txt");
			String user = "ngsrg";
			ContextValidation ctxVal = ContextValidation.createUndefinedContext(user);
//			submission = this.fileAcServices.traitementFileAC(ctxVal, submissionCode, fileEbi); 
			this.submissionNewAPI.loadEbiResponseAC(ctxVal, submission);
		}	
	}
	
	public Run createRunEntityForMinion2D(ReadSet readSet, String runCode) throws Exception {
		// On cree le run pour le readSet demandé.
		// La validite du readSet doit avoir été testé avant.
		// Recuperer pour le readSet la liste des fichiers associés:
		String laboratoryRunCode = readSet.runCode;
//		models.laboratory.run.instance.Run  laboratoryRun = 
				MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, models.laboratory.run.instance.Run.class, laboratoryRunCode);
//		InstrumentUsed instrumentUsed = laboratoryRun.instrumentUsed;
		List <models.laboratory.run.instance.File> list_files = readSet.files;
		if (list_files == null) {
			
			throw new Exception("Aucun fichier pour le readSet " + readSet.code +"???");
		} else {
			//System.out.println("nbre de fichiers = " + list_files.size());
		}
		// Pour chaque readSet, creer un objet run 
		Date runDate = readSet.runSequencingStartDate;
		Run run = new Run();
		//run.code = SraCodeHelper.getInstance().generateRunCode(readSet.code);
		run.code    = runCode;
		run.runDate = runDate;
		//run.projectCode = projectCode;
		run.runCenter = VariableSRA.centerName;
		// Renseigner le run pour ces fichiers sur la base des fichiers associes au readSet :
		// chemin des fichiers pour ce readset :
		String dataDir = readSet.path;

		for (models.laboratory.run.instance.File runInstanceFile: list_files) {
			String runInstanceExtentionFileName = runInstanceFile.extension;
			// conditions qui doivent etre suffisantes puisque verification préalable que le readSet
			// est bien valide pour la bioinformatique.			
			//if (runInstanceFile.usable 
					//&& ! runInstanceExtentionFileName.equalsIgnoreCase("fna") && ! runInstanceExtentionFileName.equalsIgnoreCase("qual")
					//&& ! runInstanceExtentionFileName.equalsIgnoreCase("fna.gz") && ! runInstanceExtentionFileName.equalsIgnoreCase("qual.gz")) {
			if  (runInstanceExtentionFileName.equalsIgnoreCase("fastq.gz") || runInstanceExtentionFileName.equalsIgnoreCase("fastq")) {
				RawData rawData = new RawData();
				//System.out.println("fichier " + runInstanceFile.fullname);
				rawData.extention = runInstanceFile.extension;
				System.out.println("dataDir "+dataDir);
				rawData.directory = dataDir.replaceFirst("\\/$", ""); // oter / terminal si besoin
				System.out.println("raw data directory"+rawData.directory);
				rawData.relatifName = runInstanceFile.fullname;
				rawData.location = readSet.location;
				if (runInstanceFile.properties != null && runInstanceFile.properties.containsKey("md5")) {
					rawData.md5 = (String) runInstanceFile.properties.get("md5").value;
					System.out.println("Recuperation du md5 pour" + rawData.relatifName +"= " + rawData.md5);
				}
				printfln("----rawData=%s",rawData.relatifName);
				if(rawData.relatifName.contains("_raw")) {
					run.listRawData.add(rawData);
				}		
			}
		}
		return run;
	}
	
	public void debugRun() throws Exception {
//		List<String> submissionCodes = new ArrayList<String>();
//		submissionCodes.add("CNS_BNZ_2CMG1195V");
//		submissionCodes.add("CNS_BNZ_2CLH1PNRK");
//		submissionCodes.add("CNS_BNZ_2CLH1D04I");
		List<String> submissionCodes = 
				Arrays.asList("CNS_BNZ_2CMG1195V",
						      "CNS_BNZ_2CLH1PNRK",
						      "CNS_BNZ_2CLH1D04I");
		//SubmissionServices submissionServices = new SubmissionServices();
		// String stateCode = "IP-SUB";
		for (String submissionCode:submissionCodes) {
			Submission submission = MongoDBDAO
					.findByCode(InstanceConstants.SRA_SUBMISSION_COLL_NAME, models.sra.submit.sra.instance.Submission.class, submissionCode);
			printfln("**********soumission = %s avec %d run ", submission.code, submission.runCodes.size());
			for (String expCode : submission.experimentCodes) {
//				Experiment exp = 
				    MongoDBDAO.findByCode(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, models.sra.submit.sra.instance.Experiment.class, expCode);
//				Run runOri = exp.run;
/*				
				MongoDBDAO.update(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, Experiment.class,
						DBQuery.is("code", exp.code),
						DBUpdate.set("state.code", stateCode));
				
				printfln("instrument model = %s et nombre de fichiers = %d", exp.instrumentModel, exp.run.listRawData.size());
				ReadSet readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, exp.readSetCode);

				// Refaire run et sauver dans base :
				Run run;	
				// si nanopore et nombre de fichier > 1 
				if(exp.instrumentModel.equalsIgnoreCase("MinION") && exp.run.listRawData.size()>1) {
					printfln("Mise a jour pour le run de l'exp %s", exp.code);
					run = createRunEntityForMinion2D(readSet, exp.run.code); 	
				} else {
					run = submissionServices.createRunEntity(readSet);
					run.code = exp.run.code;
				}
				run.expCode = expCode;
				MongoDBDAO.update(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, Experiment.class,
						DBQuery.is("code", exp.code),
						DBUpdate.set("run", run));		
				
				// Mettre les objets exp avec state = V-SUB
				printfln("Mise a jour pour l'etat de l'exp %s", exp.code);
				MongoDBDAO.update(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, Experiment.class,
						DBQuery.is("code", exp.code),
						DBUpdate.set("state.code", stateCode));
				
				// Mettre les objets readsets avec state à V-SUB:
				printfln("Mise a jour du readset %s", exp.readSetCode);
				MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class,
						DBQuery.is("code", exp.readSetCode),
						DBUpdate.set("submissionState.code", stateCode));				
			}
			// Mettre objet study à V-SUB:
			
			if (StringUtils.isNotBlank(submission.studyCode) ) {
				printfln("Mise a jour du study %s", submission.studyCode);
				MongoDBDAO.update(InstanceConstants.SRA_STUDY_COLL_NAME, Study.class,
						DBQuery.is("code", submission.studyCode),
						DBUpdate.set("state.code", stateCode));
			}
			// Mettre objets samples à V-SUB:
			for (String sampleCode : submission.sampleCodes) {
				printfln("Mise a jour du sample %s", sampleCode);
				MongoDBDAO.update(InstanceConstants.SRA_SAMPLE_COLL_NAME, Sample.class,
						DBQuery.is("code", sampleCode),
						DBUpdate.set("state.code", stateCode));
			}
			// Mettre objet submission à jour pour status :
			printfln("Mise a jour de la soumission %s", submissionCode);
			MongoDBDAO.update(InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class,
					DBQuery.is("code", submissionCode),
					DBUpdate.set("state.code", stateCode)
					.set("xmlSubmission", "").set("xmlStudys", "").set("xmlSamples", "").set("xmlExperiments", "").set("xmlRuns", ""));
*/
		}
	  }
	}
	
	
}

