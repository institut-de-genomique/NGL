package services.instance;

import java.sql.SQLException;

import org.slf4j.MDC;

import com.mongodb.MongoException;

import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import models.Constants;
import models.utils.dao.DAOException;
import rules.services.RulesException;
import scala.concurrent.duration.FiniteDuration;
import validation.ContextValidation;

public abstract class AbstractImportData implements Runnable {

	protected final play.Logger.ALogger logger;
//	protected /*final*/ ContextValidation   contextError;
	protected final String              name;
//	protected final NGLContext          ctx;
	protected final NGLApplication      app;
	
//	@Inject
//	public AbstractImportData(String name, FiniteDuration durationFromStart, FiniteDuration durationFromNextIteration, NGLContext ctx) {
//		this.contextError = new ContextValidation(Constants.NGL_DATA_USER);
//		this.name         = name;
//		this.ctx          = ctx;
//		logger            = play.Logger.of(this.getClass().getName());
//		logger.info(name+" start in "+durationFromStart.toMinutes()+" minutes and other iterations every "+durationFromNextIteration.toMinutes()+" minutes");
//		
//		//Akka.system()
//		ctx.akkaSystem().scheduler().schedule(durationFromStart,
//				                              durationFromNextIteration, 
//				                              this, 
//				                              // Akka.system()
//				                              ctx.akkaSystem().dispatcher());
//	}

//	protected AbstractImportData(String name, NGLContext ctx) {
//		this.contextError = new ContextValidation(Constants.NGL_DATA_USER);
//		this.name         = name;
//		this.ctx          = ctx;
//		logger            = play.Logger.of(this.getClass().getName());
//	}

	protected AbstractImportData() {
		this.name = "";
		this.app = null;
		logger = null;
	}

	protected AbstractImportData(String name, NGLApplication app) {
//		this.contextError = new ContextValidation(Constants.NGL_DATA_USER);
//		this.contextError = ContextValidation.createUndefinedContext(Constants.NGL_DATA_USER);
		this.name         = name;
		this.app          = app;
		logger            = play.Logger.of(this.getClass().getName());
	}
	
	public void startScheduling(FiniteDuration durationFromStart, FiniteDuration durationFromNextIteration) {
//		app.akkaSystem().scheduler().schedule(durationFromStart,
		app.actorSystem().scheduler().schedule(durationFromStart,
				                               durationFromNextIteration, 
                                               this, 
                                               app.actorSystem().dispatcher());		
	}
	
	@Override
	public void run() {
		boolean error = false;

		MDC.put("name", name);
//		contextError.clear();
//		contextError.setCreationMode();
//		contextError = ContextValidation.createCreationContext(contextError.getUser());
		ContextValidation contextError = ContextValidation.createUndefinedContext(Constants.NGL_DATA_USER);
		contextError.addKeyToRootKeyName("import");
		logger.info("ImportData execution : {}", name);
		long t1 = System.currentTimeMillis();
		try {
			runImport(contextError);
			contextError.removeKeyFromRootKeyName("import");
		} catch (Exception e) {
			logger.error("",e);
			error = true;
		} finally {
//			error = contextError.hasErrors() ? true : error;
			error = error || contextError.hasErrors();
			// Display error messages
			contextError.displayErrors(logger);
			// Logger send an email
			long t2 = System.currentTimeMillis();
			if (error) {
				logger.error("ImportData End Error - " + (t2-t1) / 1000 + " s");
			} else {
				logger.info("ImportData End - " + (t2-t1) / 1000 + " s");
			}
			MDC.remove("name");
		}
	}

//	public abstract void runImport() throws SQLException, DAOException, MongoException, RulesException, APIValidationException, APIException;
	public abstract void runImport(ContextValidation ctx) throws SQLException, DAOException, MongoException, RulesException, APIValidationException, APIException;

}
