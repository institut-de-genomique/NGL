package fr.cea.ig.ngl.test.dao.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collection;

import org.junit.Test;
import org.mongojack.DBQuery;

import fr.cea.ig.ngl.dao.experiments.ExperimentCommentsAPI;
import fr.cea.ig.ngl.dao.experiments.ExperimentsAPI;
import fr.cea.ig.ngl.test.resource.RApplication;
import fr.cea.ig.ngl.test.resource.RConstant;
import fr.cea.ig.ngl.test.resource.RExperiment;
import fr.cea.ig.play.test.TestAssertions;
import fr.cea.ig.test.APIRef;
import fr.cea.ig.util.function.CC2;
import fr.cea.ig.util.function.T;
import models.laboratory.common.instance.Comment;
import models.laboratory.experiment.instance.Experiment;

public class ExperimentCommentsAPITest {
	
	private static final String COMMENT             = "very usefull comments";
	private static final play.Logger.ALogger logger = play.Logger.of(ExperimentCommentsAPITest.class);

	//Tested API
	private static APIRef<ExperimentCommentsAPI> api 	= APIRef.experimentComment;
	private static APIRef<ExperimentsAPI> 		 expApi = APIRef.experiment;

	public static final CC2<Experiment, Experiment> createExpQCWithTube = 
			RApplication.contextResource
			.nest2(RExperiment::createExpQCWithTube)
			.cc2((context, refExperiment, experiment) -> T.t2(refExperiment, experiment));

	@Test
	public void saveTest() throws Exception {
		createExpQCWithTube.accept((refExperiment, data) -> {
			logger.debug("save test");
			api.get().save(data, new Comment(COMMENT, RConstant.USER, false), RConstant.USER);
			Experiment exp = expApi.get().get(data.code);
			assertEquals(1, exp.comments.size());
		});
	}

	@Test
	public void getSubObjectsTest() throws Exception {
		createExpQCWithTube.accept((refExperiment, data) -> {
			logger.debug("getSubObjects test");
			api.get().save(data, new Comment(COMMENT, RConstant.USER, false), RConstant.USER);
			Experiment exp = expApi.get().get(data.code);

			Collection<Comment> coms = api.get().getSubObjects(exp);
			assertNotNull(coms);
			assertEquals(1, coms.size());
		});
	}

	@Test
	public void getSubObjectTest() throws Exception {
		createExpQCWithTube.accept((refExperiment, data) -> {
			logger.debug("getSubObject test");
			Comment c = api.get().save(data, new Comment(COMMENT, RConstant.USER, false), RConstant.USER);
			Experiment exp = expApi.get().get(data.code);
			Comment com = api.get().getSubObject(exp, c.code);
			assertNotNull(com);
			assertEquals(COMMENT, com.comment);
		});
	}

	@Test
	public void listObjectsTest() throws Exception {
		logger.debug("listObjects test");
		createExpQCWithTube.accept((refExperiment, data) -> {
			Comment c = api.get().save(data, new Comment(COMMENT, RConstant.USER, false), RConstant.USER);
			Iterable<Experiment> exps = api.get().listObjects(data.code, DBQuery.and(DBQuery.is("code", data.code), 
																					 DBQuery.is("comments.code", c.code)));

			TestAssertions.assertOne(exps);
		});
	}
	
}
