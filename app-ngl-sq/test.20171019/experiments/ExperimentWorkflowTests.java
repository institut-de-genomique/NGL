package experiments;

import org.junit.After;
import org.junit.Before;

import utils.AbstractTests;
import utils.InitDataHelper;

public class ExperimentWorkflowTests extends AbstractTests {
	
	@Before
	public  void initData() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		InitDataHelper.initForProcessesTest();
	}

	@After
	public  void resetData(){
		InitDataHelper.endTest();
	}

	
	
}
