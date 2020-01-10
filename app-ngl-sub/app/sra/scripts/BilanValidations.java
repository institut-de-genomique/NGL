package sra.scripts;


import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//import java.util.List;

import javax.inject.Inject;
//import org.mongojack.DBQuery;

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
import models.sra.submit.common.instance.AbstractSample;
//import models.sra.submit.common.instance.AbstractStudy;
import models.sra.submit.common.instance.Sample;
//import models.sra.submit.common.instance.Readset;
import models.sra.submit.common.instance.Submission;
//import models.sra.submit.sra.instance.Configuration;
//import models.sra.submit.sra.instance.Experiment;
import validation.ContextValidation;
import play.libs.Json;
import services.RepriseHistorique;


/*
 * Script a lancer pour avoir les numeros d'accession associés à une soumission ou plusieurs soumissions.
 * Exemple de lancement :
 * http://localhost:9000/sra/scripts/run/sra.scripts.BilanAc?codes=code_soumission_1&codes=code_soumission_2
 * @author sgas
 *
 */
public class BilanValidations extends Script<BilanValidations.MyParam>{
	private static final play.Logger.ALogger logger = play.Logger.of(BilanValidations.class);

	private final SubmissionAPI     submissionAPI;
//	private final ConfigurationAPI  configurationAPI;
//	private final AbstractStudyAPI  abstractStudyAPI;
	private final AbstractSampleAPI abstractSampleAPI;
//	private final SampleAPI         sampleAPI;
//	private final ExperimentAPI     experimentAPI;
	private final NGLApplication    app;

	@Inject
	public BilanValidations(SubmissionAPI     submissionAPI,
					 ConfigurationAPI  configurationAPI,
					 AbstractStudyAPI  abstractStudyAPI,
					 AbstractSampleAPI abstractSampleAPI,
					 SampleAPI         sampleAPI,
					 ExperimentAPI     experimentAPI,
					 ReadSetsAPI       readsetAPI,
					 NGLApplication    app) {

		this.submissionAPI     = submissionAPI;
//		this.configurationAPI  = configurationAPI;
//		this.abstractStudyAPI  = abstractStudyAPI;
		this.abstractSampleAPI = abstractSampleAPI;
//		this.sampleAPI         = sampleAPI;
//		this.experimentAPI     = experimentAPI;
		this.app               = app;
	}


	// ma structure de controle et stockage des arguments de l'url
	public static class MyParam {
	}


	@Override
	public void execute(MyParam args) throws Exception {
		/*for (Experiment experiment : experimentAPI.dao_all()) {
			String user = "william";
			ContextValidation contextValidation = ContextValidation.createUpdateContext(user); // on veut updater l'experiment sans run pour son run
			experiment.validate(contextValidation);
			if(contextValidation.hasErrors()) {
				contextValidation.displayErrors(logger);
				println(Json.prettyPrint(app.errorsAsJson(contextValidation.getErrors())));				
			} else {
				printfln("ok pour experiment.code=%s", experiment.code);				
			}
		}
		for (Submission submission : submissionAPI.dao_all()) {
			String user = "william";
			ContextValidation contextValidation = ContextValidation.createUpdateContext(user); // on veut updater l'experiment sans run pour son run
			submission.validate(contextValidation);
			if(contextValidation.hasErrors()) {
				contextValidation.displayErrors(logger);
				println(Json.prettyPrint(app.errorsAsJson(contextValidation.getErrors())));				
			} else {
				printfln("ok pour submission.code=%s", submission.code);				
			}
		}		

		for (Configuration configuration : configurationAPI.dao_all()) {
			String user = "william";
			ContextValidation contextValidation = ContextValidation.createUpdateContext(user); // on veut updater l'experiment sans run pour son run
			configuration.validate(contextValidation);
			if(contextValidation.hasErrors()) {
				contextValidation.displayErrors(logger);
				println(Json.prettyPrint(app.errorsAsJson(contextValidation.getErrors())));				
			} else {
				printfln("ok pour configuration.code=%s", configuration.code);				
			}
		}	
		
		for (AbstractStudy abstractStudy : abstractStudyAPI.dao_all()) {
			String user = "william";
			ContextValidation contextValidation = ContextValidation.createUpdateContext(user); // on veut updater l'experiment sans run pour son run
			abstractStudy.validate(contextValidation);
			if(contextValidation.hasErrors()) {
				contextValidation.displayErrors(logger);
				println(Json.prettyPrint(app.errorsAsJson(contextValidation.getErrors())));				
			} else {
				printfln("ok pour study.code=%s", abstractStudy.code);				
			}
		}
		*/
		
		
		
		List<AbstractSample> list_abstractSample = new ArrayList<>();
		list_abstractSample.add(abstractSampleAPI.dao_findOne(DBQuery.in("accession", "ERS1034848")));
/*		
		// Les 3 ecritures sont equivalentes : on passe bien par le validate de Sample si type Sample.
		for (AbstractSample abstractSample : abstractSampleAPI.dao_all()) {
//			for (AbstractSample abstractSample : list_abstractSample) {
			String user = "william";
			ContextValidation contextValidation = ContextValidation.createUpdateContext(user); // on veut updater l'experiment sans run pour son run
			abstractSample.traceInformation.modifyDate= new Date();
			abstractSample.traceInformation.modifyUser= user;

			abstractSample.validate(contextValidation);
			if(contextValidation.hasErrors()) {
				contextValidation.displayErrors(logger);
				printfln("Erreur pour le sample avec accession=%s et code=%s:\n", abstractSample.accession, abstractSample.code);
				println(Json.prettyPrint(app.errorsAsJson(contextValidation.getErrors())));				
			} else {
				//printfln("ok pour sample.code=%s", abstractSample.code);		
			}
		}
		
		
		for (AbstractSample abstractSample : abstractSampleAPI.dao_all()) {
//			for (AbstractSample abstractSample : list_abstractSample) {
			String user = "william";
			ContextValidation contextValidation = ContextValidation.createUpdateContext(user); // on veut updater l'experiment sans run pour son run
			abstractSample.traceInformation.modifyDate= new Date();
			abstractSample.traceInformation.modifyUser= user;
			if(abstractSample instanceof Sample) {
				((Sample) abstractSample).validate(contextValidation);
				if(contextValidation.hasErrors()) {
					contextValidation.displayErrors(logger);
					printfln("Erreur pour le sample avec accession=%s et code=%s:\n", abstractSample.accession, abstractSample.code);
					println(Json.prettyPrint(app.errorsAsJson(contextValidation.getErrors())));				
				} else {
					//printfln("ok pour sample.code=%s", abstractSample.code);		
				}
			}
		}	
		
		for (AbstractSample abstractSample : abstractSampleAPI.dao_all()) {
//			for (AbstractSample abstractSample : list_abstractSample) {
			String user = "william";
			ContextValidation contextValidation = ContextValidation.createUpdateContext(user); // on veut updater l'experiment sans run pour son run

			if(abstractSample instanceof Sample) {
				Sample sample = sampleAPI.dao_getObject(abstractSample.code);
				sample.traceInformation.modifyDate= new Date();
				sample.traceInformation.modifyUser= user;
				sample.validate(contextValidation);
				if(contextValidation.hasErrors()) {
					contextValidation.displayErrors(logger);
					printfln("Erreur pour le sample avec accession=%s et code=%s:\n", abstractSample.accession, abstractSample.code);
					println(Json.prettyPrint(app.errorsAsJson(contextValidation.getErrors())));				
				} else {
					//printfln("ok pour sample.code=%s", abstractSample.code);		
				}
			}
		}	
		
	*/	
		RepriseHistorique repriseHistorique = new RepriseHistorique();
		File fileXmlSamples = new File("/env/cns/home/sgas/samples.xml");
		String user = "william";
		Iterable<Sample> listSamples = repriseHistorique.forSamples(fileXmlSamples, user);
		ContextValidation contextValidation = ContextValidation.createCreationContext(user); // on veut updater l'experiment sans run pour son run

		for (Sample sample : listSamples) {
			sample.traceInformation.modifyDate= new Date();
			sample.traceInformation.modifyUser= user;
			sample.validate(contextValidation);
			if(contextValidation.hasErrors()) {
				contextValidation.displayErrors(logger);
				printfln("Erreur pour le sample avec accession=%s et code=%s:\n", sample.accession, sample.code);
				println(Json.prettyPrint(app.errorsAsJson(contextValidation.getErrors())));				
			} else {
				//printfln("ok pour sample.code=%s", abstractSample.code);		
			}
		}
		List <Submission> submissionList = submissionAPI.dao_find(DBQuery.in("sampleCodes", "TARA_BCB-AKAI_AKBI_AKCI_AKDI")).toList();
		for (Submission submission : submissionList) {
			printfln("La soumission %s contient le sampleCode %s\n",submission.code, "TARA_BCB-AKAI_AKBI_AKCI_AKDI");
		}
	
	}

}


