package models.laboratory.project.instance;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.mongojack.MongoCollection;

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
import validation.project.instance.ProjectValidationHelper;



/**
 * Instance Project is stocked in Collection mongodb Project
 * Project Name are referencing in class Experience and Container
 * 
 * @author mhaquell
 *
 */
// TODO: check if this has any meaning as the value differs from InstanceConstants PROJECT_COLL_NAME
@MongoCollection(name="Project")
public class Project extends DBObject implements IValidation {

	public static final Supplier<ProjectsDAO> find = new GuiceSupplier<>(ProjectsDAO.class);
	
	public String                     name;
	public String                     typeCode;
	public String                     categoryCode;
	public State                      state;
    public String                     description;
    public List<Comment>              comments;
	public TraceInformation           traceInformation;
	public Map<String, PropertyValue> properties;
	public String                     umbrellaProjectCode;
	public BioinformaticParameters    bioinformaticParameters;
	public String                     lastSampleCode;
	public Integer                    nbCharactersInSampleCode = 4;
	public List<String>               authorizedUsers;
	//TODO EJACOBY AD
	//public List<String> projectManagers;
	
	public Boolean archive;

	public Project() {}
	
	public Project(Map<String, PropertyValue> properties) {
		this.properties = properties;
	}
	
	@Override
	@JsonIgnore
	public void validate(ContextValidation contextValidation) {				
		ProjectValidationHelper.validateIdPrimary                   (contextValidation, this);
		ProjectValidationHelper.validateCodePrimary                 (contextValidation, this, InstanceConstants.PROJECT_COLL_NAME);
		ProjectValidationHelper.validateTraceInformationRequired     (contextValidation,        traceInformation);
		ProjectValidationHelper.validateProjectType          (typeCode,properties,     contextValidation);
		ProjectValidationHelper.validateProjectCategoryCode  (categoryCode,            contextValidation);
		ProjectValidationHelper.validateStateRequired                (contextValidation,typeCode,          state);
		ProjectValidationHelper.validateUmbrellaProjectCode  (umbrellaProjectCode,     contextValidation);
		ProjectValidationHelper.validateBioformaticParameters(bioinformaticParameters, contextValidation);
	}

}
