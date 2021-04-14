package sra.scripts.off;
//package sra.scripts;
//import java.io.File;
//import java.io.IOException;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import javax.inject.Inject;
//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;
//import javax.xml.parsers.ParserConfigurationException;
//import static ngl.refactoring.state.SRASubmissionStateNames.F_SUB;
//
//import org.apache.commons.collections4.MapUtils;
//import org.mongojack.DBQuery;
//import org.mongojack.DBUpdate;
//import org.w3c.dom.Document;
//import org.w3c.dom.Element;
//import org.w3c.dom.Node;
//import org.w3c.dom.NodeList;
//import org.xml.sax.SAXException;
//
//import fr.cea.ig.lfw.controllers.scripts.buffered.Script;
//import fr.cea.ig.lfw.utils.Iterables;
//import fr.cea.ig.lfw.utils.ZenIterable;
//import fr.cea.ig.ngl.dao.api.sra.ExperimentAPI;
//import fr.cea.ig.ngl.dao.api.sra.ReadsetAPI;
//import fr.cea.ig.ngl.dao.api.sra.SampleAPI;
//import fr.cea.ig.ngl.dao.api.sra.StudyAPI;
//import fr.cea.ig.ngl.dao.api.sra.SubmissionAPI;
//import fr.cea.ig.ngl.dao.readsets.ReadSetsAPI;
//import models.laboratory.common.instance.State;
//import models.sra.submit.common.instance.Readset;
//import models.sra.submit.common.instance.Sample;
//import models.sra.submit.common.instance.Study;
//import models.sra.submit.common.instance.Submission;
//import models.sra.submit.sra.instance.Experiment;
//import models.sra.submit.sra.instance.RawData;
//import models.sra.submit.sra.instance.Run;
//import models.sra.submit.util.SraException;
//import play.libs.Json;
//import services.RepriseHistorique;
//import services.ncbi.NCBITaxon;
//import services.ncbi.TaxonomyServices;
//import sra.scripts.utils.EbiAPI;
//import validation.ContextValidation;
//import fr.cea.ig.ngl.NGLApplication; // pour affichage du contextValidationError
//import org.apache.commons.lang3.StringUtils;
//
//
//// Les classes internes doivent toujours etre mises en static. Cela permet d'y acceder sans avoir d'instance autour.
//
////SGAS revoir commentaires
//
///*
// * Script à lancer pour importer dans NGL-SUB une soumission réalisée 
// * hors procedure NGL-SUB. (cas d'une soumission de 454 par exemple)
// * {@code http://localhost:9000/sra/scripts/run/sra.scripts.ImportAcEbi?ebiFileAc=cheminFichierAcRetourEbi}
// * <br>
// * Si parametre absent dans url => declenchement d'une erreur.
// * 
// * @author sgas
// *
// */
//public class RepriseHistoCollectionSubmission extends Script<RepriseHistoCollectionSubmission.Args> { 
//	
//	// si pas static, j'ai une instance autour de moi (this), et donc je peux utiliser, acceder aux membres de l'instance this
//	// comme Args. Si static, je peux acceder qu'aux statics. 
//	// Une classe static interne est rangée ailleurs que dans la classe (mini package)
////	public static void m() {
////		Args A = new Args();
////	}
////	
//	private final String undifinedValueInRepriseHistorique = "undefinedValue_repriseHistorique";
//	private final String adminComment = "Creation dans le cadre d'une reprise d'historique";
//	public static class Args {  //package sra.scripts.ImportAcEbi.Args	
//		public String ebiDirFileAc; // Chemin du repertoire avec fichiers retour des numeros d'accession de l'EBI.
//	}
//	
//	
//	public abstract class AbstractInfo {
//		public String code;
//		public String accession;	
//	}
//	public class SubmissionInfo extends AbstractInfo {
//	}
//	public class ExperimentInfo extends AbstractInfo {
//	}
//	public class RunInfo extends AbstractInfo {
//	}
//	public class StudyInfo extends AbstractInfo {
//		public String externalId;
//	}
//	public class SampleInfo extends AbstractInfo {
//		public String externalId;
//	}
//	public class InfosEbi {
//		// lasy = paresseuse => initialisation paresseuse qui sera faite si on demande la Map.
////		private Map<String, SubmissionInfo> submissionsInfos = MapUtils.lazyMap(new HashMap<>(), () -> new SubmissionInfo()); 
//		private String receiptDate = null;
//		private SubmissionInfo submissionInfos               = new SubmissionInfo();
//		private Map<String, StudyInfo> studiesInfos          = MapUtils.lazyMap(new HashMap<>(), () -> new StudyInfo()); 
//		private Map<String, SampleInfo> samplesInfos         = MapUtils.lazyMap(new HashMap<>(), () -> new SampleInfo());
//		private Map<String, ExperimentInfo> experimentsInfos = MapUtils.lazyMap(new HashMap<>(), () -> new ExperimentInfo());
//		private Map<String, RunInfo> runsInfos               = MapUtils.lazyMap(new HashMap<>(), () -> new RunInfo());
//		
//		private String getSubmissionDate(){
//			return this.receiptDate;
//		}
//		
//		private void setSubmissionDate(String stringDate){
//			this.receiptDate = stringDate;
//		}
//				
//		public String getStudyAccession(String code) {
//			if (studiesInfos.get(code) != null && studiesInfos.get(code).accession != null) {
//				return studiesInfos.get(code).accession;
//			} else {
//				return null;
//			}
//		}
//		
//		public void setStudyAccession(String code, String accession) {
////          Lignes inutiles avec lazyMap			
////			if (! studiesInfos.containsKey(code)) {
////				studiesInfos.put(code, new StudyInfo());
////			} 
//			studiesInfos.get(code).accession = accession;
//		}		
//		public String getStudyExternalId(String code) {
//			// inutile de tester existance  studiesInfos.get(code) et champs externalId avec LazyMap
//			return studiesInfos.get(code).externalId;
//		}
//		public void setStudyExternalId(String code, String externalId) {
//			studiesInfos.get(code).externalId = externalId;
//		}
//		
//
//		public String getSampleAccession(String code) {
//			return samplesInfos.get(code).accession;
//		}		
//		public void setSampleAccession(String code, String accession) {
//			samplesInfos.get(code).accession = accession;
//		}		
//		public String getSampleExternalId(String code) {
//			return samplesInfos.get(code).externalId;
//		}	
//		public void setSampleExternalId(String code, String externalId) {
//			samplesInfos.get(code).externalId = externalId;
//		}	
//		public String getExperimentAccession(String code) {
//			return experimentsInfos.get(code).accession;
//		}		
//		public void setExperimentAccession(String code, String accession) {
//			experimentsInfos.get(code).accession = accession;
//		}		
//		public String getRunAccession(String code) {
//			return runsInfos.get(code).accession;
//		}		
//		public void setRunAccession(String code, String accession) {
//			runsInfos.get(code).accession = accession;
//		}		
////		public String getSubmissionAccession(String code) {
////			return submissionsInfos.get(code).accession;
////		}
////		public void setSubmissionAccession(String code, String accession) {
////			submissionsInfos.get(code).accession = accession;
////		}
//		public String getSubmissionAccession() {
//			return submissionInfos.accession;
//		}
//		public void setSubmissionAccession(String accession) {
//			submissionInfos.accession = accession;
//		}
//		public String getSubmissionCode() {
//			return submissionInfos.code;
//		}
//		public void setSubmissionCode(String code) {
//			submissionInfos.code = code;
//		}		
//		
////		public List<String> getStudyExternalIds() {
////			List<String> externalIds = new ArrayList<>();
////			for (String code : studiesInfos.keySet()) {
////				externalIds.add(studiesInfos.get(code).externalId);
////			}
////			return externalIds;
////		}		
////		public ZenIterable<String> getStudyExternalIds() {
////			return Iterables.map(studiesInfos.keySet(), code -> studiesInfos.get(code).externalId);
////		}
////		public Collection<String> getStudyCodes() {
////			return studiesInfos.keySet();
////		}	
////		public Iterable<String> getStudyCodes() {
////			return studiesInfos.keySet();
////		}	
////		public ZenIterable<String> getStudyCodes() {
////			return Iterables.zen(studiesInfos.keySet());
////		}
////		public ZenIterable<String> getStudyCodes() {
////			return zen(studiesInfos.keySet());
////		}	
//		
////		public ZenIterable<String> getSubmissionAccessions() {
////			return Iterables.map(submissionsInfos.keySet(), code -> submissionsInfos.get(code).accession);
////		}
////		public ZenIterable<String> getSubmissionCodes() {
////			return Iterables.zen(submissionsInfos.keySet());
////		}		
//
//		public ZenIterable<String> getStudyExternalIds() {
//			return Iterables.map(studiesInfos.keySet(), code -> studiesInfos.get(code).externalId);
//		}
//		public ZenIterable<String> getStudyAccessions() {
//			return Iterables.map(studiesInfos.keySet(), code -> studiesInfos.get(code).accession);
//		}
//		public ZenIterable<String> getStudyCodes() {
//			return Iterables.zen(studiesInfos.keySet());
//		}
//		public ZenIterable<String> getSampleExternalIds() {
//			return Iterables.map(samplesInfos.keySet(), code -> samplesInfos.get(code).externalId);
//		}
//		public ZenIterable<String> getSampleAccessions() {
//			return Iterables.map(samplesInfos.keySet(), code -> samplesInfos.get(code).accession);
//		}
//		public ZenIterable<String> getSampleCodes() {
//			return Iterables.zen(samplesInfos.keySet());
//		}		
//		public ZenIterable<String> getExperimentAccessions() {
//			return Iterables.map(experimentsInfos.keySet(), code -> experimentsInfos.get(code).accession);
//		}
//		public ZenIterable<String> getExperimentCodes() {
//			return Iterables.zen(experimentsInfos.keySet());
//		}	
//		public ZenIterable<String> getRunAccessions() {
//			return Iterables.map(runsInfos.keySet(), code -> runsInfos.get(code).accession);
//		}
//		public ZenIterable<String> getRunCodes() {
//			return Iterables.zen(runsInfos.keySet());
//		}		
//	}
//	
//	
//	
//	private static final play.Logger.ALogger logger = play.Logger.of(RepriseHistoCollectionSubmission.class);
//
//	// final static SubmissionWorkflows submissionWorkflows = Spring.get BeanOfType(SubmissionWorkflows.class);
//
//	private final SubmissionAPI    submissionAPI; 
//	private final StudyAPI         studyAPI;
//	private final SampleAPI        sampleAPI;
//	private final ExperimentAPI    experimentAPI;
//	private final ReadSetsAPI      readsetsAPI;
//	private final ReadsetAPI   	   readsetAPI;
//	private final EbiAPI           ebiAPI;
//	private final NGLApplication   app;
//	private final TaxonomyServices taxonomyServices;
//
//	@Inject
//	public RepriseHistoCollectionSubmission(SubmissionAPI       submissionAPI,
//					   StudyAPI            studyAPI,
//					   SampleAPI           sampleAPI, 
//					   ExperimentAPI       experimentAPI,
//					   ReadSetsAPI   	   readsetsAPI,
//					   ReadsetAPI   	   readsetAPI,
//					   EbiAPI              ebiAPI,
//					   NGLApplication      app,
//					   TaxonomyServices    taxonomyServices) {
//		this.submissionAPI       = submissionAPI;
//		this.studyAPI            = studyAPI;
//		this.sampleAPI           = sampleAPI;
//		this.experimentAPI       = experimentAPI;
//		this.readsetsAPI         = readsetsAPI;
//		this.readsetAPI          = readsetAPI;
//		this.ebiAPI              = ebiAPI;
//		this.app                 = app;	
//		this.taxonomyServices    = taxonomyServices;
//	}
//
//		
//	public InfosEbi parseEbiFileAc(String pathEbiFileAc) throws SraException {	
//		InfosEbi retour = new InfosEbi();
//
//		File ebiFileAc = new File(pathEbiFileAc);
//		if (! ebiFileAc.exists()) {
//			throw new SraException("Fichier des AC de l'Ebi non present sur disque : "+ ebiFileAc.getAbsolutePath());
//		}
//
//		Boolean ebiSuccess       = false;	
//
//		/*
//		 * Etape 1 : récupération d'une instance de la classe "DocumentBuilderFactory"
//		 */
//		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//		try {
//			/*
//			 * Etape 2 : création d'un parseur
//			 */
//			final DocumentBuilder builder = factory.newDocumentBuilder();
//			/*
//			 * Etape 3 : création d'un Document
//			 */
//			
//			final Document document= builder.parse(ebiFileAc);
//			//Affiche du prologue
//			logger.debug("*************PROLOGUE************");
//			logger.debug("version : " + document.getXmlVersion());
//			logger.debug("encodage : " + document.getXmlEncoding());      
//			logger.debug("standalone : " + document.getXmlStandalone());
//			/*
//			 * Etape 4 : récupération de l'Element racine
//			 */
//			final Element racine = document.getDocumentElement();
//			//Affichage de l'élément racine
//			logger.debug("\n*************RACINE************");
//			logger.debug(racine.getNodeName());
//			logger.debug("success = " + racine.getAttribute("success"));
//			//logger.debug(((Node) racine).getNodeName());
//			//logger.debug("success = " + ((DocumentBuilderFactory) racine).getAttribute("success"));
//			/*
//			 * Etape 5 : récupération des samples
//			 */
//			final NodeList racineNoeuds = racine.getChildNodes();
//			final int nbRacineNoeuds = racineNoeuds.getLength();
//			if ( racine.getAttribute("success").equalsIgnoreCase ("true")) {
//				ebiSuccess = true;
//			}
//			
//			retour.setSubmissionDate(racine.getAttribute("receiptDate"));
//			
//			for (int i = 0; i<nbRacineNoeuds; i++) {
//				if (racineNoeuds.item(i).getNodeType() == Node.ELEMENT_NODE) {
//					
//					final Element elt = (Element) racineNoeuds.item(i);
//					//Affichage d'un elt :
//					System.out.println("\n*************Elt************");
//					String code = elt.getAttribute("alias");
//					String accession = elt.getAttribute("accession");
//					logger.debug("alias : " + code);
//					logger.debug("accession : " + accession);
//					switch (elt.getTagName().toUpperCase()) {
//					case "SUBMISSION": 
//						retour.setSubmissionAccession(accession);
//						retour.setSubmissionCode(code);
//						break;
//					case "STUDY"     : { // on definit un bloc pour pouvoir utiliser variables locales au bloc
//						retour.setStudyAccession(code, accession);
//						final Element eltExtId = (Element) elt.getElementsByTagName("EXT_ID").item(0);
//						//String externalId = eltExtId.getAttribute("accession");
//						//retour.setStudyExternalId(code, externalId);
//						break; 
//						} 
//					case "SAMPLE"    : {
//						retour.setSampleAccession(code, accession);
//						final Element eltExtId = (Element) elt.getElementsByTagName("EXT_ID").item(0);
//						//String externalId = eltExtId.getAttribute("accession");
//						//retour.setSampleExternalId(code, externalId);
//						break;
//						}
//					case "EXPERIMENT":
//						retour.setExperimentAccession(code, accession);		
//						break;
//					case "RUN"       :
//						retour.setRunAccession(code, accession);	
//						break;
//					default :
//						// Ignore other nodes
//					}
//				}
//			}  // end for  
//		} catch (final ParserConfigurationException | SAXException | IOException e) {
//			e.printStackTrace();
//			throw new RuntimeException("io error while parsing xml ",e);
//		} 	
//		if (! ebiSuccess ) {
//			logger.debug("pattern 'success=true' absent du fichier  {}", ebiFileAc);
//			throw new SraException("Fichier retour de l'EBI " + pathEbiFileAc + " qui ne contient pas la ligne success=true");
//		} else {
//			logger.debug("Parsing du fichier {} ok ", ebiFileAc);
//		}
//		return retour;
//	}
//	
//
//	@Override
//	public void execute(Args args) throws Exception {
//		
//		File rep = new File(args.ebiDirFileAc); 
//		String liste[] = rep.list();      
//		for (int z = 0; z < liste.length; z++) {
//			String ebiFileAC = rep.getPath() +  File.separator + liste[z];
//			if (!ebiFileAC.endsWith(".txt")) {
//				continue;
//			}
//			System.out.println(ebiFileAC);
//			InfosEbi retour = new InfosEbi();
//			printfln ("Fichier des AC '%s'", ebiFileAC);
//			
//			// Recuperation des Ac de la soumission :
//			InfosEbi infosEbi = parseEbiFileAc(ebiFileAC);
//			println("OK POUR LE PARSING !!!!!!!!!!!!!!!!");
//			String user = "william";
//			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
//			State state = new State("F-SUB", user);
//		
//			String submissionCode = infosEbi.getSubmissionCode();
//			
//			// creation de l'objet soumission
//			Submission submission   = new Submission();
//			submission.code         = infosEbi.getSubmissionCode();
//			submission.creationDate = dateFormat.parse(infosEbi.getSubmissionDate());
//			submission.accession    = infosEbi.getSubmissionAccession();
//			submission.creationUser = user;
//			submission.release      = false;
//			submission.submissionDirectory = undifinedValueInRepriseHistorique;
//			submission.state               = state;
//			submission.adminComment        = adminComment;		
//			submission.configCode          = undifinedValueInRepriseHistorique;
//			submission.traceInformation.setTraceInformation(user);
//			
//			printfln("submission.code '" + infosEbi.getSubmissionCode() + "'");
//			printfln("submission.accession '" + infosEbi.getSubmissionAccession() + "'");
//
//
//			Submission subm = submissionAPI.get(infosEbi.getSubmissionCode());
//			// verifier que la soumission n'existe pas deja dans la base:
//			if (submissionAPI.dao_checkObjectExist("code",infosEbi.getSubmissionCode())) {
//				printfln("soumission " + submissionCode + " existe deja dans la base selon verif check");
//				continue;
//			} else {
//				printfln("soumission " + submissionCode + " n existe pas dans base selon verif check");
//			}
//			
//			// verifier que la soumission n'existe pas deja dans la base:
//			if (StringUtils.isBlank(infosEbi.getSubmissionAccession())) {
//				throw new SraException("Absence de numeros d'accession pour la soumission " + infosEbi.getSubmissionCode());
//			}
//			// Attention test avec check qui repond vrai si chaine vide car il existe des soumissions sans accession.
//			if (submissionAPI.dao_checkObjectExist("accession", infosEbi.getSubmissionAccession())) {
//				printfln("Soumission " + infosEbi.getSubmissionAccession() + " deja presente dans base");
//				printfln("Soumission " + infosEbi.getSubmissionCode() + " deja presente dans base");
//				continue;
//			}
//		
//			for (String studyCode : infosEbi.getStudyCodes()) {
//				// mise à jour de l'objet soumission pour studyCode :
//				submission.studyCode = studyCode;
//				Study study = studyAPI.get(studyCode);
//				if(study==null) {
//					throw new SraException("Pour la soumission " + infosEbi.submissionInfos.code + " le study " + studyCode + " est absent de la base");
//				}
//				if (! submission.refStudyCodes.contains(studyCode)) {
//					submission.refStudyCodes.add(studyCode);
//				}
//				submission.studyCode = studyCode; 
//
//				// verifier qu'il n'existe pas dans la base une soumission de creation pour le study :
//				List <Submission> submissionList = submissionAPI.dao_find(DBQuery.is("studyCode", studyCode).is("release", false)).toList();
//				
//				if (submissionList.size() != 0) {
//					logger.debug("Pour le studyCode=" + study.code + " avec state=" + study.state.code);
//					for (Submission sub: submissionList) {
//						logger.debug("     - utilise par objet Submission " + sub.code);
//					}
//					throw new SraException(studyCode + " utilise par plusieurs objets submissions");	
//				}
//				for(String projectCode: study.projectCodes) {
//					if (!submission.projectCodes.contains(projectCode)) {
//						submission.projectCodes.add(projectCode);
//					}
//				}
//			}		
//		
//			for (String sampleCode: infosEbi.getSampleCodes()) {
//				Sample sample = sampleAPI.get(sampleCode);
//				//Sample sample = sampleAPI.dao_findOne(DBQuery.is("accession", infosEbi.getSampleAccession(sampleCode)));
//				if (sample == null) {
//					throw new SraException("Pour la soumission " + infosEbi.submissionInfos.code + " le sample " + sampleCode + " est absent de la base");
//				}
//				// mise à jour de l'objet soumission pour sampleCode :			
//				if (! submission.sampleCodes.contains(sample.code)) {
//					submission.sampleCodes.add(sample.code);
//				}
//				if (! submission.refSampleCodes.contains(sample.code)) {
//					submission.refSampleCodes.add(sample.code);
//				}
//				if (!submission.projectCodes.contains(sample.projectCode)) {
//					submission.projectCodes.add(sample.projectCode);
//				}
//				// verifier qu'une soumission n'existe pas avec ce sample en mode creation
//				// verifier qu'une soumission n'existe pas avec cet experiment en mode creation
//				List <Submission> submissionList = submissionAPI.dao_find(DBQuery.in("sampleCodes", sampleCode)).toList();
//				
//				if (submissionList.size() != 0) {
//					logger.debug("sampleCode=" + sample.code + " avec state=" + sample.state.code);
//					for (Submission sub: submissionList) {
//						logger.debug("     - utilise par objet Submission " + sub.code);
//					}
//					throw new SraException(sampleCode + " utilise par plusieurs objets submissions");	
//				}
//			}
//		
//
//			for (String experimentCode: infosEbi.getExperimentCodes()) {
//				// mise à jour de l'objet soumission pour experimentCode et projectCode:			
//				if (!submission.experimentCodes.contains(experimentCode)) {
//					submission.experimentCodes.add(experimentCode);
//				}
//				Experiment experiment = experimentAPI.get(experimentCode);
//				if (experiment == null) {
//					throw new SraException("Pour la soumission " + infosEbi.submissionInfos.code + " l'experiment " + experimentCode + " est absent de la base");
//				}
//				if (!submission.projectCodes.contains(experiment.projectCode)) {
//					submission.projectCodes.add(experiment.projectCode);
//				}
//				// verifier qu'une soumission n'existe pas avec cet experiment en mode creation
//				List <Submission> submissionList = submissionAPI.dao_find(DBQuery.in("experimentCodes", experimentCode)).toList();
//				logger.debug("experimentCode=" + experiment.code + " avec state=" + experiment.state.code);
//				if (submissionList.size() != 0) {
//					logger.debug("experimentCode=" + experiment.code + " avec state=" + experiment.state.code);
//					for (Submission sub: submissionList) {
//						logger.debug("     - utilise par objet Submission " + sub.code);
//					}
//					throw new SraException(experimentCode + " utilise par plusieurs objets submissions");	
//				}
//			}
//		
//
//			for (String runCode : infosEbi.getRunCodes()) {
//				// mise à jour de l'objet soumission pour runCode:			
//				if (!submission.runCodes.contains(runCode)) {
//					submission.runCodes.add(runCode);
//				}
//			}
//			// Verifier la validité des objets SRA
//			ContextValidation contextValidation = ContextValidation.createCreationContext(user);
//			submission.validate(contextValidation);
//			int cp = 0;
//			if (contextValidation.hasErrors()) {
//				contextValidation.displayErrors(logger);
//				println(Json.prettyPrint(app.errorsAsJson(contextValidation.getErrors())));
//				throw new SraException("submission non valide pour " + submission.code);
//			} else {			
//				println("insertion de la soumission " + submission.code);
//				cp ++;
//				submissionAPI.dao_saveObject(submission);
//			}	
//			println("Nombre de creation de nouvelles soumissions : " + cp);
//		}
//	}
//}
