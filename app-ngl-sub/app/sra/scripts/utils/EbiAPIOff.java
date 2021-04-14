package sra.scripts.utils;

import java.io.IOException;
import java.io.StringReader;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import fr.cea.ig.ngl.NGLConfig;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;

public class EbiAPIOff {
//  Verifier si donnée publique existe à l'EBI : 
//	https://www.ebi.ac.uk/ena/data/view/ERR2196986
//  Verifier si donnée existe dans notre drop-box à l'EBI (publique ou privée) en s'identifiant :
//	https://www.ebi.ac.uk/ena/submit/drop-box/studies/ERP109506?format=xml&auth=ENA%20Webin-9%20Axqw16nI
	
	private static final play.Logger.ALogger logger = play.Logger.of(EbiAPIOff.class);
	private final WSClient ws;
	private final NGLConfig  config;
	// On demande à play d'instancier la classe en lui fournissant un objet WSClient
	@Inject
	public EbiAPIOff(WSClient   ws,
				  NGLConfig  config) {
		this.ws = ws;
		this.config = config;
	}
	
	/**
	 * Verifie si l'objet dont le numeros d'accession est indiqué existe sur le serveur de l'EBI
	 * @param type : type d'objet : projects, studies, samples, experiments, runs ou submission
	 * @param ac   : numeros d'accession
	 * @return     : true si l'AC existe à l'EBI, false sinon 
	 */	
	protected boolean _ebiExists(String ac, String type) {
		String url = String.format("https://www.ebi.ac.uk/ena/submit/drop-box/%s/%s?format=xml", type, ac);
		//logger.debug("ebiExists -- url : {}", url);
		//ws.url(https://www.ebi.ac.uk/ena/submit/drop-box/samples/$ac?format=xml);
		WSRequest wr = ws.url(url);
		String login = config.getString("sraEbiLogin");
		String pw = config.getString("sraEbiPassword");

		wr.setAuth(login, pw);
		
		// l'url devient https://www.ebi.ac.uk/ena/submit/drop-box/studies/ERP111093?format=xml&auth=ENA%20Webin-9%20Axqw16nI
		try {
			WSResponse wre = wr.get().toCompletableFuture().get();
				//logger.debug("Reponse pour le {} avec ac = {} : {}", type, ac, wre.getBody());
			return wre.getStatus() == 200;
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		} 
	}	
	
	/**
	 * Verifie si l'objet dont le numeros d'accession est indiqué existe sur le serveur de l'EBI
	 * @param type : type d'objet : projects, studies, samples, experiments, runs ou submission
	 * @param ac   : numeros d'accession
	 * @return     : true si l'AC existe à l'EBI, false sinon 
	 */	
	protected boolean ebiExists(String ac, String type) {
		int maxTentatives = 3;
		boolean bool = false; 
		while(maxTentatives > 0) {
			bool = _ebiExists(ac, type);
			maxTentatives--;
			if (! bool) {  // 403 : problemes de droits:
				continue;
			} else {
				break;
			}
		}
		return bool;
	}	
	

	
	/**
	 * Renvoie le xml correspondant au numeros d'accession indiqué s'il existe sur le serveur de l'EBI
	 * @param type : type d'objet : projects, studies, samples, experiments, runs ou submission
	 * @param ac   : numeros d'accession
	 * @return     : 
	 */			
	private String _ebiXml(String ac, String type) {
		
		String url = String.format("https://www.ebi.ac.uk/ena/submit/drop-box/%s/%s?format=xml", type, ac);
		//logger.debug("ebiXml -- url : {}", url);
		//ws.url(https://www.ebi.ac.uk/ena/submit/drop-box/samples/$ac?format=xml);
		WSRequest wr = ws.url(url);
		wr.setAuth("Webin-9", "Axqw16nI");
		CompletionStage<WSResponse> homePage = wr.get();		
		CompletionStage<String> xml = 
			homePage.thenApplyAsync(response -> {
			//logger.info("response "+response.getBody());
			return response.getBody();
		});
		try {
			String doc = xml.toCompletableFuture().get();
			return doc;
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}	
	}
	
	/**
	 * Renvoie le xml correspondant au numeros d'accession indiqué s'il existe sur le serveur de l'EBI
	 * @param type : type d'objet : projects, studies, samples, experiments, runs ou submission
	 * @param ac   : numeros d'accession
	 * @return     : 
	 */		
	public String ebiXml(String ac, String type) {
		int maxTentatives = 3;
		String xmlString = null; 
		while(maxTentatives > 0) {
			xmlString = _ebiXml(ac, type);
			maxTentatives--;
			if (xmlString.contains(":403")) { // 403 : problemes de droits:
				continue;
			} else {
				break;
			}
		}
		return xmlString;
	}
	

	/**
	 * Renvoie le xml correspondant au numeros d'accession indiqué s'il existe sur le serveur public de l'EBI
	 * @param type : type d'objet : projects, studies, samples, experiments, runs ou submission
	 * @param ac   : numeros d'accession
	 * @return     : 
	 */			
	public String ebiPublicXml(String ac, String type) {
		String url = String.format("https://www.ebi.ac.uk/ena/data/view/%s?display=xml", type, ac);
		//https://www.ebi.ac.uk/ena/data/view/SRX529512&display=xml
		logger.debug("ebiXml -- url : {}", url);
		WSRequest wr = ws.url(url);
		//wr.setAuth("Webin-9", "Axqw16nI");
		CompletionStage<WSResponse> homePage = wr.get();		
		CompletionStage<String> xml = 
			homePage.thenApplyAsync(response -> {
			logger.info("response "+response.getBody());
			return response.getBody();
		});
		try {
			String doc = xml.toCompletableFuture().get();
			return doc;
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}	
	}

	protected Document ebiDocumentXml(String ac, String type) {
		String url = String.format("https://www.ebi.ac.uk/ena/submit/drop-box/%s/%s?format=xml", type, ac);
		logger.debug("ebiXml -- url : {}", url);
		//ws.url(https://www.ebi.ac.uk/ena/submit/drop-box/samples/$ac?format=xml);
		WSRequest wr = ws.url(url);
		wr.setAuth("Webin-9", "Axqw16nI");
		CompletionStage<WSResponse> homePage = wr.get();
		CompletionStage<Document> xml = 
				homePage.thenApplyAsync(response -> {
					try {
						logger.info("response "+response.getBody());
						//Document d = XML.fromString(response.getBody());
						//Node n = scala.xml.XML.loadString(response.getBody());
						//System.out.println("J'ai une reponse ?"+ n.toString());
						DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
						//dbf.setValidating(false);
						//dbf.setSchema(null);
						DocumentBuilder db = dbf.newDocumentBuilder();
						Document doc = db.parse(new InputSource(new StringReader(response.getBody())));
						return doc;
					} catch (SAXException e) {
						throw new RuntimeException("xml parsing failed",e);
					} catch (IOException e) {
						throw new RuntimeException("io error while parsing xml",e);
					} catch (ParserConfigurationException e) {
						throw new RuntimeException("xml parser configuration error",e);
					}
				});
		try {
			Document doc = xml.toCompletableFuture().get();
			return doc;
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}


	public String ebiProjectXml(String ac) {
		String type="projects";		
		return ebiXml(ac, type);
	}	
	public String ebiStudyXml(String ac) {
		String type="studies";		
		return ebiXml(ac, type);
	}
	public String ebiSampleXml(String ac) {
		String type="samples";		
		return ebiXml(ac, type);
	}	
	public String ebiExperimentXml(String ac) {
		String type="experiments";		
		return ebiXml(ac, type);
	}		
	public String ebiRunXml(String ac) {
		String type="runs";		
		return ebiXml(ac, type);
	}
	public String ebiSubmissionXml(String ac) {
		String type="submissions";		
		return ebiXml(ac, type);
	}
	/**
	 * Verifie si l'objet project dont l'identifiant est indiqué existe sur le serveur de l'EBI
	 * @param   bioProjectId  : identifiant du project
	 * @return  true si l'AC existe à l'EBI, false sinon 
	 */
	public boolean ebiProjectExists(String bioProjectId) {
		return ebiExists(bioProjectId, "projects");
	}
	
	/**
	 * Verifie si le study dont le numeros d'accession est indiqué existe sur le serveur de l'EBI
	 * @param ac  : numeros d'accession
	 * @return  true si l'AC existe à l'EBI, false sinon 
	 */
	public boolean ebiStudyExists(String ac) {
		return ebiExists(ac, "studies");
	}
	
	
	/**
	 * Verifie si le submission dont le numeros d'accession est indiqué existe sur le serveur de l'EBI
	 * @param ac  : numeros d'accession
	 * @return  true si l'AC existe à l'EBI, false sinon 
	 */
	public boolean ebiSubmissionExists(String ac) {
		return ebiExists(ac, "submissions");
	}
	
	/**
	 * Verifie si le sample dont le numeros d'accession est indiqué existe sur le serveur de l'EBI
	 * @param ac  : numeros d'accession
	 * @return  true si l'AC existe à l'EBI, false sinon 
	 */
	public boolean ebiSampleExists(String ac) {
		return ebiExists(ac, "samples");
	}	
	
	/**
	 * Verifie si l'experiment dont le numeros d'accession est indiqué existe sur le serveur de l'EBI
	 * @param ac  : numeros d'accession
	 * @return  true si l'AC existe à l'EBI, false sinon 
	 */
	public boolean ebiExperimentExists(String ac) {
		return ebiExists(ac, "experiments");
	}
		
	/**
	 * Verifie si le run dont le numeros d'accession est indiqué existe sur le serveur de l'EBI
	 * @param ac  : numeros d'accession
	 * @return  true si l'AC existe à l'EBI, false sinon 
	 */
	public boolean ebiRunExists(String ac) {
		return ebiExists(ac, "runs");
	}
	
}
