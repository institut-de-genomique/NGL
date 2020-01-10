package utils;

import java.util.List;

import models.laboratory.container.instance.Container;
import play.libs.Json;
import play.mvc.Result;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;


public class ContainerBatchElementHelper{

	public static List<DatatableBatchResponseElementForTest<Container>> getElementListObjectMapper(Result result){
		MapperHelper mapper = new MapperHelper();					
		return mapper.convertValue(mapper.resultToJsNode(result), new TypeReference<List<DatatableBatchResponseElementForTest<Container>>>() {
		});
	}


}
