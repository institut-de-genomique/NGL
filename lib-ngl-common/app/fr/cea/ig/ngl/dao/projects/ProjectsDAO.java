package fr.cea.ig.ngl.dao.projects;

import javax.inject.Inject;
import javax.inject.Singleton;

import fr.cea.ig.ngl.dao.GenericMongoDAO;
import models.laboratory.project.instance.Project;
import models.utils.InstanceConstants;

@Singleton
public class ProjectsDAO extends GenericMongoDAO<Project> {

	// Generic access to db
	//private final GenericMongoDAO<Project> gdao;
	
	@Inject
	public ProjectsDAO() {
		//gdao = new GenericMongoDAO<>(InstanceConstants.PROJECT_COLL_NAME, Project.class);
		super(InstanceConstants.PROJECT_COLL_NAME, Project.class);
	}
	
//	public Project findOne(DBQuery.Query q) throws DAOException {
//		return gdao.findOne(q);
//	}
//	
//	public Project findByCode(String code) throws DAOException {
//		return gdao.findByCode(code);
//	}
//	
//	public Iterable<Project> all() throws DAOException {
//		return gdao.all();
//	}
//	
//	public Iterable<Project> find(DBQuery.Query q) throws DAOException {
//		return gdao.find(q);
//	}
//	
//	public List<Project> findAsList(DBQuery.Query q) throws DAOException {
//		return gdao.findAsList(q);
//	}
//	
//	public MongoDBResult<Project> mongoDBFinder(Query query) throws DAOException {
//		return gdao.mongoDBFinder(form, query);
//	}
//
//	public Project saveObject(Project projectInput) {
//		return gdao.saveObject(projectInput);
//	}
	
	
}
