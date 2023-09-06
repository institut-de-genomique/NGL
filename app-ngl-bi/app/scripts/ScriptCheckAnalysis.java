package scripts;

import java.util.Iterator;

import org.mongojack.DBCursor;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.MongoDBResult;
import fr.cea.ig.lfw.controllers.scripts.buffered.Script;
import models.laboratory.run.instance.Analysis;
import models.utils.InstanceConstants;
import play.Logger;
import validation.ContextValidation;
import validation.run.instance.AnalysisValidationHelper;

public class ScriptCheckAnalysis extends Script<ScriptCheckAnalysis.NoArgs> {

	public static final class NoArgs { }

	@Override
	public void execute(NoArgs args) throws Exception {
		Logger.error("ScriptCheckAnalysis started");

		MongoDBResult<Analysis> res = MongoDBDAO.find(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class);
		
		DBCursor<Analysis> c = res.cursor;
		Iterator<Analysis> it = c.iterator();

		int countErr = 0;
		int count = 0;

		while (it.hasNext()) {
			Analysis an = it.next();
			ContextValidation ctxVal = ContextValidation.createUndefinedContext("jcharpen");
			
			// an.validate(ctxVal);

			// AnalysisValidationHelper.validateMasterReadsetCodes(ctxVal, an.masterReadSetCodes, an.readSetCodes);
			AnalysisValidationHelper.validateReadSetCodes(ctxVal, an);
			
			if (ctxVal.hasErrors()) {
				Logger.error("code : " + an.code);				
				Logger.error("typeCode : " + an.typeCode);
				Logger.error("traceInformation.creationDate : " + an.traceInformation.creationDate);
				Logger.error("errors : " + ctxVal.getErrors());
					
				countErr++;
			}	

			Logger.error("count : " + count++);
		}
		
		Logger.error("countErr : " + countErr); 

		Logger.error("ScriptCheckAnalysis ended");
	}
}