package controllers.analyses.tpl;

import play.mvc.Result;

import views.html.analyses.details;
import views.html.analyses.home;
import views.html.analyses.search;
import views.html.analyses.treatments;

import javax.inject.Inject;

import controllers.NGLController;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.lfw.Historized;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.support.NGLJavascript;


/**
 * Controller around Run object
 * 
 * @author galbini
 *
 */
// public class Analyses extends CommonController {
public class Analyses extends NGLController implements NGLJavascript { // NGLBaseController {
	
	private final home home;
	private final search search;
	private final details details;
	private final treatments treatments;
	
	@Inject
	public Analyses(NGLApplication app, home home, search search, details details, treatments treatments) {
		super(app);
		this.home = home; 
		this.search = search;
		this.details = details;
		this.treatments= treatments;
	}
	
	@Authenticated
	@Historized
	@Authorized.Read
	public Result home(String homecode) {
		return ok(home.render(homecode));
	}
	
	@Authenticated
	@Historized
	@Authorized.Read
	public Result get(String code) {
		return ok(home.render("search")); 
	}

	public Result valuation(String code) {
		return ok(home.render("valuation")); 
	}
	
	public Result search(String type) {
		return ok(search.render());
	}
	
	public Result details() {
		return ok(details.render());
	}
	
	public Result treatments(String code) {
		return ok(treatments.render(code));
	}
	
	public Result javascriptRoutes() {
  	    return jsRoutes(controllers.analyses.tpl.routes.javascript.Analyses.home(),  
  	    				controllers.analyses.tpl.routes.javascript.Analyses.get(), 
  	    				controllers.analyses.tpl.routes.javascript.Analyses.valuation(),
  	    				controllers.analyses.tpl.routes.javascript.Analyses.treatments(),
  	    				controllers.analyses.api.routes.javascript.Analyses.get(),
  	    				controllers.analyses.api.routes.javascript.Analyses.list(),
  	    				controllers.analyses.api.routes.javascript.Analyses.list(),
  	    				controllers.analyses.api.routes.javascript.Analyses.updateState(),
  	    				controllers.analyses.api.routes.javascript.Analyses.updateStateBatch(),
  	    				controllers.analyses.api.routes.javascript.Analyses.valuation(),
  	    				controllers.analyses.api.routes.javascript.Analyses.valuationBatch(),
  	    				controllers.analyses.api.routes.javascript.Analyses.properties(),
  	    				controllers.analyses.api.routes.javascript.Analyses.propertiesBatch(),
  	    				controllers.samples.api.routes.javascript.Samples.list(),
  	    				controllers.commons.api.routes.javascript.States.list(),
  	    				controllers.commons.api.routes.javascript.StatesHierarchy.list(),
  	    				controllers.commons.api.routes.javascript.CommonInfoTypes.list(),
  	    				controllers.treatmenttypes.api.routes.javascript.TreatmentTypes.get(),
  	    				controllers.treatmenttypes.api.routes.javascript.TreatmentTypes.list(),
  	    				controllers.valuation.api.routes.javascript.ValuationCriterias.list(),
  	    				controllers.valuation.api.routes.javascript.ValuationCriterias.get(),
  	    				controllers.projects.api.routes.javascript.Projects.list(),	    		
  	    				controllers.reporting.api.routes.javascript.ReportingConfigurations.list(),
  	    				controllers.reporting.api.routes.javascript.ReportingConfigurations.get(),
  	    				controllers.reporting.api.routes.javascript.FilteringConfigurations.list(),
  	    				controllers.commons.api.routes.javascript.Users.list(),
  	    				controllers.resolutions.api.routes.javascript.Resolutions.list(),
  	    				controllers.readsets.tpl.routes.javascript.ReadSets.get());	  	      
	}
	
	
}
