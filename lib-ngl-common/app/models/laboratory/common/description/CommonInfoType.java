package models.laboratory.common.description;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.fasterxml.jackson.annotation.JsonIgnore;

import fr.cea.ig.lfw.utils.Iterables;
import fr.cea.ig.ngl.utils.SpringSupplier;
import models.laboratory.common.description.dao.CommonInfoTypeDAO;
import models.utils.Model;

/**
 * Class attributes common types
 * Represented by a table in the database with its own id
 * The subclasses are represented by tables in the database with the same id as the parent class
 * Relations with the protocols and instruments are accessible by the common_info_type table for the
 * experiment subclasses (experimentType, qualityCcontrolType ...)
 * 
 * @author ejacoby
 *
 */
public class CommonInfoType extends Model {

	public static final Supplier<CommonInfoTypeDAO> find = new SpringSupplier<>(CommonInfoTypeDAO.class);
	
	public String                   name;                 // used as label	
	public Integer                  displayOrder;         // position on display
	public List<State>              states                = new ArrayList<>();
	public List<PropertyDefinition> propertiesDefinitions = new ArrayList<>();
	public ObjectType               objectType;
	public List<Institute>          institutes            = new ArrayList<>();
	public Boolean                  active                = true;
	
	@JsonIgnore
	public Map<String, PropertyDefinition> getMapPropertyDefinition() {
		Map<String, PropertyDefinition> mapProperties = new HashMap<>();
		for (PropertyDefinition propertyDefinition : propertiesDefinitions) {
			mapProperties.put(propertyDefinition.code, propertyDefinition);
		}
		return mapProperties;
	}

	public void setCommonInfoType(CommonInfoType commonInfoType) {
		this.id                    = commonInfoType.id;
		this.name                  = commonInfoType.name;
		this.code                  = commonInfoType.code;
		this.states                = commonInfoType.states;
		this.propertiesDefinitions = commonInfoType.propertiesDefinitions;
		this.objectType            = commonInfoType.objectType;
		this.institutes            = commonInfoType.institutes;
		// position on display
		this.displayOrder          = commonInfoType.displayOrder;
		this.active                = commonInfoType.active;
	}

	public PropertyDefinition getPropertyDefinitionByCode(String code)
	{
		for (PropertyDefinition propertyDefinition : propertiesDefinitions) {
			if(propertyDefinition.code.equals(code))
				return propertyDefinition;
		}
		return null;
	}
	
	public Value getValueFromPropertyDefinitionByCode(String code, String codeValue)
	{
		PropertyDefinition propDefinition = getPropertyDefinitionByCode(code);
		if(propDefinition!=null) {
			for(Value value : propDefinition.possibleValues) {
				if(value.code.equals(codeValue))
					return value;
			}
		}
		return null;
	}
	
	/**
	 * Filtered property definitions that are defined for all of the provided levels.
	 * @param levels levels that must be defined for a property to be kept
	 * @return       filtered properties
	 */
	public List<PropertyDefinition> getPropertyDefinitionByLevel(Level.CODE... levels) {
		List<PropertyDefinition> proDefinitions = new ArrayList<>();
		for (PropertyDefinition propertyDefinition : propertiesDefinitions) {
			boolean containsAll = true;
			for (int i=0; i<levels.length; i++) {
				Level level = new Level(levels[i]);
				if (!propertyDefinition.levels.contains(level)) {
					containsAll = false;
					break;
				}						
			}
			if (containsAll) {
				proDefinitions.add(propertyDefinition);
			}
		}	
		return proDefinitions;
	}

	/**
	 * Filtered property definitions that are defined for all of the provided levels.
	 * @param levels levels that must be defined for a property to be kept
	 * @return       filtered properties
	 */
	// Untested alternate implementation
	public List<PropertyDefinition> getPropertyDefinitionByLevel_(Level.CODE... levels) {
		return Iterables
				.filter(propertiesDefinitions, propertyDefinition -> {
					for (Level.CODE level : levels)
						if (!propertyDefinition.levels.contains(new Level(level)))
							return false;
					return true;
				})
				.toList();	
	}
	
	// --------------------------------------------------------------------------
	// Shorthands
	
	@JsonIgnore
	public List<PropertyDefinition> getPropertiesDefinitionContainerLevel() {
		return getPropertyDefinitionByLevel(Level.CODE.Container);
	}
	
	@JsonIgnore
	public List<PropertyDefinition> getPropertiesDefinitionContentLevel() {
		return getPropertyDefinitionByLevel(Level.CODE.Content);
	}
	
	@JsonIgnore
	public List<PropertyDefinition> getPropertiesDefinitionExperimentLevel() {
		return getPropertyDefinitionByLevel(Level.CODE.Experiment);
	}
	
	@JsonIgnore
	public List<PropertyDefinition> getPropertiesDefinitionInstrumentLevel() {
		return getPropertyDefinitionByLevel(Level.CODE.Instrument);
	}

	@JsonIgnore
	public List<PropertyDefinition> getPropertiesDefinitionProcessLevel() {
		return getPropertyDefinitionByLevel(Level.CODE.Process);
	}
	
	@JsonIgnore
	public List<PropertyDefinition> getPropertiesDefinitionProjectLevel() {
		return getPropertyDefinitionByLevel(Level.CODE.Project);
	}
	
	@JsonIgnore
	public List<PropertyDefinition> getPropertiesDefinitionReadSetLevel() {
		return getPropertyDefinitionByLevel(Level.CODE.ReadSet);
	}
	
	@JsonIgnore
	public List<PropertyDefinition> getPropertiesDefinitionRunLevel() {
		return getPropertyDefinitionByLevel(Level.CODE.Run);
	}

	@JsonIgnore
	public List<PropertyDefinition> getPropertiesDefinitionSampleLevel() {
		return getPropertyDefinitionByLevel(Level.CODE.Sample);
	}
	
	
}
