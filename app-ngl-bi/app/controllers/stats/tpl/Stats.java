package controllers.stats.tpl;

import javax.inject.Inject;

import controllers.NGLController;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.lfw.Historized;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.support.NGLJavascript;
import play.mvc.Result;
import views.html.stats.choice;
import views.html.stats.configLanes;
import views.html.stats.configReadSets;
import views.html.stats.home;
import views.html.stats.showLanes;
import views.html.stats.showReadSets;


/**
 * Controller around Run object
 * @author galbini
 *
 */
public class Stats extends NGLController
                  implements NGLJavascript {
	
	private final home           home;
	private final choice         choice;
	private final configReadSets configReadSets;
	private final configLanes    configLanes;
	private final showReadSets   showReadSets;
	private final showLanes      showLanes;
	
	@Inject
	public Stats(NGLApplication app , home home, configReadSets configReadSets, configLanes configLanes, showReadSets showReadSets, showLanes showLanes, choice choice) {
		super(app);
		this.home           = home;
		this.choice         = choice;
		this.configReadSets = configReadSets;
		this.configLanes    = configLanes;
		this.showReadSets   = showReadSets;
		this.showLanes      = showLanes;
	}
	
	@Authenticated
	@Historized
	@Authorized.Read
	public Result home(String homecode) {
		return ok(home.render(homecode));
	}
	
	public Result config(String type) {
		if(type.equals("readsets"))
			return ok(configReadSets.render());
		else if(type.equals("lanes"))
			return ok(configLanes.render());
		else
			return ok(configReadSets.render());
	}
	
	
	public Result show(String type) {
		if(type.equals("readsets"))
			return ok(showReadSets.render());
		else if(type.equals("lanes"))
			return ok(showLanes.render());
		else
			return ok(showReadSets.render());
	}
	
	public Result choice(String type) {
		return ok(choice.render());
	}
	
	public Result javascriptRoutes() {
  	    return jsRoutes(controllers.stats.tpl.routes.javascript.Stats.home(),  
  	    				controllers.stats.tpl.routes.javascript.Stats.config(),
  	    				controllers.stats.api.routes.javascript.StatsConfigurations.list(),
  	    				controllers.stats.api.routes.javascript.StatsConfigurations.get(),
  	    				controllers.readsets.tpl.routes.javascript.ReadSets.home(),  
  	    				controllers.readsets.tpl.routes.javascript.ReadSets.get(),   	    		
  	    				controllers.readsets.tpl.routes.javascript.ReadSets.detailsPrintView(),
  	    				controllers.readsets.tpl.routes.javascript.ReadSets.other(), 
  	    				controllers.readsets.tpl.routes.javascript.ReadSets.valuation(),
  	    				controllers.readsets.tpl.routes.javascript.ReadSets.treatments(),
  	    				controllers.readsets.api.routes.javascript.ReadSets.get(),
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
  	    				controllers.runs.api.routes.javascript.RunCategories.list(),
  	    				controllers.runs.api.routes.javascript.RunTreatments.get(),
  	    				controllers.runs.tpl.routes.javascript.Runs.get(),
  	    				controllers.runs.tpl.routes.javascript.Runs.search(),
  	    				controllers.samples.api.routes.javascript.Samples.list(),
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
  	    				controllers.commons.api.routes.javascript.Parameters.list());
  	  }


	
}
