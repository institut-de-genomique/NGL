package services.taxonomy;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import fr.cea.ig.ngl.NGLApplication;
import play.libs.ws.WSResponse;




public class NCBITaxonomyServices {

	private static final play.Logger.ALogger logger = play.Logger.of(NCBITaxonomyServices.class);

	private static final String URLNCBI = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=taxonomy&retmote=xml";
	private static final int maxSizeUrl = 1900;  // rester avec limite inferieure à 2000.
	static final String errorAkaMessage = "Taxon renvoye par serveur different du taxon demande ";
	public static final String defaultErrorFormat = "Probleme de format pour le taxonCode ";
	public static final String defaultErrorMessage = "Recuperation invalide sur les serveurs NCBI et EBI du taxon ";

	private final NGLApplication app;

	@Inject
	public NCBITaxonomyServices(NGLApplication app) {
		this.app = app;
	}

	
	private String getValue(Document doc, String expression) throws XPathExpressionException {
		XPath xPath =  XPathFactory.newInstance().newXPath();
		//String expression = "/TaxaSet/Taxon/ScientificName";
		//read a string value
		if (doc != null) {
			String value = xPath.compile(expression).evaluate(doc);
			if (StringUtils.isNotBlank(value)) {
				return value;
			}
		}
		return null;				
	}
	
	
	private String getValue(Element eltTaxon, String expression) throws XPathExpressionException {
		XPath xPath =  XPathFactory.newInstance().newXPath();
		//String expression = "ScientificName";
		//read a string value
		if (eltTaxon != null) {
			String value = xPath.compile(expression).evaluate(eltTaxon);
			if (StringUtils.isNotBlank(value)) {
				return value;
			}
		}
		return null;				
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
		//logger.debug("Recherche du taxon au NCBI pour le code : {}", taxonCode);
		String url = URLNCBI + "&id=" + taxonCode;
		//logger.debug("accessing taxon " + url);
		CompletionStage<WSResponse> homePage = app.ws().url(url).get();
		CompletionStage<Document> xml = homePage.thenApplyAsync(response -> {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			try {
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document doc = db.parse(new InputSource(new StringReader(response.getBody())));
				return doc;
			} catch (ParserConfigurationException | IOException | SAXException e) {
				logger.debug("Probleme lors de la recuperation au NCBI du taxon " +  taxonCode);
				throw new RuntimeException(e);
			}
		});
		try {
			//logger.debug("Creation du ncbi_taxon ", taxonCode);
			Document doc = xml.toCompletableFuture().get();
			Taxon taxon = new Taxon(taxonCode);
			taxon.error = false;
			if (StringUtils.isNotBlank(getValue(doc, "/eFetchResult/ERROR"))) {
				taxon.error = true;
				taxon.errorMessage = "Le taxon " + taxonCode + " est en erreur : " + getValue(doc, "/eFetchResult/ERROR");
				return taxon;
			}
			if (StringUtils.isBlank(getValue(doc, "/TaxaSet/Taxon"))                || 
				StringUtils.isBlank(getValue(doc, "/TaxaSet/Taxon/ScientificName")) ||
				StringUtils.isBlank(getValue(doc, "/TaxaSet/Taxon/Lineage")) ) {	
				taxon.error = true;
				taxon.errorMessage += "Le taxon " + taxonCode + " n'est pas valide sur le serveur";
			} else {
				String taxId = getValue(doc, "/TaxaSet/Taxon/TaxId");
				if( ! taxonCode.equals(taxId)) {	
					taxon.error = true;
					taxon.errorMessage = errorAkaMessage + "(demande=" + taxonCode + " et retour serveur=" + taxId + ")";
				}
				// ajout
				String rank = getValue(doc, "/TaxaSet/Taxon/Rank");
				if(StringUtils.isNotBlank(rank)) {
					if (rank.contains("species") || rank.equals("varietas") || rank.equals("strain")) {
						taxon.submittable = true;
						//logger.debug("xxxxxxxxxxxxxxxx   rank = " + rank + " submittable");
					} else {
						taxon.submittable = false;
						//logger.debug("xxxxxxxxxxxxxxxx   rank = " + rank + " noSubmittable");
					}
				}
				taxon.scientificName = getValue(doc, "/TaxaSet/Taxon/ScientificName");
				taxon.lineage = getValue(doc, "/TaxaSet/Taxon/Lineage");
			}
			return taxon;
		} catch (InterruptedException | ExecutionException | XPathExpressionException e) {
			throw new RuntimeException(e);
		} 

	}

	public  Map<String, Taxon> getTaxons(List<String> taxonCodesInput) {
		List<String> taxonCodes = new ArrayList<String>();
		for (String taxonCode: taxonCodesInput) {
			if(StringUtils.isNotBlank(taxonCode)) {
				taxonCodes.add(taxonCode.trim());
			}
		}
		Map<String, Taxon> mapTaxons = new HashMap<>();   // map des taxons recuperes au ncbi
		if (taxonCodes.size() == 0) {
			//return null;
		}
		if (taxonCodes.size() == 1 ) {
			String taxonCode = taxonCodes.get(0).trim();
			Taxon taxon = getTaxon(taxonCode);
			if( ! mapTaxons.containsKey(taxon.code)) {
				mapTaxons.put(taxonCode, taxon);
			}
			return mapTaxons;
		}
		List<String>uniqTaxonCodes = new ArrayList<String>();
		List<String> uniqTaxonCodesBugFormat =  new ArrayList<String>(); // liste unique des taxonCode ne repondant pas au format attendu

		for(String taxonCode: taxonCodes) {
			if (StringUtils.isNotBlank(taxonCode)) {
				if (taxonCode.equals("-1") || ! taxonCode.matches("^\\d+$")) {
					if(! uniqTaxonCodesBugFormat.contains(taxonCode)) {
						uniqTaxonCodesBugFormat.add(taxonCode);
					}
				} else {
					if(! uniqTaxonCodes.contains(taxonCode)) {
						uniqTaxonCodes.add(taxonCode);
					}
				}
			}
		}
		//logger.debug("Dans getTaxons taxonCodes.size()= " + taxonCodes.size());
		//logger.debug("Dans getTaxons uniqTaxonCodes.size()= " + uniqTaxonCodes.size());

		mapTaxons = bodyGetTaxons(uniqTaxonCodes);
		List<String>uniqTaxonCodes_2 = new ArrayList<String>();
		for (String taxonCode : uniqTaxonCodes) {
			if(! mapTaxons.containsKey(taxonCode) ) {
				if (! uniqTaxonCodes_2.contains(taxonCode)) {
					uniqTaxonCodes_2.add(taxonCode);
				}
			}
		}
		logger.debug("appel supplementaire à getTaxons pour " + uniqTaxonCodes_2.size() + " donnees");
		Map<String, Taxon> mapTaxons_2 = bodyGetTaxons(uniqTaxonCodes_2);   // map des taxons recuperes au ncbi au tour 2
		for(Iterator<Entry<String, Taxon>> iterator = mapTaxons_2.entrySet().iterator(); iterator.hasNext();) { 
			Entry<String, Taxon> entry = iterator.next();
			Taxon taxon = entry.getValue();
			String taxonCode = entry.getKey(); // et non entry.getValue().code car peut etre != de la cle du hash si aka
			mapTaxons.put(taxonCode, taxon);
		}
		
		// ajouter à la map des taxons, les taxons demandes pour un taxonCode mal formatés :
		for (String taxonCode : uniqTaxonCodesBugFormat) {
			Taxon taxon = new Taxon(taxonCode);
			taxon.error = true;
			taxon.errorMessage = defaultErrorFormat + taxonCode;
			if( ! mapTaxons.containsKey(taxonCode)) {
				mapTaxons.put(taxonCode, taxon);
			}
		}
		// Ajouter taxon en erreur si taxon demandé n'apparait pas dans la map des taxons :
		for (String taxonCode : taxonCodes) {
			if(!mapTaxons.containsKey(taxonCode)) {
				//logger.debug("ZZZZZZZZZZZZZZ   ajout " +  taxonCode +  " en erreur dans mapTaxons");
				Taxon taxon = new Taxon(taxonCode);
				taxon.error = true;
				taxon.errorMessage =  defaultErrorMessage + taxonCode;;
				mapTaxons.put(taxonCode, taxon);
			}
		}
		return mapTaxons;
	}


	public List<Taxon> execCmd(String url) { 
		List<Taxon> listTaxons = new ArrayList<Taxon>();
		//logger.debug("accessing taxon " + url);
		CompletionStage<WSResponse> homePage = app.ws().url(url).get();
		CompletionStage<Document> xml = homePage.thenApplyAsync(response -> {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			try {
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document doc = db.parse(new InputSource(new StringReader(response.getBody())));
				return doc;
			} catch (ParserConfigurationException | IOException | SAXException e) {
				throw new RuntimeException(e);
			}
		});
		try {
			Document doc = xml.toCompletableFuture().get();
			final Element racine = doc.getDocumentElement();
			//Affichage de l'élément racine
			//logger.debug("\n*************RACINE************");
			//logger.debug(racine.getNodeName());
			 
			//récupération des taxons dans le document :
			final NodeList racineNoeuds = racine.getChildNodes();
			final int nbRacineNoeuds = racineNoeuds.getLength();
			for (int i = 0; i < nbRacineNoeuds; i++) {
				if(racineNoeuds.item(i).getNodeType() == Node.ELEMENT_NODE) {
					final Element eltTaxon = (Element) racineNoeuds.item(i);
					//Affichage d'un taxon
					//logger.debug("\n*************Taxon************");
					// possible de parser directement le document mais plus compacte et lisible de passer par getValue
//					String taxId="";
//					if (eltTaxon.getElementsByTagName("TaxId").item(0) != null) {
//						taxId =  eltTaxon.getElementsByTagName("TaxId").item(0).getTextContent();
//						//logger.debug("taxId !!!!!!!!!!!!! : " + taxId);
//					}	
					String taxId =  getValue(eltTaxon, "TaxId");
					String scientificName =  getValue(eltTaxon, "ScientificName");
					String lineage =  getValue(eltTaxon, "Lineage");
					String aka =  getValue(eltTaxon, "AkaTaxIds/TaxId");
					String rank = getValue(eltTaxon, "Rank");
					
//					List<String>akaTaxIds = new ArrayList<String>();
//					final Element eltAkaTaxIds = (Element) eltTaxon.getElementsByTagName("AkaTaxIds").item(0);
//					if (eltAkaTaxIds != null) {
//						final NodeList racine_TaxIds = eltAkaTaxIds.getElementsByTagName("TaxId");
//						final int nb_akaTaxId = racine_TaxIds.getLength();
//						for (int j = 0; j<nb_akaTaxId; j++) {
//							//logger.debug("ALERTE ALIAS : taxonId "+ taxId + " avec alias " + getValue(eltAkaTaxIds, "TaxId"));
//							if(StringUtils.isNotBlank(getValue(eltAkaTaxIds, "TaxId"))) {
//								akaTaxIds.add(getValue(eltAkaTaxIds, "TaxId"));
//							}
//						}
//					}
					
					Taxon taxon = new Taxon(taxId);
					taxon.error = false;
					if ( StringUtils.isBlank(taxId) || StringUtils.isBlank(scientificName) || StringUtils.isBlank(lineage) ) {	
						taxon.error = true;
						taxon.errorMessage += "Le taxon " + taxId + " n'est pas valide sur le serveur";
						logger.debug("Error");
					} else {
						taxon.scientificName = scientificName;
						taxon.lineage = lineage;
						if(StringUtils.isNotBlank(aka)) {
							taxon.akaTaxId = aka;
							taxon.error = true;
							taxon.errorMessage=errorAkaMessage + "(demande=" + aka + " et retour serveur=" + taxId + ")";
							logger.debug("hhhhhhhhhhhhhhhhhh         "+ taxon.errorMessage);
						}
					}	
					if(StringUtils.isNotBlank(rank)) {
						taxon.rank = rank;
					}
					listTaxons.add(taxon);
				}				
			}
			return listTaxons;
		} catch (InterruptedException | ExecutionException | XPathExpressionException e) {
			throw new RuntimeException(e);
		} 
	} 
	
	
	private Map<String, Taxon> bodyGetTaxons(List<String> uniqTaxonCodes) {
		String url = URLNCBI + "&id=";
		int initialCountCharUrl = url.length();
		int countCharUrl = url.length();
		int countExec = 0;
		//logger.debug("Dans bodyGetTaxons uniqTaxonCodes.size()= " + uniqTaxonCodes.size());
		
		Map<String, Taxon> mapTaxons = new HashMap<>();   // map des taxons recuperes au ncbi
		int countTaxonInUrl = 0;
		boolean taxonInteret = false;
		for (String taxonCode : uniqTaxonCodes) {	
			url = url + taxonCode + ",";
			countTaxonInUrl++;
			countCharUrl = url.length();

			if (countCharUrl > maxSizeUrl) {
				// derniere virgule dans l'url qui ne gene pas 
				//logger.debug("Recherche des taxons au NCBI url=" +  url);
				if(taxonInteret) {
					logger.debug("url=" + url);
					taxonInteret = false;
				}
				countExec++;
				logger.debug("getTaxons avec "+ countTaxonInUrl + " taxons dans url");
				List<Taxon> listTaxons = execCmd(url);
				int i = 0;
				for(Taxon taxon : listTaxons) {
					if(StringUtils.isNotBlank(taxon.akaTaxId)) {
						if( ! mapTaxons.containsKey(taxon.akaTaxId)) {
							i++;
							mapTaxons.put(taxon.akaTaxId, taxon);
						}
					} else {
						if( ! mapTaxons.containsKey(taxon.code)) {
							i++;
							mapTaxons.put(taxon.code, taxon);
						}
					}
				}
				logger.debug("Recuperation de "+ i + " taxons dans listTaxons");
				// reinitialiser url et count_car :
				url = URLNCBI + "&id=";
				countCharUrl = url.length();
				countTaxonInUrl = 0;

			}
		}
		// correction effet de bord :
		if (countCharUrl > initialCountCharUrl) {
			if(taxonInteret) {
				logger.debug("url dans correctionEffetBord = " + url);
				taxonInteret = false;
			}
			//logger.debug("Dans correction effets de bords : Recherche des taxons au NCBI url=" +  url);
			countExec++;
			//logger.debug("correction effet de bord : getTaxons avec "+ countTaxonInUrl + " taxons dans url");
			List<Taxon> listTaxons = execCmd(url);
			int i = 0;
			for(Taxon taxon : listTaxons) {
				if(StringUtils.isNotBlank(taxon.akaTaxId)) {
					if( ! mapTaxons.containsKey(taxon.akaTaxId)) {
						i++;
						mapTaxons.put(taxon.akaTaxId, taxon);
					}
				} else {
					if( ! mapTaxons.containsKey(taxon.code)) {
						i++;
						mapTaxons.put(taxon.code, taxon);
					}
				}
			}
			logger.debug("Recuperation de "+ i + " taxons dans listTaxons");
		}
		logger.debug("Nombre de connection au NCBI = " + countExec );
		return mapTaxons;
	}

}

