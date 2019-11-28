package ngl.data;

import com.google.inject.Inject;

import fr.cea.ig.ngl.tmp.INGLDataDB;
import fr.cea.ig.ngl.tmp.NGLDatabase;
import fr.cea.ig.play.IGGlobals;
import models.utils.ModelDAOs;
import ngl.data.api.PopulationAPI;
import nglapps.DataService;
import play.api.modules.spring.Spring;
import play.modules.mongojack.MongoDBPlugin;


public class PopulateDB {
	
	@Inject
	public PopulateDB(IGGlobals gs, MongoDBPlugin mdb, ModelDAOs mdao, DataService dataService, NGLDatabase db, INGLDataDB nd, Spring spring, PopulationAPI papi) {
		if (nd.isPopulatedWith(dataService.getClass()))
			return;
//		All all = new All(papi);
//		all.save();
		papi.fullPopulation();
		nd.setPopulatedWith(dataService.getClass());
	}
	
}
