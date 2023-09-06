package sra.scripts;

import javax.inject.Inject;

import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.sra.AbstractStudyAPI;
import fr.cea.ig.ngl.dao.api.sra.ConfigurationAPI;
import fr.cea.ig.ngl.dao.api.sra.ExperimentAPI;
import fr.cea.ig.ngl.dao.api.sra.SampleAPI;
import fr.cea.ig.ngl.dao.api.sra.SubmissionAPI;
import fr.cea.ig.ngl.dao.readsets.ReadSetsAPI;
import validation.ContextValidation;
import validation.sra.SraValidationHelper;
import play.libs.Json;


/*
 * Script a lancer pour avoir les numeros d'accession associés à une soumission ou plusieurs soumissions.
 * Exemple de lancement :
 * http://localhost:9000/sra/scripts/run/sra.scripts.TestSample
 * @author sgas
 *
 */
public class TestSample extends ScriptNoArgs {

	private static final play.Logger.ALogger logger = play.Logger.of(TestSample.class);

//	private final SubmissionAPI     submissionAPI;
//	private final ConfigurationAPI  configurationAPI;
//	private final AbstractStudyAPI  abstractStudyAPI;
	// private final SampleAPI sampleAPI;
//	private final ExperimentAPI     experimentAPI;
	private final NGLApplication    app;

	@Inject
	public TestSample(SubmissionAPI     submissionAPI,
					 ConfigurationAPI  configurationAPI,
					 AbstractStudyAPI  abstractStudyAPI,
					 SampleAPI         sampleAPI,
					 ExperimentAPI     experimentAPI,
					 ReadSetsAPI       readsetAPI,
					 NGLApplication    app) {

//		this.submissionAPI     = submissionAPI;
//		this.configurationAPI  = configurationAPI;
//		this.abstractStudyAPI  = abstractStudyAPI;
		// this.sampleAPI = sampleAPI;
//		this.experimentAPI     = experimentAPI;
		this.app               = app;
	}


	// structure de controle et de stockage des arguments de l'url
	public static class MyParam {
		public String code;
	}
	
//	public boolean validateAttributes(String value) {
////		String patternTagValue = "<([0-9a-zA-Z]+)>\\s*([0-9a-zA-Z]+)\\s*</([0-9a-zA-Z]+)>(.*)";
//		String patternTagValue = "<([^<>\\s]+)>\\s*([^<>\\s]+)\\s*</([^<>\\s]+)>(.*)";
//		
//		java.util.regex.Pattern pTV = Pattern.compile(patternTagValue);
//		logger.debug("Dans SraValidationHelper::validateAttributes");
//
//		// Enlever les retours charriots avant de tester la chaine :
//		String RC = System.getProperty("line.separator"); 
//		String aTester = value.replaceAll(RC,"" );
//		aTester = aTester.replaceAll("\n","" ); // le line.separator ne suffit pas si insertion dans base d'un retour charriot
//		aTester = aTester.replaceAll("\r","" );
//		logger.debug("aTester = "+ aTester);
//
//		boolean cond = true;
//		while (StringUtils.isNotBlank(aTester) && cond == true) {
//			java.util.regex.Matcher mTV = pTV.matcher(aTester);
//			// Appel de find obligatoire pour pouvoir récupérer $1 ...$n
//			if ( ! mTV.find() ) {
//				// autre ligne que tag value.	
//				logger.debug("La chaine '"+ aTester + "' ne matche pas avec le pattern " +  patternTagValue);
//				cond = false;
//			} else {
//				String tag = mTV.group(1);
//				String tag_fermant = mTV.group(3);
//				logger.debug("tag = "+ tag);
//				logger.debug("tag_fermant = "+ tag_fermant);
//				if (! tag.equals(tag_fermant)) {
//					logger.debug("problemes de tag qui ne matchent pas dans la chaine "+ aTester);
//					cond = false;
//				} else {
//					String val = mTV.group(2);
//					logger.debug("tag = " + tag +  " tag_fermant = " + tag_fermant + " val = " + val);
//					logger.debug(mTV.group(4));
//					if(StringUtils.isNotBlank(mTV.group(4))) {
//						aTester = mTV.group(4);
//						logger.debug("tag = " + tag +  " tag_fermant = " + tag_fermant + " val="+ val + "  aTester = '" + aTester + "'");
//					} else {
//						aTester = "";
//					}
//				}
//			}
//		}
//		return cond;
//	}
//	


	@Override
	public void execute() throws Exception {
		ContextValidation contextValidation = ContextValidation.createUpdateContext("ngsrg"); 
		String sampleAttributes = "<SAMPLE_ATTRIBUTE><TAG>Strain</TAG><VALUE>ABBA</VALUE></SAMPLE_ATTRIBUTE><SAMPLE_ATTRIBUTE><TAG>Depth</TAG><VALUE>200</VALUE><UNITS>m</UNITS></SAMPLE_ATTRIBUTE> <SAMPLE_ATTRIBUTE><TAG>Sample Collection Device</TAG><VALUE>high volume peristaltic pump [HVP-PUMP]</VALUE></SAMPLE_ATTRIBUTE>";
//ok 		String sampleAttributes = "<SAMPLE_ATTRIBUTE><TAG>Strain</TAG><VALUE>ABBA</VALUE></SAMPLE_ATTRIBUTE>";
//ok		String sampleAttributes = "<SAMPLE_ATTRIBUTE><TAG>Depth</TAG><VALUE>200</VALUE><UNITS>m</UNITS></SAMPLE_ATTRIBUTE>";
//ok		String sampleAttributes = "<SAMPLE_ATTRIBUTE><TAG>Sample Collection Device</TAG><VALUE>high volume peristaltic pump [HVP-PUMP]</VALUE></SAMPLE_ATTRIBUTE>";
		SraValidationHelper.newValidateAttributesRequired (contextValidation,"attributes", sampleAttributes);
		if(contextValidation.hasErrors()) {
			contextValidation.displayErrors(logger, "debug");
			println(Json.prettyPrint(app.errorsAsJson(contextValidation.getErrors())));				
		} else {
			printfln("ok pour sample.attributes=%s", sampleAttributes);				
		}		
	}
}


