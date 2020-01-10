package utils;

import static play.test.Helpers.fakeApplication;

import java.util.HashMap;
import java.util.Map;

import models.utils.dao.DAOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import play.test.FakeApplication;
import play.test.Helpers;

public class AbstractTests {


	protected static FakeApplication app;

	@BeforeClass
	public  static void startTest() throws InstantiationException, IllegalAccessException, ClassNotFoundException, DAOException{
		System.setProperty("config.file", utils.TestHelper.getConfigFilePath("ngl-data-test.conf"));
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



}
