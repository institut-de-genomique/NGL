package sra.scripts.generic;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;

import controllers.sra.api.AbstractStudyAPI;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.MongoDBResult;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import models.sra.submit.sra.instance.AbstractStudy;
import models.utils.InstanceConstants;
import sra.services.SraEbiAPI;
import sra.services.Tools;
import validation.ContextValidation;

/*
 * Script a lancer pour mettre à jour les study pour les identifiants ERS ou ERP
 * Exemple de lancement :
 * http://localhost:9000/sra/scripts/run/sra.scripts.generic.RepriseHistoExternalStudy
 * @author sgas
 *
 */
public class RepriseHistoExternalStudy extends ScriptNoArgs {
	private final SraEbiAPI        sraEbiAPI;
	private final AbstractStudyAPI abstractStudyAPI;

	@Inject
	public RepriseHistoExternalStudy(
			SraEbiAPI        sraEbiAPI,
			AbstractStudyAPI abstractStudyAPI) {
		this.sraEbiAPI = sraEbiAPI;
		this.abstractStudyAPI = abstractStudyAPI;
	}
	
	final String user = "ngsrg";
	final Date courantDate = new Date();
	private static final play.Logger.ALogger logger = play.Logger.of(RepriseHistoExternalStudy.class);


	
	@Override
	public void execute() throws IOException, ParseException, ParserConfigurationException, APIValidationException, APIException {
		// Recuperation de l'ensemble des studies soumis (stade SUB-F) 
		ContextValidation contextValidation = ContextValidation.createUpdateContext(user);
		MongoDBResult<AbstractStudy> dbAbstractStudies =  MongoDBDAO
				.find(InstanceConstants.SRA_STUDY_COLL_NAME, AbstractStudy.class, DBQuery.in("_type","ExternalStudy").
						and(DBQuery.or(DBQuery.notExists("accession"), DBQuery.notExists("externalId"))));

		int count = 0;
		int countCorrectif = 0;
		for(AbstractStudy absStudy : dbAbstractStudies.cursor) {
			ContextValidation ctxVal = ContextValidation.createUpdateContext(user);
			count++;
			if(StringUtils.isBlank(absStudy.accession) || ! absStudy.accession.startsWith("ERP")
					|| StringUtils.isBlank(absStudy.externalId) || ! absStudy.externalId.startsWith("PRJ")) {
				//logger.debug("appel de completeStudyIdentifier pour {}", absStudy.code);
				if(sraEbiAPI.completeStudyIdentifier(absStudy)) {
					// sauvegarde du study complété pour les identifiants :
					absStudy.traceInformation.modifyDate = courantDate;
					absStudy.traceInformation.modifyUser = user;
					//logger.debug("YYYYYYYYYYY   webin-9 externalId = "+ absStudy.externalId);
					//logger.debug("YYYYYYYYYYY   webin-9 accession = "+ absStudy.accession);

					logger.debug("mise a jour dans base du study " + absStudy.code);
					List <AbstractStudy> absStudyList_1 = abstractStudyAPI.dao_find(DBQuery.in("code", absStudy.code)).toList();
					if (absStudyList_1.size() > 1) {
						ctxVal.addError("study." + absStudy.code,"existence en base de plusieurs studies avec code=" + absStudy.code);
					} 					
					List <AbstractStudy> absStudyList_2 = abstractStudyAPI.dao_find(DBQuery.in("externalId", absStudy.externalId)).toList();
					if (absStudyList_2.size() > 1) {
						ctxVal.addError("study." + absStudy.code,"existence en base de plusieurs studies avec externalId=" + absStudy.externalId);
					} 
					List <AbstractStudy> absStudyList_3 = abstractStudyAPI.dao_find(DBQuery.in("accession", absStudy.accession)).toList();
					if (absStudyList_3.size() > 1) {
						ctxVal.addError("study." + absStudy.code,"existence en base de plusieurs studies avec accession=" + absStudy.accession);
					} 
					absStudy.validate(ctxVal);
					if( ! ctxVal.hasErrors()) {
						countCorrectif++;
						//logger.debug("YYYYYYYYYYY           sauvegarde dans base du study {}", absStudy.code);
						abstractStudyAPI.update(absStudy, user);
//						abstractStudyAPI.dao_update(DBQuery.is("code", absStudy.code),
//								DBUpdate.set("accession", absStudy.accession)
//								.set("externalId", absStudy.externalId)
//								.set("traceInformation.modifyUser", user)
//								.set("traceInformation.modifyDate", courantDate)); 
					} 
				} else {
					ctxVal.addError("study." + absStudy.code, "probleme pour recuperer les identifiant du study dans Webin-9");
				}
				Tools.addErreurCxtBInCtxA(contextValidation, ctxVal);
			}
		}
		println("Fin du traitement, parcours de "+ count + " study et correction en base de "+ countCorrectif + " study ");
		logger.debug("Fin du traitement, parcours de {} study et correction en base de {} study ", count, countCorrectif);
		
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
