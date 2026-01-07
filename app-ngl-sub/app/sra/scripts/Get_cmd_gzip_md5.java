package sra.scripts;

import java.util.regex.Pattern;
import javax.inject.Inject;

import org.mongojack.DBQuery;

import controllers.sra.api.ExperimentAPI;
import controllers.sra.api.SubmissionAPI;
import fr.cea.ig.lfw.controllers.scripts.buffered.Script;
import fr.cea.ig.ngl.NGLApplication;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.sra.instance.RawData;
import models.sra.submit.sra.instance.Submission;
import services.sra.SraException;


/*
 * Script a lancer pour recalculer les md5sum des collabFileName indiqués dans le fichier d'entrée et verifier et/ou corriger les md5sum dans ngl_sub
 * Exemple de lancement pour verifier les md5sum sans corriger la base ngl-sub:
 * http://appdev.genoscope.cns.fr:9005/sra/scripts/run/sra.scripts.ReloadMd5sum?inputFile=/env/cns/tmp/collabFileNameNoMd5.txt&user=sgas&corrige_ngl_sub=false
 * Exemple de lancement pour verifier les md5sum et corriger la base ngl-sub:
 * http://appdev.genoscope.cns.fr:9005/sra/scripts/run/sra.scripts.Get_cmd_gzip_md5?submissionCode=GSC_BSI_929G2JUDD
 * 
@author sgas
 *
 */
// ok aucune erreur en PROD
@SuppressWarnings("unused")
public class Get_cmd_gzip_md5 extends Script<Get_cmd_gzip_md5.Args> {
	
	private final ExperimentAPI experimentAPI;
	private final SubmissionAPI submissionAPI;
	private final NGLApplication    app;

	private static final play.Logger.ALogger logger = play.Logger.of(Get_cmd_gzip_md5.class);
	private String pattern_LineVide = "^\\s*$";   // pour ignorer les lignes sans caracteres visibles  
	private java.util.regex.Pattern plv = Pattern.compile(pattern_LineVide); 

	private String pattern_LineComment = "^\\s*#";   // pour ignorer les lignes de commentaire
	private java.util.regex.Pattern plc = Pattern.compile(pattern_LineComment);
	

	@Inject
	public Get_cmd_gzip_md5 (NGLApplication app,
				    	 ExperimentAPI  experimentAPI,
				    	 SubmissionAPI  submissionAPI) {
		this.app           = app;
		this.experimentAPI = experimentAPI;
		this.submissionAPI = submissionAPI;

	}
	
	
	public static class Args {
		public String submissionCode; 
	}



	@Override
	public void execute(Args args) throws Exception {
		println ("submissionCode = " + args.submissionCode);
		Submission submission = submissionAPI.get(args.submissionCode);
		if (submission == null) {
			throw new SraException("submission " + args.submissionCode + " n'existe pas dans la base");
		}
		
		printfln("Ecriture des cmd gzip");
		for (String expCode : submission.experimentCodes) {
			Experiment experiment = experimentAPI.dao_findOne(DBQuery.is("code", expCode));
			String repBigTmp = "/env/cns/submit_traces/SRA/BigTmp/" + args.submissionCode;
			for (RawData rawData : experiment.run.listRawData) {
				if(rawData.gzipForSubmission) {
					printfln("gzip -c " + rawData.directory + "/" + rawData.relatifName + " > "+ repBigTmp + "/" + rawData.relatifName + ".gz");
				}
			}
		}
		
		printfln("Ecriture des cmd md5sum");
		for (String expCode : submission.experimentCodes) {
			Experiment experiment = experimentAPI.dao_findOne(DBQuery.is("code", expCode));
			String repBigTmp = "/env/cns/submit_traces/SRA/BigTmp/" + args.submissionCode;
			for (RawData rawData : experiment.run.listRawData) {
				if(rawData.gzipForSubmission) {
					printfln("md5sum " + repBigTmp + "/" + rawData.relatifName + ".gz" + " >> md5.txt");
				}
			}
		}
		printfln("Ecriture des cmd des liens");
		for (String expCode : submission.experimentCodes) {
			Experiment experiment = experimentAPI.dao_findOne(DBQuery.is("code", expCode));
			String repBigTmp = "/env/cns/submit_traces/SRA/BigTmp/" + args.submissionCode;
			String submissionDir = submission.submissionDirectory;
			for (RawData rawData : experiment.run.listRawData) {
				if(rawData.gzipForSubmission) {
					printfln("ln -s " + repBigTmp + "/" + rawData.relatifName + ".gz" + " " + submissionDir + "/" + rawData.relatifName + ".gz");
				}
			}
		}		
		
		
	}
		
}
