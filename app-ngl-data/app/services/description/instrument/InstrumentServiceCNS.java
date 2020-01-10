package services.description.instrument;

import static services.description.DescriptionFactory.newInstrumentCategory;
import static services.description.DescriptionFactory.newInstrumentUsedType;
import static services.description.DescriptionFactory.newPropertiesDefinition;
import static services.description.DescriptionFactory.newValue;
import static services.description.DescriptionFactory.newValues;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import models.laboratory.common.description.Level;
import models.laboratory.common.description.MeasureCategory;
import models.laboratory.common.description.MeasureUnit;
import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.common.description.Value;
import models.laboratory.instrument.description.Instrument;
import models.laboratory.instrument.description.InstrumentCategory;
import models.laboratory.instrument.description.InstrumentUsedType;
import models.utils.dao.DAOException;
import models.utils.dao.DAOHelpers;
import play.data.validation.ValidationError;
import services.description.Constants;
import services.description.DescriptionFactory;
import services.description.common.LevelService;
import services.description.common.MeasureService;

public class InstrumentServiceCNS extends AbstractInstrumentService{
	
	@Override
	public void saveInstrumentCategories(Map<String, List<ValidationError>> errors) throws DAOException {
		List<InstrumentCategory> l = new ArrayList<>();
		l.add(newInstrumentCategory("Covaris","covaris"));
		l.add(newInstrumentCategory("Spri","spri"));
		l.add(newInstrumentCategory("Thermocycleur","thermocycler"));
		l.add(newInstrumentCategory("Centrifugeuse","centrifuge"));

		
		l.add(newInstrumentCategory("Quantification par fluorométrie","fluorometer"));
		l.add(newInstrumentCategory("Appareil de qPCR","qPCR-system"));
		l.add(newInstrumentCategory("Electrophorèse sur puce","chip-electrophoresis"));
		l.add(newInstrumentCategory("Electrophorèse sur gel","gel-electrophoresis"));
		
		l.add(newInstrumentCategory("Main","hand"));
		l.add(newInstrumentCategory("CBot","cbot"));
		
		l.add(newInstrumentCategory("Séquenceur Illumina","illumina-sequencer"));
		l.add(newInstrumentCategory("Cartographie Optique Opgen","opt-map-opgen"));
		l.add(newInstrumentCategory("Séquenceur Nanopore","nanopore-sequencer"));
		
		//l.add(newInstrumentCategory("Extérieur","extseq"));
		
		l.add(newInstrumentCategory("Robot pipetage","liquid-handling-robot"));
		l.add(newInstrumentCategory("Appareil de sizing","sizing-system"));
				
		l.add(newInstrumentCategory("Cartographie optique BioNano", "opt-map-bionano"));
		
		l.add(newInstrumentCategory("Robot pipetage + Appareil de qPCR", "liquid-handling-robot-and-qPCR-system"));
		l.add(newInstrumentCategory("Système de broyage", "sample-prep-system"));
		l.add(newInstrumentCategory("Robot pipetage + Thermocycleur", "liquid-handling-robot-and-thermocycler"));
		l.add(newInstrumentCategory("Thermocycleur + Robot pipetage", "thermocycler-and-liquid-handling-robot"));
		l.add(newInstrumentCategory("Robot pipetage + Covaris", "liquid-handling-robot-and-covaris"));
		l.add(newInstrumentCategory("Hydroshear","hydroshear"));
		l.add(newInstrumentCategory("Spectrophotomètre", "spectrophotometer"));
		l.add(newInstrumentCategory("Appareil de Prep. Librairie Nanopore","nanopore-library-prep-device"));
		
		DAOHelpers.saveModels(InstrumentCategory.class, l, errors);
		
	}
	
	@Override
	public void saveInstrumentUsedTypes(Map<String, List<ValidationError>> errors) throws DAOException {
		
		List<InstrumentUsedType> l = new ArrayList<>();
		
		//CNS
		/* TODO GA : A SUPPRIMER ?
		l.add(newInstrumentUsedType("Covaris S2", "covaris-s2", InstrumentCategory.find.findByCode("covaris"), getCovarisProperties(), 
				getInstruments(
						createInstrument("Covaris1", "Covaris1", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.CNS)), 
						createInstrument("Covaris2", "Covaris2", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.CNS)) ) ,
				getContainerSupportCategories(new String[]{"tube"}),getContainerSupportCategories(new String[]{"tube"}), 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		*/
		l.add(newInstrumentUsedType("Spri", "spri", InstrumentCategory.find.findByCode("spri"), getSpriProperties(), 
				getInstruments(
						createInstrument("Spri1", "Spri1", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.CNS)), 
						createInstrument("Spri2", "Spri2", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.CNS)), 
						createInstrument("Spri3", "Spri3", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.CNS)) ), 
				getContainerSupportCategories(new String[]{"tube"}),getContainerSupportCategories(new String[]{"tube"}), 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(newInstrumentUsedType("Fluoroskan", "fluoroskan", InstrumentCategory.find.findByCode("fluorometer"),getQuBitFluoroskanProperties(), 
				getInstruments(
						createInstrument("Fluoroskan1", "Fluoroskan1", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.CNS))), 
				getContainerSupportCategories(new String[]{"tube","96-well-plate"}),null, 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS))); //ok
		
		
		l.add(newInstrumentUsedType("Stratagene qPCR system", "stratagene-qPCR", InstrumentCategory.find.findByCode("qPCR-system"), null, 
				getInstruments(
						createInstrument("Stratagene1", "Stratagene1", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.CNS))),
				getContainerSupportCategories(new String[]{"tube","96-well-plate"}), null, 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(newInstrumentUsedType("cBot-interne", "cBot-interne", InstrumentCategory.find.findByCode("cbot"), getCBotInterneProperties(), 
				getInstruments(
						createInstrument("cBot Fluor A", "cBot-Fluor-A", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.CNS)),
						createInstrument("cBot Fluor B", "cBot-Fluor-B", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.CNS)),
						createInstrument("cBot Platine A", "cBot-Platine-A", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.CNS)),
						createInstrument("cBot Platine B", "cBot-Platine-B", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.CNS)),
						createInstrument("cBot Mimosa", "cBot-Mimosa", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.CNS)),
						createInstrument("cBot Melisse", "cBot-Melisse", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.CNS))), 
				getContainerSupportCategories(new String[]{"96-well-plate", "tube"}), getContainerSupportCategories(new String[]{"flowcell-2","flowcell-1"}), 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(newInstrumentUsedType("cBot-interne-NovaSeq", "cbot-onboard-novaseq", InstrumentCategory.find.findByCode("cbot"), getCBotOnBoardNovaSeqProperties(), 
				getInstruments(
						createInstrument("cBot Jarvis A", "cBot-Jarvis-A", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.CNS)),
						createInstrument("cBot Jarvis B", "cBot-Jarvis-B", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.CNS))),
						getContainerSupportCategories(new String[]{"96-well-plate", "tube"}), getContainerSupportCategories(new String[]{"flowcell-2","flowcell-4"}), 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(newInstrumentUsedType("NovaSeq Xp Flow Cell Dock", "novaseq-xp-fc-dock", InstrumentCategory.find.findByCode("cbot"), getCBotOnBoardNovaSeqProperties(), 
				getInstruments(
						createInstrument( "novaseq-xp-fc-dock-1", "NovaSeq Xp Flow Cell Dock 1", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.CNS))),
						getContainerSupportCategories(new String[]{"96-well-plate", "tube"}), getContainerSupportCategories(new String[]{"flowcell-2","flowcell-4"}), 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		
		l.add(newInstrumentUsedType("ARGUS", "ARGUS", InstrumentCategory.find.findByCode("opt-map-opgen"), getArgusProperties(), 
				getInstrumentOpgen(),
				getContainerSupportCategories(new String[]{"tube"}),getContainerSupportCategories(new String[]{"mapcard"}), 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));	
		
		/*l.add(newInstrumentUsedType("EXTSOLEXA", "EXTSOLEXA", InstrumentCategory.find.findByCode("extseq"), null, 
				getInstrumentExtSolexa(),
				getContainerSupportCategories(new String[]{"flowcell-2","flowcell-1","flowcell-8"}),null, 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));*/
		
		l.add(newInstrumentUsedType("Biomek FX", "biomek-fx", InstrumentCategory.find.findByCode("liquid-handling-robot"), null, 
				getInstruments(
						createInstrument("walle", "WALL-E", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.CNS)), 
						createInstrument("r2d2", "R2D2", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.CNS)),
						createInstrument("maya", "MAYA", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.CNS)) ) ,
				getContainerSupportCategories(new String[]{"tube"}),getContainerSupportCategories(new String[]{"96-well-plate", "tube"}), 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

		
		l.add(newInstrumentUsedType("Brand LHS", "brand-lhs", InstrumentCategory.find.findByCode("liquid-handling-robot"), null, 
				getInstruments(
						createInstrument("celeste", "CELESTE", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.CNS)) ), 
						getContainerSupportCategories(new String[]{"96-well-plate","tube"}),getContainerSupportCategories(new String[]{"96-well-plate"}), 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		
		l.add(newInstrumentUsedType("TECAN evo 100", "tecan-evo-100", InstrumentCategory.find.findByCode("liquid-handling-robot"), getTecanProperties(), 
				getInstruments(
						createInstrument("wolverine", "Wolverine", null, true, "/env/cns/proj/bureautique/atelier/SOLEXA/Solstock_TECAN/", DescriptionFactory.getInstitutes(Constants.CODE.CNS)),
						createInstrument("arrow", "Arrow", null, true, "/env/cns/proj/bureautique/atelier/SOLEXA/Solstock_TECAN/", DescriptionFactory.getInstitutes(Constants.CODE.CNS))),
						getContainerSupportCategories(new String[]{"tube","96-well-plate"}),getContainerSupportCategories(new String[]{"tube","96-well-plate"}), 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

		l.add(newInstrumentUsedType("TECAN Evo 100 / qPCR Stratagene", "tecan-evo-100-and-stratagene-qPCR-system", InstrumentCategory.find.findByCode("liquid-handling-robot-and-qPCR-system"),
				getTecanStrategeneProperties(), 
				getInstruments(
						createInstrument("wolverine-stratagene1", "Arrow / Stratagene 1", null, true, "/env/cns/proj/bureautique/atelier/SOLEXA/Solstock_TECAN/", DescriptionFactory.getInstitutes(Constants.CODE.CNS)),
						createInstrument("wolverine-stratagene2", "Arrow / Stratagene 2", null, true, "/env/cns/proj/bureautique/atelier/SOLEXA/Solstock_TECAN/", DescriptionFactory.getInstitutes(Constants.CODE.CNS)),
						createInstrument("arrow-stratagene1", "Wolwerine / Stratagene 1", null, true, "/env/cns/proj/bureautique/atelier/SOLEXA/Solstock_TECAN/", DescriptionFactory.getInstitutes(Constants.CODE.CNS)),
						createInstrument("arrow-stratagene2", "Wolwerine / Stratagene 2", null, true, "/env/cns/proj/bureautique/atelier/SOLEXA/Solstock_TECAN/", DescriptionFactory.getInstitutes(Constants.CODE.CNS))),						
						getContainerSupportCategories(new String[]{"tube","96-well-plate"}),getContainerSupportCategories(new String[]{"tube","96-well-plate"}), 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

		
		//TODO : verify getContainerSupportCategories parameters
		l.add(newInstrumentUsedType("Blue pippin", "blue-pippin", InstrumentCategory.find.findByCode("sizing-system"), getBluePippinProperties(), 
				getInstrumentBluePippin(),
						getContainerSupportCategories(new String[]{"tube"}),getContainerSupportCategories(new String[]{"tube"}), 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

		l.add(newInstrumentUsedType("Main", "hand", InstrumentCategory.find.findByCode("hand"), null, 
				getInstruments(
						createInstrument("hand", "Main", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.CNS)) ),
				getContainerSupportCategories(new String[]{"tube","96-well-plate"}),getContainerSupportCategories(new String[]{"tube","96-well-plate"}), 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(newInstrumentUsedType("QuBit", "qubit", InstrumentCategory.find.findByCode("fluorometer"), getQuBitFluoroskanProperties(), 
				getInstruments(
						createInstrument("quBit1", "QuBit1 ARN", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.CNS)),
						createInstrument("QuBit2", "QuBit2 EVAL", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.CNS)),
						createInstrument("QuBit3", "QuBit3 ADN", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.CNS)),
						createInstrument("QuBit4", "QuBit4 AMPLI", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.CNS)),
						createInstrument("QuBit5", "QuBit5 CDNA", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.CNS)),
						createInstrument("QuBit6", "QuBit6 NANOPORE", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.CNS)),
						createInstrument("QuBit7", "QuBit7 HPM (LBiomeG)", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.CNS))
						), 
				getContainerSupportCategories(new String[]{"tube"}),null, 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS))); //ok
		
		l.add(newInstrumentUsedType("GAIIx", "GAIIx", InstrumentCategory.find.findByCode("illumina-sequencer"), null, 
				getInstrumentGAII(),
				getContainerSupportCategories(new String[]{"flowcell-8"}), null, 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(newInstrumentUsedType("MISEQ", "MISEQ", InstrumentCategory.find.findByCode("illumina-sequencer"), getMiseqProperties(), 
				getInstrumentMiSeq(),
				getContainerSupportCategories(new String[]{"flowcell-1"}), null, 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(newInstrumentUsedType("HISEQ2000", "HISEQ2000", InstrumentCategory.find.findByCode("illumina-sequencer"), getHiseq2000Properties(), 
				getInstrumentHiseq2000(),
				getContainerSupportCategories(new String[]{"flowcell-8"}), null, 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(newInstrumentUsedType("HISEQ2500", "HISEQ2500", InstrumentCategory.find.findByCode("illumina-sequencer"), getHiseq2500Properties(), 
				getInstrumentHiseq2500(),
				getContainerSupportCategories(new String[]{"flowcell-8","flowcell-2"}), null, 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(newInstrumentUsedType("HISEQ4000", "HISEQ4000", InstrumentCategory.find.findByCode("illumina-sequencer"), getHiseq4000Properties(), 
				getInstrumentHiseq4000(),
				getContainerSupportCategories(new String[]{"flowcell-8"}), null, 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(newInstrumentUsedType("NOVASEQ6000", "NOVASEQ6000", InstrumentCategory.find.findByCode("illumina-sequencer"), getNovaseq6000Properties(), 
				getInstrumentNovaseq6000(),
				getContainerSupportCategories(new String[]{"flowcell-2","flowcell-4"}), 
				null, 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));	
		
		l.add(newInstrumentUsedType("Thermocycleur", "thermocycler", InstrumentCategory.find.findByCode("thermocycler"), getThermocyclerProperties(), 
				getThermocyclerInstruments(),
				getContainerSupportCategories(new String[]{"tube","96-well-plate"}),getContainerSupportCategories(new String[]{"tube","96-well-plate"}), 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(newInstrumentUsedType("cBot", "cBot", InstrumentCategory.find.findByCode("cbot"), getCBotProperties(), 
				getInstruments(
						createInstrument("cBot1", "cBot1", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.CNS)),
						createInstrument("cBot2", "cBot2", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.CNS)),
						createInstrument("cBot3", "cBot3", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.CNS)),
						createInstrument("cBot4", "cBot4", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.CNS))), 
				
				getContainerSupportCategories(new String[]{"96-well-plate", "tube"}), getContainerSupportCategories(new String[]{"flowcell-8","flowcell-2"}), 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(newInstrumentUsedType("Covaris E220", "covaris-e220", InstrumentCategory.find.findByCode("covaris"), getCovarisProperties(), 
				getInstruments(
						createInstrument("covarisE220_1", "CovarisE220_1", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.CNS))) , 
				getContainerSupportCategories(new String[]{"tube","96-well-plate"}),getContainerSupportCategories(new String[]{"tube","96-well-plate"}), 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(newInstrumentUsedType("Covaris E220 extérieur", "covaris-e220-ext", InstrumentCategory.find.findByCode("covaris"), null, 
				getInstruments(
						createInstrument("covarisE220CNG", "CovarisE220_CNG", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.CNS)) ) , 
				getContainerSupportCategories(new String[]{"tube","96-well-plate"}),getContainerSupportCategories(new String[]{"tube","96-well-plate"}), 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		/* TODO A supprimer ???
		l.add(newInstrumentUsedType("Covaris E210", "covaris-e210", InstrumentCategory.find.findByCode("covaris"), getCovarisProperties(), 
				getInstruments(
						createInstrument("covaris3", "Covaris3", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.CNS)), 
						createInstrument("covaris4", "Covaris4", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.CNS))) , 
				getContainerSupportCategories(new String[]{"tube"}),getContainerSupportCategories(new String[]{"tube"}), 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		*/
		
		l.add(newInstrumentUsedType("LabChip_GX", "labchip-gx", InstrumentCategory.find.findByCode("chip-electrophoresis"), getLabChipGXProperties(), 
				getInstruments(
						createInstrument("labChip_GX1", "LabChip_GX1 ADN", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.CNS)),
						createInstrument("labChip_GX2", "LabChip_GX2 AMPLI", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.CNS))) ,
				getContainerSupportCategories(new String[]{"96-well-plate"}),null, 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(newInstrumentUsedType("Tapestation", "tapestation", InstrumentCategory.find.findByCode("chip-electrophoresis"), getTapestationProperties(), 
				getInstruments(
						createInstrument("tapestation-inra", "Tapestation INRA", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.CNS))),
						getContainerSupportCategories(new String[]{"96-well-plate","tube"}),null, 
						DescriptionFactory.getInstitutes(Constants.CODE.CNS)));	
		
		l.add(newInstrumentUsedType("Agilent 2100 bioanalyzer", "agilent-2100-bioanalyzer", InstrumentCategory.find.findByCode("chip-electrophoresis"), getBioanalyzerProperties(), 
				getInstruments(
						createInstrument("bioanalyzer1", "Bioanalyzer 1 ADN", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.CNS)),
						createInstrument("bioanalyzer2", "Bioanalyzer 2 ARN", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.CNS)),
						createInstrument("bioanalyzer3", "Bioanalyzer 3 AMPLI", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.CNS))),
				getContainerSupportCategories(new String[]{"tube"}),null, 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(newInstrumentUsedType("Eppendorf MiniSpin plus", "eppendorf-mini-spin-plus", InstrumentCategory.find.findByCode("centrifuge"), getNanoporeMiniSpinProperties(),  getInstrumentEppendorfMiniSpinPlus()
				,getContainerSupportCategories(new String[]{"tube"}), getContainerSupportCategories(new String[]{"tube"}), DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(newInstrumentUsedType("Megaruptor 2", "megaruptor2", InstrumentCategory.find.findByCode("hydroshear"), getNanoporeMegaruptor2Properties(),  getInstrumentMegaruptor2()
				,getContainerSupportCategories(new String[]{"tube"}), getContainerSupportCategories(new String[]{"tube"}), DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		//DAOHelpers.saveModels(InstrumentUsedType.class, l, errors);
		
		
		l.add(newInstrumentUsedType("MinION", "minION", InstrumentCategory.find.findByCode("nanopore-sequencer"), getNanoporeSequencerProperties(),getInstrumentMinIon() 
				,getContainerSupportCategories(new String[]{"tube"}), getContainerSupportCategories(new String[]{"flowcell-1"}), DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(newInstrumentUsedType("Mk1", "mk1", InstrumentCategory.find.findByCode("nanopore-sequencer"), getNanoporeSequencerProperties(),getInstrumentMKI() 
				,getContainerSupportCategories(new String[]{"tube"}), getContainerSupportCategories(new String[]{"flowcell-1"}), DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(newInstrumentUsedType("Mk1B", "mk1b", InstrumentCategory.find.findByCode("nanopore-sequencer"), getNanoporeSequencerProperties(),getInstrumentMKIB() 
				,getContainerSupportCategories(new String[]{"tube"}), getContainerSupportCategories(new String[]{"flowcell-1"}), DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(newInstrumentUsedType("PromethION", "promethION", InstrumentCategory.find.findByCode("nanopore-sequencer"), getPromethIONProperties(),getInstrumentPromethION() 
				,getContainerSupportCategories(new String[]{"tube"}), getContainerSupportCategories(new String[]{"flowcell-1"}), DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(newInstrumentUsedType("IRYS", "IRYS", InstrumentCategory.find.findByCode("opt-map-bionano"), getIrysDepotProperties(),getInstrumentIRYS() 
				,getContainerSupportCategories(new String[]{"irys-chip-2"}), null, DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(newInstrumentUsedType("SAPHYR", "SAPHYR", InstrumentCategory.find.findByCode("opt-map-bionano"), getIrysDepotProperties(),getInstrumentSAPHYR() 
				,getContainerSupportCategories(new String[]{"saphyr-chip"}), null, DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(newInstrumentUsedType("Main", "irys-hand", InstrumentCategory.find.findByCode("hand"), getIrysChipProperties(),getInstrumentBionanoHand() 
				,getContainerSupportCategories(new String[]{"tube"}), getContainerSupportCategories(new String[]{"irys-chip-2","saphyr-chip"}), DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		//l.add(newInstrumentUsedType("Main", "saphyr-hand", InstrumentCategory.find.findByCode("hand"), getSaphyrChipProperties(),getInstrumentBionanoHand() 
			//	,getContainerSupportCategories(new String[]{"tube"}), getContainerSupportCategories(new String[]{"saphyr-chip"}), DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
	
		l.add(newInstrumentUsedType("Cryobroyeur", "cryobroyeur", InstrumentCategory.find.findByCode("sample-prep-system"),  getCryobroyeurProperties(),getInstrumentCryobroyeur() 
				,getContainerSupportCategories(new String[]{"tube","bottle","bag"}), getContainerSupportCategories(new String[]{"tube"}), DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

		l.add(newInstrumentUsedType("Fast Prep", "fast-prep", InstrumentCategory.find.findByCode("sample-prep-system"),  getFastPrepProperties(),getInstrumentFastPrep() 
				,getContainerSupportCategories(new String[]{"tube","bottle","bag"}), getContainerSupportCategories(new String[]{"tube"}), DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

		
		l.add(newInstrumentUsedType("Thermocycleur + Biomek FX", "thermocycler-and-biomek-fx", InstrumentCategory.find.findByCode("liquid-handling-robot-and-thermocycler"),  
				getThermoBiomekProperties(),	getInstrumentBiomekFx() 
				,getContainerSupportCategories(new String[]{"96-well-plate"}), getContainerSupportCategories(new String[]{"96-well-plate"}), DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(newInstrumentUsedType("Biomek FX + Covaris E220", "biomek-fx-and-covaris-e220", InstrumentCategory.find.findByCode("liquid-handling-robot-and-covaris"),  
					getCovarisBiomekProperties(),getInstrumentBiomekCovaris() 
				,getContainerSupportCategories(new String[]{"96-well-plate","tube"}), getContainerSupportCategories(new String[]{"96-well-plate"}), DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(newInstrumentUsedType("Nanodrop", "nanodrop", InstrumentCategory.find.findByCode("spectrophotometer"),  null,getInstrumentNanodrop() 
				,getContainerSupportCategories(new String[]{"tube"}), getContainerSupportCategories(new String[]{"tube"}), DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

		
		l.add(newInstrumentUsedType("Pippin Pulse", "pippin-pulse", InstrumentCategory.find.findByCode("gel-electrophoresis"),  
				null, getInstrumentPippinPulse()
				,getContainerSupportCategories(new String[]{"tube"}), null, DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(newInstrumentUsedType("VolTRAX", "voltrax", InstrumentCategory.find.findByCode("nanopore-library-prep-device"),  
				null, getInstrumentVoltrax()
				,getContainerSupportCategories(new String[]{"tube"}),getContainerSupportCategories(new String[]{"tube"}), DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		
		DAOHelpers.saveModels(InstrumentUsedType.class, l, errors);
	}
	
	private List<Instrument> getThermocyclerInstruments() {
		List<Instrument> instruments = new ArrayList<>();
		for(int i = 1; i <= 42; i++){
			if(i < 10){
				instruments.add(createInstrument("thermo0"+i, "Thermo_0"+i, null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.CNS))); 				
			}else{
				instruments.add(createInstrument("thermo"+i, "Thermo_"+i, null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.CNS))); 
				
			}
			instruments.add(createInstrument("thermo0"+i, "Thermo_0"+i, null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.CNS))); 				
			
		}
		instruments.add(createInstrument("FC1-Cycler", "FC1 Cycler", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.CNS))); 
		return instruments;
	}

	private List<Instrument> getInstrumentBiomekCovaris() {
		List<Instrument> instruments=new ArrayList<>();
		instruments.add(createInstrument("walle-and-covarise220-1","WALL-E / covarisE220_1", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		instruments.add(createInstrument("walle-and-covarise220-cng","WALL-E / covarise220 CNG", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		instruments.add(createInstrument("r2d2-and-covarise220-1","R2D2 / covarisE220_1", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		instruments.add(createInstrument("r2d2-and-covarise220-cng","R2D2 / covarise220 CNG", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		instruments.add(createInstrument("maya-and-covarise220-1","MAYA / covarisE220_1", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		instruments.add(createInstrument("maya-and-covarise220-cng","MAYA / covarise220 CNG", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		return instruments;
	}

	private List<PropertyDefinition> getCovarisBiomekProperties() {
		List<PropertyDefinition> l = new ArrayList<>();
		//l.add(newPropertiesDefinition("Programme Biomek", "biomekProgram", LevelService.getLevels(Level.CODE.Instrument), Integer.class, true, null, null, 
		//		"single", 10, true, null,null));
		l.add(newPropertiesDefinition("Programme Covaris", "covarisProgram", LevelService.getLevels(Level.CODE.Instrument), String.class, true, null, 
				newValues("Low cost 400","Low cost 500","Low cost LC400","Low cost LC500","Low cost LC300","Frag CDNA","Projet BCB","MP Nextera post circ","PCR-Free"), 
				"single", 10, true, null,null));
		return l;		
	}

	private List<PropertyDefinition> getThermoBiomekProperties() {
		List<PropertyDefinition> l = new ArrayList<>();
		/*l.add(newPropertiesDefinition("Nb cycles", "nbCycles", LevelService.getLevels(Level.CODE.Instrument), Integer.class, false, null, null, 
				"single", 10, true, null,null));*/
		
		//Thermo 1 -> 39 Thermo_01
		l.add(newPropertiesDefinition("Thermocycleur", "thermocycler", LevelService.getLevels(Level.CODE.Instrument), String.class, false, null, getThermoclyclerPropertyValues(), 
				"single", 10, true, null,null));
		
		return l;
	}
	
	private List<Value> getThermoclyclerPropertyValues() {
		List<Value> values = getThermocyclerInstruments().stream().map(i -> newValue(i.code, i.name)).collect(Collectors.toList());
		return values;
	}

	private List<Instrument> getInstrumentBiomekFx() {
		List<Instrument> instruments=new ArrayList<>();
		instruments.add(createInstrument("thermoX-and-maya","Thermo_X / MAYA", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		instruments.add(createInstrument("thermoX-and-r2d2","Thermo_X / R2D2", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		instruments.add(createInstrument("thermoX-and-walle","Thermo_X / WALL-E", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		return instruments;
	}

	
	private List<Instrument> getInstrumentFastPrep() {
		List<Instrument> instruments=new ArrayList<>();
		instruments.add(createInstrument("fast-prep-1","Fast Prep 1", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		return instruments;
	}

	private List<PropertyDefinition> getFastPrepProperties() {
		List<PropertyDefinition> l = new ArrayList<>();
		l.add(newPropertiesDefinition("Programme", "program", LevelService.getLevels(Level.CODE.Instrument), String.class, true, null, newValues("Programme corail"), 
				"single", 10, true, null,null));
		return l;
	}	
	
	private List<Instrument> getInstrumentBluePippin() {
		List<Instrument> instruments=new ArrayList<>();
	//	instruments.add(createInstrument("blue-pippin","Blue Pippin 1", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		instruments.add(createInstrument("blue-pippin-1", "Blue pippin 1", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		instruments.add(createInstrument("blue-pippin-2", "Blue pippin 2", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		return instruments;
	}

	private List<PropertyDefinition> getBluePippinProperties() {
		List<PropertyDefinition> l = new ArrayList<>();
		l.add(newPropertiesDefinition("Marqueur utilisé", "marker", LevelService.getLevels(Level.CODE.Instrument), String.class, false, null, null, 
				"single", 10, true, null,null));
		l.add(newPropertiesDefinition("Cassette", "cassette", LevelService.getLevels(Level.CODE.Instrument), String.class, false, null, null, 
				"single", 11, true, null,null));
		
		return l;
	}
		
	private List<Instrument> getInstrumentBionanoHand() throws DAOException {
		List<Instrument> instruments=new ArrayList<>();
		instruments.add(createInstrument("irys-hand", "Main", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		return instruments;
	}
	


	private List<Instrument> getInstrumentCryobroyeur() throws DAOException {
		List<Instrument> instruments=new ArrayList<>();
		instruments.add(createInstrument("freezer-mill-1","6770 FREEZER/MILL 1", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		instruments.add(createInstrument("large-freezer-mill-1","6870 LARGE FREEZER/MILL 1", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

		return instruments;
	}

	private List<Instrument> getInstrumentVoltrax() throws DAOException {
		List<Instrument> instruments=new ArrayList<>();
		instruments.add(createInstrument("voltrax-1","VolTRAX 1", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		return instruments;
	}

	private List<PropertyDefinition> getCryobroyeurProperties() {
		List<PropertyDefinition> l = new ArrayList<>();
		l.add(newPropertiesDefinition("Programme", "program", LevelService.getLevels(Level.CODE.Instrument), String.class, true, null, newValues("Programme 1"), 
				"single", 10, true, null,null));
		return l;
	}
	
	private static List<PropertyDefinition> getNovaseq6000Properties() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		// idem Hiseq4000, HiseqX !!
		
		propertyDefinitions.add(newPropertiesDefinition("Position","position", LevelService.getLevels(Level.CODE.Instrument),String.class, true,DescriptionFactory.newValues("A","B"), "single",100));
		propertyDefinitions.add(newPropertiesDefinition("Type lectures", "sequencingProgramType", LevelService.getLevels(Level.CODE.Instrument),String.class, true,DescriptionFactory.newValues("SR","PE"), "single",200));
		propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read1", "nbCyclesRead1", LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",300));
		propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read Index1", "nbCyclesReadIndex1", LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",400));
		propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read2", "nbCyclesRead2", LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",600));
		propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read Index2", "nbCyclesReadIndex2", LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",500));
		
		//NGL-1768: nouvelle propriétés
	//	propertyDefinitions.add(newPropertiesDefinition("Tube chargement (RFID)", "novaseqLoadingTube", LevelService.getLevels(Level.CODE.Instrument),String.class, false, "single",600));
		//propertyDefinitions.add(newPropertiesDefinition("Type flowcell", "novaseqFlowcellMode", LevelService.getLevels(Level.CODE.Instrument),String.class, false,DescriptionFactory.newValues("S1","S2","S4"), "single",700));
		
		return propertyDefinitions;
	}
	
	private List<PropertyDefinition> getIrysChipProperties() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
        propertyDefinitions.add(newPropertiesDefinition("Code CHIP", "containerSupportCode", LevelService.getLevels(Level.CODE.Instrument),String.class, true, "single",100));
		return propertyDefinitions;
	}
	
//	private List<PropertyDefinition> getSaphyrChipProperties() throws DAOException {
//		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
//        propertyDefinitions.add(newPropertiesDefinition("Code Saphyr CHIP", "containerSupportCode", LevelService.getLevels(Level.CODE.Instrument),String.class, true, "single",100));
//		return propertyDefinitions;
//	}

	private List<PropertyDefinition> getIrysDepotProperties() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
        propertyDefinitions.add(newPropertiesDefinition("Version Logiciel", "softwareVersion", LevelService.getLevels(Level.CODE.Instrument),String.class, false, "single",100));
		return propertyDefinitions;
	}

	private static List<PropertyDefinition> getNanoporeMiniSpinProperties() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
        propertyDefinitions.add(newPropertiesDefinition("Programme", "program", LevelService.getLevels(Level.CODE.Instrument),String.class, true,
        		DescriptionFactory.newValues("G-TUBE"), "G-TUBE", null, null, null, "single", 1));
        propertyDefinitions.add(newPropertiesDefinition("Vitesse", "speed", LevelService.getLevels(Level.CODE.Instrument),String.class, false,
        		null, "8000", MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_SPEED),MeasureUnit.find.findByCode( "rpm"),MeasureUnit.find.findByCode( "rpm"), "single", 2));
        // unite s
        propertyDefinitions.add(newPropertiesDefinition("Durée", "duration", LevelService.getLevels(Level.CODE.Instrument),String.class, false, 
        		null, "60",MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_TIME),MeasureUnit.find.findByCode( "s"),MeasureUnit.find.findByCode( "s"), "single", 3));
		return propertyDefinitions;
	}
	private List<PropertyDefinition> getTapestationProperties() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		 propertyDefinitions.add(newPropertiesDefinition("Taille estimée Tapestation", "estimatedSize", LevelService.getLevels(Level.CODE.ContainerIn),String.class, false,null,
		 null, "single", 12, true, null, null));
		 return propertyDefinitions;
	}
	
	private List<PropertyDefinition> getNanoporeMegaruptor2Properties() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		 propertyDefinitions.add(newPropertiesDefinition("Taille hydropores", "hydroporeSize", LevelService.getLevels(Level.CODE.Instrument),String.class, true,DescriptionFactory.newValues("short","long"), "single",100));
		return propertyDefinitions;
	}


	private static List<PropertyDefinition> getNanoporeSequencerProperties() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
        propertyDefinitions.add(newPropertiesDefinition("Code Flowcell", "containerSupportCode", LevelService.getLevels(Level.CODE.Instrument),String.class, true, "single",100));
        propertyDefinitions.add(newPropertiesDefinition("Version Flowcell", "flowcellChemistry", LevelService.getLevels(Level.CODE.Instrument,Level.CODE.Content),String.class, true, "single",200,"R9.4.1"));
       
        //Liste a definir
        propertyDefinitions.add(newPropertiesDefinition("Numero PC", "pcNumber", LevelService.getLevels(Level.CODE.Instrument),String.class, true, "single",300));
       // propertyDefinitions.add(newPropertiesDefinition("Version MinKNOW", "minKnowVersion",LevelService.getLevels(Level.CODE.Instrument),String.class,false,"single",400));
	//	propertyDefinitions.add(newPropertiesDefinition("Version Metrichor", "metrichorVersion",LevelService.getLevels(Level.CODE.Instrument),String.class,false,"single",500));
	//	propertyDefinitions.add(newPropertiesDefinition("Metrichor run ID", "metrichorRunId",LevelService.getLevels(Level.CODE.Instrument),String.class,false,"single",600));

		return propertyDefinitions;
	}
	
	private static List<PropertyDefinition> getPromethIONProperties() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
        
		
		propertyDefinitions.add(newPropertiesDefinition("Code Flowcell", "containerSupportCode", LevelService.getLevels(Level.CODE.ContainerOut),String.class, true, null, 
	        		null, "single", 48, true, null, null));
	       
        propertyDefinitions.add(newPropertiesDefinition("Version Flowcell", "flowcellChemistry", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content),String.class, true, null, 
        		null, "single", 49, true, "R9.4-spot-on", null));
        
        propertyDefinitions.add(newPropertiesDefinition("Position", "position", LevelService.getLevels(Level.CODE.ContainerOut), String.class, true, null, 
        		getPromethionPosition(), "single", 50, true, null, null));
        
		return propertyDefinitions;
	}
	
	private static List<Value> getPromethionPosition() {
		List<Value> values = new ArrayList<>();
		values.add(newValue("PH_p-101_0","pl1_A1-D1"));  
		values.add(newValue("PH_p-101_2","pl1_A2-D2"));  
		values.add(newValue("PH_p-105_0","pl1_A3-D3"));  
		values.add(newValue("PH_p-105_2","pl1_A4-D4"));  
		values.add(newValue("PH_p-109_0","pl1_A5-D5"));  
		values.add(newValue("PH_p-109_2","pl1_A6-D6")); 
		values.add(newValue("PH_p-101_1","pl1_E1-H1")); 
		values.add(newValue("PH_p-101_3","pl1_E2-H2")); 
		values.add(newValue("PH_p-105_1","pl1_E3-H3")); 
		values.add(newValue("PH_p-105_3","pl1_E4-H4")); 
		values.add(newValue("PH_p-109_1","pl1_E5-H5")); 
		values.add(newValue("PH_p-109_3","pl1_E6-H6")); 
		values.add(newValue("PH_p-102_0","pl1_A7-D7")); 
		values.add(newValue("PH_p-102_2","pl1_A8-D8")); 
		values.add(newValue("PH_p-106_0","pl1_A9-D9")); 
		values.add(newValue("PH_p-106_2","pl1_A10-D10")); 
		values.add(newValue("PH_p-110_0","pl1_A11-D11")); 
		values.add(newValue("PH_p-110_2","pl1_A12-D12")); 
		values.add(newValue("PH_p-102_1","pl1_E7-H7")); 
		values.add(newValue("PH_p-102_3","pl1_E8-H8")); 
		values.add(newValue("PH_p-106_1","pl1_E9-H9")); 
		values.add(newValue("PH_p-106_3","pl1_E10-H10")); 
		values.add(newValue("PH_p-110_1","pl1_E11-H11")); 
		values.add(newValue("PH_p-110_3","pl1_E12-H12")); 
		values.add(newValue("PH_p-103_0","pl2_A1-D1")); 
		values.add(newValue("PH_p-103_2","pl2_A2-D2")); 
		values.add(newValue("PH_p-107_0","pl2_A3-D3")); 
		values.add(newValue("PH_p-107_2","pl2_A4-D4")); 
		values.add(newValue("PH_p-111_0","pl2_A5-D5")); 
		values.add(newValue("PH_p-111_2","pl2_A6-D6")); 
		values.add(newValue("PH_p-103_1","pl2_E1-H1")); 
		values.add(newValue("PH_p-103_3","pl2_E2-H2")); 
		values.add(newValue("PH_p-107_1","pl2_E3-H3")); 
		values.add(newValue("PH_p-107_3","pl2_E4-H4")); 
		values.add(newValue("PH_p-111_1","pl2_E5-H5")); 
		values.add(newValue("PH_p-111_3","pl2_E6-H6")); 
		values.add(newValue("PH_p-104_0","pl2_A7-D7")); 
		values.add(newValue("PH_p-104_2","pl2_A8-D8")); 
		values.add(newValue("PH_p-108_0","pl2_A9-D9")); 
		values.add(newValue("PH_p-108_2","pl2_A10-D10")); 
		values.add(newValue("PH_p-112_0","pl2_A11-D11")); 
		values.add(newValue("PH_p-112_2","pl2_A12-D12")); 
		values.add(newValue("PH_p-104_1","pl2_E7-H7")); 
		values.add(newValue("PH_p-104_3","pl2_E8-H8")); 
		values.add(newValue("PH_p-108_1","pl2_E9-H9")); 
		values.add(newValue("PH_p-108_3","pl2_E10-H10")); 
		values.add(newValue("PH_p-112_1","pl2_E11-H11")); 
		values.add(newValue("PH_p-112_3","pl2_E12-H12")); 		
		return values;
	}

	


	private static List<PropertyDefinition> getCBotProperties() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
        propertyDefinitions.add(newPropertiesDefinition("Type lectures","sequencingProgramType"
        		, LevelService.getLevels(Level.CODE.Instrument,Level.CODE.ContainerSupport),String.class, true,DescriptionFactory.newValues("SR","PE"),"single"));
      //  propertyDefinitions.add(newPropertiesDefinition("Type flowcell","flowcellType"
       // 		, LevelService.getLevels(Level.CODE.Instrument),String.class, true,DescriptionFactory.newValues("Paired End FC Hiseq-v3","Single FC Hiseq-v3","Rapid FC PE HS 2500-v1","Rapid FC SR HS 2500-v1"),"single"));
        propertyDefinitions.add(newPropertiesDefinition("Code Flowcell", "containerSupportCode", LevelService.getLevels(Level.CODE.Instrument),String.class, true, "single"));
        propertyDefinitions.add(newPropertiesDefinition("Piste contrôle","controlLane", LevelService.getLevels(Level.CODE.Instrument),String.class, true,DescriptionFactory.newValuesWithDefault("Pas de piste contrôle (auto-calibrage)","Pas de piste contrôle (auto-calibrage)","1",
        		"2","3","4","5","6","7","8"),"Pas de piste contrôle (auto-calibrage)","single"));
        return propertyDefinitions;
	}

	
	private static List<PropertyDefinition> getCBotInterneProperties() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
        propertyDefinitions.add(newPropertiesDefinition("Type lectures","sequencingProgramType"
        		, LevelService.getLevels(Level.CODE.Instrument,Level.CODE.ContainerSupport),String.class, true,DescriptionFactory.newValues("SR","PE"),"single"));
     //   propertyDefinitions.add(newPropertiesDefinition("Type flowcell","flowcellType"
        //		, LevelService.getLevels(Level.CODE.Instrument),String.class, true,DescriptionFactory.newValues("Rapid FC PE HS 2500-v1","Rapid FC SR HS 2500-v1",
        	//			"FC Miseq-v2","FC Miseq-v3"),"single"));
        propertyDefinitions.add(newPropertiesDefinition("Code Flowcell", "containerSupportCode", LevelService.getLevels(Level.CODE.Instrument),String.class, true, "single"));
        propertyDefinitions.add(newPropertiesDefinition("Piste contrôle","controlLane", LevelService.getLevels(Level.CODE.Instrument),String.class, true,DescriptionFactory.newValuesWithDefault("Pas de piste contrôle (auto-calibrage)","Pas de piste contrôle (auto-calibrage)","1",
        		"2"),"Pas de piste contrôle (auto-calibrage)","single"));
        return propertyDefinitions;
	}
	
	
	private static List<PropertyDefinition> getCBotOnBoardNovaSeqProperties() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(newPropertiesDefinition("Type lectures","sequencingProgramType"
				, LevelService.getLevels(Level.CODE.Instrument,Level.CODE.ContainerSupport),String.class, true,DescriptionFactory.newValues("SR","PE"),"single"));
		propertyDefinitions.add(newPropertiesDefinition("Type flowcell","novaseqFlowcellMode"
				, LevelService.getLevels(Level.CODE.Instrument,Level.CODE.Content),String.class, true,DescriptionFactory.newValues("S1","S2","S3","S4"),"single"));
		propertyDefinitions.add(newPropertiesDefinition("Code Flowcell", "containerSupportCode", LevelService.getLevels(Level.CODE.Instrument),String.class, true, "single"));
		
		return propertyDefinitions;
	}
	
	private List<PropertyDefinition> getHiseq4000Properties() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		 propertyDefinitions.add(newPropertiesDefinition("Position","position"
        		, LevelService.getLevels(Level.CODE.Instrument),String.class, true,DescriptionFactory.newValues("A","B"), "single",100));
		
		 propertyDefinitions.add(newPropertiesDefinition("Type lectures", "sequencingProgramType", LevelService.getLevels(Level.CODE.Instrument),String.class, true,DescriptionFactory.newValues("SR","PE"), "single",200));
		
		 propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read1", "nbCyclesRead1", LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",300));
	        propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read Index1", "nbCyclesReadIndex1", LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",400));
	        propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read2", "nbCyclesRead2", LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",600));
	        propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read Index2", "nbCyclesReadIndex2", LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",500));
	        
	        return propertyDefinitions;
	}
	

	private static List<PropertyDefinition> getHiseq2000Properties() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
        propertyDefinitions.add(newPropertiesDefinition("Position","position"
        		, LevelService.getLevels(Level.CODE.Instrument),String.class, true,DescriptionFactory.newValues("A","B"), "single",200));
        propertyDefinitions.add(newPropertiesDefinition("Type lectures", "sequencingProgramType", LevelService.getLevels(Level.CODE.Instrument),String.class, true,DescriptionFactory.newValues("SR","PE"), "single",300));
        propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read1", "nbCyclesRead1", LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",400));
        propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read Index1", "nbCyclesReadIndex1", LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",500));
        propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read2", "nbCyclesRead2", LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",700));
        propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read Index2", "nbCyclesReadIndex2", LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",600));
        propertyDefinitions.add(newPropertiesDefinition("Piste contrôle","controlLane", LevelService.getLevels(Level.CODE.Instrument),String.class, true,DescriptionFactory.newValuesWithDefault("Pas de piste contrôle (auto-calibrage)","Pas de piste contrôle (auto-calibrage)","1",
        		"2","3","4","5","6","7","8"),"Pas de piste contrôle (auto-calibrage)","single",100));
        return propertyDefinitions;
	}

	private static List<PropertyDefinition> getMiseqProperties() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(newPropertiesDefinition("Nom cassette Miseq", "miseqReagentCassette",LevelService.getLevels(Level.CODE.Instrument),String.class,true,"single",100));
        propertyDefinitions.add(newPropertiesDefinition("Type lectures", "sequencingProgramType", LevelService.getLevels(Level.CODE.Instrument),String.class, true,DescriptionFactory.newValues("SR","PE"), "single",200));
        propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read1", "nbCyclesRead1", LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",300));
        propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read Index1", "nbCyclesReadIndex1", LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",400));
        propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read2", "nbCyclesRead2", LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",600));
        propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read Index2", "nbCyclesReadIndex2", LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",500));
      
        propertyDefinitions.add(newPropertiesDefinition("Custom primers", "customPrimers", LevelService.getLevels(Level.CODE.Instrument), Boolean.class, false, null, null, 
				"single", 10, true, "false",null));
		
		
        return propertyDefinitions;
	}
	
	
	private static List<PropertyDefinition> getArgusProperties() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
        propertyDefinitions.add(newPropertiesDefinition("Type de MapCard", "mapcardType", LevelService.getLevels(Level.CODE.Instrument),String.class, true, newValues("standard","HD"), "single"));
        propertyDefinitions.add(newPropertiesDefinition("Référence Carte", "containerSupportCode", LevelService.getLevels(Level.CODE.Instrument),String.class, true, "single"));
		propertyDefinitions.add(newPropertiesDefinition("Enzyme de restriction", "restrictionEnzyme", LevelService.getLevels(Level.CODE.Instrument), String.class, true, newValues("AfIII","ApaLI","BamHI","BgIII","EcoRI","HindIII","KpnI","MIuI","Ncol","NdeI","NheI","NotI","PvuII","SpeI","XbaI","XhoI"), "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Tableau sélection enzyme","enzymeChooser",LevelService.getLevels(Level.CODE.Instrument), Image.class, false, "img"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Photo digestion","digestionForTracking",LevelService.getLevels(Level.CODE.Instrument), Image.class, false, "img"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Metrix","anlyseMetrics",LevelService.getLevels(Level.CODE.Instrument), Image.class, false, "img"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Statistiques Contigs","contigStatistics",LevelService.getLevels(Level.CODE.Instrument), Image.class, false, "img"));
        return propertyDefinitions;
	}


	private static List<PropertyDefinition> getHiseq2500Properties() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = getHiseq2000Properties();		
	   propertyDefinitions.add(0, newPropertiesDefinition("Mode run","runMode"
	        		, LevelService.getLevels(Level.CODE.Instrument),String.class, true,DescriptionFactory.newValues("normal","rapide"), "single",50));
        return propertyDefinitions;
	}
	
	
	private static List<PropertyDefinition> getCovarisProperties() throws DAOException {
		List<PropertyDefinition> l = new ArrayList<>();
		//l.add(newPropertiesDefinition("Programme", "program", LevelService.getLevels(Level.CODE.Instrument), String.class, true, newValues("Frag_PE300","Frag_PE400","Frag_PE500","Frag_cDNA_Solexa"), "single"));
		
		l.add(newPropertiesDefinition("Programme", "program", LevelService.getLevels(Level.CODE.Instrument), String.class, true, null, 
				newValues("Low cost 400","Low cost 500","Low cost LC400","Low cost LC500","Low cost LC300","Frag CDNA","Projet BCB","MP Nextera post circ","PCR-Free", "Accel_ssDNA", "EPGV_Frag_350_PCR-Free_Pl96"), 
				"single", 10, true, null,null));
		
		return l;
	}
	
	private static List<PropertyDefinition> getSpriProperties() throws DAOException {
		List<PropertyDefinition> l = new ArrayList<>();
		l.add(newPropertiesDefinition("Programme", "program", LevelService.getLevels(Level.CODE.Instrument), String.class, true, newValues("300-600"), "single"));		
		return l;
	}
	
	private static List<PropertyDefinition> getThermocyclerProperties() throws DAOException {
		List<PropertyDefinition> l = new ArrayList<>();
		/*l.add(newPropertiesDefinition("Programme", "program", LevelService.getLevels(Level.CODE.Instrument), String.class, true, newValues("15","18"), "single"));*/
		/*l.add(newPropertiesDefinition("Nb cycles", "nbCycle", LevelService.getLevels(Level.CODE.Instrument), Integer.class, false, null, null, 
				"single", 10, true, null,null));*/
		return l;
	}
	
	
	
	private static List<PropertyDefinition> getTecanProperties() throws DAOException {
		List<PropertyDefinition> l = new ArrayList<>();
		l.add(newPropertiesDefinition("Programme", "program", LevelService.getLevels(Level.CODE.Instrument), String.class, false, null, newValues( 
				"Normalisation_48tubes",
				"Normalisation_Plaque_plaque",
				"Pooling_plaque_col1a6",
				"Pooling_plaque_col7a12",
				"Pooling_plaque_entiere_1tube",
				"Pooling_plaque_entiere_2tubes",
				"Pooling_plaque_WL",
				"Pooling_plaque_WL_5plaques",
				"Pooling_plaque_WL_9plaques",				
				"Pooling_tubes_10_pour_1pool", 
				"Pooling_tubes_12_pour_1pool", 
				"Pooling_tubes_16max_pour_xpoolde2",
				"Pooling_tubes_16max_pour_xpoolde5",
				"Pooling_tubes_24_pour_1pool",
				"Pooling_tubes_30_pour_3poolde10",
				"Pooling_tubes_32_pour_1pool",
				"Pooling_tubes_48_pour_1pool",
				"Pooling_tubes_48tubes_plaque",
				"Pooling_tubes_5_pour_1pool",
				"Pooling_tubes_90max_pour_xpoolde10",
				"Pooling_tubes_96_pour_1pool",
				"Pooling_plaque_col1a6_BC",
				"Pooling_plaque_WL_9plaques_BC",
				"Pooling_tubes_24_pour_1pool_BC",
				"Pooling_tubes_5_pour_1pool_BC"
				),
				"single", 10, true, null,null));
		l.add(newPropertiesDefinition("Input programme ", "inputProgram", LevelService.getLevels(Level.CODE.Instrument), String.class, false, null, newValues( 
				"WorkList",
				"3µl"
				),
				"single", 11, true, null,null));
				
		

		return l;
	}
	
	private static List<PropertyDefinition> getTecanStrategeneProperties() throws DAOException {
		List<PropertyDefinition> l = new ArrayList<>();
		l.add(newPropertiesDefinition("Programme", "program", LevelService.getLevels(Level.CODE.Instrument), String.class, true, null, newValues("QPCR_Solexa",
				"QPCR_Solexa_distrib_ADN_main",
				"QPCR_Solexa_distrib_plq_qPCR",
				"QPCR_Solexa_plaque_4titude",
				"QPCR_Solexa_plaque_BioRad"), 
				"single", 10, true, null,null));
		
		l.add(newPropertiesDefinition("Position qPCR", "qPCRposition", LevelService.getLevels(Level.CODE.ContainerIn), String.class, true, null, 
				newValues("1", "2", "3", "4", "5", "6","7","8","9","10","11","12","13","14","15","16","17"), 
				"single", 15, true, null,null));
		
		
		
		return l;
	}
	
	private static List<PropertyDefinition> getLabChipGXProperties() throws DAOException {
		List<PropertyDefinition> l = new ArrayList<>();
		
		l.add(newPropertiesDefinition("Nom run LabChip", "robotRunCode", LevelService.getLevels(Level.CODE.Instrument), String.class, false, null, null, 
				"single", 10, true, null,null));
		
		
		return l;
	}
	
	private static List<PropertyDefinition> getBioanalyzerProperties() throws DAOException {
		List<PropertyDefinition> l = new ArrayList<>();
		
		l.add(newPropertiesDefinition("Type puce", "chipType", LevelService.getLevels(Level.CODE.Instrument), String.class, true, null, newValues("DNA HS", "DNA 12000", "RNA"), 
				"single", 10, true, null,null));
		
		l.add(newPropertiesDefinition("Position sur puce", "chipPosition", LevelService.getLevels(Level.CODE.ContainerIn), String.class, false, null, 
				newValues("1", "2", "3", "4", "5", "6","7","8","9","10","11"), 
				"single", 12, true, null,null));
		
		
		return l;
	}
	
	private static List<PropertyDefinition> getQuBitFluoroskanProperties() throws DAOException {
		List<PropertyDefinition> l = new ArrayList<>();
		l.add(newPropertiesDefinition("Gamme", "gamme", LevelService.getLevels(Level.CODE.Instrument), String.class, false, null,  newValues("BR","HS","BR et HS","RNA HS","HS et ssDNA", "DeNovix Ultra HS"), 
				"single", 10, true, null,null));
		return l;
	}
		
	
	public static List<Instrument> getInstrumentOpgen()throws DAOException{
		List<Instrument> instruments=new ArrayList<>();
		instruments.add( createInstrument("APOLLON", "APOLLON", null, true, "/env/ig/atelier/opgen/cns/APOLLON", DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		return instruments;
	}
	
	private static List<Instrument> getInstrumentNovaseq6000() throws DAOException {
		List<Instrument> instruments=new ArrayList<>();
		
		instruments.add( createInstrument("JARVIS", "JARVIS", "V1", true, "/env/ig/atelier/illumina/cns/JARVIS/", DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
	
		return instruments;
	}
	
	private static List<Instrument> getInstrumentGAII() throws DAOException {
		List<Instrument> instruments=new ArrayList<>();
		instruments.add(createInstrument("BISMUTH", "BISMUTH", "G3", false, "/env/ig/atelier/illumina_BISMUTH", DescriptionFactory.getInstitutes(Constants.CODE.CNS)) );
		instruments.add(createInstrument("HELIUM", "HELIUM", "G1", false, "/env/ig/atelier/illumina_HELIUM", DescriptionFactory.getInstitutes(Constants.CODE.CNS)) );
		instruments.add(createInstrument("AZOTE", "AZOTE", "G2", false, "/env/ig/atelier/illumina_AZOTE", DescriptionFactory.getInstitutes(Constants.CODE.CNS)) );
		instruments.add( createInstrument("EXTGAIIX", "EXTGAIIX", "G0", false, "/env/atelier", DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		return instruments;
	}

	private static List<Instrument> getInstrumentMiSeq() throws DAOException {
		List<Instrument> instruments=new ArrayList<>();
		instruments.add(createInstrument("MELISSE", "MELISSE", "M2", true, "/env/ig/atelier/illumina/cns/MELISSE", DescriptionFactory.getInstitutes(Constants.CODE.CNS)) );
		instruments.add(createInstrument("MIMOSA", "MIMOSA", "M1", true, "/env/ig/atelier/illumina/cns/MIMOSA", DescriptionFactory.getInstitutes(Constants.CODE.CNS)) );
		instruments.add(createInstrument("MISEQ1", "MISEQ1", "M1C", false, "/env/ig/atelier/illumina/cng/MISEQ1", DescriptionFactory.getInstitutes(Constants.CODE.CNS)) );
		instruments.add( createInstrument("EXTMISEQ", "EXTMISEQ", "M0", false, "/env/atelier", DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		return instruments;
	}
	
	public static List<Instrument> getInstrumentHiseq2000() throws DAOException{
		List<Instrument> instruments=new ArrayList<>();
		instruments.add(createInstrument("HISEQ2", "HISEQ2", "H2C", false, "/env/ig/atelier/illumina/cng/HISEQ2/", DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		instruments.add(createInstrument("HISEQ4", "HISEQ4", "H4C", false, "/env/ig/atelier/illumina/cng/HISEQ4/", DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		instruments.add(createInstrument("HISEQ7", "HISEQ7", "H7C", false, "/env/ig/atelier/illumina/cng/HISEQ7/", DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		instruments.add(createInstrument("CARBONE", "CARBONE", "H6", true, "/env/ig/atelier/illumina/cns/CARBONE", DescriptionFactory.getInstitutes(Constants.CODE.CNS)) );
		instruments.add(createInstrument("CHROME", "CHROME", "H1", false, "/env/ig/atelier/illumina_CHROME", DescriptionFactory.getInstitutes(Constants.CODE.CNS)) );
		instruments.add(createInstrument("MERCURE", "MERCURE", "H2", true, "/env/ig/atelier/illumina/cns/MERCURE", DescriptionFactory.getInstitutes(Constants.CODE.CNS)) );
		instruments.add(createInstrument("SOUFRE", "SOUFRE", "H4", true, "/env/ig/atelier/illumina/cns/SOUFRE", DescriptionFactory.getInstitutes(Constants.CODE.CNS)) );
		instruments.add( createInstrument("PHOSPHORE", "PHOSPHORE", "H3", true, "/env/ig/atelier/illumina/cns/PHOSPHORE", DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		instruments.add( createInstrument("EXTHISEQ", "EXTHISEQ", "H0", false, "/env/atelier", DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		return instruments;
	}
	
	public static List<Instrument> getInstrumentHiseq2500() throws DAOException{
		List<Instrument> instruments=new ArrayList<>();
		instruments.add( createInstrument("HISEQ9", "HISEQ9", "H9C", false, "/env/ig/atelier/illumina/cng/HISEQ9/", DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		instruments.add( createInstrument("HISEQ10", "HISEQ10", "H10C", false, "/env/ig/atelier/illumina/cng/HISEQ10/", DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		instruments.add( createInstrument("HISEQ11", "HISEQ11", "H11C", false, "/env/ig/atelier/illumina/cng/HISEQ11/", DescriptionFactory.getInstitutes(Constants.CODE.CNS)));		
		instruments.add( createInstrument("FLUOR", "FLUOR", "H8", true, "/env/ig/atelier/illumina/cns/FLUOR", DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		instruments.add( createInstrument("PLATINE", "PLATINE", "H7", true, "/env/ig/atelier/illumina/cns/PLATINE", DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		return instruments;
	}
	
	private List<Instrument> getInstrumentHiseq4000() throws DAOException {
		List<Instrument> instruments=new ArrayList<>();
		instruments.add(createInstrument("TORNADE", "TORNADE", "H5", true, "/env/ig/atelier/illumina/cns/TORNADE", DescriptionFactory.getInstitutes(Constants.CODE.CNS)) );
		instruments.add(createInstrument("RAFALE", "RAFALE", "H9", true, "/env/ig/atelier/illumina/cns/RAFALE", DescriptionFactory.getInstitutes(Constants.CODE.CNS)) );
		return instruments;
	}

	
	/*public static List<Instrument> getInstrumentExtSolexa()throws DAOException{
		List<Instrument> instruments=new ArrayList<Instrument>();
		instruments.add( createInstrument("EXTGAIIX", "EXTGAIIX", "G0", true, "/env/atelier", DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		instruments.add( createInstrument("EXTHISEQ", "EXTHISEQ", "H0", true, "/env/atelier", DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		instruments.add( createInstrument("EXTMISEQ", "EXTMISEQ", "M0", true, "/env/atelier", DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		return instruments;
	}*/
	
	
	private List<Instrument> getInstrumentMKI() throws DAOException {
		List<Instrument> instruments=new ArrayList<>();
		instruments.add(createInstrument("MN15456", "MK15456", null, false, "/env/ig/atelier/nanopore/cns/MN15456", DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		instruments.add(createInstrument("MN15302", "MK15302", null, false, "/env/ig/atelier/nanopore/cns/MN15302", DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		instruments.add(createInstrument("MN15382", "MK15382", null, false, "/env/ig/atelier/nanopore/cns/MN15382", DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		instruments.add(createInstrument("MN15407", "MK15407", null, false, "/env/ig/atelier/nanopore/cns/MN15407", DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		instruments.add(createInstrument("MN15336", "MK15336", null, false, "/env/ig/atelier/nanopore/cns/MN15336", DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		instruments.add(createInstrument("MN15782", "MK15782", null, false, "/env/ig/atelier/nanopore/cns/MN15782", DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		instruments.add(createInstrument("MN15794", "MK15794", null, false, "/env/ig/atelier/nanopore/cns/MN15794", DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		instruments.add(createInstrument("MN15464", "MK15464", null, false, "/env/ig/atelier/nanopore/cns/MN15464", DescriptionFactory.getInstitutes(Constants.CODE.CNS)));		
		
		instruments.add(createInstrument("MN15904", "MK15904", null, false, "/env/ig/atelier/nanopore/cns/MN15904", DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		instruments.add(createInstrument("MN15911", "MK15911", null, false, "/env/ig/atelier/nanopore/cns/MN15911", DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		instruments.add(createInstrument("MN15940", "MK15940", null, false, "/env/ig/atelier/nanopore/cns/MN15940", DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

		instruments.add(createInstrument("MN16288", "MK16288", null, false, "/env/ig/atelier/nanopore/cns/MN16288", DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		return instruments;
	}
	
	private List<Instrument> getInstrumentMKIB() throws DAOException {
		List<Instrument> instruments=new ArrayList<>();
		instruments.add(createInstrument("MN16823", "MK16823", null, true, "/env/ig/atelier/nanopore/cns/MN16823", DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		instruments.add(createInstrument("MN17490", "MK17490", null, true, "/env/ig/atelier/nanopore/cns/MN17490", DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		instruments.add(createInstrument("MN18874", "MK18874", null, true, "/env/ig/atelier/nanopore/cns/MN18874", DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		instruments.add(createInstrument("MN19040", "MK19040", null, true, "/env/ig/atelier/nanopore/cns/MN19040", DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		instruments.add(createInstrument("MN17479", "MK17479", null, true, "/env/ig/atelier/nanopore/cns/MN17479", DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		instruments.add(createInstrument("MN17273", "MK17273", null, true, "/env/ig/atelier/nanopore/cns/MN17273", DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		instruments.add(createInstrument("MN17273", "MK17273", null, true, "/env/ig/atelier/nanopore/cns/MN17273", DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		instruments.add(createInstrument("MN19358", "MK19358", null, true, "/env/ig/atelier/nanopore/cns/MN19358", DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

		instruments.add(createInstrument("MN19361", "MK19361", null, true, "/env/ig/atelier/nanopore/cns/MN19361", DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		instruments.add(createInstrument("MN19368", "MK19368", null, true, "/env/ig/atelier/nanopore/cns/MN19368", DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		return instruments;
	}
	
	private static List<Instrument> getInstrumentMinIon() throws DAOException {
		List<Instrument> instruments=new ArrayList<>();
		instruments.add(createInstrument("MN02528", "MN02528", null, false, "/env/ig/atelier/nanopore/cns/MN02528", DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		instruments.add(createInstrument("MN02670", "MN02670", null, false, "/env/ig/atelier/nanopore/cns/MN02670", DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		instruments.add(createInstrument("MN02280", "MN02280", null, false, "/env/ig/atelier/nanopore/cns/MN02280", DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		instruments.add(createInstrument("MN02259", "MN02259", null, false, "/env/ig/atelier/nanopore/cns/MN02259", DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		instruments.add(createInstrument("MN02833", "MN02833", null, false, "/env/ig/atelier/nanopore/cns/MN02833", DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
			
		return instruments;
	}
	
	private static List<Instrument> getInstrumentPromethION () throws DAOException {
		List<Instrument> instruments=new ArrayList<>();
		instruments.add(createInstrument("PCA0026", "PCA0026", null, true, "/env/ig/atelier/nanopore/cns/PCA0026", DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		instruments.add(createInstrument("PCA0032", "PCA0032", null, true, "/env/ig/atelier/nanopore/cns/PCA0032", DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		instruments.add(createInstrument("PCT0004", "PCT0004", null, true, "/env/ig/atelier/nanopore/cns/PCT0004", DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		return instruments;
	}
	
	private static List<Instrument> getInstrumentEppendorfMiniSpinPlus() throws DAOException {
		List<Instrument> instruments=new ArrayList<>();
		instruments.add(createInstrument("MiniSpin plus 1", "miniSpinPlus1", null, true, "path", DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		return instruments;
	}
	
	
	private List<Instrument> getInstrumentMegaruptor2() {
		List<Instrument> instruments=new ArrayList<>();
		instruments.add(createInstrument("megaruptor2-1","Megaruptor2_1", null, true, "path", DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		return instruments;
	}

	private List<Instrument> getInstrumentIRYS() throws DAOException {
		List<Instrument> instruments=new ArrayList<>();
		instruments.add(createInstrument("ODYSSEE", "ODYSSEE", null, true, "/env/ig/atelier/bionano/cns/ODYSSEE", DescriptionFactory.getInstitutes(Constants.CODE.CNS)) );
		return instruments;
	}
	
	private List<Instrument> getInstrumentSAPHYR() throws DAOException {
		List<Instrument> instruments=new ArrayList<>();
		instruments.add(createInstrument("ILIADE", "ILIADE", null, true, "/env/ig/atelier/bionano/cns/ILIADE", DescriptionFactory.getInstitutes(Constants.CODE.CNS)) );
		return instruments;
	}
	
	private List<Instrument> getInstrumentNanodrop() {
		List<Instrument> instruments=new ArrayList<>();
		instruments.add(createInstrument("nanodrop1","Nanodrop 1", null, true, "path", DescriptionFactory.getInstitutes(Constants.CODE.CNS)) );
		return instruments;	}
	
	
	private List<Instrument> getInstrumentPippinPulse() {
		List<Instrument> instruments=new ArrayList<>();
		instruments.add(createInstrument("pippin-pulse-1","Pippin Pulse 1", null, true, "path", DescriptionFactory.getInstitutes(Constants.CODE.CNS)) );
		return instruments;	}
	
}
