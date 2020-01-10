package services.description.run;

import java.awt.Image;
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

public class TreatmentServiceCNG extends AbstractTreatmentService {

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
				DescriptionFactory.getInstitutes(Constants.CODE.CNG), "10"));

		l.add(DescriptionFactory.newTreatmentType("NGSRG","ngsrg-illumina", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.ngsrg.name()), "ngsrg", 
				getNGSRGIlluminaPropertyDefinitions(), 
				getTreatmentTypeContexts("default"), 
				DescriptionFactory.getInstitutes(Constants.CODE.CNG), "20"));		

		l.add(DescriptionFactory.newTreatmentType("NGS-RG","ngsrg-nanopore", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.ngsrg.name()), "ngsrg", 
				getNGSRGNanoporePropertyDefinitions(), 
				getTreatmentTypeContexts("default"), 
				DescriptionFactory.getInstitutes( Constants.CODE.CNG), "20"));

		l.add(DescriptionFactory.newTreatmentType("Chromium","chromium", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.ngsrg.name()), "chromium", 
				getChromiumPropertyDefinitions(), 
				Arrays.asList(getTreatmentTypeContext("pairs",Boolean.TRUE)), 
				DescriptionFactory.getInstitutes(Constants.CODE.CNG), "24"));	

		l.add(DescriptionFactory.newTreatmentType("Top Index","topIndex", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.ngsrg.name()), "topIndex", 
				getTopIndexPropertyDefinitions(), 
				Arrays.asList(getTreatmentTypeContext("default",Boolean.TRUE)), 
				DescriptionFactory.getInstitutes(Constants.CODE.CNG), "25"));	

		l.add(DescriptionFactory.newTreatmentType("Global","global", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.global.name()), "global", 
				getReadSetPropertyDefinitions(), 
				getTreatmentTypeContexts("default"), 
				DescriptionFactory.getInstitutes(Constants.CODE.CNG), "0"));

		l.add(DescriptionFactory.newTreatmentType("Read Quality","read-quality", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.quality.name()), "readQualityRaw,readQualityClean", 
				getReadQualityPropertyDefinitions(), 
				Arrays.asList(getTreatmentTypeContext("read1",Boolean.TRUE), getTreatmentTypeContext("read2", Boolean.FALSE)), 
				DescriptionFactory.getInstitutes(Constants.CODE.CNG), "30,83"));

		l.add(DescriptionFactory.newTreatmentType("Read Quality","read-quality-nanopore", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.quality.name()), "readQuality", 
				getReadQualityNanoporePropertyDefinitions(), 
				getTreatmentTypeContexts("default"), 
				DescriptionFactory.getInstitutes( Constants.CODE.CNG), "30,83,84"));
		
		l.add(DescriptionFactory.newTreatmentType("Duplicates","duplicates", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.quality.name()), "duplicatesRaw,duplicatesClean", 
				getDuplicatesPropertyDefinitions(), 
				Arrays.asList(getTreatmentTypeContext("read1",Boolean.TRUE), getTreatmentTypeContext("read2", Boolean.FALSE), getTreatmentTypeContext("pairs", Boolean.FALSE)), 
				DescriptionFactory.getInstitutes(Constants.CODE.CNG), "32,86"));

		l.add(DescriptionFactory.newTreatmentType("Mapping","mapping", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.quality.name()), "mapping", 
				getMappingPropertyDefinitions(), 
				Arrays.asList(getTreatmentTypeContext("read1",Boolean.FALSE), getTreatmentTypeContext("read2", Boolean.FALSE), getTreatmentTypeContext("pairs", Boolean.FALSE), getTreatmentTypeContext("default", Boolean.TRUE)),
				//getTreatmentTypeContexts("pairs", "default"), 
				DescriptionFactory.getInstitutes(Constants.CODE.CNG), "90"));

		l.add(DescriptionFactory.newTreatmentType("Mapping Nanopore ","mapping-nanopore", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.quality.name()), "mapping", 
				getMappingNanoporePropertyDefinitions(), 
				getTreatmentTypeContexts("default"), 
				DescriptionFactory.getInstitutes( Constants.CODE.CNG), "90"));


		l.add(DescriptionFactory.newTreatmentType("Trimming","trimming", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.quality.name()), "trimmingStd,trimmingVector", 
				getTrimmingPropertyDefinitions(), 
				Arrays.asList(getTreatmentTypeContext("read1",Boolean.TRUE), getTreatmentTypeContext("read2", Boolean.FALSE), getTreatmentTypeContext("pairs", Boolean.FALSE), 
						getTreatmentTypeContext("single", Boolean.FALSE)), 
				DescriptionFactory.getInstitutes( Constants.CODE.CNG), "33,50"));

		l.add(DescriptionFactory.newTreatmentType("Trimming Nanopore","trimming-nanopore", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.quality.name()), "trimming", 
				getTrimmingNanoporePropertyDefinitions(), 
				getTreatmentTypeContexts("default"), 
				DescriptionFactory.getInstitutes(Constants.CODE.CNG), "34"));
		
		l.add(DescriptionFactory.newTreatmentType("First Base Report", "firstBaseReport", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.quality.name()), "firstBaseReport",
				getFirstBaseReportPropertyDefinitions(),
				Arrays.asList(getTreatmentTypeContext("read1", Boolean.TRUE)),
				DescriptionFactory.getInstitutes(Constants.CODE.CNG), "5")
				);

		l.add(DescriptionFactory.newTreatmentType("Contamination","contamination", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.quality.name()), "contaminationColi,contaminationVector,contaminationPhiX", 
				getContaminationPropertyDefinitions(), 
				Arrays.asList(getTreatmentTypeContext("read1",Boolean.FALSE), getTreatmentTypeContext("pairs", Boolean.FALSE), 
						getTreatmentTypeContext("single", Boolean.FALSE)), 
				DescriptionFactory.getInstitutes(Constants.CODE.CNG), "35,36,60"));	


		//Nanopore
		l.add(DescriptionFactory.newTreatmentType("MinKnow-Metrichor","minknow-metrichor", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.sequencing.name()), "minknowMetrichor", 
				getMinknowMetrichorPropertyDefinitions(), 
				getTreatmentTypeContexts("default"), 
				DescriptionFactory.getInstitutes( Constants.CODE.CNG), "20"));	

		l.add(DescriptionFactory.newTreatmentType("MinKnow-Basecalling","minknow-basecalling", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.sequencing.name()), "minknowBasecalling", 
				getMinknowBaseCallingPropertyDefinitions(), 
				getTreatmentTypeContexts("default"), 
				DescriptionFactory.getInstitutes( Constants.CODE.CNG), "20"));	

		//specific CNG 
		l.add(DescriptionFactory.newTreatmentType("alignSingleRead BLAT","alignsingleread-blat", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.quality.name()), "alignSingleReadBLATRaw", 
				getASRBPropertyDefinitions(), 
				getTreatmentTypeContexts("read1", "read2"), 
				DescriptionFactory.getInstitutes(Constants.CODE.CNG), "50"));

		l.add(DescriptionFactory.newTreatmentType("alignSingleRead SOAP2","alignsingleread-soap2", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.quality.name()), "alignSingleReadSOAP2Raw", 
				getASRSPropertyDefinitions(), 
				getTreatmentTypeContexts("read1", "read2"), 
				DescriptionFactory.getInstitutes(Constants.CODE.CNG), "60"));

		l.add(DescriptionFactory.newTreatmentType("Exome","exome", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.quality.name()), "exome", 
				getExomeTreatmentPropertyDefinitions(), 
				getTreatmentTypeContexts("pairs"), 
				DescriptionFactory.getInstitutes(Constants.CODE.CNG), "100"));

		/*l.add(DescriptionFactory.newTreatmentType("Whole Genome","whole-genome", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.quality.name()), "wholeGenome", 
				getWholeExomeTreatmentPropertyDefinitions(), 
				getTreatmentTypeContexts("pairs"), 
				DescriptionFactory.getInstitutes(Constants.CODE.CNG), "110"));*/

		l.add(DescriptionFactory.newTreatmentType("Whole Genome","whole-genome", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.quality.name()), "wholeGenome", 
				getWholeGenomePropertyDefinitions(), 
				getTreatmentTypeContexts("pairs"), 
				DescriptionFactory.getInstitutes(Constants.CODE.CNG), "110"));
		
		l.add(DescriptionFactory.newTreatmentType("RNAseq","rna-seq", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.quality.name()), "RNASeq", 
				getRnaSeqTreatmentPropertyDefinitions(), 
				getTreatmentTypeContexts("pairs"), 
				DescriptionFactory.getInstitutes(Constants.CODE.CNG), "120"));

		l.add(DescriptionFactory.newTreatmentType("ChiPseq","chip-seq", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.quality.name()), "ChiPseq", 
				getChiPSeqTreatmentPropertyDefinitions(), 
				getTreatmentTypeContexts("read1"), 
				DescriptionFactory.getInstitutes(Constants.CODE.CNG), "130"));

		l.add(DescriptionFactory.newTreatmentType("ChiPseq-PE","chipseq-pe", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.quality.name()), "ChiPseqPE", 
				getChiPSeqPETreatmentPropertyDefinitions(), 
				getTreatmentTypeContexts("pairs"), 
				DescriptionFactory.getInstitutes(Constants.CODE.CNG), "140"));

		l.add(DescriptionFactory.newTreatmentType("FAIREseq","faire-seq", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.quality.name()), "FAIREseq", 
				getFaireSeqTreatmentPropertyDefinitions(), 
				getTreatmentTypeContexts("read1"), 
				DescriptionFactory.getInstitutes(Constants.CODE.CNG), "150"));

		//TODO : level parameter must be verified
		l.add(DescriptionFactory.newTreatmentType("Sample Control","sample-control", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.quality.name()), "sampleControl", 
				getSampleControlTreatmentPropertyDefinitions(), 
				getTreatmentTypeContexts("pairs"), 
				DescriptionFactory.getInstitutes(Constants.CODE.CNG), "160"));

		l.add(DescriptionFactory.newTreatmentType("Gender", "gender", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.quality.name()), "Gender", 
				getGenderTreatmentPropertyDefinitions(), 
				getTreatmentTypeContexts("pairs"), 
				DescriptionFactory.getInstitutes(Constants.CODE.CNG), "170"));
		
		
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
	
	private static List<PropertyDefinition> getASRBPropertyDefinitions() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Sample input","sampleInput", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Number of mapped reads","mappedReads", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Percent of mapped reads","mappedReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Number of reads with 100% of matching bases","reads100pcMatchBases", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Percent of reads with 100% of matching bases","reads100pcMatchBasesPerc", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Number of reads with >=90% of matching bases","reads90pcMatchBases", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Percent of reads with >=90% of matching bases","reads90pcMatchBasesPerc", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Number of aligned bases","alignedBases", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Number of errors","nbErrors", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Number of insertions","nbInsertions", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Number of deletions","nbDeletions", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Number of mismatches","nbMismatches", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Number of perfect reads","nbPerfectReads", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Reference genome coverage","coverageDistribution", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Image.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Errors position in reads","errorPosition", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Image.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("observed quality values in reads","qualityValueDistribution", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Image.class, true, "single"));
		return propertyDefinitions;
	}

	private static List<PropertyDefinition> getASRSPropertyDefinitions() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Sample input","sampleInput", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Number of mapped reads","mappedReads", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Percent of mapped reads","mappedReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Number of aligned bases","alignedBases", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Number of mismatches","nbMismatches", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Percent of mismatches","mismatchesPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Errors position in reads (SOAP 2)","errorPosition", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Image.class, true, "single"));
		return propertyDefinitions;
	}

	private static List<PropertyDefinition> getExomeTreatmentPropertyDefinitions() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nb reads","nbReads", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Mapped reads","mappedReads", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Percent Mapped reads","mappedReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Percent Pairing","pairingPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Duplicated reads","duplicatedReads", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Percent Duplicated Reads","duplicatedReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Size Regions Tiled","sizeRegionsTiled", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Percent target","targetPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Percent NT 30X","percentNT30X", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Percent NT 20X","percentNT20X", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Percent NT 10X","percentNT10X", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Percent NT 5X","percentNT5X", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nb regions 0X","nbRegions0X", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Percent regions 0X","regions0XPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nb regions full cover","nbRegionsFullCover", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Percent Regions Full Cover","regionsFullCoverPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Mean cover","meanCover", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Median cover","medianCover", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("SD cover","sdCover", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		return propertyDefinitions;
	}

//	private static List<PropertyDefinition> getWholeExomeTreatmentPropertyDefinitions() throws DAOException {
//		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
//		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nb reads","nbReads", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Long.class, true, "single"));
//		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Mapped reads","mappedReads", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Long.class, true, "single"));
//		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Percent Mapped reads","mappedReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
//		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Percent Pairing","pairingPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
//		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Duplicated reads","duplicatedReads", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Long.class, true, "single"));
//		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Percent Duplicated Reads","duplicatedReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
//		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Percent NT 30X","percentNT30X", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
//		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Percent NT 20X","percentNT20X", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
//		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Percent NT 10X","percentNT10X", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
//		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Percent NT 5X","percentNT5X", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
//		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nb regions 0X","nbRegions0X", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Integer.class, true, "single"));
//		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Percent regions 0X","regions0XPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
//		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Mean cover","meanCover", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
//		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Median cover","medianCover", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Integer.class, true, "single"));
//		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("SD cover","sdCover", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
//		return propertyDefinitions;
//	}

	private static List<PropertyDefinition> getRnaSeqTreatmentPropertyDefinitions() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		//echantillonnage
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nb reads","sampleInput", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Integer.class, true, "single"));
		//Nettoyage
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% both surviving","storedPairsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% R1 only surviving","storedForwardReadPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% R2 only surviving","storedReverseReadPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% dropped","rejectedPairsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		//alignement genome
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% mapped reads","alignedReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% unique mapped reads","uniqueReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% unique mapped reads of total","uniqueAlignedReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% base mismatch","mismatchPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% duplicated mapped reads","duplicatedReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Mean insert length","insertLengthMean", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("SD insert length","insertLengthMeanSd", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% R1 sense","1SenseAlignedReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% R2 sense","2SenseAlignedReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		//propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% rRNA","rRNAPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% intragenic","intragenicReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% exonic","exonicReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% intergenic","intergenicReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% intronic","intronicReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% mapped End 2","mappedEnd2ReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% mapped End 1","mappedEnd1ReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nb transcripts","nbTranscripts", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nb genes","nbGenes", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Integer.class, true, "single"));
		//annotation
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nb protein coding genes","nbProteinCodingGenes", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nb protein coding transcripts","nbProteinCodingTranscripts", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nb miRNA genes","nbMiRnaGenes", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nb snoRNA genes","nbSnoRnaGenes", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nb snRNA genes","nbSnRnaGenes", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nb protein coding genes chrY","nbProtCodGeneChrY", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nb protein coding transcripts chrY","nbProtCodTrscriptChrY", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nb lincRNA genes","nbLncRNAGenes", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nb lincRNA transcripts","nbLncRNATranscripts", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Integer.class, true, "single"));
		//alignement transcriptome
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% mapped reads","mappedReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("mean read length","readLengthMean", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% adaptateur","adaptersPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% mapped rRNA","rRnaMappedPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		//Duplication
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% duplicated mapped reads transcriptome","transcriptDuplicMappedReadsPerc", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% duplicated mapped reads genome","genomDuplicMappedReadsPerc", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		//Insert size
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("median insert size","insertsizeMedian", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("min insert size","insertsizeMin", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("max insert size","insertsizeMax", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("mean insert size","insertsizeMean", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("standard deviation","standardDeviation", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("pair orientation","pairOrientation", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), String.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("width of 10 percent","widthOf10percent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("width of 20 percent","widthOf20percent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("width of 30 percent","widthOf30percent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("width of 40 percent","widthOf40percent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("width of 50 percent","widthOf50percent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("width of 60 percent","widthOf60percent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("width of 70 percent","widthOf70percent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("width of 80 percent","widthOf80percent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("width of 90 percent","widthOf90percent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("width of 99 percent","widthOf99percent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Integer.class, true, "single"));

		return propertyDefinitions;
	}


	private static List<PropertyDefinition> getChiPSeqTreatmentPropertyDefinitions() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nb reads","sampleInput", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% both surviving","storedReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% dropped","rejectedReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% mapped reads","alignedReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% unique mapped reads","uniqueReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% unique mapped reads of total","uniqueAlignedReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% base mismatch","mismatchPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% duplicated mapped reads","duplicatedReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Mean insert length","insertLengthMean", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("SD insert length","insertLengthMeanSd", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% rRNA","rRNAPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), Double.class, true, "single"));
		return propertyDefinitions;
	}

	private static List<PropertyDefinition> getChiPSeqPETreatmentPropertyDefinitions() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nb reads","sampleInput", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% both surviving","storedReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% R1 only surviving", "storedForwardReadPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% R2 only surviving", "storedReverseReadPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% dropped", "rejectedReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% mapped reads", "alignedReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% unique mapped reads", "uniqueReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% unique mapped reads of total", "uniqueAlignedReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% base mismatch", "mismatchPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% duplicated mapped reads", "duplicatedReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Mean insert length", "insertLengthMean", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("SD insert length", "insertLengthMeanSd", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% R1 sense", "1SenseAlignedReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% R2 sense", "2SenseAlignedReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% rRNA","rRNAPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% intragenic", "intragenicReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% exonic", "exonicReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		return propertyDefinitions;
	}

	private static List<PropertyDefinition> getFaireSeqTreatmentPropertyDefinitions() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nb reads","sampleInput", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% both surviving","storedReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% dropped","rejectedReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Mapping Rate","alignedReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Unique Rate of mapped","uniqueReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Mapped Unique Rate of Total","uniqueAlignedReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Base Mismatch Rate","mismatchPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Duplication Rate of Mapped","duplicatedReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Insert Length Mean","insertLengthMean", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Standard Deviation","insertLengthMeanSd", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("rRNA rate","rRNAPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), Double.class, true, "single"));
		return propertyDefinitions;
	}

	private static List<PropertyDefinition> getSampleControlTreatmentPropertyDefinitions() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		//TODO : Level "pairs" must be confirmed 
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Sample input","sampleInput", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Sample sexe","sampleSexe", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), String.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Samples comparison","samplesComparison", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		return propertyDefinitions;
	}

	private static List<PropertyDefinition> getGenderTreatmentPropertyDefinitions(){
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Gender","gender", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Integer.class, true, DescriptionFactory.newValues("0", "1", "2"), "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Unaligned","unaligned", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("MQ0-PP","mq0PP", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("MQ1:59-PP","mq159PP", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("MQ60-PP","mq60PP", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("MQ0-NPP","mq0NPP", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("MQ1:59-NPP","mq159NPP", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("MQ60-NPP","mq60NPP", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Double.class, true, "single"));
		return propertyDefinitions;
	}

	private static List<PropertyDefinition> getWholeGenomePropertyDefinitions() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
			
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Sex","gender", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), String.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("TailleCouvGenome","coveredGenomeSize", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Couverture%","coveragePercent", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("n50","n50", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Taille+gdContig","largestContigSize", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("CouvMoy","coverageMean", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("CouvMed","coverageMedian", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("CouvQ1","q1Coverage", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("IQR","iqr", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("%Outlier","outlierPercent", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("%Region5","cov5xRegionPercent", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("%Region10","cov10xRegionPercent", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("%Region20","cov20xRegionPercent", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("%Region30","cov30xRegionPercent", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("%Region40","cov40xRegionPercent", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("PathFichier","filePath", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), String.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Chr01NoCouv","chr01NoCovered", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Chr02NoCouv","chr02NoCovered", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Chr03NoCouv","chr03NoCovered", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Chr04NoCouv","chr04NoCovered", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Chr05NoCouv","chr05NoCovered", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Chr06NoCouv","chr06NoCovered", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Chr07NoCouv","chr07NoCovered", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Chr08NoCouv","chr08NoCovered", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Chr09NoCouv","chr09NoCovered", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Chr10NoCouv","chr10NoCovered", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Chr11NoCouv","chr11NoCovered", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Chr12NoCouv","chr12NoCovered", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Chr13NoCouv","chr13NoCovered", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Chr14NoCouv","chr14NoCovered", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Chr15NoCouv","chr15NoCovered", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Chr16NoCouv","chr16NoCovered", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Chr17NoCouv","chr17NoCovered", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Chr18NoCouv","chr18NoCovered", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Chr19NoCouv","chr19NoCovered", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Chr20NoCouv","chr20NoCovered", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Chr21NoCouv","chr21NoCovered", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Chr22NoCouv","chr22NoCovered", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("ChrYNoCouv","chrYNoCovered", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("ChrXNoCouv","chrXNoCovered", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Double.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Readsets","fastqFiles", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), String.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Version Genome","humanReferenceGenome", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), String.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Version Pipeline","pipelineVersion", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), String.class, true, "single"));
		
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% Duplicats","duplicatedReadsPercent", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Double.class, false, "single"));
		
		return propertyDefinitions;
	}
	
	

}
