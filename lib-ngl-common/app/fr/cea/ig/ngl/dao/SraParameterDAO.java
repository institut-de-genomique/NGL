package fr.cea.ig.ngl.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.mongojack.DBQuery;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.ngl.dao.api.APIException;
import models.sra.submit.util.SraParameter;
import models.utils.InstanceConstants;
import models.utils.dao.DAOException;

@Singleton
public class SraParameterDAO extends GenericMongoDAO<SraParameter> {
	
	private static final play.Logger.ALogger logger = play.Logger.of(SraParameterDAO.class);
	
	@Inject
	public SraParameterDAO() {
		super(InstanceConstants.SRA_PARAMETER_COLL_NAME,SraParameter.class);
	}
	
	public SraParameter findOneByCodeAndType(String code, String type) throws DAOException {
		return findOne(DBQuery.and(DBQuery.is("code", code),DBQuery.is("type", type)));
	}
	
	public Map<String, String> getParameter(String type) throws DAOException {
		Map<String, String> map = new HashMap<>();
		List<SraParameter> sraParam = MongoDBDAO.find(InstanceConstants.SRA_PARAMETER_COLL_NAME, SraParameter.class, DBQuery.in("type", type)).toList();
		if (sraParam.isEmpty()) {
			logger.error("Absence de données de type '" + type + "' dans la table SraParmeters");
		}
		for (SraParameter param : sraParam) {
			map.put(param.code, param.value);
		}
		return map;
	}
	
	public void deleteByCodeAndType(String code, String type) throws DAOException, APIException {
		MongoDBDAO.delete(InstanceConstants.SRA_PARAMETER_COLL_NAME, SraParameter.class, DBQuery.is("code", code).and(DBQuery.is("type", type)));		
	}
	
	
}
