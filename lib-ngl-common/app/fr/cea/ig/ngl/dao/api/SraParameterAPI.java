package fr.cea.ig.ngl.dao.api;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.mongojack.DBQuery;

import fr.cea.ig.ngl.dao.SraParameterDAO;
import models.sra.submit.util.SraParameter;
import models.utils.dao.DAOException;

@Singleton
public class SraParameterAPI {

	private final SraParameterDAO dao;
	
	@Inject
	public SraParameterAPI(SraParameterDAO dao) {
		this.dao = dao;
	}
	
	public SraParameter findOneByCodeAndType(String code, String type) throws DAOException, APIException {
		return dao.findOneByCodeAndType(code, type);
	}
	
	public Iterable<SraParameter> find(DBQuery.Query q) throws DAOException, APIException {
		return dao.find(q);
	}
	
	public List<SraParameter> findAsList(DBQuery.Query q) throws DAOException, APIException {
		return dao.findAsList(q);
	}

	public Map<String, String> getParameter(String type) throws DAOException, APIException {
		return dao.getParameter(type);
	}
	
}
