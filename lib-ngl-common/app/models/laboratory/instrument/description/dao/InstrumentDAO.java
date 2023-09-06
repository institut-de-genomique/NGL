package models.laboratory.instrument.description.dao;

import static models.utils.dao.DAOException.daoAssertNotNull;

import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.stereotype.Repository;

import models.laboratory.common.description.Institute;
import models.laboratory.instrument.description.Instrument;
import models.laboratory.instrument.description.InstrumentQueryParams;
import models.utils.dao.AbstractDAOMapping;
import models.utils.dao.DAOException;
import models.utils.dao.DAOHelpers;

// import play.Logger;

@Repository
public class InstrumentDAO extends AbstractDAOMapping<Instrument> {

//	protected InstrumentDAO() {
//		super("instrument", Instrument.class, InstrumentMappingQuery.class,
//				"SELECT distinct t.id, t.short_name, t.name, t.code, t.active, t.path, t.fk_instrument_used_type FROM instrument as t "+DAOHelpers.getInstrumentSQLForInstitute("t"),
//				true);				
//	}
	protected InstrumentDAO() {
		super("instrument", Instrument.class, InstrumentMappingQuery.factory,
				"SELECT distinct t.id, t.short_name, t.name, t.code, t.active, t.path, t.fk_instrument_used_type FROM instrument as t "+DAOHelpers.getInstrumentSQLForInstitute("t"),
				true);				
	}
	
	@Override
	public long save(Instrument instrument) throws DAOException {
		Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", instrument.name);
        parameters.put("short_name", instrument.shortName);
        parameters.put("code", instrument.code);
        parameters.put("fk_instrument_used_type", instrument.instrumentUsedType.id);
        parameters.put("active", instrument.active);
        parameters.put("path", instrument.path);
        Long newId = (Long) jdbcInsert.executeAndReturnKey(parameters);
        instrument.id = newId;
        
        insertInstitutes(instrument.institutes, instrument.id, false);
        return instrument.id;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void update(Instrument instrument) throws DAOException {
		String sql = "UPDATE instrument SET code=?, short_name=?, name=?, active=?, path=? WHERE id=?";
		jdbcTemplate.update(sql, instrument.code, instrument.shortName, instrument.name, instrument.active, instrument.path, instrument.id);		
	}
	
	/*
	 * Update all fields but not active and code
	 * @param instrument
	 */
	// * @throws DAOException
	@SuppressWarnings("deprecation")
	public void updateByCode(Instrument instrument) throws DAOException {
		String sql = "UPDATE instrument SET short_name=?, name=?, path=? WHERE code=?";
		jdbcTemplate.update(sql, instrument.shortName, instrument.name, instrument.path, instrument.code);		
	}
	
	@SuppressWarnings("deprecation")
	private void insertInstitutes(List<Institute> institutes, Long instrumentId, boolean deleteBefore) throws DAOException {
		if (deleteBefore) {
			removeInstitutes(instrumentId);
		}
		if (institutes == null)
			throw new DAOException("no institutes list");
		if (institutes.size() == 0)
			throw new DAOException("empty institute list");
		//Add institutes list
		if (/*institutes != null &&*/ institutes.size() > 0) {
			String sql = "INSERT INTO instrument_institute (fk_instrument, fk_institute) VALUES (?,?)";
			for (Institute institute : institutes) {
				if (institute == null || institute.id == null )
					throw new DAOException("institute is mandatory");
				jdbcTemplate.update(sql, instrumentId, institute.id);
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	private void removeInstitutes( long instrumentId) {
		String sql = "DELETE FROM instrument_institute WHERE fk_instrument=?";
		jdbcTemplate.update(sql, instrumentId);
	}
	
	
	@Override
	public void remove(Instrument instrument) throws DAOException {
		if (instrument == null)
			throw new IllegalArgumentException("instrument is null");
		
		//remove institute
		removeInstitutes(instrument.id);

		super.remove(instrument);
	}
	
	@Override // because code is not unique for Instrument
//	public boolean isCodeExist(String code) throws DAOException {
//		if (code == null)
//			throw new DAOException("code is mandatory");
//		try {
//			try {
//				String sql = "select distinct(t.code) as code FROM instrument as t "+DAOHelpers.getInstrumentSQLForInstitute("t")+" WHERE t.code=?";
//				@SuppressWarnings("deprecation")
//				String returnCode =  this.jdbcTemplate.queryForObject(sql, String.class, code);
////				if (returnCode != null) {
////					return Boolean.TRUE;
////				} else {
////					return Boolean.FALSE;
////				}
//				return returnCode != null;
//			} catch (EmptyResultDataAccessException e) {
//				return Boolean.FALSE;
//			}
//		} catch (DataAccessException e) {
//			throw new RuntimeException(e);
//		}
//	}
	public boolean isCodeExist(String code) throws DAOException {
		daoAssertNotNull("code",code);
		try {
			String sql = "select distinct(t.code) as code FROM instrument as t "+DAOHelpers.getInstrumentSQLForInstitute("t")+" WHERE t.code=?";
			@SuppressWarnings("deprecation")
			String returnCode =  this.jdbcTemplate.queryForObject(sql, String.class, code);
			return returnCode != null;
		} catch (EmptyResultDataAccessException e) {
			return Boolean.FALSE;
		} catch (DataAccessException e) {
			throw new DAOException(e);
		}
	}
	
	public List<Instrument> findByInstrumentUsedType(long idInstrumentUsedType) throws DAOException {
		String sql = sqlCommon + " WHERE t.fk_instrument_used_type=? order by t.name";
		return initializeMapping(sql, new SqlParameter("t.fk_instrument_used_type", Types.INTEGER)).execute(idInstrumentUsedType);
	}
	
	public List<Instrument> findByExperimentTypeQueryParams(InstrumentQueryParams instumentQueryParams) throws DAOException {
		Object[] parameters = new Object[0];
		Object[] sqlParameters = new SqlParameter[0];
		
		String	sql = sqlCommon  + " inner join instrument_used_type iut on iut.id = t.fk_instrument_used_type"
				+" inner join experiment_type_instrument_type etit on etit.fk_instrument_used_type = iut.id"
				+" inner join experiment_type et on etit.fk_experiment_type = et.id"
				+" inner join common_info_type cit on cit.id = et.fk_common_info_type"					
				+" where 1=1 ";		
		
		if (instumentQueryParams.experimentTypes != null) {
			parameters = ArrayUtils.addAll(parameters, instumentQueryParams.experimentTypes.toArray());
			sqlParameters = ArrayUtils.addAll(sqlParameters, listToSqlParameters(instumentQueryParams.experimentTypes,"cit.code", Types.VARCHAR));			
			sql += " and cit.code in ("+listToParameters(instumentQueryParams.experimentTypes)+") ";
		} else if(instumentQueryParams.experimentType != null) {
			Object[] args = new Object[]{instumentQueryParams.experimentType};
			parameters = ArrayUtils.addAll(parameters,args);
			sqlParameters = ArrayUtils.add(sqlParameters, new SqlParameter("cit.code", Types.VARCHAR));			
			sql += " and cit.code = ? ";			
		}
	
		sql += " ORDER BY t.code";		
		return initializeMapping(sql, (SqlParameter[])sqlParameters).execute(parameters);	
	}
	
	public List<Instrument> findByQueryParams(InstrumentQueryParams instumentQueryParams) throws DAOException {
		Object[] parameters = new Object[0];
		Object[] sqlParameters = new SqlParameter[0];
		
		String sql = sqlCommon  + " inner join instrument_used_type iut on iut.id = t.fk_instrument_used_type"
				+ " inner join instrument_category ic on ic.id = iut.fk_instrument_category"
				+" inner join common_info_type cit on cit.id = iut.fk_common_info_type"				
				+" where 1=1 ";
		
		if(null != instumentQueryParams.active){
			parameters = ArrayUtils.add(parameters, instumentQueryParams.active);
			sqlParameters = ArrayUtils.add(sqlParameters, new SqlParameter("t.active", Types.BOOLEAN));
			sql += " and t.active=?";
		}
		
		if(CollectionUtils.isNotEmpty(instumentQueryParams.codes)){
			parameters = ArrayUtils.addAll(parameters, instumentQueryParams.codes.toArray());	
			sqlParameters = ArrayUtils.addAll(sqlParameters, listToSqlParameters(instumentQueryParams.codes,"t.code", Types.VARCHAR));	
			sql += " and  t.code in ("+listToParameters(instumentQueryParams.codes)+") ";
		}else if(StringUtils.isNotBlank(instumentQueryParams.code)){
			Object[] args = new Object[]{instumentQueryParams.code};			
			parameters = ArrayUtils.addAll(parameters,args);
			sqlParameters = ArrayUtils.add(sqlParameters, new SqlParameter("t.code", Types.VARCHAR));
			sql += " and  t.code = ? ";
		}
		
		
		if(instumentQueryParams.typeCodes != null){
			parameters = ArrayUtils.addAll(parameters, instumentQueryParams.typeCodes.toArray());	
			sqlParameters = ArrayUtils.addAll(sqlParameters, listToSqlParameters(instumentQueryParams.typeCodes,"cit.code", Types.VARCHAR));	
			sql += " and  cit.code in ("+listToParameters(instumentQueryParams.typeCodes)+") ";
		}else if(instumentQueryParams.typeCode != null){
			Object[] args = new Object[]{instumentQueryParams.typeCode};			
			parameters = ArrayUtils.addAll(parameters,args);
			sqlParameters = ArrayUtils.add(sqlParameters, new SqlParameter("cit.code", Types.VARCHAR));
			sql += " and  cit.code = ? ";
		}
					
		if(instumentQueryParams.categoryCodes != null){
			parameters = ArrayUtils.addAll(parameters, instumentQueryParams.categoryCodes.toArray());
			sqlParameters = ArrayUtils.addAll(sqlParameters, listToSqlParameters(instumentQueryParams.categoryCodes,"ic.code", Types.VARCHAR));			
			sql += " and ic.code in ("+listToParameters(instumentQueryParams.categoryCodes)+") ";
		}else if(instumentQueryParams.categoryCode != null){
			Object[] args = new Object[]{instumentQueryParams.categoryCode};
			parameters = ArrayUtils.addAll(parameters,args);
			sqlParameters = ArrayUtils.add(sqlParameters, new SqlParameter("ic.code", Types.VARCHAR));
			sql += " and ic.code = ? ";			
		}		
		
		sql += " ORDER BY t.code";
		
		return initializeMapping(sql, (SqlParameter[])sqlParameters).execute(parameters);		
	}
	
}
