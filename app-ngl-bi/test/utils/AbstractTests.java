package utils;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import play.Application;

import models.utils.DescriptionHelper;

import fr.cea.ig.DBObject;
import fr.cea.ig.MongoDBDAO;

/**
 * Application provider for test classes.
 * 
 * @author vrd
 *
 */
public abstract class AbstractTests {

	/**
	 * Application created for the class defined tests.
	 */
	protected static Application app;
	
	/**
	 * Initialize test application.
	 */
	@BeforeClass
	public static void startTestApplication() {
		app = getFakeApplication();
		// TODO: remove static reset
		DescriptionHelper.initInstitute();
	}

	/**
	 * Shutdown test application.
	 */
	@AfterClass
	public static void shutdownTestApplication() {
		app.asScala().stop();
		// TODO: remove static reset
		DescriptionHelper.initInstitute();
	}

	/**
	 * Create a test application.
	 * @return
	 */
	private static Application getFakeApplication(){
		// return fr.cea.ig.play.test.DevAppTesting.devapp("ngl-bi-test.conf","logger.xml");
		return ngl.bi.Global.devapp();
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
