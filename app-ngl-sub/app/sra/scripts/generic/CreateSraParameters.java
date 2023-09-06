package sra.scripts.generic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import models.sra.submit.util.SraParameter;
import models.utils.InstanceConstants;
import models.utils.dao.DAOException;
import ngl.refactoring.state.SRASubmissionStateNames;
//import ngl.refactoring.state.StateCategories;
//import ngl.refactoring.state.StateNames;
//import javax.inject.Inject;

import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.SraParameterAPI;


/*
 * Script a lancer pour vider et recharger la collection ngl-sub.
 * Exemple de lancement :
 * http://localhost:9000/sra/scripts/run/sra.scripts.generic.CreateSraParameters
 * @author sgas
 *
 */


public class CreateSraParameters extends ScriptNoArgs {
private SraParameterAPI sraParameterAPI;

@Inject
public CreateSraParameters(SraParameterAPI      sraParameterAPI) {
    this.sraParameterAPI = sraParameterAPI;
}
	@Override
	public void execute() throws Exception {

		delete_sraParameter_for_type("libProcessTypeCodeValue_orientation");
		genere_sraParameter_libProcessTypeCodeValue_orientation();
		delete_sraParameter_for_type("typeReadset");
		genere_sraParameter_typeReadset();
		delete_sraParameter_for_type("existingStudyType");
		genere_sraParameter_existingStudyType();
		delete_sraParameter_for_type("libraryLayoutOrientation");
		genere_sraParameter_libraryLayoutOrientation();
		delete_sraParameter_for_type("librarySource");
		genere_sraParameter_librarySource();
		delete_sraParameter_for_type("libraryStrategy");
		genere_sraParameter_libraryStrategy();
		delete_sraParameter_for_type("librarySelection");
		genere_sraParameter_librarySelection();
		delete_sraParameter_for_type("typePlatform");
		genere_sraParameter_typePlatform();
		delete_sraParameter_for_type("libraryLayout");
		genere_sraParameter_libraryLayout();
		delete_sraParameter_for_type("instrumentModel");
		genere_sraParameter_instrumentModel();
		delete_sraParameter_for_type("allInstrumentModel");
		genere_sraParameter_allInstrumentModel();
		delete_sraParameter_for_type("centerName");
		genere_sraParameter_centerName();
		delete_sraParameter_for_type("laboratoryName");
		genere_sraParameter_laboratoryName();
		delete_sraParameter_for_type("analysisFileType");
		genere_sraParameter_analysisFileType();
		delete_sraParameter_for_type("simplifiedStates");
		delete_sraParameter_for_type("simplifiedStatesWithNone");
		genere_sraParameter_simplifiedStates_simplifiedStatesWithNone();
		delete_sraParameter_for_type("miniSimplifiedStates");
		delete_sraParameter_for_type("miniSimplifiedStatesWithNone");
		genere_sraParameter_miniSimplifiedStates_miniSimplifiedStatesWithNone();;
		delete_sraParameter_for_type("pseudoStateCodeToStateCodes");
		genere_sraParameter_pseudoStateCodeToStateCodes();
		delete_sraParameter_for_type("submissionProjectType");
		genere_sraParameter_submissionProjectType();
		println("Fin du rechargement de SraParameters");
	}


	public void test_map_libProcessTypeCodeValue_orientation() {
		//SraParameter sraParam = new SraParameter();
		Map<String, String> map = SraParameter.getParameter("libProcessTypeCodeValue_orientation");
		if (map.get("TA")!= null){
			println("ok pour TA voici sa valeur "+ map.get("TA"));
		}

	}

	public void genere_sraParameter_libProcessTypeCodeValue_orientation() throws DAOException, APIException {
		//println("Generation de la map libProcessTypeCodeValue_orientation");
		List <String> libProcessTypeCodeValues_1 = new ArrayList<String>();
		libProcessTypeCodeValues_1.add("A");
		libProcessTypeCodeValues_1.add("C");
		libProcessTypeCodeValues_1.add("N");

		List <String> libProcessTypeCodeValues_2 = new ArrayList<String>();
		libProcessTypeCodeValues_2.add("E");
		libProcessTypeCodeValues_2.add("F");
		libProcessTypeCodeValues_2.add("H");
		libProcessTypeCodeValues_2.add("K");
		libProcessTypeCodeValues_2.add("L");
		libProcessTypeCodeValues_2.add("U");
		libProcessTypeCodeValues_2.add("W");
		libProcessTypeCodeValues_2.add("Z");
		libProcessTypeCodeValues_2.add("DA");
		libProcessTypeCodeValues_2.add("DB");
		libProcessTypeCodeValues_2.add("DC");
		libProcessTypeCodeValues_2.add("DD");
		libProcessTypeCodeValues_2.add("DE");
		libProcessTypeCodeValues_2.add("DF");
		libProcessTypeCodeValues_2.add("MI");
		libProcessTypeCodeValues_2.add("RA");
		libProcessTypeCodeValues_2.add("RB");
		libProcessTypeCodeValues_2.add("RC");
		libProcessTypeCodeValues_2.add("TA");
		libProcessTypeCodeValues_2.add("TB");
		libProcessTypeCodeValues_2.add("TC");


		int cp = 0;
		for (String libProcessTypeCodeValue : libProcessTypeCodeValues_1){
			SraParameter param = new SraParameter();
			param.type = "libProcessTypeCodeValue_orientation";
			param.code=libProcessTypeCodeValue;
			param.value = "reverse-forward";
			sraParameterAPI.save(param);
			//MongoDBDAO.save(InstanceConstants.SRA_PARAMETER_COLL_NAME, param);
			cp++;
		}

		for (String libProcessTypeCodeValue : libProcessTypeCodeValues_2){
			SraParameter param = new SraParameter();
			param.type = "libProcessTypeCodeValue_orientation";
			param.code=libProcessTypeCodeValue;
			param.value = "forward-reverse";
			sraParameterAPI.save(param);
			cp++;
		}	
		println("Insertion de " + cp + " donnees de type 'libProcessTypeCodeValue_orientation' dans SraParameter");
	}


	public void genere_sraParameter_typeReadset() throws DAOException, APIException { 
		//println("Generation des typeReadset");

		List <String> listTypeReadset = new ArrayList<String>();
		listTypeReadset.add("illumina");
		listTypeReadset.add("nanopore");
		listTypeReadset.add("ls454");
		listTypeReadset.add("bionano");
		listTypeReadset.add("mgi");

		int cp = 0;
		for (String typeReadset : listTypeReadset){
			//println("typeReadset = " + typeReadset);
			SraParameter param = new SraParameter();
			param.type = "typeReadset";
			param.code= typeReadset;
			param.value = typeReadset;
			sraParameterAPI.save(param);
			cp++;
		}
		println("Insertion de " + cp + " donnees de type 'typeReadset' dans SraParameter");
	}


	public void genere_sraParameter_existingStudyType() throws DAOException, APIException { 
		//println("Generation des existingStudyType");
		Map<String, String> map = new HashMap<String, String>() {
			{	
				put("whole genome sequencing", "Whole Genome Sequencing"); // value_database, label=value_ebi.
				put("metagenomics", "Metagenomics");
				put("transcriptome analysis", "Transcriptome Analysis");
				put("epigenetics","Epigenetics");
				put("synthetic genomics","Synthetic Genomics");
				put("forensic or paleo-genomics","Forensic or Paleo-genomics");
				put("gene regulation study","Gene Regulation Study");
				put("cancer genomics","Cancer Genomics");
				put("population genomics","Population Genomics");
				put("rnaseq","RNASeq");
				put("exome sequencing","Exome Sequencing");
				put("pooled clone sequencing","Pooled Clone Sequencing");
				put("other", "Other");
				put("resequencing", "Resequencing"); // verifier si toujours la dans nouvelle version ? mais eiste pour ERP000981
			}
		};
		int cp = 0;
		for (Iterator<Entry<String, String>> iterator = map.entrySet().iterator(); iterator.hasNext();) {
			Entry<String, String> entry = iterator.next();
			SraParameter param = new SraParameter();
			param.type = "existingStudyType";
			param.code = entry.getKey();
			param.value = entry.getValue();
			sraParameterAPI.save(param);
			cp++;
		}
		println("Insertion de " + cp + " donnees de type 'existingStudyType' dans SraParameter");
	}


	public void genere_sraParameter_libraryLayoutOrientation() throws DAOException, APIException { 
		//println("Generation des libraryLayoutOrientation");
		Map<String, String> map = new HashMap<String, String>() {
			{
				put("forward", "Forward"); 
				put("forward-reverse", "Forward-Reverse");
				put("reverse-forward", "Reverse-Forward");	
				put("forward-forward", "Forward-Forward");	
			}
		};
		int cp = 0;
		for (Iterator<Entry<String, String>> iterator = map.entrySet().iterator(); iterator.hasNext();) {
			Entry<String, String> entry = iterator.next();
			SraParameter param = new SraParameter();
			param.type = "libraryLayoutOrientation";
			param.code = entry.getKey();
			param.value = entry.getValue();
			sraParameterAPI.save(param);
			cp++;
		}
		println("Insertion de " + cp + " donnees de type 'libraryLayoutOrientation' dans SraParameter");
	}



	public void genere_sraParameter_librarySource() throws DAOException, APIException { 
		//println("Generation des librarySource");		
		Map<String, String> map = new HashMap<String, String>() {
			{
				put("genomic", "GENOMIC"); 
				put("genomic single cell", "GENOMIC SINGLE CELL"); 
				put("transcriptomic", "TRANSCRIPTOMIC");
				put("transcriptomic single cell", "TRANSCRIPTOMIC SINGLE CELL");
				put("metagenomic", "METAGENOMIC"); 
				put("metatranscriptomic", "METATRANSCRIPTOMIC");
				put("synthetic", "SYNTHETIC");
				put("viral rna", "VIRAL RNA");
				put("other", "OTHER");
			}
		};
		int cp = 0;
		for (Iterator<Entry<String, String>> iterator = map.entrySet().iterator(); iterator.hasNext();) {
			Entry<String, String> entry = iterator.next();
			SraParameter param = new SraParameter();
			param.type = "librarySource";
			param.code = entry.getKey();
			param.value = entry.getValue();
			sraParameterAPI.save(param);
			cp++;
		}
		println("Insertion de " + cp + " donnees de type 'librarySource' dans SraParameter");
	}


	public Map<String, String> getCourantMap_libraryStrategy() {
		Map<String, String> map = new HashMap<String, String>() {
			{
				put("wgs", "WGS"); 
				put("wga", "WGA"); 
				put("wxs", "WXS");
				put("rna-seq","RNA-Seq");
				put("ssrna-seq","ssRNA-Seq");
				put("mirna-seq","miRNA-Seq");
				put("ncrna-seq","ncRNA-Seq");
				put("fl-cdna", "FL-cDNA");
				put("est", "EST");
				put("hi-c", "Hi-C");
				put("atac-seq", "ATAC-Seq");
				put("wcs", "WCS"); 
				put("rad-seq", "RAD-Seq");
				put("clone", "CLONE"); 
				put("poolclone", "POOLCLONE"); 
				put("amplicon", "AMPLICON"); 
				put("cloneend", "CLONEEND"); 
				put("finishing", "FINISHING"); 
				put("chip-seq", "ChIP-Seq");
				put("mnase-seq", "MNase-Seq");
				put("dnase-hypersensitivity", "DNase-Hypersensitivity");
				put("bisulfite-seq", "Bisulfite-Seq");
				put("cts", "CTS");
				put("mre-seq", "MRE-Seq");
				put("medip-seq", "MeDIP-Seq");
				put("mbd-seq", "MBD-Seq");
				put("tn-seq", "Tn-Seq");
				put("validation","VALIDATION");
				put("faire-seq","FAIRE-seq");			
				put("selex","SELEX");
				put("rip-seq","RIP-Seq");
				put("chia-pet","ChIA-PET");
				put("synthetic-long-read","Synthetic-Long-Read");
				put("targeted-capture", "Targeted-Capture");
				put("tethered chromatin conformation capture", "Tethered Chromatin Conformation Capture");
				put("other", "OTHER");
			}
		};  
		return map;
	}

	public void genere_sraParameter_libraryStrategy() throws DAOException, APIException { 
		//println("Generation des libraryStrategy");	
		Map<String, String> map = getCourantMap_libraryStrategy();
		int cp = 0;
		for (Iterator<Entry<String, String>> iterator = map.entrySet().iterator(); iterator.hasNext();) {
			Entry<String, String> entry = iterator.next();
			SraParameter param = new SraParameter();
			param.type = "libraryStrategy";
			param.code = entry.getKey();
			param.value = entry.getValue();
			sraParameterAPI.save(param);
			cp++;
		}
		println("Insertion de " + cp + " donnees de type 'libraryStrategy' dans SraParameter");
	}


	public Map<String, String> getCourantMap_librarySelection() {
		Map<String, String> map = new HashMap<String, String>() {
			{
				put("random", "RANDOM"); 
				put("pcr", "PCR");
				put("random pcr", "RANDOM PCR"); 
				put("rt-pcr", "RT-PCR");
				put("hmpr", "HMPR");
				put("mf","MF");
				put("repeat fractionation","repeat fractionation");
				put("size fractionation","size fractionation");
				put("msll","MSLL");
				put("cdna","cDNA");
				put("cdna_randompriming","cDNA_randomPriming");
				put("cdna_oligo_dt", "cDNA_oligo_dT");
				put("polya","PolyA");
				put("oligo-dt","Oligo-dT");
				put("inverse rrna","Inverse rRNA");
				put("inverse rrna selection","Inverse rRNA selection");
				put("chip","ChIP");
				put("chip-seq","ChIP-Seq");
				put("mnase","MNase");
				put("dnase","DNAse");
				put("hybrid selection","Hybrid Selection");
				put("reduced representation","Reduced Representation");
				put("restriction digest","Restriction Digest");
				put("5-methylcytidine antibody","5-methylcytidine antibody");
				put("mbd2 protein methyl-cpg binding domain","MBD2 protein methyl-CpG binding domain");
				put("cage", "CAGE");
				put("race", "RACE");
				put("mda", "MDA");
				put("padlock probes capture method","padlock probes capture method");
				put("other", "other");
				put("unspecified", "unspecified");
			}
		};
		return map;
	}		


	public void genere_sraParameter_librarySelection() throws DAOException, APIException { 
		//println("Generation des librarySelection");	
		Map<String, String> map = getCourantMap_librarySelection();
		int cp = 0;
		for (Iterator<Entry<String, String>> iterator = map.entrySet().iterator(); iterator.hasNext();) {
			cp++;
			Entry<String, String> entry = iterator.next();
			SraParameter param = new SraParameter();
			param.type = "librarySelection";
			param.code = entry.getKey();
			param.value = entry.getValue();
			sraParameterAPI.save(param);
		}
		println("Insertion de " + cp + " donnees de type 'librarySelection' dans SraParameter");
	}


	public void genere_sraParameter_typePlatform() throws DAOException, APIException { 
		//println("Generation des typePlatform");	

		Map<String, String> map = new HashMap<String, String>() {
			{
				put("illumina", "ILLUMINA"); 
				put("ls454","LS454");  // pour les reprises d'historique, il existe des ls454
				put("oxford_nanopore","OXFORD_NANOPORE");
			}
		};  
		int cp = 0;
		for (Iterator<Entry<String, String>> iterator = map.entrySet().iterator(); iterator.hasNext();) {
			cp++;
			Entry<String, String> entry = iterator.next();
			SraParameter param = new SraParameter();
			param.type = "typePlatform";
			param.code = entry.getKey();
			param.value = entry.getValue();
			sraParameterAPI.save(param);
		}
		println("Insertion de " + cp + " donnees de type 'typePlatform' dans SraParameter");
	}


	public void genere_sraParameter_libraryLayout() throws DAOException, APIException { 
		//println("Generation des libraryLayout");	

		Map<String, String> map = new HashMap<String, String>() {
			{
				put("single", "SINGLE"); 
				put("paired", "PAIRED");
			}
		}; 
		int cp = 0;
		for (Iterator<Entry<String, String>> iterator = map.entrySet().iterator(); iterator.hasNext();) {
			cp++;
			Entry<String, String> entry = iterator.next();
			SraParameter param = new SraParameter();
			param.type = "libraryLayout";
			param.code = entry.getKey();
			param.value = entry.getValue();
			sraParameterAPI.save(param);

		}
		println("Insertion de " + cp + " donnees de type 'libraryLayout' dans SraParameter");
	}


	public Map<String, String> getCourantMap_instrumentModel() { 
		Map<String, String> map = new HashMap<String, String>() {
			{	// instrument model pour optical_mapping:
				put("argus",null); // pas prevus de soumettre ces données

				// instrument model pour oxford_nanopore
				put("minion", "MinION");
				put("mk1", "MinION");
				put("mk1b", "MinION");
				//put("mkic", "MinION");
				put("promethion", "PromethION");
				put("gridion", "GridION");

				// instrument model pour L454
				put("454 gs 20", "454 GS 20"); 
				put("454 gs flx", "454 GS FLX");
				put("454 gs flx titanium", "454 GS FLX Titanium");
				put("454 gs flx+", "454 GS FLX+");		

				// instrument model pour MGI
				put("dnbseq g400", "DNBSeq G400");

				// type instrument model pour illumina dans sra version 1.5 : 
				put("illumina genome analyzer","Illumina Genome Analyzer");
				put("illumina ga","Illumina Genome Analyzer");
				put("ga","Illumina Genome Analyzer");
				put("illumina genome analyzer ii","Illumina Genome Analyzer II");
				put("illumina gaii","Illumina Genome Analyzer II");
				put("gaii","Illumina Genome Analyzer II");
				put("illumina genome analyzer iix","Illumina Genome Analyzer IIx");
				put("illumina gaiix","Illumina Genome Analyzer IIx");
				put("gaiix","Illumina Genome Analyzer IIx");
				put("illumina hiseq 2500","Illumina HiSeq 2500");
				put("hiseq2500","Illumina HiSeq 2500");
				put("illumina hiseq 2000","Illumina HiSeq 2000");
				put("hiseq2000","Illumina HiSeq 2000");
				put("illumina hiseq 2000","Illumina HiSeq 2000");
				put("illumina hiseq 1500","Illumina HiSeq 1500");
				put("hiseq1500","Illumina HiSeq 1500");
				put("illumina hiseq 1000","Illumina HiSeq 1000");
				put("hiseq1000","Illumina HiSeq 1000");
				put("illumina miseq","Illumina MiSeq");
				put("miseq","Illumina MiSeq");
				put("illumina hiscansq","Illumina HiScanSQ");
				put("hiscansq","Illumina HiScanSQ");
				put("hiseq x ten","HiSeq X Ten");
				put("nextseq","NextSeq 500");
				put("illumina hiseq 4000","Illumina HiSeq 4000");
				put("illumina novaseq 6000", "Illumina NovaSeq 6000");
				put("novaseq6000", "Illumina NovaSeq 6000");
				// correspondance nomCnsInstrumentModel et instrumentModel :
				put("rgaiix","Illumina Genome Analyzer IIx");
				put("rhs2000","Illumina HiSeq 2000");
				put("rhs2500","Illumina HiSeq 2500");
				put("rhs2500r","Illumina HiSeq 2500");
				put("rmiseq","Illumina MiSeq");
				put("hiseq4000","Illumina HiSeq 4000");
				put("rhs4000","Illumina HiSeq 4000");
			}
		};
		return map;
	}

	public void genere_sraParameter_instrumentModel() throws DAOException, APIException { 
		//println("Generation des instrumentModel");	
		Map<String, String> map = getCourantMap_instrumentModel(); 
		int cp = 0;
		for (Iterator<Entry<String, String>> iterator = map.entrySet().iterator(); iterator.hasNext();) {
			cp++;
			Entry<String, String> entry = iterator.next();
			SraParameter param = new SraParameter();
			param.type = "instrumentModel";
			param.code = entry.getKey();
			param.value = entry.getValue();
			sraParameterAPI.save(param);
		}
		println("Insertion de " + cp + " donnees de type 'instrumentModel' dans SraParameter");
	}

	public void genere_sraParameter_allInstrumentModel() throws DAOException, APIException { 
		//println("Generation des allInstrumentModel");	
		Map<String, String> map = getCourantMap_instrumentModel(); 
		map.put("unspecified", "unspecified"); // pour premiers experiments
		int cp = 0;
		for (Iterator<Entry<String, String>> iterator = map.entrySet().iterator(); iterator.hasNext();) {
			cp++;
			Entry<String, String> entry = iterator.next();
			SraParameter param = new SraParameter();
			param.type = "allInstrumentModel";
			param.code = entry.getKey();
			param.value = entry.getValue();
			sraParameterAPI.save(param);

		}
		println("Insertion de " + cp + " donnees de type 'allInstrumentModel' dans SraParameter");
	}

	public void genere_sraParameter_centerName() throws DAOException, APIException { 
		//println("Generation du centerName");	
		Map<String, String> map=  new HashMap<String, String>() {
			{
				put("gsc", "GSC"); 
			}
		};
		int cp = 0;
		for (Iterator<Entry<String, String>> iterator = map.entrySet().iterator(); iterator.hasNext();) {
			cp++;
			Entry<String, String> entry = iterator.next();
			SraParameter param = new SraParameter();
			param.type = "centerName";
			param.code = entry.getKey();
			param.value = entry.getValue();
			sraParameterAPI.save(param);
		}
		println("Insertion de " + cp + " donnees de type 'centerName' dans SraParameter");
	}


	public void genere_sraParameter_laboratoryName() throws DAOException, APIException { 
		//println("Generation du laboratoryName");	
		Map<String, String> map =  new HashMap<String, String>() {
			{
				put("genoscope - cea", "Genoscope - CEA"); 
			}
		};
		int cp = 0;
		for (Iterator<Entry<String, String>> iterator = map.entrySet().iterator(); iterator.hasNext();) {
			cp++;
			Entry<String, String> entry = iterator.next();
			SraParameter param = new SraParameter();
			param.type = "laboratoryName";
			param.code = entry.getKey();
			param.value = entry.getValue();
			sraParameterAPI.save(param);

		}
		println("Insertion de " + cp + " donnees de type 'laboratoryName' dans SraParameter");
	}



	public void genere_sraParameter_analysisFileType() throws DAOException, APIException {
		//println("Generation des analysisFileType");	

		Map<String, String> map =  new HashMap<String, String>() {
			{
				put("fasta", "fasta"); 
				put("contig_fasta", "contig_fasta"); 
				put("contig_flatfile", "contig_flatfile"); 
				put("scaffold_fasta", "scaffold_fasta"); 
				put("scaffold_flatfile", "scaffold_flatfile"); 
				put("scaffold_agp", "scaffold_agp"); 
				put("chromosome_fasta", "chromosome_fasta"); 
				put("chromosome_flatfile", "chromosome_flatfile"); 
				put("chromosome_agp", "chromosome_agp"); 
				put("chromosome_list", "chromosome_list"); 
				put("unlocalised_contig_list", "unlocalised_contig_list"); 
				put("unlocalised_scaffold_list", "unlocalised_scaffold_list"); 
				put("other", "other"); 			
			}
		};
		int cp = 0;
		for (Iterator<Entry<String, String>> iterator = map.entrySet().iterator(); iterator.hasNext();) {
			cp++;
			Entry<String, String> entry = iterator.next();
			SraParameter param = new SraParameter();
			param.type = "analysisFileType";
			param.code = entry.getKey();
			param.value = entry.getValue();
			sraParameterAPI.save(param);

		}
		println("Insertion de " + cp + " donnees de type 'analysisFileType' dans SraParameter");
	}


	// liste des pseudo_states simplifies (non presents dans la description) à afficher 
	// dans les menus deroulants de study et submission
	public void genere_sraParameter_simplifiedStates_simplifiedStatesWithNone() throws DAOException, APIException { 
		//println("Generation de simplifiedStates");	
		Map<String, String> map =  new HashMap<String, String>() {
			{	
				put("pseudo-02-F"               , "Terminé"); //pour create, release ou update

				put("pseudo-03-create-N"        , "create - New");
				put("pseudo-04-create-V"        , "create - Validé utilisateur");
				put("pseudo-05-create-IW"       , "create - En cours");
				put("pseudo-06-create-FE"       , "create - En echec");

				put("pseudo-07-release-N"       , "release - New");
				put("pseudo-08-release-IW"      , "release - En cours");
				put("pseudo-09-releaseFE"       , "release - En echec");

				put("pseudo-10-update-N"        , "update - New");
				put("pseudo-11-update-V"        , "update - Validé utilisateur");
				put("pseudo-12-update-IW"       , "update - En cours");
				put("pseudo-13-update-FE"       , "update - En echec");
			}
		};
		int cp = 0;
		for (Iterator<Entry<String, String>> iterator = map.entrySet().iterator(); iterator.hasNext();) {
			cp++;
			Entry<String, String> entry = iterator.next();
			SraParameter param = new SraParameter();
			param.type = "simplifiedStates";
			param.code = entry.getKey();
			param.value = entry.getValue();
			sraParameterAPI.save(param);
		}
		println("Insertion de " + cp + " donnees de type 'simplifiedStates' dans SraParameter");
		map.put("pseudo-01-NONE"            , "Non associé à une soumission");
		cp = 0;
		for (Iterator<Entry<String, String>> iterator = map.entrySet().iterator(); iterator.hasNext();) {
			cp++;
			Entry<String, String> entry = iterator.next();
			SraParameter param = new SraParameter();
			param.type = "simplifiedStatesWithNone";
			param.code = entry.getKey();
			param.value = entry.getValue();
			sraParameterAPI.save(param);
		}
		println("Insertion de " + cp + " donnees de type 'simplifiedStatesWithNone' dans SraParameter");
	}


	// liste des pseudo_states simplifies (non presents dans la description) à afficher 
	// dans les menus deroulants de experiments et sample (=> pas de notion de release, ou de NONE)
	public void genere_sraParameter_miniSimplifiedStates_miniSimplifiedStatesWithNone() throws DAOException, APIException { 
		//println("Generation de miniSimplifiedStates");	
		Map<String, String> map =  new HashMap<String, String>() {
			{	
				put("pseudo-02-F"               , "Terminé"); //pour create, release ou update

				put("pseudo-03-create-N"        , "create - New");
				put("pseudo-04-create-V"        , "create - Validé utilisateur");
				put("pseudo-05-create-IW"       , "create - En cours");
				put("pseudo-06-create-FE"       , "create - En echec");

				put("pseudo-10-update-N"        , "update - New");
				put("pseudo-11-update-V"        , "update - Validé utilisateur");
				put("pseudo-12-update-IW"       , "update - En cours");
				put("pseudo-13-update-FE"       , "update - En echec");
			}
		};
		int cp = 0;
		for (Iterator<Entry<String, String>> iterator = map.entrySet().iterator(); iterator.hasNext();) {
			cp++;
			Entry<String, String> entry = iterator.next();
			SraParameter param = new SraParameter();
			param.type = "miniSimplifiedStates";
			param.code = entry.getKey();
			param.value = entry.getValue();
			sraParameterAPI.save(param);
		}
		println("Insertion de " + cp + " donnees de type 'miniSimplifiedStates' dans SraParameter");
		map.put("pseudo-01-NONE"            , "Non associé à une soumission");
		cp = 0;
		for (Iterator<Entry<String, String>> iterator = map.entrySet().iterator(); iterator.hasNext();) {
			cp++;
			Entry<String, String> entry = iterator.next();
			SraParameter param = new SraParameter();
			param.type = "miniSimplifiedStatesWithNone";
			param.code = entry.getKey();
			param.value = entry.getValue();
			sraParameterAPI.save(param);
		}
		println("Insertion de " + cp + " donnees de type 'miniSimplifiedStatesWithNone' dans SraParameter");
	}	

	// table de correspondance entre un pseudo_state.code (non definit dans la description) 
	// et les states (presents dans la description)
	public void genere_sraParameter_pseudoStateCodeToStateCodes() throws DAOException, APIException { 
		//println("Generation de pseudoStateCodeToStateCodes");
		final ArrayList<String> statesForPseudo_01_NONE = new ArrayList<String>();
		statesForPseudo_01_NONE.add(SRASubmissionStateNames.NONE);

		final ArrayList<String> statesForPseudo_02_F = new ArrayList<String>();
		statesForPseudo_02_F.add(SRASubmissionStateNames.SUB_F);

		final ArrayList<String> statesForPseudo_03_create_N = new ArrayList<String>();
		statesForPseudo_03_create_N.add(SRASubmissionStateNames.SUB_N);

		final ArrayList<String> statesForPseudo_04_create_V = new ArrayList<String>();
		statesForPseudo_04_create_V.add(SRASubmissionStateNames.SUB_V);

		final ArrayList<String> statesForPseudo_05_create_IW = new ArrayList<String>();
		statesForPseudo_05_create_IW.add(SRASubmissionStateNames.SUB_SMD_IW);
		statesForPseudo_05_create_IW.add(SRASubmissionStateNames.SUB_SMD_IP);
		statesForPseudo_05_create_IW.add(SRASubmissionStateNames.SUB_SMD_F);
		statesForPseudo_05_create_IW.add(SRASubmissionStateNames.SUB_SRD_IW);
		statesForPseudo_05_create_IW.add(SRASubmissionStateNames.SUB_SRD_IP);
		statesForPseudo_05_create_IW.add(SRASubmissionStateNames.SUB_SRD_F);

		final ArrayList<String> statesForPseudo_06_create_FE = new ArrayList<String>();
		statesForPseudo_06_create_FE.add(SRASubmissionStateNames.SUB_SMD_FE);
		statesForPseudo_06_create_FE.add(SRASubmissionStateNames.SUB_SRD_FE);
		statesForPseudo_06_create_FE.add(SRASubmissionStateNames.SUB_FE);

		final ArrayList<String> statesForPseudo_07_release_N = new ArrayList<String>();
		statesForPseudo_07_release_N.add(SRASubmissionStateNames.SUBR_N);

		final ArrayList<String> statesForPseudo_08_release_IW = new ArrayList<String>();
		statesForPseudo_08_release_IW.add(SRASubmissionStateNames.SUBR_SMD_IW);
		statesForPseudo_08_release_IW.add(SRASubmissionStateNames.SUBR_SMD_IP);
		statesForPseudo_08_release_IW.add(SRASubmissionStateNames.SUBR_SMD_F);

		final ArrayList<String> statesForPseudo_09_release_FE = new ArrayList<String>();
		statesForPseudo_09_release_FE.add(SRASubmissionStateNames.SUBR_SMD_FE);
		statesForPseudo_09_release_FE.add(SRASubmissionStateNames.SUBR_FE);

		final ArrayList<String> statesForPseudo_10_update_N = new ArrayList<String>();
		statesForPseudo_10_update_N.add(SRASubmissionStateNames.SUBU_N);

		final ArrayList<String> statesForPseudo_11_update_V = new ArrayList<String>();
		statesForPseudo_11_update_V.add(SRASubmissionStateNames.SUBU_V);

		final ArrayList<String> statesForPseudo_12_update_IW = new ArrayList<String>();
		statesForPseudo_12_update_IW.add(SRASubmissionStateNames.SUBU_SMD_IW);
		statesForPseudo_12_update_IW.add(SRASubmissionStateNames.SUBU_SMD_IP);
		statesForPseudo_12_update_IW.add(SRASubmissionStateNames.SUBU_SMD_F);

		final ArrayList<String> statesForPseudo_13_update_FE = new ArrayList<String>();
		statesForPseudo_13_update_FE.add(SRASubmissionStateNames.SUBU_SMD_FE);
		statesForPseudo_13_update_FE.add(SRASubmissionStateNames.SUBU_FE);



		// correspondance pour pseudoState ebiKnown mais pas affiché. Utilisé dans bilans
		final ArrayList<String> statesForPseudo_ebiKnown = new ArrayList<String>();
		statesForPseudo_ebiKnown.add(SRASubmissionStateNames.SUB_F);
		statesForPseudo_ebiKnown.add(SRASubmissionStateNames.SUBR_N);
		statesForPseudo_ebiKnown.add(SRASubmissionStateNames.SUBR_SMD_F);
		statesForPseudo_ebiKnown.add(SRASubmissionStateNames.SUBR_SMD_IW);
		statesForPseudo_ebiKnown.add(SRASubmissionStateNames.SUBR_SMD_IP);
		statesForPseudo_ebiKnown.add(SRASubmissionStateNames.SUBR_SMD_F);
		statesForPseudo_ebiKnown.add(SRASubmissionStateNames.SUBR_SMD_FE);
		statesForPseudo_ebiKnown.add(SRASubmissionStateNames.SUBR_FE); // peut etre pas utile car concerne uniquement study
		statesForPseudo_ebiKnown.add(SRASubmissionStateNames.SUBU_N);
		statesForPseudo_ebiKnown.add(SRASubmissionStateNames.SUBU_SMD_IW);
		statesForPseudo_ebiKnown.add(SRASubmissionStateNames.SUBU_SMD_IP);
		statesForPseudo_ebiKnown.add(SRASubmissionStateNames.SUBU_SMD_F);
		statesForPseudo_ebiKnown.add(SRASubmissionStateNames.SUBU_SMD_FE);
		statesForPseudo_ebiKnown.add(SRASubmissionStateNames.SUBU_FE);

		Map<String, ArrayList<String>> map =  new HashMap<String, ArrayList<String>>() {{	
			put("pseudo-01-NONE"             , statesForPseudo_01_NONE);
			put("pseudo-02-F"                , statesForPseudo_02_F);

			put("pseudo-03-create-N"         , statesForPseudo_03_create_N);
			put("pseudo-04-create-V"         , statesForPseudo_04_create_V);
			put("pseudo-05-create-IW"        , statesForPseudo_05_create_IW);
			put("pseudo-06-create-FE"        , statesForPseudo_06_create_FE);

			put("pseudo-07-release-N"         , statesForPseudo_07_release_N);
			put("pseudo-08-release-IW"        , statesForPseudo_08_release_IW);
			put("pseudo-09-release-FE"        , statesForPseudo_09_release_FE);

			put("pseudo-10-update-N"         , statesForPseudo_10_update_N);
			put("pseudo-11-update-V"         , statesForPseudo_11_update_V);
			put("pseudo-12-update-IW"        , statesForPseudo_12_update_IW);
			put("pseudo-13-update-FE"        , statesForPseudo_13_update_FE);

			put("pseudo_ebiKnown"            , statesForPseudo_ebiKnown);

		}};

		int cp = 0;
		for (Iterator<Entry<String, ArrayList<String>>> iterator = map.entrySet().iterator(); iterator.hasNext();) {
			Entry<String, ArrayList<String>> entry = iterator.next();
			for(String value : entry.getValue()) {
				cp++;
				SraParameter param = new SraParameter();
				param.type = "pseudoStateCodeToStateCodes";
				param.code = entry.getKey();
				param.value = value;
				sraParameterAPI.save(param);
			}
		}
		println("Insertion de " + cp + " donnees de type 'pseudoStateCodeToStateCodes' dans SraParameter");
	}

	// Ajout pour controle object project (hors SRA mais dans modele ENA pour locus_tag)
	public void genere_sraParameter_submissionProjectType() throws DAOException, APIException { 
		//println("Generation du submissionProjectType");	
		Map<String, String> map=  new HashMap<String, String>() {
			{
				//put("sequencing_project", "SEQUENCING_PROJECT"); // project de type SEQUENCING_PROJECT stoque dans collection Study et non plus dans collection Project
				put("umbrella_project", "UMBRELLA_PROJECT");
			}
		};
		int cp = 0;
		for (Iterator<Entry<String, String>> iterator = map.entrySet().iterator(); iterator.hasNext();) {
			cp++;
			Entry<String, String> entry = iterator.next();
			SraParameter param = new SraParameter();
			param.type = "submissionProjectType";
			param.code = entry.getKey();
			param.value = entry.getValue();
			sraParameterAPI.save(param);
		}
		println("Insertion de " + cp + " donnees de type 'submissionProjectType' dans SraParameter");
	}

	public void delete_sraParameter_for_type(String type) throws DAOException, APIException {
		List<SraParameter> sraParams = MongoDBDAO.find(InstanceConstants.SRA_PARAMETER_COLL_NAME, SraParameter.class, DBQuery.in("type", type)).toList();
		//println("nbre de parametres avec type " + type + " = " + sraParams.size());
		int cp = 0;
		for (SraParameter sraParameter : sraParams) {
			cp++;
			//println("        deletion de " + sraParameter.code);
			//MongoDBDAO.delete(InstanceConstants.SRA_PARAMETER_COLL_NAME, SraParameter.class, DBQuery.is("code", sraParameter.code).and(DBQuery.is("type", type)));		
			sraParameterAPI.deleteByCodeAndType(sraParameter.code, type);
		}
		println("\nDeletion dans table SraParameter de " + cp + " donnees de type " + type);
	}

}
