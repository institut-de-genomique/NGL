package sra.scripts;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import fr.cea.ig.ngl.dao.api.sra.AbstractSampleAPI;
import fr.cea.ig.ngl.dao.api.sra.ExperimentAPI;
import models.sra.submit.sra.instance.AbstractSample;
import models.sra.submit.sra.instance.Experiment;

/*
 * Script à utiliser pour recharger les md5 d'une soumission a partir de NGL.
 * {@code http://localhost:9000/sra/scripts/run/sra.scripts.Test}
 * <br>
 * Si parametre absent dans url => declenchement d'une erreur.
 *  
 * @author sgas
 *
 */
public class Test extends ScriptNoArgs {
	private static final play.Logger.ALogger logger = play.Logger.of(Test.class);
	private final ExperimentAPI         experimentAPI;
	private final AbstractSampleAPI     abstractSampleAPI;
	
	
	@Inject
	public Test(ExperimentAPI experimentAPI,
			AbstractSampleAPI abstractSampleAPI
				) {
		this.experimentAPI = experimentAPI;
		this.abstractSampleAPI = abstractSampleAPI;
	}


	

	public void test_allExperiements () {
		Iterable<Experiment> list_experiments = experimentAPI.dao_all();
		
		int count_experiment = 0;
		for (Experiment experiment : list_experiments) {
			if (! experiment.state.code.equals("SUB-F")) {
				continue;
			}
			
			count_experiment++;
			println(experiment.code);
			if (StringUtils.isBlank(experiment.sampleCode)) {
				println("experiment avec code "+ experiment.code +" sans sampleCode et SUB-F");
				continue;
			}
			if (StringUtils.isBlank(experiment.sampleAccession)) {
				println("experiment avec code "+ experiment.code +" sans sampleAccession et SUB-F");
			}
			if(! experiment.sampleAccession.startsWith("ERS")) {
				println("experiment avec code "+ experiment.code +" avec sampleAccession bizarre " + experiment.sampleAccession);
			}
			
			AbstractSample dbSample = abstractSampleAPI.dao_getObject(experiment.sampleCode);
			
			if(dbSample == null) {
				println("experiment avec code "+ experiment.code +" avec sampleAccession " +  experiment.sampleAccession + "qui n'existe pas dans la base");
				continue;
			}
			if (! dbSample.externalId.startsWith("SAM")) {
				println("experiment avec code "+ experiment.code + " avec dbSample.externalId  " + dbSample.externalId);
			}
			if(! dbSample.accession.startsWith("ERS")) {
				println("experiment avec code "+ experiment.code +" avec dbSample.accession " + experiment.sampleAccession);
			}
		}
		println("Fin de l'analyse des samples references dans les "+ count_experiment + " experiments");
	}
	
	public void test_allSamples () {
		Iterable<AbstractSample> list_samples = abstractSampleAPI.dao_all();
		
		int count_sample = 0;
		for (AbstractSample dbSample : list_samples) {
			if (! dbSample.state.code.equals("SUB-F")) {
				continue;
			}
			count_sample++;
			println(dbSample.code);
	
			if (StringUtils.isBlank(dbSample.accession)) {
				println("sample avec code "+ dbSample.code +" sans accession et SUB-F");
			}
			if (StringUtils.isBlank(dbSample.externalId)) {
				println("sample avec code "+ dbSample.code +" sans externalId et SUB-F");
			}
			
			if(! dbSample.externalId.startsWith("SAM")) {
				println("experiment avec code "+ dbSample.code +" avec externalId bizarre " + dbSample.externalId);
			}
			
			if(! dbSample.accession.startsWith("ERS")) {
				println("experiment avec code "+ dbSample.code +" avec accession bizarre " + dbSample.accession);
			}
		}

		println("Fin de l'analyse des " + count_sample + " samples");
	}
		
	@Override
	public void execute() throws Exception {
		//test_allExperiements();
		//test_allSamples();
		DateFormat formaterDate = new SimpleDateFormat("yyyy-MM-dd");
	    String st_date = formaterDate.format(new Date());
	    println("st_date = "  +  st_date);
	    String resultDirectory = "/env/cns/submit_traces/SRA/SNTS_output_xml/NGL/FIX_TRANSFERT/2023-03-02/";
		//String submissionName = "GSC_FixTransfert_" + resultDirectory.replaceAll("[^/]+/", "").replaceAll("/", ""); // on conserve nom du dernier sous-repertoire

		
		String fileSep = "/";
		String pattern = "\\" + fileSep + "\\s*$";
		resultDirectory = resultDirectory.replaceFirst(pattern, ""); // oter / terminal si besoin
		File runFile = new File(resultDirectory + fileSep + "run.xml");
		File submissionFile = new File(resultDirectory +  fileSep + "submission.xml");
		pattern = "[^" + fileSep + "]+" + fileSep;
		String submissionName = "GSC_FixTransfert_" + resultDirectory.replaceAll(pattern, "");// on conserve nom du dernier sous-repertoire
		pattern = fileSep;
		submissionName = submissionName.replaceAll(pattern, ""); 
	    println(submissionName);
		String[] readsetCodes = null;
		String[] readsetCodes2 = null;
		readsetCodes = "titi".split("\\|");
		for (String readsetCode : readsetCodes) {
			println ("readsetCode= " +  readsetCode);
		}
		readsetCodes2 = "lulu|lala|lolo".split("\\|");
		for (String readsetCode : readsetCodes2) {
			println ("readsetCode_2= " +  readsetCode);
		}		
	}
		
		
}