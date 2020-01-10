package services.description.declaration.cns;

import static services.description.DescriptionFactory.newExperimentType;
import static services.description.DescriptionFactory.newExperimentTypeNode;
import static services.description.DescriptionFactory.newPropertiesDefinition;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import models.laboratory.common.description.Level;
import models.laboratory.common.description.MeasureCategory;
import models.laboratory.common.description.MeasureUnit;
import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.experiment.description.ExperimentCategory;
import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.processes.description.ProcessCategory;
import models.laboratory.processes.description.ProcessType;
import models.utils.dao.DAOException;
import services.description.Constants;
import services.description.DescriptionFactory;
import services.description.common.LevelService;
import services.description.common.MeasureService;
import services.description.declaration.AbstractDeclaration;

public class Bionano extends AbstractDeclaration{

	@Override
	protected List<ExperimentType> getExperimentTypeCommon() {
		List<ExperimentType> l = new ArrayList<>();

		//Bionano
		l.add(newExperimentType("Ext to NLRS / DLS, Bionano chip, dépôt","ext-to-bionano-nlrs-process",null,-1,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null, null,"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

		l.add(newExperimentType("Ext to Bionano Chip, dépôt","ext-to-bionano-chip-process",null,-1,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null, null,"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

		l.add(newExperimentType("Ext to Redépôt BioNano","ext-to-bionano-run",null,-1,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null, null,"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));


		l.add(newExperimentType("BIONANO Prep NLRS","irys-nlrs-prep",null,3100,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()), getPropertyDefinitionIrysPrepNLRS(),
				getInstrumentUsedTypes("hand"),"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS) ));

		l.add(newExperimentType("BIONANO Prep DLS","bionano-dls-prep",null,3150,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()), getPropertyDefinitionBionanoPrepDLS(),
				getInstrumentUsedTypes("hand"),"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS) ));

		
		l.add(newExperimentType("BIONANO Prep Chip","irys-chip-preparation",null,3200,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()), getPropertyDefinitionPreparationIrysChip(),
				getInstrumentUsedTypes("irys-hand"),"ManyToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS) ));

		l.add(newExperimentType("BIONANO Dépôt","bionano-depot",null,3300,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()), getPropertyDefinitionDepotBionano(),
				getInstrumentUsedTypes("IRYS","SAPHYR"),"OneToVoid", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS) ));

		return l;
	}
	
	@Override
	protected List<ExperimentType> getExperimentTypeDEV() {
		// TODO Auto-generated method stub
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
		List<ProcessType> l=new ArrayList<>();

		l.add(DescriptionFactory.newProcessType("NLRS / DLS, Bionano chip, dépôt", "bionano-nlrs-process", 
				ProcessCategory.find.findByCode("mapping"), 101, 
				getPropertyDefinitionsBionano(), 
				Arrays.asList(getPET("ext-to-bionano-nlrs-process",-1),
						getPET("irys-nlrs-prep",0),
						getPET("bionano-dls-prep",0),
						getPET("irys-chip-preparation",1),
						getPET("bionano-depot",2)), 
						getExperimentTypes("irys-nlrs-prep").get(0), getExperimentTypes("bionano-depot").get(0), getExperimentTypes("ext-to-bionano-nlrs-process").get(0), 
						DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

		l.add(DescriptionFactory.newProcessType("Bionano Chip, dépôt", "bionano-chip-process", 
				ProcessCategory.find.findByCode("mapping"), 102, 
				getPropertyDefinitionsBionano(),
				Arrays.asList(getPET("ext-to-bionano-chip-process",-1),
						getPET("irys-nlrs-prep",-1),
						getPET("bionano-dls-prep",-1),
						getPET("irys-chip-preparation",0),
						getPET("bionano-depot",1)), 
						getExperimentTypes("irys-chip-preparation").get(0), getExperimentTypes("bionano-depot").get(0), getExperimentTypes("ext-to-bionano-chip-process").get(0), 
						DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

		l.add(DescriptionFactory.newProcessType("Redépôt BioNano", "bionano-run", 
				ProcessCategory.find.findByCode("mapping"), 103, 
				getPropertyDefinitionsBionano(), 
				Arrays.asList(getPET("ext-to-bionano-run",-1), 
						getPET("irys-chip-preparation",-1), 
						getPET("bionano-depot",0)), 
						getExperimentTypes("bionano-depot").get(0), getExperimentTypes("bionano-depot").get(0), getExperimentTypes("ext-to-bionano-run").get(0), 
						DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

		return l;
	}
	
	@Override
	protected List<ProcessType> getProcessTypeDEV() {
		// TODO Auto-generated method stub
		return null;
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
		//Bionano
		newExperimentTypeNode("ext-to-bionano-nlrs-process", getExperimentTypes("ext-to-bionano-nlrs-process").get(0), false, false, null, null, null).save();	
		newExperimentTypeNode("ext-to-bionano-chip-process", getExperimentTypes("ext-to-bionano-chip-process").get(0), false, false, null, null, null).save();	
		newExperimentTypeNode("ext-to-bionano-run", getExperimentTypes("ext-to-bionano-run").get(0), false, false, null, null, null).save();	

		newExperimentTypeNode("irys-nlrs-prep",getExperimentTypes("irys-nlrs-prep").get(0),false,false,getExperimentTypeNodes("ext-to-bionano-nlrs-process"),null,null).save();
		newExperimentTypeNode("bionano-dls-prep",getExperimentTypes("bionano-dls-prep").get(0),false,false,getExperimentTypeNodes("ext-to-bionano-nlrs-process"),null,null).save();
		
		newExperimentTypeNode("irys-chip-preparation",getExperimentTypes("irys-chip-preparation").get(0),false,false,getExperimentTypeNodes("ext-to-bionano-chip-process","irys-nlrs-prep","bionano-dls-prep"),null,null).save();
		newExperimentTypeNode("bionano-depot",getExperimentTypes("bionano-depot").get(0),false,false,getExperimentTypeNodes("ext-to-bionano-run","irys-chip-preparation"),null,null).save();
	
		
	}
	
	@Override
	protected void getExperimentTypeNodeDEV() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void getExperimentTypeNodePROD() {
	
	}

	@Override
	protected void getExperimentTypeNodeUAT() {
		// TODO Auto-generated method stub

	}

	private List<PropertyDefinition> getPropertyDefinitionsBionano() {
		// TODO Auto-generated method stub
		return null;
	}
	
	private List<PropertyDefinition> getPropertyDefinitionPreparationIrysChip() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		//propertyDefinitions.add(newPropertiesDefinition("Volume engagé", "inputVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"), "single",51,true,"8", null));		
		return propertyDefinitions;
	}
	
	private List<PropertyDefinition> getPropertyDefinitionBionanoPrepDLS() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();

				
		propertyDefinitions.add(newPropertiesDefinition("Volume engagé","inputVolume", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, true, null,
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",8, true, null, "2"));
		
		propertyDefinitions.add(newPropertiesDefinition("Quantité engagée","inputQuantity", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, true, null,
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),MeasureUnit.find.findByCode( "ng"),MeasureUnit.find.findByCode( "ng"),"single",9, true, null, "2"));

		propertyDefinitions.add(newPropertiesDefinition("Quantité prévue par le protocole","requiredQuantity", LevelService.getLevels(Level.CODE.ContainerIn),Integer.class, true, null,
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),MeasureUnit.find.findByCode( "ng"),MeasureUnit.find.findByCode( "ng"),"single",10, true, "750", null));
		
		propertyDefinitions.add(newPropertiesDefinition("Volume eau","bufferVolume", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, true, null,
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",11, true, null, "2"));
		
		//File
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Tableau sélection enzyme","enzymeLabelDensity",LevelService.getLevels(Level.CODE.ContainerIn), File.class, false, null,
				null,"file", 12, true, null, null));

		propertyDefinitions.add(newPropertiesDefinition("Enzyme de labelling", "labellingEnzyme", LevelService.getLevels(Level.CODE.ContainerIn), String.class, true, null,
				DescriptionFactory.newValues("DLE-1"), "single",13, true, "DLE-1", null));
		
		propertyDefinitions.add(newPropertiesDefinition("Volume enzyme","enzymeVolume", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, true, null,
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",14, true, "1.5", "2"));
			
				
		propertyDefinitions.add(newPropertiesDefinition("Concentration 1", "measuredConc1", LevelService.getLevels(Level.CODE.ContainerOut), Double.class, true, null,
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "ng/µl"),MeasureUnit.find.findByCode( "ng/µl"),"single",19, true, null, "2"));

		propertyDefinitions.add(newPropertiesDefinition("Concentration 2", "measuredConc2", LevelService.getLevels(Level.CODE.ContainerOut), Double.class, true, null,
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "ng/µl"),MeasureUnit.find.findByCode( "ng/µl"),"single",20, true, null, "2"));

		propertyDefinitions.add(newPropertiesDefinition("Concentration moyenne", "averageConcentration", LevelService.getLevels(Level.CODE.ContainerOut), Double.class, true, null,
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "ng/µl"),MeasureUnit.find.findByCode( "ng/µl"),"single",21, true, null, "2"));
		
		propertyDefinitions.add(newPropertiesDefinition("CV","variationCoefficient", LevelService.getLevels(Level.CODE.ContainerOut),Double.class, true, null, 
				null, null, null, null,"single", 22, true, null, "2"));
		
		propertyDefinitions.add(newPropertiesDefinition("Concentration 3", "measuredConc3", LevelService.getLevels(Level.CODE.ContainerOut), Double.class, false, null,
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "ng/µl"),MeasureUnit.find.findByCode( "ng/µl"),"single",23, true, null, "2"));

		propertyDefinitions.add(newPropertiesDefinition("Concentration 4", "measuredConc4", LevelService.getLevels(Level.CODE.ContainerOut), Double.class, false, null,
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "ng/µl"),MeasureUnit.find.findByCode( "ng/µl"),"single",24, true, null, "2"));

		propertyDefinitions.add(newPropertiesDefinition("Concentration moyenne 2", "averageConcentration2", LevelService.getLevels(Level.CODE.ContainerOut), Double.class, false, null,
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "ng/µl"),MeasureUnit.find.findByCode( "ng/µl"),"single",25, true, null, "2"));

		propertyDefinitions.add(newPropertiesDefinition("CV 2","variationCoefficient2", LevelService.getLevels(Level.CODE.ContainerOut),Double.class, false, null, 
				null, null, null, null,"single", 26, true, null, "2"));

		propertyDefinitions.add(newPropertiesDefinition("Volume DL","dlVolume", LevelService.getLevels(Level.CODE.ContainerOut),Double.class, true, "F",
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",27, true, null, "2"));
		propertyDefinitions.add(newPropertiesDefinition("Volume stain","stainVolume", LevelService.getLevels(Level.CODE.ContainerOut),Double.class, true, "F",
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",28, true, null, "2"));
		
		
		return propertyDefinitions;
	}
	

	private List<PropertyDefinition> getPropertyDefinitionIrysPrepNLRS() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();

		propertyDefinitions.add(newPropertiesDefinition("Volume engagé","inputVolume", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, true, null,
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",8, true));
		
		propertyDefinitions.add(newPropertiesDefinition("Quantité engagée","inputQuantity", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, true, null,
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),MeasureUnit.find.findByCode( "ng"),MeasureUnit.find.findByCode( "ng"),"single",9, true));

		propertyDefinitions.add(newPropertiesDefinition("Quantité prévue par le protocole","requiredQuantity", LevelService.getLevels(Level.CODE.ContainerIn),Integer.class, true, null,
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),MeasureUnit.find.findByCode( "ng"),MeasureUnit.find.findByCode( "ng"),"single",11, true));
		//File
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Tableau sélection enzyme","enzymeLabelDensity",LevelService.getLevels(Level.CODE.ContainerIn), File.class, false, "file", 12));

		propertyDefinitions.add(newPropertiesDefinition("Enzyme de restriction", "restrictionEnzyme", LevelService.getLevels(Level.CODE.ContainerIn), String.class, true,DescriptionFactory.newValues("BspQI","Bsm1","BbvCI","BsrD1","BssSI","STAIN"), null, "single",13));
			
		propertyDefinitions.add(newPropertiesDefinition("Unités d'enzyme","enzymeUnit", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, true, null, null, null, null,"single", 14, true));
		
		propertyDefinitions.add(newPropertiesDefinition("Volume Enzyme","restrictionEnzymeVolume", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, true, null,
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",15, true));

		propertyDefinitions.add(newPropertiesDefinition("Enzyme de restriction 2", "restrictionEnzyme2", LevelService.getLevels(Level.CODE.ContainerIn), String.class, false,DescriptionFactory.newValues("BspQI","Bsm1","BbvCI","BsrD1","BssSI","STAIN"), null, "single",16));
		
		propertyDefinitions.add(newPropertiesDefinition("Volume Enzyme 2","restrictionEnzyme2Volume", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, false, null,
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",17, true));
		
		propertyDefinitions.add(newPropertiesDefinition("Unités d'enzyme 2","enzyme2Unit", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, false, null, null, null, null,"single", 18, true));
		
		propertyDefinitions.add(newPropertiesDefinition("Concentration 1", "measuredConc1", LevelService.getLevels(Level.CODE.ContainerOut), Double.class, false, null
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "ng/µl"),MeasureUnit.find.findByCode( "ng/µl"),"single",19, true));

		propertyDefinitions.add(newPropertiesDefinition("Concentration 2", "measuredConc2", LevelService.getLevels(Level.CODE.ContainerOut), Double.class, false, null
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "ng/µl"),MeasureUnit.find.findByCode( "ng/µl"),"single",20, true));

		propertyDefinitions.add(newPropertiesDefinition("Concentration 3", "measuredConc3", LevelService.getLevels(Level.CODE.ContainerOut), Double.class, false, null
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "ng/µl"),MeasureUnit.find.findByCode( "ng/µl"),"single",21, true));

		propertyDefinitions.add(newPropertiesDefinition("Conc. arrondie pour Irys", "nlrsConcentration", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), Double.class, false, null
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "ng/µl"),MeasureUnit.find.findByCode( "ng/µl"),"single",24, true));

		propertyDefinitions.add(newPropertiesDefinition("CV","variationCoefficient", LevelService.getLevels(Level.CODE.ContainerOut),Double.class, false, null, null, null, null,"single", 25, true));

		return propertyDefinitions;
	}
	

	private List<PropertyDefinition> getPropertyDefinitionDepotBionano() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Date réelle de dépôt", "runStartDate", LevelService.getLevels(Level.CODE.Experiment), Date.class, true, "single",100));

		propertyDefinitions.add(newPropertiesDefinition("FC active","flowcellActiveOnChip", LevelService.getLevels(Level.CODE.ContainerIn),Boolean.class, false, null, null, null, null,"single", 8, true,"true", null));
		propertyDefinitions.add(newPropertiesDefinition("Relance optimisation","optimizationRedo", LevelService.getLevels(Level.CODE.ContainerIn),Boolean.class, false, null, null, null, null,"single", 8, true,"false", null));

		propertyDefinitions.add(newPropertiesDefinition("Laser","laser", LevelService.getLevels(Level.CODE.ContainerIn),String.class, false, DescriptionFactory.newValues("vert","rouge","vert et rouge"), null, null, null,"single", 9, true));


		propertyDefinitions.add(newPropertiesDefinition("Temps de concentration recommandé","ptrConcentrationTime", LevelService.getLevels(Level.CODE.ContainerIn),Integer.class, false, null,
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_SPEED),MeasureUnit.find.findByCode( "s"),MeasureUnit.find.findByCode( "s"),"single",10, true));
		propertyDefinitions.add(newPropertiesDefinition("Temps de concentration réel","realConcentrationTime", LevelService.getLevels(Level.CODE.ContainerIn),Integer.class, false, null/*DescriptionFactory.newValues("600","450","400","350","300","250","200","150")*/,
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_SPEED),MeasureUnit.find.findByCode( "s"),MeasureUnit.find.findByCode( "s"),"single",11, true));
		propertyDefinitions.add(newPropertiesDefinition("Nombre de cycles","nbCycles", LevelService.getLevels(Level.CODE.ContainerIn),Integer.class, false, null, null, null, null,"single", 12, true));
		//Image 
	//	propertyDefinitions.add(newPropertiesDefinition("Photo zone pillar","pillarRegionPicture", LevelService.getLevels(Level.CODE.ContainerIn),Image.class, false, null, null, null, null,"img", 13, true));
		propertyDefinitions.add(newPropertiesDefinition("Voltage","voltage", LevelService.getLevels(Level.CODE.ContainerIn),Integer.class, false, null, null, null, null,"single", 14, true));
		propertyDefinitions.add(newPropertiesDefinition("Nb d'optimisations","nbOfOptimizations", LevelService.getLevels(Level.CODE.ContainerIn),Integer.class, false, DescriptionFactory.newValues("2","3","4"), null, null, null,"single", 15, true));
		
		propertyDefinitions.add(newPropertiesDefinition("Optimized Bump Time","optimizedBumpTime", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, false, null,
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_SPEED),MeasureUnit.find.findByCode( "s"),MeasureUnit.find.findByCode( "s"),"single",16, true));
		propertyDefinitions.add(newPropertiesDefinition("Optimized Load Time","optimizedLoadTime", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, false, null,
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_SPEED),MeasureUnit.find.findByCode( "s"),MeasureUnit.find.findByCode( "s"),"single",17, true)); 
		propertyDefinitions.add(newPropertiesDefinition("Résumé manips","manipSummarize", LevelService.getLevels(Level.CODE.ContainerIn),File.class, false, null, null, null, null,"file", 20, true));
		

		return propertyDefinitions;
	}

	

	


}
