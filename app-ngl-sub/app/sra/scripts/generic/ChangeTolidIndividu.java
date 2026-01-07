package sra.scripts.generic;

import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptWithArgsAndExcelBody;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.analyses.AnalysesDAO;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APISemanticException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.sra.AtlaseaDAO;
import fr.cea.ig.ngl.dao.sra.ExperimentDAO;
import fr.cea.ig.ngl.dao.sra.ProjectDAO;
import fr.cea.ig.ngl.dao.sra.ReadsetDAO;
import fr.cea.ig.ngl.dao.sra.StudyDAO;
import fr.cea.ig.ngl.dao.sra.SubmissionDAO;
import models.laboratory.common.instance.Comment;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.run.instance.Analysis;
import models.sra.submit.sra.instance.Atlasea;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.sra.instance.OldTolid;
import models.sra.submit.sra.instance.Project;
import models.sra.submit.sra.instance.Readset;
import models.sra.submit.sra.instance.Study;
import models.sra.submit.sra.instance.Submission;
import models.sra.submit.sra.instance.Submission.TypeRawDataSubmitted;
import play.libs.Json;
import services.sra.NGLSUBConfig;
import services.sra.SraException;
import sra.api.SubmissionNewAPI;
import sra.services.XmlServices;
import validation.ContextValidation;
import static ngl.refactoring.state.SRASubmissionStateNames.SUB_SMD_IP;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.mongojack.DBQuery;

import controllers.sra.api.SubmissionAPI;
import controllers.sra.atlaseas.api.AtlaseaCommentsAPI;
import controllers.sra.experiments.api.ExperimentCommentsAPI;
import controllers.sra.projects.api.ProjectCommentsAPI;
import controllers.sra.studies.api.StudyCommentsAPI;
import controllers.sra.submissions.api.SubmissionCommentsAPI;

import org.mongojack.DBQuery.Query;


// NGL-5146
// curl -X POST -H 'User-Agent: bot' -H 'Content-Type: multipart/form-data' 
// -v "http://localhost:9000/sra/scripts/run/sra.scripts.generic.ChangeTolidIndividu?ticket=SUPSQ-6511" 
// -F xlsx="@/c/Users/sgas/Downloads/modificationTolidExtractTest.xlsx"
// fichier modificationTolidExtract.xlsx avec header :
// oldTolid | newTolid


public class ChangeTolidIndividu extends ScriptWithArgsAndExcelBody<ChangeTolidIndividu.MyParam> {
	private final List<String> projectAtlaseaCodes       = Arrays.asList("DKB", "DKC", "DKD", "DKE", "DKF");
	private final SubmissionDAO          submissionDAO;
	private final SubmissionCommentsAPI  submissionCommentsAPI;
	private final ProjectCommentsAPI     projectCommentsAPI;
	private final StudyCommentsAPI       studyCommentsAPI;
	private final AtlaseaCommentsAPI     atlaseaCommentsAPI;
	private final ExperimentCommentsAPI  experimentCommentsAPI;
	private final XmlServices            xmlServices;
	private final  SubmissionNewAPI      submissionNewAPI;
	private final AtlaseaDAO             atlaseaDAO;
	private final AnalysesDAO            biAnalysisDAO;
	private final ProjectDAO             projectDAO;
	private final StudyDAO               studyDAO;
	private final ExperimentDAO          experimentDAO;
	private final ReadsetDAO             readsetDAO;
	private final SubmissionAPI          submissionAPI;
	private final String                 submissionAtlaseaRootResultDirectory; 
	private final String                 ebiCurl;
    private final NGLSUBConfig           config;
	private final NGLApplication         app;
	private final String                 dropBox           = "Webin-9";
    private final String                 user              = "sgas";

	
	@Inject
	public ChangeTolidIndividu(
			NGLSUBConfig                    config,  
            SubmissionNewAPI                submissionNewAPI,
			SubmissionDAO                   submissionDAO,
			SubmissionCommentsAPI           submissionCommentsAPI,
			ProjectCommentsAPI              projectCommentsAPI,
			StudyCommentsAPI                studyCommentsAPI,
			AtlaseaCommentsAPI              atlaseaCommentsAPI,
			ExperimentCommentsAPI           experimentCommentsAPI,
			SubmissionAPI                   submissionAPI,
			XmlServices                     xmlServices,
			AtlaseaDAO                      atlaseaDAO,
			AnalysesDAO                     biAnalysisDAO,
			ProjectDAO                      projectDAO,
			StudyDAO                        studyDAO,
			ExperimentDAO                   experimentDAO,
			ReadsetDAO                      readsetDAO,
			NGLApplication                  app) {
		this.submissionAtlaseaRootResultDirectory = config.getCreationProjectsAtlaseaRootDirectory(); 
	    this.config                               = config;
		this.ebiCurl                              = config.getEbiCurl();
		this.submissionDAO                        = submissionDAO;
		this.submissionCommentsAPI                = submissionCommentsAPI;
		this.projectCommentsAPI                   = projectCommentsAPI;
		this.studyCommentsAPI                     = studyCommentsAPI;
		this.atlaseaCommentsAPI                   = atlaseaCommentsAPI;
		this.experimentCommentsAPI                = experimentCommentsAPI;
		this.app                                  = app;
		this.atlaseaDAO                           = atlaseaDAO;
		this.biAnalysisDAO                        = biAnalysisDAO;
		this.projectDAO                           = projectDAO;
		this.studyDAO                             = studyDAO;
		this.experimentDAO                        = experimentDAO;
		this.readsetDAO                           = readsetDAO;
		this.xmlServices                          = xmlServices;
		this.submissionAPI                        = submissionAPI;
        this.submissionNewAPI                     = submissionNewAPI;

	}

	// ma structure de controle et stockage des arguments de l'url
	public static class MyParam {
		public String ticket;  
	}


	private class TolidInfos  {
		String  oldTolid;
		String  newTolid;
		List<Atlasea> list_atlasea       = new ArrayList<Atlasea>();
		List<Analysis> list_analysis     = new ArrayList<Analysis>();
		List<Submission> list_submission = new ArrayList<Submission>();
		List<Project> list_project       = new ArrayList<Project>();
		List<Study> list_study           = new ArrayList<Study>();
		List<Experiment> list_experiment = new ArrayList<Experiment>();
		List<Readset> list_readset       = new ArrayList<Readset>();
		Submission submissionUmbrella;
		Submission submissionSequencing;
		Submission submissionPrimaryAssembly;
	}

	// verifie coherence presence des tolid à modifier dans database
	public void validDatabaseInfos(List<TolidInfos> list_tolidInfos, ContextValidation ctx) {
		for (TolidInfos tolidInfos : list_tolidInfos) {
			// verification Atlasea
			String mess = "";
			for(Atlasea atlasea : tolidInfos.list_atlasea) {
				mess += atlasea.code + ", ";
			}
			logger.debug("tolid '{}' present {} fois dans table ngl-sub.Atlasea ({}) ", tolidInfos.oldTolid, tolidInfos.list_atlasea.size(), mess);
			if ( tolidInfos.list_atlasea.size() != 1 ) {
				// on ne doit avoir au plus d'un seul atlasea déclaré avec ce tolid
				ctx.addError("tolid." + tolidInfos.oldTolid, " nombre d'atlasea contenant ce tolid != 1");
			}
			for(Atlasea atlasea : tolidInfos.list_atlasea) {
				if(!atlasea.endSubmitProjects) {
					ctx.addError("tolid." + tolidInfos.oldTolid, " traitement inacheve dans atlasea " +  atlasea.code);	
				}
			}
			// verification Analyse
			mess = "";
			for(Analysis analysis : tolidInfos.list_analysis) {
				mess += analysis.code + ", ";
			}
			logger.debug("tolid '{}' presents {} fois dans table ngl-bi.Analysis ({}) ", tolidInfos.oldTolid, tolidInfos.list_analysis.size(), mess);
			if ( tolidInfos.list_analysis.size() != 1 ) {
				ctx.addError("tolid." + tolidInfos.oldTolid, " nombre d'analyse contenant ce tolid != 1");
			} 
	
			// verification Submission
			mess = "";
			for(Submission submission : tolidInfos.list_submission) {
				mess += submission.code + ", ";
			}
			logger.debug("tolid '{}' presents {} fois dans table ngl-sub.Submission ({}) ", tolidInfos.oldTolid, tolidInfos.list_submission.size(), mess);
			if (tolidInfos.list_submission.size() != 4) {
				ctx.addError("tolid." + tolidInfos.oldTolid, " nombre de soumission contenant ce tolid != 4");
			}
			// verification project
			mess = "";
			for(Project project : tolidInfos.list_project) {
				mess += project.code + ", ";
			}
			logger.debug("tolid '{}' presents {} fois dans table ngl-bi.Project ({}) ", tolidInfos.oldTolid, tolidInfos.list_project.size(), mess);
			if( tolidInfos.list_project.size() != 1 ) {
				ctx.addError("tolid." + tolidInfos.oldTolid, " nombre de project (umbrella) contenant ce tolid != 1");
			}
			
			// verification study
			mess = "";
			for(Study study : tolidInfos.list_study) {
				mess += study.code + ", ";
			}
			logger.debug("tolid '{}' presents {} fois dans table ngl-sub.Study ({}) ", tolidInfos.oldTolid, tolidInfos.list_study.size(), mess);
			if( tolidInfos.list_study.size() !=2 ) {
				ctx.addError("tolid." + tolidInfos.oldTolid, " nombre de study contenant ce tolid != 2");
			}
			
			// verification experiment et readset
			logger.debug("tolid '{}' presents {} fois dans table ngl-sub.Experiment ({}) ", tolidInfos.oldTolid, tolidInfos.list_experiment.size(), mess);
			logger.debug("tolid '{}' presents {} fois dans table ngl-sub.Readset ({}) ", tolidInfos.oldTolid, tolidInfos.list_readset.size(), mess);

			// verifier qu'il y a autant d'experiments que de readsets à modifier :
			if( tolidInfos.list_experiment.size() != tolidInfos.list_readset.size()) {
				ctx.addError("tolid." + tolidInfos.oldTolid, " nombre d'experiment avec studyCode contenant ce toldid != nombre de readset avec studyCode contenant ce tolid");
			}			

		}
	}
	
	public void modifyTolidInSubmission(String ticket, String user, Date courantDate, TolidInfos tolidInfos) {
		//logger.debug("tolidInfos.oldTolid=" + tolidInfos.oldTolid);
		//logger.debug("tolidInfos.newTolid=" + tolidInfos.newTolid);

		for (Submission submission: tolidInfos.list_submission) {
			logger.debug("submissionCode=" + submission.code);
			// remplacer dans code et umbrellaCode ou studyCode, oldTolid par newTolid 
			//on laisse les champs submissionDirectory et ebiREsult pour pouvoir retrouver les données de soumissions originales sur disque.
			submission.code = submission.code.replace(tolidInfos.oldTolid, tolidInfos.newTolid);
			if(StringUtils.isNotBlank(submission.umbrellaCode)) {
				submission.umbrellaCode = submission.umbrellaCode.replace(tolidInfos.oldTolid, tolidInfos.newTolid);
			}
			if(StringUtils.isNotBlank(submission.studyCode)) {
				submission.studyCode = submission.studyCode.replace(tolidInfos.oldTolid, tolidInfos.newTolid);
			}
			submission.traceInformation.modifyDate = courantDate;
			submission.traceInformation.modifyUser = user;
			
			// sauvegarde de la soumission :
			//submissionDAO.save(submission);
			submissionDAO.update(submission);
			
			// ajout et sauvegarde du commentaire dans la soumission
			DateFormat formaterDate = new SimpleDateFormat("yyyy_MM_dd");
		    String st_date = formaterDate.format(courantDate);

			String commentaire = "Le " + st_date + ", " + ticket + " : Remplacement du tolid '" + tolidInfos.oldTolid + "' par le tolid '"+ tolidInfos.newTolid + "'";
			//Comment comment = new Comment(commentaire, user);
			Comment comment = new Comment(commentaire, user, false);
			try {
				submissionCommentsAPI.save(submission, comment, user);
			} catch (RuntimeException | APISemanticException | APIValidationException e) {
				throw new SraException("Probleme lors de la sauvegarde du commentaire de la soumission " + submission.code, e);
			}
		}
	}
	public void modifyTolidInProject(String ticket, String user, Date courantDate, TolidInfos tolidInfos) {
		//logger.debug("tolidInfos.oldTolid=" + tolidInfos.oldTolid);
		//logger.debug("tolidInfos.newTolid=" + tolidInfos.newTolid);

		for (Project project: tolidInfos.list_project) {
			// remplacer dans code, oldTolid par newTolid 
			project.code = project.code.replace(tolidInfos.oldTolid, tolidInfos.newTolid);
			project.traceInformation.modifyDate = courantDate;
			project.traceInformation.modifyUser = user;
			
			// sauvegarde du project :
			//projectDAO.save(project);
			projectDAO.update(project);
			
			// ajout et sauvegarde du commentaire dans le project
			DateFormat formaterDate = new SimpleDateFormat("yyyy_MM_dd");
		    String st_date = formaterDate.format(courantDate);

			String commentaire = "Le " + st_date + ", " + ticket + " : Remplacement du tolid '" + tolidInfos.oldTolid + "' par le tolid '"+ tolidInfos.newTolid + "'";
			Comment comment = new Comment(commentaire, user, false);
			try {
				projectCommentsAPI.save(project, comment, user);
			} catch (RuntimeException | APISemanticException | APIValidationException e) {
				throw new SraException("Probleme lors de la sauvegarde du commentaire du project umbrella " + project.code, e);
			}
		}
		
	}
	public void modifyTolidInStudy(String ticket, String user, Date courantDate, TolidInfos tolidInfos) {
		//logger.debug("tolidInfos.oldTolid=" + tolidInfos.oldTolid);
		//logger.debug("tolidInfos.newTolid=" + tolidInfos.newTolid);

		for (Study study: tolidInfos.list_study) {
			// remplacer dans code, et centerProjectName oldTolid par newTolid 
			study.code = study.code.replace(tolidInfos.oldTolid, tolidInfos.newTolid);
			study.centerProjectName = study.centerProjectName.replace(tolidInfos.oldTolid, tolidInfos.newTolid);
			study.title = study.title.replace(tolidInfos.oldTolid, tolidInfos.newTolid);
			study.traceInformation.modifyDate = courantDate;
			study.traceInformation.modifyUser = user;
			
			// sauvegarde du study :
			//studyDAO.save(study);
			studyDAO.update(study);
			
			// ajout et sauvegarde du commentaire dans le study
			DateFormat formaterDate = new SimpleDateFormat("yyyy_MM_dd");
		    String st_date = formaterDate.format(courantDate);

			String commentaire = "Le " + st_date + ", " + ticket + " : Remplacement du tolid '" + tolidInfos.oldTolid + "' par le tolid '"+ tolidInfos.newTolid + "'";
			Comment comment = new Comment(commentaire, user, false);
			try {
				studyCommentsAPI.save(study, comment, user);
			} catch (RuntimeException | APISemanticException | APIValidationException e) {
				throw new SraException("Probleme lors de la sauvegarde du commentaire du study " + study.code, e);
			}
		}		
	}
	
	public void modifyTolidInAnalyse(String user, Date courantDate, TolidInfos tolidInfos) {
		//logger.debug("tolidInfos.oldTolid=" + tolidInfos.oldTolid);
		//logger.debug("tolidInfos.newTolid=" + tolidInfos.newTolid);

		for (Analysis analysis: tolidInfos.list_analysis) {
			// remplacer dans propertie tolid, oldTolid par newTolid 
			analysis.properties.put("tolid", new PropertySingleValue(tolidInfos.newTolid));	

			// sauvegarde de l'analyse :
			//biAnalysisDAO.save(analysis);
			biAnalysisDAO.update(analysis);
		}			
	}
	
	public void modifyTolidInAtlasea(String ticket, String user, Date courantDate, TolidInfos tolidInfos) {
		//logger.debug("tolidInfos.oldTolid=" + tolidInfos.oldTolid);
		//logger.debug("tolidInfos.newTolid=" + tolidInfos.newTolid);

		for (Atlasea atlasea: tolidInfos.list_atlasea) {
			// remplacer dans tolId, umbrellaCode, umbrellaSubmissionCode, umbrellaUpdateSubmissionCode 
			atlasea.tolId = tolidInfos.newTolid;
			if(StringUtils.isNotBlank(atlasea.umbrellaCode)) {
				atlasea.umbrellaCode = atlasea.umbrellaCode.replace(tolidInfos.oldTolid, tolidInfos.newTolid);
			}
			if(StringUtils.isNotBlank(atlasea.umbrellaSubmissionCode)) {
				atlasea.umbrellaSubmissionCode = atlasea.umbrellaSubmissionCode.replace(tolidInfos.oldTolid, tolidInfos.newTolid);
			}
			if(StringUtils.isNotBlank(atlasea.umbrellaUpdateSubmissionCode)) {
				atlasea.umbrellaUpdateSubmissionCode = atlasea.umbrellaUpdateSubmissionCode.replace(tolidInfos.oldTolid, tolidInfos.newTolid);
			}
			if(StringUtils.isNotBlank(atlasea.sequencingProjectCode)) {
				atlasea.sequencingProjectCode = atlasea.sequencingProjectCode.replace(tolidInfos.oldTolid, tolidInfos.newTolid);
			}
			if(StringUtils.isNotBlank(atlasea.sequencingSubmissionCode)) {
				atlasea.sequencingSubmissionCode = atlasea.sequencingSubmissionCode.replace(tolidInfos.oldTolid, tolidInfos.newTolid);
			}
			if(StringUtils.isNotBlank(atlasea.primaryAssemblyProjectCode)) {
				atlasea.primaryAssemblyProjectCode = atlasea.primaryAssemblyProjectCode.replace(tolidInfos.oldTolid, tolidInfos.newTolid);
			}
			if(StringUtils.isNotBlank(atlasea.primaryAssemblySubmissionCode)) {
				atlasea.primaryAssemblySubmissionCode = atlasea.primaryAssemblySubmissionCode.replace(tolidInfos.oldTolid, tolidInfos.newTolid);
			}
			if(atlasea.oldTolids==null) {
				atlasea.oldTolids = new ArrayList<OldTolid>();
			}
			OldTolid oldTolid = new OldTolid(tolidInfos.oldTolid, ticket, courantDate);			
			atlasea.oldTolids.add(oldTolid);
			
			atlasea.traceInformation.modifyDate = courantDate;
			atlasea.traceInformation.modifyUser = user;
			
			// sauvegarde de atlasea :  // bizarre ne marche pas avec save ici
			atlaseaDAO.update(atlasea);
			
			// ajout et sauvegarde du commentaire dans le study
			DateFormat formaterDate = new SimpleDateFormat("yyyy_MM_dd");
		    String st_date = formaterDate.format(courantDate);

			String commentaire = "Le " + st_date + ", " + ticket + " : Remplacement du tolid '" + tolidInfos.oldTolid + "' par le tolid '"+ tolidInfos.newTolid + "'";
			Comment comment = new Comment(commentaire, user, false);
			try {
				atlaseaCommentsAPI.save(atlasea, comment, user);
			} catch (RuntimeException | APISemanticException | APIValidationException e) {
				throw new SraException("Probleme lors de la sauvegarde du commentaire de atlasea " + atlasea.code, e);
			}
		}			
	}	
	
	public void modifyTolidInExperiments(String ticket, String user, Date courantDate, TolidInfos tolidInfos) {
		//logger.debug("tolidInfos.oldTolid=" + tolidInfos.oldTolid);
		//logger.debug("tolidInfos.newTolid=" + tolidInfos.newTolid);

		for (Experiment exp : tolidInfos.list_experiment) {
			String newStudyCode = exp.studyCode.replace(tolidInfos.oldTolid, tolidInfos.newTolid);

			exp.studyCode = newStudyCode;
			// sauvegarde de l'experiment :  
			experimentDAO.update(exp);
			
			// ajout et sauvegarde du commentaire dans l'experiment
			DateFormat formaterDate = new SimpleDateFormat("yyyy_MM_dd");
		    String st_date = formaterDate.format(courantDate);

			String commentaire = "Le " + st_date + ", " + ticket + " : Remplacement du tolid '" + tolidInfos.oldTolid + "' par le tolid '"+ tolidInfos.newTolid + "'";
			Comment comment = new Comment(commentaire, user, false);
			try {
				experimentCommentsAPI.save(exp, comment, user);
			} catch (RuntimeException | APISemanticException | APIValidationException e) {
				throw new SraException("Probleme lors de la sauvegarde du commentaire de l'experiment " + exp.code, e);
			}
		}			
	}
	
	
	public void modifyTolidInReadsets(String ticket, String user, Date courantDate, TolidInfos tolidInfos) {
		//logger.debug("tolidInfos.oldTolid=" + tolidInfos.oldTolid);
		//logger.debug("tolidInfos.newTolid=" + tolidInfos.newTolid);

		for (Readset rs : tolidInfos.list_readset) {
			String newStudyCode = rs.studyCode.replace(tolidInfos.oldTolid, tolidInfos.newTolid);

			rs.studyCode = newStudyCode;
			// sauvegarde du readset : 
			readsetDAO.update(rs);
		}			
	} 
	
	public void modifyTolidInDatabase(String ticket, List<TolidInfos> list_tolidInfos) {
		String user = "ngl-support";
		Date courantDate = new Date();
		for (TolidInfos tolidInfos : list_tolidInfos) {
			modifyTolidInSubmission(ticket, user, courantDate, tolidInfos);
			modifyTolidInProject(ticket, user, courantDate, tolidInfos);
			modifyTolidInStudy(ticket, user, courantDate, tolidInfos);
			modifyTolidInAnalyse(user, courantDate, tolidInfos);
			modifyTolidInAtlasea(ticket, user, courantDate, tolidInfos);
			modifyTolidInExperiments(ticket, user, courantDate, tolidInfos);
			modifyTolidInReadsets(ticket, user, courantDate, tolidInfos);
		}
	}

	// construit la query permettant de rechercher dans mongodb les enregistrements ayant le champs répondant à la regExp
	public Query getQueryPattern(String field, String regExp) {
		List<Query> queries = new ArrayList<>();
		Query query = null;
		queries.add(DBQuery.regex(field, Pattern.compile(regExp)));
		query = DBQuery.and(queries.toArray(new Query[queries.size()]));
		return query;
	}
	
	// Repertorie pour chaque tolid à modifier les enregistrements dans les differentes tables ou apparait ce tolid
	public void addDatabaseInfosInTolidInfos(List<TolidInfos> listTolidInfos) {
		for(TolidInfos tolidInfos : listTolidInfos) {
			logger.debug("tolidInfos.oldTolid=" + tolidInfos.oldTolid);
			logger.debug("tolidInfos.newTolid=" + tolidInfos.newTolid);

			List<Analysis> list_analysis =  biAnalysisDAO.findAsList(DBQuery.is("properties.tolid.value", tolidInfos.oldTolid));
			tolidInfos.list_analysis.addAll(list_analysis);

			List<Atlasea> list_atlasea =  atlaseaDAO.findAsList(DBQuery.is("tolId", tolidInfos.oldTolid));
			tolidInfos.list_atlasea.addAll(list_atlasea);

			Query queryCodePattern = getQueryPattern("code", tolidInfos.oldTolid);
			List<Submission> list_submission = submissionDAO.findAsList(queryCodePattern);
			// ne pas integrer nom de la soumission courante pour debuguer 
			tolidInfos.list_submission.addAll(list_submission);

			List<Project> list_project = projectDAO.findAsList(queryCodePattern);
			tolidInfos.list_project.addAll(list_project);

			List<Study> list_study = studyDAO.findAsList(queryCodePattern);
			tolidInfos.list_study.addAll(list_study);
			
			queryCodePattern = getQueryPattern("studyCode", tolidInfos.oldTolid);
			List<Experiment> list_experiment = experimentDAO.findAsList(queryCodePattern);
			tolidInfos.list_experiment.addAll(list_experiment);		
			
			List<Readset> list_readset = readsetDAO.findAsList(queryCodePattern);
			tolidInfos.list_readset.addAll(list_readset);		
		}
	}
	
	public Submission buildDebugStudyUpdateSubmission(String tolId, String studyCode, String submissionCode, ContextValidation ctx) {
		Date courantDate = new java.util.Date();
		Submission submission = new Submission(user);
		submission.code = submissionCode;
		submission.projectCodes = this.projectAtlaseaCodes;
		submission.traceInformation.creationDate = courantDate;
		submission.state = new State(SUB_SMD_IP, user);
		submission.type = Submission.Type.UPDATE;
		submission.typeRawDataSubmitted = TypeRawDataSubmitted.withoutRawData;
		submission.dropBox = this.dropBox;
		submission.submissionDirectory = this.submissionAtlaseaRootResultDirectory + tolId + "/";	
		submission.ebiResult = "ebi_AC_" + submission.code + ".txt";
		submission.studyCode = studyCode;
		logger.debug("dans buildDebugStudyUpdateSubmission submissionCode='{}' et creationMode", submissionCode);
		ctx.setCreationMode();
		submission.validate(ctx);
		SraException.assertNoError(ctx);
		return submissionDAO.save(submission);
	}	
	
	
	public Submission buildDebugUmbrellaUpdateSubmission(Atlasea atlasea, String submissionCode, ContextValidation ctx) {
		String umbrellaCode = atlasea.umbrellaCode;
		String tolId = atlasea.tolId;
		Date courantDate = new java.util.Date();
		Submission submission = new Submission(user);
		submission.code = submissionCode;
		submission.projectCodes = this.projectAtlaseaCodes;
		submission.traceInformation.creationDate = courantDate;
		submission.state = new State(SUB_SMD_IP, user);
		submission.type = Submission.Type.UPDATE;
		submission.typeRawDataSubmitted = TypeRawDataSubmitted.withoutRawData;
		submission.dropBox = this.dropBox;
		submission.submissionDirectory = this.submissionAtlaseaRootResultDirectory + tolId + "/";	
		submission.ebiResult = "ebi_AC_" + submission.code + ".txt";
		submission.umbrellaCode = umbrellaCode;
		logger.debug("dans buildDebugUmbrellaUpdateSubmission submissionCode='{}' et creationMode", submissionCode);
		ctx.setCreationMode();
		submission.validate(ctx);
		SraException.assertNoError(ctx);
		return submissionDAO.save(submission);
	}

	
	public void activeAndWriteSubmissionUpdateUmbrella(Submission submission, ContextValidation ctx) {
		ctx.setUpdateMode();
		if(StringUtils.isNotBlank(submission.xmlSubmission)) {
			ctx.addError("submission." + submission.code, "champs xmlSubmission deja present ?");
			return;
		}
		if (StringUtils.isBlank(submission.submissionDirectory)) {
			ctx.addError("submission." + submission.code, "absence du repertoire de soumission 'submissionDirectory'");
			return;
		}
		File dataRep = new File(submission.submissionDirectory);
		if (! dataRep.exists()) {
			if (!dataRep.mkdirs()) {	
				//logger.debug("Impossible de creer le repertoire " + dataRep + " ");
				ctx.addError("submission." + submission.code, "impossible de creer le repertoire de soumission " + dataRep);
				return;
			}
		}
		File submissionFile = new File(dataRep , "submissionUpdateUmbrella.xml");
		//logger.debug("resultDirectory=" + dataRep);
		//logger.debug("fichier d'ecriture pour submissionFile=" + submissionFile.getName());
		try {
			xmlServices.writeSubmissionUpdateXmlForUmbrella(submission, submissionFile);
		} catch (IOException e) {
			ctx.addError("submission." + submission.code, "impossible d'ecrire fichiers xmlSubmission, message=" + e.getMessage());
			return;
		}
		File updateUmbrellaFile = new File(dataRep, "updateUmbrella.xml");
		try {
			xmlServices.writeProjectUmbrellaXml(submission, updateUmbrellaFile);
		} catch (IOException e) {
			ctx.addError("submission." + submission.code, "impossible d'ecrire fichiers xmlUmbrella, message=" + e.getMessage());
			return;
		}
		submission.ebiResult = "ebi_AC_" + submission.code + ".txt";
		// mettre à jour dans base, l'objet submission avec les champs xml et ebiResult:
		try {
			submissionAPI.update(submission, submission.traceInformation.createUser);
		} catch (APIException e) {
			ctx.addError("submission." + submission.code, e.getMessage());
			return;
		}
	}
	
	
	public void activeAndWriteSubmissionUpdateStudy(Submission submission, ContextValidation ctx) {
		ctx.setUpdateMode();
		if(StringUtils.isNotBlank(submission.xmlSubmission)) {
			ctx.addError("submission." + submission.code, "champs xmlSubmission deja present ?");
			return;
		}
		if (StringUtils.isBlank(submission.submissionDirectory)) {
			ctx.addError("submission." + submission.code, "absence du repertoire de soumission 'submissionDirectory'");
			return;
		}
		File dataRep = new File(submission.submissionDirectory);
		if (! dataRep.exists()) {
			if (!dataRep.mkdirs()) {	
				//logger.debug("Impossible de creer le repertoire " + dataRep + " ");
				ctx.addError("submission." + submission.code, "impossible de creer le repertoire de soumission " + dataRep);
				return;
			}
		}
		File submissionFile = new File(dataRep , submission.code + ".xml");
		//logger.debug("resultDirectory=" + dataRep);
		//logger.debug("fichier d'ecriture pour submissionFile=" + submissionFile.getName());
		try {
			xmlServices.writeSubmissionUpdateXmlPublic(submission, submissionFile);
		} catch (IOException e) {
			ctx.addError("submission." + submission.code, "impossible d'ecrire fichiers xmlSubmission, message=" + e.getMessage());
			return;
		}
		File updateStudyFile = new File(dataRep, "study_" + submission.studyCode + ".xml");
		try {
			xmlServices.writeStudyXml(submission, updateStudyFile);
		} catch (IOException e) {
			ctx.addError("submission." + submission.code, "impossible d'ecrire fichiers xmlStudy, message=" + e.getMessage());
			return;
		}
		
		File updateProjectFile = new File(dataRep, "project_" + submission.studyCode + ".xml");
		try {
			xmlServices.writeSequencingProjectXml(submission, updateProjectFile);
		} catch (IOException e) {
			ctx.addError("submission." + submission.code, "impossible d'ecrire fichiers xmlProject, message=" + e.getMessage());
			return;
		}
		
		submission.ebiResult = "ebi_AC_" + submission.code + ".txt";
		// mettre à jour dans base, l'objet submission avec les champs xml et ebiResult:
		try {
			submissionAPI.update(submission, submission.traceInformation.createUser);
		} catch (APIException e) {
			ctx.addError("submission." + submission.code, e.getMessage());
			return;
		}
	}

	public void loadReportSubmissionForUpdateProject(Submission submission, ContextValidation ctxVal) {
		submission.state.code = "SUBU-SMD-F";
		boolean sendMail = true;
		File ebiReport = new File(submission.submissionDirectory, submission.ebiResult);
		submissionNewAPI.loadRespEbiForUpdate(ctxVal, submission, ebiReport.getAbsolutePath(), sendMail);
	}

	public void loadReportSubmissionForCreationStudy(Submission submission, Study study, ContextValidation ctxVal) {
		submission.state.code = "SUB-SMD-F";
		boolean sendMail = true;
		boolean confidential = false;
		File ebiReport = new File(submission.submissionDirectory, submission.ebiResult);
		submissionNewAPI.loadRespEbiForCreation(ctxVal, submission, ebiReport.getAbsolutePath(), sendMail, confidential);	
	}	

	// main pour l'update à l'EBI de l'umbrella, l'ecriture des fichiers xml et l'envoie curl et recuperation des AC
	public void sendUmbrellaAndloadEbiReport(Submission submission, ContextValidation ctx) {
		// Envoie des fichiers xml et recuperation du fichier ebiRetour : 
		File ebiRetour = new File(submission.submissionDirectory, submission.ebiResult);
		//logger.debug("fichier reporting ebi = " + ebiRetour.getAbsolutePath());
		if( ! ebiRetour.exists()) {
			//logger.debug("appel de curl");
			submissionNewAPI.ebiCurl(submission, ctx);
			//logger.debug("apres appel de curl");
			if(ctx.hasErrors()) {
				return;
			}
		}
		// parsing du fichier retour de l'EBI et mise à jour dans base pour les AC des objets submission et project ou study 
		// inutile de faire des cascades sur releaseDate ou autre car public et aucune données associée.
		if(ebiRetour.exists()) {
			//logger.debug("appel de loadReportSubmissionForUpdateProject");
			// charge rapport de soumission et met à jour dans base, la soumission et le project pour les numeros d'accession.
			loadReportSubmissionForUpdateProject(submission, ctx);
		}
	}
	
	// main pour l'update à l'EBI de l'umbrella, l'ecriture des fichiers xml et l'envoie curl et recuperation des AC
	public void sendStudyAndloadEbiReport(Submission submission, ContextValidation ctx) {
		// Envoie des fichiers xml et recuperation du fichier ebiRetour : 
		File ebiRetour = new File(submission.submissionDirectory, submission.ebiResult);
		//logger.debug("fichier reporting ebi = " + ebiRetour.getAbsolutePath());
		if( ! ebiRetour.exists()) {
			//logger.debug("appel de curl");
			submissionNewAPI.ebiCurl(submission, ctx);
			//logger.debug("apres appel de curl");
			if(ctx.hasErrors()) {
				return;
			}
		}
		// parsing du fichier retour de l'EBI et mise à jour dans base pour les AC des objets submission et project ou study 
		// inutile de faire des cascades sur releaseDate ou autre car public et aucune données associée.
		if(ebiRetour.exists()) {
			//logger.debug("appel de loadReportSubmissionForUpdateProject");
			// charge rapport de soumission et met à jour dans base, la soumission et le project pour les numeros d'accession.
			loadReportSubmissionForUpdateProject(submission, ctx);
		}
	}
	
	
	public void buildAndActivateSubmissionForEbi(String user, List<TolidInfos> listTolidInfos){
		for(TolidInfos tolidInfos : listTolidInfos) {
			// mettre à jour l'umbrella à l'EBI
			for (Project project: tolidInfos.list_project) {
					ContextValidation ctx = ContextValidation.createCreationContext(user);
					Atlasea atlasea = tolidInfos.list_atlasea.get(0);	
					String submissionCode = "debug_" + tolidInfos.oldTolid + "_" + atlasea.umbrellaAc;
					logger.debug("buildAndActivateSubmissionForEbi, submissionCodeUmbrella='{}'", submissionCode);
					if(StringUtils.isNotBlank(atlasea.umbrellaAc)) {
						// construire une nouvelle soumission pour update de l'umbrella :
						//submission_atlasea_ooPlaJani1_updateUmbrella
						tolidInfos.submissionUmbrella = buildDebugUmbrellaUpdateSubmission(atlasea, submissionCode, ctx);
						// ecrire metadata dans fileSystem :
						activeAndWriteSubmissionUpdateUmbrella(tolidInfos.submissionUmbrella, ctx);
					} 
			}
			for (Study study: tolidInfos.list_study) {
				ContextValidation ctx = ContextValidation.createCreationContext(user);
				Atlasea atlasea = tolidInfos.list_atlasea.get(0);
				String submissionCode = "debug_";
				if(study.code.contains("study-primary-assembly")) {
					submissionCode = "debug_" + tolidInfos.oldTolid + "_" + atlasea.primaryAssemblyProjectAc;
					logger.debug("buildAndActivateSubmissionForEbi, submissionCodeAssembly='{}'", submissionCode);
				    tolidInfos.submissionPrimaryAssembly = buildDebugStudyUpdateSubmission(atlasea.tolId, study.code, submissionCode, ctx);
					activeAndWriteSubmissionUpdateStudy(tolidInfos.submissionPrimaryAssembly, ctx);
				} else if(study.code.contains("study-rawdata")) {
					submissionCode = "debug_" + tolidInfos.oldTolid + "_" + atlasea.sequencingProjectAc;
					logger.debug("buildAndActivateSubmissionForEbi, submissionCodeRawData='{}'", submissionCode);

					// construire une nouvelle soumission pour update de sequencingStudy :
				    tolidInfos.submissionSequencing = buildDebugStudyUpdateSubmission(atlasea.tolId, study.code, submissionCode, ctx);
					activeAndWriteSubmissionUpdateStudy(tolidInfos.submissionSequencing, ctx);
				} else {
					ctx.addError("oldTolid:", "study corrige qui n'a pas le nom attendu " + study.code); 
				}
			}
		}
	}
	

	
	
	@Override
	public void execute(MyParam args, XSSFWorkbook workbook) throws Exception {
		// contexte de validation pour la validation :
		ContextValidation ctx = ContextValidation.createUpdateContext("ngl-support");

		// parser le fichier d'entree pour construire la liste des modifications de tolid de l'utilisateur
		List<TolidInfos> listTolidInfosOri =  parseWorkbook(workbook);
		
		// construction de la liste de modifications de tolid en verifiant duplications et incoherences:
		List<TolidInfos> listTolidInfos = validUserListTolid(listTolidInfosOri, ctx);

		// sortir si erreurs detectees :
		if(ctx.hasErrors()) {
			ctx.displayErrors(logger, "debug");
			println(Json.prettyPrint(app.errorsAsJson(ctx.getErrors())));
			println("Fin du traitement : Aucune modification de tolid dans la base");
			return;
		}
		
		// identifier les objets de la base contenant le tolid à modifier :
		addDatabaseInfosInTolidInfos(listTolidInfos);
		
		// valider les infos des tolid à modifier dans la base
		validDatabaseInfos(listTolidInfos, ctx);
		
		// sortir si erreurs detectees :
		if(ctx.hasErrors()) {
			ctx.displayErrors(logger, "debug");
			println(Json.prettyPrint(app.errorsAsJson(ctx.getErrors())));
			println("Fin du traitement : Aucune modification de tolid dans la base car erreurs detectees");
			return;
		}
		
		// Modifier les infos des tolid dans la base :
		modifyTolidInDatabase(args.ticket, listTolidInfos);
		
		// Mettre à jour les données à l'EBI
		buildAndActivateSubmissionForEbi(user, listTolidInfos);
		SraException.assertNoError(ctx);
		
			
		
		// Envoie des fichiers xml et recuperation des fichiers ebiRetour 
		for (TolidInfos tolidInfos : listTolidInfos) {
			// pour umbrella
			sendUmbrellaAndloadEbiReport(tolidInfos.submissionUmbrella, ctx);
			
			// pour study sequencing
			sendStudyAndloadEbiReport(tolidInfos.submissionSequencing, ctx);
			
			// pour primaryAssembly
			sendStudyAndloadEbiReport(tolidInfos.submissionPrimaryAssembly, ctx);
		}
		
		println("Fin du traitement ChangeTolidIndividu");

	}


	// valide la liste des modifications de tolid et enleve doublons
	public List<TolidInfos> validUserListTolid(List<TolidInfos> listUserTolidInfos, ContextValidation ctx) {
		Map<String, TolidInfos> mapTolidInfos = new HashMap<String, TolidInfos>();
		List<TolidInfos> list_tolidInfos = new ArrayList<TolidInfos>();
		for (TolidInfos tolidInfos : listUserTolidInfos) {
			if(StringUtils.isBlank(tolidInfos.oldTolid) && StringUtils.isBlank(tolidInfos.newTolid)) {
				continue;
			}
			if(StringUtils.isBlank(tolidInfos.oldTolid)) {
				ctx.addError("oldTolid:", " absent avec newTolid=" + tolidInfos.newTolid);
				continue;
			}
			if(StringUtils.isBlank(tolidInfos.newTolid)) {
				ctx.addError("newTolid:", " absent avec oldTolid=" + tolidInfos.oldTolid);
				continue;
			}
			if(tolidInfos.oldTolid.equals(tolidInfos.newTolid)) {
				ctx.addError("oldTolid:" + tolidInfos.oldTolid + ": ", "associe a newTolid:" + tolidInfos.newTolid + " (old et new tolid identiques)");
				continue;
			}
			if( ! tolidInfos.oldTolid.replaceAll("\\d+$", "").equals(tolidInfos.newTolid.replaceAll("\\d+$", "")) ) {
				ctx.addError("oldTolid:" + tolidInfos.oldTolid, " qui doit etre remplace par newTolid:" + tolidInfos.newTolid + ": oldTolid et newTolid ne correspondent pas a la meme espece ??? ");
			}
			if (mapTolidInfos.containsKey(tolidInfos.oldTolid)) {				
				if(! mapTolidInfos.get(tolidInfos.oldTolid).newTolid.equals(tolidInfos.newTolid)) {
					// duplication du oldTolid avec incoherence sur le newTolid :
					ctx.addError("oldTolid " + tolidInfos.oldTolid + ":", "duplication dans fichier utilisateur du oldTolid avec newTolid differents " + mapTolidInfos.get(tolidInfos.oldTolid).newTolid + " et " + tolidInfos.newTolid);
					continue;
				} else {
					continue;
				}
			} 
			mapTolidInfos.put(tolidInfos.oldTolid, tolidInfos);
		}
		// verifier qu'on n'a pas 2 fois un newTolid dans la map :
		List<String> list_newTolid = new ArrayList<String>();
		for (String oldTolid : mapTolidInfos.keySet()) {
			if(list_newTolid.contains(mapTolidInfos.get(oldTolid).newTolid)) {
				ctx.addError("newTolid " + mapTolidInfos.get(oldTolid).newTolid + ":", "duplication dans fichier utilisateur avec oldTolid differents ");
				continue;
			}
			list_newTolid.add(mapTolidInfos.get(oldTolid).newTolid);
		}
		
		// construire la liste des tolidInfos si aucune erreur
		if( ! ctx.hasErrors() ) {
			for (String oldTolid : mapTolidInfos.keySet()) {
				list_tolidInfos.add(mapTolidInfos.get(oldTolid));
			}
		}
		return list_tolidInfos;
	}

	// Renvoie la liste des modifications de tolid de l'utilisateur.
	public List<TolidInfos> parseWorkbook(XSSFWorkbook workbook) throws Exception {
		List<TolidInfos> listTolidInfos = new ArrayList<TolidInfos>();
		Iterator<Row> itr = workbook.getSheetAt(0).rowIterator();
		//workbook.getSheetAt(0).rowIterator().forEachRemaining(row -> {
		int countLine = 0;

		while(itr.hasNext()) {
			countLine++;
			Row row = itr.next();
			// verification entete :
			if(countLine == 1) { 
				logger.debug("verification entete");
				if(row != null && row.getCell(0) != null && row.getCell(1) != null) {
					// certains caracteres speciaux ne repondent pas au trim()  (voir fichier donné par Caro dans ticket SUPSQ-5948)
					// ni aux expressions regulieres classiques en java pour supprimer des espaces en debut et fin de mot
					// d'ou l'utilisation de StringUtils.normalizeSpace avant le trim
					String enteteOldTolid = row.getCell(0).getStringCellValue();
					String enteteNewTolid = row.getCell(1).getStringCellValue();
					enteteOldTolid = StringUtils.normalizeSpace(java.text.Normalizer.normalize(enteteOldTolid, java.text.Normalizer.Form.NFD));
					enteteOldTolid = enteteOldTolid.trim();
					enteteNewTolid = StringUtils.normalizeSpace(java.text.Normalizer.normalize(enteteNewTolid, java.text.Normalizer.Form.NFD));
					enteteNewTolid = enteteNewTolid.trim();
					if(enteteOldTolid.equalsIgnoreCase("oldTolid") && enteteNewTolid.equalsIgnoreCase("newTolid")) {
						logger.debug("ok, on ignore entete.");
					} else {
						throw new Exception("Absence de l'entete attendue (oldTolid newTolid) dans le fichier d'entree");
					}
				} else {
					throw new Exception("Absence de l'entete attendue (oldTolid newTolid) dans le fichier d'entree");
				}
				continue;
			}
			// traitement des lignes de données :
			if(row != null && row.getCell(0) != null && row.getCell(1) != null) {
				// certains caracteres speciaux ne repondent pas au trim()  (voir fichier donné par Caro dans ticket SUPSQ-5948)
				// ni aux expressions regulieres classiques en java pour supprimer des espaces en debut et fin de mot
				// d'ou l'utilisation de StringUtils.normalizeSpace avant le trim
				String oldTolid = row.getCell(0).getStringCellValue();
				String newTolid = row.getCell(1).getStringCellValue();
				oldTolid = StringUtils.normalizeSpace(java.text.Normalizer.normalize(oldTolid, java.text.Normalizer.Form.NFD));
				oldTolid = oldTolid.trim();
				newTolid = StringUtils.normalizeSpace(java.text.Normalizer.normalize(newTolid, java.text.Normalizer.Form.NFD));
				newTolid = newTolid.trim();

				if(StringUtils.isBlank(oldTolid) && StringUtils.isBlank(newTolid)) {
					//logger.debug("ligne vide");
				} else if (StringUtils.isNotBlank(oldTolid) && StringUtils.isNotBlank(newTolid)) {
					TolidInfos tolidInfos = new TolidInfos();
					tolidInfos.oldTolid = oldTolid;
					tolidInfos.newTolid = newTolid;
					//logger.debug("kkkkkkkkkkkkkkkkkkkkkk    oldTolid='{}' et newTolid='{}'", tolidInfos.oldTolid, tolidInfos.newTolid);
					listTolidInfos.add(tolidInfos); 
				} else {
					throw new Exception("Absence d'un champs pour la ligne " + countLine);
				}
			}
		} // end while

		return listTolidInfos;
	}


}
