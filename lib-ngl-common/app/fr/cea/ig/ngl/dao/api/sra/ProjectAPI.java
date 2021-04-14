package fr.cea.ig.ngl.dao.api.sra;

import java.util.List;

import javax.inject.Inject;

import org.mongojack.DBQuery.Query;
import org.mongojack.DBUpdate.Builder;

import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.api.GenericAPI;
import fr.cea.ig.ngl.dao.sra.ProjectDAO;
import fr.cea.ig.play.IGGlobals;
import models.sra.submit.common.instance.Project;

public class ProjectAPI extends GenericAPI<ProjectDAO, Project> {
	private static final play.Logger.ALogger logger = play.Logger.of(ProjectAPI.class);

	@Inject
	public ProjectAPI(ProjectDAO dao) {
		super(dao);
	}
	
	public Project dao_getObject(String projectCode) {
		return dao.getObject(projectCode);
	}
	
	public Project dao_findOne(Query q) {
		return dao.findOne(q);		
	}

	public void dao_update(Query query, Builder set) {
		dao.update(query, set);
	}

	public boolean dao_checkObjectExist(String key, String keyValue) {
		return dao.checkObjectExist(key, keyValue);
	}

	public void dao_saveObject(Project project) {
		logger.debug("save Object "+ project.code);
		dao.saveObject(project);
	}

	public void dao_deleteByCode(String projectCode) {
		dao.deleteByCode(projectCode);
	}
	
	/*-------------------------------------------------------------------------------------------------*/

	@Override
	protected List<String> authorizedUpdateFields() {
		return null;
	}

	@Override
	protected List<String> defaultKeys() {
		return null;
	}

	@Override
	public Project create(Project input, String currentUser)
			throws APIValidationException, APIException {
		return null;
	}

	@Override
	public Project update(Project input, String currentUser) throws APIException,
			APIValidationException {
		return null;
	}

	@Override
	public Project update(Project input, String currentUser, List<String> fields)
			throws APIException, APIValidationException {
		return null;
	}

	/**
	 * Acces à une instance globale de StudyAPI, qui permet de remplacer les appels
	 * à MongoDBDAO qui est une globale, dans un contexte ou on ne peut pas injecter. 
	 * Exemple dans objets du domaine (sra.Experiment, ...)
	 * @return StudyAPI
	 */
	public static ProjectAPI get() {
		return IGGlobals.instanceOf(ProjectAPI.class);
	}


		
}
