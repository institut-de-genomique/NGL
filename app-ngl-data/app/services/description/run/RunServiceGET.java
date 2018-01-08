package services.description.run;

import static services.description.DescriptionFactory.newPropertiesDefinition;
import services.description.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import models.laboratory.common.description.Institute;
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
import services.description.DescriptionFactory;
import services.description.common.LevelService;

public class RunServiceGET extends AbstractRunService {
	
	
	public void saveReadSetType(Map<String, List<ValidationError>> errors) throws DAOException {
		List<ReadSetType> l = new ArrayList<ReadSetType>();
		l.add(DescriptionFactory.newReadSetType("Default","default-readset",  getReadSetPropertyDefinitions(),  DescriptionFactory.getInstitutes( Constants.CODE.GET) ));
		l.add(DescriptionFactory.newReadSetType("rsillumina","rsillumina",  getReadSetPropertyDefinitions(),  DescriptionFactory.getInstitutes( Constants.CODE.GET) ));
		l.add(DescriptionFactory.newReadSetType("rsnanopore","rsnanopore", null,  DescriptionFactory.getInstitutes( Constants.CODE.GET) ));
		l.add(DescriptionFactory.newReadSetType("RSARGUS","RSARGUS",  null,  DescriptionFactory.getInstitutes(Constants.CODE.GET) ));
		
		DAOHelpers.saveModels(ReadSetType.class, l, errors);
	}
	
	public void saveAnalysisType(Map<String, List<ValidationError>> errors) throws DAOException {
		List<AnalysisType> l = new ArrayList<AnalysisType>();
		l.add(DescriptionFactory.newAnalysisType("BAC pool assembly","BPA",  null,  DescriptionFactory.getInstitutes(Constants.CODE.GET) ));
		
		DAOHelpers.saveModels(AnalysisType.class, l, errors);
	}

	public void saveRunCategories(Map<String, List<ValidationError>> errors) throws DAOException {
		List<RunCategory> l = new ArrayList<RunCategory>();
		l.add(DescriptionFactory.newSimpleCategory(RunCategory.class, "Illumina", "illumina"));
		l.add(DescriptionFactory.newSimpleCategory(RunCategory.class, "Opgen", "opgen"));
		l.add(DescriptionFactory.newSimpleCategory(RunCategory.class, "Nanopore", "nanopore"));
		DAOHelpers.saveModels(RunCategory.class, l, errors);
	}
	
	public void saveRunType(Map<String, List<ValidationError>> errors) throws DAOException {
		List<RunType> l = new ArrayList<RunType>();
		l.add(DescriptionFactory.newRunType("RHS2000","RHS2000", 8, RunCategory.find.findByCode("illumina"), getRunIlluminaPropertyDefinitions(),  DescriptionFactory.getInstitutes( Constants.CODE.GET) ));
		l.add(DescriptionFactory.newRunType("RHS2500","RHS2500", 8, RunCategory.find.findByCode("illumina"), getRunIlluminaPropertyDefinitions(),  DescriptionFactory.getInstitutes( Constants.CODE.GET) ));
		l.add(DescriptionFactory.newRunType("RHS2500R","RHS2500R", 2, RunCategory.find.findByCode("illumina"), getRunIlluminaPropertyDefinitions(),  DescriptionFactory.getInstitutes( Constants.CODE.GET)));
		l.add(DescriptionFactory.newRunType("RMISEQ","RMISEQ", 1, RunCategory.find.findByCode("illumina"), getRunIlluminaPropertyDefinitions(), DescriptionFactory.getInstitutes( Constants.CODE.GET)));
		l.add(DescriptionFactory.newRunType("RGAIIx","RGAIIx", 1, RunCategory.find.findByCode("illumina"), getRunIlluminaPropertyDefinitions(), DescriptionFactory.getInstitutes(Constants.CODE.GET)));
		l.add(DescriptionFactory.newRunType("RHS4000","RHS4000", 1, RunCategory.find.findByCode("illumina"), null, DescriptionFactory.getInstitutes(Constants.CODE.GET)));

		l.add(DescriptionFactory.newRunType("RARGUS","RARGUS", 1, RunCategory.find.findByCode("opgen"), null, DescriptionFactory.getInstitutes(Constants.CODE.GET)));
		
		l.add(DescriptionFactory.newRunType("RMINION","RMINION", 1, RunCategory.find.findByCode("nanopore"), null, DescriptionFactory.getInstitutes(Constants.CODE.GET)));
		l.add(DescriptionFactory.newRunType("RMKI","RMKI", 1, RunCategory.find.findByCode("nanopore"), null, DescriptionFactory.getInstitutes(Constants.CODE.GET)));

		DAOHelpers.saveModels(RunType.class, l, errors);
	}
	
	private static List<PropertyDefinition> getReadSetPropertyDefinitions() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("asciiEncoding","asciiEncoding",LevelService.getLevels(Level.CODE.File), String.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("label","label",LevelService.getLevels(Level.CODE.File), String.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("md5","md5",LevelService.getLevels(Level.CODE.File), String.class, false, "single"));
		
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("isSentCCRT","isSentCCRT",LevelService.getLevels(Level.CODE.ReadSet), Boolean.class, false, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("isSentCollaborator","isSentCollaborator",LevelService.getLevels(Level.CODE.ReadSet), Boolean.class, false, "single"));
		
		//use only for dynamic filters and dynamic properties
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Type processus banque","libProcessTypeCode",LevelService.getLevels(Level.CODE.Content), String.class, false,
				getLibProcessTypeCodeValues(), "single"));
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
	
	//GA 24/07/2015 ajout des TagCategories
	private static List<Value> getTagCategories(){
		List<Value> values = new ArrayList<Value>();
		values.add(DescriptionFactory.newValue("SINGLE-INDEX", "SINGLE-INDEX"));
		values.add(DescriptionFactory.newValue("DUAL-INDEX", "DUAL-INDEX"));
//		values.add(DescriptionFactory.newValue("MID", "MID"));
		values.add(DescriptionFactory.newValue("NO-INDEX", "NO-INDEX"));
		values.add(DescriptionFactory.newValue("10X", "10X"));
		return values;	
	}
	
	
	private static List<Value> getLibProcessTypeCodeValues(){
		List<Value> values = new ArrayList<Value>();
		values.add(DescriptionFactory.newValue("A", "A - Mate-pair"));
		values.add(DescriptionFactory.newValue("B", "B - Multiplex-pairee"));
		values.add(DescriptionFactory.newValue("C", "C - Multiplex-mate-pair"));
		values.add(DescriptionFactory.newValue("D", "D - Digestion"));
		values.add(DescriptionFactory.newValue("E", "E - Paire chevauchant"));
		values.add(DescriptionFactory.newValue("F",	"F - Paire chevauchant multiplex"));
		values.add(DescriptionFactory.newValue("G", "G - Capture simple"));
		values.add(DescriptionFactory.newValue("H", "H - Capture multiplex"));
		values.add(DescriptionFactory.newValue("K", "K - Mate-pair clip"));
		values.add(DescriptionFactory.newValue("L", "L - Moleculo-like"));
		values.add(DescriptionFactory.newValue("M", "M - Multiplex"));
		values.add(DescriptionFactory.newValue("MI", "MI - Moleculo Illumina"));
		values.add(DescriptionFactory.newValue("N", "N - Mate-pair Nextera"));
		values.add(DescriptionFactory.newValue("P", "P - Pairee"));
		values.add(DescriptionFactory.newValue("S", "S - Simple"));
		values.add(DescriptionFactory.newValue("U", "U - Simple ou Paire"));
		values.add(DescriptionFactory.newValue("W", "W - Simple ou Paire multiplex"));
		values.add(DescriptionFactory.newValue("Z", "Z - BAC ENDs Illumina"));
		return values;
		
	} 
	
	private static List<PropertyDefinition> getRunIlluminaPropertyDefinitions() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
		propertyDefinitions.add(newPropertiesDefinition("Type lectures","sequencingProgramType"
	        		, LevelService.getLevels(Level.CODE.Run),String.class, false, DescriptionFactory.newValues("SR","PE"),"single"));
		
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Types processus banque","libProcessTypeCodes",LevelService.getLevels(Level.CODE.Run), String.class, false,
				getLibProcessTypeCodeValues(), "list"));
		
	    return propertyDefinitions;
	}
	



}
