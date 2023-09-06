package models.laboratory.project.instance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.fasterxml.jackson.annotation.JsonIgnore;

import fr.cea.ig.DBObject;
import fr.cea.ig.ngl.dao.projects.ProjectsDAO;
import fr.cea.ig.ngl.utils.GuiceSupplier;
import models.laboratory.common.instance.Comment;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.utils.InstanceConstants;
import validation.ContextValidation;
import validation.IValidation;
import validation.common.instance.CommonValidationHelper;
import validation.project.instance.ProjectValidationHelper;

/**
 * Instance Project is stocked in Collection mongodb Project
 * Project Name are referencing in class Experience and Container
 * 
 * @author mhaquell
 *
 */
public class Project extends DBObject implements IValidation {

	public static final Supplier<ProjectsDAO> find = new GuiceSupplier<>(ProjectsDAO.class);
	
	public String                     name;
	public String                     typeCode;
	public String                     categoryCode;
	public State                      state;
    public String                     description;
    public List<Comment>              comments = new ArrayList<Comment>();
	public TraceInformation           traceInformation;
	public Map<String, PropertyValue> properties;
	public String                     umbrellaProjectCode;
	public BioinformaticParameters    bioinformaticParameters;
	public String                     lastSampleCode;
	public Integer                    nbCharactersInSampleCode = 4;
	public List<String>               authorizedUsers;
	public List<String> 			  projectManagers;

	public Project() {}
	
	public Project(Map<String, PropertyValue> properties) {
		this.properties = properties;
	}
	
	@Override
	@JsonIgnore
	public void validate(ContextValidation contextValidation) {				
		CommonValidationHelper .validateIdPrimary                    (contextValidation, this);
		CommonValidationHelper .validateCodePrimary                  (contextValidation, this, InstanceConstants.PROJECT_COLL_NAME);
		CommonValidationHelper .validateTraceInformationRequired     (contextValidation, traceInformation);
		ProjectValidationHelper.validateProjectTypeCodeRequired      (contextValidation, typeCode, properties);
		ProjectValidationHelper.validateProjectCategoryCodeRequired  (contextValidation, categoryCode);
		CommonValidationHelper .validateStateRequired                (contextValidation, typeCode, state);
		ProjectValidationHelper.validateUmbrellaProjectCodeOptional  (contextValidation, umbrellaProjectCode);
		ProjectValidationHelper.validateBioformaticParametersRequired(contextValidation, bioinformaticParameters);
		ProjectValidationHelper.validateArchiveProperties			 (contextValidation, code, typeCode, properties, true);
		ProjectValidationHelper.validateAnalysisTypes				 (contextValidation, typeCode, bioinformaticParameters, properties);
	}
}
