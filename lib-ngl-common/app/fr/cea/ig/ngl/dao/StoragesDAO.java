package fr.cea.ig.ngl.dao;

import javax.inject.Inject;

import models.laboratory.storage.instance.Storage;
import models.utils.InstanceConstants;

/**
 * Default implementation of StorageDAO, if an actual implementation exists it
 * should replace this class.
 * 
 * @author vrd
 *
 */
public class StoragesDAO extends GenericMongoDAO<Storage> {
	
	@Inject // not needed, placeholder
	public StoragesDAO() {
		super(InstanceConstants.STORAGE_COLL_NAME, Storage.class);
	}
	
}
