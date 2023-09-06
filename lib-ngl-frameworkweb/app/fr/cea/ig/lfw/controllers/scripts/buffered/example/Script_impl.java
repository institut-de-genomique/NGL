package fr.cea.ig.lfw.controllers.scripts.buffered.example;

import fr.cea.ig.lfw.controllers.scripts.buffered.Script;

/**
 * Script exemple à lancer avec url de la forme 
 * {@code http://localhost:9000/sra/scripts/run/Script_impl?state=IW-SUB&codeNum=34&states=titi&states=toto&codesNum=3}
 * <br>
 * Si parametre absent dans url alors declenchement d'une erreur.
 * <p>
 * L'héritage des classes paramtres pour les script servent juste a valider
 * que l'heritage est supporté pour les classes parametres. 
 * 
 * @author sgas
 *
 */
public class Script_impl extends Script<Script_impl.Args> {

	public static class Args extends Param {
		public int codeNum;  // Attention a mettre le modifier de champs public !!!!!
		public int[] codesNum;
	}

	public static class Param {
		public String state;
		public String[] states;
	}
	
	@Override
	public void execute(Args args) throws Exception {
		
		for (String s : args.states) {
			printfln(" states : '%s'", s);
		}
		for (int s : args.codesNum) {
			printfln(" codes : '%d'", s);
		}
		printfln ("Argument state = '%s'", args.state);
		printfln ("Argument code = '%d'", args.codeNum);
	}

}
	
