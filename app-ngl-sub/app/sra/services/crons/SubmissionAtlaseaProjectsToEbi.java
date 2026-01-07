package sra.services.crons;

import static ngl.refactoring.state.SRASubmissionStateNames.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;
import com.google.inject.Provider;
import com.mongodb.MongoException;
import com.sybase.jdbc4.a.b.an;
import controllers.sra.api.AbstractStudyAPI;
import controllers.sra.api.AtlaseaAPI;
import controllers.sra.api.ProjectAPI;
import controllers.sra.api.SubmissionAPI;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.Script;
import fr.cea.ig.lfw.utils.Iterables;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.NGLConfig;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.api.AnalysisAPI;
import fr.cea.ig.ngl.dao.sra.AtlaseaDAO;
import mail.MailServiceException;
import mail.MailServices;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.run.instance.Analysis;
import models.laboratory.sample.instance.Sample;
import models.sra.submit.sra.instance.Atlasea;
import models.sra.submit.sra.instance.Project;
import models.sra.submit.sra.instance.Study;
import models.sra.submit.sra.instance.Submission;
import models.sra.submit.sra.instance.Submission.TypeRawDataSubmitted;
import models.utils.InstanceConstants;
import models.utils.dao.DAOException;
import play.Logger;
import play.data.validation.ValidationError;
import rules.services.RulesException;
import services.sra.EbiAPI;
import services.sra.NGLSUBConfig;
import services.sra.SraException;
import services.taxonomy.TaxonomyServices;
import sra.api.SubmissionNewAPI;
import sra.services.EbiFileResponseServices;
import sra.services.XmlServices;
import sra.services.lock.LockAPI;
import validation.ContextValidation;
import org.slf4j.MDC;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import sra.services.lock.LockAPI;
import sra.services.lock.ActiveLock;
import validation.ContextValidation;


// Lancement via sra.services.crons.sheduler.ProdSubmissionCronScheduler 
// ou via sra.scripts.crons.ScriptSubmissionAtlaseaProjectsToEbi

/**
 * Declaration à l'EBI des study et project Atlasea pour les analyses ayant un tolid et pas encore de primaryAssemblyProject
 * 
 * @author sgas
 */
public class SubmissionAtlaseaProjectsToEbi implements Runnable {
	private final LockAPI      lockAPI; 
	private final String       ebiCurlProd               = "webin-9_PROD";  // valeur dans application.conf pour ebiCurl si PROD
	private final String       ebiCurlTest9              = "webin-9_TEST";  // valeur possible dans application.conf pour ebiCurl si TEST
	private final String       ebiCurlTest261            = "webin-261_TEST";// valeur possible dans application.conf pour ebiCurl si TEST
	private final String       dropBox                   = "Webin-9";
	private final String       superParentUmbrella       = "PRJEB64126";
	private final List<String> projectAtlaseaCodes       = Arrays.asList("DKB", "DKC", "DKD", "DKE", "DKF");
	//	private final Iterable <String> projectAtlaseaCodes  = Arrays.asList("DKB", "DKC", "DKD", "DKE", "DKF");
	private final String       existingStudyType         = "Other";
	// variables pour les mails :
	private final String       expediteur                = "cnsnglapps";
	private final String       headerSuccessMessage      = String
			.format("<tr><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>\n",
					"tolid", "taxon", "taxonId", "date", "umbrella", "sequencingStudy", "primaryAssemblyStudy");  

	private static final play.Logger.ALogger logger      = play.Logger.of(SubmissionAtlaseaProjectsToEbi.class);
	private final String                     submissionAtlaseaRootResultDirectory; 
	private final String                     ebiCurl;
	private final String                     user;
	private final NGLSUBConfig               config;
	public final SubmissionAPI               submissionAPI;
	public final SubmissionNewAPI            submissionNewAPI;
	public final AbstractStudyAPI            abstractStudyAPI;
	public final XmlServices                 xmlServices;
	public final TaxonomyServices            taxonomyServices;
	public final ProjectAPI                  projectAPI;
	public final AtlaseaDAO                  atlaseaDAO;
	public final AtlaseaAPI                  atlaseaAPI;
	public final EbiAPI                      ebiAPI;
	public final EbiFileResponseServices     ebiFileResponseServices;
	public final AnalysisAPI                 biAnalysisAPI;   // pour faire un update des analyses de BI, sans regles sur les proprietes car pas implémentées.


	@Inject
	public SubmissionAtlaseaProjectsToEbi(NGLSUBConfig            config,
			LockAPI                 lockAPI,  
			SubmissionAPI           submissionAPI,
			SubmissionNewAPI        submissionNewAPI,
			AbstractStudyAPI        abstractStudyAPI,
			XmlServices             xmlServices,
			TaxonomyServices        taxonomyServices,
			ProjectAPI              projectAPI,
			AtlaseaDAO              atlaseaDAO,
			AtlaseaAPI              atlaseaAPI,
			EbiAPI                  ebiAPI,
			EbiFileResponseServices ebiFileResponseServices,
			AnalysisAPI             biAnalysisAPI) {

		this.config                               = config;
		this.submissionAtlaseaRootResultDirectory = config.getCreationProjectsAtlaseaRootDirectory(); 
		this.lockAPI                              = lockAPI;
		this.ebiCurl                              = config.getEbiCurl();
		this.submissionAPI                        = submissionAPI;
		this.submissionNewAPI                     = submissionNewAPI;
		this.abstractStudyAPI                     = abstractStudyAPI;
		this.xmlServices                          = xmlServices;
		this.taxonomyServices                     = taxonomyServices;
		this.projectAPI                           = projectAPI;
		this.atlaseaDAO                           = atlaseaDAO;
		this.atlaseaAPI                           = atlaseaAPI;
		this.ebiAPI                               = ebiAPI;
		this.ebiFileResponseServices              = ebiFileResponseServices;
		this.biAnalysisAPI                        = biAnalysisAPI;

		if(ebiCurl.equalsIgnoreCase(this.ebiCurlProd)) {
			this.user = "cbelser";
		} else {
			this.user = "sgas";
		}
		//logger.warn("dans constructeur SubmissionAtlaseaProjectsToEbi");
	}


	//	@Override
	//	public void runImport(ContextValidation contextValidation)
	//			throws SQLException, DAOException, MongoException, RulesException, APIValidationException, APIException {
	@Override
	public void run() {
		try (ActiveLock l = lockAPI.aquireLock(SubmissionAtlaseaProjectsToEbi.class)){
			logger.warn("dans run SubmissionAtlaseaProjectsToEbi");
			boolean alreadySendSuccessMail = false;
			boolean alreadySendErrorMail = false;

			ContextValidation globalCtx = ContextValidation.createUndefinedContext(user);
			StringBuilder errorBuilder    = new StringBuilder();
			StringBuilder successBuilder  = new StringBuilder();
			try {		
				// verification des variables de application.conf :
				// si erreur alors quitter cron en envoyant mail d'erreur.
				logInfosEnvironnement(globalCtx);
				if ( globalCtx.hasErrors() ) {
					storeErrorMessage(globalCtx, errorBuilder);
					//logger.debug("\n\nDans globalCtx :");
					//globalCtx.displayErrors(logger, "debug");
					logger.error(errorBuilder.toString());
					sendErrorMail(errorBuilder);
					return;
				}
				checkAlreadyAnalysisProcessed();
				// selection de la liste des Analyses de type genome-denovo-assembly avec tolid renseigné :
				// dans prochain modele ajouter selection pour avoir uniquement les analyses avec tolid et pas encore traités
				// par exemple avec properties.projectPrimaryAssemblyAccession
				List<Analysis> listAnalysis = MongoDBDAO.find( InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, 
						DBQuery.and(
								DBQuery.is("typeCode", "genome-denovo-assembly"), 
								DBQuery.in("projectCodes", projectAtlaseaCodes),
								DBQuery.exists("properties.tolid.value"),
								DBQuery.notIn ("properties.tolid.value", null, ""),
								DBQuery.or(DBQuery.notExists("properties.primaryAssemblyProjectAccession"),
										DBQuery.notExists("properties.primaryAssemblyProjectAccession.value"),
										DBQuery.in("properties.primaryAssemblyProjectAccession.value", null, "")))).toList();	

				// pour raccourcir tests : listAnalysis = Iterables.take(listAnalysis, 15).toList();
				logger.info("Nombre d'analyse a traiter = " + listAnalysis.size());
				logger.info("atlaseaRootResultDirectory=" + this.submissionAtlaseaRootResultDirectory);


				@SuppressWarnings("unused")
				int cp = 0;

				for (Analysis analysis : listAnalysis) {
					String tolId = (String) analysis.properties.get("tolid").value;
					//logger.debug("Entree dans boucle for execute pour analysis = " + analysis.code + ", tolId = " + tolId );
					if (StringUtils.isBlank(tolId)) {
						//logger.debug("tolId='" + tolId + "'");
						continue;
					}
					cp ++;
					ContextValidation ctx = ContextValidation.createUndefinedContext(user);

					ctx = ctx.appendPath(tolId);
					// verifier unicite et non blanc du taxonCode de l'analyse et soumettable
					String strTaxonId = getUniqTaxonCode(analysis, tolId, ctx);
					if(ctx.hasErrors()) {
						//ctx.displayErrors(logger, "debug");
						addErreurCxtBInCtxA(globalCtx, ctx);
						continue;
					}
					//logger.debug("tolId = " + tolId + ", strTaxonId = " + strTaxonId);
					String scientificName = taxonomyServices.getNcbiEbiScientificName(strTaxonId, ctx);
					if(ctx.hasErrors()) {
						//ctx.displayErrors(logger, "debug");
						addErreurCxtBInCtxA(globalCtx, ctx);
						continue;
					}

					//logger.debug("tolId = " + tolId + ", scientificName = " + scientificName);

					//logger.debug("Pour analysis=" + analysis.code + ", taxonCode=" + strTaxonId +", scientificName=" + scientificName + ", tolId=" +tolId);
					Atlasea atlasea;
					if( atlaseaDAO.isCodeExist(analysis.code)) {
						atlasea = atlaseaDAO.findByCode(analysis.code);
						//logger.debug("atlasea avec code " + atlasea.code + " bien trouve dans base");
					} else {
						atlasea = new Atlasea(analysis.code, tolId, strTaxonId, scientificName);
						//logger.debug("creation et sauvegarde dans base de atlasea avec code " + atlasea.code);
						atlaseaDAO.saveObject(atlasea);
					}

					if(atlasea.endSubmitProjects) {
						//logger.debug("Projects deja ok pour " + atlasea.code + ", avec tolId = " + tolId);
						continue;
					}

					if(StringUtils.isBlank(atlasea.umbrellaAc)) {
						logger.debug("Appel de mainSubmitUbrella pour tolId=" + tolId);
						mainSubmitUmbrella(atlasea, ctx);
						if(ctx.hasErrors()) {
							//ctx.displayErrors(logger, "debug");
							addErreurCxtBInCtxA(globalCtx, ctx);
							continue;
						}
					} 
					//logger.debug("kkkkkkkkkkk   firstSubmissionDate=" + monUmbrellaTest.firstSubmissionDate);
					if(StringUtils.isBlank(atlasea.sequencingProjectAc)) {
						//logger.debug("Appel de mainSubmitSequencingStudy pour tolId=" + tolId);
						mainSubmitSequencingStudy(atlasea, ctx);
						if(ctx.hasErrors()) {
							//ctx.displayErrors(logger, "debug");
							addErreurCxtBInCtxA(globalCtx, ctx);
							continue;
						}
					}

					if(StringUtils.isBlank(atlasea.primaryAssemblyProjectAc)) {
						//logger.debug("Appel de mainSubmitPrimaryAssemblyStudy pour tolId=" + tolId);
						mainSubmitPrimaryAssemblyStudy(atlasea, ctx);
						if(ctx.hasErrors()) {
							//ctx.displayErrors(logger, "debug");
							addErreurCxtBInCtxA(globalCtx, ctx);
							continue;
						}
					}

					if(! atlasea.endSubmitProjects 
							&& ( StringUtils.isNotBlank(atlasea.umbrellaAc) 
									&& StringUtils.isNotBlank(atlasea.sequencingProjectAc)
									&& StringUtils.isNotBlank(atlasea.primaryAssemblyProjectAc))) {
						//logger.debug("Appel de mainSubmitUpdateUmbrella pour tolId=" + tolId);
						mainSubmitUpdateUmbrella(atlasea, ctx);
						if(ctx.hasErrors()) {
							//ctx.displayErrors(logger, "debug");
							addErreurCxtBInCtxA(globalCtx, ctx);
							continue;
						}
						// mettre à jour l'analyse pour primaryAssemblyProjectAc
						updateAnalysisForPrimaryAssemblyProjectAc(analysis, atlasea.primaryAssemblyProjectAc, ctx);
						if(ctx.hasErrors()) {
							//ctx.displayErrors(logger, "debug");
							addErreurCxtBInCtxA(globalCtx, ctx);
							continue;
						}
						//logger.debug("J'ai bien 3 AC pour le tolId = " + atlasea.tolId);
						SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
						//logger.debug("Dans atlasea, date = " + atlasea.date);
						//logger.debug("Dans atlasea, strDate=" + formatter.format(atlasea.date));
						String strDate = formatter.format(atlasea.date);
						//sb.append(String.format("%-40s|%-40s|%40s|%40s<br/>\n", tolId, atlasea.umbrellaCode, atlasea.sequencingProjectCode, atlasea.primaryAssemblyProjectCode));
						successBuilder.append(String.format("<tr><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>\n", 
								tolId, atlasea.scientificName, atlasea.taxonId, strDate, atlasea.umbrellaAc, atlasea.sequencingProjectAc, atlasea.primaryAssemblyProjectAc));
					}
				}
				// Envoie message succes creation projects:
				if ( StringUtils.isNotBlank(successBuilder.toString())) {
					sendSuccessMail(successBuilder);
					alreadySendSuccessMail = true;
				}
				// Envoie message erreurs creation projects:
				if ( globalCtx.hasErrors() ) {
					storeErrorMessage(globalCtx, errorBuilder);
					//logger.debug("\n\nDans globalCtx :");
					//globalCtx.displayErrors(logger, "debug");
					sendErrorMail(errorBuilder);
					alreadySendErrorMail = true;
				}
				Logger.debug("fin du traitement SubmissionAtlaseaProjectsToEbi");		
			} catch (RuntimeException e) {
				if(!alreadySendSuccessMail && StringUtils.isNotBlank(successBuilder.toString())) {
					sendSuccessMail(successBuilder);
				}
				if(!alreadySendErrorMail && globalCtx.hasErrors()) {
					sendErrorMail(errorBuilder);
				}
				throw new RuntimeException(e);  //preferable a throw e; car on a une trace de l'endroit ou c'est capture et on garde trace ou ca se produit initialement.
			}

		} catch(Exception e) {
			throw new RuntimeException(e);
		} 
	}


	public void logInfosEnvironnement(ContextValidation ctx) {
		try {
			logger.info("atlaseaRootResultDirectory=" + this.submissionAtlaseaRootResultDirectory);
			logger.info("ebiCurl=" + this.ebiCurl);
			logger.info("successSubject           = " + config.getCreationProjectsAtlaseaEmailSubject());
			logger.info("str_successDestinataires = " + config.getCreationProjectsAtlaseaEmailTo());
			logger.info("errorSubject             = " + config.getCreationProjectsAtlaseaEmailErrorSubject());
			logger.info("str_errorDestinataires   = " + config.getCreationProjectsAtlaseaEmailErrorTo());

		} catch(Exception e) {
			ctx.addError("Probleme variable dans application.conf : ", e.getMessage()); 
			return;
		}
		if( ! this.ebiCurl.equalsIgnoreCase(this.ebiCurlTest9) && 
				! this.ebiCurl.equalsIgnoreCase(ebiCurlTest261) &&
				! this.ebiCurl.equalsIgnoreCase(ebiCurlProd)) {
			ctx.addError("Probleme variable dans application.conf :", "ebiCurl=" +  this.ebiCurl + " non reconnu"); 
			return;
		} 
	}


	public void updateAnalysisForPrimaryAssemblyProjectAc(Analysis analysis, String primaryAssemblyProjectAc, ContextValidation ctx) {
		if (analysis.properties == null) {
			analysis.properties = new HashMap<String,PropertyValue>();
		}
		if ( !analysis.properties.containsKey("primaryAssemblyProjectAccession")) {
			analysis.properties.put("primaryAssemblyProjectAccession", new PropertySingleValue(primaryAssemblyProjectAc));
		} else {
			analysis.properties.put("primaryAssemblyProjectAccession", new PropertySingleValue(primaryAssemblyProjectAc));	
		}
		// pose probleme le 20 fevrier car RulesServices6 not intiailized, call initSingleton
		//			try {
		//				println("update de l'analyse "+ analysis.code + " pour primaryAssemblyProjectAccession='" + primaryAssemblyProjectAc +"'");
		//				app.apis().analyses().update(analysis, user);
		//			} catch (Exception e) {
		//				ctx.addError("analysis."+analysis.code, "impossible a mettre à jour pour propriete primaryAssemblyProjectAccession=" 
		//								+ primaryAssemblyProjectAc + ", message=" + e.getMessage());
		//				return;
		//			}
		biAnalysisAPI.dao_saveObject(analysis);
	}


	public void checkAlreadyAnalysisProcessed() {
		Iterable<Atlasea> itAtlasea = this.atlaseaAPI.dao_all();
		int count = 0;
		for (Atlasea atlasea : itAtlasea) {
			boolean toSave = false;
			String code = atlasea.code;
			if(code.startsWith("old_")) {
				continue;
			}
			String tolid = atlasea.tolId;
			String primaryAssemblyProjectAccession = atlasea.primaryAssemblyProjectAc;
			if(StringUtils.isBlank(tolid)) {
				continue;
			}
			if(! this.biAnalysisAPI.isObjectExist(code)) {
				logger.debug("Absence de l'analyse '{}'", code);
				continue;
			}
			// ajouter pour eviter erreur lors du this.biAnalysisAPI.getObject(code);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			//plusieurs fois erreurs sur biAnalysisAPI.getObject() lors des tests du 21/07/2025 et jamais sur meme code en absence du sleep.
			Analysis analysis = this.biAnalysisAPI.getObject(code);
			if (analysis.properties == null) {
				analysis.properties = new HashMap<String,PropertyValue>();
			}
			if ( StringUtils.isNotBlank(tolid) && ! analysis.properties.containsKey("tolid")) {
				logger.debug("Ajout du tolid '{}' dans l'analyse '{}'", tolid, code);
				analysis.properties.put("tolid", new PropertySingleValue(tolid));
				toSave = true;
			}

			if ( StringUtils.isNotBlank(primaryAssemblyProjectAccession) && !analysis.properties.containsKey("primaryAssemblyProjectAccession")) {
				logger.debug("Ajout du primaryAssemblyProjectAccession '{}' dans l'analyse '{}'", primaryAssemblyProjectAccession, code);
				analysis.properties.put("primaryAssemblyProjectAccession", new PropertySingleValue(primaryAssemblyProjectAccession));
				toSave = true;
			} 

			// pose probleme le 20 fevrier car RulesServices6 not intiailized, call initSingleton
			//			try {
			//				println("update de l'analyse "+ analysis.code + " pour primaryAssemblyProjectAccession='" + primaryAssemblyProjectAc +"'");
			//				app.apis().analyses().update(analysis, user);
			//			} catch (Exception e) {
			//				ctx.addError("analysis."+analysis.code, "impossible a mettre à jour pour propriete primaryAssemblyProjectAccession=" 
			//								+ primaryAssemblyProjectAc + ", message=" + e.getMessage());
			//				return;
			//			}
			if(toSave) {
				count++;
				this.biAnalysisAPI.dao_saveObject(analysis);
			}
		}
		logger.debug("Correction de '{}' analysis pour le tolid et/ou le primaryAssemblyAccession", count);
	}


	// Ecrit le fichier submission.xml en mode creation (si soumission en mode creation de study, samples, experiments ou runs)
	public void writePublicSubmissionCreationForStudyXml (Submission submission, File outputFile) throws IOException {
		if (submission == null) {
			return;
		}
		// ouvrir fichier en ecriture
		//logger.debug("Creation du fichier " + outputFile);
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
		sb.append("<SUBMISSION_SET>\n");
		sb.append("  <SUBMISSION alias=\"").append(submission.code).append("\" >\n");
		sb.append("    <CONTACTS>\n");
		sb.append("      <CONTACT name=\"william\" inform_on_status=\"william@genoscope.cns.fr\" inform_on_error=\"william@genoscope.cns.fr\"/>\n");
		sb.append("    </CONTACTS>\n");
		sb.append("    <ACTIONS>\n");
		sb.append("      <ACTION>\n");
		sb.append("        <RELEASE/>\n");// en mode CREATION, soumission systematique en confidential sauf si soumission project atlasea
		sb.append("      </ACTION>\n");

		if (StringUtils.isNotBlank(submission.studyCode)) {
			sb.append("      <ACTION>\n");
			sb.append("        <ADD source=\"project.xml\" schema=\"project\"/>\n");
			sb.append("      </ACTION>\n");
		}

		sb.append("    </ACTIONS>\n");		
		sb.append("  </SUBMISSION>\n");
		sb.append("</SUBMISSION_SET>\n");

		try (BufferedWriter output_buffer = new BufferedWriter(new java.io.FileWriter(outputFile))) {
			output_buffer.write(sb.toString());
		}
		submission.xmlSubmission = outputFile.getName();
	}


	public void activeAndWriteSubmissionUmbrella(Submission submission, ContextValidation ctx) {
		ctx.setUpdateMode();
		if(StringUtils.isNotBlank(submission.xmlSubmission)) {
			ctx.addError("submission." + submission.code, "champs xmlSubmission deja present ?");
			return;
		}
		if (StringUtils.isBlank(submission.submissionDirectory)) {
			ctx.addError("submission." + submission.code, "absence du repertoire de soumission 'submissionDirectory'");
			return;
		}
		// Creer le repertoire de soumission si besoin :
		File dataRep = new File(submission.submissionDirectory);
		if (! dataRep.exists()) {
			if (!dataRep.mkdirs()) {	
				//logger.debug("Impossible de creer le repertoire " + dataRep + " ");
				ctx.addError("submission." + submission.code, "impossible de creer le repertoire de soumission " + dataRep);
				return;
			}
		}
		File submissionFile = new File(dataRep , "submissionUmbrella.xml");
		//logger.debug("resultDirectory=" + dataRep);
		//logger.debug("fichier d'ecriture pour submissionFile=" + submissionFile.getName());
		try {
			xmlServices.writeSubmissionCreationXmlForUmbrella(submission, submissionFile);
		} catch (IOException e) {
			ctx.addError("submission." + submission.code, "impossible d'ecrire fichiers xmlSubmission, message=" + e.getMessage());
			return;
		}
		File umbrellaFile = new File(dataRep, "umbrella.xml");
		try {
			xmlServices.writeProjectUmbrellaXml(submission, umbrellaFile);
		} catch (IOException e) {
			ctx.addError("submission." + submission.code, "impossible d'ecrire fichiers xmlUmbrella, message=" + e.getMessage());
			return;
		}
		submission.ebiResult = "ebi_AC_" + submission.code + ".txt";
		// mettre à jour l'objet submission avec les champs xml et ebiResult:
		try {
			submissionAPI.update(submission, submission.traceInformation.createUser);
		} catch (APIException e) {
			ctx.addError("submission." + submission.code, e.getMessage());
			return;
		}
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

	//activeAndWriteSubmissionStudy(submission, "assemblySubmission.txt", "assemblyProject.txt", ctx);
	//activeAndWriteSubmissionStudy(submission, "sequencingSubmission.txt", "sequencingProject.txt", ctx);
	public void activeAndWriteSubmissionStudy(Submission submission, String submissionXmlShortName, String projectXmlShortName, ContextValidation ctx) {
		ctx.setUpdateMode();
		if(StringUtils.isNotBlank(submission.xmlSubmission)) {
			ctx.addError("submission." + submission.code, "champs xmlSubmission deja present ?");
			return;
		}
		if (StringUtils.isBlank(submission.submissionDirectory)) {
			ctx.addError("submission." + submission.code, "absence du repertoire de soumission 'submissionDirectory'");
			return;
		}
		//String resultDirectory = submission.submissionDirectory;
		//File resultDirectory = new File(submission.submissionDirectory);
		// Creer le repertoire de soumission si besoin :

		File dataRep = new File(submission.submissionDirectory);
		if (! dataRep.exists()) {
			if (!dataRep.mkdirs()) {	
				//logger.debug("Impossible de creer le repertoire " + dataRep + " ");
				ctx.addError("submission." + submission.code, "impossible de creer le repertoire de soumission " + dataRep);
				return;
			}
		}
		File submissionFile = new File(dataRep , submissionXmlShortName);
		//logger.debug("resultDirectory=" + dataRep);
		//logger.debug("fichier d'ecriture pour submissionFile=" + submissionFile.getName());
		try {
			writePublicSubmissionCreationForStudyXml(submission, submissionFile);
		} catch (IOException e) {
			ctx.addError("submission." + submission.code, "impossible d'ecrire fichiers xmlSubmission, message=" + e.getMessage());
			return;
		}
		File projectFile = new File(dataRep, projectXmlShortName);
		try {
			xmlServices.writeSequencingProjectXml(submission, projectFile);
		} catch (IOException e) {
			ctx.addError("submission." + submission.code, "impossible d'ecrire fichiers xmlUmbrella, message=" + e.getMessage());
			return;
		}
		submission.ebiResult = "ebi_AC_" + submission.code + ".txt";
		// mettre à jour l'objet submission avec les champs xml et ebiResult:
		try {
			submissionAPI.update(submission, submission.traceInformation.createUser);
		} catch (APIException e) {
			ctx.addError("submission." + submission.code, e.getMessage());
			return;
		}
	}



	public void buildUmbrellaIfNecessary(Atlasea atlasea, ContextValidation ctx) {		
		String tolId = atlasea.tolId;
		String umbrellaCode = "atlasea-" + tolId + "-study-umbrella"; 
		if(StringUtils.isNotBlank(atlasea.umbrellaCode)) {
			if(atlasea.umbrellaCode.equals(umbrellaCode)) {
				// umbrella deja en base et code stoqué dans atlasea, rien à faire
				return;
			} else {
				// atlasea.umbrellaCode renseigne mais pas avec valeur attendue :
				ctx.addError("Error", "atlasea " + atlasea.code + 
						" avec umbrellaCode=" + atlasea.umbrellaCode + " different de l'umbrellaCode attendu : " + umbrellaCode);
			}
		}
		// si umbrella existe deja en base mais qu'il n'est pas dans atlasea, 
		// alors recuperer umbrella et stoquer son code dans atlasea :
		if(projectAPI.isObjectExist(umbrellaCode)) {
			atlasea.umbrellaCode = umbrellaCode;
			//atlaseaDAO.update(atlasea);
			//MongoDBDAO.update(InstanceConstants.SRA_ATLASEA_COLL_NAME, atlasea);
			atlaseaDAO.update(DBQuery.is("code", atlasea.code),
					DBUpdate.set("umbrellaCode", umbrellaCode));
			//logger.debug("le project " + umbrellaCode + " existe deja en base");
			return;
		} 

		// construction du project et stoquer dans atlasea :
		//logger.debug("appel de buildUmbrella");
		buildUmbrella(atlasea, umbrellaCode, ctx);
	}


	public void buildUmbrella(Atlasea atlasea, String umbrellaCode, ContextValidation ctx) {
		//logger.debug("construction en base de l'umbrella, dans buildUmbrella");
		String taxonId = atlasea.taxonId;
		String scientificName = atlasea.scientificName;
		String description = "This project collects the sequencing data and assemblies generated for " + scientificName + 
				" by the ATLASea programme (https://www.atlasea.fr)";
		String title = scientificName;	

		List<String >parentProjectAccessions = new ArrayList<>(Arrays.asList(this.superParentUmbrella));
		List<String> childrenProjectAccessions = null;
		List<String> idsPubmed = null;
		String attributes = "";
		ctx.setCreationMode();
		Project umbrellaProject = submissionNewAPI.createUmbrellaEntity(title, description,  this.dropBox, 
				taxonId, attributes, idsPubmed, 
				childrenProjectAccessions, parentProjectAccessions, ctx);

		// Ecraser le studyCode et le status, et voir à terme pour center_name (GSC ou Genoscope CEA)
		umbrellaProject.code = umbrellaCode;
		umbrellaProject.traceInformation.setTraceInformation(this.user);
		umbrellaProject.state = new State(SUB_SMD_IP, this.user);
		umbrellaProject.validate(ctx); // verifie que le code de l'umbrella n'existe pas en base
		if (ctx.hasErrors()) {
			logger.debug("\ndisplayErrors dans CreateProjectsAtlasea::buildAtlaseaUmbrella :");
			ctx.displayErrors(logger, "debug");
			logger.debug("\n end displayErrors");
			return;
		} 
		// sauver en base le project et l'information du umbrellaCode dans atlasea :
		projectAPI.dao_saveObject(umbrellaProject);
		atlasea.umbrellaCode = umbrellaProject.code;
		atlaseaDAO.update(DBQuery.is("code", atlasea.code),
				DBUpdate.set("umbrellaCode", umbrellaProject.code));
		//logger.debug("sortie de buildUmbrella");

	}




	public void buildUmbrellaUpdate(Atlasea atlasea, ContextValidation ctx) {
		// mise à jour de l'umbrella dans base
		//Date courantDate = new Date();
		Project umbrella = projectAPI.get(atlasea.umbrellaCode);
		if(umbrella.childrenProjectAccessions == null ) { 
			umbrella.childrenProjectAccessions = new ArrayList<String>();
		}
		if(! umbrella.childrenProjectAccessions.contains(atlasea.sequencingProjectAc)) {
			umbrella.childrenProjectAccessions.add(atlasea.sequencingProjectAc);
		}
		if(! umbrella.childrenProjectAccessions.contains(atlasea.primaryAssemblyProjectAc)) {
			umbrella.childrenProjectAccessions.add(atlasea.primaryAssemblyProjectAc);
		}
		State nextState = umbrella.state;
		nextState.code = SUBU_SMD_IP;
		nextState.user = user;
		//umbrella.setTraceUpdateStamp(ctx.getUser());
		umbrella.updateStateAndTrace(nextState);
		ctx.setUpdateMode();
		umbrella.validate(ctx);
		if(ctx.hasErrors()) {
			return;
		}
		try {
			projectAPI.update(umbrella, this.user);
		} catch (APIException e) {
			ctx.addError("Error", e.getMessage());
			return;
		}
		// Pas de mise à jour de atlasea à ce stade
	}


	public void buildUmbrellaUpdateSubmission(Atlasea atlasea, String submissionCode, ContextValidation ctx) {
		String umbrellaCode = atlasea.umbrellaCode;
		if (StringUtils.isNotBlank(atlasea.umbrellaUpdateSubmissionCode)) { 
			throw new SraException("buildAtlaseaUmbrellaUpdateSubmission::atlasea.umbrellaUpdateSubmissionCode deja existant ? pour " + atlasea.code);
		}
		String tolId = atlasea.tolId;
		Date courantDate = new java.util.Date();
		Submission submission = new Submission(user);
		submission.code = submissionCode;
		submission.traceInformation.creationDate = courantDate;
		submission.state = new State(SUB_SMD_IP, user);
		submission.type = Submission.Type.UPDATE;
		submission.typeRawDataSubmitted = TypeRawDataSubmitted.withoutRawData;
		submission.dropBox = dropBox;
		submission.submissionDirectory = this.submissionAtlaseaRootResultDirectory + tolId + "/";	
		submission.ebiResult = "ebi_AC_" + submission.code + ".txt";
		submission.umbrellaCode = umbrellaCode;
		ctx.setCreationMode();
		submission.validate(ctx);
		if (ctx.hasErrors()) {
			return;
		} 
		submissionAPI.dao_saveObject(submission);
		atlasea.umbrellaUpdateSubmissionCode = submission.code;
		//atlaseaDAO.update(atlasea);
		atlaseaDAO.update(DBQuery.is("code", atlasea.code),
				DBUpdate.set("umbrellaUpdateSubmissionCode", submission.code));

	}

	public void buildUmbrellaSubmissionUpdateIfNecessary(Atlasea atlasea, ContextValidation ctx) {
		String tolId = atlasea.tolId;
		String submissionCode = "submission_atlasea_"+ tolId + "_updateUmbrella";

		if(StringUtils.isNotBlank(atlasea.umbrellaUpdateSubmissionCode)) {
			if(atlasea.umbrellaUpdateSubmissionCode.equals(submissionCode)) {
				// umbrellaUpdateSubmissionCode deja en base et code stoqué dans atlasea, rien à faire
				return;
			} else {
				// atlasea.umbrellaUpdateSubmissionCode renseigne mais pas avec valeur attendue :
				ctx.addError("Error", "atlasea " + atlasea.code + 
						" avec umbrellaUpdateSubmissionCode=" + atlasea.umbrellaUpdateSubmissionCode + " different de l'umbrellaUpdateSubmissionCode attendu : " + submissionCode);
			}
		}
		// si umbrellaSubmission existe deja en base mais qu'il n'est pas dans atlasea, 
		// alors recuperer umbrellaSubmission et stoquer son code dans atlasea :
		if(submissionAPI.isObjectExist(submissionCode)) {
			atlasea.umbrellaUpdateSubmissionCode = submissionCode;
			atlaseaDAO.update(DBQuery.is("code", atlasea.code),
					DBUpdate.set("umbrellaUpdateSubmissionCode", submissionCode));
			return;
		} 
		// construction de la soumission pour creation umbrella :
		buildUmbrellaUpdateSubmission(atlasea, submissionCode, ctx);
	}


	public void buildUmbrellaSubmission(Atlasea atlasea, String submissionCode, ContextValidation ctx) {
		String tolId = atlasea.tolId;
		Date courantDate = new java.util.Date();

		Submission submission = new Submission(user);

		submission.code = submissionCode;
		submission.traceInformation.creationDate = courantDate;
		submission.state = new State(SUB_SMD_IP, user);
		submission.type = Submission.Type.CREATION;
		submission.typeRawDataSubmitted = TypeRawDataSubmitted.withoutRawData;
		submission.dropBox = dropBox;
		submission.submissionDirectory = this.submissionAtlaseaRootResultDirectory + tolId + "/"; 	
		submission.ebiResult = "ebi_AC_" + submission.code + ".txt";
		submission.umbrellaCode = atlasea.umbrellaCode;

		ctx.setCreationMode();
		submission.validate(ctx);
		if (ctx.hasErrors()) {
			return;
		} 
		submissionAPI.dao_saveObject(submission);
		atlasea.umbrellaSubmissionCode = submission.code;
		atlaseaDAO.update(DBQuery.is("code", atlasea.code),
				DBUpdate.set("umbrellaSubmissionCode", submission.code));

	}


	public void buildUmbrellaSubmissionIfNecessary(Atlasea atlasea, ContextValidation ctx) {
		String tolId = atlasea.tolId;
		String submissionCode = "submission_atlasea_"+ tolId + "_umbrella";		

		if(StringUtils.isNotBlank(atlasea.umbrellaSubmissionCode)) {
			if(atlasea.umbrellaSubmissionCode.equals(submissionCode)) {
				// umbrellaSubmissionCode deja en base et code stoqué dans atlasea, rien à faire
				return;
			} else {
				// atlasea.umbrellaSubmissionCode renseigne mais pas avec valeur attendue :
				ctx.addError("Error", "atlasea " + atlasea.code + 
						" avec umbrellaSubmissionCode=" + atlasea.umbrellaSubmissionCode + " different de l'umbrellaSubmissionCode attendu : " + submissionCode);
			}
		}
		// si umbrellaSubmission existe deja en base mais qu'il n'est pas dans atlasea, 
		// alors recuperer umbrellaSubmission et stoquer son code dans atlasea :
		if(submissionAPI.isObjectExist(submissionCode)) {
			atlasea.umbrellaSubmissionCode = submissionCode;
			atlaseaDAO.update(DBQuery.is("code", atlasea.code),
					DBUpdate.set("umbrellaSubmissionCode", submissionCode));
			return;
		} 
		// construction de la soumission pour creation umbrella :
		if(StringUtils.isBlank(atlasea.umbrellaCode)) {
			ctx.addError("Error", "atlasea " + atlasea.code + 
					" avec umbrellaCode null incompatible avec appel de buildUmbrellaSubmission");
		}
		//logger.debug("appel de buildUmbrellaSubmission avec submissionCode=" + submissionCode);
		buildUmbrellaSubmission(atlasea, submissionCode, ctx);
	}


	public void loadReportSubmissionForCreationProject(Submission submission, Project project, ContextValidation ctxVal) {
		submission.state.code = "SUB-SMD-F";
		boolean sendMail = true;
		boolean confidential = false;
		File ebiReport = new File(submission.submissionDirectory, submission.ebiResult);
		submissionNewAPI.loadRespEbiForCreation(ctxVal, submission, ebiReport.getAbsolutePath(), sendMail, confidential);	
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


	// main pour la construction de l'umbrella, l'ecriture des fichiers xml et l'envoie curl et recuperation des AC
	public void mainSubmitUmbrella(Atlasea atlasea, ContextValidation ctx) {
		// Construction du project umbrella si besoin et stoker code dans atlasea
		if(StringUtils.isBlank(atlasea.umbrellaCode)) {
			//logger.debug("appel de buildUmbrellaIfNecessary");
			buildUmbrellaIfNecessary(atlasea, ctx);
			if(ctx.hasErrors()) {
				//logger.debug("Erreurs dans buildAtlaseaUmbrella");
				return;
			}
		}
		// Construction de la soumission pour umbrella et stoker code dans atlasea
		if(StringUtils.isBlank(atlasea.umbrellaSubmissionCode)) {
			//logger.debug("appel de buildAtlaseaUmbrellaSubmission");
			buildUmbrellaSubmissionIfNecessary(atlasea, ctx);
			if(ctx.hasErrors()) {
				//logger.debug("Erreurs dans buildAtlaseaUmbrellaSubmission");
				return;
			}
		}

		Submission umbrellaSubmission = submissionAPI.get(atlasea.umbrellaSubmissionCode);
		Project umbrella = projectAPI.get(atlasea.umbrellaCode);
		// Ecrire fichiers xml dans repertoire de soumission et sauver objets dans bon etat
		if(StringUtils.isBlank(umbrellaSubmission.xmlSubmission)) {
			// Ecrire fichiers pour soumission umbrella:
			//logger.debug("appel de ActiveAndWriteSubmissionUmbrella");
			activeAndWriteSubmissionUmbrella(umbrellaSubmission, ctx);
			if(ctx.hasErrors()) {
				//logger.debug("Erreurs dans activeAndWriteSubmissionUmbrella");
				//			logger.debug("dans execute debut displayError");
				//			ctx.displayErrors(logger, "debug");
				//			logger.debug("dans execute fin displayError");
				//logger.debug("Recuperation message erreur pour mail :");
				return;
			}
		}
		// Envoie des fichiers xml et recuperation du fichier ebiRetour : 
		File ebiRetour = new File(umbrellaSubmission.submissionDirectory, umbrellaSubmission.ebiResult);
		//logger.debug("fichier reporting ebi = " + ebiRetour.getAbsolutePath());
		if( ! ebiRetour.exists()) {
			//logger.debug("appel de curl");
			submissionNewAPI.ebiCurl(umbrellaSubmission, ctx);
			//logger.debug("apres appel de curl");
			if(ctx.hasErrors()) {
				//logger.debug("Erreurs dans ebiCurl");
				return;
			}
		}
		// parsing du fichier retour de l'EBI et mise à jour dans base pour les AC des objets submission et project ou study 
		// inutile de faire des cascades sur releaseDate ou autre car public et aucune données associée.
		if(ebiRetour.exists()) {
			//logger.debug("appel de loadReportSubmissionForCreationProject");
			// charge rapport de soumission et met à jour dans base, la soumission et le project pour les numeros d'accession.
			loadReportSubmissionForCreationProject(umbrellaSubmission, umbrella, ctx);
			//!!!! attention :  après le chargement du rapport de soumission, il faut aller rechercher l'umbrella en base pour avoir, l'AC sur le project : 
			umbrella = projectAPI.get(atlasea.umbrellaCode);
			if(ctx.hasErrors()) {
				//logger.debug("Erreurs dans loadReportSubmissionForCreationProject pour la soumission " + umbrellaSubmission.code);
				return;
			}

			//logger.debug("dans mainSubmitUmbrella, pour project." + umbrella.code+ " AC=" + umbrella.accession + ", firstSubmissionDate=" + umbrella.firstSubmissionDate);
			//logger.debug("dans mainSubmitUmbrella, pour submission." + umbrellaSubmission.code+ " AC=" + umbrellaSubmission.accession);
			//logger.debug("dans mainSubmitUmbrella, pour submission." + umbrellaSubmission.code+ " AC=" + umbrellaSubmission.accession);

			// Mise a jour de atlasea pour AC umbrella et submissionUmbrella :
			atlasea.umbrellaSubmissionAc = umbrellaSubmission.accession;
			atlasea.umbrellaAc = umbrella.accession;
			atlaseaDAO.update(DBQuery.is("code", atlasea.code),
					DBUpdate.set("umbrellaSubmissionAc", umbrellaSubmission.accession)
					.set("umbrellaAc"                  , umbrella.accession));
			//.set("date"                        , umbrella.firstSubmissionDate));			
		} else {
			logger.debug("dans mainSubmitUmbrella, pour project." + umbrella.code + " absence du fichier retour " + ebiRetour.getAbsolutePath() + ebiRetour.getName());
		}
		//logger.debug("Fin du traitement de l'umbrella mais il manque l'update final pour ajouter les enfants");
	}	


	// main pour l'update à l'EBI de l'umbrella, l'ecriture des fichiers xml et l'envoie curl et recuperation des AC
	public void mainSubmitUpdateUmbrella(Atlasea atlasea, ContextValidation ctx) {
		if( (StringUtils.isBlank(atlasea.sequencingProjectAc)) || (StringUtils.isBlank(atlasea.primaryAssemblyProjectAc)) ) {
			ctx.addError("umbrella." + atlasea.umbrellaCode, "impossible d'updater l'umbrella en absence des AC des projects de sequencage ou assemblage");
			return;
		}
		if(! projectAPI.isObjectExist(atlasea.umbrellaCode)) {
			ctx.addError("umbrella." + atlasea.umbrellaCode, "impossible a updater car absent de la base");
			return;
		}
		buildUmbrellaUpdate(atlasea, ctx);

		// Construction de la soumission pour update umbrella et stoker code dans atlasea
		if(StringUtils.isBlank(atlasea.umbrellaUpdateSubmissionCode)) {
			//logger.debug("appel de buildAtlaseaUmbrellaUpdateSubmission");
			buildUmbrellaSubmissionUpdateIfNecessary(atlasea, ctx);
			if(ctx.hasErrors()) {
				//logger.debug("Erreurs dans umbrellaUpdateSubmissionCode");
				return;
			}
		}

		Submission umbrellaUpdateSubmission = submissionAPI.get(atlasea.umbrellaUpdateSubmissionCode);
		Project umbrella = projectAPI.get(atlasea.umbrellaCode);
		//logger.debug("kkkkkkkkkkkk   Dans mainSubmiUpdateUmbrella, umbrella.firstSubmissionDate=" + umbrella.firstSubmissionDate);
		// Ecrire fichiers xml dans repertoire de soumission et sauver objets dans bon etat
		if(StringUtils.isBlank(umbrellaUpdateSubmission.xmlSubmission)) {
			// Ecrire fichiers pour soumission umbrella:
			//logger.debug("appel de activeAndWriteSubmissionUpdateUmbrella");
			activeAndWriteSubmissionUpdateUmbrella(umbrellaUpdateSubmission, ctx);
			if(ctx.hasErrors()) {
				//logger.debug("Erreurs dans activeAndWriteSubmissionUpdateUmbrella");
				return;
			}
		}
		// Envoie des fichiers xml et recuperation du fichier ebiRetour : 
		File ebiRetour = new File(umbrellaUpdateSubmission.submissionDirectory, umbrellaUpdateSubmission.ebiResult);
		//logger.debug("fichier reporting ebi = " + ebiRetour.getAbsolutePath());
		if( ! ebiRetour.exists()) {
			//logger.debug("appel de curl");
			submissionNewAPI.ebiCurl(umbrellaUpdateSubmission, ctx);
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
			loadReportSubmissionForUpdateProject(umbrellaUpdateSubmission, ctx);
			// Après chargement rapport de soumission, aller rechercher umbrella dans base pour avoir données de soumissions à jour sur l'umbrella:
			umbrella = projectAPI.get(atlasea.umbrellaCode);
			if(ctx.hasErrors()) {
				logger.debug("Erreurs dans loadReportSubmissionForUpdateProject pour la soumission " + umbrellaUpdateSubmission.code);
				return;
			}
			//logger.debug("dans mainSubmitUpdateUmbrella, pour project." + umbrella.code+ " AC=" + umbrella.accession);
			//logger.debug("dans mainSubmitUpdateUmbrella, aucun AC pour " + umbrellaUpdateSubmission.code);

			//logger.debug("fffffffffffffffffffffff    date de premiere soumission de l'umbrella = "+ umbrella.firstSubmissionDate);
			atlasea.endSubmitProjects = true;
			atlasea.date = umbrella.firstSubmissionDate;
			atlaseaDAO.update(DBQuery.is("code", atlasea.code),
					DBUpdate.set("endSubmitProjects", atlasea.endSubmitProjects)
					.set("date", umbrella.firstSubmissionDate));
			// Mise a jour de atlasea pour endSubmitProjects :(dans le cas d'un update pas d'AC pour la soumission)
			atlasea.endSubmitProjects = true;

			//logger.debug("dans mainSubmitUpdateUmbrella, pour atlasea." + atlasea.code + " atlasea.endSubmitProjects=" + atlasea.endSubmitProjects);

		}
		logger.debug("Pour " + atlasea.tolId + ", fin du traitement de l'umbrella avec ses 2 projects enfants");
	}	




	// main pour la construction du sequencing Study, l'ecriture des fichiers xml et l'envoie curl et recuperation des AC
	public void mainSubmitSequencingStudy(Atlasea atlasea, ContextValidation ctx) {
		// Construction du project sequencing si besoin et stoker code dans atlasea
		if(StringUtils.isBlank(atlasea.sequencingProjectCode)) {
			//logger.debug("appel de buildAtlaseaSequencingStudy");
			builSequencingStudyIfNecessary(atlasea, ctx);
			if(ctx.hasErrors()) {
				//logger.debug("Erreurs dans buildAtlaseaSequencingStudy");
				return;
			}
		}
		// Construction de la soumission pour project sequencing et stoker code dans atlasea
		if(StringUtils.isBlank(atlasea.sequencingSubmissionCode)) {
			//logger.debug("appel de buildAtlaseaSequencingSubmission");
			buildSubmissionForSequencingStudyIfNecessary(atlasea, ctx);
			if(ctx.hasErrors()) {
				//logger.debug("Erreurs dans buildAtlaseaSequencingSubmission");
				return;
			}
		}

		Submission sequencingSubmission = submissionAPI.get(atlasea.sequencingSubmissionCode);
		Study study = (Study) abstractStudyAPI.get(atlasea.sequencingProjectCode);
		String tolId = atlasea.tolId;
		// Ecrire fichiers xml dans repertoire de soumission et sauver objets dans bon etat
		if(StringUtils.isBlank(sequencingSubmission.xmlSubmission)) {
			// Ecrire fichiers pour soumission du project sequencing:
			String resultDirectory = this.submissionAtlaseaRootResultDirectory + tolId + File.separator;
			sequencingSubmission.submissionDirectory = resultDirectory;
			//logger.debug("appel de activeAndWriteSubmissionStudy");
			String submissionXmlShortName = "sequencingSubmission.xml";
			String projectXmlShortName = "sequencingProject.xml";
			activeAndWriteSubmissionStudy(sequencingSubmission, submissionXmlShortName, projectXmlShortName, ctx);
			if(ctx.hasErrors()) {
				//logger.debug("Erreurs dans activeAndWriteSubmissionStudy");
				return;
			}
		}
		// Envoie des fichiers xml et recuperation du fichier ebiRetour : 
		File ebiRetour = new File(sequencingSubmission.submissionDirectory, sequencingSubmission.ebiResult);
		//logger.debug("fichier reporting ebi = " + ebiRetour.getAbsolutePath());
		if( ! ebiRetour.exists()) {
			//logger.debug("appel de curl");
			submissionNewAPI.ebiCurl(sequencingSubmission, ctx);
			//logger.debug("apres appel de curl");
			if(ctx.hasErrors()) {
				return;
			}
		}

		// parsing du fichier retour de l'EBI et mise à jour dans base pour les AC des objets submission et project ou study 
		// inutile de faire des cascades sur releaseDate ou autre car public et aucune données associée.
		if(ebiRetour.exists()) {
			//logger.debug("appel de loadReportSubmissionForCreationStudy");
			// charge rapport de soumission et met à jour dans base, la soumission et le project pour les numeros d'accession.
			loadReportSubmissionForCreationStudy(sequencingSubmission, study, ctx);
			// !!! attention, après le chargement du rapport de soumission, il faut aller rechercher le study en base pour avoir son AC : 
			study = (Study) abstractStudyAPI.get(atlasea.sequencingProjectCode);
			if(ctx.hasErrors()) {
				//logger.debug("Erreurs dans loadReportSubmissionForCreationStudy pour la soumission " + sequencingSubmission.code);
				return;
			}
			//logger.debug("dans mainSubmitSequencingStudy, pour study." + study.code+ " externalID=" + study.externalId);
			//logger.debug("dans mainSubmitSequencingStudy, pour submission." + sequencingSubmission.code+ " AC=" + sequencingSubmission.accession);

			// Mise a jour de atlasea pour AC du project sequencing et submission :
			atlasea.sequencingSubmissionAc = sequencingSubmission.accession;
			atlasea.sequencingProjectAc = study.externalId;
			atlaseaDAO.update(DBQuery.is("code", atlasea.code),
					DBUpdate.set("sequencingSubmissionAc", sequencingSubmission.accession)
					.set("sequencingProjectAc",    study.externalId));
		}
		//logger.debug("Fin du traitement du project sequencing ");		
	}	


	public void buildSubmissionForPrimaryAssemblyStudyIfNecessary(Atlasea atlasea, ContextValidation ctx) {
		String tolId = atlasea.tolId;
		String submissionCode = "submission_atlasea_"+ tolId + "_studyPrimaryAssembly";

		if(StringUtils.isNotBlank(atlasea.primaryAssemblySubmissionCode)) {
			if(atlasea.primaryAssemblySubmissionCode.equals(submissionCode)) {
				// primaryAssemblySubmissionCode deja en base et code stoqué dans atlasea, rien à faire
				return;
			} else {
				// atlasea.primaryAssemblySubmissionCode renseigne mais pas avec valeur attendue :
				ctx.addError("Error", "atlasea " + atlasea.code + 
						" avec primaryAssemblySubmissionCode=" + atlasea.primaryAssemblySubmissionCode + " different du primaryAssemblySubmissionCode : " + submissionCode);
			}
		}
		// si primaryAssemblySubmissionCode existe deja en base mais qu'il n'est pas dans atlasea, 
		// alors recuperer primaryAssemblySubmissionCode et stoquer son code dans atlasea :
		if(submissionAPI.isObjectExist(submissionCode)) {
			atlasea.umbrellaSubmissionCode = submissionCode;
			atlaseaDAO.update(DBQuery.is("code", atlasea.code),
					DBUpdate.set("primaryAssemblySubmissionCode", submissionCode));
			return;
		} 
		buildSubmissionForPrimaryAssemblyStudy(atlasea, submissionCode, ctx);
	}


	// main pour la construction du study assembly, l'ecriture des fichiers xml et l'envoie curl et recuperation des AC
	public void mainSubmitPrimaryAssemblyStudy(Atlasea atlasea, ContextValidation ctx) {
		// Construction du study assembly si besoin et stoker code dans atlasea

		if(StringUtils.isBlank(atlasea.primaryAssemblyProjectCode)) {
			// verifier en fonction d'une propertie de l'analyse comment construire code du project assembly
			// avec primary ou alternate ou autre 
			//logger.debug("appel de buildAtlaseaPrimaryAssemblyStudy");
			builPrimaryAssemblyStudyIfNecessary(atlasea, ctx);
			if(ctx.hasErrors()) {
				//logger.debug("Erreurs dans buildAtlaseaPrimaryAssemblyStudy");
				return;
			}
		}
		// Construction de la soumission pour project assembly et stoker code dans atlasea
		if(StringUtils.isBlank(atlasea.primaryAssemblySubmissionCode)) {
			//verifier en fonction d'une propertie de l'analyse comment construire code de la soumission du 
			// project assembly
			//logger.debug("appel de buildAtlaseaPrimaryAssemblySubmission");
			buildSubmissionForPrimaryAssemblyStudyIfNecessary(atlasea, ctx);
			if(ctx.hasErrors()) {
				//logger.debug("Erreurs dans buildAtlaseaPrimaryAssemblySubmission");
				return;
			}
		}
		Submission assemblySubmission = submissionAPI.get(atlasea.primaryAssemblySubmissionCode);
		Study study = (Study) abstractStudyAPI.get(atlasea.primaryAssemblyProjectCode);

		String tolId = atlasea.tolId;
		// Ecrire fichiers xml dans repertoire de soumission et sauver objets dans bon etat
		if(StringUtils.isBlank(assemblySubmission.xmlSubmission)) {
			// Ecrire fichiers pour soumission assembly:
			String resultDirectory = this.submissionAtlaseaRootResultDirectory + tolId + File.separator;
			assemblySubmission.submissionDirectory = resultDirectory;
			//logger.debug("appel de activeAndWriteSubmissionStudy");
			String submissionXmlShortName = "primaryAssemblySubmission.xml";
			String projectXmlShortName = "primaryAssemblyProject.xml";
			activeAndWriteSubmissionStudy(assemblySubmission, submissionXmlShortName, projectXmlShortName, ctx);
			if(ctx.hasErrors()) {
				//logger.debug("Erreurs dans activeAndWriteSubmissionStudy");
				return;
			}
		}
		// Envoie des fichiers xml et recuperation du fichier ebiRetour : 
		File ebiRetour = new File(assemblySubmission.submissionDirectory, assemblySubmission.ebiResult);
		//logger.debug("fichier reporting ebi = " + ebiRetour.getAbsolutePath());
		if( ! ebiRetour.exists()) {
			//logger.debug("appel de curl");
			submissionNewAPI.ebiCurl(assemblySubmission, ctx);
			//logger.debug("apres appel de curl");
			if(ctx.hasErrors()) {
				return;
			}
		}

		// parsing du fichier retour de l'EBI et mise à jour dans base pour les AC des objets submission et project ou study 
		// inutile de faire des cascades sur releaseDate ou autre car public et aucune données associée.
		if(ebiRetour.exists()) {
			// charge rapport de soumission et met à jour dans base, la soumission et le project pour les numeros d'accession.
			loadReportSubmissionForCreationStudy(assemblySubmission, study, ctx);
			// après le chargement du rapport de soumission, il faut aller rechercher assemblySubmission en base pour avoir, l'AC  : 
			study = (Study) abstractStudyAPI.get(atlasea.primaryAssemblyProjectCode);
			if(ctx.hasErrors()) {
				//logger.debug("Erreurs dans loadReportSubmissionForCreationStudy pour la soumission " + assemblySubmission.code);
				return;
			}

			// Mise a jour de atlasea pour AC study assembly et submissionAssembly :
			atlasea.primaryAssemblySubmissionAc = assemblySubmission.accession;
			atlasea.primaryAssemblyProjectAc = study.externalId;
			atlaseaDAO.update(DBQuery.is("code", atlasea.code),
					DBUpdate.set("primaryAssemblySubmissionAc", assemblySubmission.accession)
					.set("primaryAssemblyProjectAc",    study.externalId));
		}
		//logger.debug("Fin du traitement du project assembly ");
	}	


	public void builSequencingStudyIfNecessary(Atlasea atlasea, ContextValidation ctx) {		
		String tolId = atlasea.tolId;
		String code  = "atlasea-" + tolId + "-study-rawdata";
		if(StringUtils.isNotBlank(atlasea.sequencingProjectCode)) {
			if(atlasea.sequencingProjectCode.equals(code)) {
				// sequencingProjectCode deja en base et code stoqué dans atlasea, rien à faire
				return;
			} else {
				// atlasea.sequencingProjectCode renseigne mais pas avec valeur attendue :
				ctx.addError("Error", "atlasea " + atlasea.code + 
						" avec sequencingProjectCode=" + atlasea.sequencingProjectCode + " different du sequencingProjectCode attendu : " + code);
			}
		}
		// si sequencingProjectCode existe deja en base mais qu'il n'est pas dans atlasea, 
		// alors recuperer sequencingProjectCode et stoquer son code dans atlasea :
		if(abstractStudyAPI.isObjectExist(code)) {
			atlasea.sequencingProjectCode = code;
			atlaseaDAO.update(DBQuery.is("code", atlasea.code),
					DBUpdate.set("sequencingProjectCode", code));
			return;
		} 

		// construction du project et stoquer dans atlasea :
		builSequencingStudy(atlasea, code, ctx);
	}


	public void builSequencingStudy(Atlasea atlasea, String code, ContextValidation ctx) {
		String tolId = atlasea.tolId;
		String scientificName = atlasea.scientificName;
		ctx.setCreationMode();
		if (StringUtils.isBlank(atlasea.umbrellaAc)) {
			ctx.addError("atlasea.code", "absence du champs umbrellaAc (projet parent) qui interdit construction du sequencingStudy");
			return;
		}
		String parentProject = atlasea.umbrellaAc;
		String centerProjectName = tolId;
		String title = scientificName + ", genomic and transcriptomic data";
		String description = "This project collects the genomic and transcriptomic generated for " + scientificName + 
				" to facilitate genome assembly and annotation as part of the ATLASea programme (https://www.atlasea.fr)";
		String dropBox = this.dropBox;

		Study sequencingStudy = submissionNewAPI.createStudyEntity(this.projectAtlaseaCodes, 
				this.existingStudyType, 
				centerProjectName, 
				title, 
				description, 
				dropBox, 
				null, 
				null, 
				null, 
				parentProject, 
				null, 
				ctx);
		// Ecraser le studyCode et le status
		sequencingStudy.code = code;
		sequencingStudy.state = new State(SUB_SMD_IP, user);
		// verifier validite en création => verifie que le code n'existe pas deja dans base.
		sequencingStudy.validate(ctx);
		if (ctx.hasErrors()) {
			return;
		}
		abstractStudyAPI.dao_saveObject(sequencingStudy);
		atlasea.sequencingProjectCode = sequencingStudy.code;
		atlaseaDAO.update(atlasea);
	}

	public void builPrimaryAssemblyStudyIfNecessary(Atlasea atlasea, ContextValidation ctx) {		
		String tolId = atlasea.tolId;
		String code = "atlasea-" + tolId + "-study-primary-assembly";
		if(StringUtils.isNotBlank(atlasea.primaryAssemblyProjectCode)) {
			if(atlasea.primaryAssemblyProjectCode.equals(code)) {
				// primaryAssemblyProjectCode deja en base et code stoqué dans atlasea, rien à faire
				return;
			} else {
				// atlasea.primaryAssemblyProjectCode renseigne mais pas avec valeur attendue :
				ctx.addError("Error", "atlasea " + atlasea.code + 
						" avec primaryAssemblyProjectCode=" + atlasea.primaryAssemblyProjectCode + " different du primaryAssemblyProjectCode attendu : " + code);
			}
		}
		// si primaryAssemblyProjectCode existe deja en base mais qu'il n'est pas dans atlasea, 
		// alors recuperer primaryAssemblyProjectCode et stoquer son code dans atlasea :
		if(abstractStudyAPI.isObjectExist(code)) {
			atlasea.primaryAssemblyProjectCode = code;
			atlaseaDAO.update(DBQuery.is("code", atlasea.code),
					DBUpdate.set("primaryAssemblyProjectCode", code));
			return;
		} 

		// construction du project et stoquer dans atlasea :
		builPrimaryAssemblyStudy(atlasea, code, ctx);
	}

	public void builPrimaryAssemblyStudy(Atlasea atlasea, String code, ContextValidation ctx) {
		String tolId = atlasea.tolId;
		String scientificName = atlasea.scientificName;
		ctx.setCreationMode();
		if (StringUtils.isBlank(atlasea.umbrellaAc)) {
			ctx.addError("atlasea.code", "absence du champs umbrellaAc (projet parent) qui interdit construction du assemblyProject");
			return;
		}

		String parentProject = atlasea.umbrellaAc;
		// ex code : atlasea-xbVenVerr_primary-2024-07-17
		// mieux de passer par methode de service car on garde centralisation de la construction du Study :
		String centerProjectName = tolId;
		// ex de titre : Venus verrucosa, genome assembly, xbVenVerr
		String title = scientificName + ", genomic assembly, " + tolId;
		String description = "This project provides the genome assembly of " + scientificName + 
				". The assembly is provided by the ATLASea programme (https://www.atlasea.fr)";
		String dropBox = this.dropBox;
		Study assemblyStudy =submissionNewAPI.createStudyEntity(this.projectAtlaseaCodes, 
				this.existingStudyType, 
				centerProjectName, 
				title, 
				description, 
				dropBox, 
				null, 
				null, 
				null, 
				parentProject, 
				null, 
				ctx);
		// Ecraser le studyCode et le status
		assemblyStudy.code = code;
		assemblyStudy.state = new State(SUB_SMD_IP, user);
		// verifier validite en création => verifie que le code n'existe pas deja dans base.
		assemblyStudy.validate(ctx);
		if (ctx.hasErrors()) {
			return;
		}
		abstractStudyAPI.dao_saveObject(assemblyStudy);
		atlasea.primaryAssemblyProjectCode = assemblyStudy.code;
		atlaseaDAO.update(atlasea);
	}


	public void buildSubmissionForSequencingStudyIfNecessary(Atlasea atlasea, ContextValidation ctx) {
		String tolId = atlasea.tolId;
		String submissionCode = "submission_atlasea_"+ tolId + "_studyRawData";

		if(StringUtils.isNotBlank(atlasea.sequencingSubmissionCode)) {
			if(atlasea.sequencingSubmissionCode.equals(submissionCode)) {
				// sequencingSubmissionCode deja en base et code stoqué dans atlasea, rien à faire
				return;
			} else {
				// atlasea.sequencingSubmissionCode renseigne mais pas avec valeur attendue :
				ctx.addError("Error", "atlasea " + atlasea.code + 
						" avec sequencingSubmissionCode=" + atlasea.sequencingSubmissionCode + " different du sequencingSubmissionCode attendu : " + submissionCode);
			}
		}
		// si sequencingSubmissionCode existe deja en base mais qu'il n'est pas dans atlasea, 
		// alors recuperer sequencingSubmissionCode et stoquer son code dans atlasea :
		if(submissionAPI.isObjectExist(submissionCode)) {
			atlasea.sequencingSubmissionCode = submissionCode;
			atlaseaDAO.update(DBQuery.is("code", atlasea.code),
					DBUpdate.set("sequencingSubmissionCode", submissionCode));
			return;
		} 
		buildSubmissionForSequencingStudy(atlasea, submissionCode, ctx);
	}

	public void buildSubmissionForSequencingStudy(Atlasea atlasea, String submissionCode, ContextValidation ctx) {
		ctx.setCreationMode();
		String tolId = atlasea.tolId;
		String studyCode = atlasea.sequencingProjectCode;
		//logger.debug("atlasea.sequencingProjectCode=" + atlasea.sequencingProjectCode);
		Submission submission = buildSubmissionForStudy(tolId, studyCode, submissionCode, ctx);
		atlasea.sequencingSubmissionCode = submission.code;
		atlaseaDAO.update(atlasea);
	}

	public Submission buildSubmissionForStudy(String tolId, String studyCode, String submissionCode, ContextValidation ctx) {
		ctx.setCreationMode();

		Date courantDate = new java.util.Date();
		Submission submission = new Submission(user);
		submission.code = submissionCode;
		submission.traceInformation.creationDate = courantDate;
		submission.state = new State(SUB_SMD_IP, user);
		submission.type = Submission.Type.CREATION;
		submission.typeRawDataSubmitted = TypeRawDataSubmitted.withoutRawData;
		submission.dropBox = dropBox;
		submission.projectCodes = this.projectAtlaseaCodes;
		submission.submissionDirectory = this.submissionAtlaseaRootResultDirectory + tolId + "/"; 	
		submission.ebiResult = "ebi_AC_" + submission.code + ".txt";
		submission.studyCode = studyCode;
		submission.validate(ctx);
		if (ctx.hasErrors()) {

			return null;
		} 
		return submissionAPI.dao_saveObject(submission);
	}


	public void buildSubmissionForPrimaryAssemblyStudy(Atlasea atlasea,String submissionCode, ContextValidation ctx) {
		String tolId = atlasea.tolId;
		String studyCode = atlasea.primaryAssemblyProjectCode;
		Submission submission = buildSubmissionForStudy(tolId, studyCode, submissionCode, ctx);
		atlasea.primaryAssemblySubmissionCode = submission.code;
		atlaseaDAO.update(atlasea);
	}



	/**
	 * Verifie que l'analyse contient des sampleCodes ayant tous le meme taxonCode et que celui-ci est bien soumettable.
	 * 
	 * @param analysis    analyse
	 * @param tolid       tolid
	 * @param ctx         Contexte de validation avec potentielles erreurs si plusieurs taxonCodes pour l'analyse,...
	 * @return taxonCode renseigné si meme taxonCode pour tous les samples de l'analyse et taxonCode soumettable
	 */
	private String getUniqTaxonCode(Analysis analysis, String tolid, ContextValidation ctx) {
		// verifier que tous les sampleCodes ont meme taxonCode :
		List<String>taxonCodes = new ArrayList<String>();
		for (String sampleCode : analysis.sampleCodes) {
			if(! MongoDBDAO.checkObjectExist(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, "code", sampleCode)) {
				//logger.debug("analyse." + analysis.code +" contient un sampleCode="+ sampleCode + " impossible a recuperer dans base");
				ctx.addError("analyse." + analysis.code, "contient un sampleCode="+ sampleCode + " impossible a recuperer dans base");
				return null;
			}
			Sample laboSample =  MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, sampleCode);

			if(StringUtils.isBlank(laboSample.taxonCode)) {
				//logger.debug("analyse." + analysis.code +" contient un sampleCode " + sampleCode + " sans taxonCode");
				ctx.addError("analyse." + analysis.code, "contient un sampleCode " + sampleCode + " sans taxonCode");
				return null;
			}
			if( ! taxonCodes.contains(laboSample.taxonCode)) {
				taxonCodes.add(laboSample.taxonCode);
			}
		}
		if(taxonCodes.size() != 1 ) {
			//logger.debug("analyse." + analysis.code + " contient des taxonCodes differents");
			ctx.addError("analyse." + analysis.code, "contient des taxonCodes differents");
			return null;
		}	
		if(StringUtils.isBlank(taxonCodes.get(0))) {
			//logger.debug("analyse." + analysis.code +  " avec taxonId sans caractere visible");
			ctx.addError("analysis."+ analysis.code, "aucun strTaxonId");
			return null;
		} 
		// verifier taxonId au niveau feuille :
		if( ! ebiAPI.submittable(Integer.parseInt(taxonCodes.get(0))) ) {
			ctx.addError("analysis."+ analysis.code, " avec tolid=" + tolid + ", taxonId=" + taxonCodes.get(0) + " non soumettable");
			return null;
		} 
		return taxonCodes.get(0);
	}

	/**
	 * Envoie un mail de succès correspondant à sb aux destinataires definis dans la methode.
	 * 
	 * @param sb StringBuilder avec message de succès (corps du mail)
	 */
	public void sendSuccessMail(StringBuilder sb ) {
		// variables pour les mails :
		String successSubject           = config.getCreationProjectsAtlaseaEmailSubject();
		String str_successDestinataires = config.getCreationProjectsAtlaseaEmailTo();
		if ( StringUtils.isNotBlank(sb.toString())) {
			logger.debug("Envoie mail de succes");
			StringBuilder successBuilder = new StringBuilder();
			successBuilder.append(this.headerSuccessMessage)
			.append(sb.toString());
			MailServices mailService = new MailServices();
			Set<String> destinataires = new HashSet<>();
			String str_destinataires = str_successDestinataires;
			if(! str_destinataires.contains(",")) {
				str_destinataires += ",";
			}
			//logger.info("sendSuccessMail: str_destinataires=" + str_destinataires);
			destinataires.addAll(Arrays.asList(str_destinataires.split(",")));  
			String message = "<table border=\"1\" cellspacing=\"0\" cellpadding=\"10\">" + successBuilder.toString() + "</table>";
			try {
				mailService.sendMail(this.expediteur, destinataires, successSubject, new String(message.getBytes(), "iso-8859-1"));
				//logger.info("mailService.sendMail ok avec expediteur="+this.expediteur+ ", destinataires = "+ destinataires + ", successSubject="+ successSubject);
			} catch (UnsupportedEncodingException e) {
				throw new SraException("sendSuccessMail:: Probleme lors de l'encodage du message du mail pour la creation automatique des projects Atlasea, message=" + e.getMessage());
			} catch (MailServiceException e) {
				throw new SraException("sendSuccessMail:: Probleme lors de l'envoie du mail pour la creation automatique des projects Atlasea, message=" + e.getMessage());
			}
		}
	}

	/**
	 * Stocke les messages d'erreurs du context de validation ctx dans le StringBuilder errorBuilder 
	 * 
	 * @param ctx Context de validation
	 * @param errorBuilder StringBuilder pour stocker les messages d'erreur
	 * @return true si erreurs, et false sinon.
	 */
	public boolean storeErrorMessage(ContextValidation ctx, StringBuilder errorBuilder) {
		if(ctx.hasErrors()) {
			// Recuperer message erreur et envoyer mail
			//logger.debug("AAA  dans execute, apres getUniqTaxonCode debut displayError");
			//ctx.displayErrors(logger, "debug");
			//logger.debug("dans execute fin displayError");
			for (Map.Entry<String,List<ValidationError>> e : ctx.getErrors().entrySet()) {
				for (ValidationError validationError : e.getValue()) {
					for (String message : validationError.messages()) {
						//errorBuilder.append(String.format("%-25s%-6s%-100s<br/>\n", e.getKey(), " : ", message));
						errorBuilder.append(String.format("<tr><td>%s</td><td>%s</td></tr>\n", e.getKey(), message));
					}
				}
			}
			return true;
		}
		return false;
	}


	/**
	 * Envoie un mail d'erreur correspondant à errorBuilder aux destinataires definis dans la methode.
	 * 
	 * @param errorBuilder StringBuilder contenant le message d'erreur (corps du mail)
	 */
	public void sendErrorMail(StringBuilder errorBuilder) {
		String errorSubject             = config.getCreationProjectsAtlaseaEmailErrorSubject();
		String str_errorDestinataires   = config.getCreationProjectsAtlaseaEmailErrorTo();
		// Envoie message erreurs creation projects : 
		if(StringUtils.isNotBlank(errorBuilder.toString())) {
			logger.debug("Envoie mail d'erreurs");
			MailServices mailService = new MailServices();
			Set<String> destinataires = new HashSet<>();
			String str_destinataires = str_errorDestinataires;
			if(! str_destinataires.contains(",")) {
				str_destinataires += ",";
			}
			logger.info("Dans sendErrorMail, str_destinataires=" + str_destinataires );
			destinataires.addAll(Arrays.asList(str_destinataires.split(",")));  
			//logger.debug("Envoie message erreur pour mail :");
			String message = "<table border=\"0\" cellspacing=\"0\" cellpadding=\"10\">" + errorBuilder.toString() + "</table>";
			try {
				mailService.sendMail(this.expediteur, destinataires, errorSubject, new String(message.getBytes(), "iso-8859-1"));
			} catch (UnsupportedEncodingException e) {
				throw new SraException("sendErrorMail:: Probleme lors de l'encodage du message du mail d'erreurs pour la creation automatique des projects Atlasea, message=" + e.getMessage());
			} catch (MailServiceException e) {
				throw new SraException("sendErrorMail:: Probleme lors de l'envoie du mail d'erreurs pour la creation automatique des projects Atlasea, message=" + e.getMessage());
			}
		}
	}
	/**
	 * Copie le context de validation ctxB dans le context de validation ctxA
	 * 
	 * @param ctxA context de validation A
	 * @param ctxB context de validation B
	 */
	public void addErreurCxtBInCtxA(ContextValidation ctxA, ContextValidation ctxB) {
		if(ctxB.hasErrors()) {
			// Reporter les erreurs dans intermediaireCtx :
			//String messages = "";
			for (Map.Entry<String,List<ValidationError>> e : ctxB.getErrors().entrySet()) {
				for (ValidationError validationError : e.getValue()) {
					//logger.debug("error avec e.getValue()=" + e.getValue());
					for (String message : validationError.messages()) {
						//logger.debug("error avec e.getKey()=" + e.getKey());
						ctxA.appendPath(e.getKey()).addError("", message);
					}
				}
			}
		}
	}

}
