package fr.cea.ig.lfw.support;

import static play.mvc.Http.Context.Implicit.request;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.bson.BSONObject;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObject;

import fr.cea.ig.lfw.LFWApplicationHolder;
import play.libs.Json;
import views.components.datatable.IDatatableForm;

/**
 * HTTP request parsing. 
 * thing.
 * 
 * @author vrd
 *
 */
public interface LFWRequestParsing extends LFWApplicationHolder {

	// This is extracted from common controller
	
	/**
	 * Fill a form from the request query string. <br>
	 * The method throws a RuntimeException if an exception if catch during the reflection.
	 * @param  <T>   type of object to build
	 * @param  clazz class of form
	 * @return       the filled form
	 */
	// extracted from APICommonController
	default <T> T objectFromRequestQueryString(Class<T> clazz) {		
		try {
			Map<String, String[]> queryString = request().queryString();
			T instance = clazz.newInstance();
//			BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(clazz.newInstance());
			BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(instance);
			wrapper.setAutoGrowNestedPaths(true);
			for (String key : queryString.keySet()) {
				try {
					if (isNotEmpty(queryString.get(key))) {
						Object value = queryString.get(key);
						if (wrapper.isWritableProperty(key)) {
							Class<?> c = wrapper.getPropertyType(key);
							if (c != null && Date.class.isAssignableFrom(c)) {
								value = convertTimestampStringToDate(((String[])value)[0]);
							}							
						}
						wrapper.setPropertyValue(key, value);
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				} 
			}
//			@SuppressWarnings("unchecked")
//			T instance = (T)wrapper.getWrappedInstance();
			return instance; 
		} catch (Exception e) {
			throw new RuntimeException(e);
		} 
	}
	
	default Date convertTimestampStringToDate(String timestamp) {
		return new Date(Long.valueOf(timestamp));
	}
	
	default <T> T objectFromRequestBody(Class<T> clazz) {
		return Json.fromJson(request().body().asJson(),clazz);
	}
	
	default <T> List<T> objectListFromRequestBody(Class<T> clazz) {		
		JsonNode json = request().body().asJson();
		List<T> results = new ArrayList<>();
		json.forEach( n -> results.add(Json.fromJson(n, clazz)));
		return results;
	}

	// what if there is more than one string in the array ?
	default boolean isNotEmpty(String[] strings) {
		if (strings == null)     return false;
		if (strings.length == 0) return false;
		if (strings.length == 1 && StringUtils.isBlank(strings[0])) return false;
		return true;
	}
	
	default BasicDBObject generateBasicDBObjectFromKeys(IDatatableForm form) {
		BasicDBObject keys = new BasicDBObject();
		if(!form.includes().contains("*")){
			keys.putAll((BSONObject)getIncludeKeys(form.includes().toArray(new String[form.includes().size()])));
		}
		keys.putAll((BSONObject)getExcludeKeys(form.excludes().toArray(new String[form.excludes().size()])));		
		return keys;
	}
	
	default BasicDBObject getIncludeKeys(String[] keys) {
		Arrays.sort(keys, Collections.reverseOrder());
		BasicDBObject values = new BasicDBObject();
		for(String key : keys){
		    values.put(key, 1);
		}
		return values;
    }
	
	default BasicDBObject getExcludeKeys(String[] keys) {
		Arrays.sort(keys, Collections.reverseOrder());
		BasicDBObject values = new BasicDBObject();
		for (String key : keys) {
		    values.put(key, 0);
		}
		return values;
    }
	
	default String generateJSONKeys(IDatatableForm form) {
        Set<String> keys = new HashSet<>();
        if(null != form.includes() && form.includes().size() > 0 && !form.includes().contains("*")){
            getIncludeJSONKeys(form.includes().toArray(new String[form.includes().size()]),keys);
        }else if(null != form.excludes() && form.excludes().size() > 0){
            getExcludeJSONKeys(form.excludes().toArray(new String[form.excludes().size()]),keys);                   
        }
        String jsonKey = "{"+String.join(",", keys)+"}";
        return jsonKey;
    }
	
	default void getIncludeJSONKeys(String[] includes, Set<String> keys) {
        Arrays.sort(includes, Collections.reverseOrder());
        for (int i=0; i<includes.length; i++) {
            keys.add(includes[i]+":1");
        }
//        Iterables.zenThem(includes).each(k -> keys.add(k + ":1"));
    }
	
	default void getExcludeJSONKeys(String[] excludes, Set<String> keys) {
        Arrays.sort(excludes, Collections.reverseOrder());
        for (int i=0; i<excludes.length; i++) {
            keys.add(excludes[i]+":0");
        }
//        Iterables.zenThem(excludes).each(k -> keys.add(k + ":0"));
    }
	
}
