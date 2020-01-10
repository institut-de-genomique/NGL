import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.BAD_REQUEST;
import static play.test.Helpers.callAction;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.status;
import static play.test.Helpers.contentAsString;

import org.junit.Test;

import play.mvc.Result;
import utils.AbstractTests;


import static play.mvc.Http.Status.OK;


public class ControllersTest extends AbstractTests{

	@Test
	public void tplProjectsList(){
		Result result = callAction(controllers.combo.api.routes.ref.Lists.projects(),fakeRequest());
		//System.err.println(contentAsString(result));
		assertThat(status(result)).isEqualTo(OK);
		assertThat(contentAsString(result).length()).isGreaterThan(2);
	}
	
	@Test
	public void tplSamplesList(){
		Result result = callAction(controllers.combo.api.routes.ref.Lists.samples(),fakeRequest());
		//System.err.println(contentAsString(result));
		assertThat(status(result)).isEqualTo(OK);
		assertThat(contentAsString(result).length()).isGreaterThan(2);
	}
	
	@Test
	public void tplEtmanipsList(){
			Result result = callAction(controllers.combo.api.routes.ref.Lists.etmanips(),fakeRequest());
			//System.err.println(contentAsString(result));
			assertThat(status(result)).isEqualTo(OK);
			assertThat(contentAsString(result).length()).isGreaterThan(2);
		}
	
	@Test
	public void tplEtmaterielsList(){
			Result result = callAction(controllers.combo.api.routes.ref.Lists.etmateriels(),fakeRequest());
			//System.err.println(contentAsString(result));
			assertThat(status(result)).isEqualTo(OK);
			assertThat(contentAsString(result).length()).isGreaterThan(2);
		}

}
