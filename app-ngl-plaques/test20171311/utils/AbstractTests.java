package utils;


import static play.test.Helpers.fakeApplication;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;

import play.test.FakeApplication;
import play.test.Helpers;

public abstract class AbstractTests {

	protected static FakeApplication app;
	
	
	@Before
	public void start(){
		 app = getFakeApplication();
		 Helpers.start(app);
	}
	
	@After
	public void stop(){
		Helpers.stop(app);
	}
	
	public static FakeApplication getFakeApplication(){
		return fakeApplication(fakeConfiguration());
	}
	
	public static Map<String,String> fakeConfiguration(){
		Map<String,String> config = new HashMap<String,String>();
		config.put("db.lims.driver", "net.sourceforge.jtds.jdbc.Driver");
		config.put("db.lims.url", "jdbc:jtds:sybase://sybasedev.genoscope.cns.fr:3015/dblims");
		config.put("db.lims.user", "mhaquell");
		config.put("db.lims.password", "cmoexhdr");
		config.put("db.lims.partitionCount", "1");
		config.put("db.lims.maxConnectionsPerPartition", "6");
		config.put("db.lims.minConnectionsPerPartition", "1");
		config.put("db.lims.logStatements", "true");
		config.put("db.lims.jndiName", "lims");			
		return config;
		
	}
}
