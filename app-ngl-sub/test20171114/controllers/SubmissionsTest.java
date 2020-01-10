package controllers;

import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.callAction;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.contentType;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.status;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import models.laboratory.common.instance.TBoolean;
import models.laboratory.project.instance.Project;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import models.laboratory.run.instance.Treatment;
import models.sra.submit.common.instance.Sample;
import models.sra.submit.common.instance.Study;
import models.sra.submit.common.instance.Submission;
import models.sra.submit.sra.instance.Configuration;
import models.sra.submit.sra.instance.Experiment;
import models.utils.InstanceConstants;

import org.apache.http.client.ClientProtocolException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mongojack.DBQuery;

import play.Logger;
import play.libs.Json;
import play.mvc.Result;
import utils.AbstractTestController;
import builder.data.ConfigurationBuilder;
import builder.data.ExperimentBuilder;
import builder.data.LaboratoryInstrumentUsedBuilder;
import builder.data.LaboratoryReadSetBuilder;
import builder.data.LaboratoryRunBuilder;
import builder.data.LaboratorySampleBuilder;
import builder.data.LaboratorySampleOnContainerBuilder;
import builder.data.LaboratoryTreatmentBuilder;
import builder.data.LaboratoryValuationBuilder;
import builder.data.ProjectBuilder;
import builder.data.RawDataBuilder;
import builder.data.RunBuilder;
import builder.data.SampleBuilder;
import builder.data.StateBuilder;
import builder.data.StudyBuilder;
import builder.data.SubmissionBuilder;
import builder.data.TraceInformationBuilder;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import controllers.sra.submissions.api.SubmissionsCreationForm;
import fr.cea.ig.MongoDBDAO;

public class SubmissionsTest extends AbstractTestController{

	private static final String projectCode = "projectCode1";
	private static final String subCode1 = "code1";
	private static final String subCode2 = "sub2";
	private static final String subDirectory = "testDir";
	private static final String studyCode = "study";
	private static final String studyCode1 = "study1";
	private static final String codeUserValidate = "userValidate";
	private static final String expCode1 = "exp1";
	private static final String sampCode1 = "samp1";
	private static final String runCode1 = "run1";
	private static final String configCode1 = "conf1";
	private static final String readCode1 = "Read1";
	private static final String labSampleCode = "LabSamp";
	private static final String labRunCode = "LabRun";
	private static final String taxonCode = "10";
	private static final String refCollab = "refCollab";

	@BeforeClass
	public static void initData()
	{
		//Create project
		Project project = new ProjectBuilder()
		.withCode(projectCode)
		.build();
		MongoDBDAO.save(InstanceConstants.PROJECT_COLL_NAME, project);

		//Create Cconfiguration
		Configuration config = new ConfigurationBuilder()
		.withCode(configCode1)
		.withStrategySample("STRATEGY_SAMPLE_CLONE")
		.withLibrarySelection("size fractionation")
		.withLibrarySource("synthetic")
		.withLibraryStrategy("cloneend")
		.withState(new StateBuilder().withCode(codeUserValidate).build())
		.build();
		MongoDBDAO.save(InstanceConstants.SRA_CONFIGURATION_COLL_NAME, config);
		//Create simple submission with state
		Submission submission = new SubmissionBuilder()
		.withCode(subCode1)
		.withProjectCode(projectCode)
		.withSubmissionDirectory(subDirectory)
		.withStudyCode(studyCode)
		//.withRefStudyCode(studyCode1)
		.withConfigCode(config.code)
		.withState(new StateBuilder().withCode(codeUserValidate).build())
		.withTraceInformation(new TraceInformationBuilder().withCreateUser("user1").withCreationDate(new Date()).build())
		.addSampleCode("samp1")
		.build();
		MongoDBDAO.save(InstanceConstants.SRA_SUBMISSION_COLL_NAME, submission);

		//Create complete submission withRawData and with information for createXML
		Submission submissionRD = new SubmissionBuilder()
		.withCode(subCode2)
		.withState(new StateBuilder().withCode("state1").withUser("ejacoby@genoscope.cns.fr").build())
		.withSubmissionDirectory(System.getProperty("user.home")+"/NGL-SUB-Test")
		.withStudyCode(studyCode1)
		.addExperimentCode(expCode1)
		.addSampleCode(sampCode1)
		.build();
		MongoDBDAO.save(InstanceConstants.SRA_SUBMISSION_COLL_NAME, submissionRD);
		Experiment experiment = new ExperimentBuilder()
		.withCode(expCode1)
		.withRun(new RunBuilder()
		.withCode(runCode1)
		.addRawData(new RawDataBuilder()
		.withRelatifName("path1").build())
		.addRawData(new RawDataBuilder()
		.withRelatifName("path2").build())
		.build())
		.build();
		MongoDBDAO.save(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, experiment);
		Sample sample = new SampleBuilder()
		.withCode(sampCode1)
		.build();
		MongoDBDAO.save(InstanceConstants.SRA_SAMPLE_COLL_NAME, sample);
		Study study = new StudyBuilder()
		.withCode(studyCode1)
		.withState(new StateBuilder().withCode(codeUserValidate).build())
		.build();
		MongoDBDAO.save(InstanceConstants.SRA_STUDY_COLL_NAME, study);
		
		
		
		//Create laboratory entity
		ReadSet readSet = new LaboratoryReadSetBuilder()
		.withCode(readCode1)
		.withSampleCode(labSampleCode)
		.withRunCode(labRunCode)
		.withRunSequencingStartDate(new Date())
		.withValuation(new LaboratoryValuationBuilder().withValid(TBoolean.TRUE).build())
		.withSampleOnContainer(new LaboratorySampleOnContainerBuilder().addProperty("libLayoutNominalLength", 10).build())
		.initFiles()
		.build();
		MongoDBDAO.save(InstanceConstants.READSET_ILLUMINA_COLL_NAME, readSet);
		
		models.laboratory.sample.instance.Sample labSample = new LaboratorySampleBuilder()
		.withCode(labSampleCode)
		.withName(labSampleCode)
		.withRefCollab(refCollab)
		.withTaxonCode(taxonCode)
		.build();
		MongoDBDAO.save(InstanceConstants.SAMPLE_COLL_NAME, labSample);
		
		Treatment treatment = new LaboratoryTreatmentBuilder()
		.addPropertyResults("default", "nbCycle", new Long(15))
		.build();
		
		Run run = new LaboratoryRunBuilder()
		.withCode(labRunCode)
		.withTypeCode("rhs2500")
		.withInstrumentUsed(new LaboratoryInstrumentUsedBuilder().withCode("instCode").withTypeCode("illumina").build())
		.initLanes()
		.addProperty("sequencingProgramType", "SR")
		.addTreatment("ngsrg", treatment)
		.build();
		MongoDBDAO.save(InstanceConstants.RUN_ILLUMINA_COLL_NAME, run);
				

	}

	@AfterClass
	public static void deleteData()
	{
		MongoDBDAO.deleteByCode(InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class, subCode1);
		MongoDBDAO.deleteByCode(InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class, subCode2);
		MongoDBDAO.deleteByCode(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, Experiment.class, expCode1);
		MongoDBDAO.deleteByCode(InstanceConstants.SRA_SAMPLE_COLL_NAME, Sample.class, sampCode1);
		MongoDBDAO.deleteByCode(InstanceConstants.SRA_STUDY_COLL_NAME, Study.class, studyCode1);
		MongoDBDAO.deleteByCode(InstanceConstants.PROJECT_COLL_NAME, Project.class, projectCode);
		MongoDBDAO.deleteByCode(InstanceConstants.SRA_CONFIGURATION_COLL_NAME,Configuration.class, configCode1);
		MongoDBDAO.deleteByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readCode1);
		MongoDBDAO.deleteByCode(InstanceConstants.SAMPLE_COLL_NAME, models.laboratory.sample.instance.Sample.class, labSampleCode);
		MongoDBDAO.deleteByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, labRunCode);
		new File(subDirectory).delete();
	}

	@Test
	public void shouldSearchSubmissionByState() throws JsonParseException, JsonMappingException, IOException
	{
		Result result = callAction(controllers.sra.submissions.api.routes.ref.Submissions.list(),fakeRequest("GET","?state="+codeUserValidate));
		Logger.info(contentAsString(result));
		assertThat(status(result)).isEqualTo(OK);
		assertThat(contentType(result)).isEqualTo("application/json");
	}
	
	
	@Test
	public void shouldGetSubmissionByCode()
	{
		Result result = callAction(controllers.sra.submissions.api.routes.ref.Submissions.get(subCode1));
		Logger.info(contentAsString(result));
		assertThat(status(result)).isEqualTo(OK);
		assertThat(contentType(result)).isEqualTo("application/json");
	}

	@Test
	public void shouldSaveSubmission()
	{
		SubmissionsCreationForm submissionsCreationForm = new SubmissionsCreationForm();
		submissionsCreationForm.projCodes.add(projectCode);
		submissionsCreationForm.readSetCodes = Arrays.asList(readCode1);
		submissionsCreationForm.studyCode = studyCode1;
		submissionsCreationForm.configurationCode = configCode1;
		Result result = callAction(controllers.sra.submissions.api.routes.ref.Submissions.save(),fakeRequest().withJsonBody(Json.toJson(submissionsCreationForm)));
		Logger.debug("Result "+result);
		assertThat(status(result)).isEqualTo(OK);
		//Check new submission
		Submission newSubmission = MongoDBDAO.findOne(InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class, DBQuery.regex("code", Pattern.compile("CNS_"+projectCode.toUpperCase()+"_\\w+")));
		Assert.assertNotNull(newSubmission);
		//Remove submission, sample and experiment
		MongoDBDAO.delete(InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class, DBQuery.regex("code", Pattern.compile("CNS_"+projectCode.toUpperCase()+"_\\w+")));
		MongoDBDAO.deleteByCode(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, Experiment.class, "exp_"+readCode1);
		MongoDBDAO.deleteByCode(InstanceConstants.SRA_SAMPLE_COLL_NAME, Sample.class, "sample_"+sampCode1+"_"+taxonCode+"_"+refCollab);
		
	}
	
	@Test
	public void shouldUpdateSubmission()
	{
		//Change state of submission
		//Get submission
		Submission submissionToUpdate = MongoDBDAO.findByCode(InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class, subCode1);
		submissionToUpdate.state.code="inwaiting";
		//TODO pas de méthode de validation a retester avec méthode de validation
		Result result = callAction(controllers.sra.submissions.api.routes.ref.Submissions.update(subCode1),fakeRequest().withJsonBody(Json.toJson(submissionToUpdate)));
		Logger.info(contentAsString(result));
		assertThat(status(result)).isEqualTo(OK);
		//Check in db submission status
		Submission submissionUpdated = MongoDBDAO.findByCode(InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class, subCode1);
		Logger.info("submission updated "+submissionUpdated.state.code);
		assertThat(submissionUpdated.state.code).isEqualTo("inwaiting");
	}

	@Test
	public void shouldActivateSubmission()
	{
		Submission submissionToActivate = MongoDBDAO.findByCode(InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class, subCode1);
		Result result = callAction(controllers.sra.submissions.api.routes.ref.Submissions.activate(subCode1),fakeRequest().withJsonBody(Json.toJson(submissionToActivate)));
		Logger.info(contentAsString(result));
		assertThat(status(result)).isEqualTo(OK);
		
		//Check in db submission status
		Submission submissionActivated = MongoDBDAO.findByCode(InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class,subCode1);
		Logger.info("submission activate "+submissionActivated.state.code);
		assertThat(submissionActivated.state.code).isEqualTo("inWaiting");
	}

	@Test
	public void shouldGetRawDatas() throws JsonParseException, JsonMappingException, IOException
	{
		Map<String, String> mapRequets = new HashMap<String, String>();
		mapRequets.put("submissionCode", subCode2);
		Result result = callAction(controllers.sra.experiments.api.routes.ref.ExperimentsRawDatas.list(), fakeRequest("GET","?submissionCode="+subCode2));
		Logger.info(contentAsString(result));
		assertThat(status(result)).isEqualTo(OK);
		assertThat(contentType(result)).isEqualTo("application/json");
		List object = new ObjectMapper().readValue(contentAsString(result), List.class);
		assertThat(object.size()).isEqualTo(2);
		for(Object o : object)
		{
			Logger.info(o.toString());
			JsonNode jsonNode = Json.toJson(o);
			assertThat(jsonNode.findValue("relatifName")).isNotNull();
		}

	}

	@Test
	public void shouldCreateXML()
	{
		Result result = callAction(controllers.sra.submissions.api.routes.ref.Submissions.createXml(subCode2));
		Logger.info(contentAsString(result));
	}

	/*@Test
	public void shouldTreatmentAC() throws ClientProtocolException, IOException
	{

		File ebiFileAc = new File(System.getProperty("user.home")+"/NGL-SUB-Test/RESULT_AC");
		Logger.debug("JSON FILE "+Json.toJson(ebiFileAc));
		Result result = callAction(controllers.sra.submissions.api.routes.ref.Submissions.treatmentAc(subCode2),fakeRequest().withJsonBody(Json.toJson(ebiFileAc)));
		Logger.debug("Result "+result);
		assertThat(status(result)).isEqualTo(OK);
		Submission submissionSubmited = MongoDBDAO.findByCode(InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class, subCode2);
		Logger.info("submission submited "+submissionSubmited);
		assertThat(submissionSubmited.state.code).isEqualTo("submitted");
	}*/
}
