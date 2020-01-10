package services.description.run;

import static services.description.DescriptionFactory.newPropertiesDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import models.laboratory.common.description.Level;
import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.common.description.Value;
import models.laboratory.run.description.AnalysisType;
import models.laboratory.run.description.ReadSetType;
import models.laboratory.run.description.RunCategory;
import models.laboratory.run.description.RunType;
import models.utils.dao.DAOException;
import models.utils.dao.DAOHelpers;
import play.data.validation.ValidationError;
import services.description.Constants;
import services.description.DescriptionFactory;
import services.description.common.LevelService;

public class RunServiceCNG  extends AbstractRunService {
	
	// FDS 06/04/2017 NGL-1225: ajout rsnanopore
	@Override
	public void saveReadSetType(Map<String, List<ValidationError>> errors) throws DAOException {
		List<ReadSetType> l = new ArrayList<>();
		l.add(DescriptionFactory.newReadSetType("Readset Illumina","rsillumina",  getReadSetPropertyDefinitions(),  DescriptionFactory.getInstitutes( Constants.CODE.CNG) ));
		l.add(DescriptionFactory.newReadSetType("Readset Nanopore","rsnanopore", getReadSetPropertyDefinitionsNanopore(),  DescriptionFactory.getInstitutes( Constants.CODE.CNG) ));
		
		DAOHelpers.saveModels(ReadSetType.class, l, errors);
	}
	
	@Override
	public void saveAnalysisType(Map<String, List<ValidationError>> errors) throws DAOException {
		List<AnalysisType> l = new ArrayList<>();	
		l.add(DescriptionFactory.newAnalysisType("Whole genome analysis","WG-analysis",  null,  DescriptionFactory.getInstitutes(Constants.CODE.CNG) ));
		DAOHelpers.saveModels(AnalysisType.class, l, errors);
	}
	
	// FDS 06/04/2017 NGL-1225: ajout nanopore
	@Override
	public void saveRunCategories(Map<String, List<ValidationError>> errors) throws DAOException {
		List<RunCategory> l = new ArrayList<>();
		l.add(DescriptionFactory.newSimpleCategory(RunCategory.class, "Illumina", "illumina"));
		l.add(DescriptionFactory.newSimpleCategory(RunCategory.class, "Nanopore", "nanopore"));
		
		DAOHelpers.saveModels(RunCategory.class, l, errors);
	}
	
	// FDS 06/04/2017 NGL-1225: ajout "nanopore"
	// FDS 11/12/2017 NGL-1730: ajout "Novaseq 6000"; renommer les runtypes Illumina pour etre homogène avec CNS 
	@Override
	public void saveRunType(Map<String, List<ValidationError>> errors) throws DAOException {
		List<RunType> l = new ArrayList<>();
		l.add(DescriptionFactory.newRunType("HiSeq 2000","RHS2000", 8, RunCategory.find.findByCode("illumina"), getRunIlluminaPropertyDefinitions(),  DescriptionFactory.getInstitutes(Constants.CODE.CNG) ));
		l.add(DescriptionFactory.newRunType("HiSeq 2500","RHS2500", 8, RunCategory.find.findByCode("illumina"), getRunIlluminaPropertyDefinitions(),  DescriptionFactory.getInstitutes(Constants.CODE.CNG) ));
		l.add(DescriptionFactory.newRunType("HiSeq 2500 rapide","RHS2500R", 2, RunCategory.find.findByCode("illumina"), getRunIlluminaPropertyDefinitions(),   DescriptionFactory.getInstitutes(Constants.CODE.CNG)));
		l.add(DescriptionFactory.newRunType("MiSeq","RMISEQ", 1, RunCategory.find.findByCode("illumina"), getRunIlluminaPropertyDefinitions(),   DescriptionFactory.getInstitutes(Constants.CODE.CNG)));
		l.add(DescriptionFactory.newRunType("NextSeq 500","RNEXTSEQ500", 4, RunCategory.find.findByCode("illumina"), getRunIlluminaPropertyDefinitions(),   DescriptionFactory.getInstitutes(Constants.CODE.CNG)));
		l.add(DescriptionFactory.newRunType("HiSeq 4000","RHS4000", 1, RunCategory.find.findByCode("illumina"), getRunIlluminaPropertyDefinitions(), DescriptionFactory.getInstitutes(Constants.CODE.CNG)));
		l.add(DescriptionFactory.newRunType("HiSeq X","RHSX", 1, RunCategory.find.findByCode("illumina"), getRunIlluminaPropertyDefinitions(), DescriptionFactory.getInstitutes(Constants.CODE.CNG)));
		l.add(DescriptionFactory.newRunType("NovaSeq 6000","RNVS6000", 2, RunCategory.find.findByCode("illumina"), getRunIlluminaPropertyDefinitions(), DescriptionFactory.getInstitutes(Constants.CODE.CNG)));
		l.add(DescriptionFactory.newRunType("MinIon","RMINION", 1, RunCategory.find.findByCode("nanopore"), getRunNanoporePropertyDefinitions(), DescriptionFactory.getInstitutes(Constants.CODE.CNG)));
		l.add(DescriptionFactory.newRunType("MKI","RMKI",       1, RunCategory.find.findByCode("nanopore"), getRunNanoporePropertyDefinitions(), DescriptionFactory.getInstitutes(Constants.CODE.CNG)));
		l.add(DescriptionFactory.newRunType("MKIb","RMKIB",     1, RunCategory.find.findByCode("nanopore"), getRunNanoporePropertyDefinitions(), DescriptionFactory.getInstitutes(Constants.CODE.CNG)));

		DAOHelpers.saveModels(RunType.class, l, errors);
	}
	
	private static List<PropertyDefinition> getReadSetPropertyDefinitions() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("asciiEncoding","asciiEncoding",LevelService.getLevels(Level.CODE.File), String.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("label","label",LevelService.getLevels(Level.CODE.File), String.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("md5","md5",LevelService.getLevels(Level.CODE.File), String.class, false, "single"));
		
		//propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("isSentCCRT","isSentCCRT",LevelService.getLevels(Level.CODE.ReadSet), Boolean.class, false, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("isSentCollaborator","isSentCollaborator",LevelService.getLevels(Level.CODE.ReadSet), Boolean.class, false, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("localDataDeleted","localDataDeleted",LevelService.getLevels(Level.CODE.ReadSet), Boolean.class, false, "single"));
		
		//use only for dynamic filters and dynamic properties
		// !! Look also in ImportServiceCNG.java for libProcessTypeCode !!!
		/*
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Type processus banque","libProcessTypeCode",LevelService.getLevels(Level.CODE.Content), String.class, false,
				getLibProcessTypeCodeValues(), "single"));
		*/
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Category d'index","tagCategory",LevelService.getLevels(Level.CODE.Content), String.class, false, getTagCategories(), "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% par piste","percentPerLane",LevelService.getLevels(Level.CODE.Content), Double.class, false, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Layout Nominal Length","libLayoutNominalLength",LevelService.getLevels(Level.CODE.Content), Integer.class, false, "single"));
		
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Objectif taille insert","insertSizeGoal",LevelService.getLevels(Level.CODE.Content), String.class, false, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Orientation brin synthétisé","strandOrientation",LevelService.getLevels(Level.CODE.Content), String.class, false, "single"));
		
		//GA 21/07/2015 ajouter la propriété sampleAliquoteCode au readset, niveau content n'est pas idéal mais résoud le pb actuel (JIRA 672)
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Code aliquot","sampleAliquoteCode",LevelService.getLevels(Level.CODE.Content), String.class, false, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Tag", "tag", LevelService.getLevels(Level.CODE.Content), String.class, false, "single"));
		
		return propertyDefinitions;
	}
		
	// GA 24/07/2015 ajout des TagCategories
	// FDS 01/03/2017 ajout POOL-INDEX.... existe aussi dans AbstractExperimentService.java et ImportServiceCNG.java !!!!
	private static List<Value> getTagCategories(){
		List<Value> values = new ArrayList<>();
		values.add(DescriptionFactory.newValue("SINGLE-INDEX", "SINGLE-INDEX"));
		values.add(DescriptionFactory.newValue("DUAL-INDEX", "DUAL-INDEX"));
		values.add(DescriptionFactory.newValue("MID", "MID"));
		values.add(DescriptionFactory.newValue("POOL-INDEX", "POOL-INDEX"));
		return values;	
	}
	
	// 12/10/2017: liste pour les run Illumina !!! il y a une autre liste pour Nanopore
	//             PAS PRIS EN COMPTE POUR LES FILTRES...
	private static List<Value> getLibProcessTypeCodeValues(){
        List<Value> values = new ArrayList<>();
        
         // codes for Captures
         // 05/09/2017 mise en coherence avec ProcessServiceCNG getCaptureLibProcessTypeCodeValues(): si meme code => meme label 
         // ajout (DefCapxxx) tant que NGL-1569 pas resolu (stocker le path des fichier definition capture)
         values.add(DescriptionFactory.newValue("CA","CA - DefCap008_Rg"));
         values.add(DescriptionFactory.newValue("CB","CB - DefCap005_Ex"));
         values.add(DescriptionFactory.newValue("CC","CC - DefCap006_Ex"));
         values.add(DescriptionFactory.newValue("CD","CD - DefCap004_Rg"));
         values.add(DescriptionFactory.newValue("CE","CE - DefCap003_Ex"));
         values.add(DescriptionFactory.newValue("CF","CF - DefCap002_Ex"));
         values.add(DescriptionFactory.newValue("CG","CG - DefCap001_Ex"));
         values.add(DescriptionFactory.newValue("CH","CH - DefCap009_Ex"));
         values.add(DescriptionFactory.newValue("CI","CI - DefCap010_Ex"));
         values.add(DescriptionFactory.newValue("CJ","CJ - DefCap011_Ex"));
         values.add(DescriptionFactory.newValue("CK","CK - DefCap007_Ex"));
         values.add(DescriptionFactory.newValue("CL","CL - DefCapLUPA"));
         values.add(DescriptionFactory.newValue("CM","CM - DefCap012_Rg"));
         values.add(DescriptionFactory.newValue("CN","CN - DefCapINRA1_Rg"));
         values.add(DescriptionFactory.newValue("CO","CO - DefCapCAPSEQAN"));
         values.add(DescriptionFactory.newValue("CP","CP - Agilent : V5 (DefCap013_Ex)")); // !! aussi dans ProcessServiceCNG / getCaptureLibProcessTypeCodeValues
         values.add(DescriptionFactory.newValue("CQ","CQ - DefCap014_Rg"));
         values.add(DescriptionFactory.newValue("CR","CR - DefCap015_Ex"));
         values.add(DescriptionFactory.newValue("CS","CS - Agilent : V5+UTR (DefCap016_Ex)")); // !! aussi dans ProcessServiceCNG / getCaptureLibProcessTypeCodeValues
         values.add(DescriptionFactory.newValue("CT","CT - CapNimGenV3_017_Ex"));
         values.add(DescriptionFactory.newValue("CV","CV - DefCap018_Ex"));
         values.add(DescriptionFactory.newValue("CW","CW - DefCap019_Rg"));
         values.add(DescriptionFactory.newValue("CX","CX - DefCap020_Ex"));
         values.add(DescriptionFactory.newValue("CY","CY - DefCap021"));
         values.add(DescriptionFactory.newValue("CZ","CZ - Agilent : V6 (DefCap022)")); // !! aussi dans ProcessServiceCNG / getCaptureLibProcessTypeCodeValues
         values.add(DescriptionFactory.newValue("CAA","CAA - Agilent : V6+UTR (DefCap023)")); // !! aussi dans ProcessServiceCNG / getCaptureLibProcessTypeCodeValues
         values.add(DescriptionFactory.newValue("CAB","CAB - DefCap024"));
         values.add(DescriptionFactory.newValue("CAC","CAC - Agilent : V6+Cosmic (DefCap025)")); // !! aussi dans ProcessServiceCNG / getCaptureLibProcessTypeCodeValues
         values.add(DescriptionFactory.newValue("CAD","CAD - Nimblegen : MedExome (DefCap026)"));
         values.add(DescriptionFactory.newValue("CAE","CAE - Nimblegen : MedExome+Mitome (DefCap027)"));
         values.add(DescriptionFactory.newValue("CAF","CAF - Chromium Whole Exome (DefCap028)"));  // NGL-1584 ajout
         values.add(DescriptionFactory.newValue("CAG","CAG - SureSelectXTcustom(PRME) (DefCap029)"));  // NGL-2040 ajout
         
         // codes for DNA sequencing
         values.add(DescriptionFactory.newValue("DA","DA - DNASeq"));
         values.add(DescriptionFactory.newValue("DB","DB - MatePairSeq"));
         values.add(DescriptionFactory.newValue("DC","DC - Dnase-ISeq"));
         values.add(DescriptionFactory.newValue("DD","DD - PCR-NANO DNASeq")); // !! aussi dans ProcessServiceCNG / getX5WgNanoLibProcessTypeCodeValues()
         values.add(DescriptionFactory.newValue("DE","DE - Chromium WG"));     // !! aussi dans ProcessServiceCNG / getWgChromiumLibProcessTypeCodeValues()
         values.add(DescriptionFactory.newValue("DF","DF - Ancient DNASeq"));  // ajout 22/11/2017 NGL-1712
         values.add(DescriptionFactory.newValue("DG","DG - cfDNASeq"));  // NGL-1981 ajout
         
         // codes for various sequencing
         values.add(DescriptionFactory.newValue("FA","FA - MeDipSeq"));
         values.add(DescriptionFactory.newValue("FB","FB - ChipSeq"));
         values.add(DescriptionFactory.newValue("FC","FC - MeDipSeq/Depl"));
         values.add(DescriptionFactory.newValue("FD","FD - BisSeq"));
         values.add(DescriptionFactory.newValue("FE","FE - FAIRESeq"));
         values.add(DescriptionFactory.newValue("FF","FF - MBDSeq"));
         values.add(DescriptionFactory.newValue("FG","FG - GROSeq"));
         values.add(DescriptionFactory.newValue("FH","FH - oxBisSeq"));
         values.add(DescriptionFactory.newValue("FI","FI - ATACSeq"));
         values.add(DescriptionFactory.newValue("FJ","FJ - RRBSeq")); // SUPSQCNG-497: ajout 06/11/2017 car manquant
         values.add(DescriptionFactory.newValue("FK","FK - QMPSeq ")); // NGL-2039 ajout
         values.add(DescriptionFactory.newValue("HIC","HIC - HiC"));
         
         // codes for RNA sequencing
         values.add(DescriptionFactory.newValue("RA","RA - RNASeq"));
         values.add(DescriptionFactory.newValue("RB","RB - smallRNASeq"));
         values.add(DescriptionFactory.newValue("RC","RC - ssRNASeq"));
         values.add(DescriptionFactory.newValue("RD","RD - ssmRNASeq"));        // !! aussi dans ProcessServiceCNG / getRNALibProcessTypeCodeValues()
         values.add(DescriptionFactory.newValue("RE","RE - sstRNASeq"));        // !! aussi dans ProcessServiceCNG / getRNALibProcessTypeCodeValues()
         values.add(DescriptionFactory.newValue("RF","RF - sstRNASeqGlobin"));  // !! aussi dans ProcessServiceCNG / getRNALibProcessTypeCodeValues()
         values.add(DescriptionFactory.newValue("RG","RG - mRNASeq"));          // !! aussi dans ProcessServiceCNG / getRNALibProcessTypeCodeValues()
         values.add(DescriptionFactory.newValue("RH","RH - sstRNASeqGold"));    // !! aussi dans ProcessServiceCNG / getRNALibProcessTypeCodeValues()
         
         //
         values.add(DescriptionFactory.newValue("UN","UN - UKNOWN"));
        return values;
    } 
		
	private static List<PropertyDefinition> getRunIlluminaPropertyDefinitions() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(newPropertiesDefinition("Type lectures","sequencingProgramType"
	        		, LevelService.getLevels(Level.CODE.Run),String.class, false, DescriptionFactory.newValues("SR","PE"),"single"));
		
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Types processus banque","libProcessTypeCodes",LevelService.getLevels(Level.CODE.Run), String.class, false,
				getLibProcessTypeCodeValues(), "list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Codes aliquots","sampleAliquoteCodes",LevelService.getLevels(Level.CODE.Run), String.class, false,"list"));
	    return propertyDefinitions;
	}

}
