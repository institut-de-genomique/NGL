package fr.cea.ig.ngl.test.resource;

import fr.cea.ig.ngl.test.NGLTestTrace;
import fr.cea.ig.ngl.test.authentication.Identity;
import fr.cea.ig.play.test.ApplicationFactory;
import fr.cea.ig.test.TestContext;
import fr.cea.ig.test.WSTestContext;
import fr.cea.ig.util.function.CC1;
import fr.cea.ig.util.function.T;
import ngl.common.Global;
import play.Application;

/**
 * Application context CCs. There are simple application context ({@link #app}), a
 * context with a web client ({@link #wsContextResource}), a facade context ({@link #contextResource}) and
 * contexts with some MongoDB tracing capabilities ({@link #appLog}, {@link #ctxLog}).
 * 
 * @author vrd
 *
 */
public class RApplication {
	
	/**
	 * Simple application resources.
	 */
	public static final CC1<Application> app = Global.afSq.as(Identity.ReadWrite).cc1();
	
//	/**
//	 * Resource with an application and web client
//	 */
//	public static final CC2<Application, NGLWSClient> appResource = ApplicationFactory.Actions.withAppWS(Global.afSq.as(Identity.ReadWrite));
	
//	/**
//	 * Resource with an application test context.
//	 */
//	public static final CC1<TestContext> contextResource = appResource.cc1((app, client) -> new TestContext(app, client));
	
	/**
	 * Test context with simplified application API access.
	 */
	public static final CC1<TestContext> contextResource = app.cc1((app) -> T.t1(new TestContext(app)));
	
	/**
	 * Test context with simplified application API access, shorthand for {@link #contextResource}.
	 */
	public static final CC1<TestContext> ctx = contextResource;
	
	/**
	 * Test context creation action providing a web client ({@link WSTestContext#client()}.
	 */
	public static final CC1<WSTestContext> wsContextResource = 
			ApplicationFactory.Actions.withAppWS(Global.afSq.as(Identity.ReadWrite))
			.cc1((app, client) -> new WSTestContext(app, client));
	
	/**
	 * Test context creation action providing a web client, shorthand for {@link #wsContextResource}.
	 */
	public static final CC1<WSTestContext> wsc = wsContextResource;
	
	/**
	 * Tracing application context.
	 */
	public static final CC1<Application> appLog = NGLTestTrace.exlog(app);
	
	/**
	 * Tracing test context.
	 */
	public static final CC1<TestContext> ctxLog = NGLTestTrace.exlog(ctx);
	
	/**
	 * Tracing test context with a web client.
	 */
	public static final CC1<WSTestContext> wscLog = NGLTestTrace.exlog(wsc);
	
}
