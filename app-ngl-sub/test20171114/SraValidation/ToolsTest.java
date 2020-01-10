package SraValidation;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import mail.MailServiceException;
import models.sra.submit.common.instance.Sample;
import models.sra.submit.common.instance.Study;
import models.sra.submit.common.instance.Submission;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.sra.instance.ReadSpec;
import models.sra.submit.util.SraException;
import models.sra.submit.util.VariableSRA;
import models.utils.InstanceConstants;

import org.junit.Assert;
import org.junit.Test;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import fr.cea.ig.MongoDBDAO;
import play.libs.F.Promise;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;

import java.util.Calendar;

import services.FileAcServices;
import services.ReleaseServices;
import services.SubmissionServices;
import services.XmlServices;
import utils.AbstractTestsSRA;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.apache.commons.lang3.StringUtils;

import validation.ContextValidation;
//import play.Logger;

public class ToolsTest extends AbstractTestsSRA {
	private static final play.Logger.ALogger logger = play.Logger.of(ToolsTest.class);

	public  List<Sample> xmlToSample(File xmlFile) {
		List<Sample> listSamples = new ArrayList<Sample>();

		/*
		 * Etape 1 : récupération d'une instance de la classe "DocumentBuilderFactory"
		 */
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			/*
			 * Etape 2 : création d'un parseur
			 */
			final DocumentBuilder builder = factory.newDocumentBuilder();
			/*
			 * Etape 3 : création d'un Document
			 */
			
			final Document document= builder.parse(xmlFile);
			//Affiche du prologue
			System.out.println("*************PROLOGUE************");
			System.out.println("version : " + document.getXmlVersion());
			System.out.println("encodage : " + document.getXmlEncoding());      
			System.out.println("standalone : " + document.getXmlStandalone());
			/*
			 * Etape 4 : récupération de l'Element racine
			 */
			final Element racine = document.getDocumentElement();
			//Affichage de l'élément racine
			System.out.println("\n*************RACINE************");
			System.out.println(racine.getNodeName());
			System.out.println("success = " + racine.getAttribute("success"));
			//System.out.println(((Node) racine).getNodeName());
			//System.out.println("success = " + ((DocumentBuilderFactory) racine).getAttribute("success"));
			/*
			 * Etape 5 : récupération des samples
			 */
			

			
			final NodeList racineNoeuds = racine.getChildNodes();
			final int nbRacineNoeuds = racineNoeuds.getLength();
			
			Map<String, String> mapSamples = new HashMap<String, String>(); 
			Map<String, String> mapExperiments = new HashMap<String, String>(); 
			Map<String, String> mapRuns = new HashMap<String, String>(); 
			String submissionAc = null;
			String studyAc = null;
			String message = null;
			String ebiSubmissionCode = null;
			String ebiStudyCode = null;
			String errorStatus = "FE-SUB";
			String okStatus = "F-SUB";
			Boolean ebiSuccess = false;
			
			if( racine.getAttribute("success").equalsIgnoreCase ("true")){
				ebiSuccess = true;
			}
			for (int i = 0; i<nbRacineNoeuds; i++) {
				
				if(racineNoeuds.item(i).getNodeType() == Node.ELEMENT_NODE) {
					
					final Element elt = (Element) racineNoeuds.item(i);
					//Affichage d'un elt :
					System.out.println("\n*************Elt************");
					
					String alias = elt.getAttribute("alias");
					String accession = elt.getAttribute("accession");
					
					System.out.println("alias : " + alias);
					System.out.println("accession : " + accession);
					if(elt.getTagName().equalsIgnoreCase("SUBMISSION")) {
						ebiSubmissionCode = elt.getAttribute("alias");	
						submissionAc = elt.getAttribute("accession");
					} else if(elt.getTagName().equalsIgnoreCase("STUDY")) {
						ebiStudyCode = elt.getAttribute("alias");	
						studyAc = elt.getAttribute("accession");
				    } else if(elt.getTagName().equalsIgnoreCase("SAMPLE")) {
				    	mapSamples.put(elt.getAttribute("alias"), elt.getAttribute("accession"));	
					} else if(elt.getTagName().equalsIgnoreCase("EXPERIMENT")) {
				    	mapExperiments.put(elt.getAttribute("alias"), elt.getAttribute("accession"));	
					} else if(elt.getTagName().equalsIgnoreCase("RUN")) {
						mapRuns.put(elt.getAttribute("alias"), elt.getAttribute("accession"));
					} else {
						
					}
				}
				
				
			}  // end for  
		} catch (final ParserConfigurationException e) {
			e.printStackTrace();
		} catch (final SAXException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		} 
		return listSamples;
	}		
	
	//@Test
	public void testfileAc()throws IOException, SraException, MailServiceException {
		File fileEbi = new File("/env/cns/home/sgas/test/listAC_CNS_ANU_29LH3R4QK_201709251245.txt");
		String user = "william";
		ContextValidation ctxVal = new ContextValidation(user);
		String submissionCode = "CNS_ANU_29LH3R4QK";
		Submission submission = FileAcServices.traitementFileAC(ctxVal, submissionCode, fileEbi); 
		ctxVal.displayErrors(logger);

	}
	
	//@Test
	public void testRelease()throws IOException {
		Submission submission = MongoDBDAO.findOne(InstanceConstants.SRA_SUBMISSION_COLL_NAME,
				Submission.class, DBQuery.and(DBQuery.is("code", "CNS_BDQ_BGX_BGU_27RG1T269")));
		
		XmlServices xmlServices = new XmlServices();
		try {
			xmlServices.writeAllXml(submission.code);
		} catch (SraException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	//@Test
	public void testRetourRelease()throws IOException, SraException, MailServiceException {
		String user = "william";
		ContextValidation ctxVal = new ContextValidation(user);
		String submissionCode = "CNS_BDQ_BGX_BGU_27RG1T269";
		File retourEbiRelease = new File("/env/cns/submit_traces/SRA/SNTS_output_xml/NGL/BDQ_BGX_BGU/24_07_2017/releaseResult");
		Submission submission = ReleaseServices.traitementRetourRelease(ctxVal, submissionCode, retourEbiRelease); 
	}
	
	//@Test
	public void testDates()throws IOException {
		Calendar calendar = Calendar.getInstance();
		Date date  = calendar.getTime();
		int year = calendar.get(Calendar.YEAR);
		System.out.println("annee = " + year);
		System.out.println("date = " + date);

		System.out.println("getDate =" + new Date());
		calendar.add(Calendar.YEAR, 2);
		Date release_date  = calendar.getTime();
		int year_release = calendar.get(Calendar.YEAR);
		System.out.println("annee_release = " + year_release);
		System.out.println("date_release = " + release_date);
		String user = "william";
		
		MongoDBDAO.update(InstanceConstants.SRA_STUDY_COLL_NAME, Study.class, 
				DBQuery.is("code", "STUDY_BCU_25TG2F0LD"),
				DBUpdate.set("accession", "toto").set("firstSubmissionDate", date).set("releaseDate", release_date).set("traceInformation.modifyUser", user).set("traceInformation.modifyDate", date));
		
		Study study = MongoDBDAO.findByCode(InstanceConstants.SRA_STUDY_COLL_NAME, Study.class, "STUDY_BCU_25TG2F0LD");	
		
		System.out.println("apres requete");
		
		if (MongoDBDAO.checkObjectExist(InstanceConstants.SRA_STUDY_COLL_NAME, Study.class, "code", "STUDY_BCU_25TG2F0LD")){
			System.out.println("le study avec  : code=" +study.code + " existe bien dans base");
		}
		System.out.println("dans study : code=" +study.code);
		System.out.println("dans study : accession=" +study.accession);

		System.out.println("dans study : firstSubmissionDate=" +study.firstSubmissionDate);
		System.out.println("dans study : release_date=" +study.releaseDate);

		/* deprecated :
		Date release = calendar.get
		//Date date = new Date();
		System.out.println("date = " + date);
		int annee_release = date.getYear() + 2;
		date.setYear(annee_release);
		System.out.println("date release = " + date);
		*/
	}
	//@Test
	public void testRegExp()throws IOException  {
		String name = "titi/toto/tutu/lili";
		
		String relatifName = "";
		relatifName = name;
		if (name.contains("/")){
			relatifName = name.substring(name.lastIndexOf("/")+1);
		}
		System.out.println("name :" + name +" et relatifName="+ relatifName);
	}
	
	//@Test
	public void SymbolicLinkSuccess() throws IOException  {
		String nameDirectory = "/env/cns/submit_traces/SRA/NGL_test/tests_liens/linkTest4";
		
		File dir = new File(nameDirectory);
		if (!dir.exists()) {
			if(!dir.mkdirs()){
				System.out.println("impossible de creer repertoire nameDirectory");
			} 
			
		}
		File fileCible = new File(nameDirectory + File.separator + "cible");
		if (fileCible.exists()) {
			fileCible.delete();
		}
		
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(fileCible));
			writer.write("maCible");
			writer.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		File fileLien = new File(nameDirectory + File.separator + "lien_3");
		if (fileLien.exists()){
			fileLien.delete();
		}
		try {
		Path lien = Paths.get(fileLien.getPath());
		Path cible = Paths.get(fileCible.getPath());
		Files.createSymbolicLink(lien, cible);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("ok verifier lien :" + fileLien.getPath());

		Assert.assertTrue(fileLien.exists());
		
	}
	//@Test
	public void testhttp() throws IOException, XPathExpressionException  {
		Promise<WSResponse> homePage = WS.url("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=taxonomy&id=1735743&retmote=xml").get();
		Promise<Document> xml = homePage.map(response -> {
			System.out.println("response "+response.getBody());
			//Document d = XML.fromString(response.getBody());
			//Node n = scala.xml.XML.loadString(response.getBody());
			//System.out.println("J'ai une reponse ?"+ n.toString());
			DocumentBuilderFactory dbf =
		            DocumentBuilderFactory.newInstance();
			//dbf.setValidating(false);
			//dbf.setSchema(null);
	        DocumentBuilder db = dbf.newDocumentBuilder();
	        Document doc = db.parse(new InputSource(new StringReader(response.getBody())));
			return doc;
		});
		
		
		System.out.println("J'ai une reponse xml ?"+xml.get(1000) );
		Document doc = xml.get(1000);
		XPath xPath =  XPathFactory.newInstance().newXPath();
		String expression = "/TaxaSet/Taxon/ScientificName";

		//read a string value
		String scientifiqueName = xPath.compile(expression).evaluate(doc);
		System.out.println("Scientifique name "+scientifiqueName);
		


	   
		//Promise<WSResponse> result = WS.url("http://example.com").post("content");	
	
	}
	public String getNcbiScientificName(Integer taxonId) throws IOException, XPathExpressionException  {
		Promise<WSResponse> homePage = WS.url("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=taxonomy&id="+taxonId+"&retmote=xml").get();
		Promise<Document> xml = homePage.map(response -> {
			System.out.println("response "+response.getBody());
			//Document d = XML.fromString(response.getBody());
			//Node n = scala.xml.XML.loadString(response.getBody());
			//System.out.println("J'ai une reponse ?"+ n.toString());
			DocumentBuilderFactory dbf =
		            DocumentBuilderFactory.newInstance();
			//dbf.setValidating(false);
			//dbf.setSchema(null);
	        DocumentBuilder db = dbf.newDocumentBuilder();
	        Document doc = db.parse(new InputSource(new StringReader(response.getBody())));
			return doc;
		});
		
		Document doc = xml.get(1000);
		XPath xPath =  XPathFactory.newInstance().newXPath();
		String expression = "/TaxaSet/Taxon/ScientificName";

		//read a string value
		String scientificName = xPath.compile(expression).evaluate(doc);
		return scientificName;	
	}

	//@Test
	public void getNcbiScientificName() throws XPathExpressionException, IOException  {	
		Integer taxonId = new Integer(1735743);
		String scientificName = getNcbiScientificName(taxonId);
		System.out.println("getNcbiScientificName::Scientific name = '"+ scientificName + "'");
	}	
	
	
	//@Test
	public void debug_emose() throws IOException, SraException {
		Submission submission1 = MongoDBDAO.findOne(InstanceConstants.SRA_SUBMISSION_COLL_NAME,
				Submission.class, DBQuery.and(DBQuery.is("code", "CNS_CAA_28HF4VOO4")));
		Submission submission2 = MongoDBDAO.findOne(InstanceConstants.SRA_SUBMISSION_COLL_NAME,
				Submission.class, DBQuery.and(DBQuery.is("code", "CNS_BZZ_CAN_28HD5LO4V")));
		Submission submission3 = MongoDBDAO.findOne(InstanceConstants.SRA_SUBMISSION_COLL_NAME,
				Submission.class, DBQuery.and(DBQuery.is("code", "CNS_BZZ_CAN_28HD4ZIGG")));
		Submission submission4 = MongoDBDAO.findOne(InstanceConstants.SRA_SUBMISSION_COLL_NAME,
				Submission.class, DBQuery.and(DBQuery.is("code", "CNS_BZZ_CAN_28HD59RR8")));
		Submission submission5 = MongoDBDAO.findOne(InstanceConstants.SRA_SUBMISSION_COLL_NAME,
				Submission.class, DBQuery.and(DBQuery.is("code", "CNS_BZZ_CAN_28HD5FO6C")));
		System.out.println ("taille sub 1 = " +submission1.experimentCodes.size());			
		System.out.println ("taille sub 2 = " +submission2.experimentCodes.size());			
		System.out.println ("taille sub 3 = " +submission3.experimentCodes.size());			
		System.out.println ("taille sub 4 = " +submission4.experimentCodes.size());			
		System.out.println ("taille sub 5 = " +submission5.experimentCodes.size());			

		XmlServices xmlServices = new XmlServices();
		
		int taille =  submission1.experimentCodes.size() + submission2.experimentCodes.size()+ submission3.experimentCodes.size() + submission4.experimentCodes.size()+submission5.experimentCodes.size();
		System.out.println ("taille totale = " + taille);
		
		List<String> experimentCodes = new ArrayList<String>();
		
		for (String expCode : submission1.experimentCodes) {
			experimentCodes.add(expCode);
		}
		for (String expCode : submission2.experimentCodes) {
			experimentCodes.add(expCode);
		}
		for (String expCode : submission3.experimentCodes) {
			experimentCodes.add(expCode);
		}
		for (String expCode : submission4.experimentCodes) {
			experimentCodes.add(expCode);
		}
		for (String expCode : submission5.experimentCodes) {
			experimentCodes.add(expCode);
		}
		
		String outputFile = "/env/cns/home/sgas/juliePoulain/update_emose_experiment.xml";
		//experimentCodes = new ArrayList<String>();
		// Ecriture de experiment.xml :reprise du code de XmlServices ici car on ne veut pas sauver la soumission
		if (! experimentCodes.isEmpty()) {	
			// ouvrir fichier en ecriture
			System.out.println("Creation du fichier " + outputFile);
			BufferedWriter output_buffer = new BufferedWriter(new java.io.FileWriter(outputFile));
			String chaine = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
			chaine = chaine + "<EXPERIMENT_SET>\n";
			
			for (String experimentCode : experimentCodes){
				// Recuperer objet experiment dans la base :
				Experiment experiment = MongoDBDAO.findByCode(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, models.sra.submit.sra.instance.Experiment.class, experimentCode);
				//output_buffer.write("//\n");
				System.out.println("Ecriture de experiment " + experimentCode);
				if (experiment == null){
					throw new SraException("experiment impossible à recuperer dans base :"+ experimentCode);
				}
				chaine = chaine + "  <EXPERIMENT alias=\"" + experimentCode + "\" center_name=\"" + VariableSRA.centerName + "\"";
				if (StringUtils.isNotBlank(experiment.accession)) {
					chaine = chaine + " accession=\"" + experiment.accession + "\" ";	
				}
				chaine = chaine + ">\n";
				// Les champs title et libraryName sont considerés comme obligatoires
				chaine = chaine + "    <TITLE>" + experiment.title + "</TITLE>\n";
				chaine = chaine + "    <STUDY_REF ";
				//if (StringUtils.isNotBlank(experiment.studyCode) && (experiment.studyCode.startsWith("external"))) { 
				if (StringUtils.isNotBlank(experiment.studyCode)) { 
					chaine = chaine + " refname=\"" + experiment.studyCode +"\"";
				}
				if (StringUtils.isNotBlank(experiment.studyAccession)){
					chaine = chaine + " accession=\"" + experiment.studyAccession + "\"";
				}
				chaine = chaine + "/>\n"; 
				
				chaine = chaine + "      <DESIGN>\n";
				chaine = chaine + "        <DESIGN_DESCRIPTION></DESIGN_DESCRIPTION>\n";
				chaine = chaine + "          <SAMPLE_DESCRIPTOR  ";
				//if (StringUtils.isNotBlank(experiment.sampleCode) && (experiment.sampleCode.startsWith("external"))) {
				if (StringUtils.isNotBlank(experiment.sampleCode)){
					chaine = chaine+  "refname=\"" + experiment.sampleCode + "\"";
				}
				if (StringUtils.isNotBlank(experiment.sampleAccession)){
					chaine = chaine + " accession=\""+experiment.sampleAccession + "\"";
				}
				chaine = chaine + "/>\n";
				
				chaine = chaine + "          <LIBRARY_DESCRIPTOR>\n";
				chaine = chaine + "            <LIBRARY_NAME>" + experiment.libraryName + "</LIBRARY_NAME>\n";
				chaine = chaine + "            <LIBRARY_STRATEGY>"+ VariableSRA.mapLibraryStrategy().get(experiment.libraryStrategy.toLowerCase()) + "</LIBRARY_STRATEGY>\n";
				chaine = chaine + "            <LIBRARY_SOURCE>" + VariableSRA.mapLibrarySource().get(experiment.librarySource.toLowerCase()) + "</LIBRARY_SOURCE>\n";
				chaine = chaine + "            <LIBRARY_SELECTION>" + VariableSRA.mapLibrarySelection().get(experiment.librarySelection.toLowerCase()) + "</LIBRARY_SELECTION>\n";
				chaine = chaine + "            <LIBRARY_LAYOUT>\n";
				
				chaine = chaine + "              <"+ VariableSRA.mapLibraryLayout().get(experiment.libraryLayout.toLowerCase());	
				if("PAIRED".equalsIgnoreCase(experiment.libraryLayout)) {
					chaine = chaine + " NOMINAL_LENGTH=\"" + experiment.libraryLayoutNominalLength + "\"";
				}
				chaine = chaine + " />\n";

				chaine = chaine + "            </LIBRARY_LAYOUT>\n";
				if (StringUtils.isBlank(experiment.libraryConstructionProtocol)){
					chaine = chaine + "            <LIBRARY_CONSTRUCTION_PROTOCOL>none provided</LIBRARY_CONSTRUCTION_PROTOCOL>\n";
				} else {
					chaine = chaine + "            <LIBRARY_CONSTRUCTION_PROTOCOL>"+experiment.libraryConstructionProtocol+"</LIBRARY_CONSTRUCTION_PROTOCOL>\n";

				}
					chaine = chaine + "          </LIBRARY_DESCRIPTOR>\n";
				if (! "OXFORD_NANOPORE".equalsIgnoreCase(experiment.typePlatform)) {
					chaine = chaine + "          <SPOT_DESCRIPTOR>\n";
					chaine = chaine + "            <SPOT_DECODE_SPEC>\n";
					chaine = chaine + "              <SPOT_LENGTH>"+experiment.spotLength+"</SPOT_LENGTH>\n";
					for (ReadSpec readSpec: experiment.readSpecs) {
						chaine = chaine + "              <READ_SPEC>\n";
						chaine = chaine + "                <READ_INDEX>"+readSpec.readIndex+"</READ_INDEX>\n";
						chaine = chaine + "                <READ_LABEL>"+readSpec.readLabel+"</READ_LABEL>\n";
						chaine = chaine + "                <READ_CLASS>"+readSpec.readClass+"</READ_CLASS>\n";
						chaine = chaine + "                <READ_TYPE>"+readSpec.readType+"</READ_TYPE>\n";
						chaine = chaine + "                <BASE_COORD>" + readSpec.baseCoord + "</BASE_COORD>\n";
						chaine = chaine + "              </READ_SPEC>\n";
					}
					chaine = chaine + "            </SPOT_DECODE_SPEC>\n";
					chaine = chaine + "          </SPOT_DESCRIPTOR>\n";
				}
				chaine = chaine + "      </DESIGN>\n";
				chaine = chaine + "      <PLATFORM>\n";
				chaine = chaine + "        <" + VariableSRA.mapTypePlatform().get(experiment.typePlatform.toLowerCase()) + ">\n";
				chaine = chaine + "          <INSTRUMENT_MODEL>" + VariableSRA.mapInstrumentModel().get(experiment.instrumentModel.toLowerCase()) + "</INSTRUMENT_MODEL>\n";
				chaine = chaine + "        </" + VariableSRA.mapTypePlatform().get(experiment.typePlatform.toLowerCase()) + ">\n";
				chaine = chaine + "      </PLATFORM>\n";
				chaine = chaine + "  </EXPERIMENT>\n";
			}
			chaine = chaine + "</EXPERIMENT_SET>\n";
			output_buffer.write(chaine);
			output_buffer.close();
		}
		
		
		
		//xmlServices.writeAllXml(submission.code);
		
	}
	
	//@Test
	public void updateExtId() throws IOException, SraException {
		List<String> submissionCodes = new ArrayList<String>();
		submissionCodes.add("CNS_ART_254F1QHOV");
		submissionCodes.add("CNS_BCU_25U9550HF");
		submissionCodes.add("CNS_BCU_BLK_266H23OI3");
		submissionCodes.add("CNS_BDQ_BGX_BGU_27BB18OFB");
		submissionCodes.add("CNS_BHC_25CB4YW76");
		submissionCodes.add("CNS_BIL_274F26OJP");
		submissionCodes.add("CNS_BTR_26JA4HWSC");
		submissionCodes.add("CNS_BYQ_AWF_24RF4HJFI");
		submissionCodes.add("CNS_BZZ_CAN_28HD4ZIGG");
		submissionCodes.add("CNS_BZZ_CAN_28HD59RR8");
		submissionCodes.add("CNS_BZZ_CAN_28HD5FO6C");
		submissionCodes.add("CNS_BZZ_CAN_28HD5LO4V");
		submissionCodes.add("CNS_CAA_28HF4VOO4");
		for (String code : submissionCodes) {
			System.out.println("submissionCode = "+code);
			
			File fileEbi = new File("/env/cns/home/sgas/debug_extId/list_AC_"+ code +".txt");
			String user = "william";
			ContextValidation ctxVal = new ContextValidation(user);
			/*try {
				Submission submission = FileAcServices.traitementFileAC(ctxVal, code, fileEbi);
			} catch (MailServiceException e) {
				// TODO Auto-generated catch block
				ctxVal.displayErrors(Logger.of("SRA"));	
				e.printStackTrace();
			} */
			ctxVal.displayErrors(logger);	
			
		}
		
	}
	
	//@Test
	public void  testXml() throws IOException, SraException {
		//logger.info("test de argument {} pour les loggers {}", "arg_1", "other_args");
		String submissionCode = "CNS_BAN_2C5F3W6K6";
		String resultDirectory = "/env/cns/submit_traces/SRA/SNTS_output_xml/CNS_BAN_2C5F3W6K6";
		XmlServices.writeAllXml(submissionCode, resultDirectory);
	}
	
	
	//@Test
	public void  debugJM_updateAC() throws IOException, SraException {
			//logger.info("test de argument {} pour les loggers {}", "arg_1", "other_args");
			String submissionCode = "CNS_BAN_2C5F3W6K6";
			String resultDirectory = "/env/cns/submit_traces/SRA/SNTS_output_xml/CNS_BAN_2C5F3W6K6";
			File fileEbi = new File("/env/cns/submit_traces/SRA/SNTS_output_xml/CNS_BAN_2C5F3W6K6/listAC_CNS_BAN_2C5F3W6K6_201712080354.txt");
			String user = "ngsrg";
			ContextValidation ctxVal = new ContextValidation(user);
			try {
				Submission submission = FileAcServices.traitementFileAC(ctxVal, submissionCode, fileEbi);
			} catch (MailServiceException e) {
				ctxVal.displayErrors(logger);	
				e.printStackTrace();
			} 
		}
		
	//@Test
	public void  updateAC() throws IOException, SraException {
		List<String> submissionCodes = new ArrayList<String>();
		submissionCodes.add("CNS_ART_254F1QHOV");
		submissionCodes.add("CNS_BCU_25U9550HF");
		submissionCodes.add("CNS_BCU_BLK_266H23OI3");
		submissionCodes.add("CNS_BDQ_BGX_BGU_27BB18OFB");
		submissionCodes.add("CNS_BHC_25CB4YW76");
		submissionCodes.add("CNS_BIL_274F26OJP");
		submissionCodes.add("CNS_BTR_26JA4HWSC");
		submissionCodes.add("CNS_BYQ_AWF_24RF4HJFI");
		submissionCodes.add("CNS_BZZ_CAN_28HD4ZIGG");
		submissionCodes.add("CNS_BZZ_CAN_28HD59RR8");
		submissionCodes.add("CNS_BZZ_CAN_28HD5FO6C");
		submissionCodes.add("CNS_BZZ_CAN_28HD5LO4V");
		submissionCodes.add("CNS_CAA_28HF4VOO4");
		for (String code : submissionCodes) {
			System.out.println("submissionCode = "+code);
			
			File fileEbi = new File("/env/cns/home/sgas/debug_extId/list_AC_"+ code +".txt");
			String user = "william";
			ContextValidation ctxVal = new ContextValidation(user);
			/*try {
				Submission submission = FileAcServices.traitementFileAC(ctxVal, code, fileEbi);
			} catch (MailServiceException e) {
				// TODO Auto-generated catch block
				ctxVal.displayErrors(logger);	
				e.printStackTrace();
			} */
			ctxVal.displayErrors(logger);	
			
		}
		
	}
	//@Test
	public void testfileAc_2()throws IOException, SraException, MailServiceException {
		File fileEbi = new File("/env/cns/home/sgas/test/listAC_CNS_ANU_29LH3R4QK_201709251245.txt");
		String user = "william";
		ContextValidation ctxVal = new ContextValidation(user);
		String submissionCode = "CNS_ANU_29LH3R4QK";
		Submission submission = FileAcServices.traitementFileAC(ctxVal, submissionCode, fileEbi); 
		ctxVal.displayErrors(logger);

	}
	//@Test
		public void testfileAc_3()throws IOException, SraException, MailServiceException {
		List<String> submissionCodes = new ArrayList<String>();
		//submissionCodes.add("CNS_CAA_2BF9169DR");
		submissionCodes.add("CNS_BZZ_CAN_2BF93SR6M");
	
		for (String code : submissionCodes) {
			File fileEbi = new File("/env/cns/home/sgas/EMOSE/listAC_"+code+".txt");
			System.out.println("FileEbi:" + fileEbi);
			String user = "william";
			ContextValidation ctxVal = new ContextValidation(user);
			System.out.println("submissionCode = "+code);
			try {
				Submission submission = FileAcServices.traitementFileAC(ctxVal, code, fileEbi);
			} catch (MailServiceException e) {
				// TODO Auto-generated catch block
				ctxVal.displayErrors(logger);	

				e.printStackTrace();
			}
		}
   }
	
}
