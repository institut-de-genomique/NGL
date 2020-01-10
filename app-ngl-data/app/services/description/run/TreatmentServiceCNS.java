package services.description.run;

import java.awt.Image;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import models.laboratory.common.description.Level;
import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.run.description.TreatmentCategory;
import models.laboratory.run.description.TreatmentContext;
import models.laboratory.run.description.TreatmentType;
import models.laboratory.run.description.TreatmentTypeContext;
import models.utils.dao.DAOException;
import models.utils.dao.DAOHelpers;
import play.data.validation.ValidationError;
import services.description.Constants;
import services.description.DescriptionFactory;
import services.description.common.LevelService;

public class TreatmentServiceCNS extends AbstractTreatmentService {

	@Override
	public  void saveTreatmentCategory(Map<String, List<ValidationError>> errors) throws DAOException {
		List<TreatmentCategory> l = new ArrayList<>();
		for (TreatmentCategory.CODE code : TreatmentCategory.CODE.values()) {
			l.add(DescriptionFactory.newSimpleCategory(TreatmentCategory.class, code.name(), code.name()));
		}
		DAOHelpers.saveModels(TreatmentCategory.class, l, errors);
	}

	@Override
	public  void saveTreatmentContext(Map<String, List<ValidationError>> errors) throws DAOException {
		List<TreatmentContext> l = new ArrayList<>();
		l.add(DescriptionFactory.newTreatmentContext("Default","default"));
		l.add(DescriptionFactory.newTreatmentContext("Read1","read1"));
		l.add(DescriptionFactory.newTreatmentContext("Read2","read2"));
		l.add(DescriptionFactory.newTreatmentContext("Pairs","pairs"));
		l.add(DescriptionFactory.newTreatmentContext("Single","single"));

		DAOHelpers.saveModels(TreatmentContext.class, l, errors);
	}

	@Override
	public  void saveTreatmentType(Map<String, List<ValidationError>> errors) throws DAOException {
		List<TreatmentType> l = new ArrayList<>();
		// common CNS - CNG
		l.add(DescriptionFactory.newTreatmentType("SAV","sav", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.sequencing.name()), "sav", 
				getSAVPropertyDefinitionsV2(), 
				Arrays.asList(getTreatmentTypeContext("read1", Boolean.FALSE), getTreatmentTypeContext("read2", Boolean.FALSE), getTreatmentTypeContext("default", Boolean.FALSE)), 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS), "10"));


		l.add(DescriptionFactory.newTreatmentType("NGS-RG","ngsrg-illumina", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.ngsrg.name()), "ngsrg", 
				getNGSRGIlluminaPropertyDefinitions(), 
				getTreatmentTypeContexts("default"), 
				DescriptionFactory.getInstitutes( Constants.CODE.CNS), "20"));		

		l.add(DescriptionFactory.newTreatmentType("NGS-RG","ngsrg-nanopore", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.ngsrg.name()), "ngsrg", 
				getNGSRGNanoporePropertyDefinitions(), 
				getTreatmentTypeContexts("default"), 
				DescriptionFactory.getInstitutes( Constants.CODE.CNS), "20"));		

		l.add(DescriptionFactory.newTreatmentType("Chromium","chromium", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.ngsrg.name()), "chromium", 
				getChromiumPropertyDefinitions(), 
				Arrays.asList(getTreatmentTypeContext("pairs",Boolean.TRUE)), 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS), "24"));	

		l.add(DescriptionFactory.newTreatmentType("Top Index","topIndex", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.ngsrg.name()), "topIndex", 
				getTopIndexPropertyDefinitions(), 
				Arrays.asList(getTreatmentTypeContext("default",Boolean.TRUE)), 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS), "25"));	

		l.add(DescriptionFactory.newTreatmentType("Global","global", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.global.name()), "global", 
				getReadSetPropertyDefinitions(), 
				getTreatmentTypeContexts("default"), 
				DescriptionFactory.getInstitutes( Constants.CODE.CNS), "0"));

		l.add(DescriptionFactory.newTreatmentType("Read Quality","read-quality", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.quality.name()), "readQualityRaw,readQualityClean,readQualityTrim", 
				getReadQualityPropertyDefinitions(), 
				Arrays.asList(getTreatmentTypeContext("read1",Boolean.TRUE), getTreatmentTypeContext("read2", Boolean.FALSE)), 
				DescriptionFactory.getInstitutes( Constants.CODE.CNS), "30,83,84"));
		
		l.add(DescriptionFactory.newTreatmentType("Read Quality","read-quality-nanopore", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.quality.name()), "readQuality", 
				getReadQualityNanoporePropertyDefinitions(), 
				getTreatmentTypeContexts("default"), 
				DescriptionFactory.getInstitutes( Constants.CODE.CNS), "30,83,84"));

		l.add(DescriptionFactory.newTreatmentType("Duplicates","duplicates", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.quality.name()), "duplicatesRaw,duplicatesClean,duplicatesTrim", 
				getDuplicatesPropertyDefinitions(), 
				Arrays.asList(getTreatmentTypeContext("read1",Boolean.TRUE), getTreatmentTypeContext("read2", Boolean.FALSE), getTreatmentTypeContext("pairs", Boolean.FALSE)), 
				DescriptionFactory.getInstitutes( Constants.CODE.CNS), "32,86,87"));

		l.add(DescriptionFactory.newTreatmentType("Mapping","mapping", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.quality.name()), "mapping", 
				getMappingPropertyDefinitions(), 
				Arrays.asList(getTreatmentTypeContext("read1",Boolean.FALSE), getTreatmentTypeContext("read2", Boolean.FALSE), getTreatmentTypeContext("pairs", Boolean.FALSE), getTreatmentTypeContext("default", Boolean.TRUE)),
				//getTreatmentTypeContexts("pairs", "default"), 
				DescriptionFactory.getInstitutes( Constants.CODE.CNS), "90"));

		l.add(DescriptionFactory.newTreatmentType("Mapping Nanopore ","mapping-nanopore", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.quality.name()), "mapping", 
				getMappingNanoporePropertyDefinitions(), 
				getTreatmentTypeContexts("default"), 
				DescriptionFactory.getInstitutes( Constants.CODE.CNS), "90"));

		l.add(DescriptionFactory.newTreatmentType("Trimming","trimming", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.quality.name()), "trimmingStd,trimmingVector", 
				getTrimmingPropertyDefinitions(), 
				Arrays.asList(getTreatmentTypeContext("read1",Boolean.TRUE), getTreatmentTypeContext("read2", Boolean.FALSE), getTreatmentTypeContext("pairs", Boolean.FALSE), 
						getTreatmentTypeContext("single", Boolean.FALSE)), 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS), "33,50"));
		
		l.add(DescriptionFactory.newTreatmentType("Trimming Nanopore","trimming-nanopore", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.quality.name()), "trimming", 
				getTrimmingNanoporePropertyDefinitions(), 
				getTreatmentTypeContexts("default"), 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS), "34"));

		l.add(DescriptionFactory.newTreatmentType("First Base Report", "firstBaseReport", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.quality.name()), "firstBaseReport",
				getFirstBaseReportPropertyDefinitions(),
				Arrays.asList(getTreatmentTypeContext("read1", Boolean.TRUE)),
				DescriptionFactory.getInstitutes(Constants.CODE.CNS), "5")
				);

		l.add(DescriptionFactory.newTreatmentType("Contamination","contamination", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.quality.name()), "contaminationColi,contaminationVector,contaminationPhiX,contaminationAmplicons", 
				getContaminationPropertyDefinitions(), 
				Arrays.asList(getTreatmentTypeContext("read1",Boolean.FALSE), getTreatmentTypeContext("pairs", Boolean.FALSE), 
						getTreatmentTypeContext("single", Boolean.FALSE)), 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS), "35,36,60,61"));

		//specific CNS
		l.add(DescriptionFactory.newTreatmentType("Taxonomy","taxonomy", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.quality.name()), "taxonomy", 
				getTaxonomyPropertyDefinitions(), 
				Arrays.asList(getTreatmentTypeContext("read1",Boolean.FALSE), getTreatmentTypeContext("pairs", Boolean.FALSE)), 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS), "70,71"));

		l.add(DescriptionFactory.newTreatmentType("Sorting Ribo","sorting-ribo", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.quality.name()), "sortingRibo", 
				getSortingRiboPropertyDefinitions(), 
				Arrays.asList(getTreatmentTypeContext("read1",Boolean.TRUE), getTreatmentTypeContext("read2", Boolean.FALSE), getTreatmentTypeContext("pairs", Boolean.FALSE), getTreatmentTypeContext("single", Boolean.FALSE), getTreatmentTypeContext("default",Boolean.FALSE)), 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS), "80"));

		l.add(DescriptionFactory.newTreatmentType("Merging","merging", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.quality.name()), "merging", 
				getMergingPropertyDefinitions(), 
				getTreatmentTypeContexts("pairs"), 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS), "100"));
		
		l.add(DescriptionFactory.newTreatmentType("Clusters Db Assignation","clustersdb-assignation", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.quality.name()), "ClustersDbAssignation", 
				getClusterDbAssignationPropertyDefinitions(), 
				getTreatmentTypeContexts("read1"), 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS), "110"));

		l.add(DescriptionFactory.newTreatmentType("Merging BA","merging-ba", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.ba.name()), "mergingBA", 
				getMergingBAPropertyDefinitions(), 
				getTreatmentTypeContexts("pairs"), 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS), "110"));

		l.add(DescriptionFactory.newTreatmentType("Assembly BA","assembly-ba", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.ba.name()), "assemblyBA", 
				getAssemblyBAPropertyDefinitions(), 
				getTreatmentTypeContexts("pairs"), 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS), "120"));

		l.add(DescriptionFactory.newTreatmentType("Contig Filter BA","contigFilter-ba", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.ba.name()), "contigFilterBA", 
				getContigFilterBAPropertyDefinitions(), 
				getTreatmentTypeContexts("pairs"), 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS), "125"));

		l.add(DescriptionFactory.newTreatmentType("Scaffolding BA","scaffolding-ba", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.ba.name()), "scaffoldingBA", 
				getScaffoldingBAPropertyDefinitions(), 
				getTreatmentTypeContexts("pairs"), 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS), "130"));
		
		l.add(DescriptionFactory.newTreatmentType("Assembly Filter BA","assemblyFilter-ba", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.ba.name()), "assemblyFilterBA", 
				getAssemblyFilterBAPropertyDefinitions(), 
				getTreatmentTypeContexts("pairs"), 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS), "135"));
		
		l.add(DescriptionFactory.newTreatmentType("Gap Closing BA","gapClosing-ba", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.ba.name()), "gapClosingBA", 
				getGapClosingBAPropertyDefinitions(), 
				getTreatmentTypeContexts("pairs"), 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS), "140"));
		
		
		l.add(DescriptionFactory.newTreatmentType("Amplicon Assignation BA","ampliconAssignation-ba", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.ba.name()), "ampliconAssignationBA", 
				getAssignationBAPropertyDefinitions(), 
				getTreatmentTypeContexts("read1"), 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS), "150"));

		//Nanopore
		l.add(DescriptionFactory.newTreatmentType("MinKnow-Metrichor","minknow-metrichor", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.sequencing.name()), "minknowMetrichor", 
				getMinknowMetrichorPropertyDefinitions(), 
				getTreatmentTypeContexts("default"), 
				DescriptionFactory.getInstitutes( Constants.CODE.CNS), "20"));	
		
		l.add(DescriptionFactory.newTreatmentType("MinKnow-Basecalling","minknow-basecalling", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.sequencing.name()), "minknowBasecalling", 
				getMinknowBaseCallingPropertyDefinitions(), 
				getTreatmentTypeContexts("default"), 
				DescriptionFactory.getInstitutes( Constants.CODE.CNS), "20"));	

		
		
		DAOHelpers.saveModels(TreatmentType.class, l, errors);
	}


	private static List<TreatmentTypeContext> getTreatmentTypeContexts(String...codes) throws DAOException {
		List<TreatmentTypeContext> contexts = new ArrayList<>();
		for(String code : codes){
			contexts.add(getTreatmentTypeContext(code, Boolean.TRUE));
		}		
		return contexts;
	}

	private static TreatmentTypeContext getTreatmentTypeContext(String code, Boolean required) throws DAOException {
		TreatmentContext tc = DAOHelpers.getModelByCode(TreatmentContext.class, TreatmentContext.find, code);
		TreatmentTypeContext ttc = new TreatmentTypeContext(tc, required);
		return ttc;	
	}

	public static List<PropertyDefinition> getTaxonomyPropertyDefinitions() throws DAOException{
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Logiciel","software",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Pairs), String.class, false,
		DescriptionFactory.newValues("kraken","megablast_megan","sortmerna2.1","centrifuge1.0.3.b","centrifuge"),"single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Version base de données","databaseVersion",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Pairs), String.class, false, "single"));

		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Sample input","sampleInput",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Pairs), Long.class, false, "single"));

		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Organisme","organism",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Pairs), String.class, false, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Taxonomie","taxonomy",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Pairs), String.class, false, "single"));

		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Bilan par taxon (taxon)","taxonBilan.taxon",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Pairs), String.class, false, "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Bilan par taxon (nb seq)","taxonBilan.nbSeq",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Pairs), Long.class, false, "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Bilan par taxon (%)","taxonBilan.percent",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Pairs), Double.class, false, "object_list"));	

		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Bilan mitochondrion par taxon (taxon)","taxonBilanMitochondrion.taxon",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Pairs), String.class, false, "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Bilan mitochondrion par taxon (nb seq)","taxonBilanMitochondrion.nbSeq",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Pairs), Long.class, false, "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Bilan mitochondrion par taxon (%)","taxonBilanMitochondrion.percent",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Pairs), Double.class, false, "object_list"));

		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Bilan plastid par taxon (taxon)","taxonBilanPlastid.taxon",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Pairs), String.class, false, "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Bilan plastid par taxon (nb seq)","taxonBilanPlastid.nbSeq",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Pairs), Long.class, false, "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Bilan plastid par taxon (%)","taxonBilanPlastid.percent",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Pairs), Double.class, false, "object_list"));	

		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Bilan contamination par taxon (taxon)","taxonBilanContamination.taxon",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Pairs), String.class, false, "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Bilan contamination par taxon (nb seq)","taxonBilanContamination.nbSeq",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Pairs), Long.class, false, "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Bilan contamination par taxon (%)","taxonBilanContamination.percent",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Pairs), Double.class, false, "object_list"));	


		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Bilan par division (division)","divisionBilan.division",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Pairs), String.class, false,
				DescriptionFactory.newValues("Eukaryota","Bacteria","cellular organisms","Archaea","Viruses"), "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Bilan par division (nb seq)","divisionBilan.nbSeq",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Pairs), Long.class, false, "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Bilan par division (%)","divisionBilan.percent",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Pairs), Double.class, false, "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Bilan par mot-clé (mot-clé)","keywordBilan.keyword",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Pairs), String.class, false, 
				DescriptionFactory.newValues("mitochondri","virus","chloroplast","transposase",	"BAC", "Fungi"), "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Bilan par mot-clé (nb seq)","keywordBilan.nbSeq",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Pairs), Long.class, false, "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Bilan par mot-clé (%)","keywordBilan.percent",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Pairs), Double.class, false, "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("krona","krona",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Pairs), File.class, false, "file"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Arbre phylogénétique","phylogeneticTree",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Pairs), Image.class, false, "img"));
		return propertyDefinitions;		
	}

	public static List<PropertyDefinition> getSortingRiboPropertyDefinitions() throws DAOException{
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Reads input","readsInput",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2, Level.CODE.Single), Long.class, false, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Non-rRNA","no_rRNA",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2, Level.CODE.Single), Long.class, false, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("rRNA","rRNA",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2, Level.CODE.Single), Long.class, false, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% rRNA","rRNAPercent",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2, Level.CODE.Single), Double.class, false, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Bilan rRNA (type)","rRNABilan.type",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2, Level.CODE.Single), String.class, true,
				DescriptionFactory.newValues("PhiX", "Eukaryotic 18S", "Eukaryotic 28S", "Bacteria 16S", "Bacteria 23S", "Archeae 16S", "Archeae 23S", "Rfam 5.8S", "Rfam 5S", "Mitochondria 16S", "Mitochondria 23S", "Chloroplast 16S", "Chloroplast 23S"), "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Bilan rRNA (%)","rRNABilan.percent",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2, Level.CODE.Single), Double.class, true, "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Useful sequences","usefulSequences",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Useful bases","usefulBases",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Long.class, false, "single"));

		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Version logiciel","software",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Default), String.class, false, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Bilan par taxon (taxon)","taxonBilan.taxon",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2, Level.CODE.Single), String.class, false, "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Bilan par taxon (nb seq)","taxonBilan.nbSeq",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2, Level.CODE.Single), Long.class, false, "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Bilan par taxon (% vs rRNA tot.)","taxonBilan.percent",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2, Level.CODE.Single), Double.class, false, "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Bilan par taxon (% vs assigned)","taxonBilan.percentAssigned",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2, Level.CODE.Single), Double.class, false, "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("krona","krona",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2, Level.CODE.Single), File.class, false, "file"));
		return propertyDefinitions;		
	}
	
	public static List<PropertyDefinition> getMergingPropertyDefinitions() throws DAOException{
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Sample input","sampleInput",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Merged reads","mergedReads",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% merged reads","mergedReadsPercent",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Mediane size (bases)","medianeSize",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Average size (bases)","avgSize",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Min size (bases)","minSize",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Max size (bases)","maxSize",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Merged reads distribution","mergedReadsDistrib",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Image.class, true, "img"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Overlap distribution","overlapDistrib",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Image.class, true, "img"));
		return propertyDefinitions;		
	}
	

	public static List<PropertyDefinition> getMergingBAPropertyDefinitions() throws DAOException{
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Reads input (pairs)","readsInput", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Merged Reads","mergedReads", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% merged reads","mergedReadsPercent", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Mediane size (bases)","medianeSize", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Average size (bases)","avgSize", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Min size (bases)","minSize", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Max size (bases)","maxSize", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Integer.class, true, "single"));
		return propertyDefinitions;		
	}


	public static List<PropertyDefinition> getAssemblyBAPropertyDefinitions() throws DAOException{
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();	
		//new, 09-07-14 : temporary set required=false TO PASS TO TRUE
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Software version","software", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), String.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("N50 size","N50ContigSize", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Number of contigs","N50ContigNb", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("N80 size","N80ContigSize", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Number of contigs","N80ContigNb", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("N90 size","N90ContigSize", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Number of contigs","N90ContigNb", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Assembly size","assemblyContigSize", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Number of contigs","assemblyContigNb", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Smallest contig size","minContigSize", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Largest contig size","maxContigSize", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Average contig size","averageContigSize", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Double.class, true, "single"));

		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Size","contigSizeRepartition.size", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Integer.class, true, "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Number","contigSizeRepartition.contigNumber", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Integer.class, true, "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Percent","contigSizeRepartition.contigPercent", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Double.class, true, "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Cumulative size","contigSizeRepartition.cumulativeSize", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Integer.class, true, "object_list"));	 
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% cumulative size","contigSizeRepartition.cumulativeSizePercent", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Double.class, true, "object_list"));
		//end

		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% GC","GCpercent",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Double.class, true, "single"));

		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Assembly statistics","assemblyStatistics",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), String.class, false, "single"));	

		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Percentage of assembled reads","readsAssembledPercent", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Double.class, false, "single"));

		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% expected pool size", "expectedPoolSizePercent", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Double.class, true, "single"));

		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Number of reads used for assembly", "readsUsed", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Number of bases used for assembly", "basesUsed", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Coverage used for assembly", "coverageUsed", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Number of reads really assembled", "readsAssembled", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Long.class, false, "single"));



		return propertyDefinitions;		
	}

	

	public static List<PropertyDefinition> getScaffoldingBAPropertyDefinitions() throws DAOException{
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();

		//new, 09-07-14 : temporary set required=false TO PASS TO TRUE
		//propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Path","path",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Default), String.class, true, "single"));

		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("N50 size","N50ScaffoldSize",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Number of scaffolds","N50ScaffoldNb",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("N80 size","N80ScaffoldSize",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Number of scaffolds","N80ScaffoldNb",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("N90 size","N90ScaffoldSize",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Number of scaffolds","N90ScaffoldNb",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Assembly size","assemblyScaffoldSize",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Integer.class, true, "single"));

		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Number of scaffolds","assemblyScaffoldNb",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Smallest scaffold size","minScaffoldSize",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Largest scaffold size","maxScaffoldSize",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Average scaffold size","averageScaffoldSize",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Double.class, true, "single"));

		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Size","scaffoldSizeRepartition.size", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Integer.class, true, "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Number","scaffoldSizeRepartition.scaffoldNumber", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Integer.class, true, "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Percent","scaffoldSizeRepartition.scaffoldPercent", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Double.class, true, "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Cumulative size","scaffoldSizeRepartition.cumulativeSize", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Integer.class, true, "object_list"));	 
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% cumulative size","scaffoldSizeRepartition.cumulativeSizePercent", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Double.class, true, "object_list"));

		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% GC","GCpercent", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nombre de N","numberOfN", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Integer.class, true, "single"));

		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Assembly statistics","scaffoldingStatistics",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), String.class, false, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nombre de paires satisfaisantes","nbPairedSatisfied",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nombre de paires non satisfaisantes","nbPairedUnsatisfied",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nb paires mappées","nbMappedPairs",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nb séquences mappées","nbMappedSequences",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Taille d'insert médiane","medianInsertSize",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Double.class, true, "single"));

		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% expected pool size", "expectedPoolSizePercent", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Double.class, true, "single"));

		return propertyDefinitions;		
	}
	
	public static List<PropertyDefinition> getAssemblyFilterBAPropertyDefinitions() throws DAOException{
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nb de bases conservés","preservedBases",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Integer.class, true, "single"));
		//propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% bases perdues","lostBasesPercent",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nb contigs/scaffolds conservés","preservedSequences",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nombre de N","numberOfN",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Couverture conservés","preservedCoverage",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Integer.class, true, "single"));
		
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nb contigs/scaffolds","assemblySequenceNb",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Assembly size (bases)","assemblySize",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Smallest contigs/scaffolds size (bases)","minSequenceSize",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Largest contigs/scaffolds size (bases)","maxSequenceSize",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Average contigs/scaffolds size (bases)","averageSequenceSize",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% GC","GCpercent",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("N50 size (bases)","N50SequenceSize",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nb contigs/scaffolds N50","N50SequenceNb",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("N90 size (bases)","N90SequenceSize",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nb contigs/scaffolds N90","N90SequenceNb",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("N80 size (bases)","N80SequenceSize",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nb contigs/scaffolds N80","N80SequenceNb",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Integer.class, true, "single"));
		
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("size","sequenceSizeRepartition.size",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Integer.class, true, "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("number of sequences","sequenceSizeRepartition.sequenceNumber",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Integer.class, true, "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% sequences","sequenceSizeRepartition.sequencePercent",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Double.class, true, "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Cumulative Size (bases)","sequenceSizeRepartition.cumulativeSize",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Integer.class, true, "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% assembly size (bases)","sequenceSizeRepartition.cumulativeSizePercent",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Double.class, true, "object_list"));
		
	
		return propertyDefinitions;		
	}


	public static List<PropertyDefinition> getGapClosingBAPropertyDefinitions() throws DAOException{
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Actual gap sum","actualGapSum", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Extended gap sum","extendGapSum", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Actual gap count","actualGapCount", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Finish gap count","finishGapCount", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Integer.class, true, "single"));		
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% N","percentOfN", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% GC","GCpercent", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Double.class, false, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nombre de N","numberOfN", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Integer.class, false, "single"));
		return propertyDefinitions;		
	}

	public static List<PropertyDefinition> getContigFilterBAPropertyDefinitions() throws DAOException{
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nb bases conservées","storedBases", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Integer.class, true, "single"));
		// temporary set to false (computed by NGL ?)
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% bases perdues","lostBasesPercent", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Double.class, false, "single"));
		return propertyDefinitions;		
	}
	
	public static List<PropertyDefinition> getClusterDbAssignationPropertyDefinitions() throws DAOException{
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("cluster","assignationBilan.cluster",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), String.class, false, "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("OTU","assignationBilan.otu",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), String.class, false, "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Abundance","assignationBilan.abundance",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), Integer.class, false, "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Taxonomy","assignationBilan.taxonomy",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), String.class, false, "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% id","assignationBilan.idPercent",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), Double.class, false, "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% match length","assignationBilan.matchLength",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), Double.class, false, "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% abundance cluster percent","assignationBilan.abundanceClusterPercent",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), Double.class, false, "object_list"));
		return propertyDefinitions;		
	}
	
	public static List<PropertyDefinition> getAssignationBAPropertyDefinitions() throws DAOException{
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("control name","assignationBilan.controlName",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Read1), String.class, false, "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("cluster","assignationBilan.cluster",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Read1), String.class, false, "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("OTU","assignationBilan.otu",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Read1), String.class, false, "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Abundance","assignationBilan.abundance",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Read1), Integer.class, false, "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Taxonomy","assignationBilan.taxonomy",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Read1), String.class, false, "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% id","assignationBilan.idPercent",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Read1), Double.class, false, "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% match length","assignationBilan.matchLength",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Read1), Double.class, false, "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% abundance cluster percent","assignationBilan.abundanceClusterPercent",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Read1), Double.class, false, "object_list"));
		return propertyDefinitions;		
	}

	
}
