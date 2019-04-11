package fr.cea.ig.ngl.test.dao.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collection;

import org.junit.Test;
import org.mongojack.DBQuery;

import fr.cea.ig.ngl.dao.experiments.ExperimentReagentsAPI;
import fr.cea.ig.ngl.test.TUResources;
import fr.cea.ig.play.test.TestAssertions;
import fr.cea.ig.test.APIRef;
import fr.cea.ig.util.function.CC2;
import fr.cea.ig.util.function.T;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.reagent.instance.ReagentUsed;

public class ExperimentReagentsAPITest {
private static final play.Logger.ALogger logger = play.Logger.of(ExperimentReagentsAPITest.class);
	
	//Tested API
	private static APIRef<ExperimentReagentsAPI> api = APIRef.experimentReagent;
	
	public static final CC2<Experiment, Experiment> createExpDepotWithPlate = TUResources.createExpDepotWithPlateRWC
			.cc2((context, refExperiment, experiment) -> T.t2(refExperiment, experiment));
	
			
	@Test
	public void getSubObjectsTest() throws Exception {
		createExpDepotWithPlate.accept((refExperiment, data) -> {
			logger.debug("getSubObjects test");
			Collection<ReagentUsed> rus = api.get().getSubObjects(data);
			assertEquals(refExperiment.reagents.size(), rus.size());
		});
	}
	
	@Test
	public void getSubObjectTest() throws Exception {
		createExpDepotWithPlate.accept((refExperiment, data) -> {
			logger.debug("getSubObject test");
			ReagentUsed ru = api.get().getSubObject(data, refExperiment.reagents.get(0).code);
			assertNotNull(ru);
			assertEquals(refExperiment.reagents.get(0).kitCatalogCode, ru.kitCatalogCode);
		});
	}
	
	@Test
	public void listObjectsTest() throws Exception { 
		logger.debug("listObjects test");
		createExpDepotWithPlate.accept((refExperiment, data) -> {
			//AJ improve test by using an aggregation query
			
			Iterable<Experiment> exps = api.get().listObjects(data.code,
			                                                  DBQuery.and(DBQuery.is("code", data.code), 
			                                                                         DBQuery.is("reagents.code", 
			                                                                                    refExperiment.reagents.get(0).code)));

			TestAssertions.assertOne(exps);
		});		
	}
}
