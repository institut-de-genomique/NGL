package utils;

import play.libs.Json;
import play.mvc.Result;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MapperHelper extends ObjectMapper{	
	
	public MapperHelper() {
		super();
		this.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
		this.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
		this.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
		this.configure(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY, true);
		this.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);
		this.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);		
	}
	
	//Convert a result to a JsonNode that can be use in converValue method
	public JsonNode resultToJsNode(Result result){			 		
		return 	Json.parse(play.test.Helpers.contentAsString(result));	
	}
	
	
	
	

}
