package utils;


import static play.test.Helpers.fakeApplication;
import java.util.HashMap;
import java.util.Map;

import models.utils.dao.DAOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import play.test.FakeApplication;
import play.test.Helpers;
import fr.cea.ig.DBObject;
import fr.cea.ig.MongoDBDAO;

public abstract class AbstractTestsSRA {
	
	protected static FakeApplication app;
	protected final String userTest="user_test";
	@BeforeClass
	public  static void startTest() throws InstantiationException, IllegalAccessException, ClassNotFoundException, DAOException{
		//System.setProperty("config.file", TestHelper.getConfigFilePath("ngl-sub-testProd-sra.conf"));
		System.setProperty("config.file", TestHelper.getConfigFilePath("ngl-sub-testDev-sra.conf"));
		System.setProperty("logger.file", TestHelper.getConfigFilePath("logger.xml"));
		app = getFakeApplication();
		Helpers.start(app);
	}

	@AfterClass
	public  static void endTest() throws DAOException, InstantiationException, IllegalAccessException, ClassNotFoundException{
		app = getFakeApplication();
		Helpers.stop(app);
	}

	
	public static FakeApplication getFakeApplication(){
		return fakeApplication();
	}
	
	public static <T extends DBObject> T saveDBOject(Class<T> type, String collectionName,String code)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException {

		String collection=type.getSimpleName();
		T object = (T) Class.forName (type.getName()).newInstance();
		object.code=code;
		object=MongoDBDAO.save(collectionName, object);
		return object;
	}


}
