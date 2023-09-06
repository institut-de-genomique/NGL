package controllers;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

import com.fasterxml.jackson.databind.JsonNode;

import controllers.history.UserHistory;
import fr.cea.ig.lfw.LFWApplication;
import play.data.DynamicForm;
import play.data.Form;
import play.libs.Json;
import play.mvc.With;

@With({fr.cea.ig.authentication.Authenticate.class, UserHistory.class})
public abstract class APICommonController<T> extends NGLBaseController {

//	private static final play.Logger.ALogger logger = play.Logger.of(APICommonController.class);
	
	protected final DynamicForm listForm;
	protected final Class<T> type;
	private final Form<T> mainForm; 
	
	public APICommonController(LFWApplication ctx, Class<T> type) {
		super(ctx);
		this.type = type;
		listForm = ctx.form();
		mainForm = ctx.form(type);
	}

	public final LFWApplication getNGLContext(){
		return app;
	}
	
	/*
	 * Filled the main form
	 * @return
	 */
	protected Form<T> getMainFilledForm() {
		return getFilledForm(mainForm, type); 
	}
	
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
	
	protected <P> List<Form<P>> getFilledFormList(Form<P> form, Class<P> clazz) {		
		JsonNode json = request().body().asJson();
		List<Form<P>> results = new ArrayList<>();
		Iterator<JsonNode> iterator = json.elements();
		
		while (iterator.hasNext()) {
			JsonNode jsonChild = iterator.next();
			P input = Json.fromJson(jsonChild, clazz);
			Form<P> filledForm = form.fill(input);
			results.add(filledForm);
		}
		
		return results;
	}

	/*
	 * Returns a form built from the query string.
	 * @param form  form to fill
	 * @param clazz type of the form
	 * @return      filled form
	 */
	protected <A> Form<A> filledFormQueryString(Form<A> form, Class<A> clazz) {		
		Map<String, String[]> queryString =request().queryString();
		Map<String, Object> transformMap = new HashMap<>();
		for (String key :queryString.keySet()) {				
			//Logger.error("dans filledFormQueryString, key = " + key);
			//Logger.error("dans filledFormQueryString, queryString.get(key) = " + queryString.get(key)[0]);
			try {
				if (isNotEmpty(queryString.get(key))) {	
					//Logger.error("queryString.get(key)[0]   = " + queryString.get(key)[0]);
					Field field = clazz.getField(key);
					//Logger.error("field.getName()  = " + field.getName());
					Class<?> type = field.getType();
					if (type.isArray() || Collection.class.isAssignableFrom(type)) {
						//Logger.error("field.getType  is ARRAY " );
						transformMap.put(key, queryString.get(key));						
					} else {
						//Logger.error("field.getType  is NOT ARRAY " );
						transformMap.put(key, queryString.get(key)[0]);						
					}
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			} 
		}
		JsonNode json = Json.toJson(transformMap);
		A input = Json.fromJson(json, clazz);
		Form<A> filledForm = form.fill(input); 
		return filledForm;
	}
	
	/*
	 * Returns a form built from the query string.
	 * @param clazz type of the form to build
	 * @return      built form
	 */
	protected <A> Form<A> getQueryStringForm(Class<A> clazz) {
		return filledFormQueryString(app.form(clazz),clazz);
	}

	/*
	 * Fill a form from the request query string
	 * @param clazz
	 * @return
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	protected <U> U filledFormQueryString(Class<U> clazz) {		
		try {
			Map<String, String[]> queryString = request().queryString();
			U wrapped = clazz.newInstance();
			BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(wrapped);
			wrapper.setAutoGrowNestedPaths(true);
			for (String key :queryString.keySet()) {
				try {
					if (isNotEmpty(queryString.get(key))) {
						Object value = queryString.get(key);
						if(wrapper.isWritableProperty(key)){
							Class<?> c = wrapper.getPropertyType(key);
							// GA: used conversion spring system
							if(null != c && Date.class.isAssignableFrom(c)){
								//wrapper.setPropertyValue(key, new Date(Long.valueOf(value[0])));
								value = new Date(Long.valueOf(((String[])value)[0]));
							}							
						}
						wrapper.setPropertyValue(key, value);
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				} 
			}
			return wrapped;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} 
	}

	private boolean isNotEmpty(String[] strings) {
		if (strings == null)
			return false;
		if (strings.length == 0)
			return false;
		if (strings.length == 1 && StringUtils.isBlank(strings[0]))
			return false;
		return true;
	}

}
