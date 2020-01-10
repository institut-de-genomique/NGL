package fr.cea.ig.ngl.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import fr.cea.ig.ngl.test.dao.api.ContainerSupportsAPITest;
import fr.cea.ig.ngl.test.dao.api.ContainersAPITest;
import fr.cea.ig.ngl.test.dao.api.ProjectsAPITest;
import fr.cea.ig.ngl.test.dao.api.SamplesAPITest;

/**
 * Tests suite to list reviewed tests 
 * to replace temporary the sbt command 'ngl-common/test' by 'ngl-common/testOnly fr.cea.ig.ngl.test.NGLTests'
 * 
 * @author ajosso
 *
 */
@RunWith(Suite.class)

@Suite.SuiteClasses({
   ProjectsAPITest.class,
   SamplesAPITest.class,
   ContainersAPITest.class,
   ContainerSupportsAPITest.class
})

public class NGLTests {

}
