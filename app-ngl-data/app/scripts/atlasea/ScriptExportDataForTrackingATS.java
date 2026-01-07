package scripts.atlasea;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.mongojack.DBQuery;

import com.mongodb.BasicDBObject;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.Script;
import models.laboratory.common.instance.TBoolean;
import models.laboratory.resolutions.instance.Resolution;
import models.laboratory.run.instance.Analysis;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.sample.instance.Sample;
import models.laboratory.sample.instance.reporting.SampleProcess;
import models.utils.InstanceConstants;
import play.Logger;
import scripts.ScriptImportDataCNS;
import validation.ContextValidation;

/**
 * Script permettant d'exporter les données du projets ATLASea pour alimenter la base de données externes de tracking ATLASea
 * 
 */
public class ScriptExportDataForTrackingATS extends Script<ScriptImportDataCNS.Args>{

	private static final String user = "ngl-support";
	public static class Args {

	}

	@Override
	public void execute(scripts.ScriptImportDataCNS.Args args) throws Exception {
		//Entete fichier excel
		ContextValidation cv = ContextValidation.createUpdateContext(user);
		cv.putObject("ExportTrackATS", new ArrayList<String>());
		((List<String>)cv.getObject("ExportTrackATS")).add("Nom taxon,Résolution,Short Read Sequencing,Long Read Sequencing,HiC Sequencing,RNA Sequencing,Assembling,Scaffolding,Curating,Reviewing,Submitting");

		//Récuperer tous les samples DKB les grouper par taxon (nom organisme collab)
		Logger.debug("Get sample DKB");
		Map<String, List<Sample>> mapTaxonSample = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.in("projectCodes", "DKB")).toList().stream().collect(Collectors.groupingBy(sample->sample.properties.get("collabScientificName").getValue().toString()));

		Logger.debug("Get specifique sample to debug");
		//Map<String, List<Sample>> mapTaxonSample = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.and(DBQuery.in("projectCodes", "DKB"),DBQuery.is("properties.collabScientificName.value", "Chondracanthus acicularis"))).toList().stream().collect(Collectors.groupingBy(sample->sample.properties.get("collabScientificName").getValue().toString()));

		for(String collabName : mapTaxonSample.keySet()) {
			String shortReadState=null;
			String longReadState=null;
			String hicState=null;
			String rnaState=null;
			String assemblyState=null;
			Map<String,String> assemblyStateStep = new HashMap<String, String>();

			List<Sample> samples = mapTaxonSample.get(collabName);
			//Get resolution
			Set<String> resolutions = samples.stream().map(s->s.resolutionCodes).filter(Objects::nonNull).flatMap(Set::stream).collect(Collectors.toSet());
			String resolutionsValues = resolutions.stream().map(o->o.toString()).collect(Collectors.joining(";"));

			//Get all processes from all sample
			List<SampleProcess> processes = samples.stream().map(s->s.processes).filter(Objects::nonNull).flatMap(List::stream).collect(Collectors.toList());

			//Infos short read sequencing
			//Etat prepared, sequenced, submitted
			shortReadState=getShortReadState(collabName, processes);

			//Infos long read sequencing
			//Etat prepared, sequenced, submitted
			List<ReadSet> readsetsLR = getReadsetsLR(collabName);
			longReadState=getLongReadState(collabName, processes, readsetsLR);

			//Infos HiC Sequencing 
			//Etat prepared, sequenced, submitted
			hicState=getHicState(collabName, processes);

			//Infos rna sequencing 
			//Etat prepared, sequenced, submitted
			rnaState=getRnaState(collabName, processes);

			//Infos assemblage
			assemblyStateStep=getAssemblyState(readsetsLR);

			//Etape Assembling, Scaffolding, Curating, Reviewing, Submitting
			//Attention cas de Caro avec assembling=topup et scaffolding=in waiting pas gérer dans modèle actuel
			Logger.debug(collabName+ " Short Read State "+shortReadState+ " Long read State "+longReadState+ " HiC State "+hicState+" RNA State "+rnaState+" Assembly State "+assemblyState);
			Logger.debug("Map step LRA "+assemblyStateStep.get("LRA")+" SCAF "+assemblyStateStep.get("SCAF")+" CUR "+assemblyStateStep.get("CUR")+" REV "+assemblyStateStep.get("REV")+" SUB "+assemblyStateStep.get("SUB"));

			//Export dans fichier excel
			((List<String>)cv.getObject("ExportTrackATS")).add(collabName+","+resolutionsValues+","+shortReadState+","+longReadState+","+hicState+","+rnaState+","+assemblyStateStep.get("LRA")+","+assemblyStateStep.get("SCAF")+","+assemblyStateStep.get("CUR")+","+assemblyStateStep.get("REV")+","+assemblyStateStep.get("SUB"));

		}
		//Create excel file to recap in execution directory
		createExcelFileRecap(cv);
	}

	Predicate<SampleProcess> checkSample = p->p.sampleOnInputContainer!=null;
	Predicate<SampleProcess> checkSampleProperties = p->p.sampleOnInputContainer!=null && p.sampleOnInputContainer.properties!=null;
	Predicate<SampleProcess> sampleTypeCodeDNA = checkSample.and(p->p.sampleOnInputContainer.sampleTypeCode.equals("DNA"));
	Predicate<SampleProcess> notHic = checkSampleProperties.and(p->!p.sampleOnInputContainer.properties.containsKey("samplePrepMethod") || (p.sampleOnInputContainer.properties.containsKey("samplePrepMethod") && !p.sampleOnInputContainer.properties.get("samplePrepMethod").getValue().toString().equals("Hi-C")));
	Predicate<SampleProcess> notTagPCR =  p->p.experiments!=null && p.experiments.stream().noneMatch(e->e.typeCode.equals("tag-pcr"));
	Predicate<SampleProcess> sampleDNA = sampleTypeCodeDNA.and(notHic).and(notTagPCR);
	Predicate<SampleProcess> notCDNASynthesis = p->p.experiments!=null && p.experiments.stream().noneMatch(e->e.typeCode.equals("cdna-synthesis"));
	Predicate<SampleProcess> hic = checkSampleProperties.and(p->p.sampleOnInputContainer.properties.containsKey("samplePrepMethod") && p.sampleOnInputContainer.properties.get("samplePrepMethod").getValue().toString().equals("Hi-C"));
	Predicate<SampleProcess> dnaIlluminaIndexedLibrary = p->p.experiments!=null && p.experiments.stream().anyMatch(e->e.typeCode.equals("dna-illumina-indexed-library"));
	Predicate<SampleProcess> dnaPacbioLibrary = p->p.experiments!=null && p.experiments.stream().anyMatch(e->e.typeCode.equals("dna-pacbio-library"));
	Predicate<SampleProcess> nanoporeFinalLigation = p->p.experiments!=null && p.experiments.stream().anyMatch(e->e.typeCode.equals("nanopore-final-ligation"));
	Predicate<SampleProcess> nanoporeLibrary = p->p.experiments!=null && p.experiments.stream().anyMatch(e->e.typeCode.equals("nanopore-library"));
	Predicate<SampleProcess> rnaIlluminaIndexedLibrary = p->p.experiments!=null && p.experiments.stream().anyMatch(e->e.typeCode.equals("rna-illumina-indexed-library") || e.typeCode.equals("rna-illumina-library"));


	private String getShortReadState(String collabName, List<SampleProcess> processes)
	{
		String shortReadState = null;
		shortReadState=getStateFromRSValidProdBioinfo("rsillumina", "DKC", collabName);
		if(shortReadState==null) {
			//Prepared : Recherche dans processes du sample si une experience dna-illumina-indexed-library existe, sampleTypeCode=DNA, samplePrepMethod not HiC
			//Filtre "isConditions:[{criteria:{experiments:{typeCode:'tag-pcr'}},expected:false}, {criteria:{experiments:{typeCode:'cdna-synthesis'}},expected:false},{criteria:{sampleOnInputContainer:{properties:{samplePrepMethod:{value:'Hi-C'}}}},expected:false},{criteria:{sampleOnInputContainer:{sampleTypeCode:'DNA'},experiments:{typeCode:'dna-illumina-indexed-library'}},expected:true}]"
			//Pas de notion de validité (uniquement validite sur experience) on peut rechercher la validité du container de sortie de l'expérience est ce suffisant
			//Short read prepared
			List<SampleProcess> processesFilteredPrepared = processes.stream()
					.filter(sampleDNA)
					.filter(notCDNASynthesis)
					.filter(dnaIlluminaIndexedLibrary)
					.filter(Objects::nonNull)
					.collect(Collectors.toList());
			Logger.debug("nb processes filtered short read prepared "+processesFilteredPrepared.size());
			if(processesFilteredPrepared.size()>0) {
				shortReadState="Prepared";
			}
		}
		return shortReadState;
	}

	private List<ReadSet> getReadsetsLR(String collabName)
	{
		List<ReadSet> readsetsLRPacBio = getReadSetsValidProd("rspacbio", "DKC", collabName);
		List<ReadSet> readsetsLR = getReadSetsValidProdBioinfo("rsnanopore", "DKC", collabName);
		readsetsLR.addAll(readsetsLRPacBio);
		return readsetsLR;
	}
	private String getLongReadState(String collabName, List<SampleProcess> processes, List<ReadSet> readsetsLR)
	{
		String longReadState=null;
		longReadState=getStateFromRS(readsetsLR);
		if(longReadState==null) {
			//Prepared
			//PacBio isConditions:[{criteria:{experiments:{typeCode:'tag-pcr'}},expected:false},{criteria:{sampleOnInputContainer:{properties:{samplePrepMethod:{value:'Hi-C'}}}},expected:false},{criteria:{sampleOnInputContainer:{sampleTypeCode:'DNA'},experiments:{typeCode:'dna-pacbio-library'}},expected:true}]
			//Nanopore "isConditions:[{criteria:{experiments:{typeCode:'tag-pcr'}},expected:false},{criteria:{sampleOnInputContainer:{properties:{samplePrepMethod:{value:'Hi-C'}}}},expected:false},{criteria:{sampleOnInputContainer:{sampleTypeCode:'DNA'},experiments:{typeCode:'nanopore-final-ligation'}},expected:true}]:[{criteria:{experiments:{typeCode:'tag-pcr'}},expected:false},{criteria:{sampleOnInputContainer:{properties:{samplePrepMethod:{value:'Hi-C'}}}},expected:false}{criteria:{sampleOnInputContainer:{sampleTypeCode:'DNA'},experiments:{typeCode:'nanopore-library'}},expected:true}]"

			List<SampleProcess> processesFilteredPreparedPacBio = processes.stream()
					.filter(sampleDNA)
					.filter(dnaPacbioLibrary)
					.filter(Objects::nonNull)
					.collect(Collectors.toList());

			List<SampleProcess> processesFilteredPreparedNanoporeLigation = processes.stream()
					.filter(sampleDNA)
					.filter(nanoporeFinalLigation)
					.filter(Objects::nonNull)
					.collect(Collectors.toList());
			
			List<SampleProcess> processesFilteredPreparedLR = processes.stream()
					.filter(sampleDNA)
					.filter(nanoporeLibrary)
					.filter(Objects::nonNull)
					.collect(Collectors.toList());

			processesFilteredPreparedLR.addAll(processesFilteredPreparedPacBio);
			processesFilteredPreparedLR.addAll(processesFilteredPreparedNanoporeLigation);
			if(processesFilteredPreparedLR.size()>0) {
				longReadState="Prepared";
			}
		}
		return longReadState;
	}

	private String getHicState(String collabName, List<SampleProcess> processes)
	{
		String hicState=null;
		hicState=getStateFromRSValidProdBioinfo("rsillumina", "DKF", collabName);
		if(hicState==null) {
			//Prepared  a reverifier
			//"isConditions:[{criteria:{sampleOnInputContainer:{properties:{samplePrepMethod:{value:'Hi-C'}}}},experiments:{typeCode:'dna-illumina-indexed-library'},expected:true}]",
			List<SampleProcess> processesFilteredPreparedHiC = processes.stream()
					.filter(dnaIlluminaIndexedLibrary)
					//.filter(hic)
					.filter(Objects::nonNull)
					.collect(Collectors.toList());
			if(processesFilteredPreparedHiC.size()>0) {
				hicState="Prepared";
			}
		}
		return hicState;
	}

	private String getRnaState(String collabName, List<SampleProcess> processes)
	{
		String rnaState=null;
		rnaState=getStateFromRSValidProdBioinfo("rsillumina", "DKE", collabName);
		if(rnaState==null) {
			//Prepared TODO reverifier
			//"isConditions:{$or:[[{criteria:{experiments:{typeCode:'rna-illumina-indexed-library'}},expected:true}],[{criteria:{experiments:{typeCode:'rna-illumina-library'}},expected:true}]]}",

			List<SampleProcess> processesFilteredPreparedRNA = processes.stream()
					.filter(rnaIlluminaIndexedLibrary)
					.filter(Objects::nonNull)
					.collect(Collectors.toList());
			if(processesFilteredPreparedRNA.size()>0) {
				rnaState="Prepared";
			}
		}
		return rnaState;
	}

	private Map<String,String> getAssemblyState(List<ReadSet> readsetsLR)
	{
		BasicDBObject keys = new BasicDBObject();
		keys.put("treatments", 0);
		List<String> stepAssembly = Arrays.asList("LRA","SCAF","CUR","REV","SUB");
		Map<String, String> codeLabelAssembly = new HashMap<String, String>(){{
			put("IP", "InProgress");
			put("F", "Done");
			put("FE", "Issue");
			put("IWP", "TopUp");
		}};

		Map<String,String> assemblyStateStep = new HashMap<String, String>();
		String assemblyState=null;

		List<String> codeReadSetsLR = readsetsLR.stream().map(r->r.code).collect(Collectors.toList());
		List<Analysis> assemblies = MongoDBDAO.find(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, 
				DBQuery.and(DBQuery.is("typeCode", "genome-denovo-assembly"),
						DBQuery.in("projectCodes", "DKC"),
						DBQuery.in("readSetCodes", codeReadSetsLR))
				,keys).toList();
		//Récuper état assemblage et mettre à done toutes les étapes précédentes
		if(assemblies.size()>0) {
			Set<String> stateAssembly = assemblies.stream().map(a->a.properties.get("state").getValue().toString()).collect(Collectors.toSet());
			if(stateAssembly.size()==1)
				assemblyState=stateAssembly.iterator().next();
			else
				assemblyState="MULTIPLE STATE";
		}else {
			assemblyState=null;
		}
		if(assemblyState!=null) {
			//GET STEP
			String currentStep = assemblyState.split("-")[1];
			String currentStatus = assemblyState.split("-")[0];
			boolean isCurrentStep=false;
			for(String step : stepAssembly) {
				if(currentStep.equals(step)) {
					assemblyStateStep.put(step, codeLabelAssembly.get(currentStatus));
					isCurrentStep=true;
				}else if(isCurrentStep) {
					assemblyStateStep.put(step, null);
				}else if(!isCurrentStep) {
					assemblyStateStep.put(step, "Done");
				}
			}
		}
		return assemblyStateStep;
	}
	private List<ReadSet> getReadSetsValidProdBioinfo(String typeCode, String projectCode, String collabName)
	{
		BasicDBObject keys = new BasicDBObject();
		keys.put("treatments", 0);

		List<ReadSet> readsets = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, 
				DBQuery.and(
						DBQuery.is("typeCode", typeCode),
						DBQuery.is("projectCode", projectCode),
						DBQuery.is("productionValuation.valid", TBoolean.TRUE),
						DBQuery.is("bioinformaticValuation.valid", TBoolean.TRUE),
						DBQuery.is("sampleOnContainer.properties.collabScientificName.value", collabName))
				,keys).toList();
		return readsets;
	}

	private List<ReadSet> getReadSetsValidProd(String typeCode, String projectCode, String collabName)
	{
		BasicDBObject keys = new BasicDBObject();
		keys.put("treatments", 0);

		List<ReadSet> readsets = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, 
				DBQuery.and(
						DBQuery.is("typeCode", typeCode),
						DBQuery.is("projectCode", projectCode),
						DBQuery.is("productionValuation.valid", TBoolean.TRUE),
						DBQuery.is("sampleOnContainer.properties.collabScientificName.value", collabName))
				,keys).toList();
		return readsets;
	}

	private String getStateFromRSValidProdBioinfo(String typeCode, String projectCode, String collabName)
	{
		String state=null;
		List<ReadSet> readsets = getReadSetsValidProdBioinfo(typeCode, projectCode, collabName);
		if(readsets.size()>0) {
			state="Sequenced";
			List<ReadSet> submittedReadsets = readsets.stream().filter(r->r.submissionState.code.equals("SUB-F")).collect(Collectors.toList());
			if(submittedReadsets.size()>0) {
				state="Submitted";
			}
		}
		return state;
	}

	private String getStateFromRS(List<ReadSet> readsets)
	{
		String state=null;
		if(readsets.size()>0) {
			state="Sequenced";
			List<ReadSet> submittedReadsets = readsets.stream().filter(r->r.submissionState.code.equals("SUB-F")).collect(Collectors.toList());
			if(submittedReadsets.size()>0) {
				state="Submitted";
			}
		}
		return state;
	}
	/**
	 * Create excel file in execution directory
	 * File recap : list all objects updated
	 * @param cv
	 */
	private void createExcelFileRecap(ContextValidation cv)	{

		Workbook wb = new HSSFWorkbook();
		CreationHelper createHelper = wb.getCreationHelper();
		for(String key : cv.getContextObjects().keySet()){
			Sheet sheet = wb.createSheet(key);
			List<String> recaps = (List<String>) cv.getObject(key);
			int nbLine=0;
			for(String recap : recaps){
				//	Logger.debug("recap: "+recap);
				Row row = sheet.createRow(nbLine);
				String[] tabRecap = recap.split(",");
				for(int i=0;i<tabRecap.length;i++){
					row.createCell(i).setCellValue(
							createHelper.createRichTextString(tabRecap[i]));
				}
				nbLine++;
			}
		}

		// Write the output to a file
		try (OutputStream fileOut = new FileOutputStream("ExportTrackingATS.xls")) {
			wb.write(fileOut);
		} catch(Exception e) {
			Logger.debug(e.getMessage());
		}
	}
}
