package scripts;

import java.util.List;

import javax.inject.Inject;

import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.Script;
import fr.cea.ig.ngl.dao.analyses.AnalysesAPI;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import models.laboratory.common.instance.TBoolean;
import models.laboratory.common.instance.Valuation;
import models.laboratory.run.instance.Analysis;
import models.utils.InstanceConstants;
import play.Logger;
/**
 * Script permettant de valider en masse un ensemble d'analyse
 * SUPSQ-3457 : Demande de Fred pour valider en masse 2272 analyses.
 * 
 * @author ejacoby
 *
 */
public class BatchAnalysesValuationScript extends Script<BatchAnalysesValuationScript.Args>{

	private final AnalysesAPI analysesAPI;
	
	@Inject
	public BatchAnalysesValuationScript(AnalysesAPI analysesAPI) {
		this.analysesAPI=analysesAPI;
	}

	public static class Args {
		public String projectCode;
	}


	@Override
	public void execute(Args args) throws Exception {
		Logger.debug("Nb arguments "+args.projectCode);
		if(args.projectCode!=null){
			//Get all Analyses to valuate
			List<Analysis> analysis = MongoDBDAO.find(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, DBQuery.in("projectCodes", args.projectCode).is("state.code", "IW-V")).toList();
			Logger.debug("Nb to evaluate "+analysis.size());
			analysis.parallelStream().forEach(a->{
				Valuation valuation = new Valuation();
				valuation.valid=TBoolean.TRUE;
				valuation.user="ngl-admin";
				try {
					Logger.debug("Valuation "+a.code);
					analysesAPI.valuation(a.code, valuation, "ngl-admin");
				} catch (APIValidationException e) {
					e.printStackTrace();
				} catch (APIException e) {
					e.printStackTrace();
				}
			});
			Logger.debug("End evaluate");
		}
	}
	
}
