package utils;

public class TestHelper {

	public static String getConfigFilePath(String filename){
		String dir = System.getenv("NGL_CONF_TEST_DIR");
		if(null == dir)dir  = System.getProperty("NGL_CONF_TEST_DIR");
		if(null == dir)throw new RuntimeException("missing NGL_CONF_TEST_DIR variable");
		return dir+"/"+filename;	
	}
}
