package controllers.main.tpl;

//import java.util.List;
//
//import static org.mongojack.DBQuery.*;
//import org.mongojack.DBQuery;

//import jsmessages.JsMessages;
//import models.administration.authorisation.Permission;
//import models.laboratory.common.description.CodeLabel;
//import models.laboratory.common.description.dao.CodeLabelDAO;
//import models.laboratory.protocol.instance.Protocol;
//import models.laboratory.reagent.description.AbstractCatalog;
//import models.laboratory.reagent.description.BoxCatalog;
//import models.laboratory.reagent.description.KitCatalog;
//import models.laboratory.reagent.description.ReagentCatalog;
//import models.laboratory.resolutions.instance.ResolutionConfiguration;
//import models.laboratory.valuation.instance.ValuationCriteria;
//import models.laboratory.project.instance.Project;
//import models.utils.InstanceConstants;
// import play.Play;
// import play.Routes;
// import play.routing.JavaScriptReverseRouter;

//import play.api.modules.spring.Spring;
//import play.libs.Scala;
//import play.mvc.Controller;
//import play.mvc.Http.Context;
import play.mvc.Result;
//import play.mvc.With;
import views.html.home;
import fr.cea.ig.MongoDBDAO;
//import controllers.history.UserHistory;
//import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.authentication.Authenticated;
//import fr.cea.ig.authentication.Authentication;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.lfw.Historized;
import fr.cea.ig.lfw.utils.JavascriptGeneration.Codes;
//import fr.cea.ig.lfw.utils.JavascriptGeneration.Permissions;
import fr.cea.ig.ngl.support.Executor;
import fr.cea.ig.ngl.support.NGLJavascript;
import fr.cea.ig.ngl.support.api.CodeLabelAPIHolder;
import fr.cea.ig.ngl.support.api.PermissionAPIHolder;
import fr.cea.ig.ngl.support.api.ProjectAPIHolder;
import fr.cea.ig.ngl.support.api.ProtocolAPIHolder;
import fr.cea.ig.ngl.support.api.ReagentCatalogAPIHolder;
import fr.cea.ig.ngl.support.api.ResolutionConfigurationAPIHolder;
import fr.cea.ig.ngl.support.api.ValuationCriteriaAPIHolder;
import models.laboratory.parameter.Parameter;
import models.utils.InstanceConstants;
import fr.cea.ig.ngl.NGLApplication;

import java.util.List;

import javax.inject.Inject;

import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;

import controllers.NGLController;

public class Main extends NGLController
		implements CodeLabelAPIHolder,
		           PermissionAPIHolder,
		           ProjectAPIHolder,
		           ProtocolAPIHolder,
		           ReagentCatalogAPIHolder,
		           ResolutionConfigurationAPIHolder,
		           ValuationCriteriaAPIHolder,
		           NGLJavascript,
		           Executor{

	private final home home;

	@Inject
	public Main(NGLApplication app, home home) { 
		super(app);
		this.home = home;
	}

	@Authenticated
	@Historized
	@Authorized.Read
	public Result home() {
		return ok(home.render());
	}

	public Result jsCodes() {
		Query query = DBQuery.in("typeCode","index-illumina-sequencing","index-nanopore-sequencing","index-pacbio-sequencing", "index-mgi-sequencing");
		List<Parameter> values = MongoDBDAO.find(InstanceConstants.PARAMETER_COLL_NAME, Parameter.class, query).toList();
		
		return result(() -> {
			return new Codes()
				.add(getCodeLabelAPI().all(), x -> x.tableName, x -> x.code, x -> x.label)
				.add(getProjectAPI().all(),   x -> "project",  x -> x.code, x -> x.name)
				.add(getValuationCriteriaAPI().all(), x -> "valuation_criteria", x -> x.code, x -> x.name)
				.add(values, x -> "value", x -> x.code, x -> x.name)
				.add(getResolutionConfigurationAPI().all(),
				         x -> x.resolutions,
				         x -> "resolution", x -> x.code, x -> x.name)
				.add(getProtocolAPI().all(),                      x -> "protocol",       x -> x.code, x -> x.name)
				.add(getReagentCatalogAPI().getKitCatalogs(),     x -> "reagentKit",     x -> x.code, x -> x.name)
				.add(getReagentCatalogAPI().getBoxCatalogs(),     x -> "reagentBox",     x -> x.code, x -> x.name)
				.add(getReagentCatalogAPI().getReagentCatalogs(), x -> "reagentReagent", x -> x.code, x -> x.name)
				.asCodeFunction();
		}, "error while bulding jscodes");
	}
	
	public Result javascriptRoutes() {
		return jsRoutes(controllers.experiments.api.routes.javascript.Experiments.list(),
						controllers.containers.api.routes.javascript.Containers.list(),
						controllers.processes.api.routes.javascript.Processes.list(),
						controllers.samples.api.routes.javascript.Samples.list());
	}	
}
