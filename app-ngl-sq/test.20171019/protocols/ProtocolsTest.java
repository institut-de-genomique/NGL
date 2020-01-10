package protocols;

import static org.assertj.core.api.Assertions.assertThat;
import static play.test.Helpers.callAction;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.status;


import java.util.ArrayList;
import java.util.List;

import models.laboratory.protocol.instance.Protocol;
import models.utils.InstanceConstants;
import models.utils.ListObject;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import play.Logger;
import play.Logger.ALogger;
import play.mvc.Result;
import utils.AbstractTests;
import utils.Constants;
import utils.DatatableResponseForTest;
import utils.InitDataHelper;
import utils.MapperHelper;
import validation.ContextValidation;



import com.fasterxml.jackson.core.type.TypeReference;

import controllers.protocols.api.Protocols;
import controllers.protocols.api.ProtocolsSearchForm;
import fr.cea.ig.MongoDBDAO;

public class ProtocolsTest extends AbstractTests {

	@BeforeClass
	public static void initData() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		InitDataHelper.initForProcessesTest();
	}

	@AfterClass
	public static void resetData(){
		InitDataHelper.endTest();
	}	

	protected static ALogger logger=Logger.of("ProtocolsTest");

	/**********************************Tests of (DAO Helper)***************************************************/
	//NONE	

	/**********************************Tests of  class methods (DBObject)***************************************************/		

	@Test
	public void validateSetExperimentTypeCodesAndExperimentValidation(){
		ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
		Protocol protocol = ProtocolTestHelper.getFakeProtocol("validateSetExperimentTypeCodesAndExperimentValidation");
		protocol.categoryCode="development";
		protocol._id=MongoDBDAO.save(InstanceConstants.PROTOCOL_COLL_NAME, protocol)._id;
		//good values
		protocol.experimentTypeCodes.add("illumina-depot");
		protocol.experimentTypeCodes.add("opgen-depot");
		protocol.experimentTypeCodes.add("prepa-flowcell");
		assertThat("illumina-depot").isIn(protocol.experimentTypeCodes);
		assertThat("opgen-depot").isIn(protocol.experimentTypeCodes);
		assertThat("prepa-flowcell").isIn(protocol.experimentTypeCodes);

		protocol.validate(contextValidation);
		assertThat(contextValidation.hasErrors()).isFalse();

		//bad value
		protocol.categoryCode="badCategoryCode";
		protocol.validate(contextValidation);
		assertThat(contextValidation.hasErrors()).isTrue();
		
		protocol.categoryCode="production";
		protocol.experimentTypeCodes.add("fakeGoodExperiment");
		assertThat("fakeGoodExperiment").isIn(protocol.experimentTypeCodes);

		protocol.validate(contextValidation);
		assertThat(contextValidation.hasErrors()).isTrue();		

		MongoDBDAO.delete(InstanceConstants.PROTOCOL_COLL_NAME, protocol);
	}



	/**********************************Tests of  class methods (Controller)***************************************************/	

	@Test
	public void validateGet() {

		Protocol p = MongoDBDAO.save(InstanceConstants.PROTOCOL_COLL_NAME, ProtocolTestHelper.getFakeProtocol("validateGet"));
		assertThat(status(Protocols.get("validateGet"))).isEqualTo(play.mvc.Http.Status.OK);

		MongoDBDAO.delete(InstanceConstants.PROTOCOL_COLL_NAME, p);
	}

	@Test
	public void valideGetNotFound() {		
		assertThat(status(Protocols.get("Not Found"))).isEqualTo(play.mvc.Http.Status.NOT_FOUND);
	}

	@Test
	public void validateListWithDatatable() {

		ProtocolsSearchForm psf = ProtocolTestHelper.getFakeProtocolsSearchForm();
		psf.datatable = true;
		Protocol p = new Protocol();
		MapperHelper mh = new MapperHelper();
		List<Protocol> lp = new ArrayList<Protocol>();
		DatatableResponseForTest<Protocol> dr = new DatatableResponseForTest<Protocol>();

		//Test with experimentTypeCodes(good experimentTypeCodes)
		psf.experimentTypeCodes = new ArrayList<String>();
		psf.experimentTypeCodes.add("amplification");
		psf.experimentTypeCodes.add("solution-stock");

		Result result = callAction(controllers.protocols.api.routes.ref.Protocols.list(), 
				fakeRequest(play.test.Helpers.GET, "?datatable="+String.valueOf(psf.datatable)+
						"&experimentTypeCodes="+psf.experimentTypeCodes.get(0)+"&experimentTypeCodes="+psf.experimentTypeCodes.get(1)));
		assertThat(status(result)).isEqualTo(play.mvc.Http.Status.OK);

		dr = mh.convertValue(mh.resultToJsNode(result), new TypeReference<DatatableResponseForTest<Protocol>>(){});
		lp = dr.data;
		assertThat(lp).isNotEmpty();		
		assertThat(lp.get(0)).isEqualToComparingFieldByField(MongoDBDAO.findByCode(InstanceConstants.PROTOCOL_COLL_NAME, Protocol.class,"amplif_ptr_sox144_1"));

		//Test with experimentTypeCodes(bad experimentTypeCodes)
		psf.experimentTypeCodes = new ArrayList<String>();
		psf.experimentTypeCodes.add("badExperimentTypeCodes1");
		psf.experimentTypeCodes.add("badExperimentTypeCodes2");
		result = callAction(controllers.protocols.api.routes.ref.Protocols.list(), 
				fakeRequest(play.test.Helpers.GET, "?datatable="+String.valueOf(psf.datatable)+
						"&experimentTypeCodes="+psf.experimentTypeCodes.get(0)+"&experimentTypeCodes="+psf.experimentTypeCodes.get(1)));
		assertThat(status(result)).isEqualTo(play.mvc.Http.Status.OK);

		dr = mh.convertValue(mh.resultToJsNode(result), new TypeReference<DatatableResponseForTest<Protocol>>(){});
		lp = dr.data;
		assertThat(lp).isNullOrEmpty();

	}

	@Test
	public void validateListWithList() {
		ProtocolsSearchForm psf = ProtocolTestHelper.getFakeProtocolsSearchForm();
		psf.list = true;		
		MapperHelper mh = new MapperHelper();
		ListObject lo = new ListObject();
		List <ListObject> lc = new ArrayList<ListObject>();

		//Test with experimentTypeCode(good experimentTypeCode)
		psf.experimentTypeCode = "fragmentation";

		Result result = callAction(controllers.protocols.api.routes.ref.Protocols.list(), 
				fakeRequest(play.test.Helpers.GET, "?list="+String.valueOf(psf.list)+
						"&experimentTypeCode="+psf.experimentTypeCode));
		assertThat(status(result)).isEqualTo(play.mvc.Http.Status.OK);

		lc = mh.convertValue(mh.resultToJsNode(result), new TypeReference<ArrayList<ListObject>>(){});
		assertThat(lc).isNotEmpty();

		lo.code="fragmentation_ptr_sox140_1";
		lo.name="Fragmentation_ptr_sox140_1";
		assertThat(lc.get(0)).isEqualToComparingFieldByField(lo);		

		//Test with experimentTypeCode(bad experimentTypeCode)
		psf.experimentTypeCode = "badExperimentTypeCode";

		result = callAction(controllers.protocols.api.routes.ref.Protocols.list(), 
				fakeRequest(play.test.Helpers.GET, "?list="+String.valueOf(psf.list)+
						"&experimentTypeCode="+psf.experimentTypeCode));
		assertThat(status(result)).isEqualTo(play.mvc.Http.Status.OK);

		lc = mh.convertValue(mh.resultToJsNode(result), new TypeReference<ArrayList<ListObject>>(){});
		assertThat(lc).isNullOrEmpty();

	}


	@Test
	public void validateList() {
		ProtocolsSearchForm psf = ProtocolTestHelper.getFakeProtocolsSearchForm();
		psf.list = false;		
		MapperHelper mh = new MapperHelper();
		List<Protocol> lp = new ArrayList<Protocol>();

		//Test with experimentTypeCode(good experimentTypeCode)
		psf.experimentTypeCode = "prepa-flowcell";
		Result result = callAction(controllers.protocols.api.routes.ref.Protocols.list(), 
				fakeRequest(play.test.Helpers.GET, "?list="+String.valueOf(psf.list)+
						"&experimentTypeCode="+psf.experimentTypeCode));
		assertThat(status(result)).isEqualTo(play.mvc.Http.Status.OK);

		lp = mh.convertValue(mh.resultToJsNode(result), new TypeReference<ArrayList<Protocol>>(){});
		assertThat(lp).isNotEmpty();

		for(Protocol p:lp){			
			assertThat(p.experimentTypeCodes).contains("prepa-flowcell");
		}

		//Test with experimentTypeCode(bad experimentTypeCode)
		psf.experimentTypeCode = "badExperimentTypeCode";
		result = callAction(controllers.protocols.api.routes.ref.Protocols.list(), 
				fakeRequest(play.test.Helpers.GET, "?list="+String.valueOf(psf.list)+
						"&experimentTypeCode="+psf.experimentTypeCode));
		assertThat(status(result)).isEqualTo(play.mvc.Http.Status.OK);
		
		lp = mh.convertValue(mh.resultToJsNode(result), new TypeReference<ArrayList<Protocol>>(){});
		assertThat(lp).isNullOrEmpty();

	}
	
	/**********************************Tests of  rules (Drools)***************************************************/	

	@Test
	public void validateEstimatedPercentProcessRule() {
		
		

	}
	
	/*
	@Test
	public void test() {

	}
	 */
}
