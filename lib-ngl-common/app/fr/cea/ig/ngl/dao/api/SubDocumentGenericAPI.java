package fr.cea.ig.ngl.dao.api;

import java.util.Collection;

import javax.inject.Inject;

import org.mongojack.DBQuery;

import fr.cea.ig.DBObject;
import fr.cea.ig.ngl.dao.GenericMongoDAO;

public abstract class SubDocumentGenericAPI<T, U extends GenericMongoDAO<V>, V extends DBObject>  {
	
	protected static final String METHOD_NOT_ALLOWED_MESSAGE = "method not allowed for this subresource";
	
	protected final U dao;
	
	@Inject
	public SubDocumentGenericAPI(U dao) {
		this.dao = dao;
	}

	public abstract Collection<T> getSubObjects(V objectInDB);
	public abstract T getSubObject(V objectInDB, String code);
	
	public abstract Iterable<V> listObjects(String parentCode, DBQuery.Query query);
	
	public abstract T save(V objectInDB, T input, String currentUser) throws APIException;
	public abstract T update(V objectInDB, T input, String currentUser) throws APIException;
	public abstract void delete(V objectInDB, String code, String currentUser) throws APIException;
}
