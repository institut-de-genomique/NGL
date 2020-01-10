package containerSupports;

import static org.assertj.core.api.Assertions.assertThat;
import static play.test.Helpers.callAction;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.status;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import models.laboratory.common.instance.TBoolean;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.ContainerSupport;
import models.laboratory.container.instance.LocationOnContainerSupport;
import models.utils.InstanceConstants;
import models.utils.ListObject;
import models.utils.dao.DAOException;
import models.utils.instance.ContainerSupportHelper;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mongojack.DBQuery;

import play.Logger;
import play.Logger.ALogger;
import play.mvc.Result;
import utils.AbstractTests;
import utils.DatatableResponseForTest;
import utils.InitDataHelper;
import utils.MapperHelper;

import com.fasterxml.jackson.core.type.TypeReference;

import controllers.containers.api.ContainerSupports;
import controllers.containers.api.ContainerSupportsSearchForm;
import fr.cea.ig.MongoDBDAO;

public class ContainerSupportTest extends AbstractTests {

	@BeforeClass
	public static void initData() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		InitDataHelper.initForProcessesTest();
	}
	
	@AfterClass
	public static void resetData(){
		InitDataHelper.endTest();
	}	
	
	protected static ALogger logger=Logger.of("ContainerSupportTest");
	
	/**********************************Tests of ContainerSupportHelper (DAO Helper)***************************************************/	
	
	@Test
	public void validateGetContainerSupportTube(){
		LocationOnContainerSupport locs = ContainerSupportHelper.getContainerSupportTube("TEST_GetContainerSupportTube");
		
		assertThat(locs.categoryCode).isEqualTo("tube");
		assertThat(locs.code).isEqualTo("TEST_GetContainerSupportTube");
		assertThat(locs.column).isEqualTo("1");
		assertThat(locs.line).isEqualTo("1");
	}
	
	@Test
	public void validationGetContainerSupport() {
		LocationOnContainerSupport locs = null;
		try {
			locs = ContainerSupportHelper
					.getContainerSupport("lane", 5, "TEST_GetContainerSupport","1","8");
		} catch (DAOException e) {
			// TODO Auto-generated catch block
			Logger.error("DAO error: "+e.getMessage(),e);
		}
		
		assertThat(locs.categoryCode).isEqualTo("flowcell-8");
		assertThat(locs.code).isEqualTo("TEST_GetContainerSupport");
		assertThat(locs.column).isEqualTo("1");
		assertThat(locs.line).isEqualTo("8");		
	}
	
	@Test
	public void validateCreateContainerSupport(){
		PropertySingleValue  testProperty = new PropertySingleValue("testValue");
		ContainerSupport cs = ContainerSupportHelper.createContainerSupport("TEST_CreateContainerSupport", testProperty, "flowcell-2", "TEST_User");
		
		assertThat(cs.code).isEqualTo("TEST_CreateContainerSupport");
		assertThat(cs.categoryCode).isEqualTo("flowcell-2");
		assertThat(cs.state.code).isEqualTo("N");
		assertThat(cs.state.user).isEqualTo("TEST_User");
		Date d = new Date();
		DateFormat df = new SimpleDateFormat("yyyyMMdd");		
		
		assertThat(df.format(cs.state.date)).isEqualTo(df.format(d));
		assertThat(cs.valuation.valid).isEqualTo(TBoolean.UNSET);		
	}
	
	/**********************************Tests of ContainerSupport class methods (DBObject)***************************************************/		
	//None			
	/**********************************Tests of ContainerSupports class methods (Controller)***************************************************/	
	
	@Test
	public void validateGet() {
		ContainerSupport cs = MongoDBDAO.save(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupportTestHelper.getFakeContainerSupportWithCode("validateGetTEST"));
		assertThat(status(ContainerSupports.get("validateGetTEST"))).isEqualTo(play.mvc.Http.Status.OK);	
		MongoDBDAO.delete(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME,cs);
	}
	
	@Test
	public void valideGetNotFound() {		
		assertThat(status(ContainerSupports.get("not found"))).isEqualTo(play.mvc.Http.Status.NOT_FOUND);		
	}
	
	@Test
	public void validateHead() {
		ContainerSupport cs = MongoDBDAO.save(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupportTestHelper.getFakeContainerSupportWithCode("validateHeadTEST"));
		assertThat(status(ContainerSupports.head("validateHeadTEST"))).isEqualTo(play.mvc.Http.Status.OK);	
		MongoDBDAO.delete(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME,cs);
	}
	
	@Test
	public void validateHeadNotFound() {		
		assertThat(status(ContainerSupports.head("Not found"))).isEqualTo(play.mvc.Http.Status.NOT_FOUND);			
	}
	
	@Test
	public void validateListWithDatatable() {
		ContainerSupportsSearchForm ssf = ContainerSupportTestHelper.getFakeContainerSupportsSearchForm();
		DatatableResponseForTest<ContainerSupport> dcs = new DatatableResponseForTest<ContainerSupport>();
		List<ContainerSupport> lcs = new ArrayList<ContainerSupport>();
		MapperHelper mh = new MapperHelper();
		ssf.datatable = true;
		
		//Test with categoryCode (good categoryCode)
		ssf.categoryCode = "tube";
		Result result = callAction(controllers.containers.api.routes.ref.ContainerSupports.list(), fakeRequest(play.test.Helpers.GET, "?datatable="+String.valueOf(ssf.datatable)+"&categoryCode="+ssf.categoryCode));
		assertThat(status(result)).isEqualTo(play.mvc.Http.Status.OK);
		
		dcs = mh.convertValue(mh.resultToJsNode(result), new TypeReference<DatatableResponseForTest<ContainerSupport>>(){});
		lcs = dcs.data;		
		for(int i=0; i<lcs.size();i++){
			assertThat(lcs.get(i).categoryCode).isEqualTo(ssf.categoryCode);
		}		
		
		//Test with categoryCode (bad categoryCode)
		ssf.categoryCode = "badCategoryCode";
		result = callAction(controllers.containers.api.routes.ref.ContainerSupports.list(), fakeRequest(play.test.Helpers.GET, "?datatable="+String.valueOf(ssf.datatable)+"&categoryCode="+ssf.categoryCode));
		assertThat(status(result)).isEqualTo(play.mvc.Http.Status.OK);
		
		dcs = mh.convertValue(mh.resultToJsNode(result), new TypeReference<DatatableResponseForTest<ContainerSupport>>(){});
		lcs = dcs.data;		
		assertThat(lcs).isNullOrEmpty();
		
		//Test with fromTransformationTypeCodes (good fromTransformationTypeCodes)		
		ssf.fromTransformationTypeCodes = new ArrayList<String>();
		ssf.fromTransformationTypeCodes.add("solution-stock");		
		result = callAction(controllers.containers.api.routes.ref.ContainerSupports.list(), fakeRequest(play.test.Helpers.GET, "?datatable="+String.valueOf(ssf.datatable)+"&fromTransformationTypeCodes="+ssf.fromTransformationTypeCodes.get(0)));
		assertThat(status(result)).isEqualTo(play.mvc.Http.Status.OK);	
		
		dcs = mh.convertValue(mh.resultToJsNode(result), new TypeReference<DatatableResponseForTest<ContainerSupport>>(){});
		lcs = dcs.data;
		Logger.info("");
		for(int i=0; i<lcs.size();i++){
			assertThat(lcs.get(i).fromTransformationTypeCodes).contains(ssf.fromTransformationTypeCodes.get(0));
			Logger.info("");
		}	
		
		//Test with fromTransformationTypeCodes (bad fromTransformationTypeCodes)
		ssf.fromTransformationTypeCodes = new ArrayList<String>();
		ssf.fromTransformationTypeCodes.add("badFromExperimentTypeCodes");		
		result = callAction(controllers.containers.api.routes.ref.ContainerSupports.list(), fakeRequest(play.test.Helpers.GET, "?datatable="+String.valueOf(ssf.datatable)+"&fromTransformationTypeCodes="+ssf.fromTransformationTypeCodes.get(0)));
		assertThat(status(result)).isEqualTo(play.mvc.Http.Status.OK);
		
		dcs = mh.convertValue(mh.resultToJsNode(result), new TypeReference<DatatableResponseForTest<ContainerSupport>>(){});
		lcs = dcs.data;		
		assertThat(lcs).isEmpty();
		
	
		//Test with nextExperimentTypeCode (good nextExperimentTypeCode)		
		ssf.nextExperimentTypeCode="prepa-flowcell";
		result = callAction(controllers.containers.api.routes.ref.ContainerSupports.list(), fakeRequest(play.test.Helpers.GET, "?datatable="+String.valueOf(ssf.datatable)+"&nextExperimentTypeCode="+ssf.nextExperimentTypeCode));
		assertThat(status(result)).isEqualTo(play.mvc.Http.Status.OK);
		
		dcs = mh.convertValue(mh.resultToJsNode(result), new TypeReference<DatatableResponseForTest<ContainerSupport>>(){});
		lcs = dcs.data;
		Logger.info("");
		
		for(int i=0; i<lcs.size();i++){
			assertThat("solution-stock").isIn(lcs.get(i).fromTransformationTypeCodes);			
		}		
		
		
		//Test with nextExperimentTypeCode (bad nextExperimentTypeCode)
		ssf.nextExperimentTypeCode="badNextExperimentTypeCode";
		ssf.stateCode="";
		ssf.processTypeCode="";
		//ssf.valuations=null;
		Boolean exceptionError = false;
		result = null;
		
		try {
			result = callAction(controllers.containers.api.routes.ref.ContainerSupports.list(), fakeRequest(play.test.Helpers.GET, "?datatable="+String.valueOf(ssf.datatable)+"&nextExperimentTypeCode="+ssf.nextExperimentTypeCode));
		} catch (RuntimeException e) {			
			exceptionError = true;
		}
		assertThat(result).isNotNull();
		assertThat(exceptionError).isEqualTo(false);		
		
	}
	
	@Test
	public void validateListWithList() {
		ContainerSupportsSearchForm ssf = ContainerSupportTestHelper.getFakeContainerSupportsSearchForm();
		ssf.list = true;	
		MapperHelper mh = new MapperHelper();
		ListObject lo = new ListObject();
		List <ListObject> lc = new ArrayList<ListObject>();
		
		//Test with projectCodes (good projectCodes)
		ssf.projectCodes = new ArrayList<String>();
		ssf.projectCodes.add("BBA");
		Result result = callAction(controllers.containers.api.routes.ref.ContainerSupports.list(), fakeRequest(play.test.Helpers.GET, "?list="+String.valueOf(ssf.list)+"&projectCodes="+ssf.projectCodes.get(0)));
		assertThat(status(result)).isEqualTo(play.mvc.Http.Status.OK);
		
		lc = mh.convertValue(mh.resultToJsNode(result), new TypeReference<ArrayList<ListObject>>(){});
		for (int i=0;i<lc.size();i++){
			lo = lc.get(i);
			assertThat(ssf.projectCodes.get(0)).isIn((MongoDBDAO.findByCode(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class, lo.code)).projectCodes);					
			Logger.info("");
		}
		
		//Test with projectCodes (bad projectCodes)	
		ssf.projectCodes = new ArrayList<String>();
		ssf.projectCodes.add("badProjectCodes");
		result = callAction(controllers.containers.api.routes.ref.ContainerSupports.list(), fakeRequest(play.test.Helpers.GET, "?list="+String.valueOf(ssf.list)+"&projectCodes="+ssf.projectCodes.get(0)));
		assertThat(status(result)).isEqualTo(play.mvc.Http.Status.OK);
		
		lc = mh.convertValue(mh.resultToJsNode(result), new TypeReference<ArrayList<ListObject>>(){});
		assertThat(lc).isNullOrEmpty();	
		
		//Test with processTypeCode (good processTypeCode)
		ssf.processTypeCode = "illumina-run";
		ssf.nextExperimentTypeCode="prepa-flowcell";
		
		result = callAction(controllers.containers.api.routes.ref.ContainerSupports.list(), fakeRequest(play.test.Helpers.GET, "?list="+String.valueOf(ssf.list)+"&nextExperimentTypeCode="+ssf.nextExperimentTypeCode+"&processTypeCode="+ssf.processTypeCode));
		assertThat(status(result)).isEqualTo(play.mvc.Http.Status.OK);
		
		lc = mh.convertValue(mh.resultToJsNode(result), new TypeReference<ArrayList<ListObject>>(){});
		List<Container> l = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.in("processTypeCodes", ssf.processTypeCode)).toList();
		List<String> lst = new ArrayList<String>();
		for(Container c:l){
			lst.add(c.support.code);
		}
		
		for (int i=0;i<lc.size();i++){
			lo = lc.get(i);
			Logger.info("");
			assertThat(lst).contains(lo.code);
			Logger.info("");
		}
		
		
		//Test with processTypeCode (bad processTypeCode)		
		ssf.processTypeCode = "badProcessTypeCode";
		ssf.nextExperimentTypeCode="badNextExperimentTypeCode";
		Boolean exceptionError = false;
		result = null;
		
		try {
			result = callAction(controllers.containers.api.routes.ref.ContainerSupports.list(), fakeRequest(play.test.Helpers.GET, "?list="+String.valueOf(ssf.list)+"&nextExperimentTypeCode="+ssf.nextExperimentTypeCode+"&processTypeCode="+ssf.processTypeCode));
		}  catch (RuntimeException e) {			
			exceptionError = true;
		}
		assertThat(result).isNotNull();
		assertThat(exceptionError).isEqualTo(false);		
	
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void validateList() {
		ContainerSupportsSearchForm ssf = ContainerSupportTestHelper.getFakeContainerSupportsSearchForm();
		ssf.datatable = false;
		ssf.list = false;
		MapperHelper mh = new MapperHelper();
		ContainerSupport cs = new ContainerSupport();
		List<ContainerSupport> lcs = new ArrayList<ContainerSupport>();		
			
		//Test with dates (matched period)
		ssf.fromDate = new Date(2014-1900, 11, 16);
		ssf.toDate = new Date(2014-1900, 11, 18);
		
		Result result = callAction(controllers.containers.api.routes.ref.ContainerSupports.list(), fakeRequest(play.test.Helpers.GET, "?list="+String.valueOf(ssf.list)+"&fromDate="+ssf.fromDate.getTime()+"&toDate="+ssf.toDate.getTime()));
		assertThat(status(result)).isEqualTo(play.mvc.Http.Status.OK);
		
		lcs = mh.convertValue(mh.resultToJsNode(result), new TypeReference<ArrayList<ContainerSupport>>(){});
		for (int i=0;i<lcs.size();i++){
			cs = lcs.get(i);
			assertThat(cs.traceInformation.creationDate).isBetween(ssf.fromDate, ssf.toDate, true,true);					
			Logger.info("");
		}
		
		//Test with dates (unmatched period)
		ssf.fromDate = new Date(2014-1900, 11, 24);
		ssf.toDate = new Date(2015-1900, 0, 4);
		result = callAction(controllers.containers.api.routes.ref.ContainerSupports.list(), fakeRequest(play.test.Helpers.GET, "?list="+String.valueOf(ssf.list)+"&fromDate="+ssf.fromDate.getTime()+"&toDate="+ssf.toDate.getTime()));
		assertThat(status(result)).isEqualTo(play.mvc.Http.Status.OK);
		
		lcs = mh.convertValue(mh.resultToJsNode(result), new TypeReference<ArrayList<ContainerSupport>>(){});
		assertThat(lcs).isNullOrEmpty();	
		
		//Test with regex (matched pattern)
		ssf.codeRegex="^B.*1$";
		result = callAction(controllers.containers.api.routes.ref.ContainerSupports.list(), fakeRequest(play.test.Helpers.GET, "?list="+String.valueOf(ssf.list)+"&codeRegex="+ssf.codeRegex));
		assertThat(status(result)).isEqualTo(play.mvc.Http.Status.OK);
		
		lcs = mh.convertValue(mh.resultToJsNode(result), new TypeReference<ArrayList<ContainerSupport>>(){});
		Pattern p = Pattern.compile(ssf.codeRegex);
		for (int i=0;i<lcs.size();i++){
			cs = lcs.get(i);
			assertThat(cs.code).matches(p);					
			Logger.info("");
		}
		
		//Test with regex (unmatched pattern)
		ssf.codeRegex="unmatched";
		result = callAction(controllers.containers.api.routes.ref.ContainerSupports.list(), fakeRequest(play.test.Helpers.GET, "?list="+String.valueOf(ssf.list)+"&codeRegex="+ssf.codeRegex));
		assertThat(status(result)).isEqualTo(play.mvc.Http.Status.OK);
		
		lcs = mh.convertValue(mh.resultToJsNode(result), new TypeReference<ArrayList<ContainerSupport>>(){});
		assertThat(lcs).isNullOrEmpty();
		
		//Test with stateCode (good stateCode)		
		ssf.stateCode="IW-P";
		result = callAction(controllers.containers.api.routes.ref.ContainerSupports.list(), fakeRequest(play.test.Helpers.GET, "?list="+String.valueOf(ssf.list)+"&stateCode="+ssf.stateCode));
		assertThat(status(result)).isEqualTo(play.mvc.Http.Status.OK);
		
		lcs = mh.convertValue(mh.resultToJsNode(result), new TypeReference<ArrayList<ContainerSupport>>(){});	
		for (int i=0;i<lcs.size();i++){
			cs = lcs.get(i);
			assertThat(cs.state.code).isEqualTo(ssf.stateCode);					
			Logger.info("");
		}
		
		//Test with stateCode (bad stateCode)
		ssf.stateCode="BadStateCode";
		result = callAction(controllers.containers.api.routes.ref.ContainerSupports.list(), fakeRequest(play.test.Helpers.GET, "?list="+String.valueOf(ssf.list)+"&stateCode="+ssf.stateCode));
		assertThat(status(result)).isEqualTo(play.mvc.Http.Status.OK);
		
		lcs = mh.convertValue(mh.resultToJsNode(result), new TypeReference<ArrayList<ContainerSupport>>(){});	
		assertThat(lcs).isNullOrEmpty();
		
	}
	
	@Test
	public void validateUpdateBatch() {
		
	}
	
	@Test
	public void validateUpdateStateCode() {
		
	}
	
	/*
	@Test
	public void test() {
		
	}
	*/
	
}
