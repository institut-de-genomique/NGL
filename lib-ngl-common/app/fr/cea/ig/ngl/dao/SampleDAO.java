package fr.cea.ig.ngl.dao;

import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import models.utils.dao.DAOException;

public class SampleDAO {
	
	private final GenericMongoDAO<Sample> gdao;
	
	public SampleDAO() {
		this.gdao = new GenericMongoDAO<>(InstanceConstants.SAMPLE_COLL_NAME, Sample.class);
	}
	
	public void create(Sample sample) throws DAOException {
		gdao.saveObject(sample);
	}
	
	public void update(Sample sample) throws DAOException {
		gdao.updateObject(sample);
	}
	
	public Sample getByCode(String code) throws DAOException {
//		return gdao.getByCode(code);
		return gdao.getObject(code);
	}
	
	// Heavy weight definition of the create object that in turn relies on the same object definition
	// as the DAO. We do need a proper sample type definition.
	// Suppose we can rely on the Type definition.
	static class Create {
		
	}
	
	static class Update {
		
	}
	
}
