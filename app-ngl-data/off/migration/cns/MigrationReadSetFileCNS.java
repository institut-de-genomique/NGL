package controllers.migration.cns;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import models.Constants;
import models.LimsCNSDAO;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TBoolean;
import models.laboratory.common.instance.Valuation;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import models.util.DataMappingCNS;
// import models.util.Workflows;
import models.utils.InstanceConstants;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;
// import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;

// import play.Logger;
// import play.api.modules.spring.Spring;
import play.mvc.Result;
// import services.instance.run.UpdateReadSetCNS;
import validation.ContextValidation;
// import workflows.readset.ReadSetWorkflows;
import workflows.run.RunWorkflows;
import controllers.CommonController;
import fr.cea.ig.MongoDBDAO;

public class MigrationReadSetFileCNS extends CommonController {

	private static final play.Logger.ALogger logger = play.Logger.of(MigrationReadSetFileCNS.class);
	
	private static final String RUN_COLL_NAME_BCK     = InstanceConstants.RUN_ILLUMINA_COLL_NAME     + "_BCK_VALUATION";
	private static final String READSET_COLL_NAME_BCK = InstanceConstants.READSET_ILLUMINA_COLL_NAME + "_BCK_VALUATION";

	protected static LimsCNSDAO  limsServices = play.api.modules.spring.Spring.getBeanOfType(LimsCNSDAO.class);
	
	// final static RunWorkflows workflows = Spring.get BeanOfType(RunWorkflows.class); 
	private final RunWorkflows workflows;

	@Inject
	public MigrationReadSetFileCNS(RunWorkflows workflows) {
		this.workflows = workflows;
	}

	/*	public static Result updateFileReadSet(String readSetCode){
		ContextValidation ctxVal=new ContextValidation();

		ReadSet readSet=MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME,ReadSet.class,readSetCode);

		Logger.debug("ReadSet "+readSet.code);
		List<File> files;
		try {
			files = limsServices.findFileToCreateFromReadSet(readSet,ctxVal);
		} catch (SQLException e) {
			throw new RuntimeException();
		}
		String rootKeyName=null;
		ctxVal.putObject("readSet", readSet);
		ctxVal.setUpdateMode();
		for(File file:files){
			rootKeyName="file["+file.fullname+"]";
			ctxVal.addKeyToRootKeyName(rootKeyName);
			file.validate(ctxVal);
			ctxVal.removeKeyFromRootKeyName(rootKeyName);

		}
		if (!ctxVal.hasErrors()) {
			MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, 
					DBQuery.is("code", readSet.code),
					DBUpdate.unset("files"));
			MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, 
					DBQuery.is("code", readSet.code),
					DBUpdate.pushAll("files", files)); 
		} else{
			return badRequest(ctxVal.errors.toString());
		}


		return ok("Readset Files "+readSetCode+" update");
	}
	 */

	public Result migration(){

		ContextValidation ctxVal=new ContextValidation(Constants.NGL_DATA_USER);
		List<Run> containersCollBck = MongoDBDAO.find(RUN_COLL_NAME_BCK, Run.class).toList();
		if(containersCollBck.size() == 0){

			logger.info(">>>>>>>>>>> Update Run/Lane/ReadSet/File starts");
			backupCollections();

			List<Run> runs=MongoDBDAO.find(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class).toList();
			logger.info("Nb Run : "+runs.size());
			int i = 0;
			for(Run run:runs){
				i++;
				updateRunPiste(run,ctxVal);
				//UpdateReadSetCNS.updateReadSet(run, ctxVal);
				if((i % 100) == 0){
					logger.info("Nb Run Traité : "+i);
				}
			}

			logger.info(">>>>>>>>>>> Update Run/Lane/ReadSet/File finish");
			if(!ctxVal.hasErrors()){
				return ok("Update Run/Piste/ReadSet/Files ok");
			}else {
				return badRequest(ctxVal.errors.toString());
			}
		}
		else {
			return badRequest("Migration deja faite !");
		}
	}
	
	private static void backupCollections() {
		logger.info("\tCopie "+InstanceConstants.CONTAINER_COLL_NAME+" start");
		MongoDBDAO.save(READSET_COLL_NAME_BCK, MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class).toList());
		MongoDBDAO.save(RUN_COLL_NAME_BCK, MongoDBDAO.find(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class).toList());
		logger.info("\tCopie "+InstanceConstants.CONTAINER_COLL_NAME+" end");
	}

	public void updateRunPiste(Run run,ContextValidation contextValidation){
		class RunPisteValuation {
			public String runCode;
			public Integer laneNumero;			
			public Valuation valuationRun;
			public Valuation valudationLane;
			public String stateCode;
		}

		String sql="select runCode=r.runhnom, laneNumero=p.pistnum " +
				", validationValidRun=case when (select count(*) from Lotsequence where lseqval=2 and Lotsequence.runhco=r.runhco)>0 then  'UNSET' " +
				"				when runhabandon=1 then 'FALSE' else 'TRUE' end" +
				",validationDateRun=isnull(runhdaban,convert(smalldatetime,'01/01/2000',103))"+
				",validationValidPiste=case when (select count(*) from Lotsequence where lseqval=2 and Lotsequence.runhco=r.runhco)>0 then  'UNSET' when runhabandon=1 then 'FALSE' else 'TRUE' end"+
				", validationDatePiste=isnull(runhdaban,convert(smalldatetime,'01/01/2000',103))"+
				" from Runhd r, Piste p where p.runhco=r.runhco and r.runhnom='"+run.code+"'";


		//Logger.debug("SQL "+sql);
		List<RunPisteValuation> results = limsServices.jdbcTemplate.query(sql,new Object[]{} 
		,new RowMapper<RunPisteValuation>() {

			@SuppressWarnings("rawtypes")
			public RunPisteValuation mapRow(ResultSet rs, int rowNum) throws SQLException {
				RunPisteValuation runPisteValuation = new RunPisteValuation();	
				runPisteValuation.runCode=rs.getString("runCode");
				runPisteValuation.laneNumero=rs.getInt("laneNumero");
				runPisteValuation.valuationRun=new Valuation();
				runPisteValuation.valuationRun.valid=TBoolean.valueOf(rs.getString("validationValidRun"));
				runPisteValuation.valuationRun.user="lims";
				runPisteValuation.valuationRun.date=rs.getDate("validationDateRun");
				runPisteValuation.stateCode = DataMappingCNS.getStateRunFromLims(runPisteValuation.valuationRun.valid);
				runPisteValuation.valudationLane=new Valuation();
				runPisteValuation.valudationLane.valid=TBoolean.valueOf(rs.getString("validationValidPiste"));
				runPisteValuation.valudationLane.user="lims";
				runPisteValuation.valudationLane.date=rs.getDate("validationDatePiste");
				return runPisteValuation;
			}
		});
		//first update lane
		for(RunPisteValuation runPiste:results){
			MongoDBDAO.update(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, 
					DBQuery.is("code",runPiste.runCode).is("lanes.number", runPiste.laneNumero)
					, DBUpdate.set("lanes.$.valuation",runPiste.valudationLane));			
		}
		
		//then update run
		if(results.size() > 0){
			RunPisteValuation runPiste = results.get(0);
			State state = run.state;
			state.code = runPiste.stateCode;
			state.date = new Date();
			state.user = "lims";
			
			MongoDBDAO.update(InstanceConstants.RUN_ILLUMINA_COLL_NAME,Run.class
					, DBQuery.is("code",runPiste.runCode)
					, DBUpdate.set("valuation",runPiste.valuationRun).set("state",state));
			run = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME,Run.class,runPiste.runCode);
			workflows.nextState(contextValidation, run);
		}
	}

}

