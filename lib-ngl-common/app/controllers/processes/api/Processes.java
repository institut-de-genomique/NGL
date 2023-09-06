package controllers.processes.api;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import controllers.NGLAPIController;
import controllers.QueryFieldsForm;
import controllers.StateController;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.lfw.LFWApplication;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APISemanticException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.processes.ProcessesAPI;
import fr.cea.ig.ngl.dao.processes.ProcessesDAO;
import fr.cea.ig.play.IGBodyParsers;
import fr.cea.ig.play.IGGlobals;
import models.laboratory.common.instance.State;
import models.laboratory.processes.instance.Process;
import play.data.Form;
import play.i18n.Lang;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Result;
import views.components.datatable.DatatableBatchResponseElement;

public class Processes extends NGLAPIController<ProcessesAPI, ProcessesDAO, Process> implements StateController/*, BatchController*/ {

	/**
	 * Constant for the process creation from sample
	 * (see {@link ProcessesAPI#createProcessesFromSample(Process, String)}). 
	 */
	public static final String FROM_SAMPLE = "from-sample";

	/**
	 * Constant for the process creation from container
	 * (see {@link ProcessesAPI#createProcessesFromContainer(Process, String)}). .
	 */
	public static final String FROM_CONTAINER = "from-container";

	private final Form<Process> form;
	private final Form<ProcessesBatchElement> batchElementForm;

	@Inject
	public Processes(NGLApplication app, ProcessesAPI api) {
		super(app, api, ProcessesSearchForm.class);
		form = app.formFactory().form(Process.class);
		batchElementForm = app.formFactory().form(ProcessesBatchElement.class);
	}

	@Override
	public Process updateStateImpl(String code, State state, String currentUser) throws APIException {
		return api().updateState(code, state, currentUser);
	}

	@Authenticated
	@Authorized.Write
	@BodyParser.Of(value = IGBodyParsers.Json10MB.class)
	public Result save(String from) {
		return globalExceptionHandler(() -> {
			try {
				Process input = getFilledForm(form, Process.class).get();
				List<Process> processes = null;
				switch (from) {
				case FROM_CONTAINER : processes = api().createProcessesFromContainer(input, getCurrentUser()); break;
				case FROM_SAMPLE    : processes = api().createProcessesFromSample   (input, getCurrentUser()); break;
				default             : return badRequestAsJson("bad mode " + from);
				}
				//                List<Process> processes = api().createProcesses(input, getCurrentUser(), from);
				return okAsJson(processes);
			} catch (APIValidationException e) {
				return badRequestLoggingForValidationException(e);
			} catch (APISemanticException e) {
				if (getLogger().isDebugEnabled()) getLogger().warn(e.getMessage(), e);
				return badRequestAsJson("use PUT method to update");
			} 
		});
	}

	@Override
	public Process saveImpl() throws APIException {
		Process input = getFilledForm(form, Process.class).get();
		return api().create(input, getCurrentUser());
	}
	
	private boolean tryToUpdateState(Process inDbProcess, Process updatedProcess) {
		return !inDbProcess.state.code.equals(updatedProcess.state.code);
	}

	@Override
	public Process updateImpl(String code) throws Exception, APIException, APIValidationException {
		Process input = getFilledForm(form, Process.class).get();
		QueryFieldsForm queryFieldsForm = filledFormQueryString(updateForm, QueryFieldsForm.class).get();
		if(code.equals(input.code)) { 
			Process procInDB = api().get(code);
			if(queryFieldsForm.fields == null) {
				if (tryToUpdateState(procInDB, input)) throw new Exception("You can not change the state code. Please use the state url ! ");
				return api().update(input, getCurrentUser());
			} else {
				List<String> fields = queryFieldsForm.fields;
				if (fields.contains("state.code") && tryToUpdateState(procInDB, input)) throw new Exception("You can not change the state code. Please use the state url ! ");
				return api().update(input, getCurrentUser(), fields);
			}
			
		} else {
			throw new Exception("Process codes are not the same");
		}
	}


	public Result saveBatch(String from) {
		return globalExceptionHandler(() -> {
			List<Form<ProcessesBatchElement>> filledForms =  getFilledFormList(batchElementForm, ProcessesBatchElement.class);
			final String user = getCurrentUser();
			final Lang lang = currentLang();    
			List<DatatableBatchResponseElement> response = new ArrayList<DatatableBatchResponseElement>();
			IGGlobals.instanceOf(LFWApplication.class).parallelRun(() -> {
				// not available into possible threads from parallelStream 
				List<DatatableBatchResponseElement> responseP = filledForms.parallelStream()   // WARNING HTTP context not available into threads
						.map(filledForm -> {
							ProcessesBatchElement element = filledForm.get();
							Process process = element.data;
							try {
								List<Process> processes = null;
								switch (from) {
								case FROM_CONTAINER : processes = api().createProcessesFromContainer(process, user); break;
								case FROM_SAMPLE    : processes = api().createProcessesFromSample   (process, user); break;
								default             : return new DatatableBatchResponseElement(play.mvc.Http.Status.BAD_REQUEST, "bad mode "+ from, element.index);
								}
								//                            List<Process> processes = api().createProcesses(process, user, from);
								return new DatatableBatchResponseElement(OK,  processes, element.index);
							} catch (APIValidationException e) {
								return new DatatableBatchResponseElement(play.mvc.Http.Status.BAD_REQUEST, errorsAsJson(lang, e.getErrors()), element.index);
							} catch (APISemanticException e) {
								if (getLogger().isDebugEnabled()) getLogger().warn(e.getMessage(), e);
								return new DatatableBatchResponseElement(play.mvc.Http.Status.BAD_REQUEST, "use PUT method to update", element.index);
							}
						}).collect(Collectors.toList());
				response.addAll(responseP);
			});

			return ok(Json.toJson(response));
		});
	}



	public Result updateBatch() {
		return globalExceptionHandler(() -> {
			List<Form<ProcessesBatchElement>> filledForms =  getFilledFormList(batchElementForm, ProcessesBatchElement.class);
			final String user = getCurrentUser();
			final Lang lang = currentLang();
			List<DatatableBatchResponseElement> response = filledForms.parallelStream()
					.map(filledForm -> {
						ProcessesBatchElement element = filledForm.get();
						Process input = element.data;
						try {
							Process proc = api().update(input, user);
							return new DatatableBatchResponseElement(OK,  proc, element.index);
						} catch (APIValidationException e) {
							return new DatatableBatchResponseElement(play.mvc.Http.Status.BAD_REQUEST, errorsAsJson(lang, e.getErrors()), element.index);
						} catch (APIException e) {
							if (getLogger().isDebugEnabled()) getLogger().warn(e.getMessage(), e);
							return new DatatableBatchResponseElement(play.mvc.Http.Status.BAD_REQUEST, element.index);
						}
					}).collect(Collectors.toList());
			return ok(Json.toJson(response));
		});
	}


	@Override
	@Authenticated
	@Authorized.Write
	public Result delete(String code) {
		return globalExceptionHandler(() -> {
			try {
				api().delete(code, getCurrentUser());
				return ok();
			} catch (APIValidationException e) {
				return badRequestLoggingForValidationException(e);
			} catch (APIException e) {
				if (getLogger().isDebugEnabled()) getLogger().warn(e.getMessage(), e);
				return badRequestAsJson(e.getMessage());
			}
		});
	}
}

