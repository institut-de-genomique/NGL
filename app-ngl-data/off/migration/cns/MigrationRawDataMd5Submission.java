package controllers.migration.cns;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mongojack.DBQuery;

import controllers.CommonController;
import controllers.migration.MigrationForm;
import fr.cea.ig.MongoDBDAO;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.sra.instance.RawData;
import models.utils.InstanceConstants;
import play.Logger;
import play.mvc.Result;

public class MigrationRawDataMd5Submission extends CommonController{

	public static Result migration(String projectCode) throws IOException{
		MigrationForm form = filledFormQueryString(MigrationForm.class);
		Map<String, String> mapMd5 = getMapMd5(form.file);
		
		//Get all experiment from projectCode
		List<Experiment> experiments = MongoDBDAO.find(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.is("projectCode", projectCode)).toList();
		for(Experiment experiment : experiments){
			for(RawData rawData : experiment.run.listRawData){
				//get md5
				String md5 = mapMd5.get(rawData.relatifName);
				rawData.md5=md5;
			}
			MongoDBDAO.update(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, experiment);
		}
		return ok();
	}
	
	
	private static Map<String, String> getMapMd5(String fileName) throws IOException
	{
		Map<String, String> mapMd5 = new HashMap();
		BufferedReader reader = null;
		try {
			//Parse file
			reader = new BufferedReader(new FileReader(new File(fileName)));
			//Read header
			String line = "";
			while ((line = reader.readLine()) != null) {
				Logger.debug("Line "+line);
				String[] tabLine = line.split("  ");
				Logger.debug("tab 0 "+tabLine[0]);
				Logger.debug("tab 1 "+tabLine[1]);
				String md5 = tabLine[0];
				
				String codeRawData = tabLine[1].substring(tabLine[1].lastIndexOf("/")+1);
				Logger.debug("Code rawData "+codeRawData+" = "+md5);
				mapMd5.put(codeRawData, md5);
			}
			return mapMd5;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw e;
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}finally{
			reader.close();
		}
	}
}
