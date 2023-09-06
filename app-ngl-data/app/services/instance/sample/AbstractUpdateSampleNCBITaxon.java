package services.instance.sample;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.ngl.NGLApplication;
import models.Constants;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import models.utils.dao.DAOException;
import rules.services.RulesException;
import services.instance.AbstractImportData;
import services.taxonomy.Taxon;
import services.taxonomy.TaxonomyServices;
import validation.ContextValidation;

public abstract class AbstractUpdateSampleNCBITaxon extends AbstractImportData {

	private TaxonomyServices taxonomyServices;

	@Inject
	public AbstractUpdateSampleNCBITaxon(String name, NGLApplication ctx, TaxonomyServices taxonomyServices) {
		super(name, ctx);
		this.taxonomyServices = taxonomyServices;
	}

	@Override
	public void runImport(ContextValidation contextError) throws SQLException, DAOException, MongoException, RulesException {
		updateSampleNCBI(contextError, null);
	}

	public void updateSampleNCBI(ContextValidation contextError, List<String> sampleCodes) {
		// enlever la cle import qui pollue un peu les log d'erreurs :
		contextError.removeKeyFromRootKeyName("import");

		BasicDBObject keys = new BasicDBObject();
		keys.put("code", 1);
		keys.put("taxonCode", 1);

		List<Sample> samples = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, 
				DBQuery.notEquals("taxonCode", null).
				or(DBQuery.is("ncbiScientificName", null), 
				   DBQuery.is("ncbiScientificName", ""), 
				   DBQuery.is("ncbiScientificName", TaxonomyServices.defaultErrorMessage), 
				   DBQuery.is("ncbiLineage", null), 
				   DBQuery.is("ncbiLineage", ""), 
				   DBQuery.is("ncbiLineage", TaxonomyServices.defaultErrorMessage)),keys).toList();
		
		logger.info("update sample without ncbi data : "+samples.size());
		
		Map<String, List<Sample>> samplesByTaxon = samples.stream().collect(Collectors.groupingBy(sample -> sample.taxonCode));
		// NGL-3902 : 
		ArrayList<String> listTaxonCodes = new ArrayList<String>();
		listTaxonCodes.addAll(samplesByTaxon.keySet());
		Map<String, Taxon> mapTaxons = taxonomyServices.getTaxons(listTaxonCodes);
		
		for(String keyTaxonCode : samplesByTaxon.keySet()){
			logger.debug("Get ncbiTaxon for " + keyTaxonCode);
			
			Taxon taxon = mapTaxons.get(keyTaxonCode);    // taxon ne doit pas etre null, car si non trouvé lors de l'interrogation des serveurs,
			                                           // ajout dans la mapTaxons du taxon en erreur
			String scientificName = TaxonomyServices.defaultErrorMessage;
			String lineage = TaxonomyServices.defaultErrorMessage;; 
			
			if (taxon == null) { // normalement ne doit pas arriver
				taxon = new Taxon(keyTaxonCode);
				taxon.error = true;
				taxon.errorMessage = TaxonomyServices.defaultErrorMessage + taxon.code;
				//contextError.addError(keyTaxonCode, TaxonomyServices.defaultErrorMessage + keyTaxonCode);
			} else if (taxon.error) {
				// on laisse lineage et scientificName avec message d'erreur
			} else {
				scientificName = taxon.getScientificName();
				lineage = taxon.getLineage();
			}
			if (taxon.error) {
				String errorMessage = " pour les sample.codes suivants : ";
				for(Sample sample : samplesByTaxon.get(keyTaxonCode)) {
					errorMessage += sample.code + ",";
				}
				contextError.addError(keyTaxonCode, TaxonomyServices.defaultErrorMessage + keyTaxonCode  + errorMessage);
			}
			DBUpdate.Builder builder = DBUpdate.set("traceInformation.modifyDate",new Date() ).set("traceInformation.modifyUser",Constants.NGL_DATA_USER);
			builder.set("ncbiScientificName", scientificName);
			builder.set("ncbiLineage", lineage);
			
			// important d'avoir keyTaxonCode et non taxon.code car taxon.code != de keyTaxonCode si alias ou aka de taxonId :
			samplesByTaxon.get(keyTaxonCode).forEach(sample ->{
				//Logger.info("Update sample taxon info "+sample.code+" / "+taxon.code);
				MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME,  Sample.class, 
						DBQuery.is("code", sample.code), builder);	
			});		
		}

		logger.debug("finish update");
		
		if (contextError.hasErrors()) {
			contextError.displayErrors(logger);
			logger.error("ImportData End Error");
		} else {
			logger.info("ImportData End");
		}
		// remettre la cle import en sortie de la methode :
		contextError.addKeyToRootKeyName("import");

	}

}
