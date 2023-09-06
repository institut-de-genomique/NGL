package sra.scripts;


import java.util.Date;

//import java.util.List;

import javax.inject.Inject;
//import org.mongojack.DBQuery;


import fr.cea.ig.lfw.controllers.scripts.buffered.Script;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.sra.AbstractSampleAPI;
import fr.cea.ig.ngl.dao.api.sra.AbstractStudyAPI;
import fr.cea.ig.ngl.dao.api.sra.ConfigurationAPI;
import fr.cea.ig.ngl.dao.api.sra.ExperimentAPI;
import fr.cea.ig.ngl.dao.api.sra.SubmissionAPI;
import fr.cea.ig.ngl.dao.readsets.ReadSetsAPI;
import models.sra.submit.sra.instance.AbstractSample;
import models.sra.submit.sra.instance.AbstractStudy;
import models.sra.submit.sra.instance.Configuration;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.sra.instance.Submission;
import validation.ContextValidation;
import play.libs.Json;


/*
 * Script a lancer pour avoir les numeros d'accession associés à une soumission ou plusieurs soumissions.
 * Exemple de lancement :
 * http://localhost:9000/sra/scripts/run/sra.scripts.BilanValidations
 * @author sgas
 *
 */
public class BilanValidations extends Script<BilanValidations.MyParam>{
	private static final play.Logger.ALogger logger = play.Logger.of(BilanValidations.class);

	private final SubmissionAPI     submissionAPI;
	private final ConfigurationAPI  configurationAPI;
	private final AbstractStudyAPI  abstractStudyAPI;
	private final AbstractSampleAPI abstractSampleAPI;
	private final ExperimentAPI     experimentAPI;
	private final NGLApplication    app;

	@Inject
	public BilanValidations(SubmissionAPI     submissionAPI,
					 ConfigurationAPI  configurationAPI,
					 AbstractStudyAPI  abstractStudyAPI,
					 AbstractSampleAPI abstractSampleAPI,
					 ExperimentAPI     experimentAPI,
					 ReadSetsAPI       readsetAPI,
					 NGLApplication    app) {

		this.submissionAPI     = submissionAPI;
		this.configurationAPI  = configurationAPI;
		this.abstractStudyAPI  = abstractStudyAPI;
		this.abstractSampleAPI = abstractSampleAPI;
//		this.sampleAPI         = sampleAPI;
		this.experimentAPI     = experimentAPI;
		this.app               = app;
	}


	// ma structure de controle et stockage des arguments de l'url
	public static class MyParam {
	}


	public void bilanSubmission() {
		Iterable<Submission> list_submission =  submissionAPI.dao_all();
		println("Validations des submissions");	
		int cp = 0;
		int cpError = 0;
		for (Submission submission : list_submission) {
			cp++;
			String user = "william";
			ContextValidation contextValidation = ContextValidation.createUpdateContext(user); // on veut updater l'experiment sans run pour son run
			contextValidation.putObject("copy", true);
			submission.traceInformation.modifyDate = new Date();
			submission.traceInformation.modifyUser = user;

			submission.validate(contextValidation);
			if(contextValidation.hasErrors()) {
				cpError++;
				contextValidation.displayErrors(logger, "debug");
				println("Error pour " + submission.code);
				println(Json.prettyPrint(app.errorsAsJson(contextValidation.getErrors())));				
			} else {
				//printfln("ok pour submission.code=%s", submission.code);				
			}
		}	
		println("Fin des validations des  submissions " + cpError + " error / "  + cp);				
	}
	
	public void bilanConfiguration() {
		Iterable<Configuration> list_configuration = configurationAPI.dao_all();
		println("Validations des configurations");	
		int cp = 0;
		int cpError = 0;
		for (Configuration configuration : list_configuration) {
			String user = "william";
			configuration.traceInformation.modifyDate = new Date();
			configuration.traceInformation.modifyUser = user;
			cp++;
			ContextValidation contextValidation = ContextValidation.createUpdateContext(user); // on veut updater l'experiment sans run pour son run
			configuration.validate(contextValidation);
			if(contextValidation.hasErrors()) {
				cpError++;
				contextValidation.displayErrors(logger, "debug");
				println("Error pour " + configuration.code);
				println(Json.prettyPrint(app.errorsAsJson(contextValidation.getErrors())));				
			} else {
				//printfln("ok pour configuration.code=%s", configuration.code);				
			}
		}	
		println("Fin des validations des  configurations " + cpError + " error / "  + cp);				
	}

	public void bilanStudy() {
		Iterable <AbstractStudy> list_abstractStudy = abstractStudyAPI.dao_all();
		println("Validations des Study");	
		int cp = 0;
		int cpError = 0;
		for (AbstractStudy abstractStudy : list_abstractStudy) {
			cp++;
			String user = "william";
			abstractStudy.traceInformation.modifyDate = new Date();
			abstractStudy.traceInformation.modifyUser = user;
			ContextValidation contextValidation = ContextValidation.createUpdateContext(user); // on veut updater l'experiment sans run pour son run
			abstractStudy.validate(contextValidation);
			if(contextValidation.hasErrors()) {
				cpError++;
				println("Error pour " + abstractStudy.code + " cree le " + abstractStudy.traceInformation.creationDate);
				contextValidation.displayErrors(logger, "debug");
				println(Json.prettyPrint(app.errorsAsJson(contextValidation.getErrors())));				
			} else {
				//printfln("ok pour study.code=%s", abstractStudy.code);				
			}
		}
		println("Fin des validations des  studys " + cpError + " error / "  + cp);				
	}
	
	public void bilanSample() {
		Iterable<AbstractSample> list_abstractSample = abstractSampleAPI.dao_all();
		// Les 3 ecritures sont equivalentes : on passe bien par le validate de Sample si type Sample.
		println("Validations des Samples");	
		int cp = 0;
		int cpError = 0;
		for (AbstractSample abstractSample : list_abstractSample) {
			String user = "william";
			cp++;
			ContextValidation contextValidation = ContextValidation.createUpdateContext(user); // on veut updater l'experiment sans run pour son run
			abstractSample.traceInformation.modifyDate= new Date();
			abstractSample.traceInformation.modifyUser= user;

			abstractSample.validate(contextValidation);
			if(contextValidation.hasErrors()) {
				cpError++;
				contextValidation.displayErrors(logger, "debug");
				println("Error pour " + abstractSample.code + " cree le " + abstractSample.traceInformation.creationDate);
				println(Json.prettyPrint(app.errorsAsJson(contextValidation.getErrors())));				
			} else {
				//printfln("ok pour sample.code=%s", abstractSample.code);		
			}
		}
		println("Fin des validations des  samples " + cpError + " error / "  + cp);				
	}
	
	public void bilanExperiment() {
		Iterable<Experiment> list_experiment = experimentAPI.dao_all();
		println("Validations des experiments");	
		int cp = 0;
		int cpError = 0;
		for (Experiment experiment : list_experiment) {
			String user = "william";
			cp++;
			ContextValidation contextValidation = ContextValidation.createUpdateContext(user); 
			contextValidation.putObject("copy", true);
			experiment.traceInformation.modifyDate = new Date();
			experiment.traceInformation.modifyUser = user;
			experiment.validate(contextValidation);
			if(contextValidation.hasErrors()) {
				cpError++;
				contextValidation.displayErrors(logger, "debug");
				println("Error pour " + experiment.code + " cree le " + experiment.traceInformation.creationDate);
				println(Json.prettyPrint(app.errorsAsJson(contextValidation.getErrors())));				
			} else {
				//printfln("ok pour configuration.code=%s", experiment.code);				
			}
		}	
		println("Fin des validations des  experiments " + cpError + " error / "  + cp);				
	}
	
	
	@Override
	public void execute(MyParam args) throws Exception {
		bilanSubmission();
		//bilanConfiguration();
		//bilanStudy();
		//bilanSample();
		//bilanExperiment();	
	}

}


