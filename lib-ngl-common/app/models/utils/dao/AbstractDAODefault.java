package models.utils.dao;

import static models.utils.dao.DAOException.daoAssertNotNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.MetaDataAccessException;

import fr.cea.ig.lfw.utils.Iterables;
import models.utils.ListObject;

/**
 * Generic operations for SimpleDAO
 * 
 * @author ejacoby
 *
 * @param <T> DAO
 */
public abstract class AbstractDAODefault<T> extends AbstractDAO<T> {

	private static final play.Logger.ALogger logger = play.Logger.of(AbstractDAODefault.class);
	
	private String  sqlCommon;
	protected boolean usedInstitute = false;
	
	@Inject
	protected AbstractDAODefault(String tableName, Class<T> entityClass, boolean useGeneratedKey) {
		super(tableName, entityClass, useGeneratedKey);	
	}
	
	@Inject
	protected AbstractDAODefault(String tableName, Class<T> entityClass, boolean useGeneratedKey, boolean usedInstitute) {
		this(tableName, entityClass,useGeneratedKey);	
		this.usedInstitute = usedInstitute;
	}
	
	@Inject
	protected AbstractDAODefault(String tableName, Class<T> entityClass, boolean useGeneratedKey, String sqlCommon) {
		this(tableName, entityClass,useGeneratedKey);	
		this.sqlCommon = sqlCommon;
	}
	
	@Inject
	protected AbstractDAODefault(String tableName, Class<T> entityClass, boolean useGeneratedKey, boolean usedInstitute, String sqlCommon) {
		this(tableName, entityClass,useGeneratedKey, sqlCommon);	
		this.usedInstitute = usedInstitute;
	}

	@SuppressWarnings("unchecked") // untypeable DB metadata access 
	protected List<String> getColumns() throws MetaDataAccessException {
		List<String> columnNames = (List<String>)JdbcUtils.extractDatabaseMetaData(dataSource, new ColumnMetaDataCallback(tableName));
		if (columnNames.isEmpty())
			throw new RuntimeException("no columns were found");
		return columnNames;
	}

	protected String getSqlCommon() throws DAOException {
		if (sqlCommon == null) {
			sqlCommon = getSQLSelect();
			if (usedInstitute) 
				sqlCommon += DAOHelpers.getSQLForInstitute(tableName, "t");
		}
		return sqlCommon;
	}
	
	private String getSQLSelect() throws DAOException {
		try {
			String sql = "SELECT ";
			if (usedInstitute) sql += "distinct ";
			sql += Iterables.map(getColumns(), x -> "t." + x).intercalate(",").asString();
			sql += " FROM " + tableName + " as t";
			return sql;
		} catch (MetaDataAccessException e) {
			throw new DAOException(e);
		}
	}
	
	private String getSQLUpdate() throws DAOException {
		try {
			return Iterables.filter(getColumns(), c -> !c.equals("id"))
					        .map(c -> c + "=:" + c)
					        .surround("UPDATE " + tableName + " SET ",
					        		  ",",
					        		  " WHERE id=:id")
					        .asString();
		} catch (MetaDataAccessException e) {
			throw new DAOException(e);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public List<T> findAll() throws DAOException {
		try {
			String sql = getSqlCommon() + " ORDER by t.code";
			logger.debug(sql);
			BeanPropertyRowMapper<T> mapper = new BeanPropertyRowMapper<>(entityClass);
			return this.jdbcTemplate.query(sql, mapper);
		} catch (DataAccessException e) {
			return new ArrayList<>();
		}
	}

	@SuppressWarnings("deprecation")
	public List<ListObject> findAllForList() {
		String sql = "SELECT code, name from " + tableName + " ORDER by code";
		BeanPropertyRowMapper<ListObject> mapper = new BeanPropertyRowMapper<>(ListObject.class);
		return this.jdbcTemplate.query(sql, mapper);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public T findById(Long id) throws DAOException {
		daoAssertNotNull("id",id);
		try {
			String sql = getSqlCommon() + " WHERE t.id=?";
			BeanPropertyRowMapper<T> mapper = new BeanPropertyRowMapper<>(entityClass);
			return this.jdbcTemplate.queryForObject(sql, mapper, id);
		} catch (DataAccessException e) {
			return null;
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public T findByCode(String code) throws DAOException {
		daoAssertNotNull("code",code);
		T o = getObjectInCache(code);
		if (o != null)
			return o;
		try {
			String sql = getSqlCommon() + " WHERE t.code=?";
			BeanPropertyRowMapper<T> mapper = new BeanPropertyRowMapper<>(entityClass);
			o = this.jdbcTemplate.queryForObject(sql, mapper, code);
			setObjectInCache(o, code);
			return o;
		} catch (IncorrectResultSizeDataAccessException e) {
			return null;
		}
	}
	
	// ajout FDS 28/03/2018 NGL-1969: pour trouver un sampleType en se basant sur son code OU SUR SON NOM
	// sert dans quel cas ?????????????? la methode reellement utilisee est celle de AbstractDAOMapping....
	@Override
	@SuppressWarnings("deprecation")
	public T findByCodeOrName(String code) throws DAOException {
		daoAssertNotNull("code",code);
		T o = getObjectInCache(code);
		if (o != null) 
			return o;
		try {
			String sql = getSqlCommon() + " WHERE t.code=? or t.name=?";
			BeanPropertyRowMapper<T> mapper = new BeanPropertyRowMapper<>(entityClass);
			o = jdbcTemplate.queryForObject(sql, mapper, code, code); /// ajout 2eme parametre
			setObjectInCache(o, code);
			return o;
		} catch (IncorrectResultSizeDataAccessException e) {
			return null;
		}
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public List<T> findByCodes(List<String> codes) throws DAOException {
		daoAssertNotNull("codes",codes);
		try {
			String sql = getSqlCommon() + " WHERE t.code in (" + listToParameters(codes) + ")";
			BeanPropertyRowMapper<T> mapper = new BeanPropertyRowMapper<>(entityClass);
			return jdbcTemplate.query(sql, mapper, codes.toArray(new Object[codes.size()]));
		} catch (DataAccessException e) {
			logger.warn(e.getMessage());
			return null;
		}
	}

	@Override
	public long save(T value) throws DAOException {
		daoAssertNotNull("value",value);
		SqlParameterSource ps = new BeanPropertySqlParameterSource(value);
		long id  = jdbcInsert.executeAndReturnKey(ps).longValue();
		return id;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void update(T value) throws DAOException	{
		daoAssertNotNull("value",value);
		SqlParameterSource ps = new BeanPropertySqlParameterSource(value);
		jdbcTemplate.update(getSQLUpdate(), ps);
	}

}
