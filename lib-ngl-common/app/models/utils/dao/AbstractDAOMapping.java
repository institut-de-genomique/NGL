package models.utils.dao;

import static models.utils.dao.DAOException.daoAssertNotNull;

import java.sql.Types;
import java.util.List;

import org.springframework.asm.Type;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.MappingSqlQuery;

/*
 * Generic operation DAO with MappingSQL object 
 *
 * @param <T>
 * 
 * @author ejacoby
 * 
 */
public abstract class AbstractDAOMapping<T> extends AbstractDAO<T> {

	private static final play.Logger.ALogger logger = play.Logger.of(AbstractDAOMapping.class);
	
	// table of class <T> must be named as "t" and "code" field must be unique
	protected String sqlCommon;
	
	protected MappingSqlQueryFactory<T> classMapping;

	protected AbstractDAOMapping(String tableName, Class<T> entityClass, MappingSqlQueryFactory<T> classMapping, String sqlCommon, boolean useGeneratedKey) {
		super(tableName, entityClass, useGeneratedKey);
		this.classMapping = classMapping;
		this.sqlCommon    = sqlCommon;
	}

	@Override
	public T findById(Long id) throws DAOException {
//		if (id == null)
//			throw new DAOException("id is mandatory");
		daoAssertNotNull("id",id);
		String sql = sqlCommon+" where t.id = ? ";
		return initializeMapping(sql, new SqlParameter("id", Type.LONG)).findObject(id);
	}

	@Override
	public List<T> findAll() throws DAOException {
		MappingSqlQuery<T> msq = initializeMapping(sqlCommon);
//		logger.debug("findAll - {} {}", getClass(), msq.getParsedSql());
		return msq.execute();
	}
		
	@Override
	public T findByCode(String code) throws DAOException {
		daoAssertNotNull("code",code);
		T o =  getObjectInCache(code);
		if (o != null) 
			return o;
		String sql = sqlCommon + " where t.code = ?";
		o = initializeMapping(sql, new SqlParameter("code",Types.VARCHAR)).findObject(code);
		setObjectInCache(o, code);
		return o;		
	}
	
	// ajout FDS 28/03/2018 NGL-1969 (avec try/catch)
	// pour trouver un sampleType en se basant sur son code OU SUR SON NOM
	@Override
	public T findByCodeOrName(String code) throws DAOException {
		daoAssertNotNull("code",code);
		T o = getObjectInCache(code);
		if (o != null) 
			return o;
		try {
			String sql= sqlCommon+" where t.code = ? or t.name = ?";
			// doc spring: findObject=> lui donner un tableau avec autant d'object qu'on a de parametres a remplir, ici 2) => new Object[]{code,code})
			// le nom des parametres est inemployé, pourraient s'appeler toto !!!      Merci N.W.
//			o = initializeMapping(sql, new SqlParameter("code",Types.VARCHAR),new SqlParameter("name",Types.VARCHAR) ).findObject(new Object[]{code,code});
			o = initializeMapping(sql, new SqlParameter("code",Types.VARCHAR),new SqlParameter("name",Types.VARCHAR) ).findObject(code,code);
			setObjectInCache(o, code); 
			return o;
		} catch (IncorrectResultSizeDataAccessException e) {
			//Logger.warn(e.getMessage());
			return null;
		}
	}	
	
	@Override
	public List<T> findByCodes(List<String> codes) throws DAOException {
		daoAssertNotNull("codes",codes);
		try {
			String sql = sqlCommon + " WHERE t.code in (" + listToParameters(codes) + ")";
//			BeanPropertyRowMapper<T> mapper = new BeanPropertyRowMapper<T>(entityClass);
//			return initializeMapping(sql, listToSqlParameters(codes ,"t.code", Types.VARCHAR)).execute(codes.toArray( new String[0]));			
			return initializeMapping(sql, listToSqlParameters(codes ,"t.code", Types.VARCHAR)).execute(codes.toArray(new Object[codes.size()]));			
	
		} catch (DataAccessException e) {
			logger.warn(e.getMessage());
			return null;
		}
	}

	protected MappingSqlQuery<T> initializeMapping(String sql, SqlParameter... sqlParams) throws DAOException {
		try {
//			MappingSqlQuery<T> mapping = classMapping.newInstance();
//			mapping.setDataSource(dataSource);
//			mapping.setSql(sql);
			logger.debug("initializeMapping {} - {}", entityClass,  sql);
			MappingSqlQuery<T> mapping = classMapping.apply(dataSource,sql,sqlParams);
//			if (sqlParams != null && sqlParams.length > 0) {
//				for (SqlParameter sqlParam: sqlParams) {
//					mapping.declareParameter(sqlParam);
//				}
//			}
//			mapping.compile();
			return mapping;
		} catch (InvalidDataAccessApiUsageException e) {
			throw new DAOException(e);
//		} catch (InstantiationException e) {
//			throw new DAOException(e);
//		} catch (IllegalAccessException e) {
//			throw new DAOException(e);
		}
	}
	
}
