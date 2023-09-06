package controllers.main.tpl;

import javax.inject.Inject;

import controllers.NGLController;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.lfw.Historized;
import fr.cea.ig.lfw.utils.JavascriptGeneration.Codes;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.support.Executor;
import fr.cea.ig.ngl.support.NGLJavascript;
import fr.cea.ig.ngl.support.api.CodeLabelAPIHolder;
import fr.cea.ig.ngl.support.api.ReagentCatalogAPIHolder;
import play.mvc.Result;
import views.html.home;

public class Main extends NGLController
                 implements NGLJavascript,
                            Executor,
                            CodeLabelAPIHolder,
		ReagentCatalogAPIHolder {

	private final home home;

	@Inject
	public Main(NGLApplication app, home home) {
		super(app);
		this.home = home;
		this.logger.info("injected");
	}

	@Authenticated
	@Historized
	@Authorized.Read
	public Result home() {
		return ok(this.home.render());
	}

	public Result jsCodes() {
		return this.result(() -> new Codes()
				.add(this.getCodeLabelAPI().all(),                     x -> x.tableName,       x -> x.code, x -> x.label)
				.addValuationCodes()
				.add(this.getReagentCatalogAPI().getKitCatalogs(),     x -> "kitCatalogs",     x -> x.code, x -> x.name)
				.add(this.getReagentCatalogAPI().getBoxCatalogs(),     x -> "boxCatalogs",     x -> x.code, x -> x.name)
				.add(this.getReagentCatalogAPI().getReagentCatalogs(), x -> "reagentCatalogs", x -> x.code, x -> x.name)
				.asCodeFunction(),
				"error when building codes");
	}

}
