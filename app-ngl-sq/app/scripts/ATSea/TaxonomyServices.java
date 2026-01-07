package scripts.ATSea;

import java.util.Iterator;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import fr.cea.ig.ngl.NGLApplication;
import play.libs.ws.WSBodyReadables;
import scripts.ATSea.model.TaxonInfo;


public class TaxonomyServices {

	private static final play.Logger.ALogger logger = play.Logger.of(TaxonomyServices.class);
	//String URL = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=taxonomy&term="+taxonName.replaceAll(" ", "+")+"[ScientificName]";
	private static final String URLNCBI = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=taxonomy";
	//https://goat.genomehubs.org/api/v2/search?query=tax_tree%28Alsidium%20corallinum%29&result=taxon&taxonomy=ncbi&includeEstimates=true
	//private static final String URLGOAT = "https://goat.genomehubs.org/api/v2/search?result=taxon&taxonomy=ncbi&includeEstimates=true";
	
	private static final String URLGOAT = "https://goat.genomehubs.org/api/v2/search?";


	private final NGLApplication app;

	@Inject
	public TaxonomyServices(NGLApplication app) {
		this.app = app;
	}


	public TaxonInfo getTaxon(String taxonName) throws InterruptedException, ExecutionException {
		if (taxonName != null) {
			logger.debug("Get taxon info for code : {}", taxonName);
			//	NCBITaxon taxon = getObjectInCache(taxonCode);
			//	if (taxon == null) {
			//String url = URLNCBI + "&term="+taxonName.replaceAll(" ","+")+"[ScientificName]";
			String url = URLGOAT+"&query=tax_tree%28"+taxonName.replaceAll(" ", "%20")+"%29&result=taxon&taxonomy=ncbi&includeEstimates=true";
			logger.debug("accessing taxon " + url);
			//CompletionStage<WSResponse> homePage = app.ws().url(url).get();


			CompletionStage<JsonNode> jsonPromise = app.ws().url(url).get().thenApply(r -> r.getBody(WSBodyReadables.instance.json()));

			JsonNode node = jsonPromise.toCompletableFuture().get();
			//Get JsonNode results
			JsonNode nodeResultsArray = node.get("results");
			ArrayNode nodeResults = (ArrayNode) node.get("results");
			Iterator<JsonNode> nodeResultIterator = nodeResults.iterator();
			//GET first result
			JsonNode firstResult = nodeResultIterator.next();
			//Get result
			JsonNode result = firstResult.get("result");
			String taxonId = result.get("taxon_id").toString();
			String genomeSize = result.get("fields").get("genome_size").get("value").toString();
			
			//TODO create an object and convert to the object
			//JsonNode json = Json.toJson(transformMap);
			//T input = Json.fromJson(json, clazz);
		
			TaxonInfo taxonInfo = new TaxonInfo(taxonName, Integer.parseInt(taxonId), Long.parseLong(genomeSize));
			return taxonInfo;
		}else
			return null;

	}



}
