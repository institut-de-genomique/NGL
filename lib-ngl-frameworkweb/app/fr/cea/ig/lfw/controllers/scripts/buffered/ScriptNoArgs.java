package fr.cea.ig.lfw.controllers.scripts.buffered;

/**
 * Classe abstraite avec une methode execute à definir dans la classe fille.
 * La methode execute sera executé au chargement de l'url
 * de la forme: {@code http://localhost:9000/sra/scripts/run/NameClasseFille}.
 * Si url avec argument alors declenchement d'une erreur.
 * 
 * @author sgas
 *
 */
public  abstract class ScriptNoArgs extends Script<ScriptNoArgs.NoArgs> {
	
	public static final class NoArgs {
	}
	
	@Override
	public void execute(NoArgs noArgs) throws Exception {
		execute();
	}
	
	public abstract void execute() throws Exception;
	
}

