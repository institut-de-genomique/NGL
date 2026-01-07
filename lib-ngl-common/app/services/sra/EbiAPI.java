package services.sra;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import models.sra.submit.sra.instance.Submission;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import play.mvc.Http.MultipartFormData.FilePart;
import validation.ContextValidation;

public class EbiAPI {
//  Verifier si donnée publique existe à l'EBI : 
//	https://www.ebi.ac.uk/ena/data/view/ERR2196986
//  Verifier si donnée existe dans notre drop-box à l'EBI (publique ou privée) en s'identifiant :
//	https://www.ebi.ac.uk/ena/submit/drop-box/studies/ERP109506?format=xml&auth=ENA%20Webin-9%20Axqw16nI
	
	private static final play.Logger.ALogger logger = play.Logger.of(EbiAPI.class);
	private final WSClient ws;
	public final String dropBox9 = "Webin-9";
	public final String dropBox261 = "Webin-261";
	// On demande à play d'instancier la classe en lui fournissant un objet WSClient
	@Inject
	public EbiAPI(WSClient         ws) {
		this.ws            = ws;
	}
	
	/**
	 * Verifie si l'objet dont le numeros d'accession est indiqué existe sur le serveur de l'EBI
	 * @param ac   : numeros d'accession
	 * @param type : type d'objet : projects, studies, samples, experiments, runs ou submission
	 * @return     : true si l'AC existe à l'EBI, false sinon 
	 */	
	protected boolean _ebiExists(String ac, String type) {
		String url = String.format("https://www.ebi.ac.uk/ena/submit/drop-box/%s/%s?format=xml", type, ac);
		//logger.debug("ebiExists -- url : {}", url);
		//ws.url(https://www.ebi.ac.uk/ena/submit/drop-box/samples/$ac?format=xml);
		WSRequest wr = ws.url(url);
		//String login = config.getString("sraEbiLogin");		
		String login = "Webin-9";
		//String pw = config.getString("sraEbiPassword");
		String pw = "Axqw16nI";
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
	* Renvoie le xml correspondant au taxonId indiqué s'il existe sur le serveur public de l'EBI
	* @param taxonId   : identifiant du taxon
	* @return          : string_xml correspondant aux infos disponibles pour le taxonId indique
	*/			
	public String _ebiInfosTaxon(int taxonId) {
		String url = String.format("https://www.ebi.ac.uk/ena/taxonomy/rest/tax-id/%d", taxonId);
		//logger.debug("_ebiInfosTaxon -- url : {}", url);
		WSRequest wr = ws.url(url);
		//wr.setAuth("Webin-9", "Axqw16nI");
		CompletionStage<WSResponse> homePage = wr.get();		
		CompletionStage<String> xml = homePage.thenApplyAsync(response -> {
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
	* Renvoie vrai si le taxonId indiqué existe sur le serveur public de l'EBI et est soumettable, sinon renvoie false
	* @param taxonId   : identifiant du taxon
	* @return          : true si le taxonId est submittable; false sinon
	*/		
	public boolean submittable(int taxonId) {
		int maxTentatives = 3;
		String xmlString = null; 
		boolean submittable = false;
		while(maxTentatives > 0) {
			xmlString = _ebiInfosTaxon(taxonId);
			maxTentatives--;
			if ( xmlString.contains(":403")) { // 403 pb de droits sur le serveur
				logger.debug("pb de droits avec xmlString=" + xmlString);
				continue;
			} else {
				break;
			}
		}
		//logger.debug(xmlString);
		String pattern = "\"submittable\"\\s*:\\s*\"(true|false)\"";
		java.util.regex.Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(xmlString);
		// Appel de find obligatoire pour pouvoir récupérer $1 ...$n
		if ( m.find() ) {
			submittable = new Boolean(m.group(1));
		}
		return submittable;
	}

	/**
	* Renvoie le xml correspondant au taxonId indiqué s'il existe sur le serveur public de l'EBI
	* @param taxonId   : identifiant du taxon
	* @return          : string_xml correspondant aux infos disponibles pour le taxonId indique
	*/		
	public String getScientificName(int taxonId) {
		int maxTentatives = 3;
		String xmlString = null; 
		String scientificName = null;
		while(maxTentatives > 0) {
			xmlString = _ebiInfosTaxon(taxonId);
			maxTentatives--;
			if ( xmlString.contains(":403")) { // 403 pb de droits sur le serveur
				logger.debug("pb de droits avec xmlString=" + xmlString);
				continue;
			} else {
				break;
			}
		}
		//logger.debug(xmlString);
		String pattern = "\"scientificName\"\\s*:\\s*\"(.*)\"";
		java.util.regex.Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(xmlString);
		// Appel de find obligatoire pour pouvoir récupérer $1 ...$n
		if ( m.find() ) {
			scientificName = m.group(1);
		}
		return scientificName;
	}
	
	/**
	 * Verifie si l'objet dont le numeros d'accession est indiqué existe sur le serveur de l'EBI
	 * @param ac   : numeros d'accession
	 * @param type : type d'objet : projects, studies, samples, experiments, runs ou submission
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
	 * @param ac   : numeros d'accession
	 * @param type : type d'objet : projects, studies, samples, experiments, runs ou submission 
	 * @return     : string xml correspondant à l'objet
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
			//logger.info("response XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"+response.getBody());
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
	 * @param ac   : numeros d'accession
	 * @param type : type d'objet : projects, studies, samples, experiments, runs ou submission
	 * @return     : xml correspondant au numeros d'accession indiqué s'il existe sur le serveur de l'EBI
	 */	
	// Attention pour project :
	// https://www.ebi.ac.uk/ena/browser/api/xml/PRJEB47985 avec project enfants et parents mais uniquement si donnée publique.
	// https://www.ebi.ac.uk/ena/submit/drop-box/projects/PRJEB47985?format=xml&auth=ENA%20Webin-9%20Axqw16nI sans project parent ni project enfant sur notre compte
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
	 * Renvoie le xml correspondant au numeros d'accession indiqué s'il existe sur le browser de l'EBI
	 * @param ac   : numeros d'accession
	 * @return     : xml correspondant au numeros d'accession indiqué s'il existe sur le browser de l'EBI
	 */		
	public String ebiBrowserXml(String ac) {
		int maxTentatives = 3;
		String xmlString = null; 
		while(maxTentatives > 0) {
			xmlString = _ebiBrowserXml(ac);
			maxTentatives--;
			if (xmlString.contains(":403")) { // 403 : problemes de droits:
				continue;
			} else {
				break;
			}
		}
		return xmlString;
	}
	
	private String _ebiBrowserXml(String ac) {
		String url = String.format("https://www.ebi.ac.uk/ena/browser/api/xml/%s?download=true", ac);
		//logger.debug("ebiXml -- url : {}", url);
		//ws.url(https://www.ebi.ac.uk/ena/submit/drop-box/samples/$ac?format=xml);
		WSRequest wr = ws.url(url);
		//wr.setAuth("Webin-9", "Axqw16nI");
		CompletionStage<WSResponse> homePage = wr.get();		
		CompletionStage<String> xml = 
			homePage.thenApplyAsync(response -> {
			//logger.info("response XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"+response.getBody());
			return response.getBody();
		});
		try {
			String doc = xml.toCompletableFuture().get();
			return doc;
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}	
	}
	
	// solution ws completement integre dans play
	public String ebiCurlFinal(String submissionFileName, String projectFileName) throws Exception {
		//String url = "https://wwwdev.ebi.ac.uk/ena/submit/drop-box/submit/?auth=ENA%20Webin-9%20Axqw16nI";
		String url = "https://wwwdev.ebi.ac.uk/ena/submit/drop-box/submit/";
		//logger.debug("ebiXml -- url : {}", url);
		WSRequest wr = ws.url(url);
		String boundary = "------------------------" + Long.toHexString(System.currentTimeMillis());

		wr.setAuth("Webin-9", "Axqw16nI");
		//wr.addQueryParameter("auth", "ENA Webin-9 Axqw16nI");
	
		wr.addHeader("Content-Type" , "multipart/form-data; boundary=" + boundary);
		//wr.addHeader("Content-Type" , "multipart/form-data");
		//wr.addHeader("Content-Type" , "boundary=" + boundary);
		StringBuilder out = new StringBuilder();
		//out.append("\r\n");	
		writePart(boundary , out, "SUBMISSION", submissionFileName);
		writePart(boundary , out, "PROJECT", projectFileName);
			
		
		out.append("--")
		   .append(boundary)
		   .append("--\r\n");  //\r\n car protocole http 

		//Source<ByteString, ?> fileA = FileIO.fromFile(new File(submissionFileName));
		//FilePart filePart_A = new FilePart<>("SUBMISSION", submissionFileName, "application/xml", fileA);
		//Source<ByteString, ?> fileB = FileIO.fromFile(new File(projectFileName));
		//FilePart filePart_B = new FilePart<>("PROJECT", projectFileName, "application/xml", fileB);
		logger.debug("body :\n{}", out);
		
		//CompletionStage<WSResponse> homePage = wr.post(Source.from(Collections.unmodifiableList(Arrays.asList(filePart_A, filePart_B))));
		CompletionStage<WSResponse> homePage = wr.post(out.toString());
		
		//CompletionStage<WSResponse> homePage = wr.get();		
		CompletionStage<String> xml = 
			homePage.thenApplyAsync(response -> {
			logger.info("response status: {}\n{}", response.getStatus(), response.getStatusText());
			return response.getBody();
		});
		try {
			String doc = xml.toCompletableFuture().get();
			logger.info("Reponse {}", doc);
			return doc;
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}	
	}

	
	private void webinCurl(Submission submission, String ebiFileResultName, ContextValidation ctx, String dropBox, boolean prod) {
		String stringEbiResult = null;
		try {
			stringEbiResult = curlPlay(submission, dropBox, prod);
			//stringEbiResult = curlJava(submission, dropBox, prod);
		} catch (Exception e) {
			ctx.addError("Exception", e.getMessage());
		}
		try ( FileWriter fw = new FileWriter(ebiFileResultName, false); // false pour ecraser ancien fichier, true pour concatener
			  BufferedWriter bw = new BufferedWriter(fw);
			  PrintWriter out = new PrintWriter(bw) ) {   
			out.println(stringEbiResult);
		} catch (IOException e) {
			ctx.addError("Erreur : ", e.getMessage());
		}
	}
	
	public void prodWebin9Curl(Submission submission, ContextValidation ctx) {
		String ebiFileResultName = submission.submissionDirectory + "/" + submission.ebiResult; 
		String dropBox = dropBox9;
		boolean prod  = true;
		webinCurl(submission, ebiFileResultName, ctx, dropBox, prod);
	}
	
	public void devWebin9Curl(Submission submission, ContextValidation ctx) {
		String ebiFileResultName = submission.submissionDirectory + "/" + submission.ebiResult; 
		String dropBox = dropBox9;
		boolean prod = false;
		webinCurl(submission, ebiFileResultName, ctx, dropBox, prod);
	}	
	
	public void devWebin261Curl(Submission submission, ContextValidation ctx) {
		String ebiFileResultName = submission.submissionDirectory + "/" + submission.ebiResult; 
		String dropBox = dropBox261;
		boolean prod = false;
		webinCurl(submission, ebiFileResultName, ctx, dropBox, prod);
	}	
	
	
	/**
	 * Construction d'un MultiPart pour HTTP
	 * @author sgas
	 *
	 */
	public static class MultiPart {
		private final StringBuilder out;
		private final String boundary;
		/**
		 * Construit un objet MonMultiPart
		 * @param boundary  chaine de caracteres separant les Parts
		 */
		MultiPart(String boundary) {
			this.boundary = boundary;
			out = new StringBuilder();
		}
		/**
		 * Ajoute une Part (association cle-contenuFichier).
		 * @param name          nom du Part
		 * @param file          fichier de donnees
		 * @return              cet objet pour pouvoir chainer les appels (fluent java)
		 * @throws IOException  erreur lors de la lecture du fichier
		 */
		public MultiPart addPart(String name, File file) throws IOException {
			String shortName = file.getName();
			logger.debug("name : {}, short : {}, file {}", name, shortName, file);
			// entete de Parts
			out.append("--").append(boundary).append("\r\n");  // protocole http
			out.append("Content-Disposition: form-data; name=\"")
			   .append(name)
			   .append("\"; filename=\"")
			   .append(shortName)
			   .append("\"\r\n");   // protocole http
			out.append("Content-Type: application/xml\r\n\r\n");  // protocole http
			out.append(new String(Files.readAllBytes(file.toPath()))); 
			out.append("\r\n");
			return this;
		}

		/**
		 * Fin de Parts.
		 * @return cet objet pour pouvoir chainer les appels (fluent java)
		 */
		public MultiPart endParts() {
			out.append("--")
			   .append(boundary)
			   .append("--\r\n");  //\r\n car protocole http 
			return this;
		}
		public String getText() {
			return out.toString();
		}
	}
	
	public static class MonMultiPart extends MultiPart {
		/**
		 * Construit un objet MonMultiPart
		 * @param boundary  chaine de caracteres separant les Parts
		 */
		MonMultiPart(String boundary) {
			super(boundary);
		}
		
		/**
		 * Ajoute une Part (association cle-contenuFichier) si argument 
		 * fileName non null et avec caractères non blancs.
		 * @param name          nom du Part
		 * @param directory     repertoire avec fichier de données
		 * @param fileName      nom relatif du fichier de données
		 * @return              cet objet pour pouvoir chainer les appels (fluent java)
		 * @throws IOException  erreur lors de la lecture du fichier
		 */
		public MonMultiPart addPartIfFileName(String name, File directory, String fileName) throws IOException {
			if (StringUtils.isNotBlank(fileName)) {
				logger.debug("ok {} {}", name, fileName);
				addPart(name, new File(directory, fileName));
			}
			return this;
		}
	}
	
	// solution ws completement integre dans play
	private String curlPlay(Submission submission, String dropBox, boolean prod) throws Exception {
		//String url = "https://wwwdev.ebi.ac.uk/ena/submit/drop-box/submit/?auth=ENA%20Webin-9%20Axqw16nI";
		//
		if( ! dropBox.equals(dropBox9) &&  ! dropBox.equals(dropBox261)) {
			throw new Exception("appel du curlPlay avec dropBox different de " + dropBox9 +" et " + dropBox261);
		}
		if(prod && ! dropBox.equals(dropBox9)) {
			throw new Exception("appel deconseille de curlPlay en PROD avec dropBox "+ dropBox261);
		}
		
		String url = "https://wwwdev.ebi.ac.uk/ena/submit/drop-box/submit/";
		
		if(prod && dropBox.equals("Webin-9")) {
			url = "https://www.ebi.ac.uk/ena/submit/drop-box/submit/";
		}
		
		//logger.debug("ebiXml -- url : {}", url);
		WSRequest wr = ws.url(url);
		String boundary = "------------------------" + Long.toHexString(System.currentTimeMillis());
		
		if(dropBox.equals(dropBox9)) {
			wr.setAuth("Webin-9", "Axqw16nI");
		} else {
			wr.setAuth("Webin-261", "Reg1s@EMBL");
		}
		wr.addHeader("Content-Type" , "multipart/form-data; boundary=" + boundary);
		
		File submissionDirectory = new File(submission.submissionDirectory);

		String out = new MonMultiPart(boundary)
				.addPartIfFileName("SUBMISSION", submissionDirectory, submission.xmlSubmission)
				.addPartIfFileName("PROJECT",    submissionDirectory, submission.xmlUmbrella)
				.addPartIfFileName("PROJECT",    submissionDirectory, submission.xmlProjects)
				.addPartIfFileName("STUDY",      submissionDirectory, submission.xmlStudys)
				.addPartIfFileName("SAMPLE",     submissionDirectory, submission.xmlSamples)
				.addPartIfFileName("EXPERIMENT", submissionDirectory, submission.xmlExperiments)
				.endParts()
				.getText();
		
		//logger.debug("body :\n{}", out);
		CompletionStage<WSResponse> homePage = wr.post(out);
		
		//CompletionStage<WSResponse> homePage = wr.get();		
		CompletionStage<String> xml = 
			homePage.thenApplyAsync(response -> {
			//logger.info("response status: {}\n{}", response.getStatus(), response.getStatusText());
			return response.getBody();
		});
		try {
			String result = xml.toCompletableFuture().get();
			//logger.info("Reponse {}", result);
			return result;
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}	
	}
	
	
	@SuppressWarnings("unused")
	private String oriCurlPlay(Submission submission, String dropBox, boolean prod) throws Exception {
		//String url = "https://wwwdev.ebi.ac.uk/ena/submit/drop-box/submit/?auth=ENA%20Webin-9%20Axqw16nI";
		//
		if( ! dropBox.equals(dropBox9) &&  ! dropBox.equals(dropBox261)) {
			throw new Exception("appel du curlPlay avec dropBox different de " + dropBox9 +" et " + dropBox261);
		}
		if(prod && ! dropBox.equals(dropBox9)) {
			throw new Exception("appel deconseille de curlPlay en PROD avec dropBox "+ dropBox261);
		}
		
		String url = "https://wwwdev.ebi.ac.uk/ena/submit/drop-box/submit/";
		
		if(prod && dropBox.equals("Webin-9")) {
			url = "https://www.ebi.ac.uk/ena/submit/drop-box/submit/";
		}
		
		//logger.debug("ebiXml -- url : {}", url);
		WSRequest wr = ws.url(url);
		String boundary = "------------------------" + Long.toHexString(System.currentTimeMillis());
		
		if(dropBox.equals(dropBox9)) {
			wr.setAuth("Webin-9", "Axqw16nI");
		} else {
			wr.setAuth("Webin-261", "Reg1s@EMBL");
		}
		wr.addHeader("Content-Type" , "multipart/form-data; boundary=" + boundary);
		StringBuilder out = new StringBuilder();
		//out.append("\r\n");	
		if(StringUtils.isNotBlank(submission.xmlSubmission)) {
			logger.debug("ok xmlSubmission");
			writePart(boundary , out, "SUBMISSION", submission.submissionDirectory + "/" + submission.xmlSubmission);
		}
		
		if(StringUtils.isNotBlank(submission.xmlUmbrella)) {
			logger.debug("ok xmlUmbrella");
			writePart(boundary , out, "PROJECT", submission.submissionDirectory + "/" + submission.xmlUmbrella);
		}
		if(StringUtils.isNotBlank(submission.xmlProjects)) {
			logger.debug("ok xmlProjects");
			writePart(boundary , out, "PROJECT", submission.submissionDirectory + "/" + submission.xmlProjects);
		}
		if(StringUtils.isNotBlank(submission.xmlStudys)) {
			logger.debug("ok xmlStudys");
			writePart(boundary , out, "STUDY", submission.submissionDirectory + File.separator + submission.xmlStudys);
		}	
		if(StringUtils.isNotBlank(submission.xmlSamples)) {
			logger.debug("ok xmlSamples");
			writePart(boundary , out, "SAMPLE", submission.submissionDirectory + File.separator + submission.xmlSamples);
		}		
		if(StringUtils.isNotBlank(submission.xmlExperiments)) {
			logger.debug("ok xmlExperiments");
			writePart(boundary , out, "EXPERIMENT", submission.submissionDirectory + File.separator + submission.xmlExperiments);
		}		
		if(StringUtils.isNotBlank(submission.xmlRuns)) {
			logger.debug("ok xmlRuns");
			writePart(boundary , out, "RUN", submission.submissionDirectory + File.separator + submission.xmlRuns);
		}				
		out.append("--")
		   .append(boundary)
		   .append("--\r\n");  //\r\n car protocole http 
		
		logger.debug("body :\n{}", out);
		
		//CompletionStage<WSResponse> homePage = wr.post(Source.from(Collections.unmodifiableList(Arrays.asList(filePart_A, filePart_B))));
		CompletionStage<WSResponse> homePage = wr.post(out.toString());
		
		//CompletionStage<WSResponse> homePage = wr.get();		
		CompletionStage<String> xml = 
			homePage.thenApplyAsync(response -> {
			logger.info("response status: {}\n{}", response.getStatus(), response.getStatusText());
			return response.getBody();
		});
		try {
			String result = xml.toCompletableFuture().get();
			logger.info("Reponse {}", result);
			return result;
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}	
	}
			
	// solution ws completement integre dans play
	@SuppressWarnings("unused")
	private String devEbiCurl(String submissionFileName, String projectFileName) throws Exception {
		//String url = "https://wwwdev.ebi.ac.uk/ena/submit/drop-box/submit/?auth=ENA%20Webin-9%20Axqw16nI";
		String url = "https://wwwdev.ebi.ac.uk/ena/submit/drop-box/submit/";
		//logger.debug("ebiXml -- url : {}", url);
		WSRequest wr = ws.url(url);
		String boundary = "------------------------" + Long.toHexString(System.currentTimeMillis());

		wr.setAuth("Webin-9", "Axqw16nI");
	
		wr.addHeader("Content-Type" , "multipart/form-data; boundary=" + boundary);
		StringBuilder out = new StringBuilder();
		writePart(boundary , out, "SUBMISSION", submissionFileName);
		writePart(boundary , out, "PROJECT", projectFileName);
			
		
		out.append("--")
		   .append(boundary)
		   .append("--\r\n");  //\r\n car protocole http 

		logger.debug("body :\n{}", out);
		
		CompletionStage<WSResponse> homePage = wr.post(out.toString());
		
		CompletionStage<String> xml = 
			homePage.thenApplyAsync(response -> {
			logger.info("response status: {}\n{}", response.getStatus(), response.getStatusText());
			return response.getBody();
		});
		try {
			String doc = xml.toCompletableFuture().get();
			logger.info("Reponse {}", doc);
			return doc;
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}	
	}
			

	private void writePart(String boundary , StringBuilder out, String name, String fileName) throws Exception {
		writePart(boundary, out, fileName, new File(fileName)); 
	}
	
	private void writePart(String boundary , StringBuilder out, String name, File file) throws Exception {
		String shortName = file.getName();
		logger.debug("name : {}, short : {}, file {}", name, shortName, file);
		out.append("--").append(boundary).append("\r\n");  // protocole http
		out.append("Content-Disposition: form-data; name=\"")
		   .append(name)
		   .append("\"; filename=\"")
		   .append(shortName)
		   .append("\"\r\n");   // protocole http
		out.append("Content-Type: application/xml\r\n\r\n");  // protocole http
//		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(fileName)), "UTF-8"))){
//			for (String line; (line = reader.readLine()) != null;) {
//				out.append(line).append("\n"); // pas besoin ici car lecture des données par serveur donc hors protocole http
//			}
//		} 
		out.append(new String(Files.readAllBytes(file.toPath()))); 
		out.append("\r\n");
	}	
	
	//https://stackoverflow.com/questions/1378920/how-can-i-make-a-multipart-form-data-post-request-using-java
	// solution uniquement en java (sans play) donc portable
	@SuppressWarnings("unused")
	private String curlJava(Submission submission, String dropBox, boolean prod) throws Exception {
		if( ! dropBox.equals(dropBox9) &&  ! dropBox.equals(dropBox261)) {
			throw new Exception("appel du curlPlay avec dropBox different de " + dropBox9 +" et " + dropBox261);
		}
		if(prod && ! dropBox.equals(dropBox9)) {
			throw new Exception("appel deconseille de curlPlay en PROD avec dropBox "+ dropBox261);
		}

		String url = "https://wwwdev.ebi.ac.uk/ena/submit/drop-box/submit/";

		if(prod && dropBox.equals("Webin-9")) {
			url = "https://www.ebi.ac.uk/ena/submit/drop-box/submit/";
		}
		
		String boundary = "------------------------" + Long.toHexString(System.currentTimeMillis());
		URLConnection connection = new URL(url).openConnection();
		connection.setDoOutput(true);
		connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

		String encodedBytes = new String(Base64.getEncoder().encode("Webin-9:Axqw16nI".getBytes()));
			
		connection.setRequestProperty("Authorization", "Basic " + encodedBytes );	
		File submissionDirectory = new File(submission.submissionDirectory);

		String out = new MonMultiPart(boundary)
				.addPartIfFileName("SUBMISSION", submissionDirectory, submission.xmlSubmission)
				.addPartIfFileName("PROJECT",    submissionDirectory, submission.xmlUmbrella)
				.addPartIfFileName("PROJECT",    submissionDirectory, submission.xmlProjects)
				.addPartIfFileName("STUDY",      submissionDirectory, submission.xmlStudys)
				.addPartIfFileName("SAMPLE",     submissionDirectory, submission.xmlSamples)
				.addPartIfFileName("EXPERIMENT", submissionDirectory, submission.xmlExperiments)
				.endParts()
				.getText();

		//logger.debug("out:\n{}", out);
		
		try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(connection.getOutputStream(), "UTF-8"))){
			writer.println(out.toString());
		}
		// Connection is lazily executed whenever you request any status.
		@SuppressWarnings("unused")
		int responseCode = ((HttpURLConnection) connection).getResponseCode();
		// Handle response
		//logger.debug("ResponseCode {}", responseCode);
		StringBuilder sb = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
			for (String line; (line = reader.readLine()) != null;) {
				sb.append(line).append("\n"); // pas besoin ici car lecture des données par nous donc hors protocole http
			}
		}
		String  result = sb.toString();
		//logger.debug("result={}", result);
		
		return result;
	}
	
	
	//https://stackoverflow.com/questions/1378920/how-can-i-make-a-multipart-form-data-post-request-using-java
	// solution uniquement en java (sans play) donc portable
	@SuppressWarnings("unused")
	private String devEbiCurlJava(String submissionFileName, String projectFileName) throws Exception {

		String boundary = "------------------------" + Long.toHexString(System.currentTimeMillis());
		String url = "https://wwwdev.ebi.ac.uk/ena/submit/drop-box/submit/";
		//String url = "https://wwwdev.ebi.ac.uk/ena/submit/drop-box/submit/?auth=ENA%20Webin-9%20Axqw16nI";
		URLConnection connection = new URL(url).openConnection();
		connection.setDoOutput(true);
		connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

		String encodedBytes = new String(Base64.getEncoder().encode("Webin-9:Axqw16nI".getBytes()));
		
		
		connection.setRequestProperty("Authorization", "Basic " + encodedBytes );
		StringBuilder out = new StringBuilder();
			
		writePart(boundary , out, "SUBMISSION", submissionFileName);
		writePart(boundary , out, "PROJECT", projectFileName);	
	
		out.append("--")
		   .append(boundary)
		   .append("--\r\n");  //\r\n car protocole http 

		logger.debug("out:\n{}", out);
		try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(connection.getOutputStream(), "UTF-8"))){
			writer.println(out.toString());
		}
		// Connection is lazily executed whenever you request any status.
		int responseCode = ((HttpURLConnection) connection).getResponseCode();
		// Handle response
		logger.debug("ResponseCode {}", responseCode);
		StringBuilder sb = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
			for (String line; (line = reader.readLine()) != null;) {
				sb.append(line).append("\n"); // pas besoin ici car lecture des données par nous donc hors protocole http
			}
		}
		String  result = sb.toString();
		logger.debug("result={}", result);
		return result;
	}
	
	
	
    @SuppressWarnings("unused")
	private void devEbiCurlSystem (Submission submission, ContextValidation ctx)  {
    	//String cmdHeader = "cd " + submission.submissionDirectory +" ; && ";
    	File dir = new File(submission.submissionDirectory);
    	//String cmdCurl = headerCmdCurl;
    	List<String> cmdCurl = new ArrayList<String>();

    	String strCmdCurl = "curl https://wwwdev.ebi.ac.uk/ena/submit/drop-box/submit/?auth=ENA%20Webin-9%20Axqw16nI ";
    	cmdCurl.add("curl");
    	//"curl https://wwwdev.ebi.ac.uk/ena/submit/drop-box/submit/?auth=ENA%20Webin-9%20Axqw16nI ";
    	cmdCurl.add("https://wwwdev.ebi.ac.uk/ena/submit/drop-box/submit/?auth=ENA%20Webin-9%20Axqw16nI");
    	if (StringUtils.isNotBlank(submission.xmlSubmission)) {
    		cmdCurl.add("-F");
    		//cmdCurl.add("\"SUBMISSION=@" + new File(dir, submission.xmlSubmission) + "\"");
    		cmdCurl.add("\"SUBMISSION=@" + new File(submission.xmlSubmission) + "\"");
    		//strCmdCurl +=  " -F \"SUBMISSION=@" + new File(dir, submission.xmlSubmission) + "\" ";
    		strCmdCurl +=  " -F \"SUBMISSION=@" + new File(submission.xmlSubmission) + "\" ";
    	}
    	if(StringUtils.isNotBlank(submission.xmlUmbrella)) {
       		cmdCurl.add("-F");
    		//cmdCurl.add("\"PROJECT=@" + new File(dir, submission.xmlUmbrella) + "\"");
    		cmdCurl.add("\"PROJECT=@" + new File(submission.xmlUmbrella) + "\"");
    		strCmdCurl += " -F \"PROJECT=@" + new File(submission.xmlUmbrella) + "\"";
    	}
    	if(StringUtils.isNotBlank(submission.xmlProjects)) {
       		cmdCurl.add("-F");
    		//cmdCurl.add("\"PROJECT=@" + new File(dir, submission.xmlProjects) + "\"");
    		cmdCurl.add("\"PROJECT=@" + new File(submission.xmlProjects) + "\"");
    		strCmdCurl += " -F \"PROJECT=@" + new File(submission.xmlProjects) + "\"";
    	}
    	String chaine = File.separator + "$";
    	submission.submissionDirectory.replaceFirst(chaine, "");
    	
    	//cmdCurl += " -o " + submission.submissionDirectory + File.separator + submission.ebiResult;
   		cmdCurl.add("-o");
   		//cmdCurl.add("\"" + new File(dir, submission.ebiResult).getAbsolutePath() + "\"");
   		cmdCurl.add("\"" + new File(submission.ebiResult) + "\"");
   		strCmdCurl += " -o \"" + new File(submission.ebiResult) + "\"";
   		
    	logger.debug("submissionDirectory={}", submission.submissionDirectory);
    	logger.debug("echo XXXXXXXXXXXXXXXXXXX cmdCurl = {}", strCmdCurl);
    	
    	try {
    		logger.debug("xxxxxxxxxxxxxxxxx      cmdCurl={}", cmdCurl);
    		// Si on utilise exec sans specifier le directory alors commande qui doit etre avec chemin absolu
			//Runtime.getRuntime().exec(cmdCurl.toArray(new String[cmdCurl.size()])); ok
    		
    		// Si on utilise exec en specifiant le directory d'execution alors commande qui peut etre sans les chemins absolus :
       		//"https://wwwdev.ebi.ac.uk/ena/submit/drop-box/submit/?auth=ENA%20Webin-9%20Axqw16nI  
    		// -F "SUBMISSION=@submissionUmbrella.xml" 
    		// -F "PROJECT=@umbrella.xml" 
    		// -o "ebi_AC_submissionUmbrella_xbVenVerr.txt"    		
    		Runtime.getRuntime().exec(cmdCurl.toArray(new String[cmdCurl.size()]), null, dir);
		} catch (Exception e) {
			ctx.addError("probleme cmd curl pour submission " + submission.code, e.getMessage());
			return;
			//throw new SraValidationException(ctx);	 
		}
		// commande curl envoye avec Runtime.getRuntime().exec qui n'a pas toujours fini d'ecrire le fichier ebiRetour 
		// quand elle rend la main d'ou un sleep.
		int millis = 3000;
		try {
		    Thread.sleep(millis);
		} catch (InterruptedException ie) {
		}
    	
    }
    
	
	
	// ne marche pas ?
	public String ebiPublicXml(String ac) {
		int maxTentatives = 3;
		String xmlString = null; 
		while(maxTentatives > 0) {
			xmlString = _ebiPublicXml(ac);
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
	 * @param ac   : numeros d'accession
	 * @return     : xml correspondant au numeros d'accession indiqué s'il existe sur le serveur public de l'EBI
	 */	
	// ne marche pas ?
	public String _ebiPublicXml(String ac) {
		String url = String.format("https://www.ebi.ac.uk/ena/data/view/%s?display=xml", ac);
		//https://www.ebi.ac.uk/ena/data/view/SRX529512&display=xml
		//logger.debug("ebiXml -- url : {}", url);
		WSRequest wr = ws.url(url);
		//wr.setAuth("Webin-9", "Axqw16nI");
		CompletionStage<WSResponse> homePage = wr.get();		
		CompletionStage<String> xml = 
			homePage.thenApplyAsync(response -> {
			//logger.info("response ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ" + response.getBody());
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
	 * Renvoie le xml correspondant au numeros d'accession indiqué s'il existe sur le serveur public de l'EBI
	 * @param ac   : numeros d'accession
	 * @return     : xml correspondant au numeros d'accession indiqué s'il existe sur le serveur public de l'EBI
	 */	
	// ne marche pas ?
	public Document _ebiPublicXml2(String ac) {
		String url = String.format("https://www.ebi.ac.uk/ena/data/view/%s?display=xml", ac);
		//https://www.ebi.ac.uk/ena/data/view/SRX529512&display=xml
		//logger.debug("ebiXml -- url : {}", url);
		WSRequest wr = ws.url(url);
		//wr.setAuth("Webin-9", "Axqw16nI");
		CompletionStage<WSResponse> homePage = wr.get();		
		CompletionStage<Document> xml = 
			homePage.thenApplyAsync(response -> {
				try {
					//logger.info("response "+response.getBody());
					DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
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
	
	protected Document ebiDocumentXml(String ac, String type) {
		String url = String.format("https://www.ebi.ac.uk/ena/submit/drop-box/%s/%s?format=xml", type, ac);
		//logger.debug("ebiXml -- url : {}", url);
		//ws.url(https://www.ebi.ac.uk/ena/submit/drop-box/samples/$ac?format=xml);
		WSRequest wr = ws.url(url);
		wr.setAuth("Webin-9", "Axqw16nI");
		CompletionStage<WSResponse> homePage = wr.get();
		CompletionStage<Document> xml = 
				homePage.thenApplyAsync(response -> {
					try {
						//logger.info("response "+response.getBody());
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
	 * @return                : true si l'AC existe à l'EBI, false sinon 
	 */
	public boolean ebiProjectExists(String bioProjectId) {
		return ebiExists(bioProjectId, "projects");
	}
	
	/**
	 * Verifie si le study dont le numeros d'accession est indiqué existe sur le serveur de l'EBI
	 * @param ac  : numeros d'accession
	 * @return    : true si l'AC existe à l'EBI, false sinon 
	 */
	public boolean ebiStudyExists(String ac) {
		return ebiExists(ac, "studies");
	}
	
	
	/**
	 * Verifie si le submission dont le numeros d'accession est indiqué existe sur le serveur de l'EBI
	 * @param ac  : numeros d'accession
	 * @return    : true si l'AC existe à l'EBI, false sinon 
	 */
	public boolean ebiSubmissionExists(String ac) {
		return ebiExists(ac, "submissions");
	}
	
	/**
	 * Verifie si le sample dont le numeros d'accession est indiqué existe sur le serveur de l'EBI
	 * @param ac  : numeros d'accession
	 * @return    : true si l'AC existe à l'EBI, false sinon 
	 */
	public boolean ebiSampleExists(String ac) {
		return ebiExists(ac, "samples");
	}	
	
	/**
	 * Verifie si l'experiment dont le numeros d'accession est indiqué existe sur le serveur de l'EBI
	 * @param ac  : numeros d'accession
	 * @return    : true si l'AC existe à l'EBI, false sinon 
	 */
	public boolean ebiExperimentExists(String ac) {
		return ebiExists(ac, "experiments");
	}
		
	/**
	 * Verifie si le run dont le numeros d'accession est indiqué existe sur le serveur de l'EBI
	 * @param ac  : numeros d'accession
	 * @return    : true si l'AC existe à l'EBI, false sinon 
	 */
	public boolean ebiRunExists(String ac) {
		return ebiExists(ac, "runs");
	}
}
