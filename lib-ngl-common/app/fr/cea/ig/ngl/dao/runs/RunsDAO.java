package fr.cea.ig.ngl.dao.runs;

import javax.inject.Inject;
import javax.inject.Singleton;

import fr.cea.ig.ngl.dao.GenericMongoDAO;
import models.laboratory.run.instance.Run;
import models.utils.InstanceConstants;

@Singleton
public class RunsDAO extends GenericMongoDAO<Run> {

    @Inject
    public RunsDAO() {
        super(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class);
    }
    
}
