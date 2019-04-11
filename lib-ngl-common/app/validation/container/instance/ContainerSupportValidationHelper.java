package validation.container.instance;

import java.util.Set;
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

public class ContainerSupportValidationHelper extends CommonValidationHelper {

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
			if (MongoDBDAO.getCollection(InstanceConstants.CONTAINER_COLL_NAME, Container.class).getCount(query) != 0) {
				// TODO revoir le message d'erreur
				contextValidation.addError("supportCode.line.column", ValidationConstants.ERROR_NOTUNIQUE_MSG, containerSupport.code+"_"+containerSupport.line+"_"+containerSupport.column,"supportCode/line/column");		
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
//		BusinessValidationHelper.validateRequiredDescriptionCode(contextValidation, categoryCode, "categoryCode", ContainerSupportCategory.find.get(),false);
		validateCodeForeignRequired(contextValidation, ContainerSupportCategory.miniFind.get(), categoryCode, "categoryCode",false);
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
//		BusinessValidationHelper.validateExistInstanceCode(contextValidation, storageCode, "storageCode",Storage.class,InstanceConstants.STORAGE_COLL_NAME ,false);
//		validateExistInstanceCode(contextValidation, storageCode, "storageCode", Storage.class, InstanceConstants.STORAGE_COLL_NAME);
		validateCodeForeignOptional(contextValidation, Storage.find.get(), storageCode, "storageCode");
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
				} else if ("N".equals(currentStateCode) && 
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

}
