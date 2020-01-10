package controllers.migration;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.mongojack.DBCursor;
import org.mongojack.DBQuery;

import com.mongodb.BasicDBObject;

import controllers.CommonController;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.MongoDBResult;
import models.laboratory.common.instance.property.PropertyFileValue;
import models.laboratory.run.instance.ReadSet;
import models.utils.InstanceConstants;
import play.Logger;
import play.Play;
import play.mvc.Result;
import rules.services.RulesServices6;


public class MigrationTaxonomyFungi extends CommonController{
	
	public static Result migration(String code){
		BasicDBObject keys = new BasicDBObject();
		keys.put("code", 1);
		keys.put("treatments.taxonomy", 1);
		MongoDBResult<ReadSet> rsl = null;
		if(code.equals("pairs")){
			rsl = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.exists("treatments.taxonomy.pairs"), keys);
		}else if(code.equals("read1")){
			rsl = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.exists("treatments.taxonomy.read1"), keys);
		}else if(!"all".equals(code)){
			rsl = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("code", code).exists("treatments.taxonomy"), keys);
		}else{
			rsl = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.exists("treatments.taxonomy"), keys);
		}
		RulesServices6 rulesServices = RulesServices6.getInstance();
		Logger.info("Treat "+rsl.size()+" readset");
		DBCursor<ReadSet> cursor = rsl.cursor;
		while(cursor.hasNext()){
			ReadSet rs = cursor.next();
			String krona = null;
			if(rs.treatments.get("taxonomy").results.get("read1")!=null){
				krona=new String(((PropertyFileValue)rs.treatments.get("taxonomy").results.get("read1").get("krona")).value);
			}else if(rs.treatments.get("taxonomy").results.get("pairs")!=null){
				krona=new String(((PropertyFileValue)rs.treatments.get("taxonomy").results.get("pairs").get("krona")).value);
			}
			Pattern p1 = Pattern.compile(".*<node name=\"all\">\\s+<magnitude><val>(\\d+)</val></magnitude>.*", Pattern.DOTALL);
			Pattern p2 = Pattern.compile(".*<node name=\"all\"\\s+magnitude=\"(\\d+)\">.*", Pattern.DOTALL);
			Pattern p3 = Pattern.compile(".*<node name=\"Root\">\\s+(<members><val>\\S+</val>\\s+</members>\\s+)?<count><val>(\\d+)</val></count>.*", Pattern.DOTALL);
			Pattern p4 = Pattern.compile(".*<node name=\"Root\">\\s+(<members>\\s+<vals>(<val>\\S+</val>)*</vals>\\s+</members>\\s+)?<count><val>(\\d+)</val></count>.*", Pattern.DOTALL);
			if(!p1.matcher(krona).matches() && !p2.matcher(krona).matches() && !p3.matcher(krona).matches() && !p4.matcher(krona).matches()){
				Logger.debug("no version krona"+rs.code);
			}
			
			List<Object> facts = new ArrayList<Object>();
			facts.add(rs);				
			rulesServices.callRules(Play.application().configuration().getString("rules.key"), "F_QC_1", facts);
		
			
			//remove keywordBilan
			/*Map<String, PropertyValue> propPairs = rs.treatments.get("taxonomy").results.get("pairs");
			 propPairs.remove("keywordBilan");
			 MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, 
						DBQuery.is("code", rs.code),
						DBUpdate.set("treatments.taxonomy.pairs", propPairs));*/
		}
		Logger.debug("End migration taxonomy fungi");
		return ok();
	}

}