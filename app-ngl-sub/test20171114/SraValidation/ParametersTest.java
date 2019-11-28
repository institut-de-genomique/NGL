package SraValidation;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import mail.MailServiceException;
import models.sra.submit.common.instance.Sample;
import models.sra.submit.common.instance.Study;
import models.sra.submit.common.instance.Submission;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.sra.instance.ReadSpec;
import models.sra.submit.util.SraException;
import models.sra.submit.util.SraParameter;
import models.sra.submit.util.VariableSRA;
import models.utils.InstanceConstants;

import org.junit.Assert;
import org.junit.Test;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import fr.cea.ig.MongoDBDAO;
import play.libs.F.Promise;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;

import java.util.Calendar;

import services.FileAcServices;
import services.ReleaseServices;
import services.XmlServices;
import utils.AbstractTestsSRA;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.apache.commons.lang3.StringUtils;

import validation.ContextValidation;
import play.Logger;

import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;


public class ParametersTest extends AbstractTestsSRA {

	//@Test
	public void test_map_libProcessTypeCodeValue_orientation() {
		SraParameter sraParam = new SraParameter();
		Map<String, String> map = sraParam.getParameter("libProcessTypeCodeValue_orientation");

		for (Iterator<Entry<String, String>> iterator = map.entrySet().iterator(); iterator.hasNext();) {
		  Entry<String, String> entry = iterator.next();
		  
		}

		if (map.get("TA")!= null){
			System.out.println("ok pour TA voici sa valeur "+ map.get("TA"));
		}

	}
	
	//@Test
	public void genere_sraParameter_libProcessTypeCodeValue_orientation() {
		System.out.println("Generation de la map libProcessTypeCodeValue_orientation");
		List <String> libProcessTypeCodeValues_1 = new ArrayList<String>();
		libProcessTypeCodeValues_1.add("A");
		libProcessTypeCodeValues_1.add("C");
		List <String> libProcessTypeCodeValues_2 = new ArrayList<String>();
		libProcessTypeCodeValues_2.add("E");
		libProcessTypeCodeValues_2.add("F");
		libProcessTypeCodeValues_2.add("H");
		libProcessTypeCodeValues_2.add("K");
		libProcessTypeCodeValues_2.add("L");
		libProcessTypeCodeValues_2.add("U");
		libProcessTypeCodeValues_2.add("W");
		libProcessTypeCodeValues_2.add("Z");
		libProcessTypeCodeValues_2.add("DB");
		libProcessTypeCodeValues_2.add("DC");
		libProcessTypeCodeValues_2.add("DD");
		libProcessTypeCodeValues_2.add("DE");
		libProcessTypeCodeValues_2.add("RA");
		libProcessTypeCodeValues_2.add("RB");
		libProcessTypeCodeValues_2.add("TA");
		libProcessTypeCodeValues_2.add("TB");
			

		for (String libProcessTypeCodeValue : libProcessTypeCodeValues_1){
			SraParameter param = new SraParameter();
			param.type = "libProcessTypeCodeValue_orientation";
			param.code=libProcessTypeCodeValue;
			param.value = "reverse-forward";
			MongoDBDAO.save(InstanceConstants.SRA_PARAMETER_COLL_NAME, param);
		}

		for (String libProcessTypeCodeValue : libProcessTypeCodeValues_2){
			SraParameter param = new SraParameter();
			param.type = "libProcessTypeCodeValue_orientation";
			param.code=libProcessTypeCodeValue;
			param.value = "forward-reverse";
			MongoDBDAO.save(InstanceConstants.SRA_PARAMETER_COLL_NAME, param);
		}	
	}
	
	
	//@Test
	public void genere_sraParameter_typeReadset() { 
		System.out.println("Generation des typeReadset");

		List <String> listTypeReadset = new ArrayList<String>();
		listTypeReadset.add("illumina");
		listTypeReadset.add("nanopore");
		listTypeReadset.add("ls454");
		
		for (String typeReadset : listTypeReadset){
			System.out.println("typeReadset = " + typeReadset);
			SraParameter param = new SraParameter();
			param.type = "typeReadset";
			param.code= typeReadset;
			param.value = typeReadset;
			MongoDBDAO.save(InstanceConstants.SRA_PARAMETER_COLL_NAME, param);
		}
	}
		
	//@Test
	public void genere_sraParameter_existingStudyType() { 
		System.out.println("Generation des existingStudyType");
		Map map = new HashMap<String, String>() {
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
			}
		};
			
		for (Iterator<Entry<String, String>> iterator = map.entrySet().iterator(); iterator.hasNext();) {
			  Entry<String, String> entry = iterator.next();
			  SraParameter param = new SraParameter();
			  param.type = "existingStudyType";
			  param.code = entry.getKey();
			  param.value = entry.getValue();
			  MongoDBDAO.save(InstanceConstants.SRA_PARAMETER_COLL_NAME, param);
		}
	}
	
	//@Test
	public void genere_sraParameter_libraryLayoutOrientation() { 
		System.out.println("Generation des libraryLayoutOrientation");
		Map<String, String> map = new HashMap<String, String>() {
			{
				put("forward", "Forward"); 
				put("forward-reverse", "Forward-Reverse");
				put("reverse-forward", "Reverse-Forward");	
				put("forward-forward", "Forward-Forward");	
			}
		};
		for (Iterator<Entry<String, String>> iterator = map.entrySet().iterator(); iterator.hasNext();) {
			  Entry<String, String> entry = iterator.next();
			  SraParameter param = new SraParameter();
			  param.type = "libraryLayoutOrientation";
			  param.code = entry.getKey();
			  param.value = entry.getValue();
			  MongoDBDAO.save(InstanceConstants.SRA_PARAMETER_COLL_NAME, param);
		}
	}
	
	
	//@Test
	public void genere_sraParameter_librarySource() { 
		System.out.println("Generation des librarySource");		
		Map<String, String> map = new HashMap<String, String>() {
			{
			put("genomic", "GENOMIC"); 
			put("transcriptomic", "TRANSCRIPTOMIC");
			put("metagenomic", "METAGENOMIC"); 
			put("metatranscriptomic", "METATRANSCRIPTOMIC");
			put("synthetic", "SYNTHETIC");
			put("viral rna", "VIRAL RNA");
			put("other", "OTHER");
			}
		};
		for (Iterator<Entry<String, String>> iterator = map.entrySet().iterator(); iterator.hasNext();) {
			  Entry<String, String> entry = iterator.next();
			  SraParameter param = new SraParameter();
			  param.type = "librarySource";
			  param.code = entry.getKey();
			  param.value = entry.getValue();
			  MongoDBDAO.save(InstanceConstants.SRA_PARAMETER_COLL_NAME, param);
		}
	}
	
	//@Test
	public void genere_sraParameter_libraryStrategy() { 
		System.out.println("Generation des libraryStrategy");	
		Map<String, String> map = new HashMap<String, String>() {
			{
			put("wgs", "WGS"); 
			put("wga", "WGA"); 
			put("wxs", "WXS");
			put("rna-seq","RNA-Seq");
			put("mirna-seq","miRNA-Seq");
			put("ncrna-seq","ncRNA-Seq");
			put("wcs", "WCS"); 
			put("clone", "CLONE"); 
			put("poolclone", "POOLCLONE"); 
			put("amplicon", "AMPLICON"); 
			put("cloneend", "CLONEEND"); 
			put("finishing", "FINISHING"); 
			put("chip-seq", "ChIP-Seq");
			put("mnase-seq", "MNase-Seq");
			put("dnase-hypersensitivity", "DNase-Hypersensitivity");
			put("bisulfite-seq", "Bisulfite-Seq");
			put("est", "EST");
			put("fl-cdna", "FL-cDNA");
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
			put("other", "OTHER");
			}
		};  
		for (Iterator<Entry<String, String>> iterator = map.entrySet().iterator(); iterator.hasNext();) {
		  Entry<String, String> entry = iterator.next();
		  SraParameter param = new SraParameter();
		  param.type = "libraryStrategy";
		  param.code = entry.getKey();
		  param.value = entry.getValue();
		  MongoDBDAO.save(InstanceConstants.SRA_PARAMETER_COLL_NAME, param);
		}
	}
	
	//@Test
	public void genere_sraParameter_librarySelection() { 
		System.out.println("Generation des librarySelection");	
		Map<String, String> map = new HashMap<String, String>() {
			{
				put("random", "RANDOM"); 
				put("pcr", "PCR");
				put("random pcr", "RANDOM PCR"); 
				put("rt-pcr", "RT-PCR");
				put("synthetic", "SYNTHETIC");
				put("hmpr", "HMPR");
				put("mf","MF");
				put("repeat fractionation","repeat fractionation");
				put("size fractionation","size fractionation");
				put("msll","MSLL");
				put("cdna","cDNA");
				put("chip","ChIP");
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
		for (Iterator<Entry<String, String>> iterator = map.entrySet().iterator(); iterator.hasNext();) {
			  Entry<String, String> entry = iterator.next();
			  SraParameter param = new SraParameter();
			  param.type = "librarySelection";
			  param.code = entry.getKey();
			  param.value = entry.getValue();
			  MongoDBDAO.save(InstanceConstants.SRA_PARAMETER_COLL_NAME, param);
			}
		}
		
	//@Test
	public void genere_sraParameter_typePlatform() { 
		System.out.println("Generation des typePlatform");	
	
		Map<String, String> map = new HashMap<String, String>() {
			{
			put("illumina", "ILLUMINA"); 
			put("ls454","LS454");  // pour les reprises d'historique, il existe des ls454
			put("oxford_nanopore","OXFORD_NANOPORE");
			}
		};  
		for (Iterator<Entry<String, String>> iterator = map.entrySet().iterator(); iterator.hasNext();) {
		  Entry<String, String> entry = iterator.next();
		  SraParameter param = new SraParameter();
		  param.type = "typePlatform";
		  param.code = entry.getKey();
		  param.value = entry.getValue();
		  MongoDBDAO.save(InstanceConstants.SRA_PARAMETER_COLL_NAME, param);
		}
	}
		
	//@Test
	public void genere_sraParameter_libraryLayout() { 
		System.out.println("Generation des libraryLayout");	
	
		Map<String, String> map = new HashMap<String, String>() {
			{
			put("single", "SINGLE"); 
			put("paired", "PAIRED");
			}
		}; 
		for (Iterator<Entry<String, String>> iterator = map.entrySet().iterator(); iterator.hasNext();) {
			  Entry<String, String> entry = iterator.next();
			  SraParameter param = new SraParameter();
			  param.type = "libraryLayout";
			  param.code = entry.getKey();
			  param.value = entry.getValue();
			  MongoDBDAO.save(InstanceConstants.SRA_PARAMETER_COLL_NAME, param);
		}
		
	}
	
	
	//@Test
	public void genere_sraParameter_instrumentModel() { 
		System.out.println("Generation des instrumentModel");	
	
	Map<String, String> map = new HashMap<String, String>() {
		{	// instrument model pour optical_mapping:
			put("argus",null); // pas prevus de soumettre ces données

			// instrument model pour oxford_nanopore
			put("minion", "MinION");
			put("mk1", "MinION");
			put("mk1b", "MinION");
			
			// instrument model pour L454
			put("454 gs 20", "454 GS 20"); 
			put("454 gs flx", "454 GS FLX");
			put("454 gs flx titanium", "454 GS FLX Titanium");
			put("454 gs flx+", "454 GS FLX+");		
			
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
			
			// correspondance nomCnsInstrumentModel et instrumentModel :
			put("rgaiix","Illumina Genome Analyzer IIx");
			put("rhs2000","Illumina HiSeq 2000");
			put("rhs2500","Illumina HiSeq 2500");
			put("rhs2500r","Illumina HiSeq 2500");
			put("rmiseq","Illumina MiSeq");
			put("hiseq4000","Illumina HiSeq 4000");
			put("rhs4000","Illumina HiSeq 4000");

			put ("unspecified", "unspecified");  // ajout pour repriseHistorique.
			}
		};  
		for (Iterator<Entry<String, String>> iterator = map.entrySet().iterator(); iterator.hasNext();) {
		  Entry<String, String> entry = iterator.next();
		  SraParameter param = new SraParameter();
		  param.type = "instrumentModel";
		  param.code = entry.getKey();
		  param.value = entry.getValue();
		  MongoDBDAO.save(InstanceConstants.SRA_PARAMETER_COLL_NAME, param);
		}
	
	}
	
	//@Test
	public void genere_sraParameter_centerName() { 
		System.out.println("Generation du centerName");	
		Map<String, String> map=  new HashMap<String, String>() {
			{
				put("gsc", "GSC"); 
			}
		};
		for (Iterator<Entry<String, String>> iterator = map.entrySet().iterator(); iterator.hasNext();) {
			  Entry<String, String> entry = iterator.next();
			  SraParameter param = new SraParameter();
			  param.type = "centerName";
			  param.code = entry.getKey();
			  param.value = entry.getValue();
			  MongoDBDAO.save(InstanceConstants.SRA_PARAMETER_COLL_NAME, param);
		}
	}
	
	//@Test
	public void genere_sraParameter_laboratoryName() { 
		System.out.println("Generation du laboratoryName");	
		Map<String, String> map =  new HashMap<String, String>() {
			{
				put("genoscope - cea", "Genoscope - CEA"); 
			}
		};
		for (Iterator<Entry<String, String>> iterator = map.entrySet().iterator(); iterator.hasNext();) {
			  Entry<String, String> entry = iterator.next();
			  SraParameter param = new SraParameter();
			  param.type = "laboratoryName";
			  param.code = entry.getKey();
			  param.value = entry.getValue();
			  MongoDBDAO.save(InstanceConstants.SRA_PARAMETER_COLL_NAME, param);
		}
	}
	
	
	//@Test
	public void genere_sraParameter_analysisFileType() {
		System.out.println("Generation des analysisFileType");	

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
		for (Iterator<Entry<String, String>> iterator = map.entrySet().iterator(); iterator.hasNext();) {
			  Entry<String, String> entry = iterator.next();
			  SraParameter param = new SraParameter();
			  param.type = "analysisFileType";
			  param.code = entry.getKey();
			  param.value = entry.getValue();
			  MongoDBDAO.save(InstanceConstants.SRA_PARAMETER_COLL_NAME, param);
		}
	}
	
	
}
