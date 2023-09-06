package sra.scripts.generic;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import fr.cea.ig.lfw.controllers.scripts.chunked.ScriptWithArgs;
import models.sra.submit.sra.instance.UserSampleType;
import sra.parser.UserSampleTypeParser;
import validation.ContextValidation;
//Exemple de lancement :
//http://localhost:9000/sra/scripts/run/sra.scripts.generic.TestUserSampleParser?fileName=
//http://localhost:9000/sra/scripts/run/sra.scripts.generic.TestUserSampleParser?fileName=C:\Users\sgas\julie_test_submission_3.0\AKL_userSample.txt
public class TestUserSampleParser extends ScriptWithArgs<TestUserSampleParser.MyParam> {
	private static final play.Logger.ALogger logger = play.Logger.of(TestUserSampleParser.class);

	@Inject
	private TestUserSampleParser() {	
	}
	// ma structure de controle et stockage des arguments de l'url
	public static class MyParam {
		public String fileName;
	}

	public Map<String, UserSampleType> parseUserFileSample(String fileName) {
		Map<String, UserSampleType> mapUserSample = new HashMap<>();
		InputStream inputStreamUserFileSample;
		try {
			inputStreamUserFileSample = new FileInputStream(fileName);
			UserSampleTypeParser userSample = new UserSampleTypeParser();
		
			mapUserSample = userSample.loadMap(inputStreamUserFileSample);		
			logger.debug("\ntaille de la map des userSample = " + mapUserSample.size());
			for (Iterator<Entry<String, UserSampleType>> iterator = mapUserSample.entrySet().iterator(); iterator.hasNext();) {
				Entry<String, UserSampleType> entry = iterator.next();
				println("cle du userSample = '" + entry.getKey() + "'");
				println("       value = '"+ entry.getValue() + "'");
				println("                  title = '" + entry.getValue().getTitle() + "'");
				println("                  anonymizedName  = '" + entry.getValue().getAnonymizedName() + "'");
				println("                  description = '" + entry.getValue().getDescription() + "'");
				println("                  attributes  = '" + entry.getValue().getAttributes() + "'");
			}
			String user = "sgas";
			ContextValidation contextValidation = ContextValidation.createUndefinedContext(user);

			for (Iterator<Entry<String, UserSampleType>> iterator = mapUserSample.entrySet().iterator(); iterator.hasNext();) {
				Entry<String, UserSampleType> entry = iterator.next();
				entry.getValue().validate(contextValidation);
			}
			if(contextValidation.hasErrors()) {
				println("Erreur dans le fichiers des samples");
				println(contextValidation.getErrors().toString());
			} else {
				println("Pas d'erreur dans le fichier des samples");
			}	
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return mapUserSample;
	}


	public void execute(MyParam args) throws Exception {
		Map<String, UserSampleType> mapUserSample = parseUserFileSample(args.fileName); 
	}
	
}
	