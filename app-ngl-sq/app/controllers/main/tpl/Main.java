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
import fr.cea.ig.ngl.NGLApplication;

import javax.inject.Inject;

import controllers.NGLController;

// public class Main extends CommonController { // done

// @With({fr.cea.ig.authentication.Authenticate.class, UserHistory.class})
// @fr.cea.ig.authentication.Authenticated
// @With(UserHistory.class)
// public class Main extends NGLBaseController {

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
	
/*	// final static JsMessages messages = JsMessages.create(play.Play.application());

	//private static JsMessages messages;
	private final JsMessages messages;
	
	private final home home;

	// private final NGLContext ctx;
	
	// JsMessagesFactory is in ctx
	@Inject
	public Main(NGLContext ctx, jsmessages.JsMessagesFactory jsMessagesFactory, home home) {
		super(ctx);
		messages  = jsMessagesFactory.all();
		this.home = home;
		//this.ctx  = ctx;
	}*/

	@Authenticated
	@Historized
	@Authorized.Read
	public Result home() {
		return ok(home.render());
	}

	/*public Result jsPermissions() {
		// return Permissions.jsPermissions(Permission.find.findByUserLogin(Authentication.getUser()), x -> x.code);
		// return Permissions.jsPermissions(getPermissionAPI().byUserLogin(Authentication.getUser()), x -> x.code);
		String user = Authentication.getUser();
		return result(() -> jsPermissions(getPermissionAPI().byUserLogin(user), x -> x.code),
					  "could not get permissions for " + user);
	}*/

	/*
	 * jsPermissions() method
	 * These methods generate Permissions.js' Check Method
	 */
	/*
	public Result jsPermissions() {
		return ok(listPermissions()).as("application/javascript");
	}

	private String listPermissions() {
//		List<Permission> permissions = Permission.find.findByUserLogin(Context.current().session().get("NGL_FILTER_USER"));
		List<Permission> permissions = Permission.find.findByUserLogin(Authentication.getUser(Context.current().session()));
		List<Permission> permissions = Permission.find.findByUserLogin(Authentication.getUser());
		StringBuilder sb = new StringBuilder();
		sb.append("Permissions={}; Permissions.check=(function(param){var listPermissions=[");
		for(Permission p:permissions){
			sb.append("\"").append(p.code).append("\",");
		}
		sb.deleteCharAt(sb.lastIndexOf(","));
		sb.append("];return(listPermissions.indexOf(param) != -1);})");
		return sb.toString();
	}
*/
	// codes.mapDotColon(InstanceConstants.REAGENT_CATALOG_COLL_NAME, KitCatalog.class, DBQuery.is("category", "Kit"),
	//          x -> "reagentKit", x -> x.code, x -> x.name);

	public Result jsCodes() {
		return result(() -> {
			return new Codes()
				//.add(Spring.getBeanOfType(CodeLabelDAO.class).findAll(), x -> x.tableName, x -> x.code, x -> x.label)
				.add(getCodeLabelAPI().all(), x -> x.tableName, x -> x.code, x -> x.label)
				// .add(MongoDBDAO.find(InstanceConstants.PROJECT_COLL_NAME, Project.class).toList(),
				.add(getProjectAPI().all(),   x -> "project",  x -> x.code, x -> x.name)
		// .add(MongoDBDAO.find(InstanceConstants.VALUATION_CRITERIA_COLL_NAME, ValuationCriteria.class).toList(), x -> "valuation_criteria", x -> x.code, x -> x.name)
				.add(getValuationCriteriaAPI().all(), x -> "valuation_criteria", x -> x.code, x -> x.name)
		// .add(MongoDBDAO.find(InstanceConstants.RESOLUTION_COLL_NAME, ResolutionConfiguration.class).toList(),
				.add(getResolutionConfigurationAPI().all(),
				         x -> x.resolutions,
				         x -> "resolution", x -> x.code, x -> x.name)
		// .add(MongoDBDAO.find(InstanceConstants.PROTOCOL_COLL_NAME,Protocol.class).toList(), x -> "protocol", x -> x.code, x -> x.name)
				.add(getProtocolAPI().all(),                      x -> "protocol",       x -> x.code, x -> x.name)
		// .add(MongoDBDAO.find(InstanceConstants.REAGENT_CATALOG_COLL_NAME, KitCatalog.class, DBQuery.is("category", "Kit")).toList(),
				.add(getReagentCatalogAPI().getKitCatalogs(),     x -> "reagentKit",     x -> x.code, x -> x.name)
		// .add(MongoDBDAO.find(InstanceConstants.REAGENT_CATALOG_COLL_NAME, BoxCatalog.class, DBQuery.is("category", "Box")).toList(),
				.add(getReagentCatalogAPI().getBoxCatalogs(),     x -> "reagentBox",     x -> x.code, x -> x.name)
		// .add(MongoDBDAO.find(InstanceConstants.REAGENT_CATALOG_COLL_NAME, ReagentCatalog.class, DBQuery.is("category", "Reagent")).toList(),
				.add(getReagentCatalogAPI().getReagentCatalogs(), x -> "reagentReagent", x -> x.code, x -> x.name)
				.asCodeFunction();
		}, "error while bulding jscodes");
	}

	/*
	public Result jsCodes() {
		return ok(generateCodeLabel()).as("application/javascript");
	}
	
	private static StringBuilder dotColon(StringBuilder sb, String a, String b, String c) {
		return sb
				.append("\"")
				.append(a)
				.append(".")
				.append(b)
				.append("\":\"")
				.append(c)
				.append("\",");
	}
	
	private String generateCodeLabel() {
		CodeLabelDAO dao = Spring.getBeanOfType(CodeLabelDAO.class);

		StringBuilder sb = new StringBuilder();
		sb.append("Codes=(function(){var ms={");
		
		List<CodeLabel> list = dao.findAll();
//		for (CodeLabel cl : list) {
//			sb.append("\"").append(cl.tableName).append(".").append(cl.code)
//			.append("\":\"").append(cl.label).append("\",");
//		}
		for (CodeLabel cl : list) dotColon(sb,cl.tableName,cl.code,cl.label);
		
		List<Project> projects = MongoDBDAO.find(InstanceConstants.PROJECT_COLL_NAME, Project.class).toList();
//		for (Project p:  projects) {
//			sb.append("\"").append("project").append(".").append(p.code)
//			.append("\":\"").append(p.name).append("\",");
//		}
		for (Project p:  projects) dotColon(sb,"project",p.code,p.name);
		
		List<ValuationCriteria> criterias = MongoDBDAO.find(InstanceConstants.VALUATION_CRITERIA_COLL_NAME, ValuationCriteria.class).toList();
//		for (ValuationCriteria vc:  criterias) {
//			sb.append("\"").append("valuation_criteria").append(".").append(vc.code)
//			.append("\":\"").append(vc.name).append("\",");
//		}
		for (ValuationCriteria vc:  criterias) dotColon(sb,"valuation_criteria",vc.code,vc.name);
		
		List<ResolutionConfiguration> resolutionConfigs = MongoDBDAO.find(InstanceConstants.RESOLUTION_COLL_NAME, ResolutionConfiguration.class).toList();
		resolutionConfigs
		.stream()
		.map(rc -> rc.resolutions)
		.flatMap(List::stream)
		.forEach(r -> {
			//sb.append("\"").append("resolution").append(".").append(r.code)
			//.append("\":\"").append(r.name).append("\",");
			dotColon(sb,"resolution",r.code,r.name);
		});
		
		List<Protocol> protocols = MongoDBDAO.find(InstanceConstants.PROTOCOL_COLL_NAME,Protocol.class).toList();
		//for (Protocol protocol:protocols) {
		//	sb.append("\"").append("protocol").append(".").append(protocol.code).append("\":\"").append(protocol.name).append("\",");
		//}
		for (Protocol protocol:protocols) dotColon(sb,"protocol",protocol.code,protocol.name);
		
		MongoDBDAO.find(InstanceConstants.REAGENT_CATALOG_COLL_NAME, KitCatalog.class, DBQuery.is("category", "Kit"))
		.cursor.forEach(reagent -> {
			// sb.append("\"").append("reagentKit").append(".").append(reagent.code)
			// .append("\":\"").append(reagent.name).append("\",");
			dotColon(sb,"reagentKit",reagent.code,reagent.name);
		});

		MongoDBDAO.find(InstanceConstants.REAGENT_CATALOG_COLL_NAME, BoxCatalog.class, DBQuery.is("category", "Box"))
		.cursor.forEach(reagent -> {
			//sb.append("\"").append("reagentBox").append(".").append(reagent.code)
			//.append("\":\"").append(reagent.name).append("\",");
			dotColon(sb,"reagentBox",reagent.code,reagent.name);
		});

		MongoDBDAO.find(InstanceConstants.REAGENT_CATALOG_COLL_NAME, ReagentCatalog.class, DBQuery.is("category", "Reagent"))
		.cursor.forEach(reagent -> {
			//sb.append("\"").append("reagentReagent").append(".").append(reagent.code)
			//.append("\":\"").append(reagent.name).append("\",");
			dotColon(sb,"reagentReagent",reagent.code,reagent.name);
		});

		sb.append("};return function(k){if(typeof k == 'object'){for(var i=0;i<k.length&&!ms[k[i]];i++);var m=ms[k[i]]||k[0]}else{m=ms[k]||k}for(i=1;i<arguments.length;i++){m=m.replace('{'+(i-1)+'}',arguments[i])}return m}})();");
		return sb.toString();
	}
*/

	/*
	public Result jsAppURL() {
		return ok(getAppURL()).as("application/javascript");
	}

	private String getAppURL() {
		StringBuilder sb = new StringBuilder();
		sb.append("function AppURL (app){");
		sb.append("if(app===\"sq\") return \"");
		// sb.append("\""+Play.application().configuration().getString("sq.url")+"\";");
		sb.append(ctx.config().getString("sq.url"));
		sb.append("\"; else if(app===\"bi\") return \"");
		// sb.append("\""+Play.application().configuration().getString("bi.url")+"\";");
		// sb.append("\"" + ctx.config().getString("bi.url") + "\";");
		sb.append(ctx.config().getString("bi.url"));
		sb.append("\"; else if(app===\"project\") return \"");
		// sb.append("\""+Play.application().configuration().getString("project.url")+"\";");
		sb.append(ctx.config().getString("project.url"));
		sb.append("\";}");
		return sb.toString();
	}
*/
	
	public Result javascriptRoutes() {
		return jsRoutes(controllers.experiments.api.routes.javascript.Experiments.list(),
						controllers.containers.api.routes.javascript.Containers.list(),
						controllers.processes.api.routes.javascript.Processes.list(),
						controllers.samples.api.routes.javascript.Samples.list());
	}
	
	/*
	public Result javascriptRoutes() {
		// response().setContentType("text/javascript");
		return ok(	  	      
				// Routes.javascriptRouter("jsRoutes",  	   
				JavaScriptReverseRouter.create("jsRoutes",  	   
						controllers.experiments.api.routes.javascript.Experiments.list(),
						controllers.containers.api.routes.javascript.Containers.list(),
						controllers.processes.api.routes.javascript.Processes.list(),
						controllers.samples.api.routes.javascript.Samples.list()
						)
				).as("text/javascript");
	}
*/

	// public static Result jsMessages() {
	/*
	public Result jsMessages() {
		// return ok(messages.generate("Messages")).as("application/javascript");
		//return ok(messages.all(Scala.Option("Messages"))).as("application/javascript");
		return ok(messages.apply(Scala.Option("Messages"), jsmessages.japi.Helper.messagesFromCurrentHttpContext())).as("application/javascript");
	}
*/
/*	public Result jsPrintTag(){
		// Boolean isPrintTag = Play.application().configuration().getBoolean("ngl.printing.cb", Boolean.FALSE);
		boolean tag = ctx.config().isBarCodePrintingEnabled(); 
		// String js = "PrintTag={}; PrintTag.isActive =(function(){return "+isPrintTag.booleanValue()+";});";
		String js = "PrintTag={}; PrintTag.isActive =(function(){return " + tag + ";});";
		return ok(js).as("application/javascript");
	}*/
	
}
