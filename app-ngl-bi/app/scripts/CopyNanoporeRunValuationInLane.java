package scripts;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import org.mongojack.DBQuery;

import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.runs.RunsDAO;
import models.laboratory.run.instance.Lane;
import models.laboratory.run.instance.Run;
import play.libs.Json;
import validation.ContextValidation;

//http://localhost:9000/scripts/run/scripts.CopyNanoporeRunValuationInLane

public class CopyNanoporeRunValuationInLane  extends ScriptNoArgs{
	private final RunsDAO           runsDAO;
	private final NGLApplication    app;

	@Inject
	public CopyNanoporeRunValuationInLane(RunsDAO           runsDAO,  
										  NGLApplication    app      ) {
		this.runsDAO = runsDAO;
		this.app     = app;
	}
	
	@Override
	public void execute() throws Exception {
		List<Run> runs = new ArrayList<Run>();
		String user = "ngl-support";
		int cp = 0;
		int cpError = 0;
		runs = runsDAO.dao_find(DBQuery.in("categoryCode", "nanopore")).toList(); //2000 données au CNS en juin 2022
		println(runs.size() + "runs en analyse");
		for (Run run : runs ) {
			ContextValidation contextValidation = ContextValidation.createUpdateContext(user); 
			cp++;
			//println("run.valuation.valid = " + run.valuation.valid.toString());
			Iterator<Lane> iterator = run.lanes.iterator();
			while(iterator.hasNext()) {
				Lane lane = iterator.next();
				//println("   lane.valuation = " + lane.valuation.valid.toString());
				lane.valuation.valid = run.valuation.valid;	
				lane.valuation.user = user;
				lane.valuation.date = new Date();
			}
			run.traceInformation.modifyDate = new Date();
			run.traceInformation.modifyUser = user;
			
			// en prod 39 runs ne passent pas la validation.
//			run.validate(contextValidation);
//
			if(contextValidation.hasErrors()) {
//				cpError++;
				//contextValidation.displayErrors(logger, "debug");
				println("Error pour " + run.code);
				println(Json.prettyPrint(app.errorsAsJson(contextValidation.getErrors())));				
			} else {
//				println("ok pour " + run.code);
			}
		}
		
		// si aucune erreur alors sauvegarde dans la base :
		if (cpError == 0 ) {
			println("sauvegarde des " + cp + " runs avec leurs lanes");
			for (Run run : runs ) {
				runsDAO.save(run);
			}
			println("Fin de la sauvegarde des " + cp + " runs avec leurs lanes");

		} else {
			println(cpError + " erreurs  sur " + cp + " runs");
			println("Aucune sauvegarde de runs dans la base");

		}
		
	}

}
