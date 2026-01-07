package services.taxonomy;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;

import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.JsonNode;

import fr.cea.ig.ngl.NGLApplication;
import play.Logger;
import play.libs.ws.WSResponse;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.util.concurrent.locks.*;




public class NCBITaxonomyServices {

	private static final play.Logger.ALogger logger = play.Logger.of(NCBITaxonomyServices.class);

	private static final String URLNCBI = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=taxonomy&retmote=xml";
	private static final int maxSizeUrl = 1900;  // rester avec limite inferieure à 2000.
	static final String errorAkaMessage = "Taxon renvoye par serveur different du taxon demande ";
	public static final String defaultErrorFormat = "Probleme de format pour le taxonCode ";
	public static final String defaultErrorMessage = "Recuperation invalide sur les serveurs NCBI et EBI du taxon ";
	private static final String URLNCBICOLLABSCIENTIFICNAME = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=taxonomy&term=";
	private static final Map<String, Future<String>> taxonCache = new ConcurrentHashMap<>();
    private static final ExecutorService apiExecutor = Executors.newFixedThreadPool(3); // Limite à 3 requêtes simultanées
    private static final Semaphore apiLimiter = new Semaphore(3); // Max 3 appels simultanés
	private static final String URLEBICOLLABSCIENTIFICNAME = "  https://www.ebi.ac.uk/ena/taxonomy/rest/scientific-name/";
    private static final Queue<String> retryQueue = new ConcurrentLinkedQueue<>();


	private final NGLApplication app;

	@Inject
	public NCBITaxonomyServices(NGLApplication app) {
		this.app = app;
	}

	
	private String getValue(Document doc, String expression) throws XPathExpressionException {
		XPath xPath =  XPathFactory.newInstance().newXPath();

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
		
		if (eltTaxon != null) {
			String value = xPath.compile(expression).evaluate(eltTaxon);
			if (StringUtils.isNotBlank(value)) {
				return value;
			}
		}

		return null;				
	}
	
	

	public  Map<String, Taxon> getTaxons(List<String> taxonCodesInput) {
		List<String> taxonCodes = new ArrayList<String>();
		for (String taxonCode: taxonCodesInput) {
			if(StringUtils.isNotBlank(taxonCode)) {
				taxonCodes.add(taxonCode.trim());
			}
		}
		Map<String, Taxon> mapTaxons = new HashMap<>();   // map des taxons recuperes au ncbi
		
		if (taxonCodes.size() == 1 ) {
			String taxonCode = taxonCodes.get(0).trim();
			Taxon taxon = getUniqueTaxon(taxonCode);
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
				Taxon taxon = new Taxon(taxonCode);
				taxon.error = true;
				taxon.errorMessage =  defaultErrorMessage + taxonCode;;
				mapTaxons.put(taxonCode, taxon);
			}
		}
		return mapTaxons;
	}


	public List<Taxon> getTaxon(String url) { 

		List<Taxon> listTaxons = new ArrayList<Taxon>();

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
			 
			//récupération des taxons dans le document :
			final NodeList racineNoeuds = racine.getChildNodes();
			final int nbRacineNoeuds = racineNoeuds.getLength();
			for (int i = 0; i < nbRacineNoeuds; i++) {
				if(racineNoeuds.item(i).getNodeType() == Node.ELEMENT_NODE) {
					final Element eltTaxon = (Element) racineNoeuds.item(i);
					
					String taxId =  getValue(eltTaxon, "TaxId");
					String scientificName =  getValue(eltTaxon, "ScientificName");
					String lineage =  getValue(eltTaxon, "Lineage");
					String aka =  getValue(eltTaxon, "AkaTaxIds/TaxId");
					String rank = getValue(eltTaxon, "Rank");
					
					Taxon taxon = new Taxon(taxId);
					taxon.error = false;
					if ( StringUtils.isBlank(taxId) || StringUtils.isBlank(scientificName) || StringUtils.isBlank(lineage) ) {	
						taxon.error = true;
						taxon.errorMessage += "Le taxon " + taxId + " n'est pas valide sur le serveur";
					} else {
						taxon.scientificName = scientificName;
						taxon.lineage = lineage;
						if(StringUtils.isNotBlank(aka)) {
							taxon.akaTaxId = aka;
							taxon.error = true;
							taxon.errorMessage=errorAkaMessage + "(demande=" + aka + " et retour serveur=" + taxId + ")";
						}

										
					Node element = racineNoeuds.item(i);
					NodeList elementList = element.getChildNodes();
					int nbElements = elementList.getLength();
					

					for (int j = 0; j < nbElements; j++) {
						Node childNode = elementList.item(j);

						if(childNode.getNodeType() == Node.ELEMENT_NODE) {
							if (childNode.getNodeName().equals("Rank") && childNode.getTextContent().equals("phylum")) {
								taxon.phylum = scientificName;
							}

							if (childNode.getNodeName().equals("LineageEx")) {
								NodeList lineageList = childNode.getChildNodes();
								int nbElementsLineage = lineageList.getLength();

								for (int k = 0; k < nbElementsLineage; k++) {
									Node childNodeLineage = lineageList.item(k);

									if (childNodeLineage.getNodeName().equals("Taxon")) {
										NodeList taxonList = childNodeLineage.getChildNodes();
										int nbElementsTaxon = taxonList.getLength();
										String scientificNamePhylum = null;
										String scientificNameClass = null;
		
										for (int l = 0; l < nbElementsTaxon; l++) {
											Node childNodeTaxon = taxonList.item(l);

											if (childNodeTaxon.getNodeName().equals("ScientificName")) {
												scientificNamePhylum = childNodeTaxon.getTextContent();
												scientificNameClass = childNodeTaxon.getTextContent();
											}

											if (childNodeTaxon.getNodeName().equals("Rank") && childNodeTaxon.getTextContent().equals("phylum")) {
												if (scientificNamePhylum != null) {
													taxon.phylum = scientificNamePhylum;
												} else {
													Logger.error("Aucun scientific name du phylum n'a été trouvé pour le taxon: "+taxon.code);
												}

											}
											if (childNodeTaxon.getNodeName().equals("Rank") && childNodeTaxon.getTextContent().equals("class")) {
												if (scientificNameClass != null) {
													taxon.classTaxon = scientificNameClass;
												
												} else {
													Logger.error("Aucun scientific name du rang class n'a trouvé pour le taxon: "+taxon.code);
												}

											}
										}


									}
								}
							}
						}
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

	public Taxon getUniqueTaxon(String taxonCodeInput) {
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
		String url = URLNCBI + "&id=" + taxonCode;
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
			Document doc = xml.toCompletableFuture().get();
			Taxon taxon = new Taxon(taxonCode);
			taxon.error = false;
			if (StringUtils.isNotBlank(getValue(doc, "/eFetchResult/ERROR"))) {
				taxon.error = true;
				taxon.errorMessage = "Le taxon " + taxonCode + " est en erreur : " + getValue(doc, "/eFetchResult/ERROR");
				return taxon;
			}
			if ((StringUtils.isBlank(getValue(doc, "/TaxaSet/Taxon"))               || 
				StringUtils.isBlank(getValue(doc, "/TaxaSet/Taxon/ScientificName")) ||
				StringUtils.isBlank(getValue(doc, "/TaxaSet/Taxon/Lineage")))       &&
				!"superkingdom".equals(getValue(doc, "/TaxaSet/Taxon/Rank"))) {	
				taxon.error = true;
				taxon.errorMessage += "Le taxon " + taxonCode + " n'est pas valide sur le serveur";
			} else if ("superkingdom".equals(getValue(doc, "/TaxaSet/Taxon/Rank")) && (StringUtils.isBlank(getValue(doc, "/TaxaSet/Taxon/TaxId")) || StringUtils.isBlank(getValue(doc, "/TaxaSet/Taxon/ScientificName")))) {
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
					} else {
						taxon.submittable = false;
					}
				}
				taxon.scientificName = getValue(doc, "/TaxaSet/Taxon/ScientificName");

				if ("superkingdom".equals(getValue(doc, "/TaxaSet/Taxon/Rank"))) {
					taxon.lineage = "lineage:superkingdom";
				} else {
					taxon.lineage = getValue(doc, "/TaxaSet/Taxon/Lineage");

					final Element docElement = doc.getDocumentElement();
					
					NodeList elementList = docElement.getChildNodes();
					int nbElements = elementList.getLength();

					for (int j = 0; j < nbElements; j++) {
						Node childNode = elementList.item(j);

						NodeList firstTaxonList = childNode.getChildNodes();
						int taxonlength = firstTaxonList.getLength();
						for (int m = 0; m < taxonlength; m++) {
							Node taxonNode = firstTaxonList.item(m);

							if (taxonNode.getNodeType() == Node.ELEMENT_NODE) {

								if (taxonNode.getNodeName().equals("Rank")
										&& taxonNode.getTextContent().equals("phylum")) {
									taxon.phylum = taxon.scientificName;
								}

								if (taxonNode.getNodeName().equals("LineageEx")) {
									NodeList lineageList = taxonNode.getChildNodes();
									int nbElementsLineage = lineageList.getLength();

									for (int k = 0; k < nbElementsLineage; k++) {
										Node childNodeLineage = lineageList.item(k);

										if (childNodeLineage.getNodeName().equals("Taxon")) {
											NodeList taxonList = childNodeLineage.getChildNodes();
											int nbElementsTaxon = taxonList.getLength();
											String scientificNamePhylum = "";
											String scientificNameClass = "";

											for (int l = 0; l < nbElementsTaxon; l++) {
												Node childNodeTaxon = taxonList.item(l);

												if (childNodeTaxon.getNodeName().equals("ScientificName")) {
													scientificNamePhylum = childNodeTaxon.getTextContent();
													scientificNameClass = childNodeTaxon.getTextContent();
												}

												if (childNodeTaxon.getNodeName().equals("Rank")
														&& childNodeTaxon.getTextContent().equals("phylum")) {
													if (scientificNamePhylum != null) {
														taxon.phylum = scientificNamePhylum;
													} else {
														Logger.error(
																"phylum trouvé mais aucun scientific name trouvé pour le taxon: "
																		+ taxon.code);
													}													
												}
												if (childNodeTaxon.getNodeName().equals("Rank")
														&& childNodeTaxon.getTextContent().equals("class")) {
													if (scientificNameClass != null) {
														taxon.classTaxon = scientificNameClass;
													} else {
														Logger.error(
																"class trouvé mais aucun scientific name trouvé pour le taxon: "
																		+ taxon.code);
													}

												}
											}
										}
									}
								}
							}
						}
					}
				}

				



			}
			return taxon;
		} catch (InterruptedException | ExecutionException | XPathExpressionException e) {
			throw new RuntimeException(e);
		} 

	}
	
	private Map<String, Taxon> bodyGetTaxons(List<String> uniqTaxonCodes) {
		String url = URLNCBI + "&id=";
		int initialCountCharUrl = url.length();
		int countCharUrl = url.length();
		int countExec = 0;
		
		Map<String, Taxon> mapTaxons = new HashMap<>();   // map des taxons recuperes au ncbi
		@SuppressWarnings("unused")
		int countTaxonInUrl = 0;
		boolean taxonInteret = false;
		for (String taxonCode : uniqTaxonCodes) {	
			url = url + taxonCode + ",";
			countTaxonInUrl++;
			countCharUrl = url.length();

			if (countCharUrl > maxSizeUrl) {
				// derniere virgule dans l'url qui ne gene pas 
				if(taxonInteret) {
					logger.debug("url=" + url);
					taxonInteret = false;
				}
				countExec++;

				List<Taxon> listTaxons = getTaxon(url);
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
			countExec++;
			
			List<Taxon> listTaxons = getTaxon(url);
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

	public static String getCachedTaxonWithCollabScientificName(String collabScientificName) {
		try {
            // Vérifier si la valeur est déjà en cache
            if (taxonCache.containsKey(collabScientificName)) {
                return taxonCache.get(collabScientificName).get(); // Attendre le résultat du Future
            }

            // Exécuter l'appel API en arrière-plan si non trouvé
            Future<String> futureTaxon = apiExecutor.submit(() -> getTaxonWithRetry(collabScientificName));
            taxonCache.put(collabScientificName, futureTaxon);
            return futureTaxon.get(); // Attendre la fin de l'exécution

        } catch (Exception e) {
            retryQueue.add(collabScientificName);
            return null;
        }
    }	

	private static String getTaxonWithRetry(String collabScientificName) {
        int maxRetries = 5;
        int delayBetweenRetries = 2000; // Commence avec 2 secondes

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                return getTaxonWithCollabScientificName(collabScientificName);
            } catch (IOException e) {
                if (e.getMessage().contains("429")) { // API surchargée
                    logger.warn("[HTTP 429] Trop de requêtes, tentative " + attempt + "/" + maxRetries);
                    try {
                        Thread.sleep(delayBetweenRetries);
                        delayBetweenRetries *= 2; // Augmente le délai à chaque échec
                    } catch (InterruptedException ignored) {}
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        retryQueue.add(collabScientificName); // Ajoute à la file d'attente des échecs
        return null;
    }

	public static String getTaxonWithCollabScientificName(String collabScientificName) throws IOException, InterruptedException {
        apiLimiter.acquire();
        try {
            String encodedName = URLEncoder.encode(collabScientificName, "UTF-8").replace("+", "%20");
            String requestUrl = URLNCBICOLLABSCIENTIFICNAME + encodedName;

            HttpURLConnection connection = (HttpURLConnection) new URL(requestUrl).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            if (responseCode == 429) {
                throw new IOException("HTTP 429 Too Many Requests");
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            String taxonId = extractTaxonId(response.toString());
       	 	if (taxonId != null && !taxonId.isEmpty()) {
            	return taxonId;
        }

		String ebiUrl = URLEBICOLLABSCIENTIFICNAME + encodedName;
        HttpURLConnection ebiConnection = (HttpURLConnection) new URL(ebiUrl).openConnection();
        ebiConnection.setRequestMethod("GET");
        ebiConnection.setConnectTimeout(5000);
        ebiConnection.setReadTimeout(5000);

        int ebiResponseCode = ebiConnection.getResponseCode();
        if (ebiResponseCode != 200) {
            return null;
        }

        BufferedReader ebiReader = new BufferedReader(new InputStreamReader(ebiConnection.getInputStream()));
        StringBuilder ebiResponse = new StringBuilder();
        while ((line = ebiReader.readLine()) != null) {
            ebiResponse.append(line);
        }
        ebiReader.close();

        return extractTaxonIdFromEbi(ebiResponse.toString());

        } finally {
            apiLimiter.release();
        }
    }
	
	private static String extractTaxonIdFromEbi(String jsonResponse) {
    try {
        JsonNode root = Json.parse(jsonResponse);
        if (root.isArray() && root.size() > 0) {
            JsonNode first = root.get(0);
            JsonNode taxIdNode = first.get("taxId");
            if (taxIdNode != null) {
                return taxIdNode.asText();
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return null;
}

    /**
     * Extrait le TaxonID depuis la réponse XML de l'API
     */
	private static String extractTaxonId(String xmlResponse) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(xmlResponse.getBytes("UTF-8")));

            NodeList idNodes = document.getElementsByTagName("Id");
            if (idNodes.getLength() > 0) {
                return idNodes.item(0).getTextContent();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}


	