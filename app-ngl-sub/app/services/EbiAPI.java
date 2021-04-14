package services;

import static ngl.refactoring.state.SRASubmissionStateNames.SUB_F;

import java.io.IOException;
import java.io.StringReader;
import java.util.Date;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import fr.cea.ig.lfw.utils.Iterables;
import fr.cea.ig.ngl.NGLConfig;
import fr.cea.ig.ngl.dao.sra.ProjectDAO;
import fr.cea.ig.ngl.dao.sra.StudyDAO;
import fr.cea.ig.ngl.dao.sra.SubmissionDAO;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.sra.submit.common.instance.Project;
import models.sra.submit.common.instance.Study;
import models.sra.submit.common.instance.Submission;
import models.sra.submit.util.SraException;
import models.sra.submit.util.SraValidationException;
import ngl.refactoring.state.SRASubmissionStateNames;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import validation.ContextValidation;

public class EbiAPI {
//  Verifier si donnée publique existe à l'EBI : 
//	https://www.ebi.ac.uk/ena/data/view/ERR2196986
//  Verifier si donnée existe dans notre drop-box à l'EBI (publique ou privée) en s'identifiant :
//	https://www.ebi.ac.uk/ena/submit/drop-box/studies/ERP109506?format=xml&auth=ENA%20Webin-9%20Axqw16nI
	
	private static final play.Logger.ALogger logger = play.Logger.of(EbiAPI.class);
	private final WSClient ws;
	private final NGLConfig  config;
	private final SubmissionDAO submissionDAO;
	private final StudyDAO studyDAO;
	private final ProjectDAO projectDAO;
	// On demande à play d'instancier la classe en lui fournissant un objet WSClient
	@Inject
	public EbiAPI(WSClient      ws,
				  NGLConfig     config,
				  SubmissionDAO submissionDAO,
				  StudyDAO      studyDAO, 
				  ProjectDAO    projectDAO) {
		this.ws            = ws;
		this.config        = config;
		this.submissionDAO = submissionDAO;
		this.studyDAO      = studyDAO;
		this.projectDAO    = projectDAO;
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
	
	
	
	/**
	 * Recupere à l'EBI, le project correspondant au studyCode passé en parametre
	 * et l'insere dans collection ngl-sub.project
	 * @param  studyCode          code de l'objet study
	 */
	public void loadProjectFromEbi(String studyCode) {	
		if (StringUtils.isBlank(studyCode)) {
			return;
		}
		Study study = studyDAO.getObject(studyCode);
		if (study == null) {
			logger.error("Dans loadProjectFromEbi : le studyCode " + studyCode + " n'existe pas dans la base\n");
			return;
		}
		try {
			String xmlProjects = ebiXml(study.externalId, "projects");
			XmlToSra repriseHistorique = new XmlToSra();
			
			Project project = Iterables.first(repriseHistorique.forProjects(xmlProjects, study.firstSubmissionDate)).orElse(null);
			project.traceInformation = new TraceInformation();
			project.traceInformation.createUser = study.traceInformation.createUser;
			project.traceInformation.creationDate = study.firstSubmissionDate;
			project.state = new State(SUB_F, study.traceInformation.createUser);
			project.externalId = study.accession;
			for(String projectCode : study.projectCodes) {
				if (! project.projectCodes.contains(projectCode)) {
					project.projectCodes.add(projectCode);
				}
			}
			ContextValidation contextValidation = ContextValidation.createCreationContext(project.traceInformation.createUser);
			project.validate(contextValidation);
			if(contextValidation.hasErrors()) {
				contextValidation.displayErrors(logger);
			} else {
				projectDAO.save(project);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
}
