package validation.experiment;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.util.function.CC1Managed;
import fr.cea.ig.util.function.CC2;
import fr.cea.ig.util.function.CCActions;
import models.laboratory.common.instance.State;
import models.laboratory.container.instance.Container;
import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.experiment.instance.Experiment;
//import models.laboratory.instrument.description.Instrument;
import models.laboratory.instrument.description.InstrumentQueryParams;
import models.laboratory.instrument.description.InstrumentUsedType;
import models.laboratory.protocol.instance.Protocol;
import models.laboratory.resolutions.instance.Resolution;
import models.laboratory.resolutions.instance.ResolutionConfiguration;
import models.utils.InstanceConstants;
import ngl.common.Global;
import ngl.refactoring.state.ExperimentStateNames;
import utils.Constants;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import validation.experiment.instance.ExperimentValidationHelper;

public class ExperimentValidationHelperTest {

	private static final String INIT_MONGO_SUFFIX = "_init";
	
	private static class TestContext implements CC1Managed {
		
		ExperimentType     experimentType;
		Protocol           protocol;
		Set<String>        resolutionList;
//		Instrument         instrument;
		InstrumentUsedType instrumentUsedType;

		@Override
		public void setUp() throws Exception {
			//experimentType=ExperimentType.find.findByCategoryCode("transformation").get(0);
			List<Protocol> protocols = MongoDBDAO.find(InstanceConstants.PROTOCOL_COLL_NAME+INIT_MONGO_SUFFIX+"_CNS", Protocol.class).toList();
			MongoDBDAO.save(InstanceConstants.PROTOCOL_COLL_NAME, protocols);

			protocol=new Protocol();
			protocol=MongoDBDAO.find(InstanceConstants.PROTOCOL_COLL_NAME, Protocol.class,DBQuery.empty()).toList().get(0);

			experimentType=ExperimentType.find.get().findByCode(protocol.experimentTypeCodes.get(0));

			List<String> experimentTypes = new ArrayList<>();
			experimentTypes.add(experimentType.code);		
			List<ResolutionConfiguration> resolutionConfigurations = MongoDBDAO.find(InstanceConstants.RESOLUTION_COLL_NAME, ResolutionConfiguration.class, DBQuery.in("typeCodes", experimentTypes)).toList();
			resolutionList=new HashSet<>();
			for (ResolutionConfiguration rc : resolutionConfigurations) {
				for(Resolution reso: rc.resolutions) {
					resolutionList.add(reso.code);
				}
			}
			instrumentUsedType=InstrumentUsedType.find.get().findByExperimentTypeCode(experimentType.code, null).get(0);
			InstrumentQueryParams instrumentsQueryParams=new InstrumentQueryParams();
			instrumentsQueryParams.typeCode=instrumentUsedType.code;
//			instrument=Instrument.find.get().findByQueryParams(instrumentsQueryParams).get(0);
		}

		@Override
		public void tearDown() {
			MongoDBDAO.delete(InstanceConstants.PROCESS_COLL_NAME, Container.class, DBQuery.exists("code"));
		}
		
	}

	private static final CC2<TestContext,ContextValidation> af =
			Global.afSq.cc1()
			.and(CCActions.managed(TestContext::new))
			.cc1((app,ctx) -> ctx)
			.and(CCActions.f0asCC1(() -> ContextValidation.createUndefinedContext(Constants.TEST_USER)));

	@Test
	public void validationProtocolFinishTest() throws Exception {
		af.accept((ctx,contextValidation) -> {
//			contextValidation.getContextObjects().put("stateCode", "F");
//			contextValidation.putObject("stateCode", "F");
			ExperimentValidationHelper.validateProtocolCode(contextValidation, ctx.experimentType.code, ctx.protocol.code, ExperimentStateNames.F);
			assertEquals(0, contextValidation.getErrors().size());
		});
	}
	
	@Test
	public void validationProtocolNullFinishTest() throws Exception {
		af.accept((ctx,contextValidation) -> {
//			contextValidation.getContextObjects().put("stateCode", "F");
//			contextValidation.putObject("stateCode", "F");
			ExperimentValidationHelper.validateProtocolCode(contextValidation, ctx.experimentType.code, null, ExperimentStateNames.F);
			assertEquals(1, contextValidation.getErrors().size());
		});
	}
	
	@Test
	public void validationProtocolNotExperimentTypeFinishTest() throws Exception {
		af.accept((ctx,contextValidation) -> {
//			contextValidation.getContextObjects().put("stateCode", "F");
//			contextValidation.putObject("stateCode", "F");
			ExperimentValidationHelper.validateProtocolCode(contextValidation, "test", ctx.protocol.code, ExperimentStateNames.F);
			assertEquals(1, contextValidation.getErrors().size());
		});
	}

	@Test
	public void validationProtocolNewTest() throws Exception {
		af.accept((ctx,contextValidation) -> {
//			contextValidation.getContextObjects().put("stateCode", "N");
//			contextValidation.putObject("stateCode", "N");
			ExperimentValidationHelper.validateProtocolCode(contextValidation, ctx.experimentType.code, null, ExperimentStateNames.N);
			assertEquals(0, contextValidation.getErrors().size());
		});
	}
	
	@Test
	public void validationResolutionFinishTest() throws Exception {
		af.accept((ctx,contextValidation) -> {
//			contextValidation.getContextObjects().put("stateCode", "F");
			contextValidation.putObject("stateCode", "F");
			CommonValidationHelper.validateResolutionCodes(contextValidation, ctx.experimentType.code, ctx.resolutionList);
			assertEquals(0, contextValidation.getErrors().size());
		});
	}
	
	// @Test
	public void validationResolutionNullFinishTest() throws Exception {
		af.accept((ctx,contextValidation) -> {
//			contextValidation.getContextObjects().put("stateCode", "F");
			contextValidation.putObject("stateCode", "F");
			CommonValidationHelper.validateResolutionCodes(contextValidation, ctx.experimentType.code, null);
			assertEquals(1, contextValidation.getErrors().size());
		});
	}
	
	@Test
	public void validationResolutionNotExperimentTypeFinishTest() throws Exception {
		af.accept((ctx,contextValidation) -> {
//			contextValidation.getContextObjects().put("stateCode", "F");
			contextValidation.putObject("stateCode", "F");
			CommonValidationHelper.validateResolutionCodes(contextValidation, "test", ctx.resolutionList);
			assertEquals(contextValidation.getErrors().size(), ctx.resolutionList.size());
		});
	}

	@Test
	public void validationResolutionNewTest() throws Exception {
		af.accept((ctx,contextValidation) -> {
//			contextValidation.getContextObjects().put("stateCode", "N");
			contextValidation.putObject("stateCode", "N");
			CommonValidationHelper.validateResolutionCodes(contextValidation,ctx.experimentType.code, null);
			assertEquals(0, contextValidation.getErrors().size());
		});
	}
	
	//@Test
	public void validationInstrumentUsedFinishTest() throws Exception {
		af.accept((ctx,contextValidation) -> {
//			contextValidation.getContextObjects().put("stateCode", "F");
			contextValidation.putObject("stateCode", "F");
			assertEquals(0, contextValidation.getErrors().size());
		});
	}
	
	
	//@Test
//	public	void validationExperimentType(String typeCode, Map<String, PropertyValue> properties, ContextValidation contextValidation) {
//	}
	
	//@Test
	public	void validationExperimentCategoryCode(String categoryCode, ContextValidation contextValidation) {
	}
	
	//@Test
	public void	validateState(Experiment experiment, State state, ContextValidation contextValidation) {
	}
	
	//@Test
	public void validateNewState(Experiment experiment , ContextValidation contextValidation) {
	}

}

//package validation.experiment;
//
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.List;
////import java.util.Map;
//import java.util.Set;
//
//import org.junit.AfterClass;
//import org.junit.Assert;
//import org.junit.BeforeClass;
//import org.junit.Test;
//import org.mongojack.DBQuery;
//
//import fr.cea.ig.MongoDBDAO;
////import models.laboratory.common.instance.PropertyValue;
//import models.laboratory.common.instance.State;
//import models.laboratory.container.instance.Container;
//import models.laboratory.experiment.description.ExperimentType;
//import models.laboratory.experiment.instance.Experiment;
//import models.laboratory.instrument.description.Instrument;
//import models.laboratory.instrument.description.InstrumentQueryParams;
//import models.laboratory.instrument.description.InstrumentUsedType;
//import models.laboratory.protocol.instance.Protocol;
//import models.laboratory.resolutions.instance.Resolution;
//import models.laboratory.resolutions.instance.ResolutionConfiguration;
//import models.utils.InstanceConstants;
//import models.utils.dao.DAOException;
//import utils.AbstractTests;
//import utils.Constants;
//import validation.ContextValidation;
//import validation.experiment.instance.ExperimentValidationHelper;
//
//
//public class ExperimentValidationHelperTest extends AbstractTests {
//
//	private static final String INIT_MONGO_SUFFIX = "_init";
//	static ExperimentType experimentType;
//	static Protocol protocol;
//	static Set<String> resolutionList;
//	static Instrument instrument;
//	static InstrumentUsedType instrumentUsedType;
//	
//	@BeforeClass
//	public static void initData() throws DAOException, InstantiationException, IllegalAccessException, ClassNotFoundException{
//		//experimentType=ExperimentType.find.findByCategoryCode("transformation").get(0);
//		
//		
//		List<Protocol> protocols = MongoDBDAO.find(InstanceConstants.PROTOCOL_COLL_NAME+INIT_MONGO_SUFFIX+"_CNS", Protocol.class).toList();
//			MongoDBDAO.save(InstanceConstants.PROTOCOL_COLL_NAME,protocols );
//			
//		protocol=new Protocol();
//		protocol=MongoDBDAO.find(InstanceConstants.PROTOCOL_COLL_NAME, Protocol.class,DBQuery.empty()).toList().get(0);
//		
//		experimentType=ExperimentType.find.get().findByCode(protocol.experimentTypeCodes.get(0));
//
//		List<String> experimentTypes = new ArrayList<>();
//		experimentTypes.add(experimentType.code);		
//		List<ResolutionConfiguration> resolutionConfigurations = MongoDBDAO.find(InstanceConstants.RESOLUTION_COLL_NAME, ResolutionConfiguration.class, DBQuery.in("typeCodes", experimentTypes)).toList();
//		resolutionList=new HashSet<>();
//		for (ResolutionConfiguration rc : resolutionConfigurations) {
//			for(Resolution reso: rc.resolutions) {
//				resolutionList.add(reso.code);
//			}
//		}
//		instrumentUsedType=InstrumentUsedType.find.get().findByExperimentTypeCode(experimentType.code, null).get(0);
//		InstrumentQueryParams instrumentsQueryParams=new InstrumentQueryParams();
//		instrumentsQueryParams.typeCode=instrumentUsedType.code;
//		instrument=Instrument.find.get().findByQueryParams(instrumentsQueryParams).get(0);
//	}
//
//	@AfterClass
//	public static void deleteData() {
//		 MongoDBDAO.delete(InstanceConstants.PROCESS_COLL_NAME, Container.class, DBQuery.exists("code"));
//
//	}
//	
//	@Test
//	public void validationProtocolFinishTest() throws DAOException{
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		contextValidation.getContextObjects().put("stateCode", "F");
//		ExperimentValidationHelper.validationProtocoleCode(experimentType.code,protocol.code, contextValidation);
//		Assert.assertTrue(contextValidation.errors.size()==0);
//	}
//	
//	@Test
//	public void validationProtocolNullFinishTest() throws DAOException{
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		contextValidation.getContextObjects().put("stateCode", "F");
//		ExperimentValidationHelper.validationProtocoleCode(experimentType.code,null, contextValidation);
//		Assert.assertTrue(contextValidation.errors.size()==1);
//	}
//	
//	@Test
//	public void validationProtocolNotExperimentTypeFinishTest() throws DAOException{
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		contextValidation.getContextObjects().put("stateCode", "F");
//		ExperimentValidationHelper.validationProtocoleCode("test",protocol.code, contextValidation);
//		Assert.assertTrue(contextValidation.errors.size()==1);
//	}
//
//	@Test
//	public void validationProtocolNewTest() throws DAOException{
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		contextValidation.getContextObjects().put("stateCode", "N");
//		ExperimentValidationHelper.validationProtocoleCode(experimentType.code,null, contextValidation);
//		Assert.assertTrue(contextValidation.errors.size()==0);
//	}
//	
//	
//	@Test
//	public void validationResolutionFinishTest() throws DAOException{
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		contextValidation.getContextObjects().put("stateCode", "F");
//		ExperimentValidationHelper.validateResolutionCodes(experimentType.code,resolutionList, contextValidation);
//		Assert.assertTrue(contextValidation.errors.size()==0);
//	}
//	
//	// @Test
//	public void validationResolutionNullFinishTest() throws DAOException{
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		contextValidation.getContextObjects().put("stateCode", "F");
//		ExperimentValidationHelper.validateResolutionCodes(experimentType.code,null, contextValidation);
//		Assert.assertTrue(contextValidation.errors.size()==1);
//	}
//	
//	@Test
//	public void validationResolutionNotExperimentTypeFinishTest() throws DAOException{
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		contextValidation.getContextObjects().put("stateCode", "F");
//		ExperimentValidationHelper.validateResolutionCodes("test",resolutionList, contextValidation);
//		Assert.assertTrue(contextValidation.errors.size()==resolutionList.size());
//	}
//
//	@Test
//	public void validationResolutionNewTest() throws DAOException{
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		contextValidation.getContextObjects().put("stateCode", "N");
//		ExperimentValidationHelper.validateResolutionCodes(experimentType.code,null, contextValidation);
//		Assert.assertTrue(contextValidation.errors.size()==0);
//	}
//	
//
//			
//	//@Test
//	public void validationInstrumentUsedFinishTest() throws DAOException{
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		contextValidation.getContextObjects().put("stateCode", "F");
//		Assert.assertTrue(contextValidation.errors.size()==0);
//	}
//	
//	
//	//@Test
////	public	void validationExperimentType(String typeCode, Map<String, PropertyValue> properties, ContextValidation contextValidation) {
////	}
//	
//	//@Test
//	public	void validationExperimentCategoryCode(String categoryCode, ContextValidation contextValidation){
//
//	}
//	//@Test
//	public void	validateState(Experiment experiment, State state, ContextValidation contextValidation){
//
//	}
//	//@Test
//	public void validateNewState(Experiment experiment , ContextValidation contextValidation){
//
//	}
//
//}
