package fr.cea.ig.ngl.dao.api;

import javax.inject.Inject;
import javax.inject.Singleton;

import fr.cea.ig.ngl.dao.ValuationCriteriaDAO;
import models.laboratory.valuation.instance.ValuationCriteria;
import models.utils.dao.DAOException;

@Singleton
public class ValuationCriteriaAPI {

	private final ValuationCriteriaDAO dao;
	
	@Inject
	public ValuationCriteriaAPI(ValuationCriteriaDAO dao) {
		this.dao = dao;
	}
	
	public Iterable<ValuationCriteria> all() throws DAOException,APIException {
		return dao.all();
	}
	
}
