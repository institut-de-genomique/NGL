package sra.scripts.off;
//package sra.scripts.generic;
//
//
//
//import fr.cea.ig.MongoDBDAO;
//import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
//import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptWithArgsAndExcelBody;
//import fr.cea.ig.lfw.controllers.scripts.chunked.ScriptWithArgs;
//import fr.cea.ig.ngl.NGLApplication;
//import fr.cea.ig.ngl.dao.analyses.AnalysesDAO;
//import fr.cea.ig.ngl.dao.api.APIException;
//import fr.cea.ig.ngl.dao.api.APISemanticException;
//import fr.cea.ig.ngl.dao.api.APIValidationException;
//import fr.cea.ig.ngl.dao.api.AnalysisAPI;
//import fr.cea.ig.ngl.dao.readsets.ReadSetsAPI;
//import fr.cea.ig.ngl.dao.sra.AtlaseaDAO;
//import fr.cea.ig.ngl.dao.sra.ExperimentDAO;
//import fr.cea.ig.ngl.dao.sra.ProjectDAO;
//import fr.cea.ig.ngl.dao.sra.StudyDAO;
//import fr.cea.ig.ngl.dao.sra.SubmissionDAO;
//import models.laboratory.common.instance.Comment;
//import models.laboratory.common.instance.State;
//import models.laboratory.common.instance.property.PropertySingleValue;
//import models.laboratory.run.instance.Analysis;
//import models.laboratory.run.instance.ReadSet;
//import models.sra.submit.sra.instance.AbstractStudy;
//import models.sra.submit.sra.instance.Atlasea;
//import models.sra.submit.sra.instance.EmailAlert;
//import models.sra.submit.sra.instance.Experiment;
//import models.sra.submit.sra.instance.OldTolid;
//import models.sra.submit.sra.instance.Project;
//import models.sra.submit.sra.instance.Study;
//import models.sra.submit.sra.instance.Submission;
//import models.sra.submit.sra.instance.Submission.TypeRawDataSubmitted;
//import models.utils.InstanceConstants;
//import play.libs.Json;
//import services.sra.NGLSUBConfig;
//import services.sra.SraException;
//import sra.api.SubmissionNewAPI;
//import sra.api.SubmissionNewAPITools;
//import sra.services.XmlServices;
//import sra.services.crons.SubmissionAtlaseaProjectsToEbi;
//import validation.ContextValidation;
//
//import static ngl.refactoring.state.SRASubmissionStateNames.NONE;
//import static ngl.refactoring.state.SRASubmissionStateNames.SUB_SMD_IP;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.OutputStream;
//import java.text.DateFormat;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.regex.Pattern;
//
//import javax.inject.Inject;
//
//import org.apache.commons.lang3.StringUtils;
//import org.apache.poi.ss.usermodel.Row;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//import org.mongojack.DBQuery;
//import org.mongojack.DBUpdate;
//
//import controllers.sra.api.SubmissionAPI;
//import controllers.sra.atlaseas.api.AtlaseaCommentsAPI;
//import controllers.sra.projects.api.ProjectCommentsAPI;
//import controllers.sra.studies.api.StudyCommentsAPI;
//import controllers.sra.submissions.api.SubmissionCommentsAPI;
//
//import org.mongojack.DBQuery.Query;
//
//
//// NGL-5171 dev abandonné le 26/09/2025 lors de la reunion NGL-SUB Atlasea.
//// curl -X POST -H 'User-Agent: bot' -H 'Content-Type: multipart/form-data' 
//// -v "http://localhost:9000/sra/scripts/run/sra.scripts.generic.RepriseHistoAtlaseaUmbrellaTitle" 
//
//
//public class RepriseHistoAtlaseaUmbrellaTitle extends ScriptNoArgs {
//	private final List<String> projectAtlaseaCodes       = Arrays.asList("DKB", "DKC", "DKD", "DKE", "DKF");
//	private final SubmissionDAO          submissionDAO;
////	private final SubmissionCommentsAPI  submissionCommentsAPI;
////	private final ProjectCommentsAPI     projectCommentsAPI;
////	private final StudyCommentsAPI       studyCommentsAPI;
////	private final AtlaseaCommentsAPI     atlaseaCommentsAPI;
//	private final XmlServices            xmlServices;
//	private final  SubmissionNewAPI      submissionNewAPI;
////	private final AtlaseaDAO             atlaseaDAO;
//	private final AnalysesDAO            biAnalysisDAO;
//	private final ProjectDAO             projectDAO;
////	private final StudyDAO               studyDAO;
////	private final ExperimentDAO          experimentDAO;
//	private final SubmissionAPI          submissionAPI;
//	public final ReadSetsAPI             laboReadsetAPI;
//	private final String                 submissionAtlaseaRootResultDirectory; 
//	private final String                 ebiCurl;
//    private final NGLSUBConfig           config;
//	private final NGLApplication         app;
//	private final String                 dropBox           = "Webin-9";
//    private final String                 user              = "sgas";
//
//	
//	@Inject
//	public RepriseHistoAtlaseaUmbrellaTitle(
//			NGLSUBConfig                    config,  
//            SubmissionNewAPI                submissionNewAPI,
//			SubmissionDAO                   submissionDAO,
////			SubmissionCommentsAPI           submissionCommentsAPI,
////			ProjectCommentsAPI              projectCommentsAPI,
////			StudyCommentsAPI                studyCommentsAPI,
////			AtlaseaCommentsAPI              atlaseaCommentsAPI,
//			SubmissionAPI                   submissionAPI,
//			XmlServices                     xmlServices,
////			AtlaseaDAO                      atlaseaDAO,
//			AnalysesDAO                     biAnalysisDAO,
//			ProjectDAO                      projectDAO,
////			StudyDAO                        studyDAO,
////			ExperimentDAO                   experimentDAO,
//			ReadSetsAPI                     laboReadsetAPI,
//			NGLApplication                  app) {
//		this.submissionAtlaseaRootResultDirectory = config.getCreationProjectsAtlaseaRootDirectory(); 
//	    this.config                               = config;
//		this.ebiCurl                              = config.getEbiCurl();
//		this.submissionDAO                        = submissionDAO;
////		this.submissionCommentsAPI                = submissionCommentsAPI;
////		this.projectCommentsAPI                   = projectCommentsAPI;
////		this.studyCommentsAPI                     = studyCommentsAPI;
////		this.atlaseaCommentsAPI                   = atlaseaCommentsAPI;
//		this.app                                  = app;
////		this.atlaseaDAO                           = atlaseaDAO;
//		this.biAnalysisDAO                        = biAnalysisDAO;
//		this.projectDAO                           = projectDAO;
////		this.studyDAO                             = studyDAO;
////		this.experimentDAO                        = experimentDAO;
//		this.xmlServices                          = xmlServices;
//		this.submissionAPI                        = submissionAPI;
//        this.submissionNewAPI                     = submissionNewAPI;
//        this.laboReadsetAPI                       = laboReadsetAPI;
//
//	}
//
//
//
//
//	public Submission buildDebugStudyUpdateSubmission(String tolId, String studyCode, String submissionCode, ContextValidation ctx) {
//		Date courantDate = new java.util.Date();
//		Submission submission = new Submission(user);
//		submission.code = submissionCode;
//		submission.projectCodes = this.projectAtlaseaCodes;
//		submission.traceInformation.creationDate = courantDate;
//		submission.state = new State(SUB_SMD_IP, user);
//		submission.type = Submission.Type.UPDATE;
//		submission.typeRawDataSubmitted = TypeRawDataSubmitted.withoutRawData;
//		submission.dropBox = this.dropBox;
//		submission.submissionDirectory = this.submissionAtlaseaRootResultDirectory + tolId + "/";	
//		submission.ebiResult = "ebi_AC_" + submission.code + ".txt";
//		submission.studyCode = studyCode;
//		ctx.setCreationMode();
//		submission.validate(ctx);
//		SraException.assertNoError(ctx);
//		return submissionDAO.save(submission);
//	}	
//	
//	
//	public Submission buildDebugUmbrellaUpdateSubmission(Atlasea atlasea, String submissionCode, ContextValidation ctx) {
//		String umbrellaCode = atlasea.umbrellaCode;
//		String tolId = atlasea.tolId;
//		Date courantDate = new java.util.Date();
//		Submission submission = new Submission(user);
//		submission.code = submissionCode;
//		submission.projectCodes = this.projectAtlaseaCodes;
//		submission.traceInformation.creationDate = courantDate;
//		submission.state = new State(SUB_SMD_IP, user);
//		submission.type = Submission.Type.UPDATE;
//		submission.typeRawDataSubmitted = TypeRawDataSubmitted.withoutRawData;
//		submission.dropBox = this.dropBox;
//		submission.submissionDirectory = this.submissionAtlaseaRootResultDirectory + tolId + "/";	
//		submission.ebiResult = "ebi_AC_" + submission.code + ".txt";
//		submission.umbrellaCode = umbrellaCode;
//		ctx.setCreationMode();
//		submission.validate(ctx);
//		SraException.assertNoError(ctx);
//		return submissionDAO.save(submission);
//	}
//
//	
//	public void activeAndWriteSubmissionUpdateUmbrella(Submission submission, ContextValidation ctx) {
//		ctx.setUpdateMode();
//		if(StringUtils.isNotBlank(submission.xmlSubmission)) {
//			ctx.addError("submission." + submission.code, "champs xmlSubmission deja present ?");
//			return;
//		}
//		if (StringUtils.isBlank(submission.submissionDirectory)) {
//			ctx.addError("submission." + submission.code, "absence du repertoire de soumission 'submissionDirectory'");
//			return;
//		}
//		File dataRep = new File(submission.submissionDirectory);
//		if (! dataRep.exists()) {
//			if (!dataRep.mkdirs()) {	
//				//logger.debug("Impossible de creer le repertoire " + dataRep + " ");
//				ctx.addError("submission." + submission.code, "impossible de creer le repertoire de soumission " + dataRep);
//				return;
//			}
//		}
//		File submissionFile = new File(dataRep , "submissionUpdateUmbrella.xml");
//		//logger.debug("resultDirectory=" + dataRep);
//		//logger.debug("fichier d'ecriture pour submissionFile=" + submissionFile.getName());
//		try {
//			xmlServices.writeSubmissionUpdateXmlForUmbrella(submission, submissionFile);
//		} catch (IOException e) {
//			ctx.addError("submission." + submission.code, "impossible d'ecrire fichiers xmlSubmission, message=" + e.getMessage());
//			return;
//		}
//		File updateUmbrellaFile = new File(dataRep, "updateUmbrella.xml");
//		try {
//			xmlServices.writeProjectUmbrellaXml(submission, updateUmbrellaFile);
//		} catch (IOException e) {
//			ctx.addError("submission." + submission.code, "impossible d'ecrire fichiers xmlUmbrella, message=" + e.getMessage());
//			return;
//		}
//		submission.ebiResult = "ebi_AC_" + submission.code + ".txt";
//		// mettre à jour dans base, l'objet submission avec les champs xml et ebiResult:
//		try {
//			submissionAPI.update(submission, submission.traceInformation.createUser);
//		} catch (APIException e) {
//			ctx.addError("submission." + submission.code, e.getMessage());
//			return;
//		}
//	}
//	
//
//	public void loadReportSubmissionForUpdateProject(Submission submission, ContextValidation ctxVal) {
//		submission.state.code = "SUBU-SMD-F";
//		boolean sendMail = true;
//		File ebiReport = new File(submission.submissionDirectory, submission.ebiResult);
//		submissionNewAPI.loadRespEbiForUpdate(ctxVal, submission, ebiReport.getAbsolutePath(), sendMail);
//	}
//
//	public void loadReportSubmissionForCreationStudy(Submission submission, Study study, ContextValidation ctxVal) {
//		submission.state.code = "SUB-SMD-F";
//		boolean sendMail = true;
//		boolean confidential = false;
//		File ebiReport = new File(submission.submissionDirectory, submission.ebiResult);
//		submissionNewAPI.loadRespEbiForCreation(ctxVal, submission, ebiReport.getAbsolutePath(), sendMail, confidential);	
//	}	
//
//	// main pour l'update à l'EBI de l'umbrella, l'ecriture des fichiers xml et l'envoie curl et recuperation des AC
//	public void sendUmbrellaAndloadEbiReport(Submission submission, ContextValidation ctx) {
//		// Envoie des fichiers xml et recuperation du fichier ebiRetour : 
//		File ebiRetour = new File(submission.submissionDirectory, submission.ebiResult);
//		//logger.debug("fichier reporting ebi = " + ebiRetour.getAbsolutePath());
//		if( ! ebiRetour.exists()) {
//			//logger.debug("appel de curl");
//			submissionNewAPI.ebiCurl(submission, ctx);
//			//logger.debug("apres appel de curl");
//			if(ctx.hasErrors()) {
//				return;
//			}
//		}
//		// parsing du fichier retour de l'EBI et mise à jour dans base pour les AC des objets submission et project ou study 
//		// inutile de faire des cascades sur releaseDate ou autre car public et aucune données associée.
//		if(ebiRetour.exists()) {
//			//logger.debug("appel de loadReportSubmissionForUpdateProject");
//			// charge rapport de soumission et met à jour dans base, la soumission et le project pour les numeros d'accession.
//			loadReportSubmissionForUpdateProject(submission, ctx);
//		}
//	}
//
//	// NGL-5171
//	public String getGeographicLocation(Analysis analysis, ContextValidation ctx) {
//		String geographicLocation = null;
//		HashMap<String, String>   hmap = new HashMap<String, String>();
//		if(analysis.readSetCodes == null || analysis.readSetCodes.size()==0 ) {
//			ctx.addError("analysis." + analysis.code , "sans readSetCodes renseigne : impossible de determiner geographicalLocation");
//			return null;
//		}
//		for(String rsCode : analysis.readSetCodes) {
//			if(!laboReadsetAPI.isObjectExist(rsCode)) {
//				ctx.addError("analysis." + analysis.code + "." + rsCode, "readset reference dans analyse inexistante en base");
//				continue;
//			} 
//			ReadSet laboReadset = laboReadsetAPI.get(rsCode);
//			if(laboReadset == null) {
//				ctx.addError("analysis." + analysis.code + "." + rsCode, "readset reference dans analyse null en base");
//				continue;
//			}
//			if ( laboReadset.sampleOnContainer == null ||
//				laboReadset.sampleOnContainer.properties == null ||
//				! laboReadset.sampleOnContainer.properties.containsKey("geographicLocation") ||
//				laboReadset.sampleOnContainer.properties.get("geographicLocation") == null ||
//				StringUtils.isBlank((String) laboReadset.sampleOnContainer.properties.get("geographicLocation").value)
//				) {
//				ctx.addError("analysis." + analysis.code + "." + rsCode, "readset reference dans analyse sans sampleOnContainer.properties.geographicLocation");
//				continue;
//			} 
//			String gl = (String) laboReadset.sampleOnContainer.properties.get("geographicLocation").value;
//			hmap.put(rsCode, gl);
//		}
//		// On verifie que tous les rs de l'analyse ont bien meme geographicalLocalisation.
//		if(hmap.size() == 0) {
//			ctx.addError("analysis." + analysis.code , "readsets referencees dans analyse sans geographicalLocation");
//			return null;
//		}
//		ArrayList<String> geographicLocations = new ArrayList<String>();
//		String messError = "";
//		for (Map.Entry m : hmap.entrySet()) {
//			geographicLocation = (String) m.getValue();
//			messError = messError + "rsCode=" + m.getKey() + " avec geoLocalisation=" + m.getValue() + ", ";
//			if ( ! geographicLocations.contains(geographicLocation) ) {
//				geographicLocations.add(geographicLocation);
//			}
//		}
//		
//		if(geographicLocations.size() > 1) {
//			ctx.addError("analysis." + analysis.code , "readsets referencees dans analyse avec des geographicalLocation differents " + messError);
//			return null;
//		}	
//		geographicLocation = geographicLocations.get(0);
//		return geographicLocation;
//	}
//	
//	
//	
//	@Override
//	public void execute() throws Exception {
//		// contexte de validation pour la validation :
//		ContextValidation ctx = ContextValidation.createUpdateContext("ngl-support");
//		// selectionner tous les enr Atlasea pour lesquels tous les projects ont été soumis à l'EBI :
//		List<Atlasea> listAtlaseas = MongoDBDAO.find( InstanceConstants.SRA_ATLASEA_COLL_NAME, 
//													  Atlasea.class, 
//													  DBQuery.is("endSubmitProjects", true)).toList();	
//		
//		HashMap<String, String> mapGeographicLocalisation = new HashMap<String, String>();
//		for (Atlasea atlasea : listAtlaseas) {
//			// pour chaque project umbrella
//			Analysis analysis = biAnalysisDAO.getObject(atlasea.code);
//			if(analysis == null) {
//				logger.debug("absence de l'analyse " +atlasea.code);
//				continue;
//			} 
//			String geographicLocation = getGeographicLocation(analysis, ctx);
//			// sortir si erreurs detectees :
//			if(ctx.hasErrors()) {
//				continue;
//			}	
//			mapGeographicLocalisation.put(atlasea.code, geographicLocation);
//		}
//		if(ctx.hasErrors()) {
//			ctx.displayErrors(logger, "debug");
//			println(Json.prettyPrint(app.errorsAsJson(ctx.getErrors())));
//			println("Fin du traitement : Aucune modification de titre des umbrella dans la base car erreurs detectees");
//			return;
//		}
//		
//		for (Atlasea atlasea : listAtlaseas) {
//			// pour chaque project umbrella
//			String geographicLocation = mapGeographicLocalisation.get(atlasea.code);
//			Project umbrella = projectDAO.getObject(atlasea.umbrellaCode);
//			// sortir si erreurs detectees :
//			if(ctx.hasErrors()) {
//				continue;
//			}
//			if(geographicLocation == null) {
//				continue;
//			}
//			umbrella.title = umbrella.title + " (" + geographicLocation + ")";
//			// sauver umbrella en base avec nouveau titre :
//			projectDAO.save(umbrella);
//			// construire une nouvelle soumission pour update de l'umbrella :
//			//submission_atlasea_ooPlaJani1_updateUmbrella
//			String submissionCode = "debugTitle_" + atlasea.umbrellaSubmissionCode;
//			// construire soumission pour update de l'umbrella
//			Submission submission = buildDebugUmbrellaUpdateSubmission(atlasea, submissionCode, ctx);
//			// ecrire metadata dans fileSystem :
//			activeAndWriteSubmissionUpdateUmbrella(submission, ctx);
//			SraException.assertNoError(ctx);
//			sendUmbrellaAndloadEbiReport(submission, ctx);
//		} 
//		println("Fin du traitement");
//				
//	}
//
//
//
//}
