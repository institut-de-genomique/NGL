package fr.cea.ig.ngl.dao.analyses;

import fr.cea.ig.ngl.dao.GenericMongoDAO;
import models.laboratory.run.instance.Analysis;
import models.utils.InstanceConstants;

public class AnalysesDAO extends GenericMongoDAO<Analysis> {

    public AnalysesDAO() {
        super(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class);
    }
    
}
