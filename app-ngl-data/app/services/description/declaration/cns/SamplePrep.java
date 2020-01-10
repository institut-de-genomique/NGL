package services.description.declaration.cns;

import static services.description.DescriptionFactory.newExperimentType;
import static services.description.DescriptionFactory.newExperimentTypeNode;
import static services.description.DescriptionFactory.newPropertiesDefinition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import models.laboratory.common.description.Level;
import models.laboratory.common.description.MeasureCategory;
import models.laboratory.common.description.MeasureUnit;
import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.experiment.description.ExperimentCategory;
import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.processes.description.ProcessCategory;
import models.laboratory.processes.description.ProcessType;
import services.description.Constants;
import services.description.DescriptionFactory;
import services.description.common.LevelService;
import services.description.common.MeasureService;
import services.description.declaration.AbstractDeclaration;

public class SamplePrep extends AbstractDeclaration{

	@Override
	protected List<ExperimentType> getExperimentTypeCommon() {
		List<ExperimentType> l = new ArrayList<>();

		l.add(newExperimentType("Ext to Extraction ADN / ARN (corail)","ext-to-grinding-and-dna-rna-extraction",null,-1,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null, null,"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

		l.add(newExperimentType("Ext to Extraction ADN (corail CDIV)","ext-to-grinding-and-dna-extraction",null,-1,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null, null,"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

		
		l.add(newExperimentType("Broyage ","grinding",null,650,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()), null,
				getInstrumentUsedTypes("fast-prep"),"OneToMany", null,true,
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

		l.add(newExperimentType("Ext to Extraction ADN / ARN (plancton)","ext-to-dna-rna-extraction-process",null,-1,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null, null,"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

		l.add(newExperimentType("Ext to Extraction ADN (à partir aliquot corail CDIV)","ext-to-dna-extraction-process",null,-1,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null, null,"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

		
		l.add(newExperimentType("Extraction ADN / ARN","dna-rna-extraction","AN",700,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()), getPropertyDefinitionsExtractionADNARN(),
				getInstrumentUsedTypes("cryobroyeur","hand"),"OneToMany", getSampleTypes("DNA","RNA"),true,
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(newExperimentType("Extraction ADN","dna-extraction","ADN",705,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()), getPropertyDefinitionsExtractionADN(),
				getInstrumentUsedTypes("hand"),"OneToOne", getSampleTypes("DNA"),true,
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
	
		
		//LBIOMEG ADRI PROCESS
		l.add(newExperimentType("Ext to Extraction ARN (17-200 et >200nt)","ext-to-small-and-large-rna-extraction",null,-1,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null, null,"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(newExperimentType("Extraction ARN (17-200 et >200nt) à partir d'ARN total","ext-to-small-and-large-isolation-process",null,-1,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null, null,"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

		
		l.add(newExperimentType("Extraction ARN total","total-rna-extraction","RNA",710,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()), getPropertyDefinitionsExtractionARNtotal(),
				getInstrumentUsedTypes("hand"),"OneToOne", getSampleTypes("total-RNA"),true,
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(newExperimentType("Séparation ARN 17-200 et > 200nt","small-and-large-rna-isolation","RNA",720,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()), getPropertyDefinitionsExtractionARNSmallLarge(),
				getInstrumentUsedTypes("hand"),"OneToMany", getSampleTypes("RNA"),true,
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(newExperimentType("Ext to WGA (ou WTA)","ext-to-wga-or-wta-process",null,-1,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null, null,"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		
		l.add(newExperimentType("Amplification / WGA (ou WTA)","wga-amplification","WGA",730,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()), getPropertyDefinitionsWGAAmplification(),
				getInstrumentUsedTypes("hand"),"OneToOne", null,true,
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		
		
		
		return l;
	}

	

	@Override
	protected List<ExperimentType> getExperimentTypePROD() {
		return null;
	}

	@Override
	protected List<ExperimentType> getExperimentTypeDEV() {
		List<ExperimentType> l = new ArrayList<>();

		
		return l;
	}

	@Override
	protected List<ExperimentType> getExperimentTypeUAT() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected List<ProcessType> getProcessTypeCommon() {
		List<ProcessType> l=new ArrayList<>();

		l.add(DescriptionFactory.newProcessType("Extraction ADN / ARN (plancton ou à partir d'aliquot corail poisson)", "dna-rna-extraction-process", 
				ProcessCategory.find.findByCode("sample-prep"), 1,
				null, 
				Arrays.asList(
						getPET("ext-to-dna-rna-extraction-process",-1),
						getPET("grinding",-1),
						getPET("dna-rna-extraction",0)), 
						getExperimentTypes("dna-rna-extraction").get(0), getExperimentTypes("dna-rna-extraction").get(0), getExperimentTypes("ext-to-dna-rna-extraction-process").get(0), 
						DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

		l.add(DescriptionFactory.newProcessType("Extraction ADN / ARN (corail et poisson)", "grinding-and-dna-rna-extraction", 
				ProcessCategory.find.findByCode("sample-prep"), 2,
				null, 
				Arrays.asList(
						getPET("ext-to-grinding-and-dna-rna-extraction",-1),
						getPET("grinding",0),
						getPET("dna-rna-extraction",1)), 
						getExperimentTypes("grinding").get(0), getExperimentTypes("dna-rna-extraction").get(0), getExperimentTypes("ext-to-grinding-and-dna-rna-extraction").get(0), 
						DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(DescriptionFactory.newProcessType("Extraction ADN (corail CDIV)", "grinding-and-dna-extraction", 
				ProcessCategory.find.findByCode("sample-prep"), 3,
				null, 
				Arrays.asList(
						getPET("ext-to-grinding-and-dna-extraction",-1),
						getPET("grinding",0),
						getPET("dna-extraction",1)), 
						getExperimentTypes("grinding").get(0), getExperimentTypes("dna-extraction").get(0), getExperimentTypes("ext-to-grinding-and-dna-extraction").get(0), 
						DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

		l.add(DescriptionFactory.newProcessType("Extraction ADN (à partir aliquot corail CDIV)", "dna-extraction-process", 
				ProcessCategory.find.findByCode("sample-prep"), 4,
				null, 
				Arrays.asList(
						getPET("ext-to-dna-extraction-process",-1),
						getPET("grinding",-1),
						getPET("dna-extraction",0)), 
						getExperimentTypes("dna-extraction").get(0), getExperimentTypes("dna-extraction").get(0), getExperimentTypes("ext-to-dna-extraction-process").get(0), 
						DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

		
		
		l.add(DescriptionFactory.newProcessType("Extraction ARN (17-200 et >200nt) à partir de filtres", "small-and-large-rna-extraction", 
				ProcessCategory.find.findByCode("sample-prep"), 5,
				null, 
				Arrays.asList(
						getPET("ext-to-small-and-large-rna-extraction",-1),
						getPET("total-rna-extraction",0),
						getPET("small-and-large-rna-isolation",1)), 
						getExperimentTypes("total-rna-extraction").get(0), getExperimentTypes("small-and-large-rna-isolation").get(0), getExperimentTypes("ext-to-small-and-large-rna-extraction").get(0), 
						DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

	
		l.add(DescriptionFactory.newProcessType("Séparation ARN (17-200 et >200nt) à partir d'ARN total", "small-and-large-isolation-process", 
				ProcessCategory.find.findByCode("sample-prep"), 6,
				null, 
				Arrays.asList(
						getPET("ext-to-small-and-large-isolation-process",-1),
						getPET("total-rna-extraction",-1),
						getPET("small-and-large-rna-isolation",0)), 
						getExperimentTypes("small-and-large-rna-isolation").get(0), getExperimentTypes("small-and-large-rna-isolation").get(0), getExperimentTypes("ext-to-small-and-large-rna-extraction").get(0), 
						DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

		l.add(DescriptionFactory.newProcessType("WGA (ou WTA)", "wga-or-wta-process", 
				ProcessCategory.find.findByCode("sample-prep"), 7,
				null, 
				Arrays.asList(
						getPET("ext-to-wga-or-wta-process",-1),
						getPET("dna-rna-extraction",-1),
						getPET("wga-amplification",0)), 
						getExperimentTypes("wga-amplification").get(0), getExperimentTypes("wga-amplification").get(0), getExperimentTypes("ext-to-wga-or-wta-process").get(0), 
						DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

		return l;	
	}
	
	@Override
	protected List<ProcessType> getProcessTypeDEV() {
		List<ProcessType> l=new ArrayList<>();

		
		return l;	
	}

	@Override
	protected List<ProcessType> getProcessTypePROD() {
		return null;
	}

	@Override
	protected List<ProcessType> getProcessTypeUAT() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void getExperimentTypeNodeCommon() {
		//Sample Preparation 
		newExperimentTypeNode("ext-to-grinding-and-dna-rna-extraction", getExperimentTypes("ext-to-grinding-and-dna-rna-extraction").get(0), false, false, false, null, null, null, null).save();
		newExperimentTypeNode("ext-to-grinding-and-dna-extraction", getExperimentTypes("ext-to-grinding-and-dna-extraction").get(0), false, false, false, null, null, null, null).save();
			
		newExperimentTypeNode("ext-to-dna-rna-extraction-process", getExperimentTypes("ext-to-dna-rna-extraction-process").get(0), false, false, false, null, null, null, null).save();
		newExperimentTypeNode("ext-to-small-and-large-rna-extraction", getExperimentTypes("ext-to-small-and-large-rna-extraction").get(0), false, false, false, null, null, null, null).save();
		newExperimentTypeNode("ext-to-small-and-large-isolation-process", getExperimentTypes("ext-to-small-and-large-isolation-process").get(0), false, false, false, null, null, null, null).save();
		newExperimentTypeNode("ext-to-dna-extraction-process", getExperimentTypes("ext-to-dna-extraction-process").get(0), false, false, false, null, null, null, null).save();

		newExperimentTypeNode("grinding",getExperimentTypes("grinding").get(0),false, false,false,getExperimentTypeNodes("ext-to-grinding-and-dna-rna-extraction","ext-to-grinding-and-dna-extraction"),null,null,null).save();
		newExperimentTypeNode("dna-rna-extraction",getExperimentTypes("dna-rna-extraction").get(0),false, false,false,getExperimentTypeNodes("ext-to-dna-rna-extraction-process","grinding","ext-to-grinding-and-dna-rna-extraction"),
				getExperimentTypes("dnase-treatment"),getExperimentTypes("fluo-quantification","chip-migration","gel-migration","control-pcr-and-gel"),getExperimentTypes("aliquoting")).save();
		newExperimentTypeNode("dna-extraction",getExperimentTypes("dna-extraction").get(0),false, true,true,getExperimentTypeNodes("ext-to-dna-extraction-process","grinding"),
				null,getExperimentTypes("fluo-quantification","chip-migration","gel-migration","control-pcr-and-gel"),getExperimentTypes("aliquoting")).save();

		newExperimentTypeNode("total-rna-extraction",getExperimentTypes("total-rna-extraction").get(0),false, false,false,getExperimentTypeNodes("ext-to-small-and-large-rna-extraction"),
				getExperimentTypes("dnase-treatment"),getExperimentTypes("fluo-quantification","chip-migration"),null).save();
		newExperimentTypeNode("small-and-large-rna-isolation", getExperimentTypes("small-and-large-rna-isolation").get(0), false, false, false, getExperimentTypeNodes("total-rna-extraction","ext-to-small-and-large-isolation-process"), 
				getExperimentTypes("dnase-treatment"),getExperimentTypes("fluo-quantification","chip-migration"), null).save();

		newExperimentTypeNode("ext-to-wga-or-wta-process", getExperimentTypes("ext-to-wga-or-wta-process").get(0), false, false, false, null, null, null, null).save();
		newExperimentTypeNode("wga-amplification", getExperimentTypes("wga-amplification").get(0), false, false, false, getExperimentTypeNodes("ext-to-wga-or-wta-process","dna-rna-extraction"), 
				getExperimentTypes("dnase-treatment"),getExperimentTypes("fluo-quantification","chip-migration"),getExperimentTypes("aliquoting")).save();

	}




	@Override
	protected void getExperimentTypeNodeDEV() {
		
	}

	@Override
	protected void getExperimentTypeNodePROD() {

	}

	@Override
	protected void getExperimentTypeNodeUAT() {
		// TODO Auto-generated method stub

	}

	private List<PropertyDefinition> getPropertyDefinitionsExtractionADNARN() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		//InputContainer

		propertyDefinitions.add(newPropertiesDefinition("Sample Type", "sampleTypeCode", LevelService.getLevels(Level.CODE.ContainerOut), String.class, true, "N", null, 
				"single", 15, false, null,null));
		propertyDefinitions.add(newPropertiesDefinition("Projet", "projectCode", LevelService.getLevels(Level.CODE.ContainerOut), String.class, true, null, 
				null, null ,null ,null ,"single", 20, false, null,null));
		
		propertyDefinitions.add(newPropertiesDefinition("Echantillon", "sampleCode", LevelService.getLevels(Level.CODE.ContainerOut), String.class, true, null, 
				null, null, null, null,"single", 25, false, null,null));

		
		propertyDefinitions.add(newPropertiesDefinition("Code éch. témoin négatif extraction", "extractionBlankSampleCode", LevelService.getLevels(Level.CODE.ContainerOut, Level.CODE.Content), String.class, true, "F",
				null, null, null, null,"single", 26, false, null,null));

		
		return propertyDefinitions;
	}
	
	private List<PropertyDefinition> getPropertyDefinitionsExtractionADN() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		//InputContainer

		propertyDefinitions.add(newPropertiesDefinition("Volume engagé", "inputVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, 
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single", 8, true, null,null));

		propertyDefinitions.add(newPropertiesDefinition("Sample Type", "sampleTypeCode", LevelService.getLevels(Level.CODE.ContainerOut), String.class, true, "N", null, 
				"single", 15, false, null,null));
		propertyDefinitions.add(newPropertiesDefinition("Projet", "projectCode", LevelService.getLevels(Level.CODE.ContainerOut), String.class, true, null, 
				null, null ,null ,null ,"single", 20, false, null,null));
	
		propertyDefinitions.add(newPropertiesDefinition("Echantillon", "sampleCode", LevelService.getLevels(Level.CODE.ContainerOut), String.class, true, null, 
				null, null, null, null,"single", 25, false, null,null));

		propertyDefinitions.add(newPropertiesDefinition("Code éch. témoin négatif extraction", "extractionBlankSampleCode", LevelService.getLevels(Level.CODE.ContainerOut, Level.CODE.Content), String.class, true, "F",
				null, null, null, null,"single", 26, false, null,null));

		
		return propertyDefinitions;
	}


	
	private List<PropertyDefinition> getPropertyDefinitionsExtractionARNtotal() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		//InputContainer

		propertyDefinitions.add(newPropertiesDefinition("Sample Type", "sampleTypeCode", LevelService.getLevels(Level.CODE.ContainerOut), String.class, true, "N", 
				Collections.singletonList(DescriptionFactory.newValue("total-RNA","ARN total")),"single", 15, true, "total-RNA",null));
		propertyDefinitions.add(newPropertiesDefinition("Projet", "projectCode", LevelService.getLevels(Level.CODE.ContainerOut), String.class, true, null, 
				null, null ,null ,null ,"single", 20, false, null,null));
		propertyDefinitions.add(newPropertiesDefinition("Echantillon", "sampleCode", LevelService.getLevels(Level.CODE.ContainerOut), String.class, true, null, 
				null, null, null, null,"single", 25, false, null,null));

		return propertyDefinitions;
	}
	
	private List<PropertyDefinition> getPropertyDefinitionsExtractionARNSmallLarge() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		//InputContainer
		/*
		propertyDefinitions.add(newPropertiesDefinition("Quantité engagée","inputQuantity", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, false, null,
				null,MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),MeasureUnit.find.findByCode( "ng"),MeasureUnit.find.findByCode( "ng"),"single",13, true,null,"1"));
		*/
		propertyDefinitions.add(newPropertiesDefinition("Sample Type", "sampleTypeCode", LevelService.getLevels(Level.CODE.ContainerOut), String.class, true, "N", 
				Collections.singletonList(DescriptionFactory.newValue("RNA","ARN")), "single", 15, false, "RNA",null));
		propertyDefinitions.add(newPropertiesDefinition("Projet", "projectCode", LevelService.getLevels(Level.CODE.ContainerOut), String.class, true, null, 
				null, "single", 20, false, null,null));
		propertyDefinitions.add(newPropertiesDefinition("Echantillon", "sampleCode", LevelService.getLevels(Level.CODE.ContainerOut), String.class, true, null, 
				null, "single", 25, false, null,null));

		propertyDefinitions.add(newPropertiesDefinition("taille ARN", "rnaSize", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), String.class, true, "N", 
				DescriptionFactory.newValues("17-200nt",">200nt"), "single", 26, false, null,null));

		
		return propertyDefinitions;
	}
	private List<PropertyDefinition> getPropertyDefinitionsWGAAmplification() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(newPropertiesDefinition("Volume engagé", "inputVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, 
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single", 12, true, null,null));
		propertyDefinitions.add(newPropertiesDefinition("Quantité engagée","inputQuantity", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, false, null,
				null,MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),MeasureUnit.find.findByCode( "ng"),MeasureUnit.find.findByCode( "ng"),"single",13, true,null,null));
		propertyDefinitions.add(newPropertiesDefinition("Méthode préparation ADN (ou ARN)", "dnaTreatment", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), String.class, true, null,
				DescriptionFactory.newValues("WGA","WTA","SAG + WGA"), null, null, null,"single", 18, true, null,null));
		
		return propertyDefinitions;
	}

}
