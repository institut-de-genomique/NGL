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
import models.laboratory.common.description.dao.MeasureUnitDAO;
import models.laboratory.experiment.description.ExperimentCategory;
import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.experiment.description.dao.ExperimentCategoryDAO;
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
		ExperimentCategoryDAO ecfind = ExperimentCategory.find.get();

		//purif
		l.add(newExperimentType("Traitement DNAse","dnase-treatment",null, 30100,
				ecfind.findByCode(ExperimentCategory.CODE.purification.name()), getPropertyDefinitionsDNAseTreatment(),
				getInstrumentUsedTypes("hand"),"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(newExperimentType("Déplétion ARN ribo","rrna-depletion",null, 30200,
				ecfind.findByCode(ExperimentCategory.CODE.purification.name()), getPropertyDefinitionsRRNADepletion(),
				getInstrumentUsedTypes("hand"),"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(newExperimentType("Ampure","ampure",null, 30300,
				ecfind.findByCode(ExperimentCategory.CODE.purification.name()), getPropertyDefinitionsPostAmpurePCR(),
				getInstrumentUsedTypes("hand","biomek-fx"),"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));	
		
		l.add(newExperimentType("Ampure Post-PCR","post-pcr-ampure",null, 30400,
				ecfind.findByCode(ExperimentCategory.CODE.purification.name()), getPropertyDefinitionsPostAmpurePCR(),
				getInstrumentUsedTypes("hand","biomek-fx"),"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));	
		
		l.add(newExperimentType("Purification sur colonne","spin-column-purification",null, 30500,
				ecfind.findByCode(ExperimentCategory.CODE.purification.name()), getPropertyDefinitionsSpinColumnPurification(),
				getInstrumentUsedTypes("hand"),"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));	
		
		l.add(newExperimentType("Débranchage","wga-debranching",null, 30600,
				ecfind.findByCode(ExperimentCategory.CODE.purification.name()), getPropertyDefinitionsWGADebranching(),
				getInstrumentUsedTypes("hand"),"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));	
		
		l.add(newExperimentType("Ext to Purif / eval / TF","ext-to-purif-qc-transfert",null,-1,
				ecfind.findByCode(ExperimentCategory.CODE.voidprocess.name()), null, null,"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		return l;
		
	}
	
	
	private List<PropertyDefinition> getPropertyDefinitionsWGADebranching() {
		MeasureUnitDAO     mufind = MeasureUnit.find.get();
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();

		propertyDefinitions.add(newPropertiesDefinition("Volume engagé", "inputVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, 
				null, MeasureCategory.find.get().findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),mufind.findByCode( "µL"),mufind.findByCode( "µL"),"single", 12, true, null,"1"));
		
		propertyDefinitions.add(newPropertiesDefinition("Quantité engagée","inputQuantity", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, true, null,
				null,MeasureCategory.find.get().findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),mufind.findByCode( "ng"),mufind.findByCode( "ng"),"single",13, true,null,"1"));
			

		propertyDefinitions.add(newPropertiesDefinition("Nombre de débranchages", "nbDebranching", LevelService.getLevels(Level.CODE.ContainerIn), Integer.class, true, null, 
				null, null, null, null,"single", 14, true, null, null));
	
		
		propertyDefinitions.add(newPropertiesDefinition("Ratio billes Ampure", "adnBeadVolumeRatio", LevelService.getLevels(Level.CODE.ContainerIn), String.class, false, null, 
				null, null, null, null,"single", 15, true, null, null));

		return propertyDefinitions;
	}

	private List<PropertyDefinition> getPropertyDefinitionsDNAseTreatment() {
		MeasureUnitDAO     mufind = MeasureUnit.find.get();
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();

		propertyDefinitions.add(newPropertiesDefinition("Volume engagé", "inputVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, 
				null, MeasureCategory.find.get().findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),mufind.findByCode( "µL"),mufind.findByCode( "µL"),"single", 12, true, null,"1"));
		propertyDefinitions.add(newPropertiesDefinition("Quantité engagée","inputQuantity", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, false, null,
				null,MeasureCategory.find.get().findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),mufind.findByCode( "ng"),mufind.findByCode( "ng"),"single",13, true,null,"1"));
		
		propertyDefinitions.add(newPropertiesDefinition("Traitement DNAse ?","dnaseTreatment", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content),Boolean.class, true, null,
				null,"single",14, true,"true",null));
	
	
		return propertyDefinitions;
	}
	private List<PropertyDefinition> getPropertyDefinitionsRRNADepletion() {
		MeasureUnitDAO     mufind = MeasureUnit.find.get();
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();

		propertyDefinitions.add(newPropertiesDefinition("Volume engagé", "inputVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, 
				null, MeasureCategory.find.get().findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),mufind.findByCode( "µL"),mufind.findByCode( "µL"),"single", 12, true, null,"1"));
		propertyDefinitions.add(newPropertiesDefinition("Quantité engagée","inputQuantity", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, false, null,
				null,MeasureCategory.find.get().findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),mufind.findByCode( "ng"),mufind.findByCode( "ng"),"single",13, true,null,"1"));
		
		return propertyDefinitions;
	}
	
	
	private List<PropertyDefinition> getPropertyDefinitionsPostAmpurePCR() {
		MeasureUnitDAO     mufind = MeasureUnit.find.get();
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(newPropertiesDefinition("Ratio billes Ampure", "adnBeadVolumeRatio", LevelService.getLevels(Level.CODE.Experiment), String.class, true, null, 
				null, null, null, null,"single", 2, true, null, null));
	
		propertyDefinitions.add(newPropertiesDefinition("Volume engagé", "inputVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, 
				null, MeasureCategory.find.get().findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),mufind.findByCode( "µL"),mufind.findByCode( "µL"),"single", 12, true, null,"1"));
		
		return propertyDefinitions;
	}

	
	private List<PropertyDefinition> getPropertyDefinitionsSpinColumnPurification() {
		MeasureUnitDAO     mufind = MeasureUnit.find.get();
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();

		propertyDefinitions.add(newPropertiesDefinition("Volume engagé", "inputVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, 
				null, MeasureCategory.find.get().findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),mufind.findByCode( "µL"),mufind.findByCode( "µL"),"single", 12, true, null,"1"));
		
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
		return null;
	}

	@Override
	protected List<ProcessType> getProcessTypeCommon() {
		List<ProcessType> l = new ArrayList<>();
		l.add(DescriptionFactory.newProcessType("Purif puis satellites", "purif-qc-transfert", 
				ProcessCategory.find.get().findByCode("satellites"), 1020,
				null, 
				getPETForPurifQCTransfert(), 
				getExperimentTypes("rrna-depletion").get(0), getExperimentTypes("ext-to-purif-qc-transfert").get(0), getExperimentTypes("ext-to-purif-qc-transfert").get(0), 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		return l;
	}

	
	@Override
	protected List<ProcessType> getProcessTypeDEV() {
		return null;
	}

	@Override
	protected List<ProcessType> getProcessTypePROD() {
		return null;
	}

	@Override
	protected List<ProcessType> getProcessTypeUAT() {
		return null;
	}

	@Override
	protected void getExperimentTypeNodeCommon() {
		save(newExperimentTypeNode("ext-to-purif-qc-transfert", AbstractExperimentService.getExperimentTypes("ext-to-purif-qc-transfert").get(0), false, false, false, 
				null, getExperimentTypes("dnase-treatment","rrna-depletion"), getExperimentTypes("fluo-quantification","chip-migration"),getExperimentTypes("pool","tubes-to-plate","plate-to-tubes")));		
	
		
		//GA 07/11/2016 USED FOR PROCESS who start with ampure
		/*GA 20/06/2018 Do not more used
		newExperimentTypeNode("post-pcr-ampure",getExperimentTypes("post-pcr-ampure").get(0),false, false,false,
				getExperimentTypeNodes("pcr-amplification-and-purification","indexing-and-pcr-amplification")
				,null,null,null));
		*/
		//GA 20/06/2018 need to set ext-to to a next node to have good container state (IW-P) after dispatch with terminate for satellite processes
		save(newExperimentTypeNode("rrna-depletion",getExperimentTypes("rrna-depletion").get(0),false, false,false,
				getETForRrnaDepletion()
				,null,null,null));
		
	}
	
	private List<ExperimentTypeNode> getETForRrnaDepletion(){
		List<ExperimentTypeNode> pets = new ArrayList<>();
		pets.add(getExperimentTypeNodes("ext-to-purif-qc-transfert").get(0));
		
		return pets;		
	}
	
	@Override
	protected void getExperimentTypeNodeDEV() {
	}

	@Override
	protected void getExperimentTypeNodePROD() {
	}

	@Override
	protected void getExperimentTypeNodeUAT() {
	}

	private List<ProcessExperimentType> getPETForPurifQCTransfert(){
		List<ProcessExperimentType> pets = ExperimentType.find.get().findByCategoryCode("transformation")
			.stream()
			.map(et -> getPET(et.code, -1))
			.collect(Collectors.toList());
		pets.add(getPET("ext-to-purif-qc-transfert",-1));
		//pets.add(getPET("rrna-depletion",0));
		return pets;		
	}

}
