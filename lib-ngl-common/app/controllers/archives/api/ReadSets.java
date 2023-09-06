package controllers.archives.api;

import java.util.Date;

import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;
import org.mongojack.DBUpdate;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObject;

import controllers.CommonController;
import controllers.authorisation.Permission;
import controllers.history.UserHistory;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.MongoDBResult;
import fr.cea.ig.mongo.MongoStreamer;
import models.laboratory.run.instance.ReadSet;
import models.utils.InstanceConstants;
import play.mvc.Result;
import play.mvc.With;

/**
 * Controller that manage the readset archive
 * 
 * @author galbini
 *
 */
public class ReadSets extends CommonController {
	
	private static final play.Logger.ALogger logger = play.Logger.of(ReadSets.class);
	
	/*
	 * @param archive default 2
	 * @return
	 */
	@With({fr.cea.ig.authentication.Authenticate.class, UserHistory.class})
	@Permission(value={"reading"})
	public Result list(){

		BasicDBObject keys = new BasicDBObject();
		keys.put("treatments", 0);		
		Integer archive = getArchiveValue();
//		List<Archive> archives = new ArrayList<Archive>();
		MongoDBResult<ReadSet> results =  MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, getQuery(archive), keys);		
		return MongoStreamer.okStreamUDT(results, r -> { return convertToArchive(archive, r); });
	}

	private Archive convertToArchive(Integer archive, ReadSet readSet) {
		if (readSet != null) {
			if ( (archive.intValue() == 0) 
					|| (archive.intValue() == 1 && readSet.archiveId != null) 
					|| (archive.intValue() == 2 && readSet.archiveId == null) ) {
				return createArchive(readSet);
			}
		}
		return null;
	}

	private Integer getArchiveValue() {
		try {
			return Integer.valueOf(request().queryString().get("archive")[0]);
		} catch (Exception e) {
			logger.error(e.getMessage());
			return 2; // default value;
		}
	}

	private Query getQuery(Integer archive) {
		Query query = null;
		if (archive.intValue() == 0) { //all
			query = DBQuery.is("dispatch", true);
		} else if(archive.intValue() == 1) { //archive
			query = DBQuery.and(DBQuery.is("dispatch", true), DBQuery.notEquals("archiveId", null));
		} else { //not archive value = 2
			query = DBQuery.and(DBQuery.is("dispatch", true), DBQuery.is("archiveId",null), DBQuery.notEquals("state.code","UA"));
		}
		return query;
	}

	private Archive createArchive(ReadSet readset) {
		Archive archive =  new Archive();
		archive.runCode=readset.runCode;
		archive.projectCode=readset.projectCode;
		archive.readSetCode = readset.code;
		archive.path = readset.path;
		archive.id = readset.archiveId;
		archive.date = readset.archiveDate;

		return archive;
	}

	@With({fr.cea.ig.authentication.Authenticate.class, UserHistory.class})
	@Permission(value={"writing"})	
	public Result save(String readSetCode) {
		JsonNode json = request().body().asJson();
		String archiveId = json.get("archiveId").asText();		
		if (archiveId != null) {
			ReadSet readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSetCode);

			if(readSet == null) {
				return notFound();
			}
			
			if (readSet.code.equals(readSetCode)) {
				MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("code", readSet.code), DBUpdate.set("archiveId", archiveId).set("archiveDate", new Date()));
				return ok();
			}
			else {
				return notFound();
			}
			
		}
		else{
			return badRequest();
		}
	}
	
	@With({fr.cea.ig.authentication.Authenticate.class, UserHistory.class})
	@Permission(value={"writing"})
	public Result delete(Integer i){
		
		if(i % 2 == 0){
			return notFound();
		}
		
		return ok();
	}
}
