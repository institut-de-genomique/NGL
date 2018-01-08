package services.description.instrument;

import static services.description.DescriptionFactory.newInstrumentCategory;
import static services.description.DescriptionFactory.newInstrumentUsedType;
import static services.description.DescriptionFactory.newPropertiesDefinition;
import static services.description.DescriptionFactory.newValues;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import models.laboratory.common.description.Institute;
import models.laboratory.common.description.Level;
import models.laboratory.common.description.MeasureCategory;
import models.laboratory.common.description.MeasureUnit;
import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.container.description.ContainerSupportCategory;
import models.laboratory.instrument.description.Instrument;
import models.laboratory.instrument.description.InstrumentCategory;
import models.laboratory.instrument.description.InstrumentUsedType;
import models.utils.dao.DAOException;
import models.utils.dao.DAOHelpers;
import play.data.validation.ValidationError;
import services.description.DescriptionFactory;
import services.description.Constants;
import services.description.common.LevelService;
import services.description.common.MeasureService;

public class InstrumentServiceGET extends AbstractInstrumentService{
	
	
	public void saveInstrumentCategories(Map<String, List<ValidationError>> errors) throws DAOException {
		List<InstrumentCategory> l = new ArrayList<InstrumentCategory>();
		l.add(newInstrumentCategory("Covaris","covaris"));
//		l.add(newInstrumentCategory("Spri","spri"));
		l.add(newInstrumentCategory("Thermocycleur","thermocycler"));
		l.add(newInstrumentCategory("Séquenceur Pacific Biosciences","pacbio-sequencer"));
		l.add(newInstrumentCategory("Centrifugeuse","centrifuge"));

		
		l.add(newInstrumentCategory("Quantification par fluorométrie","fluorometer"));
		l.add(newInstrumentCategory("Appareil de qPCR","qPCR-system"));
		l.add(newInstrumentCategory("Electrophorèse sur puce","chip-electrophoresis"));
		
		l.add(newInstrumentCategory("Main","hand"));
		l.add(newInstrumentCategory("CBot","cbot"));
		l.add(newInstrumentCategory("XP Workflow","xp-wf"));
		
		l.add(newInstrumentCategory("Séquenceur Illumina","illumina-sequencer"));
//		l.add(newInstrumentCategory("Cartographie Optique Opgen","opt-map-opgen"));
		l.add(newInstrumentCategory("Séquenceur Nanopore","nanopore-sequencer"));
		l.add(newInstrumentCategory("Extérieur","extseq"));
		
		l.add(newInstrumentCategory("Robot pipetage","liquid-handling-robot"));
		l.add(newInstrumentCategory("Appareil de sizing","sizing-system"));
				
		DAOHelpers.saveModels(InstrumentCategory.class, l, errors);
		
	}
	
	public void saveInstrumentUsedTypes(Map<String, List<ValidationError>> errors) throws DAOException {
		
		List<InstrumentUsedType> l = new ArrayList<InstrumentUsedType>();
		
		//TODO : COVARIS  machine 
		
		l.add(newInstrumentUsedType("Covaris", "covaris-s2", InstrumentCategory.find.findByCode("covaris"), getCovarisProperties(), 
				getInstruments(
						//createInstrument("Covaris1", "Covaris1", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.GET)), 
						createInstrument("COVARIS M220", "COVARIS", null, true, "/save/devcrgs/src/NGL_Feuille_route/COVARIS/", DescriptionFactory.getInstitutes(Constants.CODE.GET))) ,
				getContainerSupportCategories(new String[]{"tube"}),getContainerSupportCategories(new String[]{"tube"}), 
				DescriptionFactory.getInstitutes(Constants.CODE.GET)));
		
//		l.add(newInstrumentUsedType("Spri", "spri", InstrumentCategory.find.findByCode("spri"), getSpriProperties(), 
//				getInstruments(
//						createInstrument("Spri1", "Spri1", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.GET)), 
//						createInstrument("Spri2", "Spri2", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.GET)), 
//						createInstrument("Spri3", "Spri3", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.GET)) ), 
//				getContainerSupportCategories(new String[]{"tube"}),getContainerSupportCategories(new String[]{"tube"}), 
//				DescriptionFactory.getInstitutes(Constants.CODE.GET)));		
		
//		l.add(newInstrumentUsedType("Stratagene qPCR system", "stratagene-qPCR", InstrumentCategory.find.findByCode("qPCR-system"), getQPCRProperties(), 
//				getInstruments(
//						createInstrument("Stratagene1", "Stratagene1", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.GET))),
//				getContainerSupportCategories(new String[]{"tube","96-well-plate"}), null, 
//				DescriptionFactory.getInstitutes(Constants.CODE.GET)));
	
// TODO: cbot interne machine 
		
		l.add(newInstrumentUsedType("cBot-interne", "cBot-interne", InstrumentCategory.find.findByCode("cbot"), getCBotInterneProperties(), 
				getInstruments(
//						createInstrument("cBot-hiseq2500","cBot HiSeq2500",  null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.GET)),
						createInstrument("cBot-Miseq_1","cBot Miseq_1",  null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.GET)),
						createInstrument("cBot-Miseq_2","cBot Miseq_2",  null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.GET)),
						createInstrument("cBot-Miseq_4","cBot Miseq_4",  null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.GET))
						),
				getContainerSupportCategories(new String[]{"tube","96-well-plate","384-well-plate"}), getContainerSupportCategories(new String[]{"flowcell-1"}), 
				DescriptionFactory.getInstitutes(Constants.CODE.GET)));

		l.add(newInstrumentUsedType("cBot-interne-NovaSeq", "cBot-interne-novaseq", InstrumentCategory.find.findByCode("cbot"), getCBotInterneProperties(), 
				getInstruments(
						createInstrument("cBot-NovaSeq","cBot NovaSeq",  null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.GET))
						),
				getContainerSupportCategories(new String[]{"tube","96-well-plate","384-well-plate"}), getContainerSupportCategories(new String[]{"flowcell-S1","flowcell-S2","flowcell-S4"}), 
				DescriptionFactory.getInstitutes(Constants.CODE.GET)));
		
// TODO:  Added cbot machine
		
		l.add(newInstrumentUsedType("cBot", "cBot", InstrumentCategory.find.findByCode("cbot"), getCBotProperties(), 
				getInstruments(
						createInstrument("cBot1", "cBot-1", null, true, "/save/devcrgs/src/NGL_Feuille_route/cBot-1", DescriptionFactory.getInstitutes(Constants.CODE.GET))),
				getContainerSupportCategories(new String[]{"tube","96-well-plate","384-well-plate"}), getContainerSupportCategories(new String[]{"flowcell-8"}), 
				DescriptionFactory.getInstitutes(Constants.CODE.GET)));
		
		//TODO : a valider  TECAN evo machine 
		
		l.add(newInstrumentUsedType("TECAN evo 150", "tecan-evo-150", InstrumentCategory.find.findByCode("liquid-handling-robot"), null, 
				getInstruments(
						createInstrument("EVO150","EVO 150 n° 1 Post-PCR", null, true,  "/save/devcrgs/src/NGL_Feuille_route/EVO150", DescriptionFactory.getInstitutes(Constants.CODE.GET)),
						createInstrument("EVO150_2","Evo 150 n° 2 Post-PCR", null, true,  "/save/devcrgs/src/NGL_Feuille_route/EVO150_2", DescriptionFactory.getInstitutes(Constants.CODE.GET)),
						createInstrument("EVO150_3","EVO 150 n° 3 Pré PCR", null, true,  "/save/devcrgs/src/NGL_Feuille_route/EVO150_3", DescriptionFactory.getInstitutes(Constants.CODE.GET))
						),
						getContainerSupportCategories(new String[]{"tube","96-well-plate","384-well-plate"}),getContainerSupportCategories(new String[]{"tube","96-well-plate","384-well-plate"}), 
						
				DescriptionFactory.getInstitutes(Constants.CODE.GET)));

		l.add(newInstrumentUsedType("TECAN evo 200", "tecan-evo-200", InstrumentCategory.find.findByCode("liquid-handling-robot"), null, 
				getInstruments(
						createInstrument("EVO","TECAN200 EVO",  null, true, "/save/devcrgs/src/NGL_Feuille_route/EVO", DescriptionFactory.getInstitutes(Constants.CODE.GET))),
						getContainerSupportCategories(new String[]{"tube","96-well-plate","384-well-plate"}),getContainerSupportCategories(new String[]{"tube","96-well-plate","384-well-plate"}), 
				DescriptionFactory.getInstitutes(Constants.CODE.GET)));
		
		
//TODO : verify getContainerSupportCategories parameters
		
		l.add(newInstrumentUsedType("Blue pippin", "blue-pippin", InstrumentCategory.find.findByCode("sizing-system"), null, 
				getInstruments(
						createInstrument("BluePippin1", "BluePippin1", null, true, "/save/devcrgs/src/NGL_Feuille_route/BluePippin1", DescriptionFactory.getInstitutes(Constants.CODE.GET))),
						getContainerSupportCategories(new String[]{"96-well-plate"}),null, 
				DescriptionFactory.getInstitutes(Constants.CODE.GET)));
		

		l.add(newInstrumentUsedType("Main", "hand", InstrumentCategory.find.findByCode("hand"), null, 
				getInstruments(
						createInstrument("hand", "Main", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.GET)) ),
						getContainerSupportCategories(new String[]{"tube","96-well-plate","384-well-plate"}),getContainerSupportCategories(new String[]{"tube","96-well-plate","384-well-plate"}), 
				DescriptionFactory.getInstitutes(Constants.CODE.GET)));

		l.add(newInstrumentUsedType("qubit", "QuBit", InstrumentCategory.find.findByCode("fluorometer"), getQuBitProperties(), 
				getInstruments(
						createInstrument("quBit1", "QuBit1", null, true, "/save/devcrgs/src/NGL_Feuille_route/QuBit1", DescriptionFactory.getInstitutes(Constants.CODE.GET))), 
				getContainerSupportCategories(new String[]{"tube"}),null, 
				DescriptionFactory.getInstitutes(Constants.CODE.GET)));

	
//TODO: MISEQ machine 
		
		l.add(newInstrumentUsedType("MiSeq", "MISEQ", InstrumentCategory.find.findByCode("illumina-sequencer"), getMiseqProperties(), 
				getInstruments(
				createInstrument("MISEQ", "MiSeq n°1", null, true,"/save/devcrgs/src/NGL_Feuille_route/MISEQ", DescriptionFactory.getInstitutes(Constants.CODE.GET)),
				createInstrument("MISEQ_2","MiSeq n°2", null, true,"/save/devcrgs/src/NGL_Feuille_route/MISEQ_2", DescriptionFactory.getInstitutes(Constants.CODE.GET)),
				createInstrument("MISEQ_4","MiSeq n°4 ", null, true,"/save/devcrgs/src/NGL_Feuille_route/MISEQ_4", DescriptionFactory.getInstitutes(Constants.CODE.GET))),
				getContainerSupportCategories(new String[]{"flowcell-1"}), null, 
				DescriptionFactory.getInstitutes(Constants.CODE.GET)));
		
//TODO: HISEQ2500 machine 
//		
//		l.add(newInstrumentUsedType("HiSeq 2500", "HISEQ2500", InstrumentCategory.find.findByCode("illumina-sequencer"), getHiseq2500Properties(), 
//				getInstruments(
//				 createInstrument("HiSeq2500", "HISEQ2000_2", null, true,"/save/devcrgs/src/NGL_Feuille_route/HISEQ2000_2/", DescriptionFactory.getInstitutes(Constants.CODE.GET))),
//				getContainerSupportCategories(new String[]{"flowcell-8","flowcell-2"}), null, 
//				DescriptionFactory.getInstitutes(Constants.CODE.GET)));


//		l.add(newInstrumentUsedType("HISEQ4000", "HISEQ4000", InstrumentCategory.find.findByCode("illumina-sequencer"), getHiseq4000Properties(), 
//				getInstrumentHiseq4000(),
//				getContainerSupportCategories(new String[]{"flowcell-8"}), null, 
//				DescriptionFactory.getInstitutes(Constants.CODE.GET)));

//TODO: HISEQ3000
		
		l.add(newInstrumentUsedType("HiSeq 3000", "HISEQ3000", InstrumentCategory.find.findByCode("illumina-sequencer"), getHiseq3000Properties(), 
				getInstruments(
				 createInstrument("HISEQ3000","HiSeq3000 HWI-J00115",  null, true,"/save/devcrgs/src/NGL_Feuille_route/HISEQ3000", DescriptionFactory.getInstitutes(Constants.CODE.GET)),
				 createInstrument("HISEQ3000_2","HiSeq3000 HWI-J00173",  null, true,"/save/devcrgs/src/NGL_Feuille_route/HISEQ3000_2", DescriptionFactory.getInstitutes(Constants.CODE.GET))),
				getContainerSupportCategories(new String[]{"flowcell-8"}), null, 
				DescriptionFactory.getInstitutes(Constants.CODE.GET)));

		/* NovaSeq*/
        l.add(newInstrumentUsedType("NovaSeq", "NOVASEQ", InstrumentCategory.find.findByCode("illumina-sequencer"), getNovaSeqProperties(), 
                getInstruments(
                 createInstrument("NOVASEQ","NovaSeq",  null, true,"/save/devcrgs/src/NGL_Feuille_route/NOVASEQ", DescriptionFactory.getInstitutes(Constants.CODE.GET))),
                 getContainerSupportCategories(new String[]{"flowcell-S1","flowcell-S2","flowcell-S4"}), null , 
                DescriptionFactory.getInstitutes(Constants.CODE.GET)));

		/* NovaSeq*/
//        l.add(newInstrumentUsedType("XP Workflow", "XPWORKFLOW ", InstrumentCategory.find.findByCode("xp-wf"), getNovaSeqProperties(), 
//                getInstruments(
//                 createInstrument("XPWORKFLOW","XP Workflow",  null, true,"/save/devcrgs/src/NGL_Feuille_route/XPWF", DescriptionFactory.getInstitutes(Constants.CODE.GET))),
//                getContainerSupportCategories(new String[]{"tube","96-well-plate", "384-well-plate"}), getContainerSupportCategories(new String[]{"flowcell-S1","flowcell-S2","flowcell-S4"}), 
//                DescriptionFactory.getInstitutes(Constants.CODE.GET)));
        
//TODO: Fragment Analyser machine 
	
		l.add(newInstrumentUsedType("Agilent 2100 bioanalyzer", "agilent-2100-bioanalyzer", InstrumentCategory.find.findByCode("chip-electrophoresis"), getChipElectrophoresisProperties(), 
				getInstruments(
						createInstrument("BIOANALYZER_1","Bioanalyzer plage",  null, true, "/save/devcrgs/src/NGL_Feuille_route/BIOANALYZER_1", DescriptionFactory.getInstitutes(Constants.CODE.GET ))),
				getContainerSupportCategories(new String[]{"tube"}),null, 
				DescriptionFactory.getInstitutes(Constants.CODE.GET)));

		// TODO: Fragment Analyser machine 
		
		l.add(newInstrumentUsedType("Fragment Analyzer", "Fragment-Analyzer", InstrumentCategory.find.findByCode("chip-electrophoresis"), getChipElectrophoresisProperties(), 
				getInstruments( 
						createInstrument("FRAGANALYZER", "Fragment Analyzer",  null, true, "/save/devcrgs/src/NGL_Feuille_route/FRAGANALYZER", DescriptionFactory.getInstitutes(Constants.CODE.GET ))),
				getContainerSupportCategories(new String[]{"tube"}),null, 
				DescriptionFactory.getInstitutes(Constants.CODE.GET)));
		
	//TODO : Added Thermocycleur Machine
		
		l.add(newInstrumentUsedType("Thermocycleur", "thermocycler", InstrumentCategory.find.findByCode("thermocycler"), getThermocyclerProperties(), 
				getInstruments(
						createInstrument("MasterCycler","Master Cycler",  null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.GET))
						), 
				getContainerSupportCategories(new String[]{"tube"}),getContainerSupportCategories(new String[]{"tube"}), 
				DescriptionFactory.getInstitutes(Constants.CODE.GET)));
		
//		l.add(newInstrumentUsedType("LabChip GX", "labChipGX", InstrumentCategory.find.findByCode("chip-electrophoresis"), null, 
//				getInstruments(
//						createInstrument("labChip1", "LabChip1", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.GET)) ) ,
//				getContainerSupportCategories(new String[]{"sheet-384","96-well-plate"}),null, 
//				DescriptionFactory.getInstitutes(Constants.CODE.GET)));
		l.add(newInstrumentUsedType("QuantStudio 6", "QS6", InstrumentCategory.find.findByCode("qPCR-system"), null, 
				getInstruments(createInstrument("QS6", "QS6", null, true, "/save/devcrgs/src/NGL_Feuille_route/QS6", DescriptionFactory.getInstitutes(Constants.CODE.GET))),
				getContainerSupportCategories(new String[]{"96-well-plate","384-well-plate"}), null, 
				DescriptionFactory.getInstitutes(Constants.CODE.GET)));	
		l.add(newInstrumentUsedType("ABI 7900HT", "ABI7900HT", InstrumentCategory.find.findByCode("qPCR-system"), null, 
				getInstruments(
						createInstrument("ABI7900HT", "ABI7900HT", null, true, "/save/devcrgs/src/NGL_Feuille_route/ABI7900HT", DescriptionFactory.getInstitutes(Constants.CODE.GET))),
				getContainerSupportCategories(new String[]{"96-well-plate","384-well-plate"}), null, 
				DescriptionFactory.getInstitutes(Constants.CODE.GET)));
		l.add(newInstrumentUsedType("PacBio RSII", "PacBioRSII", InstrumentCategory.find.findByCode("qPCR-system"), null, 
				getInstruments(
						createInstrument("RSII", "PacBioRSII", null, true, "/save/devcrgs/src/NGL_Feuille_route/RSII", DescriptionFactory.getInstitutes(Constants.CODE.GET))),
				getContainerSupportCategories(new String[]{"smrtcell-150k"}), null, 
				DescriptionFactory.getInstitutes(Constants.CODE.GET)));

//		l.add(newInstrumentUsedType("Eppendorf MiniSpin plus", "eppendorf-mini-spin-plus", InstrumentCategory.find.findByCode("centrifuge"), getNanoporeFragmentationProperties(),  getInstrumentEppendorfMiniSpinPlus()
//				,getContainerSupportCategories(new String[]{"tube"}), getContainerSupportCategories(new String[]{"tube"}), DescriptionFactory.getInstitutes(Constants.CODE.GET)));
				
//		l.add(newInstrumentUsedType("MinION", "minION", InstrumentCategory.find.findByCode("nanopore-sequencer"), getNanoporeDepotProperties(),getInstrumentMinIon() 
//				,getContainerSupportCategories(new String[]{"tube"}), getContainerSupportCategories(new String[]{"flowcell-1"}), DescriptionFactory.getInstitutes(Constants.CODE.GET)));
		
//		l.add(newInstrumentUsedType("Mk1", "mk1", InstrumentCategory.find.findByCode("nanopore-sequencer"), getNanoporeDepotProperties(),getInstrumentMKI() 
//				,getContainerSupportCategories(new String[]{"tube"}), getContainerSupportCategories(new String[]{"flowcell-1"}), DescriptionFactory.getInstitutes(Constants.CODE.GET)));
		
		DAOHelpers.saveModels(InstrumentUsedType.class, l, errors);
	}


	private static List<PropertyDefinition> getHiseq3000Properties() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
        propertyDefinitions.add(newPropertiesDefinition("Type lectures", "sequencingProgramType", LevelService.getLevels(Level.CODE.Instrument),String.class, true,DescriptionFactory.newValues("SR","PE"), "single",300));
        propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read1", "nbCyclesRead1", LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",400));
        propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read Index1", "nbCyclesReadIndex1", LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",500));
        propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read2", "nbCyclesRead2", LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",700));
        propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read Index2", "nbCyclesReadIndex2", LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",600));
//        propertyDefinitions.add(newPropertiesDefinition("Piste contrôle","controlLane", LevelService.getLevels(Level.CODE.Instrument),String.class, true,DescriptionFactory.newValuesWithDefault("Pas de piste contrôle (auto-calibrage)","Pas de piste contrôle (auto-calibrage)","1",
//        		"2","3","4","5","6","7","8"),"Pas de piste contrôle (auto-calibrage)","single",100));
        return propertyDefinitions;
	}

	private static List<PropertyDefinition> getNovaSeqProperties() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
        propertyDefinitions.add(newPropertiesDefinition("Workflow", "workflow", LevelService.getLevels(Level.CODE.Instrument),String.class, true,DescriptionFactory.newValues("Standard","XP"), null, "single",200));
        propertyDefinitions.add(newPropertiesDefinition("Type lectures", "sequencingProgramType", LevelService.getLevels(Level.CODE.Instrument),String.class, true,DescriptionFactory.newValues("SR","PE"), "single",300));
        propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read1", "nbCyclesRead1", LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",400));
        propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read Index1", "nbCyclesReadIndex1", LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",500));
        propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read Index2", "nbCyclesReadIndex2", LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",600));
        propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read2", "nbCyclesRead2", LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",700));
        propertyDefinitions.add(newPropertiesDefinition("Code Flowcell", "containerSupportCode", LevelService.getLevels(Level.CODE.Instrument),String.class, true, "single",800, "n/a"));
        propertyDefinitions.add(newPropertiesDefinition("Position", "position", LevelService.getLevels(Level.CODE.Instrument),String.class, true,DescriptionFactory.newValues("A","B","n/a"), "n/a", "single",900));
        return propertyDefinitions;
	}
	
	private static List<PropertyDefinition> getNanoporeFragmentationProperties() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
        propertyDefinitions.add(newPropertiesDefinition("Programme", "program", LevelService.getLevels(Level.CODE.Instrument),String.class, true,
        		DescriptionFactory.newValues("G-TUBE"), "G-TUBE", null, null, null, "single", 1));
        propertyDefinitions.add(newPropertiesDefinition("Vitesse", "speed", LevelService.getLevels(Level.CODE.Instrument),String.class, false,
        		null, "8000", MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_SPEED),MeasureUnit.find.findByCode( "rpm"),MeasureUnit.find.findByCode( "rpm"), "single", 2));
        // unite s
        propertyDefinitions.add(newPropertiesDefinition("Durée", "duration", LevelService.getLevels(Level.CODE.Instrument),String.class, false, 
        		null, "60",MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_TIME),MeasureUnit.find.findByCode( "s"),MeasureUnit.find.findByCode( "s"), "single", 3));
		return propertyDefinitions;
	}


	private static List<PropertyDefinition> getNanoporeDepotProperties() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
        propertyDefinitions.add(newPropertiesDefinition("Code Flowcell", "containerSupportCode", LevelService.getLevels(Level.CODE.Instrument),String.class, true, "single",100));
        propertyDefinitions.add(newPropertiesDefinition("Version Flowcell", "flowcellChemistry", LevelService.getLevels(Level.CODE.Instrument),String.class, true, "single",200));
        //Liste a definir
        propertyDefinitions.add(newPropertiesDefinition("Numero PC", "pcNumber", LevelService.getLevels(Level.CODE.Instrument),String.class, true, "single",300));
       // propertyDefinitions.add(newPropertiesDefinition("Version MinKNOW", "minKnowVersion",LevelService.getLevels(Level.CODE.Instrument),String.class,false,"single",400));
	//	propertyDefinitions.add(newPropertiesDefinition("Version Metrichor", "metrichorVersion",LevelService.getLevels(Level.CODE.Instrument),String.class,false,"single",500));
	//	propertyDefinitions.add(newPropertiesDefinition("Metrichor run ID", "metrichorRunId",LevelService.getLevels(Level.CODE.Instrument),String.class,false,"single",600));

		return propertyDefinitions;
	}


	private static List<PropertyDefinition> getCBotProperties() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
        propertyDefinitions.add(newPropertiesDefinition("Type lectures","sequencingProgramType"
        		, LevelService.getLevels(Level.CODE.Instrument,Level.CODE.ContainerSupport),String.class, true,DescriptionFactory.newValues("SR","PE"),"single"));
      //  propertyDefinitions.add(newPropertiesDefinition("Type flowcell","flowcellType"
       // 		, LevelService.getLevels(Level.CODE.Instrument),String.class, true,DescriptionFactory.newValues("Paired End FC Hiseq-v3","Single FC Hiseq-v3","Rapid FC PE HS 2500-v1","Rapid FC SR HS 2500-v1"),"single"));
        propertyDefinitions.add(newPropertiesDefinition("Code Flowcell", "containerSupportCode", LevelService.getLevels(Level.CODE.Instrument),String.class, true, "single",100, "n/a"));
//		propertyDefinitions.add(newPropertiesDefinition("Code Flowcell", "containerSupportCode", LevelService.getLevels(Level.CODE.Instrument),String.class, true, "single"));
//        propertyDefinitions.add(newPropertiesDefinition("Piste contrôle","controlLane", LevelService.getLevels(Level.CODE.Instrument),String.class, true,DescriptionFactory.newValuesWithDefault("Pas de piste contrôle (auto-calibrage)","Pas de piste contrôle (auto-calibrage)","1",
//        		"2","3","4","5","6","7","8"),"Pas de piste contrôle (auto-calibrage)","single"));
        return propertyDefinitions;
	}

	
	private static List<PropertyDefinition> getCBotInterneProperties() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
        propertyDefinitions.add(newPropertiesDefinition("Type lectures","sequencingProgramType"
        		, LevelService.getLevels(Level.CODE.Instrument,Level.CODE.ContainerSupport),String.class, true,DescriptionFactory.newValues("SR","PE"),"single"));
     //   propertyDefinitions.add(newPropertiesDefinition("Type flowcell","flowcellType"
        //		, LevelService.getLevels(Level.CODE.Instrument),String.class, true,DescriptionFactory.newValues("Rapid FC PE HS 2500-v1","Rapid FC SR HS 2500-v1",
        	//			"FC Miseq-v2","FC Miseq-v3"),"single"));
        propertyDefinitions.add(newPropertiesDefinition("Code Flowcell", "containerSupportCode", LevelService.getLevels(Level.CODE.Instrument),String.class, true, "single"));
//        propertyDefinitions.add(newPropertiesDefinition("Piste contrôle","controlLane", LevelService.getLevels(Level.CODE.Instrument),String.class, true,DescriptionFactory.newValuesWithDefault("Pas de piste contrôle (auto-calibrage)","Pas de piste contrôle (auto-calibrage)","1",
//        		"2"),"Pas de piste contrôle (auto-calibrage)","single"));
        return propertyDefinitions;
	}
	
//	private static List<PropertyDefinition> getHiseq2000Properties() throws DAOException {
//		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
//        propertyDefinitions.add(newPropertiesDefinition("Position","position"
//        		, LevelService.getLevels(Level.CODE.Instrument),String.class, true,DescriptionFactory.newValues("A","B"), "single",200));
//        propertyDefinitions.add(newPropertiesDefinition("Type lectures", "sequencingProgramType", LevelService.getLevels(Level.CODE.Instrument),String.class, true,DescriptionFactory.newValues("SR","PE"), "single",300));
//        propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read1", "nbCyclesRead1", LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",400));
//        propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read Index1", "nbCyclesReadIndex1", LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",500));
//        propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read2", "nbCyclesRead2", LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",700));
//        propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read Index2", "nbCyclesReadIndex2", LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",600));
////        propertyDefinitions.add(newPropertiesDefinition("Piste contrôle","controlLane", LevelService.getLevels(Level.CODE.Instrument),String.class, true,DescriptionFactory.newValuesWithDefault("Pas de piste contrôle (auto-calibrage)","Pas de piste contrôle (auto-calibrage)","1",
////        		"2","3","4","5","6","7","8"),"Pas de piste contrôle (auto-calibrage)","single",100));
//        return propertyDefinitions;
//	}
	private static List<PropertyDefinition> getMiseqProperties() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
		propertyDefinitions.add(newPropertiesDefinition("Nom cassette Miseq", "miseqReagentCassette",LevelService.getLevels(Level.CODE.Instrument),String.class,true,"single",100));
        propertyDefinitions.add(newPropertiesDefinition("Type lectures", "sequencingProgramType", LevelService.getLevels(Level.CODE.Instrument),String.class, true,DescriptionFactory.newValues("SR","PE"), "single",200));
        propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read1", "nbCyclesRead1", LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",300));
        propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read Index1", "nbCyclesReadIndex1", LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",400));
        propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read2", "nbCyclesRead2", LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",600));
        propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read Index2", "nbCyclesReadIndex2", LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",500));
        return propertyDefinitions;
	}
	

//	private static List<PropertyDefinition> getHiseq2500Properties() throws DAOException {
//		List<PropertyDefinition> propertyDefinitions = getHiseq2000Properties();		
//	   propertyDefinitions.add(0, newPropertiesDefinition("Mode run","runMode"
//	        		, LevelService.getLevels(Level.CODE.Instrument),String.class, true,DescriptionFactory.newValues("normal","rapide"), "single",50));
//        return propertyDefinitions;
//	}
	

//	private static List<Instrument> getInstrumentEppendorfMiniSpinPlus() throws DAOException {
//		List<Instrument> instruments=new ArrayList<Instrument>();
//		instruments.add(createInstrument("MiniSpin plus 1", "miniSpinPlus1", null, true, "path", DescriptionFactory.getInstitutes(Constants.CODE.GET)));
//		return instruments;
//	}

	
	

	private static List<PropertyDefinition> getCovarisProperties() throws DAOException {
		List<PropertyDefinition> l = new ArrayList<PropertyDefinition>();
		l.add(newPropertiesDefinition("Programme", "program", LevelService.getLevels(Level.CODE.Instrument), String.class, true, newValues("Frag_PE300","Frag_PE400","Frag_PE500","Frag_cDNA_Solexa"), "single"));
		return l;
	}
	
//	private static List<PropertyDefinition> getSpriProperties() throws DAOException {
//		List<PropertyDefinition> l = new ArrayList<PropertyDefinition>();
//		l.add(newPropertiesDefinition("Programme", "program", LevelService.getLevels(Level.CODE.Instrument), String.class, true, newValues("300-600"), "single"));		
//		return l;
//	}
	
	private static List<PropertyDefinition> getThermocyclerProperties() throws DAOException {
		List<PropertyDefinition> l = new ArrayList<PropertyDefinition>();
		l.add(newPropertiesDefinition("Programme", "program", LevelService.getLevels(Level.CODE.Instrument), String.class, true, newValues("15","18"), "single"));		
		return l;
	}
	
	private static List<PropertyDefinition> getChipElectrophoresisProperties() throws DAOException {
		List<PropertyDefinition> l = new ArrayList<PropertyDefinition>();
		l.add(newPropertiesDefinition("Type puce", "chipType", LevelService.getLevels(Level.CODE.Instrument), String.class, true, newValues("DNA HS", "DNA 12000", "RNA"), "single"));		
		return l;
	}
	
	private static List<PropertyDefinition> getQuBitProperties() throws DAOException {
		List<PropertyDefinition> l = new ArrayList<PropertyDefinition>();
		l.add(newPropertiesDefinition("Kit", "kit", LevelService.getLevels(Level.CODE.Instrument), String.class, true, newValues("HS", "BR"), "single"));		
		return l;
	}
	
	private static List<PropertyDefinition> getQPCRProperties() throws DAOException {
		List<PropertyDefinition> l = new ArrayList<PropertyDefinition>();
		l.add(newPropertiesDefinition("Nb. Echantillon", "sampleNumber", LevelService.getLevels(Level.CODE.Instrument), Integer.class, true, "single"));		
		return l;
	}
		
	

}
