package services.instance;

import java.sql.SQLException;

import models.Constants;
import models.utils.dao.DAOException;

import org.slf4j.MDC;

import com.mongodb.MongoException;

import play.Logger;
import play.Logger.ALogger;
import play.libs.Akka;
import rules.services.RulesException;
import scala.concurrent.duration.FiniteDuration;
import validation.ContextValidation;

public abstract class AbstractImportData implements Runnable{

	public ContextValidation contextError;
	protected final String name;
	protected ALogger logger;

	public abstract void runImport() throws SQLException, DAOException, MongoException, RulesException;

	public AbstractImportData(String name,FiniteDuration durationFromStart, FiniteDuration durationFromNextIteration){
		this.contextError=new ContextValidation(Constants.NGL_DATA_USER);
		this.name=name;
		logger=Logger.of(this.getClass().getName());
		
		Logger.info(name+" start in "+durationFromStart.toMinutes()+" minutes and other iterations every "+durationFromNextIteration.toMinutes()+" minutes");
		
		Akka.system().scheduler().schedule(durationFromStart,durationFromNextIteration
				, this, Akka.system().dispatcher()
				);
				 
	}

	public void run() {
		boolean error=false;

		MDC.put("name", name);
		contextError.clear();
		contextError.addKeyToRootKeyName("import");
		long t1 = System.currentTimeMillis();
		logger.info("AbstractImportData - run - ImportData execution :"+name);

		try{
			contextError.setCreationMode();
			runImport();
			contextError.removeKeyFromRootKeyName("import");

		}catch (Throwable e) {
			logger.error("AbstractImportData - run - try runImport : error",e);
			error=true;
		}
		finally{
			error=contextError.hasErrors()?true:error;
			/* Display error messages  */
			contextError.displayErrors(logger);
			/* Logger send an email */
			long t2 = System.currentTimeMillis();
			if(error){
				logger.error("AbstractImportData - run - ImportData End Error");
			}else {
				logger.info("ImportData End - "+(t2-t1)/1000+" s");
				logger.info("AbstractImportData - run - ImportData End");
			}
			MDC.remove("name");
		}
	};



}
