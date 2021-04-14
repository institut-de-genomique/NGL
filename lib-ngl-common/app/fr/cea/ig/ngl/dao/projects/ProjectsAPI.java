package fr.cea.ig.ngl.dao.projects;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.fasterxml.jackson.databind.JsonNode;

import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.api.GenericAPI;
import models.laboratory.common.description.Level;
import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.common.instance.property.PropertyObjectListValue;
import models.laboratory.common.instance.property.PropertyObjectValue;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.project.instance.Project;
import ngl.refactoring.state.ProjectStateNames;
import play.libs.Json;
import validation.ContextValidation;
import validation.project.instance.ProjectValidationHelper;
import workflows.project.ProjectWorkflows;

@Singleton
public class ProjectsAPI extends GenericAPI<ProjectsDAO, Project> {

	private static final play.Logger.ALogger logger = play.Logger.of(ProjectsAPI.class);

	private static final List<String> authorizedUpdateFields = Arrays.asList("name", "typeCode", "categoryCode",
			"properties.archiveHistory.value", "properties.synchroProj.value", "lastSampleCode", "properties.qtreeQuota.rawdata",
			"properties.qtreeQuota.scratch", "properties.qtreeQuota.proj");

	private final static List<String> defaultKeys = Arrays.asList(); // TODO

	private final ProjectWorkflows workflows;

	@Inject
	public ProjectsAPI(ProjectsDAO dao, ProjectWorkflows workflows) {
		super(dao);
		this.workflows = workflows;
	}

	@Override
	protected List<String> authorizedUpdateFields() {
		return authorizedUpdateFields;
	}

	@Override
	protected List<String> defaultKeys() {
		return defaultKeys;
	}

	public Iterable<Project> all() throws APIException {
		return dao.all();
	}

	@Override
	public Project create(Project project, String currentUser) throws APIException, APIValidationException {
		if (project._id != null)
			throw new APIException(CREATE_SEMANTIC_ERROR_MSG);

		ContextValidation ctxVal = ContextValidation.createCreationContext(currentUser);
		project.traceInformation = new TraceInformation();
		project.traceInformation.creationStamp(ctxVal, currentUser);
		if (project.state == null)
			project.state = new State();
		project.state.code = ProjectStateNames.N;
		project.state.user = currentUser;
		project.state.date = new Date();

		project.validate(ctxVal);
		if (ctxVal.hasErrors())
			throw new APIValidationException(INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
		return dao.saveObject(project);
	}

	private PropertySingleValue getPropertySingleValue(Project input, Object field) {
		if (field.equals("name")) {
			return new PropertySingleValue(input.name);
		} else if (field.equals("typeCode")) {
			return new PropertySingleValue(input.typeCode);
		} else if (field.equals("categoryCode")) {
			return new PropertySingleValue(input.categoryCode);
		} else if (field.equals("lastSampleCode")) {
			return new PropertySingleValue(input.lastSampleCode);
		}

		return null;
	}

	/**
	 * Méthode permettant de mettre à jour un champ donné dans le projet passé en
	 * input.
	 * 
	 * @param field   Le champ à mettre à jour.
	 * @param input   Les nouvelles valeurs du champ pour le projet.
	 * @param project Le projet à mettre à jour en base.
	 * 
	 * @return Le projet en base à jour avec les données du projet en input.
	 * 
	 * @throws APIException S'il y a une incohérence entre l'URL de la requête et le
	 *                      body de la requête ou un problème lors de l'exécution de
	 *                      la méthode.
	 */
	public Project updateField(String field, Project input, Project project) throws APIException {
		Project res = null;

		JsonNode inputNode = Json.toJson(input);

		for (String subField : field.split("\\.")) {
			if (inputNode.hasNonNull(subField)) {
				inputNode = inputNode.get(subField);
			} else {
				JsonNode newNode = inputNode.get("value");

				if (newNode.hasNonNull(subField)) {
					inputNode = newNode;
				} else {
					throw new APIException("Input field : " + String.valueOf(field)
					+ " is absent in request body but is present in URL fields.");
				}
			} 
		}

		JsonNode projectNode = Json.toJson(project);
		JsonNode projectNodeInit = projectNode;

		boolean isEmptyField = false;
		String[] propArray = field.split("\\.");

		for (String subField : propArray) {
			if (projectNode.hasNonNull(subField)) {
				projectNode = projectNode.get(subField);
			} else {
				isEmptyField = true;
			}
		}
		
		if (projectNode.isArray()) {
			ArrayList<Object> lisProjectMode = Json.fromJson(projectNode,
					new ArrayList<Object>().getClass());
			lisProjectMode.addAll(Json.fromJson(inputNode, new ArrayList<Object>().getClass()));

			res = Json.fromJson(projectNodeInit, Project.class);
			res.properties.get(propArray[1]).value = lisProjectMode;
		} else {
			res = Json.fromJson(projectNodeInit, Project.class);

			if (isEmptyField) {
				List<PropertyDefinition> list = PropertyDefinition.find.get().findUnique(Level.CODE.valueOf("Project"));

				for (int i = 0; i < list.size(); i++) {					
					if (list.get(i).propertyValueType.equals("object_list")
							&& list.get(i).code.contains(propArray[1])) {
						List<Map<String, Object>> li = new ArrayList<Map<String, Object>>();

						for (int j = 0; j < inputNode.size(); j++) {
							li.add(Json.fromJson(inputNode.get(j), new HashMap<String, Object>().getClass()));
						}

						PropertyObjectListValue listValue = new PropertyObjectListValue(li);

						res.properties.put(propArray[1], listValue);
					} else if (list.get(i).propertyValueType.equals("object")
							&& list.get(i).code.contains(propArray[1])) {
						PropertyObjectValue listFromBase = (PropertyObjectValue) project.properties.get(propArray[1]);
						PropertyObjectValue listValue = new PropertyObjectValue(Json.fromJson(inputNode, new HashMap<String, Object>().getClass()));
						
						Map<String, Object> liTest = new HashMap<String, Object>();

						if (listFromBase.getValue().size() > 0) {
							for (Map.Entry<String, Object> mapFromBase : listFromBase.getValue().entrySet()) {	
								liTest.put(mapFromBase.getKey(), mapFromBase.getValue());
								
								for (Map.Entry<String, Object> newMap : listValue.getValue().entrySet()) {				
									liTest.replace(newMap.getKey(), newMap.getValue());
								}
							}
						} else {
							for (Map.Entry<String, Object> newMap : listValue.getValue().entrySet()) {			
								liTest.put(newMap.getKey(), newMap.getValue());
							}
						}
						
						PropertyObjectValue newListTest = new PropertyObjectValue(liTest);
						res.properties.put(propArray[1], newListTest);
					} else if (!list.get(i).propertyValueType.equals("object_list")
							&& list.get(i).code.contains(propArray[1])) {
						if (propArray.length > 2) { // C'est une propriété.
							res.properties.put(propArray[1],
									getPropertySingleValue(input, input.properties.get(propArray[1]).value));
						} else {
							res.properties.put(propArray[0], getPropertySingleValue(input, field));
						}
					}
				}
			} else {
				if (propArray.length > 2) { // C'est une propriété.
					PropertySingleValue singValue = new PropertySingleValue(input.properties.get(propArray[1]).value);

					res.properties.put(propArray[1], singValue);
				} else {					
					try {
						Field fieldR = Project.class.getField(propArray[0]);

						if (propArray[0].equals("name")) {
							fieldR.set(res, input.name);
						} else if (propArray[0].equals("typeCode")) {
							fieldR.set(res, input.typeCode);
						} else if (propArray[0].equals("categoryCode")) {
							fieldR.set(res, input.categoryCode);
						} else if (propArray[0].equals("lastSampleCode")) {
							fieldR.set(res, input.lastSampleCode);
						}

					} catch (NoSuchFieldException | SecurityException | IllegalArgumentException
							| IllegalAccessException e) {
						throw new APIException(e.getMessage());
					}
				}
			}
		}

		return res;
	}

	/**
	 * Méthode permettant de mettre à jour une liste de champs donnés dans un
	 * projet.
	 * 
	 * @param input       Le projet donné en paramètre (donc objet partiel, qu'avec
	 *                    les champs à mettre à jour).
	 * @param currentUser L'utilisateur qui fait l'opération. Sera utilisé pour
	 *                    mettre à jour le traceInformation.
	 * @param fields      La liste des champs à mettre à jour dans le projet.
	 * 
	 * @return Le projet mis à jour.
	 * 
	 * @throws APIException           S'il y a une incohérence entre l'URL de la
	 *                                requête et le body de la requête ou un
	 *                                problème lors de l'exécution de la méthode.
	 * @throws APIValidationException S'il y a un problème de validation de l'input.
	 */
	@Override
	public Project update(Project input, String currentUser, List<String> fields)
			throws APIException, APIValidationException {
		ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser);

		checkAuthorizedUpdateFields(ctxVal, fields);

		if (ctxVal.hasErrors()) {
			throw new APIValidationException(INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
		}

		Project project = get(input.code);

		if (project == null) {
			throw new APIException(dao.getElementClass().getSimpleName() + " with code " + input.code + " not exist");
		}

		ProjectValidationHelper.validatePropertiesRequired(ctxVal, project.typeCode, input.properties, fields);
		ProjectValidationHelper.validateArchiveProperties(ctxVal, project.code, project.typeCode, input.properties, false);

		if (ctxVal.hasErrors()) {
			throw new APIValidationException(INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
		}

		TraceInformation ti = project.traceInformation;
		ti.modificationStamp(ctxVal, currentUser);

		project.traceInformation = ti;

		for (int i = 0; i < fields.size(); i++) {
			project = this.updateField(fields.get(i), input, project);
		}

		dao.update(project);

		return get(input.code);
	}

	@Override
	public Project update(Project input, String currentUser) throws APIException, APIValidationException {
		Project project = this.get(input.code);

		if (project == null) {
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

	public Project updateState(String code, State state, String currentUser) throws APIException {
		Project projectInDb = this.get(code);
		if (projectInDb == null) {
			throw new APIException("Project with code " + code + " not exist");
		} else {
			ContextValidation ctxVal = ContextValidation.createUndefinedContext(currentUser);
			workflows.setState(ctxVal, projectInDb, state);
			if (!ctxVal.hasErrors()) {
				return get(code);
			} else {
				throw new APIValidationException("Invalid state modification", ctxVal.getErrors());
			}
		}
	}
}
