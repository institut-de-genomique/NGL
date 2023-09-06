package sra.scripts.generic;

import javax.inject.Inject;

import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import services.SraEbiAPI;

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
			printfln("ERXjdflksjqdskjf");
		} else {
			printfln("ERXjdflksjqdskjf absent de l'EBI");
		}
	}
	
}
