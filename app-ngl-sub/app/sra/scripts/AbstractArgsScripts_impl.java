package sra.scripts;

import fr.cea.ig.lfw.controllers.scripts.buffered.Script;

class AbstractArgsScripts_impl extends Script<AbstractArgsScripts_impl.Arg> {
	
	public static class Arg {
		String[] states;	
		int[] codes;
		String state;
		int code;
	}
	
 	@Override
	public void execute(Arg args) throws Exception {	
		for (String s : args.states) {
			printfln(" states : %s", s);
		}
		for (int s : args.codes) {
			printfln(" codes : %d", s);
		}
		printfln ("Argument state = %s", args.state);
		printfln ("Argument code = %d", args.code);

	}

	


class AAS4_impl_fille extends AbstractArgsScripts_impl {

	// La classe qui definit la structure Args de recuperation des parametres est definie dans 
	// la classe mere.
	

 
	@Override
	public void execute(Arg args) throws Exception {
		
		for (String s : args.states) {
			printfln(" states : %s", s);
		}
		for (int s : args.codes) {
			printfln(" codes : %d", s);
		}
		printfln ("Argument state = %s", args.state);
		printfln ("Argument code = %d", args.code);
	}

}

}