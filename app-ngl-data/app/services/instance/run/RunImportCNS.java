package services.instance.run;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import com.mongodb.MongoException;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.ngl.NGLApplication;
import models.Constants;
import models.laboratory.common.description.Level;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TransientState;
import models.laboratory.run.instance.File;
import models.laboratory.run.instance.Lane;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import models.laboratory.run.instance.SampleOnContainer;
import models.laboratory.run.instance.Treatment;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import models.utils.InstanceHelpers;
import models.utils.dao.DAOException;
import rules.services.RulesException;
import rules.services.RulesServices;
import services.instance.AbstractImportDataCNS;
import validation.ContextValidation;
import validation.run.instance.LaneValidationHelper;
import workflows.run.RunWorkflows;

public class RunImportCNS extends AbstractImportDataCNS {

	// Accessed through static methods 
	@SuppressWarnings("hiding")
	private static final play.Logger.ALogger logger = play.Logger.of(RunImportCNS.class);
	
	private final RunWorkflows workflows;

	@Inject
	public RunImportCNS(NGLApplication app, RunWorkflows workflows) {
		super("RunCNS", app);
		this.workflows = workflows;
	}

//	@Override
//	public void runImport() throws SQLException, DAOException, MongoException, RulesException {
//		createRuns("pl_RunToNGL",contextError);
//	}

	@Override
	public void runImport(ContextValidation contextError) throws SQLException, DAOException, MongoException, RulesException {
		createRuns("pl_RunToNGL",contextError);
	}

	public void createRuns(String sql,ContextValidation contextError) throws SQLException, DAOException{
		logger.debug("Create Run From Lims CNS");
		List<Run> runs=limsServices.findRunsToCreate(sql, contextError);
		//Create Lane
		List<Run> newRuns=new ArrayList<>();
		String rootKeyName=null;

		for (Run run : runs) {
			if (run != null) {
				rootKeyName = "run[" + run.code + "]";
//				ContextValidation ctx = new ContextValidation(Constants.NGL_DATA_USER);
//				ctx.setCreationMode();
				ContextValidation ctx = ContextValidation.createCreationContext(Constants.NGL_DATA_USER);
				ctx.addKeyToRootKeyName(rootKeyName);

				//Save Run du Lims si n'existe pas ou n'est pas transféré dans NGL
				Run newRun=MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME,Run.class, run.code);
				if (newRun == null) {
					logger.debug("Save Run "+run.code + " mode "+contextError.getMode());
//					newRun=(Run) InstanceHelpers.save(InstanceConstants.RUN_ILLUMINA_COLL_NAME, run, ctx, true);
					newRun = InstanceHelpers.save(ctx, InstanceConstants.RUN_ILLUMINA_COLL_NAME, run, true);
				} else {

					logger.debug("Update Run "+run.code + " mode "+contextError.getMode());	
//					ctx.setCreationMode();
					ctx.putObject("level", Level.CODE.Run);
					ctx.putObject("run", run);
//					run.treatments.get("ngsrg").validate(ctx);
					run.treatments.get("ngsrg").validate(ctx, run);

					if (!ctx.hasErrors()) {
						
						run.state = fusionRunStateHistorical(run.state, newRun.state);
						
						MongoDBDAO.update(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, 
								DBQuery.is("code", run.code),
								DBUpdate.set("treatments.ngsrg",run.treatments.get("ngsrg"))
								.set("state",run.state)
								.set("dispatch",run.dispatch)
								.set("traceInformation.modifyUser","lims")
								.set("traceInformation.modifyDate",new Date()));
					}
				}

				//Si Run non null creation des lanes ou traitement ngsrg
				if(newRun!=null && !ctx.hasErrors()){
					Run runLanes=createLaneFromRun(newRun, ctx);
					if(runLanes!=null && !ctx.hasErrors()){
						newRuns.add(runLanes);
					}
				}

				if(ctx.hasErrors()){
					contextError.getErrors().putAll(ctx.getErrors());
				}

				ctx.removeKeyFromRootKeyName(rootKeyName);

			}
		}

		List<Run> updateRuns=new ArrayList<>();

		for (Run run:newRuns) {
			ContextValidation contextValidation = ContextValidation.createUndefinedContext(Constants.NGL_DATA_USER);
			logger.debug("Create ReadSet from Run "+run.code);
			createReadSetFromRun(run, contextValidation);

			Run newRun = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, run.code);
			if (!contextValidation.hasErrors()) {
				List<Object> list=new ArrayList<>();
				list.add(newRun);
				try {
					logger.debug("Run Rules from Run "+run.code);
					// new RulesServices().callRules(Play.application().configuration().getString("rules.key"),"F_RG_1",list);
//					new RulesServices().callRules(app.getRulesKey(),"F_RG_1",list);
					new RulesServices().callRules(app.nglConfig().getRulesKey(),"F_RG_1",list);
				} catch (Exception e) {
					contextValidation.addError("rules", e.toString()+ "runCode :"+run.code, run.code);
				}
				workflows.nextState(contextValidation, newRun);
			}

			if (!contextValidation.hasErrors()) {
				updateRuns.add(run);
			} else {
				contextError.getErrors().putAll(contextValidation.getErrors());
			}			

		}		
		//Update Run if lane and readSet are created
		limsServices.updateRunLims(updateRuns,contextError);
	}


	public static State fusionRunStateHistorical(State newState, State oldState) {
		if (newState != null && oldState != null && newState.historical != null && oldState.historical != null) {
			newState.historical.addAll(oldState.historical);
			//sort by date
			
			Collections.sort(new ArrayList<>(newState.historical), new Comparator<TransientState>() {
				@Override
				public int compare(TransientState state1, TransientState state2) {
					return state1.date.compareTo(state2.date);
				}				
			});
			
			//re-compute index
			int index = 0;
			for (TransientState ts : newState.historical) {
				ts.index = index++;
			}
		}
		
		return newState;
	}

	public static Run createLaneFromRun(Run newRun,ContextValidation contextError) throws SQLException {
//		logger.debug("Create Lanes from Run "+newRun.code);
		logger.debug("Create Lanes from Run {}", newRun.code);
		List<Lane> lanes = limsServices.findLanesToCreateFromRun(newRun, contextError);
		// Save TreatmentLane
//		ContextValidation contextErrorValidation = new ContextValidation(Constants.NGL_DATA_USER);
//		contextErrorValidation.setCreationMode();
		ContextValidation contextErrorValidation = ContextValidation.createCreationContext(Constants.NGL_DATA_USER);
		contextErrorValidation.addKeyToRootKeyName(contextError.getRootKeyName());
		contextErrorValidation.putObject("run", newRun);

		// Pas de lanes dans le run alors creation
		if (newRun.lanes == null) {

//			contextErrorValidation.setCreationMode();
//			LaneValidationHelper.validationLanes(lanes, contextErrorValidation);
			LaneValidationHelper.validateLanes(contextErrorValidation, newRun, lanes);

			if (!contextErrorValidation.hasErrors()) {
				for(Lane lane : lanes){

					MongoDBDAO.update(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, 
							DBQuery.is("code", newRun.code),
							DBUpdate.push("lanes", lane));

					logger.debug("Save new Lanes "+lane.number+"from Run "+newRun.code);
				}
			}
			//creation traitements ngsrg
		} else {
			for (Lane lane : lanes) {
				for (String treatmentKey : lane.treatments.keySet()) {
					if (treatmentKey.equals("ngsrg")) {
						Treatment treatment = lane.treatments.get("ngsrg");
//						contextErrorValidation.setCreationMode();
						contextErrorValidation.putObject("level", Level.CODE.Lane);
						contextErrorValidation.putObject("lane", lane);
//						treatment.validate(contextErrorValidation);
						treatment.validate(contextErrorValidation, newRun, lane);
						if (!contextErrorValidation.hasErrors()) {
							MongoDBDAO.update(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, 
									DBQuery.and(DBQuery.is("code", newRun.code), DBQuery.is("lanes.number", lane.number)),
									DBUpdate.set("lanes.$.treatments."+treatment.code, treatment));
							logger.debug("Save new treatment ngsrg lanes "+lane.number+"from run "+newRun.code);
						}
					}
				}
			}
		}

		if (contextErrorValidation.hasErrors()) {
			contextError.getErrors().putAll(contextErrorValidation.getErrors());
			return null;
		} else {
			return newRun;
		}
	}

	public static void createFileFromReadSet(ReadSet readSet, ContextValidation ctxVal) throws SQLException {
		List<File> files = limsServices.findFileToCreateFromReadSet(readSet,ctxVal);
		String rootKeyName = null;
		for (File file:files) {
			rootKeyName = "file[" + file.fullname + "]";

			ctxVal.addKeyToRootKeyName(rootKeyName);
			ctxVal.putObject("readSet", readSet);
			ctxVal.setCreationMode();
//			file.validate(ctxVal);
			file.validate(ctxVal, readSet);
			if (!ctxVal.hasErrors()) {
				MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, 
						DBQuery.is("code", readSet.code),
						DBUpdate.push("files", file)); 
			} 
			ctxVal.removeKeyFromRootKeyName(rootKeyName);

		}
	}

	public static List<ReadSet> createReadSetFromRun(Run run,ContextValidation contextValidation)throws SQLException, DAOException {

		List<ReadSet> newReadSets = new ArrayList<>();

		// Delete old readSet from run
		if (MongoDBDAO.checkObjectExist(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("runCode", run.code))) {
			MongoDBDAO.delete(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("runCode", run.code));
		}

		List<ReadSet> readSets=limsServices.findReadSetToCreateFromRun(run,contextValidation);

		if (!contextValidation.hasErrors() && readSets.size() != 0) {
			for (ReadSet readSet:readSets) {
				String rootKeyName="readSet["+readSet.code+"]";
				contextValidation.addKeyToRootKeyName(rootKeyName);

				if (!MongoDBDAO.checkObjectExistByCode(InstanceConstants.SAMPLE_COLL_NAME,Sample.class,readSet.sampleCode)) {

					Sample sample =limsServices.findSampleToCreate(contextValidation, readSet.sampleCode);
					if (sample != null) {
						InstanceHelpers.save(contextValidation,InstanceConstants.SAMPLE_COLL_NAME,sample,true);
					}
				}

				InstanceHelpers.save(contextValidation, InstanceConstants.READSET_ILLUMINA_COLL_NAME, readSet,true);

				if(!contextValidation.hasErrors()){

					MongoDBDAO.update(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class
							,DBQuery.is("code", run.code).and(DBQuery.is("lanes.number",readSet.laneNumber))
							,DBUpdate.addToSet("lanes.$.readSetCodes", readSet.code));

					MongoDBDAO.update(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class
							,DBQuery.is("code", run.code)
							,DBUpdate.addToSet("projectCodes", readSet.projectCode).addToSet("sampleCodes", readSet.sampleCode));

					createFileFromReadSet(readSet,contextValidation);

					SampleOnContainer sampleOnContainer = InstanceHelpers.getSampleOnContainer(readSet);
					if(null != sampleOnContainer){
						MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME,  ReadSet.class, 
								DBQuery.is("code", readSet.code), DBUpdate.set("sampleOnContainer", sampleOnContainer));
					}else{
						contextValidation.addError( "sampleOneContainer", "error.codeNotExist");
					}

					if(!contextValidation.hasErrors()){
						newReadSets.add(readSet);
					}
				}


				contextValidation.removeKeyFromRootKeyName(rootKeyName);

			}
		}
		return newReadSets;
	}

}
