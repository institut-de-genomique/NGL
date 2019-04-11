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

//import controllers.CommonController;
// public class Main extends -CommonController {
public class Main extends NGLController 
                 implements NGLJavascript,
                            Executor,
                            CodeLabelAPIHolder,
                            ReagentCatalogAPIHolder { // NGLBaseController {

//	private static final play.Logger.ALogger logger = play.Logger.of(Main.class);
	
	// final static JsMessages messages = JsMessages.create(play.Play.application());

	// private static JsMessages messages;
	
	private final home home;

	@Inject
	public Main(NGLApplication app, /*jsmessages.JsMessagesFactory jsMessagesFactory,*/ home home) {
		super(app);
		// logger.debug("injecting " + jsMessagesFactory);
		// messages = jsMessagesFactory.all();
		this.home = home;
		logger.info("injected");
	}

	@Authenticated
	@Historized
	@Authorized.Read
	public Result home() {
		return ok(home.render());
	}

	/*
	public static Result jsCodes() {
		return ok(generateCodeLabel()).as("application/javascript");
	}

	private static String generateCodeLabel() {
		CodeLabelDAO dao = Spring.getBeanOfType(CodeLabelDAO.class);
		List<CodeLabel> list = dao.findAll();

		StringBuilder sb = new StringBuilder();
		sb.append("Codes=(function(){var ms={");
		for(CodeLabel cl : list){
			sb.append("\"").append(cl.tableName).append(".").append(cl.code)
			.append("\":\"").append(cl.label).append("\",");
		}
		sb.append("\"valuation.TRUE\":\"Oui\",");
		sb.append("\"valuation.FALSE\":\"Non\",");
		sb.append("\"valuation.UNSET\":\"---\",");
		List<KitCatalog> kitCatalogs = MongoDBDAO.find(InstanceConstants.REAGENT_CATALOG_COLL_NAME, KitCatalog.class, DBQuery.is("category", "Kit")).toList();
		for(KitCatalog kc:  kitCatalogs){
			sb.append("\"").append("kitCatalogs").append(".").append(kc.code)
			.append("\":\"").append(kc.name).append("\",");
		}
		List<BoxCatalog> boxCatalogs = MongoDBDAO.find(InstanceConstants.REAGENT_CATALOG_COLL_NAME, BoxCatalog.class, DBQuery.is("category", "Box")).toList();
		for(BoxCatalog bc:  boxCatalogs){
			sb.append("\"").append("boxCatalogs").append(".").append(bc.code)
			.append("\":\"").append(bc.name).append("\",");
		}
		List<ReagentCatalog> reagentCatalogs = MongoDBDAO.find(InstanceConstants.REAGENT_CATALOG_COLL_NAME, ReagentCatalog.class, DBQuery.is("category", "Reagent")).toList();
		for(ReagentCatalog rc:  reagentCatalogs){
			sb.append("\"").append("reagentCatalogs").append(".").append(rc.code)
			.append("\":\"").append(rc.name).append("\",");
		}
		
		sb.append("};return function(k){if(typeof k == 'object'){for(var i=0;i<k.length&&!ms[k[i]];i++);var m=ms[k[i]]||k[0]}else{m=ms[k]||k}for(i=1;i<arguments.length;i++){m=m.replace('{'+(i-1)+'}',arguments[i])}return m}})();");
		return sb.toString();
	}
*/
	
	public Result jsCodes() {
		return result(() -> new Codes()
				.add(getCodeLabelAPI().all(),                     x -> x.tableName,       x -> x.code, x -> x.label)
				.addValuationCodes()
				.add(getReagentCatalogAPI().getKitCatalogs(),     x -> "kitCatalogs",     x -> x.code, x -> x.name)
				.add(getReagentCatalogAPI().getBoxCatalogs(),     x -> "boxCatalogs",     x -> x.code, x -> x.name)
				.add(getReagentCatalogAPI().getReagentCatalogs(), x -> "reagentCatalogs", x -> x.code, x -> x.name)
				.asCodeFunction(),
				"error when building codes");
	}
	
//	public Result jsCodes() {
//		return new Codes()
//				.add(Spring.getBeanOfType(CodeLabelDAO.class).findAll(),
//						     x -> x.tableName,       x -> x.code, x -> x.label)
//				.addValuationCodes()
//				.add(MongoDBDAO.find(InstanceConstants.REAGENT_CATALOG_COLL_NAME, KitCatalog.class, DBQuery.is("category", "Kit")).toList(),
//						     x -> "kitCatalogs",     x -> x.code, x -> x.name)
//				.add(MongoDBDAO.find(InstanceConstants.REAGENT_CATALOG_COLL_NAME, BoxCatalog.class, DBQuery.is("category", "Box")).toList(),
//						     x -> "boxCatalogs",     x -> x.code, x -> x.name)
//				.add(MongoDBDAO.find(InstanceConstants.REAGENT_CATALOG_COLL_NAME, ReagentCatalog.class, DBQuery.is("category", "Reagent")).toList(),
//						     x -> "reagentCatalogs", x -> x.code, x -> x.name)
//				.asCodeFunction();
//	}
	
//	public Result javascriptRoutes() {
//		return jsRoutes();
//	}
	
	/*
	public static Result javascriptRoutes() {
		response().setContentType("text/javascript");
		return ok(	  	      
				// Routes.javascriptRouter("jsRoutes"
				JavaScriptReverseRouter.create("jsRoutes"
						// Routes	  	         	        
						)
				);
	}
*/

	/*public static Result jsMessages() {
		//return ok(messages.generate("Messages")).as("application/javascript");
		//return ok(messages.all(Scala.Option("Messages"))).as("application/javascript");
		return ok(messages.apply(Scala.Option("Messages"), jsmessages.japi.Helper.messagesFromCurrentHttpContext()));

	}*/

}
