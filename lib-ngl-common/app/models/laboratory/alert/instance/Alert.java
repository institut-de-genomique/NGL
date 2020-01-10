package models.laboratory.alert.instance;

import java.util.Date;
import java.util.List;
import java.util.Map;

import fr.cea.ig.DBObject;

public class Alert extends DBObject{

	public String ruleName;
	public Map<String, List<String>> propertiesAlert;
	public Date createDate;
	public Date updateDate;
	
	
}
