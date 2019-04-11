package validation;

import static fr.cea.ig.play.test.DevAppTesting.newCode;
import static org.fest.assertions.Assertions.assertThat;
import static utils.TestHelper.saveDBOject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.junit.Test;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.util.function.CC1Managed;
import fr.cea.ig.util.function.CC2;
import fr.cea.ig.util.function.CCActions;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TBoolean;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.common.instance.Valuation;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.ContainerSupport;
import models.laboratory.container.instance.LocationOnContainerSupport;
import models.laboratory.project.instance.Project;
import models.laboratory.reagent.instance.Reagent;
import models.laboratory.sample.instance.Sample;
import models.laboratory.storage.instance.Storage;
import models.utils.InstanceConstants;
import ngl.common.Global;
import play.data.validation.ValidationError;
import utils.Constants;
import validation.common.instance.CommonValidationHelper;
import validation.container.instance.ContainerSupportValidationHelper;

public class InstanceValidationHelperTest {

	private static final play.Logger.ALogger logger = play.Logger.of(InstanceValidationHelperTest.class);
	
	private static class TestContext implements CC1Managed {

		Project   project;
		Project   project1;
		Sample    sample;
		Sample    sample1;
		Sample    sample2;
		Storage   stock;
		Container container;
		ContainerSupport containerSupport;
		Reagent   reagentInstance;

		@Override
		public void setUp() throws Exception {
			project  = saveDBOject(Project.class, InstanceConstants.PROJECT_COLL_NAME, newCode());
			project1 = saveDBOject(Project.class, InstanceConstants.PROJECT_COLL_NAME, newCode());
			sample   = saveDBOject(Sample.class,  InstanceConstants.SAMPLE_COLL_NAME,  newCode());
			sample1  = saveDBOject(Sample.class,  InstanceConstants.SAMPLE_COLL_NAME,  newCode());

			sample2  = new Sample(); 
			sample2.code = newCode();
			Set<String> l = new HashSet<>();
			l.add("ProjectCode"); 
			sample2.projectCodes = l;
			MongoDBDAO.save(InstanceConstants.SAMPLE_COLL_NAME, sample2);

			stock            = saveDBOject(Storage.class,          InstanceConstants.STORAGE_COLL_NAME,           newCode());
			container        = saveDBOject(Container.class,        InstanceConstants.CONTAINER_COLL_NAME,         newCode());
			containerSupport = saveDBOject(ContainerSupport.class, InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, newCode());
			reagentInstance  = saveDBOject(Reagent.class,          InstanceConstants.REAGENT_INSTANCE_COLL_NAME,  newCode());
		}

		@Override
		public void tearDown() {
			MongoDBDAO.delete(InstanceConstants.PROJECT_COLL_NAME, project);
			MongoDBDAO.delete(InstanceConstants.PROJECT_COLL_NAME, project1);

			MongoDBDAO.delete(InstanceConstants.SAMPLE_COLL_NAME, sample);
			MongoDBDAO.delete(InstanceConstants.SAMPLE_COLL_NAME, sample1);
			MongoDBDAO.delete(InstanceConstants.SAMPLE_COLL_NAME, sample2);

			MongoDBDAO.delete(InstanceConstants.STORAGE_COLL_NAME, stock);

			MongoDBDAO.delete(InstanceConstants.CONTAINER_COLL_NAME, container);

			MongoDBDAO.delete(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, containerSupport);

			MongoDBDAO.delete(InstanceConstants.REAGENT_INSTANCE_COLL_NAME,reagentInstance);
		}

	}
	
	private static final CC2<TestContext,ContextValidation> af =
			Global.afSq.cc1()
			.and(CCActions.managed(TestContext::new))
			.cc1((app,ctx) -> ctx)
			.and(CCActions.f0asCC1(() -> ContextValidation.createUndefinedContext(Constants.TEST_USER)));

	@Test
	public void validationProjectCodesTest() throws Exception {
		af.accept((ctx,contextValidation) -> {
			List<String> projects = new ArrayList<>();
			projects.add(ctx.project.code);
			projects.add(ctx.project1.code);
			CommonValidationHelper.validateProjectCodes(contextValidation,projects );
			assertThat(contextValidation.getErrors().size()).isEqualTo(0);
		});
	}
	
	@Test
	public void validationProjectCodesRequiredTest() throws Exception {
		af.accept((ctx,contextValidation) -> {	
			List<String> projects = new ArrayList<>();
			CommonValidationHelper.validateProjectCodes(contextValidation,projects );
			assertThat(contextValidation.getErrors().size()).isNotEqualTo(0);
		});
	}
	
	@Test
	public void validationProjectCodesNotExistTest() throws Exception {
		af.accept((ctx,contextValidation) -> {
			List<String> projects = new ArrayList<>();
			projects.add("notexist");
			CommonValidationHelper.validateProjectCodes(contextValidation,projects );
			assertThat(contextValidation.getErrors().size()).isNotEqualTo(0);
		});
	}
	
	@Test
	public void validationProjectCodeTest() throws Exception {
		af.accept((ctx,contextValidation) -> {
			CommonValidationHelper.validateProjectCodeRequired(contextValidation,ctx.project.code );
			assertThat(contextValidation.getErrors().size()).isEqualTo(0);
		});
	}
	
	@Test
	public void validationProjectCodeRequiredTest() throws Exception {
		af.accept((ctx,contextValidation) -> {
			CommonValidationHelper.validateProjectCodeRequired(contextValidation,null );
			assertThat(contextValidation.getErrors().size()).isNotEqualTo(0);
		});
	}
	
	@Test
	public void validationProjectCodeNotExistTest() throws Exception {
		af.accept((ctx,contextValidation) -> {
			CommonValidationHelper.validateProjectCodeRequired(contextValidation,"notexist" );
			assertThat(contextValidation.getErrors().size()).isNotEqualTo(0);
		});
	}
	
	@Test
	public void validationSampleCodesTest() throws Exception {
		af.accept((ctx,contextValidation) -> {
			List<String> samples = new ArrayList<>();
			samples.add(ctx.sample.code);
			samples.add(ctx.sample1.code);
			CommonValidationHelper.validateSampleCodes(contextValidation,samples );
			assertThat(contextValidation.getErrors().size()).isEqualTo(0);
		});
	}
	
	@Test
	public void validationSampleCodesRequiredTest() throws Exception {
		af.accept((ctx,contextValidation) -> {
			List<String> samples = new ArrayList<>();
			CommonValidationHelper.validateSampleCodes(contextValidation,samples );
			assertThat(contextValidation.getErrors().size()).isNotEqualTo(0);
		});
	}
	
	@Test
	public void validationSampleCodesNotExistTest() throws Exception {
		af.accept((ctx,contextValidation) -> {
			List<String> samples = new ArrayList<>();
			samples.add("notexist");
			CommonValidationHelper.validateSampleCodes(contextValidation,samples );
			assertThat(contextValidation.getErrors().size()).isNotEqualTo(0);
		});
	}

	@Test
	public void validationSampleCodeTest() throws Exception {
		af.accept((ctx,contextValidation) -> {
			CommonValidationHelper.validateSampleCodeRequired(contextValidation, ctx.sample2.code, ctx.sample2.projectCodes.toArray(new String[0])[0] );
			logger.debug(contextValidation.getErrors().toString());
			assertThat(contextValidation.getErrors().size()).isEqualTo(0);
		});
	}
	
	@Test
	public void validationSampleCodeNotRequiredTest() throws Exception {
		af.accept((ctx,contextValidation) -> {
			CommonValidationHelper.validateSampleCodeRequired(contextValidation,null, null );
			assertThat(contextValidation.getErrors().size()).isEqualTo(1);
		});
	}
	
	@Test
	public void validationSampleCodeNotExistTest() throws Exception {
		af.accept((ctx,contextValidation) -> {
			CommonValidationHelper.validateSampleCodeRequired(contextValidation,"notexist", "notexist" );
			assertThat(contextValidation.getErrors().size()).isNotEqualTo(0);
		});
	}

	@Test
	public void validationStockCodeTest() throws Exception {
		af.accept((ctx,contextValidation) -> {
			ContainerSupportValidationHelper.validateStorageCodeOptional(contextValidation, ctx.stock.code);
			assertThat(contextValidation.getErrors().size()).isEqualTo(0);
		});
	}

	@Test
	public void validationStockNotRequiredCodeTest() throws Exception {
		af.accept((ctx,contextValidation) -> {
			ContainerSupportValidationHelper.validateStorageCodeOptional(contextValidation, null);
			assertThat(contextValidation.getErrors().size()).isEqualTo(0);
		});
	}
	
	@Test
	public void validationStockNotExistCodeTest() throws Exception {
		af.accept((ctx,contextValidation) -> {
			ContainerSupportValidationHelper.validateStorageCodeOptional(contextValidation, "notexist");
			assertThat(contextValidation.getErrors().size()).isEqualTo(1);
		});
	}

	@Test
	public void validationContainerCodeTest() throws Exception {
		af.accept((ctx,contextValidation) -> {
			CommonValidationHelper.validateContainerCodeRequired(contextValidation,ctx.container.code, "code" );
			assertThat(contextValidation.getErrors().size()).isEqualTo(0);
		});
	}
	
	@Test
	public void validationContainerCodeRequiredTest() throws Exception {
		af.accept((ctx,contextValidation) -> {
			CommonValidationHelper.validateContainerCodeRequired(contextValidation,null, "code");
			assertThat(contextValidation.getErrors().size()).isNotEqualTo(0);
		});
	}
	
	@Test
	public void validationContainerCodeNotExistTest() throws Exception {
		af.accept((ctx,contextValidation) -> {
			CommonValidationHelper.validateContainerCodeRequired(contextValidation,"notexist", "code");
			assertThat(contextValidation.getErrors().size()).isNotEqualTo(0);
		});
	}
	
	@Test
	public void validationReagentInstanceCodeTest() throws Exception {
		af.accept((ctx,contextValidation) -> {
			validationReagentInstanceCode(ctx.reagentInstance.code,contextValidation );
			assertThat(contextValidation.getErrors().size()).isEqualTo(0);
		});
	}
	
	@Test
	public void validationReagentInstanceCodeRequiredTest() throws Exception {
		af.accept((ctx,contextValidation) -> {
			validationReagentInstanceCode(null,contextValidation );
			assertThat(contextValidation.getErrors().size()).isNotEqualTo(0);
		});
	}
	
	@Test
	public void validationReagentCodeNotExistTest() throws Exception {
		af.accept((ctx,contextValidation) -> {
			validationReagentInstanceCode("notexist",contextValidation );
			assertThat(contextValidation.getErrors().size()).isNotEqualTo(0);
		});
	}
	
	public static void validationReagentInstanceCode(String reagentInstanceCode, ContextValidation contextValidation) {
		contextValidation.addKeyToRootKeyName("reagent");
//		BusinessValidationHelper.validateRequiredInstanceCode(contextValidation, reagentInstanceCode, "code", Reagent.class,InstanceConstants.REAGENT_INSTANCE_COLL_NAME);
//		CommonValidationHelper.validateRequiredInstanceCode(reagentInstanceCode, "code", Reagent.class,InstanceConstants.REAGENT_INSTANCE_COLL_NAME, contextValidation);
		CommonValidationHelper.validateCodeForeignRequired(contextValidation, Reagent.find.get(), reagentInstanceCode, "code");
		contextValidation.removeKeyFromRootKeyName("reagent");
	}
	
	@Test
	public void validationContainerContentsTestInUpdateMode() throws Exception {
		af.accept((ctx,contextValidation) -> {
			contextValidation.setUpdateMode();

			// to get _id
			Container c = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class, ctx.container.code);

			c.categoryCode = "lane";

			Set<String> lp = new HashSet<>(); 
			lp.add(ctx.project.code);
			c.projectCodes = lp;

			Map<String, PropertyValue> m = new HashMap<>(); // <String, PropertyValue>();
			m.put("limsCode", new PropertySingleValue(3805));
			c.properties = m;

			Set<String> ls = new HashSet<>(); 
			ls.add(ctx.sample.code);
			c.sampleCodes = ls;

			State state = new State();
			state.code = "N";
			state.date = new Date();
			state.user = "ngl";
			c.state = state;

			LocationOnContainerSupport loc = new LocationOnContainerSupport();
			loc.categoryCode = "flowcell-8";
			loc.code = ctx.containerSupport.code; 
			loc.column = "1";
			loc.line = "7";
			c.support = loc;

			TraceInformation tc = new TraceInformation(); 
			tc.setTraceInformation("ngl");
			tc.modifyUser = "ngl";
			tc.modifyDate = new Date();
			c.traceInformation = tc;

			Valuation v = new Valuation(); 
			v.valid = TBoolean.UNSET;
			c.valuation = v;

			c.contents = new ArrayList<>();

//			c.validate(contextValidation);
			c.validate(contextValidation, null, null);
			assertThat(contextValidation.getErrors().size()).isEqualTo(1);

			for (String s : contextValidation.getErrors().keySet()) {
				assertThat(s).isEqualTo("contents");
				for (ValidationError ve : contextValidation.getErrors().get(s)) {
					assertThat(ve.toString()).contains("error.required");
				}
			}

			c.contents = null;
			assertThat(contextValidation.getErrors().size()).isEqualTo(1);

			for (String s : contextValidation.getErrors().keySet()) {
				assertThat(s).isEqualTo("contents");
				for (ValidationError ve : contextValidation.getErrors().get(s)) {
					assertThat(ve.toString()).contains("error.required");
				}
			}
		});
	}
	
	@Test
	public void validationContainerContentsTestInCreationMode() throws Exception {
		af.accept((ctx,contextValidation) -> {
			contextValidation.setCreationMode();

			Random randomGenerator = new Random();
			int randomInt = randomGenerator.nextInt(10000);

			Container c = new Container();
			c.code = "container" + randomInt; 
			c.categoryCode = "lane";

			Set<String> lp = new HashSet<>(); 
			lp.add(ctx.project.code);
			c.projectCodes = lp;

			Map<String, PropertyValue> m = new HashMap<>(); // <String, PropertyValue>();
			m.put("limsCode", new PropertySingleValue(3805));
			c.properties = m;

			Set<String> ls = new HashSet<>(); 
			ls.add(ctx.sample.code);
			c.sampleCodes = ls;

			State state = new State();
			state.code = "N";
			state.date = new Date();
			state.user = "ngl";
			c.state = state;

			LocationOnContainerSupport loc = new LocationOnContainerSupport();
			loc.categoryCode = "flowcell-8";
			loc.code = ctx.containerSupport.code; 
			loc.column = "1";
			loc.line = "7";
			c.support = loc;

			TraceInformation tc = new TraceInformation(); 
			tc.createUser = "ngl";
			tc.creationDate = new Date();
			c.traceInformation = tc;

			Valuation v = new Valuation(); 
			v.valid = TBoolean.UNSET;
			c.valuation = v;

			c.contents = new ArrayList<>();

//			c.validate(contextValidation);
			c.validate(contextValidation, null, null);
			assertThat(contextValidation.getErrors().size()).isEqualTo(1);

			for (String s : contextValidation.getErrors().keySet()) {
				assertThat(s).isEqualTo("contents");
				for (ValidationError ve : contextValidation.getErrors().get(s)) {
					assertThat(ve.toString()).contains("error.required");
				}
			}

			c.contents = null;
			assertThat(contextValidation.getErrors().size()).isEqualTo(1);

			for (String s : contextValidation.getErrors().keySet()) {
				assertThat(s).isEqualTo("contents");
				for (ValidationError ve : contextValidation.getErrors().get(s)) {
					assertThat(ve.toString()).contains("error.required");
				}
			}
		});
	}
	
}

//package validation;
//
//import static org.fest.assertions.Assertions.assertThat;
//
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Random;
//import java.util.Set;
//
//import org.junit.AfterClass;
//import org.junit.BeforeClass;
//import org.junit.Test;
//
//import fr.cea.ig.MongoDBDAO;
//import models.laboratory.common.instance.PropertyValue;
//import models.laboratory.common.instance.State;
//import models.laboratory.common.instance.TBoolean;
//import models.laboratory.common.instance.TraceInformation;
//import models.laboratory.common.instance.Valuation;
//import models.laboratory.common.instance.property.PropertySingleValue;
//import models.laboratory.container.instance.Container;
//import models.laboratory.container.instance.ContainerSupport;
//import models.laboratory.container.instance.LocationOnContainerSupport;
//import models.laboratory.project.instance.Project;
//import models.laboratory.reagent.instance.Reagent;
//import models.laboratory.sample.instance.Sample;
//import models.laboratory.storage.instance.Storage;
//import models.utils.InstanceConstants;
//import play.data.validation.ValidationError;
//import utils.AbstractSQTests;
//import utils.Constants;
//import validation.common.instance.CommonValidationHelper;
//import validation.container.instance.ContainerSupportValidationHelper;
//import validation.utils.BusinessValidationHelper;
//
//public class InstanceValidationHelperTest extends AbstractSQTests {
//
//	private static final play.Logger.ALogger logger = play.Logger.of(InstanceValidationHelperTest.class);
//	
//	static Project   project;
//	static Project   project1;
//	static Sample    sample;
//	static Sample    sample1;
//	static Sample    sample2;
//	static Storage   stock;
//	static Container container;
//	static ContainerSupport containerSupport;
//	static Reagent   reagentInstance;
//	static int       randomInt;
//			
//	@BeforeClass
//	public static void initData() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
//		//use a random int to avoid concurrency access to this test
//		Random randomGenerator = new Random();
//		int randomInt = randomGenerator.nextInt(10000);
//
//		project  = saveDBOject(Project.class, InstanceConstants.PROJECT_COLL_NAME, "project"  + randomInt);
//		project1 = saveDBOject(Project.class, InstanceConstants.PROJECT_COLL_NAME, "project1" + randomInt);
//		sample   = saveDBOject(Sample.class,  InstanceConstants.SAMPLE_COLL_NAME,  "sample"   + randomInt);
//		sample1  = saveDBOject(Sample.class,  InstanceConstants.SAMPLE_COLL_NAME,  "sample1"  + randomInt);
//		
//		sample2  = new Sample(); 
//		sample2.code = "SampleCode" + randomInt;
//		Set<String> l = new HashSet<>();
//		l.add("ProjectCode"); 
//		sample2.projectCodes = l;
//		MongoDBDAO.save(InstanceConstants.SAMPLE_COLL_NAME, sample2);
//		
//		stock            = saveDBOject(Storage.class,InstanceConstants.STORAGE_COLL_NAME,"stock" + randomInt);
//		container        = saveDBOject(Container.class,InstanceConstants.CONTAINER_COLL_NAME,"container" + randomInt);
//		containerSupport = saveDBOject(ContainerSupport.class, InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, "containerSupport" + randomInt);
//		reagentInstance  = saveDBOject(Reagent.class, InstanceConstants.REAGENT_INSTANCE_COLL_NAME, "reagent" + randomInt);
//	}
//	
//	@AfterClass
//	public static void deleteData(){
//		MongoDBDAO.delete(InstanceConstants.PROJECT_COLL_NAME, project);
//		MongoDBDAO.delete(InstanceConstants.PROJECT_COLL_NAME, project1);
//		
//		MongoDBDAO.delete(InstanceConstants.SAMPLE_COLL_NAME, sample);
//		MongoDBDAO.delete(InstanceConstants.SAMPLE_COLL_NAME, sample1);
//		
//		MongoDBDAO.delete(InstanceConstants.SAMPLE_COLL_NAME, sample2);
//
//		MongoDBDAO.delete(InstanceConstants.STORAGE_COLL_NAME, stock);
//		
//		MongoDBDAO.delete(InstanceConstants.CONTAINER_COLL_NAME, container);
//		
//		MongoDBDAO.delete(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, containerSupport);
//		
//		MongoDBDAO.delete(InstanceConstants.REAGENT_INSTANCE_COLL_NAME,reagentInstance);
//	}
//	
//	@Test
//	public void validationProjectCodesTest() {
//		ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
//		List<String> projects = new ArrayList<>();
//		projects.add(project.code);
//		projects.add(project1.code);
//		CommonValidationHelper.validateProjectCodes(projects,contextValidation );
//		assertThat(contextValidation.errors.size()).isEqualTo(0);
//	}
//	
//	@Test
//	public void validationProjectCodesRequiredTest() {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		List<String> projects = new ArrayList<>();
//		CommonValidationHelper.validateProjectCodes(projects,contextValidation );
//		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
//	}
//	
//	@Test
//	public void validationProjectCodesNotExistTest() {
//		ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
//		List<String> projects = new ArrayList<>();
//		projects.add("notexist");
//		CommonValidationHelper.validateProjectCodes(projects,contextValidation );
//		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
//	}
//	
//	@Test
//	public void validationProjectCodeTest() {
//		ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
//		CommonValidationHelper.validateProjectCode(project.code,contextValidation );
//		assertThat(contextValidation.errors.size()).isEqualTo(0);
//	}
//	
//	@Test
//	public void validationProjectCodeRequiredTest() {
//		ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
//		CommonValidationHelper.validateProjectCode(null,contextValidation );
//		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
//	}
//	
//	@Test
//	public void validationProjectCodeNotExistTest() {
//		ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
//		CommonValidationHelper.validateProjectCode("notexist",contextValidation );
//		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
//	}
//	
//	@Test
//	public void validationSampleCodesTest() {
//		ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
//		List<String> samples = new ArrayList<>();
//		samples.add(sample.code);
//		samples.add(sample1.code);
//		CommonValidationHelper.validateSampleCodes(samples,contextValidation );
//		assertThat(contextValidation.errors.size()).isEqualTo(0);
//	}
//	
//	@Test
//	public void validationSampleCodesRequiredTest() {
//		ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
//		List<String> samples = new ArrayList<>();
//		CommonValidationHelper.validateSampleCodes(samples,contextValidation );
//		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
//	}
//	
//	@Test
//	public void validationSampleCodesNotExistTest(){
//		ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
//		List<String> samples = new ArrayList<>();
//		samples.add("notexist");
//		CommonValidationHelper.validateSampleCodes(samples,contextValidation );
//		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
//	}
//
//	@Test
//	public void validationSampleCodeTest() {
//		ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
//		CommonValidationHelper.validateSampleCode(sample2.code, sample2.projectCodes.toArray(new String[0])[0], contextValidation );
//		logger.debug(contextValidation.errors.toString());
//		assertThat(contextValidation.errors.size()).isEqualTo(0);
//	}
//	
//	@Test
//	public void validationSampleCodeNotRequiredTest() {
//		ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
//		CommonValidationHelper.validateSampleCode(null,null, contextValidation );
//		assertThat(contextValidation.errors.size()).isEqualTo(1);
//	}
//	
//	@Test
//	public void validationSampleCodeNotExistTest() {
//		ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
//		CommonValidationHelper.validateSampleCode("notexist","notexist", contextValidation );
//		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
//	}
//
//	@Test
//	public void validationStockCodeTest() {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		ContainerSupportValidationHelper.validateStorageCode(stock.code, contextValidation);
//		assertThat(contextValidation.errors.size()).isEqualTo(0);
//	}
//
//	@Test
//	public void validationStockNotRequiredCodeTest() {
//		ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
//		ContainerSupportValidationHelper.validateStorageCode(null, contextValidation);
//		assertThat(contextValidation.errors.size()).isEqualTo(0);
//	}
//	
//	@Test
//	public void validationStockNotExistCodeTest() {
//		ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
//		ContainerSupportValidationHelper.validateStorageCode("notexist", contextValidation);
//		assertThat(contextValidation.errors.size()).isEqualTo(1);
//	}
//
//	@Test
//	public void validationContainerCodeTest() {
//		ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
//		CommonValidationHelper.validateContainerCode(container.code,contextValidation, "code" );
//		assertThat(contextValidation.errors.size()).isEqualTo(0);
//	}
//	
//	@Test
//	public void validationContainerCodeRequiredTest() {
//		ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
//		CommonValidationHelper.validateContainerCode(null,contextValidation, "code");
//		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
//	}
//	
//	@Test
//	public void validationContainerCodeNotExistTest() {
//		ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
//		CommonValidationHelper.validateContainerCode("notexist",contextValidation, "code");
//		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
//	}
//	
//	@Test
//	public void validationReagentInstanceCodeTest() {
//		ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
//		validationReagentInstanceCode(reagentInstance.code,contextValidation );
//		assertThat(contextValidation.errors.size()).isEqualTo(0);
//	}
//	
//	@Test
//	public void validationReagentInstanceCodeRequiredTest() {
//		ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
//		validationReagentInstanceCode(null,contextValidation );
//		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
//	}
//	
//	@Test
//	public void validationReagentCodeNotExistTest() {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		validationReagentInstanceCode("notexist",contextValidation );
//		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
//	}
//	
//	public static void validationReagentInstanceCode(String reagentInstanceCode, ContextValidation contextValidation) {
//		contextValidation.addKeyToRootKeyName("reagent");
//		BusinessValidationHelper.validateRequiredInstanceCode(contextValidation, reagentInstanceCode, "code", Reagent.class,InstanceConstants.REAGENT_INSTANCE_COLL_NAME);
//		contextValidation.removeKeyFromRootKeyName("reagent");
//	}
//	
//	@Test
//	public void validationContainerContentsTestInUpdateMode() {
//		ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
//		contextValidation.setUpdateMode();
//		
//		//to get _id
//		Container c = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class, container.code);
//		
//		c.categoryCode = "lane";
//		
//		Set<String> lp = new HashSet<>(); 
//		lp.add(project.code);
//		c.projectCodes = lp;
//		
//		Map<String, PropertyValue> m = new HashMap<>(); // <String, PropertyValue>();
//		m.put("limsCode", new PropertySingleValue(3805));
//		c.properties = m;
//		
//		Set<String> ls = new HashSet<>(); 
//		ls.add(sample.code);
//		c.sampleCodes = ls;
//		
//		State state = new State();
//		state.code = "N";
//		state.date = new Date();
//		state.user = "ngl";
//		c.state = state;
//		
//		LocationOnContainerSupport loc = new LocationOnContainerSupport();
//		loc.categoryCode = "flowcell-8";
//		loc.code = containerSupport.code; 
//		loc.column = "1";
//		loc.line = "7";
//		c.support = loc;
//		
//		TraceInformation tc = new TraceInformation(); 
//		tc.setTraceInformation("ngl");
//		tc.modifyUser = "ngl";
//		tc.modifyDate = new Date();
//		c.traceInformation = tc;
//		
//		Valuation v = new Valuation(); 
//		v.valid = TBoolean.UNSET;
//		c.valuation = v;
//		
//		c.contents = new ArrayList<>();
//				
//		c.validate(contextValidation);
//		assertThat(contextValidation.errors.size()).isEqualTo(1);
//
//		for (String s : contextValidation.errors.keySet()) {
//			assertThat(s).isEqualTo("contents");
//			for (ValidationError ve : contextValidation.errors.get(s)) {
//				assertThat(ve.toString()).contains("error.required");
//			}
//		}
//		
//		c.contents = null;
//		assertThat(contextValidation.errors.size()).isEqualTo(1);
//		
//		for (String s : contextValidation.errors.keySet()) {
//			assertThat(s).isEqualTo("contents");
//			for (ValidationError ve : contextValidation.errors.get(s)) {
//				assertThat(ve.toString()).contains("error.required");
//			}
//		}
//	}
//	
//	@Test
//	public void validationContainerContentsTestInCreationMode() {
//		ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
//		contextValidation.setCreationMode();
//		
//		Random randomGenerator = new Random();
//		int randomInt = randomGenerator.nextInt(10000);
//		
//		Container c = new Container();
//		c.code = "container" + randomInt; 
//		c.categoryCode = "lane";
//		
//		Set<String> lp = new HashSet<>(); 
//		lp.add(project.code);
//		c.projectCodes = lp;
//		
//		Map<String, PropertyValue> m = new HashMap<>(); // <String, PropertyValue>();
//		m.put("limsCode", new PropertySingleValue(3805));
//		c.properties = m;
//		
//		Set<String> ls = new HashSet<>(); 
//		ls.add(sample.code);
//		c.sampleCodes = ls;
//		
//		State state = new State();
//		state.code = "N";
//		state.date = new Date();
//		state.user = "ngl";
//		c.state = state;
//		
//		LocationOnContainerSupport loc = new LocationOnContainerSupport();
//		loc.categoryCode = "flowcell-8";
//		loc.code = containerSupport.code; 
//		loc.column = "1";
//		loc.line = "7";
//		c.support = loc;
//		
//		TraceInformation tc = new TraceInformation(); 
//		tc.createUser = "ngl";
//		tc.creationDate = new Date();
//		c.traceInformation = tc;
//		
//		Valuation v = new Valuation(); 
//		v.valid = TBoolean.UNSET;
//		c.valuation = v;
//		
//		c.contents = new ArrayList<>();
//			
//		c.validate(contextValidation);
//		assertThat(contextValidation.errors.size()).isEqualTo(1);
//		
//		for (String s : contextValidation.errors.keySet()) {
//			assertThat(s).isEqualTo("contents");
//			for (ValidationError ve : contextValidation.errors.get(s)) {
//				assertThat(ve.toString()).contains("error.required");
//			}
//		}
//		
//		c.contents = null;
//		assertThat(contextValidation.errors.size()).isEqualTo(1);
//
//		for (String s : contextValidation.errors.keySet()) {
//			assertThat(s).isEqualTo("contents");
//			for (ValidationError ve : contextValidation.errors.get(s)) {
//				assertThat(ve.toString()).contains("error.required");
//			}
//		}
//	}
//	
//}
