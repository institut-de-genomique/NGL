package sra.scripts.generic;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;

import controllers.sra.api.AbstractSampleAPI;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.MongoDBResult;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import models.sra.submit.sra.instance.AbstractSample;
import models.utils.InstanceConstants;
import sra.services.SraEbiAPI;
import sra.services.Tools;
import validation.ContextValidation;

/*
 * Script a lancer pour mettre à jour les ExternalSample pour les identifiants ERS (ou SRS)  ou ERP
 * Exemple de lancement :
 * http://localhost:9000/sra/scripts/run/sra.scripts.generic.RepriseHistoExternalSample
 * @author sgas
 *
 */
public class RepriseHistoExternalSample extends ScriptNoArgs {
	private final SraEbiAPI        sraEbiAPI;
	private final AbstractSampleAPI abstractSampleAPI;

	@Inject
	public RepriseHistoExternalSample(
			SraEbiAPI        sraEbiAPI,
			AbstractSampleAPI abstractSampleAPI) {
		this.sraEbiAPI = sraEbiAPI;
		this.abstractSampleAPI = abstractSampleAPI;
	}
	
	final String user = "ngsrg";
	final Date courantDate = new Date();
	private static final play.Logger.ALogger logger = play.Logger.of(RepriseHistoExternalSample.class);


	
	@Override
	public void execute() throws IOException, ParseException, ParserConfigurationException, APIValidationException, APIException {
		// Recuperation de l'ensemble des studies soumis (stade SUB-F) 
		ContextValidation contextValidation = ContextValidation.createUpdateContext(user);
		MongoDBResult<AbstractSample> dbAbstractSamples =  MongoDBDAO
				.find(InstanceConstants.SRA_SAMPLE_COLL_NAME, AbstractSample.class, DBQuery.in("_type","ExternalSample")
						.and(DBQuery.or(DBQuery.notExists("accession"), DBQuery.notExists("externalId"))));
		int count = 0;
		int countCorrectif = 0;
		for(AbstractSample absSample : dbAbstractSamples.cursor) {
			ContextValidation ctxVal = ContextValidation.createUpdateContext(user);
			count++;
			if(StringUtils.isBlank(absSample.accession) || (! absSample.accession.startsWith("ERS") && ! absSample.accession.startsWith("SRS"))
					|| StringUtils.isBlank(absSample.externalId) || ! absSample.externalId.startsWith("SAM")) {
				//logger.debug("appel de completeSampleIdentifier pour {}", absSample.code);
				if(sraEbiAPI.completeSampleIdentifier(absSample)) {
					// sauvegarde du sample complété pour les identifiants :
					absSample.traceInformation.modifyDate = courantDate;
					absSample.traceInformation.modifyUser = user;
					//logger.debug("YYYYYYYYYYY   webin-9 externalId = "+ absSample.externalId);
					//logger.debug("YYYYYYYYYYY   webin-9 accession = "+ absSample.accession);

					List <AbstractSample> absSampleList_1 = abstractSampleAPI.dao_find(DBQuery.in("code", absSample.code)).toList();
					if (absSampleList_1.size() > 1) {
						ctxVal.addError("sample." + absSample.code,"existence en base de plusieurs samples avec code=" + absSample.code);
					} 					
					List <AbstractSample> absSampleList_2 = abstractSampleAPI.dao_find(DBQuery.in("externalId", absSample.externalId)).toList();
					if (absSampleList_2.size() > 1) {
						ctxVal.addError("sample." + absSample.code,"existence en base de plusieurs samples avec externalId=" + absSample.externalId);
					} 
					List <AbstractSample> absSampleList_3 = abstractSampleAPI.dao_find(DBQuery.in("accession", absSample.accession)).toList();
					if (absSampleList_3.size() > 1) {
						ctxVal.addError("sample." + absSample.code,"existence en base de plusieurs samples avec accession=" + absSample.accession);
					} 
					absSample.validate(ctxVal);
					if( ! ctxVal.hasErrors()) {
						countCorrectif++;
						logger.debug("mise a jour dans base du sample " + absSample.code);
						//logger.debug("YYYYYYYYYYY           sauvegarde dans base du sample {}", absSample.code);
						abstractSampleAPI.update(absSample, user);
//						abstractSampleAPI.dao_update(DBQuery.is("code", absSample.code),
//								DBUpdate.set("accession", absSample.accession)
//								.set("externalId", absSample.externalId)
//								.set("traceInformation.modifyUser", user)
//								.set("traceInformation.modifyDate", courantDate)); 
					} 
				} else {
					ctxVal.addError("sample." + absSample.code, "probleme pour recuperer les identifiant du sample a l'EBI");
				}
				Tools.addErreurCxtBInCtxA(contextValidation, ctxVal);
			}
		}
		println("Fin du traitement, parcours de "+ count + " samples et correction en base de "+ countCorrectif + " samples ");
		logger.debug("Fin du traitement, parcours de {} samples et correction en base de {} samples ", count, countCorrectif);
		
		if(contextValidation.hasErrors()) {
			println("voir messages d'errreurs ci-dessous");
			println(contextValidation);
		}
	}

	
	@Override
	public LogLevel logLevel() {
		return LogLevel.DEBUG;
	}
	
	
}
