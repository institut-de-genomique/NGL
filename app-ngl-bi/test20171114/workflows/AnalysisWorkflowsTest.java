package workflows;

import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.callAction;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.status;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TBoolean;
import models.laboratory.run.instance.Analysis;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import models.utils.InstanceConstants;
import play.Logger;
import play.mvc.Result;
import utils.AbstractTestsCNS;
import utils.RunMockHelper;

public class AnalysisWorkflowsTest extends AbstractTestsCNS{

	static Analysis analysis;
	
	@BeforeClass
	public static void initData()
	{
		analysis = MongoDBDAO.find("ngl_bi.Analysis_dataWF", Analysis.class).toList().get(0);
		MongoDBDAO.save(InstanceConstants.ANALYSIS_COLL_NAME, analysis);
		
		for(String codeReadSet : analysis.masterReadSetCodes){
			ReadSet readSet = MongoDBDAO.findByCode("ngl_bi.ReadSetIllumina_dataWF", ReadSet.class, codeReadSet);
			MongoDBDAO.save(InstanceConstants.READSET_ILLUMINA_COLL_NAME, readSet);
		}
	}
	
	@AfterClass
	public static void deleteData()
	{
		analysis = MongoDBDAO.findByCode(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, analysis.code);
		MongoDBDAO.delete(InstanceConstants.ANALYSIS_COLL_NAME, analysis);
		for(String codeReadSet : analysis.masterReadSetCodes){
			ReadSet readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, codeReadSet);
			MongoDBDAO.delete(InstanceConstants.READSET_ILLUMINA_COLL_NAME, readSet);
		}
	}
	
	@Test
	public void setStateIPBA()
	{
		Logger.debug("setStateIPS");
		State state = new State("IP-BA","bot");
		Result r = callAction(controllers.analyses.api.routes.ref.Analyses.state(analysis.code),fakeRequest().withJsonBody(RunMockHelper.getJsonState(state)).withHeader("User-Agent", "bot"));
		assertThat(status(r)).isEqualTo(OK);
	
		analysis = MongoDBDAO.findByCode(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, analysis.code);
		Logger.debug("state code"+analysis.state.code);
		assertThat(analysis.state.code).isEqualTo("IP-BA");
		for(String codeReadSet : analysis.masterReadSetCodes){
			ReadSet readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, codeReadSet);
			assertThat(readSet.state.code).isEqualTo("IP-BA");
		}
		
	}
	
	@Test
	public void setStateFBA()
	{
		analysis = MongoDBDAO.findByCode(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, analysis.code);
		analysis.valuation.valid=TBoolean.TRUE;
		MongoDBDAO.save(InstanceConstants.ANALYSIS_COLL_NAME, analysis);
		
		Logger.debug("setStateFBA");
		State state = new State("F-BA","bot");
		Result r = callAction(controllers.analyses.api.routes.ref.Analyses.state(analysis.code),fakeRequest().withJsonBody(RunMockHelper.getJsonState(state)).withHeader("User-Agent", "bot"));
		assertThat(status(r)).isEqualTo(OK);
	
		analysis = MongoDBDAO.findByCode(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, analysis.code);
		Logger.debug("state code"+analysis.state.code);
		assertThat(analysis.state.code).isEqualTo("F-V");
		for(String codeReadSet : analysis.masterReadSetCodes){
			ReadSet readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, codeReadSet);
			assertThat(readSet.state.code).isEqualTo("IP-BA");
		}
		
	}
	
	@Test
	public void setStateIWV()
	{
		analysis = MongoDBDAO.findByCode(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, analysis.code);
		analysis.valuation.valid=TBoolean.UNSET;
		MongoDBDAO.save(InstanceConstants.ANALYSIS_COLL_NAME, analysis);
		
		Logger.debug("setStateFBA");
		State state = new State("IW-V","bot");
		Result r = callAction(controllers.analyses.api.routes.ref.Analyses.state(analysis.code),fakeRequest().withJsonBody(RunMockHelper.getJsonState(state)).withHeader("User-Agent", "bot"));
		assertThat(status(r)).isEqualTo(OK);
	
		analysis = MongoDBDAO.findByCode(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, analysis.code);
		Logger.debug("state code"+analysis.state.code);
		assertThat(analysis.state.code).isEqualTo("IW-V");
		for(String codeReadSet : analysis.masterReadSetCodes){
			ReadSet readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, codeReadSet);
			assertThat(readSet.state.code).isEqualTo("IP-BA");
		}
		
	}
	
	@Test
	public void setStateFV()
	{
		analysis = MongoDBDAO.findByCode(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, analysis.code);
		analysis.valuation.valid=TBoolean.UNSET;
		MongoDBDAO.save(InstanceConstants.ANALYSIS_COLL_NAME, analysis);
		
		Logger.debug("setStateFBA");
		State state = new State("IW-V","bot");
		Result r = callAction(controllers.analyses.api.routes.ref.Analyses.state(analysis.code),fakeRequest().withJsonBody(RunMockHelper.getJsonState(state)).withHeader("User-Agent", "bot"));
		assertThat(status(r)).isEqualTo(OK);
	
		analysis = MongoDBDAO.findByCode(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, analysis.code);
		Logger.debug("state code"+analysis.state.code);
		assertThat(analysis.state.code).isEqualTo("IW-V");
		for(String codeReadSet : analysis.masterReadSetCodes){
			ReadSet readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, codeReadSet);
			assertThat(readSet.state.code).isEqualTo("IP-BA");
		}
		
	}
}
