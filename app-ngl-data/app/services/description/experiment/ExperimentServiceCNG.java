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
import java.util.stream.Collectors;

import models.laboratory.common.description.Institute;
import models.laboratory.common.description.Level;
import models.laboratory.common.description.MeasureCategory;
import models.laboratory.common.description.MeasureUnit;
import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.common.description.Value;
import models.laboratory.experiment.description.ExperimentCategory;
import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.experiment.description.ProtocolCategory;
import models.laboratory.processes.description.ExperimentTypeNode;
import models.utils.dao.DAOException;
import models.utils.dao.DAOHelpers;
import play.data.validation.ValidationError;
import services.description.Constants;
import services.description.DescriptionFactory;
import services.description.common.LevelService;
import services.description.common.MeasureService;
import services.description.declaration.cng.Nanopore;

public class ExperimentServiceCNG extends AbstractExperimentService{
	
	// @SuppressWarnings("unchecked")
	@Override
	public void saveProtocolCategories(Map<String, List<ValidationError>> errors) throws DAOException {
		List<ProtocolCategory> l = new ArrayList<>();
		l.add(DescriptionFactory.newSimpleCategory(ProtocolCategory.class, "Developpement", "development"));
		l.add(DescriptionFactory.newSimpleCategory(ProtocolCategory.class, "Production", "production"));
		DAOHelpers.saveModels(ProtocolCategory.class, l, errors);
	}
	
	/**
	 * Save all Experiment Categories.
	 * @param errors        error mamanger
	 * @throws DAOException DAO problem
	 */
	@Override
	public  void saveExperimentCategories(Map<String,List<ValidationError>> errors) throws DAOException{
		List<ExperimentCategory> l = new ArrayList<>();
		
		l.add(DescriptionFactory.newSimpleCategory(ExperimentCategory.class, "Purification", ExperimentCategory.CODE.purification.name()));
		l.add(DescriptionFactory.newSimpleCategory(ExperimentCategory.class, "Control qualité", ExperimentCategory.CODE.qualitycontrol.name()));
		l.add(DescriptionFactory.newSimpleCategory(ExperimentCategory.class, "Transfert", ExperimentCategory.CODE.transfert.name()));
		l.add(DescriptionFactory.newSimpleCategory(ExperimentCategory.class, "Transformation", ExperimentCategory.CODE.transformation.name()));
		l.add(DescriptionFactory.newSimpleCategory(ExperimentCategory.class, "Void process", ExperimentCategory.CODE.voidprocess.name()));
		
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
			
			/** voidprocess: ext-to-**  display order -1 **/
			
			l.add(newExperimentType("Ext to prepa flowcell","ext-to-prepa-flowcell",null,-1,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), 
					null, 
					null,
					"OneToOne", 
					CNG));
			
			l.add(newExperimentType("Ext to prepa flowcell ordered","ext-to-prepa-fc-ordered",null,-1,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), 
					null, 
					null,
					"OneToOne", 
					CNG));
			
			l.add(newExperimentType("Ext to librairie dénaturée","ext-to-denat-dil-lib",null,-1,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), 
					null, 
					null,
					"OneToOne", 
					CNG));
			
			l.add(newExperimentType("Ext to X5_WG PCR free","ext-to-x5-wg-pcr-free",null,-1,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()),
					null, 
					null ,
					"OneToOne", 
					CNG));
			
			l.add(newExperimentType("Ext to X5_norm,FC ord, dépôt","ext-to-norm-fc-ordered-depot",null,-1,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()),
					null, 
					null ,
					"OneToOne", 
					CNG));
			
			//FDS 10/08/2016 ajout -- JIRA NGL-1047: processus X5_WG NANO;
			l.add(newExperimentType("Ext to X5_WG NANO","ext-to-x5-wg-nano",null,-1,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()),
					null, 
					null ,
					"OneToOne", 
					CNG));		
			
			//FDS 12/12/2016 ajout -- JIRA NGL-1025: processus et experiments pour RNASeq ; JIRA NGL-1259 renommage rna-sequencing=> rna-lib-process
			l.add(newExperimentType("Ext to Prep lib RNASeq","ext-to-rna-lib-process",null,-1,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()),
					null, 
					null ,
					"OneToOne", 
					CNG));
			
			/*24/10/2017   A  SUPPRIMER remplacé par ext-to-norm-fc-ordered-depot
			//FDS 12/12/2016 ajout -- JIRA NGL-1025: processus et experiments pour RNASeq 
			l.add(newExperimentType("Ext to norm+pool,FC ord, dépôt","ext-to-norm-and-pool-fc-ord-depot",null,-1,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()),
					null, 
					null ,
					"OneToOne", 
					CNG));
			*/
				
			//FDS ajout 12/12/2016 JIRA NGL-1025: nouveau processus court pour RNAseq
			l.add(newExperimentType("Ext to norm+pool, dénat, FC, dépôt","ext-to-norm-and-pool-denat-fc-depot",null,-1,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()),
					null, 
					null ,
					"OneToOne", 
					CNG));	

			//FDS 12/12/2016 JIRA NGL-1164 : pour processus sans transformation
			l.add(newExperimentType("Ext to QC / TF / purif","ext-to-qc-transfert-purif",null,-1,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()),
					null, 
					null,
					"OneToOne", 
					CNG));
			
			//FDS 10/10/2017 JIRA NGL-1625 dedoubler
			l.add(newExperimentType("Ext to  TF / QC / purif","ext-to-transfert-qc-purif",null,-1,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()),
					null, 
					null,
					"OneToOne", 
					CNG));
			
			
			//FDS ajout 21/02/2017 NGL-1167: processus Chromium
			l.add(newExperimentType("Ext to Prep Chromium WG","ext-to-wg-chromium-lib-process",null,-1,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()),
					null, 
					null,
					"OneToOne", 
					CNG));	

			//FDS ajout 10/07/2017 NGL 1201: processus Capture principal (4000/X5 = FC ordonnée)
			l.add(newExperimentType("Ext to Prep Capture","ext-to-capture-prep-process-fc-ord",null,-1,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()),
					null, 
					null,
					"OneToOne", 
					CNG));
			
			//FDS ajout 10/07/2017 NGL-1201: processus Capture principal (2000/2500/Miseq/NextSeq)
			l.add(newExperimentType("Ext to Prep Capture","ext-to-capture-prep-process-fc",null,-1,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()),
					null, 
					null,
					"OneToOne", 
					CNG));		
			
			//FDS ajout 06/07/2017 NGL 1201: processus Capture reprise (1)(4000/X5 = FC ordonnée)
			l.add(newExperimentType("Ext to Prep. Capture à partir sample prep sauvgarde","ext-to-pcr-capture-pcr-indexing-fc-ord",null,-1,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()),
					null, 
					null,
					"OneToOne", 
					CNG));
			
			//FDS ajout 06/07/2017 NGL-1201: processus Capture reprise (1)(2000/2500/Miseq/NextSeq)
			l.add(newExperimentType("Ext to Prep. Capture à partir sample prep sauvgarde","ext-to-pcr-capture-pcr-indexing-fc",null,-1,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()),
					null, 
					null,
					"OneToOne", 
					CNG));		
			
			//FDS ajout 06/07/2017 NGL 1201: processus Capture reprise (2)(4000/X5 = FC ordonnée)
			l.add(newExperimentType("Ext to Prep. Capture à partir pré Capture","ext-to-capture-pcr-indexing-fc-ord",null,-1,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()),
					null, 
					null,
					"OneToOne", 
					CNG));
			
			//FDS ajout 06/07/2017 NGL-1201: processus Capture reprise (2)(2000/2500/Miseq/NextSeq)
			l.add(newExperimentType("Ext to Prep. Capture à partir pré Capture","ext-to-capture-pcr-indexing-fc",null,-1,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()),
					null, 
					null,
					"OneToOne", 
					CNG));
			
			//FDS ajout 06/07/2017 NG-1201: processus Capture reprise (3)(4000/X5 = FC ordonnée)
			l.add(newExperimentType("Ext to PCR indexing à partir capture sauvgarde","ext-to-pcr-indexing-process-fc-ord",null,-1,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()),
					null, 
					null,
					"OneToOne", 
					CNG));
			
			//FDS ajout 06/07/2017 NGL-1201: processus Capture reprise (3)(2000/2500/Miseq/NextSeq)
			l.add(newExperimentType("Ext to PCR indexing à partir capture sauvgarde","ext-to-pcr-indexing-process-fc",null,-1,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()),
					null, 
					null,
					"OneToOne", 
					CNG));	
			
			//FDS ajout 04/04/2018  NGL-1727: processus SmallRNASeq
			l.add(newExperimentType("Ext to Small RNASeq","ext-to-small-rna-seq-process",null,-1,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()),
					null, 
					null,
					"OneToOne", 
					CNG));	
			
			//FDS ajout 04/04/2018  NGL-1727: processus BisSeq
			l.add(newExperimentType("Ext to BiSeq","ext-to-bis-seq-process-fc-ord",null,-1,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()),
					null, 
					null,
					"OneToOne", 
					CNG));	
			
			
			/** Transformation, ordered by display order **/
			
			//FDS 01/02/2016 ajout -- JIRA NGL-894: experiments pour X5
			l.add(newExperimentType("Prep. PCR free","prep-pcr-free",null,500,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()),
					getPropertyDefinitionsPrepPcrFree(), 
					getInstrumentUsedTypes(//"covaris-e210-and-sciclone-ngsx", FDS 18/07/2017 plus utilisé (inactivéé dans instrumentService...)
							               "covaris-le220-and-sciclone-ngsx",
							               "covaris-e220-and-sciclone-ngsx"),
					"OneToOne", 
					CNG));
			
		    //FDS dupliquer experience prep-pcr-free en prep-wg-nano; separer les proprietes de celles de prep-pcr-free ...
		    l.add(newExperimentType("Prep. WG Nano","prep-wg-nano",null,550,
				   ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()),
				   getPropertyDefinitionsPrepWgNano(), 
				   getInstrumentUsedTypes("covaris-le220-and-sciclone-ngsx",
						                  "covaris-e220-and-sciclone-ngsx"),
				   "OneToOne", 
				   CNG));
		    
			//FDS 12/12/2016 ajout -- JIRA NGL-1025: processus et experiments pour RNASeq; JIRA NGL-1047: processus X5_WG NANO 	
			l.add(newExperimentType("Prep. Librairie (sans frg)","library-prep",null,600,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()),
					getPropertyDefinitionsLibraryPrep(),
					getInstrumentUsedTypes("sciclone-ngsx","hand"), // 29/11/2017 NGL-1717 ajout main
					"OneToOne", 
					CNG));
			
			/* OOps trop tot... pas pour la 2.1.2 attendre
			//FDS 06/04/2018 ajout JIRA NGL-1727: pour processus SmallRNASeq
			l.add(newExperimentType("Small RNAseq lib prep","small-rnaseq-lib-prep",null,650,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()),
					getPropertyDefinitionsSmallRNASeqLibPrep(),
					getInstrumentUsedTypes("sciclone-ngsx-and-zephyr","tecan-evo-150-and-zephyr","hand"), 
					"OneToOne", 
					CNG));
			
			//FDS 04/04/2018 ajout JIRA NGL-1996: pour processus BisSeq
			l.add(newExperimentType("BisSeq lib prep","bisseq-lib-prep",null,660,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()),
					getPropertyDefinitionsBisSeqLibPrep(),  //TODO
					getInstrumentUsedTypes("sciclone-ngsx-and-zephyr","tecan-evo-150-and-zephyr","hand"), // tecan-evo-150 seul ????
					"OneToOne", 
					CNG));
			*/
	
			//FDS mise prod 01/09/2016
			l.add(newExperimentType("PCR+purification","pcr-and-purification",null,700,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()),
					getPropertyDefinitionsPcrAndPurification(),
					getInstrumentUsedTypes("mastercycler-epg-and-zephyr",
							               "mastercycler-epg-and-bravows",
							               "mastercycler-ep-gradient",        // 29/11/2017 NGL-1717 ajout 
							               "mastercycler-nexus-and-bravows"), // 22/02/2018 NGL-1860 ajout
					"OneToOne", 
					CNG));
		
			l.add(newExperimentType("Normalisation+Pooling","normalization-and-pooling",null,800,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()),
					getPropertyDefinitionsNormalizationAndPooling(), 
					getInstrumentUsedTypes("hand","janus","epmotion"),
					"ManyToOne", 
					CNG));	
		
			l.add(newExperimentType("Librairie normalisée","lib-normalization",null,900,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()), 
					getPropertyDefinitionsLibNormalization(),
					getInstrumentUsedTypes("hand","janus"), 
					"OneToOne", 
					CNG));				
			
			// 04/10/2017 NGL-1589: plaque->plaque, tubes->plaque, plaque-> tube, tube->tube => utiliser robot
			l.add(newExperimentType("Dénaturation-dilution","denat-dil-lib",null,1000,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()), 
					getPropertyDefinitionsDenatDilLibCNG(),
					getInstrumentUsedTypes("hand","epmotion"),   // 16/10/2017  remplacer janus par EpMotion
					"OneToOne", 
					CNG));
			
			l.add(newExperimentType("Préparation flowcell","prepa-flowcell",null,1200,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()), 
					getPropertyDefinitionsPrepaflowcellCNG(),
					getInstrumentUsedTypes("cBotV2","cBot-onboard"),
					"ManyToOne", 
					CNG));
			
			//FDS modif 23/01/2017 modif janus-and-cBot=>  janus-and-cBotV2, il n'y a plus de Cbot non V2...
			//FDS 07/12/2017  NGL-1730 ajout "cBot-onboard" pour NovaSeq6000
			l.add(newExperimentType("Prép. flowcell ordonnée","prepa-fc-ordered",null,1300,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()), 
					getPropertyDefinitionsPrepaflowcellOrderedCNG(),
					getInstrumentUsedTypes("cBotV2","janus-and-cBotV2","cBot-onboard"),
					"ManyToOne", 
					CNG));
	
			//FDS 28/10/2015 : ajout "HISEQ4000","HISEQX"
			//FDS 07/12/2017 NGL-1730: ajout NOVASEQ6000
			l.add(newExperimentType("Dépôt sur séquenceur","illumina-depot",null, 1400,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()),
					getPropertyDefinitionsIlluminaDepot(),
					getInstrumentUsedTypes("MISEQ","HISEQ2000","HISEQ2500","NEXTSEQ500","HISEQ4000","HISEQX","NOVASEQ6000"), 
					"OneToVoid", 
					CNG));			
			
			//FDS ajout 21/02/2017 NGL-1167: Chromium		
			l.add(newExperimentType("GEM generation (Chromium)","chromium-gem-generation",null,1500,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()), 
					getPropertyDefinitionsChromiumGemGeneration(),
					getInstrumentUsedTypes("chromium-controller"),
					"OneToOne", 
					CNG));
			
			// 13/03/2017 ne pas encore proposer le Sciclone...getInstrumentUsedTypes("hand","sciclone-ngsx"), 
			l.add(newExperimentType("Prep Lib & PCR indexing (Chromium)","wg-chromium-lib-prep",null,1600,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()), 
					getPropertyDefinitionsWGChromiumLibPrep(),
					getInstrumentUsedTypes("hand"), 
					"OneToOne", 
					CNG));	

			//FDS 10/07/2017 NGL-1201: experiences transformation pour Capture
			l.add(newExperimentType("Fragmentation","fragmentation",null,650,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()), 
					getPropertyDefinitionsFragmentation(),
					getInstrumentUsedTypes("covaris-e220-and-sciclone-ngsx","covaris-le220-and-sciclone-ngsx",
							               "covaris-e220","covaris-le220",                                 // ajoutés 29/08/2017
							               "covaris-e220-and-bravows","covaris-le220-and-bravows"),        // ajoutés 16/11/2017						               
					"OneToOne", 
					CNG));
			
			//FDS 10/07/2017 NGL-1201: experiences transformation pour Capture (Sure Select implicite)
			//    09/11/2017 NGL-1691: ajout type Bravo WorkStation
			l.add(newExperimentType("Sample prep (pré-capture)","sample-prep",null,660,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()), 
					getPropertyDefinitionsSamplePrepCapture(),
					getInstrumentUsedTypes("sciclone-ngsx","bravo-workstation"),
					"OneToMany",
					CNG));
						
			//FDS 10/07/2017 NGL-1201: experiences transformation pour Capture (Sure Select implicite)
			//    09/11/2017 NGL-1691: renommage label ( ajout wash) ; 
			//    16/11/2017 NGL-1691: renommage label ( ajout Hybridation)
			l.add(newExperimentType("Hybridation, capture & wash (post)","capture",null,710,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()), 
					getPropertyDefinitionsCapture(),
					getInstrumentUsedTypes("bravo-workstation",
							               "bravows-and-mastercycler-epg",     //15/11/2017 ajout "bravows-and-mastercycler-epg"
										   "bravows-and-mastercycler-nexus"),  // 22/02/2018 NGL-1860: ajout
					"OneToOne",
					CNG));
			
			//FDS 10/07/2017 NGL-1201: experiences transformation pour Capture (Sure Select implicite)
			l.add(newExperimentType("PCR+indexing (post-capture)","pcr-and-indexing",null,720,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()), 
					getPropertyDefinitionsPcrIndexing(),
					getInstrumentUsedTypes("mastercycler-nexus-and-bravows"),
							               // "mastercycler-epg-and-bravows"),  22/02/2018 NGL-1860 supression
					"OneToOne",
					CNG));	
			
			
			/** Quality Control, ordered by display order **/
            //NOTE: pas de Node a creer pour experiences type qualitycontrol
			//17/10/2017: sauf si existe  un processus commencant par exp type qualitycontrol...
			
			// FDS 07/04/2016 ajout --JIRA NGL-894: experiments pour X5
			// 22/09/2016 modification du name => suppression profil mais garder dans le code a cause existant ds base de données
			l.add(newExperimentType("LABCHIP_GX","labchip-migration-profile", null, 10,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.qualitycontrol.name()), 
					getPropertyDefinitionsChipMigration(), 
					getInstrumentUsedTypes("labChipGX"),
					"OneToVoid", 
					CNG));
					
			//FDS 01/02/2016 ajout -- JIRA NGL-894: experiments pour X5
			l.add(newExperimentType("Quantification qPCR","qpcr-quantification", null, 20,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.qualitycontrol.name()), 
					getPropertyDefinitionsQPCR(), 
					getInstrumentUsedTypes("qpcr-lightcycler-480II"),
					"OneToVoid", 
					CNG)); 
			
			l.add(newExperimentType("QC Miseq","miseq-qc", null, 30,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.qualitycontrol.name()), 
					getPropertyDefinitionsQCMiseq(), 
					getInstrumentUsedTypes("MISEQ-QC-MODE"),
					"OneToVoid", 
					CNG));
			
			//FDS 21/02/2017 ajout -- JIRA NGL-1167: experiments pour Chromium
			l.add(newExperimentType("Bioanalyzer","bioanalyzer-migration-profile", null, 40,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.qualitycontrol.name()), 
					getPropertyDefinitionsBioanalyzer(), 
					getInstrumentUsedTypes("agilent-2100-bioanalyzer"),
					"OneToVoid", 
					CNG));
			
			//GA pour import; non affichée
			l.add(newExperimentType("QC Bank","bank-qc", null, null,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.qualitycontrol.name()), 
					getPropertyDefinitionsBankQC(), 
					getInstrumentUsedTypes("hand"),
					"OneToVoid", false,
					CNG));	

		    // FDS 21/06/2017 ajout -- JIRA NGL-1472: necessiter d'ajouter QC provenant de collaborateur extérieur; non listée
		    l.add(newExperimentType("QC Exterieur","external-qc", null, null,
				    ExperimentCategory.find.findByCode(ExperimentCategory.CODE.qualitycontrol.name()),
				    getPropertyDefinitionsExternalQC(),
				    getInstrumentUsedTypes("hand"),
				    "OneToVoid", false,
				    CNG));
	    
			//FDS 27/07/2017 ajout NGL-1201: qc pour process Capture
			l.add(newExperimentType("Dosage Fluo","fluo-quantification", null, 50,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.qualitycontrol.name()), 
					getPropertyDefinitionsQuantIt(), 
					getInstrumentUsedTypes("spectramax",
							               "qubit"),  // NGL-1720: ajout qubit
					"OneToVoid",
					CNG));	
			
			/** Purification, ordered by display order **/
                            /*vide*/
			
			/** Transfert, ordered by display order **/
			// NOTE: pas de Node a creer pour experiences type transfert
			// 17/10/2017: sauf si existe un processus commencant par experiences type transfert...
			
			l.add(newExperimentType("Aliquot","aliquoting",null, 10300,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transfert.name()),
					getPropertyAliquoting(), 
					getInstrumentUsedTypes("hand"),
					"OneToMany", 
					CNG));
			
			// FDS 10/08/2016 NGL-1029;  05/10/2016 ajout EpMotion; 26/10/2016 renommage en "Pool"
			l.add(newExperimentType("Pool","pool",null,10400,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transfert.name()), 
					getPropertyDefinitionPool(),
					getInstrumentUsedTypes("hand","janus","epmotion"),
					"ManyToOne", 
					CNG));
			
			// FDS ajout 19/07/2017 NGL-1519: dupliquer "lib-normalization" en experience de type transfert=> meme proprietes	
			l.add(newExperimentType("Normalisation (supplémentaire)","additional-normalization",null,10500,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transfert.name()), 
					getPropertyDefinitionsLibNormalization(),
					getInstrumentUsedTypes("hand","janus"), 
					"OneToOne", 
					CNG));	
			
            // FDS 27/03/2017 renommage "Tubes" en "Tubes ou Strips"
			l.add(newExperimentType("Tubes ou Strips -> Plaque","tubes-to-plate",null,10600,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transfert.name()), null,
					getInstrumentUsedTypes("hand"),
					"OneToOne", 
					CNG));
			
			l.add(newExperimentType("Plaque -> Tubes","plate-to-tubes",null,10700,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transfert.name()), null,
					getInstrumentUsedTypes("hand"),
					"OneToOne", 
					CNG));
			
			l.add(newExperimentType("Plaques -> Plaque","plates-to-plate",null,10800,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transfert.name()), null,
					getInstrumentUsedTypes("hand"),
					"OneToOne", 
					CNG));
			
			// FDS renommage "Tubes ou Plaques" en "Tubes + Plaques
			l.add(newExperimentType("Tubes + Plaques -> Plaque","x-to-plate",null,10900,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transfert.name()), null,
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
		
		newExperimentTypeNode("ext-to-prepa-flowcell", getExperimentTypes("ext-to-prepa-flowcell").get(0), 
				false, false, false, 
				null, // no previous nodes
				null, 
				null, 
				null
				).save();
		
		newExperimentTypeNode("ext-to-prepa-fc-ordered", getExperimentTypes("ext-to-prepa-fc-ordered").get(0), 
				false, false, false, 
				null, // no previous nodes
				null, 
				null, 
				null
				).save();
		
		newExperimentTypeNode("ext-to-denat-dil-lib", getExperimentTypes("ext-to-denat-dil-lib").get(0),
				false, false, false,
				null, // no previous nodes
				null, 
				null,
				null
				).save();
		
		//FDS ajout 01/02/2016 -- JIRA NGL-894 : processus et experiments pour X5
		newExperimentTypeNode("ext-to-x5-wg-pcr-free",getExperimentTypes("ext-to-x5-wg-pcr-free").get(0),
				false,false,false,
				null, // no previous nodes
				null, 
				null, 
				null
				).save();
		
		//FDS ajout 15/04/2016 -- JIRA NGL-894 : processus court pour X5
		newExperimentTypeNode("ext-to-norm-fc-ordered-depot",getExperimentTypes("ext-to-norm-fc-ordered-depot").get(0),
				false,false,false,
				null, // no previous nodes
				null, 
				null, 
				null
				).save();	
		
		// FDS 10/08/2016 JIRA NGL-1047: X5_WG NANO; mise en prod 01/09/2016
		newExperimentTypeNode("ext-to-x5-wg-nano",getExperimentTypes("ext-to-x5-wg-nano").get(0),
				false,false,false,
				null, // no previous nodes
				null,
				null,
				null
				).save();

		/* 24/10/2017 A SUPPRIME remplacé par ext-to-norm-fc-ordered-depot
		//FDS ajout 12/12/2016 -- JIRA NGL-1025 RNA_Seq; processus long
		newExperimentTypeNode("ext-to-norm-and-pool-fc-ord-depot",getExperimentTypes("ext-to-norm-and-pool-fc-ord-depot").get(0),
				false,false,false,
				null, // no previous nodes
				null,
				null,
				null
				).save();	
		*/
		
		// FDS  ajout 06/10/2017
		newExperimentTypeNode("ext-to-norm-and-pool-denat-fc-depot",getExperimentTypes("ext-to-norm-and-pool-denat-fc-depot").get(0),
				false,false,false,
				null, // no previous nodes
				null,
				null,
				null
				).save();
		
		//FDS ajout 12/12/2016 -- JIRA NGL-1025 RNA_Seq; processus court
		newExperimentTypeNode("ext-to-rna-lib-process",getExperimentTypes("ext-to-rna-lib-process").get(0),
				false,false,false,
				null,  // no previous nodes
				null,
				null,
				null
				).save();	
		
		//FDS ajout 12/12/2016 JIRA NGL-1164
		newExperimentTypeNode("ext-to-qc-transfert-purif", getExperimentTypes("ext-to-qc-transfert-purif").get(0), 
				false, false, false, 
				null, // no previous nodes
				null,
				null,
				null
				).save();	
		
		// FDS ajout 10/10/2017 JIRA NGL-1625
		newExperimentTypeNode("ext-to-transfert-qc-purif", getExperimentTypes("ext-to-transfert-qc-purif").get(0), 
				false, false, false, 
				null, // no previous nodes
				null,
				null,
				null
				).save();
		
			
		//FDS ajout 20/02/2017 JIRA NGL-1167
		newExperimentTypeNode("ext-to-wg-chromium-lib-process", getExperimentTypes("ext-to-wg-chromium-lib-process").get(0), 
				false, false, false, 
				null, // no previous nodes
				null,
				null,
				null
				).save();	
			
		//FDS ajout 10/07/2017 NGL-1201: processus capture
		newExperimentTypeNode("ext-to-capture-prep-process-fc-ord", getExperimentTypes("ext-to-capture-prep-process-fc-ord").get(0), 
				false, false, false, 
				null, // no previous nodes
				null,
				null,
				null
				).save();
		
		//FDS ajout 10/07/2017 NGL-1201: processus capture
		newExperimentTypeNode("ext-to-capture-prep-process-fc", getExperimentTypes("ext-to-capture-prep-process-fc").get(0), 
				false, false, false, 
				null, // no previous nodes
				null,
				null,
				null
				).save();
		
		//FDS ajout 10/07/2017 NGL-1201: processus capture reprise (1)
		newExperimentTypeNode("ext-to-pcr-capture-pcr-indexing-fc-ord", getExperimentTypes("ext-to-pcr-capture-pcr-indexing-fc-ord").get(0), 
				false, false, false, 
				null, // no previous nodes
				null,
				null,
				null
				).save();
		
		//FDS ajout 10/07/2017 NGL-1201: processus capture reprise (1)
		newExperimentTypeNode("ext-to-pcr-capture-pcr-indexing-fc", getExperimentTypes("ext-to-pcr-capture-pcr-indexing-fc").get(0), 
				false, false, false, 
				null, // no previous nodes
				null,
				null,
				null
				).save();
			
		//FDS ajout 10/07/2017 NGL-1201: processus capture reprise (2)
		newExperimentTypeNode("ext-to-capture-pcr-indexing-fc-ord", getExperimentTypes("ext-to-capture-pcr-indexing-fc-ord").get(0), 
				false, false, false, 
				null, // no previous nodes
				null,
				null,
				null
				).save();
		
		//FDS ajout 10/07/2017 NGL-1201: processus capture reprise (2)
		newExperimentTypeNode("ext-to-capture-pcr-indexing-fc", getExperimentTypes("ext-to-capture-pcr-indexing-fc").get(0), 
				false, false, false, 
				null, // no previous nodes
				null,
				null,
				null
				).save();
		
		//FDS ajout 10/07/2017 NGL-1201: processus capture reprise (3)
		newExperimentTypeNode("ext-to-pcr-indexing-process-fc-ord", getExperimentTypes("ext-to-pcr-indexing-process-fc-ord").get(0), 
				false, false, false, 
				null, // no previous nodes
				null,
				null,
				null
				).save();
		
		//FDS ajout 10/07/2017 NGL-1201: processus capture reprise (3)
		newExperimentTypeNode("ext-to-pcr-indexing-process-fc", getExperimentTypes("ext-to-pcr-indexing-process-fc").get(0), 
				false, false, false, 
				null, // no previous nodes
				null,
				null,
				null
				).save();		
		
		/* OOps trop tot... pas pour la 2.1.2 attendre
		//FDS ajout 04/04/2018 NGL-1727: processus SmallRNASeq
		newExperimentTypeNode("ext-to-small-rna-seq-process", getExperimentTypes("ext-to-small-rna-seq-process").get(0), 
				false, false, false, 
				null, // no previous nodes
				null,
				null,
				null
				).save();	
				
		//FDS ajout 04/04/2018 NGL-1996: processus BisRNASeq
		newExperimentTypeNode("ext-to-bis-seq-process-fc-ord", getExperimentTypes("ext-to-bis-seq-process-fc-ord").get(0), 
				false, false, false, 
				null, // no previous nodes
				null,
				null,
				null
				).save();	
		*/
		
		
				
		/** other nodes **/
		
		newExperimentTypeNode("prep-pcr-free",getExperimentTypes("prep-pcr-free").get(0),
				false,false,false,
				getExperimentTypeNodes("ext-to-x5-wg-pcr-free"), // previous nodes
				null, // pas de purif
				getExperimentTypes("qpcr-quantification",
						           "labchip-migration-profile",
						           "miseq-qc"), // qc; est-ce necessaire de les lister ? ou un seul suffit ?
				getExperimentTypes("aliquoting")  // transfert
				).save();	
		
		newExperimentTypeNode("prep-wg-nano",getExperimentTypes("prep-wg-nano").get(0),
				false,false,false,
				getExperimentTypeNodes("ext-to-x5-wg-nano"), // previous nodes
				null, // pas de purif
				null, // pas de qc 
				getExperimentTypes("aliquoting")  // transfert
				).save();	

		newExperimentTypeNode("library-prep",getExperimentTypes("library-prep").get(0),
				true,false,false,
				getExperimentTypeNodes("ext-to-rna-lib-process"), // previous nodes
				null, // pas de purif
				null, // pas de qc
				null  // pas de transfert 
				).save();
				     		
	    //Les nodes pour process Capture doivent obligatoirement etre crees AVANT
		//FDS ajout 11/07/2017 NGL-1201: processus capture
		newExperimentTypeNode("fragmentation",getExperimentTypes("fragmentation").get(0),
				false,false,false,
				getExperimentTypeNodes("ext-to-capture-prep-process-fc",
						               "ext-to-capture-prep-process-fc-ord"), // previous nodes
				null, // pas de purif
				getExperimentTypes("bioanalyzer-migration-profile",
						           "labchip-migration-profile"),   // qc ; est-ce necessaire de les lister ? ou un seul suffit ?
				null  // pas transfert
				).save();
		
		//FDS ajout 11/07/2017 NGL-1201: processus capture
		newExperimentTypeNode("sample-prep",getExperimentTypes("sample-prep").get(0),
				false,false,false,
				getExperimentTypeNodes("fragmentation"), // previous nodes
				null, // pas de purif
				null, // pas qc
				null  // pas transfert
				).save();
			
		//FDS commun WG_NANO et RNAseq; 11/07/2017 NGL-1201: commun aussi aux processus Capture
		newExperimentTypeNode("pcr-and-purification",getExperimentTypes("pcr-and-purification").get(0),
				true,false,false,
				getExperimentTypeNodes("library-prep",
						               "prep-wg-nano",
						               "sample-prep",
						               "ext-to-pcr-capture-pcr-indexing-fc",
						               "ext-to-pcr-capture-pcr-indexing-fc-ord"), // previous nodes
				null, // pas de purif
				getExperimentTypes("labchip-migration-profile",
				                   "fluo-quantification"),            // qc; ajout "fluo-quantification"  pour process Capture ( UTILE ??)
				null  // pas de transfert
				).save();

		//FDS ajout 11/07/2017 NGL-1201: processus capture
		newExperimentTypeNode("capture",getExperimentTypes("capture").get(0),
				false,false,false,
				getExperimentTypeNodes("pcr-and-purification",
									   "ext-to-capture-pcr-indexing-fc", 
									   "ext-to-capture-pcr-indexing-fc-ord"),// previous nodes
				null, // pas de purif
				null, // pas qc
				null  // pas tranfert
				).save();
	
		//FDS ajout 11/07/2017 NGL-1201: processus capture
		newExperimentTypeNode("pcr-and-indexing",getExperimentTypes("pcr-and-indexing").get(0),
				false,false,false,
				getExperimentTypeNodes("capture",
									   "ext-to-pcr-indexing-process-fc",
									   "ext-to-pcr-indexing-process-fc-ord"), // previous nodes
				null, // pas de purif
				getExperimentTypes("bioanalyzer-migration-profile",
						           "labchip-migration-profile"),    // qc; est-ce necessaire de les lister ? ou un seul suffit ?
				null  // pas tranfert
				).save();
			
		//FDS ajout 21/02/2017 NGL-1167: processus Chromium
		newExperimentTypeNode("chromium-gem-generation",getExperimentTypes("chromium-gem-generation").get(0),
				false,false,false,
				getExperimentTypeNodes("ext-to-wg-chromium-lib-process"), // previous nodes
				null, // pas de purif
				getExperimentTypes("bioanalyzer-migration-profile"), // qc 
				getExperimentTypes("tubes-to-plate") // transfert 
				).save();
		
		//FDS ajout 21/02/2017 NGL-1167: processus Chromium
		newExperimentTypeNode("wg-chromium-lib-prep",getExperimentTypes("wg-chromium-lib-prep").get(0),
				false,false,false,
				getExperimentTypeNodes("chromium-gem-generation"), // previous nodes
				null, // pas purif
				getExperimentTypes("labchip-migration-profile"), // qc; un seul suffit meme s'il y en a plusieurs possibles
				getExperimentTypes("tubes-to-plate") // transfert 
				).save();		
		
		/* OOps trop tot... pas pour la 2.1.2 attendre
		// FDS ajout 04/04/2018 NGL-1996
		newExperimentTypeNode("bisseq-lib-prep",getExperimentTypes("bisseq-lib-prep").get(0),
				false, false,false,
				getExperimentTypeNodes("ext-to-bis-seq-process-fc-ord"),
				null, // pas de purif
				null, // pas de qc ?????????????????????????
				null  // pas de transfert
				).save();
			
		// FDS ajout 04/04/2018 NGL-1727
		newExperimentTypeNode("small-rnaseq-lib-prep",getExperimentTypes("small-rnaseq-lib-prep").get(0),
				false, false,false,
				getExperimentTypeNodes("ext-to-small-rna-seq-process"),
				null, // pas de purif
				getExperimentTypes("labchip-migration-profile"), // qc; un seul suffit meme s'il y en a plusieurs possibles
				null  // pas de transfert
				).save();
		*/	

		//FDS 24/10/2017 remplacer ext-to-norm-and-pool-fc-ord-depot  par  ext-to-norm-fc-ordered-depot
		newExperimentTypeNode("normalization-and-pooling",getExperimentTypes("normalization-and-pooling").get(0),
				false,false,false,
				getExperimentTypeNodes("ext-to-norm-fc-ordered-depot",
						               "ext-to-norm-and-pool-denat-fc-depot", // FDS ajout 06/10/2017
						               "pcr-and-purification",
						               "prep-pcr-free",             //FDS 12/12/2016 ajout prep-pcr-free en previous
						               "wg-chromium-lib-prep",      //FDS 13/03/2017 -- JIRA NGL-1167:
						               "pcr-and-indexing"          //FDS 20/07/2017 -- JIRA NGL-1201: processs capture
						               //"bisseq-lib-prep",           //FDS 04/04/2018 -- JIRA NGL-1996: processus BisSeq        oops trop tot
						               //"small-rnaseq-lib-prep"      //FDS 04/04/2018 -- JIRA NGL-1727: processus SmallRNASeq   oops trop tot
						               ), // previous
				null, // pas de purif
				null, // pas de qc
				null  // pas de transfert
				).save();	
		
		//FDS ...../2016 -- JIRA NGL-894: processus et experiments pour X5
		newExperimentTypeNode("lib-normalization",getExperimentTypes("lib-normalization").get(0), 
				false, false, false, 
				getExperimentTypeNodes("ext-to-norm-fc-ordered-depot",         //FDS 15/04/2016 -- JIRA NGL-894: processus court pour X5:
						               "ext-to-norm-and-pool-denat-fc-depot",  //FDS 24/10/2017 ajout ext-to-norm-and-pool-denat-fc-depot
						               "prep-pcr-free",
						               "pcr-and-purification",     //FDS 01/09/2016 -- WG_Nano 
						               "wg-chromium-lib-prep",     //FDS 13/03/2017 -- JIRA NGL-1167: processus chromium
						               "pcr-and-indexing"         //FDS 20/07/2017 -- JIRA NGL-1201: processus capture
						               /// "bisseq-lib-prep"           //FDS 04/04/2018 -- JIRA NGL-1727: processus BisSeq     oops trop tot
						               ), // previous nodes
				null, // pas de purif
				getExperimentTypes("miseq-qc"), // qc 
				getExperimentTypes("aliquoting","pool") // transfert
				).save();	
		
		//FDS 20/06/2016 NGL-1029: ajout transfert pool; FDS 08/12/2016 ajout "normalization-and-pooling" en previous
		newExperimentTypeNode("denat-dil-lib",getExperimentTypes("denat-dil-lib").get(0),
				false,false,false,
				getExperimentTypeNodes("ext-to-denat-dil-lib", 
						               "lib-normalization",
						               "normalization-and-pooling"), // previous nodes
				null, // pas de purif
				null, // pas qc 
				getExperimentTypes("aliquoting","pool") // tranfert
				).save();

		
		//il faut les nodes Nanopore AVANt "pool" car pool s'y refere...
		new Nanopore().getExperimentTypeNode();			
			
		newExperimentTypeNode("prepa-flowcell",getExperimentTypes("prepa-flowcell").get(0),
				false,false,false,
				getExperimentTypeNodes("ext-to-prepa-flowcell",
						               "denat-dil-lib"),         // previous nodes
				null, // pas de purif
				null, // pas qc
				null  // pas transfert
				).save();
		
		newExperimentTypeNode("prepa-fc-ordered",getExperimentTypes("prepa-fc-ordered").get(0),
				false,false,false,
				getExperimentTypeNodes("ext-to-prepa-fc-ordered",
						               "lib-normalization", 
						               "normalization-and-pooling"), // previous nodes
				null, // pas de purif
				null, // pas qc
				null  // pas transfert
				).save();
		
		newExperimentTypeNode("illumina-depot",getExperimentTypes("illumina-depot").get(0),
				false,false,false,
				getExperimentTypeNodes("prepa-flowcell",
						               "prepa-fc-ordered"), // previous nodes
				null, // pas de purif
				null, // pas qc
				null  // pas transfert
				).save();
		
		/* FDS Les noeuds qui ont de nombreux previous nodes doivent etre crees a la fin!!! */
		
		// FDS 06/06/2017: NGL-1447 => le noeud "tubes-to-plate" doit etre declaré pour les process commencant par un transfert
		newExperimentTypeNode("tubes-to-plate",getExperimentTypes("tubes-to-plate").get(0),
				false,false,false,
				getETForTubesToPlate(),  // previous nodes
				null, // pas de purif
				null, // pas qc
				null  // pas transfert
				).save();
		
		newExperimentTypeNode("labchip-migration-profile",getExperimentTypes("labchip-migration-profile").get(0),
				false, false,false,
				getETForLabchipMigrationProfile(),  // previous nodes
				null, // pas de purif
				null, // pas qc
				null  // pas transfert
				).save();
		
	}



	
	private List<ExperimentTypeNode> getETForTubesToPlate(){
		List<ExperimentTypeNode> pets = ExperimentType.find.findActiveByCategoryCode("transformation")
			.stream()
			.filter(e -> !e.code.contains("depot"))
			.map(et -> getExperimentTypeNodes(et.code).get(0))
			.collect(Collectors.toList());
		pets.add(getExperimentTypeNodes("ext-to-transfert-qc-purif").get(0));
		pets.add(getExperimentTypeNodes("ext-to-prepa-fc-ordered").get(0));
		pets.add(getExperimentTypeNodes("ext-to-denat-dil-lib").get(0));
		return pets;		
	}
	
	private List<ExperimentTypeNode> getETForLabchipMigrationProfile(){
		List<ExperimentTypeNode> pets = ExperimentType.find.findActiveByCategoryCode("transformation")
			.stream()
			.filter(e -> !e.code.contains("depot"))
			.map(et -> getExperimentTypeNodes(et.code).get(0))
			.collect(Collectors.toList());
		pets.add(getExperimentTypeNodes("ext-to-qc-transfert-purif").get(0));
		return pets;		
	}
	
	private List<PropertyDefinition> getPropertyDefinitionsQPCR() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		// laisser editable au cas ou la valeur calculée ne convient pas...
		propertyDefinitions.add(newPropertiesDefinition("Concentration", "concentration1", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, "F", null, 
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION), 
				MeasureUnit.find.findByCode("nM"), 
				MeasureUnit.find.findByCode("nM"),
				"single", 13, true, null, "2"));	
		
		propertyDefinitions.add(newPropertiesDefinition("Taille librairie (facteur correctif)", "correctionFactorLibrarySize", LevelService.getLevels(Level.CODE.ContainerIn), Integer.class, true, "F", null, 
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_SIZE), 
				MeasureUnit.find.findByCode("pb"), 
				MeasureUnit.find.findByCode("pb"),
				"single", 12, true, "470", null));
		
		return propertyDefinitions;
	}
	
	private static List<PropertyDefinition> getPropertyAliquoting() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		
		//InputContainer
		propertyDefinitions.add(newPropertiesDefinition("Volume engagé","inputVolume", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, true, null,
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME), 
				MeasureUnit.find.findByCode("µL"), 
				MeasureUnit.find.findByCode("µL"), 
				"single",10, false));
		
		return propertyDefinitions;
	}
	
	private static List<PropertyDefinition> getPropertyDefinitionsPrepaflowcellCNG() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		
		//InputContainer
		propertyDefinitions.add(newPropertiesDefinition("Conc. chargement", "finalConcentration2", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, 
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION), 
				MeasureUnit.find.findByCode("pM"), 
				MeasureUnit.find.findByCode("nM"), 
				"single",25));

		//Outputcontainer		
		propertyDefinitions.add(newPropertiesDefinition("% phiX", "phixPercent", LevelService.getLevels(Level.CODE.ContainerOut), Double.class, true, null,
				null, null, null, 
				"single",51,false,"1",null));	
		
		propertyDefinitions.add(newPropertiesDefinition("Volume final", "finalVolume", LevelService.getLevels(Level.CODE.ContainerOut), Double.class, true, null,
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME), 
				MeasureUnit.find.findByCode("µL"), 
				MeasureUnit.find.findByCode("µL"), 
				"single",52, false));

		return propertyDefinitions;
	}
	
	//FDS ajout 09/11/2015 -- JIRA NGL-838
	private List<PropertyDefinition> getPropertyDefinitionsPrepaflowcellOrderedCNG() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		
		//InputContainer
		// test NGL-1767: ajout liste de volumes pour les differents types de sequencage ?? 01/02/2018 Non laisser un champ a saisie libre et pas de valeur par defaut
		//propertyDefinitions.add(newPropertiesDefinition("Vol. engagé", "inputVolume2", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, newValues("5","150", "310"), "5",
		propertyDefinitions.add(newPropertiesDefinition("Vol. engagé", "inputVolume2", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, "5",
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME), 
				MeasureUnit.find.findByCode("µL"), 
				MeasureUnit.find.findByCode("µL"),
				"single",21));
		
		propertyDefinitions.add(newPropertiesDefinition("Vol. NaOH", "NaOHVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, "5",
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME), 
				MeasureUnit.find.findByCode("µL"), 
				MeasureUnit.find.findByCode("µL"),
				"single",22));
		
		propertyDefinitions.add(newPropertiesDefinition("Conc. NaOH", "NaOHConcentration", LevelService.getLevels(Level.CODE.ContainerIn), String.class, true, null,
				null, null, null, "single",23,true,"0.1N",null));
		
		propertyDefinitions.add(newPropertiesDefinition("Vol. TrisHCL", "trisHCLVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, "5",
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME), 
				MeasureUnit.find.findByCode("µL"), 
				MeasureUnit.find.findByCode("µL"), 
				"single",24));
		
		propertyDefinitions.add(newPropertiesDefinition("Conc. TrisHCL", "trisHCLConcentration", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, "200000000", 
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),
				MeasureUnit.find.findByCode("mM"), // NORMAL, pas une faute de frappe
				MeasureUnit.find.findByCode("nM"), 
				"single",25));
		
		propertyDefinitions.add(newPropertiesDefinition("Vol. master EPX", "masterEPXVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, "35",
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME), 
				MeasureUnit.find.findByCode("µL"), 
				MeasureUnit.find.findByCode("µL"),
				"single",26));
		
		propertyDefinitions.add(newPropertiesDefinition("Concentration finale", "finalConcentration2", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true,  null,
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME), 
				MeasureUnit.find.findByCode("pM"), 
				MeasureUnit.find.findByCode("nM"),
				"single",27,false));

		//OuputContainer
		//keep order declaration between phixPercent and finalVolume
		propertyDefinitions.add(newPropertiesDefinition("% phiX", "phixPercent", LevelService.getLevels(Level.CODE.ContainerOut), Double.class, true, null, null,
				null, null, 
				"single",51,false,"1",null));		
		
		propertyDefinitions.add(newPropertiesDefinition("Volume final", "finalVolume", LevelService.getLevels(Level.CODE.ContainerOut), Double.class, true, null,
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME), 
				MeasureUnit.find.findByCode("µL"), 
				MeasureUnit.find.findByCode( "µL"), 
				"single",28,false, "50",null));
		
		// NGL-1325 ajout propriété sequencingType de niveau Experiment, optionnelle
		// NGL-1730 ajout NovaSeq 6000;  NGL1767: subdiviser en NovaSeq 6000 / S2 + NovaSeq 6000 / S4 ( attention si changement de labels=> sont utilisés dans javascript)
		propertyDefinitions.add(newPropertiesDefinition("Type de séquençage", "sequencingType", LevelService.getLevels(Level.CODE.Experiment), String.class, false, null,
				DescriptionFactory.newValues("Hiseq 4000","Hiseq X","NovaSeq 6000 / S2","NovaSeq 6000 / S4"),null,null,null, 
				"single",10, true, null,null));	
		
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
		
		//InputContainer 
		propertyDefinitions.add(newPropertiesDefinition("Densité de clusters", "clusterDensity", LevelService.getLevels(Level.CODE.ContainerIn), Integer.class, false, null, null, 
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION), 
				MeasureUnit.find.findByCode("c/mm²"), 
				MeasureUnit.find.findByCode("c/mm²"),
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
		propertyDefinitions.add(newPropertiesDefinition("% Mismatch R1", "R2MismatchPercentage", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, null, 
				null,null,null,"single", 17, true, null, null));
		
		propertyDefinitions.add(newPropertiesDefinition("Taille d'insert médiane", "measuredInsertSize", LevelService.getLevels(Level.CODE.ContainerIn,Level.CODE.Content), Integer.class, false, null, null, 
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_SIZE), 
				MeasureUnit.find.findByCode("pb"), 
				MeasureUnit.find.findByCode("pb"),
				"single", 18, true, null, null));
		
		propertyDefinitions.add(newPropertiesDefinition("Taille Min", "minInsertSize", LevelService.getLevels(Level.CODE.ContainerIn), Integer.class, false, null, null, 
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_SIZE), 
				MeasureUnit.find.findByCode("pb"), 
				MeasureUnit.find.findByCode("pb"),
				"single", 19, true, null, null));
		
		propertyDefinitions.add(newPropertiesDefinition("Taille Max", "maxInsertSize", LevelService.getLevels(Level.CODE.ContainerIn), Integer.class, false, null, null, 
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_SIZE), 
				MeasureUnit.find.findByCode("pb"), 
				MeasureUnit.find.findByCode("pb"),
				"single", 20, true, null, null));
		
		propertyDefinitions.add(newPropertiesDefinition("Observed Diversity", "observedDiversity", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, null, 
				null,null,null,"single", 21, true, null, null));
		
		propertyDefinitions.add(newPropertiesDefinition("Estimated Diversity", "estimatedDiversity", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, null, 
				null,null,null,"single", 22, true, null, null));
	
		return propertyDefinitions;
	}
	
	// FDS JIRA NGL-1030 Ajouter la propriété size et rendre les 2 obligatoires au status "F"(Terminé)
	public static List<PropertyDefinition> getPropertyDefinitionsChipMigration() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();

		//InputContainer (pas d'outputContainer sur une experience QC )
		propertyDefinitions.add(newPropertiesDefinition("Concentration", "concentration1", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, "F", null, 
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION), 
				null, // l'unité est variable. NE PLUS LA SPECIFIER..., MeasureUnit.find.findByCode("ng/µl")
				null, // l'unité est variable. NE PLUS LA SPECIFIER..., MeasureUnit.find.findByCode("ng/µl")
				"single", 11, true, null, null));
		
		// laiser la position 12 libre pour la colonne unit
		propertyDefinitions.add(newPropertiesDefinition("Taille", "size1", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, "F", null, 
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_SIZE), 
				MeasureUnit.find.findByCode("pb"), 
				MeasureUnit.find.findByCode("pb"),
				"single", 13, true, null, null));
		
		propertyDefinitions.add(newPropertiesDefinition("Profil de migration", "migrationProfile", LevelService.getLevels(Level.CODE.ContainerIn), Image.class, false, null, null, 				
				"img", 14, false, null, null));
		
		// 07/03/2018 NGL-1859: propriété pour demander a l'utilisateur s'il faut copier concentration (par defaut TRUE)
		propertyDefinitions.add(newPropertiesDefinition("Copier concentration dans concentration finale du container ?", "copyConcentration", LevelService.getLevels(Level.CODE.Experiment), Boolean.class, true, null,
				null, "single",15, true,"true", null));
		
		return propertyDefinitions;
	}
	
	
	private static List<PropertyDefinition> getPropertyDefinitionsIlluminaDepot() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		
		//Utilisé par import ngl-data CNG de creation des depot-illumina
		//propertyDefinitions.add(newPropertiesDefinition("Code LIMS", "limsCode", LevelService.getLevels(Level.CODE.Experiment), Integer.class, false, "single"));	
		propertyDefinitions.add(newPropertiesDefinition("Date réelle de dépôt", "runStartDate", LevelService.getLevels(Level.CODE.Experiment), Date.class, true, null,
				null, "single",21, true,null, null));
		
		propertyDefinitions.add(newPropertiesDefinition("Activation NGS-RG", "rgActivation", LevelService.getLevels(Level.CODE.Experiment), Boolean.class, true, null,
				null, "single",22, true,"true", null));
		
		return propertyDefinitions;
	}
	
	// FDS ajout 05/02/2016 -- JIRA NGL-894: experiment PrepPcrFree pour le process X5
	private List<PropertyDefinition> getPropertyDefinitionsPrepPcrFree() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		
		//InputContainer
		propertyDefinitions.add(newPropertiesDefinition("Vol. engagé dans Frag", "inputVolumeFrag", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, null,
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME), 
				MeasureUnit.find.findByCode("µL"), 
				MeasureUnit.find.findByCode("µL"),
				"single",20,true,null,null));
		
		propertyDefinitions.add(newPropertiesDefinition("Qté. engagée dans Frag", "inputQuantityFrag", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, null,
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY), 
				MeasureUnit.find.findByCode("ng"), 
				MeasureUnit.find.findByCode("ng"),
				"single",21,true,null,null));
		
		propertyDefinitions.add(newPropertiesDefinition("Vol. engagé dans Lib", "inputVolumeLib", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, null,
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME), 
				MeasureUnit.find.findByCode("µL"), 
				MeasureUnit.find.findByCode("µL"),
				"single",22,true,null,null));
		
		propertyDefinitions.add(newPropertiesDefinition("Qté. engagée dans Lib", "inputQuantityLib", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, null,
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY), 
				MeasureUnit.find.findByCode("ng"), 
				MeasureUnit.find.findByCode("ng"),
				"single",23,true,null,null));
	
		//OuputContainer
		// GA 08/02/2016 =>  ces proprietes de containerOut doivent etre propagees au content
		// GA 14/03/2016 => il faut specifier l'état auquel les propriétés sont obligatoires: ici Finished (F)
		propertyDefinitions.add(newPropertiesDefinition("Tag", "tag", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), String.class, true, "F", getTagIllumina(), 
				"single", 30, true, null,null));
		
		propertyDefinitions.add(newPropertiesDefinition("Catégorie de Tag", "tagCategory", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), String.class, true, "F", getTagCategories(), 
				"single", 31, true, null,null));		
		
		// pas de niveau content car théoriques( J Guy..)
		propertyDefinitions.add(newPropertiesDefinition("Taille insert (théorique)", "insertSize", LevelService.getLevels(Level.CODE.ContainerOut),Integer.class, true, null,
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_SIZE), 
				MeasureUnit.find.findByCode("pb"), 
				MeasureUnit.find.findByCode("pb"),
				"single",32,true,"350", null));
		
		propertyDefinitions.add(newPropertiesDefinition("Taille librairie (théorique)", "librarySize", LevelService.getLevels(Level.CODE.ContainerOut),Integer.class, true, null,	
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_SIZE), 
				MeasureUnit.find.findByCode("pb"), 
				MeasureUnit.find.findByCode("pb"),
				"single",33, true,"470",null));
	
		return propertyDefinitions;
	}
	
	// FDS ajout 27/09/2016  pour le process WG_NANO
	// similaire a PrepPcrFree mais : pas de valeurs par defaut, pas de tailles theoriques
	private List<PropertyDefinition> getPropertyDefinitionsPrepWgNano() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		
		//InputContainer
		propertyDefinitions.add(newPropertiesDefinition("Vol. engagé dans Frag", "inputVolumeFrag", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, null,
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME), 
				MeasureUnit.find.findByCode("µL"), 
				MeasureUnit.find.findByCode("µL"),
				"single",20));
		
		propertyDefinitions.add(newPropertiesDefinition("Qté. engagée dans Frag", "inputQuantityFrag", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, null,
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY), 
				MeasureUnit.find.findByCode("ng"), 
				MeasureUnit.find.findByCode("ng"),
				"single",21));
		
		propertyDefinitions.add(newPropertiesDefinition("Vol. engagé dans Lib", "inputVolumeLib", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, null,
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME), 
				MeasureUnit.find.findByCode("µL"), 
				MeasureUnit.find.findByCode("µL"),
				"single",22));
		
		propertyDefinitions.add(newPropertiesDefinition("Qté. engagée dans Lib", "inputQuantityLib", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, null,
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY), 
				MeasureUnit.find.findByCode("ng"), 
				MeasureUnit.find.findByCode("ng"),
				"single",23));
	
		//OuputContainer
		// GA 08/02/2016 => ces proprietes de containerOut doivent etre propagees au content
		// GA 14/03/2016 => il faut specifier l'état auquel les propriétés sont obligatoires: ici Finished (F)
		propertyDefinitions.add(newPropertiesDefinition("Tag", "tag", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), String.class, true, "F", getTagIllumina(), 
				"single", 30, true, null,null));
		
		propertyDefinitions.add(newPropertiesDefinition("Catégorie de Tag", "tagCategory", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), String.class, true, "F", getTagCategories(), 
				"single", 31, true, null,null));		
	
		return propertyDefinitions;
	}
	
	
	// FDS ajout 05/02/2016 -- JIRA NGL-894: experiment librairie normalization pour le process X5
	// FDS 19/07/2017 -- JIRA NGL-1519: egalement utilisees par l'experience de transfert additional-normalization
	private List<PropertyDefinition> getPropertyDefinitionsLibNormalization() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		
		//InputContainer
		// calculé automatiquement en fonction du volume final et concentration final demandés ou saisie libre, non obligatoire
		// 26/07/2017  non editable
		propertyDefinitions.add(newPropertiesDefinition("Volume engagé", "inputVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, null,
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME), 
				MeasureUnit.find.findByCode("µL"), 
				MeasureUnit.find.findByCode("µL"),
				"single", 20, false, null,null));
		
		//buffer 
		// calculé automatiquement en fonction du volume final et concentration final demandés ou saisie libre, non obligatoire
		// 26/07/2017  non editable
		propertyDefinitions.add(newPropertiesDefinition("Volume tampon", "bufferVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, null,
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME), 
				MeasureUnit.find.findByCode("µL"), 
				MeasureUnit.find.findByCode("µL"),
				"single", 20, false, null,null));		
		
		//OuputContainer
		
		return propertyDefinitions;
	}
	
	// FDS ajout 02/06/2016 -- JIRA NGL-1028: experiment normalization-and-pooling
	private List<PropertyDefinition> getPropertyDefinitionsNormalizationAndPooling() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		
		//InputContainer
		// calculé automatiquement en fonction du volume final et concentration final demandés ou saisie libre, non obligatoire VERIFIER
		propertyDefinitions.add(newPropertiesDefinition("Volume engagé", "inputVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, null,
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME), 
				MeasureUnit.find.findByCode("µL"), 
				MeasureUnit.find.findByCode("µL"),
				"single", 20, true, null,null));
		
		//OuputContainer 
		propertyDefinitions.add(newPropertiesDefinition("Volume tampon Tris", "bufferVolume", LevelService.getLevels(Level.CODE.ContainerOut), Double.class, false, null, null,
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME), 
				MeasureUnit.find.findByCode("µL"), 
				MeasureUnit.find.findByCode("µL"),
				"single", 25, true, null,null));		
		
		// 18/10/2016 ajout workName
		propertyDefinitions.add(newPropertiesDefinition("Label de travail", "workName", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Container), String.class, false, null, null, 
				"single", 100, true, null,null));
		
		return propertyDefinitions;
	}
	
	// FDS ajout 17/06/2016 -- JIRA NGL-1029: experiment pool en plaque
	private static List<PropertyDefinition> getPropertyDefinitionPool() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		
		//InputContainer
		propertyDefinitions.add(newPropertiesDefinition("Volume engagé", "inputVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, null,
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),
				MeasureUnit.find.findByCode("µL"),
				MeasureUnit.find.findByCode("µL"),
				"single", 20, true, null,null));
		
		//OuputContainer 
		propertyDefinitions.add(newPropertiesDefinition("Volume tampon", "bufferVolume", LevelService.getLevels(Level.CODE.ContainerOut), Double.class, false, null, null,
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),
				MeasureUnit.find.findByCode("µL"),
				MeasureUnit.find.findByCode("µL"),
				"single", 25, true, null,null));
		
		// 18/10/2016 ajout workName
		propertyDefinitions.add(newPropertiesDefinition("Label de travail", "workName", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Container), String.class, false, null, null, 
				"single", 100, true, null,null));
		
		return propertyDefinitions;
	}
	
	
	// FDS ajout 01/08/2016 -- JIRA NGL-1027: experiment PCR + purification en plaque	
	private static List<PropertyDefinition> getPropertyDefinitionsPcrAndPurification() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		
		//InputContainer
		// volume engagé editable et obligatoire, qté pas editable calculée en fonction volume engagé et pas sauvegardée
		// 27/09/2016 ajout default value '25"; 09/11/2017 NGL-1691: supression valeur par defaut
		propertyDefinitions.add(newPropertiesDefinition("Volume engagé", "inputVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, null,
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),
				MeasureUnit.find.findByCode("µL"),
				MeasureUnit.find.findByCode("µL"),
				"single", 20, true, null, null));
		
		//OuputContainer 
		// rien...??
		
		return propertyDefinitions;
	}
	
	//FDS ajout 03/08/2016 -- JIRA NGL-1026: experiment library prepartion sans fragmentation ( duplication a partir de pcr-free .. et suppression de la fragmentation
	private List<PropertyDefinition> getPropertyDefinitionsLibraryPrep() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		
		//InputContainer
		// valeur par defaut pour volume et qté engagées ?? Pas pour l'instant...
		propertyDefinitions.add(newPropertiesDefinition("Vol. engagé dans Lib", "inputVolumeLib", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, null,
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME), 
				MeasureUnit.find.findByCode("µL"), 
				MeasureUnit.find.findByCode("µL"),
				"single",22, true, null, null));
		
		propertyDefinitions.add(newPropertiesDefinition("Qté. engagée dans Lib", "inputQuantityLib", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, null,
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY), 
				MeasureUnit.find.findByCode("ng"), 
				MeasureUnit.find.findByCode("ng"),
				"single",23, true, null, null));
	
		//OuputContainer
		// ces propriétés de containerOut doivent etre propagees au content
		// il faut specifier l'état auquel les propriétés sont obligatoires: ici Finished (F)
		propertyDefinitions.add(newPropertiesDefinition("Tag", "tag", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), String.class, true, "F", 
				getTagIllumina(), 
				"single", 30, true, null,null));
		
		propertyDefinitions.add(newPropertiesDefinition("Catégorie de Tag", "tagCategory", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), String.class, true, "F", 
				getTagCategories(), 
				"single", 31, true, null,null));		
		
		return propertyDefinitions;
	}
	
	//FDS ajout 21/02/2017 -- JIRA NGL-1167 experiences pour process Chromium
	private List<PropertyDefinition> getPropertyDefinitionsChromiumGemGeneration() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		
		//InputContainer
		propertyDefinitions.add(newPropertiesDefinition("Conc. dilution","dilutionConcentration", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, null,
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION), 
				MeasureUnit.find.findByCode("ng/µL"), 
				MeasureUnit.find.findByCode("ng/µL"),
				"single",22, true, null, null));
	
		return propertyDefinitions;
	}
	
	//FDS ajout 21/02/2017 -- JIRA NGL-1167 experiences pour process Chromium
	private List<PropertyDefinition> getPropertyDefinitionsWGChromiumLibPrep() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		
		//OuputContainer
		// ces propriétés de containerOut doivent etre propagées au content; propriétés obligatoires a: Finished 
		propertyDefinitions.add(newPropertiesDefinition("Tag", "tag", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), String.class, true, "F", 
				getTagIllumina(new ArrayList<>( Arrays.asList("POOL-INDEX"))), 
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

		//InputContainer (pas d'outputContainer sur une experience QC )
		propertyDefinitions.add(newPropertiesDefinition("Taille", "size1", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false,null, null, 
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_SIZE), 
				MeasureUnit.find.findByCode("pb"), 
				MeasureUnit.find.findByCode("pb"),
				"single", 12, true, null, null));	
	
		// le bouton import n'apparait que si la propriété est editable=true
		propertyDefinitions.add(newPropertiesDefinition("Profil de migration", "migrationProfile", LevelService.getLevels(Level.CODE.ContainerIn), Image.class, false, null, null, 				
				"img", 13,true, null, null));
		
		propertyDefinitions.add(newPropertiesDefinition("Concentration", "concentration1", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, null, 
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION), 
				MeasureUnit.find.findByCode("ng/µL"),
				MeasureUnit.find.findByCode("ng/µL"),
				"single", 14, true, null, null));
			
		return propertyDefinitions;
	}
	
	
	// GA : stocker dans un pseudo QC les valeurs initales du fichier importé
	private List<PropertyDefinition> getPropertyDefinitionsBankQC() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		
		propertyDefinitions.add(newPropertiesDefinition("Volume fourni", "providedVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, null, 
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),
				MeasureUnit.find.findByCode( "µL"),
				MeasureUnit.find.findByCode( "µL"),
				"single", 11, true, null,null));
		
		propertyDefinitions.add(newPropertiesDefinition("Concentration fournie", "providedConcentration", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, null, 
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),
				MeasureUnit.find.findByCode("ng/µL"),
				MeasureUnit.find.findByCode("ng/µL"),
				"single", 13, true, null,null));
		
		// FDS 14/03/2017 NGL-1776 ajout propriété venant du LIMS Modulbio (voir aussi ImportService) 
		propertyDefinitions.add(newPropertiesDefinition("Bank Integrity Number", "bankIntegrityNumber", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, null,
				"single", 15, true, null,null));
		
		return propertyDefinitions;
	}

	// FDS 21/06/2017 ajout -- JIRA NGL-1472: necessiter d'ajouter QC provenant de collaborateur extérieur.
	private List<PropertyDefinition> getPropertyDefinitionsExternalQC() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		
		propertyDefinitions.add(newPropertiesDefinition("Volume fourni", "providedVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, null, 
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),
				MeasureUnit.find.findByCode("µL"),
				MeasureUnit.find.findByCode("µL"),
				"single", 11, true, null,null));
		
		propertyDefinitions.add(newPropertiesDefinition("Concentration fournie", "providedConcentration", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, null, 
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),
				MeasureUnit.find.findByCode("ng/µL"),
				MeasureUnit.find.findByCode("ng/µL"),
				"single", 13, true, null,null));
		
		propertyDefinitions.add(newPropertiesDefinition("Taille fournie", "providedSize", LevelService.getLevels(Level.CODE.ContainerIn), Integer.class, false, null, null, 
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),
				MeasureUnit.find.findByCode("pb"),
				MeasureUnit.find.findByCode("pb"),
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
		
		// FDS 09/11/2017 NGL-1691: ajout propriété de niveau Global (experiment) :Temps d'hybridation==hybridizationTime : saisie libre texte, non obligatoire
		// que veut dire le parametre "position" pour les propriété de niveau Global ??
		propertyDefinitions.add(newPropertiesDefinition("Temps d'hybridation", "hybridizationTime", LevelService.getLevels(Level.CODE.Experiment), String.class, false, null,
				null, "single",13,true,null,null));	
		
		//InputContainer
		propertyDefinitions.add(newPropertiesDefinition("Volume engagé","inputVolume", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, true, null, null,
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME), 
				MeasureUnit.find.findByCode("µL"), 
				MeasureUnit.find.findByCode("µL"), 
				"single",10,false,null,null));          // pas editable=> calculé !
		
		// editable
		propertyDefinitions.add(newPropertiesDefinition("Qté. engagée", "inputQuantity", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, null,
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY), 
				MeasureUnit.find.findByCode("ng"), 
				MeasureUnit.find.findByCode("ng"),
				"single",11,true,null,null));
		
		//OuputContainer 
		// Liste; valeur par defaut= celle qui se trouve dans processus dans expectedBaits ?. PAS POSSIBLE!!!
		// 31/08/2017 erreur de spec, mettre sur ContainerOut 
		// 07/12/2017 NGL-1735 : "F" place sur default value au lieu de requiredState !!!
		propertyDefinitions.add(newPropertiesDefinition("Baits (sondes)", "baits", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), String.class, true, "F", getCaptureBaitsValues(),
				null, null, null,
				"single",12,true,null,null));
			
		return propertyDefinitions;
	}
	
	//FDS ajout 18/07/2017 NGL-1201
	private List<PropertyDefinition> getPropertyDefinitionsPcrIndexing() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		
		//OuputContainer
		// proprietes de containerOut doivent etre propagees au content
		// il faut specifier l'état auquel les propriétés sont obligatoires: ici Finished (F)
		propertyDefinitions.add(newPropertiesDefinition("Tag", "tag", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), String.class, true, "F", getTagIllumina(), 
				"single", 30, true, null,null));
		
		propertyDefinitions.add(newPropertiesDefinition("Catégorie de Tag", "tagCategory", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), String.class, true, "F", getTagCategories(), 
				"single", 31, true, null,null));	
		
		// valeur par defaut: 30 µL
		propertyDefinitions.add(newPropertiesDefinition("Volume final", "finalVolume", LevelService.getLevels(Level.CODE.ContainerOut), Double.class, true, "F", null,
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME), 
				MeasureUnit.find.findByCode("µL"), 
				MeasureUnit.find.findByCode("µL"), 
				"single", 32, true, "30", null));
		
		return propertyDefinitions;
	}
	
	//FDS ajout 27/07/2017 NGL-1201
	private List<PropertyDefinition> getPropertyDefinitionsQuantIt() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		
		propertyDefinitions.add(newPropertiesDefinition("Concentration", "concentration1", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, null, 
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),
				MeasureUnit.find.findByCode("ng/µL"),
				MeasureUnit.find.findByCode("ng/µL"),
				"single", 13, true, null, null));  
		
		// FDS ajout 26/03/2018 NGL-1970: propriété pour demander a l'utilisateur s'il faut copier concentration (par defaut TRUE)
		propertyDefinitions.add(newPropertiesDefinition("Copier concentration dans concentration finale du container ?", "copyConcentration", LevelService.getLevels(Level.CODE.Experiment), Boolean.class, true, null,
				null, "single",15, true,"true", null));
		
		return propertyDefinitions;
	}
	
	/*OOps trop tot... pas pour la 2.1.2 attendre
	//FDS ajout 05/04/2018 NGL-1996
	private List<PropertyDefinition> getPropertyDefinitionsBisSeqLibPrep() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
		
		//OuputContainer	
		// A CONFIRMER !!!
		propertyDefinitions.add(newPropertiesDefinition("Nbre Cycles PCR", "pcrCycleNumber", LevelService.getLevels(Level.CODE.ContainerOut), Double.class, false, "F", null, 
				null,null,null,"single", 29, true, "16", null)); 
		
		// proprietes de containerOut doivent etre propagees au content
		// il faut specifier l'état auquel les propriétés sont obligatoires: ici Finished (F) 
		propertyDefinitions.add(newPropertiesDefinition("Tag", "tag", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), String.class, true, "F", getTagIllumina(), 
				"single", 30, true, null,null));
		
		propertyDefinitions.add(newPropertiesDefinition("Catégorie de Tag", "tagCategory", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), String.class, true, "F", getTagCategories(), 
				"single", 31, true, null,null));	
		
		
		return propertyDefinitions;
	}
	
	//FDS ajout 05/04/2018 NGL-1727
	private List<PropertyDefinition> getPropertyDefinitionsSmallRNASeqLibPrep() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
		
		//OuputContainer	
		// A CONFIRMER !!!
		propertyDefinitions.add(newPropertiesDefinition("Nbre Cycles PCR", "pcrCycleNumber", LevelService.getLevels(Level.CODE.ContainerOut), Double.class, false, "F", null, 
				null,null,null,"single", 29, true, "16", null)); 
		
		// proprietes de containerOut doivent etre propagees au content
		// il faut specifier l'état auquel les propriétés sont obligatoires: ici Finished (F) 
		propertyDefinitions.add(newPropertiesDefinition("Tag", "tag", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), String.class, true, "F", getTagIllumina(), 
				"single", 30, true, null,null));
		
		propertyDefinitions.add(newPropertiesDefinition("Catégorie de Tag", "tagCategory", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), String.class, true, "F", getTagCategories(), 
				"single", 31, true, null,null));	
		
		
		return propertyDefinitions;
	}
	*/
	
	
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
		 values.add(DescriptionFactory.newValue("custom","custom"));
	
    	return values;
	}
	
}