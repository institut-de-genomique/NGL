package services.description.instrument;


import static services.description.DescriptionFactory.newInstrumentUsedType;
import static services.description.DescriptionFactory.newPropertiesDefinition;
import static services.description.DescriptionFactory.newValue;
import static services.description.DescriptionFactory.newValues;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import models.laboratory.common.description.Institute;
import models.laboratory.common.description.Level;
import models.laboratory.common.description.MeasureCategory;
import models.laboratory.common.description.MeasureUnit;
import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.common.description.dao.MeasureUnitDAO;
import models.laboratory.common.description.Value;
import models.laboratory.instrument.description.Instrument;
import models.laboratory.instrument.description.InstrumentCategory;
import models.laboratory.instrument.description.InstrumentUsedType;
import models.laboratory.instrument.description.dao.InstrumentCategoryDAO;

import models.utils.dao.DAOException;
import models.utils.dao.DAOHelpers;

import play.data.validation.ValidationError;

import services.description.Constants;
import services.description.DescriptionFactory;
import services.description.common.LevelService;
import services.description.common.MeasureService;

public class InstrumentServiceCNG extends AbstractInstrumentService {
	
	/** 
	 * Creation des categories d'instrument.
	 * @return liste
	 */
	public List<InstrumentCategory> getInstrumentCategories() {
		List<InstrumentCategory> l = new ArrayList<>();

		l.add(new InstrumentCategory("covaris",                                "Covaris"));
		l.add(new InstrumentCategory("spri",                                   "Spri"));
		l.add(new InstrumentCategory("thermocycler",                           "Thermocycleur"));
		l.add(new InstrumentCategory("centrifuge",                             "Centrifugeuse"));

		l.add(new InstrumentCategory("covaris-and-liquid-handling-robot",      "Covaris + Robot pipetage"));          // FDS 29/01/2016 JIRA NGL-894 (couple d'instruments...)
		l.add(new InstrumentCategory("liquid-handling-robot-and-cBot",         "Robot pipetage + cBot"));             // FDS 22/03/2016 JIRA NGL-982 (couple d'instruments...)
		l.add(new InstrumentCategory("thermocycler-and-liquid-handling-robot", "Thermocycleur + Robot pipetage"));    // FDS 29/07/2016 JIRA NGL-1027 (couple d'instruments...)
		l.add(new InstrumentCategory("liquid-handling-robot-and-qPCR-system",  "Robot pipetage + Appareil de qPCR "));// FDS 22/01/2019 ajout pour JIRA NGL-2389

		l.add(new InstrumentCategory("fluorometer",                            "Quantification par fluorométrie"));
		l.add(new InstrumentCategory("spectrophotometer",                      "Quantification par spectrophotométrie"));
		l.add(new InstrumentCategory("qPCR-system",                            "Appareil de qPCR"));
		l.add(new InstrumentCategory("chip-electrophoresis",                   "Electrophorèse sur puce"));

		l.add(new InstrumentCategory("hand",                                   "Main"));
		l.add(new InstrumentCategory("cbot",                                   "CBot"));

		l.add(new InstrumentCategory("illumina-sequencer",                     "Séquenceur Illumina"));
		l.add(new InstrumentCategory("qc-illumina-sequencer",                  "QC Séquenceur Illumina"));
		l.add(new InstrumentCategory("opt-map-opgen",                          "Cartographie Optique Opgen"));
		l.add(new InstrumentCategory("nanopore-sequencer",                     "Séquenceur Nanopore")); // FDS modifié 30/03/2017 NGL-1225
		l.add(new InstrumentCategory("extseq",                                 "Extérieur"));

		l.add(new InstrumentCategory("liquid-handling-robot",                   "Robot pipetage"));
		l.add(new InstrumentCategory("sizing-system",                           "Appareil de sizing"));

		l.add(new InstrumentCategory("10x-genomics-instrument",                 "10x Genomics Instrument"));// FDS ajout 20/02/2017 NGL-1167 (Chromium)

		return l;
	}

	@Override
	public void saveInstrumentCategories(Map<String, List<ValidationError>> errors) throws DAOException {
		DAOHelpers.saveModels(InstrumentCategory.class, getInstrumentCategories(), errors);	
	}

	/**
	 * Creation des types d'instruments ET des instruments
	 * @return liste
	 */
	public List<InstrumentUsedType> getInstrumentUsedTypes() {
		List<InstrumentUsedType> l = new ArrayList<>();
		List<Institute> CNG = DescriptionFactory.getInstitutes(Constants.CODE.CNG); // 04/04/2018; puisque tout se refere uniqt au CNG, alleger l'ecriture...
		InstrumentCategoryDAO icfind = InstrumentCategory.find.get();

		// 27/07/2016 la main peut traiter des plaques en entree ET en sortie; 02/03/2017 ajout strip-8
		l.add(newInstrumentUsedType("Main", "hand", icfind.findByCode("hand"), 
				null, 
				getInstruments(createInstrument("hand", "Main", null, true, null, CNG)),
				getContainerSupportCategories(new String[]{"tube","96-well-plate","strip-8"}),
				getContainerSupportCategories(new String[]{"tube","96-well-plate","strip-8"}),
				CNG));
		
		
		/** cBots and sequencers **/
		l.add(newInstrumentUsedType("cBot", "cBot", icfind.findByCode("cbot"), 
				getCBotProperties(),
				getInstrumentsCbot(CNG),
				getContainerSupportCategories(new String[]{"tube"}), 
				getContainerSupportCategories(new String[]{"flowcell-8","flowcell-2"}),
				CNG));
		
		// 23/01/2017 separation des cbot-v2 pour mieux gérer leurs propriétés	
		l.add(newInstrumentUsedType("cBot-v2", "cBotV2", icfind.findByCode("cbot"),
				getCBotV2Properties(),
				getInstrumentsCbotV2(CNG),
				getContainerSupportCategories(new String[]{"tube"}),
				getContainerSupportCategories(new String[]{"flowcell-8","flowcell-2"}),
				CNG));
		
		// 07/12/2017 NGL-1730 "cBot-MarieCurix-A et "cBot-MarieCurix-B
		l.add(newInstrumentUsedType("cBot-onboard", "cBot-onboard", icfind.findByCode("cbot"),
				getCBotInterneProperties(),
				getInstrumentsCbotOnboard(CNG),
				getContainerSupportCategories(new String[]{"tube"}),
				getContainerSupportCategories(new String[]{"flowcell-4","flowcell-2","flowcell-1" }),
				CNG));
		
		// 03/09/2018 NGL-2219 ajout
		l.add(newInstrumentUsedType("NovaSeq Xp Flow Cell Dock", "novaseq-xp-fc-dock", icfind.findByCode("cbot"),
				getCBotOnBoardNovaSeqProperties(),
				getInstruments(createInstrument( "novaseq-xp-fc-dock-1", "NovaSeq Xp Flow Cell Dock 1", null, true, null, CNG)),
				getContainerSupportCategories(new String[]{"96-well-plate", "tube"}),
				getContainerSupportCategories(new String[]{"flowcell-2","flowcell-4"}),
				CNG));
		
		l.add(newInstrumentUsedType("MISEQ", "MISEQ", icfind.findByCode("illumina-sequencer"),
				getMiseqProperties(),
				getInstrumentsMiSeq(CNG),
				getContainerSupportCategories(new String[]{"flowcell-1"}),
				null, // pas de sortie pour les instruments * sequencer *
				CNG));
		
		l.add(newInstrumentUsedType("MISEQ QC", "MISEQ-QC-MODE", icfind.findByCode("qc-illumina-sequencer"),
				getMiseqQCProperties(),
				getInstrumentsMiSeqQC(CNG),
				getContainerSupportCategories(new String[]{"96-well-plate","tube"}),
				null,// pas de sortie pour les instruments * sequencer *
				CNG));	
		
		l.add(newInstrumentUsedType("HISEQ2000", "HISEQ2000", icfind.findByCode("illumina-sequencer"),
				getHiseq2000Properties(),
				getInstrumentsHiseq2000(CNG),
				getContainerSupportCategories(new String[]{"flowcell-8"}),
				null,// pas de sortie pour les instruments * sequencer *
				CNG));
		
		l.add(newInstrumentUsedType("HISEQ2500", "HISEQ2500", icfind.findByCode("illumina-sequencer"),
				getHiseq2500Properties(),
				getInstrumentsHiseq2500(CNG),
				getContainerSupportCategories(new String[]{"flowcell-8","flowcell-2"}), 
				null,// pas de sortie pour les instruments * sequencer *
				CNG));
		
		l.add(newInstrumentUsedType("NEXTSEQ500", "NEXTSEQ500", icfind.findByCode("illumina-sequencer"),
				getNextseq500Properties(),
				getInstrumentsNextseq500(CNG),
				getContainerSupportCategories(new String[]{"flowcell-4"}),
				null,// pas de sortie pour les instruments * sequencer *
				CNG));
		
		l.add(newInstrumentUsedType("HISEQ4000", "HISEQ4000", icfind.findByCode("illumina-sequencer"),
				getHiseq4000Properties(), 
				getInstrumentsHiseq4000(CNG),
				getContainerSupportCategories(new String[]{"flowcell-8"}),
				null,// pas de sortie pour les instruments * sequencer *
				CNG));
		
		l.add(newInstrumentUsedType("HISEQX", "HISEQX", icfind.findByCode("illumina-sequencer"),
				getHiseqXProperties(), 
				getInstrumentsHiseqX(CNG),
				getContainerSupportCategories(new String[]{"flowcell-8"}), 
				null,// pas de sortie pour les instruments * sequencer *
				CNG));
		
		// 07/12/2017 NGL-1730: ajout Novaseq6000
		l.add(newInstrumentUsedType("NOVASEQ6000", "NOVASEQ6000", icfind.findByCode("illumina-sequencer"),
				getNovaseq6000Properties(), 
				getInstrumentsNovaseq6000(CNG),
				getContainerSupportCategories(new String[]{"flowcell-2","flowcell-4"}), 
				null,// pas de sortie pour les instruments * sequencer *
				CNG));	
		
		
		/* NOTE GENERALE 30/08/2017 
		 * les noms (names) de machine affichés a l'utilisateur se terminent par un numéro décollé du nom mais sans "-"
		 * exemple "BioAnalyzer 1" et pas "BioAnalyzer1" ni "BioAnalyzer-1"
		 */
		
		/** chip-electrophoresis **/
		// FDS 24/02/2017 ajouter strip-8 en input
		l.add(newInstrumentUsedType("Agilent 2100 bioanalyzer", "agilent-2100-bioanalyzer", icfind.findByCode("chip-electrophoresis"),
				getBioanalyzerProperties(), 
				getInstrumentsBioanalyser(CNG),
				getContainerSupportCategories(new String[]{"tube","strip-8"}),
				null,// pas de sortie pour les instruments * chip-electrophoresis *
				CNG));
		
		// FDS 01/09/2016 labGX: nom et code incorrects -/- specs!!! Laisser le code (car sinon reprise de donnees) mais corriger le name; 
		l.add(newInstrumentUsedType("LabChip GX", "labChipGX", icfind.findByCode("chip-electrophoresis"),
				null,// pas de properties ????
				getInstrumentsLabChipGX(CNG),
				getContainerSupportCategories(new String[]{"96-well-plate"}),
				null,// pas de sortie pour les instruments * chip-electrophoresis *
				CNG));
		
		
		/** thermocyclers **/
		//FDS ajout 03/04/2017 NGL-1225:  Mastercycler Nexus SX-1 seul (input tubes ou plaques / output tubes ou  plaques)
		l.add(newInstrumentUsedType("Mastercycler Nexus SX-1", "mastercycler-nexus", icfind.findByCode("thermocycler"),
				getMastercyclerNexusProperties(), 
				getInstrumentsMasterNexus(CNG),
				getContainerSupportCategories(new String[]{"tube","96-well-plate"}), // 20/05/2019  plaques sont historiques pour prod
				getContainerSupportCategories(new String[]{"tube","96-well-plate"}), // 20/05/2019  plaques sont historiques pour prod
				CNG));
		
		//FDS ajout 20/05/2019: NGL-2552  Mastercycler Nexus SX-1 seul pour équipes de développement
		l.add(newInstrumentUsedType("Mastercycler Nexus SX-1 (DEV)", "mastercycler-nexus-dev", icfind.findByCode("thermocycler"),
				getMastercyclerDevProperties(),
				getInstruments(createInstrument("mastercycler-nexus-generic-dev", "Mastercycler Nexus SX-1 (générique DEV)", null, true, null, CNG)),// 1 seul instrument generique
				getContainerSupportCategories(new String[]{"tube"}),  // pas de plaques pour dev
				getContainerSupportCategories(new String[]{"tube"}),  // pas de plaques pour dev
				CNG));
		
		//FDS 13/07/2017 renommer =>"Mastercycler EP-Gradient"
		l.add(newInstrumentUsedType("Mastercycler EP-Gradient", "mastercycler-ep-gradient", icfind.findByCode("thermocycler"),
				getMastercyclerEPGradientProperties(), 
				getInstrumentsMasterEpGradient(CNG),
				getContainerSupportCategories(new String[]{"tube","96-well-plate"}), // 20/05/2019  plaques sont historiques pour prod
				getContainerSupportCategories(new String[]{"tube","96-well-plate"}), // 20/05/2019  plaques sont historiques pour prod
				CNG));
		
		//FDS  ajout 20/05/2019: NGL-2552 Mastercycler EP-Gradient seul pour équipes de développement
		l.add(newInstrumentUsedType("Mastercycler EP-Gradient (DEV)", "mastercycler-ep-gradient-dev", icfind.findByCode("thermocycler"),
				getMastercyclerDevProperties(),
				getInstruments(createInstrument("mastercycler-ep-gradient-generic-dev", "Mastercycler EP-Gradient (générique DEV)", null, true, null, CNG)),// 1 seul instrument générique
				getContainerSupportCategories(new String[]{"tube"}),  // pas de plaques pour dev
				getContainerSupportCategories(new String[]{"tube"}),  // pas de plaques pour dev
				CNG));
		
		//FDS ajout 14/05/2019: NGL-2552  mastercycler-pro 
		l.add(newInstrumentUsedType("Mastercycler Pro/ProS", "mastercycler-pro", icfind.findByCode("thermocycler"),
				getMastercyclerProProperties(),
				getInstruments(createInstrument("mastercycler-pro-generic", "Mastercycler Pro/ProS (générique)", null, true, null, CNG)),// 1 seul instrument générique
				getContainerSupportCategories(new String[]{"tube"}),
				getContainerSupportCategories(new String[]{"tube","96-well-plate"}), // 15/10/2019 NGL-2607 ajout sortie plaques
				CNG));
		
		/** covaris **/
		l.add(newInstrumentUsedType("Covaris E210", "covaris-e210", icfind.findByCode("covaris"),
				getCovarisProperties(),
				getInstruments(createInstrument("covaris1", "Covaris 1", null, false, null, CNG)),
				getContainerSupportCategories(new String[]{"tube"}),
				getContainerSupportCategories(new String[]{"tube"}),
				CNG, false)); // FDS 23/10/2018 tous les instruments sont inactivés=> inactiver le type...sertprobablement a rien
		
		// FDS correction 29/08/2017 les covaris utilisent aussi des plaques et pas seulement des tubes !! (covaris 1 inactivé=> pas corrigé..)
		l.add(newInstrumentUsedType("Covaris LE220", "covaris-le220", icfind.findByCode("covaris"), 
				getCovarisProperties(),
				getInstruments(createInstrument("covaris2", "Covaris 2", null, true, null, CNG)),
				getContainerSupportCategories(new String[]{"tube","96-well-plate"}),
				getContainerSupportCategories(new String[]{"tube","96-well-plate"}),
				CNG));
		
		l.add(newInstrumentUsedType("Covaris E220", "covaris-e220", icfind.findByCode("covaris"),
				getCovarisProperties(), 
				getInstruments(createInstrument("covaris3", "Covaris 3", null, true, null, CNG)),
				getContainerSupportCategories(new String[]{"tube","96-well-plate"}),
				getContainerSupportCategories(new String[]{"tube","96-well-plate"}),
				CNG));
		
		
		/** quality **/
		l.add(newInstrumentUsedType("qPCR (Lightcycler 480 II)", "qpcr-lightcycler-480II", icfind.findByCode("qPCR-system"),
				getLightCyclerProperties(),
				getInstrumentsLightCycler(CNG),
				getContainerSupportCategories(new String[]{"tube","96-well-plate"}), 
				null,// pas de sortie pour les instruments * quality *
				CNG));
		
		// Note: c'est aussi un appareil de la marque ABI..
		l.add(newInstrumentUsedType("qPCR (QuantStudio)", "qpcr-quantstudio", icfind.findByCode("qPCR-system"),
				null, // pas de proprietes ??
				getInstruments(createInstrument("quantstudio1", "QuantStudio", null, true, null, CNG)),
				getContainerSupportCategories(new String[]{"tube"}),
				null,// pas de sortie pour les instruments * quality *
				CNG));

		l.add(newInstrumentUsedType("qPCR (ABI 7900HT)", "qpcr-abi-7900ht", icfind.findByCode("qPCR-system"),
				null, // pas de proprietes ??
				getInstruments(createInstrument("abi-7900ht1", "ABI 7900HT", null, true, null, CNG)),
				getContainerSupportCategories(new String[]{"tube"}),
				null,// pas de sortie pour les instruments * quality *
				CNG));
		
		l.add(newInstrumentUsedType("QuBit", "qubit", icfind.findByCode("fluorometer"),
				getQuBitProperties(),
				getInstrumentsQubit(CNG),
				getContainerSupportCategories(new String[]{"tube"}),
				null,// pas de sortie pour les instruments * quality *
				CNG));
		
		// FDS 03/08/2017 NGL-1201: Ajout fluorometer Spectramax
		l.add(newInstrumentUsedType("SpectraMax", "spectramax", icfind.findByCode("spectrophotometer"),
				null, //FDS 04/09/2017 pas de propriétés
				getInstrumentsSpectraMax(CNG),
				getContainerSupportCategories(new String[]{"96-well-plate"}),
				null,// pas de sortie pour les instruments * quality *
				CNG));
		
		
		/** liquid-handling-robot  **/
		l.add(newInstrumentUsedType("Janus", "janus", icfind.findByCode("liquid-handling-robot"),
				getJanusProperties(),
				getInstruments(createInstrument("janus1", "Janus 1", null, true, null, CNG),
							   createInstrument("janus2", "Janus 2", null, true, null, CNG)), // 06/05/2019 NGL-2534 ajout
				getContainerSupportCategories(new String[]{"96-well-plate"}),
				getContainerSupportCategories(new String[]{"96-well-plate" }),
				CNG));
		
		//FDS ajout 04/08/2016 NGL-1026: Sciclone NGSX seul
		l.add(newInstrumentUsedType("Sciclone NGSX", "sciclone-ngsx", icfind.findByCode("liquid-handling-robot"),
				getScicloneNGSXAloneProperties(),
				getInstrumentsNGS(CNG),
				getContainerSupportCategories(new String[]{"96-well-plate"}),
				getContainerSupportCategories(new String[]{"96-well-plate" }),
				CNG));	
		
		//FDS ajout 04/10/2016 EpMotion (input plate / output tubes)
		l.add(newInstrumentUsedType("EpMotion", "epmotion", icfind.findByCode("liquid-handling-robot"),
				getEpMotionProperties(),
				getInstruments(createInstrument("epmotion1", "EpMotion 1",null, true, null, CNG)),
				getContainerSupportCategories(new String[]{"tube","96-well-plate"}),//18/10/2017 ajout tube en entree
				getContainerSupportCategories(new String[]{"tube"}), 
				CNG));	
		
		//FDS ajout 26/06/2017 Bravo WorkStation (input plate / output plate); 09/11/2017 ajout de properties...
		l.add(newInstrumentUsedType("Bravo WorkStation","bravo-workstation", icfind.findByCode("liquid-handling-robot"),
				getBravoWsProperties(),
				getInstrumentsBravoWorkstation(CNG),
				getContainerSupportCategories(new String[]{"96-well-plate"}),
				getContainerSupportCategories(new String[]{"96-well-plate"}),
				CNG));	
		
		// FDS 06/04/2018 NGL-1727/NGL-1996: Sciclone NGSX + Zephyr
		l.add(newInstrumentUsedType("Sciclone NGSX + Zephyr", "sciclone-ngsx-and-zephyr", icfind.findByCode("liquid-handling-robot"),
				getScicloneNGSXAndZephyrProperties(),
				getInstrumentsScicloneNGSXAndZephyr(CNG),
				getContainerSupportCategories(new String[]{"96-well-plate"}), 
				getContainerSupportCategories(new String[]{"96-well-plate"}), 
				CNG));

		// FDS 06/04/2018 NGL-1727/NGL-1996: Tecan EVO150 + Zephyr	
		l.add(newInstrumentUsedType("Tecan EVO150 + Zephyr", "tecan-evo-150-and-zephyr", icfind.findByCode("liquid-handling-robot"),
				null, //getTecanEvo150AndZephyrProperties(), existe mais sans aucune propriete !!!!
				getInstruments(createInstrument("tecan-bollinger1-and-zephyr1","Tecan-Bollinger1 / Zephyr 1",null, true, null, CNG),  // FDS 22/01/2019 NGL-2389; 19/04/2019 supression pre-pcr-0
							   createInstrument("tecan-roederer2-and-zephyr1", "Tecan-Roederer2 / Zephyr 1", null, true, null, CNG)), // FDS 22/01/2019 NGL-2389; 19/04/2019 suppression post-pcr-0
				getContainerSupportCategories(new String[]{"96-well-plate"}), 
				getContainerSupportCategories(new String[]{"96-well-plate"}), 
				CNG));
		
		// FDS 22/01/2019 NGL-2389 Tecan EVO150 seul, ajout
		l.add(newInstrumentUsedType("Tecan EVO150", "tecan-evo-150", icfind.findByCode("liquid-handling-robot"),
				null, //getTecanEvo150Properties(), existe mais sans aucune propriete !!!!
				getInstruments(createInstrument("tecan-bollinger1","Tecan-Bollinger1",null, true, null, CNG), // FDS 19/04/2019 ajouté pour NGL-2518
							   createInstrument("tecan-roederer2", "Tecan-Roederer2" ,null, true, null, CNG)),// FDS 19/04/2019 suppression post-pcr-0
				getContainerSupportCategories(new String[]{"96-well-plate"}), 
				getContainerSupportCategories(new String[]{"96-well-plate"}), 
				CNG));
		
		// FDS 22/01/2019 NGL-2389 Tecan EVO150 + lightcycler, ajout
		l.add(newInstrumentUsedType("Tecan EVO150 + qPCR (Lightcycler 480 II)", "tecan-evo-150-and-qpcr-lightcycler-480II", icfind.findByCode("liquid-handling-robot-and-qPCR-system"),
				getTecanEvo150AndLightCyclerProperties(),
				getInstrumentsTecanEvo150AndLightCycler(CNG),
				getContainerSupportCategories(new String[]{"96-well-plate"}),
				getContainerSupportCategories(new String[]{"96-well-plate" }),
				CNG));
		
		// FDS 19/04/2019 NGL-2518 Tecan EVO150 + Tecan EVO150, ajout
		l.add(newInstrumentUsedType("Tecan EVO150 + Tecan EVO150", "tecan-evo-150-and-tecan-evo-150", icfind.findByCode("liquid-handling-robot"),
				null, //getTecanEvo150Properties(), existe mais sans aucune propriete !!!!
				getInstruments(createInstrument("tecan-bollinger1-and-tecan-roederer2", "Tecan-Bollinger1 / Tecan-Roederer2",null, true, null, CNG)),
				getContainerSupportCategories(new String[]{"96-well-plate"}), 
				getContainerSupportCategories(new String[]{"96-well-plate"}), 
				CNG));
		
		// FDS 28/02/2019 NGL-2368 aussi possibilite d'utiliser le BravoWs pour qPCR
		l.add(newInstrumentUsedType("Bravo Workstation + qPCR (Lightcycler 480 II)", "bravows-and-qpcr-lightcycler-480II", icfind.findByCode("liquid-handling-robot-and-qPCR-system"),
				getBravoWsAndLightCyclerProperties(),
				getInstrumentsBravoWsAndLightCycler(CNG),
				getContainerSupportCategories(new String[]{"96-well-plate"}),
				getContainerSupportCategories(new String[]{"96-well-plate"}),
				CNG));
		
		// FDS ajout 29/01/2016 JIRA NGL-894 pseudo instruments covaris+Sciclone (plaque input/plaque output)
		l.add(newInstrumentUsedType("Covaris E210 + Sciclone NGSX", "covaris-e210-and-sciclone-ngsx", icfind.findByCode("covaris-and-liquid-handling-robot"),
				getCovarisAndScicloneNGSXProperties(),
				getInstrumentsCovarisE210AndScicloneNGSX(CNG),
				getContainerSupportCategories(new String[]{"96-well-plate"}),
				getContainerSupportCategories(new String[]{"96-well-plate"}),
				CNG, false)); // tous les instruments sont inactivés=> inactiver le type...sert probablement a rien
		
		l.add(newInstrumentUsedType("Covaris LE220 + Sciclone NGSX", "covaris-le220-and-sciclone-ngsx", icfind.findByCode("covaris-and-liquid-handling-robot"),
				getCovarisAndScicloneNGSXProperties(), 
				getInstrumentsCovarisLE220AndScicloneNGSX(CNG),
				getContainerSupportCategories(new String[]{"96-well-plate"}),
				getContainerSupportCategories(new String[]{"96-well-plate" }),
				CNG));
		
		l.add(newInstrumentUsedType("Covaris E220 + Sciclone NGSX", "covaris-e220-and-sciclone-ngsx", icfind.findByCode("covaris-and-liquid-handling-robot"),
				getCovarisAndScicloneNGSXProperties(),
				getInstrumentsCovarisE220AndScicloneNGSX(CNG),
				getContainerSupportCategories(new String[]{"96-well-plate"}),
				getContainerSupportCategories(new String[]{"96-well-plate" }),
				CNG));
		
		// 16/11/2017 NGL-1691 ajout "Covaris LE220 + Bravo Workstation" 
		l.add(newInstrumentUsedType("Covaris LE220 + Bravo WS", "covaris-le220-and-bravows", icfind.findByCode("covaris-and-liquid-handling-robot"),
				getCovarisAndBravoWsProperties(),
				getInstrumentsCovarisLE220AndBravoWs(CNG),
				getContainerSupportCategories(new String[]{"96-well-plate"}), 
				getContainerSupportCategories(new String[]{"96-well-plate" }), 
				CNG));		
		
		// 16/11/2017 NGL-1691 ajout "Covaris E220 + Bravo Workstation"
		l.add(newInstrumentUsedType("Covaris E220 + Bravo WS", "covaris-e220-and-bravows", icfind.findByCode("covaris-and-liquid-handling-robot"),
				getCovarisAndBravoWsProperties(),
				getInstrumentsCovarisE220AndBravoWs(CNG),
				getContainerSupportCategories(new String[]{"96-well-plate"}), 
				getContainerSupportCategories(new String[]{"96-well-plate" }), 
				CNG));
		
		// FDS ajout 22/03/2016 JIRA NGL-982 pseudo instruments Janus+Cbot  
		// 23/01/2017 les cbots ancien modele n'existent plus => inactiver; 
		l.add(newInstrumentUsedType("Janus + cBot", "janus-and-cBot", icfind.findByCode("liquid-handling-robot-and-cBot"),
				getJanusAndCBotProperties(),
				getInstrumentsJanusAndCbot(CNG),
				getContainerSupportCategories(new String[]{"96-well-plate"}), 
				getContainerSupportCategories(new String[]{"flowcell-8"}), 
				CNG, false));// 25/10/2018 tous les instruments son inactivés => inactiver le type...sert probablement a rien
		
		// FDS 23/01/2017 ajout Janus + cBot-v2 
		l.add(newInstrumentUsedType("Janus + cBot-v2", "janus-and-cBotV2", icfind.findByCode("liquid-handling-robot-and-cBot"),
				getJanusAndCBotV2Properties(),
				getInstrumentsJanusAndCbotV2(CNG),
				getContainerSupportCategories(new String[]{"96-well-plate"}), 
				getContainerSupportCategories(new String[]{"flowcell-8"}), 
				CNG));
		
		// FDS ajout 29/07/2016 JIRA NGL-1027 pseudo instrument Masterycler EP-Gradient + Zephyr
		l.add(newInstrumentUsedType("Mastercycler EP-Gradient + Zephyr", "mastercycler-epg-and-zephyr", icfind.findByCode("thermocycler-and-liquid-handling-robot"),
				getMastercyclerEPGAndZephyrProperties(),
				getInstrumentsMasterEPGAndZephyr(CNG),
				getContainerSupportCategories(new String[]{"96-well-plate"}), 
				getContainerSupportCategories(new String[]{"96-well-plate"}), 
				CNG));
		
		// FDS 17/07/2017 NGL-1201 : pseudo instrument Masterycler EP-Gradient + Bravo
		l.add(newInstrumentUsedType("Mastercycler EP-Gradient + Bravo Workstation", "mastercycler-epg-and-bravows", icfind.findByCode("thermocycler-and-liquid-handling-robot"),
				getMastercyclerEPGAndBravoWsProperties(), 
				getInstrumentsMasterEPGAndBravoWs(CNG),
				getContainerSupportCategories(new String[]{"96-well-plate"}),
				getContainerSupportCategories(new String[]{"96-well-plate"}),
				CNG));
		
		// FDS 17/07/2017 NGL-1201 : pseudo instrument Masterycler Nexus SX-1 + Bravo Workstation
		l.add(newInstrumentUsedType("Mastercycler Nexus SX-1 + Bravo Workstation", "mastercycler-nexus-and-bravows", icfind.findByCode("thermocycler-and-liquid-handling-robot"),
				getMastercyclerNexusAndBravoWsProperties(), 
				getInstrumentsMasterNexusAndBravoWs(CNG),
				getContainerSupportCategories(new String[]{"96-well-plate"}),
				getContainerSupportCategories(new String[]{"96-well-plate"}),
				CNG));

		// FDS 15/11/2017 NGL-1691 : pseudo instrument "Bravo Workstation + Mastercycler EP-Gradient" (dans l'autre sens) avec des propriétés differentes (juste celles du bravo seul)...
		l.add(newInstrumentUsedType("Bravo Workstation + Mastercycler EP-Gradient", "bravows-and-mastercycler-epg", icfind.findByCode("thermocycler-and-liquid-handling-robot"),
				getBravoWsProperties(), 
				getInstrumentsBravoWsAndMasterEPG(CNG),
				getContainerSupportCategories(new String[]{"96-well-plate"}),
				getContainerSupportCategories(new String[]{"96-well-plate"}),
				CNG));

		// FDS 22/02/2018 NGL-1860 : pseudo instrument "Bravo Workstation + Mastercycler Nexus SX-1" (dans l'autre sens) avec des propriétés differentes (juste celles du bravo seul)..
		l.add(newInstrumentUsedType("Bravo Workstation + Mastercycler Nexus SX-1", "bravows-and-mastercycler-nexus", icfind.findByCode("thermocycler-and-liquid-handling-robot"),
				getBravoWsProperties(), 
				getInstrumentsBravoWsAndMasterNexus(CNG),
				getContainerSupportCategories(new String[]{"96-well-plate"}),
				getContainerSupportCategories(new String[]{"96-well-plate"}),
				CNG));
		
		
		/** Chromium **/
		// FDS ajout 20/02/2017 NGL-1167 : Chromium controller ( entree tubes / sortie strip-8 )
		l.add(newInstrumentUsedType("Chromium controller", "chromium-controller", icfind.findByCode("10x-genomics-instrument"), 
				getChromiumControllerProperties(),
				getInstruments(createInstrument("chromium1", "Chromium 1", null, true, null, CNG)),
				getContainerSupportCategories(new String[]{"tube"}), 
				getContainerSupportCategories(new String[]{"strip-8"}), 
				CNG));
		
		
		/** nanopore sequencers **/
		// FDS ajout 30/03/2017 : NGL-1225 ( Nanopore )
		l.add(newInstrumentUsedType("Mk1B", "mk1b", icfind.findByCode("nanopore-sequencer"), 
				getNanoporeSequencerProperties(),
				getInstrumentsMKIB(CNG),
				getContainerSupportCategories(new String[]{"tube"}),
				getContainerSupportCategories(new String[]{"flowcell-1"}),
				CNG));
		
		
		/** promethion sequencers **/
		l.add(newInstrumentUsedType("PromethION", "promethION", icfind.findByCode("nanopore-sequencer"), 
				getPromethIONProperties(),
				getInstrumentsPromethION(CNG),
				getContainerSupportCategories(new String[]{"tube"}),
				getContainerSupportCategories(new String[]{"flowcell-1"}),
				CNG));
		
		
		/** centrifugeuses **/
		l.add(newInstrumentUsedType("Eppendorf Centrifuge 5424", "eppendorf-5424", icfind.findByCode("centrifuge"), 
				getEppendorf5424Properties(),
				getInstruments(createInstrument("eppendorf-5424-1","Eppendorf 5424", null, true, null, CNG)),
				getContainerSupportCategories(new String[]{"tube"}), 
				getContainerSupportCategories(new String[]{"tube"}), 
				CNG));

		return l;
	}

	// NOTE FDS 12/07/2017: attention lors de la modification du booleen 'active' sur un instrument il y a un cache de 1 Heure
	@Override
	public void saveInstrumentUsedTypes(Map<String, List<ValidationError>> errors) throws DAOException {
		DAOHelpers.saveModels(InstrumentUsedType.class, getInstrumentUsedTypes(), errors);
	}


	/*-------- get properties methods ----------*/

	private static List<PropertyDefinition> getPromethIONProperties() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		
		propertyDefinitions.add(newPropertiesDefinition("Code Flowcell", "containerSupportCode", LevelService.getLevels(Level.CODE.ContainerOut),String.class, true, null,
			null, "single", 48, true, null, null));

		propertyDefinitions.add(newPropertiesDefinition("Version Flowcell", "flowcellChemistry", LevelService.getLevels(Level.CODE.ContainerOut, Level.CODE.Content),String.class, true, null,
			null, "single", 49, true, "R9.4-spot-on", null));

		propertyDefinitions.add(newPropertiesDefinition("Position", "position", 				LevelService.getLevels(Level.CODE.ContainerOut), String.class, true, null,
			getPromethionPosition(), "single", 50, true, null, null));

		return propertyDefinitions;
	}

	private static List<Value> getPromethionPosition() {
		List<Value> values = new ArrayList<>();
		
		// Voir qui pour verifier/actualiser cette liste ???
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
		List<PropertyDefinition> l = new ArrayList<>();
		
		// 17/01/2017 numérotation des propriétés
		l.add(newPropertiesDefinition("Type lectures","sequencingProgramType", LevelService.getLevels(Level.CODE.Instrument,Level.CODE.ContainerSupport),String.class, true, null, 
			DescriptionFactory.newValues("SR","PE"),"single", 70,true,null,null));

		l.add(newPropertiesDefinition("Code Flowcell", "containerSupportCode", LevelService.getLevels(Level.CODE.Instrument),                            String.class, true, null,
			null,"single", 80,true,null,null));
		
		l.add(newPropertiesDefinition("Piste contrôle","controlLane",          LevelService.getLevels(Level.CODE.Instrument),                            String.class, true, null, 
			DescriptionFactory.newValuesWithDefault("Pas de piste contrôle (auto-calibrage)","Pas de piste contrôle (auto-calibrage)","1","2","3","4","5","6","7","8"),"single", 90,true,"Pas de piste contrôle (auto-calibrage)", null));

		return l;
	}

	private static List<PropertyDefinition> getCBotInterneProperties() throws DAOException {
		List<PropertyDefinition> l = new ArrayList<>();

		/* 23/01/2017 strictement la meme liste que cBot standard!! simplification...*/
		l.addAll(getCBotProperties());

		return l;
	}

	// 23/01/2017 creation methode distincte...
	private static List<PropertyDefinition> getCBotV2Properties() throws DAOException {
		List<PropertyDefinition> l = new ArrayList<>();

		// propriete des V1
		l.addAll(getCBotProperties());

		// proprietes specifiques V2: NGL-1141: ne pas mettre ces proprietes en obligatoires=> pose probleme. Utiliser une regle drool pour l'experience prepa-flowcell
		l.add(newPropertiesDefinition("Code Strip",   "stripCode", LevelService.getLevels(Level.CODE.Instrument),String.class, false, null, null, "single", 60, true, null,null));
		
		// fichier generé cbotRunFile" (NON editable)
		l.add(newPropertiesDefinition("Fichier cBot", "cbotFile",  LevelService.getLevels(Level.CODE.Instrument),String.class, false, null, null, "single", 150, false ,null,null));

		return l;
	}

	private static List<PropertyDefinition> getHiseq2000Properties() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		
		propertyDefinitions.add(newPropertiesDefinition("Position",              "position",              LevelService.getLevels(Level.CODE.Instrument),String.class, true,DescriptionFactory.newValues("A","B"), "single",200));
		propertyDefinitions.add(newPropertiesDefinition("Type lectures",         "sequencingProgramType", LevelService.getLevels(Level.CODE.Instrument),String.class, true,DescriptionFactory.newValues("SR","PE"), "single",300));
		propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read1",       "nbCyclesRead1",         LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",400));
		propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read Index1", "nbCyclesReadIndex1",    LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",500));
		propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read2",       "nbCyclesRead2",         LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",700));
		propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read Index2", "nbCyclesReadIndex2",    LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",600));
		propertyDefinitions.add(newPropertiesDefinition("Piste contrôle",        "controlLane",           LevelService.getLevels(Level.CODE.Instrument),String.class, true,DescriptionFactory.newValuesWithDefault("Pas de piste contrôle (auto-calibrage)","Pas de piste contrôle (auto-calibrage)",
				"1","2","3","4","5","6","7","8"),"Pas de piste contrôle (auto-calibrage)","single",100));

		return propertyDefinitions;
	}

	private static List<PropertyDefinition> getMiseqProperties() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		
		propertyDefinitions.add(newPropertiesDefinition("Nom cassette Miseq",    "miseqReagentCassette", LevelService.getLevels(Level.CODE.Instrument),String.class, true,"single",100));
		propertyDefinitions.add(newPropertiesDefinition("Type lectures",         "sequencingProgramType",LevelService.getLevels(Level.CODE.Instrument),String.class, true,DescriptionFactory.newValues("SR","PE"), "single",200));
		propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read1",       "nbCyclesRead1",        LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",300));
		propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read Index1", "nbCyclesReadIndex1",   LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",400));
		propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read2",       "nbCyclesRead2",        LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",600));
		propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read Index2", "nbCyclesReadIndex2",   LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",500));

		return propertyDefinitions;
	}

	private static List<PropertyDefinition> getMiseqQCProperties() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		
		propertyDefinitions.add(newPropertiesDefinition("Nom cassette Miseq",   "miseqReagentCassette",  LevelService.getLevels(Level.CODE.Instrument),String.class,true,"single",100));
		propertyDefinitions.add(newPropertiesDefinition("Type lectures",         "sequencingProgramType",LevelService.getLevels(Level.CODE.Instrument),String.class, true,DescriptionFactory.newValues("SR","PE"), "single",200));
		propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read1",       "nbCyclesRead1",        LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",300));
		//propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read Index1", "nbCyclesReadIndex1", LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",400));
		propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read2",       "nbCyclesRead2",        LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",600));
		//propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read Index2", "nbCyclesReadIndex2", LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",500));
		propertyDefinitions.add(newPropertiesDefinition("Genome folder",         "genomeFolder",         LevelService.getLevels(Level.CODE.Instrument),String.class,true, null, null, "single", 700, true, "Homo_sapiens\\UCSC\\hg19\\Sequence\\WholeGenomeFasta", null));

		return propertyDefinitions;
	}

	private static List<PropertyDefinition> getNextseq500Properties() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();

		propertyDefinitions.add(newPropertiesDefinition("Type lectures",         "sequencingProgramType", LevelService.getLevels(Level.CODE.Instrument),String.class, true,DescriptionFactory.newValues("SR","PE"), "single",100));
		propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read1",       "nbCyclesRead1",         LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",200));
		propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read Index1", "nbCyclesReadIndex1",    LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",300));
		propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read2",       "nbCyclesRead2",         LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",500));
		propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read Index2", "nbCyclesReadIndex2",    LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",400));

		return propertyDefinitions;
	}
	
	private static List<PropertyDefinition> getHiseq2500Properties() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = getHiseq2000Properties();		

		propertyDefinitions.add(0, newPropertiesDefinition("Mode run",           "runMode",               LevelService.getLevels(Level.CODE.Instrument),String.class, true,DescriptionFactory.newValues("high-throughput","rapid run"), "single",50));

		return propertyDefinitions;
	}
	
	private static List<PropertyDefinition> getHiseq4000Properties() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		
		propertyDefinitions.add(newPropertiesDefinition("Position",              "position",              LevelService.getLevels(Level.CODE.Instrument),String.class, true,DescriptionFactory.newValues("A","B"), "single",100));
		propertyDefinitions.add(newPropertiesDefinition("Type lectures",         "sequencingProgramType", LevelService.getLevels(Level.CODE.Instrument),String.class, true,DescriptionFactory.newValues("SR","PE"), "single",200));
		propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read1",       "nbCyclesRead1",         LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",300));
		propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read Index1", "nbCyclesReadIndex1",    LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",400));
		propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read2",       "nbCyclesRead2",         LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",600));
		propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read Index2", "nbCyclesReadIndex2",    LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",500));
		
		return propertyDefinitions;
	}

	private static List<PropertyDefinition> getHiseqXProperties() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		// idem Hiseq4000 !!
		
		propertyDefinitions.add(newPropertiesDefinition("Position",              "position",              LevelService.getLevels(Level.CODE.Instrument),String.class, true,DescriptionFactory.newValues("A","B"), "single",100));
		propertyDefinitions.add(newPropertiesDefinition("Type lectures",         "sequencingProgramType", LevelService.getLevels(Level.CODE.Instrument),String.class, true,DescriptionFactory.newValues("SR","PE"), "single",200));
		propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read1",       "nbCyclesRead1",         LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",300));
		propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read Index1", "nbCyclesReadIndex1",    LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",400));
		propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read2",       "nbCyclesRead2",         LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",600));
		propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read Index2", "nbCyclesReadIndex2",    LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",500));
		
		return propertyDefinitions;
	}

	// NGL-1730: ajout Novaseq
	private static List<PropertyDefinition> getNovaseq6000Properties() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		// idem Hiseq4000, HiseqX !!
		
		propertyDefinitions.add(newPropertiesDefinition("Position",              "position",              LevelService.getLevels(Level.CODE.Instrument),String.class, true,DescriptionFactory.newValues("A","B"), "single",100));
		propertyDefinitions.add(newPropertiesDefinition("Type lectures",         "sequencingProgramType", LevelService.getLevels(Level.CODE.Instrument),String.class, true,DescriptionFactory.newValues("SR","PE"), "single",200));
		propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read1",       "nbCyclesRead1",         LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",300));
		propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read Index1", "nbCyclesReadIndex1",    LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",400));
		propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read2",       "nbCyclesRead2",         LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",600));
		propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read Index2", "nbCyclesReadIndex2",    LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",500));

		//NGL-1768: nouvelles propriétés
		//NGL-2191: nouveau type de flowcell S1, S2, S4 ( pas de S3 chez Illumina pour l'instant)
		propertyDefinitions.add(newPropertiesDefinition("Tube chargement (RFID)", "novaseqLoadingTube",   LevelService.getLevels(Level.CODE.Instrument),String.class, false, "single",700));
		// NGl-2624 ajout "SP"
		propertyDefinitions.add(newPropertiesDefinition("Type flowcell",          "novaseqFlowcellMode",  LevelService.getLevels(Level.CODE.Instrument),String.class, true,DescriptionFactory.newValues("SP","S1","S2","S4"), "single",800));

		return propertyDefinitions;
	}

	//FDS 02/02/2016 modifier 'program' en 'programCovaris' pour pourvoir creer les proprietes de l'instrument mixte covaris+Sciclone car sinon doublon de proprietes
	private static List<PropertyDefinition> getCovarisProperties() throws DAOException {
		List<PropertyDefinition> l = new ArrayList<>();

		l.add(newPropertiesDefinition("Programme", "program", LevelService.getLevels(Level.CODE.Instrument), String.class, false, null,
										null, "single",null,true, null,null));

		return l;
	}

	private static List<PropertyDefinition> getMastercyclerNexusProperties() throws DAOException {
		List<PropertyDefinition> l = new ArrayList<>();
		
		// 18/07/2017 correction !!! nbCycles => pcrCycleNumber		
		// NGL-2552 26/06/2019 correction Integer.class
		l.add(newPropertiesDefinition("Nbre Cycles PCR", "pcrCycleNumber", LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, null,
										null, "single",null,true, null,null));
		
		return l;
	}

	private static List<PropertyDefinition> getMastercyclerEPGradientProperties() throws DAOException {
		List<PropertyDefinition> l = new ArrayList<>();
		
		// 26/06/2019 correction Integer.class
		l.add(newPropertiesDefinition("Nbre Cycles PCR", "pcrCycleNumber", LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, null,
										null, "single",null,true, null,null));
		
		return l;
	}
	
	// FDS 26/06/2019 NGL-2552 ajouter pcrCycleNumber en containerOut sur l'instrument, optionnel
	private static List<PropertyDefinition> getMastercyclerDevProperties() throws DAOException {
		List<PropertyDefinition> l = new ArrayList<>();

		// !!!! NOTE 26/06/2019 les proprietes de niveau Container ( in ou out ) doivent etre ajoutée dans le datatable de l'experience par l'appel dans le
		// controleur javascript de:   atmService.addInstrumentPropertiesToDatatable(newValue.propertiesDefinitions);
		l.add(newPropertiesDefinition("Nbre Cycles PCR", "pcrCycleNumber",     LevelService.getLevels(Level.CODE.ContainerOut),Integer.class, false, null,
										null, "single",25, true, null,null));
		
		return l;
	}
	
	// FDS 26/06/2019 NGL-2552; 18/07/2019 NGL-2607 niveau Instrument + ContainerOut (il faut une position)
	private static List<PropertyDefinition> getMastercyclerProProperties() throws DAOException {
		List<PropertyDefinition> l = new ArrayList<>();

		// 25/09/2019 NGL 26-07 pas de niveau instrument!!! (voir aussi la NOTE ci dessus getMastercyclerDevProperties pour le javascript)
		// 04/10/2019  mandatory a etat Finished
		l.add(newPropertiesDefinition("Nbre Cycles PCR", "pcrCycleNumber", LevelService.getLevels(Level.CODE.ContainerOut),Integer.class, true,"F",
										null, "single",33,true, null,null));

		return l;
	}

	private static List<PropertyDefinition> getQuBitProperties() throws DAOException {
		List<PropertyDefinition> l = new ArrayList<>();

		// suppression de "Kit" demandée 29/11/2017

		return l;
	}

	//FDS 29/01/2016 ajout SicloneNGSX -- JIRA NGL-894
	private static List<PropertyDefinition> getScicloneNGSXProperties() throws DAOException {
		List<PropertyDefinition> l = new ArrayList<>();

		//FDS 25/10/2016 -- NGL-1025 : nouvelle liste (!! pas de contextualisation, tous les programmes seront listés dans toutes les experiences)
		// => les séparer au moins a la declaration..	
		ArrayList<String> progList = new ArrayList<>();

		// RNA 12/12/2016
		progList.add("Stranded_TotalRNA_Avril2016");
		progList.add("Stranded_TotalRNA_Avril2016_RAP_Plate");
		progList.add("Stranded_mRNA_Avril2016");
		progList.add("Stranded_mRNA_Avril2016_RAP_Plate");

		//Nano
		progList.add("TruSEQ_DNA_Nano");

		//PCR free
		progList.add("TruSEQ_DNA_PCR_Free_Library_Prep");
		progList.add("TruSEQ_DNA_PCR_Free_Library_Prep_DAP_Plate");
		
		//09/11/2017 Capture (valeurs reelles)
		progList.add("SureSelect XT initial SPRI cleanup");
		progList.add("SureSelect XT library prep");

		//transformer ArrayList progList en Array progList2 car newValue() prend un Array en argument !!
		String progList2[] = new String[progList.size()];
		progList2 = progList.toArray(progList2);

		//prop obligatoire, liste non editable
		l.add(newPropertiesDefinition("Programme Sciclone NGSX", "programScicloneNGSX", LevelService.getLevels(Level.CODE.Instrument),String.class, true, null,
							newValues(progList2), 
							"single",null,false, null,null));

		return l;
	}

	// 05/08/2016 Il faut une methode distincte pour ajouter la propriété "robotRunCode", et ne pas la mettre directement dans getScicloneNGSXProperties
	// sinon il y a un doublon pour l'instrument hybride CovarisAndScicloneNGSX
	private static List<PropertyDefinition> getScicloneNGSXAloneProperties() throws DAOException {
		List<PropertyDefinition> l = new ArrayList<>();

		l.addAll(getScicloneNGSXProperties());

		// optionnel, saisie libre
		l.add(newPropertiesDefinition("Nom du Run","robotRunCode", LevelService.getLevels(Level.CODE.Instrument),String.class, false, null,
										null, null, null, null, 
										"single", null, true ,null, null));
		return l;
	}

	//FDS 29/01/2016 (instrument hybride composé de 2 instruments) -- JIRA NGL-894
	//    ses propriétés sont la somme des propriétés de chacun (Attention au noms de propriété communs...)
	private static List<PropertyDefinition> getCovarisAndScicloneNGSXProperties() throws DAOException {
		List<PropertyDefinition> l = new ArrayList<>();

		// 18/07/2017 aussi utilise en Fragmentation/capture !!! liste obligatoire non editable
		l.add(newPropertiesDefinition("Programme Covaris", "programCovaris", LevelService.getLevels(Level.CODE.Instrument), String.class, true, null,
										newValues("PCR Free Prod NGS Final",
												  "SureSelect96 final"),
										"single", null, false ,null, null));

		l.addAll(getScicloneNGSXProperties());
		
		// optionnel, saisie libre
		l.add(newPropertiesDefinition("Nom du Run","robotRunCode", LevelService.getLevels(Level.CODE.Instrument),  String.class, false, null,
										null, null, null, null, 
										"single", null, true ,null, null));

		return l;
	}

	// FDS 16/11/2017 NLG-1691: ajout
	private static List<PropertyDefinition> getCovarisAndBravoWsProperties() throws DAOException {
		List<PropertyDefinition> l = new ArrayList<>();
		
		//  Programme Covaris liste obligatoire non editable
		l.add(newPropertiesDefinition("Programme Covaris", "programCovaris", LevelService.getLevels(Level.CODE.Instrument), String.class, true, null,
										newValues("SureSelect96 final",
												  "WG TruSeq Nano",        // ajout 05/07/2018
												  "PCR Free Prod final"),  // ajout 28/01/2019 NGL-2402
										"single", null, false ,null, null));
		
		//  Programme Bravo WS : optionnel, saisie libre
		l.add(newPropertiesDefinition("Programme Bravo WS","programBravoWs", LevelService.getLevels(Level.CODE.Instrument),String.class, false, null,
										null, null, null , null,
										"single", null, true ,null, null));

		// NGL-2160 :05/07/2018 ajouter nom du run: optionnel, saisie libre 
		l.add(newPropertiesDefinition("Nom du Run","robotRunCode", LevelService.getLevels(Level.CODE.Instrument),  String.class, false, null,
										null, null, null, null,
										"single", null, true ,null, null));

		return l;
	}

	//FDS 29/01/2016 ajout Janus -- JIRA NGL-894 
	private static List<PropertyDefinition> getJanusProperties() throws DAOException {
		List<PropertyDefinition> l = new ArrayList<>();

		//FDS 05/08/2016 le Janus est utilisé dans certaines experiences ou on ne veut pas tracer le programme => rendre cette propriété non obligatoire !
		//FDS 25/10/2016 -- NGL-1025 : nouvelle liste (!! pas de contextualisation, tous les programmes seront listés dans toutes les experiences)
		l.add(newPropertiesDefinition("Programme", "program", LevelService.getLevels(Level.CODE.Instrument), String.class, false, null,
										newValues("programme 1_normalisation",
												  "1_HiseqCluster_Normalisation_V0",
												  "1_HiseqCluster_Normalisation_gros_vol_tris",
												  "Transfert librairies"), //ajout apres livraison prod 27/06/2018), 
										"single", null, false ,null, null));
		
		return l;
	}

	//FDS 22/03/2016 ajout Janus+cbot --JIRA NGL-982
	//    17/01/2017 numérotation des propriétés;
	private static List<PropertyDefinition> getJanusAndCBotProperties() throws DAOException {
		List<PropertyDefinition> l = new ArrayList<>();

		l.add(newPropertiesDefinition("Programme", "program", LevelService.getLevels(Level.CODE.Instrument), String.class, true, null,
										newValues("Clusterstripprepworklist"), 
										"single", 40, false ,null, null));

		l.add(newPropertiesDefinition("Strip #", "stripDestination", LevelService.getLevels(Level.CODE.Instrument), String.class, true, null,
										newValues("1","2","3","4"), 
										"single", 50, true ,null, null));

		//Obligatoire a l'etat NEW
		l.add(newPropertiesDefinition("Source", "source", LevelService.getLevels(Level.CODE.ContainerIn), String.class, true, "N",
										Arrays.asList(newValue("1", "Source 1"),
													  newValue("2", "Source 2"),
													  newValue("3", "Source 3"),
													  newValue("4", "Source 4")),
										"single", 2, true , null, null));

		l.addAll(getCBotProperties());

		return l;
	}

	//FDS 23/01/2017 ajout Janus + cbot-v2
	//FDS 04/08/2017 NGL-1550 passage a 8 sources/6 strips pour le Janus
	private static List<PropertyDefinition> getJanusAndCBotV2Properties() throws DAOException {
		List<PropertyDefinition> l = new ArrayList<>();

		l.add(newPropertiesDefinition("Programme", "program", LevelService.getLevels(Level.CODE.Instrument), String.class, true, null,
				 						newValues("Clusterstripprepworklist"), "single", 40, false ,null, null));
		/* FDS 11/08/2017 attendre feu vert de la prod pour ajout nouveau programme....
		         						newValues("Clusterstripprepworklist",
		         						"2_HiseqCluster_ClusterStripPrep_worklist_US_plaque"), "single", 40, false ,null, null));
		*/
		//FDS 04/08/2017: NGL-1550 evolution du janus=> passer a 6 strips.
		l.add(newPropertiesDefinition("Strip #", "stripDestination", LevelService.getLevels(Level.CODE.Instrument), String.class, true, null,
										newValues("1","2","3","4","5","6"), "single", 50, true ,null, null));

		//Obligatoire a l'etat NEW
		//FDS 04/08/2017: NGL-1550 evolution du janus=> passer a 8 sources
		l.add(newPropertiesDefinition("Source", "source", LevelService.getLevels(Level.CODE.ContainerIn), String.class, true, "N",
										Arrays.asList(newValue("1", "Source 1"),
												      newValue("2", "Source 2"),
												      newValue("3", "Source 3"),
												      newValue("4", "Source 4"),
												      newValue("5", "Source 5"),
												      newValue("6", "Source 6"),
												      newValue("7", "Source 7"),
												      newValue("8", "Source 8")),
										"single", 2, true , null, null));
		
		l.addAll(getCBotV2Properties());
		
		return l;
	} 

	//FDS 04/10/2016 ajout EpMotion
	private static List<PropertyDefinition> getEpMotionProperties() throws DAOException {
			List<PropertyDefinition> l = new ArrayList<>();
			
			// propriete obligatoire ou pas ??????
			// liste des programmes pas encore definie
			l.add(newPropertiesDefinition("Programme", "program", LevelService.getLevels(Level.CODE.Instrument), String.class, false, null,
											newValues("programme 1",
													  "---"),                         // ajouté pour éviter selection automatique par defaut de programme 1
											"single", null, false ,null, null));
			return l;
	}

	//FDS 31/03/2016 ajout proprietes LightCyclers
	private static List<PropertyDefinition> getLightCyclerProperties() throws DAOException {
		List<PropertyDefinition> l = new ArrayList<>();
		
		l.add(newPropertiesDefinition("Secteur Plaque 96","sector96", LevelService.getLevels(Level.CODE.Instrument),String.class, true, null,
										newValues("1-48","49-96"), null, null , null, 
										"single", null, false ,null, null));
		
		return l;
	}

	// FDS 29/07/2016 NGL-1027 ajout propriétés pseudo instrument Mastercycler EP-Gradient + Zephyr 
	//     09/11/2017 NGL-1691  suppression valeurs par defaut ( pcrCycleNumber et AdnBeadVolumeRatio )
	private static List<PropertyDefinition> getMastercyclerEPGAndZephyrProperties() throws DAOException {
		List<PropertyDefinition> l = new ArrayList<>();

		//Mastercycler EPG
		l.add(newPropertiesDefinition("Nbre Cycles PCR","pcrCycleNumber", LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, null,
										null, null, null , null, "single", null, true ,null, null));
		
		//Zephyr
		l.add(newPropertiesDefinition("Ratio billes","AdnBeadVolumeRatio", LevelService.getLevels(Level.CODE.Instrument),Double.class, true, null,
										null, null, null , null, "single", null, true ,null, null));
		
		return l;
	}

	// FDS 17/07/2017 NGL-1201  ajout propriétés pseudo instrument Mastercycler EP-Gradient + Bravo Workstation
	//     09/11/2017 NGL-1691  suppression valeurs par defaut ( pcrCycleNumber et AdnBeadVolumeRatio )
	private static List<PropertyDefinition> getMastercyclerEPGAndBravoWsProperties() throws DAOException {
		List<PropertyDefinition> l = new ArrayList<>();
		
		//Mastercycler EPG
		l.add(newPropertiesDefinition("Nbre Cycles PCR","pcrCycleNumber", LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, null,
										null, null, null , null, "single", null, true ,null, null));
		
		// Bravo 
		l.add(newPropertiesDefinition("Ratio billes","AdnBeadVolumeRatio", LevelService.getLevels(Level.CODE.Instrument),Double.class, true, null,
										null, null, null , null, "single", null, true ,null, null));
		
		// FDS 09/11/2017 NGL-1691: ajout propriété "Programme Bravo WS" optionnel, saisie libre
		l.add(newPropertiesDefinition("Programme Bravo WS","programBravoWs", LevelService.getLevels(Level.CODE.Instrument),String.class, false, null,
										null, null, null , null, "single", null, true ,null, null));
		
		return l;
	}

	// FDS 17/07/2017 NGL-1201 Mastercycler Nexus SX-1 + Bravo Workstation
	//     09/11/2017 NGL-1691  suppression valeurs par defaut ( pcrCycleNumber et AdnBeadVolumeRatio )
	private static List<PropertyDefinition> getMastercyclerNexusAndBravoWsProperties() throws DAOException {
		List<PropertyDefinition> l = new ArrayList<>();
		
		//Mastercycler Nexus
		l.add(newPropertiesDefinition("Nbre Cycles PCR","pcrCycleNumber", LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, null,
										null, null, null , null, "single", null, true ,null, null));
		
		// Bravo
		l.add(newPropertiesDefinition("Ratio billes","AdnBeadVolumeRatio", LevelService.getLevels(Level.CODE.Instrument),Double.class, true, null,
										null, null, null , null, "single", null, true ,null, null));
		
		// FDS 09/11/2017 NGL-1691: ajout propriété "Programme Bravo WS" optionnel, saisie libre
		l.add(newPropertiesDefinition("Programme Bravo WS","programBravoWs", LevelService.getLevels(Level.CODE.Instrument),String.class, false, null,
										null, null, null , null, "single", null, true ,null, null));
		
		return l;
	}

	
	// FDS 09/11/2017 ajout pour NGL-1691 dans le cas ou BravoWS  utilisé seul
	private static List<PropertyDefinition>getBravoWsProperties()throws DAOException {
		List<PropertyDefinition> l = new ArrayList<>();
		
		// propriété "Programme Bravo WS" optionnel, saisie libre
		l.add(newPropertiesDefinition("Programme Bravo WS","programBravoWs", LevelService.getLevels(Level.CODE.Instrument),String.class, false, null,
										null, null, null , null, "single", null, true ,null, null));
		
		// ajout 15/03/2018 : NGL-1906: optionnel, saisie libre
		l.add(newPropertiesDefinition("Nom du Run","robotRunCode", LevelService.getLevels(Level.CODE.Instrument),  String.class, false, null,
										null, null, null, null, "single", null, true ,null, null));
				
		return l;
	}

	//FDS 20/02/2017 NGL-1167: Chromium controller
	private static List<PropertyDefinition> getChromiumControllerProperties() throws DAOException {
		List<PropertyDefinition> l = new ArrayList<>();
		
		//06/03/2017 chipPosition est une propriete de container et pas d'experience
		l.add(newPropertiesDefinition("Position sur puce", "chipPosition", LevelService.getLevels(Level.CODE.ContainerIn), String.class, true, null,
										newValues("1","2","3","4","5","6","7","8"),
										"single",23, true, null,null));
		
		return l;
	}

	//FDS 01/03/2017 NGL-1167: QC bioanalyser ajouté pour process Chromium
	private static List<PropertyDefinition> getBioanalyzerProperties() throws DAOException {
		List<PropertyDefinition> l = new ArrayList<>();
		
		// FDS 04/12/2018 NGL-1226 type de puce optionnel!!!!
		l.add(newPropertiesDefinition("Type puce", "chipType", LevelService.getLevels(Level.CODE.Instrument), String.class, false, null, 
										newValues("DNA 1000", "DNA HS"),
										"single", 10, true, null,null));
		
		// reunion 23/03/2017: la puce  HS n' a que 11 positions utilisables mais les puces 1K, 12K en ont 12=> ajouter position 12
		l.add(newPropertiesDefinition("Position sur puce", "chipPosition", LevelService.getLevels(Level.CODE.ContainerIn), String.class, false, null,
										newValues("1","2","3","4","5","6","7","8","9","10","11","12"),
										"single", 11, true, null,null));
		
		return l;
	}

	// FDS meme proprietes que minispin/CNS ???
	private static List<PropertyDefinition> getEppendorf5424Properties() throws DAOException {
		MeasureUnitDAO  mufind = MeasureUnit.find.get();
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();

		propertyDefinitions.add(newPropertiesDefinition("Programme", "program",  LevelService.getLevels(Level.CODE.Instrument),String.class, true,
				DescriptionFactory.newValues("G-TUBE"), "G-TUBE", null, null, null, "single", 1));

		propertyDefinitions.add(newPropertiesDefinition("Vitesse",    "speed",   LevelService.getLevels(Level.CODE.Instrument),String.class, false,
				null, "8000", MeasureCategory.find.get().findByCode(MeasureService.MEASURE_CAT_CODE_SPEED),
								mufind.findByCode("rpm"),
								mufind.findByCode("rpm"), "single", 2));

		// unite s
		propertyDefinitions.add(newPropertiesDefinition("Durée",      "duration", LevelService.getLevels(Level.CODE.Instrument),String.class, false, 
				null, "60", MeasureCategory.find.get().findByCode(MeasureService.MEASURE_CAT_CODE_TIME),
							mufind.findByCode("s"),
							mufind.findByCode("s"), "single", 3));
		
		return propertyDefinitions;
	}

	// FDS ajout 30/03/2017 NGL-1225 (Nanopore)
	private static List<PropertyDefinition> getNanoporeSequencerProperties() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();

		propertyDefinitions.add(newPropertiesDefinition("Code Flowcell",   "containerSupportCode", LevelService.getLevels(Level.CODE.Instrument),String.class, true, 
				"single",100));
		
		propertyDefinitions.add(newPropertiesDefinition("Version Flowcell","flowcellChemistry",    LevelService.getLevels(Level.CODE.Instrument,Level.CODE.Content),String.class, true,
				"single",200,"R9.4-spot-on"));
		
		propertyDefinitions.add(newPropertiesDefinition("Identifiant PC",   "pcId",                LevelService.getLevels(Level.CODE.Instrument),String.class, true, 
				"single",300));

		return propertyDefinitions;
	}

	// FDS 06/04/2018 NGL-1727
	private static List<PropertyDefinition> getScicloneNGSXAndZephyrProperties()throws DAOException {
		List<PropertyDefinition> l = new ArrayList<>();

		//Sciclone
		// PAS DEMANDE...l.addAll(getScicloneNGSXProperties());

		//Zephyr 

		return l;
	}


	/* commentee pour eviter warning compilo
	// FDS 06/04/2018 NGL-1727
	private static List<PropertyDefinition> getTecanEvo150AndZephyrProperties() throws DAOException {
		List<PropertyDefinition> l = new ArrayList<>();

		// TECAN EVO150
		// Pas de programme demandé pour l'instant
		
		// Zephyr
		// Pas de programme demandé pour l'instant
		
		return l;
	}
	*/
	
	/* commentee pour eviter warning compilo
	// FDS 24/01/2019
	private static List<PropertyDefinition> getTecanEvo150Properties() throws DAOException {
		List<PropertyDefinition> l = new ArrayList<>();
		
		// Pas demandé pour l'instant
		//l.add(newPropertiesDefinition("Programme", "program",  LevelService.getLevels(Level.CODE.Instrument),String.class, true,
		//		DescriptionFactory.newValues("program 1"), "program 1", null, null, null, "single", 1));
		
		return l;
	}
	*/
	
	// FDS 24/01/2019
	private static List<PropertyDefinition> getTecanEvo150AndLightCyclerProperties() throws DAOException {
		List<PropertyDefinition> l = new ArrayList<>();
		
		// TECAN EVO150
		// Pas de programme demandé pour l'instant

		// LightCycler 480 II
		l.addAll( getLightCyclerProperties());
		
		return l;
	}
	
	// FDS 28/01/2019
	private static List<PropertyDefinition> getBravoWsAndLightCyclerProperties() throws DAOException {
		List<PropertyDefinition> l = new ArrayList<>();
		
		// Bravo Ws
		// Pas de programme demandé pour l'instant

		// LightCycler 480 II
		l.addAll( getLightCyclerProperties());
		
		return l;
	}
	

	// FDS 03/09/2018 NGL-2219...copie code CNS
	private static List<PropertyDefinition> getCBotOnBoardNovaSeqProperties() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		
		propertyDefinitions.add(newPropertiesDefinition("Type lectures",  "sequencingProgramType", LevelService.getLevels(Level.CODE.Instrument,Level.CODE.ContainerSupport),String.class, true,
				DescriptionFactory.newValues("SR","PE"),"single"));
		
		// 18/09/2018=> il ne faut pas de propriété Type flowcell du tout au CNG!!!
		//propertyDefinitions.add(newPropertiesDefinition("Type flowcell","novaseqFlowcellMode",   LevelService.getLevels(Level.CODE.Instrument,Level.CODE.Content),         String.class, true,
		//		DescriptionFactory.newValues("S1","S2","S4"),"single"));
		
		propertyDefinitions.add(newPropertiesDefinition("Code Flowcell",  "containerSupportCode",  LevelService.getLevels(Level.CODE.Instrument),                            String.class, true,
				"single"));
		
		return propertyDefinitions;
	}

	/*------ get lists methods ------*/
	// 04/04/2018 utilisation de la variable List<Institute> CNG

	// 25/10/2018 separation
	private static List<Instrument> getInstrumentsCbot( List<Institute> CNG ) throws DAOException {
		List<Instrument> instruments=new ArrayList<>();
		
		// NGL-2583 19/06/2019  desactiver et les remettre dans l'association avec l'exp prepa-flowcell
		instruments.add(createInstrument("cBot1", "cBot1", null, false, null, CNG));
		instruments.add(createInstrument("cBot2", "cBot2", null, false, null, CNG));
		instruments.add(createInstrument("cBot3", "cBot3", null, false, null, CNG));
		instruments.add(createInstrument("cBot4", "cBot4", null, false, null, CNG));
		
		return instruments;
	}

	// 25/10/2018 separation
	private static List<Instrument> getInstrumentsCbotV2( List<Institute> CNG ) throws DAOException {
		List<Instrument> instruments=new ArrayList<>();
		
		// 19/09/2017 ajout 6 cbots V2: possibilité de lire le code barre du strip et d'importer un fichier XML...
		instruments.add(createInstrument("cBotA", "cBotA", null, true, null, CNG));
		instruments.add(createInstrument("cBotB", "cBotB", null, true, null, CNG));
		instruments.add(createInstrument("cBotC", "cBotC", null, true, null, CNG));
		instruments.add(createInstrument("cBotD", "cBotD", null, true, null, CNG));
		instruments.add(createInstrument("cBotE", "cBotE", null, true, null, CNG));
		instruments.add(createInstrument("cBotF", "cBotF", null, true, null, CNG));
		
		return instruments;
	}

	// 25/10/2018 separation
	private static List<Instrument> getInstrumentsCbotOnboard( List<Institute> CNG ) throws DAOException {
		List<Instrument> instruments=new ArrayList<>();
		
		instruments.add(createInstrument("cBot-Hi9-A",   "cBot-interne-Hi9-A",    null, true, null, CNG));
		instruments.add(createInstrument("cBot-Hi9-B",   "cBot-interne-Hi9-B",    null, true, null, CNG));
		instruments.add(createInstrument("cBot-Hi10-A",  "cBot-interne-Hi10-A",   null, true, null, CNG));
		instruments.add(createInstrument("cBot-Hi10-B",  "cBot-interne-Hi10-B",   null, true, null, CNG));
		instruments.add(createInstrument("cBot-Hi11-A",  "cBot-interne-Hi11-A",   null, true, null, CNG));
		instruments.add(createInstrument("cBot-Hi11-B",  "cBot-interne-Hi11-B",   null, true, null, CNG));
		instruments.add(createInstrument("cBot-Miseq1",  "cBot-interne-Miseq1",   null, true, null, CNG));
		instruments.add(createInstrument("cBot-NextSeq1","cBot-interne-Nextseq1", null, true, null, CNG)); // devrait etre: cBot-Nextseq1 ("s" et pas "S") !! si correction reprise historique MongoDB + impact Javascript/drools !!
		instruments.add(createInstrument("cBot-MarieCurix-A","cBot-interne-MarieCurix-A",null, true, null, CNG));
		instruments.add(createInstrument("cBot-MarieCurix-B","cBot-interne-MarieCurix-B",null, true, null, CNG));
		instruments.add(createInstrument("cBot-SuperNova-A","cBot-interne-SuperNova-A",null, true, null, CNG)); // NGL-2752 08/11/2019 manquait
		instruments.add(createInstrument("cBot-SuperNova-B","cBot-interne-SuperNova-B",null, true, null, CNG)); // NGL-2752 08/11/2019 manquait

		return instruments;
	}
	
	// FDS NGL-2662: changement des path "/env/ig/atelier/illumina/cng/"=> "/env/ig/atelier/illumina/cnrgh/"
	//                                   "/env/ig/atelier/nanopore/cng/"=> "/env/ig/atelier/nanopore/cnrgh/  TODO......

	// FDS 20/07/2016 JIRA SUPSQCNG-392 : ajout short names
	private static List<Instrument> getInstrumentsMiSeq( List<Institute> CNG ) throws DAOException {
		List<Instrument> instruments=new ArrayList<>();

		instruments.add(createInstrument("MISEQ1", "MISEQ1", "M1", true,  "/env/ig/atelier/illumina/cnrgh/MISEQ1/", CNG));
		instruments.add(createInstrument("MISEQ2", "MISEQ2", "M2", false, "/env/ig/atelier/illumina/cnrgh/MISEQ2/", CNG)); // inactif parceque ???

		return instruments;
	}
	
	private static List<Instrument> getInstrumentsMiSeqQC( List<Institute> CNG ) throws DAOException {
		List<Instrument> instruments=new ArrayList<>();

		instruments.add(createInstrument("MISEQ1-QC", "MISEQ1 QC", null, false, "/env/ig/atelier/illumina/cnrgh/MISEQ1/", CNG));
		instruments.add(createInstrument("MISEQ2-QC", "MISEQ2 QC", null, true,  "/env/ig/atelier/illumina/cnrgh/MISEQ2/", CNG));

		return instruments;
	}

	private static List<Instrument> getInstrumentsNextseq500( List<Institute> CNG ) throws DAOException {
		List<Instrument> instruments=new ArrayList<>();

		instruments.add(createInstrument("NEXTSEQ1", "NEXTSEQ1", "N1", true, "/env/ig/atelier/illumina/cnrgh/NEXTSEQ1/", CNG));
		
		return instruments;
	}

	private static List<Instrument> getInstrumentsHiseq4000( List<Institute> CNG ) throws DAOException {
		List<Instrument> instruments=new ArrayList<>();

		instruments.add(createInstrument("FALBALA", "FALBALA", "H4", true, "/env/ig/atelier/illumina/cnrgh/FALBALA/", CNG));
		//GA ajout temporaire de 2 instruments du CNS (jusqu'a ??); inactiver
		instruments.add(createInstrument("TORNADE", "TORNADE", "H5", false, "/env/ig/atelier/illumina/cns/TORNADE", CNG));
		instruments.add(createInstrument("RAFALE",  "RAFALE",  "H9", false, "/env/ig/atelier/illumina/cns/RAFALE", CNG));
		
		return instruments;
	}

	private static List<Instrument> getInstrumentsHiseqX( List<Institute> CNG ) throws DAOException {
		List<Instrument> instruments=new ArrayList<>();

		instruments.add(createInstrument("ASTERIX",   "ASTERIX",    "X1", true, "/env/ig/atelier/illumina/cnrgh/ASTERIX/",    CNG));
		instruments.add(createInstrument("OBELIX",    "OBELIX",     "X2", true, "/env/ig/atelier/illumina/cnrgh/OBELIX/",     CNG));
		instruments.add(createInstrument("IDEFIX",    "IDEFIX",     "X3", true, "/env/ig/atelier/illumina/cnrgh/IDEFIX/",     CNG));
		instruments.add(createInstrument("PANORAMIX", "PANORAMIX",  "X4", true, "/env/ig/atelier/illumina/cnrgh/PANORAMIX/",  CNG));
		instruments.add(createInstrument("DIAGNOSTIX","DIAGNOSTIX", "X5", true, "/env/ig/atelier/illumina/cnrgh/DIAGNOSTIX/", CNG));
		instruments.add(createInstrument("EXTX1",     "EXTX1",      "X6", true, "/env/ig/atelier/illumina/cnrgh/EXTX1/",      CNG));
		instruments.add(createInstrument("EXTHISEQX", "EXTHISEQX",  null, true, "/env/ig/atelier/illumina/cnrgh/EXTHISEQX/",  CNG));
		
		return instruments;
	}

	// 06/12/2017 FDS : ne sont plus actifs=> booleen a false,
	public static List<Instrument> getInstrumentsHiseq2000( List<Institute> CNG ) throws DAOException{
		List<Instrument> instruments=new ArrayList<>();

		instruments.add(createInstrument("HISEQ1", "HISEQ1", null, false, "/env/ig/atelier/illumina/cnrgh/HISEQ1/", CNG));
		instruments.add(createInstrument("HISEQ2", "HISEQ2", null, false, "/env/ig/atelier/illumina/cnrgh/HISEQ2/", CNG));
		instruments.add(createInstrument("HISEQ3", "HISEQ3", null, false, "/env/ig/atelier/illumina/cnrgh/HISEQ3/", CNG));
		instruments.add(createInstrument("HISEQ4", "HISEQ4", null, false, "/env/ig/atelier/illumina/cnrgh/HISEQ4/", CNG));
		instruments.add(createInstrument("HISEQ5", "HISEQ5", null, false, "/env/ig/atelier/illumina/cnrgh/HISEQ5/", CNG));
		instruments.add(createInstrument("HISEQ6", "HISEQ6", null, false, "/env/ig/atelier/illumina/cnrgh/HISEQ6/", CNG));
		instruments.add(createInstrument("HISEQ7", "HISEQ7", null, false, "/env/ig/atelier/illumina/cnrgh/HISEQ7/", CNG));
		instruments.add(createInstrument("HISEQ8", "HISEQ8", null, false, "/env/ig/atelier/illumina/cnrgh/HISEQ8/", CNG) );

		return instruments;
	}

	public static List<Instrument> getInstrumentsHiseq2500 (List<Institute> CNG ) throws DAOException{
		List<Instrument> instruments=new ArrayList<>();

		instruments.add(createInstrument("HISEQ9",  "HISEQ9",  "H1", true, "/env/ig/atelier/illumina/cnrgh/HISEQ9/",  CNG));
		instruments.add(createInstrument("HISEQ10", "HISEQ10", "H2", true, "/env/ig/atelier/illumina/cnrgh/HISEQ10/", CNG));
		instruments.add(createInstrument("HISEQ11", "HISEQ11", "H3", true, "/env/ig/atelier/illumina/cnrgh/HISEQ11/", CNG));

		return instruments;
	}

	// FDS ajout 30/03/2017 NGL-1225 (Nanopore)
	//     modification de path 04/09/2019 cng=>cnrgh
	private static List<Instrument> getInstrumentsMKIB( List<Institute> CNG ) throws DAOException {
		List<Instrument> instruments=new ArrayList<>();

		instruments.add(createInstrument("MN18834", "MN18834", null, true, "/env/ig/atelier/nanopore/cnrgh/MN18834", CNG));
		instruments.add(createInstrument("MN19213", "MN19213", null, true, "/env/ig/atelier/nanopore/cnrgh/MN19213", CNG));
		instruments.add(createInstrument("MN19240", "MN19240", null, true, "/env/ig/atelier/nanopore/cnrgh/MN19240", CNG));
		instruments.add(createInstrument("MN19270", "MN19270", null, true, "/env/ig/atelier/nanopore/cnrgh/MN19270", CNG));
		instruments.add(createInstrument("MN19813", "MN19813", null, true, "/env/ig/atelier/nanopore/cnrgh/MN19813", CNG));
		instruments.add(createInstrument("MN19802", "MN19802", null, true, "/env/ig/atelier/nanopore/cnrgh/MN19802", CNG));
		instruments.add(createInstrument("MN19190", "MN19190", null, true, "/env/ig/atelier/nanopore/cnrgh/MN19190", CNG));
		
		return instruments;
	}

	private static List<Instrument> getInstrumentsPromethION( List<Institute> CNG ) throws DAOException {
		List<Instrument> instruments=new ArrayList<>();

		instruments.add(createInstrument("PCT0037", "PCT0037", null, true, "/env/ig/atelier/nanopore/cnrgh/PCT0037/PCT0037/", CNG));
		
		return instruments;
	}

	// FDS ajout 06/12/2017 NGL-1730 (Novaseq6000) + SUPSQCNG-506 (EXTNOVASEQ)
	private static List<Instrument> getInstrumentsNovaseq6000( List<Institute> CNG ) throws DAOException {
		List<Instrument> instruments=new ArrayList<>();

		// FDS 01/07/2019 modification par l'equipe system des path ( suppression /ig/ +  cng->cnrgh )
		//	   modification 04/09/2019 NGL-2662: remettre ig  pour rester coherent
		instruments.add(createInstrument("MARIECURIX", "MARIECURIX", "V1", true, "/env/ig/atelier/illumina/cnrgh/MARIECURIX/",  CNG));
		instruments.add(createInstrument("SUPERNOVA",  "SUPERNOVA",  "V2", true, "/env/ig/atelier/illumina/cnrgh/SUPERNOVA/",   CNG)); // FDS 26/06/2019 NGL-2595 ajout
		instruments.add(createInstrument("EXTNOVASEQ", "EXTNOVASEQ", null, true, "/env/ig/atelier/illumina/cnrgh/EXTNOVASEQ/",  CNG));

		return instruments;
	}

	// 25/10/2018 separation
	private static List<Instrument> getInstrumentsBioanalyser( List<Institute> CNG ) throws DAOException {
		List<Instrument> instruments=new ArrayList<>();
		
		instruments.add(createInstrument("bioAnalyzer1", "BioAnalyzer 1", null, true, null, CNG));
		instruments.add(createInstrument("bioAnalyzer2", "BioAnalyzer 2", null, true, null, CNG)); // ajout 30/03/2017
		
		return instruments;
	}

	// 25/10/2018 separation
	private static List<Instrument> getInstrumentsLabChipGX( List<Institute> CNG ) throws DAOException {
		List<Instrument> instruments=new ArrayList<>();

		instruments.add(createInstrument("labGX",  "LABCHIP_GX 1", null, true, null, CNG)); // !!pas de "1" dans le code, si ajout alors faire une reprise historique MONGODB
		instruments.add(createInstrument("labGX2", "LABCHIP_GX 2", null, true, null, CNG));

		return instruments;
	}

	// 25/10/2018 separation
	private static List<Instrument> getInstrumentsMasterNexus( List<Institute> CNG ) throws DAOException {
		List<Instrument> instruments=new ArrayList<>();
		//Production
		instruments.add(createInstrument("mastercycler-nexus5", "Mastercycler 5 (Nexus SX-1)", null, true, null, CNG));
		instruments.add(createInstrument("mastercycler-nexus6", "Mastercycler 6 (Nexus SX-1)", null, true, null, CNG));
		
		//Developpement
		// NGL-2552: supprimer les anciens instruments, il ya maintenant un nouveau type pour equipes dev
		//instruments.add(createInstrument("mastercycler-nexus7", "Mastercycler 7 (Nexus SX-1)", null, true, null, CNG)); // reprise: 2 exp nanopore-frg
		//instruments.add(createInstrument("mastercycler-nexus8", "Mastercycler 8 (Nexus SX-1)", null, true, null, CNG)); // reprise: 0
		//instruments.add(createInstrument("mastercycler-nexus9", "Mastercycler 9 (Nexus SX-1)", null, true, null, CNG)); // reprise: 0

		return instruments;
	}

	// 25/10/2018 separation
	private static List<Instrument> getInstrumentsMasterEpGradient( List<Institute> CNG ) throws DAOException {
		List<Instrument> instruments=new ArrayList<>();
		
		//Production
		instruments.add(createInstrument("mastercycler-ep-gradient1", "Mastercycler 1 (EP-Gradient)", null, true, null, CNG));
		instruments.add(createInstrument("mastercycler-ep-gradient2", "Mastercycler 2 (EP-Gradient)", null, true, null, CNG));
		instruments.add(createInstrument("mastercycler-ep-gradient3", "Mastercycler 3 (EP-Gradient)", null, true, null, CNG));
		instruments.add(createInstrument("mastercycler-ep-gradient4", "Mastercycler 4 (EP-Gradient)", null, true, null, CNG));

		return instruments;
	}

	// 25/10/2018 separation
	private static List<Instrument> getInstrumentsLightCycler( List<Institute> CNG ) throws DAOException {
		List<Instrument> instruments=new ArrayList<>();
		
		instruments.add(createInstrument("lightCycler1", "LightCycler 1", null, true, null, CNG));
		instruments.add(createInstrument("lightCycler2", "LightCycler 2", null, true, null, CNG));
		instruments.add(createInstrument("lightCycler3", "LightCycler 3", null, true, null, CNG));

		return instruments;
	}

	// 25/10/2018 separation
	private static List<Instrument> getInstrumentsQubit( List<Institute> CNG ) throws DAOException {
		List<Instrument> instruments=new ArrayList<>();
		
		instruments.add(createInstrument("quBit1", "QuBit 1", null, true, null, CNG));
		instruments.add(createInstrument("quBit2", "QuBit 2", null, true, null, CNG));// 29/11/2017 ajout quBit2
		
		return instruments;
	}

	// 25/10/2018 separation
	private static List<Instrument> getInstrumentsSpectraMax( List<Institute> CNG ) throws DAOException {
		List<Instrument> instruments=new ArrayList<>();
		
		instruments.add(createInstrument("spectramax-bank1", "SpectraMax Banque 1", null, true,  null, CNG));
		instruments.add(createInstrument("spectramax-bank2", "SpectraMax Banque 2", null, true,  null, CNG));
		instruments.add(createInstrument("spectramax-prod1", "SpectraMax Prod",     null, false, null, CNG));// pas encore livré (active=false)
		
		return instruments;
	}
	
	// 25/10/2018 separation
	private static List<Instrument> getInstrumentsNGS( List<Institute> CNG ) throws DAOException {
		List<Instrument> instruments=new ArrayList<>();
		
		instruments.add(createInstrument("ngs1", "NGS 1",null, false, null, CNG));// FDS 29/08/2017 NGS-1 plus utilisé=> inactiver
		instruments.add(createInstrument("ngs2", "NGS 2",null, true,  null, CNG));
		instruments.add(createInstrument("ngs3", "NGS 3",null, true,  null, CNG));// FDS 29/08/2017 ajout
		
		return instruments;
	}
	
	// 25/10/2018 separation
	private static List<Instrument> getInstrumentsBravoWorkstation( List<Institute> CNG ) throws DAOException {
		List<Instrument> instruments=new ArrayList<>();
		
		// Production
		instruments.add(createInstrument("bravo-workstation1", "Bravo Workstation 1",null, true, null, CNG));// postPCR
		instruments.add(createInstrument("bravo-workstation3", "Bravo Workstation 3",null, true, null, CNG));// prePCR
		instruments.add(createInstrument("bravo-workstation4", "Bravo Workstation 4",null, true, null, CNG));// prePCR ajout bravo-workstation4 17/10/2018 NGL-2281
		
		// developpement
		instruments.add(createInstrument("bravo-workstation2", "Bravo Workstation 2",null, true, null, CNG));
		
		return instruments;
	}
	
	// 25/10/2018 separation
	private static List<Instrument> getInstrumentsScicloneNGSXAndZephyr( List<Institute> CNG ) throws DAOException {
		List<Instrument> instruments=new ArrayList<>();
		
		// NGS-1 plus utilisé
		instruments.add(createInstrument("ngs2-and-zephyr1","NGS-2 / Zephyr 1",null, true, null, CNG));
		instruments.add(createInstrument("ngs3-and-zephyr1","NGS-3 / Zephyr 1",null, true, null, CNG));
		
		return instruments;
	}
	
	// 25/10/2018 separation
	// FDS 12/07/2017 Covaris E210 plus utilisé=> inactiver les instruments mixtes
	private static List<Instrument> getInstrumentsCovarisE210AndScicloneNGSX( List<Institute> CNG ) throws DAOException {
		List<Instrument> instruments=new ArrayList<>();
		
		instruments.add(createInstrument("covaris1-and-ngs1", "Covaris 1 / NGS 1", null, false, null, CNG));
		instruments.add(createInstrument("covaris1-and-ngs2", "Covaris 1 / NGS 2", null, false, null, CNG));
		instruments.add(createInstrument("covaris1-and-ngs3", "Covaris 1 / NGS 3", null, false, null, CNG));
		
		return instruments;
	}
	
	// 25/10/2018 separation
	private static List<Instrument> getInstrumentsCovarisLE220AndScicloneNGSX( List<Institute> CNG ) throws DAOException {
		List<Instrument> instruments=new ArrayList<>();
		
		instruments.add(createInstrument("covaris2-and-ngs1", "Covaris 2 / NGS 1", null, false, null, CNG));// FDS 29/08/2017 NGS-1 plus utilisé=> inactiver
		instruments.add(createInstrument("covaris2-and-ngs2", "Covaris 2 / NGS 2", null, true,  null, CNG));
		instruments.add(createInstrument("covaris2-and-ngs3", "Covaris 2 / NGS 3", null, true,  null, CNG));// FDS 29/08/2017 ajout
		
		return instruments;
	}
	
	// 25/10/2018 separation
	private static List<Instrument> getInstrumentsCovarisE220AndScicloneNGSX( List<Institute> CNG ) throws DAOException {
		List<Instrument> instruments=new ArrayList<>();
		
		instruments.add(createInstrument("covaris3-and-ngs1", "Covaris 3 / NGS 1", null, false, null, CNG));// FDS 29/08/2017 NGS-1 plus utilisé=> inactiver
		instruments.add(createInstrument("covaris3-and-ngs2", "Covaris 3 / NGS 2", null, true,  null, CNG));
		instruments.add(createInstrument("covaris4-and-ngs3", "Covaris 4 / NGS 3", null, true,  null, CNG));// FDS 29/08/2017 ajout
		
		return instruments;
	}

	// 25/10/2018 separation
	private static List<Instrument> getInstrumentsCovarisLE220AndBravoWs( List<Institute> CNG ) throws DAOException {
		List<Instrument> instruments=new ArrayList<>();
		
		instruments.add(createInstrument("covaris2-and-bravows1", "Covaris 2 / Bravo Workstation 1", null, false, null, CNG));// FDS 22/02/2018 NGL-1860: inactiver le bravows1
		instruments.add(createInstrument("covaris2-and-bravows2", "Covaris 2 / Bravo Workstation 2", null, true,  null, CNG));
		instruments.add(createInstrument("covaris2-and-bravows3", "Covaris 2 / Bravo Workstation 3", null, true,  null, CNG));
		instruments.add(createInstrument("covaris2-and-bravows4", "Covaris 2 / Bravo Workstation 4", null, true,  null, CNG));// 17/10/2018 NGL-2281 ajout bravows4 
		
		return instruments;
	}
	
	// 25/10/2018 separation
	private static List<Instrument> getInstrumentsCovarisE220AndBravoWs( List<Institute> CNG ) throws DAOException {
		List<Instrument> instruments=new ArrayList<>();
		
		instruments.add(createInstrument("covaris3-and-bravows1", "Covaris 3 / Bravo Workstation 1", null, false, null, CNG));// FDS 22/02/2018 NGL-1860: inactiver le bravows1
		instruments.add(createInstrument("covaris3-and-bravows2", "Covaris 3 / Bravo Workstation 2", null, true,  null, CNG));
		instruments.add(createInstrument("covaris3-and-bravows3", "Covaris 3 / Bravo Workstation 3", null, true,  null, CNG));
		instruments.add(createInstrument("covaris3-and-bravows4", "Covaris 3 / Bravo Workstation 4", null, true,  null, CNG));// 17/10/2018 NGL-2281 ajout bravows4
		
		return instruments;
	}
	
	// 25/10/2018 separation
	// 25/10/2018 les cbot on été remplacés par les CbotV2 => inactiver
	private static List<Instrument> getInstrumentsJanusAndCbot( List<Institute> CNG ) throws DAOException {
		List<Instrument> instruments=new ArrayList<>();
		
		instruments.add(createInstrument("janus1-and-cBot1", "Janus 1 / cBot1", null, false, null, CNG));
		instruments.add(createInstrument("janus1-and-cBot2", "Janus 1 / cBot2", null, false, null, CNG));
		instruments.add(createInstrument("janus1-and-cBot3", "Janus 1 / cBot3", null, false, null, CNG));
		instruments.add(createInstrument("janus1-and-cBot4", "Janus 1 / cBot4", null, false, null, CNG));
		
		return instruments;
	}
	
	// 25/10/2018 separation
	private static List<Instrument> getInstrumentsJanusAndCbotV2( List<Institute> CNG ) throws DAOException {
		List<Instrument> instruments=new ArrayList<>();
		
		instruments.add(createInstrument("janus1-and-cBotA", "Janus 1 / cBotA", null, true, null, CNG));
		instruments.add(createInstrument("janus1-and-cBotB", "Janus 1 / cBotB", null, true, null, CNG));
		instruments.add(createInstrument("janus1-and-cBotC", "Janus 1 / cBotC", null, true, null, CNG));
		instruments.add(createInstrument("janus1-and-cBotD", "Janus 1 / cBotD", null, true, null, CNG));
		instruments.add(createInstrument("janus1-and-cBotE", "Janus 1 / cBotE", null, true, null, CNG));
		instruments.add(createInstrument("janus1-and-cBotF", "Janus 1 / cBotF", null, true, null, CNG));
		
		// 06/05/2019 ajout du Janus2
		instruments.add(createInstrument("janus2-and-cBotA", "Janus 2 / cBotA", null, true, null, CNG));
		instruments.add(createInstrument("janus2-and-cBotB", "Janus 2 / cBotB", null, true, null, CNG));
		instruments.add(createInstrument("janus2-and-cBotC", "Janus 2 / cBotC", null, true, null, CNG));
		instruments.add(createInstrument("janus2-and-cBotD", "Janus 2 / cBotD", null, true, null, CNG));
		instruments.add(createInstrument("janus2-and-cBotE", "Janus 2 / cBotE", null, true, null, CNG));
		instruments.add(createInstrument("janus2-and-cBotF", "Janus 2 / cBotF", null, true, null, CNG));
		return instruments;
	}
	
	// 25/10/2018 separation
	private static List<Instrument> getInstrumentsMasterEPGAndZephyr( List<Institute> CNG ) throws DAOException {
		List<Instrument> instruments=new ArrayList<>();
		
		// 16/09/2016 un seul Zephyr pour l'instant donc=> Zephyr1; ne laisser que Mastercycler1 et Mastercycler2
		instruments.add(createInstrument("mastercycler1-and-zephyr1", "Mastercycler 1 (EP-Gradient) / Zephyr 1", null, true, null, CNG));
		instruments.add(createInstrument("mastercycler2-and-zephyr1", "Mastercycler 2 (EP-Gradient) / Zephyr 1", null, true, null, CNG));
		
		return instruments;
	}
	
	// 25/10/2018 separation
	private static List<Instrument> getInstrumentsMasterEPGAndBravoWs( List<Institute> CNG ) throws DAOException {
		List<Instrument> instruments=new ArrayList<>();
		
		instruments.add(createInstrument("mastercycler1-and-bravows1", "Mastercycler 1 (EP-Gradient) / Bravo Workstation 1", null, true, null, CNG)); // A SUPPRIMER NGL-2128 ????
		instruments.add(createInstrument("mastercycler2-and-bravows1", "Mastercycler 2 (EP-Gradient) / Bravo Workstation 1", null, true, null, CNG)); // A SUPPRIMER NGL-2128 ????
		instruments.add(createInstrument("mastercycler4-and-bravows1", "Mastercycler 4 (EP-Gradient) / Bravo Workstation 1", null, true, null, CNG));
		
		instruments.add(createInstrument("mastercycler1-and-bravows3", "Mastercycler 1 (EP-Gradient) / Bravo Workstation 3", null, true, null, CNG)); // 18/06/2018 NGL-2125 ajout
		instruments.add(createInstrument("mastercycler2-and-bravows3", "Mastercycler 2 (EP-Gradient) / Bravo Workstation 3", null, true, null, CNG)); // 18/06/2018 NGL-2125 ajout
		instruments.add(createInstrument("mastercycler3-and-bravows3", "Mastercycler 3 (EP-Gradient) / Bravo Workstation 3", null, true, null, CNG)); // 18/06/2018 NGL-2125 ajout
		
		instruments.add(createInstrument("mastercycler1-and-bravows4", "Mastercycler 1 (EP-Gradient) / Bravo Workstation 4", null, true, null, CNG)); // 17/10/2018 NGL-2281 ajout bravows4
		instruments.add(createInstrument("mastercycler2-and-bravows4", "Mastercycler 2 (EP-Gradient) / Bravo Workstation 4", null, true, null, CNG)); // 17/10/2018 NGL-2281 ajout bravows4
		instruments.add(createInstrument("mastercycler3-and-bravows4", "Mastercycler 3 (EP-Gradient) / Bravo Workstation 4", null, true, null, CNG)); // 17/10/2018 NGL-2281 ajout bravows4
		
		
		// combinaison mastercycler PROD +  Bravo Workstation DEV ====> pas possible !!!!!
		instruments.add(createInstrument("mastercycler1-and-bravows2", "Mastercycler 1 (EP-Gradient) / Bravo Workstation 2", null, true, null, CNG)); // A SUPPRIMER NGL-2128
		instruments.add(createInstrument("mastercycler2-and-bravows2", "Mastercycler 2 (EP-Gradient) / Bravo Workstation 2", null, true, null, CNG)); // A SUPPRIMER NGL-2128
		instruments.add(createInstrument("mastercycler4-and-bravows2", "Mastercycler 4 (EP-Gradient) / Bravo Workstation 2", null, true, null, CNG)); // A SUPPRIMER NGL-2128
		
		return instruments;
	}
	
	// FDS 25/10/2018 separation
	private static List<Instrument> getInstrumentsMasterNexusAndBravoWs( List<Institute> CNG ) throws DAOException {
		List<Instrument> instruments=new ArrayList<>();
		
		// FDS 22/02/2018 NGL-1860 : correction: les combinaisons utilisees sont : Mastercycler Nexus 5 + bravo 1 (PROD)  Et   Mastercycler Nexus 7 ou 8  + bravo 2 (DEV)
		instruments.add(createInstrument("mastercycler5-and-bravows1", "Mastercycler 5 (Nexus SX-1) / Bravo Workstation 1", null, true, null, CNG));
		instruments.add(createInstrument("mastercycler6-and-bravows1", "Mastercycler 6 (Nexus SX-1) / Bravo Workstation 1", null, true, null, CNG));// 18/06/2018 NGL-2125 ajout
		
		// FDS 14/06/2019 NGL-2552: equipes DEV ne veulent pas tracer les mastercyclers => il y a maintenant un nouveau type d'instrument
		//instruments.add(createInstrument("mastercycler7-and-bravows2", "Mastercycler 7 (Nexus SX-1) / Bravo Workstation 2", null, true, null, CNG)); A SUPPRIMER, reprise non
		//instruments.add(createInstrument("mastercycler8-and-bravows2", "Mastercycler 8 (Nexus SX-1) / Bravo Workstation 2", null, true, null, CNG)); A SUPPRIMER, reprise non
		
		return instruments;
	}
	
	// FDS 25/10/2018 separation
	private static List<Instrument> getInstrumentsBravoWsAndMasterEPG( List<Institute> CNG ) throws DAOException {
		List<Instrument> instruments=new ArrayList<>();
		
		instruments.add(createInstrument("bravows1-and-mastercycler1", "Bravo Workstation 1 / Mastercycler 1 (EP-Gradient)", null, true, null, CNG));// A SUPPRIMER NGL-2128 ???
		instruments.add(createInstrument("bravows1-and-mastercycler2", "Bravo Workstation 1 / Mastercycler 2 (EP-Gradient)", null, true, null, CNG));// A SUPPRIMER NGL-2128 ???
		instruments.add(createInstrument("bravows1-and-mastercycler4", "Bravo Workstation 1 / Mastercycler 4 (EP-Gradient)", null, true, null, CNG));

		instruments.add(createInstrument("bravows3-and-mastercycler1", "Bravo Workstation 3 / Mastercycler 1 (EP-Gradient)", null, true, null, CNG));// 18/06/2018 NGL-2125 ajout
		instruments.add(createInstrument("bravows3-and-mastercycler2", "Bravo Workstation 3 / Mastercycler 2 (EP-Gradient)", null, true, null, CNG));// 18/06/2018 NGL-2125 ajout
		
		instruments.add(createInstrument("bravows4-and-mastercycler1", "Bravo Workstation 4 / Mastercycler 1 (EP-Gradient)", null, true, null, CNG));// 17/10/2018 NGL-2281 ajout bravows4
		instruments.add(createInstrument("bravows4-and-mastercycler2", "Bravo Workstation 4 / Mastercycler 2 (EP-Gradient)", null, true, null, CNG));// 17/10/2018 NGL-2281 ajout bravows4
		
		// combinaison  Bravo Workstation DEV + mastercycler PROD ====> pas possible !!!!!
		instruments.add(createInstrument("bravows2-and-mastercycler1", "Bravo Workstation 2 / Mastercycler 1 (EP-Gradient)", null, false, null, CNG));// A SUPPRIMER NGL-2128
		instruments.add(createInstrument("bravows2-and-mastercycler2", "Bravo Workstation 2 / Mastercycler 2 (EP-Gradient)", null, false, null, CNG));// A SUPPRIMER NGL-2128
		instruments.add(createInstrument("bravows2-and-mastercycler4", "Bravo Workstation 2 / Mastercycler 4 (EP-Gradient)", null, false, null, CNG));// A SUPPRIMER NGL-2128
		
		return instruments;
	}
	
	// FDS 25/10/2018 separation
	private static List<Instrument> getInstrumentsBravoWsAndMasterNexus( List<Institute> CNG ) throws DAOException {
		List<Instrument> instruments=new ArrayList<>();
		
		instruments.add(createInstrument("bravows1-and-mastercycler5", "Bravo Workstation 1 / Mastercycler 5 (Nexus SX-1)", null, true, null, CNG));
		instruments.add(createInstrument("bravows1-and-mastercycler6", "Bravo Workstation 1 / Mastercycler 6 (Nexus SX-1)", null, true, null, CNG));// 26/10/2018 manquait
		
		//FDS 14/06/2019 NGL-2552: equipes DEV ne veulent pas tracer les mastercyclers => il y a maintenant un nouveau type d'instrument
		//instruments.add(createInstrument("bravows2-and-mastercycler7", "Bravo Workstation 2 / Mastercycler 7 (Nexus SX-1)", null, true, null, CNG)); // A SUPPRIMER, reprise oui
		//instruments.add(createInstrument("bravows2-and-mastercycler8", "Bravo Workstation 2 / Mastercycler 8 (Nexus SX-1)", null, true, null, CNG)); // A SUPPRIMER, reprise non
		
		return instruments;
	}
	
	// FDS 22/01/2019 creation NGL-2389
	private static List<Instrument> getInstrumentsTecanEvo150AndLightCycler( List<Institute> CNG ) throws DAOException {
		List<Instrument> instruments=new ArrayList<>();
		
		instruments.add(createInstrument("tecan-roederer2-and-lightCycler1", "Tecan-Roederer2 / LightCycler 1", null, true, null, CNG));
		instruments.add(createInstrument("tecan-roederer2-and-lightCycler2", "Tecan-Roederer2 / LightCycler 2", null, true, null, CNG));
		instruments.add(createInstrument("tecan-roederer2-and-lightCycler3", "Tecan-Roederer2 / LightCycler 3", null, true, null, CNG));
		
		return instruments;
	}
	
	// FDS 28/01/2019 creation NGL-2368/2389
	private static List<Instrument> getInstrumentsBravoWsAndLightCycler( List<Institute> CNG ) throws DAOException {
		List<Instrument> instruments=new ArrayList<>();
		
		instruments.add(createInstrument("bravows1-and-lightCycler1", "Bravo Workstation 1 / LightCycler 1", null, true, null, CNG));
		instruments.add(createInstrument("bravows1-and-lightCycler2", "Bravo Workstation 1 / LightCycler 2", null, true, null, CNG));
		instruments.add(createInstrument("bravows1-and-lightCycler3", "Bravo Workstation 1 / LightCycler 3", null, true, null, CNG));
		
		instruments.add(createInstrument("bravows3-and-lightCycler1", "Bravo Workstation 3 / LightCycler 1", null, true, null, CNG));
		instruments.add(createInstrument("bravows3-and-lightCycler2", "Bravo Workstation 3 / LightCycler 2", null, true, null, CNG));
		instruments.add(createInstrument("bravows3-and-lightCycler3", "Bravo Workstation 3 / LightCycler 3", null, true, null, CNG));
		
		instruments.add(createInstrument("bravows4-and-lightCycler1", "Bravo Workstation 4 / LightCycler 1", null, true, null, CNG));
		instruments.add(createInstrument("bravows4-and-lightCycler2", "Bravo Workstation 4 / LightCycler 2", null, true, null, CNG));
		instruments.add(createInstrument("bravows4-and-lightCycler3", "Bravo Workstation 4 / LightCycler 3", null, true, null, CNG));
		
		return instruments;
	}
}

