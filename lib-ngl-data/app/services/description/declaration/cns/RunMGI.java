package services.description.declaration.cns;

import static services.description.DescriptionFactory.newExperimentType;
import static services.description.DescriptionFactory.newExperimentTypeNode;
import static services.description.DescriptionFactory.newPropertiesDefinition;

import java.util.ArrayList;
import java.util.Arrays;

import java.util.Date;
import java.util.List;

import models.laboratory.common.description.Level;
import models.laboratory.common.description.MeasureCategory;
import models.laboratory.common.description.MeasureUnit;
import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.common.description.Value;

import models.laboratory.common.description.dao.MeasureCategoryDAO;
import models.laboratory.common.description.dao.MeasureUnitDAO;
import models.laboratory.experiment.description.ExperimentCategory;
import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.experiment.description.dao.ExperimentCategoryDAO;
import models.laboratory.processes.description.ExperimentTypeNode;
import models.laboratory.processes.description.ProcessCategory;
import models.laboratory.processes.description.ProcessType;
import models.laboratory.processes.description.dao.ProcessCategoryDAO;
import models.utils.dao.DAOException;
import services.description.Constants;
import services.description.DescriptionFactory;
import services.description.common.LevelService;
import services.description.common.MeasureService;
import services.description.declaration.AbstractDeclaration;

public class RunMGI extends AbstractDeclaration{

	@Override
	protected List<ProcessType> getProcessTypeCommon() {
		List<ProcessType> l = new ArrayList<>();
		ProcessCategoryDAO pcfind = ProcessCategory.find.get();

		//TODO A revoir un fois les specs terminées!!!!
			l.add(DescriptionFactory.newProcessType("ssCircu, DNB, FC, dépôt (MGI)", "mgi-sscircu-dnb-fc-depot", 
				ProcessCategory.find.get().findByCode("mgi-library"),62 , 
				getMGICircuToDepotProperties(), 
				Arrays.asList(getPET("ext-to-mgi-sscircu-dnb-fc-depot",-1),
						getPET("pcr-amplification-and-purification",-1),
						getPET("mgi-indexed-library",-1),
						getPET("mgi-sscircularisation",0),
						getPET("mgi-nanoballs",1),
						getPET("mgi-prepa-fc",2), 
						getPET("mgi-depot",3)),   
						getExperimentTypes("mgi-sscircularisation").get(0),getExperimentTypes("mgi-depot").get(0), getExperimentTypes("ext-to-mgi-sscircu-dnb-fc-depot").get(0), 
						DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		return l;
	}
	@Override
	protected List<ProcessType> getProcessTypeDEV() {
		return new ArrayList<>();
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
	protected List<ExperimentType> getExperimentTypeCommon() {
		List<ExperimentType> l = new ArrayList<>();
		ExperimentCategoryDAO ecfind = ExperimentCategory.find.get();
		
		l.add(newExperimentType("Ext to ssCircu, DNB, FC, dépôt (MGI)","ext-to-mgi-sscircu-dnb-fc-depot",null,-1,
				ecfind.findByCode(ExperimentCategory.CODE.voidprocess.name()), null, null,"OneToOne",
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		//TODO a implementer NGL-3037
		//Declaration minimale pour import librairie MGI NGL-3043
		l.add(newExperimentType("Banque indexée MGI","mgi-indexed-library","LIB",2190,
				ecfind.findByCode(ExperimentCategory.CODE.transformation.name()), null,
				null,"OneToOne",
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(newExperimentType("ssCircularisation (MGI)","mgi-sscircularisation","CIR",2195,
				ecfind.findByCode(ExperimentCategory.CODE.transformation.name()), getPropertyDefinitionSsCircularisation(),
				getInstrumentUsedTypes("hand"),"OneToOne",
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(newExperimentType("Nano balls MGI","mgi-nanoballs","DNB",2200,
				ecfind.findByCode(ExperimentCategory.CODE.transformation.name()), getPropertyDefinitionNanoballs(),
				getInstrumentUsedTypes("thermocycler"),"OneToOne",
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(newExperimentType("Prep FC MGI","mgi-prepa-fc",null,2205,
				ecfind.findByCode(ExperimentCategory.CODE.transformation.name()), 
				getPropertyPrepFCMGI(),	
				getInstrumentUsedTypes("mgidl-200h","mgi-onboard-dnb-loader"),"ManyToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS) ));
	
		l.add(newExperimentType("Depot MGI","mgi-depot",null,2210,
				ecfind.findByCode(ExperimentCategory.CODE.transformation.name()), 
				getPropertyDepotMGI(),	
				getInstrumentUsedTypes("dnbseq-g400"),"OneToVoid", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS) ));
		
		return l;
		
		
	}
	@Override
	protected List<ExperimentType> getExperimentTypeDEV() {
		return new ArrayList<>();
	}
	@Override
	protected List<ExperimentType> getExperimentTypePROD() {
		return new ArrayList<>();
	}
	@Override
	protected List<ExperimentType> getExperimentTypeUAT() {
		return new ArrayList<>();
	}
	@Override
	protected void getExperimentTypeNodeCommon() {
		save(newExperimentTypeNode("ext-to-mgi-sscircu-dnb-fc-depot", getExperimentTypes("ext-to-mgi-sscircu-dnb-fc-depot").get(0), 
				false, false, false, 
				null, null, null, null));
		
		//TODO en attente du dev NGL-3037
		save(newExperimentTypeNode("mgi-indexed-library",getExperimentTypes("mgi-indexed-library").get(0),
				false, false,false,
				null,null,null,null));
		save(newExperimentTypeNode("mgi-sscircularisation",getExperimentTypes("mgi-sscircularisation").get(0),
				false, true,true,
				getExperimentTypeNodes("ext-to-mgi-sscircu-dnb-fc-depot", "pcr-amplification-and-purification","mgi-indexed-library")
				,null,
				getExperimentTypes("fluo-quantification"),
				getExperimentTypes("pool")));
		save(newExperimentTypeNode("mgi-nanoballs",getExperimentTypes("mgi-nanoballs").get(0),
				false, true,true,
				getExperimentTypeNodes("mgi-sscircularisation")
				,null,
				getExperimentTypes("fluo-quantification"),
				getExperimentTypes("pool")));

		save(newExperimentTypeNode("mgi-prepa-fc",getExperimentTypes("mgi-prepa-fc").get(0),
				false,false,false,
				getExperimentTypeNodes("mgi-nanoballs"),
				null,null,null));
		save(newExperimentTypeNode("mgi-depot",getExperimentTypes("mgi-depot").get(0),
				false,false,false,
				getExperimentTypeNodes("mgi-prepa-fc"),
				null,null,null));	 


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
	private static List<PropertyDefinition> getPropertyDefinitionSsCircularisation() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		MeasureCategoryDAO mcfind = MeasureCategory.find.get();
		MeasureUnitDAO     mufind = MeasureUnit.find.get();
		
		propertyDefinitions.add(newPropertiesDefinition("Qté engagée en ssCircu (MGI)", "inputQuantitySsCircu", LevelService.getLevels(Level.CODE.ContainerIn, Level.CODE.Content), Double.class, true, null,
				mcfind.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),mufind.findByCode("pmol"),mufind.findByCode("fmol"),"single",21, true));
		propertyDefinitions.add(newPropertiesDefinition("Volume engagé", "inputVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null,
				mcfind.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),mufind.findByCode("µl"),mufind.findByCode("µl"),"single",22, true));
		propertyDefinitions.add(newPropertiesDefinition("Volume tampon", "bufferVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null,
				mcfind.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),mufind.findByCode("µl"),mufind.findByCode("µl"),"single",23, true));
		propertyDefinitions.add(newPropertiesDefinition("Volume final à engager", "requiredVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null,
				mcfind.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),mufind.findByCode("µl"),mufind.findByCode("µl"),"single",24, true));
		propertyDefinitions.add(newPropertiesDefinition("Vol final (après digestion et purif.)", "finalVolume", LevelService.getLevels(Level.CODE.ContainerOut), Double.class, true, null,
				mcfind.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),mufind.findByCode("µl"),mufind.findByCode("µl"),"single",25, true));
			
		return propertyDefinitions; 
	}
	
	private static List<PropertyDefinition> getPropertyDefinitionNanoballs() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		MeasureCategoryDAO mcfind = MeasureCategory.find.get();
		MeasureUnitDAO     mufind = MeasureUnit.find.get();
		propertyDefinitions.add(newPropertiesDefinition("Qté engagée en DNB (MGI)", "inputQuantityDNB", LevelService.getLevels(Level.CODE.ContainerIn, Level.CODE.Content), Double.class, true, null,
				mcfind.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),mufind.findByCode("fmol"),mufind.findByCode("fmol"),"single",21, true));
		propertyDefinitions.add(newPropertiesDefinition("Volume engagé", "inputVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null,
				mcfind.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),mufind.findByCode("µl"),mufind.findByCode("µl"),"single",22, true));
		propertyDefinitions.add(newPropertiesDefinition("Volume tampon", "bufferVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null,
				mcfind.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),mufind.findByCode("µl"),mufind.findByCode("µl"),"single",23, true));
		propertyDefinitions.add(newPropertiesDefinition("Volume final à engager", "requiredVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null,
				mcfind.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),mufind.findByCode("µl"),mufind.findByCode("µl"),"single",24, true));
		propertyDefinitions.add(newPropertiesDefinition("Vol final en sortie", "finalVolume", LevelService.getLevels(Level.CODE.ContainerOut), Double.class, true, null,
				mcfind.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),mufind.findByCode("µl"),mufind.findByCode("µl"),"single",25, true));
		propertyDefinitions.add(newPropertiesDefinition("Label de travail", "workName", LevelService.getLevels(Level.CODE.ContainerOut), String.class, false, null,null,
				"single",25, true,null,null));
		return propertyDefinitions;
	}
	

	public static List<PropertyDefinition> getMGICircuToDepotProperties() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Type séquencage","sequencingType"
						, LevelService.getLevels(Level.CODE.Process),String.class, true, getSequencingType(), "single",100));
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("Type de lectures", "readType"
						, LevelService.getLevels(Level.CODE.Process),String.class, true, getReadType(), "single",200));		
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("Longueur de lecture", "readLength"
						, LevelService.getLevels(Level.CODE.Process),String.class, true, getReadLenght(), "single",300));
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("% à déposer prévisionnel", "estimatedPercentPerLane"
						, LevelService.getLevels(Level.CODE.Process),Double.class, true,"single",400));	
		return propertyDefinitions;
	}
	
	private static List<Value> getSequencingType() {
		List<Value> values = new ArrayList<>();
		values.add(DescriptionFactory.newValue("MGI / DNBSEQ-G400",  "MGI / DNBSEQ-G400"));
		
		values.add(DescriptionFactory.newValue("undefined","Non déterminé"));
		
		return values;	
	}
	
	private static List<Value> getReadLenght() {
		List<Value> values = new ArrayList<>();
		values.add(DescriptionFactory.newValue( "50",  "50"));
		values.add(DescriptionFactory.newValue( "75",  "75"));
		values.add(DescriptionFactory.newValue("100", "100"));
		values.add(DescriptionFactory.newValue("150", "150"));
		values.add(DescriptionFactory.newValue( "200",  "200"));
		values.add(DescriptionFactory.newValue("250", "250"));
		values.add(DescriptionFactory.newValue("300", "300"));
		values.add(DescriptionFactory.newValue( "400",  "400"));
		values.add(DescriptionFactory.newValue("undefined","Non déterminé"));
		
		return values;
	}
	
	private static List<Value> getReadType() {
		List<Value> values = new ArrayList<>();
		values.add(DescriptionFactory.newValue("SR", "SR"));
		values.add(DescriptionFactory.newValue("PE", "PE"));
		values.add(DescriptionFactory.newValue("undefined","Non déterminé"));
		return values;
	}
	
	private static List<PropertyDefinition> getPropertyPrepFCMGI() throws DAOException {
		MeasureCategoryDAO mcfind = MeasureCategory.find.get();
		MeasureUnitDAO     mufind = MeasureUnit.find.get();
	List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		//Niveau container
		propertyDefinitions.add(newPropertiesDefinition(
				"Volume DNB engagé","dnbRequiredVolume", 
				LevelService.getLevels(Level.CODE.ContainerIn), Double.class,
				true, null,mcfind.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),mufind.findByCode( "µL"),mufind.findByCode( "µL"),
				"single",20,true));	
		
		
		propertyDefinitions.add(newPropertiesDefinition(
				"Volume DNB loading mix déposé","dnbLoadingMixVolume", 
				LevelService.getLevels(Level.CODE.ContainerOut), Double.class,
				true, null,mcfind.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),mufind.findByCode( "µL"),mufind.findByCode( "µL"),
				"single",21,true));	
		return propertyDefinitions;
		

	}
	
	private static List<PropertyDefinition> getPropertyDepotMGI() throws DAOException {
		MeasureUnitDAO     mufind = MeasureUnit.find.get();
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		//Niveau experiments
		propertyDefinitions.add(newPropertiesDefinition(
				"Date réelle de dépôt", "runStartDate", 
				LevelService.getLevels(Level.CODE.Experiment), Date.class,
				true, null,null,"single",21,true,null,null));
		propertyDefinitions.add(newPropertiesDefinition(
				"Activation NGS-RG", "rgActivation", LevelService.getLevels(Level.CODE.Experiment), Boolean.class,
				true, null, null, "single",22, true,"true", null));

		return propertyDefinitions;
	}
	private static List<PropertyDefinition> getPropertyDefinitionsMgiProcessRun(){
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();

		return propertyDefinitions;
	}

}
