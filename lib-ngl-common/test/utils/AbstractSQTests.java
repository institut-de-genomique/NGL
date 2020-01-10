package utils;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import fr.cea.ig.DBObject;
import fr.cea.ig.MongoDBDAO;
import models.utils.DescriptionHelper;
import ngl.common.Global;
import play.Application;
import rules.services.Rules6Component;

//class XXXRules6 extends Rules6Component {
//	private static final play.Logger.ALogger logger = play.Logger.of(XXXRules6.class);
//	@Inject
//	public XXXRules6(Application app, ApplicationLifecycle lifecycle) {
//		super(app, lifecycle);
//		logger.error("we rule");
//	}
//	@Override
//	public void onStart(Application app, ApplicationLifecycle lifecycle) {
//		logger.error("on start noop");
//	}
//}

/**
 * Application provider for test classes that requires a SQ
 * application.
 * 
 * @author vrd
 *
 */
// Static definition that quite a duplicate of AbstractTests
public abstract class AbstractSQTests {

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
		app.injector().instanceOf(Rules6Component.class);
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
	private static Application getFakeApplication() {
		// return fr.cea.ig.play.test.DevAppTesting.devapp("ngl-common.test.conf");
//		System.out.println(" ************* USING SQ FACTORY **************");
		return Global.afSq.createApplication();
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
