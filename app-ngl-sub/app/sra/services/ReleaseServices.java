package sra.services;


//import static ngl.refactoring.state.SRASubmissionStateNames.SUB_N;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.util.StringUtil;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import controllers.sra.api.AbstractSampleAPI;
import controllers.sra.api.ExperimentAPI;
import controllers.sra.api.StudyAPI;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.authentication.Authentication;
import fr.cea.ig.lfw.utils.Iterables;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.sra.AnalysisDAO;
import fr.cea.ig.ngl.dao.sra.ExperimentDAO;
import fr.cea.ig.ngl.dao.sra.SampleDAO;
import fr.cea.ig.ngl.dao.sra.StudyDAO;
import fr.cea.ig.ngl.dao.sra.SubmissionDAO;
import mail.MailServiceException;
import mail.MailServices;
import models.laboratory.common.instance.State;
import models.sra.submit.sra.instance.AbstractSample;
import models.sra.submit.sra.instance.Analysis;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.sra.instance.ExternalSample;
import models.sra.submit.sra.instance.Sample;
import models.sra.submit.sra.instance.Study;
import models.sra.submit.sra.instance.Submission;
import models.sra.submit.sra.instance.Submission.TypeRawDataSubmitted;
import models.utils.InstanceConstants;
import ngl.refactoring.state.SRASubmissionStateNames;
import services.sra.SraCodeHelper;
import services.sra.SraException;
import services.sra.SraValidationException;
import services.sra.VariableSRA;
import sra.api.SubmissionNewAPITools;
import validation.ContextValidation;

public class ReleaseServices  {

	private static final play.Logger.ALogger logger = play.Logger.of(ReleaseServices.class);

	private final StudyDAO              studyDAO;
	private final SampleDAO             sampleDAO;    // preferer DAO à API pour update car pas utile de valider ici (deja fait à la création) juste pour ajout de releaseDate
	                                                  // et on peut vouloir recharger rapport de soumission pour anciennes données non valides
	private final SubmissionDAO         submissionDAO;
	private final SraCodeHelper         sraCodeHelper;

	@Inject
	public ReleaseServices(
			SubmissionDAO         submissionDAO,
			StudyDAO              studyDAO,
			SampleDAO             sampleDAO,
			ExperimentDAO         experimentDAO,
			SraCodeHelper sraCodeHelper) {
		this.submissionDAO         = submissionDAO;
		this.studyDAO              = studyDAO;               
		this.sampleDAO             = sampleDAO;
		this.sraCodeHelper         = sraCodeHelper;
	}


	/**
	 * Construit la soumission correspondant à la release du study dont le code est passé
	 * en parametre et sauve la soumission dans la database avec l'etat SUBR_N
	 * Ajoute une erreur dans le contexte de validation si une erreur survient.
	 * @param  contextValidation  contexte de validation
	 * @param  studyCode          code du study
	 * @return                    code de la soumission nouvellement crée et sauvée dans la base.
	 * @throws SraException       error
	 */
	public String createSubmissionForReleaseStudy(ContextValidation contextValidation, String studyCode) throws SraException {
		//Sting  user = Authentication.getUser(); pose probleme si on fait tourner cron en isoprod sur ngl-data
		String user = contextValidation.getUser();
		// La soumission n'existe pas à ce stade donc pas de validation de state à faire
		Submission submission = null;
		Date courantDate = new java.util.Date();
		contextValidation.setCreationMode();		
		Study study = studyDAO.findByCode(studyCode);

		if (study == null) {
			String message = "Le studyCode " + studyCode + " n'existe pas dans base";
			throw new SraException("createSubmissionFromStudy", message);
		} 
		// NGL-4593
		if (StringUtils.isBlank(study.dropBox)) {
			throw new SraException("createSubmissionForReleaseStudy", "study " + study.code + " sans dropBox \n");
		}
		SraCodeHelper.assertObjectStateCode(study, SRASubmissionStateNames.SUB_F, contextValidation);
		if (study.traceInformation.createUser == null) {
			String message = "Le study " + studyCode 
					+ " n'est pas renseigné pour le champs createUser";	
			//throw new SraException("createSubmissionFromStudy", message);
			contextValidation.addError("createSubmissionFromStudy", message);
		}
		if (user == null) {
			String message = "l'utilisateur n'est pas authentifié";
			//throw new SraException("createSubmissionFromStudy", message);
			contextValidation.addError("createSubmissionFromStudy", message);
		}
		// suprression du controle utilisateur
		//		if (! study.traceInformation.createUser.equals(user)) {		
		//			String message = "l'utilisateur " + user + 
		//					" n'est pas autorisé à rendre publique le study " + study.code 
		//					+ " crée par " + study.traceInformation.createUser;
		//			throw new SraException("createSubmissionFromStudy", message);
		//		}

		//		if (submissionDAO.checkObjectExist(DBQuery.and(DBQuery.is("studyCode", studyCode),
		//				   									   DBQuery.is("type", Submission.Type.RELEASE)))) {

		if (submissionDAO.isObjectExist(DBQuery.and(DBQuery.is("studyCode", studyCode),
				DBQuery.notIn("state.code", SRASubmissionStateNames.SUB_F)))) {

			Iterable<Submission> it = submissionDAO.find(DBQuery.and(DBQuery.is("studyCode", studyCode),
					DBQuery.notIn("state.code", SRASubmissionStateNames.SUB_F)));
			String mess = Iterables.map(it, s->s.code)
					.surround("(", ", ", ")")
					.asString();
			String message = "Il existe deja des soumissions en cours " + mess 
					+ " impliquant le study " + studyCode;
			throw new SraException("createSubmissionFromStudy", message);
		}
		submission = new Submission(user, study.projectCodes);
		//		submission.code = sraCodeHelper.generateSubmissionCode(study.projectCodes);
		submission.code = sraCodeHelper.generateSubmissionCode(study.projectCodes);
		submission.traceInformation.creationDate = courantDate;

		// mettre à jour l'objet submission pour le cas d'une release de study :
		//		submission.release = true;
		submission.type = Submission.Type.RELEASE;
		submission.refStudyCodes.add(study.code);
		submission.studyCode = study.code;		
		submission.state = new State(SRASubmissionStateNames.SUBR_N, contextValidation.getUser());
		submission.submissionDirectory = VariableSRA.submissionRootDirectory + "/" + submission.code + "_release"; 	
		submission.ebiResult = "ebi_release_" + study.code + ".txt";
		submission.typeRawDataSubmitted = TypeRawDataSubmitted.withoutRawData;
		// NGL-4593
		submission.dropBox = study.dropBox;
		
		// valider et sauver submission
		submission.validate(contextValidation);
		if (contextValidation.hasErrors()) {
			throw new SraValidationException(contextValidation);
		}
		Submission dbSubmission = submissionDAO.save(submission);
		// Recuperer object study pour mettre historique des state et traceInformation à jour:
		study.updateStateAndTrace(submission.state);
		studyDAO.update(DBQuery.is("code", study.code), 
				DBUpdate.set("state", study.state)
				.set("traceInformation", study.traceInformation));
		//logger.debug("sauvegarde dans la base de la soumission" + submission.code);
		return dbSubmission.code;
	}


	/**
	 * Construit la soumission correspondant à la release du sample dont le code est passé
	 * en parametre et sauve la soumission dans la database avec l'etatn SUBR-N
	 * Ajoute une erreur dans le contexte de validation si une erreur survient.
	 * @param  ctxVal             contexte de validation
	 * @param  sampleCode         code du sample
	 * @param  user               nom de l'utilisateur
	 * @return                    code de la soumission nouvellement crée et sauvée dans la base.
	 * @throws SraException       error
	 */

	public String createSubmissionForReleaseSample(ContextValidation ctxVal, String sampleCode, String user) throws SraException {
		// La soumission n'existe pas à ce stade donc pas de validation de state à faire
		Submission submission = null;
		Date courantDate = new java.util.Date();
		ctxVal.setCreationMode();		
		Sample sample = sampleDAO.getObject(sampleCode);

		if (sample == null) {
			String message = "Le sampleCode " + sampleCode 
					+ " n'existe pas dans base";
			throw new SraException("createSubmissionForSampleRelease", message);
		} 
		// NGL-4593
		if (StringUtils.isBlank(sample.dropBox)) {
			throw new SraException("createSubmissionForSampleRelease", "sample " + sample.code + " sans dropBox \n");
		}
		SraCodeHelper.assertObjectStateCode(sample, SRASubmissionStateNames.SUB_F, ctxVal);
		if (sample.traceInformation.createUser == null) {
			String message = "Le sample " + sampleCode 
					+ " n'est pas renseigné pour le champs createUser";	
			//throw new SraException("createSubmissionFromStudy", message);
			ctxVal.addError("createSubmissionForSampleRelease", message);
		}
		if (user == null) {
			String message = "l'utilisateur n'est pas authentifié";
			//throw new SraException("createSubmissionFromStudy", message);
			ctxVal.addError("createSubmissionForSampleRelease", message);
		}
		// suprression du controle utilisateur
		//		if (! study.traceInformation.createUser.equals(user)) {		
		//			String message = "l'utilisateur " + user + 
		//					" n'est pas autorisé à rendre publique le study " + study.code 
		//					+ " crée par " + study.traceInformation.createUser;
		//			throw new SraException("createSubmissionFromStudy", message);
		//		}

		List <Submission> submissionList = submissionDAO.dao_find(DBQuery.and(DBQuery.in("sampleCodes", sampleCode),DBQuery.is("type", Submission.Type.RELEASE))).toList();
		if (submissionList.size() > 0) {
			String message = "sampleCode=" + sampleCode + " avec state=" + sample.state.code + "\n";
			for (Submission sub: submissionList) {
				message += "       - deja utilise par objet Submission de type RELEASE avec code : " + sub.code + "\n";
			}
			//logger.debug(message);
			throw new SraException("createSubmissionForSampleRelease", message);
		} 	

		List<String>projectCodes = new ArrayList<String>();
		projectCodes.add(sample.projectCode);
		submission = new Submission(user, projectCodes);
		//		submission.code = sraCodeHelper.generateSubmissionCode(study.projectCodes);
		submission.code = sraCodeHelper.generateSubmissionCode(projectCodes);
		submission.traceInformation.creationDate = courantDate;

		// mettre à jour l'objet submission pour le cas d'une release de sample :
		submission.type = Submission.Type.RELEASE;
		submission.refSampleCodes = new ArrayList<String>();		
		submission.refSampleCodes.add(sample.code);
		submission.sampleCodes = new ArrayList<String>();		
		submission.sampleCodes.add(sample.code);	

		submission.state = new State(SRASubmissionStateNames.SUBR_N, ctxVal.getUser());
		submission.submissionDirectory = VariableSRA.submissionRootDirectory + "/" + submission.code + "_release"; 	
		submission.ebiResult = "ebi_release_" + sample.code + ".txt";
		submission.typeRawDataSubmitted = TypeRawDataSubmitted.withoutRawData;
		// NGL-4593
		submission.dropBox = sample.dropBox;
		// valider et sauver submission
		submission.validate(ctxVal);
		if (ctxVal.hasErrors()) {
			throw new SraValidationException(ctxVal);
		}
		Submission dbSubmission = submissionDAO.save(submission);
		// Recuperer object sample pour mettre historique des state et traceInformation à jour:
		sample.updateStateAndTrace(submission.state);

		//		sampleDAO.update(DBQuery.is("code", sample.code), 
		//				 DBUpdate.set("state", sample.state)
		//						 .set("traceInformation", sample.traceInformation));
		sampleDAO.update(sample);
		//logger.debug("sauvegarde dans la base de la soumission" + submission.code);
		return dbSubmission.code;
	}

}
