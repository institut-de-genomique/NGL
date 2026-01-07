package controllers.archives.api;

import java.util.Date;
import java.util.Optional;

import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;
import org.mongojack.DBUpdate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.mongodb.BasicDBObject;

import controllers.CommonController;
import controllers.authorisation.Permission;
import controllers.history.UserHistory;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.MongoDBResult;
import fr.cea.ig.mongo.MongoStreamer;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.run.instance.ReadSet;
import models.utils.InstanceConstants;
import play.mvc.Result;
import play.mvc.With;
import validation.ContextValidation;

/**
 * Controller that manage the readset archive
 * 
 * @author galbini
 *
 */
public class ReadSets extends CommonController {
	
	private static final play.Logger.ALogger logger = play.Logger.of(ReadSets.class);
	
	
	public static String lines(String... lines) {
		StringBuilder sb = new StringBuilder();
		for (String line : lines) {
			sb.append(line).append('\n');
		}
		return sb.toString();
	}
	
	public static final String helpQuery = lines(
			"help de l'url de consultation archives :",   
			"/api/archives/readsets?archive=2 renvoie la liste des readsets a archiver",
		    "/api/archives/readsets?archive=3 renvoie la liste des readsets a cloner"
			);

	/*
	 * @param archive default 2
	 * @return
	 */
	@With({fr.cea.ig.authentication.Authenticate.class, UserHistory.class})
	@Permission(value={"reading"})
	public Result list(){
		// url recu de la forme : /api/archives/readsets?archive=0
		BasicDBObject keys = new BasicDBObject();
		keys.put("treatments", 0);	
		ContextValidation ctx = ContextValidation.createUndefinedContext(getCurrentUser());

		Integer archive = getArchiveValue(ctx);
		if(ctx.hasErrors()) {
			return badRequest(helpQuery);
		}		
		Query query = buildQuery(archive, ctx);
		if(ctx.hasErrors()) {
			return badRequest(helpQuery);
		}
		logger.debug("query=" + query.toString());
		MongoDBResult<ReadSet> results =  MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, query, keys);
		return MongoStreamer.okStreamUDT(results, r -> { return convertToArchive(archive, r); });
	}

	
	private Archive convertToArchive(Integer archive, ReadSet readSet) {
		if (readSet != null) {
			return createArchive(readSet);
		}
		return null;
	}

	private Integer getArchiveValue(ContextValidation ctx) {
		try {
			// retourne l'entier qui suit le ? dans url:
			// si url recu de la forme : /api/archives/readsets?archive=x&archive=y
			if ( request().queryString().get("archive").length > 1 ) {
				ctx.addError("url ", "appel avec plusieurs parametres archives incompatibles");
			}
			return Integer.valueOf(request().queryString().get("archive")[0]);
		} catch (Exception e) {
			ctx.addError("url probleme : ", e.getMessage());
			return 0;
		}
	}

	private Query buildQuery(Integer archive, ContextValidation ctx) {
		// NGL-4937 puis NGL-5270
		switch (archive) {	
		// requete pour avoir liste des readsets à archiver :
		case 2 : return
		  DBQuery.and(DBQuery.is("dispatch"                        , true),                        // readset sur disque
				      DBQuery.is("properties.localDataCloned.value", true),                        // readset cloné sur disque
				      DBQuery.in("archiveId"                       , "", null, "null"));           // readset non archivé

		// requete pour avoir liste des readsets à cloner :
		case 3 : return
		  DBQuery.and(DBQuery.is("dispatch", true),                                              	 // readset sur disque
				  	  DBQuery.in("archiveId", "", null, "null"),                                     // readset non archivés
				  	  DBQuery.greaterThan("treatments.global.default.usefulSequences.value", 0),     // readset avec sequence (fichiers de sequences sur disque)
				  	  DBQuery.or(DBQuery.is("properties.localDataCloned.value", false),              // readset non cloné
				  			  	 DBQuery.notExists("properties.localDataCloned")));
		
		// readset non cloné sur disque 
		default : ctx.addError("helpUrl", "");
				 return null;
		}
	}

	
	private Archive createArchive(ReadSet readset) {
		Archive archive =  new Archive();
		archive.runCode=readset.runCode;
		archive.projectCode=readset.projectCode;
		archive.readSetCode = readset.code;
		archive.path = readset.path;
		archive.id = readset.archiveId;
		archive.date = readset.archiveDate;
		archive.clonePath = null;
		if(readset.properties != null && readset.properties.get("clonePath") != null) {
			archive.clonePath = (String) readset.properties.get("clonePath").getValue();
		}
		return archive;
	}

	public static class Args {
		public Optional<String> archiveId;
		public Optional<Boolean> localDataCloned;
		public Optional<String> clonePath;
	}
	
	
	@With({fr.cea.ig.authentication.Authenticate.class, UserHistory.class})
	@Permission(value={"writing"})	
	public Result save(String readSetCode) {
		 // curl -X PUT -H "Content-Type: application/json" --user-agent "bot" -d {"localDataCloned=false"} -v "http://.../api/archives/readsets/AUE_APOSW_4_70HTYAAXX"
		 // curl -X PUT -H "Content-Type: application/json" --user-agent "bot" -d {"archiveId=44444444"} -v "http://.../api/archives/readsets/AUE_APOSW_4_70HTYAAXX"
		 // curl -X PUT -H "Content-Type: application/json" --user-agent "bot" -d {"archiveId=44444444", "localDataCloned=true"} -v "http://.../api/archives/readsets/AUE_APOSW_4_70HTYAAXX"
		logger.debug("je passe bien dans controllers.archives.api.save");
		JsonNode json = request().body().asJson();
		final String cleArchiveId           = "archiveId"; 
		final String cleLocalDataCloned     = "localDataCloned"; 
		final String cleClonePath           = "clonePath"; 
		PropertySingleValue localDataCloned = null;
		PropertySingleValue clonePath = null;
		String archiveIdValue = null;
		boolean localDataClonedValue;
		String clonePathValue;

		boolean hasKeyArchiveId = json.has(cleArchiveId);
		boolean hasKeyLocalDataCloned = json.has(cleLocalDataCloned);
		boolean hasKeyClonePath = json.has(cleClonePath);
		logger.debug("hasKeyLocalDataCloned=" + hasKeyLocalDataCloned);
		logger.debug("hasKeyClonePath=" + hasKeyClonePath);
		if( ! hasKeyArchiveId && ! hasKeyLocalDataCloned && ! hasKeyClonePath) {
			return badRequest("Absence dans le body de la cle " + cleArchiveId + " et de la cle "  + cleLocalDataCloned + " et de la cle " + cleClonePath);
		} 	
		
		if(hasKeyArchiveId) {
			if(json.get(cleArchiveId) instanceof NullNode) {
				archiveIdValue = null;
			} else { 
				archiveIdValue = json.get(cleArchiveId).asText();
			}
			logger.debug("archiveIdValue={}", archiveIdValue==null?null:"'"+archiveIdValue+"'");
		}
		
		logger.debug("hasKeyLocalDataCloned=" + hasKeyLocalDataCloned);
		logger.debug("hasKeyClonePath=" + hasKeyClonePath);
		
		if(hasKeyLocalDataCloned) {
			localDataClonedValue = json.get(cleLocalDataCloned).asBoolean();
			localDataCloned = new PropertySingleValue(localDataClonedValue);	
		}
		
		if(hasKeyClonePath) {
			clonePathValue = json.get(cleClonePath).asText().trim();
			clonePath = new PropertySingleValue(clonePathValue);	
		}	
		
		ReadSet readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSetCode);
		if(readSet == null) {
			return notFound();
		}

		Date courantDate = new java.util.Date();
		if ( hasKeyArchiveId ) {
			if (hasKeyLocalDataCloned && hasKeyClonePath) {
				logger.debug("cas archivage et clone et clonePath");
				MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, 
						DBQuery.is("code", readSet.code), 
						DBUpdate.set("properties." + cleLocalDataCloned, localDataCloned)
								.set("properties." + cleClonePath, clonePath)
				        	    .set(cleArchiveId, archiveIdValue)
				        	    .set("archiveDate", courantDate));
			} else if (hasKeyLocalDataCloned) {
				logger.debug("cas archivage et clone");
				MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, 
						DBQuery.is("code", readSet.code), 
						DBUpdate.set("properties." + cleLocalDataCloned, localDataCloned)
				        	.set(cleArchiveId, archiveIdValue)
			        	    .set("archiveDate", courantDate));
			} else {
				logger.debug("cas archivage");
				MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, 
						DBQuery.is("code", readSet.code), 
						DBUpdate.set(cleArchiveId, archiveIdValue)
		        	    .set("archiveDate", courantDate));	
			}
		} else {
			// cas sans archivage
			if (hasKeyLocalDataCloned && hasKeyClonePath) {
				logger.debug("cas clone et clonePath");
				MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, 
						DBQuery.is("code", readSet.code), 
						DBUpdate.set("properties." + cleLocalDataCloned, localDataCloned)
								.set("properties." + cleClonePath, clonePath));
			} else if ( hasKeyLocalDataCloned && ! hasKeyClonePath) {
				logger.debug("cas clone sans clonePath"); //ok teste
				MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, 
						DBQuery.is("code", readSet.code), 
						DBUpdate.set("properties." + cleLocalDataCloned, localDataCloned));			
			} else if ( ! hasKeyLocalDataCloned && hasKeyClonePath) {
				logger.debug("cas clonePath sans clone"); // ok teste
				MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, 
						DBQuery.is("code", readSet.code), 
						DBUpdate.set("properties." + cleClonePath, clonePath));					
			} else {
				logger.error("controllers.archives.api.save => cas non prevu sans archiveId ni localDataCloned ni clonePath");
				return badRequest("cas non prevu, appel de l'url sans parametre pour archivage ou clone");
			}
		}
		return ok();
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
