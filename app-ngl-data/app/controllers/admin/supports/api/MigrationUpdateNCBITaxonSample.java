package controllers.admin.supports.api;		

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import services.taxonomy.Taxon;
import services.taxonomy.TaxonomyServices;

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
			samples = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, 
					DBQuery.or(DBQuery.notExists("ncbiScientificName"),
							   DBQuery.notExists("ncbiLineage"), 
					           DBQuery.is("ncbiScientificName", null), 
					           DBQuery.is("ncbiLineage", null),
					           DBQuery.is("ncbiScientificName", TaxonomyServices.defaultErrorMessage), 
					           DBQuery.is("ncbiLineage", TaxonomyServices.defaultErrorMessage))
					           ).toList();						
		} else {
			samples = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.exists("code")).toList();
		}
		ArrayList<String> listTaxonCodes = new ArrayList<String>();
		for (Sample sample : samples) {
			if (!listTaxonCodes.contains(sample.taxonCode)) {
				listTaxonCodes.add(sample.taxonCode);
			}
		}
		Map<String, Taxon> mapTaxons = taxonomyServices.getTaxons(listTaxonCodes);
		logger.debug("migre "+samples.size()+" samples");
		int size = samples.size();
		int nb = 1;
		for(Sample sample : samples){
			logger.debug("Sample code: "+sample.code+" : "+nb+"/"+size);
			Taxon taxon = mapTaxons.get(sample.taxonCode);
			migreSample(sample, taxon);
			nb++;
		}
		logger.info("Migration sample finish");
		return ok("Migration Finish");
	}
	
	
	private void migreSample(Sample sample, Taxon taxon) {
		if(taxon == null) { // normalement ne doit pas arriver car map doit contenir tous les taxonCodes demandés
			taxon = new Taxon(sample.taxonCode);
			taxon.error = true;
			taxon.errorMessage = TaxonomyServices.defaultErrorMessage + taxon.code;
		}
		
		String ncbiScientificName = TaxonomyServices.defaultErrorMessage;
		String ncbiLineage        = TaxonomyServices.defaultErrorMessage;
		
		if(! taxon.error) {
			ncbiScientificName = taxon.scientificName;
			ncbiLineage = taxon.lineage;
		}

		MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME,  
				          Sample.class, 
				          DBQuery.is("code", sample.code), 
				          DBUpdate.set("ncbiScientificName", ncbiScientificName).set("ncbiLineage", ncbiLineage));
		
		if(taxon.error) {
			logger.error(taxon.errorMessage);
			logger.error(taxon.code, "no scientific name");
			logger.error(taxon.code, "no lineage");
		}
		
	}

}
