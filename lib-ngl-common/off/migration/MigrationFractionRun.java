package controllers.migration;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.regex.Pattern;

import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import models.utils.InstanceConstants;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import play.Logger;
import play.mvc.Result;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;

import controllers.CommonController;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.MongoDBResult.Sort;


public class MigrationFractionRun extends CommonController{
	
	public static Result migration() throws MongoException, ParseException{
		BasicDBObject keys = new BasicDBObject();
		keys.put("code", 1);
		keys.put("runSequencingStartDate", 1);
		keys.put("runCode", 1);
		keys.put("treatments.ngsrg", 1);
		keys.put("sampleOnContainer", 1);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
		
		List<ReadSet> rsl = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.exists("treatments.ngsrg.default.fraction")
				.greaterThan("runSequencingStartDate", sdf.parse("2014/01/01"))
				.nor(DBQuery.regex("runCode", Pattern.compile("^.*EXT.*$"))), keys)
				.sort("runSequencingStartDate", Sort.DESC).toList();
		DecimalFormat df = new DecimalFormat("#.###");
		df.setRoundingMode(RoundingMode.HALF_EVEN);
		
		
		
		for(ReadSet rs : rsl){
			
			if(null == rs.sampleOnContainer || null == rs.sampleOnContainer.percentage || rs.treatments.get("ngsrg") == null || rs.treatments.get("ngsrg").results().get("default") == null || rs.treatments.get("ngsrg").results().get("default").get("fraction") == null){
				Logger.error("NULL : "+rs.code);
			}else{
				BasicDBObject keys2 = new BasicDBObject();
				keys.put("code", 1);
				keys.put("lanes", 1);
				
				int nbLane = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, DBQuery.is("code",rs.runCode), keys2).lanes.size();
				double th = rs.sampleOnContainer.percentage / nbLane / 100;
				
				if(!df.format(th).equals(df.format((double)rs.treatments.get("ngsrg").results().get("default").get("fraction").value))){
					Logger.info(rs.runCode+" : "+sdf.format(rs.runSequencingStartDate)+" / "+rs.code+" : "+rs.sampleOnContainer.percentage+" / "+df.format((double)rs.treatments.get("ngsrg").results().get("default").get("fraction").value)+" / "+df.format(th));					
				
					MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class,  DBQuery.is("code", rs.code), DBUpdate.set("treatments.ngsrg.default.fraction.value", th));
				}
				
				
			}
		}
		
		return ok();
	}
	

}
