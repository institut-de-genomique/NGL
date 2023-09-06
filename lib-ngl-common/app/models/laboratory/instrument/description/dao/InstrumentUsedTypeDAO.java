package models.laboratory.instrument.description.dao;


import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.asm.Type;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.stereotype.Repository;

import models.laboratory.common.description.dao.CommonInfoTypeDAO;
import models.laboratory.container.description.ContainerSupportCategory;
import models.laboratory.instrument.description.Instrument;
import models.laboratory.instrument.description.InstrumentUsedType;
import models.laboratory.instrument.description.InstrumentUsedTypeQueryParams;
import models.utils.dao.AbstractDAOCommonInfoType;
import models.utils.dao.DAOException;
//import play.Logger;
import play.api.modules.spring.Spring;

@Repository
public class InstrumentUsedTypeDAO extends AbstractDAOCommonInfoType<InstrumentUsedType> {

	private static final play.Logger.ALogger logger = play.Logger.of(InstrumentUsedTypeDAO.class);
	
	private final InstrumentDAO instrumentDAO = Spring.getBeanOfType(InstrumentDAO.class);
	
//	protected InstrumentUsedTypeDAO() {
//		super("instrument_used_type", InstrumentUsedType.class, InstrumentUsedTypeMappingQuery.class, 
//				"SELECT distinct c.id, c.fk_common_info_type, c.fk_instrument_category ",
//						"FROM instrument_used_type as c "+sqlCommonInfoType, false);
//	}
	protected InstrumentUsedTypeDAO() {
		super("instrument_used_type", InstrumentUsedType.class, InstrumentUsedTypeMappingQuery.factory, 
				"SELECT distinct c.id, c.fk_common_info_type, c.fk_instrument_category ",
						"FROM instrument_used_type as c " + sqlCommonInfoType, false);
	}

	public List<InstrumentUsedType> findByExperimentId(long id) {
		String sql = sqlCommon+
				"JOIN experiment_type_instrument_type as cit ON fk_instrument_used_type=c.id " +
				"WHERE cit.fk_experiment_type = ?";
		InstrumentUsedTypeMappingQuery instrumentUsedTypeMappingQuery = new InstrumentUsedTypeMappingQuery(dataSource, sql,new SqlParameter("id", Type.LONG));
		return instrumentUsedTypeMappingQuery.execute(id);
	}
	
	public List<InstrumentUsedType> findByExperimentTypeCode(String code, Boolean active) {
		
		if(null == active){
			String sql = sqlCommon+
					"JOIN experiment_type_instrument_type as cit ON fk_instrument_used_type=c.id " +
					"WHERE cit.fk_experiment_type IN (SELECT e.id FROM experiment_type e, common_info_type citype WHERE e.fk_common_info_type=citype.id AND citype.code=?) ";
			InstrumentUsedTypeMappingQuery instrumentUsedTypeMappingQuery = new InstrumentUsedTypeMappingQuery(dataSource, sql,new SqlParameter("code",Types.VARCHAR));
			return instrumentUsedTypeMappingQuery.execute(code);
		}else{
			String sql = sqlCommon+
					"JOIN experiment_type_instrument_type as cit ON fk_instrument_used_type=c.id " +
					"WHERE cit.fk_experiment_type IN (SELECT e.id FROM experiment_type e, common_info_type citype WHERE e.fk_common_info_type=citype.id AND citype.code=?) and t.active=?";
			InstrumentUsedTypeMappingQuery instrumentUsedTypeMappingQuery = new InstrumentUsedTypeMappingQuery(dataSource, sql,new SqlParameter("code",Types.VARCHAR),new SqlParameter("active",Types.BOOLEAN));
			return instrumentUsedTypeMappingQuery.execute(code, active);
		}
		
		
	}
	
	public List<InstrumentUsedType> findByQueryParams(InstrumentUsedTypeQueryParams queryParams) {
		StringBuilder sqlBuilder = new StringBuilder(sqlCommon);
		sqlBuilder.append(" inner join instrument_category as ic on c.fk_instrument_category=ic.id");
		List<String> parametersBuilder = new ArrayList<>();
		List<SqlParameter> sqlParametersBuilder = new ArrayList<>();
		
		if(queryParams.isCodeList()){
			parametersBuilder.addAll(queryParams.codes);	
			Stream.of(listToSqlParameters(queryParams.codes, "cit.code", Types.VARCHAR)).forEach(sqlParametersBuilder::add);
			sqlBuilder.append(" and t.code in (").append(listToParameters(queryParams.codes)).append(") ");
		}else if(queryParams.isSingleCode()){
			parametersBuilder.add(queryParams.code);
			sqlParametersBuilder.add(new SqlParameter("cit.code", Types.VARCHAR));
			sqlBuilder.append(" and t.code = ?");
		}
		
		if(queryParams.isCategoryCodeList()){
			parametersBuilder.addAll(queryParams.categoryCodes);	
			Stream.of(listToSqlParameters(queryParams.categoryCodes, "cit.code", Types.VARCHAR)).forEach(sqlParametersBuilder::add);
			sqlBuilder.append(" and ic.code in (").append(listToParameters(queryParams.categoryCodes)).append(") ");
		}else if(queryParams.isSingleCategoryCode()){
			parametersBuilder.add(queryParams.categoryCode);
			sqlParametersBuilder.add(new SqlParameter("cit.code", Types.VARCHAR));
			sqlBuilder.append(" and ic.code = ?");
		}
		
		String sql = sqlBuilder.toString();
		Object[] parameters = parametersBuilder.toArray();
		SqlParameter[] sqlParameters = sqlParametersBuilder.stream().toArray(SqlParameter[]::new);
		
		InstrumentUsedTypeMappingQuery mappingQuery = new InstrumentUsedTypeMappingQuery(dataSource, sql, sqlParameters);
		return mappingQuery.execute(parameters);
	}

	/*
	 * Used only in InstrumentMappingQuery
	 * @param id
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public Map<String, Object> findTypeCodeAndCatCode(long id){
		String sql = "SELECT distinct t.code as typeCode, cat.code as catCode "+this.sqlCommonFrom+" inner join instrument_category cat on cat.id = c.fk_instrument_category where c.id = ?"; 
		return jdbcTemplate.queryForMap(sql, id);
	}
	
	@Override
	public long save(InstrumentUsedType instrumentUsedType) throws DAOException {
		if(null == instrumentUsedType){
			throw new DAOException("InstrumentUsedType is mandatory");
		}
		// Check if category exist
		if(instrumentUsedType.category == null || instrumentUsedType.category.id == null){
			throw new DAOException("InstrumentCategory is not present !!");
		}
		
		// Add commonInfoType
		CommonInfoTypeDAO commonInfoTypeDAO = Spring.getBeanOfType(CommonInfoTypeDAO.class);
		instrumentUsedType.id = commonInfoTypeDAO.save(instrumentUsedType);
		instrumentUsedType.setCommonInfoType(instrumentUsedType);
		
		// Create new InstrumentUsedType
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("id",                     instrumentUsedType.id);
		parameters.put("fk_common_info_type",    instrumentUsedType.id);
		parameters.put("fk_instrument_category", instrumentUsedType.category.id);
		
		jdbcInsert.execute(parameters);

		//Add instruments list
		saveInstruments(instrumentUsedType, instrumentUsedType.instruments, false);
		saveContainerSupportCategoryOut(instrumentUsedType.id,instrumentUsedType.outContainerSupportCategories,false);
		saveContainerSupportCategoryIn(instrumentUsedType.id,instrumentUsedType.inContainerSupportCategories,false);
		return instrumentUsedType.id;
	}


	@SuppressWarnings("deprecation")
	private void saveContainerSupportCategoryIn(Long id,List<ContainerSupportCategory> containerSupportCategories, boolean deleteBefore) throws DAOException {
		if (deleteBefore) {
			removeContainerSupportCategoryIn(id);
		}
		if(containerSupportCategories!=null && containerSupportCategories.size()>0){
			String sql = "INSERT INTO instrument_ut_in_container_support_cat (fk_instrument_used_type,fk_container_support_category) VALUES(?,?)";
			for(ContainerSupportCategory containerSupportCategory:containerSupportCategories){
				if(containerSupportCategory == null || containerSupportCategory.id == null ){
					throw new DAOException("containerSupportCategory is mandatory");
				}
				jdbcTemplate.update(sql, id,containerSupportCategory.id);
			}
		}
		
	}
	
	@SuppressWarnings("deprecation")
	private void saveContainerSupportCategoryOut(Long id,List<ContainerSupportCategory> containerSupportCategories,  boolean deleteBefore) throws DAOException {
		if (deleteBefore) {
			removeContainerSupportCategoryOut(id);
		}
		
		if(containerSupportCategories!=null && containerSupportCategories.size()>0){
			String sql = "INSERT INTO instrument_ut_out_container_support_cat (fk_instrument_used_type,fk_container_support_category) VALUES(?,?)";
			for(ContainerSupportCategory containerSupportCategory:containerSupportCategories){
				logger.debug("Out container support type save "+containerSupportCategory);
				if(containerSupportCategory == null || containerSupportCategory.id == null ){
					throw new DAOException("containerSupportCategory is mandatory");
				}
				jdbcTemplate.update(sql, id,containerSupportCategory.id);
			}
		}
		
	}

	@SuppressWarnings("deprecation")
	private void removeContainerSupportCategoryOut(Long id) {
		String sql = "DELETE FROM instrument_ut_out_container_support_cat WHERE fk_instrument_used_type=?";
		jdbcTemplate.update(sql, id);
	}
	
	@SuppressWarnings("deprecation")
	private void removeContainerSupportCategoryIn(Long id) {
		String sql = "DELETE FROM instrument_ut_in_container_support_cat WHERE fk_instrument_used_type=?";
		jdbcTemplate.update(sql, id);
	}


	@Override
	public void update(InstrumentUsedType instrumentUsedType) throws DAOException {
		// Update commonInfoType
		CommonInfoTypeDAO commonInfoTypeDAO = Spring.getBeanOfType(CommonInfoTypeDAO.class);
		commonInfoTypeDAO.update(instrumentUsedType);
		
		// Update instrument list
		saveInstruments(instrumentUsedType, instrumentUsedType.instruments, true);
		saveContainerSupportCategoryIn(instrumentUsedType.id, instrumentUsedType.inContainerSupportCategories, true);
		saveContainerSupportCategoryOut(instrumentUsedType.id, instrumentUsedType.outContainerSupportCategories, true);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void remove(InstrumentUsedType instrumentUsedType) throws DAOException {
		//remove from abstractExperiment common_info_type_instrument_type
		String sqlExp = "DELETE FROM experiment_type_instrument_type WHERE fk_instrument_used_type=?";
		jdbcTemplate.update(sqlExp, instrumentUsedType.id);
		//remove instruments
		deleteFKFromInstruments(instrumentUsedType.id);
		removeContainerSupportCategoryIn(instrumentUsedType.id);
		removeContainerSupportCategoryOut(instrumentUsedType.id);
		//remove instrument used type
		super.remove(instrumentUsedType);
		//remove common_info_type
		CommonInfoTypeDAO commonInfoTypeDAO = Spring.getBeanOfType(CommonInfoTypeDAO.class);
		commonInfoTypeDAO.remove(instrumentUsedType);
	}


	
	private void saveInstruments(InstrumentUsedType instrumentUsedType, List<Instrument> instruments, boolean deleteBefore) throws DAOException {
		if (deleteBefore) 
			deleteFKFromInstruments(instrumentUsedType.id);

		if (instruments != null && instruments.size() > 0){
			for (Instrument instrument : instruments) {
				if (!instrumentDAO.isCodeExist(instrument.code)) {
					logger.debug("create new instrument : "+instrument.code);
					instrument.instrumentUsedType = instrumentUsedType;
					instrumentDAO.save(instrument);
				} else {
					instrumentDAO.updateByCode(instrument);
					updateFKInstrumentUsedType(instrument.code, instrumentUsedType.id);
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	private void updateFKInstrumentUsedType(String code, Long id) {
		String sqlInst = "update instrument set fk_instrument_used_type=? WHERE code=?";
		jdbcTemplate.update(sqlInst, id, code);
	}

//	private void removeInstruments(List<Instrument> instruments) {
//		for(Instrument instrument : instruments){
//			instrumentDAO.remove(instrument);
//		}
//	}

	@SuppressWarnings("deprecation")
	private void deleteFKFromInstruments(Long id) {
		String sqlInst = "update instrument set fk_instrument_used_type=null WHERE fk_instrument_used_type=?";
		jdbcTemplate.update(sqlInst, id);
	}

}
