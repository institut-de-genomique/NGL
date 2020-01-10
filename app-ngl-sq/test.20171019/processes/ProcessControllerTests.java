package processes;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.callAction;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.status;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.instance.ContainerSupport;
import models.laboratory.processes.instance.Process;
import models.utils.InstanceConstants;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import play.Logger;
import play.libs.Json;
import play.mvc.Result;
import utils.AbstractTests;
import utils.DatatableResponseForTest;
import utils.InitDataHelper;
import utils.MapperHelper;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.cea.ig.MongoDBDAO;

public class ProcessControllerTests extends AbstractTests {
	
	@Before
	public  void initData() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		InitDataHelper.initForProcessesTest();
	}
	
	@After
	public  void resetData(){
		InitDataHelper.endTest();
	}
	
	@Test
	public void delete(){
		String processCode = "ILLUMINA-RUN-BFB-NULL-2014103112111983";
		Process process = MongoDBDAO.findByCode(InstanceConstants.PROCESS_COLL_NAME, Process.class, processCode);
		Result result = callAction(controllers.processes.api.routes.ref.Processes.delete(process.code),fakeRequest());
		assertThat(status(result)).isEqualTo(play.mvc.Http.Status.OK);
		Process processTest = MongoDBDAO.findByCode(InstanceConstants.PROCESS_COLL_NAME, Process.class, processCode);
		assertThat(processTest).isNull();
		MongoDBDAO.save(InstanceConstants.PROCESS_COLL_NAME, process);

	}
	
	
	@Test
	public void deleteNotPossible(){
		String processCode = "ILLUMINA-RUN-BFB-NULL-2014103112111983";
		MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, Process.class, DBQuery.is("code",processCode), DBUpdate.set("state.code", "IP"));
		Process process;
		Result result = callAction(controllers.processes.api.routes.ref.Processes.delete(processCode),fakeRequest());
		assertThat(status(result)).isEqualTo(play.mvc.Http.Status.BAD_REQUEST);
		process = MongoDBDAO.findByCode(InstanceConstants.PROCESS_COLL_NAME, Process.class, processCode);
		assertThat(process).isNotNull();

	}

	
	//TODO: container avec plusieurs contents (impoter jeu de données)
		@Test
		public void validateSaveFromSupportIlluminaRunProcess() throws JsonParseException, JsonMappingException, IOException{
			ContainerSupport cs =  MongoDBDAO.find(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class, DBQuery.and(DBQuery.regex("code", Pattern.compile("^ASR")),DBQuery.is("state.code", "IW-P"))).toList().get(0);		
			Process process = ProcessTestHelper.getFakeProcess("sequencing", "illumina-run");
			process.projectCodes = cs.projectCodes;
			process.properties = new HashMap<String, PropertyValue>();
			process.properties.put("estimatedPercentPerLane", new PropertySingleValue("50"));
			process.properties.put("readLength", new PropertySingleValue("100"));
			process.properties.put("readType", new PropertySingleValue("PE"));
			process.properties.put("sequencingType", new PropertySingleValue("Hiseq 2500 Rapide"));		
		
			Result result = callAction(controllers.processes.api.routes.ref.Processes.save(),fakeRequest("POST","?fromSupportContainerCode="+cs.code).withJsonBody(Json.toJson(process)));			
			assertThat(status(result)).isEqualTo(play.mvc.Http.Status.OK);
			
			MapperHelper mh = new MapperHelper();		
			List<Process> processesResult = mh.convertValue(mh.resultToJsNode(result), new TypeReference<List<Process>>(){});		
			
			assertThat(processesResult).isNotNull();
			
			for(int i=0; i<processesResult.size();i++){
				Process newProcess=processesResult.get(i);
				Logger.info("code Process: "+ newProcess.code);
				result = callAction(controllers.processes.api.routes.ref.Processes.head(newProcess.code),fakeRequest());
				assertThat(status(result)).isEqualTo(play.mvc.Http.Status.OK);
				
				result = callAction(controllers.processes.api.routes.ref.Processes.get(newProcess.code),fakeRequest());
				assertThat(status(result)).isEqualTo(play.mvc.Http.Status.OK);
				
				result = callAction(controllers.processes.api.routes.ref.Processes.delete(newProcess.code),fakeRequest());
				assertThat(status(result)).isEqualTo(play.mvc.Http.Status.BAD_REQUEST);			
			}		
		}
		
		
		@Test
		public void validateDeleteNotFound(){
			Result result = callAction(controllers.processes.api.routes.ref.Processes.delete("not_found"),fakeRequest());
			assertThat(status(result)).isEqualTo(play.mvc.Http.Status.NOT_FOUND);
		}
		
		@Test
		public void validateQuery() throws JsonParseException, JsonMappingException, IOException{
			Result result = null;
			
			result = callAction(controllers.processes.api.routes.ref.Processes.list(),fakeRequest());
			assertThat(status(result)).isEqualTo(play.mvc.Http.Status.OK);
			
			result = callAction(controllers.processes.api.routes.ref.Processes.list(),fakeRequest("GET","?projectCode=ADI"));
			assertThat(status(result)).isEqualTo(play.mvc.Http.Status.OK);
			
			ObjectMapper mapper=new ObjectMapper();
			List<Process> processes=mapper.readValue(play.test.Helpers.contentAsString(result),new TypeReference<List<Process>>(){});
			
			assertThat(processes.size()).isEqualTo(1);
			assertThat(processes.get(0).projectCodes).contains("ADI");
			
			result = callAction(controllers.processes.api.routes.ref.Processes.list(),fakeRequest("GET","?datatable=true&projectCode=XXX"));
			assertThat(status(result)).isEqualTo(play.mvc.Http.Status.OK);
			mapper=new ObjectMapper();
			JsonNode jsonNode=Json.parse(play.test.Helpers.contentAsString(result));
			DatatableResponseForTest<Process> datatableResponse=mapper.convertValue(jsonNode,new TypeReference<DatatableResponseForTest<Process>>(){});
			assertThat(datatableResponse.data.size()).isEqualTo(0);
			
		}
		
		
		@Test
		public void updateNotFound(){
			Result result = callAction(controllers.processes.api.routes.ref.Processes.update("not_found"),fakeRequest());
			assertThat(status(result)).isEqualTo(play.mvc.Http.Status.NOT_FOUND);
		}
		
		@Test
		public void validateUpdateCode(){
			//TODO
		}
		
		@Test
		public void validateUpdateStateCode(){
			//TODO
		}

}
