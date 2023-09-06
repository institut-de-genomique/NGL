package fr.cea.ig.ngl.dao.descriptionHistories;
import javax.inject.Inject;

import fr.cea.ig.ngl.dao.GenericMongoDAO;
import models.laboratory.descriptionHistory.instance.DescriptionHistory;
import models.utils.InstanceConstants;


public class DescriptionHistoriesDAO extends GenericMongoDAO<DescriptionHistory> {
    @Inject
	public DescriptionHistoriesDAO() {
        super(InstanceConstants.DESCRIPTION_HISTORY_COLL_NAME, DescriptionHistory.class);
    }
    
}
