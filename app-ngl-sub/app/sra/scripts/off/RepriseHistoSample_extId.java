package sra.scripts.off;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import fr.cea.ig.lfw.utils.ZenIterable;
import models.sra.submit.sra.instance.Sample;
import models.utils.InstanceConstants;
import services.SraEbiAPI;
import sra.scripts.utils.DateTools;
import sra.scripts.utils.Tools;
import sra.scripts.utils.iteration.CSVIterable;
import sra.scripts.utils.iteration.FileLineIterable;

public class RepriseHistoSample_extId extends ScriptNoArgs {
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
	public RepriseHistoSample_extId(SraEbiAPI ebiAPI) {
//		this.ebiAPI = ebiAPI;
	}
	
	/**
	 * Classe permettant de stoquer des informations lié au sample
	 * @author sgas
	 *
	 */
	private static class SampleInfos {
		private final String accession;
		private final String externalId;
		private final String optionalCode;
		private final Date   optionalCreationDate;

		/**
		 * 
		 * @param accession     numeros de la forme ERS...
		 * @param externalId    identifiant de la forme SAME...
		 * @param code          code de la forme sample_codeProjet....
		 * @param creationDate  date de creation
		 */
		private SampleInfos(String accession, String externalId, String code, Date creationDate) {
			this.accession 	          = accession;
			this.externalId           = externalId;
			this.optionalCode         = code;
			this.optionalCreationDate = creationDate;
		}
		
		/**
		 * 
		 * @param accession     numeros de la forme ERS...
		 * @param externalId    identifiant de la forme SAME...
		 */
		private SampleInfos(String accession, String externalId) {
			this(accession, externalId, null, null);
		}
//		public String getAccession()    {return accession;}
//		public String getExternalId()   {return externalId;}
//		public String getCode()         {return optionalCode;}
//		public Date getCreationDate() {return optionalCreationDate;}
//		public Optional<Date> getCreationDate() {
//			if (optionalCreationDate==null)
//				return Optional.empty();
//			return Optional.of(optionalCreationDate);
//		}
	
	}
	
	public Boolean updateDB(SampleInfos sampleInfos) {	
		//sampleCodes.add(accession + "-" + code);
		Date today = new Date();
		final String collectionName = InstanceConstants.SRA_SAMPLE_COLL_NAME;
		final Class<Sample> elementType = Sample.class;
		DBQuery.Query q = DBQuery.is("accession", sampleInfos.accession);
		
		Sample sample = MongoDBDAO.findOne(collectionName, elementType, q);

		if (sample == null) {
			printfln("***************ERREUR pour le sample avec code=%s et accession =%s qui n'existe pas dans base", sampleInfos.optionalCode, sampleInfos.accession);
		} else if (sample._type.equals("ExternalSample")) {
			printfln("***************ERREUR pour le sample avec code=%s et accession =%s qui existe dans base comme ExternalSample", sampleInfos.optionalCode, sampleInfos.accession);
		} else {
			DBUpdate.Builder u = DBUpdate.set("externalId", sampleInfos.externalId).set("traceInformation.modifyDate", today);

			if (sampleInfos.optionalCreationDate != null) 
				u = u.set("traceInformation.creationDate", sampleInfos.optionalCreationDate);
			MongoDBDAO.update(collectionName, elementType, q, u);
			return (true);
		}
		return (false);
	}	

	
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
	
		Function<List<String>, SampleInfos> function_1 =
				new Function<List<String>, SampleInfos>() {
					//boolean first = true;
					@Override
					public SampleInfos apply(List<String> record) {
						String accession = record.get(0);
						String extId = record.get(1);
						String code = record.get(3);
						String oriDate = record.get(4);
						Date date = DateTools.dmy("-", oriDate);
						SampleInfos sampleInfos = new SampleInfos(accession, extId, code, date);
						return sampleInfos;
					}
		};
		
		// Parsing fichier 1
		File ebiFile = new File("/env/cns/home/sgas/repriseHistoExtId/repriseHistoSample.ori.csv");
		ZenIterable <List<String>> ebi_1 = new CSVIterable(ebiFile,';');
		//Iterable <SampleInfos> sampleInfos = new MappingIterable<List<String>, SampleInfos>(lili_1, function_1);
		//Function <List<String>, Boolean> taraFilter = new Function <List<String>, Boolean>(){
//		Function <SampleInfos, Boolean> taraFilter = new Function <SampleInfos, Boolean>() {
//			@Override
//			public Boolean apply(SampleInfos sampleInfo) {
//				// Ignorer les samples TARA de Pesant
//				if (sampleInfo.optionalCode != null) {
//					return !sampleInfo.optionalCode.matches("^TARA_[a-zA-Z0-9]{10,11}$");
//				} else {
//					return true;
//				}
//			}	
//		};
		Predicate <SampleInfos> taraFilter = new Predicate <SampleInfos>() {
			@Override
			public boolean test(SampleInfos sampleInfo) {
				// Ignorer les samples TARA de Pesant
				if (sampleInfo.optionalCode != null) {
					return !sampleInfo.optionalCode.matches("^TARA_[a-zA-Z0-9]{10,11}$");
				} else {
					return true;
				}
			}	
		};
		
//		Function <SampleInfos, Boolean> taraFilter_lambda = 
//				sampleInfo -> sampleInfo.optionalCode == null || !sampleInfo.optionalCode.matches("^TARA_[a-zA-Z0-9]{10,11}$");
				
//		Iterable <List<String>> titi_A = ebi_1;
//		Iterable <List<String>> titi_B = skip  (titi_A, 1);
//		Iterable <SampleInfos>  titi_C = map   (titi_B, function_1);
//		Iterable <SampleInfos>  titi_D = filter(titi_C, taraFilter);
//		Iterable <SampleInfos> sampleInfos_1 = titi_D;
								
		ZenIterable <SampleInfos> sampleInfos_1 = ebi_1.skip(1).map(function_1).filter(taraFilter);
		// si on part d'un Iterable :
		//ZenIterable <SampleInfos> sampleInfos_1 = skip(ebi_1,1).map(function_1).filter(taraFilter);
		//ZenIterable <SampleInfos> sampleInfos_1 = zen(ebi_1).skip(1).map(function_1).filter(taraFilter);
//		int cp = 0;				
		for (SampleInfos sampleInfo : sampleInfos_1) {
//			if (sampleInfo == null) {
//				continue;
//			}
			sampleAcs.add(sampleInfo.accession);
//			cp++;
			updateDB(sampleInfo);
		}
		sampleInfos_1.each(sampleInfo -> {
			sampleAcs.add(sampleInfo.accession);
			//cp++; on ne peut pas utiliser var locale si elles ne sont pas finales car var definit 
			// dans methode qui peut ne plus etre visible ici si thread
			updateDB(sampleInfo);
		});

		//----------------------------------------------------
		// Parsing fichier 2
		File ebiFile2 = new File("/env/cns/home/sgas/repriseHistoExtId/ebi_2_repriseHistoSample.txt");
		
		FileLineIterable fileLineIterable = new FileLineIterable(ebiFile2);
		ZenIterable <List<String>> ebi_2 = fileLineIterable;
		
		Function<List<String>, SampleInfos> function_2 =
				new Function<List<String>, SampleInfos>() {
//					boolean first = true;
					@Override
					public SampleInfos apply(List<String> record) {
						String accession = record.get(0);
						String extId     = record.get(1);
						SampleInfos sampleInfos = new SampleInfos(accession, extId);
						return sampleInfos;
					}
		};
		
		//Iterable <SampleInfos> sampleInfo_2 = new MappingIterable<List<String>, SampleInfos>(ebi_2, function_2);
//		Iterable <SampleInfos> sampleInfo_2 = map(ebi_2, function_2);		
//		for (SampleInfos sampleInfo: sampleInfo_2) {
		//for (SampleInfos sampleInfo: map(ebi_2, function_2)) {
		for (SampleInfos sampleInfo: ebi_2.map(function_2)) {
			if (sampleInfo == null) {
				continue;
			}
			sampleAcs.add(sampleInfo.accession);
			printfln("%s  |  %s  ", sampleInfo.externalId, sampleInfo.accession);
//			cp++;
			updateDB(sampleInfo);
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
