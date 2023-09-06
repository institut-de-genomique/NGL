package controllers.migration.cng;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;

import controllers.CommonController;
import controllers.migration.MigrationForm;
import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.instance.TBoolean;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.project.instance.Project;
import models.laboratory.run.instance.ReadSet;
import models.utils.InstanceConstants;
import play.Logger;
import play.mvc.Result;


public class MigrationTransfertCCRT extends CommonController{

	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmm");


	public static Result migration() throws IOException{
		//TODO Backup Project collection
		//backUpProject();
		//TODO backup ReadSet collection

		BasicDBObject keys = new BasicDBObject();
		keys.put("treatments", 0);
		String errorMessage ="";
		//Get correspondance projectNGL/projectFG
		MigrationForm form = filledFormQueryString(MigrationForm.class);
		Set<String> localProjects = new HashSet<String>();
		BufferedReader reader = null;
		try {
			//Parse file
			reader = new BufferedReader(new FileReader(new File(form.file)));
			//Read header
			String line = "";
			String header = reader.readLine();
			Logger.debug("Header "+header);
			
			while ((line = reader.readLine()) != null) {
				Logger.debug("Line "+line);
				String[] tabLine = line.split("\\t");
				String codeProject = tabLine[0];
				String codeFG = tabLine[1];
				boolean localDataDeleted = Boolean.parseBoolean(tabLine[2]);
				PropertySingleValue propLocalDataDeleted = new PropertySingleValue(localDataDeleted);
				if(codeProject!=null && codeFG!=null && codeFG.startsWith("fg")){
					//Get project in DB
					Project project = MongoDBDAO.findByCode(InstanceConstants.PROJECT_COLL_NAME, Project.class, codeProject);
					if(project!=null){
						Logger.debug("Project "+project.code);
						MongoDBDAO.update(InstanceConstants.PROJECT_COLL_NAME, Project.class,DBQuery.is("code", project.code),
											DBUpdate.set("bioinformaticParameters.fgGroup",codeFG).set("bioinformaticParameters.fgPriority", 0).set("bioinformaticParameters.localDataDelete", localDataDeleted));
						
						/*//Get ReadSet to update isSentCCRT=true et codeProject=
						List<ReadSet> readSetsToUpdate = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, 
								DBQuery.is("properties.isSentCCRT.value", true).is("projectCode", project.code),keys).toList();
						for(ReadSet readSet : readSetsToUpdate){
							Logger.debug("ReadSet updated "+readSet.code);
							//Calculate path CCRT
							String pathCCRT=calculateCCRTPath(readSet.path, codeFG);
							MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("code", readSet.code),
									DBUpdate.set("location", "CCRT").set("path", pathCCRT));
							if(localDataDeleted){
								MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("code", readSet.code),
										DBUpdate.set("properties.localDataDeleted", propLocalDataDeleted));
							}
						}*/
						
					}else
						errorMessage+="No code project in DB "+codeProject+"\n";
				}else{
					if(codeProject==null || codeFG==null)
						return badRequest("No code project/project FG "+codeProject+"/"+codeFG);
					else
						localProjects.add(codeProject);
				}
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return badRequest(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			return badRequest(e.getMessage());
		}finally{
			reader.close();
		}

		//Check allReadSet updated
		List<ReadSet> readSetsNotUpdated = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("properties.isSentCCRT.value", true).notEquals("location", "CCRT").notIn("projectCode", localProjects),keys).toList();
		if(readSetsNotUpdated.size()>0){
			errorMessage += readSetsNotUpdated.stream().map(readSet->readSet.projectCode).distinct().collect(Collectors.joining("\n"));
		}
		if(!errorMessage.equals("")){
			return badRequest(errorMessage);
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
	
	private static String calculateCCRTPath(String pathCNG, String groupeFG)
	{
		int index = pathCNG.indexOf("/proj/");
		String lotseq_dir = pathCNG.substring(index + 6);
		Logger.debug("lotseq_dir "+lotseq_dir);
		String pathCCRT = "/ccc/genostore/cont007/"+groupeFG+"/"+groupeFG+"/rawdata/"+lotseq_dir;
		Logger.debug("pathCCRT "+pathCCRT);
		return pathCCRT;
	}


}
