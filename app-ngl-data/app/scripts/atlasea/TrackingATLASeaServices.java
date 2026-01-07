package scripts.atlasea;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.mongojack.DBQuery;

import com.mongodb.BasicDBObject;

import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.instance.TBoolean;
import models.laboratory.run.instance.Analysis;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.sample.instance.reporting.SampleProcess;
import models.utils.InstanceConstants;
import play.Logger;

public class TrackingATLASeaServices {

	Predicate<SampleProcess> checkSample = p->p.sampleOnInputContainer!=null;
	Predicate<SampleProcess> checkSampleProperties = p->p.sampleOnInputContainer!=null && p.sampleOnInputContainer.properties!=null;
	Predicate<SampleProcess> sampleTypeCodeDNA = checkSample.and(p->p.sampleOnInputContainer.sampleTypeCode.equals("DNA"));
	Predicate<SampleProcess> notHic = checkSampleProperties.and(p->!p.sampleOnInputContainer.properties.containsKey("samplePrepMethod") || (p.sampleOnInputContainer.properties.containsKey("samplePrepMethod") && !p.sampleOnInputContainer.properties.get("samplePrepMethod").getValue().toString().equals("Hi-C")));
	Predicate<SampleProcess> notTagPCR =  p->p.experiments!=null && p.experiments.stream().noneMatch(e->e!=null && e.typeCode.equals("tag-pcr"));
	Predicate<SampleProcess> sampleDNA = sampleTypeCodeDNA.and(notHic).and(notTagPCR);
	Predicate<SampleProcess> notCDNASynthesis = p->p.experiments!=null && p.experiments.stream().noneMatch(e-> e!=null && e.typeCode.equals("cdna-synthesis"));
	Predicate<SampleProcess> hic = checkSampleProperties.and(p->p.sampleOnInputContainer.properties.containsKey("samplePrepMethod") && p.sampleOnInputContainer.properties.get("samplePrepMethod").getValue().toString().equals("Hi-C"));
	Predicate<SampleProcess> dnaIlluminaIndexedLibrary = p->p.experiments!=null && p.experiments.stream().anyMatch(e->e!=null && e.typeCode.equals("dna-illumina-indexed-library"));
	Predicate<SampleProcess> dnaPacbioLibrary = p->p.experiments!=null && p.experiments.stream().anyMatch(e->e!=null && e.typeCode.equals("dna-pacbio-library"));
	Predicate<SampleProcess> nanoporeFinalLigation = p->p.experiments!=null && p.experiments.stream().anyMatch(e->e!=null && e.typeCode.equals("nanopore-final-ligation"));
	Predicate<SampleProcess> nanoporeLibrary = p->p.experiments!=null && p.experiments.stream().anyMatch(e->e!=null && e.typeCode.equals("nanopore-library"));
	Predicate<SampleProcess> rnaIlluminaIndexedLibrary = p->p.experiments!=null && p.experiments.stream().anyMatch(e->e!=null && e.typeCode!=null && (e.typeCode.equals("rna-illumina-indexed-library") || e.typeCode.equals("rna-illumina-library")));



	public TrackingATLASeaServices() {

	}

	public String getShortReadState(String codeProperty, String value, List<SampleProcess> processes)
	{
		String shortReadState = null;
		shortReadState=getStateFromRSValidProdBioinfo("rsillumina", "DKC", codeProperty, value);
		if(shortReadState==null) {
			shortReadState=getShortReadStateLibrary(processes);
		}
		return shortReadState;
	}



	private String getShortReadStateLibrary(List<SampleProcess> processes)
	{
		String shortReadState = null;
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
		return shortReadState;
	}
	public List<ReadSet> getReadsetsLR(String codeProperty, String value)
	{
		//Nouvelle regle de recherche les rs oui oui OU oui -- pour les deux technos
		List<ReadSet> readsetsLRPacBio = getReadSetsLRValid("rspacbio", "DKC", codeProperty, value);
		List<ReadSet> readsetsLR = getReadSetsLRValid("rsnanopore", "DKC", codeProperty, value);
		readsetsLR.addAll(readsetsLRPacBio);
		return readsetsLR;
	}
	public String getLongReadState(List<SampleProcess> processes, List<ReadSet> readsetsLR)
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

	public String getHicState(String codeProperty, String value, List<SampleProcess> processes)
	{
		String hicState=null;
		hicState=getStateFromRSValidProdBioinfo("rsillumina", "DKF", codeProperty, value);
		if(hicState==null) {
			//Prepared  a reverifier
			//"isConditions:[{criteria:{sampleOnInputContainer:{properties:{samplePrepMethod:{value:'Hi-C'}}}},experiments:{typeCode:'dna-illumina-indexed-library'},expected:true}]",
			List<SampleProcess> processesFilteredPreparedHiC = processes.stream()
					.filter(dnaIlluminaIndexedLibrary)
					.filter(hic)
					.filter(Objects::nonNull)
					.collect(Collectors.toList());
			if(processesFilteredPreparedHiC.size()>0) {
				hicState="Prepared";
			}
		}
		return hicState;
	}

	public String getRnaState(String codeProperty, String value, List<SampleProcess> processes)
	{
		String rnaState=null;
		rnaState=getStateFromRSValidProdBioinfo("rsillumina", "DKE", codeProperty, value);
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

	public Map<String,String> getAssemblyState(List<ReadSet> readsetsLR)
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
			Set<String> stateAssembly = assemblies.stream().filter(a->a.properties.containsKey("state")).map(a->a.properties.get("state").getValue().toString()).collect(Collectors.toSet());
			if(stateAssembly.size()==1)
				assemblyState=stateAssembly.iterator().next();
			else
				assemblyState="MULTIPLE STATE";
		}else {
			assemblyState=null;
		}
		if(assemblyState!=null && !assemblyState.equals("MULTIPLE STATE")) {

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
		}else {
			Logger.error("MULTIPLE STATE ASSEMBLY");
		}
		return assemblyStateStep;
	}
	private List<ReadSet> getReadSetsValidProdBioinfo(String typeCode, String projectCode, String codeProperty, String value)
	{
		BasicDBObject keys = new BasicDBObject();
		keys.put("treatments", 0);

		List<ReadSet> readsets = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, 
				DBQuery.and(
						DBQuery.is("typeCode", typeCode),
						DBQuery.is("projectCode", projectCode),
						DBQuery.is("productionValuation.valid", TBoolean.TRUE),
						//DBQuery.is("bioinformaticValuation.valid", TBoolean.TRUE),
						DBQuery.is(codeProperty, value))
				,keys).toList();
		return readsets;
	}

	public List<ReadSet> getReadSetsLRValid(String typeCode, String projectCode, String codeProperty, String value)
	{
		BasicDBObject keys = new BasicDBObject();
		keys.put("treatments", 0);
		List<ReadSet> readsets = new ArrayList<ReadSet>();

		List<ReadSet> readsetsValidProdBionfo = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, 
				DBQuery.and(
						DBQuery.is("typeCode", typeCode),
						DBQuery.is("projectCode", projectCode),
						DBQuery.is("productionValuation.valid", TBoolean.TRUE),
						DBQuery.is("bioinformaticValuation.valid", TBoolean.TRUE),
						DBQuery.is(codeProperty, value))
				,keys).toList();
		readsets.addAll(readsetsValidProdBionfo);
		
		List<ReadSet> readsetsValidProdUnsetBionfo = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, 
				DBQuery.and(
						DBQuery.is("typeCode", typeCode),
						DBQuery.is("projectCode", projectCode),
						DBQuery.is("productionValuation.valid", TBoolean.TRUE),
						DBQuery.is("bioinformaticValuation.valid", TBoolean.UNSET),
						DBQuery.is(codeProperty, value))
				,keys).toList();
		readsets.addAll(readsetsValidProdUnsetBionfo);
		return readsets;
	}
	
	public List<ReadSet> getReadSetsValidProd(String typeCode, String projectCode, String codeProperty, String value)
	{
		BasicDBObject keys = new BasicDBObject();
		keys.put("treatments", 0);

		List<ReadSet> readsets = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, 
				DBQuery.and(
						DBQuery.is("typeCode", typeCode),
						DBQuery.is("projectCode", projectCode),
						DBQuery.is("productionValuation.valid", TBoolean.TRUE),
						DBQuery.is(codeProperty, value))
				,keys).toList();
		return readsets;
	}

	private String getStateFromRSValidProdBioinfo(String typeCode, String projectCode, String codeProperty, String value)
	{
		String state=null;
		List<ReadSet> readsets = getReadSetsValidProdBioinfo(typeCode, projectCode, codeProperty, value);
		if(readsets.size()>0) {
			state="Sequenced";
			List<ReadSet> submittedReadsets = readsets.stream().filter(r->r.submissionState.code.equals("SUB-F")).collect(Collectors.toList());
			if(submittedReadsets.size()>0) {
				state="Submitted";
			}
		}
		return state;
	}



	public String getStateFromRS(List<ReadSet> readsets)
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
}
