package sra.scripts;

import javax.inject.Inject;

import fr.cea.ig.lfw.controllers.scripts.ScriptNoArgs;
import sra.scripts.utils.EbiAPI;

public class TestEbiAPI extends ScriptNoArgs {
	private final EbiAPI ebiAPI;	

	@Inject
	public TestEbiAPI(EbiAPI ebiAPI) {
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
