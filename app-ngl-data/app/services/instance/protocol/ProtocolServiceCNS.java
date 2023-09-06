package services.instance.protocol;

import static services.instance.InstanceFactory.newPSV;
import static services.instance.InstanceFactory.newPSVs;
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

        lp.add(newProtocol("hiseq2000_Illumina",        "HiSeq2000_Illumina",       "path4", "1", "production", InstanceFactory.setExperimentTypeCodes("illumina-depot"), false));
        lp.add(newProtocol("hiseq2500_fast_Illumina",   "HiSeq2500_fast_Illumina",  "path5", "1", "production", InstanceFactory.setExperimentTypeCodes("illumina-depot"), false));
        lp.add(newProtocol("hiseq2500_Illumina",        "HiSeq2500_Illumina",       "path6", "1", "production", InstanceFactory.setExperimentTypeCodes("illumina-depot"), false));
        lp.add(newProtocol("ptr_sox147_v1_depot",       "PTR_SOX147_v1",            "path6", "1", "production", InstanceFactory.setExperimentTypeCodes("illumina-depot")));

        lp.add(newProtocol("prepfc_cbot_ptr_sox139_1",      "PrepFC_CBot_ptr_sox139_1",     "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("prepa-flowcell"), false));
        lp.add(newProtocol("cbot_rapid_run_2500_Illumina",  "CBot_rapid_run_2500_Illumina", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("prepa-flowcell"), false));
        lp.add(newProtocol("ptr_sox147_v1",                 "PTR_SOX147_v1",                "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("prepa-flowcell")));

        lp.add(newProtocol("hiseq_4000_system_guide", "Illumina Hiseq 4000 system guide", "path8", "1", "production", InstanceFactory.setExperimentTypeCodes("prepa-fc-ordered", "illumina-depot"), false));
        
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

        lp.add(newProtocol("genomic-dna-ligation-sqk-lsk109-flongle", "1D Genomic DNA by Ligation (SQK-LSK109) - Flongle", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-frg", "nanopore-dna-reparation", "nanopore-reparation-and-end-prep", "nanopore-end-prep", "nanopore-pre-pcr-ligation", "nanopore-pcr", "nanopore-barcode-ligation", "nanopore-final-ligation", "nanopore-library", "nanopore-depot"), concatMap(newPSV("libraryProtocol", "1D Genomic DNA by Ligation (SQK-LSK109) - Flongle"))));
        lp.add(newProtocol("genomic-dna-ligation-sqk-lsk109-promethion-1d",             "1D Genomic DNA by Ligation (SQK-LSK109) - PromethION",             "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-frg", "nanopore-dna-reparation", "nanopore-reparation-and-end-prep", "nanopore-end-prep", "nanopore-pre-pcr-ligation", "nanopore-pcr", "nanopore-barcode-ligation", "nanopore-final-ligation", "nanopore-library", "nanopore-depot"), concatMap(newPSV("libraryProtocol", "1D Genomic DNA by Ligation (SQK-LSK109) - PromethION"))));
        
        // NGL-3186 
        lp.add(newProtocol("genomic-dna-ligation-sqk-lsk110-1d", "1D Genomic DNA by Ligation (SQK-LSK110)", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-frg", "nanopore-dna-reparation", "nanopore-reparation-and-end-prep", "nanopore-end-prep", "nanopore-pre-pcr-ligation", "nanopore-pcr", "nanopore-barcode-ligation", "nanopore-final-ligation", "nanopore-library", "nanopore-depot"), concatMap(newPSV("libraryProtocol", "1D Genomic DNA by Ligation (SQK-LSK110)"))));

        //NGL-3717
        lp.add(newProtocol("genomic-dna-ligation-sqk-lsk112", "Genomic DNA by Ligation (SQK-LSK112)", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-frg", "nanopore-dna-reparation", "nanopore-reparation-and-end-prep", "nanopore-end-prep", "nanopore-pre-pcr-ligation", "nanopore-pcr", "nanopore-barcode-ligation", "nanopore-final-ligation", "nanopore-library", "nanopore-depot"), concatMap(newPSV("libraryProtocol", "Genomic DNA by Ligation (SQK-LSK112)"))));
        //NGL-3934
        lp.add(newProtocol("genomic-dna-ligation-sqk-lsk114", "Genomic DNA by Ligation (SQK-LSK114)", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes( "nanopore-reparation-and-end-prep", "nanopore-final-ligation", "nanopore-depot"), concatMap(newPSV("libraryProtocol", "Genomic DNA by Ligation (SQK-LSK114)"))));
        
        // NGL-3565
        lp.add(newProtocol("genomic-dna-ligation-sqk-q20ea", "Genomic DNA by ligation using Q20+ Early Access Kit (SQK-Q20EA)", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-frg", "nanopore-dna-reparation", "nanopore-reparation-and-end-prep", "nanopore-end-prep", "nanopore-pre-pcr-ligation", "nanopore-pcr", "nanopore-barcode-ligation", "nanopore-final-ligation", "nanopore-library", "nanopore-depot"), concatMap(newPSV("libraryProtocol", "Genomic DNA by ligation using Q20+"))));

        lp.add(newProtocol("genomic-dna-ligation-sqk-kit9chemistry-promethion-1d",      "1D Genomic DNA by ligation (Kit 9 chemistry) - PromethION",        "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-frg", "nanopore-dna-reparation", "nanopore-reparation-and-end-prep", "nanopore-end-prep", "nanopore-pre-pcr-ligation", "nanopore-pcr", "nanopore-barcode-ligation", "nanopore-final-ligation", "nanopore-library", "nanopore-depot"), concatMap(newPSV("libraryProtocol", "1D Genomic DNA by ligation (Kit 9 chemistry) - PromethION"))));
        lp.add(newProtocol("genomic-dna-ligation-sqk-lsk108-promethion-1d",             "1D Genomic DNA by ligation (SQK-LSK108) - PromethION",             "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-frg", "nanopore-dna-reparation", "nanopore-reparation-and-end-prep", "nanopore-end-prep", "nanopore-pre-pcr-ligation", "nanopore-pcr", "nanopore-barcode-ligation", "nanopore-final-ligation", "nanopore-library", "nanopore-depot"), concatMap(newPSV("libraryProtocol", "1D Genomic DNA by ligation (SQK-LSK108) - PromethION"))));
        lp.add(newProtocol("gdna-selecting-long-reads-sqk-lsk108-1d",                   "1D gDNA selecting for long reads (SQK-LSK108)",                    "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-frg", "nanopore-dna-reparation", "nanopore-reparation-and-end-prep", "nanopore-end-prep", "nanopore-pre-pcr-ligation", "nanopore-pcr", "nanopore-barcode-ligation", "nanopore-final-ligation", "nanopore-library", "nanopore-depot"), concatMap(newPSV("libraryProtocol", "1D gDNA selecting for long reads (SQK-LSK108)"))));
        lp.add(newProtocol("gdna-selecting-long-reads-sqk-lsk109-1d",                   "1D gDNA selecting for long reads (SQK-LSK109)",                    "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-frg", "nanopore-dna-reparation", "nanopore-reparation-and-end-prep", "nanopore-end-prep", "nanopore-pre-pcr-ligation", "nanopore-pcr", "nanopore-barcode-ligation", "nanopore-final-ligation", "nanopore-library", "nanopore-depot"), concatMap(newPSV("libraryProtocol", "1D gDNA selecting for long reads (SQK-LSK109)"))));
        lp.add(newProtocol("gdna-long-reads-without-bluepippin-sqk-lsk108-1d",          "1D gDNA long reads without BluePippin (SQK-LSK108)",               "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-frg", "nanopore-dna-reparation", "nanopore-reparation-and-end-prep", "nanopore-end-prep", "nanopore-pre-pcr-ligation", "nanopore-pcr", "nanopore-barcode-ligation", "nanopore-final-ligation", "nanopore-library", "nanopore-depot"), concatMap(newPSV("libraryProtocol", "1D gDNA long reads without BluePippin (SQK-LSK108)"))));
        lp.add(newProtocol("native-barcoding-gdna-with-exp-nbd103-and-sqk-lsk108-1d",   "1D Native barcoding genomic DNA (with EXP-NBD103 and SQK-LSK108)", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-frg", "nanopore-dna-reparation", "nanopore-reparation-and-end-prep", "nanopore-end-prep", "nanopore-pre-pcr-ligation", "nanopore-pcr", "nanopore-barcode-ligation", "nanopore-final-ligation", "nanopore-library", "nanopore-depot"), concatMap(newPSV("libraryProtocol", "1D Native barcoding genomic DNA (with EXP-NBD103 and SQK-LSK108)"))));
        lp.add(newProtocol("native-barcoding-gdna-with-exp-nbd103-and-sqk-lsk109-1d",   "1D Native barcoding genomic DNA (with EXP-NBD103 and SQK-LSK109)", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-frg", "nanopore-dna-reparation", "nanopore-reparation-and-end-prep", "nanopore-end-prep", "nanopore-pre-pcr-ligation", "nanopore-pcr", "nanopore-barcode-ligation", "nanopore-final-ligation", "nanopore-library", "nanopore-depot"), concatMap(newPSV("libraryProtocol", "1D Native barcoding genomic DNA (with EXP-NBD103 and SQK-LSK109)"))));
        lp.add(newProtocol("native-barcoding-gdna-with-exp-nbd104-and-sqk-lsk109-1d",   "1D Native barcoding genomic DNA (with EXP-NBD104 and SQK-LSK109)", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-frg", "nanopore-dna-reparation", "nanopore-reparation-and-end-prep", "nanopore-end-prep", "nanopore-pre-pcr-ligation", "nanopore-pcr", "nanopore-barcode-ligation", "nanopore-final-ligation", "nanopore-library", "nanopore-depot"), concatMap(newPSV("libraryProtocol", "1D Native barcoding genomic DNA (with EXP-NBD104 and SQK-LSK109)"))));      
        lp.add(newProtocol("native-barcoding-gdna-with-exp-nbd196-and-sqk-lsk109",      "Native barcoding genomic DNA (with EXP-NBD196 and SQK-LSK109)",     "path7","1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-frg", "nanopore-dna-reparation", "nanopore-reparation-and-end-prep", "nanopore-end-prep", "nanopore-pre-pcr-ligation", "nanopore-pcr", "nanopore-barcode-ligation", "nanopore-final-ligation", "nanopore-library", "nanopore-depot"), concatMap(newPSV("libraryProtocol", "Native barcoding genomic DNA (with EXP-NBD196 and SQK-LSK109)" ))));
        
        // NGL-4303
        lp.add(newProtocol("ultra-long-dna-sequencing-kit-v14-sqk-ulk114", "Ultra-Long DNA Sequencing Kit V14 (SQK-ULK114)",     "path7","1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-frg", "nanopore-library", "nanopore-depot"), concatMap(newPSV("libraryProtocol", "Ultra-Long DNA Sequencing Kit V14 (SQK-ULK114)" ))));
        lp.add(newProtocol("native-barcoding-kit24-v14-ligation-sequencing-gDNA-sqk-nbd114.24", "Native Barcoding Kit 24 V14 - Ligation sequencing gDNA (SQK-NBD114.24)",     "path7","1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-frg", "nanopore-dna-reparation", "nanopore-reparation-and-end-prep", "nanopore-end-prep", "nanopore-pre-pcr-ligation", "nanopore-pcr", "nanopore-barcode-ligation", "nanopore-final-ligation", "nanopore-library", "nanopore-depot"), concatMap(newPSV("libraryProtocol", "Native Barcoding Kit 24 V14 - Ligation sequencing gDNA (SQK-NBD114.24)" ))));

       //NGL-3717
        lp.add(newProtocol("native-barcoding-kit96-gdna-sqk-nbd112.96",      "Native Barcoding Kit 96 with genomic DNA (SQK-NBD112.96)",     "path7","1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-frg", "nanopore-dna-reparation", "nanopore-reparation-and-end-prep", "nanopore-end-prep", "nanopore-pre-pcr-ligation", "nanopore-pcr", "nanopore-barcode-ligation", "nanopore-final-ligation", "nanopore-library", "nanopore-depot"), concatMap(newPSV("libraryProtocol", "Native Barcoding Kit 96 with genomic DNA (SQK-NBD112.96)" ))));
         lp.add(newProtocol("native-barcoding-kit24-gdna-sqk-nbd112.24",      "Native Barcoding Kit 24 with genomic DNA (SQK-NBD112.24)",     "path7","1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-frg", "nanopore-dna-reparation", "nanopore-reparation-and-end-prep", "nanopore-end-prep", "nanopore-pre-pcr-ligation", "nanopore-pcr", "nanopore-barcode-ligation", "nanopore-final-ligation", "nanopore-library", "nanopore-depot"), concatMap(newPSV("libraryProtocol", "Native Barcoding Kit 24 with genomic DNA (SQK-NBD112.24)" ))));
        
        lp.add(newProtocol("cdna-pcr-sequencing-sqk-pcs-108)",                          "cDNA-PCR Sequencing (SQK-PCS108)",                                 "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-frg", "nanopore-dna-reparation", "nanopore-reparation-and-end-prep", "nanopore-end-prep", "nanopore-pre-pcr-ligation", "nanopore-pcr", "nanopore-barcode-ligation", "nanopore-final-ligation", "nanopore-library", "nanopore-depot"), concatMap(newPSV("libraryProtocol", "cDNA-PCR Sequencing (SQK-PCS108)"))));
        lp.add(newProtocol("direct-cdna-sequencing-sqk-dcs108",                         "Direct cDNA Sequencing (SQK-DCS108)",                              "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-frg", "nanopore-dna-reparation", "nanopore-reparation-and-end-prep", "nanopore-end-prep", "nanopore-pre-pcr-ligation", "nanopore-pcr", "nanopore-barcode-ligation", "nanopore-final-ligation", "nanopore-library", "nanopore-depot"), concatMap(newPSV("libraryProtocol", "Direct cDNA Sequencing (SQK-DCS108)"))));
        lp.add(newProtocol("direct-rna-sequencing-sqk-rna001",                          "Direct RNA sequencing (SQK-RNA001)",                               "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-frg", "nanopore-dna-reparation", "nanopore-reparation-and-end-prep", "nanopore-end-prep", "nanopore-pre-pcr-ligation", "nanopore-pcr", "nanopore-barcode-ligation", "nanopore-final-ligation", "nanopore-library", "nanopore-depot"), concatMap(newPSV("libraryProtocol", "Direct RNA sequencing (SQK-RNA001)"))));
        lp.add(newProtocol("cdna-by-ligation-sqk-lsk108-1d",                            "1D cDNA by ligation (SQK-LSK108)",                                 "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-frg", "nanopore-dna-reparation", "nanopore-reparation-and-end-prep", "nanopore-end-prep", "nanopore-pre-pcr-ligation", "nanopore-pcr", "nanopore-barcode-ligation", "nanopore-final-ligation", "nanopore-library", "nanopore-depot"), concatMap(newPSV("libraryProtocol", "1D cDNA by ligation (SQK-LSK108)"))));        
        lp.add(newProtocol("gdna-selecting-long-reads-and-long-reads-without-bluepippin-sqk-lsk108-1d","1D gDNA selecting for long reads et long reads without BluePippin (SQK-LSK108)","path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-frg", "nanopore-dna-reparation", "nanopore-reparation-and-end-prep", "nanopore-end-prep", "nanopore-pre-pcr-ligation", "nanopore-pcr", "nanopore-barcode-ligation", "nanopore-final-ligation", "nanopore-library", "nanopore-depot"), concatMap(newPSV("libraryProtocol", "1D gDNA selecting for long reads et long reads without BluePippin (SQK-LSK108)"))));        
        //NGL-3804     
        lp.add(newProtocol("long-read-viromics-amplification-library-preparation-virion2","Long Read Viromics Amplification Library Preparation (VirION 2)","path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-frg", "nanopore-dna-reparation", "nanopore-reparation-and-end-prep", "nanopore-pre-pcr-ligation", "nanopore-pcr", "nanopore-final-ligation", "nanopore-depot"), concatMap(newPSV("libraryProtocol", "Long Read Viromics Amplif Lib Prep (VirIon2)"))));        
        
        lp.add(newProtocol("direct-rna-sequencing-sqk-rna-002",                         "Direct RNA sequencing (SQK-RNA002)",                               "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-frg", "nanopore-dna-reparation", "nanopore-reparation-and-end-prep", "nanopore-end-prep", "nanopore-pre-pcr-ligation", "nanopore-pcr", "nanopore-barcode-ligation", "nanopore-final-ligation", "nanopore-library", "nanopore-depot"), concatMap(newPSV("libraryProtocol", "Direct RNA sequencing (SQK-RNA002)"))));        
        lp.add(newProtocol("cdna-pcr-sequencing-sqk-pcs-109",                           "cDNA-PCR Sequencing (SQK-PCS109)",                                 "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-frg", "nanopore-dna-reparation", "nanopore-reparation-and-end-prep", "nanopore-end-prep", "nanopore-pre-pcr-ligation", "nanopore-pcr", "nanopore-barcode-ligation", "nanopore-final-ligation", "nanopore-library", "nanopore-cdna-synthesis", "nanopore-depot"), concatMap(newPSV("libraryProtocol", "cDNA-PCR Sequencing (SQK-PCS109)"))));        
        lp.add(newProtocol("direct-cdna-sequencing-sqk-dcs-109",                        "Direct cDNA Sequencing (SQK-DCS109)",                              "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-frg", "nanopore-dna-reparation", "nanopore-reparation-and-end-prep", "nanopore-end-prep", "nanopore-pre-pcr-ligation", "nanopore-pcr", "nanopore-barcode-ligation", "nanopore-final-ligation", "nanopore-library", "nanopore-cdna-synthesis", "nanopore-depot"), concatMap(newPSV("libraryProtocol", "Direct cDNA Sequencing (SQK-DCS109)"))));                
        lp.add(newProtocol("Rapid-pcr-barcoding-kit-sqk-rpb004",                        "Rapid PCR Barcoding Kit (SQK-RPB004)",                             "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-frg", "nanopore-final-ligation","nanopore-pcr" , "nanopore-depot"), concatMap(newPSV("libraryProtocol", "Rapid PCR Barcoding Kit (SQK-RPB004)"))));        
        
        lp.add(newProtocol("rapid-sequencing-sqk-rad004",                               "Rapid Sequencing (SQK-RAD004)", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-frg", "nanopore-library", "nanopore-depot"), concatMap(newPSV("libraryProtocol", "Rapid Sequencing (SQK-RAD004)"))));
        lp.add(newProtocol("ultra-long-dna-sequencing-sqk-ulk001",                      "Ultra-Long DNA Sequencing (SQK-ULK001)", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-frg", "nanopore-library", "nanopore-depot"), concatMap(newPSV("libraryProtocol", "Ultra-Long DNA Sequencing (SQK-ULK001)"))));
      lp.add(newProtocol("rapid-barcoding-sequencing-sqk-rbk004",                       "Rapid Barcoding Sequencing (SQK-RBK004)", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-frg", "nanopore-library", "nanopore-depot"), concatMap(newPSV("libraryProtocol", "Rapid Barcoding Sequencing (SQK-RBK004)"))));
        
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
        //NGL-3555 Cacher fonctionalite MGI// NGL-3556 Reactivation MGI
        lp.add(newProtocol("prt_wait", "Proto_en_attente", "path1", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-end-prep", "nanopore-frg","nanopore-dna-reparation","nanopore-reparation-and-end-prep","nanopore-final-ligation","nanopore-pcr","nanopore-library", "nanopore-cdna-synthesis","nanopore-barcode-ligation", "nanopore-pre-pcr-ligation","nanopore-depot", "aliquoting","mgi-pool")));

        //lp.add(newProtocol("map005-preCR", "MAP005 preCR", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-fragmentation")));
        //lp.add(newProtocol("map006-preCR", "MAP006 preCR", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-fragmentation")));
        //lp.add(newProtocol("map006-FFPE", "MAP006 FFPE", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-fragmentation")));

        /*  lp.add(newProtocol("prt_wait", "Proto_en_attente", "path1", "1", "production", InstanceFactory.setExperimentTypeCodes("aliquoting")));
         */

        lp.add(newProtocol("irys-prep-nlrs-300-900",            "Irys Prep Labelling NLRS (300/900)",   "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("irys-nlrs-prep", "irys-chip-preparation")));
        lp.add(newProtocol("saphyr-prep-nlrs",                  "Saphyr Prep Labelling NLRS",           "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("irys-nlrs-prep", "irys-chip-preparation")));
        lp.add(newProtocol("saphyr-prep-labelling-dls-30206-A", "Saphyr Prep Labelling DLS_30206/A",    "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("bionano-dls-prep", "irys-chip-preparation")));
        lp.add(newProtocol("saphyr-prep-labelling",            "Saphyr Prep Labelling",                "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("irys-chip-preparation"))); // NGL-3977 blanc initial excedentaire dans le code


        lp.add(newProtocol("depot_irys",    "Depot IRYS",   "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("bionano-depot")));  
        lp.add(newProtocol("depot_saphyr",  "Depot SAPHYR", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("bionano-depot")));

        lp.add(newProtocol("bionano_standard_ptr",  "ptr_standard", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("bionano-depot"),false));
        lp.add(newProtocol("optimization",          "optimisation", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("bionano-depot"),false));

        //lp.add(newProtocol("fragmentation_ptr_sox140_1", "Fragmentation_ptr_sox140_1", "path1", "1", "production", InstanceFactory.setExperimentTypeCodes("fragmentation")));
        //lp.add(newProtocol("bqspri_ptr_sox142_1", "BqSPRI_ptr_sox142_1", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("librairie-indexing", "librairie-dualindexing")));
        //lp.add(newProtocol("amplif_ptr_sox144_1", "Amplif_ptr_sox144_1", "path3", "1", "production", InstanceFactory.setExperimentTypeCodes("amplification", "solution-stock")));
        //lp.add(newProtocol("proto_qc_v1", "Proto_QC_v1", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("chip-migration-post-pcr", "chip-migration-pre-pcr", "fluo-quantification", "qpcr-quantification")));

        // NGL-3761
        lp.add(newProtocol("quick-dna-rna-miniprep-plus-specific-dna", "Quick-DNA/RNA Miniprep Plus (ADN seulement)",  "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("dna-extraction"), concatMap(newPSV("extractionProtocol", "Quick-DNA Miniprep Plus"))));
        lp.add(newProtocol("quick-dna-96-plus-insect", "Quick-DNA 96 Plus 'insect'",  "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("dna-extraction"), concatMap(newPSV("extractionProtocol", "Quick-DNA 96 Plus insect"))));
        //NGL-4025
        lp.add(newProtocol("quick-dna-insect-tube", "Quick DNA Insect, tube",  "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("dna-extraction"), concatMap(newPSV("extractionProtocol", "Quick DNA Insect tube"))));
        
        lp.add(newProtocol("zr-duet-extraction", "Extraction ZR Duet",  "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("dna-rna-extraction"), false));
        lp.add(newProtocol("zr-duet-water-edna", "ZR-Duet water eDNA",  "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("dna-extraction"), concatMap(newPSV("extractionProtocol", "ZR-Duet water eDNA"))));
        
        lp.add(newProtocol("cryogenic-grinding", "Cryobroyage",         "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("dna-rna-extraction")));

        lp.add(newProtocol("zr-duet-extraction-euk-v1",     "Extraction ZR Duet euk. v1",   "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("dna-rna-extraction"), concatMap(newPSV("extractionProtocol", "ZR Duet euk."))));
        lp.add(newProtocol("zr-duet-extraction-prok-v1",    "Extraction ZR Duet prok. v1",  "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("dna-rna-extraction"), concatMap(newPSV("extractionProtocol", "ZR Duet prok."))));
        lp.add(newProtocol("zr-duet-extraction-dev",        "Extraction ZR Duet DEV",       "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("dna-rna-extraction")));

        lp.add(newProtocol("quick-dna-96-plus-prok-v1", "Quick-DNA 96 Plus Kit Prok.v1", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("dna-extraction"), concatMap(newPSV("extractionProtocol", "Quick-DNA 96 Plus Kit Prok"))));
        lp.add(newProtocol("xpedition-soil-and-fecal-miniprep-zymoresearch", "Xpedition soil and fecal miniprep Zymoresearch", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("dna-extraction"), concatMap(newPSV("extractionProtocol", "Xpedition soil and fecal miniprep")), false));
        //NGL 4138
        lp.add(newProtocol("quick-dna-fecal-soil-microbe-zymo-research", "Quick-DNA Fecal/soil Microbe Zymo research", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("dna-extraction"), concatMap(newPSV("extractionProtocol", "Quick-DNA Fecal/soil"))));
        lp.add(newProtocol("prod_ill_banques_adngacineto_100_v3", "Prod_ILL_Banques_ADNgAcineto_100_v3", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("dna-extraction")));

        lp.add(newProtocol("fast_prep_grinding", "Broyage Fast Prep", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("grinding")));
        lp.add(newProtocol("nitrogen-grinding", "Broyage azote (mortier pilon)", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("grinding")));
        lp.add(newProtocol("powersoil-vortex-grinding", "Broyage vortex Powersoil", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("grinding")));
        lp.add(newProtocol("potter-grinding", "Broyage Potter", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("grinding")));
        
        lp.add(newProtocol("plant-leave-v1", "Plant Leave_v1", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("hmw-dna-extraction"),concatMap(newPSV("extractionProtocol", "Plant Leave_v1"))));
        lp.add(newProtocol("powersoil-v1", "Powersoil_v1", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("hmw-dna-extraction"), concatMap(newPSV("extractionProtocol", "Powersoil_v1"))));
        lp.add(newProtocol("genomic-tip", "Genomic Tip", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("hmw-dna-extraction"), concatMap(newPSV("extractionProtocol", "Genomic Tip"))));
        lp.add(newProtocol("maghini-v1", "Maghini_v1", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("hmw-dna-extraction"), concatMap(newPSV("extractionProtocol", "Maghini_v1"))));
        lp.add(newProtocol("nucleobond-v1", "Nucleobond_v1", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("hmw-dna-extraction"), concatMap(newPSV("extractionProtocol", "Nucleobond_v1"))));
        lp.add(newProtocol("li-et-al-v1", "Li et al._v1", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("hmw-dna-extraction"), concatMap(newPSV("extractionProtocol", "Li et al._v1"))));
        lp.add(newProtocol("circulomics-cbb", "Circulomics CBB", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("hmw-dna-extraction"), concatMap(newPSV("extractionProtocol", "Circulomics CBB"))));
        lp.add(newProtocol("circulomics-tissue", "Circulomics Tissue", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("hmw-dna-extraction"), concatMap(newPSV("extractionProtocol", "Circulomics Tissue"))));
        lp.add(newProtocol("circulomics-plant", "Circulomics Plant", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("hmw-dna-extraction"), concatMap(newPSV("extractionProtocol", "Circulomics Plant"))));
        // NGL-3642
        lp.add(newProtocol("silex-v1", "SILEX_v1", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("hmw-dna-extraction"), concatMap(newPSV("extractionProtocol", "SILEX_v1"))));
        // NGL-4275
        lp.add(newProtocol("ramya-v1", "Ramya_v1", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("hmw-dna-extraction"), concatMap(newPSV("extractionProtocol", "Ramya_v1"))));
        
        // NGL-3882
        // lp.add(newProtocol("autre-hmw", "Autre", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("hmw-dna-extraction"), concatMap(newPSV("extractionProtocol", "Autre"))));
         // Ce protocole devra être utilisé si on demande un proto Autre pour les autres expce d'extraction "total-rna-extraction" ou dna-extraction"
         lp.add(newProtocol("other-extraction-protocol", "Autre", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("hmw-dna-extraction","total-rna-extraction","dna-extraction","dna-rna-extraction"), concatMap(newPSV("extractionProtocol", "Autre"))));
        //NGL-4167
        lp.add(newProtocol("other-library-protocol", "Autre", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-final-ligation","nanopore-library","dna-illumina-indexed-library","dna-pacbio-library","nanopore-cdna-synthesis"), concatMap(newPSV("libraryProtocol", "Autre"))));
        lp.add(newProtocol("other-prep-hic-protocol", "Autre", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("hi-c-prep"), concatMap(newPSV("prepHicProtocol", "Autre"))));
        
        lp.add(newProtocol("dneasy-powersoil-pro", "DNeasy PowerSoil Pro", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("dna-extraction"), concatMap(newPSV("extractionProtocol", "DNeasy PowerSoil Pro"))));

        // NGL-4205
        lp.add(newProtocol("rneasy-powerplant", "RNeasy PowerPlant", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("total-rna-extraction"), concatMap(newPSV("extractionProtocol", "RNeasy PowerPlant"))));

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
        lp.add(newProtocol("ptr-bq-pool-plaque-bid-v1","PTR_BANQUE_POOL_PLAQUE_BID_v1",     "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("pool")));
       
        //NGL-2585 IN Progress
        //lp.add(newProtocol("ptr_pool_tube_v1",      "PTR_POOL_TUBE_v1",     "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("pool-tube", "pool", "batch-pool", "batch-pool-two")));
        //lp.add(newProtocol("ptr-bq-pool-plaque-bid-v1","PTR_BANQUE_POOL_PLAQUE_BID_v1",     "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("pool", "batch-pool", "batch-pool-two")));

        
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
        lp.add(newProtocol("Tag_18S_Full_Length",       "Tag 18S_Full Length",      "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("tag-pcr")));   // 03/12/2019 ajout NGL-2728
        lp.add(newProtocol("Tag_ITS_Full_Length",       "Tag ITS FL",      "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("tag-pcr")));//NGL-3232
        lp.add(newProtocol("Tag_ITS_FUN",       		"Tag ITS FUN",      "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("tag-pcr")));//NGL-3232
        lp.add(newProtocol("Tag_ITS_FL_ITS_FUN",       "Tag ITS FL + ITS FUN",      "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("tag-pcr")));//NGL-3232 
        lp.add(newProtocol("Tag16S_Full_Length_16S_V3V4", "Tag 16S_Full Length + 16S_V3V4", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("tag-pcr")));//NGL-3640
        lp.add(newProtocol("Tag_18S_Full_Length_18S_V9", "Tag 18S_Full Length + 18S_V9", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("tag-pcr")));//NGL-3640
        
        lp.add(newProtocol("Protocole_BID_en_attente",       "Protocole BID_en attente",      "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("tag-pcr")));
        
        lp.add(newProtocol("metab-primer-fusion-dev",   "metaB_primerFusion_DEV",    "path2","1", "production", InstanceFactory.setExperimentTypeCodes("tag-pcr","dna-illumina-indexed-library","pcr-amplification-and-purification"),concatMap(newPSV("libraryProtocol", "metaB_primerFusion_DEV"))));
        

        // NGL-2786 : Ajout de 3 protocoles dans l'exp. de banque Illumina indexée.
        lp.add(newProtocol("accel-ngs-2s-plus-dna-lib",   "Accel NGS 2S plus DNA Lib",    "path2","1", "production", InstanceFactory.setExperimentTypeCodes("dna-illumina-indexed-library","pcr-amplification-and-purification"), concatMap(newPSV("libraryProtocol", "Bq Accel-NGS 2S plus DNA"))));
        lp.add(newProtocol("ovation-ultralow-systeme",   "Ovation Ultralow Systeme",    "path2","1", "production", InstanceFactory.setExperimentTypeCodes("dna-illumina-indexed-library","pcr-amplification-and-purification"), concatMap(newPSV("libraryProtocol", "Bq Ovation Ultralow Systeme"))));
        lp.add(newProtocol("qiaseq-ultralow-input",   "Qiaseq Ultralow Input",    "path2","1", "production", InstanceFactory.setExperimentTypeCodes("dna-illumina-indexed-library","pcr-amplification-and-purification"), concatMap(newPSV("libraryProtocol", "Bq QIAseq Ultralow Input"))));

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
        
        
        
        lp.add(newProtocol("Autre", "Autre", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("fragmentation","pcr-amplification-and-purification","indexing-and-pcr-amplification", "precipitation-purification", "cdna-synthesis", "nanopore-barcode-ligation", "nanopore-dna-reparation", "nanopore-end-prep", "nanopore-frg", "nanopore-pcr", "nanopore-pre-pcr-ligation", "nanopore-reparation-and-end-prep", "ampure", "dnase-treatment", "nanopore-sizing", "nuclease-treatment", "post-pcr-ampure", "rrna-depletion", "small-and-large-rna-purification", "spin-column-purification", "wga-debranching", "fluo-quantification", "qpcr-quantification", "reception-fluo-quantification", "grinding", "sample-prep-exp", "sizing", "small-and-large-rna-isolation", "spri-select", "tag-pcr", "wga-amplification")));
        
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
        //NGL-3002
        lp.add(newProtocol("prod-ill-bqadn-nebu2-151-v3",   "Prod_ILL_BqADN_NEBUII_151_v3",  "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("pcr-amplification-and-purification", "dna-illumina-indexed-library", "fragmentation"), concatMap(newPSV("libraryProtocol", "Bq NEB Next Ultra II")),false));
        lp.add(newProtocol("prod-ill-bqadn-nebu2-151-v4",   "Prod_ILL_BqADN_NEBUII_151_v4",  "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("pcr-amplification-and-purification", "dna-illumina-indexed-library", "fragmentation"), concatMap(newPSV("libraryProtocol", "Bq NEB Next Ultra II"))));
        lp.add(newProtocol("prod-ill-bqadn-pcrfree-157-v2", "Prod_ILL_BqADN_PCRFree_157_v2", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("dna-illumina-indexed-library", "fragmentation"), concatMap(newPSV("libraryProtocol", "Bq PCR free")), false));
        lp.add(newProtocol("prod-ill-bqadn-pcrfree-157-v3", "Prod_ILL_BqADN_PCRFree_157_v3", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("dna-illumina-indexed-library", "fragmentation"), concatMap(newPSV("libraryProtocol", "Bq PCR free"))));
        lp.add(newProtocol("prod-ill-tag-amplicons-159-v2", "Prod_ILL_Tag_Amplicons_159_v2", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("tag-pcr"),false));
        lp.add(newProtocol("prod-ill-tag-amplicons-159-v3", "Prod_ILL_Tag_Amplicons_159_v3", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("tag-pcr")));


        lp.add(newProtocol("smarter_v4", "Smarter V4_ptr_sox156_1", "path1", "1", "production", InstanceFactory.setExperimentTypeCodes("cdna-synthesis"), concatMap(newPSV("rnaLibProtocol", "Smarter V4"), newPSV("strandOrientation", "unstranded"), newPSV("cDNAsynthesisType", "oligodT"))));
        lp.add(newProtocol("ovation_rnaseq_system_v2", "Ovation RNAseq system v2", "path1", "1", "production", InstanceFactory.setExperimentTypeCodes("cdna-synthesis"), concatMap(newPSV("rnaLibProtocol", "Ovation RNAseq system v2"), newPSV("strandOrientation", "unstranded"), newPSV("cDNAsynthesisType", "random + oligodT"))));
        lp.add(newProtocol("smarter_dev", "Smarter_DEV", "path1", "1", "production", InstanceFactory.setExperimentTypeCodes("cdna-synthesis"), concatMap(newPSV("rnaLibProtocol", "Smarter DEV"), newPSV("strandOrientation", "unstranded"), newPSV("cDNAsynthesisType", "oligodT"))));
        lp.add(newProtocol("nebnext-single-cell-low-input", "NEBNext Single Cell/Low Input", "path1", "1", "production", InstanceFactory.setExperimentTypeCodes("cdna-synthesis"), concatMap(newPSV("rnaLibProtocol", "NEBNext SC Low Input"), newPSV("strandOrientation", "unstranded"), newPSV("cDNAsynthesisType", "oligodT"))));
        lp.add(newProtocol("bq-single-cell-cdna", "Bq Single Cell cDNA", "path1", "1", "production", InstanceFactory.setExperimentTypeCodes("fragmentation","rna-illumina-library", "indexing-and-pcr-amplification"), concatMap(newPSV("libraryProtocol", "Bq Single Cell cDNA"))));     
        lp.add(newProtocol("truseq_stranded_poly_a", "TruSeq Stranded poly A_ptr_sox153_1", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("pcr-amplification-and-purification", "rna-illumina-indexed-library"), concatMap(newPSV("rnaLibProtocol", "TruSeq Stranded poly A"), newPSV("strandOrientation", "reverse"), newPSV("cDNAsynthesisType", "random")), false));
        lp.add(newProtocol("truseq_stranded_proc", "TruSeq Stranded_proc_ptr_sox154_1", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("pcr-amplification-and-purification", "rna-illumina-indexed-library"), concatMap(newPSV("rnaLibProtocol", "TruSeq Stranded Proc"), newPSV("strandOrientation", "reverse"), newPSV("cDNAsynthesisType", "random")), false));
        lp.add(newProtocol("smarter_stranded", "Smarter Stranded_ptr_sox155_1", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("rna-illumina-indexed-library", "pcr-amplification-and-purification"), concatMap(newPSV("rnaLibProtocol", "Smarter Stranded"), newPSV("strandOrientation", "forward"), newPSV("cDNAsynthesisType", "random"))));
        lp.add(newProtocol("rna_neb_u2_stranded", "RNA NEB_U2 Stranded Proc_en attente", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("rna-illumina-library", "indexing-and-pcr-amplification"), concatMap(newPSV("rnaLibProtocol", "RNA NEB_U2 Stranded Proc"), newPSV("strandOrientation", "reverse"), newPSV("cDNAsynthesisType", "random"))));
        lp.add(newProtocol("prod-ill-bqarn-rna-nebuii-stranded-polya-160-v1", "Prod_ILL_BqARN_RNA-NEBUII-Stranded-PolyA_160_v1", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("rna-illumina-library", "indexing-and-pcr-amplification"), concatMap(newPSV("rnaLibProtocol", "RNA NEB_U2 Stranded PolyA"), newPSV("strandOrientation", "reverse"), newPSV("cDNAsynthesisType", "random"))));
        lp.add(newProtocol("nextflex-small-rna", "NEXTflex Small RNA", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("rna-illumina-library", "indexing-and-pcr-amplification"), concatMap(newPSV("rnaLibProtocol", "NEXTflex Small RNA")),false));
        //NGL-4004
        lp.add(newProtocol( "prod-ill-small-rna-168-v2","Prod_ILL_Small_RNA_168_v2", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("rna-illumina-library", "indexing-and-pcr-amplification"), concatMap(newPSV("rnaLibProtocol", "NEXTflex Small RNA"))));
        //NGL-4071
        lp.add(newProtocol("smarter-smrna", "Smarter smRNA", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("rna-illumina-library", "indexing-and-pcr-amplification"), concatMap(newPSV("rnaLibProtocol", "Smarter smRNA")),false));

        // NGL-3774
        // !! erreur de l'utilisateur sur les experiences auxquelle ajouter ces 2 protocoles:
        lp.add(newProtocol("illumina-stranded-total-rna-prep-ligation", "Illumina Stranded Total RNA Prep, Ligation", "path2", "1", "production",
                           InstanceFactory.setExperimentTypeCodes("rna-illumina-library", "indexing-and-pcr-amplification"),
                           concatMap(newPSV("rnaLibProtocol","Illumina Stranded Total RNA Prep, Ligation"),
                                     newPSV("strandOrientation", "reverse"),
                                     newPSV("cDNAsynthesisType", "random")
                                    )
                          )
        );
        
        lp.add(newProtocol("illumina-stranded-mrna-prep-ligation", "Illumina Stranded mRNA Prep, Ligation", "path2", "1", "production",
                           InstanceFactory.setExperimentTypeCodes("rna-illumina-library", "indexing-and-pcr-amplification"),
                           concatMap(newPSV("rnaLibProtocol","Illumina Stranded mRNA Prep, Ligation"),
                                     newPSV("strandOrientation", "reverse"),
                                     newPSV("cDNAsynthesisType", "random")
                         )
               )
        );
        
        
        //utilisé lors d'une reprise d'histo
        lp.add(newProtocol("prod-ill-bqarn-rna-nebuii-stranded-polya-160-v1b", "Prod_ILL_BqARN_RNA-NEBUII-Stranded-PolyA_160_v1", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("rna-illumina-indexed-library", "pcr-amplification-and-purification"), concatMap(newPSV("rnaLibProtocol", "RNA NEB_U2 Stranded PolyA"), newPSV("strandOrientation", "reverse"), newPSV("cDNAsynthesisType", "random")), false));

        lp.add(newProtocol("prod_ill_bqarn_truseq-stranded-polya_153_v2", "Prod_ILL_BqARN_TruSeq-Stranded-PolyA_153_v2", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("rna-illumina-indexed-library", "pcr-amplification-and-purification"), concatMap(newPSV("rnaLibProtocol", "TruSeq Stranded poly A"), newPSV("strandOrientation", "reverse"), newPSV("cDNAsynthesisType", "random")), false));

        //NGL-4071
        lp.add(newProtocol("prod_ill_bqarn_truseq-stranded-polya_153_v3", "Prod_ILL_BqARN_TruSeq-Stranded-PolyA_153_v3", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("rna-illumina-indexed-library", "pcr-amplification-and-purification"), concatMap(newPSV("rnaLibProtocol", "TruSeq Stranded poly A"), newPSV("strandOrientation", "reverse"), newPSV("cDNAsynthesisType", "random")),false));       
        lp.add(newProtocol("inda-c_ovation_universal_rna-seq", "InDA-C Ovation Universal RNA-Seq", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("rna-illumina-indexed-library", "pcr-amplification-and-purification"), concatMap(newPSV("rnaLibProtocol", "InDA-C Ovation Universal RNA-Seq"), newPSV("strandOrientation", "reverse"), newPSV("cDNAsynthesisType", "random + oligodT")),false));  
        lp.add(newProtocol("prod_ill_bqarn_truseq-stranded-proc_154_v2", " Prod_ILL_BqARN_TruSeq-Stranded-Proc_154_v2", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("rna-illumina-indexed-library", "pcr-amplification-and-purification"), concatMap(newPSV("rnaLibProtocol", "TruSeq Stranded Proc"), newPSV("strandOrientation", "reverse"), newPSV("cDNAsynthesisType", "random")),false));
        lp.add(newProtocol("other-rna-illumina-library", "Autre", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("rna-illumina-indexed-library", "rna-illumina-library"), concatMap(newPSV("rnaLibProtocol", "autre"), newPSV("strandOrientation", "autre"), newPSV("cDNAsynthesisType", "autre"))));

//NGL-2707 Ajout nouveau proto pour des tests de DEV
// Pour le choix des prop associées au Proto, regarder ce qui existe déja comme prop associées aux differentes expériences.
        /*comme le protocole est demandé dans l'exp de déplétion ARN ribo, la propriété suivante est recommandée :

depletionMethod : "value" : "depletion universelle"
comme le protocole est demandé dans l'exp de banque RNA, les prop. suivantes sont "obligatoires" (au sens métier) :

"rnaLibProtocol" : "value" : "..." 
"strandOrientation" : "value" : "..." 
"cDNAsynthesisType" : "value" : "..." */
        //NGL-4071
        lp.add(newProtocol("zymoseq_ribofree", "ZymoSeq RiboFree", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("rna-illumina-library", "indexing-and-pcr-amplification"), concatMap(newPSV("rnaLibProtocol", "ZymoSeq RiboFree"), newPSV("strandOrientation", "reverse"), newPSV("cDNAsynthesisType", "random")),false));
       
        lp.add(newProtocol("zymoseq_ribofree_depletion", "ZymoSeq RiboFree", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("rrna-depletion"), concatMap(newPSV("depletionMethod", "depletion universelle"))));

        /*
        lp.add(newProtocol("indac", "Indac", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("rna-illumina-indexed-library"), 
                concatMap(newPSV("rnaLibProtocol", "indac"),newPSV("strandOrientation", "reverse"),newPSV("cDNAsynthesisType", "?"))));
         */

        //NGL-2749 depletionrRNA bactériens + rRNA humain/rat/souris cytoplq & mitochdx + les transcripts globin.
        lp.add(newProtocol("ribozero_plus_depletion", "RiboZero Plus", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("rrna-depletion"), newPSV("depletionMethod", "déplétion RiboZero Plus")));

        
        lp.add(newProtocol("bacteria-rrna-depletion", "Bactérie", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("rrna-depletion"), newPSV("depletionMethod", "bactérienne")));
        lp.add(newProtocol("plant-rrna-depletion", "Plante", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("rrna-depletion"), newPSV("depletionMethod", "plante")));
        lp.add(newProtocol("other-rrna-depletion", "Autre", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("rrna-depletion"), newPSV("depletionMethod", "autre")));

        //NGL-2515
        lp.add(newProtocol("ribozero_human_mouse_rat", "Humain Rat Souris", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("rrna-depletion"), newPSV("depletionMethod", "déplétion humain rat souris")));

        
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

        lp.add(newProtocol("zymoclean-rna", "Zymoclean RNA", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("spin-column-purification","small-and-large-rna-purification")));
        lp.add(newProtocol("zymoclean-dna", "Zymoclean DNA", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("spin-column-purification")));
        lp.add(newProtocol("qiaamp-dna",    "QIAamp DNA",    "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("spin-column-purification")));
        lp.add(newProtocol("one-step-pcr-inhibitor",    "One Step PCR Inhibitor",    "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("spin-column-purification")));

        lp.add(newProtocol("chromium-10x", "Chromium 10x", "path1", "1", "production", InstanceFactory.setExperimentTypeCodes("dna-illumina-indexed-library", "pcr-amplification-and-purification"), concatMap(newPSV("libraryProtocol", "Chromium 10x"))));

        lp.add(newProtocol("rna-extraction-from-trizol-filter-with-dnase", "Extraction ARN à partir de filtres en Trizol avec DNAse sur colonne", "path1", "1", "production", InstanceFactory.setExperimentTypeCodes("total-rna-extraction")));
        lp.add(newProtocol("direct-zol-rna", "Direct-zol RNA", "path1", "1", "production", InstanceFactory.setExperimentTypeCodes("total-rna-extraction")));       
        lp.add(newProtocol("rna-extraction-dev", "extraction ARN DEV", "path1", "1", "production", InstanceFactory.setExperimentTypeCodes("total-rna-extraction")));
       //NGL-3925
        lp.add(newProtocol("nucleospin-rna", "Nucléospin RNA", "path1", "1", "production", InstanceFactory.setExperimentTypeCodes("total-rna-extraction"), concatMap(newPSV("extractionProtocol", "Nucléospin RNA"))));
        lp.add(newProtocol("rneasy-rna", "Rneasy RNA", "path1", "1", "production", InstanceFactory.setExperimentTypeCodes("total-rna-extraction"), concatMap(newPSV("extractionProtocol", "Rneasy RNA"))));
        lp.add(newProtocol("quick-miniprep-rna-zr-duet", "Quick Miniprep RNA (ZR-Duet)", "path1", "1", "production", InstanceFactory.setExperimentTypeCodes("total-rna-extraction"), concatMap(newPSV("extractionProtocol", "Quick Miniprep RNA (ZR-Duet)"))));

        lp.add(newProtocol("Bq_PCR_free", "Bq_PCR_free", "path1", "1", "production", InstanceFactory.setExperimentTypeCodes("dna-illumina-indexed-library", "fragmentation"), concatMap(newPSV("libraryProtocol", "Bq PCR free")), false));

        lp.add(newProtocol("amplif_bq_hi-c", "Amplif. bq Hi-C", "path1", "1", "production", InstanceFactory.setExperimentTypeCodes("pcr-amplification-and-purification")));

        //NGL-2717 puis désactivation dans NGL-3840
        lp.add(newProtocol("bq_hi-c", "Bq Hi-C", "path1", "1", "production", InstanceFactory.setExperimentTypeCodes("dna-illumina-indexed-library", "fragmentation"), concatMap(newPSV("libraryProtocol", "Bq Hi-C")), false));
      
        //NGL-3840 
        lp.add(newProtocol("frag-hi-c", "Frag Hi-C", "path1", "1", "production", InstanceFactory.setExperimentTypeCodes("fragmentation")));
          
        
        //Protocol HiC NGL-3367
        lp.add(newProtocol("hi-c-dovetail-animal-tissue", "Hi-C Dovetail - Tissu animal", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("sample-prep-exp")));
        lp.add(newProtocol("hi-c-dovetail-plant-tissue", "Hi-C Dovetail - Tissu plante", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("sample-prep-exp")));
        lp.add(newProtocol("hi-c-dovetail-cell-culture", "Hi-C Dovetail - Culture cellulaire", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("sample-prep-exp")));
        lp.add(newProtocol("hi-c-dovetail-blood", "Hi-C Dovetail - sang", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("sample-prep-exp")));
        lp.add(newProtocol("hic-arima-animal-tissue-large", "Hi-C Arima - Tissu animal (large)", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("sample-prep-exp")));
        lp.add(newProtocol("hic-arima-animal-tissue-small", "Hi-C Arima - Tissu animal (small)", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("sample-prep-exp")));
        lp.add(newProtocol("hic-arima-plant-tissue", "Hi-C Arima - Tissu plante", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("sample-prep-exp")));
        lp.add(newProtocol("hic-arima-animal-cells", "Hi-C Arima - Cellules animales", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("sample-prep-exp")));
        lp.add(newProtocol("hic-arima-mammalian-cell-lines", "Hi-C Arima - Mammalian Cell lines", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("sample-prep-exp")));
        lp.add(newProtocol("omni-c-dovetail-nucleated-blood", "Omni-C Dovetail - Nucleated Blood", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("sample-prep-exp")));
        lp.add(newProtocol("omni-c-dovetail-animal-tissue-except-insects-and-marine-invertebrates", "Omni-C Dovetail - Animal Tissue (other than insects and marine invertebrates)", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("sample-prep-exp")));
        lp.add(newProtocol("omni-c-dovetail-insects-and-marine-invertebrates", "Omni-C Dovetail - Insects and marine invertebrates", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("sample-prep-exp")));
        lp.add(newProtocol("omni-c-dovetail-plant", "Omni-C Dovetail - Plant", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("sample-prep-exp")));
        lp.add(newProtocol("omni-c-dovetail-mammalian-cells", "Omni-C Dovetail - Mammalian Cells", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("sample-prep-exp")));
        lp.add(newProtocol("omni-c-dovetail-mammalian-tissue", "Omni-C Dovetail - Mammalian Tissue", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("sample-prep-exp")));
        lp.add(newProtocol("omni-c-dovetail-mammalian-blood", "Omni-C Dovetail - Mammalian blood", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("sample-prep-exp")));
        
        //NGL-3501 FDS 04/08/2021: code propriété non conforme "prep-hi-c-protocol" => "prepHicProtocol"
        lp.add(newProtocol("dovetail-hi-c", "Dovetail Hi-C", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("hi-c-prep"),concatMap(newPSV("prepHicProtocol", "Dovetail Hi-C"))));
        lp.add(newProtocol("arima-hi-c-2-enzymes", "Arima Hi-C+ (2 enz)", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("hi-c-prep"),concatMap(newPSV("prepHicProtocol", "Arima Hi-C+ (2 enz)"))));
        lp.add(newProtocol("arima-hi-c-4-enzymes", "Arima Hi-C+ (4 enz)", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("hi-c-prep"),concatMap(newPSV("prepHicProtocol", "Arima Hi-C+ (4 enz)"))));
        lp.add(newProtocol("dovetail-omni-c", "Dovetail Omni-C", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("hi-c-prep"),concatMap(newPSV("prepHicProtocol", "Dovetail Omni-C"))));
        lp.add(newProtocol("pore-c", "Pore-C", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("hi-c-prep"),concatMap(newPSV("prepHicProtocol", "Pore-C"))));
        
        lp.add(newProtocol("hi-c-neb-u2", "Hi-C_NEB-U2", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("dna-illumina-indexed-library"),concatMap(newPSV("libraryProtocol", "Bq Hi-C NEB-U2"))));
        lp.add(newProtocol("hi-c-swift-2s", "Hi-C_Swift-2S", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("dna-illumina-indexed-library"),concatMap(newPSV("libraryProtocol", "Bq Hi-C Swift-2S"))));
        lp.add(newProtocol("hi-c-dovetail", "Hi-C_Dovetail", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("dna-illumina-indexed-library"),concatMap(newPSV("libraryProtocol", "Bq Hi-C Dovetail"))));
        lp.add(newProtocol("hi-c-arima", "HiC_Arima", "path2", "1", "production", InstanceFactory.setExperimentTypeCodes("dna-illumina-indexed-library"),concatMap(newPSV("libraryProtocol", "Bq Hi-C Arima"))));
        
        
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
        
        lp.add(newProtocol("circulomics-sre", "Circulomics SRE", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("precipitation-purification"), concatMap(newPSVs("purificationKitVersion", "SRE", "purificationKit", "Circulomics")))); 
        lp.add(newProtocol("circulomics-xs", "Circulomics XS", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("precipitation-purification"), concatMap(newPSVs("purificationKitVersion", "XS", "purificationKit", "Circulomics")))); 
        lp.add(newProtocol("circulomics-xl", "Circulomics XL", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("precipitation-purification"), concatMap(newPSVs("purificationKitVersion", "XL", "purificationKit", "Circulomics")))); 

        // NGL-2905
        lp.add(newProtocol("pcr-sequencing-kit-sqk-psk004", "PCR Sequencing Kit (SQK-PSK004)", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nanopore-end-prep", "nanopore-pre-pcr-ligation", "nanopore-pcr", "nanopore-final-ligation", "nanopore-depot"), concatMap(newPSV("libraryProtocol", "PCR Sequencing Kit (SQK-PSK004)")))); 
        
        //NGL-3166
        lp.add(newProtocol("pcr-cdna-barecoding-sqk-pcb109", "PCR-cDNA Barecoding (SQK-PCB109)", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes( "nanopore-final-ligation", "nanopore-cdna-synthesis","nanopore-depot"), concatMap(newPSV("libraryProtocol", "PCR-cDNA Barecoding (SQK-PCB109)")))); 
        
        // NGL-3374
        lp.add(newProtocol("yeast-bacteria-fastselect-depletion", "Levure & Bactérie FastSelect", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("rrna-depletion"), concatMap(newPSV("depletionMethod", "Levure et Bactérie FastSelect")), false)); 
        lp.add(newProtocol("bacteria-fastselect-depletion", "Bactérie FastSelect", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("rrna-depletion"), concatMap(newPSV("depletionMethod", "Bactérie FastSelect")))); 
        lp.add(newProtocol("bacteria-nebnext-depletion", "Bactérie NEBNext", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("rrna-depletion"), concatMap(newPSV("depletionMethod", "Bactérie NEBNext")))); 
        lp.add(newProtocol("bacteria-ribozero-depletion", "Bactérie Ribozéro", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("rrna-depletion"), concatMap(newPSV("depletionMethod", "Bactérie Ribozéro")))); 
        lp.add(newProtocol("insect-fastselect-depletion", "Insecte FastSelect", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("rrna-depletion"), concatMap(newPSV("depletionMethod", "Insecte FastSelect")))); 
        lp.add(newProtocol("plant-fastselect-depletion", "Plante FastSelect", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("rrna-depletion"), concatMap(newPSV("depletionMethod", "Plante FastSelect")))); 
        lp.add(newProtocol("nuclease-tex-depletion", "Nuclease TEX", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("rrna-depletion"), concatMap(newPSV("depletionMethod", "Nuclease TEX")))); 
        lp.add(newProtocol("dev-ill-bqarn-ribodeplet-bact-lev-qiagen-173-v1", "Dev_ILL_BqARN_RiboDeplet-Bact-Lev-QIAGEN_FastSelect_173_v1", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("rrna-depletion"), concatMap(newPSV("depletionMethod", "Levure et Bactérie FastSelect"))));
        // NGL-4276
        lp.add(newProtocol("ribozero-plus-microbiome-depletion", "RiboZero Plus Microbiome", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("rrna-depletion"), concatMap(newPSV("depletionMethod", "RiboZero Plus Microbiome"))));
        
        // NGL-2840 - Gestion du Femto Pulse.
        lp.add(newProtocol("gdna-165kb-femto-pulse-protocol", "gDNA 165 kb", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("chip-migration"))); 
        lp.add(newProtocol("extented-gdna-165kb-femto-pulse-protocol", "Extended gDNA 165 kb", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("chip-migration"))); 
        
        // NGL-3762 puis suppression/renommage NGL-3821
       // lp.add(newProtocol("dev-ill-bqarn-ribodeplet-bact-insect-qiagen-fastselect-v1", "Dev_ILL_BqARN_RiboDeplet-Bact-Insect_QIAGEN_FastSelect_v1", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("rrna-depletion"), concatMap(newPSV("depletionMethod", "Insecte et Bactérie FastSelect")))); 
        
        //NGL-3821
        lp.add(newProtocol("dev-ill-bqarn-ribodeplet-bact-insect-qiagen-fastselect-174-v1", "Dev_ILL_BqARN_RiboDeplet-Bact-Insect-QIAGEN_FastSelect_174_v1", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("rrna-depletion"), concatMap(newPSV("depletionMethod", "Insecte et Bactérie FastSelect")))); 
        
        
        /* EPGV */
        //TODO a supprimer doublon avec protocole : protocole_epgv (nécessite une reprise histo) => en attente du devenir de l'équipe EPGV au sein du CNS
        lp.add(newProtocol("protocole-epgv",    "Protocole EPGV",    "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("post-pcr-ampure", "fluo-quantification", "prepa-fc-ordered", "prepa-flowcell", "normalisation","pcr-amplification-and-purification"))); 
        lp.add(newProtocol("pcr-free-350-epgv", "PCR_free_350_EPGV", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("dna-illumina-indexed-library", "fragmentation"))); 
        lp.add(newProtocol("pcr-free-550-epgv", "PCR_free_550_EPGV", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("dna-illumina-indexed-library", "fragmentation"))); 
        lp.add(newProtocol("protocole-epgv-nebNext-direct-genotyping", "Protocole_EPGV_NebNext_Direct_Genotyping", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("dna-illumina-indexed-library")));
       
        
        //Protocol MGI
        //NGL-3555 Cacher fonctionnalite MGI // NGL-3556 Reactivation MGI
        lp.add(newProtocol("dnbseq-g400-high-throughput-seq","DNBSEQ-G400RS HotMPS High-throughput Sequencing","path7", "1","production", InstanceFactory.setExperimentTypeCodes("mgi-sscircularisation","mgi-nanoballs","mgi-prepa-fc","mgi-depot"))); 
        lp.add(newProtocol("ptr_pool_mgi","POOL MGI","path7", "1", "production", InstanceFactory.setExperimentTypeCodes("mgi-pool")));
        lp.add(newProtocol("annexe-ssdna-mgi-v1","Annexe_ssDNA_MGI_v1","path7", "1", "production", InstanceFactory.setExperimentTypeCodes("fluo-quantification")));
        
        //PacBio
        lp.add(newProtocol("hifi-wg-or-metag-pacbio-library", "HIFI - Whole Genome and Metagenome library", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("fragmentation","dna-pacbio-library"), concatMap(newPSV("libraryProtocol", "HIFI - Whole Genome and Metagenome library")))); 
        lp.add(newProtocol("pacbio-smrt-cell-prep-and-sequencing", "PacBio SMRT Cell prep and sequencing", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("prep-smrt-cell","pacbio-depot"))); 
        lp.add(newProtocol("nuclease-treatment", "Traitement Nucléase", "path7", "1", "production", InstanceFactory.setExperimentTypeCodes("nuclease-treatment"))); 
        

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

