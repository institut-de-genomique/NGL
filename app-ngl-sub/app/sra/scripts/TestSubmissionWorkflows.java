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
import models.laboratory.common.instance.State;
import models.sra.submit.sra.instance.Submission;
import services.sra.SraException;
import sra.api.SubmissionNewAPI;
import sra.services.EbiFileResponseServices;
import sra.services.EbiRetour;
import sra.workflows.SubmissionWorkflows;
import validation.ContextValidation;

import static ngl.refactoring.state.SRASubmissionStateNames.SUBR_FE;
import static ngl.refactoring.state.SRASubmissionStateNames.SUBR_SMD_IP;

import static ngl.refactoring.state.SRASubmissionStateNames.SUBR_SMD_FE;
import static ngl.refactoring.state.SRASubmissionStateNames.SUB_F;


/*
 * {@code http://localhost:9000/sra/scripts/run/sra.scripts.TestSubmissionWorkflows?submissionCode=code&ebiReport=C:\Users\sgas.IBFJ-EVRY\Ebireports\ebi_report_1.txt}
 * <br>
 * Si parametre absent dans url => declenchement d'une erreur.
 *  
 * @author sgas
 *
 */

public class TestSubmissionWorkflows extends  Script<TestSubmissionWorkflows.Args> { 

	private final SubmissionDAO             submissionDAO;
	private final SubmissionNewAPI          submissionNewAPI;
    private final SubmissionWorkflows       submissionWorkflows;

	private final EbiFileResponseServices   ebiFileResponseServices;
	private static final play.Logger.ALogger logger = play.Logger.of(ReloadEbiReport.class);

	
	@Inject
	public TestSubmissionWorkflows(SubmissionDAO             submissionDAO,
			      					SubmissionNewAPI          submissionNewAPI,
			      					SubmissionWorkflows       submissionWorkflows,
			      					EbiFileResponseServices   ebiFileResponseServices) {
		this.submissionDAO           = submissionDAO;
		this.submissionNewAPI        = submissionNewAPI;
		this.submissionWorkflows     = submissionWorkflows;
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
		println("Rapport de soumission " + ebiRetour.getNameFileInput() + " ok pour la soumission " + submission.code + " avec etat=" + submission.state.code);
		submission.state.code = SUBR_SMD_FE;
		logger.debug("dans execute, soumission " + submission.code + " mise dans l'etat " + submission.state.code + " et appel de submissionWorkflows.setState(SUB_F) => regarder log");
		String user = "sgas";
		ContextValidation ctxVal = ContextValidation.createUndefinedContext(user);
		submissionWorkflows.setState(ctxVal, submission, new State(SUB_F, user));
		logger.debug("dans fin execute, submission " + submission.code + " et etat=" + submission.state.code);
	}  

		
}