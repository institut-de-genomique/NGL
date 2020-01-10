package services.description.declaration.cns;

import static services.description.DescriptionFactory.newExperimentType;
import static services.description.DescriptionFactory.newExperimentTypeNode;
import static services.description.DescriptionFactory.newPropertiesDefinition;
import static services.description.DescriptionFactory.newValues;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import models.laboratory.common.description.Level;
import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.common.description.Value;
import models.laboratory.experiment.description.ExperimentCategory;
import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.processes.description.ProcessCategory;
import models.laboratory.processes.description.ProcessType;
import services.description.Constants;
import services.description.DescriptionFactory;
import services.description.common.LevelService;
import services.description.declaration.AbstractDeclaration;

public class BanqueIllumina extends AbstractDeclaration {

	@Override
	protected List<ExperimentType> getExperimentTypePROD() {
		return null;
	}
	
	@Override
	protected List<ExperimentType> getExperimentTypeCommon() {
		List<ExperimentType> l = new ArrayList<>();

		l.add(newExperimentType("Ext to Bq DNA à partir frg ou amplicon","ext-to-dna-illumina-indexed-library-process",null,-1,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null, null,"OneToOne",
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

		l.add(newExperimentType("Ext to Bq DNA sizing (gel) à partir frg ou amplicon","ext-to-dna-illumina-indexed-lib-sizing-process",null,-1,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null, null,"OneToOne",
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

		l.add(newExperimentType("Ext to Ampli, stk, dépôt","ext-to-ampli-stk-illumina-depot",null,-1,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null, null,"OneToOne",
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

		l.add(newExperimentType("Ext to Ampli, sizing, stk, dépôt","ext-to-ampli-sizing-stk-illumina-depot",null,-1,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null, null,"OneToOne",
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

		l.add(newExperimentType("Ext to Sizing (gel), stk, dépôt","ext-to-sizing-stk-illumina-depot",null,-1,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null, null,"OneToOne",
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

		l.add(newExperimentType("Ext to ampure, Sizing, STK, Dépôt","ext-to-ampure-sizing-stk-illumina-depot",null,-1,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null, null,"OneToOne",
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

		l.add(newExperimentType("Ext to Ampli, Spri select, stk, dépôt","ext-to-ampli-spri-select-stk-illumina-depot",null,-1,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null, null,"OneToOne",
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

		l.add(newExperimentType("Ext to Spri select, stk, dépôt","ext-to-spri-select-stk-illumina-depot",null,-1,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null, null,"OneToOne",
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

		l.add(newExperimentType("Ext to Bq DNA spri select à partir frg ou amplicon","ext-to-dna-illumina-indexed-lib-spri-select-process",null,-1,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null, null,"OneToOne",
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

		l.add(newExperimentType("Ext to ampure, STK, Dépôt","ext-to-ampure-stk-illumina-depot",null,-1,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null, null,"OneToOne",
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		return l;
	}

	@Override
	protected List<ExperimentType> getExperimentTypeDEV() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected List<ExperimentType> getExperimentTypeUAT() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected List<ProcessType> getProcessTypeDEV() {
		List<ProcessType> l = new ArrayList<>();
	
		
		return l;
	}
	
	@Override
	protected List<ProcessType> getProcessTypeUAT() {
		List<ProcessType> l = new ArrayList<>();
	
		
		return l;
	}
	
	@Override
	protected List<ProcessType> getProcessTypePROD() {
		
		List<ProcessType> l = new ArrayList<>();
		
		return l;
	}

	

	@Override
	protected List<ProcessType> getProcessTypeCommon() {
		List<ProcessType> l = new ArrayList<>();
		
		l.add(DescriptionFactory.newProcessType("Bq DNA à partir de frg ou amplicon", "dna-illumina-indexed-library-process", 
				ProcessCategory.find.findByCode("library"), 41,
			getDNALibIlluminaProcessProperties(),
			Arrays.asList(getPET("ext-to-dna-illumina-indexed-library-process",-1)
					,getPET("fragmentation",-1)
					,getPET("tag-pcr",-1)
					,getPET("dna-illumina-indexed-library",0)
					,getPET("pcr-amplification-and-purification",1)
					,getPET("solution-stock",2)
					,getPET("prepa-flowcell",3)
					,getPET("prepa-fc-ordered",3)
					,getPET("illumina-depot",4)), 
					getExperimentTypes("dna-illumina-indexed-library").get(0), getExperimentTypes("illumina-depot").get(0), getExperimentTypes("ext-to-dna-illumina-indexed-library-process").get(0), 
					DescriptionFactory.getInstitutes(Constants.CODE.CNS)));


		
		l.add(DescriptionFactory.newProcessType("Bq DNA sizing (gel) à partir de frg ou amplicon", "dna-illumina-indexed-lib-sizing-process", 
				ProcessCategory.find.findByCode("library"), 42,
				getDNALibIlluminaSizingProcessProperties(), 
				Arrays.asList(getPET("ext-to-dna-illumina-indexed-lib-sizing-process",-1)
						,getPET("fragmentation",-1)
						,getPET("tag-pcr",-1)
						,getPET("dna-illumina-indexed-library",0)
						,getPET("pcr-amplification-and-purification",1)
						,getPET("sizing",2)
						,getPET("solution-stock",3)
						,getPET("prepa-flowcell",4)
						,getPET("prepa-fc-ordered",4)
						,getPET("illumina-depot",5)), 
						getExperimentTypes("dna-illumina-indexed-library").get(0), getExperimentTypes("illumina-depot").get(0), getExperimentTypes("ext-to-dna-illumina-indexed-lib-sizing-process").get(0), 
						DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(DescriptionFactory.newProcessType("Bq DNA spri select à partir de frg ou amplicon", "dna-illumina-indexed-lib-spri-select-process", 
				ProcessCategory.find.findByCode("library"), 43,
				getDNALibIlluminaSpriSelectProcessProperties(), 
				Arrays.asList(getPET("ext-to-dna-illumina-indexed-lib-spri-select-process",-1)
						,getPET("fragmentation",-1)
						,getPET("tag-pcr",-1)
						,getPET("dna-illumina-indexed-library",0)
						,getPET("pcr-amplification-and-purification",1)
						,getPET("spri-select",2)
						,getPET("solution-stock",3)
						,getPET("prepa-flowcell",4)
						,getPET("prepa-fc-ordered",4)
						,getPET("illumina-depot",5)), 
						getExperimentTypes("dna-illumina-indexed-library").get(0), getExperimentTypes("illumina-depot").get(0), getExperimentTypes("ext-to-dna-illumina-indexed-lib-spri-select-process").get(0), 
						DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(DescriptionFactory.newProcessType("Ampli, STK, Dépôt", "ampli-stk-illumina-depot", 
				ProcessCategory.find.findByCode("library"),45,
				getAmpliSTKDepotProcessProperties(), 
				Arrays.asList(getPET("ext-to-ampli-stk-illumina-depot",-1)
						,getPET("dna-illumina-indexed-library",-1)
						,getPET("pcr-amplification-and-purification",0)
						,getPET("solution-stock",1)
						,getPET("prepa-flowcell",2)
						,getPET("prepa-fc-ordered",2)
						,getPET("illumina-depot",3)), 
						getExperimentTypes("pcr-amplification-and-purification").get(0), getExperimentTypes("illumina-depot").get(0), getExperimentTypes("ext-to-ampli-stk-illumina-depot").get(0), 
						DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(DescriptionFactory.newProcessType("Ampli, sizing (gel), STK, Dépôt", "ampli-sizing-stk-illumina-depot", 
				ProcessCategory.find.findByCode("library"), 46,
				getAmpliSizingSTKDepotProcessProperties(),
				Arrays.asList(getPET("ext-to-ampli-sizing-stk-illumina-depot",-1)
						,getPET("dna-illumina-indexed-library",-1)
						,getPET("pcr-amplification-and-purification",0)
						,getPET("sizing",1)
						,getPET("solution-stock",2)
						,getPET("prepa-flowcell",3)
						,getPET("prepa-fc-ordered",3)
						,getPET("illumina-depot",4)), 
						getExperimentTypes("pcr-amplification-and-purification").get(0), getExperimentTypes("illumina-depot").get(0), getExperimentTypes("ext-to-ampli-sizing-stk-illumina-depot").get(0), 
						DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(DescriptionFactory.newProcessType("Ampli, Spri select, STK, Dépôt", "ampli-spri-select-stk-illumina-depot", 
				ProcessCategory.find.findByCode("library"), 47,
				getAmpliSpriSelectDepotProcessProperties(),
				Arrays.asList(getPET("ext-to-ampli-spri-select-stk-illumina-depot",-1)//TODO
						,getPET("dna-illumina-indexed-library",-1)
						,getPET("pcr-amplification-and-purification",0)
						,getPET("spri-select",1)
						,getPET("solution-stock",2)
						,getPET("prepa-flowcell",3)
						,getPET("prepa-fc-ordered",3)
						,getPET("illumina-depot",4)), 
						getExperimentTypes("pcr-amplification-and-purification").get(0), getExperimentTypes("illumina-depot").get(0), getExperimentTypes("ext-to-ampli-spri-select-stk-illumina-depot").get(0), 
						DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(DescriptionFactory.newProcessType("Ampure, Sizing (gel), STK, Dépôt", "ampure-sizing-stk-illumina-depot",
				ProcessCategory.find.findByCode("library"), 48,
				getAmpureSizingSTKDepotProcessProperties(), 
				Arrays.asList(getPET("ext-to-ampure-sizing-stk-illumina-depot",-1)
						,getPET("pcr-amplification-and-purification",-1)
						,getPET("post-pcr-ampure",0)  //GA 08/11/2016 purif need to declare on first during process creation
						,getPET("sizing",0) //GA 08/11/2016 transformation need to declare on first for experiment creation
						,getPET("solution-stock",1)
						,getPET("prepa-flowcell",2)
						,getPET("prepa-fc-ordered",2)
						,getPET("illumina-depot",3)), 
						getExperimentTypes("post-pcr-ampure").get(0), getExperimentTypes("illumina-depot").get(0), getExperimentTypes("ext-to-ampure-sizing-stk-illumina-depot").get(0), 
						DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(DescriptionFactory.newProcessType("Ampure, STK, Dépôt", "ampure-stk-illumina-depot",
				ProcessCategory.find.findByCode("library"), 51,
				getAmpureSTKDepotProcessProperties(), 
				Arrays.asList(getPET("ext-to-ampure-stk-illumina-depot",-1)
						,getPET("pcr-amplification-and-purification",-1)
						,getPET("indexing-and-pcr-amplification",-1)
						,getPET("post-pcr-ampure",0)  //GA 08/11/2016 purif need to declare on first during process creation
						,getPET("solution-stock",0) //GA 08/11/2016 transformation need to declare on first for experiment creation
						,getPET("prepa-flowcell",1)
						,getPET("prepa-fc-ordered",1)
						,getPET("illumina-depot",2)), 
						getExperimentTypes("post-pcr-ampure").get(0), getExperimentTypes("illumina-depot").get(0), getExperimentTypes("ext-to-ampure-stk-illumina-depot").get(0), 
						DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		
		
		l.add(DescriptionFactory.newProcessType("Sizing (gel), STK, Dépôt", "sizing-stk-illumina-depot",
				ProcessCategory.find.findByCode("library"), 49,
				getSizingSTKDepotProcessProperties(), 
				Arrays.asList(getPET("ext-to-sizing-stk-illumina-depot",-1)
						,getPET("pcr-amplification-and-purification",-1)
						,getPET("sizing",0)
						,getPET("solution-stock",1)
						,getPET("prepa-flowcell",2)
						,getPET("prepa-fc-ordered",2)
						,getPET("illumina-depot",3)), 
						getExperimentTypes("sizing").get(0), getExperimentTypes("illumina-depot").get(0), getExperimentTypes("ext-to-sizing-stk-illumina-depot").get(0), 
						DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(DescriptionFactory.newProcessType("Spri select, STK, Dépôt", "spri-select-stk-illumina-depot",
				ProcessCategory.find.findByCode("library"), 50,
				getSpriSelectSTKDepotProcessProperties(), 
				Arrays.asList(getPET("ext-to-spri-select-stk-illumina-depot",-1)
						,getPET("pcr-amplification-and-purification",-1)
						,getPET("spri-select",0)
						,getPET("solution-stock",1)
						,getPET("prepa-flowcell",2)
						,getPET("prepa-fc-ordered",2)
						,getPET("illumina-depot",3)), 
						getExperimentTypes("spri-select").get(0), getExperimentTypes("illumina-depot").get(0), getExperimentTypes("ext-to-spri-select-stk-illumina-depot").get(0), 
						DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(DescriptionFactory.newProcessType("Réorientation manips - STK, FC, Dépôt", "reor-stk-illumina-depot", 
				ProcessCategory.find.findByCode("library"), 52,
				getReorSTKDepotProperties(), 
				Arrays.asList(getPET("ext-to-norm-fc-depot-illumina",-1),
						getPET("sizing",-1),
						getPET("spri-select",-1),
						getPET("pcr-amplification-and-purification",-1),
						getPET("indexing-and-pcr-amplification",-1),
						getPET("solution-stock",0),
						getPET("prepa-flowcell",1),
						getPET("prepa-fc-ordered",1),
						getPET("illumina-depot",2)), 
						getExperimentTypes("solution-stock").get(0), getExperimentTypes("illumina-depot").get(0), getExperimentTypes("ext-to-norm-fc-depot-illumina").get(0), 
						DescriptionFactory.getInstitutes(Constants.CODE.CNS)));		
		
		
		return l;	
	}
	
	private List<PropertyDefinition> getReorSTKDepotProperties() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();	
		
		List<Value> libProcessTypeCodes = new ArrayList<>();
		libProcessTypeCodes.addAll(MetaGenomique.getLibProcessDA());
		libProcessTypeCodes.addAll(MetaBarCoding.getBanqueProcessTypeMetaTA());
		propertyDefinitions.add(getLibProcessTypeCodeProperty(libProcessTypeCodes));
		propertyDefinitions.addAll(getAmpureProperties());
		propertyDefinitions.addAll(RunIllumina.getIlluminaDepotProperties());
		return propertyDefinitions;
	}


	private List<PropertyDefinition> getDNALibIlluminaProcessProperties() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();	
		
		List<Value> libProcessTypeCodes = new ArrayList<>();
		libProcessTypeCodes.addAll(MetaGenomique.getLibProcessDA());
		libProcessTypeCodes.addAll(MetaGenomique.getLibProcessDD());
		libProcessTypeCodes.addAll(MetaGenomique.getLibProcessDE());
		libProcessTypeCodes.addAll(MetaBarCoding.getBanqueProcessTypeMetaTA());
		libProcessTypeCodes.addAll(MetaBarCoding.getBanqueProcessTypeMetaTC());
		
		propertyDefinitions.add(getLibProcessTypeCodeProperty(libProcessTypeCodes));
		propertyDefinitions.addAll(getAmpureProperties());
		propertyDefinitions.addAll(RunIllumina.getIlluminaDepotProperties());
		return propertyDefinitions;
	}

	private List<PropertyDefinition> getDNALibIlluminaSizingProcessProperties() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();	
		
		List<Value> libProcessTypeCodes = new ArrayList<>();
		libProcessTypeCodes.addAll(MetaGenomique.getLibProcessDB());
		libProcessTypeCodes.addAll(MetaBarCoding.getBanqueProcessTypeMetaTB());
		propertyDefinitions.add(getLibProcessTypeCodeProperty(libProcessTypeCodes));
		
		
		propertyDefinitions.addAll(getAmpureProperties());
		propertyDefinitions.addAll(getSizingProperties());
		propertyDefinitions.addAll(RunIllumina.getIlluminaDepotProperties());
		return propertyDefinitions;
	}
	
	private List<PropertyDefinition> getDNALibIlluminaSpriSelectProcessProperties() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();	
		
		List<Value> libProcessTypeCodes = new ArrayList<>();
		libProcessTypeCodes.addAll(MetaGenomique.getLibProcessDC());
		libProcessTypeCodes.addAll(MetaGenomique.getLibProcessDD());
		propertyDefinitions.add(getLibProcessTypeCodeProperty(libProcessTypeCodes));
		
		
		propertyDefinitions.addAll(getSpriSelectProperties());
		return propertyDefinitions;
	}
	
	private List<PropertyDefinition> getAmpureSTKDepotProcessProperties() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();	
		
		List<Value> libProcessTypeCodes = new ArrayList<>();
		libProcessTypeCodes.addAll(MetaGenomique.getLibProcessDA());
		libProcessTypeCodes.addAll(MetaBarCoding.getBanqueProcessTypeMetaTA());
		libProcessTypeCodes.addAll(MetaBarCoding.getBanqueProcessTypeN());
		
		propertyDefinitions.add(getLibProcessTypeCodeProperty(libProcessTypeCodes));
		
		propertyDefinitions.addAll(getAmpureProperties());
		propertyDefinitions.addAll(RunIllumina.getIlluminaDepotProperties());
		return propertyDefinitions;
	}
	
	private List<PropertyDefinition> getAmpureSizingSTKDepotProcessProperties() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();	
		
		List<Value> libProcessTypeCodes = new ArrayList<>();
		libProcessTypeCodes.addAll(MetaGenomique.getLibProcessDB());
		libProcessTypeCodes.addAll(MetaBarCoding.getBanqueProcessTypeMetaTB());
		propertyDefinitions.add(getLibProcessTypeCodeProperty(libProcessTypeCodes));
		
		propertyDefinitions.addAll(getAmpureProperties());
		propertyDefinitions.addAll(getSizingProperties());
		propertyDefinitions.addAll(RunIllumina.getIlluminaDepotProperties());
		return propertyDefinitions;
	}
	
	private List<PropertyDefinition> getAmpliSizingSTKDepotProcessProperties() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();	
		
		List<Value> libProcessTypeCodes = new ArrayList<>();
		libProcessTypeCodes.addAll(MetaGenomique.getLibProcessDB());
		libProcessTypeCodes.addAll(MetaBarCoding.getBanqueProcessTypeMetaTB());
		propertyDefinitions.add(getLibProcessTypeCodeProperty(libProcessTypeCodes));
		
		propertyDefinitions.addAll(getAmpureProperties());
		propertyDefinitions.addAll(getSizingProperties());
		propertyDefinitions.addAll(RunIllumina.getIlluminaDepotProperties());
		return propertyDefinitions;
	}
	
	private List<PropertyDefinition> getAmpliSTKDepotProcessProperties() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();	
		
		List<Value> libProcessTypeCodes = new ArrayList<>();
		libProcessTypeCodes.addAll(MetaGenomique.getLibProcessDA());
		libProcessTypeCodes.addAll(MetaGenomique.getLibProcessDD());
		libProcessTypeCodes.addAll(MetaGenomique.getLibProcessDE());
		libProcessTypeCodes.addAll(MetaBarCoding.getBanqueProcessTypeMetaTA());
		propertyDefinitions.add(getLibProcessTypeCodeProperty(libProcessTypeCodes));
		
		
		propertyDefinitions.addAll(getAmpureProperties());
		propertyDefinitions.addAll(RunIllumina.getIlluminaDepotProperties());
		return propertyDefinitions;
	}

	private List<PropertyDefinition> getAmpliSpriSelectDepotProcessProperties() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();	
		
		List<Value> libProcessTypeCodes = new ArrayList<>();
		libProcessTypeCodes.addAll(MetaGenomique.getLibProcessDC());
		libProcessTypeCodes.addAll(MetaGenomique.getLibProcessDD());
		propertyDefinitions.add(getLibProcessTypeCodeProperty(libProcessTypeCodes));
		
		propertyDefinitions.addAll(getSpriSelectProperties());
		
		return propertyDefinitions;
	}
	
	private List<PropertyDefinition> getSizingSTKDepotProcessProperties(){
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();	
		
		List<Value> libProcessTypeCodes = new ArrayList<>();
		libProcessTypeCodes.addAll(MetaGenomique.getLibProcessDB());
		libProcessTypeCodes.addAll(MetaBarCoding.getBanqueProcessTypeMetaTB());
		propertyDefinitions.add(getLibProcessTypeCodeProperty(libProcessTypeCodes));
		
		propertyDefinitions.addAll(getSizingProperties());
		propertyDefinitions.addAll(RunIllumina.getIlluminaDepotProperties());
		return propertyDefinitions;
	}
	
	private List<PropertyDefinition> getSpriSelectSTKDepotProcessProperties() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();	
		
		List<Value> libProcessTypeCodes = new ArrayList<>();
		libProcessTypeCodes.addAll(MetaGenomique.getLibProcessDC());
		libProcessTypeCodes.addAll(MetaGenomique.getLibProcessDD());
		propertyDefinitions.add(getLibProcessTypeCodeProperty(libProcessTypeCodes));
		
		propertyDefinitions.addAll(getSpriSelectProperties());
		return propertyDefinitions;
	}
	
	private PropertyDefinition getLibProcessTypeCodeProperty(List<Value> libProcessTypeCodes) {
		return newPropertiesDefinition("Type processus Banque", "libProcessTypeCode", LevelService.getLevels(Level.CODE.Process,Level.CODE.Content), String.class, true, null, libProcessTypeCodes, 
				null,null,null,"single", 13, true, null, null);
	}
	

	private List<PropertyDefinition> getAmpureProperties(){
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();	
		propertyDefinitions.add(newPropertiesDefinition("Ratio ampure post-pcr", "postPcrAmpureVolume", LevelService.getLevels(Level.CODE.Process), String.class, false, null, null, 
				null,null,null,"single", 14, true, null, null));
		return propertyDefinitions;
	}
	
	private List<PropertyDefinition> getSizingProperties(){
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();	
		propertyDefinitions.add(newPropertiesDefinition("Objectif sizing 1", "sizingGoal", LevelService.getLevels(Level.CODE.Process), String.class, true, null, 
				newValues("280-310 (F300)","400-550 (ITS2)", "450-550 (W500)","550-650 (W600)","500-650", "600-700 (W700)", "650-750 (W700)", "650-700 (W700)", "700-800 (W800)","750-800","autre"), 					                             
				null,null,null,"single", 17, true, null, null));
		propertyDefinitions.add(newPropertiesDefinition("Objectif sizing 2", "sizingGoal2", LevelService.getLevels(Level.CODE.Process), String.class, false, null, 
				newValues("550-700 (ITS2)", "650-800"), 
				null,null,null,"single", 18, true, null, null));
		
		return propertyDefinitions;
	}
	
	private List<PropertyDefinition> getSpriSelectProperties(){
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();	
		propertyDefinitions.add(newPropertiesDefinition("Objectif sizing 1", "sizingGoal", LevelService.getLevels(Level.CODE.Process), String.class, true, null, 
				newValues("ss0.6/0.53","ss0.7/0.58","autre"), 											
				null,null,null,"single", 17, true, null, null));	
		propertyDefinitions.addAll(RunIllumina.getIlluminaDepotProperties());
		return propertyDefinitions;
	}
	

	@Override
	protected void getExperimentTypeNodeCommon() {
		// TODO Auto-generated method stub
		newExperimentTypeNode("ext-to-dna-illumina-indexed-library-process", getExperimentTypes("ext-to-dna-illumina-indexed-library-process").get(0), false, false, false, null, null, null, null).save();
		newExperimentTypeNode("ext-to-dna-illumina-indexed-lib-sizing-process", getExperimentTypes("ext-to-dna-illumina-indexed-lib-sizing-process").get(0), false, false, false, null, null, null, null).save();

		newExperimentTypeNode("ext-to-ampli-stk-illumina-depot", getExperimentTypes("ext-to-ampli-stk-illumina-depot").get(0), false, false, false, null, null, null, null).save();
		newExperimentTypeNode("ext-to-ampli-sizing-stk-illumina-depot", getExperimentTypes("ext-to-ampli-sizing-stk-illumina-depot").get(0), false, false, false, null, null, null, null).save();
		newExperimentTypeNode("ext-to-sizing-stk-illumina-depot", getExperimentTypes("ext-to-sizing-stk-illumina-depot").get(0), false, false, false, null, null, null, null).save();
		newExperimentTypeNode("ext-to-ampure-sizing-stk-illumina-depot", getExperimentTypes("ext-to-ampure-sizing-stk-illumina-depot").get(0), false, false, false, null, null, null, null).save();


		newExperimentTypeNode("ext-to-ampure-stk-illumina-depot", getExperimentTypes("ext-to-ampure-stk-illumina-depot").get(0), false, false, false, null, null, null, null).save();
		newExperimentTypeNode("ext-to-dna-illumina-indexed-lib-spri-select-process", getExperimentTypes("ext-to-dna-illumina-indexed-lib-spri-select-process").get(0), false, false, false, null, null, null, null).save();
		newExperimentTypeNode("ext-to-ampli-spri-select-stk-illumina-depot", getExperimentTypes("ext-to-ampli-spri-select-stk-illumina-depot").get(0), false, false, false, null, null, null, null).save();
		newExperimentTypeNode("ext-to-spri-select-stk-illumina-depot", getExperimentTypes("ext-to-spri-select-stk-illumina-depot").get(0), false, false, false, null, null, null, null).save();

	}

	@Override
	protected void getExperimentTypeNodeDEV() {
		
		
	}

	@Override
	protected void getExperimentTypeNodePROD() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void getExperimentTypeNodeUAT() {
		// TODO Auto-generated method stub
		
	}

}
