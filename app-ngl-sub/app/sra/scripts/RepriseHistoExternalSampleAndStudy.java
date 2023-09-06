package sra.scripts;



import java.util.Date;

//import java.util.Iterator;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;


import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import fr.cea.ig.ngl.dao.api.sra.AbstractSampleAPI;
import fr.cea.ig.ngl.dao.api.sra.AbstractStudyAPI;
import models.sra.submit.sra.instance.AbstractSample;
import models.sra.submit.sra.instance.AbstractStudy;
import models.sra.submit.sra.instance.EbiIdentifiers;
import models.sra.submit.sra.instance.Sample;
import models.sra.submit.sra.instance.Study;
import models.sra.submit.util.SraException;
import services.SraEbiAPI;

/*
 * Script à utiliser pour ajouter externalId sur les externalSample et externalStudy.
 * {@code http://localhost:9000/sra/scripts/run/sra.scripts.RepriseHistoExternalSampleAndStudy}
 * <br>
 * Si parametre absent dans url => declenchement d'une erreur.
 *  
 * @author sgas
 *
 */
public class RepriseHistoExternalSampleAndStudy extends ScriptNoArgs {
	private final SraEbiAPI ebiAPI;
	private final AbstractStudyAPI  abstractStudyAPI;
	private final AbstractSampleAPI abstractSampleAPI;

	
	@Inject
	public RepriseHistoExternalSampleAndStudy(
				SraEbiAPI             ebiAPI,
				AbstractStudyAPI   abstractStudyAPI,
				AbstractSampleAPI  abstractSampleAPI
				) {
		this.ebiAPI        = ebiAPI;
		this.abstractStudyAPI      = abstractStudyAPI;
		this.abstractSampleAPI     = abstractSampleAPI;
		
	}

//	
//	public Sample EbiFetchSample(String accession, Date submissionDate) throws IOException, SraException, ParserConfigurationException, ParseException {		
//		String xmlSamples = ebiAPI.ebiXml(accession, "samples");
//		println (xmlSamples);
//		XmlToSra repriseHistorique = new XmlToSra();
//		Iterable<Sample> listSamples = repriseHistorique.forSamples(xmlSamples, submissionDate);  
//		if (listSamples.iterator().hasNext()) {
//			return listSamples.iterator().next();
//		}
//		return null;
//	}	
//	
//	public Sample EbiBrowserFetchSample(String accession, Date submissionDate) throws IOException, SraException, ParserConfigurationException, ParseException {		
//		String xmlSamples = ebiAPI.ebiBrowserXml(accession);
//		//println (xmlSamples);
//		XmlToSra repriseHistorique = new XmlToSra();
//		Iterable<Sample> listSamples = repriseHistorique.forSamples(xmlSamples, submissionDate);  
//		if (listSamples.iterator().hasNext()) {
//			return listSamples.iterator().next();
//		}
//		return null;
//	}	
//	
//	
//	public Study EbiBrowserFetchStudy(String accession, Date submissionDate) throws IOException, SraException, ParserConfigurationException, ParseException {		
//		String xmlStudies = ebiAPI.ebiBrowserXml(accession);
//		//println (xmlStudies);
//		XmlToSra repriseHistorique = new XmlToSra();
//		Iterable<Study> listStudies = repriseHistorique.forStudies(xmlStudies, submissionDate);  
//		if (listStudies.iterator().hasNext()) {
//			return listStudies.iterator().next();
//		}
//		return null;
//	}	
//	
//	public EbiIdentifiers EbiBrowserFetchEbiIdentifiers(String accession) throws IOException, SraException, ParserConfigurationException, ParseException {		
//		String xmlSra = ebiAPI.ebiBrowserXml(accession);
//		Iterable<EbiIdentifiers> listEbiIdentifiers = XmlToSra.xmlSraToEbiIdentifiers(xmlSra);  
//		if (listEbiIdentifiers.iterator().hasNext()) {
//			return listEbiIdentifiers.iterator().next();
//		}
//		return null;
//	}	
//	
	

	@Override
	public void execute() throws Exception {

		Date courantDate = new Date();
		String user = "ngsrg";
		
		Iterable<AbstractSample> list_externalSample = abstractSampleAPI.dao_all();
		int count_externalSample = 0;
		for (AbstractSample dbSample : list_externalSample) {
			if(dbSample instanceof Sample) {
				continue;
			}
			// on ne travaille que sur les externalSample pour qui il manque un identifiant :
			if (StringUtils.isNotBlank(dbSample.accession) && StringUtils.isNotBlank(dbSample.externalId) ) {
				// inutile d'interroger l'EBI, on a les 2 identifiants
				continue;
			}				
			if (StringUtils.isBlank(dbSample.accession) && StringUtils.isBlank(dbSample.externalId) ) {
				throw new SraException("Le sample de la base " + dbSample.code + " ne contient ni AC ni externalId ???? ");
			}
			// recuperer sample de l'EBI :
			
			// le sample de la base a un ERS, il faut recuperer le sample à l'EBI et son SAM
			if (StringUtils.isNotBlank(dbSample.accession)) {
				//println(dbSample.code);
				if ( ! dbSample.accession.startsWith("ERS") ) {
					throw new SraException("Le sample de la base " + dbSample.code + " contient un AC qui ne commence pas par ERS " + dbSample.accession);	
				}
				EbiIdentifiers ebiIdentifiers = ebiAPI.EbiBrowserFetchEbiIdentifiers(dbSample.accession);
				// corriger dbSample et stocker pour validation et sauvegarde en base:
				if (ebiIdentifiers == null ) {
					println("Aucun sample recupere a l'EBI pour  " + dbSample.accession);
					continue;
				}
				if ( ! ebiIdentifiers.getAccession().startsWith("ERS")) {
					throw new SraException("Le sample recupere de l'EBI a un numeros d'accession qui ne commence pas par ERS " + ebiIdentifiers.getAccession());
				}
				if ( ! ebiIdentifiers.getExternalId().startsWith("SAM")) {
					throw new SraException("Le sample recupere de l'EBI a un externalId qui ne commence pas par SAM " + ebiIdentifiers.getExternalId());
				}	
				count_externalSample++;
				dbSample.externalId = ebiIdentifiers.getExternalId();
				dbSample.traceInformation.modifyUser = user;
				dbSample.traceInformation.modifyDate = courantDate;
				abstractSampleAPI.dao_saveObject(dbSample);
			}
			// le sample de la base a un SAM, il faut recuperer le sample a l'EBI et son ERS
			if (StringUtils.isNotBlank(dbSample.externalId)) {
				if ( ! dbSample.externalId.startsWith("SAM") ) {
					throw new SraException("Le sample de la base " + dbSample.code + " contient un externalId qui ne commence pas par SAM " + dbSample.externalId);	
				}
				EbiIdentifiers ebiIdentifiers = ebiAPI.EbiBrowserFetchEbiIdentifiers(dbSample.externalId);
				// corriger dbSample et stocker pour validation et sauvegarde en base:
				if (ebiIdentifiers == null ) {
					println("Aucun sample recupere a l'EBI pour  " + dbSample.externalId);
					continue;
				}
				if ( ! ebiIdentifiers.getAccession().startsWith("ERS")) {
					throw new SraException("Le sample recupere de l'EBI a un numeros d'accession qui ne commence pas par ERS " + ebiIdentifiers.getAccession());
				}
				if ( ! ebiIdentifiers.getExternalId().startsWith("SAM")) {
					throw new SraException("Le sample recupere de l'EBI a un externalId qui ne commence pas par SAM " + ebiIdentifiers.getExternalId());
				}	
				count_externalSample++;
				dbSample.accession = ebiIdentifiers.getAccession();
				dbSample.traceInformation.modifyUser = user;
				dbSample.traceInformation.modifyDate = courantDate;
				abstractSampleAPI.dao_saveObject(dbSample);
			}	
		}	
		println("Sauvegarde de " + count_externalSample + " externalSample");

		Iterable<AbstractStudy> list_externalStudy = abstractStudyAPI.dao_all();
		int count_externalStudy = 0;
		for (AbstractStudy dbStudy : list_externalStudy) {
			if(dbStudy instanceof Study) {
				continue;
			}
			// on ne travaille que sur les externalStudy pour qui il manque un identifiant :
			if (StringUtils.isNotBlank(dbStudy.accession) && StringUtils.isNotBlank(dbStudy.externalId) ) {
				// inutile d'interroger l'EBI, on a les 2 identifiants
				continue;
			}				
			if (StringUtils.isBlank(dbStudy.accession) && StringUtils.isBlank(dbStudy.externalId) ) {
				throw new SraException("Le study de la base " + dbStudy.code + " ne contient ni AC ni externalId");
			}
			// le study de la base a un ERP, il faut recuperer le study à l'EBI et son PRJ
			if (StringUtils.isNotBlank(dbStudy.accession)) {	
				if ( ! dbStudy.accession.startsWith("ERP") ) {
					throw new SraException("Le study de la base " + dbStudy.code + " contient un AC qui ne commence pas par ERP " + dbStudy.accession);	
				}
				// on recupere à l'EBI le xml d'un study et les identifiers associés:
				EbiIdentifiers ebiIdentifiers = ebiAPI.EbiBrowserFetchEbiIdentifiers(dbStudy.accession);
				// corriger dbStudy et stocker pour validation et sauvegarde en base:
				if (ebiIdentifiers == null ) {
					println("Aucun study recuperé pour  " + dbStudy.accession);
					continue;
				}
				if ( ! ebiIdentifiers.getAccession().startsWith("ERP")) {
					throw new SraException("Le study recupere de l'EBI a un numeros d'accession qui ne commence pas par ERP " + ebiIdentifiers.getAccession());
				}
				if ( ! ebiIdentifiers.getExternalId().startsWith("PRJ")) {
					throw new SraException("Le study recupere de l'EBI a un externalId qui ne commence pas par PRJ " + ebiIdentifiers.getExternalId());
				}	
				count_externalStudy++;
				dbStudy.externalId = ebiIdentifiers.getExternalId();
				dbStudy.traceInformation.modifyUser = user;
				dbStudy.traceInformation.modifyDate = courantDate;
				abstractStudyAPI.dao_saveObject(dbStudy);
			}
			// le study de la base a un PRJEB, il faut recuperer le project à l'EBI et son ERP
			if (StringUtils.isNotBlank(dbStudy.externalId)) {
				if ( ! dbStudy.externalId.startsWith("PRJ") ) {
					throw new SraException("Le study de la base " + dbStudy.code + " contient un externalId qui ne commence pas par PRJ " + dbStudy.externalId);
				}
				// on recupere à l'EBI le xml d'un project et les identifiers associés:
				EbiIdentifiers ebiIdentifiers = ebiAPI.EbiBrowserFetchEbiIdentifiers(dbStudy.externalId);
				// corriger dbStudy et stocker pour validation et sauvegarde en base:
				if (ebiIdentifiers == null ) {
					println("Aucun project recuperé pour  " + dbStudy.externalId);
					continue;
				}
				if ( ! ebiIdentifiers.getAccession().startsWith("PRJ")) {
					throw new SraException("Le project recupere de l'EBI a un numeros d'accession qui ne commence pas par PRJ " + ebiIdentifiers.getAccession());
				}
				if ( ! ebiIdentifiers.getExternalId().startsWith("ERP")) {
					throw new SraException("Le project recupere de l'EBI a un externalId qui ne commence pas par ERP " + ebiIdentifiers.getExternalId());
				}	
				count_externalStudy++;
				dbStudy.accession = ebiIdentifiers.getExternalId(); 
				dbStudy.traceInformation.modifyUser = user;
				dbStudy.traceInformation.modifyDate = courantDate;
				abstractStudyAPI.dao_saveObject(dbStudy);
			}			
		}	
		println("Sauvegarde de " + count_externalStudy + " externalStudy");
		
		println("Fin du traitement");
	}
		
	
//	/**
//	 * Recupere à l'EBI, le project correspondant au studyCode passé en parametre
//	 * et l'insere dans collection ngl-sub.project
//	 * @param  studyCode          code de l'objet study
//	 */
//	@Deprecated
//	public void loadProjectFromEbi(String studyCode) {	
//		if (StringUtils.isBlank(studyCode)) {
//			return;
//		}
//		Study study = studyDAO.getObject(studyCode);
//		if (study == null) {
//			logger.debug("Dans loadProjectFromEbi : le studyCode " + studyCode + " n'existe pas dans la base\n");
//			return;
//		}
//		try {
//			String xmlProjects = ebiXml(study.externalId, "projects");
//			XmlToSra repriseHistorique = new XmlToSra();
//			
//			Project project = Iterables.first(repriseHistorique.forProjects(xmlProjects, study.firstSubmissionDate)).orElse(null);
//			project.traceInformation = new TraceInformation();
//			project.traceInformation.createUser = study.traceInformation.createUser;
//			project.traceInformation.creationDate = study.firstSubmissionDate;
//			project.state = new State(SUB_F, study.traceInformation.createUser);
//			project.externalId = study.accession;
//			for(String projectCode : study.projectCodes) {
//				if(project.projectCodes == null) {
//					project.projectCodes = new ArrayList<String>();
//				}
//				if (! project.projectCodes.contains(projectCode)) {
//					project.projectCodes.add(projectCode);
//				}
//			}
//			ContextValidation contextValidation = ContextValidation.createCreationContext(project.traceInformation.createUser);
//			project.validate(contextValidation);
//			if(contextValidation.hasErrors()) {
//				//contextValidation.displayErrors(logger);
//				throw new SraValidationException(contextValidation);
//			} else {
//				projectDAO.save(project);
//			}
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//	}
//	
	
//	/**
//	 * Recupere à l'EBI, le project correspondant au projectAccession passé en parametre
//	 * et l'insere dans collection ngl-sub.project.
//	 * Attention si project umbrella, le xml de creation envoyé à l'EBI contient bien les projects enfants 
//	 * mais le xml visible à l'EBI ne contient plus les projects enfants
//	 * @param  projectAccession    accession de type PRJ... permettant d'identifier le project
//	 * @param  date                Date de la soumission du project umbrella
//	 * @param  user                user ayant realisé la soumission
//	 */
//	@Deprecated
//	public void loadUmbrellaFromEbi(String projectAccession, String user, Date date) {	
//		if (StringUtils.isBlank(projectAccession)) {
//			return;
//		}
//
//		try {
//			String xmlProjects = ebiXml(projectAccession, "projects");
//			XmlToSra repriseHistorique = new XmlToSra();
//			
//			if( date== null) {
//				date = new Date();
//			}
//			Project project = Iterables.first(repriseHistorique.forProjects(xmlProjects, date)).orElse(null);
//			project.traceInformation = new TraceInformation();
//			
//			project.traceInformation.createUser = user;
//			project.traceInformation.creationDate = date;
//			project.state = new State(SUB_F, user);
//			
//			ContextValidation contextValidation = ContextValidation.createCreationContext(project.traceInformation.createUser);
//			project.validate(contextValidation);
//			if(contextValidation.hasErrors()) {
//				contextValidation.displayErrors(logger);
//				throw new SraValidationException(contextValidation);
//			} else {
//				projectDAO.save(project);
//			}
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//	}
//	
//	
}