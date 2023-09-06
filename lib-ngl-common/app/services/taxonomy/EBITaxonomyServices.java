package services.taxonomy;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import javax.inject.Inject;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;

import fr.cea.ig.ngl.NGLApplication;
import play.libs.Json;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;


public class EBITaxonomyServices {

	private static final play.Logger.ALogger logger = play.Logger.of(EBITaxonomyServices.class);
	private static final String URL = "https://www.ebi.ac.uk/ena/taxonomy/rest/tax-id/"; // garder slash terminal car concatenation avec taxonId
	private final NGLApplication app;
	public static final String defaultErrorFormat = "Probleme de format pour le taxonCode ";

	@Inject
	public EBITaxonomyServices(NGLApplication app) {
		this.app = app;
	}

	public Taxon getTaxon(String taxonCodeInput) {
		if (taxonCodeInput == null) {
			return null;
		}
		if(StringUtils.isBlank(taxonCodeInput)) {
			return null;
		}

		String taxonCode = taxonCodeInput.trim();
	
		if(taxonCodeInput.equals("-1") || ! taxonCode.matches("^\\d+$")) {
			logger.debug("Presence d'un taxonId avec format non attendu " + taxonCodeInput);
			Taxon taxon = new Taxon(taxonCode);
			taxon.error = true;
			taxon.errorMessage = defaultErrorFormat + taxonCode;
			return taxon;
		}
		
		//logger.debug("Recherche du taxon a l'EBI pour le code : {}", taxonCode);
		String url = URL + taxonCode;
		WSRequest wr = app.ws().url(url);
		//wr.setAuth("Webin-9", "Axqw16nI");
		CompletionStage<WSResponse> homePage = wr.get();
		CompletionStage<String> str_ebiTaxonInfo = homePage.thenApplyAsync(response -> {
			//logger.info("response "+response.getBody());
			return response.getBody();
		});

		try {
			//logger.debug("Creation de l'ebi_taxon ", taxonCode);
			String ebiTaxonInfo = str_ebiTaxonInfo.toCompletableFuture().get();
			Taxon taxon = new Taxon(taxonCode);
			taxon.error = false;
			if(StringUtils.isBlank(ebiTaxonInfo)) {
				taxon.error = true;
				taxon.errorMessage = "Le taxon " + taxonCode + " n'est pas present sur le serveur ";
				return taxon;
			}
			JsonNode ebiJsonNodeTaxon = Json.parse(ebiTaxonInfo);
			if(ebiJsonNodeTaxon.has("error")) {
				taxon.error = true;
				String error = ebiJsonNodeTaxon.get("error").asText();
				taxon.errorMessage = "Le taxon " + taxonCode + " est en erreur sur le serveur : ";
				if(StringUtils.isNotBlank(error)) {
					taxon.errorMessage = "Le taxon " + taxonCode + " est en erreur sur le serveur : " + error;
				} else {
					taxon.errorMessage = "Le taxon " + taxonCode + " est en erreur sur le serveur : ";
				}
				return taxon;						
			}
			if(	!ebiJsonNodeTaxon.has("taxId")          || 
					!ebiJsonNodeTaxon.has("submittable")    ||
					!ebiJsonNodeTaxon.has("scientificName") || 
					!ebiJsonNodeTaxon.has("lineage")) {
				taxon.error = true;
				taxon.errorMessage = "Le taxon " + taxonCode + " n'est pas valide sur le serveur";
			} else {
				taxon.lineage = ebiJsonNodeTaxon.get("lineage").asText();
				taxon.scientificName = ebiJsonNodeTaxon.get("scientificName").asText();
				if (ebiJsonNodeTaxon.get("submittable").asText().equals("true")) {
					taxon.submittable = true;
					//							if(taxon.submittable) {
					//								logger.debug("taxon.submittable=true");
					//							} else {
					//								logger.debug("taxon.submittable=false");
					//
					//							}
				} else {
					taxon.submittable = false;
				}
			}
			return taxon;
		} catch (InterruptedException | ExecutionException | RuntimeException e) {
			Taxon taxon =  new Taxon(taxonCode);
			taxon.error = true;
			taxon.errorMessage = "Le taxon " + taxonCode + " est en erreur sur le serveur : " + e.getMessage();
			return taxon;
			//throw new RuntimeException(e);
		} 

	}




}

