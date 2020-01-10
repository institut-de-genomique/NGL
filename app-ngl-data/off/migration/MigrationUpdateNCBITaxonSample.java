package controllers.migration;		

import java.text.SimpleDateFormat;
import java.util.List;

import javax.inject.Inject;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import controllers.CommonController;
import controllers.DocumentController;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.play.migration.NGLContext;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import services.ncbi.TaxonomyServices;
import play.Logger;
import play.mvc.Result;
import services.instance.sample.UpdateSampleNCBITaxonCNS;

/**
 * Update SampleOnContainer on ReadSet
 * @author galbini
 *
 */
public class MigrationUpdateNCBITaxonSample extends DocumentController<Sample> { //CommonController {
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmm");
	private TaxonomyServices taxonomyServices;
	
	@Inject
	public MigrationUpdateNCBITaxonSample(NGLContext ctx, String collectionName, TaxonomyServices taxonomyServices) {
		super(ctx, collectionName, Sample.class);
		this.taxonomyServices = taxonomyServices;
	}



	public /*static*/ Result migration(String code, Boolean onlyNull){

		Logger.info("Migration sample start");
		//backupSample(code);
		List<Sample> samples = null;
		if(!"all".equals(code)){
			samples = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.is("code",code)).toList();						
		}else if(onlyNull.booleanValue()){
			samples = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.or(DBQuery.notExists("ncbiScientificName"),DBQuery.notExists("ncbiLineage"), 
																			DBQuery.is("ncbiScientificName", null), DBQuery.is("ncbiLineage", null))).toList();						
		}else {
			samples = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.exists("code")).toList();
		}
		Logger.debug("migre "+samples.size()+" samples");
		int size = samples.size();
		int nb = 1;
		for(Sample sample : samples){
			Logger.debug("Sample code: "+sample.code+" : "+nb+"/"+size);
			migreSample(sample);
			nb++;
		}
		Logger.info("Migration sample finish");
		return ok("Migration Finish");

	}



	private /*static*/ void migreSample(Sample sample) {
		String ncbiScientificName=null;
		String ncbiLineage=null;
		if(play.Play.application().configuration().getString("institute").equals("CNS")){
			ncbiScientificName=taxonomyServices.getScientificName(sample.taxonCode);
			ncbiLineage=taxonomyServices.getLineage(sample.taxonCode);
		}else{
			ncbiScientificName=taxonomyServices.getScientificName(sample.taxonCode);
			ncbiLineage=taxonomyServices.getLineage(sample.taxonCode);
		}
		MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME,  Sample.class, 
				DBQuery.is("code", sample.code), DBUpdate.set("ncbiScientificName", ncbiScientificName).set("ncbiLineage", ncbiLineage));
		if(ncbiScientificName==null)
			Logger.error("no scientific name "+ncbiScientificName);
	}

	private static void backupSample(String code) {
		String backupName = InstanceConstants.SAMPLE_COLL_NAME+"_BCK_NCBI_"+sdf.format(new java.util.Date());
		Logger.info("\tCopie "+InstanceConstants.SAMPLE_COLL_NAME+" to "+backupName+" start");
		List<Sample> samples = null;
		if(!"all".equals(code)){
			samples = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.is("code",code)).toList();						
		}else{
			samples = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.exists("code")).toList();						
		}

		MongoDBDAO.save(backupName, samples);
		Logger.info("\tCopie "+InstanceConstants.SAMPLE_COLL_NAME+" to "+backupName+" end");

	}



}
