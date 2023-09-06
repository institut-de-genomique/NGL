package services.instance.resolution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import fr.cea.ig.MongoDBDAO;
import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.processes.description.ProcessType;
import models.laboratory.resolutions.instance.Resolution;
import models.laboratory.resolutions.instance.ResolutionCategory;
import models.laboratory.resolutions.instance.ResolutionConfiguration;
import models.utils.InstanceConstants;
import models.utils.InstanceHelpers;
import models.utils.dao.DAOException;
import validation.ContextValidation;

/**
 * Create Resolutions : for more flexibility, these data are created in a specific collection (in MongoDB) 
 * instead of being created in the description database.
 * 
 * 23-06-2014  
 * @author dnoisett
 *
 */
public class ResolutionService {
	
	private static final play.Logger.ALogger logger = play.Logger.of(ResolutionService.class);
	
	private static HashMap<String, ResolutionCategory> resolutionCategories; 

	public static void saveResolutionsCNG(ContextValidation ctx) {
		resolutionCategories = createResolutionCategoriesCNG();

		createRunResolutionCNG(ctx); 
		createReadSetResolutionCNG(ctx); 
		// FDS 15/01: no Analysis Resolutions ???
		createIlluminaPrepFCDepotResolutionCNG(ctx);
		createPrepPcrFreeResolutionCNG(ctx);
		createQCMiseqResolutionCNG(ctx);
		createExperimentResolution(ctx); // ajoute les resolutions par defaut sur toutes les experiences
		createProcessResolutionCNG(ctx);
		createContainerResolutionCNG(ctx);		
	}
	
	public static void saveResolutionsCNS(ContextValidation ctx) {
		resolutionCategories = createResolutionCategoriesCNS();
		
		createRunResolutionCNS(ctx); 
		createReadSetResolutionCNS(ctx); 
		createAnalysisResolutionCNS(ctx); 
		createOpgenDepotResolutionCNS(ctx);
		createPurifPrecipitationResolutionCNS(ctx);
		createDepotNanoporeResolutionCNS(ctx);
		createIlluminaPrepFCDepotResolutionCNS(ctx);
		createIryPreparationNLRSResolutionCNS(ctx);
		createDepotBionanoResolutionCNS(ctx);
		createSamplePrepResolutionCNS(ctx);
		createGelMigrationResolutionCNS(ctx);
		createExperimentResolution(ctx); // ajoute les resolutions par defaut sur toutes les experiences
		createProcessResolutionCNS(ctx);
		createContainerResolutionCNS(ctx);
	}
	
	public static HashMap<String, ResolutionCategory> createResolutionCategoriesCNG() {	
		HashMap<String, ResolutionCategory> resoCategories = new HashMap<>();
		
		// Run
		resoCategories.put("SAV",      new ResolutionCategory("Problème qualité : SAV",    (short) 10));
		resoCategories.put("PbM",      new ResolutionCategory("Problème machine",          (short) 20));
		resoCategories.put("PbR",      new ResolutionCategory("Problème réactifs",         (short) 30));
		resoCategories.put("LIB",      new ResolutionCategory("Problème librairie",        (short) 50));
		resoCategories.put("PbI",      new ResolutionCategory("Problème informatique",     (short) 60));
		resoCategories.put("RUN-Info", new ResolutionCategory("Informations",              (short) 70));
		resoCategories.put("QC",       new ResolutionCategory("Observations QC",           (short) 80));
		
		// ReadSet
		resoCategories.put("Run",      new ResolutionCategory("Problème run",              (short)  5));
		resoCategories.put("Qte",      new ResolutionCategory("Problème quantité",         (short) 15));
		resoCategories.put("IND",      new ResolutionCategory("Problème indexing",         (short) 20));
		resoCategories.put("Qlte",     new ResolutionCategory("Problème qualité",          (short) 25));
		resoCategories.put("MAP",      new ResolutionCategory("Problème mapping",          (short) 40));
		resoCategories.put("Sample",   new ResolutionCategory("Problème échantillon",      (short) 55));
		resoCategories.put("LIMS",     new ResolutionCategory("Problème déclaration LIMS", (short) 60));
		resoCategories.put("Info",     new ResolutionCategory("Informations",              (short) 65));
		
		// Analysis
		
		// Experiment
		resoCategories.put("Default",  new ResolutionCategory("Default",                   (short)  0));
		
		return resoCategories;
	}

	public static HashMap<String, ResolutionCategory> createResolutionCategoriesCNS() {
		HashMap<String, ResolutionCategory> resoCategories = new HashMap<>();
		
		//Run
		resoCategories.put("PbM",      new ResolutionCategory("Problème machine",          (short) 20));
		resoCategories.put("PbR",      new ResolutionCategory("Problème réactifs",         (short) 30));
		resoCategories.put("SAV",      new ResolutionCategory("Problème qualité : SAV",    (short) 40));
		resoCategories.put("PbI",      new ResolutionCategory("Problème informatique",     (short) 60));
		resoCategories.put("Info",     new ResolutionCategory("Informations",              (short) 70));
		resoCategories.put("BioN",     new ResolutionCategory("Runs BIONANO",              (short) 80));// Run Bionano
		
		//ReadSet
		resoCategories.put("Run",      new ResolutionCategory("Problème run",              (short)  5));
		resoCategories.put("LIB",      new ResolutionCategory("Problème librairie",        (short) 10));
		resoCategories.put("Qte",      new ResolutionCategory("Problème quantité",         (short) 15));
		resoCategories.put("IND",      new ResolutionCategory("Problème indexing",         (short) 20));
		resoCategories.put("Qlte",     new ResolutionCategory("Problème qualité",          (short) 25));
		resoCategories.put("TAXO",     new ResolutionCategory("Problème taxon",            (short) 30));
		resoCategories.put("RIBO",     new ResolutionCategory("Problème ribosomes",        (short) 35));
		resoCategories.put("MAP",      new ResolutionCategory("Problème mapping",          (short) 40));
		resoCategories.put("MERG",     new ResolutionCategory("Problème merging",          (short) 45));
		resoCategories.put("Info",     new ResolutionCategory("Informations",              (short) 50));
		resoCategories.put("dsBioN",   new ResolutionCategory("Dataset BIONANO",           (short) 60));//DatasetBionano
		
		//Analysis
		resoCategories.put("BA-MERG",  new ResolutionCategory("Merging",                   (short) 10));
		resoCategories.put("CTG",      new ResolutionCategory("Contigage",                 (short) 20));
		resoCategories.put("SIZE",     new ResolutionCategory("Size Filter",               (short) 30));
		resoCategories.put("SCAFF",    new ResolutionCategory("Scaffolding",               (short) 40));
		resoCategories.put("GAP",      new ResolutionCategory("Gap Closing",               (short) 50));
		
		//Experiment	
		resoCategories.put("Default",  new ResolutionCategory("Default",                   (short)  0));
		
		return resoCategories;
	}

	
	/* sub-methods */
	
	public static void createRunResolutionCNG(ContextValidation ctx) {
		List<Resolution> l = new ArrayList<>();
		
		// PbM
		l.addAll(rList(getResolutionCategory("PbM"),		
				newResolution("Indéterminé",   "PbM-indetermine"),	
				newResolution("Chiller",       "PbM-chiller"),
				newResolution("Pelletier",     "PbM-pelletier"),
				newResolution("Fluidique",     "PbM-fluidiq"),
				newResolution("Laser",         "PbM-laser"),
				newResolution("Camera",        "PbM-camera"),
				newResolution("Focus",         "PbM-focus"),
				newResolution("Pb de vide",    "PbM-pbVide"),
				newResolution("PE module",     "PbM-PEmodule"),
				newResolution("Zone de dépôt", "PbM-zoneDepot"),				
				newResolution("cBot",          "PbM-cBot")));		
		
		// SAV
		l.addAll(rList(getResolutionCategory("SAV"),	
				newResolution("Intensité",                    "SAV-intensite"),
				newResolution("Intensité faible A",           "SAV-intFbleA"),
				newResolution("Intensité faible T",           "SAV-intFbleT"),
				newResolution("Intensité faible C",           "SAV-intFbleC"),
				newResolution("Intensité faible G",           "SAV-intFbleG"),
				newResolution("Densité clusters trop élevée", "SAV-densiteElevee"),
				newResolution("Densité clusters trop faible", "SAV-densiteFaible"),
				newResolution("Densité clusters nulle",       "SAV-densiteNulle"),
				newResolution("%PF",                          "SAV-PF"),
				newResolution("Phasing",                      "SAV-phasing"),
				newResolution("Prephasing",                   "SAV-prephasing"),
				newResolution("Error rate",                   "SAV-errorRate"),
				newResolution("Focus",                        "SAV-focus"),
				newResolution("Q30",                          "SAV-Q30"),
				newResolution("% bases déséquilibré",         "SAV-perctBasesDeseq"),
				newResolution("Index non représenté",         "SAV-indexNonPresent"),
				newResolution("Index sous-représenté",        "SAV-indexFblePerc"),
				newResolution("Indexing / demultiplexage",    "SAV-IndDemultiplex")));
		
		// PbR
		l.addAll(rList(getResolutionCategory("PbR"),
				newResolution("Indéterminé",     "PbR-indetermine"),
				newResolution("Flowcell",        "PbR-FC"),
				newResolution("cBot",            "PbR-cBot"),
				newResolution("Séquencage",      "PbR-sequencage"),
				newResolution("Indexing",        "PbR-indexing"),
				newResolution("PE module",       "PbR-PEmodule"),
				newResolution("Rehyb primer R1", "PbR-rehybR1"),
				newResolution("Rehyb primer R2", "PbR-rehybR2"),
				newResolution("Erreur réactifs", "PbR-erreurReac"),
				newResolution("Rajout réactifs", "PbR-ajoutReac")));
		
		// LIB
		l.addAll(rList(getResolutionCategory("LIB"),
				newResolution("Construction librairie",   "LIB-construction"),
				newResolution("Cause profil : librairie", "LIB-profilIntLib"),
				newResolution("Cause profil : exp type",  "LIB-profilIntExpType"),
				newResolution("Pb dilution",              "LIB-pbDilution"),
				newResolution("Pb dilution spike-In",     "LIB-pbDilSpikeIn")));
		
		// PbI
		l.addAll(rList(getResolutionCategory("PbI"),		
				newResolution("Indéterminé",            "PbI-indetermine"),
				newResolution("PC",                     "PbI-PC"),
				newResolution("Ecran",                  "PbI-ecran"),
				newResolution("Espace disq insuf",      "PbI-espDisqInsuf"),
				newResolution("Logiciel",               "PbI-logiciel"),
				newResolution("Reboot PC",              "PbI-rebootPC"),
				newResolution("Retard robocopy",        "PbI-robocopy"),
				newResolution("Erreur paramétrage run", "PbI-parametrageRun")));
		
		// RUN-Info
		l.addAll(rList(getResolutionCategory("RUN-Info"),
				newResolution("Run de validation", "Info-runValidation"),
				newResolution("Remboursement",     "Info-remboursement")));

		// QC
		l.addAll(rList(getResolutionCategory("QC"),
				newResolution("Intensité B.M.S", "QC-intBMS"),
				newResolution("Tiles out",       "QC-tilesOut"), 
				newResolution("Saut de chimie",  "QC-sautChimie")));
		
		ResolutionConfiguration r = new ResolutionConfiguration();
		r.code           = "runReso";
		r.resolutions    = l;
		r.objectTypeCode = "Run";
		r.typeCodes = Arrays.asList(
				"RHS2000",
				"RHS2500",
				"RHS2500R",
				"RHS4000",
				"RHSX",
				"RMISEQ",
				"RNEXTSEQ500",
				"RNVS6000"    // NGL-1730 ajout 14/12/2017
				);
		
		MongoDBDAO.deleteByCode(InstanceConstants.RESOLUTION_COLL_NAME, ResolutionConfiguration.class, "runReso");
		InstanceHelpers.save(ctx,InstanceConstants.RESOLUTION_COLL_NAME,r, false);
	}
	
	public static void createReadSetResolutionCNG(ContextValidation ctx) {	
		List<Resolution> l = new ArrayList<>();
		
		// Run
		l.addAll(rList(getResolutionCategory("Run"),
				newResolution("Lane abandonnée","Run-abandonLane")));
		
		//Qte
		l.addAll(rList(getResolutionCategory("Qte"),
				newResolution("Nb seq brutes faible",       "Qte-seqRawInsuf"),
				newResolution("Couverture en X hors spec.", "Qte-couverture")));
	
		//IND
		l.addAll(rList(getResolutionCategory("IND"),
				newResolution("Index incorrect", "IND-indexIncorrect")));
				
		//Qlte
		l.addAll(rList(getResolutionCategory("Qlte"),
				newResolution("Q30 hors spec.",         "Qlte-Q30HorsSpec"),
				newResolution("Répartition bases",      "Qlte-repartitionBases"),
				newResolution("% adaptateurs détectés", "Qlte-adapterPercent"),
				newResolution("% duplicat élevé",       "Qlte-duplicatElevee"),	
				newResolution("% NT 30X",               "Qlte-30XntPercent"),
				newResolution("% Target",               "Qlte-targetPercent")));

		// MAP
		l.addAll(rList(getResolutionCategory("MAP"),
				newResolution("% mapping faible", "MAP-PercMappingFble")));
		
		// Sample
		l.addAll(rList(getResolutionCategory("Sample"),
				newResolution("Sexe incorrect", "Sample-sexeIncorrect")));
		
		// Info
		l.addAll(rList(getResolutionCategory("Info"),
				newResolution("Test Dev",      "Info-testDev"),
				newResolution("Test Prod",     "Info-testProd"),
				newResolution("Redo effectué", "Info-redoDone")));
		
		// LIMS
		l.addAll(rList(getResolutionCategory("LIMS"),
				newResolution("erreur Experimental Type","LIMS-erreurExpType")));
		
		ResolutionConfiguration r = new ResolutionConfiguration();
		r.code           = "readSetReso";
		r.resolutions    = l;
		r.objectTypeCode = "ReadSet";
		r.typeCodes = Arrays.asList(
				"default-readset",
				"rsillumina"
				);		
		
		MongoDBDAO.deleteByCode(InstanceConstants.RESOLUTION_COLL_NAME, ResolutionConfiguration.class, "readSetReso");
		InstanceHelpers.save(ctx,InstanceConstants.RESOLUTION_COLL_NAME,r, false);
	}	
	
	public static void createRunResolutionCNS(ContextValidation ctx) {
		List<Resolution> l = new ArrayList<>();
		
		// PbM
		l.addAll(rList(getResolutionCategory("PbM"),
				newResolution("Indéterminé", "PbM-indetermine"),
				newResolution("Chiller",     "PbM-chiller"),
				newResolution("Pelletier",   "PbM-pelletier"),
				newResolution("Fluidique",   "PbM-fluidiq"),
				newResolution("Laser",       "PbM-laser"),
				newResolution("Camera",      "PbM-camera"),
				newResolution("Focus",       "PbM-focus"),    
				newResolution("Pb de vide",  "PbM-pbVide"),
				newResolution("PE module",   "PbM-PEmodule"),
				newResolution("cBot",        "PbM-cBot")));		
			
		// PbR
		l.addAll(rList(getResolutionCategory("PbR"),
				newResolution("Indéterminé",     "PbR-indetermine"),
				newResolution("Flowcell",        "PbR-FC"),
				newResolution("cBot",            "PbR-cBot"),
				newResolution("Séquencage",      "PbR-sequencage"),
				newResolution("Indexing",        "PbR-indexing"),
				newResolution("PE module",       "PbR-PEmodule"),
				newResolution("Rehyb primer R1", "PbR-rehybR1"),
				newResolution("Rehyb indexing",  "PbR-rehybIndexing"),
				newResolution("Rehyb primer R2", "PbR-rehybR2"),
				newResolution("Erreur réactifs", "PbR-erreurReac"),
				newResolution("Rajout réactifs", "PbR-ajoutReac")));

		// SAV
		l.addAll(rList(getResolutionCategory("SAV"),
				newResolution("Intensité",                    "SAV-intensite"),
				newResolution("Densité clusters trop élevée", "SAV-densiteElevee"),
				newResolution("Densité clusters trop faible", "SAV-densiteFaible"),
				newResolution("Densité clusters nulle",       "SAV-densiteNulle"),
				newResolution("%PF",                          "SAV-PF"),
				newResolution("Phasing",                      "SAV-phasing"),
				newResolution("Prephasing",                   "SAV-prephasing"),
				newResolution("Error rate",                   "SAV-errorRate"),
				newResolution("Q30",                          "SAV-Q30"),
				newResolution("Indexing / demultiplexage",    "SAV-IndDemultiplex")));
		
		// PbI
		l.addAll(rList(getResolutionCategory("PbI"),
				newResolution("Indéterminé",            "PbI-indetermine"),
				newResolution("PC",                     "PbI-PC"),
				newResolution("Ecran",                  "PbI-ecran"),
				newResolution("Espace disq insuf",      "PbI-espDisqInsuf"),
				newResolution("Logiciel",               "PbI-logiciel"),
				newResolution("Reboot PC",              "PbI-rebootPC"),
				newResolution("Erreur paramétrage run", "PbI-parametrageRun")));
		
		// Info
		l.addAll(rList(getResolutionCategory("Info"),
				newResolution("Run de validation",  "Info-runValidation"),
				newResolution("Arrêt séquenceur",   "Info-arretSeq"),
				newResolution("Arrêt logiciel",     "Info_arretLogiciel"),
				newResolution("Remboursement",      "Info-remboursement"),
				newResolution("Flowcell redéposée", "Info-FCredeposee")));		
		//BioNano NGL-3201
				l.addAll(rList(getResolutionCategory("BioN"),
						newResolution("problème run / flow cell",  "BioN-pbRunOuFC"),
						newResolution("problème instrument",   "BioN-instrument"),
						newResolution("problème réactifs",     "BioN-reactifs")));	
				
		ResolutionConfiguration r = new ResolutionConfiguration();
		r.code           = "runReso";
		r.resolutions    = l;
		r.objectTypeCode = "Run";
		r.typeCodes = Arrays.asList(
				"RHS2000",
				"RHS2500",
				"RHS2500R",
				"RMISEQ",	
				"RGAIIx",
				// "RARGUS",
				"RMINION",
				"RMKI",
				"RMKIB",
				"RMKIC",
				"RHS4000",
				"RNVS6000",
				"RPROMETHION",
				"RSAPHYR", //BioNano
				"RIRYS" // BioNano
				);
		
		MongoDBDAO.deleteByCode(InstanceConstants.RESOLUTION_COLL_NAME, ResolutionConfiguration.class, "runReso");
		InstanceHelpers.save(ctx,InstanceConstants.RESOLUTION_COLL_NAME,r, false);
	}	
	
	public static void createReadSetResolutionCNS(ContextValidation ctx) {	
		List<Resolution> l = new ArrayList<>();
		
		// Run
		l.addAll(rList(getResolutionCategory("Run"),
				newResolution("Lane abandonnée", "Run-abandonLane")));
		
		// LIB
		l.addAll(rList(getResolutionCategory("LIB"),
				newResolution("Pb protocole banque", "LIB-pbProtocole"),
				newResolution("Erreur dépôt banque", "LIB-erreurDepot"),
				newResolution("Pic d’intérêt minoritaire", "LIB-intoQuestionProfile")));
		
		// Qte
		l.addAll(rList(getResolutionCategory("Qte"),
				newResolution("Seq valides insuf", "Qte-seqValInsuf"),
				newResolution("Seq utiles insuf",  "Qte-seqUtileInsuf")));
		
		// IND
		l.addAll(rList(getResolutionCategory("IND"),
				newResolution("Pb demultiplexage", "IND-pbDemultiplex"),
				newResolution("Pb manip",          					"IND-pbManip"),
				newResolution("% perte demulpltiplexage bq bidée",  "IND-pbDmplxImaireIIdaire")));

		// Qlte
		l.addAll(rList(getResolutionCategory("Qlte"),
				newResolution("Q30",                 "Qlte-Q30"),				
				newResolution("Répartition bases",   "Qlte-repartitionBases"),
				newResolution("Dimère adaptateur",   "Qlte-adapterDimere"),
				newResolution("Adaptateurs/Kmers",   "Qlte-adapterKmer"),		
				newResolution("Duplicat pairs > 20", "Qlte-duplicatPairs"),
				newResolution("Duplicat > 30",       "Qlte-duplicat"),
				newResolution("Score qualité moyen", "Qlte-qualityScore")));
		
		// TAXO
		l.addAll(rList(getResolutionCategory("TAXO"),
				newResolution("Conta indéterminée",   "TAXO-contaIndeterm"),
				newResolution("Conta manip",          "TAXO-contaManip"),
				newResolution("Conta mat ori",        "TAXO-contaMatOri"),
				newResolution("Non conforme",         "TAXO-nonConforme"),
				newResolution("Mitochondrie",         "TAXO-mitochondrie"),
				newResolution("Chloroplast",          "TAXO-chloroplast"),
				newResolution("Virus",                "TAXO-virus"),
				newResolution("Bactérie",             "TAXO-bacteria"), 
				newResolution("Fungi",                "TAXO-fungi"),
				newResolution("OK post clean rRNA",   "TAXO-postCleanrRNA"),
				newResolution("Contaminant amplicon", "TAXO-contaAmplicon"),
				newResolution("% Hominidae",          "TAXO-hominidae")));
		
			
		// RIBO
		l.addAll(rList(getResolutionCategory("RIBO"),
				newResolution("% rRNA élevé", "RIBO-percEleve")));
		
		// MAP
		l.addAll(rList(getResolutionCategory("MAP"),
				newResolution("% MP",              "MAP-PercentMP"),
				newResolution("Taille moyenne MP", "MAP-tailleMP")));
		
		// MERG
		l.addAll(rList(getResolutionCategory("MERG"),
				newResolution("% lec mergées",             "MERG-PercLecMerg"),
				newResolution("Médiane lect mergées",      "MERG-MedLecMerg"),
				newResolution("Distribution lect mergées", "MERG-Distribution")));
	
		// Info
		l.addAll(rList(getResolutionCategory("Info"),
				newResolution("Test Dev",                       "Info-testDev"),
				newResolution("Nouveaux critères d'évaluation", "Info-nvoCritereEval")));

		//Dataset BIONANO
		l.addAll(rList(getResolutionCategory("dsBioN"),
				newResolution("taux de labelling insuffisant",   "BioN-labelling"),
				newResolution("taille molécules insuffisante",          "BioN-tailleMolecule"),
				newResolution("problème réactifs",        "BioN-reactifs"),
				newResolution("problème échantillon",        "BioN-echantillon"),
				newResolution("problème MQR",        "BioN-MQR"),
				newResolution("assemblage",          "BioN-assemblage")));

		ResolutionConfiguration r = new ResolutionConfiguration();
		r.code           = "readSetReso";
		r.resolutions    = l;
		r.objectTypeCode = "ReadSet";
		r.typeCodes = Arrays.asList(
				"default-readset",
				"rsillumina",
				"rsnanopore",
				"rsbionano"
				);
		
		MongoDBDAO.deleteByCode(InstanceConstants.RESOLUTION_COLL_NAME, ResolutionConfiguration.class, "readSetReso");
		InstanceHelpers.save(ctx,InstanceConstants.RESOLUTION_COLL_NAME,r, false);
	}	
	
	public static void createAnalysisResolutionCNS(ContextValidation ctx) {
		List<Resolution> l = new ArrayList<>();
		
		// BA-MERG
		l.addAll(rList(getResolutionCategory("BA-MERG"),
				newResolution("% merging",  "MERG-BA-MERGPercent"),
				newResolution("reads size", "MERG-readSize")));
		
		// CTG
		l.addAll(rList(getResolutionCategory("CTG"),
				newResolution("N50",             "CTG-N50"),
				newResolution("Cumul",           "CTG-cumul"),
				newResolution("Nb contigs",      "CTG-nbCtgs"),
				newResolution("Max size",        "CTG-maxSize"),
				newResolution("Assembled reads", "CTG-assReads")));
		
		// SIZE
		l.addAll(rList(getResolutionCategory("SIZE"),
				newResolution("% lost bases", "SIZE-lostBasesPerc")));
		
		// SCAFF
		l.addAll(rList(getResolutionCategory("SCAFF"),
				newResolution("N50",                "SCAFF-N50"),
				newResolution("Cumul",              "SCAFF-cumul"),
				newResolution("Nb scaff",           "SCAFF-nbScaff"),
				newResolution("Max size",           "SCAFF-maxSize"),
				newResolution("Median insert size", "SCAFF-medInsertSize"),
				newResolution("% satisfied pairs",  "SCAFF-satisfPairsPerc"),
				newResolution("% N",                "SCAFF-Npercent")));
		
		// GAP
		l.addAll(rList(getResolutionCategory("GAP"),
				newResolution("Gap sum",             "GAP-sum"),
				newResolution("Gap count",           "GAP-count"),
				newResolution("Corrected gap sum",   "GAP-correctedSum"),
				newResolution("Corrected gap count", "GAP-correctedCount"),
				newResolution("% N",                 "GAP-Npercent")));
		
		ResolutionConfiguration r = new ResolutionConfiguration();
		r.code           = "analysisReso";
		r.resolutions    = l;
		r.objectTypeCode = "Analysis";
		r.typeCodes = Arrays.asList(
				"BPA"
				);
		
		MongoDBDAO.deleteByCode(InstanceConstants.RESOLUTION_COLL_NAME, ResolutionConfiguration.class, "analysisReso");
		InstanceHelpers.save(ctx,InstanceConstants.RESOLUTION_COLL_NAME,r, false);
	}

	// Commune CNS/CNG
	public static void createExperimentResolution(ContextValidation ctx) {	
		List<Resolution> l = getDefaultResolutionCNS();
				
		ResolutionConfiguration r = new ResolutionConfiguration();
		r.code           = "experimentReso";
		r.resolutions    = l;
		r.objectTypeCode = "Experiment";
		ArrayList<String> al = new ArrayList<>(); 
		
		MongoDBDAO.deleteByCode(InstanceConstants.RESOLUTION_COLL_NAME, ResolutionConfiguration.class,r.code);
		// JacksonDBCollection 'distinct' return type is a raw List, this produces a warning
		// with javac but not with eclipse.
		List<String> typeCodes = 
				MongoDBDAO.getCollection(InstanceConstants.RESOLUTION_COLL_NAME, ResolutionConfiguration.class)
				          .distinct("typeCodes");
		try {
			List<ExperimentType> expTypes = ExperimentType.find.get().findAll();
			for (ExperimentType expType : expTypes) {
				if (typeCodes == null || !typeCodes.contains(expType.code)) {
					logger.debug("Add experimentType default resolution "+ expType.code);
					al.add(expType.code);
				}	
			}
		} catch (DAOException e) {
			logger.error("Creation Resolution for ExperimentType error " + e.getMessage());
		}
		r.typeCodes = al;
		
		ctx.setCreationMode();
		InstanceHelpers.save(ctx, InstanceConstants.RESOLUTION_COLL_NAME, r, false);
	}	

	public static void createPurifPrecipitationResolutionCNS(ContextValidation ctx) {
		List<Resolution> l = new ArrayList<>();
		
		l.addAll(rList(getResolutionCategory("Default"),
				getDefaultResolutionCNS(),
				newResolution("Purification sur surnageant", "purification-surnageant")));	
		
		ResolutionConfiguration r = new ResolutionConfiguration();
		r.code           = "expPurifPreciReso";
		r.resolutions    = l;
		r.objectTypeCode = "Experiment";
		r.typeCodes = Arrays.asList(
			"precipitation-purification"
		);		
		
		MongoDBDAO.deleteByCode(InstanceConstants.RESOLUTION_COLL_NAME, ResolutionConfiguration.class, r.code);
		InstanceHelpers.save(ctx, InstanceConstants.RESOLUTION_COLL_NAME,r, false);
	}

	public static void createOpgenDepotResolutionCNS(ContextValidation ctx) {
		List<Resolution> l = new ArrayList<>();
		
		l.addAll(rList(getResolutionCategory("Default"),
				getDefaultResolutionCNS(),
				newResolution("Nombre molécules insuffisant pour assemblage correct", "echec-nbMoleculesInsuf"),
				newResolution("Surface cassée",                                       "echec-surface"),	
				newResolution("Problème digestion",                                   "echec-digestion")));	
		
		ResolutionConfiguration r = new ResolutionConfiguration();
		r.code           = "expODReso";
		r.resolutions    = l;
		r.objectTypeCode = "Experiment";
		r.typeCodes = Arrays.asList(
				"void-opgen-depot",
				"opgen-depot"
				);		
		
		MongoDBDAO.deleteByCode(InstanceConstants.RESOLUTION_COLL_NAME, ResolutionConfiguration.class, r.code);
		InstanceHelpers.save(ctx, InstanceConstants.RESOLUTION_COLL_NAME,r, false);
	}
	
	public static void createIlluminaPrepFCDepotResolutionCNG(ContextValidation ctx) {
		List<Resolution> l = new ArrayList<>();
		
		l.addAll(rList(getResolutionCategory("Default"),
				getDefaultResolutionCNS(),
				newResolution("Réhybridation FC", "rehyb-FC")));
		
		ResolutionConfiguration r = new ResolutionConfiguration();
		r.code           = "expIPDReso";
		r.resolutions    = l;
		r.objectTypeCode = "Experiment";
		r.typeCodes = Arrays.asList(
				"ext-to-prepa-flowcell",
				"prepa-flowcell",		
				"ext-to-prepa-fc-ordered", //FDS ajout 10/11/2015  -- JIRA NGL-838
				"prepa-fc-ordered",	       //FDS ajout 10/11/2015  -- JIRA NGL-838
				"illumina-depot"
				);	
		
		MongoDBDAO.deleteByCode(InstanceConstants.RESOLUTION_COLL_NAME, ResolutionConfiguration.class, r.code);
		InstanceHelpers.save(ctx, InstanceConstants.RESOLUTION_COLL_NAME,r, false);
	}
	
	public static void createIlluminaPrepFCDepotResolutionCNS(ContextValidation ctx) {
		List<Resolution> l = new ArrayList<>();
		
		l.addAll(rList(getResolutionCategory("Default"),
				getDefaultResolutionCNS(),
				newResolution("Réhybridation FC", "rehyb-FC")));
		
		ResolutionConfiguration r = new ResolutionConfiguration();
		r.code           = "expIPDReso";
		r.resolutions    = l;
		r.objectTypeCode = "Experiment";
		r.typeCodes = Arrays.asList(
				"ext-to-prepa-flowcell",
				"prepa-flowcell",	
				"prepa-fc-ordered",	
				"illumina-depot"
				);	
		
		MongoDBDAO.deleteByCode(InstanceConstants.RESOLUTION_COLL_NAME, ResolutionConfiguration.class, r.code);
		InstanceHelpers.save(ctx, InstanceConstants.RESOLUTION_COLL_NAME,r, false);
	}
	
	private static void createIryPreparationNLRSResolutionCNS(ContextValidation ctx) {
		List<Resolution> l = new ArrayList<>();
		
		l.addAll(rList(getResolutionCategory("Default"),
				getDefaultResolutionCNS(),
				newResolution("Marquage incorrect",             "echec-labeling"),
				newResolution("Hors gamme",                     "out-of-range"),
				newResolution("Conc. < 5 : over-staining risk", "over-staining-risk"),
				newResolution("Conc. > 9 : over-loading risk",  "over-loading-risk")));
		
		ResolutionConfiguration r = new ResolutionConfiguration();
		r.code           = "expIrysPrepNLRSReso";
		r.resolutions    = l;
		r.objectTypeCode = "Experiment";
		r.typeCodes = Arrays.asList(
				"irys-nlrs-prep",
				"bionano-dls-prep"
				);
		
		MongoDBDAO.deleteByCode(InstanceConstants.RESOLUTION_COLL_NAME, ResolutionConfiguration.class, r.code);
		InstanceHelpers.save(ctx, InstanceConstants.RESOLUTION_COLL_NAME,r, false);
	}
	
	private static void createDepotBionanoResolutionCNS(ContextValidation ctx) {
		List<Resolution> l = new ArrayList<>();
		
		l.addAll(rList(getResolutionCategory("Default"),
				getDefaultResolutionCNS(),
				newResolution("Nb cycles insuffisant",                        "echec-nbCycleInsuf"),
				newResolution("Problème passage des molécules région pillar", "echec-pillarRegion"),
				newResolution("Labelling incorrect",                          "echec-labeling"),
				newResolution("Utilisation du NanoAnalyzer",                  "nanoAnalyzer")));
			
		ResolutionConfiguration r = new ResolutionConfiguration();
		r.code           = "expDepotBionanoReso";
		r.resolutions    = l;
		r.objectTypeCode = "Experiment";
		r.typeCodes = Arrays.asList(
				"bionano-depot"
				);
		
		MongoDBDAO.deleteByCode(InstanceConstants.RESOLUTION_COLL_NAME, ResolutionConfiguration.class, r.code);
		InstanceHelpers.save(ctx, InstanceConstants.RESOLUTION_COLL_NAME,r, false);	
	}
	
	//NGL-2837
	private static void createDepotNanoporeResolutionCNS(ContextValidation ctx) {
		List<Resolution> l = new ArrayList<>();
		
		l.addAll(rList(getResolutionCategory("Default"),
				getDefaultResolutionCNS(),
				newResolution("Nuclease flush", "nuclease-flush"),
				newResolution("Refueling", "refueling"),
				newResolution("Détails en commentaire", "comments-details")));
			
		ResolutionConfiguration r = new ResolutionConfiguration();
		r.code           = "expDepotNanoporeReso";
		r.resolutions    = l;
		r.objectTypeCode = "Experiment";
		r.typeCodes = Arrays.asList(
				"nanopore-depot"
				);
		
		MongoDBDAO.deleteByCode(InstanceConstants.RESOLUTION_COLL_NAME, ResolutionConfiguration.class, r.code);
		InstanceHelpers.save(ctx, InstanceConstants.RESOLUTION_COLL_NAME,r, false);	
	}
	
	
	private static void createSamplePrepResolutionCNS(ContextValidation ctx) {
		List<Resolution> l = new ArrayList<>();
		
		l.addAll(rList(getResolutionCategory("Default"),
				getDefaultResolutionCNS(),
				newResolution("Tube cassé dans cryobroyeur", "broken-tube-in-freezer-mill"),
				newResolution("Tube vide",                   "empty-tube"),
				newResolution("Colonne élution bouchée",     "elution-column-blocked")));

		ResolutionConfiguration r = new ResolutionConfiguration();
		r.code           = "expExtractionDNARNAReso";
		r.resolutions    = l;
		r.objectTypeCode = "Experiment";
		r.typeCodes = Arrays.asList(
				"dna-rna-extraction"
				);		
		
		MongoDBDAO.deleteByCode(InstanceConstants.RESOLUTION_COLL_NAME, ResolutionConfiguration.class, r.code);
		InstanceHelpers.save(ctx, InstanceConstants.RESOLUTION_COLL_NAME,r, false);
		
		l = new ArrayList<>();

		l.addAll(getDefaultResolutionCNS());	

		r = new ResolutionConfiguration();
		r.code           = "expBroyageReso";
		r.resolutions    = l;
		r.objectTypeCode = "Experiment";
		r.typeCodes = Arrays.asList(
				"grinding"
				);
		
		MongoDBDAO.deleteByCode(InstanceConstants.RESOLUTION_COLL_NAME, ResolutionConfiguration.class, r.code);
		InstanceHelpers.save(ctx, InstanceConstants.RESOLUTION_COLL_NAME,r, false);	
	}
	
	private static void createGelMigrationResolutionCNS(ContextValidation ctx) {
		List<Resolution> l = new ArrayList<>();
		
		l.addAll(rList(resolutionCategories.get("Default"),
				getDefaultResolutionCNS(),
				newResolution("Tâche de faible poids moléculaire", "low-molecular-weight-spot"),
				newResolution("Contamination ARN",                 "rna-contamination"),
				newResolution("ADN dégradé",                       "degraded-dna"),
				newResolution("MétaGénome",                        "metagenome"),
				newResolution("Présence de plasmide(s)",           "plasmid-presence"),
				newResolution("Profil inhabituel",                 "unusual-profile")));
		
		ResolutionConfiguration r = new ResolutionConfiguration();
		r.code           = "expGelMigrationReso";
		r.resolutions    = l;
		r.objectTypeCode = "Experiment";
		r.typeCodes = Arrays.asList(
				"gel-migration"
				);
		
		MongoDBDAO.deleteByCode(InstanceConstants.RESOLUTION_COLL_NAME, ResolutionConfiguration.class, r.code);
		InstanceHelpers.save(ctx, InstanceConstants.RESOLUTION_COLL_NAME,r, false);
	}
	
	// FDS 05/02/2016 -- JIRA NGL-894 experience processus X5
	private static void createPrepPcrFreeResolutionCNG(ContextValidation ctx) {
		List<Resolution> l = new ArrayList<>();
		
		l.addAll(rList(resolutionCategories.get("Default"),
				getDefaultResolutionCNS(),
				newResolution("Echec échantillons par puits", "echec-echPuit"),
				newResolution("Contamination",                "contamination")));
		
		ResolutionConfiguration r = new ResolutionConfiguration();
		r.code           = "expPrepPcrFreeReso";
		r.resolutions    = l;
		r.objectTypeCode = "Experiment";
		r.typeCodes = Arrays.asList(
				"prep-pcr-free"
				); 
		
		MongoDBDAO.deleteByCode(InstanceConstants.RESOLUTION_COLL_NAME, ResolutionConfiguration.class, r.code);
		InstanceHelpers.save(ctx, InstanceConstants.RESOLUTION_COLL_NAME,r, false);	
	}
	
	private static void createQCMiseqResolutionCNG(ContextValidation ctx) {
		List<Resolution> l = new ArrayList<>();
		
		l.addAll(rList(getResolutionCategory("Default"),
				getDefaultResolutionCNS(),
				newResolution("Run Miseq invalide : résultats non importés", "invalid-miseq-run")));
		
		ResolutionConfiguration r = new ResolutionConfiguration();
		r.code           = "expMiseqQCReso";
		r.resolutions    = l;
		r.objectTypeCode = "Experiment";
		r.typeCodes = Arrays.asList(
				"miseq-qc"
				); 
		
		MongoDBDAO.deleteByCode(InstanceConstants.RESOLUTION_COLL_NAME, ResolutionConfiguration.class, r.code);
		InstanceHelpers.save(ctx, InstanceConstants.RESOLUTION_COLL_NAME,r, false);	
	}
	
	// FDS 23/11/2016 NGL-1158: renommage pour separation des resolutions de Processus entre CNG et CNS
	// Seems that the original data has entry 21 defined twice.
	public static void createProcessResolutionCNS(ContextValidation ctx) {
		List<Resolution> l = new ArrayList<>();
		
		l.addAll(rList(getResolutionCategory("Default"),
				newResolution("Déroulement correct",                 "correct"),
				newResolution("Standby",                             "standby"),
				newResolution("Attente retour collaborateur pour la suite","next-experiments-waiting-for-collab"),
				newResolution("Arrêt - réorientation manip",         "stop-reor-manip"),
				newResolution("Arrêt - abandon",                     "stop-abandon"),
				newResolution("Arrêt - pb broyage",                  "stop-pb-broyage"),
				newResolution("Arrêt - pb cryobroyeur",              "stop-pb-cryobroyeur"),
				newResolution("Arrêt - pb extraction ADN/ARN",       "stop-pb-extraction"),
				newResolution("Arrêt - pb bq RNA",                   "stop-pb-bq-rna"),
				newResolution("Arrêt - pb synthèse cDNA",            "stop-pb-synthese-cdna"),
				newResolution("Arrêt - pb fragmentation",            "stop-pb-fragmentation"),
				newResolution("Arrêt - pb prep Tag",                 "stop-pb-prep-tag"),
				newResolution("Arrêt - pb bq DNA",                   "stop-pb-bq-dna"),
				newResolution("Arrêt - pb PCR amplif",               "stop-pb-pcr-ampli"),
				newResolution("Arrêt - pb sizing sur gel",           "stop-pb-sizing-gel"),
				newResolution("Arrêt - pb Ampure/SpriSelect",        "stop-pb-ampure-spriselect"),
				newResolution("Arrêt - pb sol stock",                "stop-pb-sol-stock"),
				newResolution("Arrêt - échec run",                   "stop-pb-run"),
				newResolution("Processus partiel", "processus-partiel"),
				newResolution("Arrêt - erreur déclaration",          "stop-pb-declaration"),
				newResolution("Arrêt - Rendement trop faible",       "stop-pb-yield-too-low"),
				newResolution("Arrêt - pour séquençage Sanger",      "stop-seq-sanger"),
				newResolution("Arrêt - erreur manip",                "stop-pb-experiment")));
		
		ResolutionConfiguration r = new ResolutionConfiguration();
		r.code           = "processReso";
		r.resolutions    = l;
		r.objectTypeCode = "Process";
		
		ArrayList<String> al = new ArrayList<>();
		
		try {
			List<ProcessType> processTypes = ProcessType.find.get().findAll();
			for(ProcessType processType:processTypes){
				logger.debug("Add processType default resolution "+ processType.code);
				al.add(processType.code);
			}
		} catch (DAOException e) {
			logger.error("Creation Resolution for Process Type error "+e.getMessage());
		}
		r.typeCodes = al;
		
		MongoDBDAO.deleteByCode(InstanceConstants.RESOLUTION_COLL_NAME, ResolutionConfiguration.class, r.code);
		InstanceHelpers.save(ctx, InstanceConstants.RESOLUTION_COLL_NAME,r, false);
	}
	
	// FDS 23/11/2016 NGL-1158: creation pour separation des resolutions de Processus entre CNG et CNS
	public static void createProcessResolutionCNG(ContextValidation ctx) {
		List<Resolution> l = new ArrayList<>();

		l.addAll(rList(getResolutionCategory("Default"),
				//newResolution("Déroulement correct",          "correct"), ...pour l'instant pas demandée
				//newResolution("Processus partiel",            "processus-partiel"), ...pour l'instant pas demandée
				newResolution("REDO",                       "stop-redo"),
				newResolution("concentration insuffisante", "stop-conc-insuffisante"),
				newResolution("problème profil",            "stop-pb-profil"),
				newResolution("problème technique",         "stop-pb-technique"),
				newResolution("contamination",              "stop-contamination"),
				newResolution("sauvegarde",                 "stop-backup")));   // 29/08/2017 ajout.... nom et codes exacts a définir....
		
		ResolutionConfiguration r = new ResolutionConfiguration();
		r.code           = "processReso";
		r.resolutions    = l;
		r.objectTypeCode = "Process";
		
		ArrayList<String> al = new ArrayList<>();
		
		try {
			List<ProcessType> processTypes = ProcessType.find.get().findAll();
			for (ProcessType processType:processTypes) {
					logger.debug("Add processType default resolution "+ processType.code);
					al.add(processType.code);
			}
		} catch (DAOException e) {
			logger.error("Creation Resolution for Process Type error "+e.getMessage());
		}
		r.typeCodes = al;
		
		MongoDBDAO.deleteByCode(InstanceConstants.RESOLUTION_COLL_NAME, ResolutionConfiguration.class, r.code);
		InstanceHelpers.save(ctx, InstanceConstants.RESOLUTION_COLL_NAME,r, false);
	}
	
	// !! attention commune CNS/CNG
	public static List<Resolution> getDefaultResolutionCNS() {
		return rList(getResolutionCategory("Default"),
				newResolution("Déroulement correct",             "correct"),
				newResolution("Problème signalé en commentaire", "pb-commentaire"),
				newResolution("Echec expérience",                "echec-experience"));	
	}
	
	public static void createContainerResolutionCNG(ContextValidation ctx) {
		List<Resolution> l = new ArrayList<>();
		
		l.addAll(rList(getResolutionCategory("Default"),
				newResolution("Epuisé",                "empty"),
				newResolution("Renvoyé collaborateur", "return-collab")));
		
		ResolutionConfiguration r = new ResolutionConfiguration();
		r.code           = "containerReso";
		r.resolutions    = l;
		r.objectTypeCode = "Container";
		
		MongoDBDAO.deleteByCode(InstanceConstants.RESOLUTION_COLL_NAME, ResolutionConfiguration.class, r.code);
		InstanceHelpers.save(ctx, InstanceConstants.RESOLUTION_COLL_NAME,r, false);
	}
	
	public static void createContainerResolutionCNS(ContextValidation ctx) {
		List<Resolution> l = new ArrayList<>();

		l.addAll(rList(getResolutionCategory("Default"),
				newResolution("En attente d'objectif", "iw-objective"),
				newResolution("Sauvegarde prod",       "prod-backup"),
				newResolution("Epuisé",                "empty"),
				newResolution("Jeté",                  "trash"),
				newResolution("Renvoyé collaborateur", "return-collab"),
				newResolution("Problème manip", "experimental-problem"),
				newResolution("Jamais reçu", "never-received"),
				newResolution("Non valide mais forcé en prod", "invalid-to-continue")
				));
		
		ResolutionConfiguration r = new ResolutionConfiguration();
		r.code           = "containerReso";
		r.resolutions    = l;
		r.objectTypeCode = "Container";
		
		MongoDBDAO.deleteByCode(InstanceConstants.RESOLUTION_COLL_NAME, ResolutionConfiguration.class, r.code);
		InstanceHelpers.save(ctx, InstanceConstants.RESOLUTION_COLL_NAME,r, false);
	}

	// Checked category access by name
	private static ResolutionCategory getResolutionCategory(String name) {
		ResolutionCategory rc = resolutionCategories.get(name);
		if (rc == null)
			throw new RuntimeException("undefined resolution category '" + name + "'");
		return rc;
	}

	// -- syntax lightening effort --
	
	// Incomplete resolution definition, to be completed later using rList
	private static Resolution newResolution(String name, String code) {
		return new Resolution(name, code, null, (short)-1);
	}
	
	private static List<Resolution> rList(ResolutionCategory rc, Resolution... resolutions) {
		return rList(rc, Arrays.asList(resolutions));
	}
	
	// Allows light syntax merging
	private static List<Resolution> rList(ResolutionCategory rc, List<Resolution> rs0, Resolution... rs1) {
		List<Resolution> result = new ArrayList<>();
		result.addAll(rs0);
		result.addAll(Arrays.asList(rs1));
		return rList(rc, result);
	}

	// Sets the resolution category and assigns element numbers starting at 1.
	private static List<Resolution> rList(ResolutionCategory rc, List<Resolution> resolutions) {
		if (rc == null)
			throw new IllegalArgumentException();
		for (int i=0; i<resolutions.size(); i++) {
			Resolution r = resolutions.get(i);
			r.category     = rc;
			r.displayOrder = (short)(i+1);
		}
		return resolutions;
	}

}
