package processes;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.callAction;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.status;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.ContainerSupport;
import models.laboratory.processes.instance.Process;
import models.laboratory.processes.instance.SampleOnInputContainer;
import models.utils.CodeHelper;
import models.utils.InstanceConstants;
import models.utils.instance.ProcessHelper;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mongojack.DBQuery;

import play.Logger;
import play.Logger.ALogger;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import utils.AbstractTests;
import utils.Constants;
import utils.DatatableResponseForTest;
import utils.InitDataHelper;
import utils.MapperHelper;
import validation.ContextValidation;


import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import controllers.processes.api.ProcessesSaveQueryForm;
import controllers.processes.api.ProcessesSearchForm;
import controllers.processes.api.ProcessesUpdateForm;
import fr.cea.ig.MongoDBDAO;

public class ProcessesTest extends AbstractTests{
		
	@BeforeClass
	public static void initData() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		InitDataHelper.initForProcessesTest();
	}
	
	@AfterClass
	public static void resetData(){
		InitDataHelper.endTest();
	}
	
	protected static ALogger logger=Logger.of("ProcessesTest");
	
	
/*	@Test
	public void validatesaveFromSupportOpgenRun() throws JsonParseException, JsonMappingException, IOException{
		String supportCode = InitDataHelper.getSupportCodesInContext("tube").get(0);		
		ContainerSupport cs = MongoDBDAO.findOne(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class, DBQuery.is("code", supportCode));		
		cs.state.code="IW-P";
		Process process = ProcessTestHelper.getFakeProcess("mapping", "opgen-run");
		process.projectCode = cs.projectCodes.get(0);
		process.sampleCode = cs.sampleCodes.get(0);
		process.properties = new HashMap<String, PropertyValue>();	
		ProcessesSaveQueryForm psf = ProcessTestHelper.getFakeProcessesSaveForm(supportCode, process);
		//Process process = ProcessTestHelper.getFakeProcess(psf.categoryCode, psf.typeCode);
		
			
		Logger.info("Avant Result  validatesaveFromSupportOpgenRun()");
		Result result = callAction(controllers.processes.api.routes.ref.Processes.save(),fakeRequest().withJsonBody(Json.toJson(psf)));
		assertThat(status(result)).isEqualTo(play.mvc.Http.Status.OK);		
		Logger.info("Après Result  validatesaveFromSupportOpgenRun()");
		
		MapperHelper mh = new MapperHelper();		
		List<Process> processResult = mh.convertValue(mh.resultToJsNode(result), new TypeReference<List<Process>>(){});		
		
		assertThat(processResult).isNotNull();
		
		for(int i=0; i<processResult.size();i++){
		
		Process newProcess=processResult.get(i);
		
			
		result = callAction(controllers.processes.api.routes.ref.Processes.head(processResult.get(i).code),fakeRequest());
		assertThat(status(result)).isEqualTo(play.mvc.Http.Status.OK);
		
		result = callAction(controllers.processes.api.routes.ref.Processes.get(processResult.get(i).code),fakeRequest());
		assertThat(status(result)).isEqualTo(play.mvc.Http.Status.OK);

		
		result = callAction(controllers.processes.api.routes.ref.Processes.delete(processResult.get(i).code),fakeRequest());
		assertThat(status(result)).isEqualTo(play.mvc.Http.Status.OK);
		}
		
		
	}*/
	
	@Test
	public void validateApplyRules(){
		List<Process> processes = new ArrayList<Process>();
		Process process = ProcessTestHelper.getFakeProcess("sequencing", "illumina-run");	
		
		process.properties = new HashMap<String, PropertyValue>();
		Double estimatedPercentValue = 33.0;
		process.properties.put("estimatedPercentPerLane", new PropertySingleValue(estimatedPercentValue));
		process.sampleOnInputContainer = new SampleOnInputContainer();
		process.sampleOnInputContainer.percentage = 25.0 ;
		process.properties.put("sequencingType", new PropertySingleValue("Miseq"));
		process.properties.put("readType", new PropertySingleValue("PE"));
		process.properties.put("readLength", new PropertySingleValue("300"));
		String supportCode = InitDataHelper.getSupportCodesInContext("flowcell-8").get(0);
		ContainerSupport cs = MongoDBDAO.findOne(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class, DBQuery.is("code", supportCode));	
		cs.state.code="IW-P";
		process.projectCodes = cs.projectCodes;
		process.sampleCodes = cs.sampleCodes;
		
		process.code = CodeHelper.getInstance().generateProcessCode(process);
		Process pro1 = MongoDBDAO.save(InstanceConstants.PROCESS_COLL_NAME, process);
		processes.add(pro1);
		
		process.code = CodeHelper.getInstance().generateProcessCode(process);
		Process pro2 = MongoDBDAO.save(InstanceConstants.PROCESS_COLL_NAME, process);
		processes.add(pro2);
		
		process.code = CodeHelper.getInstance().generateProcessCode(process);
		Process pro3 = MongoDBDAO.save(InstanceConstants.PROCESS_COLL_NAME, process);
		processes.add(pro3);
		
		process.code = CodeHelper.getInstance().generateProcessCode(process);
		Process pro4 = MongoDBDAO.save(InstanceConstants.PROCESS_COLL_NAME, process);
		processes.add(pro4);
		
		
		
		ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
		contextValidation.setCreationMode();
		
		Double newValue = roundValue(process.sampleOnInputContainer.percentage*estimatedPercentValue/100.0);		
			
		Logger.info("Test validateApplyRules begin............");
		ProcessHelper.applyRules(processes, contextValidation,"processCreation");
		Logger.info(".............Test validateApplyRules end!");
		
		assertThat(pro1.properties.get("estimatedPercentPerLane").value).isEqualTo(newValue);
		assertThat(pro2.properties.get("estimatedPercentPerLane").value).isEqualTo(newValue);
		assertThat(pro3.properties.get("estimatedPercentPerLane").value).isEqualTo(newValue);
		assertThat(pro4.properties.get("estimatedPercentPerLane").value).isEqualTo(newValue);
		
		Process p = MongoDBDAO.findByCode(InstanceConstants.PROCESS_COLL_NAME, Process.class, pro1.code);
		assertThat(p.properties.get("estimatedPercentPerLane").value).isEqualTo(newValue);	
		
		p = MongoDBDAO.findByCode(InstanceConstants.PROCESS_COLL_NAME, Process.class, pro2.code);
		assertThat(p.properties.get("estimatedPercentPerLane").value).isEqualTo(newValue);
		
		p = MongoDBDAO.findByCode(InstanceConstants.PROCESS_COLL_NAME, Process.class, pro3.code);
		assertThat(p.properties.get("estimatedPercentPerLane").value).isEqualTo(newValue);
		
		p = MongoDBDAO.findByCode(InstanceConstants.PROCESS_COLL_NAME, Process.class, pro3.code);
		assertThat(p.properties.get("estimatedPercentPerLane").value).isEqualTo(newValue);
		
		
	}
	
	
	
		
	private Double roundValue(Double value)
	{
		BigDecimal bg = new BigDecimal(value.toString()).setScale(2, RoundingMode.HALF_UP); 
		return bg.doubleValue();
	}
}
