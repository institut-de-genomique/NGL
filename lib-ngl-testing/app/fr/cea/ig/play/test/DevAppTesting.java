package fr.cea.ig.play.test;

import static play.test.Helpers.running;
import static play.test.Helpers.testServer;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import javax.inject.Inject;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import fr.cea.ig.util.baseN.Base26ArrayEncoding;
import fr.cea.ig.util.baseN.LongEncoding;
import fr.cea.ig.util.function.C1;
import play.Application;
import play.Environment;
import play.inject.ApplicationLifecycle;
import play.inject.guice.GuiceApplicationBuilder;
import play.libs.ws.WSClient;
import play.test.TestServer;
import play.test.WSTestClient;

/**
 * Test support for NGL on DEV server.
 * The application life cycle is managed properly and only one application is active at
 * any given time when the {@link #devapp} method is used. 
 * 
 * @author vrd
 *
 */
public class DevAppTesting {
	
	/**
	 * Logger.
	 */
	private static final play.Logger.ALogger logger = play.Logger.of(DevAppTesting.class);
	
	/**
	 * Application singleton instance.
	 */
	private static Application application;

	/**
	 * Somewhat unique identifier per test set execution that can be used to create unique identifiers.
	 */
	private static String testTimeKey = null;
	
	/**
	 * Global counter.
	 */
	private static long codeId = 0;

	/**
	 * Builds an application applying modifications 
	 * @param appConfFile application configuration file name
	 * @param mods        modification to apply to the application builder
	 * @return            built application
	 */
	public static Application devapp(String appConfFile, List<Function<GuiceApplicationBuilder,GuiceApplicationBuilder>> mods) {
		if (application != null) {
			logger.warn("returning already running application (may not be configured as expected)");
			return application;
		}
		try {
			propsDump(true);
			// ---- application configuration file
			// compute application configuration file path and check existence
			String confFilePath = System.getProperty("ngl.test.conf.dir");
			if (confFilePath == null)
				throw new RuntimeException("ngl.test.conf.dir is not defined");
			String confFileName = new File(new File(confFilePath), appConfFile).getPath();
			logger.debug("using config file '" + confFileName + "'");
			if (! new File(confFileName).exists())
				throw new RuntimeException("configuration file not found " + confFileName);
			System.setProperty("config.file", confFileName);
			// ---- Check logger file existence 
			String loggerConfFileName = System.getProperty("logger.file");
			if (loggerConfFileName == null)
				throw new RuntimeException("no logger file defined");
			logger.info("logger configuration file {}", loggerConfFileName);
			// play.Mode.TEST does not work well with the NGL aplications
			Environment env = new Environment(play.Mode.DEV);
			// ---- User specific configuration 
			// Load user specific configuration base on user name (relative ../../users/${user}.conf).
			String user = System.getProperty("ngl.test.user.name", System.getProperty("user.name"));
			logger.info("running tests as '{}'", user);
			File userConfigFile = new File(new File(confFileName).getParentFile().getParentFile(),"users/" + user + ".conf");
			if (!userConfigFile.exists())
				throw new RuntimeException("'" + userConfigFile + "' does not exist");
			Config userConfig     = ConfigFactory.parseFile(userConfigFile).resolve();
			GuiceApplicationBuilder applicationBuilder = 
					new GuiceApplicationBuilder()
					.configure(userConfig)
					.in(env);
			for (Function<GuiceApplicationBuilder,GuiceApplicationBuilder> mod : mods)
				applicationBuilder = mod.apply(applicationBuilder);
			// Build application
			application = applicationBuilder.build();
			// register application cleanup
			application.injector().instanceOf(Cleaner.class);
			return application;
		} catch (Exception e) {
			throw new RuntimeException("application build init failed",e);
		}
	}
	
	// Constructor as execution anti pattern 
	static class Cleaner {
		
		@Inject
		public Cleaner(ApplicationLifecycle c) {
			c.addStopHook(() -> {
				logger.debug("clearing application reference");
				application = null;
				return CompletableFuture.completedFuture(null);
			});
		}
		
	}
	
	/**
	 * Default port for tests HTTP server. Port number 0 is used so the system chooses
	 * a free port.
	 */
	public static final int TESTS_PORT = 0;
	
	/**
	 * Run the given test using the application and the default port for the HTTP server.
	 * @param app   application to test
	 * @param toRun test to run
	 */
	public static void testInServer(Application app, C1<WSClient> toRun) {
		testInServer(app, TESTS_PORT, toRun);
	}
	
	/**
	 * Run the given test using the application and the HTTP server at the given port.
	 * @param app   application to run
	 * @param port  server port to use
	 * @param toRun code to execute
	 */
	public static void testInServer(Application app, int port, C1<WSClient> toRun) {
		TestServer server = testServer(port,app);
	    running(server, () -> {
	    	int realPort = ((Number)server.runningHttpPort().get()).intValue();
	    	logger.debug("using test server on port {}", realPort);
	        try (WSClient ws = WSTestClient.newClient(realPort)) {
	        	toRun.accept(ws);
	        } catch (Exception e) {
	        	throw new RuntimeException("failed",e);
	        }
	    });
	}	

	public static void checkRoutes(NGLWSClient ws) {
		RoutesTest.checkRoutes(ws);
	}
	
	// ------------------------------------------------------------------
	// ----- Object codes generation

	/**
	 * Default generated code prefix.
	 */
	public static final String DEFAULT_CODE_PREFIX = "T";
	
	/**
	 * Code encoding.
	 */
	private static final LongEncoding encoder = new Base26ArrayEncoding();
	
	// This is a 'good enough' implementation, anything better is welcome. 
	/**
	 * @return session related base key
	 */
	public static String testTimeKey() {
		if (testTimeKey == null)
			testTimeKey = encoder.encode(System.currentTimeMillis() % 1_000_000_000);
		return testTimeKey;
	}

	/**
	 * Generate a code with the given prefix.
	 * @param head prefix to prepend
	 * @return     generated code
	 */
	public static synchronized String newCode(String head) {
		String iid        = encoder.encode(codeId ++, 4); 
		return codePrefix(head) + iid;
	}
	
	/**
	 * Generate a new code with {@link #DEFAULT_CODE_PREFIX} as prefix.
	 * @return generated code
	 */
	public static String newCode() {
		return newCode(DEFAULT_CODE_PREFIX);
	}

	/**
	 * Build a session (JVM) wide unique prefix.
	 * Uniqueness is supposed as users are not supposed to execute multiple test
	 * sessions at once and test take more than 1ms to run.
	 * @param head code prefix
	 * @return     unique code for the session
	 */
	public static String codePrefix(String head) {
		String testRunner = System.getProperty("user.name").toUpperCase();
		String datePart   = testTimeKey();
		return head + testRunner + datePart;
	}
	
	/**
	 * Code prefix used for this session.
	 * @return code prefix used for this session
	 */
	public static String codePrefix() {
		return codePrefix(DEFAULT_CODE_PREFIX);
	}

	// ------------------------------------------------------------------

	private static boolean isPropertyInfoKey(String key) {
		return key.startsWith("ngl")
			|| key.startsWith("NGL")
			|| key.startsWith("logger");
	}
	
	/**
	 * Debugging purpose function.
	 * @param infoOnly display only info level properties 
	 */
	private static void propsDump(boolean infoOnly) {
		for (Map.Entry<Object,Object> e : System.getProperties().entrySet()) {
			String key = e.getKey().toString();
			if (isPropertyInfoKey(key))
				logger.info("system property {}={}", e.getKey(), e.getValue());
			else if (!infoOnly)
				logger.debug("system property {}={}", e.getKey(), e.getValue());				
		}
	}
	
	// ---------------------------------------------------------------------------------------------------------
	// -- dead / old code
	
//	/*
//	 * Get the full name of the file that matches the given resource. 
//	 * @param name name of the resource to find
//	 * @return     full path to the found file
//	 */
//	public static String resourceFileName(String name) {
//		try {
//			List<URL> resources = Collections.list(DevAppTesting.class.getClassLoader().getResources(name));
//			if (resources.size() == 0)
//				throw new RuntimeException("could not locate resource '" + name + "' using classloader");
//			for (URL url : resources) {
//				try {
//					logger.info("trying to load {} from {}", name, url);
//					File file = new File(url.toURI());
//					return file.toString();
//				} catch (Exception e) {
//					logger.warn(" {} cannot be converted to a File", url);
//				}
//			}
//		} catch (IOException e) {
//			throw new RuntimeException("classloader get resource failed", e);
//		}
//		throw new RuntimeException("resource could not be loaded '" + name + "'");
//	}
//	
	
//	/**
//	 * Builds an application applying modifications 
//	 * @param appConfFile application configuration file name
//	 * @param mods        modification to apply to the application builder
//	 * @return            built application
//	 */
//	@SafeVarargs
//	public static Application devapp(String appConfFile, Function<GuiceApplicationBuilder,GuiceApplicationBuilder>... mods) {
//		return devapp(appConfFile, Arrays.asList(mods));
//	}
	
	
//	private static final BaseNEncoding encoder = new Base62ArrayEncoding();
	
//	/**
//	 * Map hexadecimal chars to letters.
//	 * @param s string to apply substitution to
//	 * @return  string with applied substitution
//	 */
//	private static String hexToLetters(String s) {
//		return org.apache.commons.lang3.StringUtils.replaceChars(s,
//				                                                 "0123456789abcdef",
//				                                                 "ABCDEFGHIJKLMONP");	
//	}
	
	
//	// Could use base 26 (A-z) or base 36 (0-9A-Z), base 52 or 62
//	// is yet another possibility.
//	
//	private static final char[] base26chars;
//	private static final char[] base36chars;
//	private static final char[] base62chars;
//	
//	static {
//		base26chars = new char[26];
//		for (int i=0; i<26; i++)
//			base26chars[i] = (char)('A' + i);
//		base36chars = new char[36];
//		System.arraycopy(base26chars, 0, base36chars, 0, base26chars.length);
//		for (int i=0; i<10; i++)
//			base36chars[26+i] = (char)('0' + i); 
////		for (int i=0; i<26; i++)
////			base36chars[i+10] = base26chars[i];
//		base62chars = new char[62];
////		for (int i=0; i<36; i++)
////			base62chars[i] = base36chars[i];
//		System.arraycopy(base36chars, 0, base62chars, 0, base36chars.length);
//		for (int i=0; i<26; i++)
//			base62chars[i+36] = (char)('a' + i);		
//	}
//	
//	// Encode integer using a given mapping.
//	public static String baseEncode(char[] chars, long value, int minLength) {
//		if (value < 0)
//			throw new IllegalArgumentException("value to encode cannot be negative");
//		minLength = Math.max(1, minLength);
//		StringBuilder sb = new StringBuilder();
//		int base = chars.length;
//		while (value != 0) {
//			long div = value / base;
//			long rem = value % base;
//			sb.append(chars[(int)rem]);
//			value = div;
//		}
//		while (sb.length() < minLength)
//			sb.append(chars[0]);
//		return sb.reverse().toString();
//	}
//	
//	public static String baseEncode(char[] chars, long value) {
//		return baseEncode(chars, value, 1);
//	}
//	
//	public static String base26encode(long value, int minLength) {
//		return baseEncode(base26chars, value, minLength);
//	}
//	
//	public static String base26encode(long value) {
//		return baseEncode(base26chars, value);
//	}
//	
//	public static String base36encode(long value, int minLength) {
//		return baseEncode(base36chars, value, minLength);
//	}
//	
//	public static String base36encode(long value) {
//		return baseEncode(base36chars, value);
//	}
//	
//	public static String base62encode(long value, int minLength) {
//		return baseEncode(base26chars, value, minLength);
//	}
//	
//	public static String base62encode(long value) {
//		return baseEncode(base26chars, value);
//	}
//
//	private static final BiFunction<Long,Integer,String> encoderL = (v,l) -> base62encode(v,l); 
//	private static final Function<Long,String>           encoder  =  v    -> encoderL.apply(v,1); 

	
	//	// Those do not work properly.
//	public static final String PROP_NAME_LOGGER_FILE     = "ngl.test.logger.file";
//	public static final String PROP_NAME_LOGGER_RESOURCE = "ngl.test.logger.resource"; 
	
	/*
	 * Read, modify data, update, read again and compare read data to modified data.
	 * @param ws       web client
	 * @param url      URL to use for the get and put
	 * @param modify   JSON modification to run
	 * @param preCheck JSON before check modification
	 */
	/*
	public static void rur(WSClient ws, String url, Consumer<JsonNode> modify, Consumer<JsonNode> preCheck) {
		// Read
		logger.debug("GET - " + url);
		WSResponse r0 = get(ws,url,Status.OK);
		JsonNode js0 = Json.parse(r0.getBody());
		modify.accept(js0);
		// Update
		logger.debug("PUT - " + url);		
		WSResponse r1 = put(ws,url,js0.toString());
		assertEquals(Status.OK, r1.getStatus());
		// Read updated
		logger.debug("GET - " + url);
		WSResponse r2 = get(ws,url);
		assertEquals(Status.OK, r2.getStatus());
		JsonNode js1 = Json.parse(r2.getBody());
		// apply precheck to js0 and js1
		preCheck.accept(js0);
		preCheck.accept(js1);
		// assertEquals(js0,js1);
		cmp("",js0,js1);
	}
	
	
	// RUR could be made a class with some configuration and run methods
	// Could check that we get come error code instead of asserting equality 
	public static void rur(WSClient ws, String url, Consumer<JsonNode> modify) {
		rur(ws,url,modify,js -> { remove(js,"traceInformation"); });
	}
	
	public static void rur(WSClient ws, String url) {
		rur(ws,url,js -> {});
	}
	*/
	
//	/**
//	 * Standard RUR test that checks that the traceInformation has chnaged after the udate.
//	 * @param url url to check
//	 * @param ws  web client to use
//	 */
//	public static void rurNeqTraceInfo(WSClient ws, String url) {
//		new ReadUpdateReadTest(url)
//			.assertion(notEqualsPath("traceInformation"))
//			.run(ws);
//	}
//	
//	// This requires that the input object has a code field (DBObject most certainly).
//	public static void rurNeqTraceInfo(WSClient ws, String url, JsonNode n) {
//		String code = new JsonFacade(n).getString("code");
//		new ReadUpdateReadTest(url + "/" + code)
//			.assertion(notEqualsPath("traceInformation"))
//			.run(ws);
//	}
	
//	public static void rurNeqTraceInfo(WSClient ws, String url, Object o) {
//		rurNeqTraceInfo(ws,url,Json.toJson(o));
//	}
	
	
//	public static void cr(WSClient ws, String url, JsonNode data) {
//		// This must post data to fill a form server side (Form<Sample>)
//		// The provided json data has to be converted to form data.
//		// With some luck we can map the provided json fields to the
//		// corresponding sample attribute.
//		// @see models.laboratory.sample.instance.Sample
//		WSResponse r0 = WSHelper.post(ws,url,data.toString(),Status.OK);
//		// logger.debug("post " + url + " : " + r0.getBody());
//		// assertEquals(Status.OK,r0.getStatus());
//		JsonNode js0 = Json.parse(r0.getBody());
//		WSResponse r1 = WSHelper.get(ws,url + "/" + JsonHelper.get(js0,"code").textValue(),Status.OK);
//		JsonNode js1 = Json.parse(r1.getBody());
//		cmp(js0,js1);
//	}
//	public static void cr(NGLWSClient ws, String url, JsonNode data) {
//		// This must post data to fill a form server side (Form<Sample>)
//		// The provided json data has to be converted to form data.
//		// With some luck we can map the provided json fields to the
//		// corresponding sample attribute.
//		// @see models.laboratory.sample.instance.Sample
//		WSResponse r0 = ws.post(url,data.toString(),Status.OK);
//		// logger.debug("post " + url + " : " + r0.getBody());
//		// assertEquals(Status.OK,r0.getStatus());
//		JsonNode js0 = Json.parse(r0.getBody());
//		WSResponse r1 = ws.get(url + "/" + JsonHelper.get(js0,"code").textValue(),Status.OK);
//		JsonNode js1 = Json.parse(r1.getBody());
//		cmp(js0,js1);
//	}
//	
//	public static void cr(WSClient ws, String url, Object data) {
//		cr(ws,url,Json.toJson(data));
//	}
	
	
//	public static final void cmp(JsonNode n0, JsonNode n1) {
//		cmp("",n0,n1);
//	}
//	
//	// Assert equals
//	public static final void cmp(String path, JsonNode n0, JsonNode n1) {
//		//System.out.println("cmp " + path);
//		assertEquals(path,n0.getNodeType(),n1.getNodeType());
//		assertEquals(path,n0.size(),n1.size());
//		switch (n0.getNodeType()) {
//		case ARRAY:
//			cmpArray(path,n0,n1);
//			break;
//		case BINARY:
//			throw new RuntimeException("unexpected BINARY at " + path);
//		case BOOLEAN:
//			assertEquals(path,n0,n1);
//			break;
//		case MISSING:
//			throw new RuntimeException("unexpected MISSING at " + path);
//		case NULL:
//			break;
//		case NUMBER:
//			assertEquals(path,n0,n1);
//			break;
//		case OBJECT:
//			Iterator<String> iter = n0.fieldNames();
//			while(iter.hasNext()) {
//				String s = iter.next();
//				JsonNode c0 = n0.get(s);
//				JsonNode c1 = n1.get(s);
//				cmp(path+"/"+s,c0,c1);
//			}
//			break;
//		case POJO:
//			throw new RuntimeException("unexpected POJO at " + path);
//		case STRING:
//			assertEquals(path,n0,n1);
//		}
//	}
//
//	// Assert equals array / indexed.
//	public static void cmpArray(String path, JsonNode n0, JsonNode n1) {
//		if (n0.size() > 0) {
//			if (n0.get(0).get("index") != null) {
//				List<JsonNode> l0 = new ArrayList<>();
//				List<JsonNode> l1 = new ArrayList<>();
//				for (int i=0; i<n0.size(); i++) {
//					l0.add(n0.get(i));
//					l1.add(n1.get(i));
//				}
//				// Sort lists by "index"
//				Comparator<JsonNode> indexCmp = new Comparator<JsonNode>() {
//					@Override
//					public int compare(JsonNode arg0, JsonNode arg1) {
//						return arg0.get("index").toString().compareTo(arg1.get("index").toString());
//					}
//				};
//				Collections.sort(l0,indexCmp);
//				Collections.sort(l1,indexCmp);
//			} else {
//				for (int i=0; i<n0.size(); i++)
//					cmp(path + "[" + i + "]", n0.get(i), n1.get(i));				
//			}
//		}
//	}
	
//	public static Application devapp(String appConfFile) {
//	ArrayList<Function<GuiceApplicationBuilder,GuiceApplicationBuilder>> l = new ArrayList<>();
//	return devapp(appConfFile,l);
//}
//
//public static Application devapp(String appConfFile, Function<GuiceApplicationBuilder,GuiceApplicationBuilder> mod0) {
//	ArrayList<Function<GuiceApplicationBuilder,GuiceApplicationBuilder>> l = new ArrayList<>();
//	l.add(mod0);
//	return devapp(appConfFile,l);
//}

//	// Run through DAO stuff.
//	public static <T extends DBObject> void savage(JsonNode n, Class<T> t, String collectionName) {
//		T o = Json.fromJson(n, t);
//		MongoDBDAO.save(collectionName, o);
//	}
//	
//	public static <T extends DBObject> void savage(T o, Class<T> t, String collectionName) {
//		MongoDBDAO.save(collectionName, o);
//	}
	
	/*
	 * Application builder instance, should either be destroyed  
	 */
	// private static GuiceApplicationBuilder applicationBuilder;
	
	/*private static void loggerSetup() {
		if (System.getProperty(PROP_NAME_LOGGER_FILE) != null)
			System.setProperty("logger.file", System.getProperty(PROP_NAME_LOGGER_FILE));
		else if (System.getProperty(PROP_NAME_LOGGER_RESOURCE) != null)
			System.setProperty("logger.resource", System.getProperty(PROP_NAME_LOGGER_RESOURCE));
		else if (System.getProperty("logger.file") != null)
			;
		else if (System.getProperty("logger.file") != null)
			;
		else 
			throw new RuntimeException(" set either '" + PROP_NAME_LOGGER_FILE + "' or '" + PROP_NAME_LOGGER_RESOURCE 
					                   + "' by setting an environment variable or running sbt \"-D" + PROP_NAME_LOGGER_FILE 
					                   + "=<absolutefilename>\" or \"-D" + PROP_NAME_LOGGER_RESOURCE + "=<name>\" that is"
					                   + " looked for in the classpath");
	}*/
//	private static String hexToLetters_(String s) {
//	StringBuilder b = new StringBuilder(s.length());
//	for (int i = 0; i < s.length(); i++) {
//		char c = s.charAt(i);
//		if (c >= '0' && c <= '9')
//			b.append(c + 'A' - '0');
//		else if (c >= 'a' && c <= 'z')
//			b.append(c + 'K' - 'a');
//		else
//			b.append('_');
//	}
//	return b.toString();	
//}

	// Locate the configuration through the resources but use it with 'config.file'
	// so the configuration file includes are consistent with the usual -Dconfig.file
	/*public static Application devappF(String appConfFile, String logConfFile) {
		if (application != null) {
			logger.warn("returning already application");
			return application;
		}
		try {
			String confFileName = resourceFileName(appConfFile);
			logger .debug("using config file '" + confFileName + "'");
			System.setProperty("config.file",     confFileName);
			System.setProperty("logger.resource", logConfFile);
			System.setProperty("play.server.netty.maxInitialLineLength", "16384");
			Environment env = new Environment(play.Mode.TEST);
			GuiceApplicationBuilder applicationBuilder = new GuiceApplicationBuilder().in(env);
			application = applicationBuilder.build();
			// Register an aplication lifecycle cleaner.
			application.injector().instanceOf(Cleaner.class);
			return application;
		} catch (Exception e) {
			throw new RuntimeException("application build init failed",e);
		}
	}*/


}






