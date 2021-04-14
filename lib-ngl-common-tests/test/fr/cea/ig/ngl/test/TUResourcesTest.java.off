package fr.cea.ig.ngl.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import fr.cea.ig.ngl.test.resource.RAnalysis;
import fr.cea.ig.ngl.test.resource.RApplication;
import fr.cea.ig.ngl.test.resource.RExperiment;
import fr.cea.ig.ngl.test.resource.RReadSet;
import fr.cea.ig.ngl.test.resource.RRun;
import fr.cea.ig.util.function.CC1;

/**
 * Test all configured entities life cycle from {@link fr.cea.ig.ngl.test.resource.RProject},
 * {@link fr.cea.ig.ngl.test.resource.RSample} and others to check if they works fine.
 * 
 * @author ajosso
 *
 */
public class TUResourcesTest  {
	
//	private static final play.Logger.ALogger logger = play.Logger.of(TUResourcesTest.class);

//	@Test
//	public void testRun() throws Exception {
//		Global.afSq.run(app -> {
//			
//		});
//	}
//	
//	/* ---------- Test Resources which only use fr.cea.ig.ngl.dao APIs ----------------- */
//	/* --------------------------------------------------------------------------------- */
//	@Test
//	public void project() throws Exception {
//		testResourceLifeCycle(ApplicationFactory.Actions.withApp(Global.afSq)
//				.and(TUResources.createProject).ct());
//	}
//	
//	@Test
//	public void sample() throws Exception {
//		testResourceLifeCycle(ApplicationFactory.Actions.withApp(Global.afSq)
//				.and(TUResources.createSample.cc1((project,sample) -> sample)).ct());
//	}
//	
//	@Test
//	public void containerAndSupport() throws Exception {
//		testResourceLifeCycle(ApplicationFactory.Actions.withApp(Global.afSq)
//				.and(TUResources.createContainerOnTubeResource.cc1((sup,cont) -> cont)).ct());
//	}
//	
//	/* ----------- Test Resources which use fr.cea.ig.test.TestContext ----------------- */
//	/* --------------------------------------------------------------------------------- */
//	
//	@Test
//	public void context() throws Exception {
//		testResourceLifeCycle(TUResources.appResource.ct());
//	}
//	
//	@Test
//	public void projectWC() throws Exception {
//		testResourceLifeCycle(TUResources.createProjectWithContext.ct());
//	}
//	@Test
//	public void samplesWC() throws Exception {
//		testResourceLifeCycle(TUResources.createSamplesWithContext.ct());
//	}
	
//	@Test
//	public void plateFullWC() throws Exception {
//		testResourceLifeCycle(RContainer.createPlateFullResourceWithContext.ct());
//	}


    @Test
    public void expPrepFCIlluminaRWC() throws Exception {
        testResourceLifeCycle(RApplication.contextResource.nest3(RExperiment::createExpPrepFCIllumina).ct());
    }
	
	@Test
    public void illuminaDepotRWC() throws Exception {
        testResourceLifeCycle(RApplication.contextResource.nest2(RExperiment::createExpDepotIllumina).ct());
    }
	
    @Test
    public void illuminaRunRWC() throws Exception {
        testResourceLifeCycle(RApplication.contextResource.nest3(RRun::createIlluminaRun).ct());
    }

    
    @Test
    public void illuminaRunAndReadSetRWC() throws Exception {
//        testResourceLifeCycle(RReadSet.createIlluminaRunAndReadSetRWC.ct());
        testResourceLifeCycle(RApplication.contextResource.nest5(RReadSet::createIlluminaRunAndReadSet).ct());
    }

    //@Test // EJACOBY (AJ) ressource non fonctionnelle pour le moment 
    //il faudra réactiver le test lorsque celle-ci le sera
    public void illuminaAnalysisRWC() throws Exception {
//        testResourceLifeCycle(RAnalysis.createIlluminaAnalysisRWC.ct());
    	testResourceLifeCycle(RApplication.contextResource.nest5(RAnalysis::createIlluminaAnalysisRWC).ct());
    }
    
//	/* _________________________________________________________________________________ */
//	@Test
//	public void experimentWC_UC() throws Exception {
//		testResourceLifeCycle(TestData.createExpQCWithTubeRWC_UC.ct());
//	}
//	
//	@Test
//	public void experimentWithPlateWC_UC() throws Exception {
//		testResourceLifeCycle(TestData.createExpQCWithPlateRWC_UC.ct());
//	}
//	
//	@Test
//	public void experimentWithPlateAndTubeWC_UC() throws Exception {
//		testResourceLifeCycle(TestData.createExpQCWithPlateAndTubeRWC_UC.ct());
//	}
//	
//	@Test
//	public void experimentDepotWithPlateWC_UC() throws Exception {
//		testResourceLifeCycle(TestData.createExpDepotWithPlateRWC_UC.ct());
//	}
//	
//	@Test
//	public void experimenTransfertWithPlateWC_UC() throws Exception {
//		testResourceLifeCycle(TestData.createExpTransfertWithPlateRWC_UC.ct());
//	}
//	/* _________________________________________________________________________________ */
	
	/* --------------------------------------------------------------------------------- */
	
	private <A> void testResourceLifeCycle(CC1<A> cc1) throws Exception {
		final List<A> done = new ArrayList<>(); 
		cc1.accept(a -> { done.add(a); });
		assertEquals("cc did not trigger", 1, done.size());
	}
    
//    /**
//     * Test that the provided CC properly executes a provided consumer
//     * so a CC combination can be verified. 
//     * @param cc1        CC to check
//     * @throws Exception error
//     */
//	private <A> void testResourceLifeCycle(CC1<A> cc1) throws Exception {
//		testResourceLifeCycle(cc1, throwException());
//	}
//	
//	private <A> void testResourceLifeCycle(CC1<A> cc1, C1<A> f) throws Exception {
//		try {
//			cc1.accept(f);
//		} catch (Exception e) {
//			if (checkException(e)) 
//				return;
//			logger.error(e.getMessage());
//			if (logger.isDebugEnabled())
//				e.printStackTrace();
//		}
//		throw new Exception("unreachable");
//	}
//
//	private <A> C1<A> throwException() {
//		return t -> { throw new TUResourcesException("it works"); };
//	}
//	
//	/** 
//	 * Check if exception is expected or not
//	 * @param e Throwable
//	 * @return true if the exception is expected
//	 */
//	private boolean checkException(Throwable e) {
//		if (e instanceof TUResourcesException)
//			return true;
//		if (e.getCause() != null)
//			return checkException(e.getCause());
//		return false;
//	}
//
//	private class TUResourcesException extends RuntimeException {
//		private static final long serialVersionUID = 1L;
//		public TUResourcesException(String message) { super(message); }
//	}
	
}
