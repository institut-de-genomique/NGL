package models.utils.dao;

import static models.utils.dao.DAOException.daoAssertNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fr.cea.ig.lfw.utils.Iterables;
import models.utils.DescriptionHelper;
import models.utils.Model;
import models.utils.ModelDAOs;
import play.data.validation.ValidationError;

/**
 * Utility methods for DAOs.
 */
public class DAOHelpers {

	private static final play.Logger.ALogger logger = play.Logger.of(DAOHelpers.class);
	
//	public static <T extends Model> Map<String,List<ValidationError>> saveModels(Class<T> type, Map<String,T> models) throws DAOException {
//		Map<String,List<ValidationError>>errors = new HashMap<>();
//		ModelDAOs daos = ModelDAOs.instance.get();
//		for (Entry<String,T> entry : models.entrySet()) {
//			T model = entry.getValue();
//			T dbModel = daos.fromDB(model);
//			if (dbModel != null)
//				daos.remove(dbModel);
//			logger.debug(" Before save :" + model.code);
//			daos.save(model);
//			logger.debug(" After save :" + model.code);
//			model = daos.fromDB(model);
//			logger.debug(" After find :" + model.code);
//		}	
//		return errors;
//	}

	// ------------------------------------------------------------------
	// removed unused Class<T> parameter
	
	/**
	 * Iteratively deletes instances accessible through the given DAO.
	 * @param <T>           element type
	 * @param c             java type of the objects to delete
	 * @param finder        DAO used to access and delete instances
	 * @throws DAOException persistence layer error
	 * @deprecated use {@link AbstractDAO#removeAll()}
	 */
	@Deprecated
	public static <T extends Model> void removeAll(Class<T> c, AbstractDAO<T> finder) throws DAOException {
		for (T t : finder.findAll()) 
			finder.remove(t);	
	}
	
	/**
	 * Iteratively deletes instances accessible through the given DAO.
	 * @param <T>           element type
	 * @param finder        DAO used to access and delete instances
	 * @throws DAOException persistence layer error
	 * @deprecated use {@link AbstractDAO#removeAll()}
	 */
	@Deprecated
	public static <T extends Model> void removeAll(AbstractDAO<T> finder) throws DAOException {
		for (T t : finder.findAll()) 
			finder.remove(t);	
	}

	// ------------------------------------------------------------------
	// use direct DAO call 
	
	/**
	 * Find by code.
	 * @param <T>           element type
	 * @param type          type of object to find
	 * @param finder        DAO
	 * @param code          code of object to find
	 * @return              found object or null
	 * @throws DAOException DAO error
	 * @deprecated use {@link AbstractDAO#findByCode(String)}
	 */
	@Deprecated
	public static <T extends Model> T getModelByCode(Class<T> type, AbstractDAO<T> finder, String code) throws DAOException {
		return finder.findByCode(code);
	}

	// ------------------------------------------------------------------

	// FDS 24/10/2019 : NGL-2719: rollback refactoring=> remove @deprecated
	/**
	 * Get a list of objects by codes and throws exception if a required object is not found
	 * 
	 * @param <T>           element type
	 * @param type          type of objects
	 * @param finder        DAO
	 * @param codes         code of objects to find
	 * @return              list of found objects
	 * @throws DAOException DAO error
	 */

	public static <T extends Model> List<T> getModelByCodes(Class<T> type, AbstractDAO<T> finder, String... codes) throws DAOException {
		List<T> l = new ArrayList<>();
		for (String code : codes) {
			logger.debug("getModelByCode {} {}", type, code);
			T t = finder.findByCode(code);
			logger.debug("getModelByCode {} {} {}", type, code, t);
			//daoAssertNotNull("<" + type + ">getModelByCodes("+code+")",t);
			daoAssertNotNull("<"+ type +"> code '"+ code +"' not found in model.",t);
			l.add(t);
		}
		return l;
	}
	
	// ------------------------------------------------------------------

	/**
	 * Persists (save, not update) an instance of a given class in the database.
	 * @param <T>           element type
	 * @param type          type object to persist
	 * @param model         instance to persist
	 * @param errors        validation context like error list (ignored)
	 * @throws DAOException persistence error
	 */
	public static <T extends Model> void saveModel(Class<T> type, T model, Map<String,List<ValidationError>> errors) throws DAOException {
		ModelDAOs daos = ModelDAOs.instance.get(); 
		T t = daos.fromDB(model);
		if (t == null) {
			logger.info("save {} : {}", type, model.code);
			daos.save(model);
		} else {
			logger.info("already exists {} : {} ", type, model.code);
		}
	}

	// ------------------------------------------------------------------

	/**
	 * Persist (save, not update) a list of instances of a given class.
	 * @param <T>           element type
	 * @param type          type of object to persist
	 * @param models        instances to persist
	 * @param errors        validation context like validation error list (ignored)
	 * @throws DAOException persistence error
	 */
 	public static <T extends Model> void saveModels(Class<T> type, List<T> models, Map<String,List<ValidationError>> errors) throws DAOException {
		for (T model : models)
			saveModel(type, model, errors);
	}

	/**
	 * Update an existing SQL mapped object.
	 * @param <T>           element type
	 * @param type          java class of the mapped object
	 * @param model         persisted object
	 * @param errors        validation context like error list (ignored)
	 * @throws DAOException persistence failed
	 */
	public static <T extends Model> void updateModel(Class<T> type, T model, Map<String,List<ValidationError>> errors) throws DAOException {
		ModelDAOs daos = ModelDAOs.instance.get();
		T t = daos.fromDB(model);
		if (t != null) {
			daos.update(model);
		} else {
			logger.debug("o object of type {} with code {}", type.getName(), model.code);
		}
	}

	/**
	 * Update a list of SQL mapped objects. 
	 * @param <T>           element type
	 * @param type          java class of the mapped objects
	 * @param models        objects to update
	 * @param errors        validation context like error list (ignored)
	 * @throws DAOException persistence failed
	 */
	public static <T extends Model> void updateModels(Class<T> type, List<T> models, Map<String,List<ValidationError>> errors) throws DAOException {
		for (T model : models)
			updateModel(type, model, errors);
	}

//	/*
//	 * Create the sql to join with institute
//	 * The rule is simple the join table name equals <main_table_name>_institute
//	 * @param mainTable
//	 * @param mainTableAlias
//	 * @return
//	 */
//	public static String getSQLForInstitute(String mainTable, String mainTableAlias) { 
//		List<String> institutes = DescriptionHelper.getInstitutes();
//		String SQLInstitute = " inner join " + mainTable + "_institute " 
//				                     + "as " + mainTable + "_join_institute " 
//				                     + "on " + mainTable + "_join_institute.fk_" + mainTable + " = " + mainTableAlias + ".id "
//				            + " inner join institute " 
//				                     + "as " + mainTable + "_inst "
//				                     + "on " + mainTable + "_inst.id = " + mainTable + "_join_institute.fk_institute ";
//		// Prend en compte tous les instituts
//		if (institutes.size() == 0) {
//			return SQLInstitute = "";
//			//Si un seul institut
//		} else if (institutes.size() == 1) {
//			return SQLInstitute+= " and "+mainTable+"_inst.code = '" + DescriptionHelper.getInstitutes().get(0)+"' ";
//		} else {
//			// Si plusieurs instituts (clause in)
////			SQLInstitute+="  and "+mainTable+"_inst.code in (";
////			
////			String comma="";
////			for(int i=0;i<institutes.size();i++){
////				if(i==1) comma=",";
////				SQLInstitute+=comma+"'"+institutes.get(i)+"'";
////			}
////			return SQLInstitute+=") ";
//			return SQLInstitute + " and " + mainTable + "_inst.code in (" + String.join(",", institutes) + ") ";
//		}		
//	}

	/*
	 * Create the sql to join with institute
	 * The rule is simple the join table name equals <main_table_name>_institute
	 * @param mainTable
	 * @param mainTableAlias
	 * @return
	 */
	public static String getSQLForInstitute(String mainTable, String mainTableAlias) { 
		List<String> institutes = DescriptionHelper.getInstitutes();
		if (institutes.size() == 0)
			return "";
		String SQLInstitute = " inner join " + mainTable + "_institute " 
				                     + "as " + mainTable + "_join_institute " 
				                     + "on " + mainTable + "_join_institute.fk_" + mainTable + " = " + mainTableAlias + ".id "
				            + " inner join institute " 
				                     + "as " + mainTable + "_inst "
				                     + "on " + mainTable + "_inst.id = " + mainTable + "_join_institute.fk_institute ";
		if (institutes.size() == 1)
			return SQLInstitute + " and " + mainTable + "_inst.code = '" + DescriptionHelper.getInstitutes().get(0)+"' ";
		else
			return SQLInstitute + " and " + mainTable + "_inst.code in " 
					            + Iterables.map(institutes, s -> "'" + s +"'").surround("(", ",", ")").asString();
	}

	public static String getInstrumentSQLForInstitute(String tableAlias) {		 
		return getSQLForInstitute("instrument", tableAlias);		
	}
		
	public static String getCommonInfoTypeSQLForInstitute(String tableAlias) {		 
		return getSQLForInstitute("common_info_type", tableAlias);		
	}
	
//	public static String getCommonInfoTypeDefaultSQLForInstitute() {
//		if (SQLInstitute == null)
//			SQLInstitute = getCommonInfoTypeSQLForInstitute("t");
//		return SQLInstitute;
//	}
	
	public static String getCommonInfoTypeDefaultSQLForInstitute() {
		return getCommonInfoTypeSQLForInstitute("t");
	}
	
}
