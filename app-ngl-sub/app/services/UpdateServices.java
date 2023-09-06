package services;

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

import fr.cea.ig.lfw.utils.Iterables;
import fr.cea.ig.ngl.dao.api.sra.AbstractSampleAPI;
import fr.cea.ig.ngl.dao.api.sra.AbstractStudyAPI;
import fr.cea.ig.ngl.dao.api.sra.ExternalSampleAPI;
import fr.cea.ig.ngl.dao.api.sra.ExternalStudyAPI;
//import fr.cea.ig.ngl.dao.api.sra.ProjectAPI;
//import fr.cea.ig.ngl.NGLConfig;
import fr.cea.ig.ngl.dao.sra.ExperimentDAO;
import fr.cea.ig.ngl.dao.sra.ProjectDAO;
import fr.cea.ig.ngl.dao.sra.SampleDAO;
import fr.cea.ig.ngl.dao.sra.StudyDAO;
import fr.cea.ig.ngl.dao.sra.SubmissionDAO;
import models.laboratory.common.instance.State;
import models.sra.submit.sra.instance.AbstractSample;
import models.sra.submit.sra.instance.AbstractStudy;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.sra.instance.ExternalSample;
import models.sra.submit.sra.instance.ExternalStudy;
import models.sra.submit.sra.instance.Project;
import models.sra.submit.sra.instance.Sample;
import models.sra.submit.sra.instance.Study;
import models.sra.submit.sra.instance.Submission;
import models.sra.submit.sra.instance.Submission.TypeRawDataSubmitted;
import models.sra.submit.util.SraCodeHelper;
import models.sra.submit.util.SraException;
import models.sra.submit.util.VariableSRA;
import ngl.refactoring.state.SRASubmissionStateNames;
import play.Logger;
import services.taxonomy.Taxon;
import services.taxonomy.TaxonomyServices;
import validation.ContextValidation;

public class UpdateServices  {

	private static final play.Logger.ALogger logger = play.Logger.of(UpdateServices.class);
	private final ProjectDAO      	  projectDAO;
	private final StudyDAO      	  studyDAO;
	private final SampleDAO           sampleDAO;
	private final ExperimentDAO       experimentDAO;
	private final SubmissionDAO       submissionDAO;
	private final SraCodeHelper       sraCodeHelper;
	private final AbstractStudyAPI    abstractStudyAPI;
	private final AbstractSampleAPI   abstractSampleAPI;
	private final ExternalSampleAPI   externalSampleAPI;
	private final ExternalStudyAPI    externalStudyAPI;

	private final CreateServices      createServices;
	private final String              patternSampleAc = "ERS\\d+";
	private final String              patternSampleExternalId = "SAM";
	private final String              patternStudyAc = "ERP\\d+";
	private final String              patternStudyExternalId  = "PRJ";
	private final TaxonomyServices    taxonomyServices;

	@Inject
	public UpdateServices(SubmissionDAO       submissionDAO,
						  ProjectDAO          projectDAO,
						  StudyDAO            studyDAO,
						  SampleDAO           sampleDAO,
						  ExperimentDAO       experimentDAO,
						  SraCodeHelper       sraCodeHelper,
						  AbstractStudyAPI    abstractStudyAPI,
						  AbstractSampleAPI   abstractSampleAPI,
						  ExternalStudyAPI    externalStudyAPI,
						  ExternalSampleAPI   externalSampleAPI,
						  TaxonomyServices    taxonomyServices,
						  CreateServices      createServices) {
		this.submissionDAO       = submissionDAO;
		this.studyDAO            = studyDAO;
		this.projectDAO          = projectDAO;
		this.sampleDAO           = sampleDAO;
		this.experimentDAO       = experimentDAO;
		this.sraCodeHelper       = sraCodeHelper;
		this.abstractStudyAPI    = abstractStudyAPI;
		this.abstractSampleAPI   = abstractSampleAPI;
		this.externalStudyAPI    = externalStudyAPI;
		this.externalSampleAPI   = externalSampleAPI;
		this.taxonomyServices    = taxonomyServices;
		this.createServices      = createServices;
	}
	
	

	
	
	private  List <String> valideArgsCreateSubmissionForUpdate(ContextValidation ctxVal,
															   Project umbrella,
															   Study study, 
															   List<Sample> samples,
															   List<Experiment> experiments)  {
		
		List <String> projectCodes = new ArrayList<>();
		boolean dataForEbi = false;
		ctxVal.setUpdateMode();
		String user = ctxVal.getUser();
		//logger.debug("Entree dans valideArgsCreateSubmissionForUpdate");
		if (study != null) {
			dataForEbi = true;	
			SraCodeHelper.assertObjectStateCode(study, SRASubmissionStateNames.SUB_F, ctxVal);
			//study.setTraceUpdateStamp(user);
			// Mettre dans l'etat qui sera sauvé si tout va bien:
			study.updateStateAndTrace(new State(SRASubmissionStateNames.SUBU_N, user));
			study.validate(ctxVal); 
						
			if (study.traceInformation.createUser == null) {
				String message = "Le study " + study.code
						+ " n'est pas renseigné dans la base pour le champs createUser";	
				//throw new SraException("createSubmissionForUpdate", message);
				ctxVal.addError("valideArgsCreateSubmissionForUpdate", message);
			}
			if (user == null) {
				String message = "l'utilisateur n'est pas authentifié";
				//throw new SraException("createSubmissionForUpdate", message);
				ctxVal.addError("valideArgsCreateSubmissionForUpdate", message);
			}
						  
			// verifier qu'aucun locusTagPrefix du study n'existe dans un autre study :
			String message = "";
			
			if (study.locusTagPrefixs != null && !study.locusTagPrefixs.isEmpty()) {
				for (String locus : study.locusTagPrefixs) {
					List <Study> studyList = studyDAO.dao_find(DBQuery.in("locusTagPrefixs", locus)).toList();
					if (studyList.size() > 0) {
						String header = "locusTagPrefix =" + locus + "\n";
						message += header;
						for (Study s: studyList) {
							if(! s.code.equals(study.code)) {
								message += "       - deja utilise par le study " + s.code + "\n";
							}
						}
						if(StringUtils.isNotBlank(message)) {
							ctxVal.addError("valideArgsCreateSubmissionForUpdate", header + message);
						}
					}	 
				}
			}

			// Une meme reference bibliographique peut exister dans plusieurs study : pas de controle d'unicité
			
			Study dbStudy = studyDAO.getObject(study.code);
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
				ctxVal.addError("valideArgsCreateSubmissionForUpdate", "Vous ne pouvez pas supprimer des locus_tag_prefix " + message);
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
				ctxVal.addError("valideArgsCreateSubmissionForUpdate", "Vous ne pouvez pas supprimer des references PUBMED  " + message);
			}
			
			// verifier qu'on ne supprime pas et/ou qu'on ne modifie pas la taxonomie du study :
			if (StringUtils.isNotBlank(dbStudy.taxonId)) {
				if(StringUtils.isBlank(study.taxonId) || !study.taxonId.equals(dbStudy.taxonId)) {
					//throw new SraException("createSubmissionForUpdate", "Vous ne pouvez pas supprimer ou modifier le taxonId du study " + study.code);
					ctxVal.addError("valideArgsCreateSubmissionForUpdate", "Vous ne pouvez pas supprimer ou modifier le taxonId du study " + study.code);
				}
			} 
			// verifier qu'on ne supprime pas et/ou qu'on ne modifie pas le scientificName du study:
			if (StringUtils.isNotBlank(dbStudy.scientificName)) {
				if(StringUtils.isBlank(study.scientificName) || !study.scientificName.equals(dbStudy.scientificName)) {
					//throw new SraException("createSubmissionForUpdate", "Vous ne pouvez pas supprimer ou modifier le scientificName du study " + study.code);
					ctxVal.addError("valideArgsCreateSubmissionForUpdate", "Vous ne pouvez pas supprimer ou modifier le scientificName du study " + study.code);
				}
			}
			// cas d'un nouveau taxonId sur le study, il faut remplir scientificName :
			if (StringUtils.isBlank(dbStudy.taxonId) && StringUtils.isNotBlank(study.taxonId)) {
				Taxon taxon = taxonomyServices.getTaxon(study.taxonId);
				if (taxon == null) {
					//throw new SraException("createSubmissionForUpdate", " Pas de recuperation du taxonId " + study.taxonId + " au NCBI ou à l'EBI");
					ctxVal.addError("valideArgsCreateSubmissionForUpdate", " Pas de recuperation du taxonId " + study.taxonId + " au NCBI ou à l'EBI");
				}
				if(StringUtils.isNotBlank(taxon.errorMessage)) {
					//throw new SraException("createSubmissionForUpdate", " Probleme lors de la recuperation du taxonId " + study.taxonId + " au NCBI ou à l'EBI " + taxon.errorMessage);
					ctxVal.addError("valideArgsCreateSubmissionForUpdate", " Probleme lors de la recuperation du taxonId " + study.taxonId + " au NCBI ou à l'EBI " + taxon.errorMessage);
				}
				if(StringUtils.isNotBlank(taxon.scientificName)) {
					study.scientificName = taxon.scientificName;
				}
			}	
			
		    // verifier date de release :
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
			
			if (userReleaseDate != null) {	
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(userReleaseDate);
				//logger.debug("releaseDate du study recupere dans Calendar = " + calendar.getTime());
				calendar.add(Calendar.HOUR_OF_DAY, 4);
				//logger.debug("releaseDate du study recupere dans Calendar après ajout de 4 heures = " + calendar.getTime());
				Date goodUserReleaseDate  = calendar.getTime();
			    String formatted_dbReleaseDate = targetFormat.format(dbReleaseDate);
			    String formatted_goodUserReleaseDate = targetFormat.format(goodUserReleaseDate);
			    // impossible de modifier releaseDate si données deja publique :
			    if(!formatted_goodUserReleaseDate.equals(formatted_dbReleaseDate) && dbReleaseDate.before(new Date())) {
					//throw new SraException("createSubmissionForUpdate", "La date de release d'origine " + formatted_dbReleaseDate + " est passee (donnee publique) et ne peut plus etre modifiée");
			    	ctxVal.addError("valideArgsCreateSubmissionForUpdate", "La date de release d'origine " + formatted_dbReleaseDate + " est passee (donnee publique) et ne peut plus etre modifiée");
			    }
				if (!formatted_goodUserReleaseDate.equals(formatted_dbReleaseDate) && goodUserReleaseDate.before(dbReleaseDate)) {
					//throw new SraException("createSubmissionForUpdate", "La date de release d'origine ne peut pas etre avancée. La date de release de l'utilisateur " + formatted_goodUserReleaseDate + " n'est pas après la date de release initiale " + formatted_dbReleaseDate);
					ctxVal.addError("valideArgsCreateSubmissionForUpdate", "La date de release d'origine ne peut pas etre avancée. La date de release de l'utilisateur " + formatted_goodUserReleaseDate + " n'est pas après la date de release initiale " + formatted_dbReleaseDate);
				}
				Calendar calendar_2 = Calendar.getInstance();
				calendar_2.add(Calendar.YEAR, 2);
				Date dans2ans = calendar_2.getTime();
				if (goodUserReleaseDate.after(dans2ans)) {
					//throw new SraException("createSubmissionForUpdate", "La date de release proposée " + formatted_goodUserReleaseDate + " ne doit pas etre dans plus de 2 ans");					
					ctxVal.addError("valideArgsCreateSubmissionForUpdate", "La date de release proposée " + formatted_goodUserReleaseDate + " ne doit pas etre dans plus de 2 ans");					
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
				ctxVal.addError("valideArgsCreateSubmissionForUpdate", message);
			}	
			for (String projCode : study.projectCodes) {
				if(!projectCodes.contains(projCode)) {
					projectCodes.add(projCode);
				}
			}
		} 

		if (umbrella != null) {
			dataForEbi = true;	
			
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
				ctxVal.addError("valideArgsCreateSubmissionForUpdate", message);
			}
			if (user == null) {
				String message = "l'utilisateur n'est pas authentifié";
				//throw new SraException("createSubmissionForUpdate", message);
				ctxVal.addError("valideArgsCreateSubmissionForUpdate", message);
			}
			// sgas user
//			if (! umbrella.traceInformation.createUser.equals(user)) {		
//				String message = "l'utilisateur " + user + 
//						" n'est pas autorisé à mettre à jour le project umbrella " + umbrella.code 
//						+ " crée par " + umbrella.traceInformation.createUser;
//				throw new SraException("createSubmissionForUpdate", message);
//			}

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
				ctxVal.addError("valideArgsCreateSubmissionForUpdate", message);
			}	
			// verifier que le project umbrella de l'utilisateur ne supprime pas des project enfants 
			Project dbUmbrella = projectDAO.getObject(umbrella.code);
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
				ctxVal.addError("valideArgsCreateSubmissionForUpdate", "Vous ne pouvez pas supprimer des projects enfants  " + message);
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
				ctxVal.addError("valideArgsCreateSubmissionForUpdate", "Vous ne pouvez pas supprimer des references PUBMED  " + message);
			}
			
			
			
			
			
			
			
			// verifier que les projects enfant d'un project umbrella ne sont pas deja declarés dans un autre project umbrella :
			message = "";
			if (umbrella.childrenProjectAccessions != null && !umbrella.childrenProjectAccessions.isEmpty()) {
				for (String projectAC : umbrella.childrenProjectAccessions) {
					List <Project> umbrellaList = projectDAO.dao_find(DBQuery.in("childrenProjectAccessions", projectAC)).toList();
					if (umbrellaList.size() > 0) {
						String header = "projectAC =" + projectAC + "\n";
						
						for (Project p: umbrellaList) {
							if(! p.code.equals(umbrella.code)) {
								message += "       - deja utilise par le project umbrella  " + p.code + "\n";
							}
						}
						if(StringUtils.isNotBlank(message)) {
							//throw new SraException(header + message);
							ctxVal.addError("valideArgsCreateSubmissionForUpdate", header + message);
						}
					}	 
				}
			}
						
		} 

		
		for (Sample sample : samples) {
			dataForEbi = true;

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
				ctxVal.addError("valideArgsCreateSubmissionForUpdate", message);
			}
			if(!projectCodes.contains(sample.projectCode)) {
				projectCodes.add(sample.projectCode);
			}
		}
		for (Experiment experiment :experiments) {
			dataForEbi = true;
			SraCodeHelper.assertObjectStateCode(experiment, SRASubmissionStateNames.SUB_F, ctxVal);

			// Mettre dans l'etat qui sera sauvé si tout va bien:
			experiment.updateStateAndTrace(new State(SRASubmissionStateNames.SUBU_N, user));
			experiment.validate(ctxVal);

			//ctxVal.displayErrors(logger);

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
				ctxVal.addError("valideArgsCreateSubmissionForUpdate", message);
			}
			if(!projectCodes.contains(experiment.projectCode)) {
				projectCodes.add(experiment.projectCode);
			}			
		}
		if(!dataForEbi) {
			//throw new SraException("createSubmissionForUpdate", "Aucun project umbrella, study, sample ou experiment à mettre à jour??");	
			ctxVal.addError("valideArgsCreateSubmissionForUpdate", "Aucun project umbrella, study, sample ou experiment à mettre à jour??");	
		}
		
		return projectCodes;
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
 */
public Submission createSubmissionForUpdate(ContextValidation contextValidation, 
										Project umbrella,
										Study study,
										List<Sample> samples,
										List<Experiment> experiments) throws SraException {
	//logger.debug("UpdateServices.createSubmissionForUpdate");
	// La soumission n'existe pas à ce stade donc pas de validation de state à faire
	Submission submission = null;
	Date courantDate = new java.util.Date();
	String user = contextValidation.getUser();		
	List <String> projectCodes = valideArgsCreateSubmissionForUpdate(contextValidation, umbrella, study, samples, experiments);
	Logger.debug("avant le assert");
	SraException.assertNoError(contextValidation);
	Logger.debug("après le assert");
	contextValidation.setCreationMode();

	submission = new Submission(contextValidation.getUser(), projectCodes);
	if(umbrella != null) {
		submission.code = sraCodeHelper.generateSubmissionCodeForUmbrella();
	} else {
		submission.code = sraCodeHelper.generateSubmissionCode(projectCodes);
	}
	submission.traceInformation.creationDate = courantDate;
	submission.type = Submission.Type.UPDATE;
	submission.state = new State(SRASubmissionStateNames.SUBU_N, contextValidation.getUser());
	submission.submissionDirectory = VariableSRA.submissionRootDirectory + File.separator + submission.code + "_update"; 	
	submission.ebiResult = "ebi_update_" + submission.code + ".txt";
	submission.typeRawDataSubmitted = TypeRawDataSubmitted.withoutRawData;
	//important pour la suite : ??????
	submission.setTraceUpdateStamp(contextValidation.getUser());


//	// mettre à jour l'objet submission pour le cas d'une mise à jour du project umbrella
	if (umbrella != null && StringUtils.isNotBlank(umbrella.code)) {		
		submission.umbrellaCode = umbrella.code;	// si update d'un project (PRJEB)
	}
	// mettre à jour l'objet submission pour le cas d'une mise à jour de study 
	if (study != null && StringUtils.isNotBlank(study.code)) {		
		submission.studyCode = study.code;	
		submission.refStudyCodes.add(study.code);
	}
	// mettre à jour l'objet submission pour le cas d'une mise à jour de samples :
	for (Sample sample :samples) {
		submission.sampleCodes.add(sample.code);
		submission.refSampleCodes.add(sample.code);
	}		 
	// mettre à jour l'objet submission pour le cas d'une mise à jour d'experiments :
	HashMap<String, ExternalSample> externalSample_to_save = new HashMap<String, ExternalSample>();
	HashMap<String, ExternalStudy> externalStudy_to_save = new HashMap<String, ExternalStudy>();

	
	// le champs experiment.sampleAccession est modifiable dans les interfaces js => si un utilisateur a modifie ce champs, il faut mettre en coherence
	// le champs sampleCode qui lui n'est pas modifiable dans les interfaces.
	// si modification par user du champs experiment.studyAccession, il faut mettre en coherence le champs experiment.studyCode
	Logger.debug("avant le for");
	for (Experiment experiment : experiments) {
		submission.experimentCodes.add(experiment.code);
		boolean sample_in_base = false;
		if(experiment.sampleAccession.matches(patternSampleAc)) {
			// on est oblige de checkque existance objet car sinon exception sur dao_findOne
			if (abstractSampleAPI.dao_checkObjectExist("accession", experiment.sampleAccession)) {
				AbstractSample abstSample = abstractSampleAPI.dao_findOne(DBQuery.is("accession", experiment.sampleAccession));
				if (abstSample != null) {
					//logger.debug("66666666666666666666666666666666         sample avec "+ experiment.sampleAccession + " bien trouve dans base");
					sample_in_base = true;
					experiment.sampleCode = abstSample.code;
					experiment.sampleAccession = abstSample.accession;
				} 
			}
		} else if(experiment.sampleAccession.startsWith(patternSampleExternalId)) {
			if (abstractSampleAPI.dao_checkObjectExist("externalId", experiment.sampleAccession)) {
				AbstractSample abstSample = abstractSampleAPI.dao_findOne(DBQuery.is("externalId", experiment.sampleAccession));
				if (abstSample != null) {
					//logger.debug("777777777777777777777777777777777777777         sample avec "+ experiment.sampleAccession + " bien trouve dans base");
					sample_in_base = true;	
					experiment.sampleCode = abstSample.code;
					experiment.sampleAccession = abstSample.accession;
				}
			}
		} else {
			//logger.debug("EEEEEEEEEEEEEEEEEEEEEEEEEEEE        sample avec "+ experiment.sampleAccession + " bien trouve dans base");
			throw new SraException("pattern de l'identifiant du sample "  + experiment.sampleAccession +" , dans l'experiment " + experiment.code + " non reconnu");
		}
		
		if(! sample_in_base) {
			//logger.debug("888888888888888888888888    demande de creation du sample avec  " + experiment.sampleAccession);
			ExternalSample externalSample = createServices.buildExternalSample(experiment.sampleAccession, user);
			experiment.sampleCode = externalSample.code;
			if( ! externalSample_to_save.containsKey(externalSample.code)) {
				//logger.debug("9999999999999999999999999999999    insertion dans liste a sauver");
				externalSample_to_save.put(externalSample.code, externalSample);
			}
		}
		
		boolean study_in_base = false;
		if(experiment.studyAccession.matches(patternStudyAc)) {
			// on est oblige de checkque existance objet car sinon exception sur dao_findOne
			if (abstractStudyAPI.dao_checkObjectExist("accession", experiment.studyAccession)) {
				AbstractStudy abstStudy = abstractStudyAPI.dao_findOne(DBQuery.is("accession", experiment.studyAccession));
				if (abstStudy != null) {
					//logger.debug("11111111111111111111111111111         study avec "+ experiment.studyAccession + " bien trouve dans base");
					study_in_base = true;
					experiment.studyCode = abstStudy.code;
					experiment.studyAccession = abstStudy.accession;
				}
			}
		} else if(experiment.studyAccession.startsWith(patternStudyExternalId)) {
			if (abstractStudyAPI.dao_checkObjectExist("externalId", experiment.studyAccession)) {
				AbstractStudy abstStudy = abstractStudyAPI.dao_findOne(DBQuery.is("externalId", experiment.studyAccession));
				if (abstStudy != null) {
					//logger.debug("22222222222222222222222222222222       study avec "+ experiment.studyAccession + " bien trouve dans base");
					study_in_base = true;
					experiment.studyCode = abstStudy.code;
					experiment.studyAccession = abstStudy.accession;
				}
			}
		} else {
			//logger.debug("AAAAAAAAAAAAAAAAAAAAAAAAAAA        sample avec "+ experiment.sampleAccession + " bien trouve dans base");
			throw new SraException("pattern de l'identifiant du study "  + experiment.studyAccession +" , dans l'experiment " + experiment.code + " non reconnu");
		}
		
		if(! study_in_base) {
			//logger.debug("3333333333333333333333333333333333333333    demande de creation du study avec study " + experiment.studyAccession);
			ExternalStudy externalStudy = createServices.buildExternalStudy(experiment.studyAccession, user);
			experiment.studyCode = externalStudy.code;
			if( ! externalStudy_to_save.containsKey(externalStudy.code)) {
				//logger.debug("44444444444444444444444444444444444    insertion dans liste a sauver");
				externalStudy_to_save.put(externalStudy.code, externalStudy);
			}
		}
		
		if(! submission.refSampleCodes.contains(experiment.sampleCode)) {
			submission.refSampleCodes.add(experiment.sampleCode);
		}
		
		if(! submission.refStudyCodes.contains(experiment.studyCode)) {
			submission.refStudyCodes.add(experiment.studyCode);
		}
		
	}

	Logger.debug("après le for");

	// inutile, sinon begaiement sur historique
	//submission.updateStateAndTrace(new State(SRASubmissionStateNames.SUBU_N, contextValidation.getUser()));
	// On verifie que l'objet qu'on vient de creer est bien ok :
	contextValidation.setCreationMode();// On crée une soumission pour update
	//logger.debug("UpdateServices.createSubmissionForUpdate:: submissionCode = " + submission.code);
	submission.validate(contextValidation);
	//logger.debug("UpdateServices.createSubmissionForUpdate:: apres validate de la soumission = ");
	if(contextValidation.hasErrors()) {
		logger.debug("UpdateServices.createSubmissionForUpdate:: validation soumission avec erreur");
	}
	
	SraException.assertNoError(contextValidation);
	//logger.debug("UpdateServices.createSubmissionForUpdate:: apres assertNoError WWWWWWWWWWWW");

	Submission dbSubmission = submissionDAO.save(submission);
	//logger.debug("UpdateServices.createSubmissionForUpdate:: dbSubmissionCode   wwwwwwwwww=" + dbSubmission.code);

	
	// updater dans la base project, study, samples et experiments 
	//qui sont deja validés et avec bon etat  et bon state après valideArgsCreateSubmissionForUpdate:
	
	contextValidation.setUpdateMode();	
	if (umbrella != null) {
		//logger.debug("WWWWWWWWWWWWWWWWWWWW  sauvegarde de l'umbrella "+ umbrella.code);
		projectDAO.save(umbrella);
	}
	if (study != null) {
		studyDAO.save(study);
		//logger.debug("XXXXXXXXXXXXXXXXXXXX  sauvegarde du study "+ study.code);
	} 
	for (Sample sample : samples) {
		//logger.debug("YYYYYYYYYYYYYYYYYYY  sauvegarde du sample "+ sample.code);
		sampleDAO.save(sample);
	}			
	for (Experiment experiment : experiments) {
		//logger.debug("ZZZZZZZZZZZZZZZZZZZZZZZ  sauvegarde de l'experiment "+ experiment.code);
		experimentDAO.save(experiment);
	}	
	for (Entry<String, ExternalSample> setExternalSample : externalSample_to_save.entrySet()) {
		//logger.debug("101010101010101010101010101010  sauvegarde dans base du sample "+ setExternalSample.getValue().code);
		externalSampleAPI.dao_saveObject(setExternalSample.getValue());
	}
	for (Entry<String, ExternalStudy> setExternalStudy : externalStudy_to_save.entrySet()) {
		//logger.debug("555555555555555555555555555  sauvegarde dans base du study "+ setExternalStudy.getValue().code);
		externalStudyAPI.dao_saveObject(setExternalStudy.getValue());
	}
	return dbSubmission;
	}
	
}