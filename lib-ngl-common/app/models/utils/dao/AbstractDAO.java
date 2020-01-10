package models.utils.dao;

import static fr.cea.ig.lfw.utils.Iterables.repeat;
import static fr.cea.ig.play.IGGlobals.cache;
import static models.utils.dao.DAOException.daoAssertNotNull;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import fr.cea.ig.ngl.tmp.NGLDatabase;
import fr.cea.ig.ngl.utils.GuiceSupplier;
import models.utils.Model;

/**
 * Common operations between Simple DAO et DAO Using mappingQuery
 * Must not implement an interface for transactional context because
 * If implements interface Spring creates an instance for the application 
 * and get not that class but a Java Dynamic Proxy that implements that classes interface.
 * Because of this, you cannot cast that object to the original type.
 * If there aren't interfaces to implement, it will give a CGLib proxy of the class, 
 * which is basically just a runtime modified version of the class and so is 
 * assignable to the class itself.
 * 
 * @author ejacoby
 *
 * @param <T> DAO 
 */
@SuppressWarnings("deprecation")
@Transactional(readOnly=false, rollbackFor=DAOException.class)
public abstract class AbstractDAO<T> {

	private static final play.Logger.ALogger logger = play.Logger.of(AbstractDAO.class);

	// Columns for enumerations.
	protected static final List<String> enumColumns = Arrays.asList("id", "name", "code");
	
	protected String             tableName;
	protected DataSource         dataSource;
	protected SimpleJdbcTemplate jdbcTemplate;
	protected SimpleJdbcInsert   jdbcInsert;
	protected Class<T>           entityClass;
	//Use automatic key id generation 
	//False for type because id provided by commonInfoType
	protected boolean            useGeneratedKey;

	private Supplier<NGLDatabase> database = new GuiceSupplier<>(NGLDatabase.class);
	
	@Inject
	protected AbstractDAO(String tableName, Class<T> entityClass, boolean useGeneratedKey) {
		this.tableName       = tableName;
		this.entityClass     = entityClass;
		this.useGeneratedKey = useGeneratedKey;
		setDataSource(database.get().getDataSource());
		logger.debug("instanciated {}<{}>", AbstractDAO.class.getName(), entityClass.getName());
	}

	private void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
		jdbcTemplate = new SimpleJdbcTemplate(dataSource);   
		if (useGeneratedKey)
			jdbcInsert = new SimpleJdbcInsert(dataSource).withTableName(tableName).usingGeneratedKeyColumns("id");
		else
			jdbcInsert = new SimpleJdbcInsert(dataSource).withTableName(tableName);
	}

	public void remove(T value) throws DAOException	{
		String sql = "DELETE FROM " + tableName + " WHERE id=:id";
		SqlParameterSource ps = new BeanPropertySqlParameterSource(value);
		jdbcTemplate.update(sql, ps);
	}

	public abstract List<T> findAll() throws DAOException;
		
	public abstract T findById(Long id) throws DAOException;
	
	public abstract T findByCode(String code) throws DAOException;
	
	// ajout FDS 28/03/2018 NGL-1969
	public abstract T findByCodeOrName(String code) throws DAOException;

	public abstract List<T> findByCodes(List<String> code) throws DAOException;

	public abstract long save(T value) throws DAOException;

	public abstract void update(T value) throws DAOException;

	public boolean isCodeExist(String code) throws DAOException	{
		daoAssertNotNull("code",code);
		try {
			String sql = "select id from " + tableName + " WHERE code=?";
			long id = jdbcTemplate.queryForLong(sql, code);
			// long id = MongoDeprecation.queryForLong(jdbcTemplate, sql, code);				
			return id > 0;
		} catch (EmptyResultDataAccessException e ) {
			return false;
		} catch (DataAccessException e) {
			throw DAOException.wrap(e);
		}
	}
	
	protected String countToParameters(int count) {
		return repeat("?",count).intercalate(",").asString();
	}
	
	protected String listToParameters(List<?> parameters) {
		return countToParameters(parameters.size());
	}
	
	protected SqlParameter[] listToSqlParameters(List<?> parameters, String paramName, int type) {
		SqlParameter[] params = new SqlParameter[parameters.size()];
		for (int i = 0; i<parameters.size(); i++) {
			params[i] =  new SqlParameter(paramName, type);
		}
		return params;
	}
	
	private String key(String code) {
		return entityClass.getName() + "." + code;
	}
	
	protected T getObjectInCache(String code) {
		if (code == null) 
			return null;
		return cache().<T>get(key(code));
	}
		
	protected void setObjectInCache(T o, String code) {
		if (o != null && code != null)
			cache().set(key(code), o, 60 * 60);		
	}
	
	public void cleanCache() {
		findAll().forEach(o -> {
			cache().remove(key(((Model)o).code));
		});
	}
	
	// --------------------------------------------------------------
	// Moved from DAOHelper
	
	/**
	 * Iteratively deletes instances accessible.
	 * @throws DAOException persistence layer error
	 */
	public void removeAll() throws DAOException {
		for (T t : findAll()) 
			remove(t);	
	}

	/**
	 * Find a list of objects by code.
	 * @param codes         codes of object to fetch
	 * @return              found objects
	 * @throws DAOException DAO error
	 */
	public List<T> findByCodes(String... codes) throws DAOException {
		return findByCodes(Arrays.asList(codes));
	}	
	
}
