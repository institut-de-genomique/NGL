package sra.scripts.generic;

import javax.inject.Inject;

import fr.cea.ig.lfw.controllers.scripts.buffered.Script;
// Exemple de lancement :
//	 http://localhost:9000/sra/scripts/run/sra.scripts.generic.UpdateSubmissionStateOutWorkflow?state=SUB-N&codes=code_soumission_1&codes=code_soumission_2
//	  @author sgas
import sra.parser.UserRefCollabTypeParser;
	 
	 
public class TestParserRefCollab extends Script<TestParserRefCollab.MyParam>{
	

	private final UserRefCollabTypeParser        userRefCollabTypeParser;

	
	@Inject
	public TestParserRefCollab (UserRefCollabTypeParser        userRefCollabTypeParser) {
		this.userRefCollabTypeParser    = userRefCollabTypeParser;
	}
		
	// ma structure de controle et stockage des arguments de l'url
	public static class MyParam {
		//public String state;
		//public int code;
		public String state;
		public String[] codes;
	}
	

	@Override
	public void execute(MyParam args) throws Exception {
//		//String submissionCode = "GSC_APX_BXT_38AF1N87U";
//		//String submissionCode = "GSC_APX_BXT_38AG1BOO0";
//		
//		// si http://localhost:9000/sra/scripts/run/sra.scripts.UpdateSubmissionStateOutWorkflow?state=IW-SUB&codes=toto&codes=titi
//		mapUserRefCollab = userRefCollabToAc.loadMap(inputStreamUserFileRefCollabToAc);		
//		InputStream inputStreamUserFileRefCollabToAc = Tools.decodeBase64(submissionsCreationForm.base64UserFileRefCollabToAc);
//		UserRefCollabTypeParser userRefCollabToAc = new UserRefCollabTypeParser();
//		mapUserRefCollab = userRefCollabToAc.loadMap(inputStreamUserFileRefCollabToAc);		
//		logger.debug("\ntaille de la map des refCollab = " + mapUserRefCollab.size());
//		for (Iterator<Entry<String, UserRefCollabType>> iterator = mapUserRefCollab.entrySet().iterator(); iterator.hasNext();) {
//		  Entry<String, UserRefCollabType> entry = iterator.next();
//		  logger.debug("cle du userRefCollab = '" + entry.getKey() + "'");
//		  logger.debug("       study_ac : '" + entry.getValue().getStudyAc()+  "'");
//		  logger.debug("       sample_ac : '" + entry.getValue().getSampleAc()+  "'");
//		}
	}


}
