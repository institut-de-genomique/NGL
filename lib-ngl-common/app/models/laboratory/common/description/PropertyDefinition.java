package models.laboratory.common.description;

import java.util.List;
import java.util.function.Supplier;

import com.fasterxml.jackson.annotation.JsonIgnore;

import fr.cea.ig.ngl.utils.SpringSupplier;
import models.laboratory.common.description.dao.PropertyDefinitionDAO;
import models.utils.Model;

/**
 * Type property definition.
 *  
 * @author ejacoby
 *
 */
// Mixes the meta data (required, ...) with presentation information
// (displayOrder,...) and presentation information is expected to be 
// a bit more presentation specific.
public class PropertyDefinition extends Model {

	@JsonIgnore
	public static final Supplier<PropertyDefinitionDAO> find = new SpringSupplier<>(PropertyDefinitionDAO.class); 

	// public String name;
	private String name;
	
	public String description;

	public Boolean required      = Boolean.FALSE;
	public String  requiredState = null;
	public Boolean editable      = Boolean.TRUE;
	public Boolean active        = Boolean.TRUE;
	public Boolean choiceInList  = Boolean.FALSE; // same as notEmpty(possibleValues)

	public String  propertyValueType ;
	public String  valueType;                     // java class name of the value type (java.lang.String, Integer, Boolean, TBoolean, etc).
	public String  displayFormat;
	public Integer displayOrder;

	public List<Level> levels;
	
	public List<Value> possibleValues;

	public String defaultValue;

	public MeasureCategory measureCategory;

	// Unité de stockage
	public MeasureUnit saveMeasureValue;
	// Unité d'affichage
	public MeasureUnit displayMeasureValue;
	
	public CommonInfoType commonInfoType;
	
	public void setName(String name) {
//		if (name == null)
//			throw new IllegalArgumentException("name is null");
		this.name = name; 
	}

	public String getName() {
//		if (name == null)
//			throw new RuntimeException("no name was set");
		return name; 
	}

//	/**
//	 * Split the propertyDefinition code with ".", returning a split string array.
//	 * @param propertyDefinition 
//	 * @return property definition code string split using dot as a delimiter and a maximum element count of 2
//	 */
//	public static String[] splitCodePropertyDefinition(PropertyDefinition propertyDefinition) {
//		return propertyDefinition.code.split("\\.", 2);
//	}

	/**
	 * Split the propertyDefinition code with ".", returning a split string array.
	 * @return property definition code string split using dot as a delimiter and a maximum element count of 2
	 */
	public String[] splitCodePropertyDefinition() {
		return code.split("\\.", 2);
	}

}
