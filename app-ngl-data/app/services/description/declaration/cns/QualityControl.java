package services.description.declaration.cns;

import static services.description.DescriptionFactory.newExperimentType;
import static services.description.DescriptionFactory.newExperimentTypeNode;
import static services.description.DescriptionFactory.newPropertiesDefinition;
import static services.description.DescriptionFactory.newValues;

import java.awt.Image;
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


public class QualityControl extends AbstractDeclaration {

	@Override
	protected List<ExperimentType> getExperimentTypeCommon() {
		List<ExperimentType> l = new ArrayList<>();
		
		l.add(newExperimentType("Dosage fluo (éval échantillons à réception)","reception-fluo-quantification", null,20050,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.qualitycontrol.name()), getPropertyDefinitionsDosageFluorometriqueEval(), 
				getInstrumentUsedTypes("qubit","fluoroskan"),"OneToVoid", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS))); 
		
		l.add(newExperimentType("Dosage fluorométrique","fluo-quantification", null,20100,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.qualitycontrol.name()), getPropertyDefinitionsDosageFluorometrique(), 
				getInstrumentUsedTypes("qubit","fluoroskan"),"OneToVoid", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS))); 
		
		l.add(newExperimentType("Migration sur gel","gel-migration", null,20200,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.qualitycontrol.name()), getPropertyDefinitionsGelMigration(), 
				getInstrumentUsedTypes("hand"),"OneToVoid", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS))); 
		
		l.add(newExperimentType("Migration sur puce (eval ARN ou cDNA)","chip-migration-rna-evaluation", null,20300,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.qualitycontrol.name()), getPropertyDefinitionsChipMigrationARNEvaluation(), 
				getInstrumentUsedTypes("agilent-2100-bioanalyzer"),"OneToVoid", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS))); 
		
		
		l.add(newExperimentType("Migration sur puce (hors eval ARN)","chip-migration", null,20350,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.qualitycontrol.name()), getPropertyDefinitionsChipMigration(), 
				getInstrumentUsedTypes("agilent-2100-bioanalyzer", "labchip-gx","tapestation"),"OneToVoid", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS))); 
		
		
		l.add(newExperimentType("PCR + gel","control-pcr-and-gel", null,20400,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.qualitycontrol.name()), getPropertyDefinitionsPCRGel(), 
				getInstrumentUsedTypes("thermocycler"),"OneToVoid", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS))); 
	
		l.add(newExperimentType("Quantification qPCR","qpcr-quantification", null,20500,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.qualitycontrol.name()), getPropertyDefinitionsQPCR(), 
				getInstrumentUsedTypes("tecan-evo-100-and-stratagene-qPCR-system"),"OneToVoid", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
			
		l.add(newExperimentType("Electrophorèse en champ pulsé","pulsed-field-electrophoresis", null,20200,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.qualitycontrol.name()), getPropertyDefinitionsPulsedFieldElectrophoresis(), 
				getInstrumentUsedTypes("pippin-pulse"),"OneToVoid", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS))); 
	
		
		l.add(newExperimentType("Ext to Eval / TF / purif","ext-to-qc-transfert-purif",null,-1,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null, null,"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

		
		l.add(newExperimentType("Ext to Eval ADN à réception","ext-to-dna-sample-valuation",null,-1,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null, null,"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

		l.add(newExperimentType("Ext to Eval ARN à réception","ext-to-rna-sample-valuation",null,-1,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null, null,"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

		l.add(newExperimentType("Ext to Eval Amplicon à réception","ext-to-amplicon-sample-valuation",null,-1,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null, null,"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

		l.add(newExperimentType("Ext to Evaluation ADN HPM", "ext-to-hmw-dna-sample-valuation",null,-1,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null, null,"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

		
		l.add(newExperimentType("Spectrophotométrie UV","uv-spectrophotometry", null,22000,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.qualitycontrol.name()), getPropertyDefinitionsUvQuantification(), 
				getInstrumentUsedTypes("nanodrop"),"OneToVoid", true, 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(newExperimentType("Qcard","qcard", null,22000,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.qualitycontrol.name()), getPropertyDefinitionsQcard(), 
				getInstrumentUsedTypes("hand"),"OneToVoid", true, 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		
		
		//QC provenant de collaborateur extérieur.
		l.add(newExperimentType("QC Exterieur","external-qc", null,22000,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.qualitycontrol.name()), getPropertyDefinitionsExternalQC(), 
				getInstrumentUsedTypes("hand"),"OneToVoid", false, 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		
		return l;
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

	/* (non-Javadoc)
	 * @see services.description.declaration.AbstractDeclaration#getProcessTypeCommon()
	 */
	@Override
	protected List<ProcessType> getProcessTypeCommon() {
		List<ProcessType> l = new ArrayList<>();
		
		l.add(DescriptionFactory.newProcessType("QC puis satellites", "qc-transfert-purif", 
				ProcessCategory.find.findByCode("satellites"), 1020,
				null, 
				getPETForQCTransfertPurif(), 
				getExperimentTypes("fluo-quantification").get(0), getExperimentTypes("ext-to-qc-transfert-purif").get(0), getExperimentTypes("ext-to-qc-transfert-purif").get(0), 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(DescriptionFactory.newProcessType("Evaluation ADN à réception", "dna-sample-valuation", 
				ProcessCategory.find.findByCode("sample-valuation"), 1010,
				getPropertyDefinitionsEvalAReception(), 
				Arrays.asList(getPET("ext-to-dna-sample-valuation",-1),
						 getPET("dna-rna-extraction",-1),
						 getPET("fluo-quantification",0)), 
				getExperimentTypes("fluo-quantification").get(0), getExperimentTypes("ext-to-dna-sample-valuation").get(0), getExperimentTypes("ext-to-dna-sample-valuation").get(0), 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(DescriptionFactory.newProcessType("Evaluation ARN à réception", "rna-sample-valuation", 
				ProcessCategory.find.findByCode("sample-valuation"), 1011,
				getPropertyDefinitionsEvalAReception(), 
				Arrays.asList(getPET("ext-to-rna-sample-valuation",-1),
						 getPET("dna-rna-extraction",-1),
						 getPET("fluo-quantification",0)),  
				getExperimentTypes("fluo-quantification").get(0), getExperimentTypes("ext-to-rna-sample-valuation").get(0), getExperimentTypes("ext-to-rna-sample-valuation").get(0), 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(DescriptionFactory.newProcessType("Evaluation Amplicon à réception", "amplicon-sample-valuation", 
				ProcessCategory.find.findByCode("sample-valuation"), 1012,
				getPropertyDefinitionsEvalAReception(), 
				Arrays.asList(getPET("ext-to-amplicon-sample-valuation",-1)), 
				getExperimentTypes("fluo-quantification").get(0), getExperimentTypes("ext-to-amplicon-sample-valuation").get(0), getExperimentTypes("ext-to-amplicon-sample-valuation").get(0), 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(DescriptionFactory.newProcessType("Evaluation ADN HPM", "hmw-dna-sample-valuation", 
				ProcessCategory.find.findByCode("sample-valuation"), 1013,
				getPropertyDefinitionsEvalAReception(), 
				Arrays.asList(getPET("ext-to-hmw-dna-sample-valuation",-1)), 
				getExperimentTypes("fluo-quantification").get(0), getExperimentTypes("ext-to-hmw-dna-sample-valuation").get(0), getExperimentTypes("ext-to-hmw-dna-sample-valuation").get(0), 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
			
		return l;
	}
	
	
	private List<ProcessExperimentType> getPETForQCTransfertPurif(){
		List<ProcessExperimentType> pets = ExperimentType.find.findByCategoryCode("transformation")
			.stream()
			.map(et -> getPET(et.code, -1))
			.collect(Collectors.toList());
		pets.add(getPET("ext-to-qc-transfert-purif",-1));
		pets.add(getPET("fluo-quantification",0));
		return pets;		
	}
	
	
	@Override
	protected List<ProcessType> getProcessTypeDEV() {
		List<ProcessType> l = new ArrayList<>();
		
		
		return l;
	}

	@Override
	protected List<ProcessType> getProcessTypePROD() {
//		List<ProcessType> l = new ArrayList<ProcessType>();
		return null;
	}

	@Override
	protected List<ProcessType> getProcessTypeUAT() {
		return null;
	}

	@Override
	protected void getExperimentTypeNodeCommon() {
		newExperimentTypeNode("ext-to-qc-transfert-purif", AbstractExperimentService.getExperimentTypes("ext-to-qc-transfert-purif").get(0), false, false, false, 
				null, getExperimentTypes("dnase-treatment","rrna-depletion"), getExperimentTypes("fluo-quantification","chip-migration"),getExperimentTypes("pool","tubes-to-plate","plate-to-tubes")).save();		
		
			
		newExperimentTypeNode("ext-to-dna-sample-valuation", AbstractExperimentService.getExperimentTypes("ext-to-dna-sample-valuation").get(0), false, false, false, 
				null, getExperimentTypes("dnase-treatment","rrna-depletion"), getExperimentTypes("fluo-quantification","chip-migration"),getExperimentTypes("pool","tubes-to-plate","plate-to-tubes")).save();		
		newExperimentTypeNode("ext-to-rna-sample-valuation", AbstractExperimentService.getExperimentTypes("ext-to-rna-sample-valuation").get(0), false, false, false, 
				null, getExperimentTypes("dnase-treatment","rrna-depletion"), getExperimentTypes("fluo-quantification","chip-migration"),getExperimentTypes("pool","tubes-to-plate","plate-to-tubes")).save();		
		newExperimentTypeNode("ext-to-amplicon-sample-valuation", AbstractExperimentService.getExperimentTypes("ext-to-amplicon-sample-valuation").get(0), false, false, false, 
				null, getExperimentTypes("dnase-treatment","rrna-depletion"), getExperimentTypes("fluo-quantification","chip-migration"),getExperimentTypes("pool","tubes-to-plate","plate-to-tubes")).save();		
	
		newExperimentTypeNode("ext-to-hmw-dna-sample-valuation", AbstractExperimentService.getExperimentTypes("ext-to-hmw-dna-sample-valuation").get(0), false, false, false, 
				null, getExperimentTypes("dnase-treatment","rrna-depletion"), getExperimentTypes("fluo-quantification","chip-migration"),getExperimentTypes("pool","tubes-to-plate","plate-to-tubes")).save();		
		
		//GA 07/11/2016 USED FOR PROCESS who start with ampure
		newExperimentTypeNode("fluo-quantification",getExperimentTypes("fluo-quantification").get(0),false, false,false,
				getETForFluoQuantification()
				,null,null,null).save();
	}
	
	private List<ExperimentTypeNode> getETForFluoQuantification(){
		List<ExperimentTypeNode> pets = ExperimentType.find.findActiveByCategoryCode("transformation")
			.stream()
			.filter(e -> !e.code.contains("depot"))
			.map(et -> getExperimentTypeNodes(et.code).get(0))
			.collect(Collectors.toList());
		
		pets.add(getExperimentTypeNodes("ext-to-qc-transfert-purif").get(0));
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
	}

	@Override
	protected void getExperimentTypeNodeUAT() {
	}
	
	private List<PropertyDefinition> getPropertyDefinitionsEvalAReception() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(newPropertiesDefinition("Type(s) banque(s) à réaliser", "libraryToDo", LevelService.getLevels(Level.CODE.Process), String.class, false, null, 
				null, null, null, null,"single", 18, true, null,null));	
		return propertyDefinitions;
	}	
		
	private List<PropertyDefinition> getPropertyDefinitionsExternalQC() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();

		propertyDefinitions.add(newPropertiesDefinition("Volume fourni", "providedVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, 
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single", 11, true, null,null));
		propertyDefinitions.add(newPropertiesDefinition("Concentration fournie", "providedConcentration", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, 
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "ng/µl"),MeasureUnit.find.findByCode("ng/µl"),"single", 13, true, null,null));
		propertyDefinitions.add(newPropertiesDefinition("Quantité fournie", "providedQuantity", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, 
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),MeasureUnit.find.findByCode( "ng"),MeasureUnit.find.findByCode("ng"),"single", 15, true, null,null));
		propertyDefinitions.add(newPropertiesDefinition("Taille fournie", "providedSize", LevelService.getLevels(Level.CODE.ContainerIn), Integer.class, false, null, 
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),MeasureUnit.find.findByCode( "pb"),MeasureUnit.find.findByCode("pb"),"single", 16, true, null,null));

		propertyDefinitions.add(newPropertiesDefinition("RIN", "providedRin", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, 
				null, "single", 17, false, null, null));

		propertyDefinitions.add(newPropertiesDefinition("Méthode quantification", "collabQuantificationMethod", LevelService.getLevels(Level.CODE.ContainerIn), String.class, false, null,
		        null, null, null, null,"single", 18, true, null,null));
		
		propertyDefinitions.add(newPropertiesDefinition("A260/A280 fourni", "providedA260A280Ratio", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null,
				null, null, null, null,"single", 19, true, null,null));
		propertyDefinitions.add(newPropertiesDefinition(" A260/A230 fourni", "providedA260A230Ratio", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null,
				null, null, null, null,"single", 20, true, null,null));
		propertyDefinitions.add(newPropertiesDefinition("Taille estimée des molécules", "providedMoleculeEstimatedSize", LevelService.getLevels(Level.CODE.ContainerIn),String.class, false, null,
				null, null, null, null,"single", 18, true, null,null));   
		
		//Remplacé par dnaTreatment niveau Sample dans import echantillon bio
		//propertyDefinitions.add(newPropertiesDefinition("Méthode d'amplification", "amplificationMethod", LevelService.getLevels(Level.CODE.ContainerIn), String.class, false, null,
			//	null, null, null, null,"single", 22, true, null,null));
		propertyDefinitions.add(newPropertiesDefinition("Commentaire", "comment", LevelService.getLevels(Level.CODE.ContainerIn), String.class, false, null, 
				null, null, null, null,"single", 18, true, null,null));
		return propertyDefinitions;
	}
	
	private List<PropertyDefinition> getPropertyDefinitionsDosageFluorometrique() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		
		propertyDefinitions.addAll(getPropertyDefinitionsDosageFluorometriqueEvalCommon());
		
		propertyDefinitions.add(newPropertiesDefinition("Volume mesuré", "preQuantificationVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, 
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single", 11, false, null,null));
		
		propertyDefinitions.add(newPropertiesDefinition("Conc. calculée en nM ", "nMcalculatedConcentration", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, 
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "nM"),MeasureUnit.find.findByCode("nM"),"single", 33, false, null,"2"));
	
		
		return propertyDefinitions;
		
	}

	private List<PropertyDefinition> getPropertyDefinitionsDosageFluorometriqueEval() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.addAll(getPropertyDefinitionsDosageFluorometriqueEvalCommon());
		
		propertyDefinitions.add(newPropertiesDefinition("Volume mesuré", "preQuantificationVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, 
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single", 11, true, null,null));
		
		return propertyDefinitions;
	}
	
	
	private List<PropertyDefinition> getPropertyDefinitionsDosageFluorometriqueEvalCommon() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		
		propertyDefinitions.add(newPropertiesDefinition("Choix visuel", "displayChoice", LevelService.getLevels(Level.CODE.Experiment), String.class, false, null, 
				DescriptionFactory.newValues("Tout", "BR1 + HS1 + HS2", "HS1 + HS2 + HS3"),null,null,null,"single", 10, true, "Tout", null));
		
		
		propertyDefinitions.add(newPropertiesDefinition("Volume engagé", "inputVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, 
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single", 12, true, null,null));		
		
		propertyDefinitions.add(newPropertiesDefinition("Dilution BR (1/X)","dilutionFactorBR1", LevelService.getLevels(Level.CODE.ContainerIn), String.class, false, null, null,"single", 13, true, null, null));
		propertyDefinitions.add(newPropertiesDefinition("Vol. engagé ds dosage BR", "inputVolumeBR1", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, 
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single", 14, true, null,null));		
		propertyDefinitions.add(newPropertiesDefinition("Dosage BR (sur dilution)", "concentrationDilBR1", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, 
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "ng/µl"),MeasureUnit.find.findByCode("ng/µl"),"single", 15, true, null,null));
		propertyDefinitions.add(newPropertiesDefinition("Dosage BR (réel)", "concentrationBR1", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, 
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "ng/µl"),MeasureUnit.find.findByCode("ng/µl"),"single", 16, false, null,null));
		
		propertyDefinitions.add(newPropertiesDefinition("Dilution HS1 (1/X)","dilutionFactorHS1", LevelService.getLevels(Level.CODE.ContainerIn), String.class, false, null, null,"single", 17, true, null, null));
		propertyDefinitions.add(newPropertiesDefinition("Vol. engagé ds dosage HS1", "inputVolumeHS1", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, 
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single", 18, true, null,null));	
		propertyDefinitions.add(newPropertiesDefinition("Dosage HS 1 (sur dilution)", "concentrationDilHS1", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, 
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "ng/µl"),MeasureUnit.find.findByCode("ng/µl"),"single", 19, true, null,null));
		propertyDefinitions.add(newPropertiesDefinition("Dosage HS 1 (réel)", "concentrationHS1", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, 
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "ng/µl"),MeasureUnit.find.findByCode("ng/µl"),"single", 20, false, null,"3"));
		
		propertyDefinitions.add(newPropertiesDefinition("Dilution HS2 (1/X)","dilutionFactorHS2", LevelService.getLevels(Level.CODE.ContainerIn), String.class, false, null, null,"single", 21, true, null, null));
		propertyDefinitions.add(newPropertiesDefinition("Vol. engagé ds dosage HS2", "inputVolumeHS2", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, 
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single", 22, true, null,null));	
		propertyDefinitions.add(newPropertiesDefinition("Dosage HS 2 (sur dilution)", "concentrationDilHS2", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, 
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "ng/µl"),MeasureUnit.find.findByCode("ng/µl"),"single", 23, true, null,null));
		propertyDefinitions.add(newPropertiesDefinition("Dosage HS 2 (réel)", "concentrationHS2", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, 
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "ng/µl"),MeasureUnit.find.findByCode("ng/µl"),"single", 24, false, null,"3"));
		
		propertyDefinitions.add(newPropertiesDefinition("Dilution HS3 (1/X)","dilutionFactorHS3", LevelService.getLevels(Level.CODE.ContainerIn), String.class, false, null, null,"single", 25, true, null, null));
		propertyDefinitions.add(newPropertiesDefinition("Vol. engagé ds dosage HS3", "inputVolumeHS3", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, 
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single", 26, true, null,null));	
		propertyDefinitions.add(newPropertiesDefinition("Dosage HS 3 (sur dilution)", "concentrationDilHS3", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, 
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "ng/µl"),MeasureUnit.find.findByCode("ng/µl"),"single", 27, true, null,null));
		propertyDefinitions.add(newPropertiesDefinition("Dosage HS 3 (réel)", "concentrationHS3", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, 
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "ng/µl"),MeasureUnit.find.findByCode("ng/µl"),"single", 28, false, null,"3"));
		
		
		propertyDefinitions.add(newPropertiesDefinition("Méthode de calcul de la concentration finale", "calculationMethod", LevelService.getLevels(Level.CODE.ContainerIn), String.class, true, "F", 
				DescriptionFactory.newValues("Moyenne HS1 HS2","Moyenne HS2 HS3","Moyenne HS1 HS3","Moyenne HS1 HS2 HS3","BR si > 25 et HS1 si BR <= 25","BR 1 seul","HS 1 seul","HS 2 seul","HS 3 seul","Non quantifiable"),null,null,null,"single", 29, true, null, null));
		
		propertyDefinitions.add(newPropertiesDefinition("Concentration finale", "concentration1", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, 
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "ng/µl"),MeasureUnit.find.findByCode("ng/µl"),"single", 30, false, null,"3"));
		propertyDefinitions.add(newPropertiesDefinition("Volume final", "volume1", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, "F", 
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode("µL"),"single", 31, true, null,null));
		propertyDefinitions.add(newPropertiesDefinition("Quantité finale", "quantity1", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, 
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),MeasureUnit.find.findByCode( "ng"),MeasureUnit.find.findByCode("ng"),"single", 32, false, null,null));
		
		return propertyDefinitions;
		
	}

	
	private List<PropertyDefinition> getPropertyDefinitionsGelMigration() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(newPropertiesDefinition("Volume engagé", "inputVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, 
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single", 12, true, null,null));
		
		propertyDefinitions.add(newPropertiesDefinition("Photo de gel 30 min.", "electrophoresisGelPhoto", LevelService.getLevels(Level.CODE.ContainerIn), Image.class, true, "F", null, 				
				"img", 13, true, null, null));
		
		propertyDefinitions.add(newPropertiesDefinition("Photo de gel 60 min.", "electrophoresisGelPhoto2", LevelService.getLevels(Level.CODE.ContainerIn), Image.class, false, null, null, 				
				"img", 14, true, null, null));
	
		propertyDefinitions.add(newPropertiesDefinition("% bas poids moléculaire", "lowMolecularWeightPercent", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, null, 				
				"single", 16, true, null, null));
		
		
		propertyDefinitions.add(newPropertiesDefinition("Volume sortie", "volume1", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, "F", 
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),
				"single", 17, true, null,null));
		return propertyDefinitions;
	}

	private List<PropertyDefinition> getPropertyDefinitionsPCRGel() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(newPropertiesDefinition("Volume engagé", "inputVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, 
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single", 12, true, null,null));
		
		propertyDefinitions.add(newPropertiesDefinition("Photo de gel", "electrophoresisGelPhoto", LevelService.getLevels(Level.CODE.ContainerIn), Image.class, true, "F", null, 				
				"img", 13, true, null, null));
		
		propertyDefinitions.add(newPropertiesDefinition("Volume sortie", "volume1", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, "F", 
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single", 14, true, null,null));
		
		propertyDefinitions.add(newPropertiesDefinition("Amorces", "amplificationPrimers", LevelService.getLevels(Level.CODE.Experiment), String.class, true, null, 
				null, null, null, null,"single", 1, true, null,null));

		propertyDefinitions.add(newPropertiesDefinition("Région ciblée","targetedRegion", LevelService.getLevels(Level.CODE.Experiment), String.class, true, null, 
				null, null, null, null,"single", 2, true, null,null));

		propertyDefinitions.add(newPropertiesDefinition("Nb cycles", "nbCycles", LevelService.getLevels(Level.CODE.Experiment), Integer.class, true, null, null, 
				"single", 3, true, null,null));		
		return propertyDefinitions;
	}

	public static List<PropertyDefinition> getPropertyDefinitionsChipMigrationARNEvaluation() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		// A supprimer une fois le type de support category sera géré		
		propertyDefinitions.add(newPropertiesDefinition("Volume engagé", "inputVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, null, 
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME), MeasureUnit.find.findByCode( "µL"), MeasureUnit.find.findByCode("µL"),
				"single", 11, true, null, "0"));		
	
		propertyDefinitions.add(newPropertiesDefinition("Profil de migration", "migrationProfile", LevelService.getLevels(Level.CODE.ContainerIn), Image.class, true, "F", null, 				
				"img", 14, true, null, null));
		
		
		propertyDefinitions.add(newPropertiesDefinition("RIN", "rin", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, null, 				
				"single", 15, true, null, null));
		
		propertyDefinitions.add(newPropertiesDefinition("Couleur éval.", "rnaEvaluation", LevelService.getLevels(Level.CODE.ContainerIn), String.class, false, null, 
				newValues("vert","jaune","orange","rouge"), "single", 16, true, null, null));
		
		
		
		propertyDefinitions.add(newPropertiesDefinition("Volume sortie", "volume1", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, "F", 
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single", 17, true, null,null));
		
		
		propertyDefinitions.add(newPropertiesDefinition("Concentration", "concentration1", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, null, 
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION), MeasureUnit.find.findByCode( "ng/µl"), MeasureUnit.find.findByCode("ng/µl"),
				"single", 18, true, null, "2"));
		
		propertyDefinitions.add(newPropertiesDefinition("Copier concentration dans concentration finale du container ?", "copyConcentration", LevelService.getLevels(Level.CODE.Experiment), Boolean.class, true, null,
				null, "single",15, true, "false", null));
		
		return propertyDefinitions;
	}
	
	
	public static List<PropertyDefinition> getPropertyDefinitionsChipMigration() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		// A supprimer une fois le type de support category sera géré
		
		propertyDefinitions.add(newPropertiesDefinition("Volume engagé", "inputVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, null, 
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME), MeasureUnit.find.findByCode( "µL"), MeasureUnit.find.findByCode("µL"),
				"single", 11, true, null, "0"));		
		
		propertyDefinitions.add(newPropertiesDefinition("Taille", "measuredSize", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, "F", null, 
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_SIZE), MeasureUnit.find.findByCode("pb"), MeasureUnit.find.findByCode("pb"),
				"single", 13, true, null, null));
		
		
		propertyDefinitions.add(newPropertiesDefinition("Taille 2", "measuredSize2", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, null, 
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_SIZE), MeasureUnit.find.findByCode("pb"), MeasureUnit.find.findByCode("pb"),
				"single", 14, true, null, null));
		
		propertyDefinitions.add(newPropertiesDefinition("Proportion pic 1", "ratioSize1", LevelService.getLevels(Level.CODE.ContainerIn), Integer.class, false, null, null, 
				"single", 15, true, null, null));
		
		
		propertyDefinitions.add(newPropertiesDefinition("Proportion pic 2", "ratioSize2", LevelService.getLevels(Level.CODE.ContainerIn), Integer.class, false, null, null, 
				"single", 16, true, null, null));
		
		propertyDefinitions.add(newPropertiesDefinition("Profil de migration", "migrationProfile", LevelService.getLevels(Level.CODE.ContainerIn), Image.class, true, "F", null, 				
				"img", 17, true, null, null));
		
		propertyDefinitions.add(newPropertiesDefinition("Volume sortie", "volume1", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, "F", 
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single", 18, true, null,null));
		
		propertyDefinitions.add(newPropertiesDefinition("Quantité", "quantity1", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, 
				null, null,null,null,"single", 19, false, null,null)); //not unit because can be different
		
		
		//Property compute in javascript
		propertyDefinitions.add(newPropertiesDefinition("Conc. calculée en nM", "nMcalculatedConcentration", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, 
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "nm"),MeasureUnit.find.findByCode("nm"),"single", 20, false, null,"2"));
	
		
		propertyDefinitions.add(newPropertiesDefinition("Layout Nominal Length","libLayoutNominalLength", LevelService.getLevels(Level.CODE.ContainerIn, Level.CODE.Content), Integer.class, false, null, 
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_SIZE),MeasureUnit.find.findByCode( "pb"),MeasureUnit.find.findByCode( "pb"),"single", 21, false, null,null));
		
		propertyDefinitions.add(newPropertiesDefinition("Taille insert","insertSize", LevelService.getLevels(Level.CODE.ContainerIn, Level.CODE.Content), Integer.class, false, null, 
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_SIZE),MeasureUnit.find.findByCode( "pb"),MeasureUnit.find.findByCode( "pb"),"single", 25, false, null,null));

		return propertyDefinitions;
	}
	
	private List<PropertyDefinition> getPropertyDefinitionsQPCR() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		
		propertyDefinitions.add(newPropertiesDefinition("Concentration", "concentration1", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, "F", null, 
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION), MeasureUnit.find.findByCode( "nM"), MeasureUnit.find.findByCode("nM"),
				"single", 17, true, null, "2"));		
		
		propertyDefinitions.add(newPropertiesDefinition("Concentration", "concentration2", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, "F", null, 
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION), MeasureUnit.find.findByCode("ng/µl"), MeasureUnit.find.findByCode("ng/µl"),
				"single", 18, true, null, "2"));		
		propertyDefinitions.add(newPropertiesDefinition("Volume sortie", "volume1", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, "F", 
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode("µL"),
				"single", 19, true, null,null));
		
		return propertyDefinitions;
	}

	private List<PropertyDefinition> getPropertyDefinitionsQcard() {
			List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
			
			propertyDefinitions.add(newPropertiesDefinition("Volume engagé", "inputVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, 
					null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single", 15, true, null,null));
			

			/*propertyDefinitions.add(newPropertiesDefinition("Dilution 1 - facteur (1/X)",	"dilutionFactor1", LevelService.getLevels(Level.CODE.ContainerIn), Integer.class, false, null, null, 				
					"single", 16, true, null, null));

			propertyDefinitions.add(newPropertiesDefinition("Dilution 2 - facteur (1/X)",	"dilutionFactor2", LevelService.getLevels(Level.CODE.ContainerIn), Integer.class, false, null, null, 				
					"single", 9, true, null, null));

			propertyDefinitions.add(newPropertiesDefinition("Dilution 3 - facteur (1/X)",	"dilutionFactor3", LevelService.getLevels(Level.CODE.ContainerIn), Integer.class, false, null, null, 				
					"single", 10, true, null, null));
			*/
			propertyDefinitions.add(newPropertiesDefinition("Profil Qcard", "qcardProfile1", LevelService.getLevels(Level.CODE.ContainerIn), Image.class, true, "F", null, 				
					"img", 19, true, null, null));
			
			propertyDefinitions.add(newPropertiesDefinition("Profil Qcard 2", "qcardProfile2", LevelService.getLevels(Level.CODE.ContainerIn), Image.class, false, null, null, 				
					"img", 20, true, null, null));
			/*
			propertyDefinitions.add(newPropertiesDefinition("Profil Qcard dil 3", "qcardProfile3", LevelService.getLevels(Level.CODE.ContainerIn), Image.class, false, null, null, 				
					"img", 15, true, null, null));
			
				*/	
			propertyDefinitions.add(newPropertiesDefinition("Taille estimée des molécules","moleculeEstimatedSize", LevelService.getLevels(Level.CODE.ContainerIn), String.class, true, "F", null, 				
					"single", 21, true, null, null));
			
			propertyDefinitions.add(newPropertiesDefinition("Volume sortie", "volume1", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, "F", 
					null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode("µL"),"single", 21, true, null,null));
			
		
		
		return propertyDefinitions;
	}

	private List<PropertyDefinition> getPropertyDefinitionsUvQuantification() {

		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();

		propertyDefinitions.add(newPropertiesDefinition("Volume mesuré", "preQuantificationVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, 
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single", 11, true, null,null));
		
		
		propertyDefinitions.add(newPropertiesDefinition("Volume engagé", "inputVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, 
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single", 13, true, null,null));
		

		propertyDefinitions.add(newPropertiesDefinition("Facteur de dilution (1/X)","dilutionFactor", LevelService.getLevels(Level.CODE.ContainerIn), String.class, false, null, null, 				
				"single", 14, true, null, null));

		propertyDefinitions.add(newPropertiesDefinition("Concentration dilution", "concentration0", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, 
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "ng/µl"),MeasureUnit.find.findByCode("ng/µl"),"single", 15, true, null,null));
		
		propertyDefinitions.add(newPropertiesDefinition("Concentration", "concentration1", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, 
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "ng/µl"),MeasureUnit.find.findByCode("ng/µl"),"single", 16, false, null,null));
		
		propertyDefinitions.add(newPropertiesDefinition("A260/A280",	"A260A280Ratio", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, null, 				
				"single", 17, true, null, null));
		
		propertyDefinitions.add(newPropertiesDefinition("A260/A230",	"A260A230Ratio", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, null, 				
				"single", 18, true, null, null));
		
		propertyDefinitions.add(newPropertiesDefinition("Spectre d'absorbance", "absorbanceVsWavelength", LevelService.getLevels(Level.CODE.ContainerIn), Image.class, false, null, null, 				
				"img", 19, true, null, null));
		
		propertyDefinitions.add(newPropertiesDefinition("Volume sortie", "volume1", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, "F", 
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode("µL"),"single", 20, true, null,null));
		
		propertyDefinitions.add(newPropertiesDefinition("Copier concentration dans concentration finale du container ?", "copyConcentration", LevelService.getLevels(Level.CODE.Experiment), Boolean.class, true, null,
				null, "single",15, true, "false", null));
		
		return propertyDefinitions;
	}
	
	
	private List<PropertyDefinition> getPropertyDefinitionsPulsedFieldElectrophoresis() {
		
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		
		propertyDefinitions.add(newPropertiesDefinition("Quantité engagée", "inputQuantity", LevelService.getLevels(Level.CODE.ContainerIn), Double.class,
				false, null,null,MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),MeasureUnit.find.findByCode( "ng"),MeasureUnit.find.findByCode("ng"),
				"single",11, true, null,null));
				
		propertyDefinitions.add(newPropertiesDefinition("Volume engagé", "inputVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class,
				false, null, null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),
				"single", 12, true, null,null));
		
		propertyDefinitions.add(newPropertiesDefinition("Volume EB", "bufferVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class,
				false, null, null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),
				"single", 13, true, null,null));
		
		propertyDefinitions.add(newPropertiesDefinition("Volume final aliquot", "aliquoteVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class,
				false, null, null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),
				"single", 14, true, null,null));
		
		 propertyDefinitions.add(newPropertiesDefinition("Photo de gel","electrophoresisGelPhoto", LevelService.getLevels(Level.CODE.ContainerIn),Image.class,
					 true, "F",null,"img",15,true,null,null));	
		 
		 propertyDefinitions.add(newPropertiesDefinition("Taille estimée des molécules","moleculeEstimatedSize",LevelService.getLevels(Level.CODE.ContainerIn), String.class, 
					true, "F",null, "single",16,true,null,null));	
	
		propertyDefinitions.add(newPropertiesDefinition("Volume en sortie", "volume1", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, 
				true, "F", null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),
				"single", 17, true, null,null));
		
		return propertyDefinitions;
	}

	
}
