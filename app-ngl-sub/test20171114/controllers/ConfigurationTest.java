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
import models.sra.submit.sra.instance.Configuration;
import models.utils.InstanceConstants;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mongojack.DBQuery;

//import play.Logger;
import play.libs.Json;
import play.mvc.Result;
import controllers.sra.configurations.api.ConfigurationsSearchForm;
import fr.cea.ig.MongoDBDAO;
import builder.data.ConfigurationBuilder;
import builder.data.ProjectBuilder;
import utils.AbstractTestController;

public class ConfigurationTest extends AbstractTestController{
	private static final play.Logger.ALogger logger = play.Logger.of(ConfigurationTest.class);

	private static final String configCode = "conf_1";
	private static final String projectCode = "proj_1";
	
	@BeforeClass
	public static void initData()
	{
		Configuration configuration = new ConfigurationBuilder()
		.withCode(configCode)
		.withProjectCode(projectCode)
		.build();
		MongoDBDAO.save(InstanceConstants.SRA_CONFIGURATION_COLL_NAME, configuration);
		
		Project project = new ProjectBuilder()
		.withCode(projectCode)
		.build();
		MongoDBDAO.save(InstanceConstants.PROJECT_COLL_NAME,project);
	}
	
	@AfterClass
	public static void deleteData()
	{
		MongoDBDAO.deleteByCode(InstanceConstants.SRA_CONFIGURATION_COLL_NAME, Configuration.class, configCode);
		MongoDBDAO.deleteByCode(InstanceConstants.PROJECT_COLL_NAME, Project.class, projectCode);
	}
	
	@Test
	public void shouldSearchConfiguration()
	{
		ConfigurationsSearchForm confSearchForm = new ConfigurationsSearchForm();
		confSearchForm.projCodes.add(projectCode);
		Result result = callAction(controllers.sra.configurations.api.routes.ref.Configurations.list(),fakeRequest().withJsonBody(Json.toJson(confSearchForm)));
		logger.info(contentAsString(result));
		assertThat(status(result)).isEqualTo(OK);
		assertThat(contentType(result)).isEqualTo("application/json");
	}
	
	@Test
	public void shouldSaveConfiguration()
	{
		Configuration configuration = new Configuration();
		configuration.code=configCode;
		configuration.projectCodes.add(projectCode);
		configuration.strategySample="STRATEGY_SAMPLE_CLONE";
		configuration.librarySelection="size fractionation";
		configuration.librarySource="synthetic";
		configuration.libraryStrategy="cloneend";
		Result result = callAction(controllers.sra.configurations.api.routes.ref.Configurations.save(),fakeRequest().withJsonBody(Json.toJson(configuration)));
		logger.info(contentAsString(result));
		assertThat(status(result)).isEqualTo(OK);
		//Check Conf in database
		Configuration newConf = MongoDBDAO.findOne(InstanceConstants.SRA_CONFIGURATION_COLL_NAME, Configuration.class, DBQuery.regex("code", Pattern.compile("CONF_"+projectCode.toUpperCase()+"_\\w+")));
		Assert.assertNotNull(newConf);
		//Remove conf from database
		MongoDBDAO.delete(InstanceConstants.SRA_CONFIGURATION_COLL_NAME, Configuration.class, DBQuery.regex("code", Pattern.compile("CONF_"+projectCode.toUpperCase()+"_\\w+")));
					
	}
}
