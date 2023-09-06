package fr.cea.ig.ngl.dao.parameters;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang.NotImplementedException;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.api.GenericAPI;
import fr.cea.ig.ngl.dao.descriptionHistories.DescriptionHistoriesAPI;
import models.laboratory.descriptionHistory.instance.DescriptionHistory;
import models.laboratory.parameter.Parameter;
import models.laboratory.parameter.map.MapParameter;
import models.laboratory.parameter.map.MapParameterEntry;
import play.libs.Json;
import validation.ContextValidation;

public class MapParametersAPI extends GenericAPI<ParametersDAO, Parameter> {

	private DescriptionHistoriesAPI descHAPI;
	private final static play.Logger.ALogger logger = play.Logger.of(MapParametersAPI.class);
	
	private final static  List<String>  authorizedUpdateFields = Collections.unmodifiableList(Arrays.asList());

	private final static List<String> defaultKeys = Collections.unmodifiableList(Arrays.asList());

	@Inject
	public MapParametersAPI(ParametersDAO dao, DescriptionHistoriesAPI descHAPI) {
		super(dao);
		this.descHAPI = descHAPI;

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
	public Parameter create(Parameter input, String currentUser) throws APIValidationException, APIException {
		throw new NotImplementedException();
	}

	@Override
	public Parameter update(Parameter input, String currentUser) throws APIException, APIValidationException {
		throw new NotImplementedException();
	}

	@Override
	public Parameter update(Parameter input, String currentUser, List<String> fields)
			throws APIException, APIValidationException {
		throw new NotImplementedException();
	}

	private void validationPreInsertion(String code, MapParameterEntry mpe, ContextValidation ctxVal)
			throws APIValidationException {
		mpe.validate(ctxVal);
		if (ctxVal.hasErrors())
			throw new APIValidationException(INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
	}

	private void validationPostInstertion(String code, String userName, MapParameter mapParameter,
			ContextValidation ctxVal) throws APIValidationException {
		mapParameter.setTraceModificationStamp(ctxVal, userName);
		mapParameter.validate(ctxVal);
		if (ctxVal.hasErrors())
			throw new APIValidationException(INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());

	}

	private void historise(MapParameterEntry entry, String code, String currentUser, String comment)
			throws APIValidationException, APIException {
		DescriptionHistory descHistory = new DescriptionHistory();
		descHistory.code = code;
		descHistory.date = new Date();
		descHistory.user = currentUser;
		descHistory.objMAjJson = asObjMAjJson(entry);
		descHistory.comment = comment;
		this.descHAPI.create(descHistory, currentUser);
	}

	public MapParameter insert(MapParameterEntry entry, String code, String currentUser, String comment)
			throws APIException, APIValidationException {
		if (!this.isObjectExist(code))
			throw new APIException("MapParameter with code " + code + " doesn't exist");
		ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser);
		MapParameter mapParameter = getMapParameter(code);
		validationPreInsertion(currentUser, entry, ctxVal);
		validateNotOverwrittingEntry(entry, mapParameter, ctxVal);
		injectInto(entry, mapParameter);
		validationPostInstertion(code, currentUser, mapParameter, ctxVal);
		dao.updateObject(mapParameter);
		historise(entry, code, currentUser, comment);
		return getMapParameter(mapParameter.code);
	}

	private void injectInto(MapParameterEntry mpe, MapParameter mapParameter) {
		mapParameter.map.put(mpe.entry.parent, mpe.entry.child);
	}

	private String asObjMAjJson(MapParameterEntry mpe) {
		return Json.stringify(Json.toJson(mpe.entry));
	}

	private void validateNotOverwrittingEntry(MapParameterEntry mpe, MapParameter mapParameter,
			ContextValidation ctxVal) throws APIException {
		existingParentDiffChild(mpe, mapParameter, ctxVal);
		existingParentWithSameChild(mpe, mapParameter, ctxVal);
	}

	private void existingParentDiffChild(MapParameterEntry mpe, MapParameter mapParameter, ContextValidation ctxVal)
			throws APIException {
		String child = mpe.entry.child;
		String parent = mpe.entry.parent;
		if (mapParameter.map.containsKey(parent) && !mapParameter.map.get(parent).equals(child)) {
			ctxVal.addError("parent",
					"Le projet Parent '" + String.valueOf(parent) + "' est déjà lié au projet Enfant '" + String.valueOf(mapParameter.map.get(parent)) + "'");
		}

	}

	private void existingParentWithSameChild(MapParameterEntry mpe, MapParameter mapParameter, ContextValidation ctxVal)
			throws APIException {
		String child = mpe.entry.child;
		String parent = mpe.entry.parent;
		if (mapParameter.map.containsKey(parent) && mapParameter.map.get(parent).equals(child)) {
			ctxVal.addError("parent", "Ce mapping existe déjà : Parent '" + String.valueOf(parent) + "' -> Enfant '"
					+ String.valueOf(child)+ "'");
		}

	}

	private MapParameter getMapParameter(String code) throws APIException {
		try {
			return (MapParameter) get(code);
		} catch (ClassCastException cce) {
			throw new APIException("Parameter " + code + " is not a MapParameter", cce);
		}
	}
}
