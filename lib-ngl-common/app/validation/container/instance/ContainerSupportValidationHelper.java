package validation.container.instance;

import play.Logger; //FDS

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;

import com.mongodb.BasicDBObject;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.utils.Iterables;
import models.laboratory.common.description.ObjectType;
import models.laboratory.common.instance.State;
import models.laboratory.container.description.ContainerSupportCategory;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.ContainerSupport;
import models.laboratory.container.instance.LocationOnContainerSupport;
import models.laboratory.storage.instance.Storage;
import models.utils.InstanceConstants;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import validation.utils.ValidationConstants;

public class ContainerSupportValidationHelper {

	// -----------------------------------------------------------------------------
	// arguments reordered
	
	/**
	 * Validate that container support location has uniqueness property (support code, line, column).
	 * @param containerSupport  location to validate
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateUniqueContainerSupportCodePosition(ContextValidation, LocationOnContainerSupport)}
	 */
	@Deprecated
	public static void validateUniqueContainerSupportCodePosition(LocationOnContainerSupport containerSupport,	ContextValidation contextValidation) {
		ContainerSupportValidationHelper.validateUniqueContainerSupportCodePosition(contextValidation, containerSupport);
	}

	/**
	 * Validate that container support location has uniqueness property (support code, line, column).
	 * @param contextValidation validation context
	 * @param containerSupport  location to validate
	 */
	public static void validateUniqueContainerSupportCodePosition(ContextValidation contextValidation, LocationOnContainerSupport containerSupport) {
		if (contextValidation.isCreationMode()) {
			Query query = DBQuery.and(DBQuery.is("support.line",   containerSupport.line),
					                  DBQuery.is("support.column", containerSupport.column),
					                  DBQuery.is("support.code",   containerSupport.code));
//			if (MongoDBDAO.getCollection(InstanceConstants.CONTAINER_COLL_NAME, Container.class).getCount(query) != 0) {
			if (MongoDBDAO.getCount(InstanceConstants.CONTAINER_COLL_NAME, Container.class, query) != 0) {
				// GA: revoir le message d'erreur
				contextValidation.addError("supportCode.line.column", ValidationConstants.ERROR_NOTUNIQUE_MSG, containerSupport.code+"_"+containerSupport.line+"_"+containerSupport.column,"supportCode/line/column");		
			}
		}
	}
	
	/** 
	 * 23/09/2021 NGL-2322 validate support.line and support.column against supportCategoryCode
	 *         validateCategoryCodeAgainstSupportCategoryCode has already been validated !!!!
	 * @author F. Dos Santos
	 * @param contextValidation validation context
	 * @param container
	 * @param support
	 *   exemples:
	 *      support tube           valid values: line=1 / column=1
	 *      support strip-8        valid values: line=1 / column in [1-8]
	 *      support 96-well-plate  valid values: line in [A-H] / column in [1-12]
	 *      support 384-well-plate valid values: line in [A-P] / column in [1-24]
	 *      support flowcell-2     valid values: line in [1-2] / column in [1-1]
	 *      support flowcell-4     valid values: line in [1-4] / column in [1-1]
	 *      support flowcell-8     valid values: line in [1-8] / column in [1-1]
	 *      
	 */
	public static void  validateContainerSupportLineColumnConsistancy(ContextValidation contextValidation, LocationOnContainerSupport support) {
		//System.out.println("validateContainerSupportLineColumnConsistancy......");
		Logger.debug("validateContainerSupportLineColumnConsistancy...");
		
		Map<String,validLC> supportMap = new HashMap<>(0);
                                                 //   line        column
		supportMap.put("tube",          new validLC("1","1",    "1","1"));
		supportMap.put("strip-8",       new validLC("1","1",    "1","8"));
		supportMap.put("96-well-plate", new validLC("A","H",    "1","12"));
		supportMap.put("384-well-plate",new validLC("A","P",    "1","24"));
		supportMap.put("flowcell-2",    new validLC("1","2",    "1","1"));
		supportMap.put("flowcell-4",    new validLC("1","4",    "1","1"));
		supportMap.put("flowcell-8",    new validLC("1","8",    "1","1"));
		/// TO BE COMPLETED !!!!!

		if ( ! supportMap.containsKey(support.categoryCode)) {
			//System.out.println("warning "+ support.categoryCode + " not controled !!!");
			Logger.debug("warning "+ support.categoryCode + "validLC not controled !!!");
		} else {
			final validLC vlc= supportMap.get(support.categoryCode);
			if (null == vlc) 
				//contextValidation.addError( "Internal Error","validLC missing for ", support.categoryCode );
				throw new RuntimeException("validLC missing for support " +support.categoryCode);
			
			if ( !contextValidation.hasErrors() ) {
				String valid=null;
				if (! isValidLine( support.line, vlc) ){
					if (! vlc.minLine.equals(vlc.maxLine)){ valid=vlc.minLine+ "-"+vlc.maxLine; } 
					else {                                  valid=vlc.minLine;}
					//System.out.println("not valid:<"+support.line+">");
					// attention la clé doit être unique sinon plusieurs messages s'écrasent!!
					contextValidation.addError(support.code+ " "+support.line, ValidationConstants.ERROR_VALUENOTAUTHORIZED_MSG, 
							    support.line + " (lignes valides pour la catégorie de support '"+ support.categoryCode+ "' : ["+valid+"])");
					
				}
				if  (! isValidColumn ( support.column, vlc) ){
					if (!vlc.minColumn.equals(vlc.maxColumn)){valid=vlc.minColumn+ "-"+vlc.maxColumn;}
					else {                                    valid=vlc.minColumn;}
					//System.out.println("not valid:<"+support.column+">");
					// attention la clé doit être unique sinon plusieurs messages s'écrasent!!
					contextValidation.addError(support.code+" "+support.column, ValidationConstants.ERROR_VALUENOTAUTHORIZED_MSG, 
							    support.column + " (colonnes valides pour la catégorie de support '"+ support.categoryCode+ "' : ["+valid+"])");	
				}
			}
		}
	}

	
	// -------------------------------------------------------------------------
	// renamed and arguments reordered
	
	/**
	 * Validate a container support category code.
	 * @param categoryCode      support category code
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateContainerSupportCategoryCodeRequired(ContextValidation, String)}
	 */
	@Deprecated
	public static void validateContainerSupportCategoryCode_(String categoryCode, ContextValidation contextValidation) {
		ContainerSupportValidationHelper.validateContainerSupportCategoryCodeRequired(contextValidation, categoryCode);
	}
	
	/**
	 * Validate a container support category code.
	 * @param contextValidation validation context
	 * @param categoryCode      support category code
	 */
	public static void validateContainerSupportCategoryCodeRequired(ContextValidation contextValidation, String categoryCode) {
		CommonValidationHelper.validateCodeForeignRequired(contextValidation, ContainerSupportCategory.miniFind.get(), categoryCode, "categoryCode",false);
	}

	// -------------------------------------------------------------------------
	// renamed and arguments reordered

	/**
	 * Validate a storage code.
	 * @param storageCode       storage code to validate
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateStorageCodeOptional(ContextValidation, String)}
	 */
	@Deprecated
	public static void validateStorageCode_(String storageCode, ContextValidation contextValidation) {
		validateStorageCodeOptional(contextValidation, storageCode);
	}

	/**
	 * Validate a storage code.
	 * @param contextValidation validation context
	 * @param storageCode       storage code to validate
	 */
	public static void validateStorageCodeOptional(ContextValidation contextValidation, String storageCode) {
		CommonValidationHelper.validateCodeForeignOptional(contextValidation, Storage.find.get(), storageCode, "storageCode");
	}

	/**
	 * validateCodeImportFile
	 * 
	 * @param contextValidation Le contexte de validation de l'import fichier.
	 * @param supportCode Le code du support à valider.
	 */
	public static void validateCodeImportFile(ContextValidation contextValidation, String supportCode) {
		if (supportCode != null) {
			Pattern pattern = Pattern.compile("_[A-Z][0-9]$");
			Matcher matcher = pattern.matcher(supportCode);

			Pattern pattern2 = Pattern.compile("_[A-Z]1[0-2]$");
			Matcher matcher2 = pattern2.matcher(supportCode);

			if (matcher.find() || matcher2.find()) {
				contextValidation.addError("code", "Le code du support doit comporter N lettres ou chiffres et pas un _ avec une position.");
			}
		}
	}
	
	// -------------------------------------------------------------------------
	// arguments reordered
	
	/**
	 * Validate that a state is a valid next state for a container. 
	 * @param container         container
	 * @param nextState         next state
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateNextState(ContextValidation, ContainerSupport, State)}
	 */
	@Deprecated
	public static void validateNextState(ContainerSupport container, State nextState, ContextValidation contextValidation) {
		ContainerSupportValidationHelper.validateNextState(contextValidation, container, nextState);
	}

	/**
	 * Validate that a state is a valid next state for a container (context
	 * parameter {@link ContainerValidationHelper#FIELD_STATE_CONTAINER_CONTEXT}). 
	 * @param contextValidation validation context
	 * @param container         container
	 * @param nextState         next state
	 */
	@Deprecated
	public static void validateNextState(ContextValidation contextValidation, ContainerSupport container, State nextState) {
		String context          = contextValidation.getTypedObject(ContainerValidationHelper.FIELD_STATE_CONTAINER_CONTEXT);
		validateNextState(contextValidation, container, nextState, context);
	}
	
	/**
	 * Validate that a state is a valid next state for a container. 
	 * @param contextValidation validation context
	 * @param container         container
	 * @param nextState         next state
	 * @param context           either "worlkflow" or "controller"
	 */
	public static void validateNextState(ContextValidation contextValidation, ContainerSupport container, State nextState, String context) {
		CommonValidationHelper.validateStateRequired(contextValidation, ObjectType.CODE.Container, nextState);
		if (!contextValidation.hasErrors() && !nextState.code.equals(container.state.code)) {
			String nextStateCode    = nextState.code;
			String currentStateCode = container.state.code;
//			String context          = (String) contextValidation.getObject(CommonValidationHelper.FIELD_STATE_CONTAINER_CONTEXT);
//			String context          = contextValidation.getTypedObject(CommonValidationHelper.FIELD_STATE_CONTAINER_CONTEXT);
			if (context == null)
				throw new RuntimeException("context paramter CommonValidationHelper.FIELD_STATE_CONTAINER_CONTEXT is not set");
			switch (context) {
			case "workflow":
				if ("IW-P".equals(currentStateCode) && !nextStateCode.startsWith("A")) {
					contextValidation.addError("code",ValidationConstants.ERROR_BADSTATE_MSG, nextStateCode );
				} else if (currentStateCode.startsWith("A") && !nextStateCode.startsWith("A") && !"IW-E".equals(nextStateCode) && !"IU".equals(nextStateCode) && !"IW-D".equals(nextStateCode) ) {
					contextValidation.addError("code",ValidationConstants.ERROR_BADSTATE_MSG, nextStateCode );
				} else if ("IW-E".equals(currentStateCode) && !"IU".equals(nextStateCode) && !"IW-D".equals(nextStateCode) && !nextStateCode.startsWith("A")) {
					contextValidation.addError("code",ValidationConstants.ERROR_BADSTATE_MSG, nextStateCode );
				} else if ("IU".equals(currentStateCode) && !"IW-D".equals(nextStateCode)
						&& !nextStateCode.startsWith("A")/*delete exp case*/) {
					contextValidation.addError("code",ValidationConstants.ERROR_BADSTATE_MSG, nextStateCode );
				}
				break;
			case "controllers":
				if ("IW-P".equals(currentStateCode) && 
						!nextStateCode.equals("UA") && !nextStateCode.equals("IS")){
					contextValidation.addError("code",ValidationConstants.ERROR_BADSTATE_MSG, nextStateCode );
				} else if (currentStateCode.startsWith("A") && 
						(!nextStateCode.startsWith("A") && !nextStateCode.equals("UA") && !nextStateCode.equals("IS") && !nextStateCode.equals("IW-P") 
								|| (!"A".equals(nextStateCode) && !getContainerStates(container).contains(nextStateCode)))) {
					contextValidation.addError("code",ValidationConstants.ERROR_BADSTATE_MSG, nextStateCode );
				} else if ("IS".equals(currentStateCode) && 
						!nextStateCode.equals("UA") && !nextStateCode.equals("IW-P")) {
					contextValidation.addError("code",ValidationConstants.ERROR_BADSTATE_MSG, nextStateCode );
				} else if ("UA".equals(currentStateCode) && 
						!nextStateCode.equals("IW-P") && !nextStateCode.equals("IS")){
					contextValidation.addError("code",ValidationConstants.ERROR_BADSTATE_MSG, nextStateCode );
				} else if ("N".equals(currentStateCode) && !nextStateCode.equals("N") &&
						!nextStateCode.equals("UA") && !nextStateCode.equals("IS") && !nextStateCode.equals("IW-P") && !nextStateCode.startsWith("A")){
					contextValidation.addError("code",ValidationConstants.ERROR_BADSTATE_MSG, nextStateCode );
				} else if("IW-E".equals(currentStateCode) || "IU".equals(currentStateCode)) {
					contextValidation.addError("code",ValidationConstants.ERROR_BADSTATE_MSG, nextStateCode );
				}
				// !!! No need rules for state IW-D because when we manage a plate the support state is depending of container states
				break;
			default:
				throw new RuntimeException("FIELD_STATE_CONTAINER_CONTEXT : " + context + " not handled");
			}
			/*
			if (("IS".equals(currentStateCode) || "UA".equals(currentStateCode)) && !nextStateCode.equals("IW-P")) {
				contextValidation.addErrors("code",ValidationConstants.ERROR_BADSTATE_MSG, nextStateCode );
			}
			*/
		}
				
	}
	
	public static Set<String> getContainerStates(ContainerSupport containerSupport) {
		BasicDBObject keys = new BasicDBObject();
		keys.put("code",  1);
		keys.put("state", 1);
		return MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, 
				               Container.class, 
				               DBQuery.in("support.code", containerSupport.code), keys)
				         .toList()
				         .stream()
				         .map(c -> c.state.code)
				         .collect(Collectors.toSet());
	}
	
	// This avoids the construction of an intermediate list compared to the original implementation (cursor#toList) 
	public static Set<String> getContainerStates_(ContainerSupport containerSupport) {
		BasicDBObject keys = new BasicDBObject();
		keys.put("code",  1);
		keys.put("state", 1);
		return Iterables.zen(MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, 
				                             Container.class, 
				                             DBQuery.in("support.code", containerSupport.code), keys).cursor)
				        .map(c -> c.state.code)
				        .toSet();
	}
	
	//23/09/2021 NGL-2322 
	public static class validLC {
		private String minLine;
		private String maxLine;
		private String minColumn;
		private String maxColumn;
		
		public validLC(String minL, String maxL, String minC, String maxC) {
			minLine = minL;
			maxLine = maxL;
			minColumn = minC;
			maxColumn = maxC;
		}
	}
	// usage of compareTo :
	// "a".compareTo("b"); // returns a negative number, here -1
	// "a".compareTo("a"); // returns  0
	//  b".compareTo("a"); // returns a positive number, here +1
	//// ! ne marche pas pour des numériques !!!!
	public static Boolean isValidLine( String supportLine , validLC vlc ) {
		// if ( support.line    ! between vlc.minLine and vlc.maxLine  return false;
		//System.out.println("validLine :"+ supportLine + "["+vlc.minLine+"-"+ vlc.maxLine+"]");
  
		if (( supportLine.compareTo(vlc.minLine) < 0 )||( supportLine.compareTo(vlc.maxLine) > 0 )){ return false; }
		else { return true; }
	}
	
	// ne pas utiliser compareTo !!
	public static Boolean isValidColumn( String supportColumn , validLC vlc ) {
		// if (support.column   ! between vlc.minColumn and vlc.maxColumn  return false;
		//System.out.println("validColumn :"+ supportColumn + "["+vlc.minColumn+"-"+ vlc.maxColumn+"]");
		int supportCol;
		try {
			supportCol=Integer.parseInt(supportColumn);
		} catch (NumberFormatException e) {
			return false; 
		}
		
		if ( (supportCol < Integer.parseInt(vlc.minColumn)) || (supportCol > Integer.parseInt(vlc.maxColumn))) { return false;}
		//if (( supportColumn.compareTo(vlc.minColumn) < 0 )||( supportColumn.compareTo(vlc.maxColumn) > 0 )){ return false; }
		else { return true; }
	}
}
