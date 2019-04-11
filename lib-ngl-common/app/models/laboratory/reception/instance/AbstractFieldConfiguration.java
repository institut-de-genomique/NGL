package models.laboratory.reception.instance;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import models.laboratory.reception.instance.ReceptionConfiguration.Action;
import validation.ContextValidation;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

@JsonTypeInfo(use=Id.NAME, include=As.EXTERNAL_PROPERTY, property="_type", visible=true)
@JsonSubTypes({
	@JsonSubTypes.Type(value = DefaultFieldConfiguration.class,         name = AbstractFieldConfiguration.defaultType),
	@JsonSubTypes.Type(value = ExcelFieldConfiguration.class,           name = AbstractFieldConfiguration.excelType),
	@JsonSubTypes.Type(value = DoubleExcelFieldConfiguration.class,     name = AbstractFieldConfiguration.doubleExcelType),
	@JsonSubTypes.Type(value = PropertiesFieldConfiguration.class,      name = AbstractFieldConfiguration.propertiesType),
	@JsonSubTypes.Type(value = PropertyValueFieldConfiguration.class,   name = AbstractFieldConfiguration.propertyValueType),
	@JsonSubTypes.Type(value = ObjectFieldConfiguration.class,          name = AbstractFieldConfiguration.objectType),
	@JsonSubTypes.Type(value = CommentsFieldConfiguration.class,        name = AbstractFieldConfiguration.commentsType),
	@JsonSubTypes.Type(value = ContentsFieldConfiguration.class,        name = AbstractFieldConfiguration.contentsType),
	@JsonSubTypes.Type(value = QualityControlResultConfiguration.class, name = AbstractFieldConfiguration.qualityControlResultsType),
	@JsonSubTypes.Type(value = TagExcelFieldConfiguration.class,        name = AbstractFieldConfiguration.tagExcelType)
})
public abstract class AbstractFieldConfiguration {

	public static final String defaultType               = "default";
	public static final String excelType                 = "excel";
	public static final String doubleExcelType           = "doubleExcel";
	public static final String propertiesType            = "properties";
	public static final String objectType                = "object";
	public static final String propertyValueType         = "propertyValue";
	public static final String commentsType              = "comments";
	public static final String contentsType              = "contents";
	public static final String qualityControlResultsType = "qualityControlResults";
	public static final String tagExcelType              = "tagExcel";
	
	public static final String deleteFieldValue          = "A_EFFACER";

	public String _type;
	public Boolean required = Boolean.FALSE;

	public AbstractFieldConfiguration(String _type) {
		this._type = _type;
	}

	/*
	 * Extract value and then set value in object
	 * @param field
	 * @param dbObject
	 * @param rowMap
	 * @param contextValidation
	 * @param action 
	 */
	// * @throws Exception
	public abstract void populateField(Field field, 
			                           Object dbObject,
			                           Map<Integer, 
			                           String> rowMap, 
			                           ContextValidation contextValidation, 
			                           Action action) throws Exception;

	/*
	 * Set value directly in object
	 * @param field
	 * @param dbObject
	 * @param value
	 */
	// * @throws Exception
	protected void populateField(Field field, Object dbObject, Object value) throws Exception {
		//in case of collection, we tranform single value to the good collection type
		if (Collection.class.isAssignableFrom(field.getType()) && !Collection.class.isAssignableFrom(value.getClass())) {
			if (Set.class.isAssignableFrom(field.getType())) {
				field.set(dbObject, Collections.singleton(value));
			} else {
				field.set(dbObject, Collections.singletonList(value));
			}			
		} else if(String.class.isAssignableFrom(field.getType()) && null != value) {
			field.set(dbObject, value.toString());
		} else if(Number.class.isAssignableFrom(field.getType()) && null != value) {
			if (Double.class.isAssignableFrom(field.getType())) {
				field.set(dbObject, Double.valueOf(value.toString()));
			} else if(Integer.class.isAssignableFrom(field.getType())) {
				field.set(dbObject, Integer.valueOf(value.toString()));
			} else if(Long.class.isAssignableFrom(field.getType())) {
				field.set(dbObject, Long.valueOf(value.toString()));
			}
		} else {
			field.set(dbObject, value);
		}
	}

}
