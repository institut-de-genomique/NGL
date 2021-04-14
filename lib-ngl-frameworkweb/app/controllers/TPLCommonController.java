package controllers;


import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.With;
import play.mvc.Http.Context;

@With({fr.cea.ig.authentication.Authenticate.class})
public abstract class TPLCommonController extends Controller {
	
	/*
	 * Fill a form in json mode
	 * @param form
	 * @param clazz
	 * @return
	 */
	protected <P> Form<P> getFilledForm(Form<P> form, Class<P> clazz) {		
		JsonNode json = request().body().asJson();
		P input = Json.fromJson(json, clazz);
		Form<P> filledForm = form.fill(input); 
		return filledForm;
	}
	
	protected void fillDataWith(Map<String, Object> data, Map<String, String[]> urlFormEncoded) {
		urlFormEncoded.forEach((key, values) -> {
			if (key.endsWith("[]")) {
				String k = key.substring(0, key.length() - 2);
				for (int i = 0; i < values.length; i++) {
					data.put(k + "[" + i + "]", values[i]);
				}
			} else if (values.length > 0) {
				data.put(key, values[0]);
			}
		});
	}
	
	protected String getCurrentUser() {
		return fr.cea.ig.authentication.Authentication.getUser(Context.current().session());
	}
	
}
