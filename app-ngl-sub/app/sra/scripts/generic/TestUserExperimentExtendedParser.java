package sra.scripts.generic;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import javax.inject.Inject;

//import models.sra.submit.common.instance.UserExperimentType;
import fr.cea.ig.lfw.controllers.scripts.chunked.ScriptWithArgs;
import models.sra.submit.sra.instance.UserExperimentExtendedType;
import sra.parser.UserExperimentExtendedTypeParser;
import validation.ContextValidation;
//Exemple de lancement :
//http://localhost:9000/sra/scripts/run/sra.scripts.generic.TestUserExperimentExtendedParser?fileName=C:\Users\sgas\julie_test_submission_3.0\AKL_userExperiment.txt
public class TestUserExperimentExtendedParser extends ScriptWithArgs<TestUserExperimentExtendedParser.MyParam> {
	private static final play.Logger.ALogger logger = play.Logger.of(TestUserExperimentExtendedParser.class);

	@Inject
	private TestUserExperimentExtendedParser() {	
	}
	// ma structure de controle et stockage des arguments de l'url
	public static class MyParam {
		public String fileName;
	}

	public Map<String, UserExperimentExtendedType> parseUserFileExperiment(String fileName) {
		Map<String, UserExperimentExtendedType> mapUserExperiment = new HashMap<>();
		InputStream inputStreamUserFileExperiment;
		try {
			inputStreamUserFileExperiment = new FileInputStream(fileName);
			UserExperimentExtendedTypeParser userExperiment = new UserExperimentExtendedTypeParser();
		
			mapUserExperiment = userExperiment.loadMap(inputStreamUserFileExperiment);		
			logger.debug("\ntaille de la map des userExperiment = " + mapUserExperiment.size());
			for (Iterator<Entry<String, UserExperimentExtendedType>> iterator = mapUserExperiment.entrySet().iterator(); iterator.hasNext();) {
				Entry<String, UserExperimentExtendedType> entry = iterator.next();
				println("cle du userExperiment = '" + entry.getKey() + "'");
//				if(entry.getValue().getLibraryStrategy() != null) {
//					println("                  libraryStrategy = '" + entry.getValue().getLibraryStrategy() + "'");
//				}
				println("                  library_strategy  = '" + entry.getValue().getLibraryStrategy() + "'");
				println("                  library_source    = '" + entry.getValue().getLibrarySource() + "'");
				println("                  library_selection = '" + entry.getValue().getLibrarySelection() + "'");
				println("                  library_construction_protocol  = '" + entry.getValue().getLibraryProtocol() + "'");
				println("                  library_layout_nominal_length  = '" + entry.getValue().getNominalLength() + "'");
				println("                  title  = '" + entry.getValue().getTitle() + "'");
				println("                  spot_length  = '" + entry.getValue().getSpotLength() + "'");
				println("                  last_base_coordonnee  = '" + entry.getValue().getLastBaseCoordonnee() + "'");
				println("                  study_accession = '" + entry.getValue().getStudyAccession() + "'");
				println("                  sample_accession = '" + entry.getValue().getSampleAccession() + "'");

			}
			String user = "sgas";
			ContextValidation contextValidation = ContextValidation.createUndefinedContext(user);

			for (Iterator<Entry<String, UserExperimentExtendedType>> iterator = mapUserExperiment.entrySet().iterator(); iterator.hasNext();) {
				Entry<String, UserExperimentExtendedType> entry = iterator.next();
				entry.getValue().validate(contextValidation);
			}
			if(contextValidation.hasErrors()) {
				println("Erreur dans le fichiers des experiments");
				println(contextValidation.getErrors().toString());
			} else {
				println("Pas d'erreur dans le fichier des experiments");
			}	
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return mapUserExperiment;
	}


	public void execute(MyParam args) throws Exception {
		Map<String, UserExperimentExtendedType> mapUserExperiment = parseUserFileExperiment(args.fileName); 
	}
	
}
	