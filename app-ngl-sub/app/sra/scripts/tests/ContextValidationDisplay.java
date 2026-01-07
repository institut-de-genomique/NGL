package sra.scripts.tests;


import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import validation.ContextValidation;

/*
 * Script TEST
 * {@code http://localhost:9000/sra/scripts/run/sra.scripts.tests.ContextValidationDisplay}
 * <br> *  
 * @author sgas
 *
 */
public class ContextValidationDisplay extends ScriptNoArgs {

	@Override
	public void execute() throws Exception {
		ContextValidation ctx = ContextValidation.createCreationContext("ngsrg");
		ctx.addError("cle_A", "mess_A");
		ctx.addError("cle_B", "mess_B");
		ctx.addError("cle_C", "mess_C");
		ctx.displayUniqMessageError(logger, "debug");
		println("Verifier que vous avez bien un seul log en debug indiquant 3 cles et 3 mess d'erreurs");		
	}
	
}

