package sra.scripts.tests;

import javax.inject.Inject;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
//import sra.logging.CronLoggingEnabler;
import sra.services.lock.LockAPI;
import sra.services.lock.LockException;
import sra.services.lock.ActiveLock;
/*
 * Script TEST
 * {@code http://localhost:9000/sra/scripts/run/sra.scripts.tests.LockWait}
 * <br> *  
 * @author sgas
 *
 */
public class LockWait extends  ScriptNoArgs { 
	private final LockAPI lockAPI;
	
	@Inject
	public LockWait(LockAPI lockAPI) {
		this.lockAPI = lockAPI;
	}
	
	@Override
	public void execute() throws Exception  {
		
		try (ActiveLock l = lockAPI.aquireLock(LockWait.class); 
			 //CronLoggingEnabler cl = CronLoggingEnabler.getEnabler()
				){
			logger.info("XXXXXXXXXXXXXXX          starting");
			Thread.sleep(10_000);
			println("{\"result\": \"ok\"}");
			logger.info("XXXXXXXXXXXXXX   done");
		} catch (LockException e) {
			println(e.getMessage());
			logger.info("XXXXXXXXXXXXXX  impossible d'acquerir le lock " + e.getMessage());
		} 
	}
	
}
