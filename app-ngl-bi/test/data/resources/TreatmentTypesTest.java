package data.resources;

//import ngl.bi.AbstractBIServerTest;
import static ngl.bi.Global.af;
import static org.fest.assertions.Assertions.assertThat;

import org.junit.Test;

import play.libs.ws.WSResponse;

//@FixMethodOrder(MethodSorters.NAME_ASCENDING)
//public class TreatmentTypesTest extends AbstractBIServerTest {
public class TreatmentTypesTest {

	private static final play.Logger.ALogger logger = play.Logger.of(TreatmentTypesTest.class);

//	@Test
//	public void test1list()
//	{
//		Logger.debug("list TreatmentType");
//		WSResponse response = WSHelper.get(ws, "/api/treatment-types", 200);
//		assertThat(response.asJson()).isNotNull();
//	}
//
//	@Test
//	public void test2get()
//	{
//		Logger.debug("get TreatmentType");
//		WSResponse response = WSHelper.get(ws, "/api/treatment-types/ngsrg-illumina", 200);
//		assertThat(response.asJson()).isNotNull();
//	}
	
	@Test
	public void test1list()	{
		af.ws(ws -> {
			logger.debug("list TreatmentType");
//			WSResponse response = WSHelper.get(ws, "/api/treatment-types", 200);
			WSResponse response = ws.get("/api/treatment-types", 200);
			assertThat(response.asJson()).isNotNull();
		});
	}

	@Test
	public void test2get() {
		af.ws(ws -> {
			logger.debug("get TreatmentType");
//			WSResponse response = WSHelper.get(ws, "/api/treatment-types/ngsrg-illumina", 200);
			WSResponse response = ws.get("/api/treatment-types/ngsrg-illumina", 200);
			assertThat(response.asJson()).isNotNull();
		});
	}
		
}
