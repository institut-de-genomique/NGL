package sra.scripts;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.util.StringUtil;
import org.mongojack.DBQuery;

import controllers.sra.api.AbstractSampleAPI;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.chunked.ScriptWithArgs;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.sra.AnalysisDAO;
import fr.cea.ig.ngl.dao.sra.ExperimentDAO;
import fr.cea.ig.ngl.dao.sra.SampleDAO;
import fr.cea.ig.ngl.dao.sra.StudyDAO;
import models.sra.submit.sra.instance.AbstractSample;
import models.sra.submit.sra.instance.Analysis;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.sra.instance.ExternalSample;
import models.sra.submit.sra.instance.Sample;
import models.sra.submit.sra.instance.Study;
import models.utils.InstanceConstants;
import services.sra.SraException;
import sra.api.SubmissionNewAPITools;
import validation.ContextValidation;

/*
  * Attention, ce script force la modification de la releaseDate et permet de remettre dans la base des objets publics dans le domaine privé.
 *  Script à utiliser si study devenu public et remis privé par l'EBI, pour mettre à jour notre base.
 *  Script à utiliser pour modifier la date de release du study, des analyses et experiments et samples qui le reférencent sans
 *  controle de violation des status public/privé
 * {@code http://localhost:9000/sra/scripts/run/sra.scripts.ForceAndCascadeReleaseDate?studyIdentifier=ERP122213xxx&releaseDate=13-03-2026}
 *
 * @author sgas
 *
 */
public class ForceAndCascadeReleaseDate extends ScriptWithArgs<ForceAndCascadeReleaseDate.MyParam>{
	@SuppressWarnings("unused")
	private static final play.Logger.ALogger logger = play.Logger.of(ForceAndCascadeReleaseDate.class);
	private final ExperimentDAO         experimentDAO;
	private final AbstractSampleAPI     abstractSampleAPI;
	private final StudyDAO              studyDAO;
	private final SubmissionNewAPITools submissionNewAPITools;
	private final AnalysisDAO           analysisDAO;
	private final SampleDAO             sampleDAO;

	@Inject
	public ForceAndCascadeReleaseDate(
			ExperimentDAO 			experimentDAO,
			AbstractSampleAPI 		abstractSampleAPI,
			StudyDAO          		studyDAO,
			SampleDAO               sampleDAO,
			SubmissionNewAPITools   submissionNewAPITools, 
			AnalysisDAO 			analysisDAO
			) {
		this.experimentDAO = experimentDAO;
		this.studyDAO = studyDAO;
		this.sampleDAO = sampleDAO;
		this.abstractSampleAPI = abstractSampleAPI;
		this.submissionNewAPITools = submissionNewAPITools;
		this.analysisDAO       = analysisDAO;
	}
	
	// ma structure de controle et stockage des arguments de l'url
	public static class MyParam {
		public String   releaseDate;
		public String   studyIdentifier;
	}
	
	
	private void forceAndPropageReleaseDateInBaseForExpAnalysisSamples(Study study, String user) throws APIValidationException, APIException {
		List <Analysis> analysisList = new ArrayList<Analysis>();
		List <Experiment> expList    = new ArrayList<Experiment>();
		if (study == null) {
			throw new SraException("study en entree null ");
		}
		if (study.code == null || StringUtils.isBlank(study.code)) {
			throw new SraException("study en entree avec code null");
		}
		if (study.accession == null || StringUtils.isBlank(study.accession)) {
			throw new SraException("study en entree avec accession null ");
		}		
		if(study.releaseDate==null) {
			throw new SraException("study en entree sans releaseDate");
		}
		if (study.releaseDate != null && study.releaseDate.before(new Date())) {
			study.consolidatedPublicStatus = true;
		} else {
			study.consolidatedPublicStatus = false;
		}
		study.traceInformation.modifyUser = user;
		study.traceInformation.modifyDate = new Date();	
		// sauver le study avec sa date de release et son consolidatedPublicStatus
		studyDAO.update(study);
		
		// Recuperer analyses liées au study
		analysisList = MongoDBDAO.find( InstanceConstants.SRA_ANALYSIS_COLL_NAME, Analysis.class, 
				DBQuery.or
				(DBQuery.in("studyAccession", study.accession), 
				 DBQuery.in("studyCode", study.code))).toList();

		expList = MongoDBDAO.find( InstanceConstants.SRA_EXPERIMENT_COLL_NAME, Experiment.class, 
				DBQuery.or
				(DBQuery.in("studyAccession", study.accession), 
						DBQuery.in("studyCode", study.code))).toList();

		HashMap<String, Sample>       hSampleToUpdateForReleaseDate = new HashMap<String, Sample>();

		// Sauver les analyses deja soumises à l'EBI avec releaseDate et consolidatedPublicStatus
		for(Analysis analysis : analysisList) {
			if(StringUtils.isBlank(analysis.accession)) {
				// ignorer analysis non soumis pour releaseDate
				continue;
			}
			// Renseigner les analyses et sauver en base
			analysis.traceInformation.modifyUser = user;
			analysis.traceInformation.modifyDate = new Date();	
			analysis.releaseDate = study.releaseDate;
			analysis.consolidatedPublicStatus = study.consolidatedPublicStatus;
			analysisDAO.update(analysis);

			// Repertorier sample à modifier 
			if(analysis.sampleCode == null || StringUtils.isBlank(analysis.sampleCode)) {
				throw new SraException("analysis "+ analysis.code + " sans sampleCode");
			}
			if(analysis.sampleCode.startsWith("externalSample_")) {
				// rien le sample est un ExternalSample., il est public.
			} else {
				AbstractSample abstSample = abstractSampleAPI.get(analysis.sampleCode);
				if(abstSample instanceof ExternalSample) {
					// rien à recalculer
				} else {
					if(StringUtil.isNotBlank(abstSample.accession) && ! hSampleToUpdateForReleaseDate.containsKey(abstSample.code)) {
						hSampleToUpdateForReleaseDate.put(analysis.sampleCode, (Sample) abstSample);
					}
				}	
			}
		}
		// Sauver les experiments deja soumis à l'EBI avec releaseDate et consolidatedPublicStatus
		for(Experiment experiment : expList) {
			if(StringUtils.isBlank(experiment.accession)) {
				// ignorer experiment non soumis pour releaseDate
				continue;
			}
			// Renseigner les experiments et sauver en base
			experiment.traceInformation.modifyUser = user;
			experiment.traceInformation.modifyDate = new Date();	
			experiment.releaseDate = study.releaseDate;
			experiment.consolidatedPublicStatus = study.consolidatedPublicStatus;
			experimentDAO.update(experiment);
			
			// Repertorier sample à modifier 
			if(experiment.sampleCode == null || StringUtils.isBlank(experiment.sampleCode)) {
				throw new SraException("analysis "+ experiment.code + " sans sampleCode");
			}
			if(experiment.sampleCode.startsWith("externalSample_")) {
				// rien le sample est un ExternalSample., il est public.
			} else {
				AbstractSample abstSample = abstractSampleAPI.get(experiment.sampleCode);
				if(abstSample instanceof ExternalSample) {
					// rien à recalculer
				} else {
					if(StringUtil.isNotBlank(abstSample.accession) && ! hSampleToUpdateForReleaseDate.containsKey(abstSample.code)) {
						hSampleToUpdateForReleaseDate.put(experiment.sampleCode, (Sample) abstSample);
					}
				}	
			}
		}
		// Calculer les releaseDate des samples liés au study et sauver  
		for (Entry<String, Sample> setSample : hSampleToUpdateForReleaseDate.entrySet()) {
			Sample sample = setSample.getValue();
			sample.traceInformation.modifyUser = user;
			sample.traceInformation.modifyDate = new Date();
			// Comme tous les exp et analyse de la soumission sont en base avec leurs releaseDate, et comme pas de dereferencement de sample par les analyses
			// ou les experiments, on appelle la methode calculateSampleReleaseDate avec des hash vides.
			HashMap<String, HashMap<String,Experiment>> hSampleReferenceByExpCodeExp             = new HashMap<String,HashMap<String,Experiment>>();
			HashMap<String, HashMap<String,Experiment>> hSampleDereferenceByExpCodeExp           = new HashMap<String,HashMap<String,Experiment>>();
			HashMap<String, HashMap<String,Analysis>>   hSampleReferenceByAnalysisCodeAnalysis   = new HashMap<String,HashMap<String,Analysis>>();
			HashMap<String, HashMap<String,Analysis>>   hSampleDereferenceByAnalysisCodeAnalysis = new HashMap<String,HashMap<String,Analysis>>();
			// important de mettre context undefined plutot que update car validation des vieux samples
			// en mode undefined qui ne verifie pas presence attributs collectionDate et geographic 
			ContextValidation contextValidation = ContextValidation.createUndefinedContext(user);	
			submissionNewAPITools.calculateSampleReleaseDate(sample, 
					hSampleReferenceByExpCodeExp, 
					hSampleDereferenceByExpCodeExp,
					hSampleReferenceByAnalysisCodeAnalysis,
					hSampleDereferenceByAnalysisCodeAnalysis,
					contextValidation);
			SraException.assertNoError(contextValidation);
			sampleDAO.update(sample);	
		}
			
	}

	

	@Override
	public void execute(MyParam args) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		//String stReleaseDate = "13/03/2026";
		String stReleaseDate = args.releaseDate;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(sdf.parse(stReleaseDate));
		calendar.add(Calendar.HOUR_OF_DAY, 6);
		Date releaseDate = calendar.getTime();
		String studyIdentifier = args.studyIdentifier;
		println("studyAccession="+studyIdentifier);
		println("stReleaseDate = " + stReleaseDate);
		println("Demande pour forcer releaseDate du studyAccession " + studyIdentifier + " avec releaseDate=" + releaseDate);
		Study study = null;
		if ( studyIdentifier.startsWith("ERP") ) {
			if ( studyDAO.checkObjectExist("accession", studyIdentifier) ) {
				study = studyDAO.findOne(DBQuery.is("accession", studyIdentifier));
			}
		} else {
			if (studyDAO.checkObjectExist("externalId", studyIdentifier) ) {
				study = studyDAO.findOne(DBQuery.is("externalId", studyIdentifier));
			}	
		}
		if(study==null) {
			println("study " + studyIdentifier + " absent de la base");
			throw new SraException("study " + studyIdentifier + " absent de la base");
		}
		study.releaseDate = releaseDate;
		
		String user = "ngsrg";
		forceAndPropageReleaseDateInBaseForExpAnalysisSamples(study, user);
		println("fin d'execution du script");
	}





}