package fr.cea.ig.ngl.test.dao.api;

import static fr.cea.ig.play.test.TestAssertions.assertOne;
import static fr.cea.ig.test.Actions.using;
import static fr.cea.ig.util.function.T.t2;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.function.Consumer;

import javax.inject.Singleton;

import org.junit.Test;

import fr.cea.ig.ngl.dao.samples.SamplesAPI;
import fr.cea.ig.ngl.support.ListFormWrapper;
import fr.cea.ig.ngl.test.dao.api.factory.QueryMode;
import fr.cea.ig.ngl.test.dao.api.factory.TestSampleFactory;
import fr.cea.ig.ngl.test.resource.RApplication;
import fr.cea.ig.ngl.test.resource.RConstant;
import fr.cea.ig.ngl.test.resource.RProject;
import fr.cea.ig.ngl.test.resource.RSample;
import fr.cea.ig.test.APIRef;
import fr.cea.ig.test.TestContext;
import fr.cea.ig.util.function.CC1;
import fr.cea.ig.util.function.CC2;
import fr.cea.ig.util.function.CC3;
import fr.cea.ig.util.function.F1;
import fr.cea.ig.util.function.T;
import models.laboratory.project.instance.Project;
import models.laboratory.sample.instance.Sample;

/**
 * Test {@link SamplesAPI} methods.
 * 
 * @author ajosso
 *
 */
@Singleton  
public class SamplesAPITest {

	private static final play.Logger.ALogger logger = play.Logger.of(SamplesAPITest.class);

	private static final APIRef<SamplesAPI>  api        = APIRef.sample;
	
//	private static final CC2<Sample,Sample> testData = TUResources.contextResource
//			.nest2((context) 					-> TUResources.createFullSample.cc2((project,refSample,sample) -> t2(refSample,sample)))
//			.cc2(  (context, refSample, sample) -> t2(refSample,sample));
	
//	private static final CC2<Sample,Sample> testData = 
//			RApplication.app
//			.and3(RSample.createSampleRaw)
//			.cc2 ((context,project,refSample,sample) -> t2(refSample,sample));

	/**
	 * Test data constructor.
	 * @param cc context constructor
	 * @return   test data
	 */
	private static final <A> CC2<Sample,Sample> buildTestData(CC1<A> cc) {
		return cc.and3(RSample.createSampleRaw)
				 .cc2 ((context,project,refSample,sample) -> t2(refSample,sample));
	}
	
	/**
	 * Test data.
	 */
	private static CC2<Sample,Sample> testData = buildTestData(RApplication.app);
	
	/**
	 * Test debug data.
	 */
	@SuppressWarnings("unused")
	private static CC2<Sample,Sample> testDataLog = buildTestData(RApplication.appLog);
	
	@Test
	public void createTest() throws Exception {
		testData.accept((refSample,data) -> {
			logger.debug("creation test");
			assertEquals(refSample.categoryCode,   data.categoryCode);
			assertEquals(refSample.code,           data.code);
			assertEquals(refSample.importTypeCode, data.importTypeCode);
			assertEquals(refSample.name,           data. name);
			assertEquals(refSample.typeCode,       data.typeCode);
		});
	}

	@Test
	public void deleteTest() throws Exception {
		testData.accept((refSample,__) -> {
			logger.debug("Delete test");
			api.get().delete(refSample.code);
			assertNull(api.get().get(refSample.getCode()));
		});
	}

	@Test
	public void getTest() throws Exception {
		testData.accept((refSample,data) -> {
			logger.debug("get test");
			Sample sample = api.get().get(refSample.code);
			assertNotNull(sample);
			assertEquals(data.get_id(),       sample.get_id());
			assertEquals(refSample.getCode(), sample.getCode());
		});
	}

	@Test
	public void listTest() throws Exception {
		logger.debug("List test");
		
		// Testing rendering ?
//		ListFormWrapper<Sample> listing = TestSampleFactory.wrapper(refProject.code, true, false, false);
//		ListFormWrapper<Sample> datatabling = TestSampleFactory.wrapper(refProject.code, false, true, false);
//		ListFormWrapper<Sample> counting = TestSampleFactory.wrapper(refProject.code, false, false, true);
//		ListFormWrapper<Sample> othering = TestSampleFactory.wrapper(refProject.code, false, false, false);
		
		testData.accept((refSample,__) -> { 
			//---------- default mode ----------
			logger.debug("default mode");
			final String projectCode = refSample.projectCodes.iterator().next();
			ListFormWrapper<Sample> wrapper = TestSampleFactory.wrapper(projectCode);

			Consumer<Sample> sampleAssertions =
					s -> {
						  assertEquals(refSample.code,         s.code);
						  assertEquals(refSample.categoryCode, s.categoryCode);
					};
			
			assertOne(api.get().listObjects(wrapper), sampleAssertions);
			
			//---------- reporting mode----------
			logger.debug("reporting mode");
			wrapper = TestSampleFactory.wrapper(projectCode, QueryMode.REPORTING, null);

			assertOne(api.get().listObjects(wrapper), sampleAssertions);
		});
	}
	
	@Test
	public void isObjectExistsTest() throws Exception {
		testData.accept((refSample,__) -> {
			assertTrue(api.get().isObjectExist(refSample.code));
		});
	}
	
	@Test
	public void isObjectNotExistsTest() throws Exception {
		testData.accept((refSample,__) -> {
			api.get().delete(refSample.code);
			assertFalse(api.get().isObjectExist(refSample.code));
		});
	}

	@Test
	public void updateTest() throws Exception {
		logger.debug("update test");
		testData.accept((refSample,data) -> {
			Sample sampleToUpdate                      = data;
			String newName                             = "New name after update";
			sampleToUpdate.name                        = newName;
			sampleToUpdate.traceInformation.modifyUser = RConstant.USER;
			sampleToUpdate.traceInformation.modifyDate = new Date();
			Sample sample = api.get().update(sampleToUpdate, RConstant.USER);

			assertNotNull("sample",sample);
			assertEquals   (refSample.categoryCode, sample.categoryCode);
			assertNotEquals(refSample.name,         sample.name);
			assertEquals   (newName,                sample.name);
		});
	}

	// Test that two samples created from the same project are distinct
	// but have the same project code referenced.
	@Test
	public void test2Samples() throws Exception {
		F1<Project,CC1<Sample>> newSample = p -> using(RConstant.USER, () -> TestSampleFactory.sample(RConstant.USER, p.code)); 
		CC3<TestContext,Sample,Sample> s2 = RApplication.contextResource
				.nest2((context) ->	RProject.createProject.nest2(p -> newSample.apply(p).and(newSample.apply(p)))
				                                             .cc2((p, s0, s1) -> T.t2(s0, s1)));
		s2.accept((__,s0,s1) -> {
			assertNotEquals(s0.getCode(),    s1.getCode());
			assertEquals   (s0.projectCodes, s1.projectCodes);
		});
	}
	
}
