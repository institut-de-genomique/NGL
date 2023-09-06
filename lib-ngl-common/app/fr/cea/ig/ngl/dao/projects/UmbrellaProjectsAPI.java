package fr.cea.ig.ngl.dao.projects;

import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.api.GenericAPI;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.project.instance.UmbrellaProject;
import validation.ContextValidation;

@Singleton
public class UmbrellaProjectsAPI extends GenericAPI<UmbrellaProjectsDAO, UmbrellaProject> {

	private static final play.Logger.ALogger logger = play.Logger.of(UmbrellaProjectsAPI.class);

	private static final List<String> authorizedUpdateFields = Arrays.asList(); // TODO

	private final static List<String> defaultKeys = Arrays.asList(); // TODO

	@Inject
	public UmbrellaProjectsAPI(UmbrellaProjectsDAO dao) {
		super(dao);
	}

	@Override
	protected List<String> authorizedUpdateFields() {
		return authorizedUpdateFields;
	}

	@Override
	protected List<String> defaultKeys() {
		return defaultKeys;
	}

	@Override
	public UmbrellaProject create(UmbrellaProject umbrellaProject, String currentUser) throws APIException, APIValidationException {
		if (umbrellaProject._id != null)
			throw new APIException(CREATE_SEMANTIC_ERROR_MSG);

		ContextValidation ctxVal = ContextValidation.createCreationContext(currentUser);
		umbrellaProject.traceInformation = new TraceInformation();
		umbrellaProject.traceInformation.creationStamp(ctxVal, currentUser);

		umbrellaProject.validate(ctxVal);

		if (ctxVal.hasErrors())
			throw new APIValidationException(INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());

		return dao.saveObject(umbrellaProject);
	}

	@Override
	public UmbrellaProject update(UmbrellaProject input, String currentUser, List<String> fields)
			throws APIException, APIValidationException {
		throw new RuntimeException("not implemented");
	}

	@Override
	public UmbrellaProject update(UmbrellaProject input, String currentUser) throws APIException, APIValidationException {
		UmbrellaProject umbrellaProject = this.get(input.code);

		if (umbrellaProject == null) {
			throw new APIException("Project with code " + input.code + " not exist");
		}

		ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser);

		if (input.traceInformation != null) {
			input.traceInformation.modificationStamp(ctxVal, currentUser);
		} else {
			logger.error("traceInformation is null !!");
		}	

		input.validate(ctxVal);

		if (ctxVal.hasErrors()) {
			throw new APIValidationException("Invalid Project object", ctxVal.getErrors());
		}

		dao.updateObject(input);

		return get(input.code);
	}
}
