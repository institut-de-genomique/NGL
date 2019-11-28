package sra.scripts;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import static ngl.refactoring.state.SRASubmissionStateNames.F_SUB;

import org.apache.commons.collections4.MapUtils;
import org.eclipse.jetty.util.StringUtil;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import fr.cea.ig.lfw.controllers.scripts.buffered.Script;
import fr.cea.ig.lfw.utils.Iterables;
import fr.cea.ig.lfw.utils.ZenIterable;
import fr.cea.ig.ngl.dao.api.sra.ExperimentAPI;
import fr.cea.ig.ngl.dao.api.sra.ReadsetAPI;
import fr.cea.ig.ngl.dao.api.sra.SampleAPI;
import fr.cea.ig.ngl.dao.api.sra.StudyAPI;
import fr.cea.ig.ngl.dao.api.sra.SubmissionAPI;
import fr.cea.ig.ngl.dao.readsets.ReadSetsAPI;
import models.laboratory.common.instance.State;
import models.sra.submit.common.instance.Readset;
import models.sra.submit.common.instance.Sample;
import models.sra.submit.common.instance.Study;
import models.sra.submit.common.instance.Submission;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.sra.instance.RawData;
import models.sra.submit.sra.instance.Run;
import models.sra.submit.util.SraException;
import play.libs.Json;
import services.RepriseHistorique;
import services.ncbi.NCBITaxon;
import services.ncbi.TaxonomyServices;
import sra.scripts.utils.EbiAPI;
import validation.ContextValidation;
import fr.cea.ig.ngl.NGLApplication; // pour affichage du contextValidationError


// Les classes internes doivent toujours etre mises en static. Cela permet d'y acceder sans avoir d'instance autour.


/*
 * Script à lancer pour importer dans NGL-SUB une soumission réalisée 
 * hors procedure NGL-SUB. (cas d'une soumission de 454 par exemple)
 * {@code http://localhost:9000/sra/scripts/run/sra.scripts.ImportAcEbi?ebiFileAc=cheminFichierAcRetourEbi}
 * <br>
 * Si parametre absent dans url => declenchement d'une erreur.
 * 
 * @author sgas
 *
 */
//SGAS
// ne marche pas pour les submissions sans sample ni experiments car pas de determination de code projet
public class ImportAcEbi extends Script<ImportAcEbi.Args> { 
	
	// si pas static, j'ai une instance autour de moi (this), et donc je peux utiliser, acceder aux membres de l'instance this
	// comme Args. Si static, je peux acceder qu'aux statics. 
	// Une classe static interne est rangée ailleurs que dans la classe (mini package)
//	public static void m() {
//		Args A = new Args();
//	}
//	
	private final String undifinedValueInRepriseHistorique = "undifinedValueRepriseHistorique";
	private final String adminComment = "Creation dans le cadre d'une reprise d'historique";
	
	public static class Args {  //package sra.scripts.ImportAcEbi.Args	
		public String ebiFileAc; // Chemin du fichier retour des numeros d'accession de l'EBI.
	}
	
	
	public abstract class AbstractInfo {
		public String code;
		public String accession;	
	}
	public class SubmissionInfo extends AbstractInfo {
	}
	public class ExperimentInfo extends AbstractInfo {
	}
	public class RunInfo extends AbstractInfo {
	}
	public class StudyInfo extends AbstractInfo {
		public String externalId;
	}
	public class SampleInfo extends AbstractInfo {
		public String externalId;
	}
	public class InfosEbi {
		// lasy = paresseuse => initialisation paresseuse qui sera faite si on demande la Map.
//		private Map<String, SubmissionInfo> submissionsInfos = MapUtils.lazyMap(new HashMap<>(), () -> new SubmissionInfo()); 
		private String receiptDate = null;
		private SubmissionInfo submissionInfos               = new SubmissionInfo();
		private Map<String, StudyInfo> studiesInfos          = MapUtils.lazyMap(new HashMap<>(), () -> new StudyInfo()); 
		private Map<String, SampleInfo> samplesInfos         = MapUtils.lazyMap(new HashMap<>(), () -> new SampleInfo());
		private Map<String, ExperimentInfo> experimentsInfos = MapUtils.lazyMap(new HashMap<>(), () -> new ExperimentInfo());
		private Map<String, RunInfo> runsInfos               = MapUtils.lazyMap(new HashMap<>(), () -> new RunInfo());
		
		private String getSubmissionDate(){
			return this.receiptDate;
		}
		
		private void setSubmissionDate(String stringDate){
			this.receiptDate = stringDate;
		}
				
		public String getStudyAccession(String code) {
			if (studiesInfos.get(code) != null && studiesInfos.get(code).accession != null) {
				return studiesInfos.get(code).accession;
			} else {
				return null;
			}
		}
		
		public void setStudyAccession(String code, String accession) {
//          Lignes inutiles avec lazyMap			
//			if (! studiesInfos.containsKey(code)) {
//				studiesInfos.put(code, new StudyInfo());
//			} 
			studiesInfos.get(code).accession = accession;
		}		
		public String getStudyExternalId(String code) {
			// inutile de tester existance  studiesInfos.get(code) et champs externalId avec LazyMap
			return studiesInfos.get(code).externalId;
		}
		public void setStudyExternalId(String code, String externalId) {
			studiesInfos.get(code).externalId = externalId;
		}
		

		public String getSampleAccession(String code) {
			return samplesInfos.get(code).accession;
		}		
		public void setSampleAccession(String code, String accession) {
			samplesInfos.get(code).accession = accession;
		}		
		public String getSampleExternalId(String code) {
			return samplesInfos.get(code).externalId;
		}	
		public void setSampleExternalId(String code, String externalId) {
			samplesInfos.get(code).externalId = externalId;
		}	
		public String getExperimentAccession(String code) {
			return experimentsInfos.get(code).accession;
		}		
		public void setExperimentAccession(String code, String accession) {
			experimentsInfos.get(code).accession = accession;
		}		
		public String getRunAccession(String code) {
			return runsInfos.get(code).accession;
		}		
		public void setRunAccession(String code, String accession) {
			runsInfos.get(code).accession = accession;
		}		
//		public String getSubmissionAccession(String code) {
//			return submissionsInfos.get(code).accession;
//		}
//		public void setSubmissionAccession(String code, String accession) {
//			submissionsInfos.get(code).accession = accession;
//		}
		public String getSubmissionAccession() {
			return submissionInfos.accession;
		}
		public void setSubmissionAccession(String accession) {
			submissionInfos.accession = accession;
		}
		public String getSubmissionCode() {
			return submissionInfos.code;
		}
		public void setSubmissionCode(String code) {
			submissionInfos.code = code;
		}		
		
//		public List<String> getStudyExternalIds() {
//			List<String> externalIds = new ArrayList<>();
//			for (String code : studiesInfos.keySet()) {
//				externalIds.add(studiesInfos.get(code).externalId);
//			}
//			return externalIds;
//		}		
//		public ZenIterable<String> getStudyExternalIds() {
//			return Iterables.map(studiesInfos.keySet(), code -> studiesInfos.get(code).externalId);
//		}
//		public Collection<String> getStudyCodes() {
//			return studiesInfos.keySet();
//		}	
//		public Iterable<String> getStudyCodes() {
//			return studiesInfos.keySet();
//		}	
//		public ZenIterable<String> getStudyCodes() {
//			return Iterables.zen(studiesInfos.keySet());
//		}
//		public ZenIterable<String> getStudyCodes() {
//			return zen(studiesInfos.keySet());
//		}	
		
//		public ZenIterable<String> getSubmissionAccessions() {
//			return Iterables.map(submissionsInfos.keySet(), code -> submissionsInfos.get(code).accession);
//		}
//		public ZenIterable<String> getSubmissionCodes() {
//			return Iterables.zen(submissionsInfos.keySet());
//		}		

		public ZenIterable<String> getStudyExternalIds() {
			return Iterables.map(studiesInfos.keySet(), code -> studiesInfos.get(code).externalId);
		}
		public ZenIterable<String> getStudyAccessions() {
			return Iterables.map(studiesInfos.keySet(), code -> studiesInfos.get(code).accession);
		}
		public ZenIterable<String> getStudyCodes() {
			return Iterables.zen(studiesInfos.keySet());
		}
		public ZenIterable<String> getSampleExternalIds() {
			return Iterables.map(samplesInfos.keySet(), code -> samplesInfos.get(code).externalId);
		}
		public ZenIterable<String> getSampleAccessions() {
			return Iterables.map(samplesInfos.keySet(), code -> samplesInfos.get(code).accession);
		}
		public ZenIterable<String> getSampleCodes() {
			return Iterables.zen(samplesInfos.keySet());
		}		
		public ZenIterable<String> getExperimentAccessions() {
			return Iterables.map(experimentsInfos.keySet(), code -> experimentsInfos.get(code).accession);
		}
		public ZenIterable<String> getExperimentCodes() {
			return Iterables.zen(experimentsInfos.keySet());
		}	
		public ZenIterable<String> getRunAccessions() {
			return Iterables.map(runsInfos.keySet(), code -> runsInfos.get(code).accession);
		}
		public ZenIterable<String> getRunCodes() {
			return Iterables.zen(runsInfos.keySet());
		}		
	}
	
	
	
	private static final play.Logger.ALogger logger = play.Logger.of(ImportAcEbi.class);

	// final static SubmissionWorkflows submissionWorkflows = Spring.get BeanOfType(SubmissionWorkflows.class);

	private final SubmissionAPI    submissionAPI; 
	private final StudyAPI         studyAPI;
	private final SampleAPI        sampleAPI;
	private final ExperimentAPI    experimentAPI;
	private final ReadSetsAPI      readsetsAPI;
	private final ReadsetAPI   	   readsetAPI;
	private final EbiAPI           ebiAPI;
	private final NGLApplication   app;
	private final TaxonomyServices taxonomyServices;

	@Inject
	public ImportAcEbi(SubmissionAPI       submissionAPI,
					   StudyAPI            studyAPI,
					   SampleAPI           sampleAPI, 
					   ExperimentAPI       experimentAPI,
					   ReadSetsAPI   	   readsetsAPI,
					   ReadsetAPI   	   readsetAPI,
					   EbiAPI              ebiAPI,
					   NGLApplication      app,
					   TaxonomyServices    taxonomyServices) {
		this.submissionAPI       = submissionAPI;
		this.studyAPI            = studyAPI;
		this.sampleAPI           = sampleAPI;
		this.experimentAPI       = experimentAPI;
		this.readsetsAPI         = readsetsAPI;
		this.readsetAPI          = readsetAPI;
		this.ebiAPI              = ebiAPI;
		this.app                 = app;	
		this.taxonomyServices    = taxonomyServices;
	}

		
	public InfosEbi parseEbiFileAc(String pathEbiFileAc) throws SraException {	
		
		InfosEbi retour = new InfosEbi();
		printfln ("Fichier des AC '%s'", pathEbiFileAc);
		File ebiFileAc = new File(pathEbiFileAc);
		if (! ebiFileAc.exists()) {
			throw new SraException("Fichier des AC de l'Ebi non present sur disque : "+ ebiFileAc.getAbsolutePath());
		}

		Boolean ebiSuccess       = false;	

		/*
		 * Etape 1 : récupération d'une instance de la classe "DocumentBuilderFactory"
		 */
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			/*
			 * Etape 2 : création d'un parseur
			 */
			final DocumentBuilder builder = factory.newDocumentBuilder();
			/*
			 * Etape 3 : création d'un Document
			 */
			
			final Document document= builder.parse(ebiFileAc);
			//Affiche du prologue
			logger.debug("*************PROLOGUE************");
			logger.debug("version : " + document.getXmlVersion());
			logger.debug("encodage : " + document.getXmlEncoding());      
			logger.debug("standalone : " + document.getXmlStandalone());
			/*
			 * Etape 4 : récupération de l'Element racine
			 */
			final Element racine = document.getDocumentElement();
			//Affichage de l'élément racine
			logger.debug("\n*************RACINE************");
			logger.debug(racine.getNodeName());
			logger.debug("success = " + racine.getAttribute("success"));
			//logger.debug(((Node) racine).getNodeName());
			//logger.debug("success = " + ((DocumentBuilderFactory) racine).getAttribute("success"));
			/*
			 * Etape 5 : récupération des samples
			 */
			final NodeList racineNoeuds = racine.getChildNodes();
			final int nbRacineNoeuds = racineNoeuds.getLength();
			if ( racine.getAttribute("success").equalsIgnoreCase ("true")) {
				ebiSuccess = true;
			}
			
			retour.setSubmissionDate(racine.getAttribute("receiptDate"));
			
			for (int i = 0; i<nbRacineNoeuds; i++) {
				if (racineNoeuds.item(i).getNodeType() == Node.ELEMENT_NODE) {
					
					final Element elt = (Element) racineNoeuds.item(i);
					//Affichage d'un elt :
					System.out.println("\n*************Elt************");
					String code = elt.getAttribute("alias");
					String accession = elt.getAttribute("accession");
					logger.debug("alias : " + code);
					logger.debug("accession : " + accession);
					switch (elt.getTagName().toUpperCase()) {
					case "SUBMISSION": 
						retour.setSubmissionAccession(accession);
						retour.setSubmissionCode(code);
						break;
					case "STUDY"     : { // on definit un bloc pour pouvoir utiliser variables locales au bloc
						retour.setStudyAccession(code, accession);
						final Element eltExtId = (Element) elt.getElementsByTagName("EXT_ID").item(0);
						String externalId = eltExtId.getAttribute("accession");
						retour.setStudyExternalId(code, externalId);
						break; 
						} 
					case "SAMPLE"    : {
						retour.setSampleAccession(code, accession);
						final Element eltExtId = (Element) elt.getElementsByTagName("EXT_ID").item(0);
						String externalId = eltExtId.getAttribute("accession");
						retour.setSampleExternalId(code, externalId);
						break;
						}
					case "EXPERIMENT":
						retour.setExperimentAccession(code, accession);		
						break;
					case "RUN"       :
						retour.setRunAccession(code, accession);	
						break;
					default :
						// Ignore other nodes
					}
				}
			}  // end for  
		} catch (final ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
			throw new RuntimeException("io error while parsing xml ",e);
		} 	
		if (! ebiSuccess ) {
			logger.debug("pattern 'success=true' absent du fichier  {}", ebiFileAc);
			throw new SraException("Fichier retour de l'EBI " + pathEbiFileAc + " qui ne contient pas la ligne success=true");
		} else {
			logger.debug("Parsing du fichier {} ok ", ebiFileAc);
		}
		return retour;
	}
	
	
	public Study EbiFetchStudy(String accession) {		
		try {
			String xmlStudies = ebiAPI.ebiXml(accession, "studies");
			RepriseHistorique repriseHistorique = new RepriseHistorique();
//			Iterable<Study> listStudies = repriseHistorique.forStudies(xmlStudies);
//			// la string xml correspond a un objet study, on aura donc en retour un seul objet study
//			if (listStudies.iterator().hasNext()) {
//				return listStudies.iterator().next();
//			}
//			return null;
			return Iterables.first(repriseHistorique.forStudies(xmlStudies)).orElse(null);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Experiment EbiFetchExperiment(String accession) {	
		try {
			String xmlExperiments = ebiAPI.ebiXml(accession, "experiments");
			RepriseHistorique repriseHistorique = new RepriseHistorique();
			Iterable<Experiment> listExperiments = repriseHistorique.forExperiments(xmlExperiments);
			if (listExperiments.iterator().hasNext()) {
				return listExperiments.iterator().next();
			}
			return null;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
			
	public Run EbiFetchRun(String accession) throws IOException, SraException, ParserConfigurationException, ParseException {		
		String xmlRuns = ebiAPI.ebiXml(accession, "runs");
		RepriseHistorique repriseHistorique = new RepriseHistorique();
		Iterable<Run> listRuns = repriseHistorique.forRuns(xmlRuns);
		// la string xml correspond a un seul objet run, on aura donc en retour un seul objet 
		if (listRuns.iterator().hasNext()) {
			return listRuns.iterator().next();
		}
		return null;
	}
		
	public Sample EbiFetchSample(String accession) throws IOException, SraException, ParserConfigurationException, ParseException {		
		String xmlSamples = ebiAPI.ebiXml(accession, "samples");
		RepriseHistorique repriseHistorique = new RepriseHistorique();
		Iterable<Sample> listSamples = repriseHistorique.forSamples(xmlSamples);  
		if (listSamples.iterator().hasNext()) {
			return listSamples.iterator().next();
		}
		return null;
	}	
	
	@Override
	public void execute(Args args) throws Exception {
		// Recuperation des Ac de la soumission :
		InfosEbi infosEbi = parseEbiFileAc(args.ebiFileAc);
		String user = "william";
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
		State state = new State("F-SUB", user);
		
		
		
		// creation de l'objet soumission
		Submission submission   = new Submission();
		submission.code         = infosEbi.getSubmissionCode();
		submission.creationDate = dateFormat.parse(infosEbi.getSubmissionDate());
		submission.accession    = infosEbi.getSubmissionAccession();
		submission.creationUser = user;
		submission.release      = false;
		submission.submissionDirectory = undifinedValueInRepriseHistorique;
		submission.state               = state;
		submission.adminComment        = adminComment;		
		//submission.configCode          = undifinedValueInRepriseHistorique;
		submission.traceInformation.setTraceInformation(user);
		println("submission.code " + infosEbi.getSubmissionCode());
		
		// Recuperation des objets SRA correspondant à la soumission
		//Set <Study> allStudies = new HashSet<>();
		Set <Sample> allSamples = new HashSet<>();
//		Set <Experiment> allExperiments = new HashSet<>();
		Set <Run> allRuns = new HashSet<>();
//		Set <Readset> allReadsets = new HashSet<>();
//		for (String acStudy : infosEbi.getStudyAccessions()) {
//			Study study = EbiFetchStudy(acStudy);
//			study.adminComment = comment;
//			allStudies.add(study);
//		}
		
//		Iterable <Study> allStudies_ =
//				Iterables.map(infosEbi.getStudyAccessions(), acStudy -> EbiFetchStudy(acStudy)) // retourne un zenIterables
//		         		 .each(study -> study.adminComment = comment);
		
		
		// Toutes les methodes de Iterables retournent un zenIterable.
		
		// Verifier la validité des objets SRA
		ContextValidation contextValidation = ContextValidation.createCreationContext(user);

		// Attention la map devrait prendre une fonction pure : fonction qui retourne toujours le meme resulstat
		// si memme arguments utilisés, hors ici EbiFetchStudy appelé 2 fois avec meme AC en argument retourna 2 objets
		// avec memes valeurs mais adresse differente, d'ou probleme ensuite si utilisation de 2 each à suivre car 
		// alors implicitement 2 appels de map. 
//		Iterable <Study> allStudies = Iterables.zen(infosEbi.getStudyAccessions())
//        .map(acStudy -> EbiFetchStudy(acStudy))
//        .each(study -> study.adminComment = comment)
//        .each(study -> {
//       	 // update du study pour champs specifiques qui n'apparaissent pas à l'EBI :
//       	 study.state = state;
//       	 study.traceInformation.setTraceInformation(user);
//       	 study.adminComment = adminComment;
//       	 // mise à jour de l'objet soumission pour studyCode :
//			 submission.studyCode = study.code;
//			 if (! submission.refStudyCodes.contains(study.code)) {
//				 submission.refStudyCodes.add(study.code);
//			 }
//			 // validation du study en mode creation
//       	 study.validate(contextValidation);
//       	 println("study.state.code = "+study.state.code);
//        });
		  
		
		Iterable <Study> allStudies = Iterables.zen(infosEbi.getStudyAccessions())
		         .map(acStudy -> EbiFetchStudy(acStudy))
		         .toList(); // On fixe la liste des studies, on n'enchaine pas les map et ou les each car ici
							// utilisation d'une methode EbiFetchStudy dans map qui n'est pas une fonction "pure".
		
		
		for (Study study : allStudies) {
			// update du study pour champs specifiques qui n'apparaissent pas à l'EBI :
			study.state = state;
			study.traceInformation.setTraceInformation(user);
			study.adminComment = adminComment;
			study.externalId = infosEbi.getStudyExternalId(study.code);
			String[] virtualProjectCodes = study.code.split("_");
			for (String virtualProjectCode : virtualProjectCodes) {
				if (virtualProjectCode.matches("^[A-Z]+$") && ! virtualProjectCode.equalsIgnoreCase("STUDY")) {
					if (!study.projectCodes.contains(virtualProjectCode)) {
						study.projectCodes.add(virtualProjectCode);
					}
					if (!submission.projectCodes.contains(virtualProjectCode)) {
						submission.projectCodes.add(virtualProjectCode);
					}
				}
			}
			// mise à jour de l'objet soumission pour studyCode :
			submission.studyCode = study.code;
			if (! submission.refStudyCodes.contains(study.code)) {
				submission.refStudyCodes.add(study.code);
			}
			
			// validation du study en mode creation
			study.validate(contextValidation);
		}
		
		for (String acSample : infosEbi.getSampleAccessions()) {
			if(StringUtil.isBlank(acSample)) {
				continue;
			}
			Sample sample = EbiFetchSample(acSample);
			// update du sample pour champs specifiques qui n'apparaissent pas à l'EBI :
			sample.state = state;
			sample.traceInformation.setTraceInformation(user);
			sample.adminComment = adminComment;
			NCBITaxon ncbiTaxon = taxonomyServices.getNCBITaxon("" + sample.taxonId).toCompletableFuture().get();
			sample.scientificName = ncbiTaxon.getScientificName();
			sample.externalId = infosEbi.getSampleExternalId(sample.code);
			//logger.debug("sample.scientificName = " + sample.scientificName);
			//println("sample.code = " + sample.code +", sample.scientificName = " + sample.scientificName);
			
			// mise à jour de l'objet soumission pour sampleCode :			
			if (! submission.sampleCodes.contains(sample.code)) {
				submission.sampleCodes.add(sample.code);
			}
			if (! submission.refSampleCodes.contains(sample.code)) {
				submission.refSampleCodes.add(sample.code);
			}
			if (!submission.projectCodes.contains(sample.projectCode)) {
				submission.projectCodes.add(sample.projectCode);
			}
			 // validation du sample en mode creation

			
			sample.validate(contextValidation);
			allSamples.add(sample);	
		}
		
//		ZenIterable <Experiment> allExperiments = Iterables.zen(infosEbi.getExperimentAccessions())
//				.map(acExperiment -> EbiFetchExperiment(acExperiment))
//				.each(experiment -> {
//					experiment.adminComment = comment;
//					experiment.validateInvariantsNoRun(contextValidation);
//					println(experiment.typePlatform);
//					println(experiment.readSetCode);
//					println(experiment.traceInformation.createUser);
//					//println(experiment.traceInformation.creationDate);
//					println(experiment.state.user);
//					println(experiment.state.code);					
//				});

//      instable	:	allExperiments = Iterables.map(infosEbi.getExperimentAccessions(), acExperiment -> EbiFetchExperiment(acExperiment));		
//      idem mais stable car toList():
//		ZenIterable <Experiment> allExperiments = 
//				Iterables.zen(Iterables.zen(infosEbi.getExperimentAccessions()) // ZenIterable <String>
//				         .map(acExperiment -> EbiFetchExperiment(acExperiment)).toList()); // String -> Experiment 
//		allExperiments =  Iterables.filter(allExperiments, exp -> exp.code != null);		
//	
		
//		Good :
		ZenIterable <Experiment> allExperiments = 
				Iterables.zen(infosEbi.getExperimentAccessions()) 
				         .map(acExperiment -> EbiFetchExperiment(acExperiment))
				         .stable()  // on stabilise (equivalent du toList())
				         .filter(exp -> exp.code != null);
//      Good en plus court
//		ZenIterable <Experiment> allExperiments = 
//				Iterables.map(infosEbi.getExperimentAccessions(), acExperiment -> EbiFetchExperiment(acExperiment))
//				         .stable()  // on stabilise (equivalent du toList())
//				         .filter(exp -> exp.code != null);
		
		// map s'applique sur le zenIterable de String zen(infosEbi.getExperimentAccessions())
		
		
		List<Readset> allReadsets = new ArrayList<>();
		
		for (Experiment experiment: allExperiments) {
			// update de l'experiment pour champs specifiques qui n'apparaissent pas à l'EBI :
			experiment.state = state;
			experiment.traceInformation.setTraceInformation(user);
			experiment.adminComment = adminComment;	
			// mise à jour de l'objet soumission pour experimentCode et projectCode:			
			if (!submission.experimentCodes.contains(experiment.code)) {
				submission.experimentCodes.add(experiment.code);
			}
			if (!submission.projectCodes.contains(experiment.projectCode)) {
				submission.projectCodes.add(experiment.projectCode);
			}
			 // validation de l'experiment en mode creation :
			experiment.validateInvariantsNoRun(contextValidation);	
			
			// creation du readset (collection readset de ngl-sub)
			Readset readset = new Readset();
			readset.code = experiment.readSetCode;
			readset.experimentCode = experiment.code;
			readset.type = experiment.typePlatform.toLowerCase();
			allReadsets.add(readset);
		}
		
		
//		ZenIterable <Readset> allReadsets = allExperiments
//				.map(experiment -> {
//					Readset readset = new Readset();
//					readset.code = experiment.readSetCode;
//					readset.experimentCode = experiment.code;
//					//readset.runCode = experiment.
//					readset.type = experiment.typePlatform.toLowerCase();
//					return readset;
//				});
		

//		for (String acExperiment : infosEbi.getExperimentAccessions()) {
//			Experiment experiment = EbiFetchExperiment(acExperiment);
//			experiment.adminComment = comment;
//			experiment.validateInvariantsNoRun(contextValidation);
//			allExperiments.add(experiment);
//			println(experiment.typePlatform);
//			println(experiment.readSetCode);
//			println(experiment.traceInformation.createUser);
//			//println(experiment.traceInformation.creationDate);
//			println(experiment.state.user);
//			println(experiment.state.code);
//			Readset readset = new Readset();
//			readset.code = experiment.readSetCode;
//			readset.experimentCode = experiment.code;
//			//readset.runCode = experiment.
//			readset.type = experiment.typePlatform.toLowerCase();
//			allReadsets.add(readset);
//		}

		for (String acRun : infosEbi.getRunAccessions()) {
			if(StringUtil.isBlank(acRun)) {
				continue;
			}
			Run run = EbiFetchRun(acRun);
			// update du run pour champs specifiques qui n'apparaissent pas à l'EBI :
			run.adminComment = adminComment;
			for (RawData rawData : run.listRawData) {
				rawData.directory = undifinedValueInRepriseHistorique;
				rawData.location  = undifinedValueInRepriseHistorique;
			}
			
			// mise à jour de l'objet soumission pour runCode:			
			if (!submission.runCodes.contains(run.code)) {
				submission.runCodes.add(run.code);
			}
			 // validation du run en mode creation :
			run.validate(contextValidation);
			allRuns.add(run);
		}
		
		submission.validate(contextValidation);
		
		if (contextValidation.hasErrors()) {
			contextValidation.displayErrors(logger);
			println(Json.prettyPrint(app.errorsAsJson(contextValidation.getErrors())));
		} else {			
			for (Study study : allStudies) {			
				studyAPI.dao_saveObject(study);
			}
			for (Sample sample : allSamples) {
				sampleAPI.dao_saveObject(sample);
			}			
			for (Experiment experiment : allExperiments) {
				experimentAPI.dao_saveObject(experiment);
			}
			for (Run run : allRuns) {
				Experiment experiment =  experimentAPI.dao_getObject(run.expCode);
				run.expAccession = experiment.accession;
				experimentAPI.dao_update(DBQuery.is("code", run.expCode),
						DBUpdate.set("run", run));
			}			
			for (Readset readset : allReadsets) {
				Experiment experiment = experimentAPI.dao_getObject(readset.experimentCode);
				// Mise à jour de l'objet readset pour runCode (une fois que le run a ete sauve dans son experiment) :
				readset.runCode = experiment.run.code;
				if ( ! readset.type.equalsIgnoreCase("ls454") ) {
					if (! readsetsAPI.isObjectExist(readset.code)) {
						throw new SraException("readset " + readset.code+ " de type " + readset.type + " absent de la table ReadSetIllumina");
					} 
					// updater collection readset exp_2.GAC.ALI_AFKORS_H3FLAJF02de ngl-seq si autre que 454 :
					readsetAPI.dao_update(DBQuery.is("code", readset.code), 
						DBUpdate.set("submissionState.code", F_SUB)
						.set("traceInformation.modifyUser", user)
						.set("traceInformation.modifyDate", new Date()));
				}
				// Creer le readset dans la collection sra_readset:
				if (! readsetAPI.dao_checkObjectExist("code", readset.code)) {
					readsetAPI.dao_saveObject(readset);
				}
			}
			logger.debug("submission.accession" + submission.accession);
			submissionAPI.dao_saveObject(submission);
		}	
	}
}
