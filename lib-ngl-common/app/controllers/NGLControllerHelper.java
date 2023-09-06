package controllers;

import static validation.utils.ValidationHelper.dynamicCast;
import static validation.utils.ValidationHelper.dynamicCastElements;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;

import models.laboratory.common.description.Level;
import models.laboratory.common.description.PropertyDefinition;
import models.utils.dao.DAOException;

public class NGLControllerHelper {
	
	public static Query generateQueriesForProperties(Map<String, List<String>> properties,
													 Level.CODE level, 
													 List<String> prefixPropertyPath) {
		
		List<Query> queryElts = prefixPropertyPath.stream()
				.map(prefix -> generateQueriesForProperties(properties, level, prefix))
				.flatMap(List::stream)
				.collect(Collectors.toList());
		
		return DBQuery.or(queryElts.toArray(new DBQuery.Query[queryElts.size()]));
	}
	
	public static List<Query> generateQueriesForProperties(Map<String, List<String>> properties,
			                                               Level.CODE level, 
			                                               String prefixPropertyPath) {
		List<Query> queries = new ArrayList<>();
		try {
			for (String keyValue : properties.keySet()) {
				String[] key = keyValue.split("\\|",2);
				// Replace ".value" to deal with object_list
				PropertyDefinition pd = PropertyDefinition.find.get().findUnique(key[0], level);
				List<String> stringValues = properties.get(keyValue);
				if (pd != null && CollectionUtils.isNotEmpty(stringValues)) {					
					Query subQueries = DBQuery.empty();
					//Define query property
					String property = null;
					if (pd.propertyValueType.equals("object_list"))
						property = prefixPropertyPath+"."+key[0].substring(0, key[0].indexOf("."))+".value."+key[0].substring(key[0].indexOf(".")+1);
					else
						property = prefixPropertyPath+"."+key[0]+".value";
					
					if (key.length == 1) {
//						List<Object> values = ValidationHelper.convertStringToType(pd.valueType, stringValues);
						List<Object> values = dynamicCastElements(pd.valueType, stringValues);
						// use $in because is more generic than $is and work to field of type array or single
						subQueries = DBQuery.in(property, values);
						// in case of property is not defined in the document ???
						if (Boolean.class.getName().equals(pd.valueType) && !((Boolean)values.get(0)).booleanValue()) {
							subQueries = DBQuery.or(subQueries, DBQuery.notExists(property));
						}						
					} else if (key.length > 1 && stringValues.size() == 1) {
						// the test ensures that StringValues.size() == 1 so this test could be removed
						// from the following if statements. It then follows that a string switch could be used.
						if (key[1].equals("regex")) {
							Pattern pattern = convertStringToPattern(stringValues.get(0));
							subQueries = DBQuery.regex(property, pattern);
							queries.add(subQueries);							
						} else if (key[1].equals("gte") && stringValues.size() == 1) {
//							Object value = ValidationHelper.convertStringToType(pd.valueType, stringValues.get(0));
							Object value = dynamicCast(pd.valueType, stringValues.get(0));
							subQueries = DBQuery.greaterThanEquals(property, value);							
						} else if (key[1].equals("gt") && stringValues.size() == 1) {
//							Object value = ValidationHelper.convertStringToType(pd.valueType, stringValues.get(0));
							Object value = dynamicCast(pd.valueType, stringValues.get(0));
							subQueries = DBQuery.greaterThan(property, value);							
						} else if (key[1].equals("lte") && stringValues.size() == 1) {
//							Object value = ValidationHelper.convertStringToType(pd.valueType, stringValues.get(0));
							Object value = dynamicCast(pd.valueType, stringValues.get(0));
							subQueries = DBQuery.lessThanEquals(property, value);							
						} else if (key[1].equals("lt") && stringValues.size() == 1) {
							Object value = dynamicCast(pd.valueType, stringValues.get(0));
							subQueries = DBQuery.lessThan(property, value);							
						} else if (key[1].equals("exists") && stringValues.size() == 1) {
							if ("TRUE".equals(stringValues.get(0).toUpperCase())) {
								subQueries = DBQuery.exists(property);
							} else if("FALSE".equals(stringValues.get(0).toUpperCase())) {
								subQueries = DBQuery.notExists(property);
							}
						} else {
							throw new RuntimeException("key[1] not valid : " + key[1]);
						}
					} else {
						throw new RuntimeException("key not valid : "+keyValue+" or stringValues.size != 1 / "+stringValues.size());
					}
					queries.add(subQueries);
				}				
			}
		} catch (DAOException e) {
			throw new RuntimeException(e);
		}
		return queries;
	}
	
	private static Pattern convertStringToPattern(String value) {
		return Pattern.compile(value);
	}
	
//	private static List<Pattern> convertStringToPatterns(List<String> values) {
//		List<Pattern> objects = new ArrayList<Pattern>(values.size());
//		for(String value : values){
//			objects.add(Pattern.compile(value));
//		}	
//		return objects;
//	}

	public static List<Query> generateQueriesForTreatmentProperties(Map<String, Map<String, 
			                                                        List<String>>> treatmentProperties, 
																	Level.CODE level, 
																	String prefixPropertyPath) {
		List<Query> queries = new ArrayList<>();
		for (String key : treatmentProperties.keySet()) {
			queries.addAll(generateQueriesForProperties(treatmentProperties.get(key), level, prefixPropertyPath+"."+key));			
		}
		return queries;
	}

	public static Collection<? extends Query> generateExistsQueriesForFields(Map<String, Boolean> existingFields) {
		List<Query> queries = new ArrayList<>();
		if (MapUtils.isNotEmpty(existingFields)) { //all
			for (String field : existingFields.keySet()) {
				if (Boolean.FALSE.equals(existingFields.get(field))) {
					queries.add(DBQuery.notExists(field));
				} else if(Boolean.TRUE.equals(existingFields.get(field))) {
					queries.add(DBQuery.exists(field));
				}
			}		
		}
		return queries;
	}
	
	public static Collection<? extends Query> generateQueriesForFields(Map<String, String> fieldsQueries) {
		List<Query> queries = new ArrayList<>();
		if (MapUtils.isNotEmpty(fieldsQueries)) {
			try {
				for (String keyValue : fieldsQueries.keySet()) {
					String[] keys = keyValue.split("\\|",2);
					if (keys.length != 2) {
						throw new RuntimeException("bad query fields configuration :" + keyValue);
					}
					String fieldName = keys[0];
					String operator = keys[1];
					String value = fieldsQueries.get(keyValue);						
					Query subQueries = DBQuery.empty();
					switch (operator) {
					case "regex":
						Pattern pattern = convertStringToPattern(value);
						subQueries = DBQuery.regex(fieldName, pattern);
						queries.add(subQueries);
						break;
					case "eq":
						subQueries = DBQuery.is(fieldName, value);
						break;
					case "ne":
						subQueries = DBQuery.notEquals(fieldName, value);
						break;
					case "exists":
						if ("TRUE".equals(value)) {
							subQueries = DBQuery.exists(fieldName);
						} else if("FALSE".equals(value)) {
							subQueries = DBQuery.notExists(fieldName);
						}	
						break;
					default:
						throw new RuntimeException("operator not managed or not valid : "+operator);					
					}				
					queries.add(subQueries);
				}
			} catch (DAOException e) {
				throw new RuntimeException(e);
			}
		}
		return queries;
	}
	
}
