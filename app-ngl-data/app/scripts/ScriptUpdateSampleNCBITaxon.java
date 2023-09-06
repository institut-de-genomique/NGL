package scripts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;
import com.mongodb.BasicDBObject;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.Script;
import models.Constants;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import services.taxonomy.Taxon;
import services.taxonomy.TaxonomyServices;


// permet de mettre à jour une liste de sampleCodes donnee en argument pour les informations de taxon
//http://localhost:9000/scripts/run/scripts.ScriptUpdateSampleNCBITaxon?code=AAD_CA,AADEA
public class ScriptUpdateSampleNCBITaxon extends Script<ScriptUpdateSampleNCBITaxon.Args>{

	public static class Args {
		//Sample code
		public String code;
	}

	private TaxonomyServices taxonomyServices;
	private static final play.Logger.ALogger logger = play.Logger.of(ScriptUpdateSampleNCBITaxon.class);

	@Inject
	public ScriptUpdateSampleNCBITaxon(TaxonomyServices taxonomyServices) {
		this.taxonomyServices=taxonomyServices;
	}

	@Override
	public void execute(Args args) throws Exception {
		BasicDBObject keys = new BasicDBObject();
		keys.put("code", 1);
		keys.put("taxonCode", 1);
		List<Sample> samplesToUpdate = new ArrayList<Sample>();
		
		//logger.debug("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX      args.code=" + args.code);
		
		// creer une liste unique des sampleCodes :
		List<String> sampleCodes = Arrays.asList(args.code.split(","));
		sampleCodes = sampleCodes.stream()
                .collect(Collectors.toSet())
                .stream()
                .collect(Collectors.toList());
		
		logger.info("Nombre d'update de sample distincts demandes = " + sampleCodes.size());
		printfln("Nombre d'update de sample distincts demandes = " + sampleCodes.size());

		for(String sampleCode : sampleCodes) {
			Sample sampleToUpdate = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, sampleCode);
			if(sampleToUpdate==null) {
				logger.error("sample absent de la base pour '" + sampleCode + "'");
				printfln("sample absent de la base pour '" + sampleCode + "'");
				continue;
			} 
			if(StringUtils.isBlank(sampleToUpdate.taxonCode)) {
				logger.error("sample sans taxonCode dans la base pour " + sampleToUpdate.code);
				printfln("sample sans taxonCode dans la base pour " + sampleToUpdate.code);
				continue;
			}
			samplesToUpdate.add(sampleToUpdate);
		}

		Map<String, List<Sample>> samplesByTaxon = samplesToUpdate.stream().collect(Collectors.groupingBy(sample -> sample.taxonCode));
		//NGL-3902 :
		ArrayList<String> listTaxonCodes = new ArrayList<String>();
		listTaxonCodes.addAll(samplesByTaxon.keySet());
		logger.debug("Recherche sur les serveurs des " + listTaxonCodes.size() + " taxons");
		printfln("Recherche sur les serveurs des " + listTaxonCodes.size() + " taxons");

		Map<String, Taxon> mapTaxons = taxonomyServices.getTaxons(listTaxonCodes);
		
		for(String keyTaxonCode : samplesByTaxon.keySet()){
			Taxon taxon = mapTaxons.get(keyTaxonCode); 
			String ncbiScientificName = TaxonomyServices.defaultErrorMessage; 
			String ncbiLineage = TaxonomyServices.defaultErrorMessage;
			//Taxon taxon = taxonomyServices.getTaxon(keyTaxonCode);
			if(taxon == null) { // taxon ne doit pas etre null, car si non trouvé lors de l'interrogation des serveurs,
				                // ajout dans la mapTaxons du taxon en erreur
				taxon = new Taxon(keyTaxonCode);
				taxon.error = true;
				taxon.errorMessage = TaxonomyServices.defaultErrorMessage + taxon.code;
			} else if (taxon.error) {
				// on laisse lineage et scientificName avec message d'erreur
			} else {
				ncbiScientificName = taxon.scientificName;
				ncbiLineage        = taxon.lineage;
			}
			if(taxon.error) {
				String errorMessage = " pour les sample.code suivants: ";
				for(Sample sample : samplesByTaxon.get(keyTaxonCode)) {
					errorMessage += sample.code + ",";
				}
				logger.error(taxon.errorMessage + errorMessage);
				printfln(taxon.errorMessage + errorMessage);

			}
			DBUpdate.Builder builder = DBUpdate.set("traceInformation.modifyDate",new Date() ).set("traceInformation.modifyUser",Constants.NGL_DATA_USER);

			builder.set("ncbiScientificName", ncbiScientificName);
			builder.set("ncbiLineage", ncbiLineage);
			
			// important d'avoir keyTaxonCode et non taxon.code car taxon.code != de keyTaxonCode si alias ou aka de taxonId :
			samplesByTaxon.get(keyTaxonCode).forEach(sample ->{
				//Logger.info("Update sample taxon info "+sample.code+" / "+taxon.code);
				MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME,  Sample.class, 
						DBQuery.is("code", sample.code), builder);	
			});	
			logger.info("Mise a jour dans base de " + samplesByTaxon.size() + " samples");
			printfln("Mise a jour dans base de " + samplesByTaxon.size() + " samples");
		}
		
		logger.debug("fin de traitement");
		printfln("fin de traitement");
	}
}
