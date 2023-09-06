package controllers.migration.cns;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;
import org.springframework.jdbc.core.RowMapper;

import com.mongodb.BasicDBObject;

import controllers.CommonController;
import fr.cea.ig.MongoDBDAO;
import models.LimsCNSDAO;
import models.laboratory.container.instance.Container;
import models.laboratory.project.instance.Project;
import models.laboratory.run.instance.ReadSet;
import models.utils.InstanceConstants;
import play.Logger;
import play.api.modules.spring.Spring;
import play.mvc.Result;


public class MigrationTransfertCCRT extends CommonController{

	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmm");

	protected static LimsCNSDAO  limsServices = Spring.getBeanOfType(LimsCNSDAO.class);


	public static Result migration(){
		//Backup Project collection
		backUpProject();

		List<Project> projects = MongoDBDAO.find(InstanceConstants.PROJECT_COLL_NAME, Project.class).toList();

		for(Project project : projects){
			//Get from LIMS fgGroup/fgPriority
			ProjectTransfertCCRT projectTransfertCCRT = findProject(project.code);
			if(projectTransfertCCRT!=null && projectTransfertCCRT.groupefg!=null){
				Logger.debug("Project "+project.code);
				Logger.debug("Fg project "+projectTransfertCCRT.groupefg);
				Logger.debug("Priority "+projectTransfertCCRT.priorityfg);
				//Update project collection 
				MongoDBDAO.update(InstanceConstants.PROJECT_COLL_NAME, Project.class,DBQuery.is("code", project.code),
						DBUpdate.set("bioinformaticParameters.fgGroup",projectTransfertCCRT.groupefg).set("bioinformaticParameters.fgPriority", projectTransfertCCRT.priorityfg));
				//Update ReadSet to transfert to CCRT
				//State=A; location=CNS
				//MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, 
				//			DBQuery.is("projectCode", project.code).is("state.code", "A").is("location", "CCRT"),
				//			DBUpdate.set("state.code", "IW-TF"));
			}
			
			
		}

		return ok();

	}

	private static void backUpProject()
	{
		String backupName = InstanceConstants.PROJECT_COLL_NAME+"_BCK_ProjectCCRT_"+sdf.format(new java.util.Date());
		Logger.info("\tCopie "+InstanceConstants.PROJECT_COLL_NAME+" to "+backupName+" start");

		List<Project> projects = MongoDBDAO.find(InstanceConstants.PROJECT_COLL_NAME, Project.class).toList();
		MongoDBDAO.save(backupName, projects);

		Logger.info("\tCopie "+InstanceConstants.PROJECT_COLL_NAME+" to "+backupName+" end");

	}

	private static ProjectTransfertCCRT findProject(String code){

		List<ProjectTransfertCCRT> results = limsServices.jdbcTemplate.query("pl_ProjetUn @prsco=?", new String[]{code} 
		,new RowMapper<ProjectTransfertCCRT>() {
			public ProjectTransfertCCRT mapRow(ResultSet rs, int rowNum) throws SQLException {
				ProjectTransfertCCRT project = new ProjectTransfertCCRT();
				project.groupefg = rs.getString("groupefg");
				project.priorityfg = rs.getInt("prioritefg");
				return project;						
			}
		});

		if(results.size() != 1 ){
			return null;
		}else{
			return results.get(0);
		}

	}
}
