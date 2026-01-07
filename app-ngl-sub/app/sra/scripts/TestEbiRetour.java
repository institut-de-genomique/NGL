package sra.scripts;



import java.io.File;
import java.util.Date;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import fr.cea.ig.lfw.controllers.scripts.buffered.Script;
import fr.cea.ig.ngl.dao.sra.SubmissionDAO;
import models.sra.submit.sra.instance.Submission;
import services.sra.SraException;
import sra.api.SubmissionNewAPI;
import sra.services.EbiFileResponseServices;
import sra.services.EbiRetour;


/*
 * {@code http://localhost:9000/sra/scripts/run/sra.scripts.TestEbiRetour?submissionCode=code&ebiReport=C:\Users\sgas.IBFJ-EVRY\Ebireports\ebi_report_1.txt}
 * <br>
 * Si parametre absent dans url => declenchement d'une erreur.
 *  
 * @author sgas
 *
 */

public class TestEbiRetour extends  Script<TestEbiRetour.Args> { 

	private final SubmissionDAO             submissionDAO;
	private final SubmissionNewAPI          submissionNewAPI;
	private final EbiFileResponseServices   ebiFileResponseServices;
	private static final play.Logger.ALogger logger = play.Logger.of(ReloadEbiReport.class);

	
	@Inject
	public TestEbiRetour (SubmissionDAO             submissionDAO,
			      SubmissionNewAPI          submissionNewAPI,
			      EbiFileResponseServices   ebiFileResponseServices) {
		this.submissionDAO           = submissionDAO;
		this.submissionNewAPI        = submissionNewAPI;
		this.ebiFileResponseServices = ebiFileResponseServices;
	}
	

	public static class Args {  //package sra.scripts.ImportAcEbi.Args
		public String submissionCode;
		public String ebiReport; // Chemin du fichier retour des numeros d'accession de l'EBI.
	}
	
	
	@Override
	public void execute(Args args) throws Exception {
		File ebiReport = new File(args.ebiReport);
		Submission submission = submissionDAO.findByCode(args.submissionCode);
		EbiRetour ebiRetour = null;
		ebiRetour = ebiFileResponseServices.parseRetourEbi(ebiReport, submission);
		logger.debug("avant assert, ebiRetour.getAccessionStudy=" + ebiRetour.getAccessionStudy());
		logger.debug("avant assert, ebiRetour.getTypeRetour=" + ebiRetour.getTypeRetour());
		ebiFileResponseServices.assertEbiRetourValide(ebiRetour, submission);

		println("Rapport de soumission " + ebiRetour.getNameFileInput() + " ok pour la soumission " + args.submissionCode);
	}  

		
}