package controllers;

import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.callAction;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.contentType;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.status;

import java.util.regex.Pattern;

import models.laboratory.project.instance.Project;
import models.sra.submit.common.instance.Study;
import models.sra.submit.common.instance.Submission;
import models.utils.InstanceConstants;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mongojack.DBQuery;

import play.Logger;
import play.libs.Json;
import play.mvc.Result;
import utils.AbstractTestController;
import builder.data.ProjectBuilder;
import builder.data.StudyBuilder;
import controllers.sra.studies.api.StudiesSearchForm;
import fr.cea.ig.MongoDBDAO;

public class StudiesTest extends AbstractTestController{

	private static final String studyCode="study_1";
	private static final String projectCode="proj_1";
	
	@BeforeClass
	public static void initData()
	{
		Study study = new StudyBuilder()
		.withCode(studyCode)
		.withProjectCode(projectCode)
		.build();
		MongoDBDAO.save(InstanceConstants.SRA_STUDY_COLL_NAME, study);
		
		Project project = new ProjectBuilder()
		.withCode(projectCode)
		.build();
		MongoDBDAO.save(InstanceConstants.PROJECT_COLL_NAME, project);
		
		
	}
	
	@AfterClass
	public static void deleteData()
	{
		MongoDBDAO.deleteByCode(InstanceConstants.SRA_STUDY_COLL_NAME, Study.class, studyCode);
		MongoDBDAO.deleteByCode(InstanceConstants.PROJECT_COLL_NAME, Project.class, projectCode);
	}
	
	@Test
	public void shouldSearchStudy()
	{
		StudiesSearchForm studiesSearchForm = new StudiesSearchForm();
		studiesSearchForm.projCodes.add(projectCode);
		Result result = callAction(controllers.sra.studies.api.routes.ref.Studies.list(),fakeRequest().withJsonBody(Json.toJson(studiesSearchForm)));
		Logger.info(contentAsString(result));
		assertThat(status(result)).isEqualTo(OK);
		assertThat(contentType(result)).isEqualTo("application/json");
	}
	
	@Test
	public void shouldSaveStudy()
	{
		Study study = new Study();
		study.centerProjectName="CNS_Proj_1";
		study.existingStudyType="pooled clone sequencing";
		study.projectCodes.add(projectCode);
		
		Result result = callAction(controllers.sra.studies.api.routes.ref.Studies.save(),fakeRequest().withJsonBody(Json.toJson(study)));
		Logger.info(contentAsString(result));
		assertThat(status(result)).isEqualTo(OK);
		assertThat(contentType(result)).isEqualTo("application/json");
		//Check Study in database
		Study newStudy = MongoDBDAO.findOne(InstanceConstants.SRA_STUDY_COLL_NAME, Study.class, DBQuery.regex("code", Pattern.compile("STUDY_"+projectCode.toUpperCase()+"_\\w+")));
		Assert.assertNotNull(newStudy);
		//Remove study from database
		MongoDBDAO.delete(InstanceConstants.SRA_STUDY_COLL_NAME, Study.class, DBQuery.regex("code", Pattern.compile("STUDY_"+projectCode.toUpperCase()+"_\\w+")));
				
	}
}
