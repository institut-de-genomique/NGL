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

public class RunServiceCNS extends AbstractRunService {
	
	@Override
	public void saveReadSetType(Map<String, List<ValidationError>> errors) throws DAOException {
		List<ReadSetType> l = new ArrayList<>();
		l.add(DescriptionFactory.newReadSetType("Readset Illumina","rsillumina",  getReadSetPropertyDefinitions(),  DescriptionFactory.getInstitutes( Constants.CODE.CNS) ));
		l.add(DescriptionFactory.newReadSetType("Readset Nanopore","rsnanopore", getReadSetPropertyDefinitionsNanopore(),  DescriptionFactory.getInstitutes( Constants.CODE.CNS) ));
		l.add(DescriptionFactory.newReadSetType("Dataset Opgen","RSARGUS",  null,  DescriptionFactory.getInstitutes(Constants.CODE.CNS) ));
		
		DAOHelpers.saveModels(ReadSetType.class, l, errors);
	}
	
	@Override
	public void saveAnalysisType(Map<String, List<ValidationError>> errors) throws DAOException {
		List<AnalysisType> l = new ArrayList<>();
		l.add(DescriptionFactory.newAnalysisType("BAC pool assembly","BPA",  null,  DescriptionFactory.getInstitutes(Constants.CODE.CNS) ));
		l.add(DescriptionFactory.newAnalysisType("Dietetic Assembly","dietetic-assembly",  null,  DescriptionFactory.getInstitutes(Constants.CODE.CNS) ));
		l.add(DescriptionFactory.newAnalysisType("Mopad Assembly","mopad-assembly",  null,  DescriptionFactory.getInstitutes(Constants.CODE.CNS) ));
		l.add(DescriptionFactory.newAnalysisType("Amplicons Contamination Control","amplicons-contamination-control",  null,  DescriptionFactory.getInstitutes(Constants.CODE.CNS) ));
		
		DAOHelpers.saveModels(AnalysisType.class, l, errors);
	}

	@Override
	public void saveRunCategories(Map<String, List<ValidationError>> errors) throws DAOException {
		List<RunCategory> l = new ArrayList<>();
		l.add(DescriptionFactory.newSimpleCategory(RunCategory.class, "Illumina", "illumina"));
		l.add(DescriptionFactory.newSimpleCategory(RunCategory.class, "Opgen", "opgen"));
		l.add(DescriptionFactory.newSimpleCategory(RunCategory.class, "Nanopore", "nanopore"));
		DAOHelpers.saveModels(RunCategory.class, l, errors);
	}
	
	@Override
	public void saveRunType(Map<String, List<ValidationError>> errors) throws DAOException {
		List<RunType> l = new ArrayList<>();
		l.add(DescriptionFactory.newRunType("HiSeq 2000","RHS2000", 8, RunCategory.find.findByCode("illumina"), getRunIlluminaPropertyDefinitions(),  DescriptionFactory.getInstitutes( Constants.CODE.CNS) ));
		l.add(DescriptionFactory.newRunType("HiSeq 2500","RHS2500", 8, RunCategory.find.findByCode("illumina"), getRunIlluminaPropertyDefinitions(),  DescriptionFactory.getInstitutes( Constants.CODE.CNS) ));
		l.add(DescriptionFactory.newRunType("HiSeq 2500 rapide","RHS2500R", 2, RunCategory.find.findByCode("illumina"), getRunIlluminaPropertyDefinitions(),  DescriptionFactory.getInstitutes( Constants.CODE.CNS)));
		l.add(DescriptionFactory.newRunType("MiSeq","RMISEQ", 1, RunCategory.find.findByCode("illumina"), getRunIlluminaPropertyDefinitions(), DescriptionFactory.getInstitutes( Constants.CODE.CNS)));
		l.add(DescriptionFactory.newRunType("GA IIx","RGAIIx", 1, RunCategory.find.findByCode("illumina"), getRunIlluminaPropertyDefinitions(), DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		l.add(DescriptionFactory.newRunType("HiSeq 4000","RHS4000", 1, RunCategory.find.findByCode("illumina"), getRunIlluminaPropertyDefinitions(), DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		l.add(DescriptionFactory.newRunType("NovaSeq 6000","RNVS6000", 2, RunCategory.find.findByCode("illumina"), getRunIlluminaPropertyDefinitions(), DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(DescriptionFactory.newRunType("Argus","RARGUS", 1, RunCategory.find.findByCode("opgen"), null, DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(DescriptionFactory.newRunType("MinIon","RMINION", 1, RunCategory.find.findByCode("nanopore"), getRunNanoporePropertyDefinitions(), DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		l.add(DescriptionFactory.newRunType("MKI","RMKI", 1, RunCategory.find.findByCode("nanopore"), getRunNanoporePropertyDefinitions(), DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		l.add(DescriptionFactory.newRunType("MKIb","RMKIB", 1, RunCategory.find.findByCode("nanopore"), getRunNanoporePropertyDefinitions(), DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		l.add(DescriptionFactory.newRunType("PromethION ","RPROMETHION", 1, RunCategory.find.findByCode("nanopore"), getRunNanoporePropertyDefinitions(), DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

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
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Type processus banque","libProcessTypeCode",LevelService.getLevels(Level.CODE.Content), String.class, false,
				getLibProcessTypeCodeValues(), "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Category d'index","tagCategory",LevelService.getLevels(Level.CODE.Content), String.class, false, getTagCategories(), "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% par piste","percentPerLane",LevelService.getLevels(Level.CODE.Content), Double.class, false, "single"));
		//propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Layout Nominal Length","libLayoutNominalLength",LevelService.getLevels(Level.CODE.Content), Integer.class, false, "single"));
		
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Objectif taille insert","insertSizeGoal",LevelService.getLevels(Level.CODE.Content), String.class, false, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Orientation brin synthétisé","strandOrientation",LevelService.getLevels(Level.CODE.Content), String.class, false, "single"));
		
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Méthode synthèse cDNA","cDNAsynthesisType",LevelService.getLevels(Level.CODE.Content), String.class, false, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Protocole bq RNA","rnaLibProtocol",LevelService.getLevels(Level.CODE.Content), String.class, false, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Protocole d'extraction","extractionProtocol",LevelService.getLevels(Level.CODE.Content), String.class, false, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Méthode de déplétion","depletionMethod",LevelService.getLevels(Level.CODE.Content), String.class, false, "single"));
		
		//GA 21/07/2015 ajouter la propriété sampleAliquoteCode au readset, niveau content n'est pas idéal mais résoud le pb actuel (JIRA 672)
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Code aliquot","sampleAliquoteCode",LevelService.getLevels(Level.CODE.Content), String.class, false, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Tag", "tag", LevelService.getLevels(Level.CODE.Content), String.class, false, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Issu du type d'échantillon", "fromSampleTypeCode", LevelService.getLevels(Level.CODE.Content), String.class, false, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Issu de l'echantillon", "fromSampleCode", LevelService.getLevels(Level.CODE.Content), String.class, false, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Issu du projet ", "fromProjectCode", LevelService.getLevels(Level.CODE.Content), String.class, false, "single"));
		
		return propertyDefinitions;
	}
	
	
	//GA 24/07/2015 ajout des TagCategories
	private static List<Value> getTagCategories(){
		List<Value> values = new ArrayList<>();
		values.add(DescriptionFactory.newValue("SINGLE-INDEX", "SINGLE-INDEX"));
		values.add(DescriptionFactory.newValue("DUAL-INDEX", "DUAL-INDEX"));
		values.add(DescriptionFactory.newValue("MID", "MID"));
		values.add(DescriptionFactory.newValue("POOL-INDEX", "POOL-INDEX"));
		return values;	
	}
	
	
	private static List<Value> getLibProcessTypeCodeValues(){
		List<Value> values = new ArrayList<>();
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
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(newPropertiesDefinition("Type lectures","sequencingProgramType"
	        		, LevelService.getLevels(Level.CODE.Run),String.class, false, DescriptionFactory.newValues("SR","PE"),"single"));
		
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Types processus banque","libProcessTypeCodes",LevelService.getLevels(Level.CODE.Run), String.class, false,
				getLibProcessTypeCodeValues(), "list"));
		
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Mail Agirs envoyé ?", "sendMailAgirs", LevelService.getLevels(Level.CODE.Run), Boolean.class, false, null, null, 
				"single", null, false, null, null));
		
	    return propertyDefinitions;
	}
	
	

}
