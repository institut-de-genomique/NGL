package services.taxonomy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import fr.cea.ig.ngl.NGLApplication;

public class TaxonomyServices {

	private static final play.Logger.ALogger logger = play.Logger.of(TaxonomyServices.class);

	private final NGLApplication app;
	public static final String defaultErrorMessage = "Recuperation invalide sur les serveurs NCBI et EBI du taxon ";
	public static final String defaultErrorFormat = "Probleme de format pour le taxonCode ";

	private NCBITaxonomyServices ncbi_taxonomyServices;
	private EBITaxonomyServices  ebi_taxonomyServices;


	@Inject
	public TaxonomyServices(NGLApplication app) {
		this.app = app;
		this.ncbi_taxonomyServices = new NCBITaxonomyServices(app);
		this.ebi_taxonomyServices = new EBITaxonomyServices(app);
	}

	public String getErrorAkaMessage() {
		return NCBITaxonomyServices.errorAkaMessage;
	}
	
	// methode mise en publique pour pouvoir etre mocké dans les TU
	public void setNCBITaxonomyServices(NCBITaxonomyServices NCBITaxonomyServices) {
		this.ncbi_taxonomyServices = NCBITaxonomyServices;
	}
	
	// methode mise en publique pour pouvoir etre mocké dans les TU
	public void setEBITaxonomyServices(EBITaxonomyServices EBITaxonomyServices) {
		this.ebi_taxonomyServices = EBITaxonomyServices;
	}
	
	
	// Test unitaires => si modifs, pensez à verifier  project ngl-common puis testOnly fr.cea.ig.ngl.services.TaxonomyServicesTest
	public Taxon getTaxon(String taxonCodeInput) {
		if (taxonCodeInput == null) {
			return null;
		}
		if(StringUtils.isBlank(taxonCodeInput)) {
			return null;
		}
		if (taxonCodeInput.equals("-1")) {
			return null;
		}

		String taxonCode = taxonCodeInput.trim();
	
		if(! taxonCode.matches("^\\d+$")) {
			logger.debug("Presence d'un taxonId avec format non attendu " + taxonCodeInput);
			return null;
		}
		//logger.debug("Recherche du taxon {} dans le cache", taxonCode);
		Taxon taxon = getObjectInCache(taxonCode);
		if (taxon == null) {
			//logger.debug("taxon {} absent du cache ", taxonCode);
			//logger.debug("Recherche du taxon {} au ncbi ", taxonCode);
			taxon = this.ncbi_taxonomyServices.getTaxon(taxonCode);
			if( taxon != null && ! taxon.error) {
				setObjectInCache(taxon, taxonCode);
				return taxon;
			} else {
				//logger.debug("Recherche du taxon {} a l'EBI ", taxonCode);
				Taxon ebiTaxon  = this.ebi_taxonomyServices.getTaxon(taxonCode);	
				if ( ebiTaxon.error ) {
					logger.debug("le taxonId " + taxonCode + " est en erreur sur les sites du NCBI et de l'EBI : " + taxon.errorMessage + ebiTaxon.error);
					setObjectInCache(taxon, taxonCode); // on met preferenciellement le taxon du ncbi dans le cache car info aka utile
					return taxon;
				} else {
					setObjectInCache(ebiTaxon, taxonCode); // on met preferenciellement le taxon de l'ebi valide dans le cache 
					return ebiTaxon;
				}
			}
		} else {
			//logger.debug("Taxon {} present dans le cache ", taxonCode);
			return taxon;
		}
	}

	// Test unitaires => si modifs, pensez à verifier  project ngl-common puis testOnly fr.cea.ig.ngl.services.TaxonomyServicesTest
	public Taxon getEbiTaxon(String taxonCode) {
		return this.ebi_taxonomyServices.getTaxon(taxonCode);				
	}

	
	public Taxon getNCBITaxon(String taxonCode) {
		return this.ncbi_taxonomyServices.getTaxon(taxonCode);				
	}
	
	
	// Retourne la  map des taxons demandés avec les informations du NCBI ou de l'EBI : attention cette map peut contenir des taxons en erreur et peut contenir des taxonId 
	// qui n'ont pas ete explicitement demandés :
	// Le NCBI peut retourner un alias (aka) de taxonId s'il existe plusieurs entrees ou taxon pour une meme donnée.
	// ex https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=taxonomy&retmote=xml&id=2589293 retourne le taxonId 2923461
	// on aura alors dans la map le taxon 2589293 en erreur et le taxon 2923461 qui n'a pas été demandé.
	public Map<String, Taxon> getTaxons(List<String> taxonCodesInput) {
		List<String> taxonCodes = new ArrayList<String>();
		for (String taxonCode: taxonCodesInput) {
			if(StringUtils.isNotBlank(taxonCode)) {
				taxonCodes.add(taxonCode.trim());
			}
		}
		
		if (taxonCodes == null || taxonCodes.size()== 0 ) {
			return null;
		}
		List<String> uniqTaxonCodes = new ArrayList<String>();    // liste unique des taxonCodes à recuperer sur les serveurs 
		                                                          // liste unique des taxonCodes non presents dans le cache

		Map<String, Taxon> mapTaxons = new HashMap<>();           // map des taxons recuperes sur serveur ou dans cache
		Map<String, Taxon> mapTaxonsInCache = new HashMap<>();    // map des taxons recuperes dans cache

//		int countTaxonNCBI    = 0; 
//		int countTaxonEBI     = 0;
//		int countTaxonError   = 0;
//		int countTaxonGood    = 0;
//		int taxonNotfound     = 0;
		//logger.debug("nombre de taxonCode en entree = "+ taxonCodes.size());
		
		for (String taxonCode: taxonCodes) {
			if(StringUtils.isBlank(taxonCode)) {
				continue;
			}
			// Pas besoin de verifier le format de taxonCode, il seront verifiés dans les methodes de ncbi_taxonomyServices et ebi_taxonomyServices
			Taxon taxon = getObjectInCache(taxonCode);
			if (taxon == null) {
				//logger.debug("WWWWWWWWWWWWWWWWWWWWWWWWWWWW taxonCode " + taxonCode +" absent du cache");
				//  ajouter le taxonCode dans la liste des taxons à rechercher sur les serveurs
				if (!uniqTaxonCodes.contains(taxonCode)) {
					uniqTaxonCodes.add(taxonCode.trim());
				}
			} else {
				//logger.debug("XXXXXXXXXXXXXXXXXXXXXXXXXXXX  Recuperation du taxon {} dans le cache", taxonCode);
				// renseigner la map des taxons avec les taxons trouvés dans le cache :
				mapTaxonsInCache.put(taxonCode, taxon); // attention a utiliser taxonCode et non taxon.code car peuvent etre !=
			}
		}
		
		mapTaxons = ncbi_taxonomyServices.getTaxons(uniqTaxonCodes);
		// countTaxonNCBI = mapTaxons.size();
		
		// ajouter les taxons trouvés dans le cache à la map de l'ensemble des taxons :
		for(Iterator<Entry<String, Taxon>> iterator = mapTaxonsInCache.entrySet().iterator(); iterator.hasNext();) { 
			Entry<String, Taxon> entry = iterator.next();
			if( ! mapTaxons.containsKey(entry.getKey())) {
				mapTaxons.put(entry.getKey(), entry.getValue());
			}
		}	

		// Faire la liste des taxons absents du cache et du NCBI :
		List<String> taxonCodesNotNCBI = new ArrayList<String>();
		for(String taxonCode : uniqTaxonCodes) {
			if(! mapTaxons.containsKey(taxonCode) || mapTaxons.get(taxonCode).error) {
				taxonCodesNotNCBI.add(taxonCode);
			}
		}
		
		//logger.debug("XXXXXXXXXX  XXXXXX  XXXXXXXXXX  Nombre de taxons non trouves dans la requete par liste au NCBI = "+ taxonCodesNotNCBI.size());
		
		// Recuperer à l'EBI les taxons absents du NCBI:
		for(String taxonCode : taxonCodesNotNCBI) {
			Taxon taxon  = ebi_taxonomyServices.getTaxon(taxonCode);
			if(taxon != null && ! taxon.error) {
				mapTaxons.put(taxonCode, taxon); // on ecrase valeur du NCBI si existe mais est en erreur
				//countTaxonEBI++;
			}  else {
				if( ! mapTaxons.containsKey(taxonCode)) { // on garde valeur NCBI meme si en erreur car peut contenir aka
					mapTaxons.put(taxonCode, taxon);
				}
			}	
		}	
		
		// mettre le cache à jour pour tous les taxons trouves sur le serveur ou dans le cache :
		for(Iterator<Entry<String, Taxon>> iterator = mapTaxons.entrySet().iterator(); iterator.hasNext();) { 
			Entry<String, Taxon> entry = iterator.next();
			//logger.debug("AAAAAAAAAAAAAAAAAAA         Mise dans le cache de " + entry.getKey());
			setObjectInCache(entry.getValue(), entry.getKey()); // attention à ne pas mettre entry.getValue().code car peut etre != si aka
		}
		// Ajouter taxon en erreur si taxon demandé n'apparait pas dans la map des taxons :
		for (String taxonCode : taxonCodes) {
			if(!mapTaxons.containsKey(taxonCode)) {
				//logger.debug("ZZZZZZZZZZZZ   ajout " + taxonCode +  " en erreur dans mapTaxons");
				Taxon taxon = new Taxon(taxonCode);
				taxon.error = true;
				taxon.errorMessage =  defaultErrorFormat + taxonCode;;
				mapTaxons.put(taxonCode, taxon);
			}
		}
		return mapTaxons;
	}

	private static String key(String code) {
		return Taxon.class.toString() + "." + code;
	}

	private Taxon getObjectInCache(String code) {
		if (code != null) {
			return app.cache().<Taxon>get(key(code));
		} else {
			return null;
		}		
	}

	private void setObjectInCache(Taxon o, String code) {
		if (o != null && code != null) {
			app.cache().set(key(code), o, 60 * 60 * 24); // 24h
		}		
	}

}
