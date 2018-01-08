package services.description.run;

import java.awt.Image;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import models.laboratory.common.description.Institute;
import models.laboratory.common.description.Level;
import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.run.description.TreatmentCategory;
import models.laboratory.run.description.TreatmentContext;
import models.laboratory.run.description.TreatmentType;
import models.laboratory.run.description.TreatmentTypeContext;
import models.utils.dao.DAOException;
import models.utils.dao.DAOHelpers;
import play.data.validation.ValidationError;
import services.description.DescriptionFactory;
import services.description.Constants;
import services.description.common.LevelService;

import com.typesafe.config.ConfigFactory;

public class TreatmentServiceGET extends AbstractTreatmentService {
	
	public  void saveTreatmentCategory(Map<String, List<ValidationError>> errors) throws DAOException {
		List<TreatmentCategory> l = new ArrayList<TreatmentCategory>();
		for (TreatmentCategory.CODE code : TreatmentCategory.CODE.values()) {
			l.add(DescriptionFactory.newSimpleCategory(TreatmentCategory.class, code.name(), code.name()));
		}
		DAOHelpers.saveModels(TreatmentCategory.class, l, errors);
	}
	
	public  void saveTreatmentContext(Map<String, List<ValidationError>> errors) throws DAOException {
		List<TreatmentContext> l = new ArrayList<TreatmentContext>();
		l.add(DescriptionFactory.newTreatmentContext("Default","default"));
		l.add(DescriptionFactory.newTreatmentContext("Read1","read1"));
		l.add(DescriptionFactory.newTreatmentContext("Read2","read2"));
		l.add(DescriptionFactory.newTreatmentContext("Pairs","pairs"));
		l.add(DescriptionFactory.newTreatmentContext("Single","single"));
		
		DAOHelpers.saveModels(TreatmentContext.class, l, errors);
	}
	
	public  void saveTreatmentType(Map<String, List<ValidationError>> errors) throws DAOException {
		List<TreatmentType> l = new ArrayList<TreatmentType>();
		l.add(DescriptionFactory.newTreatmentType("SAV","sav", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.sequencing.name()), "sav", 
				getSAVPropertyDefinitions(), 
				Arrays.asList(getTreatmentTypeContext("read1", Boolean.TRUE), getTreatmentTypeContext("read2", Boolean.FALSE)), 
				DescriptionFactory.getInstitutes( Constants.CODE.GET), "10"));
		
		l.add(DescriptionFactory.newTreatmentType("NGSRG","ngsrg-illumina", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.ngsrg.name()), "ngsrg", 
				getNGSRGPropertyDefinitions(), 
				getTreatmentTypeContexts("default"), 
				DescriptionFactory.getInstitutes( Constants.CODE.GET), "20"));		
		
		l.add(DescriptionFactory.newTreatmentType("Global","global", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.global.name()), "global", 
				getReadSetPropertyDefinitions(), 
				getTreatmentTypeContexts("default"), 
				DescriptionFactory.getInstitutes( Constants.CODE.GET), "0"));
		
		l.add(DescriptionFactory.newTreatmentType("Read Quality","read-quality", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.quality.name()), "readQualityRaw,readQualityClean", 
				getReadQualityPropertyDefinitions(), 
				Arrays.asList(getTreatmentTypeContext("read1",Boolean.TRUE), getTreatmentTypeContext("read2", Boolean.FALSE)), 
				DescriptionFactory.getInstitutes( Constants.CODE.GET), "30,83"));
		
		l.add(DescriptionFactory.newTreatmentType("Duplicates","duplicates", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.quality.name()), "duplicatesRaw,duplicatesClean", 
				getDuplicatesPropertyDefinitions(), 
				Arrays.asList(getTreatmentTypeContext("read1",Boolean.TRUE), getTreatmentTypeContext("read2", Boolean.FALSE), getTreatmentTypeContext("pairs", Boolean.FALSE)), 
				DescriptionFactory.getInstitutes( Constants.CODE.GET), "32,86"));
		
		l.add(DescriptionFactory.newTreatmentType("Mapping","mapping", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.quality.name()), "mapping", 
				getMappingPropertyDefinitions(), 
				Arrays.asList(getTreatmentTypeContext("read1",Boolean.FALSE), getTreatmentTypeContext("read2", Boolean.FALSE), getTreatmentTypeContext("pairs", Boolean.FALSE), getTreatmentTypeContext("default", Boolean.TRUE)),
				//getTreatmentTypeContexts("pairs", "default"), 
				DescriptionFactory.getInstitutes( Constants.CODE.GET), "90"));
		
		l.add(DescriptionFactory.newTreatmentType("Trimming","trimming", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.quality.name()), "trimmingStd,trimmingVector", 
				getTrimmingPropertyDefinitions(), 
				Arrays.asList(getTreatmentTypeContext("read1",Boolean.TRUE), getTreatmentTypeContext("read2", Boolean.FALSE), getTreatmentTypeContext("pairs", Boolean.FALSE), 
				getTreatmentTypeContext("single", Boolean.FALSE)), 
				DescriptionFactory.getInstitutes(Constants.CODE.GET), "33,50"));
		
		l.add(DescriptionFactory.newTreatmentType("First Base Report", "firstBaseReport", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.quality.name()), "firstBaseReport",
				getFirstBaseReportPropertyDefinitions(),
				Arrays.asList(getTreatmentTypeContext("read1", Boolean.TRUE)),
				DescriptionFactory.getInstitutes(Constants.CODE.GET), "5")
				);
		
		l.add(DescriptionFactory.newTreatmentType("Contamination","contamination", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.quality.name()), "contaminationColi,contaminationVector,contaminationPhiX", 
				getContaminationPropertyDefinitions(), 
				Arrays.asList(getTreatmentTypeContext("read1",Boolean.FALSE), getTreatmentTypeContext("pairs", Boolean.FALSE), 
				getTreatmentTypeContext("single", Boolean.FALSE)), 
				DescriptionFactory.getInstitutes(Constants.CODE.GET), "35,36,60"));
		
		l.add(DescriptionFactory.newTreatmentType("Taxonomy","taxonomy", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.quality.name()), "taxonomy", 
				getTaxonomyPropertyDefinitions(), 
				getTreatmentTypeContexts("read1"), 
				DescriptionFactory.getInstitutes(Constants.CODE.GET), "70"));
		
		l.add(DescriptionFactory.newTreatmentType("Sorting Ribo","sorting-ribo", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.quality.name()), "sortingRibo", 
				getSortingRiboPropertyDefinitions(), 
				Arrays.asList(getTreatmentTypeContext("read1",Boolean.TRUE), getTreatmentTypeContext("read2", Boolean.FALSE), getTreatmentTypeContext("pairs", Boolean.FALSE), getTreatmentTypeContext("single", Boolean.FALSE)), 
				DescriptionFactory.getInstitutes(Constants.CODE.GET), "80"));
		
		l.add(DescriptionFactory.newTreatmentType("Merging","merging", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.quality.name()), "merging", 
				getMergingPropertyDefinitions(), 
				getTreatmentTypeContexts("pairs"), 
				DescriptionFactory.getInstitutes(Constants.CODE.GET), "100"));
		
		l.add(DescriptionFactory.newTreatmentType("Merging BA","merging-ba", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.ba.name()), "mergingBA", 
				getMergingBAPropertyDefinitions(), 
				getTreatmentTypeContexts("pairs"), 
				DescriptionFactory.getInstitutes(Constants.CODE.GET), "110"));
		
		l.add(DescriptionFactory.newTreatmentType("Assembly BA","assembly-ba", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.ba.name()), "assemblyBA", 
				getAssemblyBAPropertyDefinitions(), 
				getTreatmentTypeContexts("pairs"), 
				DescriptionFactory.getInstitutes(Constants.CODE.GET), "120"));
		
		l.add(DescriptionFactory.newTreatmentType("Contig Filter BA","contigFilter-ba", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.ba.name()), "contigFilterBA", 
				getContigFilterBAPropertyDefinitions(), 
				getTreatmentTypeContexts("pairs"), 
				DescriptionFactory.getInstitutes(Constants.CODE.GET), "125"));
		
		l.add(DescriptionFactory.newTreatmentType("Scaffolding BA","scaffolding-ba", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.ba.name()), "scaffoldingBA", 
				getScaffoldingBAPropertyDefinitions(), 
				getTreatmentTypeContexts("pairs"), 
				DescriptionFactory.getInstitutes(Constants.CODE.GET), "130"));

		l.add(DescriptionFactory.newTreatmentType("Gap Closing BA","gapClosing-ba", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.ba.name()), "gapClosingBA", 
				getGapClosingBAPropertyDefinitions(), 
				getTreatmentTypeContexts("pairs"), 
				DescriptionFactory.getInstitutes(Constants.CODE.GET), "140"));

		//Nanopore
		l.add(DescriptionFactory.newTreatmentType("MinKnow-Metrichor","minknow-metrichor", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.sequencing.name()), "minknowMetrichor", 
				getMinknowMetrichorPropertyDefinitions(), 
				getTreatmentTypeContexts("default"), 
				DescriptionFactory.getInstitutes( Constants.CODE.GET), "20"));	
		
		DAOHelpers.saveModels(TreatmentType.class, l, errors);
	}
	

	private static List<TreatmentTypeContext> getTreatmentTypeContexts(String...codes) throws DAOException {
		List<TreatmentTypeContext> contexts = new ArrayList<TreatmentTypeContext>();
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
	
	
	
	
		
	private static List<PropertyDefinition> getNGSRGPropertyDefinitions() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
        //Run level
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Position flowcell","flowcellPosition", LevelService.getLevels(Level.CODE.Run, Level.CODE.Default), String.class, true, "single"));
        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nb cycles","nbCycle", LevelService.getLevels(Level.CODE.Run, Level.CODE.Default), Long.class, true, "single"));
        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Version flowcell","flowcellVersion", LevelService.getLevels(Level.CODE.Run, Level.CODE.Default), String.class, true, "single"));
        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nb clusters filt. (total)","nbClusterIlluminaFilter", LevelService.getLevels(Level.CODE.Run, Level.CODE.Lane, Level.CODE.Default), Long.class, true, "single"));
        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% clusters filtrés (Illumina)","percentClusterIlluminaFilter", LevelService.getLevels(Level.CODE.Run, Level.CODE.Lane, Level.CODE.Default), Double.class, false, "single"));        
        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nb bases (total)","nbBase", LevelService.getLevels(Level.CODE.Run, Level.CODE.Default), Long.class, true, "single"));
        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Mismatch autorisé pour le démultiplexage","mismatch", LevelService.getLevels(Level.CODE.Run, Level.CODE.Default), Boolean.class, true, "single"));
        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Piste contrôle","controlLane", LevelService.getLevels(Level.CODE.Run, Level.CODE.Default), Integer.class, true, "single"));
        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Version RTA","rtaVersion", LevelService.getLevels(Level.CODE.Run, Level.CODE.Default), String.class, true, "single"));
        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nb clusters (total)","nbClusterTotal", LevelService.getLevels(Level.CODE.Run, Level.CODE.Default), Long.class, true, "single"));
        //Lane & ReadSet level
        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nb clusters","nbCluster", LevelService.getLevels(Level.CODE.Lane, Level.CODE.ReadSet, Level.CODE.Default), Long.class, true, "single"));
        // Lane level
        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Prephasing","prephasing", LevelService.getLevels(Level.CODE.Lane, Level.CODE.Default), String.class, true, "single"));
        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% séquences filtrées (interne)","percentClusterInternalAndIlluminaFilter", LevelService.getLevels(Level.CODE.Lane, Level.CODE.Default), Double.class, true, "single"));
        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Phasing","phasing", LevelService.getLevels(Level.CODE.Lane, Level.CODE.Default), String.class, true, "single"));
        
        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nb bases (filtre Illumina + interne)","nbBaseInternalAndIlluminaFilter", LevelService.getLevels(Level.CODE.Lane, Level.CODE.Default), Long.class, true, "single"));
        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nb séquences (filtre Illumina + interne)","nbClusterInternalAndIlluminaFilter", LevelService.getLevels(Level.CODE.Lane, Level.CODE.Default), Long.class, true, "single"));
        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% Perte","seqLossPercent", LevelService.getLevels(Level.CODE.Lane, Level.CODE.Default), Double.class, false, "single"));
        // ReadSet level
        // nbCluster define in the lane level for the 2 levels
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% >= Q30","Q30", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Default), Double.class, true, "single"));
        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nb bases","nbBases", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Default), Long.class, true, "single"));
        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Fraction de run","fraction", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Default), Double.class, true, "single"));
        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Score qualité moyen","qualityScore", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Default), Double.class, true, "single"));
        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nb Read Illumina","nbReadIllumina", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Default), Integer.class, true, "single"));
        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% séquences valides/piste","validSeqPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Default), Double.class, false, "single"));
        
        /*TODO Pass to false in waiting of Fred development*/
        
        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nb cycles read index2","nbCycleReadIndex2", LevelService.getLevels(Level.CODE.Run, Level.CODE.Lane, Level.CODE.Default), Integer.class, false, "single"));
        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nb cycles read2","nbCycleRead2", LevelService.getLevels(Level.CODE.Run, Level.CODE.Lane, Level.CODE.Default), Integer.class, false, "single"));
        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nb cycles read1","nbCycleRead1", LevelService.getLevels(Level.CODE.Run, Level.CODE.Lane, Level.CODE.Default), Integer.class, false, "single"));
        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nb cycles read index1","nbCycleReadIndex1", LevelService.getLevels(Level.CODE.Run, Level.CODE.Lane, Level.CODE.Default), Integer.class, false, "single"));
        
        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nb cycles read index2 utiles Casava","nbUsefulCycleReadIndex2", LevelService.getLevels(Level.CODE.Lane, Level.CODE.ReadSet, Level.CODE.Default), Integer.class, false, "single"));
        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nb cycles read2 utiles","nbUsefulCycleRead2", LevelService.getLevels(Level.CODE.Lane, Level.CODE.ReadSet, Level.CODE.Default), Integer.class, false, "single"));
        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nb cycles read1 utiles","nbUsefulCycleRead1", LevelService.getLevels(Level.CODE.Lane, Level.CODE.ReadSet, Level.CODE.Default), Integer.class, false, "single"));
        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nb cycles read index1 utiles Casava","nbUsefulCycleReadIndex1", LevelService.getLevels(Level.CODE.Lane, Level.CODE.ReadSet, Level.CODE.Default), Integer.class, false, "single"));
        
	      
	    propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Trimming adaptateurs par ngs-rg","casavaAdapterTrimming", LevelService.getLevels(Level.CODE.Run, Level.CODE.Default), Boolean.class, false, "single"));
	    propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nb bases après trimming","nbBaseAfterTrim", LevelService.getLevels(Level.CODE.Run, Level.CODE.Lane, Level.CODE.ReadSet, Level.CODE.Default), Long.class, false, "single"));
	    propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% bases trimmées","percentBaseTrim", LevelService.getLevels(Level.CODE.Run, Level.CODE.Lane, Level.CODE.ReadSet, Level.CODE.Default), Double.class, false, "single"));
       
        return propertyDefinitions;
	}
	
	public static List<PropertyDefinition> getReadSetPropertyDefinitions() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
        // just readset level
        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Useful sequences","usefulSequences", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Default), Long.class, true, "single"));
        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Useful bases","usefulBases", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Default), Long.class, true, "single"));
        return propertyDefinitions;
	}
	
	
	private static List<PropertyDefinition> getSAVPropertyDefinitions() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Density","clusterDensity",LevelService.getLevels(Level.CODE.Lane, Level.CODE.Read1, Level.CODE.Read2), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("+/-","clusterDensityStd",LevelService.getLevels(Level.CODE.Lane, Level.CODE.Read1, Level.CODE.Read2), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% Cluster PF","clusterPFPerc",LevelService.getLevels(Level.CODE.Lane, Level.CODE.Read1, Level.CODE.Read2), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("+/-","clusterPFPercStd",LevelService.getLevels(Level.CODE.Lane, Level.CODE.Read1, Level.CODE.Read2), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Phasing","phasing",LevelService.getLevels(Level.CODE.Lane, Level.CODE.Read1, Level.CODE.Read2), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Prephasing","prephasing",LevelService.getLevels(Level.CODE.Lane, Level.CODE.Read1, Level.CODE.Read2), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Reads","reads",LevelService.getLevels(Level.CODE.Lane, Level.CODE.Read1, Level.CODE.Read2), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Reads PF","readsPF",LevelService.getLevels(Level.CODE.Lane, Level.CODE.Read1, Level.CODE.Read2), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("greaterQ30Perc","greaterQ30Perc",LevelService.getLevels(Level.CODE.Lane, Level.CODE.Read1, Level.CODE.Read2), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Cycles Err Rated","cyclesErrRated",LevelService.getLevels(Level.CODE.Lane, Level.CODE.Read1, Level.CODE.Read2), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% Aligned","alignedPerc",LevelService.getLevels(Level.CODE.Lane, Level.CODE.Read1, Level.CODE.Read2), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("+/-","alignedPercStd",LevelService.getLevels(Level.CODE.Lane, Level.CODE.Read1, Level.CODE.Read2), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Error Rate","errorRatePerc",LevelService.getLevels(Level.CODE.Lane, Level.CODE.Read1, Level.CODE.Read2), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("+/-","errorRatePercStd",LevelService.getLevels(Level.CODE.Lane, Level.CODE.Read1, Level.CODE.Read2), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Error Rate cycle 35","errorRatePercCycle35",LevelService.getLevels(Level.CODE.Lane, Level.CODE.Read1,Level.CODE.Read2), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("+/-","errorRatePercCycle35Std",LevelService.getLevels(Level.CODE.Lane, Level.CODE.Read1, Level.CODE.Read2), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Error Rate cycle 75","errorRatePercCycle75",LevelService.getLevels(Level.CODE.Lane, Level.CODE.Read1, Level.CODE.Read2), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("+/-","errorRatePercCycle75Std",LevelService.getLevels(Level.CODE.Lane, Level.CODE.Read1, Level.CODE.Read2), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Error Rate cycle 100","errorRatePercCycle100",LevelService.getLevels(Level.CODE.Lane, Level.CODE.Read1, Level.CODE.Read2), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("+/-","errorRatePercCycle100Std",LevelService.getLevels(Level.CODE.Lane, Level.CODE.Read1, Level.CODE.Read2), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Intensity cycle 1","intensityCycle1",LevelService.getLevels(Level.CODE.Lane, Level.CODE.Read1, Level.CODE.Read2), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("+/-","intensityCycle1Std",LevelService.getLevels(Level.CODE.Lane, Level.CODE.Read1,Level.CODE.Read2), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% Intensity cycle 20","intensityCycle20Perc",LevelService.getLevels(Level.CODE.Lane, Level.CODE.Read1, Level.CODE.Read2), Double.class, false, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("+/-","intensityCycle20PercStd",LevelService.getLevels(Level.CODE.Lane, Level.CODE.Read1, Level.CODE.Read2), Double.class, false, "single"));
		return propertyDefinitions;
	}
	
	public static List<PropertyDefinition> getReadQualityPropertyDefinitions() throws DAOException{
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Sample input","sampleInput",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Quality scores for each read position","qualScore",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Image.class, true, "img"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Distribution of nucleotids for each read position","nuclDistribution",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Image.class, true, "img"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Percent of reads with sequenced N","readWithNpercent",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Image.class, true, "img"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Read size distribution","readSizeDistribution",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Image.class, true, "img"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Contamination adaptateurs : liste d'adaptateurs vs cycles run","adapterContamination",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Image.class, true, "img"));		
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Adaptateurs sur-représentés","adapters",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), String.class, false, "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Proportion of reads for different values of G+C content","GCDistribution",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Image.class, false, "img"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Position of N in reads","positionN",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Image.class, true, "img"));				
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Suspected Kmers (Kmer)","suspectedKmers.Kmer",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), String.class, false, "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Suspected Kmers (Nb occurences)","suspectedKmers.nbOccurences",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Long.class, false, "object_list"));		
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Suspected primers","suspectedPrimers",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), String.class, false, "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Estimation du nb de lectures de taille","maxSizeReads",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Long.class, false, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Pourcentage de lectures de taille","maxSizeReadsPercent",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Double.class, false, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Taille maximale de lecture","maxSize",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Integer.class, false, "single"));
		
		//new properties to generate image
		/*  dnoisett, 10-07-14, to permit UAT test for the shared treatment QC, comment lines from 475 to 507  !!!!!!!!!!!!!!!!!!!! */
		if(	!ConfigFactory.load().getString("ngl.env").equals("PROD") ){
			propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("adapterContaminationDetails.adapterName","adapterContaminationDetails.adapterName",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), String.class, false, "object_list"));
			propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("adapterContaminationDetails.contaminationIntensities","adapterContaminationDetails.contaminationIntensities",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), List.class, false, "object_list"));
			
			propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("readWithNpercentDetails.numberOfN","readWithNpercentDetails.numberOfN",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Integer.class, false, "object_list"));
			propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("readWithNpercentDetails.percentOfReads","readWithNpercentDetails.percentOfReads",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Double.class, false, "object_list"));
			
			propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("positionNdetails.positionInReads","positionNdetails.positionInReads",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Integer.class, false, "object_list"));
			propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("positionNdetails.numberOfN","positionNdetails.numberOfN",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Integer.class, false, "object_list"));
			
			propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("readSizeDistributionDetails.readsLength","readSizeDistributionDetails.readsLength",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Integer.class, false, "object_list"));
			propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("readSizeDistributionDetails.percentOfReads","readSizeDistributionDetails.percentOfReads",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Double.class, false, "object_list"));
			
			propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("qualScoreDetails.position","qualScoreDetails.position",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Integer.class, false, "object_list"));
			propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("qualScoreDetails.minQualityScore","qualScoreDetails.minQualityScore",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Integer.class, false, "object_list"));
			propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("qualScoreDetails.maxQualityScore","qualScoreDetails.maxQualityScore",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Integer.class, false, "object_list"));
			propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("qualScoreDetails.meanQualityScore","qualScoreDetails.meanQualityScore",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Double.class, false, "object_list"));
			propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("qualScoreDetails.Q1","qualScoreDetails.Q1",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Integer.class, false, "object_list"));
			propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("qualScoreDetails.medianQualityScore","qualScoreDetails.medianQualityScore",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Double.class, false, "object_list"));
			propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("qualScoreDetails.Q3","qualScoreDetails.Q3",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Integer.class, false, "object_list"));
			propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("qualScoreDetails.lowerWhisker","qualScoreDetails.lowerWhisker",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Integer.class, false, "object_list"));
			propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("qualScoreDetails.upperWhisker","qualScoreDetails.upperWhisker",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Integer.class, false, "object_list"));
			
			propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("nuclDistributionDetails.readPosition","nuclDistributionDetails.readPosition",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Integer.class, false, "object_list"));
			propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("nuclDistributionDetails.APercent","nuclDistributionDetails.APercent",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Double.class, false, "object_list"));
			propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("nuclDistributionDetails.CPercent","nuclDistributionDetails.CPercent",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Double.class, false, "object_list"));
			propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("nuclDistributionDetails.GPercent","nuclDistributionDetails.GPercent",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Double.class, false, "object_list"));
			propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("nuclDistributionDetails.TPercent","nuclDistributionDetails.TPercent",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Double.class, false, "object_list"));
			propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("nuclDistributionDetails.NPercent","nuclDistributionDetails.NPercent",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Double.class, false, "object_list"));
	
			propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("GCDistributionDetails.percentGCcontent","GCDistributionDetails.percentGCcontent",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Double.class, false, "object_list"));
			propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("GCDistributionDetails.percentOfReads","GCDistributionDetails.percentOfReads",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Double.class, false, "object_list"));
		}
		return propertyDefinitions;		
	}
	
	public static List<PropertyDefinition> getDuplicatesPropertyDefinitions() throws DAOException{
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Sample input","sampleInput",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2, Level.CODE.Pairs), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("estimation lectures dupliquées","estimateDuplicatedReads",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2, Level.CODE.Pairs), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("estimation % lectures dupliquées","estimateDuplicatedReadsPercent",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("lectures dupliquées X+1 fois (nb)","estimateDuplicatedReadsNTimes.times",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2, Level.CODE.Pairs), Integer.class, true, "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("lectures dupliquées X+1 fois (%)","estimateDuplicatedReadsNTimes.percent",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2, Level.CODE.Pairs), Double.class, true, "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("estimation lectures uniques","estimateUniqueReads",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2, Level.CODE.Pairs), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("estimation % lectures uniques","estimateUniqueReadsPercent",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2, Level.CODE.Pairs), Double.class, true, "single"));
		return propertyDefinitions;		
	}
	
	public static List<PropertyDefinition> getTrimmingPropertyDefinitions() throws DAOException{
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Size range","sizeRange",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), String.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Reads input","readsInput",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Reads output","readsOutput",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Reads noTrim.","readsNoTrim",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Long.class, false, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Reads noTrimPercent","readsNoTrimPercent",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Double.class, false, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Reads trim.","readsTrim",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% Reads trim.","readsTrimPercent",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nucleotides trim.","nucleotidesTrim",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Rejected (short)","trimRejectedShort",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Rejected (length0)","trimRejectedLength0",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Trim. Stored","trimStored",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Long.class, false, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Stored pairs","storedPairs",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Rejected pairs","rejectedPairs",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Long.class, false, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("stored singleton","storedSingleton",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Single), Long.class, true, "single"));
		return propertyDefinitions;		
	}
	
	public static List<PropertyDefinition> getFirstBaseReportPropertyDefinitions() throws DAOException{
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Cluster Density (K/mm²)", "clusterDensityTop",LevelService.getLevels(Level.CODE.Lane, Level.CODE.Read1), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("A Intensity", "intensityATop",LevelService.getLevels(Level.CODE.Lane, Level.CODE.Read1), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("C Intensity", "intensityCTop",LevelService.getLevels(Level.CODE.Lane, Level.CODE.Read1), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("G Intensity", "intensityGTop",LevelService.getLevels(Level.CODE.Lane, Level.CODE.Read1), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("T Intensity", "intensityTTop",LevelService.getLevels(Level.CODE.Lane, Level.CODE.Read1), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("A Focus Score", "focusScoreATop",LevelService.getLevels(Level.CODE.Lane, Level.CODE.Read1), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("C Focus Score", "focusScoreCTop",LevelService.getLevels(Level.CODE.Lane, Level.CODE.Read1), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("G Focus Score", "focusScoreGTop",LevelService.getLevels(Level.CODE.Lane, Level.CODE.Read1), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("T Focus Score", "focusScoreTTop",LevelService.getLevels(Level.CODE.Lane, Level.CODE.Read1), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Cluster Density (K/mm²)", "clusterDensityBottom",LevelService.getLevels(Level.CODE.Lane, Level.CODE.Read1), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("A Intensity", "intensityABottom",LevelService.getLevels(Level.CODE.Lane, Level.CODE.Read1), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("C Intensity", "intensityCBottom",LevelService.getLevels(Level.CODE.Lane, Level.CODE.Read1), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("G Intensity", "intensityGBottom",LevelService.getLevels(Level.CODE.Lane, Level.CODE.Read1), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("T Intensity", "intensityTBottom",LevelService.getLevels(Level.CODE.Lane, Level.CODE.Read1), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("A Focus Score", "focusScoreABottom",LevelService.getLevels(Level.CODE.Lane, Level.CODE.Read1), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("C Focus Score", "focusScoreCBottom",LevelService.getLevels(Level.CODE.Lane, Level.CODE.Read1), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("G Focus Score", "focusScoreGBottom",LevelService.getLevels(Level.CODE.Lane, Level.CODE.Read1), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("T Focus Score", "focusScoreTBottom",LevelService.getLevels(Level.CODE.Lane, Level.CODE.Read1), Double.class, true, "single"));
		return propertyDefinitions;
	}
	
	public static List<PropertyDefinition> getContaminationPropertyDefinitions() throws DAOException{
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Reads input","readsInput",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Pairs, Level.CODE.Single), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Removed reads","removedReads",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Pairs, Level.CODE.Single), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Remaining reads","remainingReads",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Pairs, Level.CODE.Single), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% removed reads","removedReadsPercent",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Pairs, Level.CODE.Single), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Remaining nucleotides","remainingNucleotides",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Pairs), Long.class, false, "single"));
		return propertyDefinitions;		
	}
	
	public static List<PropertyDefinition> getTaxonomyPropertyDefinitions() throws DAOException{
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Sample input","sampleInput",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Organisme","organism",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), String.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Taxonomie","taxonomy",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), String.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Bilan par taxon (taxon)","taxonBilan.taxon",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), String.class, true, "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Bilan par taxon (nb seq)","taxonBilan.nbSeq",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), Long.class, true, "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Bilan par taxon (%)","taxonBilan.percent",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), Double.class, true, "object_list"));	
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Bilan par division (division)","divisionBilan.division",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), String.class, true,
				DescriptionFactory.newValues("eukaryota","bacteria","cellular organisms","archaea","viruses"), "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Bilan par division (nb seq)","divisionBilan.nbSeq",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), Long.class, true, "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Bilan par division (%)","divisionBilan.percent",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), Double.class, true, "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Bilan par mot-clé (mot-clé)","keywordBilan.keyword",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), String.class, false, 
				DescriptionFactory.newValues("mitochondri","virus","chloroplast","transposase",	"BAC"), "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Bilan par mot-clé (nb seq)","keywordBilan.nbSeq",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), Long.class, false, "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Bilan par mot-clé (%)","keywordBilan.percent",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), Double.class, false, "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("krona","krona",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), File.class, true, "file"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Arbre phylogénétique","phylogeneticTree",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), Image.class, false, "img"));
		return propertyDefinitions;		
	}
	
	public static List<PropertyDefinition> getSortingRiboPropertyDefinitions() throws DAOException{
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Reads input","readsInput",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2, Level.CODE.Single), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Non-rRNA","no_rRNA",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2, Level.CODE.Single), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("rRNA","rRNA",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2, Level.CODE.Single), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% rRNA","rRNAPercent",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2, Level.CODE.Single), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Bilan rRNA (type)","rRNABilan.type",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2, Level.CODE.Single), String.class, true,
				DescriptionFactory.newValues("PhiX", "Eukaryotic 18S", "Eukaryotic 28S", "Bacteria 16S", "Bacteria 23S", "Archeae 16S", "Archeae 23S", "Rfam 5.8S", "Rfam 5S"), "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Bilan rRNA (%)","rRNABilan.percent",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2, Level.CODE.Single), Double.class, true, "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Useful sequences","usefulSequences",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Useful bases","usefulBases",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Long.class, false, "single"));
		return propertyDefinitions;		
	}
	
	public static List<PropertyDefinition> getMappingPropertyDefinitions() throws DAOException{
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Sample input","sampleInput",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Non-chimeric aligned reads","nonChimericAlignedReads",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("FR (PE) aligned reads","FRAlignedReads",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("RF (MP) aligned reads","RFAlignedReads",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("FF aligned reads","FFAlignedReads",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("RR aligned reads","RRAlignedReads",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Trans aligned reads","transAlignedReads",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Single aligned reads","singleAlignedReads",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% non-chimeric aligned reads","nonChimericAlignedReadsPercent",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% FR (PE) aligned reads","FRAlignedReadsPercent",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% RF (MP) aligned reads","RFAlignedReadsPercent",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% FF aligned reads","FFAlignedReadsPercent",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% RR aligned reads","RRAlignedReadsPercent",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% Trans aligned reads","transAlignedReadsPercent",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% Single aligned reads","singleAlignedReadsPercent",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Distance between MP reads","MPReadDistanceSeparation",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Image.class, true, "img"));		
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("MP insert size estimated","estimatedMPInsertSize",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Integer.class, false, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Distance between PE reads","PEReadDistanceSeparation",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Image.class, true, "img"));	
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("PE insert size estimated","estimatedPEInsertSize",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Integer.class, false, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Reference","reference",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Default), String.class, true, "single"));
		//21-07-2014
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Errors position in reads","errorPosition",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Image.class, false, "img"));	
		
		return propertyDefinitions;		
	}
	
	public static List<PropertyDefinition> getMergingPropertyDefinitions() throws DAOException{
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
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
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
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
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();	
		//new, 09-07-14 : temporary set required=false TO PASS TO TRUE
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
		
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Percentage of assembled reads","readsAssembledPercent", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Double.class, true, "single"));
		
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% expected pool size", "expectedPoolSizePercent", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Double.class, true, "single"));
		
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Number of reads used for assembly", "readsUsed", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Number of bases used for assembly", "basesUsed", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Coverage used for assembly", "coverageUsed", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Number of reads really assembled", "readsAssembled", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Long.class, true, "single"));

		
		
		return propertyDefinitions;		
	}
	
	
	public static List<PropertyDefinition> getScaffoldingBAPropertyDefinitions() throws DAOException{
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();

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
	
	
	public static List<PropertyDefinition> getGapClosingBAPropertyDefinitions() throws DAOException{
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
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
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nb bases conservées","storedBases", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Integer.class, true, "single"));
		// temporary set to false (computed by NGL ?)
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% bases perdues","lostBasesPercent", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Double.class, false, "single"));
		return propertyDefinitions;		
	}
		
		
//	private List<PropertyDefinition> getMinknowMetrichorPropertyDefinitions() throws DAOException {
//		
//		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
//        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Version MinKNOW","minKnowVersion", LevelService.getLevels(Level.CODE.Run, Level.CODE.Default), String.class, false, "single"));
//        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Version Metrichor","metrichorVersion", LevelService.getLevels(Level.CODE.Run, Level.CODE.Default), String.class, false, "single"));
//        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Metrichor run ID","metrichorRunID", LevelService.getLevels(Level.CODE.Run, Level.CODE.Default), String.class, false, "single"));
//        
//        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Channels with Reads","minknowChannelsWithReads", LevelService.getLevels(Level.CODE.Run, Level.CODE.Default), Long.class, false, "single"));
//        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Events in Reads","minknowEvents", LevelService.getLevels(Level.CODE.Run, Level.CODE.Default), Long.class, false, "single"));
//        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Complete reads","minknowCompleteReads", LevelService.getLevels(Level.CODE.Run, Level.CODE.Default), Long.class, false, "single"));
//        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Read count","metrichorReadCount", LevelService.getLevels(Level.CODE.Run, Level.CODE.Default), Long.class, false, "single"));
//
//        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Total 2D yield (bases)","metrichor2DReadsYield", LevelService.getLevels(Level.CODE.Run, Level.CODE.Default), Long.class, false, "single"));
//        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Longest 2D read (bases)","metrichorMax2DRead", LevelService.getLevels(Level.CODE.Run, Level.CODE.Default), Long.class, false, "single"));        
//        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Peak 2D quality score","metrichorMax2DQualityScore", LevelService.getLevels(Level.CODE.Run, Level.CODE.Default), Double.class, false, "single"));
//
//		return propertyDefinitions;
//	}
	
	


}
