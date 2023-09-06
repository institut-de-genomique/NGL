package models.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.property.PropertySingleValue;

public class MappingHelper {

	/**
	 * Populate a property map from a SQL result set.  
	 * @param rs                    SQL result set
	 * @param propertiesDefinitions property definitions
	 * @param properties            property map
	 * @throws SQLException         SQL error
	 */
	public static void getPropertiesFromResultSet(ResultSet                  rs,
				                                  List<PropertyDefinition>   propertiesDefinitions,
				                                  Map<String, PropertyValue> properties) throws SQLException {
		for (PropertyDefinition propertyDefinition : propertiesDefinitions) {
			try {
				String code = rs.getString(propertyDefinition.code);
				if (code != null) {
					try {
						String unite = rs.getString(propertyDefinition.code + "Unit");
						properties.put(propertyDefinition.code, new PropertySingleValue(code, unite));
					} catch(SQLException e) {
						properties.put(propertyDefinition.code, new PropertySingleValue(code));
					}
				}
			} catch (SQLException e) {
			}
		}
	}

}
