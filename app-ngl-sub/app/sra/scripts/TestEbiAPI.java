package sra.scripts;

import javax.inject.Inject;

import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import services.SraEbiAPI;


//http://localhost:9000/sra/scripts/run/sra.scripts.TestEbiAPI
public class TestEbiAPI extends ScriptNoArgs {
	private final SraEbiAPI ebiAPI;	

	@Inject
	public TestEbiAPI(SraEbiAPI ebiAPI) {
		this.ebiAPI = ebiAPI;
	}
	
	@Override
	public void execute() throws Exception {
		if (ebiAPI.ebiSampleExists("ERS487755")) {
			printfln("ERS487755 present à l'EBI");
		} else {
			printfln("ERS487755 absent de l'EBI");
		}	
		if (ebiAPI.ebiSampleExists("ERXjdflksjqdskjf")) {
			printfln("ERXjdflksjqdskjf present à l'EBI");
		} else {
			printfln("ERXjdflksjqdskjf absent de l'EBI");
		}
		if (ebiAPI.ebiSubmissionExists("ERA972018")) {
			printfln("ERA972018 present à l'EBI");
			String xmlString = ebiAPI.ebiSubmissionXml("ERA972018");
			printfln("xml pour ERA972018  = '%s'", xmlString);
		} else {
			printfln("ERA972018 absent de l'EBI");
		}	
		// Pas de numeros d'accession pour les soumissions de type update et 
		// pas de stockage sur notre site de l'EBI
	}
	
}
