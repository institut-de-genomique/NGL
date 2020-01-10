package models.util;

import java.sql.ResultSet;
import java.sql.SQLException;
// import java.util.HashMap;
import java.util.List;
import java.util.Map;

// import play.Logger;

import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.property.PropertySingleValue;

public class MappingHelper {

	public static void getPropertiesFromResultSet(ResultSet rs,
				                                  List<PropertyDefinition> propertiesDefinitions,
				                                  Map<String, PropertyValue> properties) throws SQLException {
		for (PropertyDefinition propertyDefinition : propertiesDefinitions) {
			String code  = null;
			String unite = null;
			try {
				code = rs.getString(propertyDefinition.code);
			//	Logger.debug("Property definition to retrieve "+propertyDefinition.code+ "value "+ code);
				if (code != null) {
					try {
						unite=rs.getString(propertyDefinition.code+"Unit");
						properties.put(propertyDefinition.code, new PropertySingleValue(code,unite));
					} catch(SQLException e) {
						properties.put(propertyDefinition.code, new PropertySingleValue(code));
					}
				}

			} catch (SQLException e) {
			//	Logger.info("Property "+propertyDefinition.code+" not exist in "+rs.getStatement().toString()+ " query");
			}
		}
	}

}
