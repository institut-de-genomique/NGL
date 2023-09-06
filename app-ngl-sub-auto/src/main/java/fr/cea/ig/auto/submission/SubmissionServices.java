package fr.cea.ig.auto.submission;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import fr.genoscope.lis.devsi.birds.api.device.JSONDevice;
import fr.genoscope.lis.devsi.birds.api.entity.ResourceProperties;
import fr.genoscope.lis.devsi.birds.api.exception.BirdsException;
import fr.genoscope.lis.devsi.birds.api.exception.FatalException;
import fr.genoscope.lis.devsi.birds.api.exception.JSONDeviceException;
import fr.genoscope.lis.devsi.birds.extension.api.exception.MailServiceException;
import fr.genoscope.lis.devsi.birds.extension.api.service.IMailService;
import fr.genoscope.lis.devsi.birds.extension.impl.factory.MailServiceFactory;
import fr.genoscope.lis.devsi.birds.impl.properties.ProjectProperties;

public class SubmissionServices implements ISubmissionServices{

	private static Logger log = Logger.getLogger(SubmissionServices.class);
	
	
	//TODO
	@Override
	public Set<ResourceProperties> getRawDataResources(String submissionCode)
	{
		//TODO
		//Call NGL SUB Services to get rawData resources from submission
		//Convert JSON to JobResource
		return null;
	}

	@Override
	public void createXMLSubmission(String submissionCode, String submissionDirectory, String studyCode, String sampleCodes, String experimentCodes, String runCodes) throws IOException, FatalException, JSONDeviceException
	{

		log.debug("creation des fichiers xml pour l'ensemble de la soumission "+ submissionCode);
		log.debug("resultDirectory = " + submissionDirectory);
		log.debug("studyCode "+studyCode+" sampleCodes "+sampleCodes+" experimentCodes "+experimentCodes+" runCodes "+runCodes);

		IXMLServices xmlServices = XMLServicesFactory.getInstance();
		File studyFile = null;
		// si on est dans soumission de données :
		if (SRAFilesUtil.isNotNullValue(studyCode)) {	
			studyFile = new File(submissionDirectory + File.separator + ProjectProperties.getProperty("xmlStudies"));
			xmlServices.writeStudyXml(studyFile, studyCode);
		}
		File sampleFile = null;
		if (SRAFilesUtil.isNotNullValue(sampleCodes)){
			sampleFile = new File(submissionDirectory + File.separator + ProjectProperties.getProperty("xmlSamples"));
			xmlServices.writeSampleXml(sampleFile, sampleCodes); 
		}
		File experimentFile = null;
		if (SRAFilesUtil.isNotNullValue(experimentCodes)){
			log.debug("Create experiment file");
			experimentFile = new File(submissionDirectory + File.separator + ProjectProperties.getProperty("xmlExperiments"));
			xmlServices.writeExperimentXml(experimentFile, experimentCodes); 
		} else {
			log.debug("experimentCodes==0 ??????????");
		}
		File runFile = null;
		if (SRAFilesUtil.isNotNullValue(runCodes)){
			log.debug("create run file");
			runFile = new File(submissionDirectory + File.separator + ProjectProperties.getProperty("xmlRuns"));
			xmlServices.writeRunXml(runFile, runCodes); 
		} else {
			log.debug("runCodes==0 ??????????");
		}

		File submissionFile = new File(submissionDirectory + File.separator + ProjectProperties.getProperty("xmlSubmission"));
		xmlServices.writeSubmissionXml(submissionFile, submissionCode, studyCode, sampleCodes, experimentCodes);
		
		//Update xml fields
		JSONDevice jsonDevice = new JSONDevice();
		String fields = "?fields=xmlSubmission";
		String submission = "{\"xmlSubmission\":\""+submissionFile.getName()+"\"";
		if(studyFile!=null){
			submission+=",\"xmlStudys\":\""+studyFile.getName()+"\"";
			fields+="&fields=xmlStudys";
		}
		if(sampleFile!=null){
			submission+=",\"xmlSamples\":\""+sampleFile.getName()+"\"";
			fields+="&fields=xmlSamples";
		}
		if(experimentFile!=null){
			submission+=",\"xmlExperiments\":\""+experimentFile.getName()+"\"";
			fields+="&fields=xmlExperiments";
		}
		if(runFile!=null){
			submission+=",\"xmlRuns\":\""+runFile.getName()+"\"";
			fields+="&fields=xmlRuns";
		}
		submission+="}";
		//Call PUT update with submission modified
		log.debug("Call PUT "+ProjectProperties.getProperty("server")+"/api/sra/submissions/"+submissionCode+fields);
		log.debug("with JSON "+submission);
		jsonDevice.httpPut(ProjectProperties.getProperty("server")+"/api/sra/submissions/"+submissionCode+fields, submission,"bot");

	}


	@Override
	public void createXMLRelease(String submissionCode, String submissionDirectory, String studyCode) throws BirdsException, IOException, FatalException
	{
		File submissionFile = new File(submissionDirectory + File.separator +"submission.xml");

		if(studyCode==null || !SRAFilesUtil.isNotNullValue(studyCode)){
			throw new BirdsException("Impossible de faire la soumission pour release " + submissionCode + " sans studyCode");

		}

		IXMLServices xmlServices = XMLServicesFactory.getInstance();
		xmlServices.createXMLRelease(submissionFile, submissionCode, studyCode);
	}

	@Override
	public boolean treatmentFileSubmission(String ebiFileName, String submissionCode, String studyCode, String sampleCodes, String experimentCodes, String runCodes, String creationUser) throws FatalException, BirdsException, UnsupportedEncodingException
	{
		if(ebiFileName==null)
			throw new FatalException("Pas de fichier retour ebi");
		File retourEbiSub = new File(ebiFileName);

		if (! retourEbiSub.exists()){
			throw new BirdsException("Fichier resultat de l'ebi pour la release absent des disques : "+ retourEbiSub.getAbsolutePath());
		}

		boolean ebiSuccess =false;
		String ebiSubmissionCode = null;
		String submissionAc = null;
		String ebiStudyCode = null;
		String studyAc = null;
		String studyExtId = null;
		Map<String, String> mapSamples = new HashMap<String, String>(); 
		Map<String, String> mapExtIdSamples = new HashMap<String, String>(); 
		Map<String, String> mapExperiments = new HashMap<String, String>(); 
		Map<String, String> mapRuns = new HashMap<String, String>(); 
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

			final Document document= builder.parse(retourEbiSub);
			//Affiche du prologue
			log.debug("*************PROLOGUE************");
			log.debug("version : " + document.getXmlVersion());
			log.debug("encodage : " + document.getXmlEncoding());      
			log.debug("standalone : " + document.getXmlStandalone());
			/*
			 * Etape 4 : récupération de l'Element racine
			 */
			final Element racine = document.getDocumentElement();
			//Affichage de l'élément racine
			log.debug("\n*************RACINE************");
			log.debug(racine.getNodeName());
			log.debug("success = " + racine.getAttribute("success"));
			//System.out.println(((Node) racine).getNodeName());
			//System.out.println("success = " + ((DocumentBuilderFactory) racine).getAttribute("success"));
			/*
			 * Etape 5 : récupération des samples
			 */



			final NodeList racineNoeuds = racine.getChildNodes();
			final int nbRacineNoeuds = racineNoeuds.getLength();



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
						final Element eltExtId = (Element) elt.getElementsByTagName("EXT_ID").item(0);
						studyExtId = eltExtId.getAttribute("accession");
						System.out.println("study_ext_id: " + studyExtId);
					} else if(elt.getTagName().equalsIgnoreCase("SAMPLE")) {
						mapSamples.put(elt.getAttribute("alias"), elt.getAttribute("accession"));	
						final Element eltExtId = (Element) elt.getElementsByTagName("EXT_ID").item(0);
						//String sampleExtId = eltExtId.getAttribute("accession");
						mapExtIdSamples.put(elt.getAttribute("alias"), eltExtId.getAttribute("accession"));	
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
		String message = null;
		String sujet = null;

		if (! ebiSuccess ) {
			log.debug("success=true absent du fichier  " + ebiFileName);
			message = "Absence de la ligne RECEIPT ... pour  " + submissionCode + " dans fichier "+ ebiFileName;
			sendMail(creationUser, "ngl-sub : Ebi Accession Reporting error", new String(message.getBytes(), "iso-8859-1"));
			return false;
		}

		// Mise à jour des objets :
		Boolean error = false;
		sujet = "Probleme parsing fichier des AC : ";
		message = "Pour la soumission " + submissionCode + ", le fichier des AC "+ ebiFileName + "\n";

		if (ebiSubmissionCode==null || !SRAFilesUtil.isNotNullValue(ebiSubmissionCode)) {
			//System.out.println("Pas de Recuperation de ebiSubmissionCode");
			message += "- ne contient pas ebiSubmissionCode \n";
			error = true;
		} 
		if (! ebiSubmissionCode.equals(submissionCode)) {
			//System.out.println("ebiSubmissionCode != submissionCode");
			message += "- contient un ebiSubmissionCode ("  + ebiSubmissionCode + ") different du submissionCode passé en parametre "+ submissionCode + "\n"; 
			error = true;
		}
		// Verifier que le nombre d'ac recuperés dans le fichier est bien celui attendu pour l'objet submission:
		if (studyCode== null || !SRAFilesUtil.isNotNullValue(studyCode)) {
			if (studyAc== null || !SRAFilesUtil.isNotNullValue(studyAc)) {
				//System.out.println("studyAc attendu non trouvé pour " + submission.studyCode);
				message += "- ne contient pas de valeur pour le studyCode " + studyCode+"\n";
			}
		}
		if (SRAFilesUtil.isNotNullValue(sampleCodes)){
			String[] tabSampleCodes = sampleCodes.split(",");
			for (int i = 0; i < tabSampleCodes.length ; i++) {
				String sampleCode = tabSampleCodes[i].replaceAll("\"", "");
				if (!mapSamples.containsKey(sampleCode)){
					//System.out.println("sampleAc attendu non trouvé pour " + submission.sampleCodes.get(i));
					message += "- ne contient pas d'AC pour le sampleCode " + sampleCode+"\n";
					error = true;

				}	
			}
		}
		if (SRAFilesUtil.isNotNullValue(experimentCodes)){
			String[] tabExperimentCodes = experimentCodes.split(",");
			for (int i = 0; i < tabExperimentCodes.length ; i++) {
				String experimentCode = tabExperimentCodes[i].replaceAll("\"", "");
				if (!mapExperiments.containsKey(experimentCode)){
					//System.out.println("experimentAc attendu non trouvé pour " + submission.experimentCodes.get(i));
					message += "- ne contient pas d'AC pour l'experimentCode " + experimentCode + "\n";
					error = true;
				}	
			}
		}
		if (SRAFilesUtil.isNotNullValue(runCodes)){
			String[] tabRunCodes = runCodes.split(",");
			for (int i = 0; i < tabRunCodes.length ; i++) {
				String runCode = tabRunCodes[i].replaceAll("\"", "");
				//System.out.println("runCode========="+ submission.runCodes.get(i));
				if (!mapRuns.containsKey(runCode)){
					//System.out.println("runAc attendu non trouvé pour " + submission.runCodes.get(i));
					message += "- ne contient pas d'AC pour le runCode " + runCode + "\n";
					error = true;
				}	
			}
		}

		if (error){
			sendMail(creationUser, sujet, new String(message.getBytes(), "iso-8859-1"));
			return false;
		} 

		Calendar calendar = Calendar.getInstance();
		Date date  = calendar.getTime();
		calendar.add(Calendar.YEAR, 2);
		Date release_date  = calendar.getTime();

		message = "Liste des AC attribues pour la soumission "  + submissionCode + " en mode confidentiel jusqu'au : " + release_date +" \n\n";

		message += "submissionCode = " + submissionCode + ",   AC = "+ submissionAc + "\n";  
		updateSubmissionAC(submissionCode, submissionAc);
		
		if (SRAFilesUtil.isNotNullValue(ebiStudyCode)) {	
			message += "studyCode = " + ebiStudyCode + ",   AC = "+ studyAc + ",   BioProjectId = "+studyExtId+"\n";  
			updateStudyAC(ebiStudyCode, studyAc, studyExtId);
		}
		for(Entry<String, String> entry : mapSamples.entrySet()) {
			String code = entry.getKey();
			String ac = entry.getValue();
			String ext_id_ac = mapExtIdSamples.get(code);
			message += "sampleCode = " + code + ",   AC = "+ ac + ", External Id "+ext_id_ac+"\n";  
			updateSampleAC(code, ac, ext_id_ac);
			
		}

		for(Entry<String, String> entry : mapExperiments.entrySet()) {
			String code = entry.getKey();
			String ac = entry.getValue();
			message += "experimentCode = " + code + ",   AC = "+ ac + "\n";  
			updateExperimentAC(code, ac);
		}

		for(Entry<String, String> entry : mapRuns.entrySet()) {
			String code = entry.getKey();
			String ac = entry.getValue();
			message += "runCode = " + code + ",   AC = "+ ac  + "\n";  
			updateRunAC(code, ac);
		}

		sendMail(creationUser, "ngl-sub : Ebi Accession Reporting success", new String(message.getBytes(), "iso-8859-1"));
			
		return ebiSuccess;
	}

	private void updateSubmissionAC(String code, String accession) throws FatalException, JSONDeviceException
	{
		//Update submission acNumber and date
		JSONDevice jsonDevice = new JSONDevice();
		String submission = "{\"code\":\""+code+"\",\"accession\":\""+accession+"\"}";
		//Call PUT update with submission modified
		log.debug("Call PUT "+ProjectProperties.getProperty("server")+"/api/sra/submissions/"+code+"?fields=accession");
		log.debug("with JSON "+submission);
		jsonDevice.httpPut(ProjectProperties.getProperty("server")+"/api/sra/submissions/"+code+"?fields=accession", submission,"bot");
	}

	private void updateStudyAC(String code, String accession, String externalId) throws FatalException, JSONDeviceException
	{
		JSONDevice jsonDevice = new JSONDevice();
		String study = "{\"code\":\""+code+"\",\"accession\":\""+accession+"\",\"externalId\":\""+externalId+"\"}";
		//Call PUT update with submission modified
		log.debug("Call PUT "+ProjectProperties.getProperty("server")+"/api/sra/studies/internal/"+code+"?fields=accession&fields=externalId");
		log.debug("with JSON "+study);
		jsonDevice.httpPut(ProjectProperties.getProperty("server")+"/api/sra/studies/internal/"+code+"?fields=accession&fields=externalId", study,"bot");
	}

	private void updateSampleAC(String code, String accession, String externalId) throws FatalException, JSONDeviceException
	{
		JSONDevice jsonDevice = new JSONDevice();
		String sample = "{\"code\":\""+StringEscapeUtils.escapeHtml(code)+"\",\"accession\":\""+accession+"\",\"externalId\":\""+externalId+"\"}";
		//Call PUT update with submission modified
		log.debug("Call PUT "+ProjectProperties.getProperty("server")+"/api/sra/samples/internal/"+code+"?fields=accession&fields=externalId");
		log.debug("with JSON "+sample);
		jsonDevice.httpPut(ProjectProperties.getProperty("server")+"/api/sra/samples/internal/"+code+"?fields=accession&fields=externalId", sample,"bot");
	}
	
	private void updateExperimentAC(String code, String accession) throws FatalException, JSONDeviceException
	{
		JSONDevice jsonDevice = new JSONDevice();
		String experiment = "{\"code\":\""+code+"\",\"accession\":\""+accession+"\"}";
		//Call PUT update with submission modified
		log.debug("Call PUT "+ProjectProperties.getProperty("server")+"/api/sra/experiments/"+code+"?fields=accession");
		log.debug("with JSON "+experiment);
		jsonDevice.httpPut(ProjectProperties.getProperty("server")+"/api/sra/experiments/"+code+"?fields=accession", experiment,"bot");
	}
	
	private void updateRunAC(String code, String accession) throws FatalException, JSONDeviceException
	{
		JSONDevice jsonDevice = new JSONDevice();
		String run = "{\"code\":\""+code+"\",\"accession\":\""+accession+"\"}";
		//Call PUT update with submission modified
		log.debug("Call PUT "+ProjectProperties.getProperty("server")+"/api/sra/experiments/run/"+code+"?fields=accession");
		log.debug("with JSON "+run);
		jsonDevice.httpPut(ProjectProperties.getProperty("server")+"/api/sra/experiments/run/"+code+"?fields=accession", run,"bot");
	}

	@Override
	public boolean treatmentFileRelease(String ebiFileName, String submissionCode, String accessionStudy, String studyCode, String creationUser) throws FatalException, BirdsException, UnsupportedEncodingException
	{
		if(ebiFileName==null)
			throw new FatalException("Pas de fichier retour ebi");
		File retourEbiRelease = new File(ebiFileName);

		if (! retourEbiRelease.exists()){
			throw new BirdsException("Fichier resultat de l'ebi pour la release absent des disques : "+ retourEbiRelease.getAbsolutePath());
		}

		log.debug("Parse ebi file "+ebiFileName);
		boolean ebiSuccess = false;
		String message = null;
		String infos = null;
		String studyAccession = null;
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

			final Document document= builder.parse(retourEbiRelease);
			//Affiche du prologue
			/*System.out.println("*************PROLOGUE************");
			System.out.println("version : " + document.getXmlVersion());
			System.out.println("encodage : " + document.getXmlEncoding());      
			System.out.println("standalone : " + document.getXmlStandalone());
			 */
			/*
			 * Etape 4 : récupération de l'Element racine
			 */
			final Element racine = document.getDocumentElement();
			//Affichage de l'élément racine
			/*System.out.println("\n*************RACINE************");
			System.out.println(racine.getNodeName());
			System.out.println("success = " + racine.getAttribute("success"));
			 */
			final NodeList racineNoeuds = racine.getChildNodes();
			final int nbRacineNoeuds = racineNoeuds.getLength();

			log.debug("Nombre de racine noeud = "+ nbRacineNoeuds);

			if( racine.getAttribute("success").equalsIgnoreCase ("true")){
				ebiSuccess = true;
			} else {
				ebiSuccess = false;
				message = "Absence de la ligne RECEIPT ... pour  " + submissionCode + " dans fichier "+ retourEbiRelease.getPath();
			}
			for (int i = 0; i<nbRacineNoeuds; i++) {
				if(racineNoeuds.item(i).getNodeType() == Node.ELEMENT_NODE) {
					final Element elt = (Element) racineNoeuds.item(i);
					//Affichage d'un elt :
					/*System.out.println("\n*************Elt************");
					System.out.println("localName="+elt.getLocalName());
					System.out.println("nodeName="+ elt.getNodeName());
					System.out.println("nodeValue="+elt.getNodeValue());
					System.out.println("nodeType="+elt.getNodeType());
					System.out.println("textContent="+elt.getTextContent());	
					 */
					if (elt.getNodeName().equals("MESSAGES")){
						if (elt.getElementsByTagName("INFO").item(0) != null){
							infos = elt.getElementsByTagName("INFO").item(0).getTextContent();
							//System.out.println("infos="+ infos);
							String pattern = "study accession \"([^\"]+)\" is set to public status";
							java.util.regex.Pattern p = Pattern.compile(pattern);
							Matcher m = p.matcher(infos);
							if ( m.find() ) { 
								studyAccession = m.group(1);
								log.debug("studyAccession="+ studyAccession);
							}
						}
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

		if (SRAFilesUtil.isNotNullValue(studyAccession)){
			if(studyAccession.equals(accessionStudy)) {
				log.debug("studyAccession :'"+ studyAccession + "' ==  study.accession :'" +  accessionStudy +"'");
				ebiSuccess = true;
				message = "Objets lies au studyAccession = " + studyAccession + " mis dans le domaine public via la soumission "+ submissionCode + "</br>"; 
			} else {
				ebiSuccess = false;
				log.debug("studyAccession :'"+ studyAccession + "' !=  study.accession :'" +  accessionStudy +"' pour study.code = '"+ studyCode +"'");
				message = "La soumission ."+ submissionCode + " indique un study à releaser "+ accessionStudy + " different du studyAccession indiqué dans " + retourEbiRelease.getName();
			}
		} else {
			log.debug("Pas de recuperation du studyAccession");
			message = "La soumission ."+ submissionCode + " a un retour incorrect " + retourEbiRelease.getName();
		}

		//Send mail
		if (! ebiSuccess ) {
			// mettre status à jour
			sendMail(creationUser, "ngl-sub : Ebi Accession Reporting error", new String(message.getBytes(), "iso-8859-1"));
		} else {
			message = "Objets lies au studyAccession = " + studyAccession + " mis dans le domaine public via la soumission "+ submissionCode + "\n"; 
			sendMail(creationUser, "ngl-sub : Ebi Accession Reporting success", new String(message.getBytes(), "iso-8859-1"));
		}
		return ebiSuccess;
	}

	@Override
	public void sendMail(String creationUser, String subject, String message) throws FatalException, MailServiceException, UnsupportedEncodingException
	{
		IMailService mailService = MailServiceFactory.getInstance();

		String from = ProjectProperties.getProperty(ProjectProperties.ADMIN_EMAIL);
		String[] emailTo = ProjectProperties.getProperty(ProjectProperties.ADDRESSES_EMAIL).split(",");
		Set<String> to = new HashSet<String>(Arrays.asList(emailTo));

		if(creationUser!=null && !creationUser.equals("ngsrg") && !creationUser.equals("")){
			to.add(creationUser);
		}

		mailService.sendMail(from, to, subject, new String(message.getBytes(), "iso-8859-1"), null);

	}


}
