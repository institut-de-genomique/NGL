package validation.project.instance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;

import fr.cea.ig.MongoDBDAO;
import io.jsonwebtoken.lang.Collections;
import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.property.PropertyListValue;
import models.laboratory.project.description.ProjectCategory;
import models.laboratory.project.description.ProjectType;
import models.laboratory.project.instance.BioinformaticParameters;
import models.laboratory.project.instance.Project;
import models.laboratory.project.instance.SystemParameters;
import models.laboratory.project.instance.SystemParameters.ArchiveEntry;
import models.laboratory.project.instance.UmbrellaProject;
import models.utils.InstanceConstants;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import validation.utils.ValidationConstants;
import validation.utils.ValidationHelper;

public class ProjectValidationHelper {

	public static final int AUTHORIZED_SIZE = 10;
	public static final String AUTHORIZED_ID_FORMAT = "^[a-zA-Z0-9]+_\\d{" + AUTHORIZED_SIZE + "}$";
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
		if (projectType != null)
			ValidationHelper.validateProperties(contextValidation, properties,
					projectType.getPropertiesDefinitionProjectLevel()); 
	}
	
	/**
	 * validateArchiveHistory()
	 * 
	 * @param contextValidation validation context
	 * @param archiveHistory archive history
	 */
	public static void validateArchiveHistory(ContextValidation contextValidation, List<ArchiveEntry> archiveHistory) {
		// S'il n'y a pas d'archives en input on ne va pas plus loin.
		if (Collections.isEmpty(archiveHistory)) { return; }
		
		String projectCode = (String) contextValidation.getObject("projectCode");
		if (StringUtils.isBlank(projectCode)) { 
			contextValidation.addError("systemParameters.archiveHistory", "error.missingprojectcode.archive"); 
			return;
		}

		//list all id archives without duplicate
		//Iterate once archiveHistory : validate and get informations for unique validation
		List<String> allIdArchives = new ArrayList<String>();
		for(int index = 0; index < archiveHistory.size(); index++) {
			ArchiveEntry archiveEntry = archiveHistory.get(index);			
			validateArchiveEntryDate(contextValidation, archiveEntry, index);
			validateArchiveEntryAction(contextValidation, archiveEntry, allIdArchives, index);
		}
		
		//Verifier unicité des id archives dans les instances en base
		//Get list id query
		if(!allIdArchives.isEmpty()) {
			Query isAlreadyExistingId = getUniqueArchiveIdValidationQuery(projectCode, allIdArchives);
			Project.find.get().find(isAlreadyExistingId).forEach((Project p) -> contextValidation.addError("systemParameters.archiveHistory.notUnique", "error.idnotuniqueProject", p.code) );
		}
	}
	
	private static void validateArchiveEntryDate(ContextValidation contextValidation, ArchiveEntry archiveEntry, int index) {
		if(archiveEntry.date == null) {
			contextValidation.addError("systemParameters.archiveHistory["+index+"].date",ValidationConstants.ERROR_REQUIRED_MSG);
		}
	}
	
	private static void validateArchiveEntryAction(ContextValidation contextValidation, ArchiveEntry archiveEntry, List<String> allIdArchives, int index) {
		//get action && validate value action
		if(archiveEntry.action == null) {
			contextValidation.addError("systemParameters.archiveHistory["+index+"].action",ValidationConstants.ERROR_REQUIRED_MSG);
			return;
		}
		switch(archiveEntry.action) {
		case DESARCHIVAGE: validateDesarchivingAction(contextValidation, archiveEntry, index); break;
		case ARCHIVAGE:  validateArchivingAction(contextValidation, archiveEntry, allIdArchives, index); break;
		default: contextValidation.addError("systemParameters.archiveHistory["+index+"].action", "error.unauthorizedValue");
		}
	}
	
	private static void validateDesarchivingAction(ContextValidation contextValidation, ArchiveEntry archiveEntry, int index) {
		if(ValidationHelper.isAnyNotEmpty(archiveEntry.projArchiveId, archiveEntry.scratchArchiveId, archiveEntry.rawArchiveId)) {
			contextValidation.addError("systemParameters.archiveHistory["+index+"]","error.desarchivage.idnotrequired");
		}
	}
	
	private static void validateArchivingAction(ContextValidation contextValidation, ArchiveEntry archiveEntry, List<String> allIdArchives, int index) {
		validateOptionalArchiveId(contextValidation, archiveEntry.rawArchiveId, "rawArchiveId", allIdArchives, index);
		validateOptionalArchiveId(contextValidation, archiveEntry.scratchArchiveId, "scratchArchiveId", allIdArchives, index);
		validateProjArchiveId(contextValidation, archiveEntry.projArchiveId, allIdArchives, index);
	}

	private static void validateOptionalArchiveId(ContextValidation contextValidation, String archiveId, String keyProperty, List<String> allIdArchives, int index) {
		if(ValidationHelper.isNotEmpty(archiveId)) {
			validateArchiveId(contextValidation, archiveId, keyProperty, allIdArchives, index);
		}
	}
	
	private static void validateProjArchiveId(ContextValidation contextValidation, String projArchiveId, List<String> allIdArchives, int index) {
		//Project id obligatoire en mode archivage
		if(ValidationHelper.isEmpty(projArchiveId)) {
			contextValidation.addError("systemParameters.archiveHistory["+index+"]","error.required.archive","projArchiveId");
			return;
		}
		//Validate format for archivage Unknow_id_format accepted
		if(UNKNOWN_ID.equals(projArchiveId)) { return; }
		validateArchiveId(contextValidation, projArchiveId, "projArchiveId", allIdArchives, index);
	}
	
	private static void validateArchiveId(ContextValidation contextValidation, String archiveId, String keyProperty, List<String> allIdArchives, int index) {
		if(!Pattern.matches(AUTHORIZED_ID_FORMAT, archiveId)) {
			contextValidation.addError("systemParameters.archiveHistory["+index+"]","error.badformat.archive",keyProperty);
		} else {
			//Add to list id to check unique value
			if(allIdArchives.contains(archiveId)) {
				contextValidation.addError("systemParameters.archiveHistory["+index+"]","error.codedouble.archive",keyProperty, archiveId);
			} else {
				allIdArchives.add(archiveId);
			}
		}
	}
	
	private static Query getUniqueArchiveIdValidationQuery(String code, List<String> allIdArchives) {
		return DBQuery.and(
				DBQuery.or(
					DBQuery.in("systemParameters.archiveHistory.rawArchiveId", allIdArchives),
					DBQuery.in("systemParameters.archiveHistory.projArchiveId", allIdArchives),
					DBQuery.in("systemParameters.archiveHistory.scratchArchiveId", allIdArchives)
				),
				DBQuery.notEquals("code", code)
			);
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
	 * @param contextValidation validation context
	 * @param typeCode type code
	 * @param properties properties
	 * @param fields fields
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
										@SuppressWarnings("unchecked")
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
										@SuppressWarnings("unchecked")
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
		if (ValidationHelper.validateNotEmpty(contextValidation, bioinformaticParameters, "bioinformaticParameters")) {
			bioinformaticParameters.validate(contextValidation);
		}
	}
	
	/**
	 * Validate a required systemParameters parameter object.
	 * 
	 * @param contextValidation       validation context
	 * @param projectCode code project
	 * @param systemParameters systemParameters parameter to validate
	 */
	public static void validateSystemParametersRequired(ContextValidation contextValidation, String projectCode, SystemParameters systemParameters) {
		if (ValidationHelper.validateNotEmpty(contextValidation, systemParameters, "systemParameters")) {
			contextValidation.putObject("projectCode", projectCode);
			systemParameters.validate(contextValidation);
		}
	}

}
