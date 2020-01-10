package experiments;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.callAction;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.status;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.instance.Container;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.experiment.instance.InputContainerUsed;
import models.laboratory.instrument.instance.InstrumentUsed;
import models.laboratory.processes.instance.Process;
import models.utils.InstanceConstants;
import models.utils.instance.ExperimentHelper;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mongojack.DBQuery;

import play.Logger;
import play.libs.Json;
import play.mvc.Result;
import utils.AbstractTests;
import utils.InitDataHelper;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.cea.ig.MongoDBDAO;

public class ExperimentControllerTests extends AbstractTests {
	
	@BeforeClass
	public static void initData() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		InitDataHelper.initForProcessesTest();
	}

	@AfterClass	
	public static  void resetData(){
		InitDataHelper.endTest();
	}
	
	
	@Test
	public void saveManyToOneExperiment() throws JsonParseException, JsonMappingException, IOException{
		
		List<Experiment> exps = MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME+"_new", Experiment.class,DBQuery.is("typeCode", "prepa-flowcell")).toList();
		Experiment exp=exps.get(0);
		exp._id=null;
		Result result = callAction(controllers.experiments.api.routes.ref.Experiments.save(),fakeRequest().withJsonBody(Json.toJson(exp)));
		assertThat(status(result)).isEqualTo(play.mvc.Http.Status.OK);

		ObjectMapper mapper=new ObjectMapper();

		exp=mapper.readValue(play.test.Helpers.contentAsString(result),Experiment.class);
		assertThat(exp.state.code).isEqualTo("N");
		assertThat(exp.projectCodes).isNotNull();
		assertThat(exp.sampleCodes).isNotNull();
		assertThat(exp.inputContainerSupportCodes).isNotNull();
		assertThat(exp.outputContainerSupportCodes).isNull();
		//Valide process = "IP", InputContainer ="IW-E"
		List<InputContainerUsed> containersUsed=ExperimentHelper.getAllInputContainers(exp);
		for(InputContainerUsed containerUsed:containersUsed){
			Container container=MongoDBDAO.findByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class, containerUsed.code);
			assertThat(container.state.code).isEqualTo("IW-E");
			List<Process> processes=MongoDBDAO.find(InstanceConstants.PROCESS_COLL_NAME, Process.class, DBQuery.in("code", container.processCodes)).toList();
			for(Process process:processes){
				assertThat(process.state.code).isEqualTo("N");
			}
		}
		
		
	}
	
	
	
	
	
	
}
