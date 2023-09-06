package sra.scripts;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
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
import models.sra.submit.sra.instance.Submission;
import models.sra.submit.util.SraException;
import services.SraEbiAPI;


/*
 * Script a lancer pour verifier soumission.submissionDate à partir des donnees de l'EBI.
 * Exemple de lancement :
 * http://localhost:9000/sra/scripts/run/sra.scripts.VerifSubmissionForEbiDate
 
 * @author sgas
 *
 */
public class VerifSubmissionForEbiDate extends ScriptNoArgs {
	
	private final SubmissionAPI submissionAPI;
	private final SraEbiAPI ebiAPI;	
	
	

	@Inject
	public VerifSubmissionForEbiDate (SubmissionAPI submissionAPI,
				    				  SraEbiAPI        ebiAPI
				    				  ) {
		this.submissionAPI = submissionAPI;
		this.ebiAPI        = ebiAPI;

	}
	

	public Date fetchEbiInfoSubmission(Submission submission) throws ParserConfigurationException, ParseException {
		Date sqlDate = null;

		if (StringUtils.isBlank(submission.accession) ) {
			println(submission.code + " sans numeros d'accession");
			return null;
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
					sqlDate = Date.valueOf(dateInString);
					//println("pour alias=" + alias + " ou ac=" + accession + ", sqlDate=" + sqlDate);
					submission.submissionDate = sqlDate;
					if(StringUtils.isBlank(submission.accession)) {
						//submission.accession = accession;
					} else {
						if (! submission.accession.equalsIgnoreCase(accession)) {
							throw new SraException("accession de la soumission " + submission.code + " different dans NGL-SUB ("+ submission.accession + ")" + " et a l'EBI (" + accession + ")");
						}
						
					}				

				}
			} // end for  
			return sqlDate;
			
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
		//List <Submission> listSubmissions =new ArrayList<Submission>();
//		for (String code: listSubCodes) {
//			Submission submission = submissionAPI.get(code);
//			if (submission != null) {
//				if (listSubmissions.contains(submission)) {
//					listSubmissions.add(submission);
//				}
//			}
//		}
		File fileSubmission_ok = new File("C:\\Users\\sgas\\debug\\submission_ok_.txt");

		Iterable<Submission> iterable = submissionAPI.dao_all();
		try (BufferedWriter output_buffer = new BufferedWriter(new java.io.FileWriter(fileSubmission_ok))) {
			
			for (Submission submission: iterable) {
				if (StringUtils.isNotBlank(submission.accession) && submission.type.equals(Submission.Type.CREATION)) {
					//println("submission.accession=" + submission.accession + "submission.date");
					Date date_ebi = null;
					try {
						date_ebi = fetchEbiInfoSubmission(submission);
						output_buffer.write("AC="+ submission.accession + ", code = " + submission.code + ", date="+ submission.submissionDate + ", date_ebi=" + date_ebi + "\n" );
						cp++;
						if(! submission.submissionDate.toString().equals(date_ebi.toString())) {
							println("Probleme de date pour la soumission" + submission.code + " => date = " + submission.submissionDate + " et ebi_date = "+ date_ebi);

						}
						if(! submission.state.code.equals("SUB-F")) {
							println("Probleme de state pour la soumission" + submission.code + " => state = " + submission.state.code);

						}
					} catch (ParserConfigurationException e) {
						e.printStackTrace();
					}
				}
				
				
			}
			println("Nombre de soumission traitées = " + cp);
		}
			
	}
	
	


}
