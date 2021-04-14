package sra.scripts;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import fr.cea.ig.ngl.dao.api.sra.ExperimentAPI;
import fr.cea.ig.ngl.dao.api.sra.SubmissionAPI;
import models.sra.submit.common.instance.Submission;
import services.XmlServices;
import models.sra.submit.util.SraParameter;
import models.sra.submit.util.VariableSRA;
import models.utils.InstanceConstants;

/*
 * Script test pour variableSra
 * {@code http://localhost:9000/sra/scripts/run/sra.scripts.TestVariableSra}
 * <br>
 * Si parametre absent dans url => declenchement d'une erreur.
 *  
 * @author sgas
 *
 */
public class TestVariableSra extends ScriptNoArgs {
	
	@Inject
	public TestVariableSra() {
	}
	
	@Override
	public void execute() throws Exception {
		Map<String, ArrayList<String>> map = VariableSRA.mapPseudoStateCodeToStateCodes();
		
		
		for (Iterator<Entry<String, ArrayList<String>>> iterator = map.entrySet().iterator(); iterator.hasNext();) {
			  Entry<String, ArrayList<String>> entry = iterator.next();
			  String code = entry.getKey();
			  printfln("code : %s", code);
			  for(String value : entry.getValue()) {
				  printfln("      valeur : %s", value);
			  }
		}	

		
		for (String code : VariableSRA.mapPseudoStateCodeToStateCodes().keySet()) {
			printfln("code = %s", code);
			for (String value : VariableSRA.mapPseudoStateCodeToStateCodes().get(code)){
				printfln("       value = %s", value);
			}
		}
		
	}



}
