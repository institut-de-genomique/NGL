package services.description.declaration.cns;

import static services.description.DescriptionFactory.newExperimentType;
import static services.description.DescriptionFactory.newExperimentTypeNode;
import static services.description.DescriptionFactory.newPropertiesDefinition;
import static services.description.DescriptionFactory.newValue;

import java.util.ArrayList;
import java.util.Arrays;
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
import models.utils.dao.DAOException;
import services.description.Constants;
import services.description.DescriptionFactory;
import services.description.common.LevelService;
import services.description.common.MeasureService;
import services.description.declaration.AbstractDeclaration;
import services.description.experiment.AbstractExperimentService;

public class Transfert extends AbstractDeclaration {

	@Override
	protected List<ExperimentType> getExperimentTypeCommon() {
		List<ExperimentType> l = new ArrayList<>();

		l.add(newExperimentType("Aliquot","aliquoting",null,10100,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transfert.name()),
				getPropertyAliquoting(), getInstrumentUsedTypes("hand"),"OneToMany", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(newExperimentType("Pool Tube","pool-tube",null,10200,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transfert.name()), getPropertyDefinitionPoolTube(),
				getInstrumentUsedTypes("hand","tecan-evo-100"),"ManyToOne", false,
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		
		l.add(newExperimentType("Pool générique","pool",null,10300,
			ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transfert.name()), getPropertyDefinitionPoolTube(),
			getInstrumentUsedTypes("tecan-evo-100", "biomek-fx", "hand"),"ManyToOne", 
			DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(newExperimentType("Normalisation","normalisation",null,10400,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transfert.name()), getPropertyDefinitionNormalisation(),
				getInstrumentUsedTypes("biomek-fx","brand-lhs","hand"),"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(newExperimentType("Dilution","dilution",null,10450,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transfert.name()), getPropertyDefinitionDilution(),
				getInstrumentUsedTypes("biomek-fx","hand"),"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		
		l.add(newExperimentType("Tubes -> Plaque","tubes-to-plate",null,10500,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transfert.name()), null,
				getInstrumentUsedTypes("hand","tecan-evo-100","biomek-fx"),"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(newExperimentType("Plaque -> Tubes","plate-to-tubes",null,10600,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transfert.name()), null,
				getInstrumentUsedTypes("hand","tecan-evo-100","biomek-fx"),"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(newExperimentType("Plaques -> Plaque","plates-to-plate",null,10700,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transfert.name()), null,
				getInstrumentUsedTypes("hand","tecan-evo-100","biomek-fx"),"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(newExperimentType("Tubes / Plaques -> Plaque","x-to-plate",null,10700,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transfert.name()),  getPropertyDefinitionPlateTubeToPlate(),
				getInstrumentUsedTypes("hand","tecan-evo-100","biomek-fx"),"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(newExperimentType("Ext to TF / Eval / purif","ext-to-transfert-qc-purif",null,-1,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null, null,"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		
		return l;
	}
	
	@Override
	protected List<ExperimentType> getExperimentTypeDEV() {
		return null;
	}

	@Override
	protected List<ExperimentType> getExperimentTypePROD() {
		List<ExperimentType> l = new ArrayList<>();

		return l;
	}

	@Override
	protected List<ExperimentType> getExperimentTypeUAT() {
		return null;
	}

	@Override
	protected void getExperimentTypeNodeCommon() {
		// TODO Auto-generated method stub
		newExperimentTypeNode("ext-to-transfert-qc-purif", AbstractExperimentService.getExperimentTypes("ext-to-transfert-qc-purif").get(0), false, false, false, 
				null, getExperimentTypes("dnase-treatment","rrna-depletion"), getExperimentTypes("aliquoting"),getExperimentTypes("fluo-quantification")).save();		
		
		newExperimentTypeNode("tubes-to-plate",getExperimentTypes("tubes-to-plate").get(0),false, false,false,
				getETForTubeToPlaque()
				,null,null,null).save();
	}
	
	private List<ExperimentTypeNode> getETForTubeToPlaque(){
		List<ExperimentTypeNode> pets = ExperimentType.find.findActiveByCategoryCode("transformation")
			.stream()
			.filter(e -> !e.code.contains("depot"))
			.map(et -> getExperimentTypeNodes(et.code).get(0))
			.collect(Collectors.toList());
		
		pets.add(getExperimentTypeNodes("ext-to-transfert-qc-purif").get(0));
		pets.add(getExperimentTypeNodes("ext-to-dna-sample-valuation").get(0));
		pets.add(getExperimentTypeNodes("ext-to-rna-sample-valuation").get(0));
		pets.add(getExperimentTypeNodes("ext-to-amplicon-sample-valuation").get(0));
		
		return pets;		
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
	protected List<ProcessType> getProcessTypeCommon() {
		List<ProcessType> l = new ArrayList<>();
		
		
		l.add(DescriptionFactory.newProcessType("Transfert puis satellites", "transfert-qc-purif", 
				ProcessCategory.find.findByCode("satellites"), 1020,
				null, 
				getPETForTransfertQCPurif(), 
				getExperimentTypes("tubes-to-plate").get(0), getExperimentTypes("ext-to-transfert-qc-purif").get(0), getExperimentTypes("ext-to-transfert-qc-purif").get(0), 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		// TODO Auto-generated method stub
		return l;
	}
	
	@Override
	protected void getExperimentTypeNodeDEV() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void getExperimentTypeNodePROD() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void getExperimentTypeNodeUAT() {
		// TODO Auto-generated method stub
		
	}

	private List<ProcessExperimentType> getPETForTransfertQCPurif(){
		List<ProcessExperimentType> pets = ExperimentType.find.findByCategoryCode("transformation")
			.stream()
			.map(et -> getPET(et.code, -1))
			.collect(Collectors.toList());
		pets.add(getPET("ext-to-transfert-qc-purif",-1));
		pets.add(getPET("tubes-to-plate",0));
		return pets;		
	}
	
	
	private static List<PropertyDefinition> getPropertyDefinitionNormalisation() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		//InputContainer
		propertyDefinitions.add(newPropertiesDefinition("Label de travail", "workName", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Container), String.class, false, null, null, 
				"single", 100, true, null,null));
		
		propertyDefinitions.add(newPropertiesDefinition("Conc. max theorique", "maximumConcentration", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, 
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),null,null,"single", 18, false, null,null));
		
		propertyDefinitions.add(newPropertiesDefinition("Mode calcul", "computeMode", LevelService.getLevels(Level.CODE.ContainerIn), String.class, true, null, 
				Arrays.asList(newValue("fixeCfVi", "Volume à engager fixe"), newValue("fixeCfVf", "Volume final fixe")), "single", 19, true, null,null));
		
		propertyDefinitions.add(newPropertiesDefinition("Volume engagé", "inputVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, 
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single", 20, true, null,null));
		
		propertyDefinitions.add(newPropertiesDefinition("Volume tampon", "bufferVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, 
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single", 21, false, null,null));
		
		

		return propertyDefinitions;
	}
	
	private static List<PropertyDefinition> getPropertyDefinitionPlateTubeToPlate() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		//InputContainer
		propertyDefinitions.add(newPropertiesDefinition("Label de travail", "workName", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Container), String.class, false, "F", null, 
				"single", 100, true, null,null));
		
		return propertyDefinitions;
	}
	
	
	private static List<PropertyDefinition> getPropertyDefinitionDilution() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		//InputContainer
		propertyDefinitions.add(newPropertiesDefinition("Label de travail", "workName", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Container), String.class, true, "F", null, 
				"single", 100, true, null,null));
		
		propertyDefinitions.add(newPropertiesDefinition("Volume engagé", "inputVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, 
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single", 20, true, null,null));
		
		propertyDefinitions.add(newPropertiesDefinition("Facteur de dilution (1/X)","dilutionFactor", LevelService.getLevels(Level.CODE.ContainerIn), String.class, true, null, 
				null,"single", 21, true, null, null));
		
		propertyDefinitions.add(newPropertiesDefinition("Volume tampon", "bufferVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, 
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single", 22, false, null,null));
		
		

		return propertyDefinitions;
	}
	
	private static List<PropertyDefinition> getPropertyAliquoting() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(newPropertiesDefinition("Volume engagé","inputVolume", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, true, null
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"), "single",10, false));
		
		return propertyDefinitions;
	}
	
	private static List<PropertyDefinition> getPropertyDefinitionPoolTube() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		//InputContainer
		
		propertyDefinitions.add(newPropertiesDefinition("Volume engagé", "inputVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, 
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single", 20, true, null,null));
		
		propertyDefinitions.add(newPropertiesDefinition("Volume tampon", "bufferVolume", LevelService.getLevels(Level.CODE.ContainerOut), Double.class, false, null, 
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single", 25, true, null,null));
		
		propertyDefinitions.add(newPropertiesDefinition("Label de travail", "workName", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Container), String.class, false, null, null, 
				"single", 30, true, null,null));
		
	/*	propertyDefinitions.add(newPropertiesDefinition("Volume tampon à rajouter", "bufferVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null,
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",9, false)); */
		//Outputcontainer
		/*	propertyDefinitions.add(newPropertiesDefinition("Volume final", "finalVolume", LevelService.getLevels(Level.CODE.ContainerOut), Double.class, true, null
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"), "single",13,true));
		propertyDefinitions.add(newPropertiesDefinition("Concentration finale", "finalConcentration", LevelService.getLevels(Level.CODE.ContainerOut), Double.class, false, null
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "nM"),MeasureUnit.find.findByCode( "nM"),"single",14,true));	*/	
		
		return propertyDefinitions;
	}

	

		
}
