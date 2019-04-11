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
import models.laboratory.run.description.dao.RunCategoryDAO;
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
//		l.add(DescriptionFactory.newSimpleCategory(RunCategory.class, "Illumina", "illumina"));
//		l.add(DescriptionFactory.newSimpleCategory(RunCategory.class, "Nanopore", "nanopore"));
		l.add(new RunCategory("illumina",  "Illumina"));
		l.add(new RunCategory( "nanopore", "Nanopore"));		
		DAOHelpers.saveModels(RunCategory.class, l, errors);
	}
	
	// FDS 06/04/2017 NGL-1225: ajout "nanopore"
	// FDS 11/12/2017 NGL-1730: ajout "Novaseq 6000"; renommer les runtypes Illumina pour etre homogène avec CNS 
	@Override
	public void saveRunType(Map<String, List<ValidationError>> errors) throws DAOException {
		List<RunType> l = new ArrayList<>();
		RunCategoryDAO rcfind = RunCategory.find.get();
		l.add(DescriptionFactory.newRunType("HiSeq 2000",       "RHS2000",     8, rcfind.findByCode("illumina"), getRunIlluminaPropertyDefinitions(),  DescriptionFactory.getInstitutes(Constants.CODE.CNG) ));
		l.add(DescriptionFactory.newRunType("HiSeq 2500",       "RHS2500",     8, rcfind.findByCode("illumina"), getRunIlluminaPropertyDefinitions(),  DescriptionFactory.getInstitutes(Constants.CODE.CNG) ));
		l.add(DescriptionFactory.newRunType("HiSeq 2500 rapide","RHS2500R",    2, rcfind.findByCode("illumina"), getRunIlluminaPropertyDefinitions(),   DescriptionFactory.getInstitutes(Constants.CODE.CNG)));
		l.add(DescriptionFactory.newRunType("MiSeq",            "RMISEQ",      1, rcfind.findByCode("illumina"), getRunIlluminaPropertyDefinitions(),   DescriptionFactory.getInstitutes(Constants.CODE.CNG)));
		l.add(DescriptionFactory.newRunType("NextSeq 500",      "RNEXTSEQ500", 4, rcfind.findByCode("illumina"), getRunIlluminaPropertyDefinitions(),   DescriptionFactory.getInstitutes(Constants.CODE.CNG)));
		l.add(DescriptionFactory.newRunType("HiSeq 4000",       "RHS4000",     1, rcfind.findByCode("illumina"), getRunIlluminaPropertyDefinitions(), DescriptionFactory.getInstitutes(Constants.CODE.CNG)));
		l.add(DescriptionFactory.newRunType("HiSeq X",          "RHSX",        1, rcfind.findByCode("illumina"), getRunIlluminaPropertyDefinitions(), DescriptionFactory.getInstitutes(Constants.CODE.CNG)));
		l.add(DescriptionFactory.newRunType("NovaSeq 6000",     "RNVS6000",    2, rcfind.findByCode("illumina"), getRunIlluminaPropertyDefinitions(), DescriptionFactory.getInstitutes(Constants.CODE.CNG)));
		l.add(DescriptionFactory.newRunType("MinIon",           "RMINION",     1, rcfind.findByCode("nanopore"), getRunNanoporePropertyDefinitions(), DescriptionFactory.getInstitutes(Constants.CODE.CNG)));
		l.add(DescriptionFactory.newRunType("MKI",              "RMKI",        1, rcfind.findByCode("nanopore"), getRunNanoporePropertyDefinitions(), DescriptionFactory.getInstitutes(Constants.CODE.CNG)));
		l.add(DescriptionFactory.newRunType("MKIb",             "RMKIB",       1, rcfind.findByCode("nanopore"), getRunNanoporePropertyDefinitions(), DescriptionFactory.getInstitutes(Constants.CODE.CNG)));
		l.add(DescriptionFactory.newRunType("PromethION ",      "RPROMETHION", 1, rcfind.findByCode("nanopore"), getRunNanoporePropertyDefinitions(), DescriptionFactory.getInstitutes(Constants.CODE.CNG)));
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

	private static List<PropertyDefinition> getRunIlluminaPropertyDefinitions() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(newPropertiesDefinition("Type lectures","sequencingProgramType"
	        		, LevelService.getLevels(Level.CODE.Run),String.class, false, DescriptionFactory.newValues("SR","PE"),"single"));
		
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Types processus banque","libProcessTypeCodes",LevelService.getLevels(Level.CODE.Run), String.class, false,"list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Codes aliquots","sampleAliquoteCodes",LevelService.getLevels(Level.CODE.Run), String.class, false,"list"));
	    return propertyDefinitions;
	}

}
