package services.description.experiment;

import static services.description.DescriptionFactory.newExperimentType;
import static services.description.DescriptionFactory.newExperimentTypeNode;
import static services.description.DescriptionFactory.newPropertiesDefinition;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import java.util.Collections; // TEST FDS

import models.laboratory.common.description.Institute;
import models.laboratory.common.description.Level;
import models.laboratory.common.description.MeasureCategory;
import models.laboratory.common.description.MeasureUnit;
import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.common.description.Value;
import models.laboratory.common.description.dao.MeasureCategoryDAO;
import models.laboratory.common.description.dao.MeasureUnitDAO;
import models.laboratory.experiment.description.ExperimentCategory;
import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.experiment.description.ProtocolCategory;
import models.laboratory.experiment.description.dao.ExperimentCategoryDAO;
import models.laboratory.processes.description.ExperimentTypeNode;
import models.utils.dao.DAOException;
import models.utils.dao.DAOHelpers;
import play.data.validation.ValidationError;
import services.description.Constants;
import services.description.DescriptionFactory;
import services.description.common.LevelService;
import services.description.common.MeasureService;
import services.description.declaration.cng.Nanopore;

public class ExperimentServiceCNG extends AbstractExperimentService {
	
	@Override
	public void saveProtocolCategories(Map<String, List<ValidationError>> errors) throws DAOException {
		List<ProtocolCategory> l = new ArrayList<>();
		
		l.add(new ProtocolCategory("development", "Developpement"));
		l.add(new ProtocolCategory("production",  "Production"));
		
		DAOHelpers.saveModels(ProtocolCategory.class, l, errors);
	}
	
	/**
	 * Save all Experiment Categories.
	 * @param errors        error manager
	 * @throws DAOException DAO problem
	 */
	@Override
	public  void saveExperimentCategories(Map<String,List<ValidationError>> errors) throws DAOException{
		List<ExperimentCategory> l = new ArrayList<>();
		
		l.add(new ExperimentCategory(ExperimentCategory.CODE.purification  , "Purification"));
		l.add(new ExperimentCategory(ExperimentCategory.CODE.qualitycontrol, "Control qualité"));
		l.add(new ExperimentCategory(ExperimentCategory.CODE.transfert     , "Transfert"));
		l.add(new ExperimentCategory(ExperimentCategory.CODE.transformation, "Transformation"));
		l.add(new ExperimentCategory(ExperimentCategory.CODE.voidprocess   , "Void process"));
		
		DAOHelpers.saveModels(ExperimentCategory.class, l, errors);
	}

	/**
	 * Save all Experiment Types. 
	 * @param errors        error manager
	 * @throws DAOException DAO problem
	 */
	@Override
	public void saveExperimentTypes(Map<String, List<ValidationError>> errors) throws DAOException {
		List<ExperimentType> l = new ArrayList<>();
		List<Institute> CNG = DescriptionFactory.getInstitutes(Constants.CODE.CNG); // 04/04/2018; puisque tout se refere uniqt au CNG, alleger l'ecriture...
		
		ExperimentCategoryDAO ecfind = ExperimentCategory.find.get();
		
		/** voidprocess: ext-to-**  display order -1 **/
		
		l.add(newExperimentType("Ext to prepa flowcell","ext-to-prepa-flowcell",null,-1,
				ecfind.findByCode(ExperimentCategory.CODE.voidprocess.name()), 
				null,
				null,
				"OneToOne",
				CNG));
		
		l.add(newExperimentType("Ext to prepa flowcell ordered","ext-to-prepa-fc-ordered",null,-1,
				ecfind.findByCode(ExperimentCategory.CODE.voidprocess.name()), 
				null,
				null,
				"OneToOne",
				CNG));
		
		l.add(newExperimentType("Ext to librairie dénaturée","ext-to-denat-dil-lib",null,-1,
				ecfind.findByCode(ExperimentCategory.CODE.voidprocess.name()), 
				null,
				null,
				"OneToOne",
				CNG));
		
		l.add(newExperimentType("Ext to X5_WG PCR free","ext-to-x5-wg-pcr-free",null,-1,
				ecfind.findByCode(ExperimentCategory.CODE.voidprocess.name()),
				null,
				null,
				"OneToOne",
				CNG));
		
		l.add(newExperimentType("Ext to X5_norm,FC ord, dépôt","ext-to-norm-fc-ordered-depot",null,-1,
				ecfind.findByCode(ExperimentCategory.CODE.voidprocess.name()),
				null, 
				null ,
				"OneToOne", 
				CNG));
		
		//FDS 10/08/2016 ajout -- JIRA NGL-1047: processus X5_WG NANO;
		l.add(newExperimentType("Ext to X5_WG NANO","ext-to-x5-wg-nano",null,-1,
				ecfind.findByCode(ExperimentCategory.CODE.voidprocess.name()),
				null,
				null,
				"OneToOne",
				CNG));
		
		//FDS 12/12/2016 ajout -- JIRA NGL-1025: processus et experiments pour RNASeq ; JIRA NGL-1259 renommage rna-sequencing=> rna-lib-process
		l.add(newExperimentType("Ext to Prep lib RNASeq","ext-to-rna-lib-process",null,-1,
				ecfind.findByCode(ExperimentCategory.CODE.voidprocess.name()),
				null,
				null,
				"OneToOne",
				CNG));
		
		//FDS ajout 12/12/2016 JIRA NGL-1025: nouveau processus court pour RNAseq
		l.add(newExperimentType("Ext to norm+pool, dénat, FC, dépôt","ext-to-norm-and-pool-denat-fc-depot",null,-1,
				ecfind.findByCode(ExperimentCategory.CODE.voidprocess.name()),
				null,
				null,
				"OneToOne",
				CNG));	
		
		//FDS 12/12/2016 JIRA NGL-1164 : pour processus sans transformation
		l.add(newExperimentType("Ext to QC / TF / purif","ext-to-qc-transfert-purif",null,-1,
				ecfind.findByCode(ExperimentCategory.CODE.voidprocess.name()),
				null,
				null,
				"OneToOne",
				CNG));
		
		//FDS 10/10/2017 JIRA NGL-1625 dedoubler
		l.add(newExperimentType("Ext to  TF / QC / purif","ext-to-transfert-qc-purif",null,-1,
				ecfind.findByCode(ExperimentCategory.CODE.voidprocess.name()),
				null,
				null,
				"OneToOne",
				CNG));
			
			//FDS ajout 21/02/2017 NGL-1167: processus Chromium
			l.add(newExperimentType("Ext to Prep Chromium WG","ext-to-wg-chromium-lib-process",null,-1,
					ecfind.findByCode(ExperimentCategory.CODE.voidprocess.name()),
					null,
					null,
					"OneToOne",
					CNG));	

			//FDS ajout 10/07/2017 NGL 1201: processus Capture principal (4000/X5 = FC ordonnée)
			l.add(newExperimentType("Ext to Prep Capture","ext-to-capture-prep-process-fc-ord",null,-1,
					ecfind.findByCode(ExperimentCategory.CODE.voidprocess.name()),
					null,
					null,
					"OneToOne",
					CNG));
			
			//FDS ajout 10/07/2017 NGL-1201: processus Capture principal (2000/2500/Miseq/NextSeq)
			l.add(newExperimentType("Ext to Prep Capture","ext-to-capture-prep-process-fc",null,-1,
					ecfind.findByCode(ExperimentCategory.CODE.voidprocess.name()),
					null,
					null,
					"OneToOne",
					CNG));		
			
			//FDS ajout 06/07/2017 NGL 1201: processus Capture reprise (1)(4000/X5 = FC ordonnée)
			l.add(newExperimentType("Ext to Prep. Capture à partir sample prep sauvgarde","ext-to-pcr-capture-pcr-indexing-fc-ord",null,-1,
					ecfind.findByCode(ExperimentCategory.CODE.voidprocess.name()),
					null,
					null,
					"OneToOne",
					CNG));
			
			//FDS ajout 06/07/2017 NGL-1201: processus Capture reprise (1)(2000/2500/Miseq/NextSeq)
			l.add(newExperimentType("Ext to Prep. Capture à partir sample prep sauvgarde","ext-to-pcr-capture-pcr-indexing-fc",null,-1,
					ecfind.findByCode(ExperimentCategory.CODE.voidprocess.name()),
					null,
					null,
					"OneToOne",
					CNG));
			
			//FDS ajout 06/07/2017 NGL 1201: processus Capture reprise (2)(4000/X5 = FC ordonnée)
			l.add(newExperimentType("Ext to Prep. Capture à partir pré Capture","ext-to-capture-pcr-indexing-fc-ord",null,-1,
					ecfind.findByCode(ExperimentCategory.CODE.voidprocess.name()),
					null,
					null,
					"OneToOne",
					CNG));
			
			//FDS ajout 06/07/2017 NGL-1201: processus Capture reprise (2)(2000/2500/Miseq/NextSeq)
			l.add(newExperimentType("Ext to Prep. Capture à partir pré Capture","ext-to-capture-pcr-indexing-fc",null,-1,
					ecfind.findByCode(ExperimentCategory.CODE.voidprocess.name()),
					null,
					null,
					"OneToOne",
					CNG));
			
			//FDS ajout 06/07/2017 NG-1201: processus Capture reprise (3)(4000/X5 = FC ordonnée)
			l.add(newExperimentType("Ext to PCR indexing à partir capture sauvegarde","ext-to-pcr-indexing-process-fc-ord",null,-1,
					ecfind.findByCode(ExperimentCategory.CODE.voidprocess.name()),
					null,
					null,
					"OneToOne",
					CNG));
			
			//FDS ajout 06/07/2017 NGL-1201: processus Capture reprise (3)(2000/2500/Miseq/NextSeq)
			l.add(newExperimentType("Ext to PCR indexing à partir capture sauvegarde","ext-to-pcr-indexing-process-fc",null,-1,
					ecfind.findByCode(ExperimentCategory.CODE.voidprocess.name()),
					null,
					null,
					"OneToOne",
					CNG));	
			
			//FDS ajout 04/04/2018  NGL-1727: processus SmallRNASeq
			l.add(newExperimentType("Ext to Small RNASeq","ext-to-small-rna-seq-process",null,-1,
					ecfind.findByCode(ExperimentCategory.CODE.voidprocess.name()),
					null,
					null,
					"OneToOne",
					CNG));	
			
			//FDS ajout 04/04/2018  NGL-1727: processus BisSeq
			l.add(newExperimentType("Ext to BiSeq (FC ordonnée)","ext-to-bis-seq-process-fc-ord",null,-1,
					ecfind.findByCode(ExperimentCategory.CODE.voidprocess.name()),
					null,
					null,
					"OneToOne",
					CNG));
			
			//FDS ajout 04/06/2018  NGL-1728: processus OxBisSeq
			l.add(newExperimentType("Ext to (Ox)BiSeq (FC ordonnée)","ext-to-oxbisseq-and-bisseq-process-fc-ord",null,-1,
					ecfind.findByCode(ExperimentCategory.CODE.voidprocess.name()),
					null,
					null,
					"OneToMany", /// 26/09/2018   c'est un bug ??.. devrait etre One to One ???
					CNG));
			
			//FDS ajout 26/09/2018  NGL-2235 : process tansfert puis Denat....// 05/12/2018 suppression "2000"
			l.add(newExperimentType("Ex to Transfert puis Dénat, prep FC, dépôt (2500 / MiSeq / NextSeq)","ext-to-tf-illumina-run",null,-1,
					ecfind.findByCode(ExperimentCategory.CODE.voidprocess.name()),
					null,
					null,
					"OneToOne",
					CNG));
			
			//FDS ajout 12/09/2018  NGL-1226: processus RNAseqFrag
			l.add(newExperimentType("Ext to RNAseq (DEV) frag, cDNA, indexing","ext-to-rna-seq-frg-cdna-indexing-process",null,-1,
					ecfind.findByCode(ExperimentCategory.CODE.voidprocess.name()),
					null,
					null,
					"OneToOne",
					CNG));
			
			//FDS ajout 12/09/2018  NGL-1226: processus RNAseqFull
			l.add(newExperimentType("Ext to RNAseq (DEV) full length cDNA, frag indexing","ext-to-rna-seq-flcdna-frg-indexing-process",null,-1,
					ecfind.findByCode(ExperimentCategory.CODE.voidprocess.name()),
					null,
					null,
					"OneToOne",
					CNG));
			
			//FDS ajout 14/12/2018  NGL-1226: processus reprise
			l.add(newExperimentType("Ext to RNAseq (DEV) Prep Lib Nextera à partir de synthèse cDNA (reprise)","ext-to-rnaseq-frg-indexing-process-from-cdna",null,-1,
					ecfind.findByCode(ExperimentCategory.CODE.voidprocess.name()),
					null,
					null,
					"OneToOne",
					CNG));
			
			//FDS ajout 30/04/2019  NGL-1725 processus ATAC-Seq
			l.add(newExperimentType("Ext to ATAC-Seq","ext-to-atac-seq-process",null,-1,
					ecfind.findByCode(ExperimentCategory.CODE.voidprocess.name()),
					null,
					null,
					"OneToOne",
					CNG));
			
			/** Transformation, ordered by display order **/
			
			//FDS 01/02/2016 ajout -- JIRA NGL-894: experiments pour X5
			l.add(newExperimentType("Prep. PCR free","prep-pcr-free",null,500,
					ecfind.findByCode(ExperimentCategory.CODE.transformation.name()),
					getPropertyDefinitionsPrepPcrFree(), 
					getInstrumentUsedTypes("covaris-e210-and-sciclone-ngsx", //FDS 18/07/2017 plus utilisé (inactivéé dans instrumentService...)
							               "covaris-le220-and-sciclone-ngsx",
							               "covaris-e220-and-sciclone-ngsx",
							               "covaris-le220-and-bravows",      // 17/09/2018  NGL-2230 ajout
							               "covaris-e220-and-bravows"),      // 17/09/2018  NGL-2230 ajout
					"OneToOne",
					CNG));
			
			//FDS dupliquer experience prep-pcr-free en prep-wg-nano; separer les proprietes de celles de prep-pcr-free ...
			l.add(newExperimentType("Prep. WG Nano","prep-wg-nano",null,550,
					ecfind.findByCode(ExperimentCategory.CODE.transformation.name()),
					getPropertyDefinitionsPrepWgNano(),
					getInstrumentUsedTypes("covaris-le220-and-sciclone-ngsx",
						                   "covaris-e220-and-sciclone-ngsx",
						                   "covaris-le220-and-bravows",          // 18/06/2018 NGL-2125 ajout
						                   "covaris-e220-and-bravows"),          // 18/06/2018 NGL-2125 ajout
					"OneToOne",
					CNG));
			
			//FDS 12/12/2016 ajout -- JIRA NGL-1025: processus et experiments pour RNASeq; JIRA NGL-1047: processus X5_WG NANO 	
			l.add(newExperimentType("Prep. Librairie (sans frg)","library-prep",null,600,
					ecfind.findByCode(ExperimentCategory.CODE.transformation.name()),
					getPropertyDefinitionsLibraryPrep(),
					getInstrumentUsedTypes("sciclone-ngsx",
										   "bravo-workstation", // 03/07/2019 NGL-2602 ajout
							               "hand"),             // 29/11/2017 NGL-1717 ajout
					"OneToOne",
					CNG));
			
			//FDS 06/04/2018 ajout JIRA NGL-1727: pour processus SmallRNASeq
			l.add(newExperimentType("Small RNAseq lib prep","small-rnaseq-lib-prep",null,730,
					ecfind.findByCode(ExperimentCategory.CODE.transformation.name()),
					getPropertyDefinitionsSmallRNASeqLibPrep(),
					getInstrumentUsedTypes("sciclone-ngsx-and-zephyr",
											"tecan-evo-150-and-zephyr",
											"tecan-evo-150-and-tecan-evo-150", // 19/04/2019 NGL-2518 ajout
											"tecan-evo-150",                   // 19/04/2019 NGL-2518 ajout
											"hand"), 
					"OneToOne",
					CNG));
			
			//FDS 04/04/2018 ajout JIRA NGL-1996: pour processus BisSeq
			l.add(newExperimentType("BisSeq lib prep","bisseq-lib-prep",null,740,
					ecfind.findByCode(ExperimentCategory.CODE.transformation.name()),
					getPropertyDefinitionsBisSeqLibPrep(),
					getInstrumentUsedTypes("sciclone-ngsx-and-zephyr",
							               "tecan-evo-150-and-zephyr",
							               "hand"),
					"OneToOne",
					CNG));
			
			//FDS 04/06/2018 ajout JIRA NGL-1728: pour processus OxBisSeq. 
			//    23/10/2018 !!!! seul l' instrument "hand" est déclaré=> probleme si jamais import workbook est demandé (voir "bisseq-lib-prep-ctrl.js")
			l.add(newExperimentType("(Ox)BisSeq lib prep","oxbisseq-and-bisseq-lib-prep",null,750,
					ecfind.findByCode(ExperimentCategory.CODE.transformation.name()),
					getPropertyDefinitionsOxBisSeqLibPrep(),
					getInstrumentUsedTypes("hand"),
					"OneToMany",
					CNG));
			
			l.add(newExperimentType("PCR+purification","pcr-and-purification",null,700,
					ecfind.findByCode(ExperimentCategory.CODE.transformation.name()),
					getPropertyDefinitionsPcrAndPurification(),
					getInstrumentUsedTypes("mastercycler-epg-and-zephyr",
							               "mastercycler-epg-and-bravows",
							               //"mastercycler-ep-gradient",        supression 20/06/2019 NGL-2552
							               "mastercycler-nexus-and-bravows"), // 22/02/2018 NGL-1860 ajout; 
					"OneToOne",
					CNG));
			
			l.add(newExperimentType("Normalisation+Pooling","normalization-and-pooling",null,800,
					ecfind.findByCode(ExperimentCategory.CODE.transformation.name()),
					getPropertyDefinitionsNormalizationAndPooling(), 
					getInstrumentUsedTypes("hand",
							               "janus",
							               "epmotion",
							               "tecan-evo-150" ), //FDS 21/01/2019 NGL-2381 ajout tecan-evo-150
					"ManyToOne",
					CNG));
			
			l.add(newExperimentType("Librairie normalisée","lib-normalization",null,900,
					ecfind.findByCode(ExperimentCategory.CODE.transformation.name()), 
					getPropertyDefinitionsLibNormalization(),
					getInstrumentUsedTypes("hand",
							               "janus"),
					"OneToOne",
					CNG));
			
			// 04/10/2017 NGL-1589: plaque->plaque, tubes->plaque, plaque-> tube, tube->tube => utiliser robot
			l.add(newExperimentType("Dénaturation-dilution","denat-dil-lib",null,1000,
					ecfind.findByCode(ExperimentCategory.CODE.transformation.name()), 
					getPropertyDefinitionsDenatDilLibCNG(),
					getInstrumentUsedTypes("hand",
							               "epmotion"), // 16/10/2017 remplacer janus par EpMotion
					"OneToOne",
					CNG));
			
			l.add(newExperimentType("Préparation flowcell","prepa-flowcell",null,1200,
					ecfind.findByCode(ExperimentCategory.CODE.transformation.name()), 
					getPropertyDefinitionsPrepaflowcellCNG(),
					getInstrumentUsedTypes("cBot",   //NGL-2583 remettre le type sinon on ne peut plus faire de recherche sur experiences anciennes!!!
										   "cBotV2",
							               "cBot-onboard"),
					"ManyToOne",
					CNG));
			
			//FDS modif 23/01/2017 modif janus-and-cBot=>  janus-and-cBotV2, il n'y a plus de Cbot non V2...
			//FDS 07/12/2017  NGL-1730 ajout "cBot-onboard" pour NovaSeq6000; 04/09/2018 NGL-2219: ajout novaseq-xp-fc-dock
			l.add(newExperimentType("Prép. flowcell ordonnée","prepa-fc-ordered",null,1300,
					ecfind.findByCode(ExperimentCategory.CODE.transformation.name()), 
					getPropertyDefinitionsPrepaflowcellOrderedCNG(),
					getInstrumentUsedTypes("cBot",   //NGL-2583 remettre le type sinon on ne peut plus faire de recherche sur experiences anciennes!!!
										   "cBotV2",
										   "janus-and-cBot", //NGL-2583 remettre le type sinon on ne peut plus faire de recherche sur experiences anciennes!!!
							               "janus-and-cBotV2",
							               "cBot-onboard",
							               "novaseq-xp-fc-dock"),
					"ManyToOne", 
					CNG));
			
			//FDS 28/10/2015 : ajout "HISEQ4000","HISEQX"
			//FDS 07/12/2017 NGL-1730: ajout NOVASEQ6000
			l.add(newExperimentType("Dépôt sur séquenceur","illumina-depot",null, 1400,
					ecfind.findByCode(ExperimentCategory.CODE.transformation.name()),
					getPropertyDefinitionsIlluminaDepot(),
					getInstrumentUsedTypes("MISEQ",
							               "HISEQ2000",
							               "HISEQ2500",
							               "NEXTSEQ500",
							               "HISEQ4000",
							               "HISEQX",
							               "NOVASEQ6000"),
					"OneToVoid", 
					CNG));			
			
			//FDS ajout 21/02/2017 NGL-1167: Chromium		
			l.add(newExperimentType("GEM generation (Chromium)","chromium-gem-generation",null,1500,
					ecfind.findByCode(ExperimentCategory.CODE.transformation.name()), 
					getPropertyDefinitionsChromiumGemGeneration(),
					getInstrumentUsedTypes("chromium-controller"),
					"OneToOne",
					CNG));
			
			l.add(newExperimentType("Prep Lib & PCR indexing (Chromium)","wg-chromium-lib-prep",null,1600,
					ecfind.findByCode(ExperimentCategory.CODE.transformation.name()),
					getPropertyDefinitionsWGChromiumLibPrep(),
					getInstrumentUsedTypes("hand"),	  // 13/03/2017 ne pas encore proposer "sciclone-ngsx"
					"OneToOne",
					CNG));
			
			//FDS 10/07/2017 NGL-1201: experiences transformation pour Capture
			l.add(newExperimentType("Fragmentation","fragmentation",null,650,
					ecfind.findByCode(ExperimentCategory.CODE.transformation.name()), 
					getPropertyDefinitionsFragmentation(),
					getInstrumentUsedTypes("covaris-e220-and-sciclone-ngsx","covaris-le220-and-sciclone-ngsx",
							               "covaris-e220",                  "covaris-le220",                     // ajoutés 29/08/2017
							               "covaris-e220-and-bravows",      "covaris-le220-and-bravows"),        // ajoutés 16/11/2017
					"OneToOne",
					CNG));
			
			//FDS 10/07/2017 NGL-1201: experiences transformation pour Capture (Sure Select implicite)
			l.add(newExperimentType("Sample prep (pré-capture)","sample-prep",null,660,
					ecfind.findByCode(ExperimentCategory.CODE.transformation.name()),
					getPropertyDefinitionsSamplePrepCapture(),
					getInstrumentUsedTypes("sciclone-ngsx",
							               "bravo-workstation"),    // 09/11/2017 NGL-1691: ajout
					"OneToMany",
					CNG));
			
			//FDS 10/07/2017 NGL-1201: experiences transformation pour Capture (Sure Select implicite)
			l.add(newExperimentType("Hybridation, capture & wash (post)","capture",null,710,
					ecfind.findByCode(ExperimentCategory.CODE.transformation.name()),
					getPropertyDefinitionsCapture(),
					getInstrumentUsedTypes("bravo-workstation",
							               "bravows-and-mastercycler-epg",    // 15/11/2017 ajout "bravows-and-mastercycler-epg"
										   "bravows-and-mastercycler-nexus"), // 22/02/2018 NGL-1860: ajout
					"OneToOne",
					CNG));
			
			//FDS 10/07/2017 NGL-1201: experiences transformation pour Capture (Sure Select implicite)
			l.add(newExperimentType("PCR+indexing (post-capture)","pcr-and-indexing",null,720,
					ecfind.findByCode(ExperimentCategory.CODE.transformation.name()),
					getPropertyDefinitionsPcrIndexing(),
					getInstrumentUsedTypes("mastercycler-nexus-and-bravows",
							               "mastercycler-epg-and-bravows"),  // 22/02/2018 NGL-1860 supression, 06/2019 remis car il existe du passif a requeter
					"OneToOne",
					CNG));
			
			//FDS 12/09/2018 NGL-1226: RNAseq (DEV) Frag, cDNA, indexing   pour processus RNAseqFrag
			l.add(newExperimentType("RNAseq (DEV) Frag, cDNA, indexing","frg-cdna-indexing",null,722,
					ecfind.findByCode(ExperimentCategory.CODE.transformation.name()),
					getPropertyDefinitionsFrgCdnaIndexing(),
					getInstrumentUsedTypes("hand"),
					"OneToOne",
					CNG));	
			
			//FDS 12/09/2018 NGL-1226: RNAseq (DEV) ampli/PCR              pour processus RNAseqFrag
			//	  13/06/2019 NGL-2552: remplacer "hand" par mastercycler de dev
			l.add(newExperimentType("RNAseq (DEV) ampli/PCR","dev-pcr-amplification",null,724,
					ecfind.findByCode(ExperimentCategory.CODE.transformation.name()),
					getPropertyDefinitionsDevPcrAmpli(),
					getInstrumentUsedTypes("mastercycler-ep-gradient-dev",
										   "mastercycler-nexus-dev",
										   "mastercycler-pro"),        // 03/09/2019 NGL-2637: ajouter Mastercycler Pro/ProS
					"OneToOne",
					CNG));
			
			//FDS 12/09/2018 NGL-1226: RNAseq (DEV) Prep lib Nextera       pour processus RNAseqFull
			//	  13/06/2019 NGL-2552: remplacer "hand" par mastercycler de dev
			l.add(newExperimentType("RNAseq (DEV) Prep lib Nextera","lib-and-pcr",null,726,
					ecfind.findByCode(ExperimentCategory.CODE.transformation.name()),
					getPropertyDefinitionsLibAndPcr(),
					getInstrumentUsedTypes("mastercycler-ep-gradient-dev",
										   "mastercycler-nexus-dev"),
					"OneToOne",
					CNG));
			
			//FDS 30/04/2019 NGL-1725: permeabilization-transposition-purification       pour processus ATAC-Seq
			l.add(newExperimentType("Perméabilisation, Transposition, Purification","permeabilization-transposition-purification",null,728,
					ecfind.findByCode(ExperimentCategory.CODE.transformation.name()),
					getPropertyDefinitionsPermTranspPurif(),
					getInstrumentUsedTypes("hand"),
					"OneToOne",
					getSampleTypes("DNA"),true, // type de samples produit en sortie par l'experience (attention code)
					CNG));
			
			//FDS 30/04/2019 NGL-1725: pcr-amplif-and-purif-atac-chip-seq       pour processus ATAC-Seq; NGL-2607 chgt nom et code
			l.add(newExperimentType("Amplification PCR + purification [ATAC/Chip Seq]","pcr-amplif-and-purif-atac-chip-seq",null,730,
					ecfind.findByCode(ExperimentCategory.CODE.transformation.name()),
					getPropertyDefinitionsPCRAmpliPurifAtacChip(),
					getInstrumentUsedTypes("mastercycler-pro"),
					"OneToOne",
					CNG));
			
			
			/** Quality Control, ordered by display order **/
			//NOTE: pas de Node a creer pour experiences type qualitycontrol
			//17/10/2017: sauf si existe  un processus commencant par exp type qualitycontrol...
			
			// FDS 07/04/2016 ajout --JIRA NGL-894: experiments pour X5
			// 22/09/2016 modification du name => suppression 'profil' mais garder dans le code a cause existant ds base de données
			l.add(newExperimentType("LABCHIP_GX","labchip-migration-profile", null, 10,
					ecfind.findByCode(ExperimentCategory.CODE.qualitycontrol.name()),
					getPropertyDefinitionsChipMigration(),
					getInstrumentUsedTypes("labChipGX"),
					"OneToVoid",
					CNG));
					
			//FDS 01/02/2016 ajout -- JIRA NGL-894: experiments pour X5
			l.add(newExperimentType("Quantification qPCR","qpcr-quantification", null, 20,
					ecfind.findByCode(ExperimentCategory.CODE.qualitycontrol.name()),
					getPropertyDefinitionsQPCR(),
					getInstrumentUsedTypes("qpcr-lightcycler-480II",
										   "bravows-and-qpcr-lightcycler-480II",         // FDS 28/01/2019 NGL-2389 ajout
										   "tecan-evo-150-and-qpcr-lightcycler-480II"),  // FDS 23/01/2019 NGL-2389 ajout
					"OneToVoid",
					CNG));
			
			// FDS 07/05/2019 ajout -- JIRA NGL-1725: qc pour ATAC seq
			l.add(newExperimentType("qPCR (détermination nb cycles PCR)","qpcr-nb-cycle-setting", null, 30,
					ecfind.findByCode(ExperimentCategory.CODE.qualitycontrol.name()),
					getPropertyDefinitionsQPCRCycleSetting(),
					getInstrumentUsedTypes("qpcr-quantstudio",
				               			   "qpcr-abi-7900ht"),		
					"OneToVoid",
					CNG));
			
			l.add(newExperimentType("QC Miseq","miseq-qc", null, 30,
					ecfind.findByCode(ExperimentCategory.CODE.qualitycontrol.name()),
					getPropertyDefinitionsQCMiseq(),
					getInstrumentUsedTypes("MISEQ-QC-MODE"),
					"OneToVoid",
					CNG));
			
			//FDS 21/02/2017 ajout -- JIRA NGL-1167: experiments pour Chromium
			l.add(newExperimentType("Bioanalyzer","bioanalyzer-migration-profile", null, 40,
					ecfind.findByCode(ExperimentCategory.CODE.qualitycontrol.name()),
					getPropertyDefinitionsBioanalyzer(), 
					getInstrumentUsedTypes("agilent-2100-bioanalyzer"),
					"OneToVoid",
					CNG));
			
			//GA pour import; non affichée (=> displayOrder=null)
			l.add(newExperimentType("QC Bank","bank-qc", null, null,
					ecfind.findByCode(ExperimentCategory.CODE.qualitycontrol.name()), 
					getPropertyDefinitionsBankQC(), 
					getInstrumentUsedTypes("hand"),
					"OneToVoid", false,
					CNG));	
			
			// FDS 21/06/2017 ajout -- JIRA NGL-1472: necessiter d'ajouter QC provenant de collaborateur extérieur; non listée (=> displayOrder=null)
			l.add(newExperimentType("QC Exterieur","external-qc", null, null,
					ecfind.findByCode(ExperimentCategory.CODE.qualitycontrol.name()),
					getPropertyDefinitionsExternalQC(),
					getInstrumentUsedTypes("hand"),
					"OneToVoid", false,
					CNG));
		
			//FDS 27/07/2017 ajout NGL-1201: qc pour process Capture
			l.add(newExperimentType("Dosage Fluo","fluo-quantification", null, 50,
					ecfind.findByCode(ExperimentCategory.CODE.qualitycontrol.name()),
					getPropertyDefinitionsQuantIt(), 
					getInstrumentUsedTypes("spectramax",
							               "qubit"),  // NGL-1720: ajout
					"OneToVoid",
					CNG));
			
			/** Purification, ordered by display order **/
						/*vide*/
			
			/** Transfert, ordered by display order **/
			// NOTE: pas de Node a creer pour experiences type transfert
			// 17/10/2017: sauf si existe un processus commencant par experiences type transfert...
			
			l.add(newExperimentType("Aliquot","aliquoting",null, 10300,
					ecfind.findByCode(ExperimentCategory.CODE.transfert.name()),
					getPropertyAliquoting(),
					getInstrumentUsedTypes("hand"),
					"OneToMany",
					CNG));
			
			// FDS 10/08/2016 NGL-1029;  05/10/2016 ajout EpMotion; 26/10/2016 renommage en "Pool"
			l.add(newExperimentType("Pool","pool",null,10400,
					ecfind.findByCode(ExperimentCategory.CODE.transfert.name()),
					getPropertyDefinitionPool(),
					getInstrumentUsedTypes("hand",
							               "janus",
							               "epmotion",
							               "tecan-evo-150"), //FDS 21/01/2019 NGL-2381 ajout 
					"ManyToOne",
					CNG));
			
			// FDS ajout 19/07/2017 NGL-1519: dupliquer "lib-normalization" en experience de type transfert=> meme proprietes
			// FDS 02/08/2019 NGL-2635 chgt label
			l.add(newExperimentType("Normalisation (satellite)","additional-normalization",null,10500,
					ecfind.findByCode(ExperimentCategory.CODE.transfert.name()),
					getPropertyDefinitionsLibNormalization(),
					getInstrumentUsedTypes("hand",
							               "janus",
							               "tecan-evo-150"), //FDS 21/01/2019 NGL-2381 ajout 
					"OneToOne",
					CNG));
			
			// FDS 27/03/2017 renommage "Tubes" en "Tubes ou Strips"
			l.add(newExperimentType("Tubes ou Strips -> Plaque","tubes-to-plate",null,10600,
					ecfind.findByCode(ExperimentCategory.CODE.transfert.name()), null,
					getInstrumentUsedTypes("hand"),
					"OneToOne",
					CNG));
			
			l.add(newExperimentType("Plaque -> Tubes","plate-to-tubes",null,10700,
					ecfind.findByCode(ExperimentCategory.CODE.transfert.name()), null,
					getInstrumentUsedTypes("hand"),
					"OneToOne",
					CNG));
			
			l.add(newExperimentType("Plaques -> Plaque","plates-to-plate",null,10800,
					ecfind.findByCode(ExperimentCategory.CODE.transfert.name()), null,
					getInstrumentUsedTypes("hand",
							               "janus",          //FDS 14/06/2018 NGL-2115 ajout 
							               "tecan-evo-150"), //FDS 21/01/2019 NGL-2381 ajout 
					"OneToOne",
					CNG));
			
			// FDS renommage "Tubes + Plaques"
			l.add(newExperimentType("Tubes + Plaques -> Plaque","x-to-plate",null,10900,
					ecfind.findByCode(ExperimentCategory.CODE.transfert.name()), null,
					getInstrumentUsedTypes("hand"),
					"OneToOne",
					CNG));

			
			/** NOTE: toutes les experiences nanopores sont regroupées dans la classe Nanopore.java **/

			l.addAll(new Nanopore().getExperimentType());
			
			
		DAOHelpers.saveModels(ExperimentType.class, l, errors);
	}


	/**
	 * Save all Experiment TypeNodes.
	 * @param errors        error manager
	 * @throws DAOException DAO problem
	 */
	@Override
	public void saveExperimentTypeNodes(Map<String, List<ValidationError>> errors) throws DAOException {
		//NOTE FDS: les nodes qui apparaissent en previous doivent etre crees avant sinon==>message : experimentTypeNode is mandatory
		
		/**  ext-to nodes **/
		
		save(newExperimentTypeNode("ext-to-prepa-flowcell", getExperimentTypes("ext-to-prepa-flowcell").get(0),
				false, false, false,
				null, // no previous nodes
				null,
				null,
				null
				));
		
		save(newExperimentTypeNode("ext-to-prepa-fc-ordered", getExperimentTypes("ext-to-prepa-fc-ordered").get(0),
				false, false, false, 
				null, // no previous nodes
				null,
				null,
				null
				));
		
		save(newExperimentTypeNode("ext-to-denat-dil-lib", getExperimentTypes("ext-to-denat-dil-lib").get(0),
				false, false, false,
				null, // no previous nodes
				null, 
				null,
				null
				));
		
		//FDS ajout 01/02/2016 -- JIRA NGL-894 : processus et experiments pour X5
		save(newExperimentTypeNode("ext-to-x5-wg-pcr-free",getExperimentTypes("ext-to-x5-wg-pcr-free").get(0),
				false,false,false,
				null, // no previous nodes
				null,
				null,
				null
				));
		
		//FDS ajout 15/04/2016 -- JIRA NGL-894 : processus court pour X5
		save(newExperimentTypeNode("ext-to-norm-fc-ordered-depot",getExperimentTypes("ext-to-norm-fc-ordered-depot").get(0),
				false,false,false,
				null, // no previous nodes
				null,
				null,
				null
				));	
		
		// FDS 10/08/2016 JIRA NGL-1047: X5_WG NANO; mise en prod 01/09/2016
		save(newExperimentTypeNode("ext-to-x5-wg-nano",getExperimentTypes("ext-to-x5-wg-nano").get(0),
				false,false,false,
				null, // no previous nodes
				null,
				null,
				null
				));
		
		// FDS  ajout 06/10/2017
		save(newExperimentTypeNode("ext-to-norm-and-pool-denat-fc-depot",getExperimentTypes("ext-to-norm-and-pool-denat-fc-depot").get(0),
				false,false,false,
				null, // no previous nodes
				null,
				null,
				null
				));
		
		//FDS ajout 12/12/2016 -- JIRA NGL-1025 RNA_Seq; processus court
		save(newExperimentTypeNode("ext-to-rna-lib-process",getExperimentTypes("ext-to-rna-lib-process").get(0),
				false,false,false,
				null, // no previous nodes
				null,
				null,
				null
				));
		
		//FDS ajout 12/12/2016 JIRA NGL-1164
		save(newExperimentTypeNode("ext-to-qc-transfert-purif", getExperimentTypes("ext-to-qc-transfert-purif").get(0),
				false, false, false,
				null, // no previous nodes
				null,
				null,
				null
				));
		
		// FDS ajout 10/10/2017 JIRA NGL-1625
		save(newExperimentTypeNode("ext-to-transfert-qc-purif", getExperimentTypes("ext-to-transfert-qc-purif").get(0),
				false, false, false,
				null, // no previous nodes
				null,
				null,
				null
				));
		
			
		//FDS ajout 20/02/2017 JIRA NGL-1167
		save(newExperimentTypeNode("ext-to-wg-chromium-lib-process", getExperimentTypes("ext-to-wg-chromium-lib-process").get(0),
				false, false, false,
				null, // no previous nodes
				null,
				null,
				null
				));
			
		//FDS ajout 10/07/2017 NGL-1201: processus capture
		save(newExperimentTypeNode("ext-to-capture-prep-process-fc-ord", getExperimentTypes("ext-to-capture-prep-process-fc-ord").get(0),
				false, false, false,
				null, // no previous nodes
				null,
				null,
				null
				));
		
		//FDS ajout 10/07/2017 NGL-1201: processus capture
		save(newExperimentTypeNode("ext-to-capture-prep-process-fc", getExperimentTypes("ext-to-capture-prep-process-fc").get(0),
				false, false, false,
				null, // no previous nodes
				null,
				null,
				null
				));
		
		//FDS ajout 10/07/2017 NGL-1201: processus capture reprise (1)
		save(newExperimentTypeNode("ext-to-pcr-capture-pcr-indexing-fc-ord", getExperimentTypes("ext-to-pcr-capture-pcr-indexing-fc-ord").get(0),
				false, false, false,
				null, // no previous nodes
				null,
				null,
				null
				));
		
		//FDS ajout 10/07/2017 NGL-1201: processus capture reprise (1)
		save(newExperimentTypeNode("ext-to-pcr-capture-pcr-indexing-fc", getExperimentTypes("ext-to-pcr-capture-pcr-indexing-fc").get(0),
				false, false, false,
				null, // no previous nodes
				null,
				null,
				null
				));
			
		//FDS ajout 10/07/2017 NGL-1201: processus capture reprise (2)
		save(newExperimentTypeNode("ext-to-capture-pcr-indexing-fc-ord", getExperimentTypes("ext-to-capture-pcr-indexing-fc-ord").get(0),
				false, false, false,
				null, // no previous nodes
				null,
				null,
				null
				));
		
		//FDS ajout 10/07/2017 NGL-1201: processus capture reprise (2)
		save(newExperimentTypeNode("ext-to-capture-pcr-indexing-fc", getExperimentTypes("ext-to-capture-pcr-indexing-fc").get(0),
				false, false, false,
				null, // no previous nodes
				null,
				null,
				null
				));
		
		//FDS ajout 10/07/2017 NGL-1201: processus capture reprise (3)
		save(newExperimentTypeNode("ext-to-pcr-indexing-process-fc-ord", getExperimentTypes("ext-to-pcr-indexing-process-fc-ord").get(0),
				false, false, false,
				null, // no previous nodes
				null,
				null,
				null
				));
		
		//FDS ajout 10/07/2017 NGL-1201: processus capture reprise (3)
		save(newExperimentTypeNode("ext-to-pcr-indexing-process-fc", getExperimentTypes("ext-to-pcr-indexing-process-fc").get(0),
				false, false, false,
				null, // no previous nodes
				null,
				null,
				null
				));
		
		//FDS ajout 04/04/2018 NGL-1727: processus SmallRNASeq
		save(newExperimentTypeNode("ext-to-small-rna-seq-process", getExperimentTypes("ext-to-small-rna-seq-process").get(0),
				false, false, false,
				null, // no previous nodes
				null,
				null,
				null
				));
				
		//FDS ajout 04/04/2018 NGL-1996: processus BisSeq
		save(newExperimentTypeNode("ext-to-bis-seq-process-fc-ord", getExperimentTypes("ext-to-bis-seq-process-fc-ord").get(0),
				false, false, false, 
				null, // no previous nodes
				null,
				null,
				null
				));
		
		//FDS ajout 04/06/2018 NGL-1728: processus OxBisSeq
		save(newExperimentTypeNode("ext-to-oxbisseq-and-bisseq-process-fc-ord", getExperimentTypes("ext-to-oxbisseq-and-bisseq-process-fc-ord").get(0),
				false, false, false,
				null, // no previous nodes
				null,
				null,
				null
				));
		
		//FDS ajout 26/09/2018 NGL-2235: processus Transfert Dénat...
		save(newExperimentTypeNode("ext-to-tf-illumina-run", getExperimentTypes("ext-to-tf-illumina-run").get(0),
				false, false, false,
				null, // no previous nodes
				null,
				null,
				null
				));
		
		//FDS ajout 12/09/2018 NGL-1226: processus RNAseqFrag
		save(newExperimentTypeNode("ext-to-rna-seq-frg-cdna-indexing-process", getExperimentTypes("ext-to-rna-seq-frg-cdna-indexing-process").get(0),
				false, false, false,
				null, // no previous nodes
				null,
				null,
				null
				));
		
		//FDS ajout 12/09/2018 NGL-1226: processus RNAseqFull
		save(newExperimentTypeNode("ext-to-rna-seq-flcdna-frg-indexing-process", getExperimentTypes("ext-to-rna-seq-flcdna-frg-indexing-process").get(0),
				false, false, false,
				null, // no previous nodes
				null,
				null,
				null
				));
		
		//FDS ajout 14/12/2018 NGL-1226: processus reprise
		save(newExperimentTypeNode("ext-to-rnaseq-frg-indexing-process-from-cdna", getExperimentTypes("ext-to-rnaseq-frg-indexing-process-from-cdna").get(0),
				false, false, false,
				null, // no previous nodes
				null,
				null,
				null
				));
		
		//FDS ajout 30/04/2019 NGL-1725: processus ATACseq
		save(newExperimentTypeNode("ext-to-atac-seq-process", getExperimentTypes("ext-to-atac-seq-process").get(0),
				false, false, false,
				null, // no previous nodes
				null,
				null,
				null
				));
				
		/** other nodes **/
		/** Note generale 19/11/2018: l'ajout d'une seule experience de QC ou Transfert (quelle qu'elle soit suffit pour faire afficher la possibilite 
		 * QC ou Tranfert au moment de l'orientation des containers apres une experience de transformation. 
		 * Il n'est pas utile de lister les differentes possibles, ni meme exact...
		 */
		
		save(newExperimentTypeNode("prep-pcr-free",getExperimentTypes("prep-pcr-free").get(0),
				false,false,false,
				getExperimentTypeNodes("ext-to-x5-wg-pcr-free"), // previous nodes
				null, // pas de purif
				getExperimentTypes("qpcr-quantification",
						           "labchip-migration-profile",
						           "miseq-qc"), // qc; est-ce necessaire de les lister ? ou un seul suffit ?
				getExperimentTypes("aliquoting")  // transfert
				));
		
		save(newExperimentTypeNode("prep-wg-nano",getExperimentTypes("prep-wg-nano").get(0),
				false,false,false,
				getExperimentTypeNodes("ext-to-x5-wg-nano"), // previous nodes
				null, // pas de purif
				null, // pas de qc
				getExperimentTypes("aliquoting")  // transfert
				));
		
		save(newExperimentTypeNode("library-prep",getExperimentTypes("library-prep").get(0),
				true,false,false,
				getExperimentTypeNodes("ext-to-rna-lib-process"), // previous nodes
				null, // pas de purif
				null, // pas de qc
				null  // pas de transfert
				));
		
		//Les nodes pour process Capture doivent obligatoirement etre crees AVANT
		//FDS ajout 11/07/2017 NGL-1201: processus capture
		save(newExperimentTypeNode("fragmentation",getExperimentTypes("fragmentation").get(0),
				false,false,false,
				getExperimentTypeNodes("ext-to-capture-prep-process-fc",
						               "ext-to-capture-prep-process-fc-ord"), // previous nodes
				null, // pas de purif
				getExperimentTypes("bioanalyzer-migration-profile",
						           "labchip-migration-profile"),   // qc ; est-ce necessaire de les lister ? ou un seul suffit ?
				null  // pas transfert
				));
		
		//FDS ajout 11/07/2017 NGL-1201: processus capture
		save(newExperimentTypeNode("sample-prep",getExperimentTypes("sample-prep").get(0),
				false,false,false,
				getExperimentTypeNodes("fragmentation"), // previous nodes
				null, // pas de purif
				null, // pas qc
				null  // pas transfert
				));
		
		//FDS commun WG_NANO et RNAseq; 11/07/2017 NGL-1201: commun aussi aux processus Capture
		save(newExperimentTypeNode("pcr-and-purification",getExperimentTypes("pcr-and-purification").get(0),
				true,false,false,
				getExperimentTypeNodes("library-prep",
						               "prep-wg-nano",
						               "sample-prep",
						               "ext-to-pcr-capture-pcr-indexing-fc",
						               "ext-to-pcr-capture-pcr-indexing-fc-ord"), // previous nodes
				null, // pas de purif
				getExperimentTypes("labchip-migration-profile",
				                   "fluo-quantification"),            // qc; est-ce necessaire de les lister ? ou un seul suffit ?
				null  // pas de transfert
				));
		
		//FDS ajout 11/07/2017 NGL-1201: processus capture
		save(newExperimentTypeNode("capture",getExperimentTypes("capture").get(0),
				false,false,false,
				getExperimentTypeNodes("pcr-and-purification",
									   "ext-to-capture-pcr-indexing-fc", 
									   "ext-to-capture-pcr-indexing-fc-ord"),// previous nodes
				null, // pas de purif
				null, // pas qc
				null  // pas tranfert
				));
		
		//FDS ajout 11/07/2017 NGL-1201: processus capture
		save(newExperimentTypeNode("pcr-and-indexing",getExperimentTypes("pcr-and-indexing").get(0),
				false,false,false,
				getExperimentTypeNodes("capture",
									   "ext-to-pcr-indexing-process-fc",
									   "ext-to-pcr-indexing-process-fc-ord"), // previous nodes
				null, // pas de purif
				getExperimentTypes("bioanalyzer-migration-profile",
						           "labchip-migration-profile"),    // qc; est-ce necessaire de les lister ? ou un seul suffit ?
				null  // pas tranfert
				));
		
		//FDS ajout 21/02/2017 NGL-1167: processus Chromium
		save(newExperimentTypeNode("chromium-gem-generation",getExperimentTypes("chromium-gem-generation").get(0),
				false,false,false,
				getExperimentTypeNodes("ext-to-wg-chromium-lib-process"), // previous nodes
				null, // pas de purif
				getExperimentTypes("bioanalyzer-migration-profile"), // qc; un seul suffit meme s'il y en a plusieurs possibles
				getExperimentTypes("tubes-to-plate") // transfert
				));
		
		//FDS ajout 21/02/2017 NGL-1167: processus Chromium
		save(newExperimentTypeNode("wg-chromium-lib-prep",getExperimentTypes("wg-chromium-lib-prep").get(0),
				false,false,false,
				getExperimentTypeNodes("chromium-gem-generation"), // previous nodes
				null, // pas purif
				getExperimentTypes("labchip-migration-profile"), // qc; un seul suffit meme s'il y en a plusieurs possibles
				getExperimentTypes("tubes-to-plate") // transfert 
				));		
		
		// FDS ajout 04/04/2018 NGL-1996
		save(newExperimentTypeNode("bisseq-lib-prep",getExperimentTypes("bisseq-lib-prep").get(0),
				false, false,false,
				getExperimentTypeNodes("ext-to-bis-seq-process-fc-ord"),// previous nodes
				null, // pas de purif
				getExperimentTypes("labchip-migration-profile"), // qc; un seul suffit meme s'il y en a plusieurs possibles
				null  // pas de transfert
				));
			
		// FDS ajout 04/04/2018 NGL-1727
		save(newExperimentTypeNode("small-rnaseq-lib-prep",getExperimentTypes("small-rnaseq-lib-prep").get(0),
				false, false,false,
				getExperimentTypeNodes("ext-to-small-rna-seq-process"),// previous nodes
				null, // pas de purif
				getExperimentTypes("labchip-migration-profile"), // qc; un seul suffit meme s'il y en a plusieurs possibles
				null  // pas de transfert
				));
		
		// FDS ajout 04/06/2018 NGL-1728
		save(newExperimentTypeNode("oxbisseq-and-bisseq-lib-prep",getExperimentTypes("oxbisseq-and-bisseq-lib-prep").get(0),
				false, false,false,
				getExperimentTypeNodes("ext-to-oxbisseq-and-bisseq-process-fc-ord"),// previous nodes
				null, // pas de purif
				getExperimentTypes("labchip-migration-profile"), // qc; un seul suffit meme s'il y en a plusieurs possibles
				null  // pas de transfert
				));
		
		// deplacer avant normalization-and-pooling.. aurait du planter ??!!!
		//FDS 30/04/2019 NGL-1725: Perméabilisation, Transposition, Purification pour processus ATACseq
		save(newExperimentTypeNode("permeabilization-transposition-purification",getExperimentTypes("permeabilization-transposition-purification").get(0),
				false,false,false,
				getExperimentTypeNodes("ext-to-atac-seq-process"), // previous nodes
				null, // pas de purif
				getExperimentTypes("qpcr-nb-cycle-setting"), // qc; un seul suffit meme s'il y en a plusieurs possibles
				null  // pas transfert
				));
		//deplacer avant normalization-and-pooling.. aurait du planter ??!!!
		//FDS 30/04/2019 NGL-1725: Amplification PCR + purification [ATAC Seq] pour processus ATACseq; 18/07 NGL-2607 chg nom et code
		save(newExperimentTypeNode("pcr-amplif-and-purif-atac-chip-seq",getExperimentTypes("pcr-amplif-and-purif-atac-chip-seq").get(0),
				false,false,false,
				getExperimentTypeNodes("permeabilization-transposition-purification"), // previous nodes
				null, // pas de purif
				getExperimentTypes("bioanalyzer-migration-profile"), // qc; un seul suffit meme s'il y en a plusieurs possibles
				null  // pas transfert
				));
		
		//FDS 24/10/2017 remplacer ext-to-norm-and-pool-fc-ord-depot  par  ext-to-norm-fc-ordered-depot
		save(newExperimentTypeNode("normalization-and-pooling",getExperimentTypes("normalization-and-pooling").get(0),
				false,false,false,
				getExperimentTypeNodes("ext-to-norm-fc-ordered-depot",
						               "ext-to-norm-and-pool-denat-fc-depot", // FDS ajout 06/10/2017
						               "pcr-and-purification",
						               "prep-pcr-free",                       // FDS 12/12/2016 ajout prep-pcr-free en previous
						               "wg-chromium-lib-prep",                // FDS 13/03/2017 NGL-1167: processus chromium
						               "pcr-and-indexing",                    // FDS 20/07/2017 NGL-1201: processus capture
						               "bisseq-lib-prep",                     // FDS 04/04/2018 NGL-1996: processus BisSeq
						               "small-rnaseq-lib-prep",               // FDS 04/04/2018 NGL-1727: processus SmallRNASeq
						               "oxbisseq-and-bisseq-lib-prep",        // FDS 04/06/2018 NGL-1728: processus OxBisSeq
						               "pcr-amplif-and-purif-atac-chip-seq"   // FDS 14/10/2019 NGL-2607: processus ATACSeq
						               ), // previous
				null, // pas de purif
				null, // pas de qc
				null  // pas de transfert
				));
		
		//FDS 12/09/2018 NGL-1226: RNAseq (DEV) Frag, cDNA, indexing   pour processus RNAseqFrag
		save(newExperimentTypeNode("frg-cdna-indexing",getExperimentTypes("frg-cdna-indexing").get(0),
				false,false,false,
				getExperimentTypeNodes("ext-to-rna-seq-frg-cdna-indexing-process"), // previous nodes
				null, // pas de purif
				null, // pas qc
				null  // pas transfert
				));
		
		//FDS 12/09/2018 NGL-1226: RNAseq (DEV) ampli/PCR  pour processus RNAseqFrag
		save(newExperimentTypeNode("dev-pcr-amplification",getExperimentTypes("dev-pcr-amplification").get(0),
				false,false,false,
				getExperimentTypeNodes("frg-cdna-indexing"), // previous nodes
				null, // pas de purif
				getExperimentTypes("bioanalyzer-migration-profile"), // qc; un seul suffit meme s'il y en a plusieurs possibles
				null  // pas transfert
				));
		
		// 12/09/2018 remonté ici car contient "cdna-synthesis" ( voir si ne pose pas de problemes...)
		new Nanopore().getExperimentTypeNode();	

		//FDS 12/09/2018 NGL-1226: (RNAseq DEV) Prep lib Nextera pour processus RNAseqFull; ajout ext-to-rnaseq-frg-indexing-process-from-cdna en previous
		save(newExperimentTypeNode("lib-and-pcr",getExperimentTypes("lib-and-pcr").get(0),
				false,false,false,
				getExperimentTypeNodes("cdna-synthesis",
									   "ext-to-rnaseq-frg-indexing-process-from-cdna"), // previous nodes
				null,// pas de purif
				getExperimentTypes("bioanalyzer-migration-profile"), // qc; un seul suffit meme s'il y en a plusieurs possibles
				null  // pas transfert
				));
		
		//FDS xx/2016 NGL-894: processus et experiments pour X5
		save(newExperimentTypeNode("lib-normalization",getExperimentTypes("lib-normalization").get(0), 
				false, false, false, 
				getExperimentTypeNodes("ext-to-norm-fc-ordered-depot",               //FDS 15/04/2016 NGL-894: processus court pour X5
						               "ext-to-norm-and-pool-denat-fc-depot",        //FDS 24/10/2017 ajout 
						               "prep-pcr-free",
						               "pcr-and-purification",                       //FDS 01/09/2016 WG_Nano 
						               "wg-chromium-lib-prep",                       //FDS 13/03/2017 NGL-1167: processus chromium
						               "pcr-and-indexing",                           //FDS 20/07/2017 NGL-1201: processus capture
						               "bisseq-lib-prep",                            //FDS 04/04/2018 NGL-1996: processus BisSeq
						               "oxbisseq-and-bisseq-lib-prep" ,              //FDS 04/06/2018 NGL-1728: processus BisSeq
						               "dev-pcr-amplification",                      //FDS 26/10/2018 NGL-1226: processus RNAseqFrag
						               "lib-and-pcr",                                //FDS 26/10/2018 NGL-1226: processus RNAseqFull
						               "pcr-amplif-and-purif-atac-chip-seq",          //FDS 30/04/2019 NGL-1725: processus ATACseq; 18/07/2019 chgt code
						               "TOTO"
						               ), // previous nodes
				null, // pas de purif
				getExperimentTypes("miseq-qc"), // qc ; un seul suffit meme s'il y en a plusieurs possibles
				getExperimentTypes("aliquoting","pool") // transfert
				));
		
		save(newExperimentTypeNode("denat-dil-lib",getExperimentTypes("denat-dil-lib").get(0),
				false,false,false,
				getExperimentTypeNodes("ext-to-denat-dil-lib", 
						               "ext-to-tf-illumina-run",   // FDS 26/09/2018 NGL-2235: ext-to du nouveau process
						               "lib-normalization",
						               "normalization-and-pooling" // FDS 08/12/2016 ajout
						               ), // previous nodes
				null, // pas de purif
				null, // pas qc 
				getExperimentTypes("aliquoting","pool") // tranfert; //FDS 20/06/2016 NGL-1029: ajout transfert pool;
				));
		
		save(newExperimentTypeNode("prepa-flowcell",getExperimentTypes("prepa-flowcell").get(0),
				false,false,false,
				getExperimentTypeNodes("ext-to-prepa-flowcell",
						               "denat-dil-lib"),         // previous nodes
				null, // pas de purif
				null, // pas qc
				null  // pas transfert
				));
		
		save(newExperimentTypeNode("prepa-fc-ordered",getExperimentTypes("prepa-fc-ordered").get(0),
				false,false,false,
				getExperimentTypeNodes("ext-to-prepa-fc-ordered",
						               "lib-normalization", 
						               "normalization-and-pooling"), // previous nodes
				null, // pas de purif
				null, // pas qc
				null  // pas transfert
				));
		
		save(newExperimentTypeNode("illumina-depot",getExperimentTypes("illumina-depot").get(0),
				false,false,false,
				getExperimentTypeNodes("prepa-flowcell",
						               "prepa-fc-ordered"), // previous nodes
				null, // pas de purif
				null, // pas qc
				null  // pas transfert
				));
		
		
		/* FDS Les noeuds qui ont de nombreux previous nodes doivent etre crees a la fin!!! */
		
		// FDS 06/06/2017: NGL-1447 => le noeud "tubes-to-plate" doit etre declaré pour les process commencant par un transfert
		//GA 20/06/2018 need to set ext-to to a next node to have good container state (IW-P) after dispatch with terminate for satellite processes
		
		save(newExperimentTypeNode("tubes-to-plate",getExperimentTypes("tubes-to-plate").get(0),
				false,false,false,
				getETForTubesToPlate(),  // previous nodes
				null, // pas de purif
				null, // pas qc
				null  // pas transfert
				));
		
		save(newExperimentTypeNode("labchip-migration-profile",getExperimentTypes("labchip-migration-profile").get(0),
				false, false,false,
				getETForLabchipMigrationProfile(),  // previous nodes
				null, // pas de purif
				null, // pas qc
				null  // pas transfert
				));
		
	}
	
	private List<ExperimentTypeNode> getETForTubesToPlate(){
		List<ExperimentTypeNode> pets = new ArrayList<>();
		pets.add(getExperimentTypeNodes("ext-to-transfert-qc-purif").get(0));
		return pets;		
	}
	
	private List<ExperimentTypeNode> getETForLabchipMigrationProfile(){
		List<ExperimentTypeNode> pets = new ArrayList<>();
		pets.add(getExperimentTypeNodes("ext-to-qc-transfert-purif").get(0));
		return pets;		
	}
	
	
	
	/* property definitions */
	
	private List<PropertyDefinition> getPropertyDefinitionsQPCR() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		MeasureCategoryDAO mcfind = MeasureCategory.find.get();
		MeasureUnitDAO     mufind = MeasureUnit.find.get();

		// laisser editable au cas ou la valeur calculée ne convient pas...
		propertyDefinitions.add(newPropertiesDefinition("Concentration", "concentration1", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, "F", null,
				mcfind.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION), 
				mufind.findByCode("nM"),
				mufind.findByCode("nM"),
				"single", 13, true, null, "2"));
		
		propertyDefinitions.add(newPropertiesDefinition("Taille librairie (facteur correctif)", "correctionFactorLibrarySize", LevelService.getLevels(Level.CODE.ContainerIn), Integer.class, true, "F", null,
				mcfind.findByCode(MeasureService.MEASURE_CAT_CODE_SIZE), 
				mufind.findByCode("pb"),
				mufind.findByCode("pb"),
				"single", 12, true, "470", null));
		
		return propertyDefinitions;
	}
	
	//FDS 07/05/2019 ajout -- JIRA NGL-1725
	private List<PropertyDefinition> getPropertyDefinitionsQPCRCycleSetting() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		
		// image, updatable; NGL-2607: changement de nom et de code
		propertyDefinitions.add(newPropertiesDefinition("Profil d'amplification", "amplificationProfile", LevelService.getLevels(Level.CODE.ContainerIn), Image.class, false, null, null,
				"img", 14, true, null, null));
		
		//InputContainer; NGL-2607: pas obligatoire
		propertyDefinitions.add(newPropertiesDefinition("Nb cycles PCR optimal", "optimalPcrCycleNb", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, null,
				null,null,null,
				"single", 15, true, null, null)); 
		
		return propertyDefinitions;
	}
	
	private static List<PropertyDefinition> getPropertyAliquoting() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		
		//InputContainer
		propertyDefinitions.add(newPropertiesDefinition("Volume engagé","inputVolume", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, true, null,
				MeasureCategory.find.get().findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),
				MeasureUnit.find.get().findByCode("µL"),
				MeasureUnit.find.get().findByCode("µL"),
				"single",10, false));
		
		return propertyDefinitions;
	}
	
	private static List<PropertyDefinition> getPropertyDefinitionsPrepaflowcellCNG() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		MeasureCategoryDAO mcfind = MeasureCategory.find.get();
		MeasureUnitDAO     mufind = MeasureUnit.find.get();
		
		//InputContainer
		propertyDefinitions.add(newPropertiesDefinition("Conc. chargement", "finalConcentration2", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null,
				mcfind.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION), 
				mufind.findByCode("pM"),
				mufind.findByCode("nM"),
				"single",25));
		
		//Outputcontainer		
		propertyDefinitions.add(newPropertiesDefinition("% phiX", "phixPercent", LevelService.getLevels(Level.CODE.ContainerOut), Double.class, true, null,
				null, null, null, 
				"single",51,false,"1",null));	
		
		propertyDefinitions.add(newPropertiesDefinition("Volume final", "finalVolume", LevelService.getLevels(Level.CODE.ContainerOut), Double.class, true, null,
				mcfind.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME), 
				mufind.findByCode("µL"), 
				mufind.findByCode("µL"), 
				"single",52, false));
		
		return propertyDefinitions;
	}
	
	//FDS ajout 09/11/2015 -- JIRA NGL-838
	private List<PropertyDefinition> getPropertyDefinitionsPrepaflowcellOrderedCNG() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		MeasureCategoryDAO mcfind = MeasureCategory.find.get();
		MeasureUnitDAO     mufind = MeasureUnit.find.get();

		//InputContainer
		// test NGL-1767: ajout liste de volumes pour les differents types de sequencage ?? 01/02/2018 Non laisser un champ a saisie libre et pas de valeur par defaut
		//propertyDefinitions.add(newPropertiesDefinition("Vol. engagé", "inputVolume2", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, newValues("5","150", "310"), "5",
		propertyDefinitions.add(newPropertiesDefinition("Vol. engagé", "inputVolume2", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, "5",
				mcfind.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME), 
				mufind.findByCode("µL"),
				mufind.findByCode("µL"),
				"single",21));
		
		propertyDefinitions.add(newPropertiesDefinition("Vol. NaOH", "NaOHVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, "5",
				mcfind.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME), 
				mufind.findByCode("µL"),
				mufind.findByCode("µL"),
				"single",22));
		
		propertyDefinitions.add(newPropertiesDefinition("Conc. NaOH", "NaOHConcentration", LevelService.getLevels(Level.CODE.ContainerIn), String.class, true, null,
				null, null, null, "single",23,true,"0.1N",null));
		
		propertyDefinitions.add(newPropertiesDefinition("Vol. TrisHCL", "trisHCLVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, "5",
				mcfind.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME), 
				mufind.findByCode("µL"), 
				mufind.findByCode("µL"), 
				"single",24));
		
		// attention mettre null comme valeur par defaut sinon pb....
		propertyDefinitions.add(newPropertiesDefinition("Conc. TrisHCL", "trisHCLConcentration", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, null,
				mcfind.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),
				mufind.findByCode("mM"), // NORMAL, pas une faute de frappe !!!!
				mufind.findByCode("nM"),
				"single",25));
		
		propertyDefinitions.add(newPropertiesDefinition("Vol. master EPX", "masterEPXVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, "35",
				mcfind.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME), 
				mufind.findByCode("µL"),
				mufind.findByCode("µL"),
				"single",26));
		
		propertyDefinitions.add(newPropertiesDefinition("Concentration finale", "finalConcentration2", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null,
				mcfind.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME), 
				mufind.findByCode("pM"),
				mufind.findByCode("nM"),
				"single",27,false));
		
		//OuputContainer
		//keep order declaration between phixPercent and finalVolume
		propertyDefinitions.add(newPropertiesDefinition("% phiX", "phixPercent", LevelService.getLevels(Level.CODE.ContainerOut), Double.class, true, null,
				null,null, null,
				"single",51,false,"1",null));
		
		//NGL-2083 ne plus mettre de valeur par defaut
		propertyDefinitions.add(newPropertiesDefinition("Volume final", "finalVolume", LevelService.getLevels(Level.CODE.ContainerOut), Double.class, true, null,
				mcfind.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),
				mufind.findByCode("µL"),
				mufind.findByCode("µL"),
				"single",28,false, null ,null));
		
		// NGL-1325 ajout propriété sequencingType de niveau Experiment, optionnelle
		// NGL-1730 ajout NovaSeq 6000;  NGL1767: subdiviser en NovaSeq 6000 / S2 + NovaSeq 6000 / S4 ( attention si changement de labels=> sont utilisés dans javascript)
		// NGL-2083 obligatoire=>true + editable=>false; NGL-2219 ajout "et FC" au nom de propriete
		propertyDefinitions.add(newPropertiesDefinition("Type de séquençage (et FC)", "sequencingType", LevelService.getLevels(Level.CODE.Experiment), String.class, true, null,
				DescriptionFactory.newValues("Hiseq 4000",
						                     "Hiseq X",
						                     "NovaSeq 6000 / S1-SP",           // NGL-2191 ajout NovaSeq 6000 / S1     ; NGL-2624 renommage "S1"=> "S1-SP"
						                     "NovaSeq 6000 / S1-SP / XP",      // NGL-2219 ajout NovaSeq 6000 / S1 / XP; NGL-2624 renommage "S1"=> "S1-SP"
						                     "NovaSeq 6000 / S2",
						                     "NovaSeq 6000 / S2 / XP",      // NGL-2219 ajout NovaSeq 6000 / S2 / XP
						                     "NovaSeq 6000 / S4",
						                     "NovaSeq 6000 / S4 / XP"),     // NGL-2219 ajout NovaSeq 6000 / S4 / XP
				null,null,null,
				"single",10, false, null,null));
		
		return propertyDefinitions;
		
	}
	
	private static List<PropertyDefinition> getPropertyDefinitionsDenatDilLibCNG() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		
		//FDS 01/02/2016 pourquoi est commenté ???
		//propertyDefinitions.add(newPropertiesDefinition("Stockage", "storage", LevelService.getLevels(Level.CODE.ContainerOut), String.class, false, null, null, null, null, "single",55,true,null));
		
		return propertyDefinitions;
	}
	
	public static List<PropertyDefinition> getPropertyDefinitionsQCMiseq() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		MeasureCategoryDAO mcfind = MeasureCategory.find.get();
		MeasureUnitDAO     mufind = MeasureUnit.find.get();

		//InputContainer 
		propertyDefinitions.add(newPropertiesDefinition("Densité de clusters", "clusterDensity", LevelService.getLevels(Level.CODE.ContainerIn), Integer.class, false, null, null,
				mcfind.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),
				mufind.findByCode("c/mm²"),
				mufind.findByCode("c/mm²"),
				"single", 11, true, null, null));
		
		//FDS 26/08/2016 -- JIRA NGL-1046: ajouter toutes les autres propriétés du fichier
		propertyDefinitions.add(newPropertiesDefinition("% cluster", "clusterPercentage", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, null, 
				null,null,null,"single", 12, true, null, null));
		propertyDefinitions.add(newPropertiesDefinition("% PF", "passingFilter", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, null, 
				null,null,null,"single", 13, true, null, null));
		propertyDefinitions.add(newPropertiesDefinition("% Aligned R1", "R1AlignedPercentage", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, null, 
				null,null,null,"single", 14, true, null, null));
		propertyDefinitions.add(newPropertiesDefinition("% Aligned R2", "R2AlignedPercentage", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, null, 
				null,null,null,"single", 15, true, null, null));
		propertyDefinitions.add(newPropertiesDefinition("% Mismatch R1", "R1MismatchPercentage", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, null, 
				null,null,null,"single", 16, true, null, null));
		// NGL-2337 correction label "% Mismatch R2"
		propertyDefinitions.add(newPropertiesDefinition("% Mismatch R2", "R2MismatchPercentage", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, null, 
				null,null,null,"single", 17, true, null, null));
		
		propertyDefinitions.add(newPropertiesDefinition("Taille d'insert médiane", "measuredInsertSize", LevelService.getLevels(Level.CODE.ContainerIn,Level.CODE.Content), Integer.class, false, null, null, 
				mcfind.findByCode(MeasureService.MEASURE_CAT_CODE_SIZE), 
				mufind.findByCode("pb"), 
				mufind.findByCode("pb"),
				"single", 18, true, null, null));
		
		propertyDefinitions.add(newPropertiesDefinition("Taille Min", "minInsertSize", LevelService.getLevels(Level.CODE.ContainerIn), Integer.class, false, null, null,
				mcfind.findByCode(MeasureService.MEASURE_CAT_CODE_SIZE), 
				mufind.findByCode("pb"),
				mufind.findByCode("pb"),
				"single", 19, true, null, null));
		
		propertyDefinitions.add(newPropertiesDefinition("Taille Max", "maxInsertSize", LevelService.getLevels(Level.CODE.ContainerIn), Integer.class, false, null, null,
				mcfind.findByCode(MeasureService.MEASURE_CAT_CODE_SIZE), 
				mufind.findByCode("pb"),
				mufind.findByCode("pb"),
				"single", 20, true, null, null));
		
		propertyDefinitions.add(newPropertiesDefinition("Observed Diversity", "observedDiversity", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, null,
				null,null,null,
				"single",21, true, null, null));
		
		propertyDefinitions.add(newPropertiesDefinition("Estimated Diversity", "estimatedDiversity", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, null,
				null,null,null,
				"single", 22, true, null, null));
		
		return propertyDefinitions;
	}
	
	// FDS JIRA NGL-1030 Ajouter la propriété size et rendre les 2 obligatoires au status "F"(Terminé)
	public static List<PropertyDefinition> getPropertyDefinitionsChipMigration() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();

		//InputContainer (pas d'outputContainer sur une experience QC )
		propertyDefinitions.add(newPropertiesDefinition("Concentration", "concentration1", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, "F", null,
				MeasureCategory.find.get().findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),
				null, // l'unité est variable. NE PLUS LA SPECIFIER..., MeasureUnit.find.findByCode("ng/µl")
				null, // l'unité est variable. NE PLUS LA SPECIFIER..., MeasureUnit.find.findByCode("ng/µl")
				"single", 11, true, null, null));
		
		// laiser la position 12 libre pour la colonne unit
		propertyDefinitions.add(newPropertiesDefinition("Taille", "size1", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, "F", null,
				MeasureCategory.find.get().findByCode(MeasureService.MEASURE_CAT_CODE_SIZE), 
				MeasureUnit.find.get().findByCode("pb"), 
				MeasureUnit.find.get().findByCode("pb"),
				"single", 13, true, null, null));
		
		propertyDefinitions.add(newPropertiesDefinition("Profil de migration", "migrationProfile", LevelService.getLevels(Level.CODE.ContainerIn), Image.class, false, null, null,
				"img", 14, false, null, null));
		
		// 07/03/2018 NGL-1859: propriété pour demander a l'utilisateur s'il faut copier concentration (par defaut TRUE)
		propertyDefinitions.add(newPropertiesDefinition("Copier concentration dans concentration finale du container ?", "copyConcentration", LevelService.getLevels(Level.CODE.Experiment), Boolean.class, true, null, null,
				"single",15, true,"true", null));
		
		return propertyDefinitions;
	}
	
	private static List<PropertyDefinition> getPropertyDefinitionsIlluminaDepot() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		
		//Utilisé par import ngl-data CNG de creation des depot-illumina
		//propertyDefinitions.add(newPropertiesDefinition("Code LIMS", "limsCode", LevelService.getLevels(Level.CODE.Experiment), Integer.class, false, "single"));	
		propertyDefinitions.add(newPropertiesDefinition("Date réelle de dépôt", "runStartDate", LevelService.getLevels(Level.CODE.Experiment), Date.class, true, null, null,
				"single",21, true,null, null));
		
		propertyDefinitions.add(newPropertiesDefinition("Activation NGS-RG", "rgActivation", LevelService.getLevels(Level.CODE.Experiment), Boolean.class, true, null, null,
				"single",22, true,"true", null));
		
		return propertyDefinitions;
	}
	
	// FDS ajout 05/02/2016 -- JIRA NGL-894: experiment PrepPcrFree pour le process X5
	private List<PropertyDefinition> getPropertyDefinitionsPrepPcrFree() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		MeasureCategoryDAO mcfind = MeasureCategory.find.get();
		MeasureUnitDAO     mufind = MeasureUnit.find.get();
		
		//InputContainer
		propertyDefinitions.add(newPropertiesDefinition("Vol. engagé dans Frag", "inputVolumeFrag", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, null,
				mcfind.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME), 
				mufind.findByCode("µL"), 
				mufind.findByCode("µL"),
				"single",20,true,null,null));
		
		propertyDefinitions.add(newPropertiesDefinition("Qté. engagée dans Frag", "inputQuantityFrag", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, null,
				mcfind.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY), 
				mufind.findByCode("ng"), 
				mufind.findByCode("ng"),
				"single",21,true,null,null));
		
		propertyDefinitions.add(newPropertiesDefinition("Vol. engagé dans Lib", "inputVolumeLib", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, null,
				mcfind.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME), 
				mufind.findByCode("µL"), 
				mufind.findByCode("µL"),
				"single",22,true,null,null));
		
		propertyDefinitions.add(newPropertiesDefinition("Qté. engagée dans Lib", "inputQuantityLib", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, null,
				mcfind.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY), 
				mufind.findByCode("ng"), 
				mufind.findByCode("ng"),
				"single",23,true,null,null));
		
		//OuputContainer
		// GA 08/02/2016 =>  ces proprietes de containerOut doivent etre propagees au content
		// GA 14/03/2016 => il faut specifier l'état auquel les propriétés sont obligatoires: ici Finished (F)
		propertyDefinitions.add(newPropertiesDefinition("Tag", "tag", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), String.class, true, "F", 
				getTagIllumina(), 
				"single", 30, true, null,null));
		
		propertyDefinitions.add(newPropertiesDefinition("Catégorie de Tag", "tagCategory", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), String.class, true, "F", 
				getTagCategories() , 
				"single", 31, false, null,null)); // 26/06/2018 non editable
		
		// pas de niveau content car théoriques( J Guy..)
		propertyDefinitions.add(newPropertiesDefinition("Taille insert (théorique)", "insertSize", LevelService.getLevels(Level.CODE.ContainerOut),Integer.class, true, null,
				mcfind.findByCode(MeasureService.MEASURE_CAT_CODE_SIZE), 
				mufind.findByCode("pb"), 
				mufind.findByCode("pb"),
				"single",32,true,"350", null));
		
		propertyDefinitions.add(newPropertiesDefinition("Taille librairie (théorique)", "librarySize", LevelService.getLevels(Level.CODE.ContainerOut),Integer.class, true, null,	
				mcfind.findByCode(MeasureService.MEASURE_CAT_CODE_SIZE), 
				mufind.findByCode("pb"), 
				mufind.findByCode("pb"),
				"single",33, true,"470",null));
	
		return propertyDefinitions;
	}
	
	// FDS ajout 27/09/2016  pour le process WG_NANO
	// similaire a PrepPcrFree mais : pas de valeurs par defaut, pas de tailles theoriques
	private List<PropertyDefinition> getPropertyDefinitionsPrepWgNano() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		MeasureCategoryDAO mcfind = MeasureCategory.find.get();
		MeasureUnitDAO     mufind = MeasureUnit.find.get();
		
		//InputContainer
		propertyDefinitions.add(newPropertiesDefinition("Vol. engagé dans Frag", "inputVolumeFrag", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, null,
				mcfind.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME), 
				mufind.findByCode("µL"), 
				mufind.findByCode("µL"),
				"single",20));
		
		propertyDefinitions.add(newPropertiesDefinition("Qté. engagée dans Frag", "inputQuantityFrag", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, null,
				mcfind.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY), 
				mufind.findByCode("ng"), 
				mufind.findByCode("ng"),
				"single",21));
		
		propertyDefinitions.add(newPropertiesDefinition("Vol. engagé dans Lib", "inputVolumeLib", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, null,
				mcfind.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME), 
				mufind.findByCode("µL"), 
				mufind.findByCode("µL"),
				"single",22));
		
		propertyDefinitions.add(newPropertiesDefinition("Qté. engagée dans Lib", "inputQuantityLib", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, null,
				mcfind.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY), 
				mufind.findByCode("ng"), 
				mufind.findByCode("ng"),
				"single",23));
		
		//OuputContainer
		// GA 08/02/2016 => ces proprietes de containerOut doivent etre propagees au content
		// GA 14/03/2016 => il faut specifier l'état auquel les propriétés sont obligatoires: ici Finished (F)
		propertyDefinitions.add(newPropertiesDefinition("Tag", "tag", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), String.class, true, "F",
				getTagIllumina(),
				"single", 30, true, null,null));
		
		propertyDefinitions.add(newPropertiesDefinition("Catégorie de Tag", "tagCategory", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), String.class, true, "F",
				getTagCategories(),
				"single", 31, false, null,null)); // 06/09/2018 non editable
		
		return propertyDefinitions;
	}
	
	// FDS ajout 05/02/2016 -- JIRA NGL-894: experiment librairie normalization pour le process X5
	// FDS 19/07/2017 -- JIRA NGL-1519: egalement utilisees par l'experience de transfert additional-normalization
	private List<PropertyDefinition> getPropertyDefinitionsLibNormalization() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		MeasureCategoryDAO mcfind = MeasureCategory.find.get();
		MeasureUnitDAO     mufind = MeasureUnit.find.get();
		
		//InputContainer
		// calculé automatiquement en fonction du volume final et concentration final demandés ou saisie libre, non obligatoire
		// 26/07/2017  non editable
		propertyDefinitions.add(newPropertiesDefinition("Volume engagé", "inputVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, null,
				mcfind.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),
				mufind.findByCode("µL"),
				mufind.findByCode("µL"),
				"single", 20, false, null,null));
		
		//buffer 
		// calculé automatiquement en fonction du volume final et concentration final demandés ou saisie libre, non obligatoire
		// 26/07/2017  non editable
		propertyDefinitions.add(newPropertiesDefinition("Volume tampon", "bufferVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, null,
				mcfind.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),
				mufind.findByCode("µL"),
				mufind.findByCode("µL"),
				"single", 21, false, null,null));
		
		//OuputContainer	
		// 18/07/2018 ajoutée pour NGL-1996; ContainerOut ET Container; 09/10/2019 NGL-2395 modif label: ajout "(déjà présent)"
		propertyDefinitions.add(newPropertiesDefinition("% Phix (Ox)Biseq (déjà présent)", "oxbsPhixPercentage", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Container), Double.class, false, null, null,
				"single", 150, true, null,null));

		return propertyDefinitions;
	}
	
	// FDS ajout 02/06/2016 -- JIRA NGL-1028: experiment normalization-and-pooling
	private List<PropertyDefinition> getPropertyDefinitionsNormalizationAndPooling() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		MeasureCategoryDAO mcfind = MeasureCategory.find.get();
		MeasureUnitDAO     mufind = MeasureUnit.find.get();
		
		//InputContainer
		// calculé automatiquement en fonction du volume final et concentration final demandés ou saisie libre, non obligatoire VERIFIER
		propertyDefinitions.add(newPropertiesDefinition("Volume engagé", "inputVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, null,
				mcfind.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),
				mufind.findByCode("µL"),
				mufind.findByCode("µL"),
				"single", 20, true, null,null));
		
		//OuputContainer 
		propertyDefinitions.add(newPropertiesDefinition("Volume tampon Tris", "bufferVolume", LevelService.getLevels(Level.CODE.ContainerOut), Double.class, false, null, null,
				mcfind.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),
				mufind.findByCode("µL"),
				mufind.findByCode("µL"),
				"single", 25, true, null,null));
		
		// 18/10/2016 ajout workName
		propertyDefinitions.add(newPropertiesDefinition("Label de travail", "workName", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Container), String.class, false, null, null,
				"single", 100, true, null,null));
		
		// 18/07/2018 ajoutée pour NGL-1996; ContainerOut ET Container; 09/10/2019 NGL-2395 modif label: ajout "(déjà présent)"
		propertyDefinitions.add(newPropertiesDefinition("% Phix (Ox)Biseq (déjà présent)", "oxbsPhixPercentage", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Container), Double.class, false, null,
				null, 
				"single", 150, true, null,null));
		
		return propertyDefinitions;
	}
	
	// FDS ajout 17/06/2016 -- JIRA NGL-1029: experiment pool en plaque
	private static List<PropertyDefinition> getPropertyDefinitionPool() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		MeasureCategoryDAO mcfind = MeasureCategory.find.get();
		MeasureUnitDAO     mufind = MeasureUnit.find.get();
		
		//InputContainer
		propertyDefinitions.add(newPropertiesDefinition("Volume engagé", "inputVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, null,
				mcfind.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),
				mufind.findByCode("µL"),
				mufind.findByCode("µL"),
				"single", 20, true, null,null));
		
		//OuputContainer 
		propertyDefinitions.add(newPropertiesDefinition("Volume tampon", "bufferVolume", LevelService.getLevels(Level.CODE.ContainerOut), Double.class, false, null, null,
				mcfind.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),
				mufind.findByCode("µL"),
				mufind.findByCode("µL"),
				"single", 25, true, null,null));
		
		// 18/10/2016 ajout workName
		propertyDefinitions.add(newPropertiesDefinition("Label de travail", "workName", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Container), String.class, false, null, null, 
				"single", 100, true, null,null));
		
		// 23/07/2018 ajoutée pour NGL-1996; ContainerOut ET Container; 09/10/2019 NGL-2395 modif label: ajout "(déjà présent)"
		propertyDefinitions.add(newPropertiesDefinition("% Phix (Ox)Biseq (déjà présent)", "oxbsPhixPercentage", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Container), Double.class, false, null, null, 
				"single", 150, true, null,null));
		
		return propertyDefinitions;
	}
	
	// FDS ajout 01/08/2016 -- JIRA NGL-1027: experiment PCR + purification en plaque	
	private static List<PropertyDefinition> getPropertyDefinitionsPcrAndPurification() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		MeasureUnitDAO     mufind = MeasureUnit.find.get();
		
		//InputContainer
		// volume engagé editable et obligatoire, qté pas editable calculée en fonction volume engagé et pas sauvegardée
		// 27/09/2016 ajout default value '25"; 09/11/2017 NGL-1691: supression valeur par defaut
		propertyDefinitions.add(newPropertiesDefinition("Volume engagé", "inputVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, null,
				MeasureCategory.find.get().findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),
				mufind.findByCode("µL"),
				mufind.findByCode("µL"),
				"single", 20, true, null, null));
		
		//OuputContainer 
		// rien
		
		return propertyDefinitions;
	}
	
	//FDS ajout 03/08/2016 -- JIRA NGL-1026: experiment library prepartion sans fragmentation ( duplication a partir de pcr-free .. et suppression de la fragmentation
	private List<PropertyDefinition> getPropertyDefinitionsLibraryPrep() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		MeasureCategoryDAO mcfind = MeasureCategory.find.get();
		MeasureUnitDAO     mufind = MeasureUnit.find.get();
		
		//InputContainer
		// valeur par defaut pour volume et qté engagées ?? Pas pour l'instant...
		propertyDefinitions.add(newPropertiesDefinition("Vol. engagé dans Lib", "inputVolumeLib", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, null,
				mcfind.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),
				mufind.findByCode("µL"),
				mufind.findByCode("µL"),
				"single",22, true, null, null));
		
		propertyDefinitions.add(newPropertiesDefinition("Qté. engagée dans Lib", "inputQuantityLib", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, null,
				mcfind.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),
				mufind.findByCode("ng"),
				mufind.findByCode("ng"),
				"single",23, true, null, null));
		
		//OuputContainer
		// ces propriétés de containerOut doivent etre propagees au content
		// il faut specifier l'état auquel les propriétés sont obligatoires: ici Finished (F)
		propertyDefinitions.add(newPropertiesDefinition("Tag", "tag", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), String.class, true, "F", 
				getTagIllumina(), 
				"single", 30, true, null,null));
		
		propertyDefinitions.add(newPropertiesDefinition("Catégorie de Tag", "tagCategory", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), String.class, true, "F", 
				getTagCategories(),
				"single", 31, false, null,null)); // 06/09/2018 non editable	
		
		return propertyDefinitions;
	}
	
	//FDS ajout 21/02/2017 -- JIRA NGL-1167 experiences pour process Chromium
	private List<PropertyDefinition> getPropertyDefinitionsChromiumGemGeneration() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		MeasureUnitDAO     mufind = MeasureUnit.find.get();
		
		//InputContainer
		propertyDefinitions.add(newPropertiesDefinition("Conc. dilution","dilutionConcentration", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, null,
				MeasureCategory.find.get().findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION), 
				mufind.findByCode("ng/µL"), 
				mufind.findByCode("ng/µL"),
				"single",22, true, null, null));
		
		return propertyDefinitions;
	}
	
	//FDS ajout 21/02/2017 -- JIRA NGL-1167 experiences pour process Chromium
	private List<PropertyDefinition> getPropertyDefinitionsWGChromiumLibPrep() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		
		//OuputContainer
		//restreindre aux tags "POOL-INDEX"
		// ces propriétés de containerOut doivent etre propagées au content; propriétés obligatoires a: Finished 
		propertyDefinitions.add(newPropertiesDefinition("Tag", "tag", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), String.class, true, "F", 
				getTagIllumina(Arrays.asList("POOL-INDEX")), 
				"single", 30, true, null,null));
		
		// restreindre tagCategory a POOL-INDEX 
		propertyDefinitions.add(newPropertiesDefinition("Catégorie de Tag", "tagCategory", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), String.class, true, "F",
				DescriptionFactory.newValues("POOL-INDEX"), 
				"single", 31, false ,"POOL-INDEX", null));		
		
		return propertyDefinitions;
	}
	
	// FDS ajout 21/02/2017 -- JIRA NGL-1167: QC Bioanalyzer pour process Chromium: aucune obligatoire !!
	public static List<PropertyDefinition> getPropertyDefinitionsBioanalyzer() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		MeasureCategoryDAO mcfind = MeasureCategory.find.get();
		MeasureUnitDAO     mufind = MeasureUnit.find.get();
		
		//InputContainer (pas d'outputContainer sur une experience QC )
		propertyDefinitions.add(newPropertiesDefinition("Taille", "size1", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false,null, null,
				mcfind.findByCode(MeasureService.MEASURE_CAT_CODE_SIZE),
				mufind.findByCode("pb"),
				mufind.findByCode("pb"),
				"single", 12, true, null, null));
		
		// le bouton import n'apparait que si la propriété est editable=true
		propertyDefinitions.add(newPropertiesDefinition("Profil de migration", "migrationProfile", LevelService.getLevels(Level.CODE.ContainerIn), Image.class, false, null, null,
				"img", 13,true, null, null));
		
		// 6/12/2018 NGL-1226 ne plus imposer d'unité
		propertyDefinitions.add(newPropertiesDefinition("Concentration", "concentration1", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, null,
				mcfind.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),
				null, //mufind.findByCode("ng/µL"),
				null, //mufind.findByCode("ng/µL"),
				"single", 14, true, null, null));
		
		// 04/01/2019 NGL-1226: propriété pour demander a l'utilisateur s'il faut copier concentration (par defaut TRUE)
		propertyDefinitions.add(newPropertiesDefinition("Copier concentration dans concentration finale du container ?", "copyConcentration", LevelService.getLevels(Level.CODE.Experiment), Boolean.class, true, null, null,
				"single",15, true,"true", null));
			
		return propertyDefinitions;
	}
	
	// GA : stocker dans un pseudo QC les valeurs initales du fichier importé
	private List<PropertyDefinition> getPropertyDefinitionsBankQC() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		MeasureCategoryDAO mcfind = MeasureCategory.find.get();
		MeasureUnitDAO     mufind = MeasureUnit.find.get();
		
		propertyDefinitions.add(newPropertiesDefinition("Volume fourni", "providedVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, null,
				mcfind.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),
				mufind.findByCode( "µL"),
				mufind.findByCode( "µL"),
				"single", 11, true, null,null));
		
		propertyDefinitions.add(newPropertiesDefinition("Concentration fournie", "providedConcentration", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, null,
				mcfind.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),
				mufind.findByCode("ng/µL"),
				mufind.findByCode("ng/µL"),
				"single", 13, true, null,null));
		
		// FDS 14/03/2017 NGL-1776 ajout propriété venant du LIMS Modulbio (voir aussi ImportService) 
		propertyDefinitions.add(newPropertiesDefinition("Bank Integrity Number", "bankIntegrityNumber", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, null,
				"single", 15, true, null,null));
		
		return propertyDefinitions;
	}
	
	// FDS 21/06/2017 ajout -- JIRA NGL-1472: necessiter d'ajouter QC provenant de collaborateur extérieur.
	private List<PropertyDefinition> getPropertyDefinitionsExternalQC() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		MeasureCategoryDAO mcfind = MeasureCategory.find.get();
		MeasureUnitDAO     mufind = MeasureUnit.find.get();
		
		propertyDefinitions.add(newPropertiesDefinition("Volume fourni", "providedVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, null,
				mcfind.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),
				mufind.findByCode("µL"),
				mufind.findByCode("µL"),
				"single", 11, true, null,null));
		
		propertyDefinitions.add(newPropertiesDefinition("Concentration fournie", "providedConcentration", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, null,
				mcfind.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),
				mufind.findByCode("ng/µL"),
				mufind.findByCode("ng/µL"),
				"single", 13, true, null,null));
		
		// FDS 30/09/2019 NGL-2681: autorisez decimaux pour providedSize=> changer  Integer.class => Double.class
		propertyDefinitions.add(newPropertiesDefinition("Taille fournie", "providedSize", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, null,
				mcfind.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),
				mufind.findByCode("pb"),
				mufind.findByCode("pb"),
				"single", 16, true, null,null));
		
		return propertyDefinitions;	
	}
	
	//FDS ajout 18/07/2017 NGL-1201
	private List<PropertyDefinition> getPropertyDefinitionsSamplePrepCapture() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		
		// pas de propriétés pour l'instant..
		return propertyDefinitions;
	}
	
	//FDS ajout 18/07/2017 NGL-1201
	private List<PropertyDefinition> getPropertyDefinitionsFragmentation() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		
		// pas de propriétés pour l'instant...
		return propertyDefinitions;
	}
	
	//FDS ajout 18/07//2017 NGL-1201
	private List<PropertyDefinition> getPropertyDefinitionsCapture() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		MeasureCategoryDAO mcfind = MeasureCategory.find.get();
		MeasureUnitDAO     mufind = MeasureUnit.find.get();

		// FDS 09/11/2017 NGL-1691: ajout propriété de niveau Global (experiment) :Temps d'hybridation==hybridizationTime : saisie libre texte, non obligatoire
		// que veut dire le parametre "position" pour les propriété de niveau Global ??
		propertyDefinitions.add(newPropertiesDefinition("Temps d'hybridation", "hybridizationTime", LevelService.getLevels(Level.CODE.Experiment), String.class, false, null, null,
				"single",13,true,null,null));	
		
		//InputContainer
		propertyDefinitions.add(newPropertiesDefinition("Volume engagé","inputVolume", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, true, null, null,
				mcfind.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME), 
				mufind.findByCode("µL"), 
				mufind.findByCode("µL"), 
				"single",10,false,null,null));          // pas editable=> calculé !
		
		// editable
		propertyDefinitions.add(newPropertiesDefinition("Qté. engagée", "inputQuantity", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, null,
				mcfind.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY), 
				mufind.findByCode("ng"), 
				mufind.findByCode("ng"),
				"single",11,true,null,null));
		
		//OuputContainer 
		// Liste; valeur par defaut= celle qui se trouve dans processus dans expectedBaits ?. PAS POSSIBLE!!!
		// 31/08/2017 erreur de spec, mettre sur ContainerOut 
		// 07/12/2017 NGL-1735 : "F" place sur default value au lieu de requiredState !!!
		propertyDefinitions.add(newPropertiesDefinition("Baits (sondes)", "baits", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), String.class, true, "F",
				getCaptureBaitsValues(),
				null, null, null,
				"single",12,true,null,null));
			
		return propertyDefinitions;
	}
	
	//FDS ajout 18/07/2017 NGL-1201
	private List<PropertyDefinition> getPropertyDefinitionsPcrIndexing() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		MeasureUnitDAO     mufind = MeasureUnit.find.get();
		MeasureCategoryDAO mcfind = MeasureCategory.find.get();
		
		//OuputContainer
		// proprietes de containerOut doivent etre propagees au content
		// il faut specifier l'état auquel les propriétés sont obligatoires: ici Finished (F)
		propertyDefinitions.add(newPropertiesDefinition("Tag", "tag", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), String.class, true, "F", 
				getTagIllumina(), 
				"single", 30, true, null, null));
		
		propertyDefinitions.add(newPropertiesDefinition("Catégorie de Tag", "tagCategory", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), String.class, true, "F",
				getTagCategories(), 
				"single", 31, false, null, null)); // 06/09/2018 non editable
		
		// valeur par defaut: 30 µL
		propertyDefinitions.add(newPropertiesDefinition("Volume final", "finalVolume", LevelService.getLevels(Level.CODE.ContainerOut), Double.class, true, "F", null,
				mcfind.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME), 
				mufind.findByCode("µL"), 
				mufind.findByCode("µL"), 
				"single", 32, true, "30", null));
		
		return propertyDefinitions;
	}
	
	//FDS ajout 27/07/2017 NGL-1201
	private List<PropertyDefinition> getPropertyDefinitionsQuantIt() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		MeasureUnitDAO     mufind = MeasureUnit.find.get();
		MeasureCategoryDAO mcfind = MeasureCategory.find.get();

		propertyDefinitions.add(newPropertiesDefinition("Concentration", "concentration1", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, null, 
				mcfind.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),
				mufind.findByCode("ng/µL"),
				mufind.findByCode("ng/µL"),
				"single", 13, true, null, null));
		
		// FDS ajout 26/03/2018 NGL-1970: propriété pour demander a l'utilisateur s'il faut copier concentration (par defaut TRUE)
		propertyDefinitions.add(newPropertiesDefinition("Copier concentration dans concentration finale du container ?", "copyConcentration", LevelService.getLevels(Level.CODE.Experiment), Boolean.class, true, null, null,
				"single",15, true,"true", null));
		
		return propertyDefinitions;
	}
	
	//FDS ajout 05/04/2018 NGL-1996
	private List<PropertyDefinition> getPropertyDefinitionsBisSeqLibPrep() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		
		//OuputContainer
		// FDS 26/06/2019 NGL-2552 correction pcrCycleNumber => Integer.class
		propertyDefinitions.add(newPropertiesDefinition("Nbre Cycles PCR", "pcrCycleNumber", LevelService.getLevels(Level.CODE.ContainerOut), Integer.class, false, "F", null,
				null,null,null,
				"single", 29, true, "16", null)); 
		
		// proprietes de containerOut doivent etre propagees au content
		// il faut specifier l'état auquel les propriétés sont obligatoires: ici Finished (F) 
		propertyDefinitions.add(newPropertiesDefinition("Tag", "tag", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), String.class, true, "F",
				getTagIllumina(),
				"single", 30, true, null, null));
		
		propertyDefinitions.add(newPropertiesDefinition("Catégorie de Tag", "tagCategory", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), String.class, true, "F",
				getTagCategories(), 
				"single", 31, false, null, null)); // 06/09/2018 non editable
		
		return propertyDefinitions;
	}
	
	//FDS ajout 05/04/2018 NGL-1727
	private List<PropertyDefinition> getPropertyDefinitionsSmallRNASeqLibPrep() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		
		//OuputContainer
		// FDS 26/06/2019 NGL-2552 correction pcrCycleNumber => Integer.class
		propertyDefinitions.add(newPropertiesDefinition("Nbre Cycles PCR", "pcrCycleNumber", LevelService.getLevels(Level.CODE.ContainerOut), Integer.class, false, "F", null,
				null,null,null,
				"single", 29, true, "16", null)); 
		
		// proprietes de containerOut doivent etre propagees au content
		// il faut specifier l'état auquel les propriétés sont obligatoires: ici Finished (F) 
		propertyDefinitions.add(newPropertiesDefinition("Tag", "tag", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), String.class, true, "F", 
				getTagIllumina(), 
				"single", 30, true, null,null));
		
		propertyDefinitions.add(newPropertiesDefinition("Catégorie de Tag", "tagCategory", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), String.class, true, "F",
				getTagCategories(), 
				"single", 31, false, null, null)); // 06/09/2018 non editable
		
		
		return propertyDefinitions;
	}
	
	//FDS ajout 05/06/2018 NGL-1728
	private List<PropertyDefinition> getPropertyDefinitionsOxBisSeqLibPrep() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		MeasureCategoryDAO mcfind = MeasureCategory.find.get();
		MeasureUnitDAO     mufind = MeasureUnit.find.get();
		
		//NGL-2211=> plus globale mais expérience CONTAINER OUT (uniquement); position=à droite de type processus banque
		// FDS 26/06/2019 NGL-2552 correction pcrCycleNumber => Integer.class
		propertyDefinitions.add(newPropertiesDefinition("Nbre Cycles PCR", "pcrCycleNumber", LevelService.getLevels(Level.CODE.ContainerOut), Integer.class, true, "F", null,
				null,null,null,
				"single", 33, true, null, null)); 
		
		/* NGL-2211 supprimer
		//InputContainer
		propertyDefinitions.add(newPropertiesDefinition("Volume engagé","inputVolume", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, true, null, null,
				mcfind.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME), 
				mufind.findByCode("µL"), 
				mufind.findByCode("µL"), 
				"single",10,false,null,null));          // pas editable=> calcul automatique par NGL = volume IN / 2
		*/
		
		propertyDefinitions.add(newPropertiesDefinition("Qté. engagée", "inputQuantity", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, null,
				mcfind.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY), 
				mufind.findByCode("ng"), 
				mufind.findByCode("ng"),
				"single", 11, true, null, null)); // editable
		
		//OuputContainer
		// proprietes de containerOut doivent etre propagees au content
		propertyDefinitions.add(newPropertiesDefinition("Type processus banque", "libProcessTypeCode", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), String.class, true, "F",
				getOxBisSeqTypeCodeValues(),
				"single", 32, true, null, null));
		
		propertyDefinitions.add(newPropertiesDefinition("Tag", "tag", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), String.class, true, "F", 
				getTagIllumina(), 
				"single", 30, true, null, null));
		
		propertyDefinitions.add(newPropertiesDefinition("Catégorie de Tag", "tagCategory", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), String.class, true, "F", 
				getTagCategories(), 
				"single", 31, false, null, null)); // 06/09/2018 non editable
		
		return propertyDefinitions;
	}
	
	//FDS ajout 26/10/2018 NGL-1226 : lib-and-pcr
	private List<PropertyDefinition> getPropertyDefinitionsLibAndPcr() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		MeasureCategoryDAO mcfind = MeasureCategory.find.get();
		MeasureUnitDAO     mufind = MeasureUnit.find.get();
		
		//Input Container
		// retour formatiom 05/12/2018 ajout propriete  non obligatoire / editable
		propertyDefinitions.add(newPropertiesDefinition("Temps fragmentation", "fragDuration", LevelService.getLevels(Level.CODE.ContainerIn), String.class, false, null, null,
				null,null,null,
				"single", 12, true, null,null));
		
		// FDS 03/09/2019 NGL-2637 ajout Volume engagé non obligatoire / editable
		propertyDefinitions.add(newPropertiesDefinition("Volume engagé","inputVolume", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, false, null, null,
				mcfind.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME), 
				mufind.findByCode("µL"), 
				mufind.findByCode("µL"), 
				"single",13,true, null, null));
		
		//OuputContainer
		propertyDefinitions.add(newPropertiesDefinition("Tag", "tag", LevelService.getLevels(Level.CODE.ContainerOut, Level.CODE.Content), String.class, true, "F",
				getTagIllumina(), 
				"single", 30, true, null, null));
		
		propertyDefinitions.add(newPropertiesDefinition("Catégorie de Tag", "tagCategory", LevelService.getLevels(Level.CODE.ContainerOut, Level.CODE.Content), String.class, true, "F",
				getTagCategories(), 
				"single", 31, false, null, null)); // non editable
		
		//saisie libre number, non obligatoire; 
		// FDS 26/06/2019 NGL-2552 passe de niveau instrument/containerOut
		//propertyDefinitions.add(newPropertiesDefinition("Nbre Cycles PCR", "pcrCycleNumber", LevelService.getLevels(Level.CODE.ContainerOut), Integer.class, false, null, null,
		//		null,null,null,
		//		"single", 25, true, null, null)); 
		
		//liste, non obligatoire; NGL-1725 mise encoherence du code avec le label
		propertyDefinitions.add(newPropertiesDefinition("Type purification", "purificationType", LevelService.getLevels(Level.CODE.ContainerOut), String.class, false, null, 
				getPurificationTypeValues(),
				null, null, null,
				"single", 34, true, null, null));
		
		return propertyDefinitions;	
	}
	
	//FDS ajout 26/10/2018 NGL-1226 : frg-cdna-indexing
	private List<PropertyDefinition> getPropertyDefinitionsFrgCdnaIndexing() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		
		//Input Container
		// retour formatiom 05/12/2018 ajout propriete
		propertyDefinitions.add(newPropertiesDefinition("Temps fragmentation", "fragDuration", LevelService.getLevels(Level.CODE.ContainerIn), String.class, false, null, null,
				null,null,null,
				"single", 12, true, null, null));
		
		//OuputContainer	
		propertyDefinitions.add(newPropertiesDefinition("Tag", "tag", LevelService.getLevels(Level.CODE.ContainerOut, Level.CODE.Content), String.class, true, "F", 
				getTagIllumina(), 
				"single", 30, true, null, null));
		
		propertyDefinitions.add(newPropertiesDefinition("Catégorie de Tag", "tagCategory", LevelService.getLevels(Level.CODE.ContainerOut, Level.CODE.Content), String.class, true, "F", 
				getTagCategories(), 
				"single", 31, false, null, null)); // non editable
		
		//saisie libre du type 0,8 X ;
		propertyDefinitions.add(newPropertiesDefinition("Ratio billes", "AdnBeadVolumeRatio", LevelService.getLevels(Level.CODE.ContainerOut), String.class, true, "F", null, 
				null,null,null,
				"single", 33, true, null, null)); 
		
		return propertyDefinitions;	
	}
	
	//FDS ajout 26/11/2018 NGL-1226 : dev-pcr-amplification
	private List<PropertyDefinition> getPropertyDefinitionsDevPcrAmpli() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		
		//Output Container	
		//saisie libre number, non obligatoire;
		// FDS 26/06/2019 NGL-2552 passe de niveau instrument/containerOut
		//propertyDefinitions.add(newPropertiesDefinition("Nbre Cycles PCR", "pcrCycleNumber", LevelService.getLevels(Level.CODE.ContainerOut), Integer.class, false, null, null, 
		//		null,null,null,
		//		"single", 25, true, null, null)); 
		
		//optionnelle; éditable/liste; NGL-1725 mise en coherence code avec label
		propertyDefinitions.add(newPropertiesDefinition("Type purification", "purificationType", LevelService.getLevels(Level.CODE.ContainerOut), String.class, false, null, 
				getPurificationTypeValues(),
				null, null, null,
				"single", 34, true, null, null));
		
		return propertyDefinitions;	
	}
	
	//FDS ajout 30/04/2019 NGL-1725 : permeabilization-transposition-purification
	private List<PropertyDefinition> getPropertyDefinitionsPermTranspPurif() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		
		//input Container
		// ajout NGL-2607 "Nb cellules": Entier, optionnel, editable
		propertyDefinitions.add(newPropertiesDefinition("Nb cellules","inputNbCells", LevelService.getLevels(Level.CODE.ContainerIn), Integer.class, false, null, null,
				"single", 7, true, null, null));
		
		//OutputContainer
		// Le type généré est toujours le même=> mettre en default ( =>code DNA )
		propertyDefinitions.add(newPropertiesDefinition("Sample Type", "sampleTypeCode", LevelService.getLevels(Level.CODE.ContainerOut), String.class, true, "N", 
				// DescriptionFactory.newValues("DNA"),"single", 15, true, null, null));   // necessite convertOutputPropertiesToDatatableColumn dans le controleur javascript
				// Collections.singletonLis=> evite convertOutputPropertiesToDatatableColumn dans le controleur javascript
				Collections.singletonList(DescriptionFactory.newValue("DNA","ADN")),"single", 21, true, null, null)); // test avec editable false...=> marche pas
		
		// Pour le CNG le project du sample cree est le meme que le projet du sample parent (il n'y a pas de Map Parameter) (le project est mis a jour par drools)
		// obligatoire OUI, editable NON
		propertyDefinitions.add(newPropertiesDefinition("Projet", "projectCode", LevelService.getLevels(Level.CODE.ContainerOut), String.class, true, "N", null,
				"single", 22, false, null, null));
		
		// pour le CNG, sample code généré est éditable !
		propertyDefinitions.add(newPropertiesDefinition("Echantillon", "sampleCode", LevelService.getLevels(Level.CODE.ContainerOut), String.class, true, "N", null,
				"single", 25, true, null, null));
		
		// ajout NGL-2607, optionnelle niveau containerOut + content, editable
		propertyDefinitions.add(newPropertiesDefinition("Détails conditions expérience", "experimentDetails", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), String.class, false, null, null,
				"single", 50, true, null, null));
		
		return propertyDefinitions;	
	}
	
	//FDS ajout 30/04/2019 NGL-1725 : pcr-amplif-and-purif-atac-chip-seq
	private List<PropertyDefinition> getPropertyDefinitionsPCRAmpliPurifAtacChip () {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		
		//Experiment	
		   //rien
		
		//Output Container
		// optionnelle; éditable/liste; NGL-2607 passe de niveau niveau Experiment a ContainerOut (il faut une position) !!
		propertyDefinitions.add(newPropertiesDefinition("Méthode de purification", "purificationMethod", LevelService.getLevels( Level.CODE.ContainerOut),String.class, false, null,
				getPurificationMethodValues(),
				"single",30,true, null, null));
		
		// obligatoire; éditable/libre...utiliser un type string; NGL-2607 passe de niveau niveau Experiment a ContainerOut (il faut une position) !!
		// obligatoire etat Finished, default value 1.6X
		propertyDefinitions.add(newPropertiesDefinition("Ratio billes","AdnBeadVolumeRatio", LevelService.getLevels( Level.CODE.ContainerOut),String.class, true, "F",
				null, 
				null, null , null,
				"single", 31, true ,"1.6X", null));
		
		//optionnelle; éditable/libre...utiliser un type string; NGL-2607 passe de niveau niveau Experiment a ContainerOut (il faut une position) !!
		propertyDefinitions.add(newPropertiesDefinition("Ratio billes 2","AdnBeadVolumeRatio2", LevelService.getLevels( Level.CODE.ContainerOut),String.class, false, null,
				null, 
				null, null , null,
				"single", 32, true ,null, null));
		
		//Output Container + Content
		// obligatoire a Terminé ; éditable/liste
		propertyDefinitions.add(newPropertiesDefinition("Tag", "tag", LevelService.getLevels(Level.CODE.ContainerOut, Level.CODE.Content), String.class, true, "F", 
				getTagIllumina(), 
				"single", 40, true, null, null));
		
		// obligatoire a Terminé; non editable (auto positionné)
		propertyDefinitions.add(newPropertiesDefinition("Catégorie de Tag", "tagCategory", LevelService.getLevels(Level.CODE.ContainerOut, Level.CODE.Content), String.class, true, "F", 
				null, //getTagCategories(),  inutile car auto positionné corriger autres experiences !!!
				"single", 41, false, null, null));
		
		return propertyDefinitions;	
	}
	
	//FDS ajout 26/10/2018; correction du nom=>Type
	private static List<Value>getPurificationTypeValues() {
		List<Value> values = new ArrayList<>();
		
		values.add(DescriptionFactory.newValue("1X",   "1X"));
		values.add(DescriptionFactory.newValue("0,6X", "0,6X"));
		values.add(DescriptionFactory.newValue("0,8X", "0,8X"));
		values.add(DescriptionFactory.newValue("350 bp NEB", "350 bp NEB"));
		
		return values;
	}
	
	//FDS ajout 02/05/2019 NGL-1725
	private static List<Value>getPurificationMethodValues() {
		List<Value> values = new ArrayList<>();
		
		values.add(DescriptionFactory.newValue("Ampure",   "Ampure"));
		values.add(DescriptionFactory.newValue("Double sizing", "Double sizing"));
		
		return values;
	}
	
	// FDS ajout 18/07/2017 pour JIRA NGL-1201: processus capture
	// utilisé par processus getPropertyDefinitionsCapture ET getPropertyDefinitionsCapturePcrIndexing
	// !! code dupliqué dans ProcessServiceCNG
	private static List<Value>getCaptureBaitsValues() {
		List<Value> values = new ArrayList<>();
		
		values.add(DescriptionFactory.newValue("V5",    "V5"));
		values.add(DescriptionFactory.newValue("V5+UTR","V5+UTR"));
		values.add(DescriptionFactory.newValue("V6",    "V6"));
		values.add(DescriptionFactory.newValue("V6+UTR","V6+UTR"));  
		values.add(DescriptionFactory.newValue("V6+Cosmic","V6+Cosmic"));
		values.add(DescriptionFactory.newValue("V7","V7"));                       // ajout NGL-2186
		values.add(DescriptionFactory.newValue("custom","custom"));
		
		return values;
	}
	
	// FDS ajout 04/06/2018 pour NGL-1728 : processus OxBisSeq
	private static List<Value> getOxBisSeqTypeCodeValues(){
		List<Value> values = new ArrayList<>();
		
		// Liste evolutive !!!!
		values.add(DescriptionFactory.newValue("FHO","FHO - oxBisSeq_O"));
		values.add(DescriptionFactory.newValue("FHB","FHB - oxBisSeq_B"));
		values.add(DescriptionFactory.newValue("FH","FH - oxBisSeq"));     // 24/08/2018 NGL-2216: laisser le FH d'origine
		
		return values;
	}
	
}