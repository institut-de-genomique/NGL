package controllers.projects.api;

//
//public class ProjectsController extends CommonController {
//
//    protected static Project getProject(String code) {
//    	Project project = MongoDBDAO.findByCode(InstanceConstants.PROJECT_COLL_NAME, Project.class, code);
//    	return project;
//    }
//    
//    protected static Project getProject(String code, String...keys) {
//    	MongoDBResult<Project> projects = MongoDBDAO.find(InstanceConstants.PROJECT_COLL_NAME, Project.class, DBQuery.is("code", code), getIncludeKeys(keys));
//		if(projects.size() == 1)
//		    return projects.toList().get(0);
//		else
//		    return null;
//    }
//    
//    protected static TraceInformation getUpdateTraceInformation(Project project) {
//		TraceInformation ti = project.traceInformation;
//		ti.setTraceInformation(getCurrentUser());
//		return ti;
//	}
//	
//    protected static UmbrellaProject getUmbrellaProject(String code) {
//    	UmbrellaProject proj = MongoDBDAO.findByCode(InstanceConstants.UMBRELLA_PROJECT_COLL_NAME, UmbrellaProject.class, code);
//    	return proj;
//    }
//    
//    protected static UmbrellaProject getProjectUmbrella(String code, String...keys) {
//    	MongoDBResult<UmbrellaProject> projects = MongoDBDAO.find(InstanceConstants.UMBRELLA_PROJECT_COLL_NAME, UmbrellaProject.class, DBQuery.is("code", code), getIncludeKeys(keys));
//    	if(projects.size() == 1)
//    	    return projects.toList().get(0);
//    	else
//    	    return null;
//        }
//        
//        protected static TraceInformation getUpdateTraceInformation(UmbrellaProject proj) {
//    		TraceInformation ti = proj.traceInformation;
//    		ti.setTraceInformation(getCurrentUser());
//    		return ti;
//    	}
//
//
//}
