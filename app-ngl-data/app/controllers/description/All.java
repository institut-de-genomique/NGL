package controllers.description;

import com.google.inject.Inject;

import ngl.data.api.PopulationAPI;
import play.mvc.Controller;
import play.mvc.Result;

public class All extends Controller { 
	
	private final PopulationAPI papi;
	
	@Inject
	public All(PopulationAPI papi) {
		this.papi = papi;
	}
	
	public Result save() {
		return papi.fullPopulation();
	}
	
	public Result init() {
        return papi.cleanAndFullPopulation();
    }
	
}

