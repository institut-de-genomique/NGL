package sra.scripts;


import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.Script;
import fr.cea.ig.ngl.dao.sra.AnalysisDAO;
import fr.cea.ig.ngl.dao.sra.ConfigurationDAO;
import fr.cea.ig.ngl.dao.sra.ExperimentDAO;
import fr.cea.ig.ngl.dao.sra.ProjectDAO;
import fr.cea.ig.ngl.dao.sra.SampleDAO;
import fr.cea.ig.ngl.dao.sra.StudyDAO;
import fr.cea.ig.ngl.dao.sra.SubmissionDAO;
import models.sra.submit.sra.instance.Analysis;
import models.sra.submit.sra.instance.Configuration;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.sra.instance.Project;
import models.sra.submit.sra.instance.Sample;
import models.sra.submit.sra.instance.Study;
import models.sra.submit.sra.instance.Submission;
import models.utils.InstanceConstants;
import models.utils.dao.DAOException;



/*
 * Met a jour les objets pour modele avec champs consolidatedPublicStatus et releaseDate sur tous les objets SRA
 * Exemple de lancement :
 * http://localhost:9000/sra/scripts/run/sra.scripts.RepriseHistoForDropBox
 * @author sgas
 *
 */


// on passe par DAO plutot que par API car certains vieux objets ne franchissent pas la validation.
public class RepriseHistoForDropBox extends Script<RepriseHistoForDropBox.MyParam>{
	// private static final play.Logger.ALogger logger = play.Logger.of(UpdateExperimentForAccessions.class);

	private final StudyDAO          studyDAO;         // utilisation DAO car existe des vieux study sans description
	private final ProjectDAO        projectDAO;       // utilisation DAO car existe des vieux study sans description
	private final ExperimentDAO     experimentDAO;    // utilisation DAO car existe des vieux exp avec instrument=unspecified
	private final SampleDAO         sampleDAO;// utilisation DAO car sample soumis avant 2023 sans attributs obligatoires geographicLocation et collectionDate
	private final AnalysisDAO       analysisDAO;
	private final SubmissionDAO     submissionDAO;
	private final ConfigurationDAO  configurationDAO;

	@Inject
	public RepriseHistoForDropBox(
			StudyDAO              studyDAO,
			ProjectDAO            projectDAO,
			SampleDAO             sampleDAO,
			ExperimentDAO         experimentDAO,
			AnalysisDAO           analysisDAO,
			ConfigurationDAO      configurationDAO,
			SubmissionDAO         submissionDAO) {

		this.studyDAO              = studyDAO;
		this.projectDAO            = projectDAO;
		this.experimentDAO         = experimentDAO;
		this.sampleDAO             = sampleDAO;
		this.analysisDAO           = analysisDAO;
		this.configurationDAO      = configurationDAO;
		this.submissionDAO         = submissionDAO;

	}


	// ma structure de controle et stockage des arguments de l'url
	public static class MyParam {
	}

	public class VisibiliteSample {
		boolean consolidatedPublicStatus = false; 
		Date releaseDate = null;  // releaseDate la plus petite 
	}
	public class Infos {
		String studyAccession = null; 
		String studyCode = null; 
		String sampleAccession = null;
		String sampleCode = null;
	}	

	@Override
	public void execute(MyParam args) throws Exception {
		Date dateDebut = new Date();
		printfln("debut traitement le " +  dateDebut);
		String user = "ngsrg";
		//updateModeleForConfiguration(user);
		//updateModeleForProject(user);
		//updateModeleForAnalysis(user);
		//updateModeleForStudy(user);
		//updateModeleForSample(user);
		//updateModeleForExperiment(user);
		updateModeleForSubmission(user);
		Date dateFin = new Date();
		printfln("debut du traitement le "+ dateDebut + " fin du traitement le " + dateFin);

	} // end execute

	
	@SuppressWarnings("unused")
	private void updateModeleForConfiguration(String user) {
		List<Configuration> configurationList = MongoDBDAO.find( InstanceConstants.SRA_CONFIGURATION_COLL_NAME, Configuration.class, 
				DBQuery.notExists("dropBox")).toList(); 
		printfln("nombre de configuration " + configurationList.size());

		for (Configuration configuration : configurationList) {
			configuration.dropBox = "Webin-9";
			// Mise à jour du study et sauvegarde en base:
			configuration.traceInformation.modifyDate = new Date();
			configuration.traceInformation.modifyUser = user;
			configurationDAO.update(configuration);
			printfln("MAJ de la configuration " + configuration.code + " pour modele et dropBox");
		}
	}
		
	
	@SuppressWarnings("unused")
	private void updateModeleForProject(String user) {
		List<Project> projectList = MongoDBDAO.find( InstanceConstants.SRA_PROJECT_COLL_NAME, Project.class, 
				DBQuery.notExists("dropBox")).toList(); 	
		printfln("nombre de project " + projectList.size());

		for (Project project : projectList) {
			project.dropBox = "Webin-9";
			// Mise à jour du study et sauvegarde en base:
			project.traceInformation.modifyDate = new Date();
			project.traceInformation.modifyUser = user;
			projectDAO.update(project);
			printfln("MAJ du project " + project.code + " pour modele et dropBox");
		}
	}
	

	@SuppressWarnings("unused")
	private void updateModeleForStudy(String user) {
		List<Study> studyList = MongoDBDAO.find( InstanceConstants.SRA_STUDY_COLL_NAME, Study.class, 
				DBQuery.and(DBQuery.in("_type", "Study"), DBQuery.notExists("dropBox"))).toList(); 	
		printfln("nombre de study " + studyList.size());

		for (Study study : studyList) {
			study.dropBox = "Webin-9";
			// Mise à jour du study et sauvegarde en base:
			study.traceInformation.modifyDate = new Date();
			study.traceInformation.modifyUser = user;
			studyDAO.update(study);
			printfln("MAJ du study " + study.code + " pour modele et dropBox");
		}
	}


	@SuppressWarnings("unused")
	private void updateModeleForSample(String user) {
		List<Sample> sampleList = MongoDBDAO.find( InstanceConstants.SRA_SAMPLE_COLL_NAME, Sample.class, 
				DBQuery.and(DBQuery.in("_type", "Sample"), DBQuery.notExists("dropBox"))).toList(); 
		printfln("nombre de sample " + sampleList.size());

		for (Sample sample : sampleList) {
			sample.dropBox = "Webin-9";
			// Mise à jour du study et sauvegarde en base:
			sample.traceInformation.modifyDate = new Date();
			sample.traceInformation.modifyUser = user;
			sampleDAO.update(sample);
			printfln("MAJ du sample " + sample.code + " pour modele et dropBox");
		}
	}

	@SuppressWarnings("unused")
	private void updateModeleForExperiment(String user) {
		List<Experiment> experimentList = MongoDBDAO.find( InstanceConstants.SRA_EXPERIMENT_COLL_NAME, Experiment.class, 
				DBQuery.notExists("dropBox")).toList(); 	
		printfln("nombre d experiment " + experimentList.size());

		for (Experiment experiment : experimentList) {
			experiment.dropBox = "Webin-9";
			// Mise à jour du study et sauvegarde en base:
			experiment.traceInformation.modifyDate = new Date();
			experiment.traceInformation.modifyUser = user;
			experimentDAO.update(experiment);
			printfln("MAJ de l'experiment " + experiment.code + " pour modele et dropBox");
		}
	}

	@SuppressWarnings("unused")
	private void updateModeleForAnalysis(String user) {
		List<Analysis> analysisList = MongoDBDAO.find( InstanceConstants.SRA_ANALYSIS_COLL_NAME, Analysis.class, 
				DBQuery.notExists("dropBox")).toList(); 
		printfln("nombre d analyse " + analysisList.size());

		for (Analysis analysis : analysisList) {
			analysis.dropBox = "Webin-9";
			// Mise à jour du study et sauvegarde en base:
			analysis.traceInformation.modifyDate = new Date();
			analysis.traceInformation.modifyUser = user;
			analysisDAO.update(analysis);
			printfln("MAJ de l'analyse " + analysis.code + " pour modele et dropBox");
		}
	}
	
	private void updateModeleForSubmission(String user) {
		List<Submission> submissionList = MongoDBDAO.find( InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class, 
				DBQuery.notExists("dropBox")).toList(); 
		printfln("nombre de submission " + submissionList.size());

		for (Submission submission : submissionList) {
			submission.dropBox = "Webin-9";
			// Mise à jour du study et sauvegarde en base:
			submission.traceInformation.modifyDate = new Date();
			submission.traceInformation.modifyUser = user;
			if(StringUtils.isNotBlank(submission.code)) {
				printfln("soumission avec code " + submission.code);
			}
			if(submission.code.equals("GSC_AQG_BXW_6CHE3N17Y")) {
				continue;
			}
			try {
			submissionDAO.update(submission);
			} catch (DAOException e) {
				printfln( "Exception pour sauvegarde de la soumission " + submission.code);
				continue;
			}
				
			
			if(StringUtils.isBlank(submission.code)) {
				printfln("soumission sans code ???");
			} else {
				printfln("MAJ de la soumission " + submission.code + " pour modele et dropBox");
			}
		}
	}
		

}
