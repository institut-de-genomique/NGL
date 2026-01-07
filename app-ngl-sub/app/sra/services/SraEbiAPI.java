package sra.services;



import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.MongoDBResult;
import models.sra.submit.sra.instance.AbstractSample;
import models.sra.submit.sra.instance.AbstractStudy;
import models.sra.submit.sra.instance.EbiIdentifiers;
import models.sra.submit.sra.instance.EbiStudyIdentifiersInExperiment;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.sra.instance.Sample;
import models.sra.submit.sra.instance.Study;
import models.utils.InstanceConstants;
import play.libs.ws.WSClient;
import services.sra.EbiAPI;
import services.sra.NGLSUBConfig;
import services.sra.SraException;


public class SraEbiAPI extends EbiAPI{

	//private final WSClient ws;
	//private final NGLConfig  config;
	// On demande à play d'instancier la classe en lui fournissant un objet WSClient
	@Inject
	public SraEbiAPI(WSClient         ws) {
		super(ws);
		//this.ws     = ws;
		//this.config = config;
	}

//  Verifier si donnée publique existe à l'EBI : 
//	https://www.ebi.ac.uk/ena/data/view/ERR2196986
//  Verifier si donnée existe dans notre drop-box à l'EBI (publique ou privée) en s'identifiant :
//	https://www.ebi.ac.uk/ena/submit/drop-box/studies/ERP109506?format=xml&auth=ENA%20Webin-9%20Axqw16nI
	

	private static final play.Logger.ALogger logger = play.Logger.of(SraEbiAPI.class);


	/**
	 * Renvoie l'objet EbiIdentifiers correspondant au numeros d'accession (ou externalId) indiqué du sample, study ou project 
	 *  s'il existe sur le browser de l'EBI. 
	 * @param accession   : numeros d'accession ou externalId du sample ou study ou project
	 * @return            : EbiIdentifiers: objet de stoquage des identifiants de l'objet recupere a l'EBI
	 * @throws ParserConfigurationException : exception
	 */		
	public EbiIdentifiers ebiBrowserFetchEbiIdentifiers(String accession) throws ParserConfigurationException  {
		//logger.debug("Dans EbiBrowserFetchEbiIdentifiers avec accession en arg = '"+ accession + "'");
		String xmlSra = ebiBrowserXml(accession);
		EbiIdentifiers ebiIdentifiers = null;
		if(StringUtils.isBlank(xmlSra) || xmlSra.contains(accession + " not found")) {
			return null;
		}
		//logger.debug("gggggggggggggggg              xmlSra=" + xmlSra);
		Iterable<EbiIdentifiers> listEbiIdentifiers = XmlToSra.xmlSraToEbiIdentifiers(xmlSra);  
		//logger.debug("hhhhhhhhhhhhhhhhhh              " );


		if(listEbiIdentifiers == null) {
			return null;
		}
		if (listEbiIdentifiers.iterator().hasNext()) {
			ebiIdentifiers = listEbiIdentifiers.iterator().next();
		}
		return ebiIdentifiers;
	}	
	
	public EbiStudyIdentifiersInExperiment webin9ExperimentStudyIdentifiers(String expAccession) throws ParserConfigurationException  {		
		//logger.debug("expAccession='{}'", expAccession);
		String xml = ebiExperimentXml(expAccession);
		EbiStudyIdentifiersInExperiment ebiStudyIdentifiersInExperiment = null;
		if(StringUtils.isBlank(xml) || xml.contains(expAccession + " not found")) {
			//logger.debug("expAccession='{}' introuvable sur webin-9", expAccession);
			return null;
		}
		//logger.debug("gggggggggggggggg              xmlSra=" + xml);
		Iterable<EbiStudyIdentifiersInExperiment> listEbiStudyIdentifiersInExperiment = XmlToSra.xmlSraToEbiStudyIdentifiersInExperiment(xml);  
		//logger.debug("hhhhhhhhhhhhhhhhhh              " );


		if(listEbiStudyIdentifiersInExperiment == null) {
			return null;
		}
		if (listEbiStudyIdentifiersInExperiment.iterator().hasNext()) {
			ebiStudyIdentifiersInExperiment = listEbiStudyIdentifiersInExperiment.iterator().next();
		}
		return ebiStudyIdentifiersInExperiment;
	}	
	
	
	public boolean completeStudyIdentifier(AbstractStudy abstStudy) throws ParserConfigurationException {
		logger.debug("Dans completeStudyIdentifier avec abstStudy.code='{}'", abstStudy.code);
		// Rechercher les identifiants du study à l'EBI :
		// Il faut passer par les experiments de notre drop-box pour acceder aux 2 identifiants du study dans le cas ou seul le PRJ est connu au depart
		MongoDBResult<Experiment> dbExperiments =  MongoDBDAO
				.find(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, 
					  Experiment.class, 
					  DBQuery.in("studyCode", abstStudy.code).and(DBQuery.exists("accession").and(DBQuery.in("state.code", "SUB-F"))));
		@SuppressWarnings("unused")
		int count = 0;
		boolean completed = false;
		for(Experiment experiment : dbExperiments.cursor) {
			count++;
			if (experiment != null) {
				// on recupere sur webin-9 le xml d'un experiment et les identifiers associés:
				//logger.debug("appel de webin9ExperimentStudyIdentifiers avec expAccession='{}'", experiment.accession);
				EbiStudyIdentifiersInExperiment ebiStudyIdentifiersInExperiment = this.webin9ExperimentStudyIdentifiers(experiment.accession);
				if(StringUtils.isNotBlank(ebiStudyIdentifiersInExperiment.getStudyAccession()) 
						&& StringUtils.isNotBlank(ebiStudyIdentifiersInExperiment.getStudyExternalId())) {
					completed = true;
					//logger.debug("identifiants du study trouves avec exp " + count);
					abstStudy.externalId = ebiStudyIdentifiersInExperiment.getStudyExternalId();
					//logger.debug("XXXXXXXXXXXXXXXXX   webin-9 externalId = "+ abstStudy.externalId);
					abstStudy.accession = ebiStudyIdentifiersInExperiment.getStudyAccession();
					//logger.debug("XXXXXXXXXXXXXXXXX   webin-9 accession = "+ abstStudy.accession);

					//abstStudy.traceInformation.modifyUser = user;
					//abstStudy.traceInformation.modifyDate = courantDate;
					break;	
				}
			}
		}
		return completed;
	}
	
	public boolean completeSampleIdentifier(AbstractSample abstSample) throws ParserConfigurationException {
		// Rechercher les identifiants du sample à l'EBI :

		boolean completed = false;
		String sampleIdentifier = null;
		if(StringUtils.isNotBlank(abstSample.accession)) {
			sampleIdentifier = abstSample.accession;
		} else if(StringUtils.isNotBlank(abstSample.externalId)) {
			sampleIdentifier = abstSample.externalId;
		} else {
			throw new SraException("abstSample donne en entree sans accession ni externalId");
		}
		//logger.debug("sampleIdentifier='{}'", sampleIdentifier);
		EbiIdentifiers ebiIdentifiers = ebiBrowserFetchEbiIdentifiers(sampleIdentifier);
		if (ebiIdentifiers != null 
				&& StringUtils.isNotBlank(ebiIdentifiers.getAccession()) 
				&& StringUtils.isNotBlank(ebiIdentifiers.getExternalId())) {
			completed = true;
			abstSample.accession = ebiIdentifiers.getAccession();
			abstSample.externalId = ebiIdentifiers.getExternalId();
		}
		//logger.debug("completed='{}'", completed);
		return completed;
	}
	

	public Sample ebiBrowserFetchSample(String accession) throws IOException, SraException, ParserConfigurationException, ParseException {		
		String xmlSamples = this.ebiBrowserXml(accession);
		Iterable<Sample> listSamples = XmlToSra.xmlToSample(xmlSamples);  
		if (listSamples.iterator().hasNext()) {
			return listSamples.iterator().next();
		}
		return null;
	}	
	
	public Study ebiBrowserFetchStudy(String accession) throws IOException, SraException, ParserConfigurationException, ParseException {		
		String xmlStudies = this.ebiBrowserXml(accession);
		Iterable<Study> listStudies = XmlToSra.xmlToStudy(xmlStudies);  
		if (listStudies.iterator().hasNext()) {
			return listStudies.iterator().next();
		}
		return null;
	}	
	
	
}
