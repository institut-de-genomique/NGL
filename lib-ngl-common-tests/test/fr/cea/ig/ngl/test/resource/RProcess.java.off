package fr.cea.ig.ngl.test.resource;

import static fr.cea.ig.ngl.test.resource.RConstant.USER;

import java.util.List;

import fr.cea.ig.ngl.test.dao.api.factory.TestProcessFactory;
import fr.cea.ig.test.Actions;
import fr.cea.ig.test.TestContext;
import fr.cea.ig.util.function.CC3;
import fr.cea.ig.util.function.T;
import models.laboratory.container.instance.Container;
import models.laboratory.processes.instance.Process;

/**
 * Process actions.
 * 
 * @author vrd
 *
 */
public class RProcess {

	public static final <C extends TestContext> CC3<Container, Process, List<Process>> createProcessQC(C ctx) {
		return RContainer.createIWPTube(ctx)
			   .nest((container)                        -> T.cc1(TestProcessFactory.processQC(container.code, container.support.code)))    
			   .nest((container, refProcess)            -> Actions.cleaning(() -> ctx.apis().process().createProcessesFromContainer(refProcess, USER)))
			   .cc3 ((container, refProcess, processes) -> T.t3(ctx.apis().container().get(container.code), refProcess, processes));
	}
	
}
