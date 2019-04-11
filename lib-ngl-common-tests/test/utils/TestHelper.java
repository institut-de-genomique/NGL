package utils;

import fr.cea.ig.DBObject;
import fr.cea.ig.MongoDBDAO;

public class TestHelper {

	public static String getConfigFilePath(String filename) {
		String dir = System.getenv("NGL_CONF_TEST_DIR");
		if (dir == null) 
			dir  = System.getProperty("NGL_CONF_TEST_DIR");
		if (dir == null) 
			throw new RuntimeException("missing NGL_CONF_TEST_DIR variable");
		return dir + "/" + filename;	
	}
	
	/**
	 * Create and save a new instance of a DBObject subclass with some provided code.
	 * @param type           class of the instance to create 
	 * @param collectionName name of the collection to save the instance to 
	 * @param code           DBObject code of the created instance
	 * @return               the created and saved instance
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public static <T extends DBObject> T saveDBOject(Class<T> type, String collectionName, String code)
			throws InstantiationException, IllegalAccessException {
		T object = type.newInstance();
		object.code = code;
		object = MongoDBDAO.save(collectionName, object);
		return object;
	}

}
