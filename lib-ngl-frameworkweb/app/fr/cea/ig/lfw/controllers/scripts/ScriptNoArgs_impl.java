package fr.cea.ig.lfw.controllers.scripts;

/**
 * Script exemple à lancer avec url de la forme
 * {@code http://localhost:9000/sra/scripts/run/ScriptNoArgs}.
 * Si parametre present dans url alors declenche une erreur.
 * 
 * @author sgas
 *
 */
public class ScriptNoArgs_impl extends ScriptNoArgs {
	
	@Override
	public void execute() {
		println("Hello word");
	}

	
}
