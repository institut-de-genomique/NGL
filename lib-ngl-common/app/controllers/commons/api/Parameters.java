package controllers.commons.api;

import javax.inject.Inject;

import org.mongojack.DBQuery;

import controllers.NGLAPIController;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.parameters.MapParametersAPI;
import fr.cea.ig.ngl.dao.parameters.ParametersAPI;
import fr.cea.ig.ngl.dao.parameters.ParametersDAO;
import models.laboratory.parameter.Parameter;
import models.laboratory.parameter.map.MapParameter;
import models.laboratory.parameter.map.MapParameterEntry;
import models.utils.InstanceConstants;
import models.utils.dao.DAOException;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;

/* FDS/EJ 27/11/2018 Il faudra creer une ressource "Index/Tag" distincte des parametres avec sa propre collection Mongo...
 * au CNS il y des type de parametres qui n'ont rien a voir avec les tag/index de sequencage...
	 => "BBP11", "map-parameter", "context-description"
   NGL-836: Pour l'instant completer Parameter... 
            oui mais filtrer les categoryCode ci-dessus qui posent probleme car ne mappent pas le modele...package models.laboratory.parameter.index;
   */

public class Parameters extends NGLAPIController<ParametersAPI, ParametersDAO, Parameter> {

	private final Form<MapParameterEntry> mapParameterEntryform;
	private final MapParametersAPI mapParametersAPI;

	@Inject
	public Parameters(NGLApplication app, ParametersAPI api, MapParametersAPI mapParametersAPI) {
		super(app, api, ParametersSearchForm.class);
		mapParameterEntryform = app.formFactory().form(MapParameterEntry.class);
		this.mapParametersAPI = mapParametersAPI;
	}

	@Authenticated
	@Authorized.Admin
	public Result save() {
		return super.save();
	}

	private Parameter insertMappingProject(String code) throws APIValidationException, APIException {
		Form<MapParameterEntry> filledForm = getFilledForm(mapParameterEntryform, MapParameterEntry.class);
		MapParameterEntry mapParameterEntry = filledForm.get();
		String comment = mapParameterEntry.comment;
		MapParameter apiResult = mapParametersAPI.insert(mapParameterEntry, code, getCurrentUser(), comment);
		return apiResult;
	}

	@Override
	public Parameter updateImpl(String code) throws Exception, APIException, APIValidationException {
		return api().update(null, null);
	}

	private ParametersSaveForm getParametersSaveForm() {
		Form<ParametersSaveForm> filledQueryFieldsForm = filledFormQueryString(ParametersSaveForm.class);
		ParametersSaveForm parametersSaveForm = filledQueryFieldsForm.get();
		return parametersSaveForm;
	}

	@Override
	public Parameter saveImpl() throws APIValidationException, APIException {
		ParametersSaveForm parametersSaveForm = getParametersSaveForm();
		String typeCode = parametersSaveForm.typeCode;
		String code = parametersSaveForm.code;
		if (parametersSaveForm.typeCode == null)
			throw new APIException("typecode is null");
		switch (typeCode) {
			case "map-parameter":
				return insertMappingProject(code);
			default:
				return insertGenericParameter();
		}
	}

	private Parameter insertGenericParameter() throws APIValidationException, APIException {
		return api().create(null, getCurrentUser());
	}

	@Authenticated
	@Authorized.Read
	public Result get(String typeCode, String code) throws DAOException {
		Parameter index = MongoDBDAO.findOne(InstanceConstants.PARAMETER_COLL_NAME, Parameter.class,
				DBQuery.is("typeCode", typeCode).is("code", code));
		if (index != null)
			return ok(Json.toJson(index));
		return badRequest("erreur de type ou de code");
	}

}
