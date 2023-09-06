package sra.scripts.off;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import models.sra.submit.sra.instance.Study;
import models.utils.InstanceConstants;
import sra.scripts.utils.CSVParsing;
import sra.scripts.utils.DateTools;
import sra.scripts.utils.Tools;

public class RepriseHistoStudy_extId extends ScriptNoArgs {
	
	@Override
	public void execute() throws IOException, ParseException {
		// Recuperation de l'ensemble des studies de type Study (et non externalStudy)
		List <Study> dbStudies = MongoDBDAO.find(InstanceConstants.SRA_STUDY_COLL_NAME, Study.class, DBQuery.in("_type", "Study")).toList();
		// Construire la liste des codes des studies de type Study dans base et soumis à l'EBI (avec AC) 
		Set <String> dbStudyCodes = new HashSet<>();
		for (Study dbStudy : dbStudies){
			if(StringUtils.isNotBlank(dbStudy.accession)) {
				if (! dbStudyCodes.contains(dbStudy.code)){
					dbStudyCodes.add(dbStudy.code);
				}
			}	
		}
		
		Set <String> studyCodes = new HashSet<>();		//Set <String> studyCodes = new HashSet<String>(); 
		// a ne pas confondre avec un hash generique

		File ebiFile = new File("/env/cns/home/sgas/repriseHistoExtId/repriseHistoStudyEbi.csv");
		//CSVParser csvParser = CSVParsing.parse(ebiFile, Charset.forName("UTF-8"), CSVFormat.EXCEL.withDelimiter(';'));
		CSVParser csvParser = CSVParsing.parse(ebiFile, ';');

		boolean first = true;
		int cp = 0;
		for (CSVRecord  record : csvParser) {
			if (first){
				first = false;
				continue;
			}
			cp++;

			String extId = record.get(0);
			String code = record.get(1);
			String mode = record.get(2); // 2 si private et 4 si public
			String releaseDateEbi = record.get(3);
			String firstCreatedDateEbi = record.get(4);
			String firstPublicDateEbi = record.get(6);
			
			if (! studyCodes.contains(code)) {
				studyCodes.add(code);
			}

				
			//String [] tmp = firstCreatedDateEbi.split("-");
			//Date createdDate = DateFormat.getDateInstance(DateFormat.SHORT).parse(tmp[0]+ "/"+DateTools.monthOrdinal(tmp[1])+"/20"+tmp[2]);
//			Date today = new Date();
			Date createdDate = DateTools.dmy("-", firstCreatedDateEbi);
			Date releaseDate = null;
			
			if (StringUtils.isNotBlank(releaseDateEbi)) {
				releaseDate =  DateTools.dmy("-", releaseDateEbi);
				printfln("extId=%s  |  code=%s  |  mode=%s  |  releaseDate=%s|  createdDate=%s| createdDateEBI=%s", extId, code, mode, releaseDate, createdDate, firstCreatedDateEbi);
			} else if (StringUtils.isNotBlank(firstPublicDateEbi)) {
				releaseDate = DateTools.dmy("-", firstPublicDateEbi);
				printfln("extId=%s  |  code=%s  |  mode=%s  |  releaseDate=%s|  createdDate=%s | createdDateEBI=%s ", extId, code, mode, releaseDate, createdDate, firstCreatedDateEbi);
			} else {
				printfln("PAS DE RELEASE DATE POUR CODE %s ? ??????", code);
			}
			
				
			Study study = MongoDBDAO.findOne(InstanceConstants.SRA_STUDY_COLL_NAME,
					Study.class, DBQuery.and(DBQuery.is("code", code)));
			
			if (study == null) {
				printfln("***************ERREUR pour le study %d avec code=%s qui n'existe pas dans base", cp, code);
				continue;
			}
			
		}
		
		//Set<String> toto = Tools.subtract(new ArrayList<>(), studyCodes);
		Set<String> absentFichier = Tools.subtract(dbStudyCodes, studyCodes);
		printfln("************* codes dans base et absent fichier  *****************\n");

		for (String studyCode : absentFichier) {
			printfln(studyCode);
				
		}
		
		Set<String> absentBase = Tools.subtract(studyCodes, dbStudyCodes);
		printfln("************* codes dans fichier et absents dans base  *****************\n");

		for (String studyCode : absentBase) {
			printfln(studyCode);
				
		}
				
		
	}
	
	@Override
	public LogLevel logLevel() {
		return LogLevel.DEBUG;
	}
	
}
