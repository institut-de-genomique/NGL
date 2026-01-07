package sra.scripts.off;
//package sra.scripts;
//
//
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.List;
//
//import javax.inject.Inject;
//
//import org.apache.commons.lang.StringUtils;
//import org.mongojack.DBQuery;
//
//import fr.cea.ig.MongoDBDAO;
//import fr.cea.ig.lfw.controllers.scripts.buffered.Script;
//import fr.cea.ig.ngl.dao.api.APIException;
//import fr.cea.ig.ngl.dao.api.APIValidationException;
//import fr.cea.ig.ngl.dao.api.sra.AnalysisAPI;
//import fr.cea.ig.ngl.dao.sra.AbstractSampleDAO;
//import fr.cea.ig.ngl.dao.sra.AbstractStudyDAO;
//import fr.cea.ig.ngl.dao.sra.ExperimentDAO;
//import models.sra.submit.sra.instance.AbstractSample;
//import models.sra.submit.sra.instance.AbstractStudy;
//import models.sra.submit.sra.instance.Analysis;
//import models.sra.submit.sra.instance.Experiment;
//import models.sra.submit.sra.instance.ExternalSample;
//import models.sra.submit.sra.instance.ExternalStudy;
//import models.sra.submit.sra.instance.Sample;
//import models.sra.submit.sra.instance.Study;
//import models.sra.submit.util.SraException;
//import models.utils.InstanceConstants;
//import sra.api.submission.SubmissionNewAPITools;
//import validation.ContextValidation;
//
//
///*
// * Met a jour les objets pour modele avec champs consolidatedPublicStatus et releaseDate sur tous les objets SRA
// * Exemple de lancement :
// * http://localhost:9000/sra/scripts/run/sra.scripts.RepriseHistoReleaseDate
// * @author sgas
// *
// */
//
//
//// on passe par DAO plutot que par API car certains vieux objets ne franchissent pas la validation.
//public class RepriseHistoReleaseDate extends Script<RepriseHistoReleaseDate.MyParam>{
//	// private static final play.Logger.ALogger logger = play.Logger.of(UpdateExperimentForAccessions.class);
//
//	private final AbstractStudyDAO  abstractStudyDAO; // utilisation DAO car existe des vieux study sans description
//	private final ExperimentDAO     experimentDAO;    // utilisation DAO car existe des vieux exp avec instrument=unspecified
//	private final AbstractSampleDAO abstractSampleDAO;// utilisation DAO car sample soumis avant 2023 sans attributs obligatoires geographicLocation et collectionDate
//	private final AnalysisAPI       analysisAPI;
//	private final SubmissionNewAPITools submissionNewAPITools;
//	@Inject
//	public RepriseHistoReleaseDate(
//			AbstractStudyDAO      abstractStudyDAO,
//			AbstractSampleDAO     abstractSampleDAO,
//			ExperimentDAO         experimentDAO,
//			AnalysisAPI           analysisAPI,
//			SubmissionNewAPITools submissionNewAPITools) {
//
//		this.abstractStudyDAO      = abstractStudyDAO;
//		this.experimentDAO         = experimentDAO;
//		this.abstractSampleDAO     = abstractSampleDAO;
//		this.analysisAPI           = analysisAPI;
//		this.submissionNewAPITools = submissionNewAPITools;
//	}
//
//
//	// ma structure de controle et stockage des arguments de l'url
//	public static class MyParam {
//	}
//
//	public class VisibiliteSample {
//		boolean consolidatedPublicStatus = false; 
//		Date releaseDate = null;  // releaseDate la plus petite 
//	}
//	public class Infos {
//		String studyAccession = null; 
//		String studyCode = null; 
//		String sampleAccession = null;
//		String sampleCode = null;
//	}	
//
//	@Override
//	public void execute(MyParam args) throws Exception {
//		printfln("debut traitement");
//
//		String user = "ngsrg";
//		updateAnalysisForStudyAndSampleCode(user);
//		int countAbsStudy = 0;
//		for (AbstractStudy absStudy : abstractStudyDAO.all()) {
//		//AbstractStudy absStudy = abstractStudyDAO.getObject("STUDY_8CJM3NULJ");
//			countAbsStudy++;
//			updateModeleForStudy(absStudy, user);
//			// on appelle la methode pour mettre à jour les autres objets de la base avec le study sauvé en base
//			updateModeleForAnalysis(absStudy, user);			
//			updateModeleForExperiments(absStudy, user);
//		}
//		printfln("MAJ de " + countAbsStudy + " abstStudy dans la base");
//		updateModeleForAbsSample(user);
//		printfln("fin du traitement");
//
//	} // end execute
//
//	
//	private void updateAnalysisForStudyAndSampleCode(String user) throws APIValidationException, APIException {
//		// Mise a jour des analyses pour studyCode et sampleCode et sauvegarde en base :
//		int countAnalysis = 0;
//		for(Analysis analysis : analysisAPI.dao_all()) {
//			countAnalysis++;
//			analysis.traceInformation.modifyUser = user;
//			analysis.traceInformation.modifyDate = new Date();	
//			if(! abstractSampleDAO.checkObjectExist("accession", analysis.sampleAccession) ) {
//				if (StringUtils.isNotBlank(analysis.accession)) {
//					throw new SraException("analyse "+ analysis.code + " qui reference un sample " + analysis.sampleAccession + " qui n'existe pas dans base");
//				} else {
//					// analyse non soumis qui reference peut-etre un sample ext qui sera en base quand on creera la soumission
//				}
//			} else {
//				AbstractSample abstSample = abstractSampleDAO.findOne(DBQuery.in("accession", analysis.sampleAccession));
//				analysis.sampleCode = abstSample.code;
//			}
//			if(! abstractStudyDAO.checkObjectExist("accession", analysis.studyAccession)) {
//				if (StringUtils.isNotBlank(analysis.accession)) {
//					throw new SraException("analyse "+ analysis.code + " qui reference un study " + analysis.studyAccession + " qui n'existe pas dans base");
//				} else {
//					// analyse non soumis qui reference peut-etre un study ext qui sera en base quand on creera la soumission
//				}
//			} else {
//				AbstractStudy abstStudy = abstractStudyDAO.findOne(DBQuery.in("accession", analysis.studyAccession));
//				analysis.studyCode = abstStudy.code;
//			}
//			analysisAPI.update(analysis, user);
//
//		}	
//		printfln("MAJ de " + countAnalysis + " analyses pour studyCode et sampleCode");
//
//	}
//
//
//	private void updateModeleForStudy(AbstractStudy absStudy, String user) {
//
//		// ne pas ignorer absStudy sans numeros d'accession car consolidatedPublicStatus a renseigner
//		if(absStudy instanceof ExternalStudy) {
//			absStudy.consolidatedPublicStatus = true;
//		} else {
//			if(((Study) absStudy).releaseDate == null && StringUtils.isNotBlank(absStudy.accession)) {
//				// cas d'un study sans releaseDate et avec AC (pb, si AC alors doit avoir releaseDate)
//				throw new SraException("study "+ absStudy.code + " avec AC et aucune date de release");
//			} else if (((Study) absStudy).releaseDate == null && StringUtils.isBlank(absStudy.accession)) {
//				// cas d'un study sans releaseDate et sans AC (normal non soumis)
//				absStudy.consolidatedPublicStatus = false;
//			} else if (((Study) absStudy).releaseDate != null && StringUtils.isBlank(absStudy.accession)) {
//				// cas d'un study avec releaseDate sans AC (pb, releaseDate attribué à la soumission
//				throw  new SraException("study "+ absStudy.code + " sans AC et avec date de release ???");
//			} else if (((Study) absStudy).releaseDate != null && StringUtils.isNotBlank(absStudy.accession)) {
//				// cas d'un study avec releaseDate et AC
//				if(((Study) absStudy).releaseDate.before(new Date())) {
//					absStudy.consolidatedPublicStatus = true;
//				} else {
//					absStudy.consolidatedPublicStatus = false;
//				}
//			} else {
//				throw new SraException("study "+ absStudy.code + " cas non prevu");
//			}		
//		}
//		// Mise à jour du study et sauvegarde en base:
//		absStudy.traceInformation.modifyDate = new Date();
//		absStudy.traceInformation.modifyUser = user;
//		
//		//try {
//			//absStudy = abstractStudyAPI.update(absStudy, user);
//			abstractStudyDAO.update(absStudy);
//		//} catch (APIException e) {
//		//	throw new SraException("erreur lors de la sauvegarde du study " + absStudy.code + " " + e.getMessage());
//		//}
//		
//		printfln("MAJ de l'AbstractStudy " + absStudy.code + " pour modele et releaseDate");
//
//	}
//
//
//
//	private void updateModeleForAnalysis(AbstractStudy absStudy, String user) throws APIValidationException, APIException {
//
//		List <Analysis> analysisList = new ArrayList<Analysis>();
//		String studyAccession = "NONE";
//		String studyCode = "NONE";
//		if (StringUtils.isNotBlank(absStudy.accession)) {
//			studyAccession = absStudy.accession;
//		}
//		if (StringUtils.isNotBlank(absStudy.code)) {
//			studyCode = absStudy.code;
//		}
//		printfln("OK avec studyCode = " + studyCode + " et studyAccession="+ studyAccession);
//		analysisList = MongoDBDAO.find( InstanceConstants.SRA_ANALYSIS_COLL_NAME, Analysis.class, 
//				DBQuery.or(DBQuery.in("studyAccession", studyAccession), DBQuery.in("studyCode", studyCode))).toList(); 			
//		
//		int countAnalysis = 0;
//		for(Analysis analysis : analysisList) {
//			countAnalysis++;
//			if(StringUtils.isBlank(analysis.accession)) {
//				// ignorer analysis non soumis pour releaseDate mais ajouter consolidatedPublicStatus à false
//				analysis.consolidatedPublicStatus = false;
//				//analysis.releaseDate = null;
//				printfln("analysis 	avec accession");
//			} else {
//				printfln("analysis 	sans accession");
//				// Renseigner les analyses qui n'ont pas dans l'ancien modele de champs releaseDate ou consolidatedPublicStatus 
//				if(absStudy instanceof ExternalStudy) {
//					printfln("absStudy de type ExternalStudy");
//					analysis.consolidatedPublicStatus = true;
//					// si analyse (avec AC) associé à un study externe, date de release = date de premiere soumission
//					analysis.releaseDate = analysis.firstSubmissionDate;
//				} else {
//					printfln("absStudy de type Study");
//					if (absStudy.consolidatedPublicStatus) {
//						printfln("absStudy de type Study public");
//
//						// si study public avec date de release
//						analysis.consolidatedPublicStatus = true;
//						// si analyse soumise après release du study alors analyse soumise en public dès première soumission, sinon devenue public en meme temps que le study
//						if (( (Study) absStudy ).releaseDate.before(analysis.firstSubmissionDate)) {
//							printfln("analysis public soumis apres releaseDate du study");
//							analysis.releaseDate = analysis.firstSubmissionDate;
//						} else {
//							printfln("analysis public soumis avant releaseDate du study");
//							analysis.releaseDate = ( (Study) absStudy ).releaseDate;
//						}
//					} else {
//						printfln("absStudy de type Study prive");
//						analysis.consolidatedPublicStatus = false;
//						analysis.releaseDate = ( (Study) absStudy ).releaseDate;
//					}
//				}
//			}
//			// Sauver en base
//			analysis.traceInformation.modifyUser = user;
//			analysis.traceInformation.modifyDate = new Date();	
//			analysis = analysisAPI.update(analysis, user);
//		}
//		printfln("MAJ de " + countAnalysis + " analyses pour modele et releaseDate");
//	}
//
//	private void updateModeleForExperiments(AbstractStudy absStudy, String user) throws APIValidationException, APIException {
//
//		List <Experiment> experimentList = new ArrayList<Experiment>();
//		String studyAccession = "NONE";
//		String studyCode = "NONE";
//		if (StringUtils.isNotBlank(absStudy.accession)) {
//			studyAccession = absStudy.accession;
//		}
//		if (StringUtils.isNotBlank(absStudy.code)) {
//			studyCode = absStudy.code;
//		}
//
//		experimentList = MongoDBDAO.find( InstanceConstants.SRA_EXPERIMENT_COLL_NAME, Experiment.class, 
//				DBQuery.or(DBQuery.in("studyAccession", studyAccession), 
//						DBQuery.in("studyCode", studyCode))).toList();
//
//		int countExperiments = 0;
//
//		for(Experiment experiment : experimentList) {
//			countExperiments++;
//			if(StringUtils.isBlank(experiment.accession)) {
//				// ignorer experiment non soumis pour releaseDate mais ajouter consolidatedPublicStatus à false
//				experiment.consolidatedPublicStatus = false;
//				//experiment.releaseDate = null;
//			} else {
//				// Renseigner les analyses qui n'ont pas dans l'ancien modele de champs releaseDate ou consolidatedPublicStatus 
//				if(absStudy instanceof ExternalStudy) {
//					experiment.consolidatedPublicStatus = true;
//					// si experiment (avec AC) associé à un study externe, date de release = date de premiere soumission
//					experiment.releaseDate = experiment.firstSubmissionDate;
//				} else {
//					if (absStudy.consolidatedPublicStatus) {
//						// si study public avec date de release
//						experiment.consolidatedPublicStatus = true;
//						//printfln(experiment.code);
//						// corrections erreurs dans base pour 48 exp soumis avant 2018 avec AC sans firstSubmissionDate:
//						if(experiment.firstSubmissionDate==null ) {
//							printfln("Pas de firstSubmissionDate pour " + experiment.code);
//							experiment.firstSubmissionDate = experiment.state.date; 
//						}
//						// si experiment soumis après release du study alors experiment soumis en public dès première soumission, sinon devenue public en meme temps que le study
//						if ( ((Study) absStudy).releaseDate != null && 
//							    ((Study) absStudy).releaseDate.before(experiment.firstSubmissionDate)) {							
//							experiment.releaseDate = experiment.firstSubmissionDate;
//						} else {
//							experiment.releaseDate = ( (Study) absStudy ).releaseDate;
//						}
//					} else {
//						experiment.consolidatedPublicStatus = false;
//						experiment.releaseDate = ( (Study) absStudy ).releaseDate;
//					}
//				}
//			}
//			// Sauver en base
//			experiment.traceInformation.modifyUser = user;
//			experiment.traceInformation.modifyDate = new Date();	
//			// utilisation de experimentDAO plutot que experimentAPI car erreur de vieux exp avec nstrument="unspecified"
//			experimentDAO.update(experiment);  
//		}
//		printfln("MAJ de " + countExperiments + " experiments pour modele et releaseDate");
//
//	}
//
//	private void updateModeleForAbsSample(String user) throws APIValidationException, APIException {
//		HashMap<String, HashMap<String,Experiment>> hSampleReferenceByExpCodeExp             = new HashMap<String,HashMap<String,Experiment>>();
//		HashMap<String, HashMap<String,Experiment>> hSampleDereferenceByExpCodeExp           = new HashMap<String,HashMap<String,Experiment>>();
//		HashMap<String, HashMap<String,Analysis>>   hSampleReferenceByAnalysisCodeAnalysis   = new HashMap<String,HashMap<String,Analysis>>();
//		HashMap<String, HashMap<String,Analysis>>   hSampleDereferenceByAnalysisCodeAnalysis = new HashMap<String,HashMap<String,Analysis>>();
//
////		ContextValidation contextValidation = ContextValidation.createUpdateContext(user);	
//		ContextValidation contextValidation = ContextValidation.createUndefinedContext(user);	
//		
//		int countAbstSample = 0;
//		
//		for (AbstractSample abstSample : abstractSampleDAO.all()) {
//			countAbstSample++;
//			printfln("abstSample="+ abstSample.code);
//			if(abstSample instanceof ExternalSample) {
//				// rien à recalculer pour releaseDate mais il faut ajouter consolidated à true et sauver
//				abstSample.consolidatedPublicStatus = true;
//				abstSample.traceInformation.modifyUser = user;
//				abstSample.traceInformation.modifyDate = new Date();	
//				abstractSampleDAO.update(abstSample);
//			} else {
//				if(StringUtils.isBlank(abstSample.accession)) {
//					// cas d'un Sample non soumis  : rien à recalculer pour releaseDate mais il faut ajouter consolidated à false et sauver
//					abstSample.consolidatedPublicStatus = false;
//					abstSample.traceInformation.modifyUser = user;
//					abstSample.traceInformation.modifyDate = new Date();	
//					abstractSampleDAO.update(abstSample);
//				} else {
//					// NGL-4373 : si update des experiments ou analyse pour releaseDate alors, il faut calculer les releaseDates des samples references par ces exp et analyses
//					// Comme tous les exp et analyse de la soumission sont en base avec leurs releaseDate, et comme pas de dereferencement de sample par les analyses
//					// ou les experiments, on appelle la methode calculateSampleReleaseDate avec des hash vides.
//					//HashMap<String, HashMap<String,Experiment>> hSampleReferenceByExpCodeExp             = new HashMap<String,HashMap<String,Experiment>>();
//					//HashMap<String, HashMap<String,Experiment>> hSampleDereferenceByExpCodeExp           = new HashMap<String,HashMap<String,Experiment>>();
//					//HashMap<String, HashMap<String,Analysis>>   hSampleReferenceByAnalysisCodeAnalysis   = new HashMap<String,HashMap<String,Analysis>>();
//					//HashMap<String, HashMap<String,Analysis>>   hSampleDereferenceByAnalysisCodeAnalysis = new HashMap<String,HashMap<String,Analysis>>();
//					//ContextValidation contextValidation = ContextValidation.createUpdateContext(user);		
//					Sample sample = (Sample) abstSample;
//						//printfln("calcul releaseDate pour "+ sample.code);
//						submissionNewAPITools.calculateSampleReleaseDate(sample, 
//								hSampleReferenceByExpCodeExp, 
//								hSampleDereferenceByExpCodeExp,
//								hSampleReferenceByAnalysisCodeAnalysis,
//								hSampleDereferenceByAnalysisCodeAnalysis,
//								contextValidation);
//						//printfln("fin calcul releaseDate pour "+ sample.code);
//						SraException.assertNoError(contextValidation);
//						abstractSampleDAO.update(sample);
//				}	
//			}
//		}
//		printfln("MAJ de " + countAbstSample + " AbstractSample pour modele et releaseDate");
//
//	}
//
//}
