package fr.cea.ig.ngl.test.dao.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import fr.cea.ig.ngl.dao.processes.ProcessesAPI;
import fr.cea.ig.ngl.support.ListFormWrapper;
import fr.cea.ig.ngl.test.TUResources;
import fr.cea.ig.ngl.test.dao.api.factory.QueryMode;
import fr.cea.ig.ngl.test.dao.api.factory.TestProcessFactory;
import fr.cea.ig.test.APIRef;
import fr.cea.ig.util.function.CC3;
import fr.cea.ig.util.function.T;
import models.laboratory.common.instance.State;
import models.laboratory.container.instance.Container;
import models.laboratory.processes.instance.Process;
import ngl.refactoring.state.ProcessStateNames;

public class ProcessesAPITest {
	private static final play.Logger.ALogger logger = play.Logger.of(ProcessesAPITest.class);

	//Tested API
	private static APIRef<ProcessesAPI> api = APIRef.process;

	private static final CC3<Container, Process, List<Process>> data = TUResources.createProcessQCRWC
			.cc3((context, container, refProcess, processes) -> T.t3(container, refProcess, processes));


	@Test
	public void createTest() throws Exception {
		data.accept((container, refProcess, processes) -> {
			logger.debug("Creation test");
			for(Process p : processes) {
				assertEquals(refProcess.categoryCode, p.categoryCode);
				assertEquals(refProcess.typeCode, 	  p.typeCode);
				assertEquals(container.code,          p.inputContainerCode);
			}
			logger.debug("nb processes created: " + processes.size());
		});
	}

	@Test
	public void deleteTest() throws Exception {
		data.accept((container, refProcess, processes) -> {
			logger.debug("Delete test");
			for(Process p : processes) {
				api.get().delete(p.code);
				assertNull(api.get().get(p.code));
			}
		});
	}
	
	@Test
	public void listTest() throws Exception {
		logger.debug("List test");
		data.accept((container, refProcess, processes) -> {
			//---------- default mode ----------
			logger.debug("default mode");
			ListFormWrapper<Process> wrapper = TestProcessFactory.wrapper(refProcess.projectCodes, refProcess.categoryCode);
			Iterable<Process> procs          = api.get().listObjects(wrapper);
			Iterator<Process> iter           = procs.iterator();
			
			while(iter.hasNext()) {
				Process p = iter.next();
				assertEquals(refProcess.typeCode,     p.typeCode);
				assertEquals(refProcess.categoryCode, p.categoryCode);
				assertEquals(container.code,          p.inputContainerCode);
			}
			
			//---------- reporting mode----------
			logger.debug("reporting mode");
			wrapper = TestProcessFactory.wrapper(refProcess.projectCodes, QueryMode.REPORTING, null, refProcess.categoryCode);
			procs = api.get().listObjects(wrapper);
			iter = procs.iterator();
			while(iter.hasNext()) {
				Process p = iter.next();
				assertEquals(refProcess.typeCode,     p.typeCode);
				assertEquals(refProcess.categoryCode, p.categoryCode);
				assertEquals(container.code,          p.inputContainerCode);
			}
		});
	}
	
	@Test
	public void updateStateTest() throws Exception {
		logger.debug("update state test");
		data.accept((container, refProcess, processes) -> {
			State state       = new State(ProcessStateNames.IP, TUResources.USER);
			final String code = processes.iterator().next().code;
			Process proc = api.get().updateState(code, state, TUResources.USER);

			assertEquals(state.code, proc.state.code);
		});
	}
}
