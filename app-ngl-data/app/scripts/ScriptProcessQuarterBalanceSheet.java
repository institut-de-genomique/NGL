package scripts;

import javax.inject.Inject;

import fr.cea.ig.lfw.controllers.scripts.buffered.Script;
import fr.cea.ig.ngl.NGLApplication;
import models.Constants;
import services.instance.balancesheet.UpdateQuarterBalanceSheet;
import validation.ContextValidation;

/**
 * Script permettant d'exécuter la mise à jour de tous les bilans annuels.
 * CNS / CNG
 * 
 * @author aprotat
 *
 */
public class ScriptProcessQuarterBalanceSheet extends Script<ScriptProcessQuarterBalanceSheet.Args> {
	
	public static class Args {}
	
private final NGLApplication app;
	
	@Inject
	public ScriptProcessQuarterBalanceSheet(NGLApplication app) {
		super();
		this.app=app;
	}

	@Override
	public void execute(Args args) throws Exception {
		ContextValidation contextError = ContextValidation.createUndefinedContext(Constants.NGL_DATA_USER);
		contextError.addKeyToRootKeyName("import");
		UpdateQuarterBalanceSheet update = new UpdateQuarterBalanceSheet(app);
		getLogger().debug("Start calculating balanceSheets");
		update.runImport(contextError);		
		getLogger().debug("End saving balanceSheets");
	}

}
