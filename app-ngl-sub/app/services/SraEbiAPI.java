package services;



import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import fr.cea.ig.ngl.NGLConfig;
import models.sra.submit.sra.instance.EbiIdentifiers;
import models.sra.submit.util.EbiAPI;
import play.libs.ws.WSClient;


public class SraEbiAPI extends EbiAPI{

	// On demande à play d'instancier la classe en lui fournissant un objet WSClient
	@Inject
	public SraEbiAPI(WSClient      ws,
				     NGLConfig     config) {
		super(ws, config);
	}

//  Verifier si donnée publique existe à l'EBI : 
//	https://www.ebi.ac.uk/ena/data/view/ERR2196986
//  Verifier si donnée existe dans notre drop-box à l'EBI (publique ou privée) en s'identifiant :
//	https://www.ebi.ac.uk/ena/submit/drop-box/studies/ERP109506?format=xml&auth=ENA%20Webin-9%20Axqw16nI
	



	/**
	 * Renvoie l'objet EbiIdentifiers correspondant au numeros d'accession (ou externalId) indiqué du sample, study ou project 
	 *  s'il existe sur le browser de l'EBI. 
	 * @param accession   : numeros d'accession ou externalId du sample ou study ou project
	 * @return            : EbiIdentifiers: objet de stoquage des identifiants de l'objet recupere a l'EBI
	 * @throws ParserConfigurationException : exception
	 */		
	public EbiIdentifiers EbiBrowserFetchEbiIdentifiers(String accession) throws ParserConfigurationException  {		
		String xmlSra = ebiBrowserXml(accession);
		EbiIdentifiers ebiIdentifiers = null;
		if(StringUtils.isBlank(xmlSra) || xmlSra.contains(accession + " not found")) {
			return null;
		}
		Iterable<EbiIdentifiers> listEbiIdentifiers = XmlToSra.xmlSraToEbiIdentifiers(xmlSra);  
		if(listEbiIdentifiers == null) {
			return null;
		}
		if (listEbiIdentifiers.iterator().hasNext()) {
			ebiIdentifiers = listEbiIdentifiers.iterator().next();
		}
		return ebiIdentifiers;
	}	
	
}
