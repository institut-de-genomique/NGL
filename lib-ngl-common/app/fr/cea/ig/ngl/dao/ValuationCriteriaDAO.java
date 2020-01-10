package fr.cea.ig.ngl.dao;

import javax.inject.Inject;
import javax.inject.Singleton;

import models.laboratory.valuation.instance.ValuationCriteria;
import models.utils.InstanceConstants;

@Singleton
public class ValuationCriteriaDAO extends GenericMongoDAO<ValuationCriteria> {

	// Not needed, placeholder
	@Inject
	public ValuationCriteriaDAO() {
		super(InstanceConstants.VALUATION_CRITERIA_COLL_NAME, ValuationCriteria.class);
	}

}
