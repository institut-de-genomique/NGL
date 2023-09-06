package sra.scripts;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;

import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import fr.cea.ig.ngl.dao.api.sra.SubmissionAPI;
import models.sra.submit.sra.instance.EbiIdentifiers;
import models.sra.submit.sra.instance.Sample;
import models.sra.submit.sra.instance.Study;
import models.sra.submit.util.SraException;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import services.SraEbiAPI;
import services.XmlToSra;

/*
 * Script à utiliser pour recharger les md5 d'une soumission a partir de NGL.
 * {@code http://localhost:9000/sra/scripts/run/sra.scripts.TestGetEbiAccessionExternalId}
 * <br>
 * Si parametre absent dans url => declenchement d'une erreur.
 *  
 * @author sgas
 *
 */
public class TestGetEbiAccessionExternalId extends ScriptNoArgs {
	private static final play.Logger.ALogger logger = play.Logger.of(TestGetEbiAccessionExternalId.class);
	private final SubmissionAPI     submissionAPI;
	private final SraEbiAPI ebiAPI;
	private final WSClient ws;
	
	@Inject
	public TestGetEbiAccessionExternalId(SubmissionAPI submissionAPI,
				SraEbiAPI        ebiAPI,
				WSClient      ws
				) {
		this.submissionAPI = submissionAPI;
		this.ebiAPI        = ebiAPI;
		this.ws            = ws;

		
	}

	private String ebiBrowserXml(String ac) {
		String url = String.format("https://www.ebi.ac.uk/ena/browser/api/xml/%s?download=true", ac);
		//logger.debug("ebiXml -- url : {}", url);
		//ws.url(https://www.ebi.ac.uk/ena/submit/drop-box/samples/$ac?format=xml);
		WSRequest wr = ws.url(url);
		//wr.setAuth("Webin-9", "Axqw16nI");
		CompletionStage<WSResponse> homePage = wr.get();		
		CompletionStage<String> xml = 
			homePage.thenApplyAsync(response -> {
			//logger.info("response XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"+response.getBody());
			return response.getBody();
		});
		try {
			String doc = xml.toCompletableFuture().get();
			return doc;
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}	
	}
	
	
	public Sample EbiFetchSample(String accession, Date submissionDate) throws IOException, SraException, ParserConfigurationException, ParseException {		
		String xmlSamples = ebiAPI.ebiXml(accession, "samples");
		println (xmlSamples);
		XmlToSra repriseHistorique = new XmlToSra();
		Iterable<Sample> listSamples = repriseHistorique.forSamples(xmlSamples, submissionDate);  
		if (listSamples.iterator().hasNext()) {
			return listSamples.iterator().next();
		}
		return null;
	}	
	
	public Sample EbiBrowserFetchSample(String accession, Date submissionDate) throws IOException, SraException, ParserConfigurationException, ParseException {		
		String xmlSamples = ebiAPI.ebiBrowserXml(accession);
		//println (xmlSamples);
		XmlToSra repriseHistorique = new XmlToSra();
		Iterable<Sample> listSamples = repriseHistorique.forSamples(xmlSamples, submissionDate);  
		if (listSamples.iterator().hasNext()) {
			return listSamples.iterator().next();
		}
		return null;
	}	
	
	public Study EbiBrowserFetchStudy(String accession, Date submissionDate) throws IOException, SraException, ParserConfigurationException, ParseException {		
		String xmlStudies = ebiAPI.ebiBrowserXml(accession);
		//println (xmlStudies);
		XmlToSra repriseHistorique = new XmlToSra();
		Iterable<Study> listStudies = repriseHistorique.forStudies(xmlStudies, submissionDate);  
		if (listStudies.iterator().hasNext()) {
			return listStudies.iterator().next();
		}
		return null;
	}	
	
	

	@Override
	public void execute() throws Exception {
		Date courantDate = new Date();
		String user_id_sample ="ERS3727201";
		//String xmlRegister = ebiAPI.ebiBrowserXml(accession);
		String xmlRegister = ebiAPI.ebiXml(user_id_sample, "samples");
		println (xmlRegister);
		Sample sample = EbiBrowserFetchSample(user_id_sample, courantDate);
		printfln("user accession " + user_id_sample + ", ebiAccession = " + sample.accession + ", ebiExternalId = " + sample.externalId );

		user_id_sample ="ERS9877289";
		sample = EbiBrowserFetchSample(user_id_sample, courantDate);
		printfln("user accession " + user_id_sample + ", ebiAccession = " + sample.accession + ", ebiExternalId = " + sample.externalId );

		String user_id_study ="ERP112360";
		Study study = EbiBrowserFetchStudy(user_id_study, courantDate);
		printfln("user accession " + user_id_study + ", ebiAccession = " + study.accession + ", ebiExternalId = " + study.externalId );

		String user_id_project ="PRJEB29999";
		EbiIdentifiers ebiIdentifiers = ebiAPI.EbiBrowserFetchEbiIdentifiers(user_id_project);
		printfln("user accession " + user_id_project + ", ebiAccession = " + ebiIdentifiers.getAccession() + ", ebiExternalId = " + ebiIdentifiers.getExternalId() );

	}
		
		
}