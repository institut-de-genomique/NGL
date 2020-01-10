package controllers.migration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.mongojack.DBQuery;

import controllers.CommonController;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.MongoDBResult;
import models.laboratory.common.description.Level;
import models.laboratory.run.description.TreatmentType;
import models.laboratory.run.instance.Analysis;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import models.laboratory.run.instance.Treatment;
import models.utils.InstanceConstants;
import models.utils.dao.DAOException;
import play.Logger;
import play.Logger.ALogger;
import play.mvc.Result;
import validation.ContextValidation;

public class CheckTypeDescription extends CommonController{


	public static Result check() throws DAOException
	{
		//Get all type treatments
		List<TreatmentType> treatmentTypes = TreatmentType.find.findAll();
		List<Run> noValidateRuns = new ArrayList<>();
		List<ReadSet> noValidateReadSets = new ArrayList<>();
		List<Analysis> noValidateAnalysis = new ArrayList<>();
		for(TreatmentType treatType : treatmentTypes){
			Logger.debug("Treatment type "+treatType.code);
			//By names
			String[] names = treatType.names.split(",");
			for(int i=0; i<names.length; i++){
				Logger.debug("For name "+names[i]);
				MongoDBResult<Run> runs = MongoDBDAO.find(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, DBQuery.is("treatments."+names[i]+".typeCode", treatType.code)).limit(1);
				if(runs!=null && runs.toList().size()>0){
					Run run = runs.toList().iterator().next();
					if(run!=null){
						Treatment treatment = run.treatments.get(names[i]);
						ContextValidation ctxVal = new ContextValidation(getCurrentUser());
						ctxVal.setUpdateMode();
						ctxVal.putObject("level", Level.CODE.Run);
						ctxVal.putObject("run", run);
						treatment.validate(ctxVal);
						if(ctxVal.hasErrors()){
							Logger.error("Run no validate");
						}
					}
				}
			}

			for(int i=0; i<names.length; i++){
				MongoDBResult<ReadSet> readSets = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("treatments."+names[i]+".typeCode", treatType.code)).limit(1);
				if(readSets!=null && readSets.toList().size()>0){
					ReadSet readSet = readSets.toList().iterator().next();
					if(readSet!=null){
						Logger.debug("ReadSet "+readSet.code);
						Treatment treatment = readSet.treatments.get(names[i]);
						ContextValidation ctxVal = new ContextValidation(getCurrentUser());
						ctxVal.setUpdateMode();
						ctxVal.putObject("level", Level.CODE.ReadSet);
						ctxVal.putObject("readSet", readSet);
						treatment.validate(ctxVal);
						if(ctxVal.hasErrors()){
							Logger.error("ReadSet no validate");
							noValidateReadSets.add(readSet);
						}
					}
				}
			}

			for(int i=0; i<names.length; i++){
				MongoDBResult<Analysis> analysis = MongoDBDAO.find(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, DBQuery.is("treatments."+names[i]+".typeCode", treatType.code)).limit(1);
				if(analysis!=null && analysis.toList().size()>0){
					Analysis analyse = analysis.toList().iterator().next();
					if(analyse!=null){
						Logger.debug("Analyse "+analyse.code);
						Treatment treatment = analyse.treatments.get(names[i]);
						ContextValidation ctxVal = new ContextValidation(getCurrentUser());
						ctxVal.setUpdateMode();
						ctxVal.putObject("level", Level.CODE.Analysis);
						ctxVal.putObject("analysis", analyse);
						treatment.validate(ctxVal);
						if(ctxVal.hasErrors()){
							Logger.error("Analyse no validate");
							noValidateAnalysis.add(analyse);
						}
					}
				}
			}

		}

		if(noValidateRuns.size()==0 && noValidateReadSets.size()==0 && noValidateAnalysis.size()==0){
			return ok("Runs, Readsets and Analysis treatment validate");
		}else{
			return badRequest("Runs, ReadSets and Analysis treatment no validate");
		}

	}
}
