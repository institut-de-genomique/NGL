package controllers.migration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import controllers.CommonController;
import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Treatment;
import models.utils.InstanceConstants;
import play.Logger;
import play.mvc.Result;

public class MigrationReadSetQualityScore extends CommonController{


	public static Result migration(String fileName) throws IOException{

		BufferedReader reader = new BufferedReader(new FileReader(new File(fileName)));
		String line = "";
		while ((line = reader.readLine()) != null) {
			String[] tabLine = line.split(" ");
			String codeReadSet = tabLine[0];
			Double qualityScore = Double.parseDouble(tabLine[1]);
			ReadSet readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, codeReadSet);
			Logger.debug("Get readSet "+readSet);
			Treatment treatment = readSet.treatments.get("ngsrg");
			treatment.results.get("default").put("qualityScore", new PropertySingleValue(qualityScore)); 
			MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class,  
					DBQuery.is("code",readSet.code),
					DBUpdate.set("treatments", readSet.treatments));
		}
		reader.close();
		
		
		return ok("Migration ReadSet qualityScore finished");
	}


}
