package models.sra.submit.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.mongojack.DBQuery;

import fr.cea.ig.DBObject;
import fr.cea.ig.MongoDBDAO;
import models.utils.InstanceConstants;

public class SraParameter extends DBObject {
	
	private static final play.Logger.ALogger logger = play.Logger.of(SraParameter.class);
	
	public String type;
	// public String code;
	public String value;

	//public ArrayList<String> values;
	
	public SraParameter() { }
	
	public SraParameter(String code, String type, String value) {
		this.code  = code;
		this.type  = type;
		this.value = value;
	}
	
	public static Map <String, String> getParameter(String type) {
		Map<String, String> map = new HashMap<>();
		List<SraParameter> sraParam = MongoDBDAO.find(InstanceConstants.SRA_PARAMETER_COLL_NAME, SraParameter.class, DBQuery.in("type", type)).toList();
		if (sraParam.isEmpty()) {
			logger.error("Absence de données de type '" + type + "' dans la table SraParmeters");
		}
		for (SraParameter param: sraParam) {
			map.put(param.code, param.value);
		}
		return map;
	}
	
	public static Map <String, ArrayList<String>> getParameters(String type) {
		Map<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
		List<SraParameter> sraParam = MongoDBDAO.find(InstanceConstants.SRA_PARAMETER_COLL_NAME, SraParameter.class, DBQuery.in("type", type)).toList();
		if (sraParam.isEmpty()) {
			logger.error("Absence de données de type '" + type + "' dans la table SraParmeters");
		}
		for (SraParameter param: sraParam) {
			if (!map.containsKey(param.code)) {
				map.put(param.code, new ArrayList<String>());
			}
			map.get(param.code).add(param.value);
		}
		return map;
	}

//	public void deleteByCode(String code) {
//		MongoDBDAO.delete(InstanceConstants.SRA_PARAMETER_COLL_NAME, SraParameter.class, code);
//	}	


}