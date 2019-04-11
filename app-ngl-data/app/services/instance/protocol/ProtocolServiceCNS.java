package services.instance.protocol;

import static services.instance.InstanceFactory.newPSV;
import static services.instance.InstanceFactory.newProtocol;

import java.util.ArrayList;
//import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mongojack.DBQuery;

//import com.google.common.collect.Maps;
import com.typesafe.config.ConfigFactory;

import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.protocol.instance.Protocol;
import models.utils.InstanceConstants;
import models.utils.InstanceHelpers;
//import play.Logger;
//import scala.collection.script.Remove;
import services.instance.InstanceFactory;
import validation.ContextValidation;

public class ProtocolServiceCNS {   

    private static final play.Logger.ALogger logger = play.Logger.of(ProtocolServiceCNS.class);

    private final static String institute = "CNS";

    public static void main(ContextValidation ctx) {    
        logger.info("Start to create protocols collection for "+institute+"...");
        logger.info("Remove protocol");
        removeProtocols(ctx);
        logger.info("Save protocols ...");
        saveProtocols(ctx);
        logger.info(institute+" Protocols collection creation is done!");
    }

    private static void removeProtocols(ContextValidation ctx) {
        MongoDBDAO.delete(InstanceConstants.PROTOCOL_COLL_NAME, Protocol.class, DBQuery.empty());
    }

    public static void saveProtocols(ContextValidation ctx){        
        List<Protocol> lp = new ArrayList<>();
       
        lp.add(newProtocol("depot_opgen_ptr_1",         "Depot_Opgen_prt_1",        "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("opgen-depot")));

        lp.add(newProtocol("hiseq2000_Illumina",        "HiSeq2000_Illumina",       "path4", "1", "production", InstanceFactory.setExperimentTypeCodes("illumina-depot")));
        lp.add(newProtocol("hiseq2500_fast_Illumina",   "HiSeq2500_fast_Illumina",  "path5", "1", "production", InstanceFactory.setExperimentTypeCodes("illumina-depot")));
        lp.add(newProtocol("hiseq2500_Illumina",        "HiSeq2500_Illumina",       "path6", "1", "production", InstanceFactory.setExperimentTypeCodes("illumina-depot")));
        lp.add(newProtocol("ptr_sox147_v1_depot",       "PTR_SOX147_v1",            "path6", "1", "production", InstanceFactory.setExperimentTypeCodes("illumina-depot")));

        lp.add(newProtocol("prepfc_cbot_ptr_sox139_1",      "PrepFC_CBot_ptr_sox139_1",     "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("prepa-flowcell")));
        lp.add(newProtocol("cbot_rapid_run_2500_Illumina",  "CBot_rapid_run_2500_Illumina", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("prepa-flowcell")));
        lp.add(newProtocol("ptr_sox147_v1",                 "PTR_SOX147_v1",                "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("prepa-flowcell")));

        lp.add(newProtocol("hiseq_4000_system_guide", "Illumina Hiseq 4000 system guide", "path8", "1", "production", InstanceFactory.setExperimentTypeCodes("prepa-fc-ordered", "illumina-depot")));
        
        //NovaSeq
        lp.add(newProtocol("Illumina NovaSeq 6000 system guide", "Illumina NovaSeq 6000 system guide", "path8", "1", "production", InstanceFactory.setExperimentTypeCodes("prepa-fc-ordered", "illumina-depot")));    


        //Nanopore
        lp.add(newProtocol("R9-1D-ligation",            "R9-1D ligation",               "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-library", "nanopore-final-ligation"), concatMap(newPSV("libraryProtocol", "R9-1D ligation")),false));
        lp.add(newProtocol("R9-1D-transposition",       "R9-1D transposition",          "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-library", "nanopore-final-ligation"), concatMap(newPSV("libraryProtocol", "R9-1D transposition")),false));
        lp.add(newProtocol("R9-Long-Read 1D",           "R9-Long Read 1D",              "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-library", "nanopore-final-ligation"), concatMap(newPSV("libraryProtocol", "R9-Long Read 1D")),false));
        lp.add(newProtocol("R9-Long-Read 2D",           "R9-Long Read 2D",              "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-library", "nanopore-final-ligation"), concatMap(newPSV("libraryProtocol", "R9-Long Read 2D")),false));
        lp.add(newProtocol("R9-Low-input",              "R9-Low input",                 "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-library", "nanopore-final-ligation"), concatMap(newPSV("libraryProtocol", "R9-Low input")),false));
        lp.add(newProtocol("R9-2D",                     "R9-2D",                        "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-library", "nanopore-final-ligation"), concatMap(newPSV("libraryProtocol", "R9-2D")),false));
        lp.add(newProtocol("1D2-library",               "Banque 1D²",                   "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-library", "nanopore-final-ligation"), concatMap(newPSV("libraryProtocol", "Banque 1D²")),false));

        lp.add(newProtocol("R9-depot",                  "R9-dépôt",                     "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-depot"),false));
        lp.add(newProtocol("R9-depot-SpotON",           "R9-dépôt-SpotON",              "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-depot"),false));

        lp.add(newProtocol("R9-on-bead-depot",          "R9-dépôt sur billes",          "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-depot"),false));
        lp.add(newProtocol("R9-on-bead-spotOn-depot",   "R9-dépôt-SpotON sur billes",   "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-depot"),false));

        lp.add(newProtocol("PromethION_DEV",            "PromethION_DEV",               "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-depot"),false));

        lp.add(newProtocol("genomic-dna-ligation-sqk-lsk109-lfb-1d", "1D Genomic DNA by Ligation (SQK-LSK109)_LFB", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-reparation-and-end-prep", "nanopore-frg", "nanopore-depot", "nanopore-final-ligation"), concatMap(newPSV("libraryProtocol", "LSK109")),false));
        lp.add(newProtocol("genomic-dna-ligation-sqk-lsk109-sfb-1d", "1D Genomic DNA by Ligation (SQK-LSK109)_SFB", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-reparation-and-end-prep", "nanopore-frg", "nanopore-depot", "nanopore-final-ligation"), concatMap(newPSV("libraryProtocol", "LSK109")),false));

        lp.add(newProtocol("genomic-dna-ligation-sqk-lsk109-promethion-1d",             "1D Genomic DNA by Ligation (SQK-LSK109) - PromethION",             "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-frg", "nanopore-dna-reparation", "nanopore-reparation-and-end-prep", "nanopore-end-prep", "nanopore-pre-pcr-ligation", "nanopore-pcr", "nanopore-barcode-ligation", "nanopore-final-ligation", "nanopore-library", "nanopore-depot"), concatMap(newPSV("libraryProtocol", "1D Genomic DNA by Ligation (SQK-LSK109) - PromethION"))));
        lp.add(newProtocol("genomic-dna-ligation-sqk-kit9chemistry-promethion-1d",      "1D Genomic DNA by ligation (Kit 9 chemistry) - PromethION",        "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-frg", "nanopore-dna-reparation", "nanopore-reparation-and-end-prep", "nanopore-end-prep", "nanopore-pre-pcr-ligation", "nanopore-pcr", "nanopore-barcode-ligation", "nanopore-final-ligation", "nanopore-library", "nanopore-depot"), concatMap(newPSV("libraryProtocol", "1D Genomic DNA by ligation (Kit 9 chemistry) - PromethION"))));
        lp.add(newProtocol("genomic-dna-ligation-sqk-lsk108-promethion-1d",             "1D Genomic DNA by ligation (SQK-LSK108) - PromethION",             "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-frg", "nanopore-dna-reparation", "nanopore-reparation-and-end-prep", "nanopore-end-prep", "nanopore-pre-pcr-ligation", "nanopore-pcr", "nanopore-barcode-ligation", "nanopore-final-ligation", "nanopore-library", "nanopore-depot"), concatMap(newPSV("libraryProtocol", "1D Genomic DNA by ligation (SQK-LSK108) - PromethION"))));
        lp.add(newProtocol("gdna-selecting-long-reads-sqk-lsk108-1d",                   "1D gDNA selecting for long reads (SQK-LSK108)",                    "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-frg", "nanopore-dna-reparation", "nanopore-reparation-and-end-prep", "nanopore-end-prep", "nanopore-pre-pcr-ligation", "nanopore-pcr", "nanopore-barcode-ligation", "nanopore-final-ligation", "nanopore-library", "nanopore-depot"), concatMap(newPSV("libraryProtocol", "1D gDNA selecting for long reads (SQK-LSK108)"))));
        lp.add(newProtocol("gdna-selecting-long-reads-sqk-lsk109-1d",                   "1D gDNA selecting for long reads (SQK-LSK109)",                    "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-frg", "nanopore-dna-reparation", "nanopore-reparation-and-end-prep", "nanopore-end-prep", "nanopore-pre-pcr-ligation", "nanopore-pcr", "nanopore-barcode-ligation", "nanopore-final-ligation", "nanopore-library", "nanopore-depot"), concatMap(newPSV("libraryProtocol", "1D gDNA selecting for long reads (SQK-LSK109)"))));
        lp.add(newProtocol("gdna-long-reads-without-bluepippin-sqk-lsk108-1d",          "1D gDNA long reads without BluePippin (SQK-LSK108)",               "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-frg", "nanopore-dna-reparation", "nanopore-reparation-and-end-prep", "nanopore-end-prep", "nanopore-pre-pcr-ligation", "nanopore-pcr", "nanopore-barcode-ligation", "nanopore-final-ligation", "nanopore-library", "nanopore-depot"), concatMap(newPSV("libraryProtocol", "1D gDNA long reads without BluePippin (SQK-LSK108)"))));
        lp.add(newProtocol("native-barcoding-gdna-with-exp-nbd103-and-sqk-lsk108-1d",   "1D Native barcoding genomic DNA (with EXP-NBD103 and SQK-LSK108)", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-frg", "nanopore-dna-reparation", "nanopore-reparation-and-end-prep", "nanopore-end-prep", "nanopore-pre-pcr-ligation", "nanopore-pcr", "nanopore-barcode-ligation", "nanopore-final-ligation", "nanopore-library", "nanopore-depot"), concatMap(newPSV("libraryProtocol", "1D Native barcoding genomic DNA (with EXP-NBD103 and SQK-LSK108)"))));
        lp.add(newProtocol("native-barcoding-gdna-with-exp-nbd103-and-sqk-lsk109-1d",   "1D Native barcoding genomic DNA (with EXP-NBD103 and SQK-LSK109)", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-frg", "nanopore-dna-reparation", "nanopore-reparation-and-end-prep", "nanopore-end-prep", "nanopore-pre-pcr-ligation", "nanopore-pcr", "nanopore-barcode-ligation", "nanopore-final-ligation", "nanopore-library", "nanopore-depot"), concatMap(newPSV("libraryProtocol", "1D Native barcoding genomic DNA (with EXP-NBD103 and SQK-LSK109)"))));
        lp.add(newProtocol("native-barcoding-gdna-with-exp-nbd104-and-sqk-lsk109-1d",   "1D Native barcoding genomic DNA (with EXP-NBD104 and SQK-LSK109)", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-frg", "nanopore-dna-reparation", "nanopore-reparation-and-end-prep", "nanopore-end-prep", "nanopore-pre-pcr-ligation", "nanopore-pcr", "nanopore-barcode-ligation", "nanopore-final-ligation", "nanopore-library", "nanopore-depot"), concatMap(newPSV("libraryProtocol", "1D Native barcoding genomic DNA (with EXP-NBD104 and SQK-LSK109)"))));
        
        
        lp.add(newProtocol("cdna-pcr-sequencing-sqk-pcs-108)",                          "cDNA-PCR Sequencing (SQK-PCS108)",                                 "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-frg", "nanopore-dna-reparation", "nanopore-reparation-and-end-prep", "nanopore-end-prep", "nanopore-pre-pcr-ligation", "nanopore-pcr", "nanopore-barcode-ligation", "nanopore-final-ligation", "nanopore-library", "nanopore-depot"), concatMap(newPSV("libraryProtocol", "cDNA-PCR Sequencing (SQK-PCS108)"))));
        lp.add(newProtocol("direct-cdna-sequencing-sqk-dcs108",                         "Direct cDNA Sequencing (SQK-DCS108)",                              "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-frg", "nanopore-dna-reparation", "nanopore-reparation-and-end-prep", "nanopore-end-prep", "nanopore-pre-pcr-ligation", "nanopore-pcr", "nanopore-barcode-ligation", "nanopore-final-ligation", "nanopore-library", "nanopore-depot"), concatMap(newPSV("libraryProtocol", "Direct cDNA Sequencing (SQK-DCS108)"))));
        lp.add(newProtocol("direct-rna-sequencing-sqk-rna001",                          "Direct RNA sequencing (SQK-RNA001)",                               "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-frg", "nanopore-dna-reparation", "nanopore-reparation-and-end-prep", "nanopore-end-prep", "nanopore-pre-pcr-ligation", "nanopore-pcr", "nanopore-barcode-ligation", "nanopore-final-ligation", "nanopore-library", "nanopore-depot"), concatMap(newPSV("libraryProtocol", "Direct RNA sequencing (SQK-RNA001)"))));
        lp.add(newProtocol("cdna-by-ligation-sqk-lsk108-1d",                            "1D cDNA by ligation (SQK-LSK108)",                                 "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-frg", "nanopore-dna-reparation", "nanopore-reparation-and-end-prep", "nanopore-end-prep", "nanopore-pre-pcr-ligation", "nanopore-pcr", "nanopore-barcode-ligation", "nanopore-final-ligation", "nanopore-library", "nanopore-depot"), concatMap(newPSV("libraryProtocol", "1D cDNA by ligation (SQK-LSK108)"))));        
        lp.add(newProtocol("gdna-selecting-long-reads-and-long-reads-without-bluepippin-sqk-lsk108-1d","1D gDNA selecting for long reads et long reads without BluePippin (SQK-LSK108)","path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-frg", "nanopore-dna-reparation", "nanopore-reparation-and-end-prep", "nanopore-end-prep", "nanopore-pre-pcr-ligation", "nanopore-pcr", "nanopore-barcode-ligation", "nanopore-final-ligation", "nanopore-library", "nanopore-depot"), concatMap(newPSV("libraryProtocol", "1D gDNA selecting for long reads et long reads without BluePippin (SQK-LSK108)"))));        
        
        //To disable
        lp.add(newProtocol("map005",            "MAP005",               "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-library"), concatMap(newPSV("libraryProtocol", "MAP005")), false));
        lp.add(newProtocol("map005-on-beads",   "MAP005 sur billes",    "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-library"), concatMap(newPSV("libraryProtocol", "MAP005 sur billes")), false));
        lp.add(newProtocol("map006-low-input",  "MAP006 low input",     "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-library"), concatMap(newPSV("libraryProtocol", "MAP006 low input")), false));
        lp.add(newProtocol("map006",            "MAP006",               "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-library"), concatMap(newPSV("libraryProtocol", "MAP006")), false));
        lp.add(newProtocol("R9-Long-Read",      "R9-Long Read",         "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-library"), concatMap(newPSV("libraryProtocol", "R9-Long Read")), false));
        lp.add(newProtocol("R9-1D",             "R9-1D",                "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-library"), concatMap(newPSV("libraryProtocol", "R9-1D")), false));

        lp.add(newProtocol("map005-depot",              "MAP005_dépôt",             "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-depot"),false));
        lp.add(newProtocol("map005-on-bead-depot",      "MAP005 sur billes_dépôt",  "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-depot"),false));
        lp.add(newProtocol("map006-depot",              "MAP006_dépôt",             "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-depot"),false));
        lp.add(newProtocol("map006-low-input-depot",    "MAP006 low input_dépôt",   "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-depot"),false));

        lp.add(newProtocol("R9-1D-depot", "R9-1D-depot", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-depot"), null, false));

        lp.add(newProtocol("R9-2D-depot", "R9-2D-depot", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-depot"), null, false));

        lp.add(newProtocol("direct-rnasequencing", "direct RNAsequencing", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-library", "nanopore-final-ligation"), concatMap(newPSV("libraryProtocol", "direct RNAsequencing")),false));

        lp.add(newProtocol("cdna-pcr-sequencing",       "cDNA-PCR Sequencing",      "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-library", "nanopore-final-ligation"), concatMap(newPSV("libraryProtocol", "cDNA-PCR Sequencing")),false));
        lp.add(newProtocol("direct-cdna-sequencing",    "Direct cDNA Sequencing",   "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-library", "nanopore-final-ligation"), concatMap(newPSV("libraryProtocol", "Direct cDNA Sequencing")),false));

        lp.add(newProtocol("prt_wait", "Proto_en_attente", "path1", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-end-prep", "nanopore-pcr", "nanopore-barcode-ligation", "nanopore-pre-pcr-ligation", "aliquoting")));

        //lp.add(newProtocol("map005-preCR", "MAP005 preCR", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-fragmentation")));
        //lp.add(newProtocol("map006-preCR", "MAP006 preCR", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-fragmentation")));
        //lp.add(newProtocol("map006-FFPE", "MAP006 FFPE", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-fragmentation")));

        /*  lp.add(newProtocol("prt_wait", "Proto_en_attente", "path1", "1", "production", InstanceFactory.setExperimentTypeCodes("aliquoting")));
         */

        lp.add(newProtocol("irys-prep-nlrs-300-900",            "Irys Prep Labelling NLRS (300/900)",   "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("irys-nlrs-prep", "irys-chip-preparation")));
        lp.add(newProtocol("saphyr-prep-nlrs",                  "Saphyr Prep Labelling NLRS",           "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("irys-nlrs-prep", "irys-chip-preparation")));
        lp.add(newProtocol("saphyr-prep-labelling-dls-30206-A", "Saphyr Prep Labelling DLS_30206/A",    "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("bionano-dls-prep", "irys-chip-preparation")));
        lp.add(newProtocol(" saphyr-prep-labelling",            "Saphyr Prep Labelling",                "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("irys-chip-preparation")));


        lp.add(newProtocol("depot_irys",    "Depot IRYS",   "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("bionano-depot")));  
        lp.add(newProtocol("depot_saphyr",  "Depot SAPHYR", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("bionano-depot")));

        lp.add(newProtocol("bionano_standard_ptr",  "ptr_standard", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("bionano-depot"),false));
        lp.add(newProtocol("optimization",          "optimisation", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("bionano-depot"),false));

        //lp.add(newProtocol("fragmentation_ptr_sox140_1", "Fragmentation_ptr_sox140_1", "path1", "1", "production", InstanceFactory.setExperimentTypeCodes("fragmentation")));
        //lp.add(newProtocol("bqspri_ptr_sox142_1", "BqSPRI_ptr_sox142_1", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("librairie-indexing", "librairie-dualindexing")));
        //lp.add(newProtocol("amplif_ptr_sox144_1", "Amplif_ptr_sox144_1", "path3", "1", "production", InstanceFactory.setExperimentTypeCodes("amplification", "solution-stock")));
        //lp.add(newProtocol("proto_qc_v1", "Proto_QC_v1", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("chip-migration-post-pcr", "chip-migration-pre-pcr", "fluo-quantification", "qpcr-quantification")));

        lp.add(newProtocol("zr-duet-extraction", "Extraction ZR Duet",  "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("dna-rna-extraction"), false));
        lp.add(newProtocol("cryogenic-grinding", "Cryobroyage",         "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("dna-rna-extraction")));

        lp.add(newProtocol("zr-duet-extraction-euk-v1",     "Extraction ZR Duet euk. v1",   "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("dna-rna-extraction"), concatMap(newPSV("extractionProtocol", "ZR Duet euk."))));
        lp.add(newProtocol("zr-duet-extraction-prok-v1",    "Extraction ZR Duet prok. v1",  "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("dna-rna-extraction"), concatMap(newPSV("extractionProtocol", "ZR Duet prok."))));
        lp.add(newProtocol("zr-duet-extraction-dev",        "Extraction ZR Duet DEV",       "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("dna-rna-extraction")));

        lp.add(newProtocol("quick-dna-96-plus-prok-v1", "Quick-DNA 96 Plus Kit Prok.v1", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("dna-extraction"), concatMap(newPSV("extractionProtocol", "Quick-DNA 96 Plus Kit Prok"))));
        lp.add(newProtocol("xpedition-soil-and-fecal-miniprep-zymoresearch", "Xpedition soil and fecal miniprep Zymoresearch", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("dna-extraction"), concatMap(newPSV("extractionProtocol", "Xpedition soil and fecal miniprep"))));

        lp.add(newProtocol("fast_prep_grinding", "Broyage Fast Prep", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("grinding")));

        lp.add(newProtocol("dnase-treatment", "Traitement à la Dnase", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("dnase-treatment")));

        lp.add(newProtocol("fluo-dosage",                   "dosage_fluo",                  "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("fluo-quantification", "reception-fluo-quantification"),false));
        lp.add(newProtocol("annexe-dosagearn-en-attente",   "Annexe_DosageARN_en attente",  "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("fluo-quantification", "reception-fluo-quantification"),false));
        lp.add(newProtocol("annexe-dosagearn-qubit-v1",     "Annexe_DosageARN-Qubit_v1",    "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("fluo-quantification", "reception-fluo-quantification"),false));
        lp.add(newProtocol("annexe-dosagearn-qubit-v2",     "Annexe_DosageARN-Qubit_v2",    "path2", "2", "production", InstanceFactory.setExperimentTypeCodes("fluo-quantification", "reception-fluo-quantification")));


        lp.add(newProtocol("annexe-dosage-fluoroskan-v1", "Annexe_DosageFluoroskan_v1", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("fluo-quantification", "reception-fluo-quantification"),false));
        lp.add(newProtocol("annexe-dosage-fluoroskan-v2", "Annexe_DosageFluoroskan_v2", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("fluo-quantification", "reception-fluo-quantification"),false));
        lp.add(newProtocol("annexe-dosage-fluoroskan-v3", "Annexe_DosageFluoroskan_v3", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("fluo-quantification", "reception-fluo-quantification")));

        lp.add(newProtocol("annexe-dosage-qubit-v1", "Annexe_DosageQubit_v1", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("fluo-quantification", "reception-fluo-quantification"),false));
        lp.add(newProtocol("annexe-dosage-qubit-v2", "Annexe_DosageQubit_v2", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("fluo-quantification", "reception-fluo-quantification")));


        lp.add(newProtocol("ptr-ctl-123-4",                         "PTR_CTL123_4",                                 "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("gel-migration"),false));        
        lp.add(newProtocol("prod-ill-evaladn-en-tubes-123-v5",      "Prod_ILL_EvalADN_EnTubes_123_v5",              "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("gel-migration", "reception-fluo-quantification", "normalisation", "dilution")));        
        lp.add(newProtocol("prod-ill-evaladn-en-plaques-161-v1",    "Prod_ILL_EvalADN_EnPlaques_161_v1_en attente", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("gel-migration", "reception-fluo-quantification", "dilution"),false));       
        lp.add(newProtocol("prod-ill-evaladn-en-plaques-161-v1f",   "Prod_ILL_EvalADN_EnPlaques_161_v1",            "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("gel-migration", "reception-fluo-quantification", "dilution", "normalisation")));        


        lp.add(newProtocol("proto-eval-hpm_en-attente", "proto Eval HPM_en attente", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("reception-fluo-quantification", "gel-migration", "chip-migration", "pulsed-field-electrophoresis", "uv-spectrophotometry", "qcard")));     


        lp.add(newProtocol("proto_qc_v1", "Proto_QC_v1", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("qpcr-quantification"), false));

        lp.add(newProtocol("ptr_pool_tube_v1",      "PTR_POOL_TUBE_v1",     "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("pool-tube", "pool")));
        lp.add(newProtocol("amplif_ptr_sox144_1",   "Amplif_ptr_sox144_1",  "path3", "1", "production", InstanceFactory.setExperimentTypeCodes("solution-stock")));

        lp.add(newProtocol("Tag18S_V9",             "Tag18S V9",            "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("tag-pcr"),false));
        lp.add(newProtocol("Tag16S_V4V5_Fuhrmann",  "Tag16S V4V5 Fuhrmann", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("tag-pcr"),false));
        lp.add(newProtocol("Tag_18S_V4",            "Tag 18S_V4",           "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("tag-pcr"),false));
        lp.add(newProtocol("Tag_ITS2",              "Tag ITS2",             "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("tag-pcr"),false));
        lp.add(newProtocol("Tag_ITS2_int",          "Tag ITS2_int",         "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("tag-pcr"),false));
        //lp.add(newProtocol("Tag_ITS2_SYM_VAR", "Tag ITS2_SYM_VAR", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("tag-pcr")));
        lp.add(newProtocol("Tag_ITS2_SYM_VAR_Tm56", "Tag ITS2_SYM_VAR_Tm56", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("tag-pcr"),false));
        lp.add(newProtocol("Tag_ITS2_SYM_VAR_Tm59", "Tag ITS2_SYM_VAR_Tm59", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("tag-pcr"),false));
        lp.add(newProtocol("Tag_ITS2_ITSD",         "Tag ITS2_ITSD",        "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("tag-pcr"),false));
        lp.add(newProtocol("Tag_CP23S",             "Tag_CP23S",            "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("tag-pcr"),false));
        lp.add(newProtocol("Tag_COI",               "Tag COI",              "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("tag-pcr"),false));
        lp.add(newProtocol("Access_Array_48.48",    "Access Array 48.48",   "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("tag-pcr", "dna-illumina-indexed-library")));
        lp.add(newProtocol("Protocole_EPGV",        "Protocole EPGV",       "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("solution-stock", "chip-migration", "reception-fluo-quantification", "pool", "uv-spectrophotometry", "pulsed-field-electrophoresis", "qcard")));


        lp.add(newProtocol("Tag16S_Full_Length_16S_V4V5_Fuhrman",   "Tag 16S_Full Length + 16S_V4V5_Fuhrman",   "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("tag-pcr")));
        lp.add(newProtocol("Tag_16S_V1V2V3",                        "Tag 16S_V1V2V3",                           "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("tag-pcr")));

        lp.add(newProtocol("Tag_16S_V4V5_archae",       "Tag 16S_V4V5_Archae",      "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("tag-pcr")));
        lp.add(newProtocol("Tag_16S_V5V6_Procaryote",   "Tag 16S_V5V6_Procaryote",  "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("tag-pcr")));
        lp.add(newProtocol("Tag_18S_V1_Metazoaire",     "Tag 18S_V1_Metazoaire",    "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("tag-pcr")));
        lp.add(newProtocol("Tag_16S_V4_Procaryote",     "Tag 16S_V4_Procaryote",    "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("tag-pcr")));
        lp.add(newProtocol("Tag_16S_Full_Length",       "Tag 16S_Full Length",      "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("tag-pcr")));

        lp.add(newProtocol("metab-primer-fusion-dev",   "metaB_primerFusion_DEV",    "path2","1", "production", InstanceFactory.setExperimentTypeCodes("tag-pcr","dna-illumina-indexed-library","pcr-amplification-and-purification"),concatMap(newPSV("libraryProtocol", "metaB_primerFusion_DEV"))));
        
        lp.add(newProtocol("amplif_ptr_sox_144-4",              "Amplif ptr Sox 144-4",             "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("pcr-amplification-and-purification")));
        lp.add(newProtocol("amplif_nebnext_ultraii_ptr_151_1",  "Amplif_NebNext_UltraII ptr 151_1", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("pcr-amplification-and-purification")));
        lp.add(newProtocol("ampli-swift-accel-1s",              "Ampli_Swift_Accel_1S",             "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("pcr-amplification-and-purification")));



        lp.add(newProtocol("Amplif_ptr_Sox_144-4newProtocol",   "Amplif ptr Sox 144-4newProtocol",  "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("sizing")));
        lp.add(newProtocol("Decoupe_sur_gel",                   "Découpe sur gel",                  "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("sizing")));


        /*if(ConfigFactory.load().getString("ngl.env").equals("PROD") ){
            lp.add(newProtocol("Spri_select", "Spri select", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("sizing", "spri-select")));     
        }else if(ConfigFactory.load().getString("ngl.env").equals("DEV") ){         
            lp.add(newProtocol("Spri_select", "Spri select", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("spri-select")));
        }*/
        lp.add(newProtocol("Spri_select", "Spri select", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("spri-select")));

        lp.add(newProtocol("Bq_Super_low_cost_ptr_150_1", "Bq_Super_low cost_ptr 150_1", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("dna-illumina-indexed-library", "fragmentation"), concatMap(newPSV("libraryProtocol", "Bq Super low cost")),false));
        lp.add(newProtocol("Prod_ILL_BqADN_SuperLowCost_150_v2", "Prod_ILL_BqADN_SuperLowCost_150_v2", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("dna-illumina-indexed-library", "fragmentation","pcr-amplification-and-purification"), concatMap(newPSV("libraryProtocol", "Bq Super low cost")),false));
        lp.add(newProtocol("Prod_ILL_BqADN_SuperLowCost_150_v3", "Prod_ILL_BqADN_SuperLowCost_150_v3", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("dna-illumina-indexed-library", "fragmentation","pcr-amplification-and-purification","normalisation"), concatMap(newPSV("libraryProtocol", "Bq Super low cost"))));
        
        
        
        lp.add(newProtocol("Autre", "Autre", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("dna-illumina-indexed-library", "fragmentation")));
        
        lp.add(newProtocol("Bq_Low cost_ptr_148_3",          "Bq_Low cost_ptr_148_3",           "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("dna-illumina-indexed-library", "fragmentation"), concatMap(newPSV("libraryProtocol", "Bq low cost")), false));
        lp.add(newProtocol("Bq_NEB_Next_Ultra_II_ptr_151_1", "Bq_NEB Next Ultra II ptr_151_1",  "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("dna-illumina-indexed-library", "fragmentation"), concatMap(newPSV("libraryProtocol", "Bq NEB Next Ultra II")), false));

        lp.add(newProtocol("Bq_NEB_Reagent_ptr_143_4", "Bq_NEB Reagent ptr_143_4", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("dna-illumina-indexed-library", "fragmentation"), concatMap(newPSV("libraryProtocol", "Bq NEB Reagent"))));

        lp.add(newProtocol("swift-accel-1s", "Swift_Accel_1S", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("dna-illumina-indexed-library", "fragmentation"), concatMap(newPSV("libraryProtocol", "Bq Swift 1S"))));

        lp.add(newProtocol("prod_ill_bqadn_bqsags-lcmanuel_164_v1", "Prod_ILL_BqADN_BqSAGs-LC Manuel_164_v1", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("dna-illumina-indexed-library", "fragmentation", "pcr-amplification-and-purification"), concatMap(newPSV("libraryProtocol", "Bq SAG LC"))));

        /*
        lp.add(newProtocol("swift-accel-1s", "Swift_Accel_1S", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("dna-illumina-indexed-library", "fragmentation", "pcr-amplification-and-purification"), 
                concatMap(newPSV("libraryProtocol", "Bq Swift Accel"))));
         */

        lp.add(newProtocol("prod-ill-bqadn-lowcost-148-v4", "Prod_ILL_BqADN_LowCost_148_v4",    "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("pcr-amplification-and-purification", "dna-illumina-indexed-library", "fragmentation"), concatMap(newPSV("libraryProtocol", "Bq low cost")), false));
        lp.add(newProtocol("prod-ill-bqadn-nebu2-151-v2",   "Prod_ILL_BqADN_NEBUII_151_v2",     "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("pcr-amplification-and-purification", "dna-illumina-indexed-library", "fragmentation"), concatMap(newPSV("libraryProtocol", "Bq NEB Next Ultra II")), false));
        lp.add(newProtocol("prod-ill-bqadn-pcrfree-157-v1", "Prod_ILL_BqADN_PCRFree_157_v1",    "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("dna-illumina-indexed-library", "fragmentation"), concatMap(newPSV("libraryProtocol", "Bq PCR free")), false));
        lp.add(newProtocol("prod-ill-tag-amplicons-159-v1", "Prod_ILL_Tag_Amplicons_159_v1",    "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("tag-pcr"), false));
        lp.add(newProtocol("prod-ill-dep-qpcr-149-v2",      "Prod_ILL_Dep_qPCR_149_v2",         "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("qpcr-quantification")));

        lp.add(newProtocol("prod-ill-bqadn-lowcost-148-v5", "Prod_ILL_BqADN_LowCost_148_v5", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("pcr-amplification-and-purification", "dna-illumina-indexed-library", "fragmentation"), concatMap(newPSV("libraryProtocol", "Bq low cost"))));
        lp.add(newProtocol("prod-ill-bqadn-nebu2-151-v3",   "Prod_ILL_BqADN_NEBUII_151_v3",  "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("pcr-amplification-and-purification", "dna-illumina-indexed-library", "fragmentation"), concatMap(newPSV("libraryProtocol", "Bq NEB Next Ultra II"))));
        lp.add(newProtocol("prod-ill-bqadn-pcrfree-157-v2", "Prod_ILL_BqADN_PCRFree_157_v2", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("dna-illumina-indexed-library", "fragmentation"), concatMap(newPSV("libraryProtocol", "Bq PCR free")), false));
        lp.add(newProtocol("prod-ill-bqadn-pcrfree-157-v3", "Prod_ILL_BqADN_PCRFree_157_v3", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("dna-illumina-indexed-library", "fragmentation"), concatMap(newPSV("libraryProtocol", "Bq PCR free"))));
        lp.add(newProtocol("prod-ill-tag-amplicons-159-v2", "Prod_ILL_Tag_Amplicons_159_v2", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("tag-pcr"),false));
        lp.add(newProtocol("prod-ill-tag-amplicons-159-v3", "Prod_ILL_Tag_Amplicons_159_v3", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("tag-pcr")));


        lp.add(newProtocol("smarter_v4", "Smarter V4_ptr_sox156_1", "path1", "1", "production", InstanceFactory.setExperimentTypeCodes("cdna-synthesis"), concatMap(newPSV("rnaLibProtocol", "Smarter V4"), newPSV("strandOrientation", "unstranded"), newPSV("cDNAsynthesisType", "oligodT"))));

        lp.add(newProtocol("ovation_rnaseq_system_v2", "Ovation RNAseq system v2", "path1", "1", "production", InstanceFactory.setExperimentTypeCodes("cdna-synthesis"), concatMap(newPSV("rnaLibProtocol", "Ovation RNAseq system v2"), newPSV("strandOrientation", "unstranded"), newPSV("cDNAsynthesisType", "random + oligodT"))));

        lp.add(newProtocol("smarter_dev", "Smarter_DEV", "path1", "1", "production", InstanceFactory.setExperimentTypeCodes("cdna-synthesis"), concatMap(newPSV("rnaLibProtocol", "Smarter DEV"), newPSV("strandOrientation", "unstranded"), newPSV("cDNAsynthesisType", "oligodT"))));

        lp.add(newProtocol("nebnext-single-cell-low-input", " NEBNext Single Cell/Low Input", "path1", "1", "production", InstanceFactory.setExperimentTypeCodes("cdna-synthesis"), concatMap(newPSV("rnaLibProtocol", "NEBNext SC Low Input"), newPSV("strandOrientation", "unstranded"), newPSV("cDNAsynthesisType", "oligodT"))));

        lp.add(newProtocol("truseq_stranded_poly_a", "TruSeq Stranded poly A_ptr_sox153_1", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("pcr-amplification-and-purification", "rna-illumina-indexed-library"), concatMap(newPSV("rnaLibProtocol", "TruSeq Stranded poly A"), newPSV("strandOrientation", "reverse"), newPSV("cDNAsynthesisType", "random")), false));

        lp.add(newProtocol("truseq_stranded_proc", "TruSeq Stranded_proc_ptr_sox154_1", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("pcr-amplification-and-purification", "rna-illumina-indexed-library"), concatMap(newPSV("rnaLibProtocol", "TruSeq Stranded Proc"), newPSV("strandOrientation", "reverse"), newPSV("cDNAsynthesisType", "random")), false));

        lp.add(newProtocol("smarter_stranded", "Smarter Stranded_ptr_sox155_1", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("rna-illumina-indexed-library", "pcr-amplification-and-purification"), concatMap(newPSV("rnaLibProtocol", "Smarter Stranded"), newPSV("strandOrientation", "forward"), newPSV("cDNAsynthesisType", "random"))));

        lp.add(newProtocol("rna_neb_u2_stranded", "RNA NEB_U2 Stranded Proc_en attente", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("rna-illumina-library", "indexing-and-pcr-amplification"), concatMap(newPSV("rnaLibProtocol", "RNA NEB_U2 Stranded Proc"), newPSV("strandOrientation", "reverse"), newPSV("cDNAsynthesisType", "random"))));

        lp.add(newProtocol("prod-ill-bqarn-rna-nebuii-stranded-polya-160-v1", "Prod_ILL_BqARN_RNA-NEBUII-Stranded-PolyA_160_v1", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("rna-illumina-library", "indexing-and-pcr-amplification"), concatMap(newPSV("rnaLibProtocol", "RNA NEB_U2 Stranded PolyA"), newPSV("strandOrientation", "reverse"), newPSV("cDNAsynthesisType", "random"))));

        lp.add(newProtocol("nextflex-small-rna", "NEXTflex Small RNA", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("rna-illumina-library", "indexing-and-pcr-amplification"), concatMap(newPSV("rnaLibProtocol", "NEXTflex Small RNA"))));

        lp.add(newProtocol("smarter-smrna", "Smarter smRNA", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("rna-illumina-library", "indexing-and-pcr-amplification"), concatMap(newPSV("rnaLibProtocol", "Smarter smRNA"))));

        //utilisé lors d'une reprise d'histo
        lp.add(newProtocol("prod-ill-bqarn-rna-nebuii-stranded-polya-160-v1b", "Prod_ILL_BqARN_RNA-NEBUII-Stranded-PolyA_160_v1", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("rna-illumina-indexed-library", "pcr-amplification-and-purification"), concatMap(newPSV("rnaLibProtocol", "RNA NEB_U2 Stranded PolyA"), newPSV("strandOrientation", "reverse"), newPSV("cDNAsynthesisType", "random")), false));

        lp.add(newProtocol("prod_ill_bqarn_truseq-stranded-polya_153_v2", "Prod_ILL_BqARN_TruSeq-Stranded-PolyA_153_v2", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("rna-illumina-indexed-library", "pcr-amplification-and-purification"), concatMap(newPSV("rnaLibProtocol", "TruSeq Stranded poly A"), newPSV("strandOrientation", "reverse"), newPSV("cDNAsynthesisType", "random"))));

        lp.add(newProtocol("inda-c_ovation_universal_rna-seq", "InDA-C Ovation Universal RNA-Seq", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("rna-illumina-indexed-library", "pcr-amplification-and-purification"), concatMap(newPSV("rnaLibProtocol", "InDA-C Ovation Universal RNA-Seq"), newPSV("strandOrientation", "reverse"), newPSV("cDNAsynthesisType", "random + oligodT"))));
        
        lp.add(newProtocol("prod_ill_bqarn_truseq-stranded-proc_154_v2", " Prod_ILL_BqARN_TruSeq-Stranded-Proc_154_v2", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("rna-illumina-indexed-library", "pcr-amplification-and-purification"), concatMap(newPSV("rnaLibProtocol", "TruSeq Stranded Proc"), newPSV("strandOrientation", "reverse"), newPSV("cDNAsynthesisType", "random"))));


        /*
        lp.add(newProtocol("indac", "Indac", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("rna-illumina-indexed-library"), 
                concatMap(newPSV("rnaLibProtocol", "indac"),newPSV("strandOrientation", "reverse"),newPSV("cDNAsynthesisType", "?"))));
         */

        lp.add(newProtocol("bacteria-rrna-depletion", "Déplétion bactérienne", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("rrna-depletion"), newPSV("depletionMethod", "bactérienne")));

        lp.add(newProtocol("plant-rrna-depletion", "Déplétion plante", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("rrna-depletion"), newPSV("depletionMethod", "plante")));

        lp.add(newProtocol("prt_wait_2", "Proto_en_attente", "path1", "1", "production", InstanceFactory.setExperimentTypeCodes("pulsed-field-electrophoresis", "small-and-large-rna-isolation", "chip-migration", "control-pcr-and-gel", "normalisation", "tubes-to-plate", "plate-to-tubes", "plates-to-plate", "x-to-plate", "dilution")));

        lp.add(newProtocol("annexe-puce-adn-hs-v1",     "Annexe_PuceADN-HS_v1",         "path1", "1", "production", InstanceFactory.setExperimentTypeCodes("chip-migration", "chip-migration-rna-evaluation")));
        lp.add(newProtocol("annexe-labchip-v1",         "Annexe_LabChip_v1",            "path1", "1", "production", InstanceFactory.setExperimentTypeCodes("chip-migration")));
        lp.add(newProtocol("annexe-pucearn-en-attente", "Annexe_PuceARN_en attente",    "path1", "1", "production", InstanceFactory.setExperimentTypeCodes("chip-migration-rna-evaluation"),false));
        lp.add(newProtocol("annexe-pucearn-pico-v1",    "Annexe_PuceARN-pico_v1",       "path1", "1", "production", InstanceFactory.setExperimentTypeCodes("chip-migration-rna-evaluation")));



        lp.add(newProtocol("ampure_post_pcr",                               "ampure_post_pcr",                              "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("post-pcr-ampure"),false));
        lp.add(newProtocol("ampure",                                        "ampure",                                       "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("ampure"),false));
        lp.add(newProtocol("annexe-purifampureinversee-nucleospin-v2",      "Annexe_PurifAMPureInversée-NucleoSpin_v2",     "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("ampure", "post-pcr-ampure"),false));
        lp.add(newProtocol("annexe-purifampure-au-robot-tagamplicons-v1",   "Annexe_PurifAMPure au Robot-TagAmplicons_v1",  "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("ampure", "post-pcr-ampure"),false));

        lp.add(newProtocol("annexe-purifampureinversee-nucleospin-v3",  "Annexe_PurifAMPureInversée-NucleoSpin_v3", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("spin-column-purification", "ampure", "post-pcr-ampure")));
        lp.add(newProtocol("nucleospin_smarter_smrna",                  "Nucleospin_Smarter_smRNA",                 "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("spin-column-purification")));

        lp.add(newProtocol("ampure_post_pcr_smarter_smrna",     "Ampure_post_PCR_SMARTer_smRNA",    "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("post-pcr-ampure")));
        lp.add(newProtocol("ampure_post_pcr_nextflex_smrna",    "Ampure_post_PCR_NextFlex_smRNA",   "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("post-pcr-ampure")));

        lp.add(newProtocol("annexe-purifampure-au-robot-tagamplicons-v2", "Annexe_PurifAMPure au Robot-TagAmplicons_v2", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("ampure", "post-pcr-ampure")));


        lp.add(newProtocol("annexe-purif-adn-ampure-v1", "Annexe_PurifADN-AMPure_v1", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("ampure", "post-pcr-ampure"),false));
        lp.add(newProtocol("annexe-purif-adn-ampure-v2", "Annexe_PurifADN-AMPure_v2", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("ampure", "post-pcr-ampure")));

        lp.add(newProtocol("zymoclean-rna", "Zymoclean RNA", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("spin-column-purification")));
        lp.add(newProtocol("zymoclean-dna", "Zymoclean DNA", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("spin-column-purification")));
        lp.add(newProtocol("qiaamp-dna",    "QIAamp DNA",    "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("spin-column-purification")));

        lp.add(newProtocol("chromium-10x", "Chromium 10x", "path1", "1", "production", InstanceFactory.setExperimentTypeCodes("dna-illumina-indexed-library", "pcr-amplification-and-purification"), concatMap(newPSV("libraryProtocol", "Chromium 10x"))));

        lp.add(newProtocol("rna-extraction-from-trizol-filter-with-dnase", "Extraction ARN à partir de filtres en Trizol avec DNAse sur colonne", "path1", "1", "production", InstanceFactory.setExperimentTypeCodes("total-rna-extraction")));

        lp.add(newProtocol("Bq_PCR_free", "Bq_PCR_free", "path1", "1", "production", InstanceFactory.setExperimentTypeCodes("dna-illumina-indexed-library", "fragmentation"), concatMap(newPSV("libraryProtocol", "Bq PCR free")), false));

        lp.add(newProtocol("mechanical-fragmentation", "fragmentation mécanique",   "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-fragmentation", "nanopore-frg"),false));
        lp.add(newProtocol("enzymatic-fragmentation",  "fragmentation enzymatique", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-fragmentation", "nanopore-frg"),false));


        lp.add(newProtocol("ffpe-reparation",       "réparation FFPE", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-dna-reparation"))); 
        lp.add(newProtocol("nanopore-sizing-ptr",   "sizing nanopore", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-sizing")));

        lp.add(newProtocol("nanodrop",                  "Nanodrop",                 "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("uv-spectrophotometry"),false)); 
        lp.add(newProtocol("annexe-dosage-nanodrop-v1", "Annexe_DosageNanoDrop_v1", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("uv-spectrophotometry")));   

        lp.add(newProtocol("qcard-qc", "QC_qcard", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("qcard")));   

        lp.add(newProtocol("ptr-wga-bq-sags-v3",                "PTR_WGA_Bq_SAGs_V3",               "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("wga-amplification"),false));    
        lp.add(newProtocol("prod_ill_evaladn_wga-sags_163_v4",  "Prod_ILL_EvalADN_WGA-SAGs_163_v4", "path1", "1", "production", InstanceFactory.setExperimentTypeCodes("wga-amplification")));


        lp.add(newProtocol("wga-nanopore",       "WGA_Nanopore",        "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("wga-amplification")));  
        lp.add(newProtocol("qiagen-repli-g-wga", "QIAGEN REPLI-g WGA",  "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("wga-amplification")));  
        lp.add(newProtocol("qiagen-repli-g-wta", "QIAGEN REPLI-g WTA",  "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("wga-amplification")));  

        lp.add(newProtocol("debranchage-nanopore", "Débranchage_Nanopore", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("wga-debranching"))); 


        /* EPGV */
        //TODO a supprimer doublon avec protocole : protocole_epgv (nécessite une reprise histo) => en attente du devenir de l'équipe EPGV au sein du CNS
        lp.add(newProtocol("protocole-epgv",    "Protocole EPGV",    "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("post-pcr-ampure", "fluo-quantification", "prepa-fc-ordered", "prepa-flowcell", "normalisation"))); 
        lp.add(newProtocol("pcr-free-350-epgv", "PCR_free_350_EPGV", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("dna-illumina-indexed-library", "fragmentation"))); 
        lp.add(newProtocol("pcr-free-550-epgv", "PCR_free_550_EPGV", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("dna-illumina-indexed-library", "fragmentation"))); 


        if(ConfigFactory.load().getString("ngl.env").equals("PROD") ){

        }else if(ConfigFactory.load().getString("ngl.env").equals("DEV") ){


        }else if(ConfigFactory.load().getString("ngl.env").equals("UAT") ){ 

        }

        for(Protocol protocole:lp){
            InstanceHelpers.save(InstanceConstants.PROTOCOL_COLL_NAME, protocole,ctx);
            logger.debug(" Protocole "+protocole.code);
        }
    }

    /*
protocole   Smarter V4  Ovation RNAseq system v2    TruSeq Stranded poly A  TruSeq Stranded Proc    Smarter Stranded    Indac
rnaLibProtocol  smarterV4   ovationRNAseqSystemV2   truseqStrandedPolyA truseqStrandedProk  smarterStranded indac
strandOrientation   ?   ?   reverse reverse forward reverse
cDNAsynthesisType   ?   ?   ?   ?   ?   ?
     */
    @SafeVarargs
    private static Map<String, PropertyValue> concatMap(Map<String, PropertyValue>... map) {
        Map<String, PropertyValue> mapFinal = new HashMap<>(map.length); // <String, PropertyValue>(map.length);
        for (int i = 0 ; i < map.length; i++) {
            mapFinal.putAll(map[i]);
        }
        return mapFinal;
    }

}

