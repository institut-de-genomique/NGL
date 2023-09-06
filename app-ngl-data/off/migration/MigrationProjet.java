package controllers.migration;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import models.LimsCNSDAO;
import models.laboratory.experiment.instance.Experiment;
import models.utils.InstanceConstants;
import models.utils.InstanceHelpers;
import models.utils.dao.DAOException;
import models.utils.instance.SampleHelper;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;
import org.mongojack.JacksonDBCollection;
import org.mongojack.DBUpdate.Builder;

import play.Logger;
import play.Logger.ALogger;
import play.api.modules.spring.Spring;
import play.mvc.Result;
import validation.ContextValidation;
import validation.processes.instance.ProcessValidationHelper;
import controllers.CommonController;
import controllers.migration.models.ProcessOld;
import fr.cea.ig.MongoDBDAO;
import models.laboratory.processes.instance.Process;
import models.laboratory.project.instance.Project;
import models.laboratory.sample.instance.Sample;

public class MigrationProjet extends CommonController {
	
	private static final String PROCESS_COLL_NAME_BCK = InstanceConstants.PROCESS_COLL_NAME+"_BCK";

	static ALogger logger=Logger.of("MigrationProcessSample");
	 static LimsCNSDAO  limsServices = Spring.getBeanOfType(LimsCNSDAO.class);
	
	public static Result migration() throws DAOException, SQLException{
		ContextValidation ctxVal = new ContextValidation(getCurrentUser());
		lastNGLSampleCode(ctxVal);
		createProject(ctxVal);
		
		return ok("Migration Process Finish");
	}

	
	public static void createProject(ContextValidation contextValidation) throws SQLException, DAOException{
		
		List<Project> projects = limsServices.findProjectToCreate(contextValidation) ;
			
			for(Project limsProject:projects){
		
				Project nglProject = MongoDBDAO.findByCode(InstanceConstants.PROJECT_COLL_NAME, Project.class, limsProject.code);
				if(nglProject != null){
					Builder update =  DBUpdate.set("name",limsProject.name).set("archive", limsProject.archive);
					
					if((nglProject.lastSampleCode == null &&  limsProject.lastSampleCode != null)
							|| (nglProject.lastSampleCode != null &&  limsProject.lastSampleCode != null
								&& limsProject.nbCharactersInSampleCode > nglProject.nbCharactersInSampleCode)
							|| (nglProject.lastSampleCode != null &&  limsProject.lastSampleCode != null
									&& limsProject.nbCharactersInSampleCode == nglProject.nbCharactersInSampleCode
									&& limsProject.lastSampleCode.compareTo(nglProject.lastSampleCode) > 0)){
						update.set("lastSampleCode",limsProject.lastSampleCode);
						update.set("nbCharactersInSampleCode",limsProject.nbCharactersInSampleCode);
						Logger.error(nglProject.code+": "+limsProject.lastSampleCode +" != "+ nglProject.lastSampleCode);
					}
						
					nglProject.traceInformation.setTraceInformation("ngl-data");
					MongoDBDAO.update(InstanceConstants.PROJECT_COLL_NAME, Project.class, 
							DBQuery.is("code", limsProject.code),update);
					
				}else{
					InstanceHelpers.save(InstanceConstants.PROJECT_COLL_NAME,limsProject,contextValidation);
				}
			}
		
			//InstanceHelpers.save(InstanceConstants.PROJECT_COLL_NAME,projects,contextValidation);
			
		}

	
	private static void lastNGLSampleCode(ContextValidation contextError) {
		MongoDBDAO.find(InstanceConstants.PROJECT_COLL_NAME, Project.class).getCursor().forEach(p -> {
				String lastNGLSampleCode = getLastSample(p.code);
				if(null != lastNGLSampleCode){
					p.lastSampleCode = lastNGLSampleCode;
					p.nbCharactersInSampleCode = lastNGLSampleCode.replace(p.code+"_","").length();
					MongoDBDAO.update(InstanceConstants.PROJECT_COLL_NAME, p);
				}else{
					p.lastSampleCode = null;
					p.nbCharactersInSampleCode = null;
					MongoDBDAO.update(InstanceConstants.PROJECT_COLL_NAME, p);
				}
			
		});;
		
	}

	
	private static String getLastSample(String code) {
		String[] last = new String[]{null};
		MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.in("projectCodes", code))
			.sort("code").getCursor().forEach(s -> {
				if(last[0] == null || s.code.length() > last[0].length()){
					last[0] = s.code;
				}else if(s.code.length() == last[0].length() && s.code.compareTo(last[0]) > 0){
					last[0] = s.code;
				}
				
			});
		return last[0];
	}
}
