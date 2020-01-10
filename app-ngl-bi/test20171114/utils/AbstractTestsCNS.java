package utils;



import static play.test.Helpers.fakeApplication;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import play.Logger;
import play.test.FakeApplication;
import play.test.Helpers;



public abstract class AbstractTestsCNS {
	
	protected static FakeApplication app;
	@BeforeClass
	public static void start(){
		System.setProperty("config.file", TestHelper.getConfigFilePath("ngl-bi-test-cns.conf"));
		app = getFakeApplication();
		Helpers.start(app);
	}
	
	
	@AfterClass
	public static void stopApp(){
		Helpers.stop(app);
	}
	
	public static FakeApplication getFakeApplication(){
		return fakeApplication();
	}
	
	
	protected Double roundValue(double value) throws ParseException
	{
		DecimalFormat df=new DecimalFormat("0.00");
		return (Double)df.parse(df.format(value)).doubleValue();
	}
}
