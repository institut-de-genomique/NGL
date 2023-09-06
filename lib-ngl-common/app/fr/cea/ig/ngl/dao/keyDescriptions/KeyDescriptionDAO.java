package fr.cea.ig.ngl.dao.keyDescriptions;

import fr.cea.ig.ngl.dao.GenericMongoDAO;
import models.administration.authorisation.instance.KeyDescription;
import models.utils.InstanceConstants;

public class KeyDescriptionDAO extends GenericMongoDAO<KeyDescription> {

	public KeyDescriptionDAO() {
		super(InstanceConstants.API_KEY_COLL_NAME, KeyDescription.class);
	}

}
