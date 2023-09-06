package controllers.readsets.tpl;


import play.mvc.Result;
import views.html.readsets.details;
import views.html.readsets.home;
import views.html.readsets.printView;
import views.html.readsets.search;
import views.html.readsets.treatments;

import javax.inject.Inject;

import controllers.NGLController;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.lfw.Historized;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.support.NGLJavascript;


/**
 * Controller around Run object
 * @author galbini
 *
 */
public class ReadSets extends NGLController implements NGLJavascript {
	
	private final home       home;
	private final search     search;
	private final details    details;
	private final printView  printView;
	private final treatments treatments;
	
	@Inject
	public ReadSets(NGLApplication app, home home, search search, details details, printView printView, treatments treatments) {
		super(app);
		this.home       = home;
		this.search     = search;
		this.details    = details;
		this.printView  = printView;
		this.treatments = treatments;	
	}
	
	@Authenticated
	@Historized
	@Authorized.Read
	public Result home(String homecode) {
		return ok(home.render(homecode));
	}
	
	public Result get(String code) {
		return ok(home.render("search")); 
	}
	
	public Result valuation(String code) {
		return ok(home.render("valuation")); 
	}
	
	public Result other(String code, String other) {
		return ok(home.render("search")); 
	}
	
	public Result search() {
		return ok(search.render());		
	}
	
	public Result details() {
		return ok(details.render());
	}
	
	public Result detailsPrintView() {
		return ok(printView.render());
	}
	
	public Result treatments(String code, String media) {
		return ok(treatments.render(code, media));
	}

	public Result javascriptRoutes() {
  	   return jsRoutes(controllers.readsets.tpl.routes.javascript.ReadSets.home(),  
  	    			   controllers.readsets.tpl.routes.javascript.ReadSets.get(),   	    		
  	    			   controllers.readsets.tpl.routes.javascript.ReadSets.detailsPrintView(),
  	    			   controllers.readsets.tpl.routes.javascript.ReadSets.other(), 
  	    			   controllers.readsets.tpl.routes.javascript.ReadSets.valuation(),
  	    			   controllers.readsets.tpl.routes.javascript.ReadSets.treatments(),
  	    			   controllers.readsets.api.routes.javascript.ReadSets.get(),
  	    			   controllers.readsets.api.routes.javascript.ReadSets.list(),
  	    			   controllers.readsets.api.routes.javascript.ReadSets.list(),
  	    			   controllers.readsets.api.routes.javascript.ReadSets.updateState(),
  	    			   controllers.readsets.api.routes.javascript.ReadSets.updateStateBatch(),
  	    			   controllers.readsets.api.routes.javascript.ReadSets.valuation(),
  	    			   controllers.readsets.api.routes.javascript.ReadSets.valuationBatch(),
  	    			   controllers.readsets.api.routes.javascript.ReadSets.properties(),
  	    			   controllers.readsets.api.routes.javascript.ReadSets.propertiesBatch(),
  	    			   controllers.runs.api.routes.javascript.Lanes.get(),
  	    			   controllers.runs.api.routes.javascript.Runs.get(),
  	    			   controllers.runs.api.routes.javascript.Runs.list(),
  	    			   controllers.runs.api.routes.javascript.RunTreatments.get(),
  	    			   controllers.runs.tpl.routes.javascript.Runs.get(),
  	    			   controllers.samples.api.routes.javascript.Samples.list(),
  	    			   controllers.samples.api.routes.javascript.Samples.get(),
  	    			   controllers.commons.api.routes.javascript.States.list(),
  	    			   controllers.commons.api.routes.javascript.StatesHierarchy.list(),
  	    			   controllers.commons.api.routes.javascript.CommonInfoTypes.list(),
  	    			   controllers.treatmenttypes.api.routes.javascript.TreatmentTypes.get(),
  	    			   controllers.treatmenttypes.api.routes.javascript.TreatmentTypes.list(),
  	    			   controllers.instruments.api.routes.javascript.Instruments.list(),
  	    			   controllers.valuation.api.routes.javascript.ValuationCriterias.list(),
  	    			   controllers.valuation.api.routes.javascript.ValuationCriterias.get(),
  	    			   controllers.projects.api.routes.javascript.Projects.list(),	    		
  	    			   controllers.reporting.api.routes.javascript.ReportingConfigurations.list(),
  	    			   controllers.reporting.api.routes.javascript.ReportingConfigurations.get(),
  	    			   controllers.reporting.api.routes.javascript.FilteringConfigurations.list(),
  	    			   controllers.commons.api.routes.javascript.Users.list(),
  	    			   controllers.resolutions.api.routes.javascript.Resolutions.list(),
  	    			   controllers.projects.api.routes.javascript.Projects.get(),
  	    			   controllers.commons.api.routes.javascript.Values.list(),
  	    			   controllers.commons.api.routes.javascript.PropertyDefinitions.list(),
  	    			   controllers.commons.api.routes.javascript.Parameters.list());
  	  }


}
