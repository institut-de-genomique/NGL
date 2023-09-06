package sra.scripts;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import models.sra.submit.sra.instance.Sample;
import models.utils.InstanceConstants;
import services.SraEbiAPI;
import sra.scripts.utils.DateTools;
import sra.scripts.utils.Tools;
import sra.scripts.utils.iteration.CSVIterable;



public class OriRepriseHistoSample_extId extends ScriptNoArgs {

	/* version 1 : on demande à play d'instancier la classe avec un objet WSClient et on instantiera 
	un objet EbiAPI avec ws
	private final WSClient ws;	
	@Inject
	public RepriseHistoSample_extId(WSClient ws) {
		this.ws = ws;
	}
	*/
	
	// version 2 : On demande à play d'instancier la classe avec un objet EbiAPI
//	private final EbiAPI ebiAPI;	

	@Inject
	public OriRepriseHistoSample_extId(SraEbiAPI ebiAPI) {
//		this.ebiAPI = ebiAPI;
	}
	
//	private static class SampleInfos {
//		String accession;
//		String extIdAccession;		
//	}
	
	@Override
	public void execute() throws IOException, ParseException {

		List <Sample> dbSamples = MongoDBDAO.find(InstanceConstants.SRA_SAMPLE_COLL_NAME, Sample.class, DBQuery.in("_type", "Sample")).toList();
		Set <String> dbSampleAcs = new HashSet<>();

		for (Sample dbSample : dbSamples){
			//dbSampleCodes.add(dbSample.accession + "-" + dbSample.code);
			if (StringUtils.isNotBlank(dbSample.accession)) {
				dbSampleAcs.add(dbSample.accession);
			}
		}
		
		Set <String> sampleAcs = new HashSet<>();		//Set <String> studyCodes = new HashSet<String>(); 
		
//		File ebiFile = new File("/env/cns/home/sgas/repriseHistoExtId/repriseHistoSample.ori.csv");
//		//CSVParser csvParser = CSVParser.parse(ebiFile, Charset.forName("UTF-8"), CSVFormat.EXCEL.withDelimiter(';'));
//		CSVParser csvParser = CSVParsing.parse(ebiFile, ';');

//		execute(new CSVIterable(csvParser));
//	}
//	
//	
//	
//	public void execute2() throws IOException, ParseException {
		File ebiFile = new File("/env/cns/home/sgas/repriseHistoExtId/repriseHistoSample.ori.csv");
		//new CSVIterable(ebiFile,';'));
	
		boolean first = true;
		int cp = 0;	
	
		for (List<String> record : new CSVIterable(ebiFile,';')) {
			
//		}
//		for (CSVRecord  record : csvParser) {
			if (first) { // ignorer premier ligne correspondant à legende.
				first = false;
				continue;
			}
			cp++;
			// marche ssi pas de % dans args:
			//printf(csvRecord.get(0) + "  " + csvRecord.get(1) + "  " + csvRecord.get(4));
			// facon correcte d'ecrire
			//printf("%s  |  %s  |  %s  |  %s\n", record.get(0), record.get(1), record.get(3), record.get(4));
			String accession = record.get(0);
			String extId = record.get(1);
			String code = record.get(3);
			String oriDate = record.get(4);
			
			// Ignorer les samples TARA de Pesant 
			if (code.matches("^TARA_[a-zA-Z0-9]{10,11}$")){
				continue;
			}
			//sampleCodes.add(accession + "-" + code);
			sampleAcs.add(accession);
			//String [] tmp = oriDate.split("-");
			//Date date = DateFormat.getDateInstance(DateFormat.SHORT).parse(tmp[0]+ "/"+DateTools.monthOrdinal(tmp[1])+"/20"+tmp[2]);
			Date date = DateTools.dmy("-", oriDate);
			
			//print(DateFormat.getDateInstance(DateFormat.SHORT).format(new Date()));
			//Date date = DateFormat.getDateInstance(DateFormat.SHORT).parse("1/1/18");
			printfln("%s  |  %s  |  %s  |  %s", accession, extId, code, date);

			Date today = new Date();
			
			Sample sample = MongoDBDAO.findOne(InstanceConstants.SRA_SAMPLE_COLL_NAME,
					Sample.class, DBQuery.and(DBQuery.is("accession", accession)));
			
			if (sample == null) {
				printfln("***************ERREUR pour le sample %d avec code=%s et accession =%s qui n'existe pas dans base", cp, code, accession);
				continue;
			} 
			
			if (sample._type.equals("ExternalSample")) {
				printfln("***************ERREUR pour le sample %d avec code=%s et accession =%s qui existe dans base comme ExternalSample", cp, code, accession);
				continue;
			} 
			
			MongoDBDAO.update(InstanceConstants.SRA_SAMPLE_COLL_NAME, Sample.class, 
					DBQuery.is("accession", accession),
					DBUpdate.set("externalId", extId).set("traceInformation.creationDate", date).set("traceInformation.modifyDate", today));
			
//			if (sample != null) {
				printfln("update ok  " + cp);
//			}
					
		}
		
		// Controle coherence :
		Set<String> absentFichier = Tools.subtract(dbSampleAcs, sampleAcs);
		printfln("************* %d codes presents dans base et absents du fichier d'entree *****************",absentFichier.size());
		for (String sampleAc : absentFichier) {
			printfln("%s",sampleAc);
		}

		Set<String> absentBase = Tools.subtract(sampleAcs, dbSampleAcs);
		printfln("************* %d codes presents dans fichier d'entree et absents dans base *****************",absentBase.size());
		for (String sampleAc : absentBase) {
			printfln("%s",sampleAc);
		}
		
	
	}
		
	@Override
	public LogLevel logLevel() {
		return LogLevel.INFO;
	}
	
}
