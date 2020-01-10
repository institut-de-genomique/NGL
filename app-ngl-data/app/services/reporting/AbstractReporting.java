package services.reporting;

import java.io.UnsupportedEncodingException;

import javax.inject.Inject;
import javax.mail.MessagingException;

import org.slf4j.MDC;

import fr.cea.ig.ngl.NGLApplication;
import scala.concurrent.duration.FiniteDuration;

public abstract class AbstractReporting implements Runnable {

	protected play.Logger.ALogger logger;
	
	private final String name;
//	protected final NGLContext ctx;
	protected final NGLApplication app;
	
//	@Inject
//	public AbstractReporting(String name,FiniteDuration durationFromStart, FiniteDuration durationFromNextIteration, NGLContext ctx){
//		this.name = name;
//		logger = play.Logger.of(this.getClass().getName());
//		// Akka.system()
//		ctx.akkaSystem()
//		.scheduler().schedule(durationFromStart,durationFromNextIteration
//				, this, //Akka.system().dispatcher()
//				ctx.akkaSystem().dispatcher()
//				); 
//	}
	
//	@Inject
//	public AbstractReporting(String name, NGLContext ctx) {
//		this.name = name;
//		this.ctx  = ctx;
//		logger = play.Logger.of(this.getClass().getName());
//	}

	@Inject
	public AbstractReporting(String name, NGLApplication app) {
		this.name = name;
		this.app  = app;
		logger    = play.Logger.of(this.getClass().getName());
	}

	public void startScheduling(FiniteDuration durationFromStart, FiniteDuration durationFromNextIteration) {
//		ctx.akkaSystem()
//			.scheduler()
//			.schedule(durationFromStart,durationFromNextIteration,
//					  this,
//					  ctx.akkaSystem().dispatcher()); 		
		app.actorSystem()
		   .scheduler()
		   .schedule(durationFromStart,durationFromNextIteration,
			         this,
			         app.actorSystem().dispatcher()); 		
	}
	
	@Override
	public void run() {
		MDC.put("name", name);
		logger.info("Reporting execution :"+name);
		try {
			runReporting();
		} catch (Throwable e) {
			logger.error("",e);
		} finally {
			MDC.remove("name");
		}
	}

	public abstract void runReporting() throws UnsupportedEncodingException, MessagingException;

}
