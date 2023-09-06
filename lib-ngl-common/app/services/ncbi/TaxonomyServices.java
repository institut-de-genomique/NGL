package services.ncbi;

import java.io.IOException;
import java.io.StringReader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import fr.cea.ig.ngl.NGLApplication;
import play.libs.ws.WSResponse;

public class TaxonomyServices {

	private static final play.Logger.ALogger logger = play.Logger.of(TaxonomyServices.class);

	private static final String URLNCBI = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=taxonomy&retmote=xml";

	private final NGLApplication app;

	@Inject
	public TaxonomyServices(NGLApplication app) {
		this.app = app;
	}

	@Deprecated
	public CompletionStage<NCBITaxon> getNCBITaxon(String taxonCode) {
		if (taxonCode != null) {
			logger.debug("Get taxon info for code : {}", taxonCode);
			NCBITaxon taxon = getObjectInCache(taxonCode);
			if (taxon == null) {
				String url = URLNCBI + "&id=" + taxonCode;
				logger.debug("accessing taxon " + url);
				CompletionStage<WSResponse> homePage = app.ws().url(url).get();
				CompletionStage<NCBITaxon> xml = homePage.thenApplyAsync(response -> {
					DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
					try {
						DocumentBuilder db = dbf.newDocumentBuilder();
						Document doc = db.parse(new InputSource(new StringReader(response.getBody())));
						NCBITaxon newTaxon = new NCBITaxon(taxonCode, doc);
						setObjectInCache(newTaxon, taxonCode);
						return newTaxon;
					} catch (ParserConfigurationException | IOException | SAXException e) {
						throw new RuntimeException(e);
					}
				});
				return xml;
			} else {
				logger.debug("found taxon in cache {}", taxonCode);
				return CompletableFuture.completedFuture(taxon);
			}			
		}
		return CompletableFuture.completedFuture(new NCBITaxon());
	}

	/**
	 * To replace getNCBITaxon 
	 * NGL-2533 : bug max connection (en attendant de trouver la bonne procédure pour interroger NCBI)
	 * methode getNCBITaxon met à jour tous les samples uniquement lorsque toutes les requêtes au NCBI sont passées
	 * Si plus de 10 mises à jours de taxon demandées les requêtes n'aboutissent jamais car nb de connexion max atteint
	 * @param taxonCode
	 * @return
	 */
	public NCBITaxon _getNCBITaxon(String taxonCode) {
		if (taxonCode != null) {
			logger.debug("Get taxon info for code : {}", taxonCode);
			NCBITaxon taxon = getObjectInCache(taxonCode);
			if (taxon == null) {
				String url = URLNCBI + "&id=" + taxonCode;
				logger.debug("accessing taxon " + url);
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
					logger.debug("Create NCBI Taxon", taxonCode);
					Document doc = xml.toCompletableFuture().get();
					NCBITaxon newTaxon = new NCBITaxon(taxonCode, doc);
					setObjectInCache(newTaxon, taxonCode);
					return newTaxon;
				} catch (InterruptedException | ExecutionException e) {
					throw new RuntimeException(e);
				} 

			} else {
				logger.debug("found taxon in cache {}", taxonCode);
				return taxon;
			}		
		}
		return null;

	}


	private static String key(String code) {
		return NCBITaxon.class.toString() + "." + code;
	}

	private NCBITaxon getObjectInCache(String code) {
		if (code != null) {
			return app.cache().<NCBITaxon>get(key(code));
		} else {
			return null;
		}		
	}

	private void setObjectInCache(NCBITaxon o, String code) {
		if (o != null && code != null) {
			app.cache().set(key(code), o, 60 * 60 * 24); // 24h
		}		
	}

	@Deprecated
	public String getTaxonomyInfo(String taxonCode, String expression) throws XPathExpressionException {
		if (taxonCode != null && expression != null) {
			logger.debug("Get taxon info for "+expression+" for taxon "+taxonCode);
			CompletionStage<WSResponse> homePage = app.ws().url(URLNCBI+"&id="+taxonCode).get();
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

			String value = null;
			int nbTry = 0;
			while (nbTry < 3 && value == null) {
				try {
					value = getValue(xml, expression);
					logger.debug("value {}", value);
				} catch (TimeoutException e) {
					//Retry connect
					logger.debug("Retry connect NCBI");
					nbTry++;
				}
			}
			if (nbTry == 3)
				logger.error("NCBI Timeout for taxonId "+taxonCode);

			if (StringUtils.isBlank(value))
				return null;
			else
				return value;
		} else {
			return null;
		}
	}

	// There is no information about why this should be deprecated and what is the call to substitute.
	// @Deprecated
	// public static String getValue(Promise<Document> xml, String expression) throws XPathExpressionException, RuntimeException, TimeoutException	{
	public static String getValue(CompletionStage<Document> xml, String expression) throws XPathExpressionException, RuntimeException, TimeoutException	{
		try {
			Document doc = xml.toCompletableFuture().get();
			XPath xPath =  XPathFactory.newInstance().newXPath();
			return xPath.compile(expression).evaluate(doc);
		} catch (ExecutionException | InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	// There is no information about why this should be deprecated and what is the call to substitute.
	// @Deprecated
	public String getScientificName(String taxonCode) {
		try {
			return getTaxonomyInfo(taxonCode, "/TaxaSet/Taxon/ScientificName");
		} catch (XPathExpressionException e) {
			logger.error("Error Xpath /TaxaSet/Taxon/ScientificName "+e.getMessage());
		}
		return null;
	}

	// There is no information about why this should be deprecated and what is the call to substitute.
	// @Deprecated
	public String getLineage(String taxonCode) {
		try {
			return getTaxonomyInfo(taxonCode, "/TaxaSet/Taxon/Lineage");
		} catch (XPathExpressionException e) {
			logger.error("Error Xpath /TaxaSet/Taxon/Lineage"+e.getMessage());
		}
		return null;
	}

}
