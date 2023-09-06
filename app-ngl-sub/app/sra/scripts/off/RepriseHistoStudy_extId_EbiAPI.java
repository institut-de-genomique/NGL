package sra.scripts.off;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import javax.inject.Inject;

import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import models.sra.submit.sra.instance.Study;
import models.utils.InstanceConstants;
import services.SraEbiAPI;

public class RepriseHistoStudy_extId_EbiAPI extends ScriptNoArgs {
	private SraEbiAPI ebiAPI;

	@Inject
	public RepriseHistoStudy_extId_EbiAPI(SraEbiAPI ebiAPI) {
		this.ebiAPI = ebiAPI;
	}
	
	@Override
	public void execute() throws IOException, ParseException {
		// Recuperation de l'ensemble des studies de type Study (et non externalStudy) 
		// avec champs accession renseigné et champs externalId non renseigné :		
		List <Study> dbStudies = MongoDBDAO.find(InstanceConstants.SRA_STUDY_COLL_NAME, Study.class, 
				DBQuery.in("_type", "Study").notExists("externalId").exists("accession")).toList();
		printfln("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx  Nombre de study sans externalId = " + dbStudies.size());
		for (Study dbStudy : dbStudies) {
			printfln(dbStudy.accession + " --  " + dbStudy.code );
			ebiAPI.ebiStudyExists(dbStudy.accession);
			String xmlStudy = ebiAPI.ebiStudyXml(dbStudy.accession);
			
		}
	}
	
	@Override
	public LogLevel logLevel() {
		return LogLevel.DEBUG;
	}
	
}
