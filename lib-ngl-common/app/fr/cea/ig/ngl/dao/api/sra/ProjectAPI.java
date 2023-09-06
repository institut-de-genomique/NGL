package fr.cea.ig.ngl.dao.api.sra;

import static ngl.refactoring.state.SRASubmissionStateNames.NONE;
import static ngl.refactoring.state.SRASubmissionStateNames.SUB_N;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;
import org.mongojack.DBUpdate.Builder;

import fr.cea.ig.MongoDBResult;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.api.GenericAPI;
import fr.cea.ig.ngl.dao.sra.ProjectDAO;
import fr.cea.ig.play.IGGlobals;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.sra.submit.sra.instance.Project;
import models.sra.submit.util.SraCodeHelper;
import validation.ContextValidation;


public class ProjectAPI extends GenericAPI<ProjectDAO, Project> {

	private final  List<String> DEFAULT_KEYS            = Arrays.asList("code", "title", "description","centerName", "accession", 
			                                                            "submissionProjectType", "firstSubmissionDate", "state", 
			                                                            "traceInformation", "childrenProjectAccessions", "adminComment");
	
	private final List<String> AUTHORIZED_UPDATE_FIELDS = null;

	private final SraCodeHelper       sraCodeHelper;
	@Inject
	public ProjectAPI(ProjectDAO dao,
		              SraCodeHelper sraCodeHelper) {
		super(dao);
		this.sraCodeHelper = sraCodeHelper;
	}

//	public Project dao_getObject(String projectCode) {
//		return dao.getObject(projectCode);
//	}
//	
	
	
	@Override
	protected List<String> authorizedUpdateFields() {
		return this.AUTHORIZED_UPDATE_FIELDS;
	}

	@Override
	protected List<String> defaultKeys() {
		return this.DEFAULT_KEYS;
	}

	@Authenticated
	@Authorized.Write
	public Project create(Project userProject, String currentUser, Boolean modeCopy) throws APIValidationException, APIException {	
		
		Boolean copy = false;
		
		if(modeCopy != null && modeCopy==true) {
			copy = true;
		}
		
	    ContextValidation ctxVal = ContextValidation.createCreationContext(currentUser);
		// important de mettre le mode copy dans le context de validation car ce boolean est utilisé pour la validation de l'objet
		ctxVal.putObject("copy", copy);

		// code difficilement  deportable dans validate car dans validate necessité d'avoir le code avant de sauver quel que soit le mode (copy a true ou false) 
		// alors qu'ici on veut tester avant generation eventuelle du code . => sinon doublons des tests 
		if(copy) {
			// en mode copy, le userProject._id est ignoré est mis à null pour le save.
			userProject._id = null;
		}
		if (userProject._id != null && !copy) {
			ctxVal.addError("userProject.id " + userProject._id, " presence de l'id incompatible  si copy a false ");
		}
		
		if (StringUtils.isBlank(userProject.code) && (copy)) { 
			ctxVal.addError("userProject sans code", " absence de code incompatible avec si copy a true");
		} 
		if (StringUtils.isNotBlank(userProject.code) && (!copy)) {		// autoriser code existant dans inputObject uniquement en mode copy :
			ctxVal.addError("userProject.code " + userProject.code, " presence du code incompatible  si copy a false  "); 
		}
		
		if (ctxVal.hasErrors()) { // inutile de faire le try catch 
			throw new APIValidationException(INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
		}
		try {
			// preparation donnée :
			if (!copy) { /// En mode classic, le project est sans code et sans id, il faut generer traceInformation, code et mettre state
				userProject.traceInformation = new TraceInformation(currentUser);
				userProject.state = new State(NONE, currentUser);
				//ok, code aléatoire qui ne doit pas exister en base :
				userProject.code = sraCodeHelper.generateUmbrellaCode();
				while (dao.checkObjectExist("code", userProject.code)) {
					userProject.code = sraCodeHelper.generateUmbrellaCode();
				}
				userProject.state = new State(SUB_N, currentUser);
			} else {
				// en mode copy, rien : le project a un code mais pas d'id, et pas de traceIformation ou state ou code à generer puisqu'on 
				// conserve les champs d'origine
			}
		} catch(RuntimeException e) {
			ctxVal.addError("RuntimeException", e.getMessage());
		} catch(Exception e) {
			ctxVal.addError("Exception", e.getMessage());
		}
		if (ctxVal.hasErrors()) {
			throw new APIValidationException("Exception", ctxVal.getErrors());
		}
		// validation et sauvegarde dans base
		userProject.validate(ctxVal);
		if (ctxVal.hasErrors()) {
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}
		Project  dbProject = dao.save(userProject);
		return dbProject;
	}
	

	@Override
	@Authenticated
	@Authorized.Write
	public Project create(Project input, String currentUser)
			throws APIValidationException, APIException {
		return create(input, currentUser, false);
	}

	public void validateParamsForUpdateOrThrow(Project userProject, String currentUser, Boolean modeCopy) throws APIValidationException {
		Boolean copy = false;		
		if(modeCopy != null && modeCopy==true) {
			copy = true;
		}
		ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser);
		// important de mettre le mode copy dans le context de validation car ce boolean peut etre utilisé pour la validation de l'objet
		ctxVal.putObject("copy", copy);

		if(userProject == null ) {
			ctxVal.addError("project ",  " valeur nulle  incompatible avec la methode update");
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}
		if (StringUtils.isBlank(userProject.code)) {
			ctxVal.addError("project.code ",  " valeur nulle incompatible avec la methode ");
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}	
		Project dbProject = get(userProject.code);
		if (dbProject == null) {
			ctxVal.addError("project.code " + userProject.code , " n'existe pas dans la base et mode update");
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}

		// En mode copy ou non, l'id de l'objet qui provient d'une autre collection est mis à l'id de l'objet dans la collection cible :			
		userProject._id = dbProject._id;
		// en mode normal, on impose traceInformation, alors qu'en mode copy, on conserve le traceInformation de l'objet en entrée
		if (!copy) {
			userProject.traceInformation.forceModificationStamp(currentUser);
		}
		userProject.validate(ctxVal);
		if (ctxVal.hasErrors()) {
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}
	}
	
	@Override
	@Authenticated
	@Authorized.Write
	public Project update(Project userProject, String currentUser) throws APIException, APIValidationException {
		return update(userProject, currentUser, false);
	}
	
	@Authenticated
	@Authorized.Write
	public Project update(Project userProject, String currentUser, Boolean modeCopy) throws APIException, APIValidationException {
		validateParamsForUpdateOrThrow(userProject, currentUser, modeCopy);
	    dao.update(userProject);
	    return  get(userProject.code);
	}

	@Override
	@Authenticated
	@Authorized.Write
	public Project update(Project userProject, String currentUser, List<String> fields)
		throws APIException, APIValidationException {
		validateParamsForUpdateOrThrow(userProject, currentUser, false);
	    ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser);
	    checkAuthorizedUpdateFields(ctxVal, fields);
	    checkIfFieldsAreDefined(ctxVal, fields, userProject);
		if (ctxVal.hasErrors()) {
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}
		
		Project confInDb= dao.getObject(userProject.code);
		TraceInformation ti = confInDb.traceInformation;
		ti.modificationStamp(ctxVal, currentUser);
		dao.updateObject(DBQuery.and(DBQuery.is("code", userProject.code)), 
									dao.getBuilder(userProject, fields).set("traceInformation", ti));
		
	    return  get(userProject.code);
	}
	
	/*-------------------------------------------------------------------------------------------------*/
	
	public Project dao_findOne(Query q) {
		return dao.findOne(q);		
	}
	@Deprecated
	public void dao_update(Query query, Builder set) {
		dao.update(query, set);
	}

	public boolean dao_checkObjectExist(String key, String keyValue) {
		return dao.checkObjectExist(key, keyValue);
	}
    @Deprecated
	public Project dao_saveObject(Project project) {
		return dao.saveObject(project);
	}

//	public void dao_deleteByCode(String projectCode) {
//		dao.deleteByCode(projectCode);
//	}

	public  MongoDBResult<Project> dao_find(Query q) {
		return dao.dao_find(q);		
	}
	
	/**
	 * Acces à une instance globale de ProjectAPI, qui permet de remplacer les appels
	 * à MongoDBDAO qui est une globale, dans un contexte ou on ne peut pas injecter. 
	 * Exemple dans objets du domaine (sra.Project, ...)
	 * @return ProjectAPI
	 */
	public static ProjectAPI get() {
		return IGGlobals.instanceOf(ProjectAPI.class);
	}

	public Iterable<Project> dao_all() {
		return dao.all();
	}
		
}
