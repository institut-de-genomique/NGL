package validation.container.instance;

import static fr.cea.ig.lfw.utils.Iterables.range;
import static fr.cea.ig.lfw.utils.Iterables.zip;

import static ngl.refactoring.state.ContainerStateNames.A;
import static ngl.refactoring.state.ContainerStateNames.A_PF;
import static ngl.refactoring.state.ContainerStateNames.A_QC;
import static ngl.refactoring.state.ContainerStateNames.A_TF;
import static ngl.refactoring.state.ContainerStateNames.A_TM;
import static ngl.refactoring.state.ContainerStateNames.IS;
import static ngl.refactoring.state.ContainerStateNames.IU;
import static ngl.refactoring.state.ContainerStateNames.IW_D;
import static ngl.refactoring.state.ContainerStateNames.IW_E;
import static ngl.refactoring.state.ContainerStateNames.IW_P;
import static ngl.refactoring.state.ContainerStateNames.N;
import static ngl.refactoring.state.ContainerStateNames.UA;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.collections4.CollectionUtils;

import fr.cea.ig.lfw.utils.DoubleKeySet;
import models.laboratory.common.description.Level;
import models.laboratory.common.description.ObjectType;
import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.description.ContainerCategory;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.Content;
import models.laboratory.container.instance.LocationOnContainerSupport;
import models.laboratory.container.instance.QualityControlResult;
import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.instrument.description.InstrumentUsedType;
import models.laboratory.processes.description.ProcessType;
import models.laboratory.processes.instance.Process;
import models.laboratory.sample.description.ImportType;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import validation.utils.ValidationConstants;
import validation.utils.ValidationHelper;

public class ContainerValidationHelper extends CommonValidationHelper {

	/**
	 * Container state context parameter key (see {@link #STATE_CONTEXT_CONTROLLER}, 
	 * {@link #STATE_CONTEXT_WORKFLOW}).
	 */
//	@Deprecated
	public static final String FIELD_STATE_CONTAINER_CONTEXT           = "stateContainerContext";
	
	/**
	 * Container update context parameter key (boolean value).
	 */
//	@Deprecated
	public static final String FIELD_UPDATE_CONTAINER_SUPPORT_STATE    = "updateContainerSupportState";
	
	/**
	 * Container state parameter key (string value).
	 */
//	@Deprecated
	public static final String FIELD_UPDATE_CONTAINER_STATE            = "updateContainerState";

	// State context should be an enumeration
	
	/**
	 * State context.
	 */
	public static final String STATE_CONTEXT_WORKFLOW   = "workflow";
	
	/**
	 * State context.
	 */
	public static final String STATE_CONTEXT_CONTROLLER = "controllers";

	// -----------------------------------------------------------------
	// renamed and arguments reordered
	
	/**
	 * Validate that a container category exists and that the container support category
	 * is valid for the container category.
	 * @param categoryCode        container category code
	 * @param supportCategoryCode container support category code
	 * @param contextValidation   validation context
	 * @deprecated use {@link #validateContainerCategoryCodeRequired(ContextValidation, String, String)}
	 */
	@Deprecated
	public static void validateContainerCategoryCode_(String categoryCode, String supportCategoryCode, ContextValidation contextValidation) {
		ContainerValidationHelper.validateContainerCategoryCodeRequired(contextValidation, categoryCode, supportCategoryCode);
	}

	/**
	 * Validate that a container category exists and that the container support category
	 * is valid for the container category.
	 * @param contextValidation   validation context
	 * @param categoryCode        container category code
	 * @param supportCategoryCode container support category code
	 */
	public static void validateContainerCategoryCodeRequired(ContextValidation contextValidation, String categoryCode, String supportCategoryCode) {
//		BusinessValidationHelper.validateRequiredDescriptionCode(contextValidation, categoryCode, "categoryCode", ContainerCategory.find.get(),false);
		validateCodeForeignRequired                   (contextValidation, ContainerCategory.miniFind.get(), categoryCode, "categoryCode", false);
		validateCategoryCodeAgainstSupportCategoryCode(contextValidation, categoryCode, supportCategoryCode);
	}
	
	private static void validateCategoryCodeAgainstSupportCategoryCode(ContextValidation contextValidation, 
																	   String categoryCode,
																	   String supportCategoryCode) {
		ContainerCategory cc = ContainerCategory.find.get().findByContainerSupportCategoryCode(supportCategoryCode);
		if (! categoryCode.equals(cc.code))  
			contextValidation.addError("categoryCode", ValidationConstants.ERROR_VALUENOTAUTHORIZED_MSG);
	}

	// -----------------------------------------------------------------
	// arguments reordered  
	
	/**
	 * Validate container contents.
	 * @param contents          contents to validate 
	 * @param contextValidation validation context
	 */
	public static void validateContents(List<Content> contents, ContextValidation contextValidation) {
		ContainerValidationHelper.validateContents(contextValidation, contents);
	}

	/**
	 * Validate container contents.
	 * @param contextValidation validation context
	 * @param contents          contents to validate
	 * @deprecated use {@link #validateContents(ContextValidation, List, String)} 
	 */
	@Deprecated
	public static void validateContents(ContextValidation contextValidation, List<Content> contents) {
		if (ValidationHelper.validateNotEmpty(contextValidation, contents, "contents")) {
//			Iterator<Content> iterator = contents.iterator();
//			int i = 0;
//			while (iterator.hasNext()) {
//				contextValidation.addKeyToRootKeyName("contents["+i+"]");
//				iterator.next().validate(contextValidation);
//				contextValidation.removeKeyFromRootKeyName("contents["+i+"]");
//				i++;
//			}
//			for (ImmutablePair<Integer,Content> p : Iterables.zip(Iterables.range(0), contents)) {
//				contextValidation.addKeyToRootKeyName("contents[" + p.left + "]");
//				p.right.validate(contextValidation);
//				contextValidation.removeKeyFromRootKeyName("contents[" + p.left + "]");
//
//			}
			int i = 0;
			for (Content content : contents) {
				String key = "contents[" + i + "]";
				contextValidation.addKeyToRootKeyName(key);
				content.validate(contextValidation);
				contextValidation.removeKeyFromRootKeyName(key);
				i++;
			}
			validateContentPercentageSum(contents, contextValidation);
		}
	}
	
	/**
	 * Validate container contents using an optional import type.
	 * @param contextValidation validation context
	 * @param contents          contents to validate
	 * @param importTypeCode    import type code
	 */
	// Iterable + validation context alternate implementation
	// _CTX_PARAM: append path + iterables
	public static void validateContents_(ContextValidation contextValidation, List<Content> contents, String importTypeCode) {
		if (ValidationHelper.validateNotEmpty(contextValidation, contents, "contents")) {
			zip(range(0), contents).unzipEach((i,c) -> c.validate(contextValidation.appendPath("contents[" + i + "]"), importTypeCode));
			validateContentPercentageSum(contents, contextValidation);
		}
	}
	
	/**
	 * Validate container contents using an optional import type.
	 * @param contextValidation validation context
	 * @param contents          contents to validate
	 * @param importTypeCode    import type code
	 */
	public static void validateContents(ContextValidation contextValidation, List<Content> contents, String importTypeCode) {
		if (ValidationHelper.validateNotEmpty(contextValidation, contents, "contents")) {
			int i = 0;
			for (Content content : contents) {
				String key = "contents[" + i + "]";
				contextValidation.addKeyToRootKeyName(key);
				content.validate(contextValidation, importTypeCode);
				contextValidation.removeKeyFromRootKeyName(key);
				i++;
			}
			validateContentPercentageSum(contents, contextValidation);
		}
	}

	// --------------------------------------------------------------------------------
	// renamed and arguments reordered
	
	/**
	 * Validate a (container) state.
	 * @param state             state
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateContainerStateRequired(ContextValidation, State)}
	 */
	@Deprecated
	public static void validateState(State state, ContextValidation contextValidation) {
		validateContainerStateRequired(contextValidation, state);
	}
	
	/**
	 * Validate a (container) state.
	 * @param contextValidation validation context
	 * @param state             state
	 */
	public static void validateContainerStateRequired(ContextValidation contextValidation, State state) {
		if (ValidationHelper.validateNotEmpty(contextValidation, state, "state")) {
//			contextValidation.putObject(FIELD_OBJECT_TYPE_CODE, ObjectType.CODE.Container);
			contextValidation.addKeyToRootKeyName("state");
//			state.validate(contextValidation);
			state.validate(contextValidation, ObjectType.CODE.Container);
			contextValidation.removeKeyFromRootKeyName("state");
//			contextValidation.removeObject(FIELD_OBJECT_TYPE_CODE);
		}		
	}
	
	// --------------------------------------------------------------------------------
	// Alternate table based implementation
	
	public static void validateNextState_TT(Container container, State nextState, ContextValidation contextValidation) {
		CommonValidationHelper.validateStateRequired(contextValidation, ObjectType.CODE.Container, nextState);
		DoubleKeySet<String,String> wts = new DoubleKeySet<>(); // workflow transitions
		//         from      to
		wts.addK2s(A,                                   IU, IW_D, IW_E);
		wts.addK2s(A_PF,                                IU, IW_D, IW_E);
		wts.addK2s(A_QC,                                IU, IW_D, IW_E);
		wts.addK2s(A_TM,                                IU, IW_D, IW_E);
		wts.addK2s(A_TF,                                IU, IW_D, IW_E);
		wts.addK2s(IU,       A, A_PF, A_QC, A_TM, A_TF,     IW_D      );
		wts.addK2s(IW_E,     A, A_PF, A_QC, A_TM, A_TF, IU, IW_D      );
		wts.addK2s(IW_P,     A, A_PF, A_QC, A_TM, A_TF                );

		DoubleKeySet<String,String> cts = new DoubleKeySet<>(); // controller transitions
		//         from      to
		cts.addK2s(A,        A, A_PF, A_QC, A_TM, A_TF, IS, UA, IW_P);
		cts.addK2s(A_PF,     A, A_PF, A_QC, A_TM, A_TF, IS, UA, IW_P);
		cts.addK2s(A_QC,     A, A_PF, A_QC, A_TM, A_TF, IS, UA, IW_P);
		cts.addK2s(A_TM,     A, A_PF, A_QC, A_TM, A_TF, IS, UA, IW_P);
		cts.addK2s(A_TF,     A, A_PF, A_QC, A_TM, A_TF, IS, UA, IW_P);
		cts.addK2s(IS,                                      UA, IW_P);
		cts.addK2s(N,        A, A_PF, A_QC, A_TM, A_TF, IS, UA, IW_P);
		cts.addK2s(IU                                               );
		cts.addK2s(IW_D,     A, A_PF, A_QC, A_TM, A_TF, IS, UA, IW_P);
		cts.addK2s(IW_E                                             );
		cts.addK2s(IW_P,                                IS, UA      );
		cts.addK2s(UA,                                  IS,     IW_P);
		
		if (!contextValidation.hasErrors() && !nextState.code.equals(container.state.code)) {
			String nextStateCode    = nextState.code;
			String currentStateCode = container.state.code;
			String context          = contextValidation.getTypedObject(ContainerValidationHelper.FIELD_STATE_CONTAINER_CONTEXT);
			switch (context) {
//			case "workflow":
			case STATE_CONTEXT_WORKFLOW:
				if (!wts.contains(currentStateCode, nextStateCode))
					contextValidation.addError("code",ValidationConstants.ERROR_BADSTATE_MSG, nextStateCode );
				break;
//			case "controllers":
			case STATE_CONTEXT_CONTROLLER:
				if (!cts.contains(currentStateCode, nextStateCode))
					contextValidation.addError("code",ValidationConstants.ERROR_BADSTATE_MSG, nextStateCode );
				break;
			default:
				throw new RuntimeException("FIELD_STATE_CONTAINER_CONTEXT : " + context + " not manage !!!");
			}
		}	

	}
	
	// arguments reordered
	
	/**
	 * Validate that the requested new state can be reached from the current state.
	 * @param container         container with current state
	 * @param nextState         new state
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateNextState(ContextValidation, Container, State)}
	 */
	@Deprecated
	public static void validateNextState(Container container, State nextState, ContextValidation contextValidation) {
		ContainerValidationHelper.validateNextState(contextValidation, container, nextState);
	}

	/**
	 * Validate that the requested new state can be reached from the current state (context
	 * parameter {@link ContainerValidationHelper#FIELD_STATE_CONTAINER_CONTEXT}).
	 * @param contextValidation validation context
	 * @param container         container with current state
	 * @param nextState         new state
	 * @deprecated use {@link #validateNextState(ContextValidation, Container, State, String)}
	 */
	@Deprecated
	public static void validateNextState(ContextValidation contextValidation, Container container, State nextState) {
		CommonValidationHelper.validateStateRequired(contextValidation, ObjectType.CODE.Container, nextState);
		if (!contextValidation.hasErrors() && !nextState.code.equals(container.state.code)) {
//			String nextStateCode    = nextState.code;
//			String currentStateCode = container.state.code;
//			String context = (String) contextValidation.getObject(CommonValidationHelper.FIELD_STATE_CONTAINER_CONTEXT);
			String context = contextValidation.getTypedObject(ContainerValidationHelper.FIELD_STATE_CONTAINER_CONTEXT);
//			switch (context) {
//			case "workflow":
//				if ("IW-P".equals(currentStateCode) && !nextStateCode.startsWith("A")) {
//					contextValidation.addError("code",ValidationConstants.ERROR_BADSTATE_MSG, nextStateCode );
//				} else if(currentStateCode.startsWith("A") && !"IW-E".equals(nextStateCode)  && !"IU".equals(nextStateCode) && !"IW-D".equals(nextStateCode)){
//					contextValidation.addError("code",ValidationConstants.ERROR_BADSTATE_MSG, nextStateCode );
//				} else if("IW-E".equals(currentStateCode) && !"IU".equals(nextStateCode) && !"IW-D".equals(nextStateCode) && !nextStateCode.startsWith("A")){
//					contextValidation.addError("code",ValidationConstants.ERROR_BADSTATE_MSG, nextStateCode );
//				} else if("IU".equals(currentStateCode) && !"IW-D".equals(nextStateCode) 
//						&& !nextStateCode.startsWith("A")/*delete exp case*/){
//					contextValidation.addError("code",ValidationConstants.ERROR_BADSTATE_MSG, nextStateCode );
//				}
//				break;
//			case "controllers":
//				if ("IW-P".equals(currentStateCode) && !nextStateCode.equals("UA") && !nextStateCode.equals("IS")) {
//					contextValidation.addError("code",ValidationConstants.ERROR_BADSTATE_MSG, nextStateCode );
//				} else if(currentStateCode.startsWith("A") && 
//						!nextStateCode.startsWith("A") && !nextStateCode.equals("UA") && !nextStateCode.equals("IS") && !nextStateCode.equals("IW-P")){
//					contextValidation.addError("code",ValidationConstants.ERROR_BADSTATE_MSG, nextStateCode );
//				} else if("IW-D".equals(currentStateCode) && 
//						!nextStateCode.equals("UA") && !nextStateCode.equals("IS") && !nextStateCode.startsWith("A") && !nextStateCode.startsWith("IW-P")){
//					contextValidation.addError("code",ValidationConstants.ERROR_BADSTATE_MSG, nextStateCode );
//				} else if("IS".equals(currentStateCode) && 
//						!nextStateCode.equals("UA") && !nextStateCode.equals("IW-P")){
//					contextValidation.addError("code",ValidationConstants.ERROR_BADSTATE_MSG, nextStateCode );
//				} else if("UA".equals(currentStateCode) && 
//						!nextStateCode.equals("IW-P") && !nextStateCode.equals("IS")){
//					contextValidation.addError("code",ValidationConstants.ERROR_BADSTATE_MSG, nextStateCode );
//				} else if("N".equals(currentStateCode) && 
//						!nextStateCode.equals("UA") && !nextStateCode.equals("IS") && !nextStateCode.startsWith("A") && !nextStateCode.startsWith("IW-P")){
//					contextValidation.addError("code",ValidationConstants.ERROR_BADSTATE_MSG, nextStateCode );
//				} else if("IW-E".equals(currentStateCode) || "IU".equals(currentStateCode)) {
//					contextValidation.addError("code",ValidationConstants.ERROR_BADSTATE_MSG, nextStateCode );
//				}
//				break;
//			default:
//				throw new RuntimeException("FIELD_STATE_CONTAINER_CONTEXT : " + context + " not manage !!!");
//			}
			validateNextState(contextValidation, container, nextState, context);
		}	
	}
	
	/**
	 * Validate that the requested new state can be reached from the current state.
	 * @param contextValidation validation context
	 * @param container         container with current state
	 * @param nextState         new state
	 * @param context           "workflow" or "controllers"
	 */
	public static void validateNextState(ContextValidation contextValidation, Container container, State nextState, String context) {
		CommonValidationHelper.validateStateRequired(contextValidation, ObjectType.CODE.Container, nextState);
		if (!contextValidation.hasErrors() && !nextState.code.equals(container.state.code)) {
			String nextStateCode    = nextState.code;
			String currentStateCode = container.state.code;
			switch (context) {
//			case "workflow":
			case STATE_CONTEXT_WORKFLOW:
				if ("IW-P".equals(currentStateCode) && !nextStateCode.startsWith("A")) {
					contextValidation.addError("code",ValidationConstants.ERROR_BADSTATE_MSG, nextStateCode );
				} else if(currentStateCode.startsWith("A") && !"IW-E".equals(nextStateCode)  && !"IU".equals(nextStateCode) && !"IW-D".equals(nextStateCode)){
					contextValidation.addError("code",ValidationConstants.ERROR_BADSTATE_MSG, nextStateCode );
				} else if("IW-E".equals(currentStateCode) && !"IU".equals(nextStateCode) && !"IW-D".equals(nextStateCode) && !nextStateCode.startsWith("A")){
					contextValidation.addError("code",ValidationConstants.ERROR_BADSTATE_MSG, nextStateCode );
				} else if("IU".equals(currentStateCode) && !"IW-D".equals(nextStateCode) 
						&& !nextStateCode.startsWith("A")/*delete exp case*/){
					contextValidation.addError("code",ValidationConstants.ERROR_BADSTATE_MSG, nextStateCode );
				}
				break;
//			case "controllers":
			case STATE_CONTEXT_CONTROLLER:
				if ("IW-P".equals(currentStateCode) && !nextStateCode.equals("UA") && !nextStateCode.equals("IS")) {
					contextValidation.addError("code",ValidationConstants.ERROR_BADSTATE_MSG, nextStateCode );
				} else if(currentStateCode.startsWith("A") && 
						!nextStateCode.startsWith("A") && !nextStateCode.equals("UA") && !nextStateCode.equals("IS") && !nextStateCode.equals("IW-P")){
					contextValidation.addError("code",ValidationConstants.ERROR_BADSTATE_MSG, nextStateCode );
				} else if("IW-D".equals(currentStateCode) && 
						!nextStateCode.equals("UA") && !nextStateCode.equals("IS") && !nextStateCode.startsWith("A") && !nextStateCode.startsWith("IW-P")){
					contextValidation.addError("code",ValidationConstants.ERROR_BADSTATE_MSG, nextStateCode );
				} else if("IS".equals(currentStateCode) && 
						!nextStateCode.equals("UA") && !nextStateCode.equals("IW-P")){
					contextValidation.addError("code",ValidationConstants.ERROR_BADSTATE_MSG, nextStateCode );
				} else if("UA".equals(currentStateCode) && 
						!nextStateCode.equals("IW-P") && !nextStateCode.equals("IS")){
					contextValidation.addError("code",ValidationConstants.ERROR_BADSTATE_MSG, nextStateCode );
				} else if("N".equals(currentStateCode) && 
						!nextStateCode.equals("UA") && !nextStateCode.equals("IS") && !nextStateCode.startsWith("A") && !nextStateCode.startsWith("IW-P")){
					contextValidation.addError("code",ValidationConstants.ERROR_BADSTATE_MSG, nextStateCode );
				} else if("IW-E".equals(currentStateCode) || "IU".equals(currentStateCode)) {
					contextValidation.addError("code",ValidationConstants.ERROR_BADSTATE_MSG, nextStateCode );
				}
				break;
			default:
				throw new RuntimeException("FIELD_STATE_CONTAINER_CONTEXT : " + context + " not handled, use 'workflow' or 'controllers'");
			}
		}	
	}
	
	// ----------------------------------------------------------------------------------------------------

	@Deprecated
	public static void validateStateCode(String stateCode, ContextValidation contextValidation) {
		contextValidation.addKeyToRootKeyName("state");
		CommonValidationHelper.validateStateCodeRequired(contextValidation, ObjectType.CODE.Container, stateCode);
		contextValidation.removeKeyFromRootKeyName("state");
	}
	
	public static <A> void validatePercentageSum(ContextValidation ctx, Collection<A> c, Function<A,Double> f, double epsilon, String path0, String path1) {
		double sum = 0;
		for (A a : c) {
			if (a != null) {
				Double p = f.apply(a);
				if (p != null)
					sum += p;
			}
		}
		if (!(Math.abs(100.00 - sum) <= epsilon)) 
			ctx.appendPath(path0)
			   .addError(path1, ValidationConstants.ERROR_VALUENOTAUTHORIZED_MSG, sum);
	}
	
	public static void validateContentPercentageSum_(List<Content> contents, ContextValidation contextValidation) {
		validatePercentageSum(contextValidation, contents, c -> c.percentage, 0.4, "contents", "precentageSum");
	}
	
	// Check the sum of percentage of contents is 100
	public static void validateContentPercentageSum(List<Content> contents, ContextValidation contextValidation) {
		Double percentageSum = 0.00;
		for (Content t : contents) {
			if (t.percentage != null) {
				percentageSum = percentageSum + t.percentage;
			}							
		}
		// NOTE do not test exactly 100 because of floating values...
		if (!(Math.abs(100.00-percentageSum) <= 0.40)) {
			contextValidation.addKeyToRootKeyName("contents");
			contextValidation.addError("percentageSum", ValidationConstants.ERROR_VALUENOTAUTHORIZED_MSG, percentageSum);
			contextValidation.removeKeyFromRootKeyName("contents");			
		}
	}

	// -----------------------------------------------------------------------------
	//
	
	/**
	 * Validate a required container support. 
	 * @param support           container support to validate
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateContainerSupportRequired(ContextValidation, LocationOnContainerSupport)}
	 */
	@Deprecated
	public static void validateContainerSupport(LocationOnContainerSupport support, ContextValidation contextValidation) {
		ContainerValidationHelper.validateContainerSupportRequired(contextValidation, support);
	}

	/**
	 * Validate a required container support. 
	 * @param contextValidation validation context
	 * @param support           container support to validate
	 */
	public static void validateContainerSupportRequired(ContextValidation contextValidation, LocationOnContainerSupport support) {
		if (ValidationHelper.validateNotEmpty(contextValidation, support, "support")) {
			contextValidation.addKeyToRootKeyName("support");
			support.validate(contextValidation);
			contextValidation.removeKeyFromRootKeyName("support");
		}		
	}

	// -----------------------------------------------------------------------------
	// arguments reordered
	
	/**
	 * Validate an optional list of optional process codes (context 
	 * parameter {@link #FIELD_STATE_CODE}).
	 * @param processCodes      process codes to validate    
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateInputProcessCodes(ContextValidation, Set)}
	 */
	@Deprecated
	public static void validateInputProcessCodes(Set<String> processCodes, ContextValidation contextValidation) {
		ContainerValidationHelper.validateInputProcessCodes(contextValidation, processCodes);
	}

	/**
	 * Validate an optional list of optional process codes (context 
	 * parameter {@link #FIELD_STATE_CODE}).
	 * @param contextValidation validation context
	 * @param processCodes      process codes to validate
	 * @deprecated use {@link #validateInputProcessCodes(ContextValidation, Collection, String)}   
	 */
	@Deprecated
	public static void validateInputProcessCodes(ContextValidation contextValidation, Set<String> processCodes) {
//		if (processCodes != null && processCodes.size() > 0) {
//			for (String processCode: processCodes) {
////				BusinessValidationHelper.validateExistInstanceCode(contextValidation, processCode, "processCodes", Process.class, InstanceConstants.PROCESS_COLL_NAME); 
////				validateExistInstanceCode(contextValidation, processCode, "processCodes", Process.class, InstanceConstants.PROCESS_COLL_NAME); 
//				validateCodeForeignOptional(contextValidation, Process.find.get(), processCode);
//			}
//		}
////		String stateCode = getObjectFromContext(FIELD_STATE_CODE, String.class, contextValidation);
//		String stateCode = contextValidation.getTypedObject(FIELD_STATE_CODE);
//		if (stateCode.startsWith("A") || stateCode.startsWith("IW-E")) {
//			ValidationHelper.validateNotEmpty(contextValidation, processCodes, "processCodes");
//		} else if ("IW-P".equals(stateCode) && CollectionUtils.isNotEmpty(processCodes)) {
//			contextValidation.addError("processCodes", "error.validation.container.inputProcesses.notnull");
//		}
		String stateCode = contextValidation.getTypedObject(FIELD_STATE_CODE);
		validateInputProcessCodes(contextValidation, processCodes, stateCode);
	}
	
	public static void validateInputProcessCodes(ContextValidation contextValidation, Collection<String> processCodes, String stateCode) {
		if (processCodes != null)
			for (String processCode: processCodes) 
				validateCodeForeignOptional(contextValidation, Process.find.get(), processCode);
		if (stateCode.startsWith("A") || stateCode.startsWith("IW-E")) {
			ValidationHelper.validateNotEmpty(contextValidation, processCodes, "processCodes");
		} else if ("IW-P".equals(stateCode) && CollectionUtils.isNotEmpty(processCodes)) {
			contextValidation.addError("processCodes", "error.validation.container.inputProcesses.notnull");
		}		
	}
	
	// -----------------------------------------------------------------------
	// renamed and arguments reordered
	
	/**
	 * Validate an optional import type and associated properties (if the import type
	 * is provided). 
	 * @param importTypeCode    import type code
	 * @param properties        properties
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateImportTypeOptional(ContextValidation, String, Map)}
	 */
	@Deprecated
	public static void validateImportType(String importTypeCode, Map<String, PropertyValue> properties, ContextValidation contextValidation) {
		ContainerValidationHelper.validateImportTypeOptional(contextValidation, importTypeCode, properties);
	}

	/**
	 * Validate an optional import type and associated properties (if the import type
	 * is provided). 
	 * @param contextValidation validation context
	 * @param importTypeCode    import type code
	 * @param properties        properties
	 */
	public static void validateImportTypeOptional(ContextValidation contextValidation, String importTypeCode,	Map<String, PropertyValue> properties) {
//		ImportType importType = BusinessValidationHelper.validateExistDescriptionCode(contextValidation, importTypeCode,"importTypeCode", ImportType.find.get(),true);
		ImportType importType = validateCodeForeignOptional(contextValidation, ImportType.miniFind.get(), importTypeCode, "importTypeCode",true);
		if (importType != null) {
			List<PropertyDefinition> proDefinitions = new ArrayList<>();
			proDefinitions.addAll(importType.getPropertiesDefinitionContainerLevel());
			
			if (proDefinitions.size() > 0) { 
				ValidationHelper.validateProperties(contextValidation, properties, proDefinitions);
			}
		}
	}
	
	// -----------------------------------------------------------------------
	// arguments reordered
	
	/**
	 * Validate quality control results.
	 * @param qualityControlResults results to validate
	 * @param contextValidation     validation context
	 * @deprecated use {@link #validateQualityControlResults(ContextValidation, List)}
	 */
	@Deprecated
	public static void validateQualityControlResults_(List<QualityControlResult> qualityControlResults, ContextValidation contextValidation) {
		ContainerValidationHelper.validateQualityControlResults(contextValidation, qualityControlResults);
	}

	/**
	 * Validate an optional quality control result collection.
	 * @param contextValidation     validation context
	 * @param qualityControlResults results to validate
	 */
	public static void validateQualityControlResults(ContextValidation contextValidation, List<QualityControlResult> qualityControlResults) {
		contextValidation.addKeyToRootKeyName("qualityControlResults");
		if (qualityControlResults != null) {
			qualityControlResults.stream().forEach(qcr -> {
				String key = "[" + qcr.typeCode + "]";
				contextValidation.addKeyToRootKeyName(key);
				List<PropertyDefinition> propertyDefinitionContainerIn = new ArrayList<>();
				if (qcr.typeCode != null) {
//					ExperimentType exType = BusinessValidationHelper.validateRequiredDescriptionCode(contextValidation, qcr.typeCode, "typeCode", ExperimentType.find.get(),true);
					ExperimentType exType = validateCodeForeignRequired(contextValidation, ExperimentType.miniFind.get(), qcr.typeCode, "typeCode", true);
					if (exType != null) {
						propertyDefinitionContainerIn.addAll(exType.getPropertyDefinitionByLevel(Level.CODE.ContainerIn));								
					}
				}
				
				if (qcr.instrumentUsedTypeCode != null) {
//					InstrumentUsedType insType = BusinessValidationHelper.validateRequiredDescriptionCode(contextValidation, qcr.instrumentUsedTypeCode, "typeCode", InstrumentUsedType.find.get(),true);
					InstrumentUsedType insType = validateCodeForeignRequired(contextValidation, InstrumentUsedType.miniFind.get(), qcr.instrumentUsedTypeCode, "typeCode",true);
					if (insType != null) {
						propertyDefinitionContainerIn.addAll(insType.getPropertyDefinitionByLevel(Level.CODE.ContainerIn));								
					}
				}
				if (CollectionUtils.isNotEmpty(propertyDefinitionContainerIn)) {
					ValidationHelper.validateProperties(contextValidation, qcr.properties, propertyDefinitionContainerIn, true, false, null, null);
				}
				contextValidation.removeKeyFromRootKeyName(key);
			});
		}
		contextValidation.removeKeyFromRootKeyName("qualityControlResults");		
	}
	
	// -------------------------------------------------------------------------
	
	// This should be a factory method in the PropertyDefinition class.
	private static PropertyDefinition newDoublePropertyDefinition(String code) {
		PropertyDefinition pd = new PropertyDefinition();			
		pd.code              = code;
		pd.valueType         = Double.class.getName();
		pd.propertyValueType = PropertyValue.singleType;
		return pd;
	}
	
//	private static void validateOptionalDoubleValueProperty(ContextValidation contextValidation, PropertyValue prop, String name) {
//		if (prop != null && prop.value != null) {
//			Collection<PropertyDefinition> pdefs = new ArrayList<>();		
//			PropertyDefinition pd = new PropertyDefinition();			
//			pd.code = name;
//			pd.valueType = Double.class.getName();
//			pd.propertyValueType = PropertyValue.singleType;
//			pdefs.add(pd);
//			contextValidation.putObject("propertyDefinitions", pdefs);
//			prop.validate(contextValidation);
//			contextValidation.removeObject("propertyDefinitions");	
//			ValidationHelper.validateNotEmpty(contextValidation, ((PropertySingleValue)prop).unit, name + ".unit");
//		}
//	}
	
	/**
	 * Validate an optional double valued property value and a mandatory
	 * unit if the value is defined. 
	 * @param contextValidation validation context
	 * @param prop              property value
	 * @param name              error key
	 */
	private static void validatePropertyValueDoubleOptional(ContextValidation contextValidation, PropertyValue prop, String name) {
		if (prop != null && prop.value != null) {
			prop.validate(contextValidation, Arrays.asList(newDoublePropertyDefinition(name)));
			ValidationHelper.validateNotEmpty(contextValidation, ((PropertySingleValue)prop).unit, name + ".unit");
		}
	}
	
//	public static void validateVolume(PropertyValue volume, ContextValidation contextValidation) {
//		if (volume != null && volume.value != null) {
//			Collection<PropertyDefinition> pdefs = new ArrayList<>();		
//			PropertyDefinition pd = new PropertyDefinition();			
//			pd.code = "volume";
//			pd.valueType = Double.class.getName();
//			pd.propertyValueType = PropertyValue.singleType;
//			pdefs.add(pd);
//			contextValidation.putObject("propertyDefinitions", pdefs);
//			volume.validate(contextValidation);
//			contextValidation.removeObject("propertyDefinitions");	
//			ValidationHelper.validateNotEmpty(contextValidation, ((PropertySingleValue)volume).unit, "volume.unit");
//		}
//	}
	
	// -----------------------------------------------------------------
	// renamed and reordered
	
	/**
	 * Validate an optional volume (no bound check, see 
	 * {@link #validatePropertyValueDoubleOptional(ContextValidation, PropertyValue, String)}).
	 * @param volume            value
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateVolumeOptional(ContextValidation, PropertyValue)}
	 */
	@Deprecated
	public static void validateVolume(PropertyValue volume, ContextValidation contextValidation) {
		ContainerValidationHelper.validateVolumeOptional(contextValidation, volume);
	}

	/**
	 * Validate an optional volume (no bound check, see 
	 * {@link #validatePropertyValueDoubleOptional(ContextValidation, PropertyValue, String)}).
	 * @param contextValidation validation context
	 * @param volume            value
	 */
	public static void validateVolumeOptional(ContextValidation contextValidation, PropertyValue volume) {
		validatePropertyValueDoubleOptional(contextValidation, volume, "volume");
	}
	
	// -------------------------------------------------------------------
	
//	public static void validateConcentration(PropertyValue concentration, ContextValidation contextValidation) {
//		if (concentration != null && concentration.value != null) {
//			Collection<PropertyDefinition> pdefs = new ArrayList<>();
//			PropertyDefinition pd = new PropertyDefinition();
//			pd.code = "concentration";
//			pd.valueType = Double.class.getName();
//			pd.propertyValueType = PropertyValue.singleType;
//			pdefs.add(pd);
//			contextValidation.putObject("propertyDefinitions", pdefs);
//			concentration.validate(contextValidation);
//			contextValidation.removeObject("propertyDefinitions");		
//			ValidationHelper.validateNotEmpty(contextValidation, ((PropertySingleValue)concentration).unit, "concentration.unit");
//		}
//	}

	// -------------------------------------------------------------------
	// renamed and arguments reordered
	
	/**
	 * Validate an optional concentration (no bound check, see 
	 * {@link #validatePropertyValueDoubleOptional(ContextValidation, PropertyValue, String)}).
	 * @param concentration     concentration
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateConcentrationOptional(ContextValidation, PropertyValue)}
	 */
	@Deprecated
	public static void validateConcentration(PropertyValue concentration, ContextValidation contextValidation) {
		ContainerValidationHelper.validateConcentrationOptional(contextValidation, concentration);
	}

	/**
	 * Validate an optional concentration (no bound check, see 
	 * {@link #validatePropertyValueDoubleOptional(ContextValidation, PropertyValue, String)}).
	 * @param contextValidation validation context
	 * @param concentration     concentration
	 */
	public static void validateConcentrationOptional(ContextValidation contextValidation, PropertyValue concentration) {
		validatePropertyValueDoubleOptional(contextValidation, concentration, "concentration");
	}
	
	// -------------------------------------------------------------------
	// renamed and arguments reordered
	
//	public static void validateQuantity(PropertyValue quantity,	ContextValidation contextValidation) {
//		if (quantity != null && quantity.value != null) {
//			Collection<PropertyDefinition> pdefs = new ArrayList<>();
//			PropertyDefinition pd = new PropertyDefinition();
//			pd.code = "quantity";
//			pd.valueType = Double.class.getName();
//			pd.propertyValueType = PropertyValue.singleType;
//			pdefs.add(pd);
//			contextValidation.putObject("propertyDefinitions", pdefs);
//			quantity.validate(contextValidation);
//			contextValidation.removeObject("propertyDefinitions");
//			ValidationHelper.validateNotEmpty(contextValidation, ((PropertySingleValue)quantity).unit, "quantity.unit");
//		}
//	}
	
	/**
	 * Validate an optional quantity (no bound check, see 
	 * {@link #validatePropertyValueDoubleOptional(ContextValidation, PropertyValue, String)}).
	 * @param quantity          quantity   
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateQuantityOptional(ContextValidation, PropertyValue)}
	 */
	@Deprecated
	public static void validateQuantity(PropertyValue quantity, ContextValidation contextValidation) {
		ContainerValidationHelper.validateQuantityOptional(contextValidation, quantity);
	}

	/**
	 * Validate an optional quantity (no bound check, see 
	 * {@link #validatePropertyValueDoubleOptional(ContextValidation, PropertyValue, String)}).
	 * @param contextValidation validation context
	 * @param quantity          quantity   
	 */
	public static void validateQuantityOptional(ContextValidation contextValidation,	PropertyValue quantity) {
		validatePropertyValueDoubleOptional(contextValidation, quantity, "quantity");
	}

	// -------------------------------------------------------------------
	// renamed, arguments reordered
	
//	public static void validateSize(PropertyValue size, ContextValidation contextValidation) {
//		if (size != null && size.value != null) {
//			Collection<PropertyDefinition> pdefs = new ArrayList<>();		
//			PropertyDefinition pd = new PropertyDefinition();			
//			pd.code = "size";
//			pd.valueType = Double.class.getName();
//			pd.propertyValueType = PropertyValue.singleType;
//			pdefs.add(pd);
//			contextValidation.putObject("propertyDefinitions", pdefs);
//			size.validate(contextValidation);
//			contextValidation.removeObject("propertyDefinitions");	
//			ValidationHelper.validateNotEmpty(contextValidation, ((PropertySingleValue)size).unit, "size.unit");
//		}
//	}
	
	/**
	 * Validate an optional size (no bound check, see 
	 * {@link #validatePropertyValueDoubleOptional(ContextValidation, PropertyValue, String)}).
	 * @param contextValidation validation context
	 * @param size              size
	 * @deprecated use {@link #validateSizeOptional(ContextValidation, PropertyValue)}   
	 */
	@Deprecated
	public static void validateSize(PropertyValue size, ContextValidation contextValidation) {
		ContainerValidationHelper.validateSizeOptional(contextValidation, size);
	}

	/**
	 * Validate an optional size (no bound check, see 
	 * {@link #validatePropertyValueDoubleOptional(ContextValidation, PropertyValue, String)}).
	 * @param contextValidation validation context
	 * @param size              size   
	 */
	public static void validateSizeOptional(ContextValidation contextValidation, PropertyValue size) {
		validatePropertyValueDoubleOptional(contextValidation, size, "size");
	}
	
	// -------------------------------------------------------------------
	// arguments reordered
	
	/**
	 * Validate rules for a container.
	 * @param container         container to apply validation rules to
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateRules(ContextValidation, Container)}
	 */
	@Deprecated
	public static void validateRules(Container container, ContextValidation contextValidation) {
		ContainerValidationHelper.validateRules(contextValidation, container);
	}

	/**
	 * Validate rules for a container.
	 * @param contextValidation validation context
	 * @param container         container to apply validation rules to
	 */
	public static void validateRules(ContextValidation contextValidation, Container container) {
		ArrayList<Object> validationfacts = new ArrayList<>();
		validationfacts.add   (container);
		validationfacts.addAll(container.contents);
		validateRulesWithList(contextValidation, validationfacts);
	}
	
	// -------------------------------------------------------------------

	// There is no information about why this should be deprecated and what is the call to substitute.
	// @Deprecated
	public static void validateProcessTypeCode(String processTypeCode, ContextValidation contextValidation) {
//		BusinessValidationHelper.validateExistDescriptionCode(contextValidation, processTypeCode, "processTypeCode", ProcessType.find.get());
		CommonValidationHelper.validateCodeForeignOptional(contextValidation, ProcessType.miniFind.get(), processTypeCode, "processTypeCode");
//		String stateCode = getObjectFromContext(FIELD_STATE_CODE, String.class, contextValidation);
		String stateCode = contextValidation.getTypedObject(FIELD_STATE_CODE);
		if (stateCode.startsWith("A") || stateCode.startsWith("IW-E")) {
			ValidationHelper.validateNotEmpty(contextValidation, processTypeCode, "processTypeCode");
		} else if ("IW-P".equals(stateCode) && processTypeCode != null) {
			contextValidation.addError("processTypeCode", "error.validation.container.inputProcesses.notnull");
		}	
	}
	
	@Deprecated
	public static void validateStateCode(Container container, ContextValidation contextValidation) {
//		boolean workflow = false;
//		if (contextValidation.getObject("workflow") != null) {
//			workflow=true;
//		}
		boolean workflow = contextValidation.getObject("workflow") != null;
		if (CollectionUtils.isEmpty(container.processCodes) && container.state.code.startsWith("A") && !workflow ) {
			contextValidation.addError("state.code",ValidationConstants.ERROR_BADSTATE_MSG,container.code );
		}
		if (CollectionUtils.isNotEmpty(container.processCodes) && container.state.code.equals("IW-P") && !workflow) {
			contextValidation.addError("state.code",ValidationConstants.ERROR_BADSTATE_MSG,container.code );
		}
		contextValidation.addKeyToRootKeyName("state");
		CommonValidationHelper.validateStateCodeRequired(contextValidation, ObjectType.CODE.Container, container.state.code);
		contextValidation.removeKeyFromRootKeyName("state");
	}

}
