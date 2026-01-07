package sra.scripts.generic;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;

import controllers.sra.api.AnalysisAPI;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.MongoDBResult;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import models.sra.submit.sra.instance.Analysis;
import models.utils.InstanceConstants;
import sra.services.SraEbiAPI;
import validation.ContextValidation;

/*
 * Script a lancer pour mettre à jour les analysis pour les identifiants ERS ou ERP
 * Exemple de lancement :
 * http://localhost:9000/sra/scripts/run/sra.scripts.generic.RepriseHistoExternalAnalysis
 * @author sgas
 *
 */
public class RepriseHistoAnalysis extends ScriptNoArgs {
	//private final SraEbiAPI        sraEbiAPI;
	private final AnalysisAPI      analysisAPI;

	@Inject
	public RepriseHistoAnalysis(
			//SraEbiAPI        sraEbiAPI,
			AnalysisAPI      analysisAPI) {
		//this.sraEbiAPI   = sraEbiAPI;
		this.analysisAPI = analysisAPI;
	}
	
	final String user = "ngsrg";
	final Date courantDate = new Date();
	private static final play.Logger.ALogger logger = play.Logger.of(RepriseHistoAnalysis.class);


	
	@Override
	public void execute() throws IOException, ParseException, ParserConfigurationException, APIValidationException, APIException {
		// Recuperation de l'ensemble des studies soumis (stade SUB-F) 
		ContextValidation contextValidation = ContextValidation.createUpdateContext(user);
		MongoDBResult<Analysis> dbAnalysis =  MongoDBDAO
				.find(InstanceConstants.SRA_ANALYSIS_COLL_NAME, Analysis.class);
	

		int count = 0;
		int countCorrectif = 0;
		for(Analysis analysis : dbAnalysis.cursor) {
			ContextValidation ctxVal = ContextValidation.createUpdateContext(user);
			count++;
			analysis.studyIdentifier = analysis.studyIdentifier;
			analysis.sampleIdentifier = analysis.sampleIdentifier;
			//analysis.studyAccession = null;
			//analysis.sampleAccession = null;
			analysis.traceInformation.modifyDate = courantDate;
			analysis.traceInformation.modifyUser = user;
			analysis.validate(ctxVal);
			if( ! ctxVal.hasErrors()) {
				countCorrectif++;
				logger.debug("YYYYYYYYYYY           sauvegarde dans base de l'analyse {}", analysis.code);
				analysisAPI.update(analysis, user);
			}
			
		}
		println("Fin du traitement, parcours de "+ count + " analysis et correction en base de "+ countCorrectif + " analysis ");
		logger.debug("Fin du traitement, parcours de {} analysis et correction en base de {} analysis ", count, countCorrectif);
		
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
