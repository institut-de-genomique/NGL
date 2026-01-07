package sra.services;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import controllers.sra.api.AbstractSampleAPI;
import controllers.sra.api.AbstractStudyAPI;
import controllers.sra.api.AnalysisAPI;
import controllers.sra.api.ExperimentAPI;
import controllers.sra.api.ExternalSampleAPI;
import controllers.sra.api.ExternalStudyAPI;
import controllers.sra.api.ProjectAPI;
import controllers.sra.api.ReadsetAPI;
import controllers.sra.api.SampleAPI;
import controllers.sra.api.StudyAPI;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.utils.Iterables;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.sra.ExperimentDAO;
import fr.cea.ig.ngl.dao.sra.SampleDAO;
import fr.cea.ig.ngl.dao.sra.SubmissionDAO;
import models.laboratory.common.instance.State;
import models.sra.submit.sra.instance.AbstractSample;
import models.sra.submit.sra.instance.AbstractStudy;
import models.sra.submit.sra.instance.Analysis;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.sra.instance.ExternalSample;
import models.sra.submit.sra.instance.ExternalStudy;
import models.sra.submit.sra.instance.Project;
import models.sra.submit.sra.instance.Readset;
import models.sra.submit.sra.instance.Sample;
import models.sra.submit.sra.instance.Study;
import models.sra.submit.sra.instance.Submission;
import models.sra.submit.sra.instance.Submission.TypeRawDataSubmitted;
import models.utils.InstanceConstants;
import ngl.refactoring.state.SRASubmissionStateNames;
import play.Logger;
import services.sra.SraCodeHelper;
import services.sra.SraException;
import services.sra.VariableSRA;
import services.taxonomy.Taxon;
import services.taxonomy.TaxonomyServices;
import sra.api.SubmissionNewAPITools;
import validation.ContextValidation;

public class UpdateServices  {

	private static final play.Logger.ALogger logger = play.Logger.of(UpdateServices.class);
	private final ProjectAPI      	  projectAPI;
	private final StudyAPI      	  studyAPI;
	private final SampleAPI           sampleAPI;
	private final SampleDAO           sampleDAO;
	private final ExperimentAPI       experimentAPI;
	private final ExperimentDAO       experimentDAO;
	private final ReadsetAPI          readsetAPI;
	private final SubmissionDAO       submissionDAO;
	private final SraCodeHelper       sraCodeHelper;
	private final AbstractStudyAPI    abstractStudyAPI;
	private final AbstractSampleAPI   abstractSampleAPI;
	private final ExternalSampleAPI   externalSampleAPI;
	private final ExternalStudyAPI    externalStudyAPI;
	private final AnalysisAPI         analysisAPI;
	private final CreateServices      createServices;
	private final String              patternSampleAc = "(E|S)RS\\d+";
	private final String              patternSampleExternalId = "SAM";
	private final String              patternStudyAc = "ERP\\d+";
	private final String              patternStudyExternalId  = "PRJ";
	//private final TaxonomyServices    taxonomyServices;
	private final SubmissionNewAPITools submissionNewAPITools;


	@Inject
	public UpdateServices(SubmissionDAO       submissionDAO,
			ProjectAPI          projectAPI,
			StudyAPI            studyAPI,
			SampleAPI           sampleAPI,
			SampleDAO           sampleDAO,
			ExperimentAPI       experimentAPI,
			ExperimentDAO       experimentDAO,
			ReadsetAPI          readsetAPI,
			SraCodeHelper       sraCodeHelper,
			AbstractStudyAPI    abstractStudyAPI,
			AbstractSampleAPI   abstractSampleAPI,
			ExternalStudyAPI    externalStudyAPI,
			ExternalSampleAPI   externalSampleAPI,
			AnalysisAPI         analysisAPI,
			SubmissionNewAPITools submissionNewAPITools,
			//TaxonomyServices    taxonomyServices,
			CreateServices      createServices) {
		this.submissionDAO       = submissionDAO;
		this.studyAPI            = studyAPI;
		this.projectAPI          = projectAPI;
		this.sampleAPI           = sampleAPI;
		this.sampleDAO           = sampleDAO;
		this.experimentAPI       = experimentAPI;
		this.experimentDAO       = experimentDAO;
		this.readsetAPI          = readsetAPI;
		this.sraCodeHelper       = sraCodeHelper;
		this.abstractStudyAPI    = abstractStudyAPI;
		this.abstractSampleAPI   = abstractSampleAPI;
		this.externalStudyAPI    = externalStudyAPI;
		this.externalSampleAPI   = externalSampleAPI;
		this.analysisAPI         = analysisAPI;
		this.submissionNewAPITools = submissionNewAPITools;
		//this.taxonomyServices    = taxonomyServices;
		this.createServices      = createServices;
		//this.tools               = tools;
	}



	private  List <String> valideArgsCreateSubmissionForUpdate(ContextValidation ctxVal,
			Project umbrella,
			Study study, 
			List<Sample> samples,
			List<Experiment> experiments)  {
		//logger.debug("BBBBBBBBBBB   Dans valideArgsCreateSubmissionForUpdate " + new Date());

		List <String> projectCodes = new ArrayList<String>();
		boolean dataForEbi = false;
		ctxVal.setUpdateMode();
		String user = ctxVal.getUser();

		if (study != null) {
			dataForEbi = isValideStudyCreateSubmissionForUpdate(ctxVal, user, study, projectCodes);
		} 
		if (umbrella != null) {
			dataForEbi = isValideUmbrellaCreateSubmissionForUpdate(ctxVal, user, umbrella);
		} 	
		if(samples.size()>0) {
			dataForEbi = isValideSamplesCreateSubmissionForUpdate(ctxVal, user, samples, projectCodes);
		}
		if(experiments.size() > 0) {
			dataForEbi = isValideExperimentsCreateSubmissionForUpdate(ctxVal, user, experiments, projectCodes);
		}
		
		if(!dataForEbi) {
			ctxVal.addError("valideArgsCreateSubmissionForUpdate", "Aucun project umbrella, study, sample ou experiment à mettre à jour??");	
		}
		//logger.debug("BBBBBBBBBBB   je sors de  valideArgsCreateSubmissionForUpdate " + new Date());

		return projectCodes;
	}
	
	private boolean isValideStudyCreateSubmissionForUpdate(ContextValidation ctxVal, String user, Study study, List<String>projectCodes) {

			
		SraCodeHelper.assertObjectStateCode(study, SRASubmissionStateNames.SUB_F, ctxVal);
		//study.setTraceUpdateStamp(user);
		// Mettre dans l'etat qui sera sauvé si tout va bien:
		study.updateStateAndTrace(new State(SRASubmissionStateNames.SUBU_N, user));
		study.validate(ctxVal); 
		// NGL-4593
		if(StringUtils.isBlank(study.dropBox)) {
			ctxVal.addError("valideStudyCreateSubmissionForUpdate", "study "+ study.code + " sans dropBox\n");
		}
		if (study.traceInformation.createUser == null) {
			String message = "Le study " + study.code
					+ " n'est pas renseigné dans la base pour le champs createUser\n";	
			//throw new SraException("createSubmissionForUpdate", message);
			ctxVal.addError("valideStudyCreateSubmissionForUpdate", message);
		}
		if (user == null) {
			String message = "l'utilisateur n'est pas authentifie \n";
			//throw new SraException("createSubmissionForUpdate", message);
			ctxVal.addError("valideStudyCreateSubmissionForUpdate", message);
		}

		// verifier qu'aucun locusTagPrefix du study n'existe dans un autre study :
		String message = "";

		if (study.locusTagPrefixs != null && !study.locusTagPrefixs.isEmpty()) {
			for (String locus : study.locusTagPrefixs) {
				List <Study> studyList = studyAPI.dao_find(DBQuery.in("locusTagPrefixs", locus)).toList();
				if (studyList.size() > 0) {
					String header = "locusTagPrefix =" + locus + "\n";
					//message += header;
					for (Study s: studyList) {
						if(! s.code.equals(study.code)) {
							message += "       - deja utilise par le study " + s.code + "\n";
						}
					}
					if(StringUtils.isNotBlank(message)) {
						ctxVal.addError("valideStudyCreateSubmissionForUpdate", header + message);
					}
				}	 
			}
		}

		// Une meme reference bibliographique peut exister dans plusieurs study : pas de controle d'unicité

		Study dbStudy = studyAPI.get(study.code);
		// verifier qu'on ne supprime pas un locus_tag existant dans le study :
		message = "";
		if (dbStudy.locusTagPrefixs != null && !dbStudy.locusTagPrefixs.isEmpty()) {
			for (String dblocus : dbStudy.locusTagPrefixs) {
				if (study.locusTagPrefixs == null || !study.locusTagPrefixs.contains(dblocus)) {
					message += dblocus + ", ";
				}
			}
		}
		if (StringUtils.isNotBlank(message)) {
			message = message.replaceFirst(", $", ""); // oter virgule terminale si besoin
			//throw new SraException("createSubmissionForUpdate", "Vous ne pouvez pas supprimer des locus_tag_prefix " + message);
			ctxVal.addError("valideStudyCreateSubmissionForUpdate", "Vous ne pouvez pas supprimer des locus_tag_prefix " + message);
		}

		// verifier qu'on ne supprime pas une reference bibliographique existante dans le study :
		message = "";
		if (dbStudy.idsPubmed != null && !dbStudy.idsPubmed.isEmpty()) {
			for (String dbId : dbStudy.idsPubmed) {
				if (study.idsPubmed == null || !study.idsPubmed.contains(dbId)) {
					message += dbId + ", ";
				}
			}
		}
		if (StringUtils.isNotBlank(message)) {
			message = message.replaceFirst(", $", ""); // oter virgule terminale si besoin
			//throw new SraException("createSubmissionForUpdate", "Vous ne pouvez pas supprimer des references PUBMED  " + message);
			ctxVal.addError("valideStudyCreateSubmissionForUpdate", "Vous ne pouvez pas supprimer des references PUBMED  " + message);
		}

		// verifier qu'on ne supprime pas et/ou qu'on ne modifie pas la taxonomie du study :
//		if (StringUtils.isNotBlank(dbStudy.taxonId)) {
//			if(StringUtils.isBlank(study.taxonId) || !study.taxonId.equals(dbStudy.taxonId)) {
//				//throw new SraException("createSubmissionForUpdate", "Vous ne pouvez pas supprimer ou modifier le taxonId du study " + study.code);
//				ctxVal.addError("valideStudyCreateSubmissionForUpdate", "Vous ne pouvez pas supprimer ou modifier le taxonId du study " + study.code);
//			}
//		} 
//		// verifier qu'on ne supprime pas et/ou qu'on ne modifie pas le scientificName du study:
//		if (StringUtils.isNotBlank(dbStudy.scientificName)) {
//			if(StringUtils.isBlank(study.scientificName) || !study.scientificName.equals(dbStudy.scientificName)) {
//				//throw new SraException("createSubmissionForUpdate", "Vous ne pouvez pas supprimer ou modifier le scientificName du study " + study.code);
//				ctxVal.addError("valideStudyCreateSubmissionForUpdate", "Vous ne pouvez pas supprimer ou modifier le scientificName du study " + study.code);
//			}
//		}
		
		// synchronisation du taxonId et scientificName et abstract et description
		submissionNewAPITools.synchronyseValuesInStudy(study, ctxVal);
		// verifier date de release dans le cas ou l'utilisateur a reculé la date de release via l'interface :
		Date dbReleaseDate = dbStudy.releaseDate;
		Date userReleaseDate = study.releaseDate;
		DateFormat targetFormat = new SimpleDateFormat("dd/MM/yyyy");
		//String formatted_dbReleaseDate = targetFormat.format(dbReleaseDate);
		//String formatted_releaseDate = targetFormat.format(releaseDate);

		//logger.debug("releaseDate du study passe en arg = " + study.releaseDate);
		//logger.debug ("releaseDate du study dans base = " + dbStudy.releaseDate);
		// la releaseDate recuperée depuis le javascript n'est pas à la bonne valeur dans le java quand on utilise Date. C'est 
		// une histoire de fuseaux horaires et d'heures d'été ou hivers. Du coup, si on doit utiliser cette date, on lui ajoute 4 heures dans calendar
		// pour avoir la meme date que celle que l'utilisateur à entré dans le javascript. 
		// => retrait car pose pb ensuite en comparant dbReleaseDate et userReleaseDate

		if (userReleaseDate != null) {	
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(userReleaseDate);
			//logger.debug("releaseDate du study recupere dans Calendar = " + calendar.getTime());
			//Ne pas ajouter : calendar.add(Calendar.HOUR_OF_DAY, 4);


			//logger.debug("releaseDate du study recupere dans Calendar après ajout de 4 heures = " + calendar.getTime());
			Date goodUserReleaseDate  = calendar.getTime();
			String formatted_dbReleaseDate = targetFormat.format(dbReleaseDate);
			String formatted_goodUserReleaseDate = targetFormat.format(goodUserReleaseDate);
			// impossible de modifier releaseDate si données deja publique :				
			if(!formatted_goodUserReleaseDate.equals(formatted_dbReleaseDate) && dbReleaseDate.before(new Date())) {
				//throw new SraException("createSubmissionForUpdate", "La date de release d'origine " + formatted_dbReleaseDate + " est passee (donnee publique) et ne peut plus etre modifiée");
				ctxVal.addError("valideStudyCreateSubmissionForUpdate", "La date de release d'origine " + formatted_dbReleaseDate + " est passee (donnee publique) et ne peut plus etre modifiée");
			}
			if (!formatted_goodUserReleaseDate.equals(formatted_dbReleaseDate) && goodUserReleaseDate.before(dbReleaseDate)) {
				//throw new SraException("createSubmissionForUpdate", "La date de release d'origine ne peut pas etre avancée. La date de release de l'utilisateur " + formatted_goodUserReleaseDate + " n'est pas après la date de release initiale " + formatted_dbReleaseDate);
				ctxVal.addError("valideStudyCreateSubmissionForUpdate", "La date de release d'origine ne peut pas etre avancée. La date de release de l'utilisateur " + formatted_goodUserReleaseDate + " n'est pas après la date de release initiale " + formatted_dbReleaseDate);
			}
			Calendar calendar_2 = Calendar.getInstance();
			calendar_2.add(Calendar.YEAR, 2);
			Date dans2ans = calendar_2.getTime();
			if (goodUserReleaseDate.after(dans2ans)) {
				//throw new SraException("createSubmissionForUpdate", "La date de release proposée " + formatted_goodUserReleaseDate + " ne doit pas etre dans plus de 2 ans");					
				ctxVal.addError("valideStudyCreateSubmissionForUpdate", "La date de release proposée " + formatted_goodUserReleaseDate + " ne doit pas etre dans plus de 2 ans");					
			}

			// important de sauvegarder la releaseDate de l'utilisateur apres ajout des 4 h
			study.releaseDate = goodUserReleaseDate;
			//logger.debug("releaseDate du study passe en arg apres traitement = " + formatted_goodUserReleaseDate);  
		}


		// sgas user
		//			if (! study.traceInformation.createUser.equals(user)) {		
		//				String message = "l'utilisateur " + user + 
		//						" n'est pas autorisé à mettre à jour le study " + study.code 
		//						+ " crée par " + study.traceInformation.createUser;
		//				throw new SraException("createSubmissionForUpdate", message);
		//			}
		// Modification pour ticket SUPSQ-4248 : on ne veut pas seulement qu'il n'y ait aucun studyCode dans 
		// une soumission de type release mais on veut qu'il n'y ait aucun studyCode 
		// engagé dans une soumission avec un status autre que SUB-F :
		//			if (submissionDAO.checkObjectExist(DBQuery.and(DBQuery.is("studyCode", study.code),
		//					DBQuery.is("type", Submission.Type.UPDATE)))) {
		//		
		//				Iterable<Submission> it = submissionDAO.find(DBQuery.and(DBQuery.is("studyCode", study.code),
		//						DBQuery.is("type", Submission.Type.UPDATE)));
		//				String mess = Iterables.map(it, s->s.code)
		//						.surround("(", ", ", ")")
		//						.asString();
		//				String message = "Il existe deja des soumissions " + mess 
		//						+ " pour la mise à jour du study " + study.code;
		//				logger.debug("dans OOOOOOOOOOOOOO, message = " + message);
		//				throw new SraException("createSubmissionForUpdate", message);
		//			}

		if (submissionDAO.checkObjectExist(DBQuery.and(DBQuery.is("studyCode", study.code),
				DBQuery.notIn("state.code", SRASubmissionStateNames.SUB_F)))) {

			Iterable<Submission> it = submissionDAO.find(DBQuery.and(DBQuery.is("studyCode", study.code),
					DBQuery.notIn("state.code", SRASubmissionStateNames.SUB_F)));
			String mess = Iterables.map(it, s->s.code)
					.surround("(", ", ", ")")
					.asString();
			message = "Il existe deja des soumissions en cours " + mess 
					+ " impliquant le study " + study.code;
			//logger.debug("dans OOOOOOOOOOOOOO, message = " + message);
			//throw new SraException("createSubmissionForUpdate", message);
			ctxVal.addError("valideStudyCreateSubmissionForUpdate", message);
		}	
		for (String projCode : study.projectCodes) {
			if(!projectCodes.contains(projCode)) {
				projectCodes.add(projCode);
			}
		}
		
		logger.debug("End valideStudyCreateSubmissionForUpdate pour study" + new Date());
		if(ctxVal.hasErrors()) {
			SraException.assertNoError(ctxVal);
		}
		return true;
	}
	
	private boolean isValideUmbrellaCreateSubmissionForUpdate(ContextValidation ctxVal, String user, Project umbrella) {
		// NGL-4593
		if(StringUtils.isBlank(umbrella.dropBox)) {
			ctxVal.addError("valideUmbrellaCreateSubmissionForUpdate", "umbrella "+ umbrella.code + " sans dropBox");
		}
		SraCodeHelper.assertObjectStateCode(umbrella, SRASubmissionStateNames.SUB_F, ctxVal);
		//study.setTraceUpdateStamp(user);
		// Mettre dans l'etat qui sera sauvé si tout va bien:
		umbrella.updateStateAndTrace(new State(SRASubmissionStateNames.SUBU_N, user));
		umbrella.validate(ctxVal); 
		//SraException.assertNoError(ctxVal);

		if (umbrella.traceInformation.createUser == null) {
			String message = "Le project umbrella " + umbrella.code
					+ " n'est pas renseigné dans la base pour le champs createUser";	
			//throw new SraException("createSubmissionForUpdate", message);
			ctxVal.addError("valideUmbrellaCreateSubmissionForUpdate", message);
		}
		if (user == null) {
			String message = "l'utilisateur n'est pas authentifié";
			//throw new SraException("createSubmissionForUpdate", message);
			ctxVal.addError("valideUmbrellaCreateSubmissionForUpdate", message);
		}
		// sgas user
		//			if (! umbrella.traceInformation.createUser.equals(user)) {		
		//				String message = "l'utilisateur " + user + 
		//						" n'est pas autorisé à mettre à jour le project umbrella " + umbrella.code 
		//						+ " crée par " + umbrella.traceInformation.createUser;
		//				throw new SraException("createSubmissionForUpdate", message);
		//			}
		
		// synchronisation du taxonId et scientificName et abstract et description
		submissionNewAPITools.synchronyseValuesInUmbrella(umbrella, ctxVal);
		
		if (submissionDAO.checkObjectExist(DBQuery.and(DBQuery.is("umbrellaCode", umbrella.code),
				DBQuery.notIn("state.code", SRASubmissionStateNames.SUB_F)))) {

			Iterable<Submission> it = submissionDAO.find(DBQuery.and(DBQuery.is("umbrellaCode", umbrella.code),
					DBQuery.notIn("state.code", SRASubmissionStateNames.SUB_F)));
			String mess = Iterables.map(it, s->s.code)
					.surround("(", ", ", ")")
					.asString();
			String message = "Il existe deja des soumissions en cours " + mess 
					+ " impliquant le project umbrella " + umbrella.code;
			//logger.debug("dans OOOOOOOOOOOOOO, message = " + message);
			//throw new SraException("createSubmissionForUpdate", message);
			ctxVal.addError("valideUmbrellaCreateSubmissionForUpdate", message);
		}	
		// verifier que le project umbrella de l'utilisateur ne supprime pas des project enfants 
		Project dbUmbrella = projectAPI.get(umbrella.code);
		String message = "";
		if (dbUmbrella.childrenProjectAccessions != null && !dbUmbrella.childrenProjectAccessions.isEmpty()) {
			for (String dbChildren : dbUmbrella.childrenProjectAccessions) {
				if (umbrella.childrenProjectAccessions == null || !umbrella.childrenProjectAccessions.contains(dbChildren)) {
					message += dbChildren + ", ";
				}
			}
		}
		if (StringUtils.isNotBlank(message)) {
			message = message.replaceFirst(", $", ""); // oter virgule terminale si besoin
			//throw new SraException("createSubmissionForUpdate", "Vous ne pouvez pas supprimer des projects enfants  " + message);
			ctxVal.addError("valideUmbrellaCreateSubmissionForUpdate", "Vous ne pouvez pas supprimer des projects enfants : " + message);
		}
		// NGL-4013 
		// verifier que le project umbrella de l'utilisateur ne supprime pas ou ne modifie pas le project parent :
		message = "";
		if (dbUmbrella.parentProjectAccessions != null && !dbUmbrella.parentProjectAccessions.isEmpty()) {
			for (String dbParent : dbUmbrella.parentProjectAccessions) {
				if (umbrella.parentProjectAccessions == null || !umbrella.parentProjectAccessions.contains(dbParent)) {
					message += dbParent + ", ";
				}
			}
		}
		if (StringUtils.isNotBlank(message)) {
			message = message.replaceFirst(", $", ""); // oter virgule terminale si besoin
			//throw new SraException("createSubmissionForUpdate", "Vous ne pouvez pas supprimer des projects parents  " + message);
			ctxVal.addError("valideUmbrellaCreateSubmissionForUpdate", "Vous ne pouvez pas supprimer des projects parents : " + message);
		}
		
		message = "";
		if (dbUmbrella.idsPubmed != null && !dbUmbrella.idsPubmed.isEmpty()) {
			for (String dbId : dbUmbrella.idsPubmed) {
				if (umbrella.idsPubmed == null || !umbrella.idsPubmed.contains(dbId)) {
					message += dbId + ", ";
				}
			}
		}
		if (StringUtils.isNotBlank(message)) {
			message = message.replaceFirst(", $", ""); // oter virgule terminale si besoin
			//throw new SraException("createSubmissionForUpdate", "Vous ne pouvez pas supprimer des references PUBMED  " + message);
			ctxVal.addError("valideUmbrellaCreateSubmissionForUpdate", "Vous ne pouvez pas supprimer des references PUBMED : " + message);
		}


		// verifier que les projects enfant d'un project umbrella ne sont pas deja declarés dans un autre project umbrella :
		message = "";
		if (umbrella.childrenProjectAccessions != null && !umbrella.childrenProjectAccessions.isEmpty()) {
			for (String projectAC : umbrella.childrenProjectAccessions) {
				List <Project> umbrellaList = projectAPI.dao_find(DBQuery.in("childrenProjectAccessions", projectAC)).toList();
				if (umbrellaList.size() > 0) {
					String header = "projectAC =" + projectAC + "\n";

					for (Project p: umbrellaList) {
						if(! p.code.equals(umbrella.code)) {
							message += "       - deja utilise par le project umbrella  " + p.code + "\n";
						}
					}
					if(StringUtils.isNotBlank(message)) {
						//throw new SraException(header + message);
						ctxVal.addError("valideUmbrellaCreateSubmissionForUpdate", header + message);
					}
				}	 
			}
		}
		if(ctxVal.hasErrors()) {
			logger.debug("dans isValidUbrellaCreateSubmissionForUpdate, j'ai un context avec erreur :");
			ctxVal.displayErrors(logger, "debug");
			logger.debug("fin displayError, j'envoie SraException.assertNoError");
			SraException.assertNoError(ctxVal);
		}
		return true;
	}

	
	private boolean isValideSamplesCreateSubmissionForUpdate(ContextValidation ctxVal, String user, List<Sample> samples, List<String> projectCodes) {
		String dropBoxSample = "";
		for (Sample sample : samples) {
			// NGL-4593
			if (StringUtils.isBlank(sample.dropBox)) {
				ctxVal.addError("valideSamplesCreateSubmissionForUpdate", "sample " + sample.code + " sans dropBox");
			} else {
				if(StringUtils.isBlank(dropBoxSample)) {
					dropBoxSample = sample.dropBox;
				}
				if( ! dropBoxSample.equals(sample.dropBox)) {	
					ctxVal.addError("valideSamplesCreateSubmissionForUpdate", "sample "+ sample.code + " avec dropBox=" + sample.dropBox + " != " + dropBoxSample );
				}
			}
			SraCodeHelper.assertObjectStateCode(sample, SRASubmissionStateNames.SUB_F, ctxVal);
			// Mettre dans l'etat qui sera sauvé si tout va bien:
			sample.updateStateAndTrace(new State(SRASubmissionStateNames.SUBU_N, user));
			sample.validate(ctxVal);
			//SraException.assertNoError(ctxVal);// ok et bien recuperé 
			// sgas user
			//			if (! sample.traceInformation.createUser.equals(user)) {		
			//				String message = "l'utilisateur " + user + 
			//						" n'est pas autorisé à mettre à jour le sample " + sample.code 
			//						+ " crée par " + sample.traceInformation.createUser;
			//				throw new SraException("createSubmissionForUpdate", message);
			//			}
			if (submissionDAO.checkObjectExist(DBQuery.and(DBQuery.in("sampleCodes", sample.code),
					DBQuery.notIn("state.code", SRASubmissionStateNames.SUB_F)))) {

				Iterable<Submission> it = submissionDAO.find(DBQuery.and(DBQuery.in("sampleCodes", sample.code),
						DBQuery.notIn("state.code", SRASubmissionStateNames.SUB_F)));
				String mess = Iterables.map(it, s->s.code)
						.surround("(", ", ", ")")
						.asString();
				String message = "Il existe deja des soumissions en cours " + mess 
						+ " impliquant le sample " + sample.code;
				//throw new SraException("createSubmissionForUpdate", message);
				ctxVal.addError("valideSamplesCreateSubmissionForUpdate", message);
			}
			if(!projectCodes.contains(sample.projectCode)) {
				projectCodes.add(sample.projectCode);
			}
		}
		if(ctxVal.hasErrors()) {
			SraException.assertNoError(ctxVal);
		}
		return true;
	}
	
	
	private boolean isValideExperimentsCreateSubmissionForUpdate(ContextValidation ctxVal, String user, List<Experiment> experiments, List<String> projectCodes) {
		String dropBoxExperiment = "";
		for (Experiment experiment : experiments) {
			// NGL-4593
			if (StringUtils.isBlank(experiment.dropBox)) {
				ctxVal.addError("valideExperimentsCreateSubmissionForUpdate", "experiment " + experiment.code + " sans dropBox");
			} else {
				if(StringUtils.isBlank(dropBoxExperiment)) {
					dropBoxExperiment = experiment.dropBox;
				}
				if( ! dropBoxExperiment.equals(experiment.dropBox)) {	
					ctxVal.addError("valideExperimentsCreateSubmissionForUpdate", "experiment "+ experiment.code + " avec dropBox=" + experiment.dropBox + " != " + dropBoxExperiment );
				}
			}
			SraCodeHelper.assertObjectStateCode(experiment, SRASubmissionStateNames.SUB_F, ctxVal);

			// Mettre dans l'etat qui sera sauvé si tout va bien:
			experiment.updateStateAndTrace(new State(SRASubmissionStateNames.SUBU_N, user));
			
			experiment.validate(ctxVal);

			//ctxVal.displayErrors(logger, "debug");

			//SraException.assertNoError(ctxVal);// ok et bien recuperé 
			// sgas user
			//			if (! experiment.traceInformation.createUser.equals(user)) {		
			//				String message = "l'utilisateur " + user + 
			//						" n'est pas autorisé à mettre à jour l'experiment " + experiment.code 
			//						+ " crée par " + experiment.traceInformation.createUser;
			//				throw new SraException("createSubmissionForUpdate", message);
			//			}
			if (submissionDAO.checkObjectExist(DBQuery.and(DBQuery.in("experimentCodes", experiment.code),
					DBQuery.notIn("state.code", SRASubmissionStateNames.SUB_F)))) {
				Iterable<Submission> it = submissionDAO.find(DBQuery.and(DBQuery.in("experimentCodes", experiment.code),
						DBQuery.notIn("state.code", SRASubmissionStateNames.SUB_F)));
				String mess = Iterables.map(it, s->s.code)
						.surround("(", ", ", ")")
						.asString();
				String message = "Il existe deja des soumissions en cours " + mess 
						+ " impliquant l'experiment " + experiment.code;
				//throw new SraException("createSubmissionForUpdate", message);
				ctxVal.addError("valideExperimentsCreateSubmissionForUpdate", message);
			}
			if(!projectCodes.contains(experiment.projectCode)) {
				projectCodes.add(experiment.projectCode);
			}	
		}
		if(ctxVal.hasErrors()) {
			SraException.assertNoError(ctxVal);
		}
		return true;
	}
	
	
	/**
	 * Construit la soumission correspondant au study, samples ou experiments presents dans la base
	 * et qui doivent etre soumis à l'EBI pour une mise à jour. 
	 * Renvoie la soumission dans l'etat {@link ngl.refactoring.state.States#SUBU_N}.
	 * Ajoute une erreur dans le contexte de validation si une erreur survient ou declenche une erreur
	 * @param  contextValidation  contexte de validation
	 * @param umbrella            objet Project umbrella à mettre à jour
	 * @param study               objet study à mettre à jour à l'EBI
	 * @param samples             liste des samples à mettre à jour à l'EBI
	 * @param experiments         liste des experiments à mettre à jour à l'EBI
	 * @return                    soumission nouvellement cree, et sauvée dans base.
	 * @throws SraException       error
	 * @throws APIException 	  error
	 * @throws APIValidationException error
	 */
	public Submission createSubmissionForUpdate(ContextValidation contextValidation, 
												Project umbrella,
												Study study,
												List<Sample> samples,
												List<Experiment> experiments,
												boolean noCheckTaxonSampleCollab) throws SraException, APIValidationException, APIException {
		// La soumission n'existe pas à ce stade donc pas de validation de state à faire
		Submission submission = null;
		//Date courantDate = new java.util.Date();
		String user = contextValidation.getUser();	
		//logger.debug("Entree dans valideArgsCreateSubmissionForUpdate " + new Date());
		if( study != null ) {
			submissionNewAPITools.synchronyseValuesInStudy(study, contextValidation);
		}
		List <String> projectCodes = valideArgsCreateSubmissionForUpdate(contextValidation, umbrella, study, samples, experiments);
		SraException.assertNoError(contextValidation);
		contextValidation.setCreationMode();

		submission = getBasicSubmissionForUpdate(projectCodes, umbrella, contextValidation);

		// hashMap qui seront remplis dans le cas d'un update de study ou d'exp 
		HashMap<String, Experiment>   hExperimentToUpdateForReleaseDate = new HashMap<String, Experiment>();
		HashMap<String, Analysis>     hAnalysisToUpdateForReleaseDate = new HashMap<String, Analysis>();
		HashMap<String, Sample>       hSampleToUpdateForReleaseDate = new HashMap<String, Sample>();
		
		
		// hashMap qui seront remplis dans le cas d'un update d'exp
		HashMap<String, ExternalSample> externalSample_to_save = new HashMap<String, ExternalSample>();
		HashMap<String, ExternalStudy> externalStudy_to_save = new HashMap<String, ExternalStudy>();
		//HashMap<String, Sample>   listSampleToUpdateForReleaseDate = new HashMap<String, Sample>();
		
		
		if (umbrella != null && StringUtils.isNotBlank(umbrella.code)) {
			// mettre à jour l'objet submission pour le cas d'une mise à jour du project umbrella
			submission.umbrellaCode = umbrella.code;	// si update d'un project (PRJEB)
			//NGL-4593
			submission.dropBox = umbrella.dropBox;
		} else if(samples != null && samples.size()>0) {
			//NGL-4593
			submission.dropBox = samples.get(0).dropBox;
			// si samples modifiés pour date de release, aucun impact sur les autres objets.
			// il faut juste verifier qu'on ne cherche pas à mettre un sample public en privé et mettre à jour object submission
			updateObjectsCaseUpdateSamples(submission, samples, contextValidation);
		} else if (study != null && StringUtils.isNotBlank(study.code)) {	
			//logger.debug("PPPPPPPPPPP        entree dans updateObjectsCaseUpdateStudy " + new Date());
			//NGL-4593
			submission.dropBox = study.dropBox;
			// mettre à jour les experiments et samples associés pour releaseDate
			// mettre à jour l'objet submission pour le cas d'une mise à jour de study 			
			submission.studyCode = study.code;	
			submission.refStudyCodes.add(study.code);
			updateObjectsCaseUpdateStudy(submission, 
										 study, 
										 hAnalysisToUpdateForReleaseDate, 
										 hExperimentToUpdateForReleaseDate, 
										 hSampleToUpdateForReleaseDate, 
										 contextValidation);
			//logger.debug("PPPPPPPPPPP         sortie de updateObjectsCaseUpdateStudy " + new Date());

		// mettre à jour l'objet submission pour le cas d'une mise à jour des experiments
		} else if (experiments != null && experiments.size() > 0) {
			// mettre à jour les experiments et samples associés pour releaseDate, creer les ExternalStudy et ExternalSample et mettre à jour submission
			//logger.debug("PPPPPPPPPPP         entree dans updateObjectsCaseUpdateExperiments " + new Date());
			
			//logger.debug("dans createSubmissionForUpdate, noCheckTaxon='{}'", noCheckTaxonSampleCollab);
			// NGL-5313
			submissionNewAPITools.checkVisibiliteSamplesAndStudy(experiments, contextValidation);
			// NGL-4952 :
			if( ! noCheckTaxonSampleCollab) {
				//logger.debug("je fais le controle de taxonCode");
				submissionNewAPITools.checkCoherenceTaxonInSampleAndReadset(experiments, contextValidation);
			}
			//NGL-4593
			submission.dropBox = experiments.get(0).dropBox;
			updateObjectsCaseUpdateExperiments(submission, 
					    					   experiments,
					    					   hExperimentToUpdateForReleaseDate,
					    	                   hSampleToUpdateForReleaseDate,
					    	                   externalSample_to_save,
					    	                   externalStudy_to_save,
					    	                   user,
					                           contextValidation); 	
			//logger.debug("PPPPPPPPPPP        sortie de updateObjectsCaseUpdateExperiments " + new Date());

			//logger.debug("BBBBBBBBBBBBBBBBBBBBBBBBBB Sortie de updateObjectsCaseUpdateExperiments et taille hSampleToUpdateForReleaseDate = " + hSampleToUpdateForReleaseDate.entrySet().size());
//			for (Entry<String, Sample> setSample : hSampleToUpdateForReleaseDate.entrySet()) {
//				if(setSample.getValue().releaseDate!= null) {
//					logger.debug("dans createSubmissionForUpdate apres updateObjectsCaseUpdateExperiments KKKKKKKKKKKKK         pour le sample " + setSample.getValue().code + ", releaseDate = " + setSample.getValue().releaseDate);
//				} else {
//					logger.debug("dans createSubmissionForUpdate  apres updateObjectsCaseUpdateExperiments KKKKKKKKKKKKK         pour le sample " + setSample.getValue().code + ", releaseDate = null");
//				}
//			}	
		} else {
			contextValidation.addError("createSubmissionForUpdate", " appel de la methode sans umbrella, study, samples ou experiments ? ");
		}
	
		// inutile, sinon begaiement sur historique
		//submission.updateStateAndTrace(new State(SRASubmissionStateNames.SUBU_N, contextValidation.getUser()));
		// On verifie que l'objet qu'on vient de creer est bien ok :
		contextValidation.setCreationMode();// On crée une soumission pour update
		//logger.debug("UpdateServices.createSubmissionForUpdate:: submissionCode = " + submission.code);
		submission.validate(contextValidation);

		//logger.debug("UpdateServices.createSubmissionForUpdate:: apres validate de la soumission = ");
		SraException.assertNoError(contextValidation);
		
		// sauvegarde de tous les objets dans la base :
		contextValidation.setCreationMode();
		Submission dbSubmission = submissionDAO.save(submission);
		for (Entry<String, ExternalSample> setExternalSample : externalSample_to_save.entrySet()) {
			//logger.debug("101010101010101010101010101010  sauvegarde dans base du sample "+ setExternalSample.getValue().code);
			externalSampleAPI.dao_saveObject(setExternalSample.getValue());
		}
		for (Entry<String, ExternalStudy> setExternalStudy : externalStudy_to_save.entrySet()) {
			//logger.debug("555555555555555555555555555  sauvegarde dans base du study "+ setExternalStudy.getValue().code);
			externalStudyAPI.dao_saveObject(setExternalStudy.getValue());
		}
		
		// updater dans la base project, study, samples et experiments 
		//qui sont deja validés et avec bon etat  et bon state après valideArgsCreateSubmissionForUpdate:

		contextValidation.setUpdateMode();	
		if (umbrella != null) {
			//logger.debug("WWWWWWWWWWWWWWWWWWWW  sauvegarde de l'umbrella "+ umbrella.code);
			projectAPI.update(umbrella, user);
		}
		if (study != null) {
			// sauvegarde du study avec potentiellement modification de sa date de release => il faut mettre à jour les experiments et analyses qui en dependent à jour.
			//logger.debug("begin 666666666666666 update study");
			studyAPI.update(study, user);
			//logger.debug("end 666666666666666 update study");
		} 
		
		// experiments de la soumission (recentes qui peuvent etre sauvés avec validation => API plutot que DAO
		for (Experiment experiment : experiments) {
			//logger.debug("ZZZZZZZZZZZZZZZZZZZZZZZ  sauvegarde de l'experiment "+ experiment.code);
			experimentAPI.update(experiment, user); // utiliser API plutot que DAO

			// recuperer le readset de la collection ngl-sub
			if(! readsetAPI.dao_checkObjectExist("code", experiment.readSetCode)) {
				throw new SraException("le readset " + experiment.readSetCode + " n'existe pas dans la base de données");
			}
			Readset readset = readsetAPI.get(experiment.readSetCode);
			// updater le readset dans la collection sra_readset de ngl-sub :
			AbstractStudy abstStudy = abstractStudyAPI.get(experiment.studyCode);
			AbstractSample abstSample = abstractSampleAPI.get(experiment.sampleCode);
			readsetAPI.dao_update(DBQuery.is("code", readset.code), 
					DBUpdate.set("state", experiment.state)
					.set("traceInformation", experiment.traceInformation)
					.set("firstSubmissionDate", experiment.firstSubmissionDate)
					.set("consolidatedPublicStatus", experiment.consolidatedPublicStatus)
					.set("releaseDate", experiment.releaseDate)
					.set("experimentCode", experiment.code)
					.set("experimentAccession", experiment.accession)
					.set("runCode", experiment.run.code)
					.set("runAccession", experiment.run.accession)
					.set("studyCode", abstStudy.code)
					.set("studyAccession", abstStudy.accession)
					.set("studyExternalId", abstStudy.externalId)
					.set("sampleCode", abstSample.code)
					.set("sampleAccession", abstSample.accession)
					.set("sampleExternalId", abstSample.externalId));
		}
		// NGL-4554
		// sauvegarde des experiments pour releaseDate avec optimisation des updates 
		//dans le cas ou meme releaseDate et consolidatedPublicStatus pour chaque exp:
		if (study != null && StringUtils.isNotBlank(study.code)) { 
			//logger.debug("QQQQQQQQQQQQQQQQQQQQQQQQQ                Entree dans cas ou study!=null et study.code renseigne");
			String modifyUser = user;
			Date modifyDate = new Date();
			Date releaseDate = study.releaseDate;
			boolean consolidatedPublicStatus = study.consolidatedPublicStatus;
			int countExp = 0;
			int maxIn = 250;
			List<String> expAccessions = new ArrayList<String>();
			for (Entry<String, Experiment> setExperiment : hExperimentToUpdateForReleaseDate.entrySet()) {
				//logger.debug("MMMMMMMMMMMMMMMMMMMMMMM sauvegarde de l'experiment "+ setExperiment.getValue().code);
				Experiment experiment = setExperiment.getValue();
				countExp++;
				if(experiment.accession != null && StringUtils.isNotBlank(experiment.accession)) {
					expAccessions.add(experiment.accession);
				} 
				if(countExp > maxIn) {
					experimentDAO.update(DBQuery.in("accession", expAccessions),
							DBUpdate
							.set("consolidatedPublicStatus",  consolidatedPublicStatus)
							.set("releaseDate", releaseDate)			
							.set("traceInformation.modifyUser", modifyUser)
							.set("traceInformation.modifyDate", modifyDate));
					//logger.debug("IIIIIIIIIIIIIIIIIIIIIIIIII              sauvegarde de " + expAccessions.size() + " experiments " + new Date());
					expAccessions = new ArrayList<String>();
					countExp = 0;
					// Repercuter releaseDate sur collection readset de ngl-sub
					readsetAPI.dao_update(DBQuery.in("experimentAccession", expAccessions), 
							DBUpdate
							.set("consolidatedPublicStatus",  consolidatedPublicStatus)
							.set("releaseDate", releaseDate)			
							.set("traceInformation.modifyUser", modifyUser)
							.set("traceInformation.modifyDate", modifyDate));
					//logger.debug("IIIIIIIIIIIIIIIIIIIIIIIIII              sauvegarde de " + expAccessions.size() + " readsets " + new Date());
				}
			}
			// correction effet de bord :
			if(expAccessions.size() > 0) {
				experimentDAO.update(DBQuery.in("accession", expAccessions),
						DBUpdate
						.set("consolidatedPublicStatus",  consolidatedPublicStatus)
						.set("releaseDate", releaseDate)			
						.set("traceInformation.modifyUser", modifyUser)
						.set("traceInformation.modifyDate", modifyDate));
				//logger.debug("IIIIIIIIIIIIIIIIIIIIIIIIII     correction effet de bord          sauvegarde de " + expAccessions.size() + " experiments " + new Date());

				// Repercuter releaseDate sur collection readset de ngl-sub
				readsetAPI.dao_update(DBQuery.in("experimentAccession", expAccessions), 
						DBUpdate
						.set("consolidatedPublicStatus",  consolidatedPublicStatus)
						.set("releaseDate", releaseDate)			
						.set("traceInformation.modifyUser", modifyUser)
						.set("traceInformation.modifyDate", modifyDate));
				//logger.debug("IIIIIIIIIIIIIIIIIIIIIIIIII      correction effet de bord         sauvegarde de " + expAccessions.size() + " readsets " + new Date());

				//logger.debug("IIIIIIIIIIIIIIIIIIIIIIIIII     correction effet de bord         sauvegarde de " + expAccessions.size() + " experiments et readsets " + new Date());
			}
		} else {
			//logger.debug("QQQQQQQQQQQQQQQQQQQQQQ                Entree dans sauvegarde des exp cas ou studyCode=null");
			// sauvegarde des experiments pour releaseDate avec releaseDate qui peut etre differente pour chaque exp 
			// => pas d'optimisation des updates :
			for (Entry<String, Experiment> setExperiment : hExperimentToUpdateForReleaseDate.entrySet()) {
				//logger.debug("YYYYYYYYYYYYYYYYYYY  sauvegarde de l'experiment "+ setExperiment.getValue().code);
				Experiment experiment = setExperiment.getValue();
				//experimentDAO.update(setExperiment.getValue()); // mettre DAO pour sauver anciens exp juste pour releaseDate
				if(experiment.accession != null && StringUtils.isNotBlank(experiment.accession)) {
					experimentDAO.update(DBQuery.in("accession", experiment.accession),
							DBUpdate
							.set("consolidatedPublicStatus",  experiment.consolidatedPublicStatus)
							.set("releaseDate", experiment.releaseDate)			
							.set("traceInformation.modifyUser", user)
							.set("traceInformation.modifyDate", new Date()));
					
					readsetAPI.dao_update(DBQuery.is("experimentAccession", experiment.accession), 
							DBUpdate
							.set("consolidatedPublicStatus",  experiment.consolidatedPublicStatus)
							.set("releaseDate", experiment.releaseDate)			
							.set("traceInformation.modifyUser", experiment.traceInformation.modifyUser)
							.set("traceInformation.modifyDate", experiment.traceInformation.modifyDate));
				} 
			}
		}

		
		for (Entry<String, Analysis> setAnalysis : hAnalysisToUpdateForReleaseDate.entrySet()) {
			//logger.debug("YYYYYYYYYYYYYYYYYYY  sauvegarde de l'analyse "+ setAnalysis.getValue().code);
			analysisAPI.update(setAnalysis.getValue(), user);
		}	
		
		// sauvegarde des samples recemment updates pour l'EBI avec validation => API plutot que DAO
		for (Sample sample : samples) {
			//logger.debug("YYYYYYYYYYYYYYYYYYY  sauvegarde du sample "+ sample.code);
			sampleAPI.update(sample, user);
		}
//		for (Entry<String, Sample> setSample : hSampleToUpdateForReleaseDate.entrySet()) {
//			if(setSample.getValue().releaseDate!= null) {
//				logger.debug("dans createSubmissionForUpdate final KKKKKKKKKKKKK         pour le sample " + setSample.getValue().code + ", releaseDate = " + setSample.getValue().releaseDate);
//			} else {
//				logger.debug("dans createSubmissionForUpdate  final KKKKKKKKKKKKK         pour le sample " + setSample.getValue().code + ", releaseDate = null");
//			}
//		}	
		// sauvegarde d'anciens samples juste pour releaseDate => utiliser DAO plutot que API car anciens samples ne passent pas tous validation
		for (Entry<String, Sample> setSample : hSampleToUpdateForReleaseDate.entrySet()) {
			//logger.debug("YYYYYYYYYYYYYYYYYYY  sauvegarde du sample "+ setSample.getValue().code);
			sampleDAO.update(setSample.getValue());
		}	
		//logger.debug("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF  Sortie de UpdateServices.createSubmissionForUpdate " + new Date());

		return dbSubmission;
	} // end create

	
	
	// renvoie l'objet soumission "basic" (incomplet) de type update
	private Submission getBasicSubmissionForUpdate(List <String> projectCodes, Project umbrella,  ContextValidation contextValidation) {
		Submission submission = new Submission(contextValidation.getUser(), projectCodes);
		if(umbrella != null) {
			submission.code = sraCodeHelper.generateSubmissionCodeForUmbrella();
		} else {
			submission.code = sraCodeHelper.generateSubmissionCode(projectCodes);
		}
		Date courantDate = new java.util.Date();

		submission.traceInformation.creationDate = courantDate;
		submission.type = Submission.Type.UPDATE;
		submission.state = new State(SRASubmissionStateNames.SUBU_N, contextValidation.getUser());
		submission.submissionDirectory = VariableSRA.submissionRootDirectory + "/" + submission.code + "_update"; 	
		submission.ebiResult = "ebi_update_" + submission.code + ".txt";
		submission.typeRawDataSubmitted = TypeRawDataSubmitted.withoutRawData;
		//important pour la suite : ??????
		submission.setTraceUpdateStamp(contextValidation.getUser());
		return submission;
	}
	
	
	
	private void updateObjectsCaseUpdateStudy(Submission                    submission, 
											  Study                         study, 
											  HashMap<String, Analysis>     hAnalyzesToUpdateForReleaseDate,
											  HashMap<String, Experiment>   hExperimentToUpdateForReleaseDate,
											  HashMap<String, Sample>       hSampleToUpdateForReleaseDate,
											  ContextValidation             contextValidation) {
		
		if(study == null) {
			contextValidation.addError("updateObjectsCaseUpdateStudy", "argument study null");
			return;
		} 
		
		if(StringUtils.isBlank(study.accession)) { 
			contextValidation.addError("updateObjectsCaseUpdateStudy ", " study "+ study.code + " sans numeros d'accession");	
			return;
		}
		
		if( ! studyAPI.dao_checkObjectExist("code", study.code)) {
			contextValidation.addError("updateObjectsCaseUpdateStudy ", " study "+ study.code + " qui n'existe pas dans base");
			return;
		}
			
		Study oldStudy = studyAPI.get(study.code);
		if(submissionNewAPITools.isPublicAbstractStudy((AbstractStudy)oldStudy) && ! submissionNewAPITools.isPublicAbstractStudy((AbstractStudy)study)) {
			contextValidation.addError("updateObjectsCaseUpdateStudy ", " tentative de mettre le study "+ study.code + " prive alors qu'il est public en base");
			return;
		}
		// synchronise les valeurs de certains champs du study (taxonId et scientificName et studyAbstract et description)
		submissionNewAPITools.synchronyseValuesInStudy(study, contextValidation);
		if (oldStudy.releaseDate != null && ! oldStudy.releaseDate.equals(study.releaseDate)) {
			List <Analysis> analysisList = new ArrayList<Analysis>();
			List <Experiment> expList    = new ArrayList<Experiment>();
			String studyAccession = "NONE";
			String studyCode = "NONE";
		
			if(StringUtils.isNotBlank(study.accession)) {
				studyAccession = study.accession;
			}
			if (StringUtils.isNotBlank(study.code)) {
				studyCode = study.code;
			}

			analysisList = MongoDBDAO.find( InstanceConstants.SRA_ANALYSIS_COLL_NAME, Analysis.class, 
					   		DBQuery.or
					   			(DBQuery.in("studyAccession", studyAccession), 
					   			 DBQuery.in("studyCode", studyCode))).toList();

			expList = MongoDBDAO.find( InstanceConstants.SRA_EXPERIMENT_COLL_NAME, Experiment.class, 
							DBQuery.or
								(DBQuery.in("studyAccession", studyAccession), 
								DBQuery.in("studyCode", studyCode))).toList();

			for(Analysis analysis : analysisList) {
				submissionNewAPITools.updateAnalysisForReleaseDate((AbstractStudy) study, analysis, hAnalyzesToUpdateForReleaseDate, contextValidation);	
			}
			for(Experiment experiment : expList) {
				submissionNewAPITools.updateExperimentForReleaseDate((AbstractStudy) study, experiment, hExperimentToUpdateForReleaseDate, contextValidation);	
			}
			hSampleToUpdateForReleaseDate.putAll(submissionNewAPITools.updateSampleForReleaseDate(hExperimentToUpdateForReleaseDate, hAnalyzesToUpdateForReleaseDate, contextValidation, false));

		}
		
		// mettre à jour l'object submission :
		submission.studyCode = study.code;
		if(! submission.refStudyCodes.contains(study.code)) {
			submission.refStudyCodes.add(study.code);
		}
		
	} // end updateObjectsCaseUpdateStudy

	
	private void updateObjectsCaseUpdateSamples(Submission submission, List<Sample> samples, ContextValidation contextValidation) {
		for (Sample sample : samples) {
			AbstractSample abstSample = sample;
			AbstractSample oldAbstSample = abstractSampleAPI.get(sample.code);
			if(submissionNewAPITools.isPublicAbstractSample(oldAbstSample) && ! submissionNewAPITools.isPublicAbstractSample(abstSample)) { 
				contextValidation.addError("updateObjectsCaseUpdateSamples "," Tentative de mettre en private le sample public " + sample.code);
			}
			// Si update des samples, uniquement pas de calcul des releaseDate, c'est celle des samples passés en argument et pas d'impact sur les autres objets
			// Aucune possibilité de modifier la date de release du sample via la vue updateSamples car pas de releaseDate sur sample si aucun experiment ne pointe dessus
			if(! submission.sampleCodes.contains(sample.code)) {
				submission.sampleCodes.add(sample.code);
			}
			if(! submission.sampleCodes.contains(sample.code)) {
				submission.refSampleCodes.add(sample.code);	
			}
		}
	}
	
	
	private void updateObjectsCaseUpdateExperiments(Submission                      submission, 
													List<Experiment>                experiments,
													HashMap<String, Experiment>     hExperimentToUpdateForReleaseDate,
													HashMap<String, Sample>         hSampleToUpdateForReleaseDate,
													HashMap<String, ExternalSample> hExternalSample_to_save,
													HashMap<String, ExternalStudy>  hExternalStudy_to_save,
													String                          user,
													ContextValidation               contextValidation) {
		
		// le champs experiment.sampleIdentifier est modifiable dans les interfaces js => si un utilisateur a modifie ce champs, il faut mettre en coherence
		// le champs sampleCode qui lui n'est pas modifiable dans les interfaces.
		// si modification par user du champs experiment.studyIdentifier, il faut mettre en coherence le champs experiment.studyCode
		// Si un exp a ete modifié par utilisateur pour le faire pointer vers un study ou un sample qui n'existe pas dans la base, alors, construction de cet externalSample.
	    // ou de cet externalStudy.

		Logger.debug("avant le for");

		for (Experiment experiment : experiments) {	
			boolean study_in_base = false;
			AbstractStudy abstStudy = null;
			// On met a jour le studyCode en coherence avec le studyIdentifier modifié par utilisateur sans modifier studyIdentifier de l'utilisateur
			if(experiment.studyIdentifier.matches(patternStudyAc)) {
				// on est oblige de checkque existance objet car sinon exception sur dao_findOne
				if (abstractStudyAPI.dao_checkObjectExist("accession", experiment.studyIdentifier)) {
					abstStudy = abstractStudyAPI.dao_findOne(DBQuery.is("accession", experiment.studyIdentifier));
					study_in_base = true;
					experiment.studyCode = abstStudy.code;
					// laisser le studyIdentifier de l'exp tel quel.
					//experiment.studyIdentifier = abstStudy.accession;
				}		
			// On met a jour le studyCode en coherence avec le studyIdentifier modifié par utilisateur sans modifier studyIdentifier de l'utilisateur
			} else if(experiment.studyIdentifier.startsWith(patternStudyExternalId)) {
				// cas ou le user a mis dans exp.studyAccession un PRJ
				if (abstractStudyAPI.dao_checkObjectExist("externalId", experiment.studyIdentifier)) {
					abstStudy = abstractStudyAPI.dao_findOne(DBQuery.is("externalId", experiment.studyIdentifier));
					study_in_base = true;
					experiment.studyCode = abstStudy.code;
					// laisser le studyIdentifier de l'exp tel quel.
					//experiment.studyIdentifier = abstStudy.accession;
				}
			} else {
				//throw new SraException("pattern de l'identifiant du study "  + experiment.studyIdentifier +" , dans l'experiment " + experiment.code + " non reconnu");
				contextValidation.addError("updateObjectsCaseUpdateExperiments ", "pattern de l'identifiant du study "  + experiment.studyIdentifier +" , dans l'experiment " + experiment.code + " non reconnu" );
				continue;
			}
			// On met a jour le studyCode en coherence avec le studyIdentifier modifié par utilisateur sans modifier studyIdentifier de l'utilisateur
			if(! study_in_base) {
				//logger.debug("3333333333333333333333333333333333333333    demande de creation du study avec study " + experiment.studyIdentifier);
				ExternalStudy externalStudy = createServices.buildExternalStudy(experiment.studyIdentifier, user);
				abstStudy = (AbstractStudy) externalStudy;
				experiment.studyCode = abstStudy.code;
				// laisser le studyIdentifier de l'exp tel quel.
				//experiment.studyIdentifier = abstStudy.accession;
				if( ! hExternalStudy_to_save.containsKey(externalStudy.code)) {
					hExternalStudy_to_save.put(externalStudy.code, externalStudy);
				}
			}
		
			boolean sample_in_base = false;
			AbstractSample abstSample = null;
			// dans le cas ou user a mis ERS dans experiment.sampleAccession (cas classique)
			if(experiment.sampleIdentifier.matches(patternSampleAc)) {
				// on est oblige de checkque existance objet car sinon exception sur dao_findOne
				if (abstractSampleAPI.dao_checkObjectExist("accession", experiment.sampleIdentifier)) {
					abstSample = abstractSampleAPI.dao_findOne(DBQuery.is("accession", experiment.sampleIdentifier));
					//logger.debug("66666666666666666666666666666666         sample avec "+ experiment.sampleIdentifier + " bien trouve dans base");
					sample_in_base = true;
					//logger.debug("ZZZZZZZZZZZZZZ   dans expUser avant correction experiment.code="+experiment.code+", experiment.sampleIdentifier="+ experiment.sampleIdentifier+ ", experiment.sampleCode="+ experiment.sampleCode);
					experiment.sampleCode = abstSample.code;
					// laisser le sampleIdentifier de l'exp tel quel.
					//experiment.sampleIdentifier = abstSample.accession;
					//Experiment oldExpInBase = experimentAPI.get(experiment.code);
					//logger.debug("ZZZZZZZZZZZZZZ   dans expUser apres correction (hors base) experiment.code="+experiment.code+", experiment.sampleIdentifier="+ experiment.sampleIdentifier+ ", experiment.sampleCode="+ experiment.sampleCode);
					//logger.debug("ZZZZZZZZZZZZZZ   dans base oldExpInBase.code="+oldExpInBase.code+", oldExpInBase.sampleIdentifier="+ oldExpInBase.sampleIdentifier+ ", oldExpInBase.sampleCode="+ oldExpInBase.sampleCode);
					
				}
				// Dans le cas ou l'utilisateur a mis SAME dans experiment.sampleAccession (cas France)
			} else if(experiment.sampleIdentifier.startsWith(patternSampleExternalId)) {
				if (abstractSampleAPI.dao_checkObjectExist("externalId", experiment.sampleIdentifier)) {
					abstSample = abstractSampleAPI.dao_findOne(DBQuery.is("externalId", experiment.sampleIdentifier));
					//logger.debug("777777777777777777777777777777777777777         sample avec "+ experiment.sampleIdentifier + " bien trouve dans base");
					sample_in_base = true;	
					experiment.sampleCode = abstSample.code;
					// laisser le sampleIdentifier de l'exp tel quel.
					//experiment.sampleIdentifier = abstSample.accession;	
				}
			} else {
				//logger.debug("EEEEEEEEEEEEEEEEEEEEEEEEEEEE        sample avec "+ experiment.sampleIdentifier + " non trouve dans base");
				//throw new SraException("pattern de l'identifiant du sample "  + experiment.sampleIdentifier +" , dans l'experiment " + experiment.code + " non reconnu");
				contextValidation.addError("updateObjectsCaseUpdateExperiments ", "pattern de l'identifiant du sample "  + experiment.sampleIdentifier +" , dans l'experiment " + experiment.code + " non reconnu" );
				continue;
			}

			if(! sample_in_base) {
				//logger.debug("888888888888888888888888    demande de creation du sample avec  " + experiment.sampleAccession);
				ExternalSample externalSample = createServices.buildExternalSample(experiment.sampleIdentifier, user);
				abstSample = (AbstractSample) externalSample;
				experiment.sampleCode = abstSample.code;
				// laisser le sampleIdentifier de l'exp tel quel.
				//experiment.sampleIdentifier = abstSample.accession;	
				if( ! hExternalSample_to_save.containsKey(externalSample.code)) {
					hExternalSample_to_save.put(externalSample.code, externalSample);
				}
			} 
			
			submissionNewAPITools.updateExperimentForReleaseDate(abstStudy, experiment, hExperimentToUpdateForReleaseDate, contextValidation);	
			
			// mettre à jour l'object submission :
			if(! submission.experimentCodes.contains(experiment.code)) {
				submission.experimentCodes.add(experiment.code);
			}
			if(! submission.refSampleCodes.contains(experiment.sampleCode)) {
				submission.refSampleCodes.add(experiment.sampleCode);
			}
			if(! submission.refStudyCodes.contains(experiment.studyCode)) {
				submission.refStudyCodes.add(experiment.studyCode);
			}
			
		} // end for experiments.
		
		// C'est seulement une fois tous les experiment mis à jour pour releaseDate qu'on peut calculer les releaseDate des samples referencés par ces exp
		// mais il faut aussi recalculés les releaseDate des samples qui ne sont plus referencés par les exp modifiés.
		HashMap<String, Analysis>     hAnalyzesToUpdateForReleaseDate = new HashMap<String, Analysis>();

		//submissionNewAPITools.updateSampleForReleaseDate(hExperimentToUpdateForReleaseDate, hAnalyzesToUpdateForReleaseDate, contextValidation, hSampleToUpdateForReleaseDate);
		// on ne recupere pas bien le hash ci-dessous
		// hSampleToUpdateForReleaseDate = submissionNewAPITools.updateSampleForReleaseDate(hExperimentToUpdateForReleaseDate, hAnalyzesToUpdateForReleaseDate, contextValidation);
		 hSampleToUpdateForReleaseDate.putAll(submissionNewAPITools.updateSampleForReleaseDate(hExperimentToUpdateForReleaseDate, hAnalyzesToUpdateForReleaseDate, contextValidation, true));
		 //logger.debug("YYYYYYYY   Dans updateObjectsCaseUpdateExperiments : taille de samples a mettre a jour pour releaseDate = " + hSampleToUpdateForReleaseDate.entrySet().size());
		// Calcul des releaseDate des samples referencés et deréférencés par les experiments et les analyses:
		for (Entry<String, Sample> setSample : hSampleToUpdateForReleaseDate.entrySet()) {
			if(setSample.getValue().releaseDate!= null) {
				logger.debug("dans updateObjectsCaseUpdateExperiments apres updateSampleForReleaseDate AAAAAAAAAAAAAA         pour le sample " + setSample.getValue().code + ", releaseDate = " + setSample.getValue().releaseDate);
			} else {
				logger.debug("dans updateObjectsCaseUpdateExperiments  apres updateSampleForReleaseDate AAAAAAAAAAAAAA         pour le sample " + setSample.getValue().code + ", releaseDate = null");
			}
		}	
	} // end updateObjectsCaseUpdateExperiments
	


} // end UpdateServices
