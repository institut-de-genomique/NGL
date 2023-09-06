package validation.project.instance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.common.description.Value;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.property.PropertyListValue;
import models.laboratory.project.description.ProjectCategory;
import models.laboratory.project.description.ProjectType;
import models.laboratory.project.instance.BioinformaticParameters;
import models.laboratory.project.instance.Project;
import models.laboratory.project.instance.UmbrellaProject;
import models.utils.InstanceConstants;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import validation.utils.ValidationConstants;
import validation.utils.ValidationHelper;

public class ProjectValidationHelper {

	public static final int AUTHORIZED_SIZE = 10;
	public static final String AUTHORIZED_ID_FORMAT = "^[a-zA-z0-9]+_\\d{" + AUTHORIZED_SIZE + "}$";
	public static final String ARCHIVAGE_ACTION = "ARCHIVAGE";
	public static final String DESARCHIVAGE_ACTION = "DESARCHIVAGE";
	public static final String UNKNOWN_ID = "UNKNOWN_ID";

	// -----------------------------------------------------------------------------------
	// renamed and arguments reordered

	/**
	 * Validate a required project type code and project properties.
	 * 
	 * @param typeCode          project type code
	 * @param properties        properties
	 * @param contextValidation validation context
	 * @deprecated use
	 *             {@link #validateProjectTypeCodeRequired(ContextValidation, String, Map)}
	 */
	@Deprecated
	public static void validateProjectType(String typeCode, Map<String, PropertyValue> properties,
			ContextValidation contextValidation) {
		ProjectValidationHelper.validateProjectTypeCodeRequired(contextValidation, typeCode, properties);
	}

	/**
	 * Validate a required project type code and project properties.
	 * 
	 * @param contextValidation validation context
	 * @param typeCode          project type code
	 * @param properties        properties
	 */
	public static void validateProjectTypeCodeRequired(ContextValidation contextValidation, String typeCode,
			Map<String, PropertyValue> properties) {
		ProjectType projectType = CommonValidationHelper.validateCodeForeignRequired(contextValidation,
				ProjectType.miniFind.get(), typeCode, "typeCode", true);
		/** if (projectType != null)
			ValidationHelper.validateProperties(contextValidation, properties,
					projectType.getPropertiesDefinitionProjectLevel()); */
	}

	/**
	 * validateArchiveProperties()
	 * 
	 * @param contextValidation
	 * @param code
	 * @param properties
	 * @param majTotal          Booléen qui permet de savoir dans quel mode on est :
	 *                          mise à jour totale (on met tout à jour) ou partielle
	 *                          (on ne met à jour qu'une partie du projet).
	 */
	public static void validateArchiveProperties(ContextValidation contextValidation, String code, String typeCode,
			Map<String, PropertyValue> properties, boolean majTotal) {

		// S'il n'y a pas d'archives en input on ne va pas plus loin.
		if (properties == null || properties.get("archiveHistory") == null
				|| properties.get("archiveHistory").value == null) {
			return;
		}


		ProjectType projectType = CommonValidationHelper.validateCodeForeignRequired(contextValidation,
				ProjectType.miniFind.get(), typeCode, "typeCode", true);
		List<Value> possibleActionValues = projectType.getMapPropertyDefinition().get("archiveHistory.action").possibleValues;

		//list all id archives without duplicate
		//Iterate once archiveHistory properties : validate and get informations for unique validation
		List<String> allIdArchives = new ArrayList<String>();
		List<Map<String, Object>> inputArchiveHistory = (List<Map<String, Object>>) properties.get("archiveHistory").value;
		int index=0;
		for(Map<String,Object> archive : inputArchiveHistory) {
			//get action && validate value action
			String action = (String) archive.get("action");
			if(action==null) {
				contextValidation.addError("properties.archiveHistory.action["+index+"]",ValidationConstants.ERROR_REQUIRED_MSG);
				return;
			}
			
			if(possibleActionValues.stream().filter(v->action.equals(v.value)).findAny().orElse(null)==null) {
				contextValidation.addError("properties.archiveHistory.action["+index+"]", "error.unauthorizedValue");
			}

			//get archive ids
			String projArchiveId = (String) archive.get("projArchiveId");
			String scratchArchiveId = (String) archive.get("scratchArchiveId");
			String rawArchiveId = (String) archive.get("rawArchiveId");
			//Vérifier le format des id
			//Vérifier unicité des id archives au sein du projet en input

			//Action desarchivage pas d'id
			if(DESARCHIVAGE_ACTION.equals(action) && 
					(!ValidationHelper.isEmpty(projArchiveId) ||
							!ValidationHelper.isEmpty(scratchArchiveId) || 
							!ValidationHelper.isEmpty(rawArchiveId))) {
				contextValidation.addError("properties.archiveHistory["+index+"]","error.desarchivage.idnotrequired");
			}else if(ARCHIVAGE_ACTION.equals(action)) {
				//Validate format id
				validateArchiveId(contextValidation, rawArchiveId, "rawArchiveId", allIdArchives,index);
				validateArchiveId(contextValidation, scratchArchiveId, "scratchArchiveId", allIdArchives,index);

				//Project id obligatoire en mode archivage
				if(ValidationHelper.isEmpty(projArchiveId)) {
					contextValidation.addError("properties.archiveHistory["+index+"]","error.required.archive","projArchiveId");
				}else if(!ValidationHelper.isEmpty(projArchiveId)){
					//Validate format for archivage Unknow_id_format accepted
					if(!projArchiveId.equals(UNKNOWN_ID) && !Pattern.matches(AUTHORIZED_ID_FORMAT, projArchiveId))
						contextValidation.addError("properties.archiveHistory["+index+"]","error.badformat.archive", "projArchiveId");
					else if(!projArchiveId.equals(UNKNOWN_ID)){
						//Add to list id to check unique value
						if(allIdArchives.contains(projArchiveId))
							contextValidation.addError("properties.archiveHistory["+index+"]","error.codedouble.archive","projArchiveId",projArchiveId);
						else
							allIdArchives.add(projArchiveId);
					}
				}
			}

			index++;

		}
		//Verifier unicité des id archives dans les instances en base
		//Get list id query
		if(allIdArchives.size()>0) {
			String queryAllIds = String.join("\",\"", allIdArchives);
			queryAllIds="\""+queryAllIds+"\"";

			//Cas 1 Mise à jour gobal recherche sur l'ensemble des projets sauf projet courant
			//Cas 2 Mise à jour partielle recherche sur l'ensembles des projets
			String query = "{ $or: [ { \"properties.archiveHistory.value.rawArchiveId\": { $in: [ "+queryAllIds+"] } },"
						+ "{ \"properties.archiveHistory.value.projArchiveId\": { $in: ["+queryAllIds+"] } }, "
						+ "{ \"properties.archiveHistory.value.scratchArchiveId\": { $in: [ "+queryAllIds+" ] } } ] }";



			Iterator<Project> projects = Project.find.get().findByQueryWithProjection(query,"{ \"code\": 1, \"properties.archiveHistory\": 1 }").iterator();
			while (projects.hasNext()) {
				Project p = projects.next();
				if((!p.code.equals(code) && majTotal) || !majTotal)
				contextValidation.addError("properties.archiveHistory.notUnique",
						"error.idnotuniqueProject", p.code);
			}

		}
	}

	private static void validateArchiveId(ContextValidation contextValidation, String archiveId, String keyProperty, List<String> allIdArchives, int index)
	{
		if(!ValidationHelper.isEmpty(archiveId)) {
			if(!Pattern.matches(AUTHORIZED_ID_FORMAT, archiveId))
				contextValidation.addError("properties.archiveHistory["+index+"]","error.badformat.archive",keyProperty);
			else {
				//Add to list id to check unique value
				if(allIdArchives.contains(archiveId))
					contextValidation.addError("properties.archiveHistory["+index+"]","error.codedouble.archive",keyProperty, archiveId);
				else
					allIdArchives.add(archiveId);
			}
		}
	}

	public static void validateAnalysisTypes(ContextValidation contextValidation, String typeCode, BioinformaticParameters bioinformaticParameters, Map<String, PropertyValue> properties)
	{
		//Validation si la propriété analysisTypes existe
		ProjectType projectType = ProjectType.miniFind.get().findByCode(typeCode);
		List<PropertyDefinition> propDefs = projectType.propertiesDefinitions.stream().filter(pdf->pdf.code.equals("analysisTypes")).collect(Collectors.toList());
		if(propDefs.size()>0) {
			if(bioinformaticParameters.biologicalAnalysis && 
					(!properties.containsKey("analysisTypes") || 
					(properties.containsKey("analysisTypes") && ((PropertyListValue)properties.get("analysisTypes")).listValue().size()==0))) {
				contextValidation.addError("analysisTypes","error.noanalysetypeforba");
			}
		}
		
	}
	/**
	 * validatePropertiesRequired()
	 * 
	 * @param contextValidation
	 * @param typeCode
	 * @param properties
	 * @param fields
	 */
	public static void validatePropertiesRequired(ContextValidation contextValidation, String typeCode,
			Map<String, PropertyValue> properties, List<String> fields) {
		ProjectType projectType = CommonValidationHelper.validateCodeForeignRequired(contextValidation,
				ProjectType.miniFind.get(), typeCode, "typeCode", true);

		if (projectType != null) {
			for (int i = 0; i < projectType.propertiesDefinitions.size(); i++) {
				boolean isFound = false;

				if (projectType.propertiesDefinitions.get(i).required.booleanValue()) {
					boolean isInFields = false;

					for (int b = 0; b < fields.size(); b++) {
						if (projectType.propertiesDefinitions.get(i).code
								.contains(fields.get(b).replace("properties.", "").replace(".value", ""))) {
							isInFields = true;
						}
					}

					if (isInFields) {
						for (int b = 0; b < fields.size(); b++) {
							for (Map.Entry<String, PropertyValue> entry : properties.entrySet()) {
								PropertyValue val = null;

								if (entry.getValue() != null) {
									val = entry.getValue();
								}

								if (val != null && val.value != null) {
									if (val.value.getClass().equals(ArrayList.class)) {
										List<Map<String, Object>> l = (List<Map<String, Object>>) val.value;

										for (int m = 0; m < l.size(); m++) {
											for (Map.Entry<String, Object> entry2 : l.get(m).entrySet()) {
												String propName = entry.getKey() + "." + entry2.getKey();

												if (propName.equals(projectType.propertiesDefinitions.get(i).code)
														&& propName.contains(fields.get(b).replace("properties.", "")
																.replace(".value", ""))) {
													isFound = true;
												}
											}
										}
									} else if (val.value.getClass().equals(LinkedHashMap.class)) {
										Map<String, Object> l = (HashMap<String, Object>) val.value;

										for (Map.Entry<String, Object> entryIt : l.entrySet()) {
											String propName = entry.getKey() + "." + entryIt.getKey();
											
											if (propName.equals(projectType.propertiesDefinitions.get(i).code) &&
									        		propName.contains(fields.get(b).replace("properties.", "")
															.replace(".value", ""))) {
												isFound = true;
											}
									    }
									} else {
										String propName = entry.getKey();

										if (propName.equals(projectType.propertiesDefinitions.get(i).code)) {
											isFound = true;
										}
									}
								} else {
									if (entry.getKey().equals(projectType.propertiesDefinitions.get(i).code)) {
										isFound = true;
									}
								}
							}
						}
					} else {
						isFound = true;
					}

					if (!isFound) {
						contextValidation.addError(projectType.propertiesDefinitions.get(i).code,
								ValidationConstants.ERROR_REQUIRED_MSG);
					}
				}
			}
		}
	}

	// -----------------------------------------------------------------------------------
	// renamed and arguments reordered

	/**
	 * Validate a required project category code.
	 * 
	 * @param categoryCode      project category code
	 * @param contextValidation validation context
	 * @deprecated use
	 *             {@link #validateProjectCategoryCodeRequired(ContextValidation, String)}
	 */
	@Deprecated
	public static void validateProjectCategoryCode(String categoryCode, ContextValidation contextValidation) {
		ProjectValidationHelper.validateProjectCategoryCodeRequired(contextValidation, categoryCode);
	}

	/**
	 * Validate a required project category code.
	 * 
	 * @param contextValidation validation context
	 * @param categoryCode      project category code
	 */
	public static void validateProjectCategoryCodeRequired(ContextValidation contextValidation, String categoryCode) {
		CommonValidationHelper.validateCodeForeignRequired(contextValidation, ProjectCategory.miniFind.get(),
				categoryCode, "categoryCode");
	}

	// -----------------------------------------------------------------------------------
	// renamed and arguments reordered

	/**
	 * Validate an optional umbrella project code.
	 * 
	 * @param umbrellaProjectCode umbrella project code
	 * @param contextValidation   validation context
	 * @deprecated use
	 *             {@link #validateUmbrellaProjectCodeOptional(ContextValidation, String)}
	 */
	@Deprecated
	public static void validateUmbrellaProjectCode(String umbrellaProjectCode, ContextValidation contextValidation) {
		ProjectValidationHelper.validateUmbrellaProjectCodeOptional(contextValidation, umbrellaProjectCode);
	}

	/**
	 * Validation an optional umbrella project code.
	 * 
	 * @param contextValidation   validation context
	 * @param umbrellaProjectCode umbrella project code
	 */
	public static void validateUmbrellaProjectCodeOptional(ContextValidation contextValidation,
			String umbrellaProjectCode) {
		// GA: temporary unset if
		// if (ValidationHelper.required(contextValidation, umbrellaProjectCode,
		// "umbrellaProjectCode")) {
		if ((umbrellaProjectCode != null) && !MongoDBDAO.checkObjectExist(InstanceConstants.UMBRELLA_PROJECT_COLL_NAME,
				UmbrellaProject.class, DBQuery.is("code", umbrellaProjectCode))) {
			contextValidation.addError("umbrellaProjectCode", ValidationConstants.ERROR_CODE_NOTEXISTS_MSG,
					umbrellaProjectCode);
		}
		// }
	}

	// -----------------------------------------------------------------------------------
	// renamed and arguments reordered

	/**
	 * Validate a required bioinformatics parameter object.
	 * 
	 * @param bioinformaticParameters bioinformatics parameter to validate
	 * @param contextValidation       validation context
	 * @deprecated use
	 *             {@link #validateBioformaticParametersRequired(ContextValidation, BioinformaticParameters)}
	 */
	@Deprecated
	public static void validateBioformaticParameters_(BioinformaticParameters bioinformaticParameters,
			ContextValidation contextValidation) {
		ProjectValidationHelper.validateBioformaticParametersRequired(contextValidation, bioinformaticParameters);
	}

	/**
	 * Validate a required bioinformatics parameter object.
	 * 
	 * @param contextValidation       validation context
	 * @param bioinformaticParameters bioinformatics parameter to validate
	 */
	public static void validateBioformaticParametersRequired(ContextValidation contextValidation,
			BioinformaticParameters bioinformaticParameters) {
		if (ValidationHelper.validateNotEmpty(contextValidation, bioinformaticParameters, "bioinformaticParameters"))
			bioinformaticParameters.validate(contextValidation);
	}

}
