package controllers.admin.supports.api;		

import java.util.List;

import javax.inject.Inject;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import controllers.DocumentController;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.NGLConfig;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import play.mvc.Result;
import services.ncbi.TaxonomyServices;

/**
 * Update SampleOnContainer on ReadSet
 * 
 * @author galbini
 *
 */
public class MigrationUpdateNCBITaxonSample extends DocumentController<Sample> {
	
	private static final play.Logger.ALogger logger = play.Logger.of(MigrationUpdateNCBITaxonSample.class);
	
	private final TaxonomyServices taxonomyServices;
//	private final NGLConfig config;
	
//	@Inject
//	public MigrationUpdateNCBITaxonSample(NGLContext ctx, TaxonomyServices taxonomyServices, NGLConfig config) {
//		super(ctx, InstanceConstants.SAMPLE_COLL_NAME, Sample.class);
//		this.taxonomyServices = taxonomyServices;
//		this.config = config;
//	}

//	@Inject
//	public MigrationUpdateNCBITaxonSample(NGLApplication ctx, TaxonomyServices taxonomyServices, NGLConfig config) {
//		super(ctx, InstanceConstants.SAMPLE_COLL_NAME, Sample.class);
//		this.taxonomyServices = taxonomyServices;
//		this.config = config;
//	}

	@Inject
	public MigrationUpdateNCBITaxonSample(NGLApplication ctx, TaxonomyServices taxonomyServices, NGLConfig config) {
		super(ctx, InstanceConstants.SAMPLE_COLL_NAME, Sample.class);
		this.taxonomyServices = taxonomyServices;
	}

	public Result migration(String code, Boolean onlyNull) {
		logger.info("Migration sample start");
		//backupSample(code);
		List<Sample> samples = null;
		if(!"all".equals(code)) {
			samples = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.is("code",code)).toList();						
		} else if(onlyNull.booleanValue()) {
			samples = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.or(DBQuery.notExists("ncbiScientificName"),DBQuery.notExists("ncbiLineage"), 
																			DBQuery.is("ncbiScientificName", null), DBQuery.is("ncbiLineage", null))).toList();						
		} else {
			samples = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.exists("code")).toList();
		}
		logger.debug("migre "+samples.size()+" samples");
		int size = samples.size();
		int nb = 1;
		for(Sample sample : samples){
			logger.debug("Sample code: "+sample.code+" : "+nb+"/"+size);
			migreSample(sample);
			nb++;
		}
		logger.info("Migration sample finish");
		return ok("Migration Finish");
	}

//	private void migreSample(Sample sample) {
//		String ncbiScientificName = null;
//		String ncbiLineage        = null;
//		
////		if(play.Play.application().configuration().getString("institute").equals("CNS")){
//		if(config.getInstitute().equals("CNS")) {
//			ncbiScientificName = taxonomyServices.getScientificName(sample.taxonCode);
//			ncbiLineage        = taxonomyServices.getLineage(sample.taxonCode);
//		} else {
//			ncbiScientificName = taxonomyServices.getScientificName(sample.taxonCode);
//			ncbiLineage        = taxonomyServices.getLineage(sample.taxonCode);
//		}
//		MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME,  
//				          Sample.class, 
//				          DBQuery.is("code", sample.code), 
//				          DBUpdate.set("ncbiScientificName", ncbiScientificName).set("ncbiLineage", ncbiLineage));
//		if (ncbiScientificName == null) {
//			logger.error("no scientific name {}",ncbiScientificName);
//		}
//	}
	
	private void migreSample(Sample sample) {
		String ncbiScientificName = taxonomyServices.getScientificName(sample.taxonCode);
		String ncbiLineage        = taxonomyServices.getLineage(sample.taxonCode);
		
		MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME,  
				          Sample.class, 
				          DBQuery.is("code", sample.code), 
				          DBUpdate.set("ncbiScientificName", ncbiScientificName).set("ncbiLineage", ncbiLineage));
		if (ncbiScientificName == null) 
			logger.error("no scientific name {}",ncbiScientificName);
	}

//	private /*static*/ void backupSample(String code) {
//		String backupName = InstanceConstants.SAMPLE_COLL_NAME+"_BCK_NCBI_"+sdf.format(new java.util.Date());
//		Logger.info("\tCopie "+InstanceConstants.SAMPLE_COLL_NAME+" to "+backupName+" start");
//		List<Sample> samples = null;
//		if(!"all".equals(code)) {
//			samples = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.is("code",code)).toList();						
//		} else {
//			samples = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.exists("code")).toList();						
//		}
//		MongoDBDAO.save(backupName, samples);
//		Logger.info("\tCopie "+InstanceConstants.SAMPLE_COLL_NAME+" to "+backupName+" end");
//	}

}
