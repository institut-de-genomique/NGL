package sra.scripts;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import models.sra.submit.util.VariableSRA;

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
