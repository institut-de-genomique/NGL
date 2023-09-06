import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.data.format.Formatters;
import play.mvc.Action;
import play.mvc.Http.Request;
import rules.services.RulesException;
import rules.services.RulesServices;


public class Global extends GlobalSettings {

	
	
	@Override
	public void onStart(Application app) {
		Logger.info("NGL-authorization start");
	}  

	@Override
	public void onStop(Application app) {
		Logger.info("NGL-authorization shutdown...");
	}  

	@Override
	public Action onRequest(Request request, Method actionMethod) {
		return new fr.cea.ig.authentication.Authenticate();
	}
}