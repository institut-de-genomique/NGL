package models.utils.dao;

import static models.utils.dao.DAOException.daoAssertNotNull;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import models.laboratory.common.description.CommonInfoType;
import models.utils.ListObject;

public abstract class AbstractDAOCommonInfoType<T extends CommonInfoType> extends AbstractDAOMapping<T> {

//	private static final play.Logger.ALogger logger = play.Logger.of(AbstractDAOCommonInfoType.class);
	
	protected final static String sqlCommonInfoType = "INNER JOIN common_info_type as t ON t.id=c.fk_common_info_type ";
	
	protected String sqlCommonFrom;
	
	protected String sqlCommonSelect;

	
	protected AbstractDAOCommonInfoType(String                    tableName, 
			                            Class<T>                  entityClass,
			                            MappingSqlQueryFactory<T> classMapping, 
			                            String                    sqlCommonSelect, 
			                            String                    sqlCommonFrom,
			                            boolean                   useGeneratedKey) {
		super(tableName, entityClass, classMapping, sqlCommonSelect+sqlCommonFrom+DAOHelpers.getCommonInfoTypeDefaultSQLForInstitute(), useGeneratedKey);
		this.sqlCommonFrom   = sqlCommonFrom + DAOHelpers.getCommonInfoTypeDefaultSQLForInstitute();
		this.sqlCommonSelect = sqlCommonSelect;		
	}
	
	@Override
//	public Boolean isCodeExist(String code) throws DAOException {
//		if (code == null) 
//			throw new DAOException("code is mandatory");
//		try {
//			String	sql = "SELECT t.id FROM common_info_type t " + DAOHelpers.getCommonInfoTypeDefaultSQLForInstitute() + " where t.code=?";
//			try {
////				long id =  this.jdbcTemplate.queryForLong(sql, code);
//				@SuppressWarnings("deprecation")
//				long id =  jdbcTemplate.queryForLong(sql, code);
////				long id = MongoDeprecation.queryForLong(jdbcTemplate, sql, code);
////				if (id > 0) {
////					return Boolean.TRUE;
////				} else {
////					return Boolean.FALSE;
////				}
//				return id > 0;
//			} catch (EmptyResultDataAccessException e) {
//				return Boolean.FALSE;
//			}
//		} catch (DataAccessException e) {
//			logger.warn(e.getMessage());
//			return null;
//		}
//	}
	public boolean isCodeExist(String code) throws DAOException {
		daoAssertNotNull("code",code);
		try {
			String	sql = "SELECT t.id FROM common_info_type t " + DAOHelpers.getCommonInfoTypeDefaultSQLForInstitute() + " where t.code=?";
			@SuppressWarnings("deprecation")
			long id =  jdbcTemplate.queryForLong(sql, code);
			return id > 0;
		} catch (EmptyResultDataAccessException e) {
			return false;
		} catch (DataAccessException e) {
			throw new DAOException(e);
		}
	}
	
/*
	public T findById(Long id) throws DAOException {
		if(null == id){
			throw new DAOException("id is mandatory");
		}
		String sql = sqlCommon+" where t.id = ? ";
		return initializeMapping(sql, new SqlParameter("id", Type.LONG)).findObject(id);
	}


	public T findByCode(String code) throws DAOException {

		if(null == code){
			throw new DAOException("code is mandatory");
		}

		String sql= sqlCommon+" where t.code = ?";
		return initializeMapping(sql, new SqlParameter("code",Types.VARCHAR)).findObject(code);
	}
*/

	@SuppressWarnings("deprecation")
	public List<ListObject> findAllForList() {
		String sql = "SELECT t.code, t.name " + sqlCommonFrom;
		BeanPropertyRowMapper<ListObject> mapper = new BeanPropertyRowMapper<>(ListObject.class);
		return jdbcTemplate.query(sql, mapper);
	}

}
