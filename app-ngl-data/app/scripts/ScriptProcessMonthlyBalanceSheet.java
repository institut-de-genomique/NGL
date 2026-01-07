package scripts;

import javax.inject.Inject;

import fr.cea.ig.lfw.controllers.scripts.buffered.Script;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.NGLConfig;
import models.Constants;
import services.instance.balancesheet.UpdateMonthlyBalanceSheet;
import validation.ContextValidation;

/**
 * Script permettant d'exécuter la mise à jour de tous les bilans annuels.
 * CNS / CNG
 * 
 * @author aprotat
 *
 */
public class ScriptProcessMonthlyBalanceSheet extends Script<ScriptProcessMonthlyBalanceSheet.Args> {
	
	public static class Args {}
	
	private final NGLApplication app;
	private NGLConfig config;
	
	@Inject
	public ScriptProcessMonthlyBalanceSheet(NGLApplication app, NGLConfig config) {
		super();
		this.app=app;
		this.config=config;
	}

	@Override
	public void execute(Args args) throws Exception {
		ContextValidation contextError = ContextValidation.createUndefinedContext(Constants.NGL_DATA_USER);
		UpdateMonthlyBalanceSheet update = new UpdateMonthlyBalanceSheet(app, config);
		getLogger().info("Try re-calculating evry balanceSheets...");
		update.runImport(contextError);		
		getLogger().info("End saving balanceSheets");
	}

}
