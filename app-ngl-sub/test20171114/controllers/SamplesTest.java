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
import java.util.List;

import models.laboratory.project.instance.Project;
import models.sra.submit.common.instance.AbstractSample;
import models.sra.submit.common.instance.ExternalSample;
import models.sra.submit.common.instance.Sample;
import models.utils.InstanceConstants;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import play.Logger;
import play.libs.Json;
import play.mvc.Result;
import utils.AbstractTestController;
import builder.data.ExternalSampleBuilder;
import builder.data.ProjectBuilder;
import builder.data.SampleBuilder;
import builder.data.StateBuilder;
import builder.data.TraceInformationBuilder;
import controllers.sra.samples.api.SamplesSearchForm;
import fr.cea.ig.MongoDBDAO;

public class SamplesTest extends AbstractTestController{

	private static final String sampleCode="sample_1";
	private static final String externalSampleCode="ext_sample_1";
	private static final String projectCode="proj_1";

	@BeforeClass
	public static void initData()
	{
		AbstractSample sample = new SampleBuilder()
		.withCode(sampleCode)
		.withProjectCode(projectCode)
		.withState(new StateBuilder().withCode("new").withUser("test").build())
		.withTraceInformation(new TraceInformationBuilder().withModifyUser("user").withModifyDate(new Date()).build())
		.build();
		MongoDBDAO.save(InstanceConstants.SRA_SAMPLE_COLL_NAME, sample);

		AbstractSample externalSample = new ExternalSampleBuilder()
				.withCode(externalSampleCode)
				.withState(new StateBuilder().withCode("new").build())
				.withTraceInformation(new TraceInformationBuilder().withModifyUser("user").withModifyDate(new Date()).build())
				.build();
		MongoDBDAO.save(InstanceConstants.SRA_SAMPLE_COLL_NAME, externalSample);
		
		Project project = new ProjectBuilder()
		.withCode(projectCode)
		.build();
		MongoDBDAO.save(InstanceConstants.PROJECT_COLL_NAME,project);
	}

	@AfterClass
	public static void deleteData()
	{
		MongoDBDAO.deleteByCode(InstanceConstants.SRA_SAMPLE_COLL_NAME, Sample.class, sampleCode);
		MongoDBDAO.deleteByCode(InstanceConstants.SRA_SAMPLE_COLL_NAME, ExternalSample.class, externalSampleCode);
		MongoDBDAO.deleteByCode(InstanceConstants.PROJECT_COLL_NAME, Project.class, projectCode);
	}

	@Test
	public void shouldSearchSample()
	{
		SamplesSearchForm samplesSearchForm = new SamplesSearchForm();
		samplesSearchForm.listSampleCodes =  Arrays.asList(sampleCode);
		Result result = callAction(controllers.sra.samples.api.routes.ref.Samples.list(),fakeRequest().withJsonBody(Json.toJson(samplesSearchForm)));
		Logger.info(contentAsString(result));
		assertThat(status(result)).isEqualTo(OK);
		assertThat(contentType(result)).isEqualTo("application/json");
	}
	
	@Test
	public void getSamples(){
		List<AbstractSample> abstractSamples = MongoDBDAO.find(InstanceConstants.SRA_SAMPLE_COLL_NAME, AbstractSample.class).toList();
		Logger.debug("Size "+abstractSamples.size());
		Logger.debug(Json.toJson(abstractSamples).toString());
		
	}
	@Test
	public void shouldUpdateSample()
	{
		//Change state of sample
		//Get sample
		Sample sampleToUpdate = MongoDBDAO.findByCode(InstanceConstants.SRA_SAMPLE_COLL_NAME, Sample.class, sampleCode);
		sampleToUpdate.state.code="IW-SUB";
		
		Result result = callAction(controllers.sra.samples.api.routes.ref.Samples.update(sampleCode),fakeRequest().withJsonBody(Json.toJson(sampleToUpdate)));
		Logger.info(contentAsString(result));
		assertThat(status(result)).isEqualTo(OK);
		//Check in db submission status
		Sample sampleUpdated = MongoDBDAO.findByCode(InstanceConstants.SRA_SAMPLE_COLL_NAME, Sample.class, sampleCode);
		Logger.info("sample updated "+sampleUpdated.state.code);
		assertThat(sampleUpdated.state.code).isEqualTo("IW-SUB");
	}
}
