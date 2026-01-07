package sra.scripts.generic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import models.utils.InstanceConstants;
import models.utils.dao.DAOException;
import ngl.refactoring.state.SRASubmissionStateNames;
import services.sra.SraParameter;

import org.mongojack.DBQuery;

import controllers.sra.api.SraParameterAPI;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import fr.cea.ig.ngl.dao.api.APIException;


/*
 * Script a lancer pour vider et recharger la collection ngl-sub.
 * Exemple de lancement :
 * http://localhost:9000/sra/scripts/run/sra.scripts.generic.CreateSraParameters
 * @author sgas
 *
 */


public class CreateSraParameters extends ScriptNoArgs {
private SraParameterAPI sraParameterAPI;
// pour la correspondance entre type d'instrument au CNS et à l'EBI 
private String paramTypeOxfordNanoporeInstrumentModel = "oxfordNanoporeInstrumentModel";
private String paramTypeL454InstrumentModel           = "l454InstrumentModel";
private String paramTypeIlluminaInstrumentModel       = "illuminaInstrumentModel";
private String paramTypePacbioInstrumentModel         = "pacbioInstrumentModel";
private String paramTypeMgiInstrumentModel            = "mgiInstrumentModel";
private String paramTypeInstrumentModel               = "instrumentModel";
private String paramTypeAllInstrumentModel            = "allInstrumentModel";
// pour les titres des experiments : 
private String paramTypeCnsNameInstrumentModel        = "cnsNameInstrumentModel"; // utilisé pour titre des experiments

@Inject
public CreateSraParameters(SraParameterAPI      sraParameterAPI) {
    this.sraParameterAPI = sraParameterAPI;
}
	@Override
	public void execute() throws Exception {
		delete_sraParameter_for_type("static");
		genere_sraParameter_static();
		delete_sraParameter_for_type("dropBox");
		genere_sraParameter_dropBox();
		delete_sraParameter_for_type("legendeDropBox");
		genere_sraParameter_legendeDropBox();
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
		
		delete_sraParameter_allInstrumentModel();		
		genere_sraParameter_allInstrumentModel();
		
		delete_sraParameter_for_type(paramTypeCnsNameInstrumentModel);
		genere_sraParameter_cnsNameInstrumentModel(paramTypeCnsNameInstrumentModel);
		delete_sraParameter_for_type("experimentTitle");
		genere_sraParameter_experimentTitle();
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
		libProcessTypeCodeValues_2.add("DK"); // specifique MGI
		libProcessTypeCodeValues_2.add("DL"); // specifique MGI
		libProcessTypeCodeValues_2.add("DG"); // specifique pacbio
		libProcessTypeCodeValues_2.add("DLS");  // spécifique techno BIONANO (optical mapping)
		libProcessTypeCodeValues_2.add("NLRS"); // spécifique techno BIONANO (optical mapping)
		
		// Pas besoin de renseigner orientation pour oxfordNanopore (ONT et RD)


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

	public void genere_sraParameter_experimentTitle() throws DAOException, APIException { 
		genere_sraParameter_illuminaExperimentTitle();
		genere_sraParameter_pacbioExperimentTitle();
		genere_sraParameter_oxfordNanoporeExperimentTitle();
		genere_sraParameter_mgiExperimentTitle();
		//genere_sraParameter_bionanoExperimentTitle();
	}

	// Donne le titre de l'experiment construit en fonction du type de readset (Illumina, pacbio, nanopore ou mgi) et du libProcessTypeCode
	public void genere_sraParameter_illuminaExperimentTitle() throws DAOException, APIException { 
		int cp = 0;
		String paramType = "experimentTitle";
		
		// definition des titres pour Illumina et "A", "C", "K", "N" :
		List<String> libProcessTypeCodes = new ArrayList<String>(Arrays.asList("A", "C", "K", "N"));
		for (String libProcessTypeCode : libProcessTypeCodes) {
			SraParameter param = new SraParameter();
			param.type = paramType;
			param.code = "Illumina_" + libProcessTypeCode;
			param.code = param.code.toLowerCase();
			param.value = "WGS data, Illumina INSTRUMENT mate pairs sequencing";
			sraParameterAPI.save(param);
			cp++;
		}
		// definition des titres pour Illumina et "E", "F", "P", "DA", "DB", "DC","DE" : ok
		libProcessTypeCodes = new ArrayList<String>(Arrays.asList("E", "F", "P", "DA", "DB", "DC","DE"));
		for (String libProcessTypeCode : libProcessTypeCodes) {
			SraParameter param = new SraParameter();
			param.type = paramType;
			param.code = "Illumina_" + libProcessTypeCode;
			param.code = param.code.toLowerCase();
			param.value = "WGS data, Illumina INSTRUMENT paired end sequencing";
			sraParameterAPI.save(param);
			cp++;
		}
		// definition des titres pour Illumina et "U", "W"
		libProcessTypeCodes = new ArrayList<String>(Arrays.asList("U", "W"));
		for (String libProcessTypeCode : libProcessTypeCodes) {
			SraParameter param = new SraParameter();
			param.type = paramType;
			param.code = "Illumina_" + libProcessTypeCode;
			param.code = param.code.toLowerCase();
			param.value = "WGS data, Illumina INSTRUMENT sequencing";
			sraParameterAPI.save(param);
			cp++;
		}
		
		// definition des titres pour Illumina et "S" : ok
		SraParameter param_S = new SraParameter();
		param_S.type = paramType;
		param_S.code = "Illumina_" + "DD";
		param_S.code = param_S.code.toLowerCase();
		param_S.value = "WGS data, Illumina INSTRUMENT single end sequencing";
		sraParameterAPI.save(param_S);
		cp++;
		
		SraParameter param_DD = new SraParameter();
		param_DD.type = paramType;
		param_DD.code = "Illumina_" + "DD";
		param_DD.code = param_DD.code.toLowerCase();
		param_DD.value = "10X Genomics WGS data, Illumina INSTRUMENT paired end sequencing";
		sraParameterAPI.save(param_DD);
		cp++;
		
		// definition des titres pour Illumina et "DF" : ok
		SraParameter param_DF = new SraParameter();
		param_DF.type = paramType;
		param_DF.code = "Illumina_" + "DF";
		param_DF.code = param_DF.code.toLowerCase();
		param_DF.value = "Hi-C data, Illumina INSTRUMENT paired end sequencing";
		sraParameterAPI.save(param_DF);
		cp++;
		
		// definition des titres pour Illumina et "H", "G", "DI", "DJ", "TA", "TB", "TC" : ok
		libProcessTypeCodes = new ArrayList<String>(Arrays.asList("H", "G", "DI", "DJ", "TA", "TB", "TC"));
		for (String libProcessTypeCode : libProcessTypeCodes) {
			SraParameter param = new SraParameter();
			param.type = paramType;
			param.code = "Illumina_" + libProcessTypeCode;
			param.code = param.code.toLowerCase();
			param.value = "Targeted-Capture data, Illumina INSTRUMENT paired end sequencing";
			sraParameterAPI.save(param);
			cp++;
		}
		
		// definition des titres pour Illumina et "L", "MI" :  ok
		libProcessTypeCodes = new ArrayList<String>(Arrays.asList("L", "MI"));
		for (String libProcessTypeCode : libProcessTypeCodes) {
			SraParameter param = new SraParameter();
			param.type = paramType;
			param.code = "Illumina_" + libProcessTypeCode;
			param.code = param.code.toLowerCase();
			param.value = "Synthetic long-reads, Illumina INSTRUMENT sequencing";
			sraParameterAPI.save(param);
			cp++;
		}
		// definition des titres pour Illumina et "Z" : ok
		SraParameter param_Z = new SraParameter();
		param_Z.type = paramType;
		param_Z.code = "Illumina_" + "Z";
		param_Z.code = param_Z.code.toLowerCase();
		param_Z.value = "Bac-end data, Illumina INSTRUMENT sequencing";
		sraParameterAPI.save(param_Z);
		cp++;
		
		// definition des titres pour Illumina et "RA", "RB" : ok
		libProcessTypeCodes = new ArrayList<String>(Arrays.asList("RA", "RB"));
		for (String libProcessTypeCode : libProcessTypeCodes) {
			SraParameter param = new SraParameter();
			param.type = paramType;
			param.code = "Illumina_" + libProcessTypeCode;
			param.code = param.code.toLowerCase();
			param.value = "RNA-Seq data, Illumina INSTRUMENT paired end sequencing";
			sraParameterAPI.save(param);
			cp++;
		}
		// definition des titres pour Illumina et "RC" : ok
		SraParameter param_RC = new SraParameter();
		param_RC.type = paramType;
		param_RC.code = "Illumina_" + "RC";
		param_RC.code = param_RC.code.toLowerCase();
		param_RC.value = "Small RNA-Seq data, Illumina INSTRUMENT paired end sequencing";
		sraParameterAPI.save(param_RC);
		cp++;
		
		// definition des titres pour Illumina et "RI" : ok
		SraParameter param_RI = new SraParameter();
		param_RI.type = paramType;
		param_RI.code = "Illumina_" + "RI";
		param_RI.code = param_RI.code.toLowerCase();
		param_RI.value = "10X Genomics RNA-Seq data, Illumina INSTRUMENT paired end sequencing";
		sraParameterAPI.save(param_RI);
		cp++;
		println("Pour illuminaExperimentTitle, insertion de " + cp + " donnees de type '" + paramType + "' dans SraParameter");
	}
	
	public void genere_sraParameter_pacbioExperimentTitle() throws DAOException, APIException { 
		int cp = 0;
		String paramType = "experimentTitle";
		// definition des titres pour PacBio et "DG" : ok
		SraParameter param_DG = new SraParameter();
		param_DG.type = paramType;
		param_DG.code = "pacbio_smrt_" + "DG";
		param_DG.code = param_DG.code.toLowerCase();
		param_DG.value = "WGS data, PACBIO INSTRUMENT sequencing";
		sraParameterAPI.save(param_DG);
		cp++;
		println("Pour pacbioExperimentTitle, insertion de " + cp + " donnees de type '" + paramType + "' dans SraParameter");
	}
	
	public void genere_sraParameter_oxfordNanoporeExperimentTitle() throws DAOException, APIException { 
		int cp = 0;
		String paramType = "experimentTitle";
		// definition des titres pour oxford_nanopore et "ONT" : ok
		SraParameter param_ONT = new SraParameter();
		param_ONT.type = paramType;
		param_ONT.code = "oxford_nanopore_" + "ONT";
		param_ONT.code = param_ONT.code.toLowerCase();
		param_ONT.value = "WGS data, Oxford Nanopore Technologies INSTRUMENT sequencing";
		sraParameterAPI.save(param_ONT);
		cp++;	
		SraParameter param_RD = new SraParameter();
		param_RD.type = paramType;
		param_RD.code = "oxford_nanopore_" + "RD";
		param_RD.code = param_RD.code.toLowerCase();
		param_RD.value = "RNA-Seq data, Oxford Nanopore Technologies INSTRUMENT sequencing";
		sraParameterAPI.save(param_RD);		
		cp++;	
		println("Pour oxfordNanoporeExperimentTitle, insertion de " + cp + " donnees de type '" + paramType + "' dans SraParameter");
	}
	
	public void genere_sraParameter_mgiExperimentTitle() throws DAOException, APIException {
		int cp = 0;
		String paramType = "experimentTitle";
		// definition des titres pour MGI (dnbseq) et "DK", "DL" : ok		
		List<String> libProcessTypeCodes = new ArrayList<String>(Arrays.asList("DK", "DL"));
		for (String libProcessTypeCode : libProcessTypeCodes) {
			SraParameter param = new SraParameter();
			param.type = paramType;
			param.code = "dnbseq_" + libProcessTypeCode;
			param.code = param.code.toLowerCase();
			param.value = "WGS data, MGI INSTRUMENT paired end sequencing";
			sraParameterAPI.save(param);
			cp++;
		}	
		println("Pour mgiExperimentTitle, insertion de " + cp + " donnees de type '" + paramType + "' dans SraParameter");
	}
	
//	public void genere_sraParameter_bionanoExperimentTitle() throws DAOException, APIException {
//		int cp = 0;
//		String paramType = "experimentTitle";
//		// definition des titres pour bionano et "DLS", "NLRS" => normalement pour analyse et non pour experiment
//		libProcessTypeCodes = new ArrayList(Arrays.asList("DLS", "NLRS"));
//		for (String libProcessTypeCode : libProcessTypeCodes) {
//			SraParameter param = new SraParameter();
//		
//			param.type = paramType;
//			param.code="Illumina " + libProcessTypeCode;
//			param.value = "Optical map data, Bionano INSTRUMENT";
//			sraParameterAPI.save(param);
//			cp++;
//		}	
//		println("ajout de " + cp + " titres d'experiments pour oxford nanopores");
// }
		
		
	

	public void genere_sraParameter_typeReadset() throws DAOException, APIException { 
		//println("Generation des typeReadset");

		List <String> listTypeReadset = new ArrayList<String>();
		listTypeReadset.add("illumina");
		listTypeReadset.add("oxford_nanopore");
		listTypeReadset.add("ls454");
		listTypeReadset.add("bionano");
		listTypeReadset.add("mgi");
		listTypeReadset.add("pacbio_smrt");

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
		@SuppressWarnings("serial")
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
		@SuppressWarnings("serial")
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
		@SuppressWarnings("serial")
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

	public void genere_sraParameter_legendeDropBox() throws DAOException, APIException { 
		//println("Generation des dropBox");		
		@SuppressWarnings("serial")
		Map<String, String> map = new HashMap<String, String>() {
			{
				put("Webin-9 (default dropBox)", "Webin-9"); 
				//put("Webin-77 (Atlasea dropBox)", "Webin-77"); 
			}
		};
		int cp = 0;
		for (Iterator<Entry<String, String>> iterator = map.entrySet().iterator(); iterator.hasNext();) {
			Entry<String, String> entry = iterator.next();
			SraParameter param = new SraParameter();
			param.type = "legendeDropBox";
			param.code = entry.getKey();
			param.value = entry.getValue();
			sraParameterAPI.save(param);
			cp++;
		}
		println("Insertion de " + cp + " donnees de type 'dropBox' dans SraParameter");
	}
	public void genere_sraParameter_dropBox() throws DAOException, APIException { 
		//println("Generation des dropBox");		
		@SuppressWarnings("serial")
		Map<String, String> map = new HashMap<String, String>() {
			{
				put("webin-9", "Webin-9"); 
				//put("webin-77", "Webin-77"); 
			}
		};
		int cp = 0;
		for (Iterator<Entry<String, String>> iterator = map.entrySet().iterator(); iterator.hasNext();) {
			Entry<String, String> entry = iterator.next();
			SraParameter param = new SraParameter();
			param.type = "dropBox";
			param.code = entry.getKey();
			param.value = entry.getValue();
			sraParameterAPI.save(param);
			cp++;
		}
		println("Insertion de " + cp + " donnees de type 'dropBox' dans SraParameter");
	}

	public Map<String, String> getCourantMap_libraryStrategy() {
		@SuppressWarnings("serial")
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
		@SuppressWarnings("serial")
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


	public void genere_sraParameter_libraryLayout() throws DAOException, APIException { 
		//println("Generation des libraryLayout");	

		@SuppressWarnings("serial")
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
	
	public Map<String, String> getCnsNameInstrumentModel() { 
		// donne les noms au CNS (peut etre different de ceux à l'EBI) des differents typeCode pour instrumentModel
		
		@SuppressWarnings("serial")
		// correspondance entre run.instrumentUsed.typeCode et typeName.
		// correspondance donnee par la description SQ (et non BI) 
		// voir description SQ pour Instrument et taper dans menu deroulant "Catégorie d'instruments" le mot "Séq" (avec accent) pour pouvoir sélect Illumina, Mgi, nanopore et "cartographie" pour selectionner bionano et Opgen
		Map<String, String> map = new HashMap<String, String>() {
			{	// instrument model pour pacbio : 
				put("SEQUEL2".toLowerCase(),      "SEQUEL II");
				put("REVIO".toLowerCase(),        "REVIO");
				
				// instrument model pour nanopore : 
				put("minION".toLowerCase(),       "MinION");
				put("mk1".toLowerCase(),          "Mk1");
				put("mk1b".toLowerCase(),         "Mk1B");
				put("mk1c".toLowerCase(),         "Mk1C");
				put("promethION".toLowerCase(),   "PromethION");
				put("gridION".toLowerCase(),      "GridION");
				
				// instrument model pour mgi : 
				put("DNBSEQG400".toLowerCase(),   "DNBSEQG400");
				
				// instrument model pour illumina : 
				put("GAIIx".toLowerCase(),        "GAIIx");
				put("MISEQ".toLowerCase(),        "MISEQ");
				put("HISEQ2000".toLowerCase(),    "HISEQ2000");
				put("HISEQ2500".toLowerCase(),    "HISEQ2500");
				put("HISEQ4000".toLowerCase(),    "HISEQ4000");
				put("NOVASEQ6000".toLowerCase(),  "NOVASEQ6000");
				put("NOVASEQXPLUS".toLowerCase(), "NOVASEQXPLUS");
				put("NEXTSEQ500".toLowerCase(),   "NEXTSEQ500");
				
				// instrument model pour opgen :            // Pas prevu d'etre soumis
				//put("ARGUS".toLowerCase(),       "ARGUS");
				
				// instrument model pour bionano :          // soumis avec analyse et donc n'intervient pas dans titre des experiments
				//put("IRYS".toLowerCase(),        "IRYS");
				//put("SAPHYR".toLowerCase(),      "SAPHYR");
				
			}
		};
		return map;
	}
	
	// utilisé pour substitution de "INSTRUMENT" dans titre de l'experiment
	public void genere_sraParameter_cnsNameInstrumentModel(String paramType) throws DAOException, APIException { 
		Map<String, String> map = getCnsNameInstrumentModel(); 
		int cp = genere_sraParameter(paramType, map);
		println("Pour cnsNameInstrumentModel, insertion de " + cp + " donnees de type '" + paramType + "' dans SraParameter");
	}


	
	// regarder description SQ(et non BI) et typCode, pour mettre à jour la correspondance entre les typeCodes des instruments 
	// (run.instrumentUsed.typeCode qui apparait dans description SQ) avec le nom des instruments autorisés à l'EBI
	// voir description SQ pour Instrument et taper dans menu deroulant "Catégorie d'instruments" 
	// le mot "Séq" (avec accent) pour pouvoir sélect Illumina, Mgi, nanopore et "cartographie" pour selectionner bionano et Opgen
	public Map<String, String> getOxfordNanoporeInstrumentModel() { 
		@SuppressWarnings("serial")
		Map<String, String> map = new HashMap<String, String>() {
			{// instrument model pour oxford_nanopore
				put("minION".toLowerCase(),     "MinION");
				put("mk1".toLowerCase(),        "MinION");
				put("mk1b".toLowerCase(),       "MinION");
				put("mk1c".toLowerCase(),       "MinION");
				put("promethION".toLowerCase(), "PromethION");
				put("gridION".toLowerCase(),    "gridION");
			}		
		};
		return map;
	}

	public Map<String, String> getL454InstrumentModel() { 
		@SuppressWarnings("serial")
		Map<String, String> map = new HashMap<String, String>() {
			{	
				// instrument model pour L454
				put("454 gs 20",           "454 GS 20"); 
				put("454 gs flx",          "454 GS FLX");
				put("454 gs flx titanium", "454 GS FLX Titanium");
				put("454 gs flx+",         "454 GS FLX+");	
			}		
		};
		return map;
	}

	@Deprecated
	public Map<String, String> getOpticalMappingInstrumentModel() { 
		@SuppressWarnings("serial")
		Map<String, String> map = new HashMap<String, String>() {
			{	// instrument model pour optical_mapping:
				put("argus",null); // pas prevus de soumettre ces données

			}		
		};
		return map;
	}
	
	
	// regarder description SQ(et non BI) et typCode, pour mettre à jour la correspondance entre les typeCodes des instruments 
	// (run.instrumentUsed.typeCode qui apparait dans description SQ) avec le nom des instruments autorisés à l'EBI
	// voir description SQ pour Instrument et taper dans menu deroulant "Catégorie d'instruments" 
	// le mot "Séq" (avec accent) pour pouvoir sélect Illumina, Mgi, nanopore et "cartographie" pour selectionner bionano et Opgen
	public Map<String, String> getIlluminaInstrumentModel() { 
		@SuppressWarnings("serial")
		Map<String, String> map = new HashMap<String, String>() {
			{	// type instrument model pour illumina dans sra version 1.5 : 

				// correspondance nomCnsInstrumentModel et instrumentModel pour Illumina:
				// on met systematiquement la valeur comme cle de hash ce qui permet de valider l'instrument
				// qu'il ait la valeur connu au CNS ou bien la valeur connu à l'EBI.
				
				put("GAIIx".toLowerCase(),                                "Illumina Genome Analyzer IIx");
				put("Illumina Genome Analyzer IIx".toLowerCase(),         "Illumina Genome Analyzer IIx");
				put("MISEQ".toLowerCase(),                                "Illumina MiSeq");
				put("Illumina MiSeq".toLowerCase(),                       "Illumina MiSeq");
				put("HISEQ2000".toLowerCase(),                            "Illumina HiSeq 2000");
				put("Illumina HiSeq 2000".toLowerCase(),                  "Illumina HiSeq 2000");
				put("HISEQ2500".toLowerCase(),                            "Illumina HiSeq 2500");
				put("Illumina HiSeq 2500".toLowerCase(),                  "Illumina HiSeq 2500");
				put("HISEQ4000".toLowerCase(),                            "Illumina HiSeq 4000");
				put("Illumina HiSeq 4000".toLowerCase(),                  "Illumina HiSeq 4000");
				put("NOVASEQ6000".toLowerCase(),                          "Illumina NovaSeq 6000");
				put("Illumina NovaSeq 6000".toLowerCase(),                "Illumina NovaSeq 6000");
				put("NOVASEQXPLUS".toLowerCase(),                         "Illumina NovaSeq X Plus");
				put("Illumina NovaSeq X Plus".toLowerCase(),              "Illumina NovaSeq X Plus");
				put("NEXTSEQ500".toLowerCase(),                           "NextSeq 500");
				put("NextSeq 500".toLowerCase(),                          "NextSeq 500");
			}
		};
		return map;
	}
	
	// regarder description SQ(et non BI) et typCode, pour mettre à jour la correspondance entre les typeCodes des instruments 
	// (run.instrumentUsed.typeCode qui apparait dans description SQ) avec le nom des instruments autorisés à l'EBI
	// voir description SQ pour Instrument et taper dans menu deroulant "Catégorie d'instruments" 
	// le mot "Séq" (avec accent) pour pouvoir sélect Illumina, Mgi, nanopore et "cartographie" pour selectionner bionano et Opgen
	public Map<String, String> getPacbioInstrumentModel() { //ok
		@SuppressWarnings("serial")
		Map<String, String> map = new HashMap<String, String>() {
			{	// instrument model pour pacbio :
				put("REVIO".toLowerCase(),     "Revio");
				put("SEQUEL2".toLowerCase(),   "Sequel II");  // nom utilisé au CNS
				put("Sequel II".toLowerCase(), "Sequel II");  // nom utilisé au CNS
			}
		};
		return map;
	}
		
	// regarder description SQ(et non BI) et typCode, pour mettre à jour la correspondance entre les typeCodes des instruments 
	// (run.instrumentUsed.typeCode qui apparait dans description SQ) avec le nom des instruments autorisés à l'EBI
	// voir description SQ pour Instrument et taper dans menu deroulant "Catégorie d'instruments" 
	// le mot "Séq" (avec accent) pour pouvoir sélect Illumina, Mgi, nanopore et "cartographie" pour selectionner bionano et Opgen
	public Map<String, String> getMgiInstrumentModel() { 
		@SuppressWarnings("serial")
		Map<String, String> map = new HashMap<String, String>() {
			{	// instrument model pour mgi :
				put("DNBSEQG400".toLowerCase(),"DNBSEQ-G400");
				put("DNBSEQ-G400".toLowerCase(),"DNBSEQ-G400");
			}
		};
		return map;
	}

	public void genere_sraParameter_allInstrumentModel() throws DAOException, APIException { 
		
		genere_sraParameter_oxfordNanoporeInstrumentModel(paramTypeAllInstrumentModel);
		genere_sraParameter_oxfordNanoporeInstrumentModel(paramTypeInstrumentModel);
		genere_sraParameter_oxfordNanoporeInstrumentModel(paramTypeOxfordNanoporeInstrumentModel);
		genere_sraParameter_l454InstrumentModel(paramTypeAllInstrumentModel);
		genere_sraParameter_l454InstrumentModel(paramTypeInstrumentModel);
		genere_sraParameter_l454InstrumentModel(paramTypeL454InstrumentModel);
		genere_sraParameter_illuminaInstrumentModel(paramTypeAllInstrumentModel);
		genere_sraParameter_illuminaInstrumentModel(paramTypeInstrumentModel);
		genere_sraParameter_illuminaInstrumentModel(paramTypeIlluminaInstrumentModel);
		genere_sraParameter_pacbioInstrumentModel(paramTypeAllInstrumentModel);
		genere_sraParameter_pacbioInstrumentModel(paramTypeInstrumentModel);
		genere_sraParameter_pacbioInstrumentModel(paramTypePacbioInstrumentModel);
		genere_sraParameter_mgiInstrumentModel(paramTypeAllInstrumentModel);
		genere_sraParameter_mgiInstrumentModel(paramTypeInstrumentModel);
		genere_sraParameter_mgiInstrumentModel(paramTypeMgiInstrumentModel);
		// ajout de unspecified dans la collection AllInstrumentModel :
		Map<String, String> map = new HashMap<String, String>();
		map.put("unspecified", "unspecified"); // pour premiers experiments lorsque instrumentModel n'etait pas obligatoire
		genere_sraParameter(paramTypeAllInstrumentModel, map);
		println("ajout de unspecified dans " + paramTypeAllInstrumentModel);
	}
		
	public void delete_sraParameter_allInstrumentModel() throws DAOException, APIException { 
		delete_sraParameter_for_type(paramTypeOxfordNanoporeInstrumentModel);
		delete_sraParameter_for_type(paramTypeL454InstrumentModel);
		delete_sraParameter_for_type(paramTypeIlluminaInstrumentModel);
		delete_sraParameter_for_type(paramTypePacbioInstrumentModel);
		delete_sraParameter_for_type(paramTypeMgiInstrumentModel);
		delete_sraParameter_for_type(paramTypeInstrumentModel);
		delete_sraParameter_for_type(paramTypeAllInstrumentModel);
	}

	public void genere_sraParameter_oxfordNanoporeInstrumentModel(String paramType) throws DAOException, APIException { 
		Map<String, String> map = getOxfordNanoporeInstrumentModel(); 
		int cp = genere_sraParameter(paramType, map);
		println("Pour oxfordNanoporeInstrumentModel, insertion de " + cp + " donnees de type '" + paramType + "' dans SraParameter");		
	}
	
	public void genere_sraParameter_l454InstrumentModel(String paramType) throws DAOException, APIException { 
		Map<String, String> map = getL454InstrumentModel(); 
		int cp = genere_sraParameter(paramType, map);
		println("Pour l454InstrumentModel, insertion de " + cp + " donnees de type '" + paramType + "' dans SraParameter");
	}
	
	public void genere_sraParameter_illuminaInstrumentModel(String paramType) throws DAOException, APIException { 
		Map<String, String> map = getIlluminaInstrumentModel(); 
		int cp = genere_sraParameter(paramType, map);
		println("Pour illuminaInstrumentModel, insertion de " + cp + " donnees de type '" + paramType + "' dans SraParameter");
	}

	public void genere_sraParameter_pacbioInstrumentModel(String paramType) throws DAOException, APIException { 
		Map<String, String> map = getPacbioInstrumentModel(); 
		int cp = genere_sraParameter(paramType, map);
		println("Pour pacbioInstrumentModel, insertion de " + cp + " donnees de type '" + paramType + "' dans SraParameter");
	}
	
	public void genere_sraParameter_mgiInstrumentModel(String paramType) throws DAOException, APIException { 
		Map<String, String> map = getMgiInstrumentModel(); 
		int cp = genere_sraParameter(paramType, map);
		println("Pour mgiInstrumentModel, insertion de " + cp + " donnees de type '" + paramType + "' dans SraParameter");
	}	
	
	public int genere_sraParameter(String paramType, Map<String, String> map) throws DAOException, APIException { 
		//println("Generation des instrumentModel");	

		int cp = 0;
		for (Iterator<Entry<String, String>> iterator = map.entrySet().iterator(); iterator.hasNext();) {
			cp++;
			Entry<String, String> entry = iterator.next();
			SraParameter param = new SraParameter();
			param.type = paramType;
			param.code = entry.getKey();
			param.value = entry.getValue();
			// ajout condition pour eviter doublon si genereIlluminaInstrumentModel appelé plusieurs fois ...
			if (sraParameterAPI.getParameter(param.type).containsKey(param.code) && sraParameterAPI.getParameter(param.type).containsValue(param.value)) {
			} else {
				sraParameterAPI.save(param);
			}
		}
		//println("Insertion de " + cp + " donnees de type '" + paramType + "' dans SraParameter");
		return cp;
	}

	
	public void genere_sraParameter_centerName() throws DAOException, APIException { 
		//println("Generation du centerName");	
		@SuppressWarnings("serial")
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
		@SuppressWarnings("serial")
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

		@SuppressWarnings("serial")
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
		@SuppressWarnings("serial")
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
		@SuppressWarnings("serial")
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
		statesForPseudo_ebiKnown.add(SRASubmissionStateNames.SUBR_SMD_IP);
		statesForPseudo_ebiKnown.add(SRASubmissionStateNames.SUBR_SMD_F);
		statesForPseudo_ebiKnown.add(SRASubmissionStateNames.SUBR_SMD_FE);
		statesForPseudo_ebiKnown.add(SRASubmissionStateNames.SUBR_FE); // peut etre pas utile car concerne uniquement study
		statesForPseudo_ebiKnown.add(SRASubmissionStateNames.SUBU_N);
		statesForPseudo_ebiKnown.add(SRASubmissionStateNames.SUBU_SMD_IP);
		statesForPseudo_ebiKnown.add(SRASubmissionStateNames.SUBU_SMD_F);
		statesForPseudo_ebiKnown.add(SRASubmissionStateNames.SUBU_SMD_FE);
		statesForPseudo_ebiKnown.add(SRASubmissionStateNames.SUBU_FE);

		@SuppressWarnings("serial")
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
		@SuppressWarnings("serial")
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

	public void genere_sraParameter_typePlatform() throws DAOException, APIException { 
		//println("Generation des typePlatform");	

		@SuppressWarnings("serial")
		Map<String, String> map = new HashMap<String, String>() {
			{
				put("illumina", "ILLUMINA"); 
				put("rsillumina", "ILLUMINA"); 
				put("ls454","LS454");  // pour les reprises d'historique, il existe des ls454
				put("oxford_nanopore","OXFORD_NANOPORE");
				put("rsnanopore","OXFORD_NANOPORE");
				put("pacbio_smrt","PACBIO_SMRT");
				put("rspacbio","PACBIO_SMRT");
				put("rsmgi", "DNBSEQ");
				put("dnbseq", "DNBSEQ");
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

	public void genere_sraParameter_static() throws DAOException, APIException { 
		@SuppressWarnings("serial")
		Map<String, String> map = new HashMap<String, String>() {
			{	put("centerName", "GSC"); 
				put("laboratoryName", "Genoscope - CEA"); 
				put("submissionRootDirectory","submissionRootDirectory"); 
				put("defaultLibraryConstructionProtocol","none provided");
				put("admin","william");
				put("xmlSubmission","submission.xml");
				put("xmlStudies","study.xml");
				put("xmlSamples", "sample.xml");
				put("xmlExperiments", "experiment.xml");
				put("xmlRuns","run.xml");
				put("xmlProjects","project.xml");
				put("xmlUmbrella","umbrella.xml");
				put("xmlAnalysis","analysis.xml");
				put("defaultDropBox", "Webin-9");
				put("defaultPassword", "Axqw16nI");

			}
		};  
		int cp = 0;
		for (Iterator<Entry<String, String>> iterator = map.entrySet().iterator(); iterator.hasNext();) {
			cp++;
			Entry<String, String> entry = iterator.next();
			SraParameter param = new SraParameter();
			param.type = "static";
			param.code = entry.getKey();
			param.value = entry.getValue();
			sraParameterAPI.save(param);
		}
		println("Insertion de " + cp + " donnees de type 'static' dans SraParameter");
	}
	

	
}
