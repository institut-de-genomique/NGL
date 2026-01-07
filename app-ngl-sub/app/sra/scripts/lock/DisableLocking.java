package sra.scripts.lock;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.bson.types.ObjectId;
import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import models.laboratory.run.instance.Run;
import models.utils.InstanceConstants;
//import sra.logging.CronLoggingEnabler;
import sra.services.crons.SubmissionAtlaseaProjectsToEbi;
import sra.services.lock.LockAPI;
import sra.services.lock.LockException;
import sra.services.lock.ActiveLock;
import validation.ContextValidation;

//{@code http://localhost:9000/sra/scripts/run/sra.scripts.lock.DisableLocking}
 
/**
 * Classe permettant de desactiver l'attribution des verrous (lock) dans NGL-SUB
 * 
 * <p>
 * Exemple d'utilisation : 
 * <p>
 * curl  -X GET -H 'User-Agent: bot' -H 'Content-Type: application/json' 
 * -v "http://localhost:9000/sra/scripts/run/sra.scripts.lock.DisableLocking"
 * 
 * @author sgas
 *
 */
public class DisableLocking extends  ScriptNoArgs { 
	private final LockAPI lockAPI;
	
	@Inject
	public DisableLocking(LockAPI lockAPI) {
		this.lockAPI = lockAPI;
	}
	
	@Override
	public void execute() throws Exception  {
		lockAPI.setAllowLocking(false);
	}
	
}
