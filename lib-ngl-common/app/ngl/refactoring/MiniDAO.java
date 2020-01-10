package ngl.refactoring;

import java.util.function.Supplier;

import fr.cea.ig.DBObject;
import fr.cea.ig.ngl.dao.GenericMongoDAO;
import models.utils.dao.AbstractDAO;

/**
 * Minimal DAO definition for a given type. Not named DAO as the name
 * is already used in NGL.
 * <p>
 * MongoDB DAO can be used as MiniDAO (a minimal MiniDAO instance for a DBObject subclass
 * can be created using {@link #createSupplier(String, Class)}. DAO cannot be used as MiniDAO as 
 * the MiniDAO interface cannot be implemented by DAO and a wrapper is needed {@link #createSupplier(Supplier)}. 
 * 
 * @author vrd
 *
 * @param <T> type of objects managed by this DAO
 */
public interface MiniDAO<T> {
	
	
	/**
	 * Find an instance by code.
	 * @param code code of instance to find
	 * @return     the instance if found, null otherwise
	 */
	T findByCode(String code);
	
	/**
	 * Tests the existence of an object with the given code.
	 * @param code code of object to test existence of
	 * @return     true if the object exists, false otherwise
	 */
	boolean isCodeExist(String code);
	
	/**
	 * Create a supplier of MiniDAO for the given object type. 
	 * @param <C>            DAO element type
	 * @param collectionName collection name
	 * @param objectsType    collection objects type 
	 * @return               supplier of MiniDAO
	 */
	public static <C extends DBObject> Supplier<MiniDAO<C>> createSupplier(String collectionName, Class<C> objectsType) {
		return () -> new MiniDAOMongo<>(collectionName, objectsType);
	}

	/**
	 * Create a a supplier of MiniDAO from the given AbstractDAO. 
	 * @param <C>            DAO element type
	 * @param s AbstractDAO supplier
	 * @return  MiniDAO
	 */
	public static <C> Supplier<MiniDAO<C>> createSupplier(Supplier<? extends AbstractDAO<C>> s) {
		return () -> new MiniDAOModel<>(s.get());
	}
	 
	/**
	 * MiniDAO facade in front of a MongoDBDAO so only a minimal number of 
	 * methods are exposed and not all of the MongoDAO methods.
	 * This should be used when it's not clear if the full MongoDAO should be exposed.
	 * 
	 * @author vrd
	 *
	 * @param <T> type of objects managed by this DAO
	 */
	public class MiniDAOMongo<T extends DBObject> implements MiniDAO<T> {

		/**
		 * Wrapped DAO.
		 */
		private GenericMongoDAO<T> dao;

		/**
		 * Build a MiniDAO for the given object type and collection name.
		 * @param collectionName collection name
		 * @param objectsType    collection object type
		 */
		public MiniDAOMongo(String collectionName, Class<T> objectsType) {
			dao = new GenericMongoDAO<>(collectionName, objectsType);
		}

		@Override
		public T findByCode(String code) {
			return dao.findByCode(code);
		}

		@Override
		public boolean isCodeExist(String code) {
			return dao.isCodeExist(code);
		}

	}

	/**
	 * MiniDAO facade in front of a {@link models.utils.dao.AbstractDAO}.
	 * This is needed as the DAO cannot implement MiniDAO because of Spring 
	 * constraints.
	 * 
	 * @author vrd
	 *
	 * @param <T> type of objects managed by this DAO
	 */
	public static class MiniDAOModel<T> implements MiniDAO<T> {

		/**
		 * Wrapped DAO.
		 */
		private final AbstractDAO<T> dao;

		/**
		 * Build a wrapper for a given DAO.
		 * @param dao DAO to wrap
		 */
		public MiniDAOModel(AbstractDAO<T> dao) {
			this.dao = dao;
		}

		@Override
		public T findByCode(String code) {
			return dao.findByCode(code);
		}

		@Override
		public boolean isCodeExist(String code) {
			return dao.isCodeExist(code);
		}

	}

}
