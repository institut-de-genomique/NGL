package models.laboratory.protocol.instance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import fr.cea.ig.DBObject;
import models.laboratory.common.instance.PropertyValue;
import models.utils.InstanceConstants;
import validation.ContextValidation;
import validation.IValidation;
import validation.protocol.instance.ProtocolValidationHelper;

public class Protocol extends DBObject implements IValidation {
	
	public String name;
	public String filePath;
	public String version;	
	public String categoryCode;
	public List<String> experimentTypeCodes;
	public Map<String, PropertyValue> properties;
	public Boolean active; // = Boolean.TRUE;
	
	public Protocol() {		
//		super();
		experimentTypeCodes = new ArrayList<>();
		active              = true;
	}
	
//	public Protocol(String code, String name, String filePath, String version, String categoryCode) {
//		this();
//		this.code         = code;
//		this.name         = name;
//		this.filePath     = filePath;
//		this.version      = version;
//		this.categoryCode = categoryCode;
//	}
	
	public Protocol(String code, String name, String filePath, String version, String categoryCode, 
			        List<String> experimentTypeCodes,  Map<String, PropertyValue> properties, Boolean active) {
		this.code                = code.toLowerCase().replace("\\s+", "-");
		this.name                = name;
		this.filePath            = filePath;
		this.version             = version;
		this.categoryCode        = categoryCode;
		this.experimentTypeCodes = experimentTypeCodes;
		this.properties          = properties;
		this.active              = active;
	}
	
	@JsonIgnore
	@Override
	public void validate(ContextValidation contextValidation) {
		ProtocolValidationHelper.validateIdPrimary           (contextValidation, this);
		ProtocolValidationHelper.validateCodePrimary         (contextValidation, this, InstanceConstants.PROTOCOL_COLL_NAME);
		ProtocolValidationHelper.validateExperimentTypeCodes (contextValidation, experimentTypeCodes);
		ProtocolValidationHelper.validateProtocolCategoryCode(categoryCode, contextValidation);
		// filePath ( required ??), version (required ??)	
	}
	
}
