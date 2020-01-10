package services.description.declaration.cns;

import static services.description.DescriptionFactory.newExperimentType;
import static services.description.DescriptionFactory.newExperimentTypeNode;
import static services.description.DescriptionFactory.newPropertiesDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import models.laboratory.common.description.Level;
import models.laboratory.common.description.MeasureCategory;
import models.laboratory.common.description.MeasureUnit;
import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.experiment.description.ExperimentCategory;
import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.processes.description.ExperimentTypeNode;
import models.laboratory.processes.description.ProcessCategory;
import models.laboratory.processes.description.ProcessExperimentType;
import models.laboratory.processes.description.ProcessType;
import services.description.Constants;
import services.description.DescriptionFactory;
import services.description.common.LevelService;
import services.description.common.MeasureService;
import services.description.declaration.AbstractDeclaration;
import services.description.experiment.AbstractExperimentService;

public class Purif extends AbstractDeclaration {

	@Override
	protected List<ExperimentType> getExperimentTypeCommon() {
		List<ExperimentType> l = new ArrayList<>();

		//purif
		l.add(newExperimentType("Traitement DNAse","dnase-treatment",null, 30100,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.purification.name()), getPropertyDefinitionsDNAseTreatment(),
				getInstrumentUsedTypes("hand"),"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(newExperimentType("Déplétion ARN ribo","rrna-depletion",null, 30200,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.purification.name()), getPropertyDefinitionsRRNADepletion(),
				getInstrumentUsedTypes("hand"),"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(newExperimentType("Ampure","ampure",null, 30300,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.purification.name()), getPropertyDefinitionsPostAmpurePCR(),
				getInstrumentUsedTypes("hand","biomek-fx"),"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));	
		
		l.add(newExperimentType("Ampure Post-PCR","post-pcr-ampure",null, 30400,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.purification.name()), getPropertyDefinitionsPostAmpurePCR(),
				getInstrumentUsedTypes("hand","biomek-fx"),"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));	
		
		l.add(newExperimentType("Purification sur colonne","spin-column-purification",null, 30500,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.purification.name()), getPropertyDefinitionsSpinColumnPurification(),
				getInstrumentUsedTypes("hand"),"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));	
		
		l.add(newExperimentType("Débranchage","wga-debranching",null, 30600,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.purification.name()), getPropertyDefinitionsWGADebranching(),
				getInstrumentUsedTypes("hand"),"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));	
		
		
		l.add(newExperimentType("Ext to Purif / eval / TF","ext-to-purif-qc-transfert",null,-1,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null, null,"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		
		return l;
		
	}
	
	
	private List<PropertyDefinition> getPropertyDefinitionsWGADebranching() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();

		propertyDefinitions.add(newPropertiesDefinition("Volume engagé", "inputVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, 
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single", 12, true, null,"1"));
		
		propertyDefinitions.add(newPropertiesDefinition("Quantité engagée","inputQuantity", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, true, null,
				null,MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),MeasureUnit.find.findByCode( "ng"),MeasureUnit.find.findByCode( "ng"),"single",13, true,null,"1"));
			

		propertyDefinitions.add(newPropertiesDefinition("Nombre de débranchages", "nbDebranching", LevelService.getLevels(Level.CODE.ContainerIn), Integer.class, true, null, 
				null, null, null, null,"single", 14, true, null, null));
	
		
		propertyDefinitions.add(newPropertiesDefinition("Ratio billes Ampure", "adnBeadVolumeRatio", LevelService.getLevels(Level.CODE.ContainerIn), String.class, false, null, 
				null, null, null, null,"single", 15, true, null, null));

		return propertyDefinitions;
	}

	private List<PropertyDefinition> getPropertyDefinitionsDNAseTreatment() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();

		propertyDefinitions.add(newPropertiesDefinition("Volume engagé", "inputVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, 
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single", 12, true, null,"1"));
		propertyDefinitions.add(newPropertiesDefinition("Quantité engagée","inputQuantity", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, false, null,
				null,MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),MeasureUnit.find.findByCode( "ng"),MeasureUnit.find.findByCode( "ng"),"single",13, true,null,"1"));
		
		return propertyDefinitions;
	}
	private List<PropertyDefinition> getPropertyDefinitionsRRNADepletion() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();

		propertyDefinitions.add(newPropertiesDefinition("Volume engagé", "inputVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, 
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single", 12, true, null,"1"));
		propertyDefinitions.add(newPropertiesDefinition("Quantité engagée","inputQuantity", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, false, null,
				null,MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),MeasureUnit.find.findByCode( "ng"),MeasureUnit.find.findByCode( "ng"),"single",13, true,null,"1"));
		
		return propertyDefinitions;
	}
	
	
	private List<PropertyDefinition> getPropertyDefinitionsPostAmpurePCR() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(newPropertiesDefinition("Ratio billes Ampure", "adnBeadVolumeRatio", LevelService.getLevels(Level.CODE.Experiment), String.class, true, null, 
				null, null, null, null,"single", 2, true, null, null));
	
		propertyDefinitions.add(newPropertiesDefinition("Volume engagé", "inputVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, 
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single", 12, true, null,"1"));
		
		return propertyDefinitions;
	}

	
	private List<PropertyDefinition> getPropertyDefinitionsSpinColumnPurification() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();

		propertyDefinitions.add(newPropertiesDefinition("Volume engagé", "inputVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, 
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single", 12, true, null,"1"));
		
		return propertyDefinitions;
	}
	
	
	
	@Override
	protected List<ExperimentType> getExperimentTypeDEV() {
		return null;
	}

	@Override
	protected List<ExperimentType> getExperimentTypePROD() {
		return null;
	}

	@Override
	protected List<ExperimentType> getExperimentTypeUAT() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected List<ProcessType> getProcessTypeCommon() {
	List<ProcessType> l = new ArrayList<>();
		
		
		l.add(DescriptionFactory.newProcessType("Purif puis satellites", "purif-qc-transfert", 
				ProcessCategory.find.findByCode("satellites"), 1020,
				null, 
				getPETForPurifQCTransfert(), 
				getExperimentTypes("rrna-depletion").get(0), getExperimentTypes("ext-to-purif-qc-transfert").get(0), getExperimentTypes("ext-to-purif-qc-transfert").get(0), 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		// TODO Auto-generated method stub
		return l;

	}

	
	@Override
	protected List<ProcessType> getProcessTypeDEV() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected List<ProcessType> getProcessTypePROD() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected List<ProcessType> getProcessTypeUAT() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void getExperimentTypeNodeCommon() {
		newExperimentTypeNode("ext-to-purif-qc-transfert", AbstractExperimentService.getExperimentTypes("ext-to-purif-qc-transfert").get(0), false, false, false, 
				null, getExperimentTypes("dnase-treatment","rrna-depletion"), getExperimentTypes("fluo-quantification","chip-migration"),getExperimentTypes("pool","tubes-to-plate","plate-to-tubes")).save();		
	
		
		//GA 07/11/2016 USED FOR PROCESS who start with ampure
		newExperimentTypeNode("post-pcr-ampure",getExperimentTypes("post-pcr-ampure").get(0),false, false,false,
				getExperimentTypeNodes("pcr-amplification-and-purification","indexing-and-pcr-amplification")
				,null,null,null).save();
		

		newExperimentTypeNode("rrna-depletion",getExperimentTypes("rrna-depletion").get(0),false, false,false,
				getETForRrnaDepletion()
				,null,null,null).save();
	}
	
	private List<ExperimentTypeNode> getETForRrnaDepletion(){
		List<ExperimentTypeNode> pets = ExperimentType.find.findActiveByCategoryCode("transformation")
			.stream()
			.filter(e -> !e.code.contains("depot"))
			.map(et -> getExperimentTypeNodes(et.code).get(0))
			.collect(Collectors.toList());
		
		pets.add(getExperimentTypeNodes("ext-to-purif-qc-transfert").get(0));
		pets.add(getExperimentTypeNodes("ext-to-dna-sample-valuation").get(0));
		pets.add(getExperimentTypeNodes("ext-to-rna-sample-valuation").get(0));
		pets.add(getExperimentTypeNodes("ext-to-amplicon-sample-valuation").get(0));
		
		return pets;		
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

	private List<ProcessExperimentType> getPETForPurifQCTransfert(){
		List<ProcessExperimentType> pets = ExperimentType.find.findByCategoryCode("transformation")
			.stream()
			.map(et -> getPET(et.code, -1))
			.collect(Collectors.toList());
		pets.add(getPET("ext-to-purif-qc-transfert",-1));
		pets.add(getPET("rrna-depletion",0));
		return pets;		
	}

}
