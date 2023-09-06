package controllers.admin.supports.api;	

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.mongojack.DBCursor;
import org.mongojack.DBQuery;

import com.mongodb.BasicDBObject;

import controllers.DocumentController;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.MongoDBResult;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.NGLConfig;
import models.laboratory.run.instance.ReadSet;
import models.utils.InstanceConstants;
import play.mvc.Result;
import rules.services.RulesServices6;

public class MigrationTaxonomyFungi extends DocumentController<ReadSet> {
	
	private static final play.Logger.ALogger logger = play.Logger.of(MigrationTaxonomyFungi.class);
	
	private final NGLConfig config;
	
//	@Inject
//	public MigrationTaxonomyFungi(NGLContext ctx, NGLConfig config) {
//		super(ctx, InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class);
//		this.config = config;
//	}

	@Inject
	public MigrationTaxonomyFungi(NGLApplication app, NGLConfig config) {
		super(app, InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class);
		this.config = config;
	}

	public Result migration(String code){
		BasicDBObject keys = new BasicDBObject();
		keys.put("code", 1);
		keys.put("treatments.taxonomy", 1);
		MongoDBResult<ReadSet> rsl = null;
		if (code.equals("pairs")) {
			rsl = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.exists("treatments.taxonomy.pairs"), keys);
		} else if(code.equals("read1")) {
			rsl = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.exists("treatments.taxonomy.read1"), keys);
		} else if(!"all".equals(code)) {
			rsl = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("code", code).exists("treatments.taxonomy"), keys);
		} else {
			rsl = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.exists("treatments.taxonomy.read1").exists("treatments.taxonomy.pairs"), keys);
		}
		RulesServices6 rulesServices = RulesServices6.getInstance();
		logger.info("Treat {} readsets", rsl.size());
		DBCursor<ReadSet> cursor = rsl.cursor;
		while (cursor.hasNext()) {
			ReadSet rs = cursor.next();
			logger.debug("ReadSet {}", rs.code);
			
			List<Object> facts = new ArrayList<>();
			facts.add(rs);				
//			rulesServices.callRules(Play.application().configuration().getString("rules.key"), "F_QC_1", facts);
			rulesServices.callRules(config.getRulesKey(), "F_QC_1", facts);
		
		}
		logger.debug("End migration taxonomy fungi");
		return ok();
	}

}
