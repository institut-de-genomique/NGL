package sra.scripts;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import fr.cea.ig.ngl.dao.api.sra.SubmissionAPI;
import models.sra.submit.common.instance.Submission;
import services.EbiAPI;


/*
 * Script a lancer pour mettre a jour la base pour soumission.submissionDate à partir des donnees de l'EBI.
 * Exemple de lancement :
 * http://localhost:9000/sra/scripts/run/sra.scripts.UpdateSubmissionForEbiDate

 * @author sgas
 *
 */
public class UpdateSubmissionForEbiDate extends ScriptNoArgs {
	
	private final SubmissionAPI submissionAPI;
	private final EbiAPI ebiAPI;	

	
	

	@Inject
	public UpdateSubmissionForEbiDate (SubmissionAPI submissionAPI,
				    				   EbiAPI        ebiAPI) {
		this.submissionAPI = submissionAPI;
		this.ebiAPI        = ebiAPI;

	}
	
	
	public void fetchEbiInfoSubmission(Submission submission) throws ParserConfigurationException, ParseException {
		if (StringUtils.isBlank(submission.accession) ) {
			println(submission.code + " sans numeros d'accession");
			return;
		}
			
		String xmlString = ebiAPI.ebiSubmissionXml(submission.accession);
//		if (xmlString.contains(":403")) { // 403 : problemes de droits:
//			// relancer requete 1 fois:
//			xmlString = ebiAPI.ebiSubmissionXml(submission.accession);
//		}
		
		
		/*
		 * Etape 1 : récupération d'une instance de la classe "DocumentBuilderFactory"
		 */
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		/*
		 * Etape 2 : création d'un parseur
		 */
		final DocumentBuilder builder = factory.newDocumentBuilder();
		/*
		 * Etape 3 : création d'un Document
		 */
		Document document;
		try {
			InputStream targetStream = new ByteArrayInputStream(xmlString.getBytes());
			//				document = builder.parse(xmlSamples);
			document = builder.parse(targetStream);

			/*
			 * Etape 4 : récupération de l'Element racine
			 */
			final Element racine = document.getDocumentElement();
			//Affichage de l'élément racine
			//logger.debug("\n*************RACINE************");
			//logger.debug(racine.getNodeName());
			/*
			 * Etape 5 : récupération des samples
			 */
			final NodeList racineNoeuds = racine.getChildNodes();
			final int nbRacineNoeuds = racineNoeuds.getLength();

			for (int i = 0; i<nbRacineNoeuds; i++) {
				if(racineNoeuds.item(i).getNodeType() == Node.ELEMENT_NODE) {
					final Element elt = (Element) racineNoeuds.item(i);

					//String alias = elt.getAttribute("alias");
					String accession = elt.getAttribute("accession");
					String dateInString = elt.getAttribute("submission_date");
					dateInString = dateInString.substring(0, 10);
					//println("pour alias=" + alias + " ou ac=" + accession + ", dateInString=" +dateInString);

					//println("dateInString %s", dateInString);
					//SimpleDateFormat formatter = new SimpleDateFormat("yyyy-mm-dd");
					//Date date = formatter.parse(dateInString);
					Date sqlDate = Date.valueOf(dateInString);
					//println("pour alias=" + alias + " ou ac=" + accession + ", sqlDate=" + sqlDate);
					submission.submissionDate = sqlDate;
					if(StringUtils.isBlank(submission.accession)) {
						submission.accession = accession;
					}
					submissionAPI.dao_saveObject(submission);


				}
			} // end for  

		} catch (SAXException |IOException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	

	@Override
	public void execute() throws Exception {
		//writeEbiSubmissionDate(new File(args.outputFile));
		int cp = 0;
		List<String> listSubCodes = new ArrayList<String>();
		listSubCodes.add("CNS_BYQ_AWF_24RF4HJFI");
		List <Submission> listSubmissions =new ArrayList<Submission>();
		for (String code: listSubCodes) {
			Submission submission = submissionAPI.get(code);
			if (submission != null) {
				if (listSubmissions.contains(submission)) {
					listSubmissions.add(submission);
				}
			}
		}
		for (Submission submission : submissionAPI.dao_all()) {
		//for (Submission submission : listSubmissions) {
			if (StringUtils.isNotBlank(submission.accession) && submission.type.equals(Submission.Type.CREATION)) {
				println("submission.accession=" + submission.accession);
				try {
					fetchEbiInfoSubmission(submission);
					cp++;
				} catch (ParserConfigurationException e) {
					e.printStackTrace();
				}
			}
		}
		println("Nombre de soumission traitées = " + cp);
	}
			
	
	
	


}
