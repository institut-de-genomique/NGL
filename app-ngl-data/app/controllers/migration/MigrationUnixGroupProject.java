package controllers.migration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import javax.inject.Inject;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import com.mongodb.MongoException;

import controllers.DocumentController;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.play.migration.NGLContext;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.project.instance.Project;
import models.utils.InstanceConstants;
import play.mvc.Result;

public class MigrationUnixGroupProject extends DocumentController<Project>{

	private static final play.Logger.ALogger logger = play.Logger.of(MigrationUnixGroupProject.class);
	
	@Inject
	protected MigrationUnixGroupProject(NGLContext ctx) {
		super(ctx, InstanceConstants.PROJECT_COLL_NAME, Project.class);
	}
	
	public Result migration(String fileName) throws NumberFormatException, MongoException, IOException {
		
		BufferedReader reader = new BufferedReader(new FileReader(new File(fileName)));
		logger.debug("File name "+fileName);
		String line = "";
		while ((line = reader.readLine()) != null) {
			String[] tabLine = line.split("\t");
			String codeProjet = tabLine[0];
			String unixGroup = tabLine[4];
			logger.debug("Project "+codeProjet+" "+unixGroup);
			Project project = MongoDBDAO.findByCode(InstanceConstants.PROJECT_COLL_NAME, Project.class, codeProjet);
			logger.debug("Get project "+project.code+" "+unixGroup);
			if(project.properties==null){
				project.properties=new HashMap<>();
			}
			
			if (unixGroup == null) {
				project.properties.put("unixGroup", new PropertySingleValue("g-extprj"));
			} else {
				project.properties.put("unixGroup", new PropertySingleValue(unixGroup));
			}
			logger.debug("Project "+project);
			MongoDBDAO.update(InstanceConstants.PROJECT_COLL_NAME,Project.class, DBQuery.is("code", project.code),DBUpdate.set("properties",project.properties));
		}
		reader.close();		
		return ok("Migration UnixGroup finished");
	}
	
}
