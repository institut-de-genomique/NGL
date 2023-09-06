package fr.cea.ig.ngl.dao.sra;

import javax.inject.Inject;

import models.sra.submit.sra.instance.Analysis;
import models.utils.InstanceConstants;
import fr.cea.ig.ngl.dao.GenericMongoDAO;

public class AnalysisDAO extends GenericMongoDAO<Analysis> {
	
	@Inject
	public AnalysisDAO() {
		super(InstanceConstants.SRA_ANALYSIS_COLL_NAME, Analysis.class);
	}
	
}

