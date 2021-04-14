package sra.scripts;


import java.util.Date;
import java.util.List;
//import java.util.regex.Pattern;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

//import java.util.List;

import javax.inject.Inject;
//import org.mongojack.DBQuery;

import fr.cea.ig.lfw.controllers.scripts.buffered.Script;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import fr.cea.ig.lfw.controllers.scripts.chunked.ScriptWithArgs;
import fr.cea.ig.ngl.NGLApplication;
//import fr.cea.ig.ngl.dao.api.sra.AbstractSampleAPI;
import fr.cea.ig.ngl.dao.api.sra.AbstractStudyAPI;
import fr.cea.ig.ngl.dao.api.sra.ConfigurationAPI;
import fr.cea.ig.ngl.dao.api.sra.ExperimentAPI;
import fr.cea.ig.ngl.dao.api.sra.SampleAPI;
import fr.cea.ig.ngl.dao.api.sra.SubmissionAPI;
import fr.cea.ig.ngl.dao.readsets.ReadSetsAPI;
//import models.laboratory.common.instance.PropertyValue;
//import models.laboratory.common.instance.property.PropertySingleValue;
//import models.sra.submit.common.instance.AbstractSample;
//import models.sra.submit.common.instance.AbstractStudy;
import models.sra.submit.common.instance.Sample;
import models.sra.submit.common.instance.Submission;
//import models.sra.submit.common.instance.Readset;
//import models.sra.submit.common.instance.Submission;
//import models.sra.submit.sra.instance.Configuration;
//import models.sra.submit.sra.instance.Experiment;
import validation.ContextValidation;
import validation.sra.SraValidationHelper;
import play.data.validation.ValidationError;
import play.libs.Json;
//import play.shaded.ahc.io.netty.util.internal.StringUtil;
import services.ActivateServices;
import sra.scripts.UpdateSubmissionStateInWorkflow.MyParam;


/*
 * Exemple de lancement :
 * http://localhost:9000/sra/scripts/run/sra.scripts.TestCheckActivate?code=submissionCode
 * http://appdev.genoscope.cns.fr:9005
 * @author sgas
 *
 */
public class TestCheckActivate extends ScriptWithArgs<TestCheckActivate.MyParam> {

	private static final play.Logger.ALogger logger = play.Logger.of(TestCheckActivate.class);

//	private final SubmissionAPI     submissionAPI;
//	private final ConfigurationAPI  configurationAPI;
//	private final AbstractStudyAPI  abstractStudyAPI;
	private final ExperimentAPI     experimentAPI;
	private final NGLApplication    app;
	private final ActivateServices  activateServices;
	private final SubmissionAPI     submissionAPI;

	@Inject
	public TestCheckActivate(SubmissionAPI     submissionAPI,
					 ActivateServices  activateServices,
					 ExperimentAPI     experimentAPI,
					 ReadSetsAPI       readsetAPI,
					 NGLApplication    app) {

		this.activateServices  = activateServices;
		this.experimentAPI     = experimentAPI;
		this.app               = app;
		this.submissionAPI     = submissionAPI;
	}


	// structure de controle et de stockage des arguments de l'url
	public static class MyParam {
		public String code;
	}
	
	
	@Override
	public void execute(MyParam args) throws Exception {
		Submission submission = submissionAPI.dao_getObject(args.code);
		ContextValidation contextValidation = ContextValidation.createUpdateContext("sgas"); 
		activateServices.checkForActivatePrimarySubmission(contextValidation, submission);
//		if(contextValidation.hasErrors()) {
//			//contextValidation.displayErrors(logger, "debug");
//			Set<Entry<String, List<ValidationError>>> map = contextValidation.getErrors().entrySet();
//			for(Entry<?, ?> entry : map) {
//				println("cle: " + entry.getKey() + ", value: " + entry.getValue() + " <br/>");
//			}
//			println(Json.prettyPrint(app.errorsAsJson(contextValidation.getErrors())));				
//		} else {
//			println("ok pour checkForActivatePrimarySubmission");				
//		}		

	}
	
}


