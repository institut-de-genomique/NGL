package models.laboratory.run.description.dao;

import static models.utils.dao.DAOException.daoAssertNotNull;

import java.sql.Types;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.stereotype.Repository;

import models.laboratory.common.description.Level;
import models.laboratory.common.description.dao.CommonInfoTypeDAO;
import models.laboratory.run.description.TreatmentType;
import models.laboratory.run.description.TreatmentTypeContext;
import models.utils.dao.AbstractDAOCommonInfoType;
import models.utils.dao.DAOException;
import play.api.modules.spring.Spring;

@Repository
public class TreatmentTypeDAO extends AbstractDAOCommonInfoType<TreatmentType> {

	protected TreatmentTypeDAO() {
		super("treatment_type", TreatmentType.class, TreatmentTypeMappingQuery.factory, 
				"SELECT distinct c.id, c.names, c.fk_common_info_type, c.fk_treatment_category, c.display_orders ",
						"FROM treatment_type as c "+sqlCommonInfoType, false);
	}	
	
	@Override
	public long save(TreatmentType treatmentType) throws DAOException {	
//		if (treatmentType == null)
//			throw new DAOException("ProjectType is mandatory");
//		//Check if category exist
//		if (treatmentType.category == null || treatmentType.category.id == null)
//			throw new DAOException("TreatmentCategory is not present !!");
		
		daoAssertNotNull("treatmentType",             treatmentType);
		daoAssertNotNull("treatmentType.category",    treatmentType.category);
		daoAssertNotNull("treatmentType.category.id", treatmentType.category.id);
		
		//Add commonInfoType
		CommonInfoTypeDAO commonInfoTypeDAO = Spring.getBeanOfType(CommonInfoTypeDAO.class);
		treatmentType.id = commonInfoTypeDAO.save(treatmentType);
		//Create new treatmentType
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("id", treatmentType.id);
		parameters.put("names", treatmentType.names);
		parameters.put("fk_common_info_type", treatmentType.id);
		parameters.put("fk_treatment_category", treatmentType.category.id);
		parameters.put("display_orders", treatmentType.displayOrders);
		jdbcInsert.execute(parameters);
		//Add contexts
		insertTreatmentContexts(treatmentType.contexts, treatmentType.id, false);
		
		return treatmentType.id;
	}
	
	@SuppressWarnings("deprecation")
	private void insertTreatmentContexts(List<TreatmentTypeContext> contexts, Long id, boolean deleteBefore) throws DAOException {
		if (deleteBefore) {
			removeTreatmentContexts(id);
		}
		// Add contexts list		
		if (contexts != null && contexts.size() > 0) {
			String sql = "INSERT INTO treatment_type_context (fk_treatment_type, fk_treatment_context, required) VALUES(?,?,?)";
			for (TreatmentTypeContext context : contexts) {
//				if (context == null || context.id == null ) {
//					throw new DAOException("context is mandatory");
//				}
				daoAssertNotNull("context",    context);
				daoAssertNotNull("context.id", context.id);				
				jdbcTemplate.update(sql, id, context.id, context.required);
			}
		} else {
			throw new DAOException("contexts null or empty");
		}
	}
	
	@SuppressWarnings("deprecation")
	private void removeTreatmentContexts(Long id)  throws DAOException {
		String sql = "DELETE FROM treatment_type_context WHERE fk_treatment_type=?";
		jdbcTemplate.update(sql, id);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void update(TreatmentType treatmentType) throws DAOException {
		//Update contexts
		insertTreatmentContexts(treatmentType.contexts, treatmentType.id, true);
		String sql = "UPDATE treatment_type SET names=?, display_order=? WHERE id=?";
		jdbcTemplate.update(sql, treatmentType.names, treatmentType.displayOrders, treatmentType.id);
	}

	@Override
	public void remove(TreatmentType treatmentType) throws DAOException {
		//Remove contexts for this treatmentType
		removeTreatmentContexts(treatmentType.id);
		//Remove treatmentType
		super.remove(treatmentType);
		//Remove commonInfoType
		CommonInfoTypeDAO commonInfoTypeDAO = Spring.getBeanOfType(CommonInfoTypeDAO.class);
		commonInfoTypeDAO.remove(treatmentType);
	}
	
	public List<TreatmentType> findByTreatmentContextId(long id) {
		String sql = sqlCommon+
				" inner join treatment_type_context as ttc ON ttc.fk_treatment_type=c.id "+
				"WHERE fk_treatment_context = ? ";
		TreatmentTypeMappingQuery treatmentTypeMappingQuery=new TreatmentTypeMappingQuery(dataSource, sql,new SqlParameter("id", Types.BIGINT));
		return treatmentTypeMappingQuery.execute(id);
	}
	
	public List<TreatmentType> findByTreatmentCategoryNames(String...categoryNames) throws DataAccessException, DAOException {
		String sql = sqlCommon+
				" inner join treatment_category cat on cat.id = c.fk_treatment_category where cat.name in ("+listToParameters(Arrays.asList(categoryNames))+")";
		return initializeMapping(sql, listToSqlParameters(Arrays.asList(categoryNames),"cat.name", Types.VARCHAR)).execute((Object[])categoryNames);
	}
	
	// FDS ajout combiner les 2 critères !!!
	public List<TreatmentType> findByCodesAndCategoryNames(List<String> categoryNames, List<String> codes) throws DataAccessException, DAOException {
		
		// pas utiliser Arrays.asList() car ce sont déjà des listes
		String sql = sqlCommon
				+" inner join treatment_category cat on cat.id = c.fk_treatment_category where cat.name in (" +listToParameters(categoryNames) +")"
				+" and t.code in (" + listToParameters(codes) + ")";
		
		SqlParameter[] pCategory= listToSqlParameters(Arrays.asList(categoryNames),"cat.name", Types.VARCHAR);
		SqlParameter[] pCode= listToSqlParameters(Arrays.asList(codes),"t.code", Types.VARCHAR);
			
		/*    concaténer les 2 tableaux:  OK marche */
		int sizeCategory=pCategory.length;
		int sizeCode=pCode.length;
		SqlParameter[] pAll = new SqlParameter[sizeCategory + sizeCode]; 
		System.arraycopy(pCategory, 0, pAll, 0, sizeCategory);  
		System.arraycopy(pCode, 0, pAll, sizeCategory, sizeCode);  
			
		/*utilisation de ArrayUtils.addAll pour concatener les tableaux ?? : 
			==> marche pas !!!
		SqlParameter[] pAll =ArrayUtils.addAll(pCategory,pCode);
		*/
			
		return initializeMapping(sql, pAll).execute(categoryNames,codes);
	}

	public List<TreatmentType> findByLevels(Level.CODE...levels) throws DAOException{
		Object[] parameters = new Object[0];
		Object[] sqlParameters = new SqlParameter[0];
		
		String sql = sqlCommon
				+" inner join property_definition pd on pd.fk_common_info_type = t.id"
				+" inner join property_definition_level pdl on pdl.fk_property_definition = pd.id"
				+" inner join level l on l.id = pdl.fk_level"
				+" where 1=1 ";
		
		if (levels != null && levels.length > 0){
			parameters = ArrayUtils.addAll(parameters,levels);
			sqlParameters = ArrayUtils.addAll(sqlParameters, listToSqlParameters(Arrays.asList(levels),"l.code", Types.VARCHAR));
			sql += "and l.code in ("+listToParameters(Arrays.asList(levels))+")";
		}
		
		return initializeMapping(sql, (SqlParameter[])sqlParameters).execute(parameters);
	}
	
	public List<TreatmentType> findByNames(List<String> names) throws DAOException {
		String sql = sqlCommon + " WHERE t.name in (" + listToParameters(names) + ")";
		return initializeMapping(sql, listToSqlParameters(names ,"t.name", Types.VARCHAR)).execute(names.toArray(new Object[names.size()]));			
	}
	
}
