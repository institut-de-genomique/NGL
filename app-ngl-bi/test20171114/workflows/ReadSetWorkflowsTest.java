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

import com.mongodb.BasicDBObject;

import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TBoolean;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.Content;
import models.laboratory.project.instance.Project;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import play.Logger;
import play.mvc.Result;
import utils.AbstractTestsCNS;
import utils.RunMockHelper;

public class ReadSetWorkflowsTest extends AbstractTestsCNS{

	static ReadSet readSet;
	static Run run;
	static Container container;
	static Project project;
	
	@BeforeClass
	public static void initData()
	{
		readSet = MongoDBDAO.find("ngl_bi.ReadSetIllumina_dataWF", ReadSet.class, DBQuery.is("state.code","N"), getReadSetKeys()).limit(1).toList().get(0);
		readSet.sampleOnContainer=null;
		readSet.dispatch=false;
		readSet.productionValuation.valid=TBoolean.UNSET;
		
		MongoDBDAO.save(InstanceConstants.READSET_ILLUMINA_COLL_NAME, readSet);
		
		
		//get container 
		run = MongoDBDAO
				.findOne("ngl_bi.RunIllumina_dataWF", Run.class, DBQuery.is("code", readSet.runCode));
		MongoDBDAO.save(InstanceConstants.RUN_ILLUMINA_COLL_NAME, run);
		
		String containerSupportCode = run.containerSupportCode;
		container = MongoDBDAO.findOne("ngl_sq.Container_dataWF",Container.class,
				DBQuery.and(DBQuery.is("support.code", containerSupportCode),
						DBQuery.is("support.line", readSet.laneNumber.toString()),
						DBQuery.in("sampleCodes", readSet.sampleCode)));
		MongoDBDAO.save(InstanceConstants.CONTAINER_COLL_NAME, container);
		
		for(Content content : container.contents){
			Sample sample = MongoDBDAO.findByCode("ngl_bq.Sample_dataWF", Sample.class, content.sampleCode);
			MongoDBDAO.save(InstanceConstants.SAMPLE_COLL_NAME, sample);
		}
		
		project = MongoDBDAO.findByCode("ngl_project.Project_dataWF", Project.class, readSet.projectCode);
		project.bioinformaticParameters.biologicalAnalysis=false;
		project.bioinformaticParameters.fgGroup=null;
		MongoDBDAO.save(InstanceConstants.PROJECT_COLL_NAME, project);
	}
	
	@AfterClass
	public static void deleteData()
	{
		//get readSet to test
		readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
		MongoDBDAO.delete(InstanceConstants.READSET_ILLUMINA_COLL_NAME, readSet);
		run = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, run.code);
		MongoDBDAO.delete(InstanceConstants.RUN_ILLUMINA_COLL_NAME, run);
		container = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class, container.code);
		MongoDBDAO.delete(InstanceConstants.CONTAINER_COLL_NAME, container);
		for(Content content : container.contents){
			Sample sample = MongoDBDAO.findByCode("ngl_bq.Sample_dataWF", Sample.class, content.sampleCode);
			MongoDBDAO.delete(InstanceConstants.SAMPLE_COLL_NAME, sample);
			
		}
		project = MongoDBDAO.findByCode(InstanceConstants.PROJECT_COLL_NAME, Project.class, readSet.projectCode);
		MongoDBDAO.delete(InstanceConstants.PROJECT_COLL_NAME, project);
	}
	
	
	@Test
	public void setStateIPRG()
	{
		Logger.debug("setStateIPRG");
		State state = new State("IP-RG","bot");
		Result r = callAction(controllers.readsets.api.routes.ref.ReadSets.state(readSet.code),fakeRequest().withJsonBody(RunMockHelper.getJsonState(state)).withHeader("User-Agent", "bot"));
		assertThat(status(r)).isEqualTo(OK);
	
		readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
		Logger.debug("state code"+readSet.state.code);
		assertThat(readSet.state.code).isEqualTo("IP-RG");
		
		//Check sampleOnContainer
		assertThat(readSet.sampleOnContainer).isNotNull();
	}
	
	@Test
	public void setStateFRG()
	{
		Logger.debug("setStateFRG");
		State state = new State("F-RG","bot");
		Result r = callAction(controllers.readsets.api.routes.ref.ReadSets.state(readSet.code),fakeRequest().withJsonBody(RunMockHelper.getJsonState(state)).withHeader("User-Agent", "bot"));
		assertThat(status(r)).isEqualTo(OK);
	
		readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
		Logger.debug("state code"+readSet.state.code);
		assertThat(readSet.state.code).isEqualTo("IW-QC");
		//check dispatch
		assertThat(readSet.dispatch).isEqualTo(true);
		
		
	}
	
	@Test
	public void setStateFQC()
	{
		readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
		readSet.productionValuation.valid=TBoolean.UNSET;
		MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, readSet);
		Logger.debug("setStateFQC");
		State state = new State("F-QC","bot");
		Result r = callAction(controllers.readsets.api.routes.ref.ReadSets.state(readSet.code),fakeRequest().withJsonBody(RunMockHelper.getJsonState(state)).withHeader("User-Agent", "bot"));
		assertThat(status(r)).isEqualTo(OK);
	
		readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
		Logger.debug("state code"+readSet.state.code);
		assertThat(readSet.state.code).isEqualTo("IW-VQC");
	}
	
	@Test
	public void setStateFQCA()
	{
		readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
		readSet.productionValuation.valid=TBoolean.TRUE;
		MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, readSet);
		
		Logger.debug("setStateFQC to A");
		State state = new State("F-QC","bot");
		Result r = callAction(controllers.readsets.api.routes.ref.ReadSets.state(readSet.code),fakeRequest().withJsonBody(RunMockHelper.getJsonState(state)).withHeader("User-Agent", "bot"));
		assertThat(status(r)).isEqualTo(OK);
	
		readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
		Logger.debug("state code"+readSet.state.code);
		assertThat(readSet.state.code).isEqualTo("A");
	}
	
	@Test
	public void setStateIWBA()
	{
		
		Logger.debug("setState IWBA");
		State state = new State("IW-BA","bot");
		Result r = callAction(controllers.readsets.api.routes.ref.ReadSets.state(readSet.code),fakeRequest().withJsonBody(RunMockHelper.getJsonState(state)).withHeader("User-Agent", "bot"));
		assertThat(status(r)).isEqualTo(OK);
	
		readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
		Logger.debug("state code"+readSet.state.code);
		assertThat(readSet.state.code).isEqualTo("IW-BA");
	}
	
	@Test
	public void setStateIPVQC()
	{
		readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
		readSet.productionValuation.valid=TBoolean.UNSET;
		MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, readSet);
		Logger.debug("setState IPVQC");
		State state = new State("IP-VQC","bot");
		Result r = callAction(controllers.readsets.api.routes.ref.ReadSets.state(readSet.code),fakeRequest().withJsonBody(RunMockHelper.getJsonState(state)).withHeader("User-Agent", "bot"));
		assertThat(status(r)).isEqualTo(OK);
	
		readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
		Logger.debug("state code"+readSet.state.code);
		assertThat(readSet.state.code).isEqualTo("IP-VQC");
	}
	
	@Test
	public void setStateIPVQCA()
	{
		readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
		readSet.productionValuation.valid=TBoolean.TRUE;
		readSet.bioinformaticValuation.valid=TBoolean.TRUE;
		
		MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, readSet);
		Logger.debug("setState IPVQC");
		State state = new State("IP-VQC","bot");
		Result r = callAction(controllers.readsets.api.routes.ref.ReadSets.state(readSet.code),fakeRequest().withJsonBody(RunMockHelper.getJsonState(state)).withHeader("User-Agent", "bot"));
		assertThat(status(r)).isEqualTo(OK);
	
		readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
		Logger.debug("state code"+readSet.state.code);
		assertThat(readSet.state.code).isEqualTo("A");
	}
	
	@Test
	public void setStateIPVQCUA()
	{
		readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
		readSet.productionValuation.valid=TBoolean.TRUE;
		readSet.bioinformaticValuation.valid=TBoolean.FALSE;
		
		MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, readSet);
		Logger.debug("setState IPVQC");
		State state = new State("IP-VQC","bot");
		Result r = callAction(controllers.readsets.api.routes.ref.ReadSets.state(readSet.code),fakeRequest().withJsonBody(RunMockHelper.getJsonState(state)).withHeader("User-Agent", "bot"));
		assertThat(status(r)).isEqualTo(OK);
	
		readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
		Logger.debug("state code"+readSet.state.code);
		assertThat(readSet.state.code).isEqualTo("UA");
	}
	
	@Test
	public void setStateFVQCIWBA()
	{
		project = MongoDBDAO.findByCode(InstanceConstants.PROJECT_COLL_NAME, Project.class, readSet.projectCode);
		project.bioinformaticParameters.biologicalAnalysis=true;
		//project.bioinformaticParameters.regexBiologicalAnalysis="test";
		MongoDBDAO.update(InstanceConstants.PROJECT_COLL_NAME, project);
		
		readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
		readSet.productionValuation.valid=TBoolean.TRUE;
		readSet.bioinformaticValuation.valid=TBoolean.TRUE;
		
		MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, readSet);
		Logger.debug("setState FVQC");
		State state = new State("F-VQC","bot");
		Result r = callAction(controllers.readsets.api.routes.ref.ReadSets.state(readSet.code),fakeRequest().withJsonBody(RunMockHelper.getJsonState(state)).withHeader("User-Agent", "bot"));
		assertThat(status(r)).isEqualTo(OK);
	
		readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
		Logger.debug("state code"+readSet.state.code);
		assertThat(readSet.state.code).isEqualTo("IW-BA");
	}
	
	
	@Test
	public void setStateFBAIWVBA()
	{
		readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
		readSet.productionValuation.valid=TBoolean.TRUE;
		readSet.bioinformaticValuation.valid=TBoolean.UNSET;
		
		MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, readSet);
		Logger.debug("setState FBA");
		State state = new State("F-BA","bot");
		Result r = callAction(controllers.readsets.api.routes.ref.ReadSets.state(readSet.code),fakeRequest().withJsonBody(RunMockHelper.getJsonState(state)).withHeader("User-Agent", "bot"));
		assertThat(status(r)).isEqualTo(OK);
	
		readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
		Logger.debug("state code"+readSet.state.code);
		assertThat(readSet.state.code).isEqualTo("IW-VBA");
	}
	
	@Test
	public void setStateFBAA()
	{
		readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
		readSet.productionValuation.valid=TBoolean.TRUE;
		readSet.bioinformaticValuation.valid=TBoolean.TRUE;
		
		MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, readSet);
		Logger.debug("setState FBA");
		State state = new State("F-BA","bot");
		Result r = callAction(controllers.readsets.api.routes.ref.ReadSets.state(readSet.code),fakeRequest().withJsonBody(RunMockHelper.getJsonState(state)).withHeader("User-Agent", "bot"));
		assertThat(status(r)).isEqualTo(OK);
	
		readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
		Logger.debug("state code"+readSet.state.code);
		assertThat(readSet.state.code).isEqualTo("A");
	}
	
	@Test
	public void setStateFBAUA()
	{
		readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
		readSet.productionValuation.valid=TBoolean.TRUE;
		readSet.bioinformaticValuation.valid=TBoolean.FALSE;
		
		MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, readSet);
		Logger.debug("setState FBA");
		State state = new State("F-BA","bot");
		Result r = callAction(controllers.readsets.api.routes.ref.ReadSets.state(readSet.code),fakeRequest().withJsonBody(RunMockHelper.getJsonState(state)).withHeader("User-Agent", "bot"));
		assertThat(status(r)).isEqualTo(OK);
	
		readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
		Logger.debug("state code"+readSet.state.code);
		assertThat(readSet.state.code).isEqualTo("UA");
	}
	
	private static BasicDBObject getReadSetKeys() {
		BasicDBObject keys = new BasicDBObject();
		keys.put("treatments", 0);
		return keys;
	}
}
