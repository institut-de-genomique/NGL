package models.laboratory.common.description.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;

import models.laboratory.common.description.CommonInfoType;
import models.laboratory.common.description.Level;
import models.laboratory.common.description.MeasureCategory;
import models.laboratory.common.description.MeasureUnit;
import models.laboratory.common.description.ObjectType;
import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.common.description.Value;
import models.utils.dao.DAOException;
import models.utils.dao.MappingSqlQueryFactory;
import models.utils.dao.NGLMappingSqlQuery;
import play.api.modules.spring.Spring;

public class PropertyDefinitionMappingQuery extends NGLMappingSqlQuery<PropertyDefinition> {

	public static final MappingSqlQueryFactory<PropertyDefinition> factory = (d,s,ps) -> new PropertyDefinitionMappingQuery(d,s,ps);
	
	public enum Version {DEFAULT, LIGHT, OBJECT_TYPE};
	
	private Version version;

	public PropertyDefinitionMappingQuery(DataSource ds, String sql, SqlParameter... sqlParameters) {
		this(ds, sql, Version.DEFAULT, sqlParameters);
	}

	public PropertyDefinitionMappingQuery(DataSource ds, String sql, Version version , SqlParameter... sqlParameters)	{
		super(ds, sql, sqlParameters);
		this.version = version;
	}

	@Override
	protected PropertyDefinition mapRow(ResultSet rs, int rowNumber) throws SQLException {
		PropertyDefinition propertyDefinition = new PropertyDefinition();
		MeasureUnitDAO     mufind = MeasureUnit.find.get();
		if (Version.DEFAULT.equals(this.version)) {
			propertyDefinition.id            = rs.getLong("id");
			propertyDefinition.setName(        rs.getString("name"));
			propertyDefinition.code          = rs.getString("code");
			propertyDefinition.description   = rs.getString("description");
			propertyDefinition.required      = rs.getBoolean("required");
			propertyDefinition.requiredState = rs.getString("required_state");
			propertyDefinition.active        = rs.getBoolean("active");
			propertyDefinition.choiceInList  = rs.getBoolean("choice_in_list");
			propertyDefinition.valueType     = rs.getString("type");
			propertyDefinition.displayFormat = rs.getString("display_format");
			propertyDefinition.displayOrder  = rs.getInt("display_order");
			propertyDefinition.defaultValue  = rs.getString("default_value");
			propertyDefinition.propertyValueType = rs.getString("property_value_type");
			propertyDefinition.editable      = rs.getBoolean("editable");
			//Add measure category
			try {
				//Add levels
				LevelDAO levelDAO=Spring.getBeanOfType(LevelDAO.class);
				List<Level> levels=levelDAO.findByPropertyDefinitionID(propertyDefinition.id);
				propertyDefinition.levels = levels;
				
				if (rs.getLong("fk_measure_category") != 0) {
					propertyDefinition.measureCategory = MeasureCategory.find.get().findById(rs.getLong("fk_measure_category"));
				}
				//Add measure value
				if (rs.getLong("fk_save_measure_unit") != 0) {		
					propertyDefinition.saveMeasureValue = mufind.findById(rs.getLong("fk_save_measure_unit"));
				}
				
				if (rs.getLong("fk_display_measure_unit")!= 0) {		
					propertyDefinition.displayMeasureValue = mufind.findById(rs.getLong("fk_display_measure_unit"));
				}
	
			} catch (DAOException e) {
				throw new SQLException(e);
			}
			//Add possible values
			ValueDAO valueDAO = Spring.getBeanOfType(ValueDAO.class);
			List<Value> values = valueDAO.findByPropertyDefinition(propertyDefinition.id);
			// GA: convert value to the good type number or string ???
			propertyDefinition.possibleValues = values;
		} else if(Version.LIGHT.equals(this.version)) { //pd.code, pd.type, pd.property_value_type, pd.choice_in_list
			propertyDefinition.code = rs.getString("code");
			propertyDefinition.choiceInList = rs.getBoolean("choice_in_list");
			propertyDefinition.valueType = rs.getString("type");
			propertyDefinition.propertyValueType = rs.getString("property_value_type");
			
		} else if(Version.OBJECT_TYPE.equals(this.version)) {
			propertyDefinition.id            = rs.getLong("t.id");
			propertyDefinition.setName(        rs.getString("t.name"));
			propertyDefinition.code          = rs.getString("t.code");
			propertyDefinition.description   = rs.getString("t.description");
			propertyDefinition.required      = rs.getBoolean("t.required");
			propertyDefinition.requiredState = rs.getString("t.required_state");
			propertyDefinition.active        = rs.getBoolean("t.active");
			propertyDefinition.choiceInList  = rs.getBoolean("t.choice_in_list");
			propertyDefinition.valueType     = rs.getString("t.type");
			propertyDefinition.displayFormat = rs.getString("t.display_format");
			propertyDefinition.displayOrder  = rs.getInt("t.display_order");
			propertyDefinition.defaultValue  = rs.getString("t.default_value");
			propertyDefinition.propertyValueType = rs.getString("t.property_value_type");
			propertyDefinition.editable      = rs.getBoolean("t.editable");
			
			CommonInfoType commonInfoType = new CommonInfoType();
			commonInfoType.id				= rs.getLong("c.id");
			commonInfoType.name				= rs.getString("c.name");
			commonInfoType.code				= rs.getString("c.code");
			commonInfoType.displayOrder		= rs.getInt("c.display_order");
			commonInfoType.active			= rs.getBoolean("c.active");
			
			ObjectType objectType = new ObjectType();
			objectType.id					= rs.getLong("o.id");
			objectType.code					= rs.getString("o.code");
			objectType.generic				= rs.getBoolean("o.generic");
			
			commonInfoType.objectType = objectType;
			propertyDefinition.commonInfoType = commonInfoType;
			
			//Add measure category
			try {
				//Add levels
				LevelDAO levelDAO=Spring.getBeanOfType(LevelDAO.class);
				List<Level> levels=levelDAO.findByPropertyDefinitionID(propertyDefinition.id);
				propertyDefinition.levels = levels;
				
				if (rs.getLong("t.fk_measure_category") != 0) {
					propertyDefinition.measureCategory = MeasureCategory.find.get().findById(rs.getLong("t.fk_measure_category"));
				}
				//Add measure value
				if (rs.getLong("t.fk_save_measure_unit") != 0) {		
					propertyDefinition.saveMeasureValue = mufind.findById(rs.getLong("t.fk_save_measure_unit"));
				}
				
				if (rs.getLong("t.fk_display_measure_unit")!= 0) {		
					propertyDefinition.displayMeasureValue = mufind.findById(rs.getLong("t.fk_display_measure_unit"));
				}
	
			} catch (DAOException e) {
				throw new SQLException(e);
			}
			ValueDAO valueDAO = Spring.getBeanOfType(ValueDAO.class);
			List<Value> values = valueDAO.findByPropertyDefinition(propertyDefinition.id);
			propertyDefinition.possibleValues = values;
		}
		return propertyDefinition;
	}

}
