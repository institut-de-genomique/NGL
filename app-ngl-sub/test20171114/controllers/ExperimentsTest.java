package controllers;

import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.callAction;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.contentType;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.status;

import java.util.Arrays;
import java.util.Date;

import models.laboratory.project.instance.Project;
import models.sra.submit.sra.instance.Experiment;
import models.utils.InstanceConstants;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


//import play.Logger;
import play.libs.Json;
import play.mvc.Result;
import utils.AbstractTestController;
import builder.data.ExperimentBuilder;
import builder.data.ProjectBuilder;
import builder.data.ReadSpecBuilder;
import builder.data.RunBuilder;
import builder.data.StateBuilder;
import builder.data.TraceInformationBuilder;
import controllers.sra.configurations.api.Configurations;
import controllers.sra.experiments.api.ExperimentsSearchForm;
import fr.cea.ig.MongoDBDAO;

public class ExperimentsTest extends AbstractTestController{
	private static final play.Logger.ALogger logger = play.Logger.of(ExperimentsTest.class);

	private static final String experimentCode = "exp_1";
	private static final String projectCode="project_1";
	private static final String sampleCode="sample_1";
	private static final String readSetCode="read_1";
	private static final String runCode="run_1";
	private static final String studyCode="study_1";
	
	@BeforeClass
	public static void initData()
	{
		Experiment experiment = new ExperimentBuilder()
		.withCode(experimentCode)
		.withProjectCode(projectCode)
		.withLibraryConstructionProtocol("protocol")
		.withLibrarySource("synthetic")
		.withLibraryLayoutNominalLength(10)
		.withLibrarySelection("size fractionation")
		.withLibraryStrategy("cloneend")
		.withLibraryLayoutOrientation("forward-reverse")
		.withLibraryName("libName")
		.withLibraryLayout("single")
		.withTitle("Title")
		.withInstrumentModel("illumina hiseq 2500")
		.withSpotLength(new Long(10))
		.withSampleCode(sampleCode)
		.withReadSetCode(readSetCode)
		.withStudyCode(studyCode)
		.withTraceInformation(new TraceInformationBuilder().withModifyUser("user").withModifyDate(new Date()).build())
		.withState(new StateBuilder().withCode("new").build())
		.withRun(new RunBuilder().withCode(runCode).withRunDate(new Date()).withRunCenter("gsc").build())
		.addReadSpec(new ReadSpecBuilder().withIndex(0).withLastBaseCoord(1).withReadLabel("F").withReadType("Forward").withReadClass("Application Read").build())
		.build();
		MongoDBDAO.save(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, experiment);
		
		Project project = new ProjectBuilder()
		.withCode(projectCode)
		.build();
		MongoDBDAO.save(InstanceConstants.PROJECT_COLL_NAME,project);
	}

	@AfterClass
	public static void deleteData()
	{
		MongoDBDAO.deleteByCode(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, Experiment.class, experimentCode);
		MongoDBDAO.deleteByCode(InstanceConstants.PROJECT_COLL_NAME, Project.class, projectCode);
	}
	
	@Test
	public void shouldSearchExperiment()
	{
		ExperimentsSearchForm experimentSearchForm = new ExperimentsSearchForm();
		experimentSearchForm.listExperimentCodes =  Arrays.asList(experimentCode);
		Result result = callAction(controllers.sra.experiments.api.routes.ref.Experiments.list(),fakeRequest().withJsonBody(Json.toJson(experimentSearchForm)));
		logger.info(contentAsString(result));
		assertThat(status(result)).isEqualTo(OK);
		assertThat(contentType(result)).isEqualTo("application/json");
	}

	@Test
	public void shouldUpdateExperiment()
	{
		//Change state of sample
		//Get sample
		Experiment experimentToUpdate = MongoDBDAO.findByCode(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, Experiment.class, experimentCode);
		experimentToUpdate.state.code="inWaiting";
		Result result = callAction(controllers.sra.experiments.api.routes.ref.Experiments.update(experimentCode),fakeRequest().withJsonBody(Json.toJson(experimentToUpdate)));
		logger.info(contentAsString(result));
		assertThat(status(result)).isEqualTo(OK);
		//Check in db submission status
		Experiment experimentUpdated = MongoDBDAO.findByCode(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, Experiment.class, experimentCode);
		logger.info("experiment updated "+experimentUpdated.state.code);
		assertThat(experimentUpdated.state.code).isEqualTo("inWaiting");
	}
}
