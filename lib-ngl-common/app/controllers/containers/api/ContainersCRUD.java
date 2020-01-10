package controllers.containers.api;

//import static fr.cea.ig.play.IGGlobals.form;
import static validation.container.instance.ContainerValidationHelper.validateConcentration;
import static validation.container.instance.ContainerValidationHelper.validateQuantity;
import static validation.container.instance.ContainerValidationHelper.validateSize;
import static validation.container.instance.ContainerValidationHelper.validateVolume;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
// import java.util.HashMap;
import java.util.List;
// import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;

// import com.mongodb.BasicDBObject;

import controllers.AbstractCRUDAPIController;
// import controllers.DocumentController;
import controllers.ListForm;
import controllers.NGLControllerHelper;
// import controllers.QueryFieldsForm;
import controllers.authorisation.Permission;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.play.migration.NGLContext;
import models.laboratory.common.description.Level;
import models.laboratory.common.instance.State;
// import models.laboratory.common.instance.TraceInformation;
import models.laboratory.container.description.ContainerSupportCategory;
import models.laboratory.container.instance.Container;
import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.processes.description.ProcessType;
import models.laboratory.processes.instance.Process;
import models.utils.InstanceConstants;
// import models.utils.InstanceHelpers;
// import models.utils.ListObject;
import models.utils.dao.DAOException;
// import play.Logger;
// import play.api.modules.spring.Spring;
import play.data.Form;
import play.i18n.Lang;
import play.libs.Json;
// import play.mvc.BodyParser;
import play.mvc.Http;
import play.mvc.Result;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import views.components.datatable.DatatableBatchResponseElement;
// import views.components.datatable.DatatableForm;
import workflows.container.ContWorkflows;

public class ContainersCRUD extends AbstractCRUDAPIController<Container> {
	
	/**
	 * Logger.
	 */
	private static final play.Logger.ALogger logger = play.Logger.of(Containers.class);
		
	private static final List<String> DEFAULT_KEYS = 
			Arrays.asList("code",   "importTypeCode","categoryCode",
					"state","valuation","traceInformation","properties",
					"comments","support","contents","volume",
					"concentration","quantity","size","projectCodes",
					"sampleCodes",  "fromTransformationTypeCodes","processTypeCodes");
	
	private static final List<String> authorizedUpdateFields = 
			Arrays.asList("valuation","state","comments","volume","quantity","size","concentration");
	
	// private final Form<QueryFieldsForm>       updateForm; // = form(QueryFieldsForm.class);
	// private final Form<Container>             containerForm; // = form(Container.class);
	// private final Form<ContainersSearchForm>  containerSearchForm; // = form(ContainersSearchForm.class);
	private final Form<ContainerBatchElement> batchElementForm; // = form(ContainerBatchElement.class);
	private final Form<State>                 stateForm; // = form(State.class);
	// private final static ContWorkflows workflows = Spring.get BeanOfType(ContWorkflows.class);
	private final ContWorkflows               workflows;
		
	@Inject
	public ContainersCRUD(NGLContext ctx, ContWorkflows workflows) {
		super(ctx,InstanceConstants.CONTAINER_COLL_NAME, Container.class, DEFAULT_KEYS);
		// updateForm          = ctx.form(QueryFieldsForm.class);
		// containerForm       = ctx.form(Container.class);
		// containerSearchForm = ctx.form(ContainersSearchForm.class);
		batchElementForm    = ctx.form(ContainerBatchElement.class);
		stateForm           = ctx.form(State.class);
		this.workflows      = workflows;
	}

	@Override
	@Permission(value={"reading"})
	public Result get(String code) {
		return super.get(code);
	}
	@Override
	@Permission(value={"reading"})
	public Result head(String code) {
		return super.head(code);
	}
	
	private Container findContainer(String code) { return getObject(code); } 
	
//	private static Container findContainer(String containerCode){
//		return  MongoDBDAO.findOne(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.is("code",containerCode));
//	}

	// TBD
	/*
	@Permission(value={"reading"})
	public Result list() throws DAOException{
		ContainersSearchForm containersSearch = filledFormQueryString(ContainersSearchForm.class);
		DBQuery.Query query = getQuery(containersSearch);
		BasicDBObject keys = getKeys(updateForm(containersSearch));
		
		if (containersSearch.reporting) {
			return nativeMongoDBQQuery(containersSearch);
		} else if (containersSearch.datatable) {
			MongoDBResult<Container> results = mongoDBFinder(containersSearch,query, keys);
			return ok(MongoStreamer.streamUDT(results)).as("application/json");
		} else if (containersSearch.count) {
			keys.put("_id", 0);//Don't need the _id field
			keys.put("code", 1);
			// MongoDBResult<Container> results = mongoDBFinder(InstanceConstants.CONTAINER_COLL_NAME, containersSearch, Container.class, query, keys);
			MongoDBResult<Container> results = mongoDBFinder(containersSearch,query, keys);
			int count = results.count();
			Map<String, Integer> m = new HashMap<String, Integer>(1);
			m.put("result", count);
			return ok(Json.toJson(m));
		} else if (containersSearch.list) {
			// MongoDBResult<Container> results = mongoDBFinder(InstanceConstants.CONTAINER_COLL_NAME, containersSearch, Container.class, query, keys);
			MongoDBResult<Container> results = mongoDBFinder(containersSearch,query, keys);
			List<Container> containers = results.toList();
			List<ListObject> los = new ArrayList<ListObject>();
			for (Container p: containers) {
				los.add(new ListObject(p.code, p.code));
			}
			return ok(Json.toJson(los));
		} else {
			MongoDBResult<Container> results = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, query, keys);
			// return ok(new MongoDBResponseChunks<Container>(results)).as("application/json");
			return ok(MongoStreamer.stream(results)).as("application/json");
		}
	}
*/
	private static void cleanProperty(Container input) {
		if (null != input.volume && null == input.volume.value) {
			input.volume = null;
		}
		if (null != input.concentration && null == input.concentration.value) {
			input.concentration = null;
		}
		if (null != input.size && null == input.size.value) {
			input.size = null;
		}
		if (null != input.quantity && null == input.quantity.value) {
			input.quantity = null;
		}
	}

	@Override
	public Container beforeUpdateValidation(ContextValidation ctx, Container past, Container future) {
		if (!past.state.code.equals(future.state.code)) {
			ctx.addError("container","You cannot change the state code. Please used the state url ! ");
			return null;
		}
		cleanProperty(future);
		return future;
	}
	
	@Override
	public Container beforePartialUpdate(ContextValidation ctx, List<String> fields, Container input) {
		if (fields.contains("valuation")) {
			input.valuation.user = getCurrentUser();
			input.valuation.date = new Date();
		}
		if (fields.contains("volume"))        validateVolume       (input.volume,        ctx);					
		if (fields.contains("quantity"))	  validateQuantity     (input.quantity,      ctx);
		if (fields.contains("size"))          validateSize         (input.size,          ctx);
		if (fields.contains("concentration")) validateConcentration(input.concentration, ctx);					
		return input;
	}
	
	/*
	@Permission(value={"writing"})
	// @BodyParser.Of(value = IGBodyParsers.Json5MB.class)
	public Result update(String code) {
		logger.debug("udpate " + code);
		Container container = findContainer(code);
		// 
		if (container == null)
			return badRequest("container with code " + code + " does not exist");
		
		Form<QueryFieldsForm> filledQueryFieldsForm = filledFormQueryString(updateForm, QueryFieldsForm.class);
		QueryFieldsForm queryFieldsForm = filledQueryFieldsForm.get();
		Form<Container> filledForm = getFilledForm(containerForm, Container.class);
		Container input = filledForm.get();

		if (queryFieldsForm.fields == null) {
			if (code.equals(input.code)) {
				//if (null != input.traceInformation) { 
				//	input.traceInformation.setTraceInformation(getCurrentUser());
				//} else {
				//	Logger.error("traceInformation is null !!");
				//} -> container implements IAccessTracking
				
				//if (!input.state.code.equals(input.state.code)) {
				//	return badRequest("You cannot change the state code. Please used the state url ! ");
				//} -> beforeUpdateValidation

				ContextValidation ctxVal = new ContextValidation(getCurrentUser(), filledForm.errors()); 	
				ctxVal.setUpdateMode();
				// input.comments = InstanceHelpers.updateComments(input.comments, ctxVal); -> Container implements Commentable
				// cleanProperty(input); -> beforeUpdateValidation
				// input.validate(ctxVal); -> Standard validation
				if (ctxVal.hasErrors())
					return badRequest(errorsAsJson(ctxVal.getErrors()));
				MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, input);
				return ok(Json.toJson(input));				
			} else {
				// This uses a non json overload and this makes the result unreadable
				// in the UI.
				return badRequest("url container code and json container code are not the same");
			}	
		} else {
			ContextValidation ctxVal = new ContextValidation(getCurrentUser(), filledForm.errors()); 	
			ctxVal.setUpdateMode();
			validateAuthorizedUpdateFields(ctxVal, queryFieldsForm.fields, authorizedUpdateFields);
			validateIfFieldsArePresentInForm(ctxVal, queryFieldsForm.fields, filledForm);
			if(ctxVal.hasErrors())
				// return badRequest(filledForm.errors-AsJson());
				return badRequest(errorsAsJson(ctxVal.getErrors()));
			
			// input.comments = InstanceHelpers.updateComments(input.comments, ctxVal); -> ICommentable

			// TraceInformation ti = container.traceInformation;
			// ti.setTraceInformation(getCurrentUser());

			// Not appearing in regular update, could be valuation auto stamp
//			if (queryFieldsForm.fields.contains("valuation")) {
//				input.valuation.user = getCurrentUser();
//				input.valuation.date = new Date();
//			}
//
//			if (queryFieldsForm.fields.contains("volume"))        validateVolume(input.volume, ctxVal);					
//			if (queryFieldsForm.fields.contains("quantity"))	  validateQuantity(input.quantity, ctxVal);
//			if (queryFieldsForm.fields.contains("size"))          validateSize(input.size, ctxVal);
//			if (queryFieldsForm.fields.contains("concentration")) validateConcentration(input.concentration, ctxVal);
//			-> beforePartialUpdate 					

			if (ctxVal.hasErrors())
				return badRequest(errorsAsJson(ctxVal.getErrors()));
			MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, Container.class, 
					DBQuery.and(DBQuery.is("code", code)), getBuilder(input, queryFieldsForm.fields, Container.class).set("traceInformation", ti));
			return ok(Json.toJson(findContainer(code)));
		}		
		
	}
	*/
	
	
	// TODO: fix return value
	@Permission(value={"writing"})
	public Result updateState(String code) {
		Container container = findContainer(code);
		if (container == null)
			return badRequest("Container with code " + code + " does not exist");
		Form<State> filledForm =  getFilledForm(stateForm, State.class);
		State state = filledForm.get();
		state.date = new Date();
		state.user = getCurrentUser();
//		ContextValidation ctxVal = new ContextValidation(getCurrentUser(), filledForm.errors());
		ContextValidation ctxVal = new ContextValidation(getCurrentUser(), filledForm);
		ctxVal.putObject(CommonValidationHelper.FIELD_STATE_CONTAINER_CONTEXT, "controllers");
		ctxVal.putObject(CommonValidationHelper.FIELD_UPDATE_CONTAINER_SUPPORT_STATE, Boolean.TRUE);		
		workflows.setState(ctxVal, container, state);
		if (!ctxVal.hasErrors()) {
			return ok(Json.toJson(findContainer(code)));
		} else {
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		}
	}

	@Permission(value={"writing"})
	public Result updateStateBatch() {
		List<Form<ContainerBatchElement>> filledForms =  getFilledFormList(batchElementForm, ContainerBatchElement.class);
		final String user = getCurrentUser();
		final Lang lang = Http.Context.Implicit.lang();
		List<DatatableBatchResponseElement> response = filledForms.parallelStream()
		.map(filledForm -> {
			ContainerBatchElement element = filledForm.get();
			Container container = findContainer(element.data.code);
			if (container != null) {
				State state = element.data.state;
				state.date = new Date();
				state.user = user;
//				ContextValidation ctxVal = new ContextValidation(user, filledForm.errors());
				ContextValidation ctxVal = new ContextValidation(user, filledForm);
				ctxVal.putObject(CommonValidationHelper.FIELD_STATE_CONTAINER_CONTEXT, "controllers");
				ctxVal.putObject(CommonValidationHelper.FIELD_UPDATE_CONTAINER_SUPPORT_STATE, Boolean.TRUE);
				workflows.setState(ctxVal, container, state);
				if (!ctxVal.hasErrors()) {
					return new DatatableBatchResponseElement(OK,  findContainer(container.code), element.index);
				} else {
					return new DatatableBatchResponseElement(BAD_REQUEST, filledForm.errorsAsJson(lang), element.index);
				}
			} else {
				return new DatatableBatchResponseElement(BAD_REQUEST, element.index);
			}
		}).collect(Collectors.toList());
		return ok(Json.toJson(response));
	}

	/*
	 * Construct the container query
	 * @param containersSearch
	 * @return
	 * @throws DAOException 
	 */
	public DBQuery.Query getQuery(ContainersSearchForm containersSearch) throws DAOException {		
		List<DBQuery.Query> queryElts = new ArrayList<>();
		Query query = DBQuery.empty();

		
		if(containersSearch.processProperties.size() > 0){	
			List<String> processCodes = new ArrayList<>();
			List<DBQuery.Query> listProcessQuery = NGLControllerHelper.generateQueriesForProperties(containersSearch.processProperties, Level.CODE.Process, "properties");
			Query processQuery = DBQuery.and(listProcessQuery.toArray(new DBQuery.Query[queryElts.size()]));

			List<Process> processes = MongoDBDAO.find(InstanceConstants.PROCESS_COLL_NAME, Process.class, processQuery).toList();
			for(Process p : processes){
				processCodes.add(p.code);
			}
			queryElts.add(DBQuery.in("processCodes", processCodes));
		}
		
		if (CollectionUtils.isNotEmpty(containersSearch.sampleTypeCodes)) { //all
			queryElts.add(DBQuery.in("contents.sampleTypeCode", containersSearch.sampleTypeCodes));
		}

		if(StringUtils.isNotBlank(containersSearch.ncbiScientificNameRegex)){
			queryElts.add(DBQuery.regex("contents.ncbiScientificName", Pattern.compile(containersSearch.ncbiScientificNameRegex)));
		}
		
		if(CollectionUtils.isNotEmpty(containersSearch.projectCodes)){
			queryElts.add(DBQuery.in("projectCodes", containersSearch.projectCodes));
		}else if(StringUtils.isNotBlank(containersSearch.projectCode)){
			queryElts.add(DBQuery.in("projectCodes", containersSearch.projectCode));
		}

		if(CollectionUtils.isNotEmpty(containersSearch.codes)){
			queryElts.add(DBQuery.in("code", containersSearch.codes));
		}else if(StringUtils.isNotBlank(containersSearch.code)){
			queryElts.add(DBQuery.is("code", containersSearch.code));
		}else if(StringUtils.isNotBlank(containersSearch.codeRegex)){
			queryElts.add(DBQuery.regex("code", Pattern.compile(containersSearch.codeRegex)));
		}
		
		if(StringUtils.isNotBlank(containersSearch.treeOfLifePathRegex)){
			queryElts.add(DBQuery.regex("treeOfLife.paths", Pattern.compile(containersSearch.treeOfLifePathRegex)));
		}
		
		if(CollectionUtils.isNotEmpty(containersSearch.stateCodes)){
			queryElts.add(DBQuery.in("state.code", containersSearch.stateCodes));
		}else if(StringUtils.isNotBlank(containersSearch.stateCode)){
			queryElts.add(DBQuery.is("state.code", containersSearch.stateCode));
		}

		if(StringUtils.isNotBlank(containersSearch.categoryCode)){
			queryElts.add(DBQuery.is("categoryCode", containersSearch.categoryCode));
		}

		if(CollectionUtils.isNotEmpty(containersSearch.sampleCodes)){
			queryElts.add(DBQuery.in("sampleCodes", containersSearch.sampleCodes));
		}else if(StringUtils.isNotBlank(containersSearch.sampleCode)){
			queryElts.add(DBQuery.in("sampleCodes", containersSearch.sampleCode));
		}

		
		if(CollectionUtils.isNotEmpty(containersSearch.supportCodes)){
			queryElts.add(DBQuery.in("support.code", containersSearch.supportCodes));
		}else if(StringUtils.isNotBlank(containersSearch.supportCode)){
			queryElts.add(DBQuery.is("support.code", containersSearch.supportCode));
		}else if(StringUtils.isNotBlank(containersSearch.supportCodeRegex)){
			queryElts.add(DBQuery.regex("support.code", Pattern.compile(containersSearch.supportCodeRegex)));
		}

		if(StringUtils.isNotBlank(containersSearch.supportStorageCodeRegex)){
			queryElts.add(DBQuery.regex("support.storageCode", Pattern.compile(containersSearch.supportStorageCodeRegex)));
		}
		
		if(CollectionUtils.isNotEmpty(containersSearch.fromPurificationTypeCodes)){
			if(containersSearch.fromPurificationTypeCodes.contains("none")){
				queryElts.add(DBQuery.or(DBQuery.size("fromPurificationTypeCode", 0),DBQuery.notExists("fromPurificationTypeCode")
						,DBQuery.in("fromPurificationTypeCode", containersSearch.fromPurificationTypeCodes)));
			}else{
				queryElts.add(DBQuery.in("fromPurificationTypeCode", containersSearch.fromPurificationTypeCodes));

			}
		}
		
		if(CollectionUtils.isNotEmpty(containersSearch.fromTransfertTypeCodes)){ 
				if(containersSearch.fromTransfertTypeCodes.contains("none")){
					queryElts.add(DBQuery.or(DBQuery.size("fromTransfertTypeCode", 0),DBQuery.notExists("fromTransfertTypeCode")
							,DBQuery.in("fromTransfertTypeCode", containersSearch.fromTransfertTypeCodes)));
				}else{
					queryElts.add(DBQuery.in("fromTransfertTypeCode", containersSearch.fromTransfertTypeCodes));
				}			
		}
				
		if(CollectionUtils.isNotEmpty(containersSearch.containerSupportCategories)){
			queryElts.add(DBQuery.in("support.categoryCode", containersSearch.containerSupportCategories));
		}else if(StringUtils.isNotBlank(containersSearch.containerSupportCategory)){
			queryElts.add(DBQuery.is("support.categoryCode", containersSearch.containerSupportCategory));
		}else if(StringUtils.isNotBlank(containersSearch.nextExperimentTypeCode)){
			List<ContainerSupportCategory> containerSupportCategories = ContainerSupportCategory.find.findInputByExperimentTypeCode(containersSearch.nextExperimentTypeCode);
			List<String> cs = new ArrayList<>();
			for(ContainerSupportCategory c:containerSupportCategories){
				cs.add(c.code);
			}
			if(cs.size() > 0){
				queryElts.add(DBQuery.in("support.categoryCode", cs));
			}
		}



		List<String> listePrevious = new ArrayList<>();
		//used in processes creation
		if(StringUtils.isNotBlank(containersSearch.nextProcessTypeCode)){					
					
			ProcessType processType = ProcessType.find.findByCode(containersSearch.nextProcessTypeCode);
			if(processType != null){
				//List<ExperimentType> experimentTypes = ExperimentType.find.findPreviousExperimentTypeForAnExperimentTypeCode(processType.firstExperimentType.code);
				
				List<ExperimentType> experimentTypes = ExperimentType.find.findPreviousExperimentTypeForAnExperimentTypeCodeAndProcessTypeCode(processType.firstExperimentType.code, processType.code);
				
				boolean onlyEx = true;
				for(ExperimentType e:experimentTypes){
					//Logger.info(e.code);
					if(!e.code.startsWith("ex")){
						onlyEx = false;
					}
					listePrevious.add(e.code);
				}			
				
				if(CollectionUtils.isNotEmpty(containersSearch.fromTransformationTypeCodes) && containersSearch.fromTransformationTypeCodes.contains("none")){
						queryElts.add(DBQuery.or(DBQuery.size("fromTransformationTypeCodes", 0),DBQuery.notExists("fromTransformationTypeCodes")
						,DBQuery.in("fromTransformationTypeCodes", containersSearch.fromTransformationTypeCodes)));					
				}else if(CollectionUtils.isNotEmpty(containersSearch.fromTransformationTypeCodes)){
					queryElts.add(DBQuery.in("fromTransformationTypeCodes", containersSearch.fromTransformationTypeCodes));
				}else if(!onlyEx){
					queryElts.add(DBQuery.or(DBQuery.in("fromTransformationTypeCodes", listePrevious), DBQuery.size("fromTransformationTypeCodes", 0),DBQuery.notExists("fromTransformationTypeCodes")));
				}else{
					queryElts.add(DBQuery.or(DBQuery.size("fromTransformationTypeCodes", 0),DBQuery.notExists("fromTransformationTypeCodes")));
				}
				
			
			}else{
				logger.error("NGL-SQ bad nextProcessTypeCode: "+containersSearch.nextProcessTypeCode);
				return null;
			}
		//used in experiment creation	
		}else if(StringUtils.isNotBlank(containersSearch.nextExperimentTypeCode)){
			
			//TODO GA Prendre la précédente dans chacun des processus et pas celle de l'expérience
			/*
			List<ExperimentType> previous = ExperimentType.find.findPreviousExperimentTypeForAnExperimentTypeCode(containersSearch.nextExperimentTypeCode);
			if(CollectionUtils.isNotEmpty(previous)){
				for(ExperimentType e:previous){
					listePrevious.add(e.code);
				}

				if(CollectionUtils.isNotEmpty(listePrevious)){
					queryElts.add(DBQuery.or(DBQuery.in("fromTransformationTypeCodes", listePrevious)));
				}
			
			//NextExperimentTypeCode appartient au processType des containers
				List<String> listProcessType=new ArrayList<String>();
				List<ProcessType> processTypes=ProcessType.find.findByExperimentTypeCode(containersSearch.nextExperimentTypeCode);
				if(CollectionUtils.isNotEmpty(processTypes)){
					for(ProcessType processType:processTypes){
						listProcessType.add(processType.code);
						
						//TODO GA NEW CODE TO ASSOCIATE expType and processType 
						List<ExperimentType> previousExpType = ExperimentType.find.findPreviousExperimentTypeForAnExperimentTypeCodeAndProcessTypeCode(containersSearch.nextExperimentTypeCode,processType.code);
						Logger.debug("NB Previous exp : "+previousExpType.size());
					}
				}
				//TODO Erreur quand pas de processus pour un type d'expérience
				
				if(CollectionUtils.isNotEmpty(listProcessType)){
					queryElts.add(DBQuery.in("processTypeCodes", listProcessType));
				}
				
				
				
			}else{
				//throw new RuntimeException("nextExperimentTypeCode = "+ containersSearch.nextExperimentTypeCode +" does not exist!");
			}
			queryElts.add(DBQuery.nor(DBQuery.notExists("processCodes"),DBQuery.size("processCodes", 0)));
			*/
			
			List<DBQuery.Query> subQueryElts = new ArrayList<>();
			List<ProcessType> processTypes=ProcessType.find.findByExperimentTypeCode(containersSearch.nextExperimentTypeCode);
			if(CollectionUtils.isNotEmpty(processTypes)){
				for(ProcessType processType:processTypes){
					List<ExperimentType> previousExpType = ExperimentType.find.findPreviousExperimentTypeForAnExperimentTypeCodeAndProcessTypeCode(containersSearch.nextExperimentTypeCode,processType.code);
					//Logger.debug("NB Previous exp : "+previousExpType.size());
					Set<String> previousExpTypeCodes = previousExpType.stream().map(et -> et.code).collect(Collectors.toSet());
					
					if(CollectionUtils.isNotEmpty(containersSearch.fromTransformationTypeCodes)){
						previousExpTypeCodes.retainAll(containersSearch.fromTransformationTypeCodes);
					}
					
					if(CollectionUtils.isNotEmpty(previousExpTypeCodes)){
						subQueryElts.add(DBQuery.in("processTypeCodes", processType.code).in("fromTransformationTypeCodes", previousExpTypeCodes));
					}else{
						subQueryElts.add(DBQuery.in("processTypeCodes", "-1")); //force to return zero result;
					}
					
					
				}
				if(subQueryElts.size() > 0){
					queryElts.add(DBQuery.or(subQueryElts.toArray(new DBQuery.Query[0])));
				}
				
			}else{
				//if not processType we not return any container
				queryElts.add(DBQuery.notExists("code"));
			}
			
			
		} else if(CollectionUtils.isNotEmpty(containersSearch.fromTransformationTypeCodes)){
			if(containersSearch.fromTransformationTypeCodes.contains("none")){
					queryElts.add(DBQuery.or(DBQuery.size("fromTransformationTypeCodes", 0),DBQuery.notExists("fromTransformationTypeCodes")
					, DBQuery.regex("fromTransformationTypeCodes", Pattern.compile("^ext-to-.+$")),DBQuery.in("fromTransformationTypeCodes", containersSearch.fromTransformationTypeCodes)));
			} else {
				queryElts.add(DBQuery.in("fromTransformationTypeCodes", containersSearch.fromTransformationTypeCodes));
			}
		}
		
		
		if(null != containersSearch.fromDate){
			queryElts.add(DBQuery.greaterThanEquals("traceInformation.creationDate", containersSearch.fromDate));
		}

		if(null != containersSearch.toDate){
			queryElts.add(DBQuery.lessThan("traceInformation.creationDate", (DateUtils.addDays(containersSearch.toDate, 1))));
		}

		if(CollectionUtils.isNotEmpty(containersSearch.valuations)){
			queryElts.add(DBQuery.or(DBQuery.in("valuation.valid", containersSearch.valuations)));
		}

		if(StringUtils.isNotBlank(containersSearch.column)){
			queryElts.add(DBQuery.is("support.column", containersSearch.column));
		}

		if(StringUtils.isNotBlank(containersSearch.line)){
			queryElts.add(DBQuery.is("support.line", containersSearch.line));
		}

		if(StringUtils.isNotBlank(containersSearch.processTypeCode)){   
			queryElts.add(DBQuery.in("processTypeCodes", containersSearch.processTypeCode));
		}

		
		if(CollectionUtils.isNotEmpty(containersSearch.createUsers)){
			queryElts.add(DBQuery.in("traceInformation.createUser", containersSearch.createUsers));
		}else if(StringUtils.isNotBlank(containersSearch.createUser)){
			queryElts.add(DBQuery.is("traceInformation.createUser", containersSearch.createUser));
		}
		
		
		if (CollectionUtils.isNotEmpty(containersSearch.stateResolutionCodes)) { //all
			queryElts.add(DBQuery.in("state.resolutionCodes", containersSearch.stateResolutionCodes));
		}
		
		if(StringUtils.isNotBlank(containersSearch.commentRegex)){
			queryElts.add(DBQuery.elemMatch("comments", DBQuery.regex("comment", Pattern.compile(containersSearch.commentRegex))));
		}
		
		queryElts.addAll(NGLControllerHelper.generateQueriesForProperties(containersSearch.contentsProperties,Level.CODE.Content, "contents.properties"));
		queryElts.addAll(NGLControllerHelper.generateQueriesForProperties(containersSearch.properties,Level.CODE.Container, "properties"));

		queryElts.addAll(NGLControllerHelper.generateExistsQueriesForFields(containersSearch.existingFields));
		queryElts.addAll(NGLControllerHelper.generateQueriesForFields(containersSearch.queryFields));
		
		
		if(queryElts.size() > 0){
			query = DBQuery.and(queryElts.toArray(new DBQuery.Query[queryElts.size()]));
		}		
		
		return query;
	}
	
	/*private static DatatableForm updateForm(ContainersSearchForm form) {
		if(form.includes.contains("default")){
			form.includes.remove("default");
			form.includes.addAll(defaultKeys);
		}
		return form;
	}*/

	@Override
	public List<String> getAuthorizedUpdateFields() {
		return authorizedUpdateFields;
	}

	@Override
	public Query getQuery(ContextValidation ctx, ListForm f) {
		if (f instanceof ContainersSearchForm) {
			ContainersSearchForm c = (ContainersSearchForm)f;
			try {
				return getQuery(c);
			} catch (DAOException e) {
				logger.error("query construction failed",e);
				ctx.addError("query","construction failed %s",e.getMessage());
			}
		}
		return null;
	}
	
}
